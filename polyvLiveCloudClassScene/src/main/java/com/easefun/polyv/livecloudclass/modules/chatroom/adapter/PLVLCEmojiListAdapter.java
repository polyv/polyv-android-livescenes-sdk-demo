package com.easefun.polyv.livecloudclass.modules.chatroom.adapter;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 表情列表适配器
 */
public class PLVLCEmojiListAdapter extends RecyclerView.Adapter<PLVLCEmojiListAdapter.EmojiItemViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private List<String> emoLists;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCEmojiListAdapter() {
        emoLists = new ArrayList<>(PLVFaceManager.getInstance().getFaceMap().keySet());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public EmojiItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EmojiItemViewHolder
                (LayoutInflater.from(parent.getContext()).inflate(R.layout.plvlc_chatroom_emoji_item, parent, false));
    }

    @Override
    public void onBindViewHolder(EmojiItemViewHolder holder, int position) {
        holder.processData(emoLists.get(position), position);
    }

    @Override
    public int getItemCount() {
        return emoLists.size();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onEmojiViewClick(String emoKey);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - EmojiItemViewHolder">
    class EmojiItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView emo;
        private String item;

        EmojiItemViewHolder(View itemView) {
            super(itemView);
            emo = itemView.findViewById(R.id.emoji_iv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onEmojiViewClick(emoLists.get(getVHPosition()));
                    }
                }
            });
        }

        int getVHPosition() {
            int position = 0;//item 移动时 position 需更新
            for (int i = 0; i < emoLists.size(); i++) {
                Object obj = emoLists.get(i);
                if (obj == item) {
                    position = i;
                    break;
                }
            }
            return position;
        }

        void processData(String item, int position) {
            this.item = item;
            EmojiItemViewHolder emoItemViewHolder = this;
            int id = PLVFaceManager.getInstance().getFaceId(item);
            Drawable drawable = itemView.getContext().getResources().getDrawable(id);
            emoItemViewHolder.emo.setImageDrawable(drawable);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - ItemDecoration">
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
    // </editor-fold>
}
