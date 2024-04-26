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
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 混流设置布局
 */
public class PLVLSMixLayout extends FrameLayout {

    private TextView mixTitleTv;
    private View mixTitleSeparator;
    private RecyclerView mixRv;

    private final MixAdapter mixAdapter = new MixAdapter();

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
        initRecyclerView();
    }

    private void findView() {
        mixTitleTv = findViewById(R.id.plvls_mix_title_tv);
        mixTitleSeparator = findViewById(R.id.plvls_mix_title_separator);
        mixRv = findViewById(R.id.plvls_mix_rv);
    }

    private void initRecyclerView() {
        mixRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mixRv.setAdapter(mixAdapter);
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
    }

    public void updateData(final PLVStreamerConfig.MixLayoutType currentSelectedMix) {
        mixAdapter.updateData(currentSelectedMix);
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

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
            return new MixViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvls_live_room_setting_mix_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final MixViewHolder holder, int position) {
            holder.plvlsSettingMixSelTv.setText(getMixValueByPos(position));
            if (position == selPosition) {
                holder.plvlsSettingMixSelIndicatorView.setVisibility(View.VISIBLE);
                holder.plvlsSettingMixSelTv.setSelected(true);
            } else {
                holder.plvlsSettingMixSelIndicatorView.setVisibility(View.GONE);
                holder.plvlsSettingMixSelTv.setSelected(false);
            }
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
            private TextView plvlsSettingMixSelTv;
            private View plvlsSettingMixSelIndicatorView;

            MixViewHolder(View itemView) {
                super(itemView);
                plvlsSettingMixSelTv = itemView.findViewById(R.id.plvls_setting_mix_sel_tv);
                plvlsSettingMixSelIndicatorView = itemView.findViewById(R.id.plvls_setting_mix_sel_indicator_view);
            }
        }
    }

    public interface OnViewActionListener {
        void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix);
    }

}
