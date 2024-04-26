package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.liveecommerce.R;
import com.plv.livescenes.model.interact.PLVChatFunctionVO;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class PLVECMoreAdapter extends RecyclerView.Adapter<PLVECMoreAdapter.ChatMoreViewHolder> {

    private List<PLVChatFunctionVO> functionList;
    private Context context;
    private OnItemClickListener listener;
    private int itemWidth;

    public PLVECMoreAdapter(int spanCount, Context context) {
        this.context = context;
        int portraitWidth = ScreenUtils.isPortrait() ? ScreenUtils.getScreenWidth() : ScreenUtils.getScreenHeight();
        itemWidth = portraitWidth / spanCount;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(@NonNull List<PLVChatFunctionVO> functionList) {
        this.functionList = new ArrayList<>(functionList);
    }

    public void updateFunctionList(@NonNull List<PLVChatFunctionVO> functionList) {
        List<PLVChatFunctionVO> newList = new ArrayList<>();
        for (PLVChatFunctionVO functionVO : functionList) {
            if (functionVO.isShow()) {
                newList.add(functionVO);
            }
        }
        this.functionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatMoreViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ChatMoreViewHolder holder = new ChatMoreViewHolder(
                LayoutInflater.from(context).inflate(R.layout.plvec_live_more_item,
                        viewGroup, false));
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        params.width = itemWidth;
        holder.itemView.setLayoutParams(params);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatMoreViewHolder chatMoreViewHolder, int i) {
        final PLVChatFunctionVO functionVO = functionList.get(i);
        if(TextUtils.isEmpty(functionVO.getIcon())){
            chatMoreViewHolder.iconIv.setImageResource(functionVO.getImageResourceId());
        } else {
            PLVImageLoader.getInstance().loadImage(functionVO.getIcon(), chatMoreViewHolder.iconIv);
        }
        chatMoreViewHolder.iconIv.setSelected(functionVO.isSelected());
        chatMoreViewHolder.nameTv.setText(functionVO.getName());
        chatMoreViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(functionVO.getType(), chatMoreViewHolder.iconIv);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return functionList.size();
    }


    static class ChatMoreViewHolder extends RecyclerView.ViewHolder {
        ViewGroup itemLayout;
        TextView nameTv;
        ImageView iconIv;

        public ChatMoreViewHolder(View itemView) {
            super(itemView);
            iconIv = itemView.findViewById(R.id.plvec_live_more_iv);
            nameTv = itemView.findViewById(R.id.plvec_live_more_tv);
            itemLayout = itemView.findViewById(R.id.plvec_live_more_floating_ll);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String type, View iconView);
    }
}
