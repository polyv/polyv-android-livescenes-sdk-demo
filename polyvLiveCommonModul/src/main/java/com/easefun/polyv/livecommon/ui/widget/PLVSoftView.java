
package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.plv.foundationsdk.utils.PLVControlUtils;


public class PLVSoftView extends LinearLayout {

    public static final byte KEYBOARD_STATE_SHOW = -3;
    public static final byte KEYBOARD_STATE_HIDE = -2;
    public static final byte KEYBOARD_STATE_INIT = -1;

    private boolean mHasInit = false;
    private boolean mHasKeyboard = false;
    private int mHeightOrigin;  //   原始的高度，有全屏切换回来时，可能会计算加上状态栏的高度

    private int mStatusBarHeight = 0;

    private IOnKeyboardStateChangedListener onKeyboardStateChangedListener;

    public void setOnKeyboardStateChangedListener(
            IOnKeyboardStateChangedListener onKeyboardStateChangedListener) {
        this.onKeyboardStateChangedListener = onKeyboardStateChangedListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasInit) {
            mHasInit = true;
            mHeightOrigin = b;
            if (onKeyboardStateChangedListener != null) {
                onKeyboardStateChangedListener.onKeyboardStateChanged(KEYBOARD_STATE_INIT);
            }
        } else {
            mHeightOrigin = mHeightOrigin < b ? b : mHeightOrigin;
        }

        if (mHeightOrigin - mStatusBarHeight > b) {   //因为全屏切换回来时，会导致本页面的高度会从全屏切回到正常高度(去掉状态栏的高度)，会导致处理
            mHasKeyboard = true;
            if (onKeyboardStateChangedListener != null) {
                onKeyboardStateChangedListener.onKeyboardStateChanged(KEYBOARD_STATE_SHOW);
            }
        }
        if (mHasKeyboard && (mHeightOrigin == b || mHeightOrigin - mStatusBarHeight == b)) {
            mHasKeyboard = false;
            if (onKeyboardStateChangedListener != null) {
                onKeyboardStateChangedListener.onKeyboardStateChanged(KEYBOARD_STATE_HIDE);
            }
        }
    }



    public interface IOnKeyboardStateChangedListener {

        void onKeyboardStateChanged(int state);
    }

    public PLVSoftView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mStatusBarHeight = PLVControlUtils.getStatusBarHeight(context);
    }

    public PLVSoftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mStatusBarHeight = PLVControlUtils.getStatusBarHeight(context);
    }

    public PLVSoftView(Context context) {
        super(context);
        mStatusBarHeight = PLVControlUtils.getStatusBarHeight(context);
    }

}
