package com.easefun.polyv.livehiclass.modules.liveroom;

import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;
import com.plv.foundationsdk.rx.PLVRxTimer;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 直播开始倒数弹窗
 */
public class PLVHCTeacherBeginCountDownWindow {
    //倒数次数
    private static final int COUNT_DOWN_TIMES = 3;

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

    public PLVHCTeacherBeginCountDownWindow(View anchor) {
        this.anchor = anchor;
        window = new PopupWindow();
        View rootView = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plvhc_live_room_teacher_begin_countdown_layout, null);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);

        window.setContentView(rootView);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.setBackgroundDrawable(new ColorDrawable());

        window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        initView(rootView);

        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!dismissWindowByFinishCountDown) {
                    onClassBeginCountdownEnd.onCountDownCanceled();
                }

                if (countDownDisposable != null) {
                    countDownDisposable.dispose();
                }
            }
        });
    }

    private void initView(View root) {
        tvCountDown = root.findViewById(R.id.plvhc_live_room_teacher_count_down_time_tv);
    }

    private void show() {
        dismissWindowByFinishCountDown = false;
        window.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        showCountDownWindowAndMakeCallback();
    }

    /**
     * 每秒更新一次倒计时数字
     */
    private void showCountDownWindowAndMakeCallback() {
        tvCountDown.setText(String.valueOf(COUNT_DOWN_TIMES));

        final AtomicInteger countDownTimes = new AtomicInteger(COUNT_DOWN_TIMES);
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

    public void startCountDown() {
        show();
    }

    public void stopCountDown() {
        hide();
    }

    public void setOnCountDownListener(OnCountDownListener listener) {
        this.onClassBeginCountdownEnd = listener;
    }

    public interface OnCountDownListener {
        //倒计时完成
        void onCountDownFinished();

        //倒计时被取消
        void onCountDownCanceled();
    }
}