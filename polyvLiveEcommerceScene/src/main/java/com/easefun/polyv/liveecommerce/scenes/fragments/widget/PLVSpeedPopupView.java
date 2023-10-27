package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.liveecommerce.R;

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
    }

    public void show(float currentSpeed, OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        updateSpeedView(currentSpeed);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
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
