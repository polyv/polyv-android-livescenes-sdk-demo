package com.easefun.polyv.livecommon.ui.widget.floating;

import android.app.Activity;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.easefun.polyv.livecommon.ui.widget.floating.widget.IPLVFloatingLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.widget.PLVAbsFloatingLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.widget.PLVAppFloatingLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.widget.PLVSystemFloatingLayout;

/**
 * 单例，悬浮窗管理类
 */
public class PLVFloatingWindowManager implements IPLVFloatingLayout {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private static final PLVFloatingWindowManager INSTANCE = new PLVFloatingWindowManager();

    public static PLVFloatingWindowManager getInstance() {
        return INSTANCE;
    }

    private PLVFloatingWindowManager() {
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    @Nullable
    private PLVAbsFloatingLayout floatingLayout;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    /**
     * 构造悬浮窗，多次构造仅生效一次
     */
    public WindowBuilder createNewWindow(Activity activity) {
        destroy();
        return new WindowBuilder(activity);
    }

    @Override
    public void setContentView(View view) {
        if (floatingLayout != null) {
            floatingLayout.setContentView(view);
        }
    }

    @Nullable
    @Override
    public View getContentView() {
        if (floatingLayout != null) {
            return floatingLayout.getContentView();
        }
        return null;
    }

    @Override
    public void show(Activity activity) {
        if (floatingLayout != null) {
            floatingLayout.show(activity);
        }
    }

    @Override
    public void hide() {
        if (floatingLayout != null) {
            floatingLayout.hide();
        }
    }

    @Override
    public boolean isShowing() {
        if (floatingLayout != null) {
            return floatingLayout.isShowing();
        }
        return false;
    }

    @Override
    public void setShowType(PLVFloatingEnums.ShowType showType) {
        if (floatingLayout != null) {
            floatingLayout.setShowType(showType);
        }
    }

    @Override
    public void setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType autoEdgeType) {
        if (floatingLayout != null) {
            floatingLayout.setAutoMoveToEdge(autoEdgeType);
        }
    }

    @Override
    public void updateFloatSize(int width, int height) {
        if (floatingLayout != null) {
            floatingLayout.updateFloatSize(width, height);
        }
    }

    @Override
    public void updateFloatLocation(int x, int y) {
        if (floatingLayout != null) {
            floatingLayout.updateFloatLocation(x, y);
        }
    }

    @Override
    public void setEnableDrag(boolean enableDrag) {
        if (floatingLayout != null) {
            floatingLayout.setEnableDrag(enableDrag);
        }
    }

    @Override
    public void setConsumeTouchEventOnMove(boolean consumeTouchEventOnMove) {
        if (floatingLayout != null) {
            floatingLayout.setConsumeTouchEventOnMove(consumeTouchEventOnMove);
        }
    }

    @Nullable
    @Override
    public Point getFloatLocation() {
        if (floatingLayout != null) {
            return floatingLayout.getFloatLocation();
        }
        return null;
    }

    @Override
    public void destroy() {
        if (floatingLayout != null) {
            floatingLayout.destroy();
            floatingLayout = null;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="悬浮窗构造器Builder">
    public static class WindowBuilder {
        private final Param buildParam = new Param();

        private WindowBuilder(@NonNull Activity activity) {
            buildParam.activity = activity;
        }

        public WindowBuilder setIsSystemWindow(boolean isSystemWindow) {
            buildParam.isSystemWindow = isSystemWindow;
            return this;
        }

        public WindowBuilder setContentView(View view) {
            buildParam.contentView = view;
            return this;
        }

        public WindowBuilder setSize(int width, int height) {
            buildParam.width = width;
            buildParam.height = height;
            return this;
        }

        public WindowBuilder setFloatLocation(int left, int top) {
            buildParam.left = left;
            buildParam.top = top;
            return this;
        }

        public WindowBuilder setShowType(PLVFloatingEnums.ShowType showType) {
            buildParam.showType = showType;
            return this;
        }

        public WindowBuilder setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType autoEdgeType) {
            buildParam.autoEdgeType = autoEdgeType;
            return this;
        }

        public WindowBuilder setEnableDrag(boolean enableDrag) {
            buildParam.enableDrag = enableDrag;
            return this;
        }

        public WindowBuilder setConsumeTouchEventOnMove(boolean consumeTouchEventOnMove) {
            buildParam.consumeTouchEventOnMove = consumeTouchEventOnMove;
            return this;
        }

        public PLVFloatingWindowManager build() {
            PLVFloatingWindowManager.getInstance().floatingLayout = buildParam.create();
            return PLVFloatingWindowManager.getInstance();
        }

        private static class Param {
            private Activity activity;
            private boolean isSystemWindow = true;
            private View contentView;
            private int width;
            private int height;
            private int left;
            private int top;
            private PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ONLY_BACKGROUND;
            private PLVFloatingEnums.AutoEdgeType autoEdgeType = PLVFloatingEnums.AutoEdgeType.AUTO_MOVE_TO_RIGHT;
            private boolean enableDrag = true;
            private boolean consumeTouchEventOnMove = true;

            private PLVAbsFloatingLayout create() {
                final PLVAbsFloatingLayout floatingLayout;
                if (isSystemWindow) {
                    floatingLayout = new PLVSystemFloatingLayout(activity);
                } else {
                    floatingLayout = new PLVAppFloatingLayout(activity);
                }
                ViewGroup.LayoutParams layoutParams = floatingLayout.getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                layoutParams.width = width;
                layoutParams.height = height;
                floatingLayout.setLayoutParams(layoutParams);
                floatingLayout.setContentView(contentView);
                floatingLayout.updateFloatSize(width, height);
                floatingLayout.updateFloatLocation(left, top);
                floatingLayout.setShowType(showType);
                floatingLayout.setAutoMoveToEdge(autoEdgeType);
                floatingLayout.setEnableDrag(enableDrag);
                floatingLayout.setConsumeTouchEventOnMove(consumeTouchEventOnMove);

                return floatingLayout;
            }
        }

    }

    // </editor-fold>

}
