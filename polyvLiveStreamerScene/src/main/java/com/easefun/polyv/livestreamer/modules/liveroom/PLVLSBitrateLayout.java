package com.easefun.polyv.livestreamer.modules.liveroom;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.linkmic.model.PLVPushStreamTemplateJsonBean;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;

/**
 * @author Hoshiiro
 */
public class PLVLSBitrateLayout extends FrameLayout {

    private TextView bitrateTitleTv;
    private View bitrateTitleSeparator;
    private RecyclerView bitrateRv;

    private final BitrateAdapter bitrateAdapter = new BitrateAdapter();

    // 青春套餐配置模版
    private PLVPushStreamTemplateJsonBean pushStreamTemplate;
    //是否开启青春套餐
    private boolean isPushStreamTemplateEnable;

    private String channelId;

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
    }

    private void findView() {
        bitrateTitleTv = findViewById(R.id.plvls_bitrate_title_tv);
        bitrateTitleSeparator = findViewById(R.id.plvls_bitrate_title_separator);
        bitrateRv = findViewById(R.id.plvls_bitrate_rv);
    }

    private void initRecyclerView() {
        bitrateRv.setLayoutManager(new LinearLayoutManager(getContext(), isPushStreamTemplateEnable ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL, false));
        bitrateRv.setAdapter(bitrateAdapter);
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
        pushStreamTemplate = PLVStreamerConfig.getPushStreamTemplate(channelId);
        isPushStreamTemplateEnable = pushStreamTemplate != null && pushStreamTemplate.isEnabled();
        initRecyclerView();
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
            return new PLVLSBitrateLayout.BitrateAdapter.BitrateViewHolder(LayoutInflater.from(parent.getContext()).inflate(isPushStreamTemplateEnable ? R.layout.plvls_live_room_setting_bitrate_template_enable_item : R.layout.plvls_live_room_setting_bitrate_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final PLVLSBitrateLayout.BitrateAdapter.BitrateViewHolder holder, int position) {
            int bitrate = getBitrate(position);
            String bitrateText = getBitrateText(bitrate);
            holder.plvlsSettingBitrateSelTv.setText(bitrateText);
            if (holder.plvlsSettingBitrateSelIndicatorView != null) {
                if (position == selPosition) {
                    holder.plvlsSettingBitrateSelIndicatorView.setVisibility(View.VISIBLE);
                    holder.plvlsSettingBitrateSelTv.setSelected(true);
                } else {
                    holder.plvlsSettingBitrateSelIndicatorView.setVisibility(View.GONE);
                    holder.plvlsSettingBitrateSelTv.setSelected(false);
                }
            }
            if (holder.plvlsBitrateParentLy != null) {
                holder.plvlsBitrateParentLy.setSelected(position == selPosition);
            }
            if (isPushStreamTemplateEnable && holder.plvlsSettingBitrateDescTv != null) {
                int pos = Math.min(pushStreamTemplate.getVideoParams().size() - 1, bitrate - 1);
                PLVPushStreamTemplateJsonBean.VideoParamsBean videoParamsBean = pushStreamTemplate.getVideoParams().get(pos);
                int videoHeight = videoParamsBean.getVideoHeight();
                int videoBitrate = videoParamsBean.getVideoBitrate();
                int videoFps = videoParamsBean.getVideoFps();
                String baseDesc = PLVAppUtils.formatString(R.string.plv_streamer_bitrate_desc, videoBitrate, videoFps);
                String desc = PLVLanguageUtil.isENLanguage() ? baseDesc : "分辨率：" + videoHeight + "p，" + baseDesc;// no need i18n
                holder.plvlsSettingBitrateDescTv.setText(desc);

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

        private String getBitrateText(int bitrate) {
            return PLVStreamerConfig.QualityLevel.getTextCombineTemplate(bitrate, channelId);
        }

        private int getBitrate(int pos) {
            return maxBitrate - pos >= 1 ? maxBitrate - pos : PLVSStreamerConfig.Bitrate.BITRATE_STANDARD;
        }

        class BitrateViewHolder extends RecyclerView.ViewHolder {
            private TextView plvlsSettingBitrateSelTv;
            @Nullable
            private View plvlsSettingBitrateSelIndicatorView;
            @Nullable
            private ViewGroup plvlsBitrateParentLy;
            @Nullable
            private TextView plvlsSettingBitrateDescTv;

            BitrateViewHolder(View itemView) {
                super(itemView);
                plvlsSettingBitrateSelTv = itemView.findViewById(R.id.plvls_setting_bitrate_sel_tv);
                plvlsSettingBitrateSelIndicatorView = itemView.findViewById(R.id.plvls_setting_bitrate_sel_indicator_view);
                plvlsBitrateParentLy = itemView.findViewById(R.id.plvls_bitrate_parent_ly);
                plvlsSettingBitrateDescTv = itemView.findViewById(R.id.plvls_setting_bitrate_desc_tv);
            }
        }
    }

    public interface OnViewActionListener {
        void onBitrateClick(int bitrate);
    }

}
