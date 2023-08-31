package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.module.utils.PLVBezierEvaluator;
import com.easefun.polyv.livecommon.ui.widget.imageview.IPLVVisibilityChangedListener;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.lang.ref.WeakReference;
import java.util.Random;


public class PLVECLikeIconView extends RelativeLayout {

    private int width;
    private int height;
    private int iconWidth;
    private int iconHeight;

    private Interpolator[] interpolators;
    private Random random = new Random();

    private FrameLayout loveIconContainer;

    private int srcWH;
    private int topViewId;
    private ViewGroup topView;

    private IPLVVisibilityChangedListener visibilityChangedListener;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    private boolean avoidTopView = false;

    public PLVECLikeIconView(Context context) {
        this(context, null);
    }

    public PLVECLikeIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLikeIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PLVECLikeIconView, defStyleAttr, 0);
        srcWH = a.getDimensionPixelSize(R.styleable.PLVECLikeIconView_src_wh, ConvertUtils.dp2px(46));
        topViewId = a.getResourceId(R.styleable.PLVECLikeIconView_top_view, 0);
        a.recycle();
        init();
    }

    private void init() {
        initInterpolator();
        initChild();
        initViewTreeObserver();
    }

    private void initChild() {
        //漂浮爱心容器
        loveIconContainer = new FrameLayout(getContext());
        LayoutParams containerLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        loveIconContainer.setLayoutParams(containerLp);


        //圆背景
        final View bg = new View(getContext());
        bg.setBackgroundResource(R.drawable.plvec_chatroom_btn_like);
        float d = srcWH;
        LayoutParams bgLp = new LayoutParams((int) d, (int) d);
        bgLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bgLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        bgLp.bottomMargin = ConvertUtils.dp2px(5);
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
                    onButtonClickListener.onClick(PLVECLikeIconView.this);
                }
                bg.startAnimation(createClickAnimation());
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

    private void initViewTreeObserver() {
        getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (topViewId != 0 && topView == null) {
                    topView = ((Activity) getContext()).findViewById(topViewId);
                }
                avoidTopView = topView != null && hasShownChildView(topView);
            }
        });
    }

    private boolean hasShownChildView(View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                boolean result = hasShownChildView(((ViewGroup) view).getChildAt(i));
                if (result) {
                    return true;
                }
            }
        } else {
            return view.isShown();
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibilityChangedListener != null) {
            visibilityChangedListener.onChanged(visibility);
        }
    }

    public void setAvoidTopView(boolean avoidTopView) {
        this.avoidTopView = avoidTopView;
    }

    public void setVisibilityChangedListener(IPLVVisibilityChangedListener listener) {
        this.visibilityChangedListener = listener;
    }

    private OnClickListener onButtonClickListener;

    public void setOnButtonClickListener(@Nullable OnClickListener l) {
        onButtonClickListener = l;
    }

    private AnimationSet createClickAnimation() {

        CycleInterpolator interpolator = new CycleInterpolator(1);

        Animation rotateAnimation = new RotateAnimation(0f, -30f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        Animation scaleAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        AnimationSet animatorSet = new AnimationSet(true);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(interpolator);
//        animatorSet.addAnimation(rotateAnimation);
        animatorSet.addAnimation(scaleAnimation);
        return animatorSet;

    }

    @Override
    protected void onDetachedFromWindow() {
        removeAllViews();
        getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        super.onDetachedFromWindow();
    }

    private void startAnimator(ImageView view) {
        if (height <= 0 || width <= 0)
            return;
        PointF pointF1, pointF2, pointStart, pointEnd;
        if (avoidTopView) {
            pointF1 = new PointF(iconWidth, height / 4.0f);
            pointF2 = new PointF(iconWidth, height / 4.0f);
            pointStart = new PointF(Math.max(2, (width - iconWidth - ConvertUtils.dp2px(28))), (height - srcWH));
            pointEnd = new PointF(Math.max(iconWidth, random.nextInt(Math.max(2, width - iconWidth - ConvertUtils.dp2px(28)))), 0);
        } else {
            pointF1 = new PointF(Math.max(2, (width - iconWidth - ConvertUtils.dp2px(16))), height / 3.0f);
            pointF2 = new PointF(Math.max(2, (width - iconWidth - ConvertUtils.dp2px(16))), height / 3.0f);
            pointStart = new PointF(Math.max(2, (width - iconWidth - ConvertUtils.dp2px(18))), (height - srcWH));
            pointEnd = new PointF(Math.max(iconWidth, random.nextInt(Math.max(2, width - ConvertUtils.dp2px(16))) + ConvertUtils.dp2px(6)), 0);
        }
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
            R.drawable.plvec_chatroom_btn_like_1,
            R.drawable.plvec_chatroom_btn_like_2,
            R.drawable.plvec_chatroom_btn_like_3,
            R.drawable.plvec_chatroom_btn_like_4,
            R.drawable.plvec_chatroom_btn_like_5,
            R.drawable.plvec_chatroom_btn_like_6,
            R.drawable.plvec_chatroom_btn_like_7,
            R.drawable.plvec_chatroom_btn_like_8,
            R.drawable.plvec_chatroom_btn_like_9,
            R.drawable.plvec_chatroom_btn_like_10,
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

                addView(view, 0);
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
                //刚出现时有从小到大的效果
                view.setScaleX(Math.min(1.2f, 0.5f + animation.getAnimatedFraction()));
                view.setScaleY(Math.min(1.2f, 0.5f + animation.getAnimatedFraction()));
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
