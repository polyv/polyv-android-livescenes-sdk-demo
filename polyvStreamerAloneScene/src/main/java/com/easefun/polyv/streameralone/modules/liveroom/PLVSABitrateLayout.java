package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.streameralone.R;

/**
 * 清晰度设置布局
 */
public class PLVSABitrateLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //布局弹层
    private PLVMenuDrawer menuDrawer;

    //view
    private RecyclerView plvsaSettingBitrateRv;

    //adapter
    private BitrateAdapter bitrateAdapter;

    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSABitrateLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSABitrateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSABitrateLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_setting_bitrate_layout, this, true);

        plvsaSettingBitrateRv = findViewById(R.id.plvsa_setting_bitrate_rv);

        plvsaSettingBitrateRv.setLayoutManager(new GridLayoutManager(getContext(), 3));
//        plvsaSettingBitrateRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(6), 0));
        bitrateAdapter = new BitrateAdapter();
        plvsaSettingBitrateRv.setAdapter(bitrateAdapter);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        // 更新清晰度数据
        if (onViewActionListener != null && onViewActionListener.getBitrateInfo() != null) {
            bitrateAdapter.updateData(onViewActionListener.getBitrateInfo().first, onViewActionListener.getBitrateInfo().second);
        }

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

    // <editor-fold defaultstate="collapsed" desc="内部类 - 清晰度适配器">
    private class BitrateAdapter extends RecyclerView.Adapter<BitrateAdapter.BitrateViewHolder> {
        private int maxBitrate;
        private int selPosition;

        @NonNull
        @Override
        public BitrateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BitrateViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvsa_live_room_setting_bitrate_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final BitrateViewHolder holder, int position) {
            String bitrateText = getBitrateText(position);
            holder.plvsaBitrateTv.setText(bitrateText);
            if (position == selPosition) {
                holder.plvsaBitrateParentLy.setSelected(true);
            } else {
                holder.plvsaBitrateParentLy.setSelected(false);
            }
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.getAdapterPosition() == selPosition) {
                        return;
                    }
                    selPosition = holder.getAdapterPosition();
                    if (onViewActionListener != null) {
                        onViewActionListener.onBitrateClick(getBitrate(selPosition));
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return maxBitrate;
        }

        public void updateData(int maxBitrate, int selBitrate) {
            this.maxBitrate = maxBitrate;
            this.selPosition = Math.max(0, maxBitrate - selBitrate);
            notifyDataSetChanged();
        }

        private String getBitrateText(int position) {
            String bitrateText = "";
            if (position == 0) {
                bitrateText = maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? "超清" : maxBitrate == PLVSStreamerConfig.Bitrate.BITRATE_HIGH ? "高清" : "标清";
            } else if (position == 1) {
                bitrateText = maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? "高清" : "标清";
            } else if (position == 2) {
                bitrateText = "标清";
            }
            return bitrateText;
        }

        private int getBitrate(int pos) {
            if (pos == 0) {
                return maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? PLVSStreamerConfig.Bitrate.BITRATE_SUPER : maxBitrate == PLVSStreamerConfig.Bitrate.BITRATE_HIGH ? PLVSStreamerConfig.Bitrate.BITRATE_HIGH : PLVSStreamerConfig.Bitrate.BITRATE_STANDARD;
            } else if (pos == 1) {
                return maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? PLVSStreamerConfig.Bitrate.BITRATE_HIGH : PLVSStreamerConfig.Bitrate.BITRATE_STANDARD;
            } else {
                return PLVSStreamerConfig.Bitrate.BITRATE_STANDARD;
            }
        }

        class BitrateViewHolder extends RecyclerView.ViewHolder {
            private ViewGroup plvsaBitrateParentLy;
            private TextView plvsaBitrateTv;

            BitrateViewHolder(View itemView) {
                super(itemView);
                plvsaBitrateParentLy = itemView.findViewById(R.id.plvsa_bitrate_parent_ly);
                plvsaBitrateTv = itemView.findViewById(R.id.plvsa_bitrate_tv);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        Pair<Integer, Integer> getBitrateInfo();

        void onBitrateClick(int bitrate);
    }
    // </editor-fold>
}
