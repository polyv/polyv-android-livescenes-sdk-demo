package com.easefun.polyv.livecommon.module.modules.cast.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.R;

import java.util.List;

public class PolyvScreencastBitrateAdapter extends RecyclerView.Adapter<PolyvScreencastBitrateAdapter.PlvBitrateViewHolder> {

    List<PolyvDefinitionVO> mBitrates;
    int mBitrateIndex;
    OnItemClickListener clickListener;


    public void setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setBitrateList(List<PolyvDefinitionVO> list, int defIndex) {
        mBitrates = list;
        mBitrateIndex = defIndex;
    }

    public void setBitrateIndex(int index) {
        this.mBitrateIndex = index;
    }

    @NonNull
    @Override
    public PlvBitrateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_cast_bitrates_item, parent, false);
        return new PlvBitrateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlvBitrateViewHolder holder, final int position) {
        holder.tvBitrate.setText(mBitrates.get(position).getDefinition());
        if (mBitrateIndex == position) {
            holder.tvBitrate.setSelected(true);
        } else {
            holder.tvBitrate.setSelected(false);
        }
        holder.tvBitrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBitrateIndex = position;
                if (clickListener != null) {
                    clickListener.onItemClick(position);
                    notifyItemRangeChanged(0, getItemCount());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBitrates == null ? 0 : mBitrates.size();
    }

    class PlvBitrateViewHolder extends RecyclerView.ViewHolder {
        TextView tvBitrate;

        public PlvBitrateViewHolder(View itemView) {
            super(itemView);
            tvBitrate = itemView.findViewById(R.id.tv_bitrate);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int index);
    }

}
