package com.easefun.polyv.livecloudclass.modules.chatroom.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.PLVBezierEvaluator;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.lang.ref.WeakReference;
import java.util.Random;


public class PLVLCLikeIconView extends RelativeLayout {

    private int width;
    private int height;
    private int iconWidth;
    private int iconHeight;

    private Interpolator[] interpolators;
    private Random random = new Random();

    private FrameLayout loveIconContainer;

    private int srcWH;

    public PLVLCLikeIconView(Context context) {
        this(context, null);
    }

    public PLVLCLikeIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLikeIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PLVLCLikeIconView, defStyleAttr, 0);
        srcWH = a.getDimensionPixelSize(R.styleable.PLVLCLikeIconView_src_wh, ConvertUtils.dp2px(46));
        a.recycle();
        init();
    }

    private void init() {
        initInterpolator();
        initChild();
    }

    private void initChild() {
        //漂浮爱心容器
        loveIconContainer = new FrameLayout(getContext());
        LayoutParams containerLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        loveIconContainer.setLayoutParams(containerLp);


        //圆背景
        View bg = new View(getContext());
        bg.setBackgroundResource(R.drawable.plvlc_chatroom_btn_like);
        float d = srcWH;
        LayoutParams bgLp = new LayoutParams((int) d, (int) d);
        bgLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bgLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        /**
         * ///暂时保留，以后有必要时再使用
         *  bgLp.addRule(Gravity.CENTER);
         *  bgLp.bottomMargin = PolyvScreenUtils.dip2px(getContext(), 3);
         */

        bgLp.rightMargin = PLVScreenUtils.dip2px(getContext(), 16);
        bg.setLayoutParams(bgLp);
        bg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onClick(PLVLCLikeIconView.this);
                }
            }
        });

        //圆背景
        addView(bg);
        //漂浮爱心容器
        addView(loveIconContainer);
    }

    private void initInterpolator() {
        interpolators = new Interpolator[]{
                /**
                 * 暂时保留，以后有必要时再使用
                new AccelerateDecelerateInterpolator(),
                new AccelerateInterpolator(),
                new DecelerateInterpolator(),**/
                new LinearInterpolator()

        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    private OnClickListener onButtonClickListener;

    public void setOnButtonClickListener(@Nullable OnClickListener l) {
        onButtonClickListener = l;
    }

    @Override
    protected void onDetachedFromWindow() {
        removeAllViews();
        super.onDetachedFromWindow();
    }

    private void startAnimator(ImageView view) {
        if (height <= 0 || width <= 0)
            return;
        PointF pointF1 = new PointF(
                random.nextInt(Math.max(2, width - ConvertUtils.dp2px(16))) - ConvertUtils.dp2px(6),
                random.nextInt(height / 2));
        PointF pointF2 = new PointF(
                random.nextInt(Math.max(2, width - ConvertUtils.dp2px(16))) - ConvertUtils.dp2px(6),
                random.nextInt(height / 2));
        PointF pointStart = new PointF((width - iconWidth) / 2.0f, (float) (height - srcWH));
        PointF pointEnd = new PointF(random.nextInt(Math.max(2, width - ConvertUtils.dp2px(16))) - ConvertUtils.dp2px(6), 0);

        //贝塞尔估值器
        PLVBezierEvaluator evaluator = new PLVBezierEvaluator(pointF1, pointF2);
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, pointStart, pointEnd);
        animator.setTarget(view);
        animator.setDuration(Const.DURATION_FLY_LOVE_ICON);
        animator.addUpdateListener(new UpdateListener(view));
        animator.addListener(new AnimatorListener(view, (ViewGroup) view.getParent()));
        animator.setInterpolator(interpolators[random.nextInt(interpolators.length)]);

        animator.start();
    }


    private int[] imageId = new int[]{
            R.drawable.plvlc_chatroom_btn_like_1,
            R.drawable.plvlc_chatroom_btn_like_2,
            R.drawable.plvlc_chatroom_btn_like_3,
            R.drawable.plvlc_chatroom_btn_like_4,
            R.drawable.plvlc_chatroom_btn_like_5,
            R.drawable.plvlc_chatroom_btn_like_6,
            R.drawable.plvlc_chatroom_btn_like_7,
            R.drawable.plvlc_chatroom_btn_like_8,
            R.drawable.plvlc_chatroom_btn_like_9
    };
    private Random randomColor = new Random();

    public void addLoveIcon(final int count) {
        if (height <= 0 || width <= 0)
            return;
        post(new Runnable() {
            @Override
            public void run() {
                ImageView view = new ImageView(getContext());
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                view.setImageResource(imageId[random.nextInt(imageId.length)]);
                int scale = random.nextInt(4) + 7;
                iconWidth = view.getDrawable().getIntrinsicWidth() * scale / 10;
                iconHeight = view.getDrawable().getIntrinsicHeight() * scale / 10;
                LayoutParams bgLp = new LayoutParams(iconWidth, iconHeight);
                view.setLayoutParams(bgLp);

                addView(view);
                startAnimator(view);
            }
        });
    }

    private static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private WeakReference<View> iv;

        UpdateListener(View iv) {
            this.iv = new WeakReference<>(iv);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            PointF pointF = (PointF) animation.getAnimatedValue();
            View view = iv.get();
            if (null != view) {
                view.setX(pointF.x);
                view.setY(pointF.y);
                view.setAlpha(1 - animation.getAnimatedFraction() + 0.1f);
            }
        }
    }

    private static class AnimatorListener extends AnimatorListenerAdapter {

        private WeakReference<View> iv;
        private WeakReference<ViewGroup> parent;

        AnimatorListener(View iv, ViewGroup parent) {
            this.iv = new WeakReference<>(iv);
            this.parent = new WeakReference<>(parent);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            View view = iv.get();
            ViewGroup parent = this.parent.get();
            if (null != view
                    && null != parent) {
                parent.removeView(view);
            }
        }
    }

    static class Const {
        //心跳在父布局的内边距
        static final int BOTTOM_MARGIN_DP = 44 / 3;
        static final int RIGHT_MARGIN_DP = 44 / 3;

        //让心跳的圆心向下偏移一点距离（原本圆心重合），让爱心和背景的相对位置更协调。
        static final int OFFSET_OF_HEART = 5;

        //背景圆形和心跳的直径比
        static final float BG_RATIO = 1.6f;

        //心跳缩放比例
        static final float BEAT_ZOOM_RATIO = 1.3f;

        //漂浮爱心的持续时间
        static final int DURATION_FLY_LOVE_ICON = 2000;
    }
}
