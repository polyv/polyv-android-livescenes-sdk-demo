package com.easefun.polyv.livestreamer.modules.document.popuplist.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.easefun.polyv.livestreamer.modules.document.popuplist.vo.PLVLSPptVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * PPT列表项视图
 *
 * @author suhongtao
 */
public abstract class PLVLSAbsPptViewItem extends FrameLayout {

    /**
     * 设计图中recyclerView所占宽度 px
     */
    protected static final double RECYCLER_VIEW_WIDTH_IN_DESIGN = ConvertUtils.dp2px(144 * 4 + 36 * 3);

    protected static int LANDSCAPE_SCREEN_WIDTH = -1;
    protected static int ITEM_MARGIN_LEFT;
    protected static int ITEM_PICTURE_WIDTH;
    protected static int ITEM_PICTURE_HEIGHT;

    public PLVLSAbsPptViewItem(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSAbsPptViewItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSAbsPptViewItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        // recyclerView与屏幕左侧margin 右侧margin相同
        int pptListRecyclerViewMarginLeft = ConvertUtils.dp2px(44);
        // 实际recyclerView所占宽度
        int recyclerViewWidthReal = LANDSCAPE_SCREEN_WIDTH - pptListRecyclerViewMarginLeft * 2;
        // 实际宽度与设计图宽度比值
        double ratioRealDivideByDesign = recyclerViewWidthReal / RECYCLER_VIEW_WIDTH_IN_DESIGN;

        // 缩放后图片所需宽度
        ITEM_PICTURE_WIDTH = (int) (ratioRealDivideByDesign * ConvertUtils.dp2px(144));
        // 缩放后图片所需高度
        ITEM_PICTURE_HEIGHT = (int) (ratioRealDivideByDesign * ConvertUtils.dp2px(80));
        // 缩放后整个item的左右margin
        ITEM_MARGIN_LEFT = (int) (ratioRealDivideByDesign * ConvertUtils.dp2px(18));
    }

    /**
     * 列表项处理视图数据
     *
     * @param pptVO
     */
    public abstract void processData(PLVLSPptVO pptVO);

}
