package com.easefun.polyv.livecommon.ui.util;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVTimeUnit;

/**
 * @author Hoshiiro
 */
public class PLVPopupHelper {

    private static final int TAG_VIEW_UTIL_SHOW_DURATION = R.id.plv_view_util_show_view_for_duration_tag;
    private static final int TAG_VIEW_UTIL_POPUP_WINDOW_OBJ = R.id.plv_view_util_popup_window_obj_tag;

    public static PopupWindow show(final View anchor, final View target, final ShowPopupConfig showPopupConfig) {
        removeFromParent(target);

        target.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int width = target.getMeasuredWidth();
        final int height = target.getMeasuredHeight();
        int[] popupLocation = showPopupConfig.position.layout(anchor, target, showPopupConfig);

        final PopupWindow popupWindow = new PopupWindow();
        popupWindow.setContentView(target);
        popupWindow.setWidth(width);
        popupWindow.setHeight(height);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setFocusable(showPopupConfig.focusable);
        popupWindow.setOutsideTouchable(showPopupConfig.outsideTouchable);

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, popupLocation[0], popupLocation[1]);

        if (showPopupConfig.autoHide > 0) {
            target.setTag(TAG_VIEW_UTIL_POPUP_WINDOW_OBJ, popupWindow);
            target.setTag(TAG_VIEW_UTIL_SHOW_DURATION, System.currentTimeMillis() + showPopupConfig.autoHide - 100);
            postToMainThread(showPopupConfig.autoHide, new Runnable() {
                @Override
                public void run() {
                    if (!(target.getTag(TAG_VIEW_UTIL_SHOW_DURATION) instanceof Long)) {
                        return;
                    }
                    final long ts = (long) target.getTag(TAG_VIEW_UTIL_SHOW_DURATION);
                    if (ts > System.currentTimeMillis()) {
                        return;
                    }
                    if (target.getTag(TAG_VIEW_UTIL_POPUP_WINDOW_OBJ) instanceof PopupWindow) {
                        final PopupWindow window = (PopupWindow) target.getTag(TAG_VIEW_UTIL_POPUP_WINDOW_OBJ);
                        window.dismiss();
                        target.setTag(TAG_VIEW_UTIL_POPUP_WINDOW_OBJ, null);
                    }
                }
            });
        } else {
            target.setTag(TAG_VIEW_UTIL_SHOW_DURATION, null);
        }
        return popupWindow;
    }

    private static void removeFromParent(final View target) {
        if (target.getParent() == null) {
            return;
        }
        if (target.getTag(TAG_VIEW_UTIL_POPUP_WINDOW_OBJ) instanceof PopupWindow) {
            final PopupWindow window = (PopupWindow) target.getTag(TAG_VIEW_UTIL_POPUP_WINDOW_OBJ);
            window.dismiss();
        } else {
            ((ViewGroup) target.getParent()).removeView(target);
        }
    }

    public static class ShowPopupConfig {
        private PopupPosition position;
        /**
         * 显示后自动隐藏，单位ms
         * 小于等于0不自动隐藏
         */
        private long autoHide = 0;
        private boolean focusable;
        private boolean outsideTouchable;
        private int marginLeft;
        private int marginRight;
        private int marginTop;
        private int marginBottom;

        public PopupPosition getPosition() {
            return position;
        }

        public ShowPopupConfig setPosition(PopupPosition position) {
            this.position = position;
            return this;
        }

        public long getAutoHide() {
            return autoHide;
        }

        public ShowPopupConfig setAutoHide(long autoHide) {
            this.autoHide = autoHide;
            return this;
        }

        public ShowPopupConfig setAutoHide(PLVTimeUnit autoHide) {
            this.autoHide = autoHide.toMillis();
            return this;
        }

        public boolean isFocusable() {
            return focusable;
        }

        public ShowPopupConfig setFocusable(boolean focusable) {
            this.focusable = focusable;
            return this;
        }

        public boolean isOutsideTouchable() {
            return outsideTouchable;
        }

        public ShowPopupConfig setOutsideTouchable(boolean outsideTouchable) {
            this.outsideTouchable = outsideTouchable;
            return this;
        }

        public int getMarginLeft() {
            return marginLeft;
        }

        public ShowPopupConfig setMarginLeft(int marginLeft) {
            this.marginLeft = marginLeft;
            return this;
        }

        public int getMarginRight() {
            return marginRight;
        }

        public ShowPopupConfig setMarginRight(int marginRight) {
            this.marginRight = marginRight;
            return this;
        }

        public int getMarginTop() {
            return marginTop;
        }

        public ShowPopupConfig setMarginTop(int marginTop) {
            this.marginTop = marginTop;
            return this;
        }

        public int getMarginBottom() {
            return marginBottom;
        }

        public ShowPopupConfig setMarginBottom(int marginBottom) {
            this.marginBottom = marginBottom;
            return this;
        }
    }

    public enum PopupPosition {
        LEFT_CENTER {
            @Override
            public int[] layout(View anchor, View target, ShowPopupConfig config) {
                int[] res = new int[2];
                anchor.getLocationInWindow(res);
                final int anchorLeft = res[0];
                final int anchorTop = res[1];
                final int anchorWidth = anchor.getMeasuredWidth();
                final int anchorHeight = anchor.getMeasuredHeight();
                final int targetWidth = target.getMeasuredWidth();
                final int targetHeight = target.getMeasuredHeight();
                res[0] = anchorLeft - config.marginRight - targetWidth;
                res[1] = anchorTop + anchorHeight / 2 - targetHeight / 2;
                return res;
            }
        },
        TOP_CENTER {
            @Override
            public int[] layout(View anchor, View target, ShowPopupConfig config) {
                int[] res = new int[2];
                anchor.getLocationInWindow(res);
                final int anchorLeft = res[0];
                final int anchorTop = res[1];
                final int anchorWidth = anchor.getMeasuredWidth();
                final int anchorHeight = anchor.getMeasuredHeight();
                final int targetWidth = target.getMeasuredWidth();
                final int targetHeight = target.getMeasuredHeight();
                res[0] = anchorLeft + anchorWidth / 2 - targetWidth / 2;
                res[1] = anchorTop - config.marginBottom - targetHeight;
                return res;
            }
        },

        ;

        public abstract int[] layout(final View anchor, final View target, final ShowPopupConfig config);
    }

}
