package com.easefun.polyv.livecommon.ui.widget.seekbar;


public interface PLVSeekBarOnRangeChangedListener {
    void onRangeChanged(PLVRangeSeekBar view, float leftValue, float rightValue, boolean isFromUser);

    void onStartTrackingTouch(PLVRangeSeekBar view, boolean isLeft);

    void onStopTrackingTouch(PLVRangeSeekBar view, boolean isLeft);

    //测试
    void onRangeChangeStep(int step);
}
