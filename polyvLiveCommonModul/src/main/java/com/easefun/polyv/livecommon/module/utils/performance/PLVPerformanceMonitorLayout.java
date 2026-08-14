package com.easefun.polyv.livecommon.module.utils.performance;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 开播页性能监控浮层，用于定位长时间超清 RTMP/SRT 直推时的发热、内存和网络状态。
 */
public class PLVPerformanceMonitorLayout extends TextView {

    private static final String TAG = "PLVPerformanceMonitor";
    private static final long SAMPLE_INTERVAL_MS = 2000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ActivityManager activityManager;
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refresh();
            mainHandler.postDelayed(this, SAMPLE_INTERVAL_MS);
        }
    };

    private boolean released;
    private boolean compactMode;
    @Nullable
    private CpuSnapshot lastCpuSnapshot;
    private long lastTxBytes = TrafficStats.UNSUPPORTED;
    private long lastRxBytes = TrafficStats.UNSUPPORTED;
    private long lastTrafficTimeMs;

    public PLVPerformanceMonitorLayout(@NonNull Context context) {
        super(context);
        activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        initView();
    }

    @NonNull
    public static PLVPerformanceMonitorLayout attachToActivity(@NonNull Activity activity) {
        final PLVPerformanceMonitorLayout monitorLayout = new PLVPerformanceMonitorLayout(activity);
        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.leftMargin = dp(activity, 8);
        lp.topMargin = dp(activity, 48);
        activity.addContentView(monitorLayout, lp);
        monitorLayout.bringToFront();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            monitorLayout.setElevation(dp(activity, 100));
        }
        monitorLayout.start();
        return monitorLayout;
    }

    public void start() {
        released = false;
        resetStats();
        mainHandler.removeCallbacks(refreshRunnable);
        refreshRunnable.run();
    }

    public void release() {
        released = true;
        mainHandler.removeCallbacks(refreshRunnable);
        final ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mainHandler.removeCallbacks(refreshRunnable);
    }

    private void initView() {
        setTextColor(Color.WHITE);
        setTextSize(10);
        setTypeface(Typeface.MONOSPACE);
        setGravity(Gravity.START);
        setPadding(dp(getContext(), 8), dp(getContext(), 6), dp(getContext(), 8), dp(getContext(), 6));
        setBackgroundColor(0x99000000);
        setMaxLines(12);
        setSingleLine(false);
        setIncludeFontPadding(false);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                compactMode = !compactMode;
                refresh();
            }
        });
    }

    private void resetStats() {
        lastCpuSnapshot = new CpuSnapshot(SystemClock.elapsedRealtime(), Process.getElapsedCpuTime());
        lastTrafficTimeMs = System.currentTimeMillis();
        lastTxBytes = TrafficStats.getUidTxBytes(Process.myUid());
        lastRxBytes = TrafficStats.getUidRxBytes(Process.myUid());
    }

    private void refresh() {
        if (released) {
            return;
        }
        final CpuInfo cpuInfo = readCpuInfo();
        final MemoryInfo memoryInfo = readMemoryInfo();
        final NetworkInfo networkInfo = readNetworkInfo();
        final BatteryInfo batteryInfo = readBatteryInfo();
        final ThermalInfo thermalInfo = readThermalInfo();
        final CpuFreqInfo cpuFreqInfo = readCpuFreqInfo();
        final int threadCount = readThreadCount();

        logPerformance(cpuInfo, memoryInfo, networkInfo, batteryInfo, thermalInfo, cpuFreqInfo, threadCount);

        if (compactMode) {
            setText(String.format(Locale.US,
                    "CPU %.0f%%/%.0f%%  PSS %dM  RSS %dM  可用 %dM  热 %s",
                    cpuInfo.processCpuTotalPercent,
                    cpuInfo.processCpuAvgPercent,
                    memoryInfo.totalPssMb,
                    memoryInfo.rssMb,
                    memoryInfo.systemAvailMb,
                    thermalInfo.maxTemperatureText
            ));
        } else {
            setText(String.format(Locale.US,
                    "性能监控  CPU %.0f%%/%.0f%%  线程 %d\nApp PSS %dM(D%d/N%d/O%d)  RSS %dM\nJava %d/%dM  NativeHeap %dM\n系统可用 %d/%dM  阈值 %dM  %s\nNet ↑%s/s ↓%s/s\n电池 %d%% %.1f℃ %dmV  %s\n热状态 %s  Max %s\n%s\n%s",
                    cpuInfo.processCpuTotalPercent,
                    cpuInfo.processCpuAvgPercent,
                    threadCount,
                    memoryInfo.totalPssMb,
                    memoryInfo.dalvikPssMb,
                    memoryInfo.nativePssMb,
                    memoryInfo.otherPssMb,
                    memoryInfo.rssMb,
                    memoryInfo.javaUsedMb,
                    memoryInfo.javaMaxMb,
                    memoryInfo.nativeUsedMb,
                    memoryInfo.systemAvailMb,
                    memoryInfo.systemTotalMb,
                    memoryInfo.systemThresholdMb,
                    memoryInfo.lowMemory ? "低内存" : "正常",
                    formatBytes(networkInfo.txBytesPerSecond),
                    formatBytes(networkInfo.rxBytesPerSecond),
                    batteryInfo.level,
                    batteryInfo.temperature,
                    batteryInfo.voltage,
                    batteryInfo.powerText,
                    thermalInfo.thermalStatusText,
                    thermalInfo.maxTemperatureText,
                    thermalInfo.detailText,
                    cpuFreqInfo.displayText
            ));
        }
    }

    private void logPerformance(@NonNull CpuInfo cpuInfo,
                                @NonNull MemoryInfo memoryInfo,
                                @NonNull NetworkInfo networkInfo,
                                @NonNull BatteryInfo batteryInfo,
                                @NonNull ThermalInfo thermalInfo,
                                @NonNull CpuFreqInfo cpuFreqInfo,
                                int threadCount) {
        Log.d(TAG, String.format(Locale.US,
                "sample cpuTotal=%.1f%% cpuAvg=%.1f%% cpuCores=%d threads=%d "
                        + "pss=%dMB dalvikPss=%dMB nativePss=%dMB otherPss=%dMB rss=%dMB java=%d/%dMB nativeHeap=%dMB "
                        + "sysAvail=%dMB sysTotal=%dMB sysThreshold=%dMB lowMemory=%s "
                        + "netTx=%s/s netRx=%s/s trafficSupported=%s "
                        + "batteryLevel=%d%% batteryTemp=%.1fC batteryVoltage=%dmV power=%s "
                        + "thermalStatus=%s thermalSource=%s thermalMax=%s thermalZoneCount=%d thermalDetail=%s "
                        + "cpuFreqAvailable=%s cpuFreq=%s",
                cpuInfo.processCpuTotalPercent,
                cpuInfo.processCpuAvgPercent,
                cpuInfo.coreCount,
                threadCount,
                memoryInfo.totalPssMb,
                memoryInfo.dalvikPssMb,
                memoryInfo.nativePssMb,
                memoryInfo.otherPssMb,
                memoryInfo.rssMb,
                memoryInfo.javaUsedMb,
                memoryInfo.javaMaxMb,
                memoryInfo.nativeUsedMb,
                memoryInfo.systemAvailMb,
                memoryInfo.systemTotalMb,
                memoryInfo.systemThresholdMb,
                memoryInfo.lowMemory,
                formatBytes(networkInfo.txBytesPerSecond),
                formatBytes(networkInfo.rxBytesPerSecond),
                networkInfo.supported,
                batteryInfo.level,
                batteryInfo.temperature,
                batteryInfo.voltage,
                batteryInfo.powerText,
                thermalInfo.thermalStatusText,
                thermalInfo.sourceText,
                thermalInfo.maxTemperatureText,
                thermalInfo.zoneCount,
                thermalInfo.detailText,
                cpuFreqInfo.available,
                cpuFreqInfo.displayText
        ));
    }

    @NonNull
    private CpuInfo readCpuInfo() {
        final CpuSnapshot current = new CpuSnapshot(SystemClock.elapsedRealtime(), Process.getElapsedCpuTime());
        if (lastCpuSnapshot == null) {
            lastCpuSnapshot = current;
            return new CpuInfo(0);
        }
        final long processDelta = current.processCpuTimeMs - lastCpuSnapshot.processCpuTimeMs;
        final long totalDelta = current.elapsedRealtimeMs - lastCpuSnapshot.elapsedRealtimeMs;
        lastCpuSnapshot = current;
        if (processDelta <= 0 || totalDelta <= 0) {
            return new CpuInfo(0);
        }
        final int coreCount = Math.max(1, Runtime.getRuntime().availableProcessors());
        final float processCpuTotalPercent = processDelta * 100F / totalDelta;
        return new CpuInfo(processCpuTotalPercent, processCpuTotalPercent / coreCount, coreCount);
    }

    @NonNull
    private MemoryInfo readMemoryInfo() {
        final Runtime runtime = Runtime.getRuntime();
        final long javaUsedMb = bytesToMb(runtime.totalMemory() - runtime.freeMemory());
        final long javaMaxMb = bytesToMb(runtime.maxMemory());
        final long nativeUsedMb = bytesToMb(Debug.getNativeHeapAllocatedSize());
        final Debug.MemoryInfo processMemoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(processMemoryInfo);
        int totalPssMb = processMemoryInfo.getTotalPss() / 1024;
        int dalvikPssMb = processMemoryInfo.dalvikPss / 1024;
        int nativePssMb = processMemoryInfo.nativePss / 1024;
        int otherPssMb = processMemoryInfo.otherPss / 1024;
        final long rssMb = readRssMb();
        long systemAvailMb = 0;
        long systemTotalMb = 0;
        long systemThresholdMb = 0;
        boolean lowMemory = false;
        if (activityManager != null) {
            final Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(new int[]{Process.myPid()});
            if (totalPssMb <= 0 && memoryInfos != null && memoryInfos.length > 0) {
                totalPssMb = memoryInfos[0].getTotalPss() / 1024;
                dalvikPssMb = memoryInfos[0].dalvikPss / 1024;
                nativePssMb = memoryInfos[0].nativePss / 1024;
                otherPssMb = memoryInfos[0].otherPss / 1024;
            }
            final ActivityManager.MemoryInfo systemMemoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(systemMemoryInfo);
            systemAvailMb = bytesToMb(systemMemoryInfo.availMem);
            systemTotalMb = bytesToMb(systemMemoryInfo.totalMem);
            systemThresholdMb = bytesToMb(systemMemoryInfo.threshold);
            lowMemory = systemMemoryInfo.lowMemory;
        }
        return new MemoryInfo(javaUsedMb, javaMaxMb, nativeUsedMb, totalPssMb,
                dalvikPssMb, nativePssMb, otherPssMb,
                rssMb, systemAvailMb, systemTotalMb, systemThresholdMb, lowMemory);
    }

    @NonNull
    private NetworkInfo readNetworkInfo() {
        final long now = System.currentTimeMillis();
        final long txBytes = TrafficStats.getUidTxBytes(Process.myUid());
        final long rxBytes = TrafficStats.getUidRxBytes(Process.myUid());
        long txBytesPerSecond = 0;
        long rxBytesPerSecond = 0;
        final long intervalMs = Math.max(1, now - lastTrafficTimeMs);
        if (lastTxBytes != TrafficStats.UNSUPPORTED && txBytes != TrafficStats.UNSUPPORTED) {
            txBytesPerSecond = Math.max(0, (txBytes - lastTxBytes) * 1000L / intervalMs);
        }
        if (lastRxBytes != TrafficStats.UNSUPPORTED && rxBytes != TrafficStats.UNSUPPORTED) {
            rxBytesPerSecond = Math.max(0, (rxBytes - lastRxBytes) * 1000L / intervalMs);
        }
        lastTxBytes = txBytes;
        lastRxBytes = rxBytes;
        lastTrafficTimeMs = now;
        return new NetworkInfo(txBytesPerSecond, rxBytesPerSecond,
                txBytes != TrafficStats.UNSUPPORTED && rxBytes != TrafficStats.UNSUPPORTED);
    }

    @NonNull
    private BatteryInfo readBatteryInfo() {
        final Intent batteryIntent = getContext().getApplicationContext().registerReceiver(
                null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
        if (batteryIntent == null) {
            return new BatteryInfo(0, 0, 0, "供电 --");
        }
        final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        final int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        final int voltage = normalizeBatteryVoltage(batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0));
        final int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        final int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        final int percent = scale <= 0 ? level : level * 100 / scale;
        final String powerText;
        if ((plugged & BatteryManager.BATTERY_PLUGGED_AC) != 0) {
            powerText = "AC供电";
        } else if ((plugged & BatteryManager.BATTERY_PLUGGED_USB) != 0) {
            powerText = "USB供电";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && (plugged & BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0) {
            powerText = "无线供电";
        } else {
            powerText = "未充电";
        }
        return new BatteryInfo(percent, temperature / 10F, voltage, powerText);
    }

    @NonNull
    private ThermalInfo readThermalInfo() {
        final String thermalStatusText = readThermalStatusText();
        final List<ThermalZoneInfo> zones = readThermalZones();
        if (zones.isEmpty()) {
            return new ThermalInfo(thermalStatusText, "--", "--", "温度 --", 0);
        }
        Collections.sort(zones, new Comparator<ThermalZoneInfo>() {
            @Override
            public int compare(ThermalZoneInfo o1, ThermalZoneInfo o2) {
                return Float.compare(o2.temperature, o1.temperature);
            }
        });

        final StringBuilder detailBuilder = new StringBuilder("温度 ");
        final List<ThermalZoneInfo> matchedZones = pickImportantThermalZones(zones);
        for (int i = 0; i < matchedZones.size(); i++) {
            if (i > 0) {
                detailBuilder.append("  ");
            }
            final ThermalZoneInfo zone = matchedZones.get(i);
            detailBuilder.append(zone.displayName)
                    .append(" ")
                    .append(String.format(Locale.US, "%.1f℃", zone.temperature));
        }
        return new ThermalInfo(thermalStatusText,
                zones.get(0).source,
                String.format(Locale.US, "%s %.1f℃", zones.get(0).displayName, zones.get(0).temperature),
                detailBuilder.toString(),
                zones.size());
    }

    @NonNull
    private String readThermalStatusText() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return "--";
        }
        final PowerManager powerManager = (PowerManager) getContext().getApplicationContext().getSystemService(Context.POWER_SERVICE);
        return powerManager == null ? "--" : formatThermalStatus(powerManager.getCurrentThermalStatus());
    }

    @NonNull
    private List<ThermalZoneInfo> readThermalZones() {
        final List<ThermalZoneInfo> result = new ArrayList<>();
        readThermalZoneFiles(result);
        readHwmonFiles(result);
        return result;
    }

    private void readThermalZoneFiles(@NonNull List<ThermalZoneInfo> result) {
        final File thermalDir = new File("/sys/class/thermal");
        final File[] files = thermalDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file == null || !file.getName().startsWith("thermal_zone")) {
                continue;
            }
            final String type = readFirstLine(new File(file, "type"));
            final String tempText = readFirstLine(new File(file, "temp"));
            final Float temperature = parseTemperature(tempText);
            if (temperature == null) {
                continue;
            }
            result.add(new ThermalZoneInfo(type, formatThermalType(type), temperature, "thermal"));
        }
    }

    private void readHwmonFiles(@NonNull List<ThermalZoneInfo> result) {
        final File hwmonDir = new File("/sys/class/hwmon");
        final File[] files = hwmonDir.listFiles();
        if (files == null) {
            return;
        }
        for (File hwmon : files) {
            if (hwmon == null || !hwmon.getName().startsWith("hwmon")) {
                continue;
            }
            final String hwmonName = readFirstLine(new File(hwmon, "name"));
            final File[] tempFiles = hwmon.listFiles();
            if (tempFiles == null) {
                continue;
            }
            for (File tempFile : tempFiles) {
                if (tempFile == null
                        || !tempFile.getName().startsWith("temp")
                        || !tempFile.getName().endsWith("_input")) {
                    continue;
                }
                final String index = tempFile.getName().substring("temp".length(), tempFile.getName().length() - "_input".length());
                String type = readFirstLine(new File(hwmon, "temp" + index + "_label"));
                if (TextUtils.isEmpty(type)) {
                    type = TextUtils.isEmpty(hwmonName) ? hwmon.getName() + "_" + tempFile.getName() : hwmonName + "_" + tempFile.getName();
                }
                final Float temperature = parseTemperature(readFirstLine(tempFile));
                if (temperature == null) {
                    continue;
                }
                result.add(new ThermalZoneInfo(type, formatThermalType(type), temperature, "hwmon"));
            }
        }
    }

    @NonNull
    private List<ThermalZoneInfo> pickImportantThermalZones(@NonNull List<ThermalZoneInfo> zones) {
        final List<ThermalZoneInfo> result = new ArrayList<>();
        addFirstMatchedZone(result, zones, "CPU", "cpu", "ap", "soc");
        addFirstMatchedZone(result, zones, "GPU", "gpu");
        addFirstMatchedZone(result, zones, "Video", "video", "vcodec", "codec", "venc", "vdec");
        addFirstMatchedZone(result, zones, "Camera", "camera", "cam");
        addFirstMatchedZone(result, zones, "DDR", "ddr", "mem");
        addFirstMatchedZone(result, zones, "Modem", "modem", "mdm", "pa");
        addFirstMatchedZone(result, zones, "WiFi", "wifi", "wlan");
        addFirstMatchedZone(result, zones, "Battery", "battery", "bat");
        for (ThermalZoneInfo zone : zones) {
            if (result.size() >= 8) {
                break;
            }
            if (!containsThermalZone(result, zone)) {
                result.add(zone);
            }
        }
        return result;
    }

    private void addFirstMatchedZone(@NonNull List<ThermalZoneInfo> result,
                                     @NonNull List<ThermalZoneInfo> zones,
                                     @NonNull String displayName,
                                     @NonNull String... keys) {
        if (result.size() >= 8) {
            return;
        }
        for (ThermalZoneInfo zone : zones) {
            if (containsThermalZone(result, zone)) {
                continue;
            }
            final String type = zone.type == null ? "" : zone.type.toLowerCase(Locale.US);
            for (String key : keys) {
                if (type.contains(key)) {
                    result.add(new ThermalZoneInfo(zone.type, displayName, zone.temperature, zone.source));
                    return;
                }
            }
        }
    }

    private boolean containsThermalZone(@NonNull List<ThermalZoneInfo> zones, @NonNull ThermalZoneInfo target) {
        for (ThermalZoneInfo zone : zones) {
            if (TextUtils.equals(zone.type, target.type)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private CpuFreqInfo readCpuFreqInfo() {
        final StringBuilder builder = new StringBuilder("CPU频率 ");
        boolean hasFreq = false;
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            final File freqFile = new File("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
            final String freqText = readFirstLine(freqFile);
            final long freqKhz = parseLong(freqText);
            if (freqKhz <= 0) {
                continue;
            }
            if (hasFreq) {
                builder.append("  ");
            }
            hasFreq = true;
            builder.append("c").append(i).append(" ").append(freqKhz / 1000).append("MHz");
        }
        return new CpuFreqInfo(hasFreq, hasFreq ? builder.toString() : "CPU频率 --");
    }

    private long readRssMb() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/self/status"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("VmRSS:")) {
                    final String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return parseLong(parts[1]) / 1024;
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            closeQuietly(reader);
        }
        return 0;
    }

    private int readThreadCount() {
        final File taskDir = new File("/proc/self/task");
        final String[] threads = taskDir.list();
        return threads == null ? 0 : threads.length;
    }

    @NonNull
    private String formatThermalStatus(int status) {
        switch (status) {
            case PowerManager.THERMAL_STATUS_NONE:
                return "正常";
            case PowerManager.THERMAL_STATUS_LIGHT:
                return "轻微";
            case PowerManager.THERMAL_STATUS_MODERATE:
                return "中等";
            case PowerManager.THERMAL_STATUS_SEVERE:
                return "严重";
            case PowerManager.THERMAL_STATUS_CRITICAL:
                return "临界";
            case PowerManager.THERMAL_STATUS_EMERGENCY:
                return "紧急";
            case PowerManager.THERMAL_STATUS_SHUTDOWN:
                return "关机风险";
            default:
                return String.valueOf(status);
        }
    }

    @NonNull
    private String formatBytes(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format(Locale.US, "%.1fMB", bytes / 1024F / 1024F);
        }
        if (bytes >= 1024) {
            return String.format(Locale.US, "%.0fKB", bytes / 1024F);
        }
        return bytes + "B";
    }

    private long bytesToMb(long bytes) {
        return bytes / 1024 / 1024;
    }

    private int normalizeBatteryVoltage(int voltage) {
        if (voltage > 0 && voltage < 100) {
            return voltage * 1000;
        }
        return voltage;
    }

    @Nullable
    private String readFirstLine(@NonNull File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return reader.readLine();
        } catch (Exception ignored) {
            return null;
        } finally {
            closeQuietly(reader);
        }
    }

    @Nullable
    private Float parseTemperature(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            final float temperature = Float.parseFloat(value.trim());
            if (temperature > 1000) {
                return temperature / 1000F;
            }
            if (temperature > 150) {
                return temperature / 10F;
            }
            return temperature;
        } catch (Exception e) {
            return null;
        }
    }

    private long parseLong(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    private String formatThermalType(@Nullable String type) {
        if (TextUtils.isEmpty(type)) {
            return "Zone";
        }
        final String lowerType = type.toLowerCase(Locale.US);
        if (lowerType.contains("cpu") || lowerType.contains("soc") || lowerType.contains("ap")) {
            return "CPU";
        } else if (lowerType.contains("gpu")) {
            return "GPU";
        } else if (lowerType.contains("video") || lowerType.contains("vcodec") || lowerType.contains("codec")) {
            return "Video";
        } else if (lowerType.contains("camera") || lowerType.contains("cam")) {
            return "Camera";
        } else if (lowerType.contains("ddr") || lowerType.contains("mem")) {
            return "DDR";
        } else if (lowerType.contains("modem") || lowerType.contains("mdm")) {
            return "Modem";
        } else if (lowerType.contains("wifi") || lowerType.contains("wlan")) {
            return "WiFi";
        } else if (lowerType.contains("battery") || lowerType.contains("bat")) {
            return "Battery";
        }
        return type.length() > 10 ? type.substring(0, 10) : type;
    }

    private static int dp(@NonNull Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5F);
    }

    private static void closeQuietly(@Nullable BufferedReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

    private static class CpuSnapshot {
        private final long elapsedRealtimeMs;
        private final long processCpuTimeMs;

        private CpuSnapshot(long elapsedRealtimeMs, long processCpuTimeMs) {
            this.elapsedRealtimeMs = elapsedRealtimeMs;
            this.processCpuTimeMs = processCpuTimeMs;
        }
    }

    private static class CpuInfo {
        private final float processCpuTotalPercent;
        private final float processCpuAvgPercent;
        private final int coreCount;

        private CpuInfo(float processCpuTotalPercent) {
            this(processCpuTotalPercent, 0, Math.max(1, Runtime.getRuntime().availableProcessors()));
        }

        private CpuInfo(float processCpuTotalPercent, float processCpuAvgPercent, int coreCount) {
            this.processCpuTotalPercent = processCpuTotalPercent;
            this.processCpuAvgPercent = processCpuAvgPercent;
            this.coreCount = coreCount;
        }
    }

    private static class MemoryInfo {
        private final long javaUsedMb;
        private final long javaMaxMb;
        private final long nativeUsedMb;
        private final int totalPssMb;
        private final int dalvikPssMb;
        private final int nativePssMb;
        private final int otherPssMb;
        private final long rssMb;
        private final long systemAvailMb;
        private final long systemTotalMb;
        private final long systemThresholdMb;
        private final boolean lowMemory;

        private MemoryInfo(long javaUsedMb, long javaMaxMb, long nativeUsedMb, int totalPssMb,
                           int dalvikPssMb, int nativePssMb, int otherPssMb,
                           long rssMb, long systemAvailMb, long systemTotalMb,
                           long systemThresholdMb, boolean lowMemory) {
            this.javaUsedMb = javaUsedMb;
            this.javaMaxMb = javaMaxMb;
            this.nativeUsedMb = nativeUsedMb;
            this.totalPssMb = totalPssMb;
            this.dalvikPssMb = dalvikPssMb;
            this.nativePssMb = nativePssMb;
            this.otherPssMb = otherPssMb;
            this.rssMb = rssMb;
            this.systemAvailMb = systemAvailMb;
            this.systemTotalMb = systemTotalMb;
            this.systemThresholdMb = systemThresholdMb;
            this.lowMemory = lowMemory;
        }
    }

    private static class NetworkInfo {
        private final long txBytesPerSecond;
        private final long rxBytesPerSecond;
        private final boolean supported;

        private NetworkInfo(long txBytesPerSecond, long rxBytesPerSecond, boolean supported) {
            this.txBytesPerSecond = txBytesPerSecond;
            this.rxBytesPerSecond = rxBytesPerSecond;
            this.supported = supported;
        }
    }

    private static class BatteryInfo {
        private final int level;
        private final float temperature;
        private final int voltage;
        private final String powerText;

        private BatteryInfo(int level, float temperature, int voltage, String powerText) {
            this.level = level;
            this.temperature = temperature;
            this.voltage = voltage;
            this.powerText = powerText;
        }
    }

    private static class ThermalInfo {
        private final String thermalStatusText;
        private final String sourceText;
        private final String maxTemperatureText;
        private final String detailText;
        private final int zoneCount;

        private ThermalInfo(String thermalStatusText, String sourceText, String maxTemperatureText, String detailText, int zoneCount) {
            this.thermalStatusText = thermalStatusText;
            this.sourceText = sourceText;
            this.maxTemperatureText = maxTemperatureText;
            this.detailText = detailText;
            this.zoneCount = zoneCount;
        }
    }

    private static class CpuFreqInfo {
        private final boolean available;
        private final String displayText;

        private CpuFreqInfo(boolean available, String displayText) {
            this.available = available;
            this.displayText = displayText;
        }
    }

    private static class ThermalZoneInfo {
        private final String type;
        private final String displayName;
        private final float temperature;
        private final String source;

        private ThermalZoneInfo(String type, String displayName, float temperature, String source) {
            this.type = type;
            this.displayName = displayName;
            this.temperature = temperature;
            this.source = source;
        }
    }
}
