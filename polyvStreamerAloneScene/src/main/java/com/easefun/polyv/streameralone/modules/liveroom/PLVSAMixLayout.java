package com.easefun.polyv.streameralone.modules.liveroom;

import static com.plv.foundationsdk.ext.PLVViewGroupExt.children;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.streamer.model.enums.PLVStreamerMixBackground;
import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerPreferenceCardView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * 混流布局设置布局
 */
public class PLVSAMixLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 混流布局弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 混流布局宽度、高度、布局位置
    private static final int MIX_LAYOUT_WIDTH_PORT = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int MIX_LAYOUT_WIDTH_LAND = ConvertUtils.dp2px(375);
    private static final int MIX_LAYOUT_HEIGHT_PORT = ConvertUtils.dp2px(462);
    private static final int MIX_LAYOUT_HEIGHT_LAND = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int MIX_LAYOUT_GRAVITY_PORT = Gravity.BOTTOM;
    private static final int MIX_LAYOUT_GRAVITY_LAND = Gravity.END;
    // 混流布局背景
    private static final int MIX_LAYOUT_BACKGROUND_RES_PORT = R.drawable.plvsa_setting_bitrate_ly_shape;
    private static final int MIX_LAYOUT_BACKGROUND_RES_LAND = R.drawable.plvsa_setting_bitrate_ly_shape_land;

    //布局弹层
    private PLVMenuDrawer menuDrawer;

    //view
    private ConstraintLayout settingMixLayoutRoot;
    private TextView settingMixTitle;
    private TextView settingMixDesc;
    private PLVStreamerPreferenceCardView settingMixSpeakerCard;
    private PLVStreamerPreferenceCardView settingMixTileCard;
    private PLVStreamerPreferenceCardView settingMixSingleCard;
    private PLVStreamerPreferenceCardView settingMixListRightCard;
    private PLVStreamerPreferenceCardView settingMixListBottomCard;
    private TextView settingMixBackgroundTitle;
    private LinearLayout settingMixBackgroundContainer;

    private String channelId;

    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAMixLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAMixLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAMixLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_setting_mix_layout, this, true);

        settingMixLayoutRoot = findViewById(R.id.plvsa_setting_mix_layout_root);
        settingMixTitle = findViewById(R.id.plvsa_setting_mix_title);
        settingMixDesc = findViewById(R.id.plvsa_setting_mix_desc);
        settingMixSpeakerCard = findViewById(R.id.plvsa_setting_mix_speaker_card);
        settingMixTileCard = findViewById(R.id.plvsa_setting_mix_tile_card);
        settingMixSingleCard = findViewById(R.id.plvsa_setting_mix_single_card);
        settingMixListRightCard = findViewById(R.id.plvsa_setting_mix_list_right_card);
        settingMixListBottomCard = findViewById(R.id.plvsa_setting_mix_list_bottom_card);
        settingMixBackgroundTitle = findViewById(R.id.plvsa_setting_mix_background_title);
        settingMixBackgroundContainer = findViewById(R.id.plvsa_setting_mix_background_container);

        settingMixSpeakerCard.setOnClickListener(this);
        settingMixTileCard.setOnClickListener(this);
        settingMixSingleCard.setOnClickListener(this);
        settingMixListRightCard.setOnClickListener(this);
        settingMixListBottomCard.setOnClickListener(this);

        initMixBackgroundItems();
    }

    private void initMixBackgroundItems() {
        for (PLVStreamerMixBackground mixBackground : PLVStreamerMixBackground.values()) {
            final MixBackgroundItemLayout itemLayout = new MixBackgroundItemLayout(getContext());
            itemLayout.setMixBackground(mixBackground);
            itemLayout.setOnClickListener(this);
            settingMixBackgroundContainer.addView(itemLayout);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
    }

    public void open() {
        // 更新混流布局数据
        updateSelectedState();
        updateViewWithOrientation();

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
        menuDrawer.setMenuSize(ScreenUtils.isPortrait() ? MIX_LAYOUT_HEIGHT_PORT : MIX_LAYOUT_WIDTH_LAND);
        menuDrawer.setPosition(ScreenUtils.isPortrait() ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND);
        menuDrawer.openMenu();
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

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    private void updateSelectedState() {
        if (onViewActionListener == null) {
            return;
        }
        final PLVStreamerConfig.MixLayoutType currentMixLayoutType = onViewActionListener.getMixLayoutType();
        settingMixSpeakerCard.setSelected(currentMixLayoutType == PLVStreamerConfig.MixLayoutType.SPEAKER);
        settingMixTileCard.setSelected(currentMixLayoutType == PLVStreamerConfig.MixLayoutType.TILE);
        settingMixSingleCard.setSelected(currentMixLayoutType == PLVStreamerConfig.MixLayoutType.SINGLE);
        settingMixListRightCard.setSelected(currentMixLayoutType == PLVStreamerConfig.MixLayoutType.SPEAKER_LIST_RIGHT);
        settingMixListBottomCard.setSelected(currentMixLayoutType == PLVStreamerConfig.MixLayoutType.SPEAKER_LIST_BOTTOM);
        final PLVStreamerMixBackground currentMixBackground = onViewActionListener.getMixBackground();
        for (View mixBackgroundItem : children(settingMixBackgroundContainer)) {
            if (mixBackgroundItem instanceof MixBackgroundItemLayout) {
                ((MixBackgroundItemLayout) mixBackgroundItem).onCurrentSelectedMixBackground(currentMixBackground);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        LayoutParams mixLayoutParam = (LayoutParams) settingMixLayoutRoot.getLayoutParams();

        if (ScreenUtils.isPortrait()) {
            mixLayoutParam.width = MIX_LAYOUT_WIDTH_PORT;
            mixLayoutParam.height = MIX_LAYOUT_HEIGHT_PORT;
            mixLayoutParam.gravity = MIX_LAYOUT_GRAVITY_PORT;
            settingMixLayoutRoot.setBackgroundResource(MIX_LAYOUT_BACKGROUND_RES_PORT);
        } else {
            mixLayoutParam.width = MIX_LAYOUT_WIDTH_LAND;
            mixLayoutParam.height = MIX_LAYOUT_HEIGHT_LAND;
            mixLayoutParam.gravity = MIX_LAYOUT_GRAVITY_LAND;
            settingMixLayoutRoot.setBackgroundResource(MIX_LAYOUT_BACKGROUND_RES_LAND);
        }

        settingMixLayoutRoot.setLayoutParams(mixLayoutParam);
    }

    // </editor-fold>

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == settingMixSpeakerCard.getId()) {
            changeMixLayoutType(PLVStreamerConfig.MixLayoutType.SPEAKER);
        } else if (id == settingMixTileCard.getId()) {
            changeMixLayoutType(PLVStreamerConfig.MixLayoutType.TILE);
        } else if (id == settingMixSingleCard.getId()) {
            changeMixLayoutType(PLVStreamerConfig.MixLayoutType.SINGLE);
        } else if (id == settingMixListRightCard.getId()) {
            changeMixLayoutType(PLVStreamerConfig.MixLayoutType.SPEAKER_LIST_RIGHT);
        } else if (id == settingMixListBottomCard.getId()) {
            changeMixLayoutType(PLVStreamerConfig.MixLayoutType.SPEAKER_LIST_BOTTOM);
        } else if (v instanceof MixBackgroundItemLayout) {
            changeMixBackground(((MixBackgroundItemLayout) v).getMixBackground());
        }
    }

    private void changeMixLayoutType(PLVStreamerConfig.MixLayoutType mixLayoutType) {
        if (!PLVNetworkUtils.isConnected(getContext())) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_network_bad)
                    .build()
                    .show();
            return;
        }
        if (onViewActionListener == null) {
            return;
        }
        final PLVStreamerConfig.MixLayoutType currentMixLayoutType = onViewActionListener.getMixLayoutType();
        if (currentMixLayoutType != mixLayoutType) {
            onViewActionListener.onChangeMixLayoutType(mixLayoutType);
            updateSelectedState();
        }
    }

    private void changeMixBackground(PLVStreamerMixBackground mixBackground) {
        if (!PLVNetworkUtils.isConnected(getContext())) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_network_bad)
                    .build()
                    .show();
            return;
        }
        if (onViewActionListener == null) {
            return;
        }
        final PLVStreamerMixBackground currentMixBackground = onViewActionListener.getMixBackground();
        if (currentMixBackground != mixBackground) {
            onViewActionListener.onChangeMixBackground(mixBackground);
            updateSelectedState();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="内部类 - 连麦背景item">
    public class MixBackgroundItemLayout extends FrameLayout {
        private PLVStreamerPreferenceCardView settingMixBackgroundItemRoot;
        private ImageView settingMixBackgroundItemIv;
        private TextView settingMixBackgroundItemTv;

        private PLVStreamerMixBackground mixBackground;

        public MixBackgroundItemLayout(Context context) {
            super(context);
            init();
        }

        public MixBackgroundItemLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public MixBackgroundItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_setting_mix_background_item, this);
            settingMixBackgroundItemRoot = findViewById(R.id.plvsa_setting_mix_background_item_root);
            settingMixBackgroundItemIv = findViewById(R.id.plvsa_setting_mix_background_item_iv);
            settingMixBackgroundItemTv = findViewById(R.id.plvsa_setting_mix_background_item_tv);
        }

        public void setMixBackground(PLVStreamerMixBackground mixBackground) {
            this.mixBackground = mixBackground;
            settingMixBackgroundItemTv.setText(mixBackground.getDisplayName());
            PLVImageLoader.getInstance().loadImage(mixBackground.getUrl(), settingMixBackgroundItemIv);
        }

        public PLVStreamerMixBackground getMixBackground() {
            return mixBackground;
        }

        public void onCurrentSelectedMixBackground(PLVStreamerMixBackground mixBackground) {
            boolean selected = mixBackground == this.mixBackground;
            settingMixBackgroundItemRoot.setSelected(selected);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        PLVStreamerConfig.MixLayoutType getMixLayoutType();
        void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix);

        PLVStreamerMixBackground getMixBackground();

        void onChangeMixBackground(PLVStreamerMixBackground mixBackground);
    }
    // </editor-fold>
}
