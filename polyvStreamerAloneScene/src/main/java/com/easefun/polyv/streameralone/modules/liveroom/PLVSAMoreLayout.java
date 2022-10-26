package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
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
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.chatroom.IPLVChatroomManager;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 更多布局
 */
public class PLVSAMoreLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 按钮表格每行显示数量
    private static final int GRID_COLUMN_COUNT_PORT = 5;
    private static final int GRID_COLUMN_COUNT_LAND = 3;
    // 弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 更多布局高度
    private static final int MORE_LAYOUT_HEIGHT_PORT = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int MORE_LAYOUT_HEIGHT_LAND = ViewGroup.LayoutParams.MATCH_PARENT;
    // 更多布局位置
    private static final int MORE_LAYOUT_GRAVITY_PORT = Gravity.BOTTOM;
    private static final int MORE_LAYOUT_GRAVITY_LAND = Gravity.END;
    // 更多布局背景
    private static final int MORE_LAYOUT_BACKGROUND_RES_PORT = R.drawable.plvsa_more_ly_shape;
    private static final int MORE_LAYOUT_BACKGROUND_RES_LAND = R.drawable.plvsa_more_ly_shape_land;

    //view
    private ConstraintLayout plvsaMoreLayout;
    private TextView plvsaMoreTextTv;
    private ViewGroup plvsaMoreSettingsSv;
    private GridLayout plvsaMoreSettingsLayout;
    private ImageView plvsaMoreCameraIv;
    private TextView plvsaMoreCameraTv;
    private ImageView plvsaMoreMicIv;
    private TextView plvsaMoreMicTv;
    private ImageView plvsaMoreCameraSwitchIv;
    private TextView plvsaMoreCameraSwitchTv;
    private ImageView plvsaMoreMirrorIv;
    private TextView plvsaMoreMirrorTv;
    private ImageView plvsaMoreFlashlightIv;
    private TextView plvsaMoreFlashlightTv;
    private ImageView plvsaMoreBitrateIv;
    private TextView plvsaMoreBitrateTv;
    private ImageView plvsaMoreCloseRoomIv;
    private TextView plvsaMoreCloseRoomTv;
    private View plvsaMoreCloseRoomLayout;
    private View plvsaMoreShareScreenLl;
    private ImageView plvsaMoreShareScreenIv;
    private TextView plvsaMoreShareScreenTv;
    private LinearLayout moreBeautyLl;
    private LinearLayout moreShareLl;

    //streamerPresenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //清晰度设置布局
    private PLVSABitrateLayout bitrateLayout;

    //分享布局
    private PLVSAShareLayout shareLayout;

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    private IPLVChatroomManager.RoomStatusListener roomStatusListener;

    // 标记位 当用户手动切换清晰度时为true 避免进入设置页时内部初始化清晰度弹出toast提示
    private boolean switchBitrateByUser = false;
    // 标记位
    private boolean attachedToWindow = false;

    private boolean isEnableVideo = true;

    private long lastClickCameraSwitchViewTime;

    /**
     * 限制每800ms点击一次
     **/
    private static final long QUICK_CLICK_LIMIT_TIME = 800;
    private long lastClickTime = 0;
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

        plvsaMoreLayout = (ConstraintLayout) findViewById(R.id.plvsa_more_layout);
        plvsaMoreTextTv = (TextView) findViewById(R.id.plvsa_more_text_tv);
        plvsaMoreSettingsSv = findViewById(R.id.plvsa_more_settings_sv);
        plvsaMoreSettingsLayout = (GridLayout) findViewById(R.id.plvsa_more_settings_layout);
        plvsaMoreCameraIv = (ImageView) findViewById(R.id.plvsa_more_camera_iv);
        plvsaMoreCameraTv = (TextView) findViewById(R.id.plvsa_more_camera_tv);
        plvsaMoreMicIv = (ImageView) findViewById(R.id.plvsa_more_mic_iv);
        plvsaMoreMicTv = (TextView) findViewById(R.id.plvsa_more_mic_tv);
        plvsaMoreCameraSwitchIv = (ImageView) findViewById(R.id.plvsa_more_camera_switch_iv);
        plvsaMoreCameraSwitchTv = (TextView) findViewById(R.id.plvsa_more_camera_switch_tv);
        plvsaMoreMirrorIv = (ImageView) findViewById(R.id.plvsa_more_mirror_iv);
        plvsaMoreMirrorTv = (TextView) findViewById(R.id.plvsa_more_mirror_tv);
        plvsaMoreFlashlightIv = (ImageView) findViewById(R.id.plvsa_more_flashlight_iv);
        plvsaMoreFlashlightTv = (TextView) findViewById(R.id.plvsa_more_flashlight_tv);
        plvsaMoreBitrateIv = (ImageView) findViewById(R.id.plvsa_more_bitrate_iv);
        plvsaMoreBitrateTv = (TextView) findViewById(R.id.plvsa_more_bitrate_tv);
        plvsaMoreCloseRoomIv = (ImageView) findViewById(R.id.plvsa_more_close_room_iv);
        plvsaMoreCloseRoomTv = (TextView) findViewById(R.id.plvsa_more_close_room_tv);
        plvsaMoreCloseRoomLayout = findViewById(R.id.plvsa_more_close_room_layout);
        plvsaMoreShareScreenLl = findViewById(R.id.plvsa_more_share_screen_ll);
        plvsaMoreShareScreenIv = findViewById(R.id.plvsa_more_share_screen_iv);
        plvsaMoreShareScreenTv = findViewById(R.id.plvsa_more_share_screen_tv);
        moreBeautyLl = findViewById(R.id.plvsa_more_beauty_ll);
        moreShareLl = findViewById(R.id.plvsa_more_share_layout);


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
        plvsaMoreShareScreenLl.setOnClickListener(this);
        moreBeautyLl.setOnClickListener(this);
        moreShareLl.setOnClickListener(this);

        plvsaMoreCloseRoomIv.setSelected(PolyvChatroomManager.getInstance().isCloseRoom());
        plvsaMoreCloseRoomTv.setText(plvsaMoreCloseRoomIv.isSelected() ? "取消全体禁言" : "开启全体禁言");

        if(!PLVLinkMicConfig.getInstance().isSupportScreenShare()){
            plvsaMoreSettingsLayout.removeView(plvsaMoreShareScreenLl);
        }

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

        //init shareLayout
        shareLayout = new PLVSAShareLayout(getContext());

        observeBeautyModuleInitResult();
        observeChatroomStatus();
    }

    private void observeChatroomStatus() {
        PolyvChatroomManager.getInstance().addOnRoomStatusListener(roomStatusListener = new IPLVChatroomManager.RoomStatusListener() {
            @Override
            public void onStatus(boolean isClose) {
                plvsaMoreCloseRoomIv.setSelected(isClose);
                plvsaMoreCloseRoomTv.setText(plvsaMoreCloseRoomIv.isSelected() ? "取消全体禁言" : "开启全体禁言");
            }
        });
    }

    private void observeBeautyModuleInitResult() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    private Boolean lastShowBeautyLayout = null;

                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        if (beautyUiState == null) {
                            return;
                        }
                        final boolean isBeautySupport = beautyUiState.isBeautySupport;
                        final boolean isInitSuccess = beautyUiState.isBeautyModuleInitSuccess;
                        final boolean showBeautyLayout = isBeautySupport && isInitSuccess;

                        if (lastShowBeautyLayout != null && lastShowBeautyLayout == showBeautyLayout) {
                            return;
                        }
                        lastShowBeautyLayout = showBeautyLayout;

                        moreBeautyLl.setVisibility(showBeautyLayout ? View.VISIBLE : View.GONE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            final GridLayout.LayoutParams lp = (GridLayout.LayoutParams) moreBeautyLl.getLayoutParams();
                            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, showBeautyLayout ? 1 : 0, 1F);
                            moreBeautyLl.setLayoutParams(lp);
                        }
                    }
                });
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (shareLayout != null) {
            shareLayout.init(liveRoomDataManager);
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
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
        } else {
            menuDrawer.attachToContainer();
        }

        updateViewWithOrientation();
        menuDrawer.openMenu();
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void updateCloseRoomLayout(boolean hide){
        plvsaMoreCloseRoomLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final GridLayout.LayoutParams lp = (GridLayout.LayoutParams) plvsaMoreCloseRoomLayout.getLayoutParams();
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, plvsaMoreCloseRoomLayout.getVisibility() == View.VISIBLE ? 1 : 0, 1F);
            plvsaMoreCloseRoomLayout.setLayoutParams(lp);
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
        if (shareLayout != null && shareLayout.onBackPressed()) {
            return true;
        }
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        if (shareLayout != null) {
            shareLayout.destroy();
        }
        PolyvChatroomManager.getInstance().removeOnRoomStatusListener(roomStatusListener);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull final IPLVStreamerContract.IStreamerPresenter presenter) {
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
                public void onChanged(@Nullable Boolean enableVideo) {
                    if (enableVideo == null) {
                        return;
                    }
                    PLVSAMoreLayout.this.isEnableVideo = enableVideo;

                    plvsaMoreCameraIv.setSelected(!enableVideo);
                    plvsaMoreCameraSwitchIv.setEnabled(enableVideo);
                    plvsaMoreMirrorIv.setEnabled(enableVideo && !plvsaMoreCameraSwitchIv.isSelected());

                    if (attachedToWindow) {
                        String toastText = "已" + (enableVideo ? "开启" : "关闭") + "摄像头";
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
            presenter.getData().getIsStartShareScreen().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isStartShare) {
                    if (isStartShare == null) {
                        return;
                    }
                    plvsaMoreShareScreenIv.setSelected(isStartShare);
                    plvsaMoreShareScreenTv.setText(isStartShare ? getContext().getString(R.string.plvsa_streamer_sharescreen_exit) : getContext().getString(R.string.plvsa_streamer_sharescreen_start));
                    //去掉摄像头可选项
                    plvsaMoreCameraIv.setClickable(!isStartShare);
                    plvsaMoreCameraTv.setClickable(!isStartShare);
                    plvsaMoreCameraSwitchIv.setClickable(!isStartShare);
                    plvsaMoreCameraSwitchTv.setClickable(!isStartShare);
                    plvsaMoreMirrorIv.setClickable(!isStartShare);
                    plvsaMoreMirrorTv.setClickable(!isStartShare);
                    //置灰
                    if(isStartShare) {
                        plvsaMoreCameraIv.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        plvsaMoreCameraSwitchIv.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        plvsaMoreMirrorIv.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    } else {
                        plvsaMoreCameraIv.clearColorFilter();
                        plvsaMoreCameraSwitchIv.clearColorFilter();
                        plvsaMoreMirrorIv.clearColorFilter();
                    }
                }
            });
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private boolean quickClickLimit() {
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= QUICK_CLICK_LIMIT_TIME) {
            lastClickTime = curClickTime;
            return false;
        }
        return true;
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.plvsa_more_camera_iv
                || id == R.id.plvsa_more_camera_tv) {
            if (!quickClickLimit()) {
                if (streamerPresenter != null) {
                    streamerPresenter.enableLocalVideo(plvsaMoreCameraIv.isSelected());
                }
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
            PolyvChatroomManager.getInstance().toggleRoomByEvent(new IPolyvChatroomManager.RequestApiListener<String>() {
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
        } else if (id == R.id.plvsa_more_share_screen_ll){
            close();
            if(!PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_GRANT_PERMISSION_SHARE_SCREEN)){
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plvsa_streamer_sharescreen_need_permission))
                        .build()
                        .show();
                return;
            }

            LiveData<Boolean> enableVideo = streamerPresenter.getData().getEnableVideo();
            if(enableVideo != null && enableVideo.getValue() != null){
                if(!enableVideo.getValue()){
                    //屏幕共享需要打开摄像头
                    PLVToast.Builder.context(getContext())
                            .setText(getContext().getString(R.string.plvsa_streamer_sharescreen_need_video_first))
                            .build()
                            .show();
                    return;
                }
            }

            //开始屏幕共享
            if (streamerPresenter != null) {
                if (!plvsaMoreShareScreenIv.isSelected()) {
                    streamerPresenter.requestShareScreen((Activity) getContext());
                } else {
                    streamerPresenter.exitShareScreen();
                }
            }
        } else if (id == moreBeautyLl.getId()) {
            close();
            if (!isEnableVideo) {
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plvsa_beauty_need_open_camera))
                        .show();
                return;
            }
            PLVDependManager.getInstance().get(PLVBeautyViewModel.class).showBeautyMenu();
        } else if (id == moreShareLl.getId()) {
            close();
            shareLayout.open();
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

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        Position menuPosition;
        FrameLayout.LayoutParams moreLayoutParam = (FrameLayout.LayoutParams) plvsaMoreLayout.getLayoutParams();
        ConstraintLayout.LayoutParams settingLayoutParam = (ConstraintLayout.LayoutParams) plvsaMoreSettingsSv.getLayoutParams();

        if (PLVScreenUtils.isPortrait(getContext())) {
            menuPosition = MENU_DRAWER_POSITION_PORT;
            moreLayoutParam.height = MORE_LAYOUT_HEIGHT_PORT;
            moreLayoutParam.gravity = MORE_LAYOUT_GRAVITY_PORT;
            plvsaMoreSettingsLayout.setColumnCount(GRID_COLUMN_COUNT_PORT);
            plvsaMoreLayout.setBackgroundResource(MORE_LAYOUT_BACKGROUND_RES_PORT);
            settingLayoutParam.topMargin = ConvertUtils.dp2px(10);
        } else {
            menuPosition = MENU_DRAWER_POSITION_LAND;
            moreLayoutParam.height = MORE_LAYOUT_HEIGHT_LAND;
            moreLayoutParam.gravity = MORE_LAYOUT_GRAVITY_LAND;
            plvsaMoreSettingsLayout.setColumnCount(GRID_COLUMN_COUNT_LAND);
            plvsaMoreLayout.setBackgroundResource(MORE_LAYOUT_BACKGROUND_RES_LAND);
            settingLayoutParam.topToBottom = R.id.plvsa_more_text_tv;
            settingLayoutParam.topMargin = ConvertUtils.dp2px(plvsaMoreSettingsLayout.getChildCount() > 9 ? 48 : 0);
        }

        if (menuDrawer != null) {
            menuDrawer.setPosition(menuPosition);
        }
        plvsaMoreLayout.setLayoutParams(moreLayoutParam);
        plvsaMoreSettingsSv.setLayoutParams(settingLayoutParam);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
    }
    // </editor-fold>
}
