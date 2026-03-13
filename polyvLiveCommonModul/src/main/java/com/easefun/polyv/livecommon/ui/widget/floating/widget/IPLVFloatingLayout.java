package com.easefun.polyv.livecommon.ui.widget.floating.widget;

import android.app.Activity;
import android.graphics.Point;
import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;

/**
 * 悬浮窗Layout - 对外接口定义
 */
public interface IPLVFloatingLayout {


    /**
     * 设置悬浮窗显示的View
     * @param view
     */
    void setContentView(View view);

    /**
     * 获取悬浮窗显示的View
     * @return
     */
    View getContentView();


    /**
     * 显示悬浮窗
     * @param activity
     */
    void show(Activity activity);

    /**
     * 隐藏悬浮窗
     */
    void hide();

    /**
     * 悬浮窗是否正在显示
     *
     * @return
     */
    boolean isShowing();

    void setShowType(PLVFloatingEnums.ShowType showType);

    void setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType autoEdgeType);

    void setEnableDrag(boolean enableDrag);

    void setEnableDragX(boolean enableDrag);

    void setEnableDragY(boolean enableDrag);

    void setConsumeTouchEventOnMove(boolean consumeTouchEventOnMove);

    /**
     * 更新悬浮窗尺寸
     */
    void updateFloatSize(int width, int height);

    /**
     * 更新悬浮窗位置
     * @param x 坐标x
     * @param y 坐标y
     */
    void updateFloatLocation(int x, int y);

    Point getFloatLocation();

    /**
     * 启用缩放功能
     */
    void setEnableScale(boolean enableScale);

    /**
     * 设置缩放范围
     */
    void setScaleRange(float minScale, float maxScale);

    /**
     * 获取当前缩放比例
     */
    float getCurrentScale();

    /**
     * 重置缩放比例
     */
    void resetScale();

    /**
     * 设置缩放比例
     */
    void setScale(float scale);

    /**
     * 启用收起功能
     */
    void setEnableCollapse(boolean enableCollapse);

    /**
     * 销毁
     */
    void destroy();


}
