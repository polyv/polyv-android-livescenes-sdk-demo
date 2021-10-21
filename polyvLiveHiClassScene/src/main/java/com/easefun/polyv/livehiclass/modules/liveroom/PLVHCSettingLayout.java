package com.easefun.polyv.livehiclass.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.view.PLVAbsMultiRoleLinkMicView;
import com.easefun.polyv.livecommon.ui.widget.PLVOutsideTouchableLayout;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 设置布局
 */
public class PLVHCSettingLayout extends FrameLayout implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private Switch plvhcSettingMicSw;
    private ImageView plvhcSettingCameraIv;
    private Switch plvhcSettingCameraSw;
    private ImageView plvhcSettingCameraOrientIv;
    private TextView plvhcSettingCameraOrientTv;
    private ViewGroup plvhcSettingCameraOrientSw;
    private ImageView plvhcSettingScreenFullIv;
    private Switch plvhcSettingScreenFullSw;
    private TextView plvhcSettingExitTv;
    private TextView plvhcWidgetFrontTv;
    private TextView plvhcWidgetBackTv;

    //presenter
    private IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter linkMicPresenter;
    //container
    private PLVOutsideTouchableLayout container;
    //listener
    private OnViewActionListener onViewActionListener;

    private long lastClickCameraSwitchViewTime;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCSettingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_setting_layout, this);

        plvhcSettingMicSw = findViewById(R.id.plvhc_setting_mic_sw);
        plvhcSettingCameraIv = findViewById(R.id.plvhc_setting_camera_iv);
        plvhcSettingCameraSw = findViewById(R.id.plvhc_setting_camera_sw);
        plvhcSettingCameraOrientIv = findViewById(R.id.plvhc_setting_camera_orient_iv);
        plvhcSettingCameraOrientTv = findViewById(R.id.plvhc_setting_camera_orient_tv);
        plvhcSettingCameraOrientSw = findViewById(R.id.plvhc_setting_camera_orient_sw);
        plvhcSettingScreenFullIv = findViewById(R.id.plvhc_setting_screen_full_iv);
        plvhcSettingScreenFullSw = findViewById(R.id.plvhc_setting_screen_full_sw);
        plvhcSettingExitTv = findViewById(R.id.plvhc_setting_enter_tv);
        plvhcWidgetFrontTv = findViewById(R.id.plvhc_widget_front_tv);
        plvhcWidgetBackTv = findViewById(R.id.plvhc_widget_back_tv);

        plvhcSettingMicSw.setOnCheckedChangeListener(this);
        plvhcSettingCameraSw.setOnCheckedChangeListener(this);
        plvhcSettingScreenFullSw.setOnCheckedChangeListener(this);
        plvhcWidgetFrontTv.setSelected(true);
        plvhcWidgetFrontTv.setOnClickListener(this);
        plvhcWidgetBackTv.setOnClickListener(this);
        plvhcSettingExitTv.setOnClickListener(this);
    }

    private void initViewLocation(int addMargin) {
        MarginLayoutParams cameraIvLp = (MarginLayoutParams) plvhcSettingCameraIv.getLayoutParams();
        cameraIvLp.topMargin = cameraIvLp.topMargin + addMargin;
        plvhcSettingCameraIv.setLayoutParams(cameraIvLp);

        MarginLayoutParams cameraOrientLp = (MarginLayoutParams) plvhcSettingCameraOrientIv.getLayoutParams();
        cameraOrientLp.topMargin = cameraOrientLp.topMargin + addMargin;
        plvhcSettingCameraOrientIv.setLayoutParams(cameraOrientLp);

        MarginLayoutParams screenFullIvLp = (MarginLayoutParams) plvhcSettingScreenFullIv.getLayoutParams();
        screenFullIvLp.topMargin = screenFullIvLp.topMargin + addMargin;
        plvhcSettingScreenFullIv.setLayoutParams(screenFullIvLp);

        MarginLayoutParams exitTvLp = (MarginLayoutParams) plvhcSettingExitTv.getLayoutParams();
        exitTvLp.topMargin = exitTvLp.topMargin + addMargin;
        plvhcSettingExitTv.setLayoutParams(exitTvLp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void show(int viewWidth, int viewHeight, int[] viewLocation) {
        int height = viewHeight - ConvertUtils.dp2px(16);
        int width = ConvertUtils.dp2px(203);

        if (container == null) {
            container = ((Activity) getContext()).findViewById(R.id.plvhc_live_room_popup_container);
            container.addOnDismissListener(new PLVOutsideTouchableLayout.OnOutsideDismissListener(this) {
                @Override
                public void onDismiss() {
                    hide();
                }
            });

            int addMargin = (height - ConvertUtils.dp2px(255)) / 4;
            initViewLocation(addMargin);
        }

        FrameLayout.LayoutParams lp = new LayoutParams(width, height);
        lp.rightMargin = ConvertUtils.dp2px(66);
        lp.bottomMargin = ConvertUtils.dp2px(8);
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        setLayoutParams(lp);

        container.removeAllViews();
        container.addView(this);

        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(true);
        }
    }

    public void hide() {
        if (container != null) {
            container.removeAllViews();
        }
        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(false);
        }
    }

    public void initDefaultMediaStatus(boolean isMuteAudio, boolean isMuteVideo, boolean isFrontCamera) {
        updateCameraView(isMuteVideo);
        updateAudioView(isMuteAudio);
        updateCameraOrientView(isFrontCamera);
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView getLinkMicView() {
        return linkMicView;
    }

    public boolean onBackPressed() {
        if (isShown()) {
            hide();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦 - MVP模式的view层实现">
    private PLVAbsMultiRoleLinkMicView linkMicView = new PLVAbsMultiRoleLinkMicView() {

        @Override
        public void setPresenter(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter presenter) {
            linkMicPresenter = presenter;
            linkMicPresenter.getData().getEnableVideo().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    updateCameraView(!aBoolean);
                }
            });
            linkMicPresenter.getData().getEnableAudio().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    updateAudioView(!aBoolean);
                }
            });
            linkMicPresenter.getData().getIsFrontCamera().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    updateCameraOrientView(aBoolean);
                }
            });
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void updateCameraView(boolean isMuteVideo) {
        plvhcSettingCameraSw.setChecked(!isMuteVideo);
        plvhcSettingCameraOrientIv.setVisibility(!isMuteVideo ? View.VISIBLE : View.GONE);
        plvhcSettingCameraOrientTv.setVisibility(!isMuteVideo ? View.VISIBLE : View.GONE);
        plvhcSettingCameraOrientSw.setVisibility(!isMuteVideo ? View.VISIBLE : View.GONE);
        MarginLayoutParams cameraOrientIvLp = (MarginLayoutParams) plvhcSettingCameraOrientIv.getLayoutParams();
        MarginLayoutParams exitTvLp = (MarginLayoutParams) plvhcSettingExitTv.getLayoutParams();
        if (exitTvLp != null && cameraOrientIvLp != null) {
            if (!isMuteVideo) {
                if (plvhcSettingExitTv.getTag() instanceof Boolean) {
                    exitTvLp.topMargin = exitTvLp.topMargin - ConvertUtils.dp2px(24) - cameraOrientIvLp.topMargin;
                    plvhcSettingExitTv.setTag(null);
                }
            } else {
                if (plvhcSettingExitTv.getTag() == null) {
                    exitTvLp.topMargin = exitTvLp.topMargin + ConvertUtils.dp2px(24) + cameraOrientIvLp.topMargin;
                    plvhcSettingExitTv.setTag(true);
                }
            }
        }
        plvhcSettingExitTv.setLayoutParams(exitTvLp);
    }

    private void updateAudioView(boolean isMuteVideo) {
        plvhcSettingMicSw.setChecked(!isMuteVideo);
    }

    private void updateCameraOrientView(boolean isFrontCamera) {
        plvhcWidgetFrontTv.setSelected(isFrontCamera);
        plvhcWidgetBackTv.setSelected(!isFrontCamera);
    }

    private boolean checkInClassStatus(String toastMsg) {
        if (linkMicPresenter != null) {
            if (!linkMicPresenter.isInClassStatus() && !linkMicPresenter.isTeacherType()) {
                PLVHCToast.Builder.context(getContext())
                        .setText(toastMsg)
                        .build()
                        .show();
                return false;
            }
        }
        return true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvhc_widget_front_tv
                || id == R.id.plvhc_widget_back_tv) {
            if (!checkInClassStatus("未上台无法设置摄像头方向")) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickCameraSwitchViewTime > 500) {
                if (linkMicPresenter != null) {
                    linkMicPresenter.switchCamera(v == plvhcWidgetFrontTv);
                }
                lastClickCameraSwitchViewTime = currentTime;
            }
        } else if (id == R.id.plvhc_setting_enter_tv) {
            hide();
            ((Activity) getContext()).onBackPressed();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!buttonView.isPressed()) {
            return;
        }
        int id = buttonView.getId();
        if (id == R.id.plvhc_setting_mic_sw) {
            if (!checkInClassStatus("未上台无法设置麦克风")) {
                buttonView.setChecked(!buttonView.isChecked());
                return;
            }
            if (linkMicPresenter != null) {
                boolean result = linkMicPresenter.muteAudio(!isChecked);
                if (result) {
                    PLVHCToast.Builder.context(getContext())
                            .setDrawable(isChecked ? R.drawable.plvhc_member_mic : R.drawable.plvhc_member_mic_sel)
                            .setText((isChecked ? "已开启" : "已关闭") + "麦克风")
                            .build()
                            .show();
                } else {
                    buttonView.setChecked(!buttonView.isChecked());
                }
            }
        } else if (id == R.id.plvhc_setting_camera_sw) {
            if (!checkInClassStatus("未上台无法设置摄像头")) {
                buttonView.setChecked(!buttonView.isChecked());
                return;
            }
            if (linkMicPresenter != null) {
                boolean result = linkMicPresenter.muteVideo(!isChecked);
                if (result) {
                    PLVHCToast.Builder.context(getContext())
                            .setDrawable(isChecked ? R.drawable.plvhc_member_camera : R.drawable.plvhc_member_camera_sel)
                            .setText((isChecked ? "已开启" : "已关闭") + "摄像头")
                            .build()
                            .show();
                } else {
                    buttonView.setChecked(!buttonView.isChecked());
                }
            }
        } else if (id == R.id.plvhc_setting_screen_full_sw) {
            if (onViewActionListener != null) {
                onViewActionListener.onFullScreenControl(isChecked);
            }
            PLVHCToast.Builder.context(getContext())
                    .setText(isChecked ? "已开启全屏模式" : "退出全屏模式")
                    .build()
                    .show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">

    /**
     * view交互事件监听器
     */
    public interface OnViewActionListener {
        /**
         * 可见性改变回调
         *
         * @param isVisible true：显示，false：隐藏
         */
        void onVisibilityChanged(boolean isVisible);

        /**
         * 全屏控制
         *
         * @param isFullScreen true：全屏，false：退出全屏
         */
        void onFullScreenControl(boolean isFullScreen);
    }
    // </editor-fold>
}
