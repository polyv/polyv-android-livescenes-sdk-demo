package com.easefun.polyv.livecommon.module.utils.water;

import android.graphics.Rect;
import android.view.View;

public interface IPLVToggleView {
    boolean isEditMode();
    void toggleBorder(boolean toggle);
    void removeFromParent();
    View getCanScaleView();
    boolean isBorderVisible();
    void onClick(float upX, float upY);
    Rect getExtraPadding();
}
