package com.easefun.polyv.livecommon.ui.widget.seekbar;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2018/5/9
 * 描    述: it works for draw indicator text
 * ================================================
 */
public class PLVSeekBarState {
    public String indicatorText;
    public float value; //now progress value
    public boolean isMin;
    public boolean isMax;

    @Override
    public String toString() {
        return "indicatorText: " + indicatorText + " ,isMin: " + isMin + " ,isMax: " + isMax;
    }
}
