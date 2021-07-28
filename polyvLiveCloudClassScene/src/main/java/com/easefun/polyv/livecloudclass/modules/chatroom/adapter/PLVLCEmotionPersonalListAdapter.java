package com.easefun.polyv.livecloudclass.modules.chatroom.adapter;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;

import java.util.List;

/**
 * 个性表情列表适配器，用于聊天室展示个性表情列表
 */
public class PLVLCEmotionPersonalListAdapter extends RecyclerView.Adapter<PLVLCEmotionPersonalListAdapter.PersonalItemViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private List<PLVEmotionImageVO.EmotionImage> emotionPersonalList;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCEmotionPersonalListAdapter(List<PLVEmotionImageVO.EmotionImage> emotionImages) {
        emotionPersonalList = emotionImages;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public PersonalItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PersonalItemViewHolder
                (LayoutInflater.from(parent.getContext()).inflate(R.layout.plvlc_chatroom_emoji_personal_item, parent, false));

    }

    @Override
    public void onBindViewHolder(PersonalItemViewHolder holder,  int position) {

        final PLVEmotionImageVO.EmotionImage emotionImage = emotionPersonalList.get(position);
        PLVImageLoader.getInstance().loadImage(emotionImage.getUrl(), holder.personalEmotionIv);
        holder.personalEmotionNameTv.setText(emotionImage.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onEmotionViewClick(emotionImage);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onViewActionListener != null){
                    onViewActionListener.onEmotionViewLongClick(emotionImage, v);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return emotionPersonalList.size();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onEmotionViewClick(PLVEmotionImageVO.EmotionImage emotionImage);

        void onEmotionViewLongClick(PLVEmotionImageVO.EmotionImage emotionImage, View view);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - EmojiItemViewHolder">


    class PersonalItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView personalEmotionIv;
        private TextView personalEmotionNameTv;

        public PersonalItemViewHolder(View itemView) {
            super(itemView);
            personalEmotionIv = itemView.findViewById(R.id.emotion_iv);
            personalEmotionNameTv = itemView.findViewById(R.id.emotion_name_tv);
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
