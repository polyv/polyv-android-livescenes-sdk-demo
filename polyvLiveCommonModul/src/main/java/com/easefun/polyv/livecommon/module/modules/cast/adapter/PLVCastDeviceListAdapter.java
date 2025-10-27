package com.easefun.polyv.livecommon.module.modules.cast.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;

import net.polyv.android.media.cast.model.vo.PLVMediaCastDevice;

import java.util.ArrayList;
import java.util.List;

public class PLVCastDeviceListAdapter extends RecyclerView.Adapter<PLVCastDeviceListAdapter.DevicesHolder> {
    private List<PLVMediaCastDevice> mDatas;
    private PLVMediaCastDevice mSelectInfo;
    private OnItemClickListener mItemClickListener;
    private int mLayoutId;

    public PLVCastDeviceListAdapter(int layoutId) {
        mDatas = new ArrayList<>();
        mLayoutId = layoutId;
    }

    public void updateDatas(List<PLVMediaCastDevice> infos) {
        if (null != infos) {
            mDatas.clear();
            mDatas.addAll(infos);
            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    public PLVMediaCastDevice getSelectInfo() {
        return mSelectInfo;
    }

    public void setSelectInfo(PLVMediaCastDevice selectInfo) {
        mSelectInfo = selectInfo;
    }

    @NonNull
    @Override
    public DevicesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DevicesHolder(LayoutInflater.from(parent.getContext()).
                inflate(mLayoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesHolder holder, final int position) {
        final PLVMediaCastDevice info = mDatas.get(position);
        if (null == info) {
            return;
        }
        String item = info.getFriendlyName()/* + " isOnLine:" + info.isOnLine() + " uid:" + info.getUid() + " types:" + info.getTypes()*/;
        holder.tvDeviceName.setText(item);
        if (info == mSelectInfo ||
                (mSelectInfo != null && info.getFriendlyName() != null && info.getFriendlyName().equals(mSelectInfo.getFriendlyName()))) {
            holder.llItem.setSelected(true);
//            holder.ivCastVideo.setSelected(true);
//            holder.ivCastYes.setVisibility(View.VISIBLE);
        } else {
            holder.llItem.setSelected(false);
//            holder.ivCastVideo.setSelected(false);
//            holder.ivCastYes.setVisibility(View.GONE);
        }
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(position, info);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }


    public interface OnItemClickListener {
        void onClick(int position, PLVMediaCastDevice pInfo);
    }

    class DevicesHolder extends RecyclerView.ViewHolder {
        LinearLayout llItem;
        ImageView ivCastVideo;
        TextView tvDeviceName;
        ImageView ivCastYes;

        public DevicesHolder(View itemView) {
            super(itemView);
            ivCastVideo = itemView.findViewById(R.id.iv_cast_video);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            ivCastYes = itemView.findViewById(R.id.iv_cast_yes);
            llItem = itemView.findViewById(R.id.ll_plv_device_item);
        }
    }

    public interface OnDeviceClickListener {
        void onClick(int position, PLVMediaCastDevice pInfo);
    }


}
