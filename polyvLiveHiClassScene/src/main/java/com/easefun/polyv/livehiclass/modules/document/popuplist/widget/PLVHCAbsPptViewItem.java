package com.easefun.polyv.livehiclass.modules.document.popuplist.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.easefun.polyv.livehiclass.modules.document.popuplist.vo.PLVHCPptVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * PPT列表项视图
 *
 * @author suhongtao
 */
public abstract class PLVHCAbsPptViewItem extends FrameLayout {

    /**
     * 设计图中屏幕宽度 px
     */
    protected static final double SCREEN_WIDTH_IN_DESIGN = ConvertUtils.dp2px(812);

    protected static int LANDSCAPE_SCREEN_WIDTH = -1;
    protected static int ITEM_MARGIN_LEFT;
    protected static int ITEM_PICTURE_WIDTH;
    protected static int ITEM_PICTURE_HEIGHT;

    public PLVHCAbsPptViewItem(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCAbsPptViewItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCAbsPptViewItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updateItemSize();
    }

    /**
     * 动态设置PPT列表项尺寸大小
     */
    protected static void updateItemSize() {
        int landscapeScreenWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (landscapeScreenWidth == LANDSCAPE_SCREEN_WIDTH) {
            return;
        }
        LANDSCAPE_SCREEN_WIDTH = landscapeScreenWidth;

        // 实际宽度与设计图宽度比值
        double ratioRealDivideByDesign = LANDSCAPE_SCREEN_WIDTH / SCREEN_WIDTH_IN_DESIGN;

        // 缩放后图片所需宽度
        ITEM_PICTURE_WIDTH = (int) (ratioRealDivideByDesign * ConvertUtils.dp2px(144));
        // 缩放后图片所需高度
        ITEM_PICTURE_HEIGHT = (int) (ratioRealDivideByDesign * ConvertUtils.dp2px(80));
        // 缩放后整个item的左右margin
        ITEM_MARGIN_LEFT = (int) (ratioRealDivideByDesign * ConvertUtils.dp2px(8));
    }

    /**
     * 列表项处理视图数据
     *
     * @param pptVO
     */
    public abstract void processData(PLVHCPptVO pptVO);

}
