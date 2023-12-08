package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 混流布局设置布局
 */
public class PLVSAMixLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 混流布局表格每行显示数量
    private static final int MIX_LAYOUT_SPAN_PORT = 3;
    private static final int MIX_LAYOUT_SPAN_LAND = 1;
    // 混流布局弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 混流布局宽度、高度、布局位置
    private static final int MIX_LAYOUT_WIDTH_PORT = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int MIX_LAYOUT_WIDTH_LAND = ConvertUtils.dp2px(214);
    private static final int MIX_LAYOUT_HEIGHT_PORT = ConvertUtils.dp2px(214);
    private static final int MIX_LAYOUT_HEIGHT_LAND = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int MIX_LAYOUT_GRAVITY_PORT = Gravity.BOTTOM;
    private static final int MIX_LAYOUT_GRAVITY_LAND = Gravity.END;
    // 混流布局背景
    private static final int MIX_LAYOUT_BACKGROUND_RES_PORT = R.drawable.plvsa_setting_bitrate_ly_shape;
    private static final int MIX_LAYOUT_BACKGROUND_RES_LAND = R.drawable.plvsa_setting_bitrate_ly_shape_land;

    //布局弹层
    private PLVMenuDrawer menuDrawer;

    //view
    private RelativeLayout plvsaSettingMixLayoutRoot;
    private TextView plvsaSettingMixTv;
    private RecyclerView plvsaSettingMixRv;

    //adapter
    private MixAdapter mixAdapter;
    // Layout Manager
    private GridLayoutManager mixLayoutManager;

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

        plvsaSettingMixLayoutRoot = findViewById(R.id.plvsa_setting_mix_layout_root);
        plvsaSettingMixTv = findViewById(R.id.plvsa_setting_mix_tv);
        plvsaSettingMixRv = findViewById(R.id.plvsa_setting_mix_rv);

        mixAdapter = new MixAdapter();
        mixLayoutManager = new GridLayoutManager(getContext(), 3);
        plvsaSettingMixRv.setLayoutManager(mixLayoutManager);
        plvsaSettingMixRv.setAdapter(mixAdapter);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
    }

    public void open() {
        // 更新混流布局数据
        if (onViewActionListener != null) {
            mixAdapter.updateData(onViewActionListener.getMixLayoutType());
        }
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

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        Position menuDrawerPosition;
        LayoutParams mixLayoutParam = (LayoutParams) plvsaSettingMixLayoutRoot.getLayoutParams();

        if (PLVScreenUtils.isPortrait(getContext())) {
            mixLayoutParam.width = MIX_LAYOUT_WIDTH_PORT;
            mixLayoutParam.height = MIX_LAYOUT_HEIGHT_PORT;
            mixLayoutParam.gravity = MIX_LAYOUT_GRAVITY_PORT;
            mixLayoutManager.setSpanCount(MIX_LAYOUT_SPAN_PORT);
            plvsaSettingMixLayoutRoot.setBackgroundResource(MIX_LAYOUT_BACKGROUND_RES_PORT);
            menuDrawerPosition = MENU_DRAWER_POSITION_PORT;
        } else {
            mixLayoutParam.width = MIX_LAYOUT_WIDTH_LAND;
            mixLayoutParam.height = MIX_LAYOUT_HEIGHT_LAND;
            mixLayoutParam.gravity = MIX_LAYOUT_GRAVITY_LAND;
            mixLayoutManager.setSpanCount(MIX_LAYOUT_SPAN_LAND);
            plvsaSettingMixLayoutRoot.setBackgroundResource(MIX_LAYOUT_BACKGROUND_RES_LAND);
            menuDrawerPosition = MENU_DRAWER_POSITION_LAND;
        }

        plvsaSettingMixLayoutRoot.setLayoutParams(mixLayoutParam);
        if (menuDrawer != null) {
            menuDrawer.setPosition(menuDrawerPosition);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 混流布局适配器">
    private class MixAdapter extends RecyclerView.Adapter<MixAdapter.MixViewHolder> {
        private final LinkedHashMap<PLVStreamerConfig.MixLayoutType, String> MIX_LAYOUT_TYPE_MAP = new LinkedHashMap<PLVStreamerConfig.MixLayoutType, String>() {
            {
                put(PLVStreamerConfig.MixLayoutType.SPEAKER, PLVAppUtils.getString(R.string.plv_streamer_mix_type_speaker));
                put(PLVStreamerConfig.MixLayoutType.TILE, PLVAppUtils.getString(R.string.plv_streamer_mix_type_tile));
                put(PLVStreamerConfig.MixLayoutType.SINGLE, PLVAppUtils.getString(R.string.plv_streamer_mix_type_single));
            }
        };
        private int selPosition;

        @NonNull
        @Override
        public MixViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MixViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvsa_live_room_setting_mix_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final MixViewHolder holder, int position) {
            holder.plvsaMixTv.setText(getMixValueByPos(position));
            holder.plvsaMixParentLy.setSelected(position == selPosition);
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.getAdapterPosition() == selPosition) {
                        return;
                    }
                    if (!PLVNetworkUtils.isConnected(getContext())) {
                        PLVToast.Builder.context(getContext())
                                .setText(R.string.plv_streamer_network_bad)
                                .build()
                                .show();
                        return;
                    }
                    selPosition = holder.getAdapterPosition();
                    if (onViewActionListener != null) {
                        onViewActionListener.onChangeMixLayoutType(getMixKeyByPos(selPosition));
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return MIX_LAYOUT_TYPE_MAP.size();
        }

        public void updateData(PLVStreamerConfig.MixLayoutType selMix) {
            int index = 0;
            for (Map.Entry<PLVStreamerConfig.MixLayoutType, String> entry : MIX_LAYOUT_TYPE_MAP.entrySet()) {
                if (entry.getKey() == selMix) {
                    this.selPosition = index;
                    break;
                }
                index++;
            }
            notifyDataSetChanged();
        }

        private String getMixValueByPos(int pos) {
            int index = 0;
            for (Map.Entry<PLVStreamerConfig.MixLayoutType, String> entry : MIX_LAYOUT_TYPE_MAP.entrySet()) {
                if (index == pos) {
                    return entry.getValue();
                }
                index++;
            }
            return "";
        }

        private PLVStreamerConfig.MixLayoutType getMixKeyByPos(int pos) {
            int index = 0;
            for (Map.Entry<PLVStreamerConfig.MixLayoutType, String> entry : MIX_LAYOUT_TYPE_MAP.entrySet()) {
                if (index == pos) {
                    return entry.getKey();
                }
                index++;
            }
            return PLVStreamerConfig.MixLayoutType.TILE;
        }

        class MixViewHolder extends RecyclerView.ViewHolder {
            private ViewGroup plvsaMixParentLy;
            private TextView plvsaMixTv;

            MixViewHolder(View itemView) {
                super(itemView);
                plvsaMixParentLy = itemView.findViewById(R.id.plvsa_mix_parent_ly);
                plvsaMixTv = itemView.findViewById(R.id.plvsa_mix_tv);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        PLVStreamerConfig.MixLayoutType getMixLayoutType();

        void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix);
    }
    // </editor-fold>
}
