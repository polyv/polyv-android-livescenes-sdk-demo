package com.easefun.polyv.livestreamer.modules.liveroom;

import static com.plv.foundationsdk.ext.PLVViewGroupExt.children;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.streamer.model.enums.PLVStreamerMixBackground;
import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerPreferenceCardView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;

/**
 * 混流设置布局
 */
public class PLVLSMixLayout extends FrameLayout implements View.OnClickListener {

    private TextView mixTitleTv;
    private View mixTitleSeparator;
    private TextView settingMixDesc;
    private PLVStreamerPreferenceCardView settingMixSpeakerCard;
    private PLVStreamerPreferenceCardView settingMixTileCard;
    private PLVStreamerPreferenceCardView settingMixSingleCard;
    private PLVStreamerPreferenceCardView settingMixListRightCard;
    private PLVStreamerPreferenceCardView settingMixListBottomCard;
    private TextView settingMixBackgroundTitle;
    private LinearLayout settingMixBackgroundContainer;

    private String channelId;

    private OnViewActionListener onViewActionListener;

    public PLVLSMixLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLSMixLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLSMixLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_mix_layout, this);

        findView();
        settingMixSpeakerCard.setOnClickListener(this);
        settingMixTileCard.setOnClickListener(this);
        settingMixSingleCard.setOnClickListener(this);
        settingMixListRightCard.setOnClickListener(this);
        settingMixListBottomCard.setOnClickListener(this);

        initMixBackgroundItems();
    }

    private void findView() {
        mixTitleTv = findViewById(R.id.plvls_mix_title_tv);
        mixTitleSeparator = findViewById(R.id.plvls_mix_title_separator);
        settingMixDesc = findViewById(R.id.plvls_setting_mix_desc);
        settingMixSpeakerCard = findViewById(R.id.plvls_setting_mix_speaker_card);
        settingMixTileCard = findViewById(R.id.plvls_setting_mix_tile_card);
        settingMixSingleCard = findViewById(R.id.plvls_setting_mix_single_card);
        settingMixListRightCard = findViewById(R.id.plvls_setting_mix_list_right_card);
        settingMixListBottomCard = findViewById(R.id.plvls_setting_mix_list_bottom_card);
        settingMixBackgroundTitle = findViewById(R.id.plvls_setting_mix_background_title);
        settingMixBackgroundContainer = findViewById(R.id.plvls_setting_mix_background_container);
    }

    private void initMixBackgroundItems() {
        for (PLVStreamerMixBackground mixBackground : PLVStreamerMixBackground.values()) {
            final MixBackgroundItemLayout itemLayout = new MixBackgroundItemLayout(getContext());
            itemLayout.setMixBackground(mixBackground);
            itemLayout.setOnClickListener(this);
            settingMixBackgroundContainer.addView(itemLayout);
        }
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
    }

    public void updateSelectedState() {
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

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

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
        final PLVStreamerConfig.MixLayoutType currentSelectedMix = onViewActionListener.getMixLayoutType();
        if (currentSelectedMix != mixLayoutType) {
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
            LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_setting_mix_background_item, this);
            settingMixBackgroundItemRoot = findViewById(R.id.plvls_setting_mix_background_item_root);
            settingMixBackgroundItemIv = findViewById(R.id.plvls_setting_mix_background_item_iv);
            settingMixBackgroundItemTv = findViewById(R.id.plvls_setting_mix_background_item_tv);
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

    public interface OnViewActionListener {
        PLVStreamerConfig.MixLayoutType getMixLayoutType();
        void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix);

        PLVStreamerMixBackground getMixBackground();

        void onChangeMixBackground(PLVStreamerMixBackground mixBackground);
    }

}
