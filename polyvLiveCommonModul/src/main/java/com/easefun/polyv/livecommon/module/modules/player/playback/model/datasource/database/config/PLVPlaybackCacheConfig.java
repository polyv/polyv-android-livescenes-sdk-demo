package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.plv.foundationsdk.log.PLVCommonLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheConfig {

    private static final String TAG = PLVPlaybackCacheConfig.class.getSimpleName();

    private Context applicationContext;
    private String databaseName;
    private File downloadRootDirectory;

    public Context getApplicationContext() {
        return applicationContext;
    }

    public PLVPlaybackCacheConfig setApplicationContext(Context applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public PLVPlaybackCacheConfig setDatabaseNameByViewerId(String viewerId) {
        this.databaseName = "playback_cache_" + viewerId + ".db";
        return this;
    }

    public File getDownloadRootDirectory() {
        return downloadRootDirectory;
    }

    public PLVPlaybackCacheConfig setDownloadRootDirectory(File downloadRootDirectory) {
        this.downloadRootDirectory = downloadRootDirectory;
        PLVCommonLog.i("PLVPlaybackCacheConfig", "setDownloadRootDirectory: " + downloadRootDirectory.getAbsolutePath());
        return this;
    }

    public static String defaultPlaybackCacheDownloadDirectory(@NonNull final Context context) {
        final File[] files;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //列表中包含了可移除的存储介质（例如 SD 卡）的路径。
            files = context.getExternalFilesDirs(null);
        } else {
            files = ContextCompat.getExternalFilesDirs(context, null);
        }

        ArrayList<File> storageList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //判断存储设备可用性
            for (File file : files) {
                if (file != null) {
                    String state = Environment.getExternalStorageState(file);
                    if ("mounted".equals(state)) {
                        storageList.add(file);
                    }
                }
            }
        } else {
            storageList.addAll(Arrays.asList(files));
        }

        if (storageList.isEmpty()) {
            PLVCommonLog.e(TAG, "没有可用的存储设备,后续不能使用视频缓存功能");
            return "";
        } else {
            return storageList.get(0).getAbsolutePath() + File.separator + "playback_cache";
        }
    }
}
