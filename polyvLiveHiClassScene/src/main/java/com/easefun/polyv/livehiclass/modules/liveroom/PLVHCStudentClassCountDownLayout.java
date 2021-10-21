package com.easefun.polyv.livehiclass.modules.liveroom;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.ui.widget.PLVGradientView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.livehiclass.R;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.livescenes.hiclass.PLVHiClassDataBean;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 学生观看 上课前倒计时布局
 *
 * @author suhongtao
 */
public class PLVHCStudentClassCountDownLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private View rootView;
    private ConstraintLayout plvhcLiveRoomStudentCountdownLayout;
    private PLVGradientView plvhcLiveRoomStudentCountdownGradientBg;
    private ImageView plvhcLiveRoomStudentCountdownClockIv;
    private TextView plvhcLiveRoomStudentCountdownLabelTv;
    private TextView plvhcLiveRoomStudentCountdownTimeTv;
    private LinearLayout plvhcLiveRoomStudentCountdownPlaceholderLayout;
    private PLVRoundRectLayout plvhcLiveRoomStudentStartClassLayout;
    private TextView plvhcLiveRoomStudentStartClassLabel;
    private TextView plvhcLiveRoomStudentGoToClassTv;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    private Observer<PLVStatefulData<PLVHiClassDataBean>> hiClassDataBeanObserver;

    private Disposable countDownDisposable;

    private long lessonStartTime;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCStudentClassCountDownLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCStudentClassCountDownLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCStudentClassCountDownLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_student_class_countdown_layout, this);
        findView();
        initShowType();
        initCountDownTimer();
        initGoToClassOnClickListener();
    }

    private void findView() {
        plvhcLiveRoomStudentCountdownLayout = (ConstraintLayout) rootView.findViewById(R.id.plvhc_live_room_student_countdown_layout);
        plvhcLiveRoomStudentCountdownGradientBg = (PLVGradientView) rootView.findViewById(R.id.plvhc_live_room_student_countdown_gradient_bg);
        plvhcLiveRoomStudentCountdownClockIv = (ImageView) rootView.findViewById(R.id.plvhc_live_room_student_countdown_clock_iv);
        plvhcLiveRoomStudentCountdownLabelTv = (TextView) rootView.findViewById(R.id.plvhc_live_room_student_countdown_label_tv);
        plvhcLiveRoomStudentCountdownTimeTv = (TextView) rootView.findViewById(R.id.plvhc_live_room_student_countdown_time_tv);
        plvhcLiveRoomStudentCountdownPlaceholderLayout = (LinearLayout) rootView.findViewById(R.id.plvhc_live_room_student_countdown_placeholder_layout);
        plvhcLiveRoomStudentStartClassLayout = (PLVRoundRectLayout) rootView.findViewById(R.id.plvhc_live_room_student_start_class_layout);
        plvhcLiveRoomStudentStartClassLabel = (TextView) rootView.findViewById(R.id.plvhc_live_room_student_start_class_label);
        plvhcLiveRoomStudentGoToClassTv = (TextView) rootView.findViewById(R.id.plvhc_live_room_student_go_to_class_tv);
    }

    private void initShowType() {
        plvhcLiveRoomStudentCountdownLayout.setVisibility(VISIBLE);
        plvhcLiveRoomStudentCountdownPlaceholderLayout.setVisibility(GONE);
        plvhcLiveRoomStudentStartClassLayout.setVisibility(GONE);
    }

    private void initCountDownTimer() {
        countDownDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (lessonStartTime == 0) {
                            return;
                        }
                        final long timeDiff = lessonStartTime - System.currentTimeMillis();
                        if (timeDiff < 0) {
                            // 超时未上课
                            plvhcLiveRoomStudentCountdownLayout.setVisibility(GONE);
                            plvhcLiveRoomStudentCountdownPlaceholderLayout.setVisibility(VISIBLE);
                            plvhcLiveRoomStudentStartClassLayout.setVisibility(GONE);
                            countDownDisposable.dispose();
                            return;
                        }
                        final int timeDiffSecond = (int) (timeDiff / 1000);
                        final int second = timeDiffSecond % 60;
                        final int minute = (timeDiffSecond / 60) % 60;
                        final int hour = timeDiffSecond / 3600;
                        final String timeDiffStr = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, minute, second);
                        plvhcLiveRoomStudentCountdownTimeTv.setText(timeDiffStr);
                    }
                });
    }

    private void initGoToClassOnClickListener() {
        plvhcLiveRoomStudentGoToClassTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromParent();
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setLiveRoomDataManager(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        liveRoomDataManager.getFulHiClassDataBean().observe((LifecycleOwner) getContext(),
                hiClassDataBeanObserver = new Observer<PLVStatefulData<PLVHiClassDataBean>>() {
                    @Override
                    public void onChanged(@Nullable PLVStatefulData<PLVHiClassDataBean> plvHiClassDataBeanPLVStatefulData) {
                        if (plvHiClassDataBeanPLVStatefulData == null) {
                            return;
                        }
                        if (!plvHiClassDataBeanPLVStatefulData.isSuccess()) {
                            return;
                        }
                        Long lessonStartTime = plvHiClassDataBeanPLVStatefulData.getData().getLessonStartTime();
                        if (lessonStartTime == null) {
                            return;
                        }
                        setLessonStartTime(lessonStartTime);
                    }
                });
    }

    public void setLessonStartTime(long lessonStartTimeInMs) {
        this.lessonStartTime = lessonStartTimeInMs;
    }

    public void startClass() {
        plvhcLiveRoomStudentCountdownLayout.setVisibility(GONE);
        plvhcLiveRoomStudentCountdownPlaceholderLayout.setVisibility(GONE);
        plvhcLiveRoomStudentStartClassLayout.setVisibility(VISIBLE);
        plvhcLiveRoomStudentGoToClassTv.setText("立即前往(3s)");

        final AtomicInteger countDownTimes = new AtomicInteger(3);
        if (countDownDisposable != null) {
            countDownDisposable.dispose();
        }
        countDownDisposable = PLVRxTimer.timer(1000, 1000, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                final int timeRest = countDownTimes.decrementAndGet();
                if (timeRest <= 0) {
                    removeFromParent();
                }
                plvhcLiveRoomStudentGoToClassTv.setText("立即前往(" + timeRest + "s)");
            }
        });
    }

    public void removeFromParent() {
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
        destroy();
    }

    public void destroy() {
        if (countDownDisposable != null) {
            countDownDisposable.dispose();
            countDownDisposable = null;
        }
        if (liveRoomDataManager != null && hiClassDataBeanObserver != null) {
            liveRoomDataManager.getFulHiClassDataBean().removeObserver(hiClassDataBeanObserver);
        }
    }

    // </editor-fold>

}
