package com.easefun.polyv.livestreamer.modules.liveroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livestreamer.R;


/**
 * 上课倒数弹窗
 */
public class PLVLSClassBeginCountDownWindow implements IPLVLSCountDownView {
    //倒数次数
    private static final int COUNT_DOWN_TIMES = 3;
    private static final int TEXT_SIZE_DP = 140;

    private static final float ONE_COUNT_DURATION = 1.0f;

    private PopupWindow window;
    private View anchor;

    private TextView tvCountDown;

    //listener
    private OnCountDownListener onClassBeginCountdownEnd;

    //动画
    private ObjectAnimator animator;

    //true表示是倒计时结束了关闭了window。false表示被取消了关闭了window。
    private boolean dismissWindowByFinishCountDown;

    public PLVLSClassBeginCountDownWindow(View anchor) {
        this.anchor = anchor;
        window = new PopupWindow();
        View rootView = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plvls_status_bar_class_begin_countdown_layout, null);
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

                if (animator != null) {
                    animator.removeAllListeners();
                    animator.cancel();
                }
            }
        });
    }

    private void initView(View root) {
        tvCountDown = root.findViewById(R.id.plvs_tv_class_begin_countdown);
    }

    private void show() {
        dismissWindowByFinishCountDown = false;
        window.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        showCountDownWindowAndMakeCallback();
    }

    /**
     * 计时数字动效说明：0s时，数字3 开始渐变出现，此时字体不变，到 0.3s 完整出现。
     * 0.6s 开始缩小渐隐，且字体缩小到0.6的大小，到 0.95s 完全消失
     * 每个数字以1秒的时间重复上述过程
     * <p>
     * 注意：使用了PropertyValuesHolder和Keyframe来保证在低端机上不会掉帧。
     */
    private void showCountDownWindowAndMakeCallback() {
        tvCountDown.setText(String.valueOf(COUNT_DOWN_TIMES));
        tvCountDown.setAlpha(1);
        int originTextSize = TEXT_SIZE_DP;

        //textSize
        Keyframe keyframe0 = Keyframe.ofFloat(0, originTextSize);
        Keyframe keyframe1 = Keyframe.ofFloat(0.6f / ONE_COUNT_DURATION, originTextSize);
        Keyframe keyframe2 = Keyframe.ofFloat(0.95f / ONE_COUNT_DURATION, originTextSize * 0.6f);
        Keyframe keyframe3 = Keyframe.ofFloat(1, 0);

        PropertyValuesHolder holder = PropertyValuesHolder.ofKeyframe("textSize",
                keyframe0,
                keyframe1,
                keyframe2,
                keyframe3);

        //alpha
        Keyframe keyframeAlpha0 = Keyframe.ofFloat(0, 0);
        Keyframe keyframeAlpha1 = Keyframe.ofFloat(0.3f / ONE_COUNT_DURATION, 1);
        Keyframe keyframeAlpha2 = Keyframe.ofFloat(0.6f / ONE_COUNT_DURATION, 1);
        Keyframe keyframeAlpha3 = Keyframe.ofFloat(0.95f / ONE_COUNT_DURATION, 0);
        Keyframe keyframeAlpha4 = Keyframe.ofFloat(1, 0);

        PropertyValuesHolder holder1 = PropertyValuesHolder.ofKeyframe("alpha",
                keyframeAlpha0,
                keyframeAlpha1,
                keyframeAlpha2,
                keyframeAlpha3,
                keyframeAlpha4);

        animator = ObjectAnimator.ofPropertyValuesHolder(tvCountDown, holder, holder1);
        animator.setRepeatCount(COUNT_DOWN_TIMES - 1);
        animator.setDuration((long) (ONE_COUNT_DURATION * 1000));

        animator.addListener(new AnimatorListenerAdapter() {
            int curCountDownNumber = COUNT_DOWN_TIMES;

            @Override
            public void onAnimationEnd(Animator animation) {
                dismissWindowByFinishCountDown = true;
                hide();

                //回调开始上课倒计时完成
                if (onClassBeginCountdownEnd != null) {
                    onClassBeginCountdownEnd.onCountDownFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                tvCountDown.setText(String.valueOf(--curCountDownNumber));
            }
        });
        animator.start();
    }

    private void hide() {
        window.dismiss();
    }

    @Override
    public void startCountDown() {
        show();
    }

    @Override
    public void stopCountDown() {
        hide();
    }

    @Override
    public void setOnCountDownListener(OnCountDownListener listener) {
        this.onClassBeginCountdownEnd = listener;
    }
}