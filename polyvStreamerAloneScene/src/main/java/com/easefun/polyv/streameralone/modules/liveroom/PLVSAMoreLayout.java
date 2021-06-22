package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.chatroom.IPolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.streameralone.R;

/**
 * 更多布局
 */
public class PLVSAMoreLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private ImageView plvsaMoreCameraIv;
    private ImageView plvsaMoreMicIv;
    private ImageView plvsaMoreCameraSwitchIv;
    private ImageView plvsaMoreMirrorIv;
    private ImageView plvsaMoreFlashlightIv;
    private TextView plvsaMoreCameraTv;
    private TextView plvsaMoreMicTv;
    private TextView plvsaMoreCameraSwitchTv;
    private TextView plvsaMoreMirrorTv;
    private TextView plvsaMoreFlashlightTv;
    private ImageView plvsaMoreBitrateIv;
    private ImageView plvsaMoreCloseRoomIv;
    private TextView plvsaMoreBitrateTv;
    private TextView plvsaMoreCloseRoomTv;

    //streamerPresenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //清晰度设置布局
    private PLVSABitrateLayout bitrateLayout;

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;

    // 标记位 当用户手动切换清晰度时为true 避免进入设置页时内部初始化清晰度弹出toast提示
    private boolean switchBitrateByUser = false;
    // 标记位
    private boolean attachedToWindow = false;

    private long lastClickCameraSwitchViewTime;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAMoreLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAMoreLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAMoreLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_more_layout, this);

        plvsaMoreCameraIv = (ImageView) findViewById(R.id.plvsa_more_camera_iv);
        plvsaMoreMicIv = (ImageView) findViewById(R.id.plvsa_more_mic_iv);
        plvsaMoreCameraSwitchIv = (ImageView) findViewById(R.id.plvsa_more_camera_switch_iv);
        plvsaMoreMirrorIv = (ImageView) findViewById(R.id.plvsa_more_mirror_iv);
        plvsaMoreFlashlightIv = (ImageView) findViewById(R.id.plvsa_more_flashlight_iv);
        plvsaMoreCameraTv = (TextView) findViewById(R.id.plvsa_more_camera_tv);
        plvsaMoreMicTv = (TextView) findViewById(R.id.plvsa_more_mic_tv);
        plvsaMoreCameraSwitchTv = (TextView) findViewById(R.id.plvsa_more_camera_switch_tv);
        plvsaMoreMirrorTv = (TextView) findViewById(R.id.plvsa_more_mirror_tv);
        plvsaMoreFlashlightTv = (TextView) findViewById(R.id.plvsa_more_flashlight_tv);
        plvsaMoreBitrateIv = (ImageView) findViewById(R.id.plvsa_more_bitrate_iv);
        plvsaMoreCloseRoomIv = (ImageView) findViewById(R.id.plvsa_more_close_room_iv);
        plvsaMoreBitrateTv = (TextView) findViewById(R.id.plvsa_more_bitrate_tv);
        plvsaMoreCloseRoomTv = (TextView) findViewById(R.id.plvsa_more_close_room_tv);

        plvsaMoreCameraIv.setOnClickListener(this);
        plvsaMoreCameraTv.setOnClickListener(this);
        plvsaMoreMicIv.setOnClickListener(this);
        plvsaMoreMicTv.setOnClickListener(this);
        plvsaMoreCameraSwitchIv.setOnClickListener(this);
        plvsaMoreCameraSwitchTv.setOnClickListener(this);
        plvsaMoreMirrorIv.setOnClickListener(this);
        plvsaMoreMirrorTv.setOnClickListener(this);
        plvsaMoreFlashlightIv.setOnClickListener(this);
        plvsaMoreFlashlightTv.setOnClickListener(this);
        plvsaMoreBitrateIv.setOnClickListener(this);
        plvsaMoreBitrateTv.setOnClickListener(this);
        plvsaMoreCloseRoomIv.setOnClickListener(this);
        plvsaMoreCloseRoomTv.setOnClickListener(this);

        plvsaMoreCloseRoomIv.setSelected(PolyvChatroomManager.getInstance().isCloseRoom());
        plvsaMoreCloseRoomTv.setText(plvsaMoreCloseRoomIv.isSelected() ? "取消全体禁言" : "开启全体禁言");

        //init bitrateLayout
        bitrateLayout = new PLVSABitrateLayout(getContext());
        bitrateLayout.setOnViewActionListener(new PLVSABitrateLayout.OnViewActionListener() {
            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return streamerPresenter != null ? new Pair<>(streamerPresenter.getMaxBitrate(), streamerPresenter.getBitrate()) : null;
            }

            @Override
            public void onBitrateClick(int bitrate) {
                bitrateLayout.close();
                switchBitrateByUser = true;
                if (streamerPresenter != null) {
                    streamerPresenter.setBitrate(bitrate);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                    }

                    ViewGroup popupContainer = (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
                    View maskView = ((Activity) getContext()).findViewById(R.id.plvsa_popup_container_mask);
                    if (popupContainer.getChildCount() > 0) {
                        maskView.setVisibility(View.VISIBLE);
                    } else {
                        maskView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
            menuDrawer.openMenu();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            super.setPresenter(presenter);
            streamerPresenter = presenter;

            //添加获取当前码率的监听器
            presenter.getData().getCurBitrate().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Integer>() {
                @Override
                public void onChanged(@Nullable Integer bitrate) {
                    if (bitrate == null || getContext() == null) {
                        return;
                    }
                    String bitrateText = PLVSStreamerConfig.Bitrate.getText(bitrate);
                    plvsaMoreBitrateTv.setText(bitrateText);
                    switch (bitrate) {
                        case PLVSStreamerConfig.Bitrate.BITRATE_STANDARD:
                            plvsaMoreBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_sd);
                            break;
                        case PLVSStreamerConfig.Bitrate.BITRATE_HIGH:
                            plvsaMoreBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_hd);
                            break;
                        case PLVSStreamerConfig.Bitrate.BITRATE_SUPER:
                            plvsaMoreBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_uhd);
                            break;
                        default:
                    }

                    if (switchBitrateByUser) {
                        String toastText = "已切换为" + bitrateText;
                        PLVToast.Builder.context(getContext())
                                .setText(toastText)
                                .build().show();
                    }
                    switchBitrateByUser = false;
                }
            });
            //添加推流的媒体状态监听器
            presenter.getData().getEnableAudio().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    plvsaMoreMicIv.setSelected(!aBoolean);

                    if (attachedToWindow) {
                        String toastText = "已" + (aBoolean ? "开启" : "关闭") + "麦克风";
                        PLVToast.Builder.context(getContext())
                                .setText(toastText)
                                .build().show();
                    }
                }
            });
            presenter.getData().getEnableVideo().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    plvsaMoreCameraIv.setSelected(!aBoolean);
                    plvsaMoreCameraSwitchIv.setEnabled(aBoolean);
                    plvsaMoreMirrorIv.setEnabled(aBoolean && !plvsaMoreCameraSwitchIv.isSelected());

                    if (attachedToWindow) {
                        String toastText = "已" + (aBoolean ? "开启" : "关闭") + "摄像头";
                        PLVToast.Builder.context(getContext())
                                .setText(toastText)
                                .build().show();
                    }
                }
            });
            presenter.getData().getIsFrontCamera().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    plvsaMoreCameraSwitchIv.setSelected(!aBoolean);
                    plvsaMoreMirrorIv.setEnabled(aBoolean && !plvsaMoreCameraIv.isSelected());
                    plvsaMoreFlashlightIv.setEnabled(!aBoolean);
                    if (aBoolean) {
                        plvsaMoreFlashlightIv.setSelected(false);
                    }
                }
            });
            presenter.getData().getIsFrontMirrorMode().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    plvsaMoreMirrorIv.setSelected(!aBoolean);
                }
            });
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.plvsa_more_camera_iv
                || id == R.id.plvsa_more_camera_tv) {
            if (streamerPresenter != null) {
                streamerPresenter.enableLocalVideo(plvsaMoreCameraIv.isSelected());
            }
        } else if (id == R.id.plvsa_more_mic_iv
                || id == R.id.plvsa_more_mic_tv) {
            if (streamerPresenter != null) {
                streamerPresenter.enableRecordingAudioVolume(plvsaMoreMicIv.isSelected());
            }
        } else if (id == R.id.plvsa_more_camera_switch_iv
                || id == R.id.plvsa_more_camera_switch_tv) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickCameraSwitchViewTime > 500) {
                if (streamerPresenter != null) {
                    streamerPresenter.setCameraDirection(plvsaMoreCameraSwitchIv.isSelected());
                }
                lastClickCameraSwitchViewTime = currentTime;
            }
        } else if (id == R.id.plvsa_more_mirror_iv
                || id == R.id.plvsa_more_mirror_tv) {
            if (streamerPresenter != null) {
                streamerPresenter.setFrontCameraMirror(plvsaMoreMirrorIv.isSelected());
            }
        } else if (id == R.id.plvsa_more_flashlight_iv
                || id == R.id.plvsa_more_flashlight_tv) {
            if (streamerPresenter != null) {
                boolean result = streamerPresenter.enableTorch(!plvsaMoreFlashlightIv.isSelected());
                if (result) {
                    plvsaMoreFlashlightIv.setSelected(!plvsaMoreFlashlightIv.isSelected());
                }
            }
        } else if (id == R.id.plvsa_more_bitrate_iv
                || id == R.id.plvsa_more_bitrate_tv) {
            bitrateLayout.open();
        } else if (id == R.id.plvsa_more_close_room_iv
                || id == R.id.plvsa_more_close_room_tv) {
            PolyvChatroomManager.getInstance().toggleRoom(!plvsaMoreCloseRoomIv.isSelected(), new IPolyvChatroomManager.RequestApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    plvsaMoreCloseRoomIv.setSelected(!plvsaMoreCloseRoomIv.isSelected());
                    plvsaMoreCloseRoomTv.setText(plvsaMoreCloseRoomIv.isSelected() ? "取消全体禁言" : "开启全体禁言");

                    String toastText = "已" + (plvsaMoreCloseRoomIv.isSelected() ? "开启" : "解除") + "全体禁言";
                    PLVToast.Builder.context(getContext())
                            .setText(toastText)
                            .build().show();
                }

                @Override
                public void onFailed(Throwable t) {
                    PLVToast.Builder.context(getContext())
                            .setText("操作失败，请检查网络")
                            .build()
                            .show();
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View父类方法重写">

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
    }
    // </editor-fold>
}
