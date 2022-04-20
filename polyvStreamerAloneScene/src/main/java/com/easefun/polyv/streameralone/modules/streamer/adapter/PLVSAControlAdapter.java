package com.easefun.polyv.streameralone.modules.streamer.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.streameralone.R;

import java.util.ArrayList;
import java.util.List;

public class PLVSAControlAdapter extends RecyclerView.Adapter<PLVSAControlAdapter.PLVSAControlViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String PAYLOAD_UPDATE_STATUS = "payloadUpdateStatus";


    private List<Pair<Integer, String>> list = new ArrayList<>();

    //<selected, visiable>
    private List<Pair<Boolean,Boolean>> statusList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    private int itemWidth = 0;

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public PLVSAControlViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvsa_streamer_member_control_item, viewGroup, false);
        return new PLVSAControlViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVSAControlViewHolder holder,  int position) {
        Pair<Integer, String> cell = list.get(position);
        Pair<Boolean, Boolean> status = statusList.get(position);
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if(status.second){
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = itemWidth;
        } else {
            params.height = 0;
            params.width = 0;
        }
        holder.plvsaStreamerControlImage.setImageResource(cell.first);
        holder.plvsaStreamerControlName.setText(cell.second);
        holder.plvsaStreamerControlImage.setSelected(status.first);
        holder.plvsaStreamerControlName.setSelected(status.first);

        final int pos = position;
        final boolean isSelected = status.first;
        holder.plvsaStreamerControlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(v, pos, statusList.get(pos).first);
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull PLVSAControlViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }

        Pair<Boolean, Boolean> status = statusList.get(position);
        Pair<Integer, String> cell = list.get(position);
        for (Object payload : payloads){
            switch (payload.toString()){
                case PAYLOAD_UPDATE_STATUS:
                    holder.plvsaStreamerControlImage.setSelected(status.first);
                    holder.plvsaStreamerControlName.setSelected(status.first);
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    if(status.second){
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        params.width = itemWidth;
                        holder.itemView.setVisibility(View.VISIBLE);
                    } else {
                        params.height = 0;
                        params.width = 0;
                        holder.itemView.setVisibility(View.GONE);
                    }
                    holder.itemView.setLayoutParams(params);
                    holder.plvsaStreamerControlName.setText(cell.second);
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API - 数据传递">

    public void setData(List<Pair<Integer, String>> list){
        this.list = list;
        statusList.clear();
        for (int i = 0; i < list.size(); i++) {
            statusList.add(new Pair<Boolean, Boolean>(false, true));
        }
        notifyDataSetChanged();
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API - 接口设置">

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setItemWidth(int width){
        itemWidth = width;
    }


    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API - item状态">
    public void updateItemSelectStatus(int position, boolean isSelected){
        statusList.set(position, new Pair<Boolean, Boolean>(isSelected, statusList.get(position).second));
        notifyItemChanged(position, PAYLOAD_UPDATE_STATUS);
    }

    public void updateItemVisibility(int position, boolean isShow){
        statusList.set(position, new Pair<Boolean, Boolean>(statusList.get(position).first, isShow));
        notifyItemChanged(position, PAYLOAD_UPDATE_STATUS);
    }

    public int getVisibilityItem(){
        int itemCount = 0;
        for (int i = 0; i < statusList.size(); i++) {
            if(statusList.get(i).second){
                itemCount++;
            }
        }
        return itemCount;
    }
    // </editor-fold >

    class PLVSAControlViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout plvsaStreamerControlView;
        private ImageView plvsaStreamerControlImage;
        private TextView plvsaStreamerControlName;

        public PLVSAControlViewHolder(View itemView) {
            super(itemView);
            plvsaStreamerControlView = itemView.findViewById(R.id.plvsa_streamer_control_view);
            plvsaStreamerControlImage = itemView.findViewById(R.id.plvsa_streamer_control_image);
            plvsaStreamerControlName = itemView.findViewById(R.id.plvsa_streamer_control_name);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position, boolean isSelected);
    }

}
