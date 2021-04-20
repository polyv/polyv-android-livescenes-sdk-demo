package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author df
 * @create 2018/8/11
 * @Describe 可移动悬浮View
 */
public class PLVTouchFloatingView extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="静态变量">
    private static final String TAG = PLVTouchFloatingView.class.getSimpleName();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实例变量">
    // 点击的位置
    private float lastX;
    private float lastY;

    //初始竖屏位置
    private int originPortraitLeft = 0;
    private int originPortraitTop = 0;
    //初始横屏位置
    private int originLandscapeLeft = 0;
    private int originLandscapeTop = 0;
    //竖屏位置
    private int portraitLeft = -1;
    private int portraitTop = -1;
    //横屏位置
    private int landscapeLeft = -1;
    private int landscapeTop = -1;

    //键盘弹起前得位置
    private int beforeSoftLeft = 0;
    private int beforeSoftTop = 0;
    //旋转任务
    private RotateTask rotateTask;

    //是否开启横向拖动
    private boolean isEnabledHorizontalDrag = true;
    //是否开启纵向拖动
    private boolean isEnabledVerticalDrag = true;
    //是否拦截所有子View点击事件
    private boolean isInterceptTouchEvent = true;

    //是否已经初始化了竖屏/横屏下的位置
    private boolean hasInitPortraitLocation = false;
    private boolean hasInitLandscapeLocation = false;

    private boolean canMove = true;//是否能移动
    //是否是横屏
    private boolean isLandscape = false;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVTouchFloatingView(Context context) {
        this(context, null);
    }

    public PLVTouchFloatingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVTouchFloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        rotateTask = new RotateTask();
        isLandscape = PLVScreenUtils.isLandscape(context);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 设置初始化位置
     *
     * @param originPortraitLeft  初始化竖屏左偏移
     * @param originPortraitTop   初始化竖屏上偏移
     * @param originLandscapeLeft 初始化横屏左偏移
     * @param originLandscapeTop  初始化横屏上偏移
     */
    public void setInitLocation(int originPortraitLeft, int originPortraitTop, int originLandscapeLeft, int originLandscapeTop) {
        this.originPortraitLeft = originPortraitLeft;
        this.originPortraitTop = originPortraitTop;
        this.originLandscapeLeft = originLandscapeLeft;
        this.originLandscapeTop = originLandscapeTop;

        if (ScreenUtils.isPortrait()) {
            tryInitPortraitLocation();
        } else {
            tryInitLandscapeLocation();
        }
    }

    /**
     * 设置悬浮窗是否可以移动
     *
     * @param canMove true为可移动，false为不可移动
     */
    public void setContainerMove(boolean canMove) {
        this.canMove = canMove;
    }

    /**
     * 设置小窗是否拦截全部点击事件
     *
     * @param isInterceptTouchEvent true表示拦截，false表示不拦截
     */
    public void setIsInterceptTouchEvent(boolean isInterceptTouchEvent) {
        this.isInterceptTouchEvent = isInterceptTouchEvent;
    }

    /**
     * 设置是否可以横向拖动
     *
     * @param enable true表示开启纵向拖动
     */
    public void enableHorizontalDrag(boolean enable) {
        isEnabledHorizontalDrag = enable;
    }

    /**
     * 设置是否可以纵向拖动
     *
     * @param enable true表示开启横向拖动
     */
    public void enableVerticalDrag(boolean enable) {
        isEnabledVerticalDrag = enable;
    }

    /**
     * 键盘弹起时调用
     *
     * @param top 距离父布局顶部的位置
     */
    public void topSubviewTo(final int top) {
        post(new Runnable() {
            @Override
            public void run() {
                MarginLayoutParams rlp = (MarginLayoutParams) getLayoutParams();
                if (rlp == null) {
                    return;
                }
                beforeSoftLeft = rlp.leftMargin;
                beforeSoftTop = rlp.topMargin;
                if (rlp.topMargin + rlp.height < top) {
                    return;
                }

                PLVCommonLog.d(TAG, "topSubviewTo left :" + beforeSoftLeft + "   top " + top);
                rlp.topMargin = top - rlp.height;
                setLayoutParams(rlp);
            }
        });
    }

    /**
     * 键盘收缩时调用，恢复到键盘弹起前的位置
     */
    public void resetSoftTo() {
        post(new Runnable() {
            @Override
            public void run() {
                MarginLayoutParams rlp = (MarginLayoutParams) getLayoutParams();
                if (rlp == null) {
                    return;
                }
                PLVCommonLog.d(TAG, "resetSoftTo left :" + beforeSoftLeft + "   top " + beforeSoftTop);
                rlp.leftMargin = beforeSoftLeft;
                rlp.topMargin = beforeSoftTop;
                setLayoutParams(rlp);
            }
        });

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理触摸事件">
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canMove) {
            return super.onTouchEvent(event);
        }
        //子view为invisible时(即非visible)不要拦截点击事件
        boolean firstChildIsVisible = getChildAt(0) == null || (getChildAt(0).getVisibility() == View.VISIBLE);
        if (/*getVisibility() != View.VISIBLE || */!firstChildIsVisible)
            return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastX = event.getX();
            lastY = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // 计算移动的距离
            float x = event.getX();
            float y = event.getY();
            // 偏移量
            int offX = (int) (x - lastX);
            int offY = (int) (y - lastY);
            View view = this;
            int left = view.getLeft() + offX;
            int top = view.getTop() + offY;
            int parentWidth = ((View) view.getParent()).getMeasuredWidth();
            int parentHeight = ((View) view.getParent()).getMeasuredHeight();
            if (offX < 0 && left < 0)
                left = 0;
            if (offY < 0 && top < 0)
                top = 0;
            if (offX > 0 && view.getRight() + offX > parentWidth)
                left = view.getLeft() + (parentWidth - view.getRight());
            if (offY > 0 && view.getBottom() + offY > parentHeight)
                top = view.getTop() + (parentHeight - view.getBottom());

            MarginLayoutParams rlp = (MarginLayoutParams) view.getLayoutParams();
            if (isLandscape) {
                //如果当前是横屏，实时保存横屏的位置
                landscapeTop = top;
                landscapeLeft = left;
            } else {
                //如果当前是竖屏，实时保存竖屏的位置
                portraitTop = top;
                portraitLeft = left;
            }
            if (isEnabledVerticalDrag) {
                rlp.topMargin = top;
            }
            if (isEnabledHorizontalDrag) {
                rlp.leftMargin = left;
            }
            view.setLayoutParams(rlp);
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            lastX = 0;
            lastY = 0;
        }
        return true;
    }

    //是否把小窗中的点击事件全部拦截下来
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isInterceptTouchEvent;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">
    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        rotateTask.buildConfig(newConfig);
        if (getHandler() != null) {
            getHandler().removeCallbacks(rotateTask);
        }
        post(rotateTask);
    }

    //初始化竖屏位置
    private void tryInitPortraitLocation() {
        if (hasInitPortraitLocation) {
            return;
        }
        hasInitPortraitLocation = true;
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        lp.leftMargin = originPortraitLeft;
        lp.topMargin = originPortraitTop;
        setLayoutParams(lp);

        portraitTop = originPortraitTop;
        portraitLeft = originPortraitLeft;
    }

    //初始化横屏位置
    private void tryInitLandscapeLocation() {
        if (hasInitLandscapeLocation) {
            return;
        }
        hasInitLandscapeLocation = true;
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        lp.leftMargin = originLandscapeLeft;
        lp.topMargin = originLandscapeTop;
        setLayoutParams(lp);

        landscapeLeft = originLandscapeLeft;
        landscapeTop = originLandscapeTop;
    }

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="inner class">
    class RotateTask implements Runnable {
        private Configuration newConfig;

        public void buildConfig(Configuration configuration) {
            newConfig = configuration;
        }

        @Override
        public void run() {

            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                isLandscape = false;
                PLVCommonLog.d(TAG, "RotateTask.run->portrait");
                //恢复竖屏的位置
                resetFloatViewPort();
                //如果竖屏位置没有初始化过，初始化竖屏位置
                tryInitPortraitLocation();
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                isLandscape = true;
                PLVCommonLog.d(TAG, "RotateTask.run->landscape");
                //恢复横屏的位置
                resetFloatViewLand();
                //如果横屏位置没有初始化过，初始化横屏位置
                tryInitLandscapeLocation();
            }
        }

        //恢复竖屏的位置
        private void resetFloatViewLand() {
            MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
            if (landscapeLeft >= 0) {
                layoutParams.leftMargin = landscapeLeft;
            }
            if (landscapeTop >= 0) {
                layoutParams.topMargin = landscapeTop;
            }
            setLayoutParams(layoutParams);

        }

        //恢复横屏的位置
        private void resetFloatViewPort() {
            MarginLayoutParams rlp = (MarginLayoutParams) getLayoutParams();
            if (portraitLeft >= 0) {
                rlp.leftMargin = portraitLeft;
            }
            if (portraitTop >= 0) {
                rlp.topMargin = portraitTop;
            }
            setLayoutParams(rlp);
        }
    }
    // </editor-fold>
}
