package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.easefun.polyv.livecommon.R;


public class PLVMarqueeTextView extends AppCompatTextView {

    /**
     * 默认滚动时间
     */
    private static final int ROLLING_INTERVAL_DEFAULT = 10000;
    /**
     * 第一次滚动默认延迟
     */
    private static final int FIRST_SCROLL_DELAY_DEFAULT = 1000;
    /**
     * 滚动模式-一直滚动
     */
    public static final int SCROLL_FOREVER = 100;
    /**
     * 滚动模式-只滚动一次
     */
    public static final int SCROLL_ONCE = 101;

    /**
     * 滚动器
     */
    private Scroller mScroller;
    /**
     * 滚动一次的时间
     */
    private int mRollingInterval;
    /**
     * 滚动的初始 X 位置
     */
    private int mXPaused = 0;
    /**
     * 是否暂停
     */
    private boolean mPaused = true;
    /**
     * 是否第一次
     */
    private boolean mFirst = true;
    /**
     * 滚动模式
     */
    private int mScrollMode;
    /**
     * 初次滚动时间间隔
     */
    private int mFirstScrollDelay;
    private Runnable runnable;
    private int rollDuration;
    private boolean isStopToCenter;
    private boolean useTotalTime;

    public PLVMarqueeTextView(Context context) {
        this(context, null);
    }

    public PLVMarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PLVMarqueeTextView);
        mRollingInterval = typedArray.getInt(R.styleable.PLVMarqueeTextView_scroll_interval, ROLLING_INTERVAL_DEFAULT);
        mScrollMode = typedArray.getInt(R.styleable.PLVMarqueeTextView_scroll_mode, SCROLL_FOREVER);
        mFirstScrollDelay = typedArray.getInt(R.styleable.PLVMarqueeTextView_scroll_first_delay, FIRST_SCROLL_DELAY_DEFAULT);
        typedArray.recycle();
        setSingleLine();
        setEllipsize(null);
    }

    public interface OnGetRollDurationListener {
        void onFirstGetRollDuration(int rollDuration);
    }

    private OnGetRollDurationListener onGetRollDurationListener;

    public void setOnGetRollDurationListener(OnGetRollDurationListener onGetRollDurationListener) {
        this.onGetRollDurationListener = onGetRollDurationListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(runnable);
    }

    public void setStopToCenter(boolean isStopToCenter) {
        this.isStopToCenter = isStopToCenter;
    }

    /**
     * 开始滚动
     */
    public void startScroll() {
        mPaused = true;
        mFirst = true;
        resumeScroll();
    }

    /**
     * 继续滚动
     */
    public void resumeScroll() {
        if (!mPaused)
            return;
        // 设置水平滚动
        setHorizontallyScrolling(true);

        // 使用 LinearInterpolator 进行滚动
        if (mScroller == null) {
            mScroller = new Scroller(this.getContext(), new LinearInterpolator(getContext(), null));
            setScroller(mScroller);
        }
        /**
         * ///暂时保留
         * if (getWidth() > 0) {//parent can invalidate
         *     scroll();
         * } else {
         */
        runnable = new Runnable() {
            @Override
            public void run() {
                scroll();
            }
        };
        post(runnable);
        /**
         * }
         */
    }

    private void scroll() {
        if (mXPaused == 0)
            mXPaused = -1 * getWidth();
        int scrollingLen = calculateScrollingLen();
        //滚动的距离
        int distance = scrollingLen - mXPaused;
        double durationDouble = mRollingInterval * distance * 1.00000 / scrollingLen;
        if (scrollingLen < getWidth()) {
            durationDouble = durationDouble / (getWidth() / (float) scrollingLen);
        }
        int tmpDistance = distance;
        rollDuration = (int) durationDouble;
        if (isStopToCenter && mXPaused < 0) {
            if (scrollingLen >= getWidth())
                distance = Math.abs(mXPaused);
            else
                distance = Math.abs(mXPaused) - (getWidth() - scrollingLen) / 2;
            rollDuration = (int) (rollDuration / (tmpDistance * 1.0f / distance));
        }
        if (isStopToCenter) {
            setGravity(Gravity.LEFT);
        }
        final int finalDistance = distance;
        if (!useTotalTime && !isStopToCenter) {
            if (scrollingLen > getWidth()) {
                rollDuration = (int) (mRollingInterval * (1 + scrollingLen * 1.0f / getWidth()));
            } else {
                rollDuration = mRollingInterval;
            }
        }
        if (mFirst) {
            callOnFirstGetRollDuration(rollDuration);
            runnable = new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.VISIBLE);//gone不能获取宽高，需使用invisible
                    mScroller.startScroll(mXPaused, 0, finalDistance, 0, rollDuration);
                    invalidate();
                    mPaused = false;
                }
            };
            postDelayed(runnable, mFirstScrollDelay);
        } else {
            callOnFirstGetRollDuration(rollDuration);
            mScroller.startScroll(mXPaused, 0, distance, 0, rollDuration);
            invalidate();
            mPaused = false;
        }
    }

    private void callOnFirstGetRollDuration(int rollDuration) {
        if (onGetRollDurationListener != null) {
            onGetRollDurationListener.onFirstGetRollDuration(rollDuration);
            onGetRollDurationListener = null;
        }
    }

    /**
     * 暂停滚动
     */
    public void pauseScroll() {
        if (null == mScroller)
            return;

        if (mPaused)
            return;

        mPaused = true;

        mXPaused = mScroller.getCurrX();

        mScroller.abortAnimation();
    }

    /**
     * 停止滚动，并回到初始位置
     */
    public void stopScroll() {
        if (null == mScroller) {
            return;
        }
        mPaused = true;
        if (!isStopToCenter) {
            mScroller.startScroll(0, 0, 0, 0, 0);//src
        }
        mXPaused = 0;
    }

    /**
     * 计算滚动的距离
     *
     * @return 滚动的距离
     */
    public int calculateScrollingLen() {
        TextPaint tp = getPaint();
        Rect rect = new Rect();
        String strTxt = getText().toString();
        tp.getTextBounds(strTxt, 0, strTxt.length(), rect);
        return rect.width();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (null == mScroller) return;
        if (mScroller.isFinished() && (!mPaused)) {
            if (mScrollMode == SCROLL_ONCE) {
                stopScroll();
                return;
            }
            mPaused = true;
            mXPaused = -1 * getWidth();
            mFirst = false;
            this.resumeScroll();
        }
        if (mScroller != null && mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (isStopToCenter) {
            if (mScroller != null && mScroller.isFinished()) {
                setGravity(Gravity.CENTER);
            }
        }
    }

    /**
     * 获取滚动一次的时间(文本的宽度刚好和控件的宽度相等时的时间)
     */
    public int getRndDuration() {
        return mRollingInterval;
    }

    //实际滚动的时间，可能为0
    public int getRollDuration() {
        return rollDuration;
    }

    /**
     * 设置滚动一次的时间(文本的宽度刚好和控件的宽度相等时的时间)
     */
    public void setRndDuration(int duration) {
        this.mRollingInterval = duration;
    }

    /**
     * 设置滚动模式
     */
    public void setScrollMode(int mode) {
        this.mScrollMode = mode;
    }

    /**
     * 获取滚动模式
     */
    public int getScrollMode() {
        return this.mScrollMode;
    }

    /**
     * 设置第一次滚动延迟
     */
    public void setScrollFirstDelay(int delay) {
        this.mFirstScrollDelay = delay;
    }

    /**
     * 获取第一次滚动延迟
     */
    public int getScrollFirstDelay() {
        return mFirstScrollDelay;
    }

    public boolean isPaused() {
        return mPaused;
    }
}