package com.easefun.polyv.livehiclass.modules.linkmic.widget;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.rx.PLVRxTimer;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 加入分组讨论倒数弹窗
 */
public class PLVHCJoinDiscussCountDownWindow {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //倒数次数
    private int countDownTime = 3;

    private static final float ONE_COUNT_DURATION = 1F;

    private PopupWindow window;
    private View anchor;

    private TextView tvCountDown;

    //listener
    private OnCountDownListener onClassBeginCountdownEnd;

    // 倒计时
    private Disposable countDownDisposable;

    //true表示是倒计时结束了关闭了window。false表示被取消了关闭了window。
    private boolean dismissWindowByFinishCountDown;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCJoinDiscussCountDownWindow(View anchor) {
        this.anchor = anchor;
        window = new PopupWindow();
        View rootView = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plvhc_linkmic_join_discuss_countdown_layout, null);
        rootView.setFocusable(false);
        rootView.setFocusableInTouchMode(false);

        window.setContentView(rootView);
        window.setOutsideTouchable(false);
        window.setFocusable(false);
        window.setBackgroundDrawable(null);

        window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        initView(rootView);

        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!dismissWindowByFinishCountDown && onClassBeginCountdownEnd != null) {
                    onClassBeginCountdownEnd.onCountDownCanceled();
                }

                if (countDownDisposable != null) {
                    countDownDisposable.dispose();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - view">
    private void initView(View root) {
        tvCountDown = root.findViewById(R.id.plvhc_linkmic_join_discuss_down_time_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void acceptOnWillJoinDiscuss(long countdownTimeMs) {
        setOnCountDownListener(new OnCountDownListener() {
            @Override
            public void onCountDownFinished() {
                PLVHCToast.Builder.context(anchor.getContext())
                        .setText("已开始分组讨论")
                        .setDrawable(R.drawable.plvhc_linkmic_join_status)
                        .build()
                        .show();
            }

            @Override
            public void onCountDownCanceled() {
            }
        });
        startCountDown((int) (countdownTimeMs / 1000));
    }

    public void startCountDown(int countDownTimeSec) {
        this.countDownTime = countDownTimeSec;
        show();
    }

    public void stopCountDown() {
        hide();
    }

    public void setOnCountDownListener(OnCountDownListener listener) {
        this.onClassBeginCountdownEnd = listener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void show() {
        dismissWindowByFinishCountDown = false;
        window.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        showCountDownWindowAndMakeCallback();
    }

    /**
     * 每秒更新一次倒计时数字
     */
    private void showCountDownWindowAndMakeCallback() {
        tvCountDown.setText(String.valueOf(countDownTime));

        final AtomicInteger countDownTimes = new AtomicInteger(countDownTime);
        final int oneCountDownDurationInMillis = (int) (ONE_COUNT_DURATION * 1000);
        countDownDisposable = PLVRxTimer.timer(oneCountDownDurationInMillis, oneCountDownDurationInMillis, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                int countDown = countDownTimes.decrementAndGet();
                if (countDown <= 0) {
                    dismissWindowByFinishCountDown = true;
                    countDownDisposable.dispose();
                    hide();

                    //回调开始上课倒计时完成
                    if (onClassBeginCountdownEnd != null) {
                        onClassBeginCountdownEnd.onCountDownFinished();
                    }
                    return;
                }
                tvCountDown.setText(String.valueOf(countDown));
            }
        });
    }

    private void hide() {
        window.dismiss();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 倒计时监听器">
    public interface OnCountDownListener {
        //倒计时完成
        void onCountDownFinished();

        //倒计时被取消
        void onCountDownCanceled();
    }
    // </editor-fold>
}