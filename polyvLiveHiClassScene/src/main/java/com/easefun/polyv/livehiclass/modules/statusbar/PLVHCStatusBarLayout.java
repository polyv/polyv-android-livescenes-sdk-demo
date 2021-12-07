package com.easefun.polyv.livehiclass.modules.statusbar;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livehiclass.R;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.hiclass.PLVHiClassDataBean;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

/**
 * 状态栏布局
 */
public class PLVHCStatusBarLayout extends FrameLayout implements IPLVHCStatusBarLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final int MAX_LENGTH_LESSON_NAME = 12;

    private View rootView;
    private LinearLayout plvhcStatusLessonInfoLayout;
    private TextView plvhcStatusLessonCodeTv;
    private TextView plvhcStatusLessonClassTimeTv;
    private LinearLayout plvhcStatusLessonNetworkSpeedLayout;
    private ImageView plvhcStatusLessonNetworkIv;
    private TextView plvhcStatusLessonLatencyTv;
    private TextView plvhcStatusLessonNameTv;

    private final Map<Integer, Integer> linkMicQualityResMap = mapOf(
            pair(PLVLinkMicConstant.NetQuality.NET_QUALITY_NO_CONNECTION, R.drawable.plvhc_network_signal_0),
            pair(PLVLinkMicConstant.NetQuality.NET_QUALITY_POOR, R.drawable.plvhc_network_signal_1),
            pair(PLVLinkMicConstant.NetQuality.NET_QUALITY_MIDDLE, R.drawable.plvhc_network_signal_2),
            pair(PLVLinkMicConstant.NetQuality.NET_QUALITY_GOOD, R.drawable.plvhc_network_signal_3)
    );

    private IPLVLiveRoomDataManager liveRoomDataManager;
    private Observer<PLVStatefulData<PLVHiClassDataBean>> hiClassDataBeanObserver;

    private long lessonPresetStartTime;
    private long lessonRealStartTime;
    private long lessonLocalStartTime;
    private long lessonPresetEndTime;
    private boolean lessonStart = false;
    private boolean lessonEnd = false;

    private String saveLessonName = "";

    private PLVNetworkStatusVO upstreamNetworkStatus;

    private Disposable statusUpdateDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">

    public PLVHCStatusBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCStatusBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCStatusBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_status_layout, this);
        findView();
        initStatusUpdateTimer();
    }

    private void findView() {
        plvhcStatusLessonInfoLayout = (LinearLayout) rootView.findViewById(R.id.plvhc_status_lesson_info_layout);
        plvhcStatusLessonCodeTv = (TextView) rootView.findViewById(R.id.plvhc_status_lesson_code_tv);
        plvhcStatusLessonClassTimeTv = (TextView) rootView.findViewById(R.id.plvhc_status_lesson_class_time_tv);
        plvhcStatusLessonNetworkSpeedLayout = (LinearLayout) rootView.findViewById(R.id.plvhc_status_lesson_network_speed_layout);
        plvhcStatusLessonNetworkIv = (ImageView) rootView.findViewById(R.id.plvhc_status_lesson_network_iv);
        plvhcStatusLessonLatencyTv = (TextView) rootView.findViewById(R.id.plvhc_status_lesson_latency_tv);
        plvhcStatusLessonNameTv = (TextView) rootView.findViewById(R.id.plvhc_status_lesson_name_tv);
    }

    private void initStatusUpdateTimer() {
        statusUpdateDisposable = PLVRxTimer.timer(1000, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                processUpdateLessonTimeStatus();
                processUpdateNetworkDelay();
                adjustLessonNameTvWidth();
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法 - 外部调用一次">

    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        initLessonIdText(liveRoomDataManager);
        observeLessonDataBean(liveRoomDataManager);
    }

    private void initLessonIdText(IPLVLiveRoomDataManager liveRoomDataManager) {
        plvhcStatusLessonCodeTv.setText("课节号 " + liveRoomDataManager.getConfig().getHiClassConfig().getLessonId());
    }

    private void observeLessonDataBean(IPLVLiveRoomDataManager liveRoomDataManager) {
        liveRoomDataManager.getFulHiClassDataBean().observe((LifecycleOwner) getContext(),
                hiClassDataBeanObserver = new Observer<PLVStatefulData<PLVHiClassDataBean>>() {
                    @Override
                    public void onChanged(@Nullable final PLVStatefulData<PLVHiClassDataBean> hiClassDataBean) {
                        if (hiClassDataBean != null
                                && hiClassDataBean.isSuccess()
                                && hiClassDataBean.getData() != null) {
                            if (hiClassDataBean.getData().getLessonStartTime() != null) {
                                lessonPresetStartTime = hiClassDataBean.getData().getLessonStartTime();
                            }
                            if (hiClassDataBean.getData().getLessonEndTime() != null) {
                                lessonPresetEndTime = hiClassDataBean.getData().getLessonEndTime();
                            }
                            if (hiClassDataBean.getData().getInClassTime() != null
                                    && hiClassDataBean.getData().getServerTime() != null) {
                                final long inClassTimeInSec = hiClassDataBean.getData().getInClassTime();
                                final long serverTimeInMs = hiClassDataBean.getData().getServerTime();
                                lessonRealStartTime = serverTimeInMs - inClassTimeInSec * 1000;
                            }
                            if (hiClassDataBean.getData().getName() != null) {
                                saveLessonName = hiClassDataBean.getData().getName();
                                setLessonName(hiClassDataBean.getData().getName());
                            }
                        }
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    @Override
    public void onLessonStart() {
        if (liveRoomDataManager != null) {
            liveRoomDataManager.requestLessonDetail();
        }
        lessonStart = true;
        lessonEnd = false;
        lessonLocalStartTime = System.currentTimeMillis();
    }

    @Override
    public void onLessonEnd() {
        lessonEnd = true;
    }

    @Override
    public void onJoinDiscuss(String groupId, String groupName) {
        setLessonName(saveLessonName + "-" + groupName);
    }

    @Override
    public void onLeaveDiscuss() {
        setLessonName(saveLessonName);
    }

    @Override
    public void acceptNetworkQuality(int networkQuality) {
        if (linkMicQualityResMap.containsKey(networkQuality)) {
            plvhcStatusLessonNetworkIv.setImageResource(linkMicQualityResMap.get(networkQuality));
        }
    }

    @Override
    public void acceptUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
        this.upstreamNetworkStatus = networkStatusVO;
    }

    @Override
    public void acceptRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO) {

    }

    @Override
    public void destroy() {
        if (statusUpdateDisposable != null) {
            statusUpdateDisposable.dispose();
        }
        if (liveRoomDataManager != null) {
            liveRoomDataManager.getFulHiClassDataBean().removeObserver(hiClassDataBeanObserver);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 状态栏UI更新">

    private void setLessonName(final String lessonName) {
        if (lessonName.length() > MAX_LENGTH_LESSON_NAME) {
            plvhcStatusLessonNameTv.setText(lessonName.substring(0, MAX_LENGTH_LESSON_NAME) + "...");
        } else {
            plvhcStatusLessonNameTv.setText(lessonName);
        }
    }

    /**
     * 当前上课状态判断
     */
    private void processUpdateLessonTimeStatus() {
        // 未上课
        if (!lessonStart && lessonRealStartTime == 0) {
            if (lessonPresetStartTime == 0 || System.currentTimeMillis() <= lessonPresetStartTime) {
                updateLessonTimeStatus(ClassStatus.NOT_START);
            } else if (System.currentTimeMillis() <= lessonPresetEndTime) {
                updateLessonTimeStatus(ClassStatus.LATE_NOT_START);
            } else {
                updateLessonTimeStatus(ClassStatus.END);
            }
            return;
        }
        // 上课中
        if (!lessonEnd) {
            if (lessonPresetEndTime == 0 || System.currentTimeMillis() <= lessonPresetEndTime) {
                updateLessonTimeStatus(ClassStatus.ON_CLASS);
            } else {
                updateLessonTimeStatus(ClassStatus.ON_CLASS_LATE);
            }
            return;
        }
        // 下课
        updateLessonTimeStatus(ClassStatus.END);
    }

    /**
     * 上课状态UI更新
     *
     * @param status 上课状态
     */
    private void updateLessonTimeStatus(ClassStatus status) {
        if (status == ClassStatus.NOT_START) {
            plvhcStatusLessonClassTimeTv.setText("未上课");
        } else if (status == ClassStatus.LATE_NOT_START) {
            SpannableStringBuilder builder = new SpannableStringBuilder("已延误");
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FF2639")), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            plvhcStatusLessonClassTimeTv.setText(builder);
        } else if (status == ClassStatus.ON_CLASS) {
            long startTime = 0;
            if (lessonPresetStartTime == 0 && lessonRealStartTime == 0 && lessonLocalStartTime == 0) {
                updateLessonTimeStatus(ClassStatus.NOT_START);
                return;
            } else if (lessonRealStartTime != 0 && System.currentTimeMillis() > lessonRealStartTime) {
                startTime = lessonRealStartTime;
            } else if (lessonLocalStartTime != 0) {
                startTime = lessonLocalStartTime;
            } else {
                // should never get in here
                return;
            }
            String timeText = PLVTimeUtils.generateTime(System.currentTimeMillis() - startTime, true);

            SpannableStringBuilder builder = new SpannableStringBuilder("上课中 " + timeText);
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFF")), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            plvhcStatusLessonClassTimeTv.setText(builder);
        } else if (status == ClassStatus.ON_CLASS_LATE) {
            long startTime = 0;
            if (lessonPresetStartTime == 0 && lessonRealStartTime == 0 && lessonLocalStartTime == 0) {
                updateLessonTimeStatus(ClassStatus.NOT_START);
                return;
            } else if (lessonRealStartTime != 0 && System.currentTimeMillis() > lessonRealStartTime) {
                startTime = lessonRealStartTime;
            } else if (lessonLocalStartTime != 0) {
                startTime = lessonLocalStartTime;
            } else {
                // should never get in here
                return;
            }
            String timeText = PLVTimeUtils.generateTime(System.currentTimeMillis() - startTime, true);

            SpannableStringBuilder builder = new SpannableStringBuilder("拖堂 " + timeText);
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FF2639")), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFF")), 2, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            plvhcStatusLessonClassTimeTv.setText(builder);
        } else if (status == ClassStatus.END) {
            plvhcStatusLessonClassTimeTv.setText("已下课");
        }
    }

    private void processUpdateNetworkDelay() {
        if (upstreamNetworkStatus == null) {
            return;
        }
        int upstreamDelay = upstreamNetworkStatus.getUpstreamDelayMs();
        int downstreamDelay = upstreamNetworkStatus.getDownstreamDelayMs();
        int delay = Math.max(0, Math.max(upstreamDelay, downstreamDelay));
        plvhcStatusLessonLatencyTv.setText(delay + "ms");
    }

    private void adjustLessonNameTvWidth() {
        final int nameTvMaxWidth = getWidth() - plvhcStatusLessonInfoLayout.getRight() * 2 - ConvertUtils.dp2px(12);
        if (plvhcStatusLessonNameTv.getMaxWidth() != nameTvMaxWidth) {
            plvhcStatusLessonNameTv.setMaxWidth(nameTvMaxWidth);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="上课状态定义枚举">

    enum ClassStatus {
        /**
         * 未上课
         */
        NOT_START,

        /**
         * 上课延误，超过预设的上课时间未上课
         */
        LATE_NOT_START,

        /**
         * 上课中
         */
        ON_CLASS,

        /**
         * 拖堂，超过预设的下课时间未下课
         */
        ON_CLASS_LATE,

        /**
         * 已结束
         */
        END
    }

    // </editor-fold>


}
