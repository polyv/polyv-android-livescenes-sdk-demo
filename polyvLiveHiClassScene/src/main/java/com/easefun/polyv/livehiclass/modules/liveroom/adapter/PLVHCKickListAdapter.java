package com.easefun.polyv.livehiclass.modules.liveroom.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.socket.user.PLVSocketUserBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 踢出列表适配器
 */
public class PLVHCKickListAdapter extends RecyclerView.Adapter<PLVHCKickListAdapter.KickListViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //data
    private List<PLVSocketUserBean> dataList;
    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCKickListAdapter() {
        dataList = new ArrayList<>();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public KickListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KickListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvhc_live_room_member_list_kick_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final KickListViewHolder holder, int position) {
        final PLVSocketUserBean socketUserBean = dataList.get(position);
        holder.plvhcMemberListKickNickTv.setText(socketUserBean.getNick());
        holder.plvhcMemberListKickCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toastMsg = "";
                int sendResult = PLVChatroomManager.getInstance().unKick(socketUserBean.getUserId());
                if (sendResult > 0) {
                    toastMsg = "移入成功";
                    dataList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    if (onViewActionListener != null) {
                        onViewActionListener.onRemoveKickUserAction();
                    }
                } else {
                    toastMsg = "移入失败" + "(" + sendResult + ")";
                }
                PLVHCToast.Builder.context(v.getContext())
                        .setText(toastMsg)
                        .build()
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void bindData(List<PLVSocketUserBean> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        onViewActionListener = listener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - ViewHolder">
    public static class KickListViewHolder extends RecyclerView.ViewHolder {
        private TextView plvhcMemberListKickNickTv;
        private TextView plvhcMemberListKickCancelTv;

        public KickListViewHolder(View itemView) {
            super(itemView);
            plvhcMemberListKickNickTv = (TextView) findViewById(R.id.plvhc_member_list_kick_nick_tv);
            plvhcMemberListKickCancelTv = (TextView) findViewById(R.id.plvhc_member_list_kick_cancel_tv);
        }

        private <T extends View> T findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        /**
         * 把用户从踢出列表中移除
         */
        void onRemoveKickUserAction();
    }
    // </editor-fold>
}
