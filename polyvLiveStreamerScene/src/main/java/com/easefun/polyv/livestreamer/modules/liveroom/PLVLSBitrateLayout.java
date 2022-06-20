package com.easefun.polyv.livestreamer.modules.liveroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.livestreamer.R;

/**
 * @author Hoshiiro
 */
public class PLVLSBitrateLayout extends FrameLayout {

    private TextView bitrateTitleTv;
    private View bitrateTitleSeparator;
    private RecyclerView bitrateRv;

    private final BitrateAdapter bitrateAdapter = new BitrateAdapter();

    private OnViewActionListener onViewActionListener;

    public PLVLSBitrateLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLSBitrateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLSBitrateLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_bitrate_layout, this);

        findView();
        initRecyclerView();
    }

    private void findView() {
        bitrateTitleTv = findViewById(R.id.plvls_bitrate_title_tv);
        bitrateTitleSeparator = findViewById(R.id.plvls_bitrate_title_separator);
        bitrateRv = findViewById(R.id.plvls_bitrate_rv);
    }

    private void initRecyclerView() {
        bitrateRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bitrateRv.setAdapter(bitrateAdapter);
    }

    public void updateData(final int maxBitrate, final int currentSelectedBitrate) {
        bitrateAdapter.updateData(maxBitrate, currentSelectedBitrate);
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    private class BitrateAdapter extends RecyclerView.Adapter<PLVLSBitrateLayout.BitrateAdapter.BitrateViewHolder> {
        private int maxBitrate;
        private int selPosition;

        @NonNull
        @Override
        public PLVLSBitrateLayout.BitrateAdapter.BitrateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PLVLSBitrateLayout.BitrateAdapter.BitrateViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvls_live_room_setting_bitrate_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final PLVLSBitrateLayout.BitrateAdapter.BitrateViewHolder holder, int position) {
            String bitrateText = getBitrateText(position);
            holder.plvlsSettingBitrateSelTv.setText(bitrateText);
            if (position == selPosition) {
                holder.plvlsSettingBitrateSelIndicatorView.setVisibility(View.VISIBLE);
                holder.plvlsSettingBitrateSelTv.setSelected(true);
            } else {
                holder.plvlsSettingBitrateSelIndicatorView.setVisibility(View.GONE);
                holder.plvlsSettingBitrateSelTv.setSelected(false);
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
            private TextView plvlsSettingBitrateSelTv;
            private View plvlsSettingBitrateSelIndicatorView;

            BitrateViewHolder(View itemView) {
                super(itemView);
                plvlsSettingBitrateSelTv = itemView.findViewById(R.id.plvls_setting_bitrate_sel_tv);
                plvlsSettingBitrateSelIndicatorView = itemView.findViewById(R.id.plvls_setting_bitrate_sel_indicator_view);
            }
        }
    }

    public interface OnViewActionListener {
        void onBitrateClick(int bitrate);
    }

}
