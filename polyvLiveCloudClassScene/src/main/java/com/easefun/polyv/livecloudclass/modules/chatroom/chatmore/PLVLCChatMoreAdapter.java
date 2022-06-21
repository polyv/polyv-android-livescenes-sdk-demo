package com.easefun.polyv.livecloudclass.modules.chatroom.chatmore;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.plv.livescenes.model.interact.PLVChatFunctionVO;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class PLVLCChatMoreAdapter extends RecyclerView.Adapter<PLVLCChatMoreAdapter.ChatMoreViewHolder> {

    private List<PLVChatFunctionVO> functionList;
    private Context context;
    private OnItemClickListener listener;
    private int itemWidth;

    public PLVLCChatMoreAdapter(int spanCount, Context context) {
        this.context = context;
        itemWidth = ScreenUtils.getScreenWidth() / spanCount;
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
                LayoutInflater.from(context).inflate(R.layout.plvlc_chatroom_chat_more_item,
                        viewGroup, false));
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        params.width = itemWidth;
        holder.itemView.setLayoutParams(params);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMoreViewHolder chatMoreViewHolder, int i) {
        final PLVChatFunctionVO functionVO = functionList.get(i);
        if(TextUtils.isEmpty(functionVO.getIcon())){
            chatMoreViewHolder.iconIv.setImageResource(functionVO.getImageResourceId());
        } else {
            PLVImageLoader.getInstance().loadImage(functionVO.getIcon(), chatMoreViewHolder.iconIv);
        }
        chatMoreViewHolder.iconIv.setSelected(functionVO.isSelected());
        chatMoreViewHolder.nameTv.setText(functionVO.getName());
        chatMoreViewHolder.newIv.setVisibility(View.INVISIBLE);
        chatMoreViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(functionVO.getType());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return functionList.size();
    }


    static class ChatMoreViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout itemLayout;
        TextView nameTv;
        ImageView iconIv;
        ImageView newIv;

        public ChatMoreViewHolder(View itemView) {
            super(itemView);
            iconIv = itemView.findViewById(R.id.plvlc_chat_more_item_icon);
            nameTv = itemView.findViewById(R.id.plvlc_chat_more_item_name);
            itemLayout = itemView.findViewById(R.id.plvlc_chat_more_item);
            newIv = itemView.findViewById(R.id.plvlc_chat_more_item_new);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String type);
    }
}
