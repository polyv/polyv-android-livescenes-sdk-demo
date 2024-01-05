package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.liveecommerce.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 播放速度弹窗View
 */
public class PLVSpeedPopupView {
    private PopupWindow popupWindow;
    private View view;
    //回放切换倍速布局
    private ViewGroup changeSpeedLy;
    private SparseArray<Float> speedArray;
    private OnViewActionListener onViewActionListener;
    private PLVOrientationSensibleLinearLayout orientationLayout;

    public PLVSpeedPopupView(View anchor) {
        speedArray = new SparseArray<>();
        speedArray.put(0, 0.5f);
        speedArray.put(1, 1f);
        speedArray.put(2, 1.25f);
        speedArray.put(3, 1.5f);
        speedArray.put(4, 2.0f);
        popupWindow = new PopupWindow(anchor.getContext());
        view = PLVViewInitUtils.initPopupWindow(anchor, R.layout.plvec_playback_more_speed_change_layout, popupWindow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
        changeSpeedLy = view.findViewById(R.id.change_speed_ly);
        orientationLayout = view.findViewById(R.id.plvec_playback_more_speed_orientation_ly);

        orientationLayout.setOnPortrait(new Runnable() {
            @Override
            public void run() {
                initPortrait();
            }
        });

        orientationLayout.setOnLandscape(new Runnable() {
            @Override
            public void run() {
                initLandscape();
            }
        });
    }

    private void initPortrait() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }

        View linesRoundBg = view.findViewById(R.id.plvec_widget_round_ly);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) linesRoundBg.getLayoutParams();
        layoutParams.height = ConvertUtils.dp2px(130);
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        linesRoundBg.setLayoutParams(layoutParams);

        View morely = view.findViewById(R.id.more_ly);
        RelativeLayout.LayoutParams morelyParams = (RelativeLayout.LayoutParams) morely.getLayoutParams();
        morelyParams.height = ConvertUtils.dp2px(130);
        morelyParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        morelyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        morelyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        morely.setLayoutParams(morelyParams);

    }

    private void initLandscape() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        View linesRoundBg = view.findViewById(R.id.plvec_widget_round_ly);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) linesRoundBg.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ConvertUtils.dp2px(375);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        linesRoundBg.setLayoutParams(layoutParams);

        View morely = view.findViewById(R.id.more_ly);
        RelativeLayout.LayoutParams morelyParams = (RelativeLayout.LayoutParams) morely.getLayoutParams();
        morelyParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        morelyParams.width = ConvertUtils.dp2px(375);
        morelyParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        morelyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        morely.setLayoutParams(morelyParams);
    }

    public void show(boolean isOnLandscape, float currentSpeed, OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        updateSpeedView(currentSpeed);
        if (isOnLandscape) {
            initLandscape();
            popupWindow.showAtLocation(view, Gravity.RIGHT, 0, 0);
        } else {
            initPortrait();
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        }
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public void updateSpeedView(float currentSpeed) {
        if (changeSpeedLy != null) {
            for (int i = 0; i < changeSpeedLy.getChildCount(); i++) {
                View view = changeSpeedLy.getChildAt(i);
                final int finalI = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateSpeedView(speedArray.get(finalI));
                        if (onViewActionListener != null) {
                            onViewActionListener.onChangeSpeedClick(v, speedArray.get(finalI));
                        }
                    }
                });
                view.setSelected(false);
                if (speedArray.valueAt(i).equals(currentSpeed)) {
                    view.setSelected(true);
                }
            }
        }
    }

    public interface OnViewActionListener {
        void onChangeSpeedClick(View v, float speed);
    }
}
