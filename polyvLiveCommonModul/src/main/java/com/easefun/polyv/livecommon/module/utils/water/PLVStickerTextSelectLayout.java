package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 贴图文字模版选择布局
 */
public class PLVStickerTextSelectLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    // 布局宽度、高度、布局位置
    private static final int LAYOUT_HEIGHT_LAND = ConvertUtils.dp2px(146);
    private static final int LAYOUT_HEIGHT_PORT = ConvertUtils.dp2px(224);
    public static final StickerTextModel[] stickerTextModels = new StickerTextModel[]{
            new StickerTextModel("关注主播", 1, 14, 0),
            new StickerTextModel("限时抢购", 2, 14, 0),
            new StickerTextModel("新品推荐", 3, 12, 0.04f),
            new StickerTextModel("精品课程", 4, 12, 0.04f),
            new StickerTextModel("分享有礼", 5, 14, 0),
            new StickerTextModel("精品课程", 6, 14, 0.04f),
            new StickerTextModel("扫码关注", 7, 14, 0),
            new StickerTextModel("看这里", 8, 14, 0),
    };

    //view
    private RelativeLayout settingStickerLayoutRoot;
    private ImageView closeIv;
    private ImageView doneIv;
    private RecyclerView textSelectRv;

    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVStickerTextSelectLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVStickerTextSelectLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVStickerTextSelectLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_live_room_setting_sticker_text_select_layout, this, true);

        settingStickerLayoutRoot = findViewById(R.id.plv_setting_sticker_layout_root);
        closeIv = findViewById(R.id.plv_setting_sticker_close_iv);
        doneIv = findViewById(R.id.plv_setting_sticker_done_iv);
        textSelectRv = findViewById(R.id.plv_setting_sticker_rv);

        setupRecyclerView();

        closeIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onViewActionListener != null) {
                    onViewActionListener.cancel();
                }
                close();
            }
        });
        doneIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onViewActionListener != null) {
                    onViewActionListener.done();
                }
                close();
            }
        });
    }

    private void setupRecyclerView() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false);
            textSelectRv.setLayoutManager(gridLayoutManager);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 8, GridLayoutManager.VERTICAL, false);
            textSelectRv.setLayoutManager(gridLayoutManager);
        }
        final int spacePx = ConvertUtils.dp2px(6);
        if (textSelectRv.getItemDecorationCount() == 0) {
            textSelectRv.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    int position = parent.getChildAdapterPosition(view);
                    int orientation = getResources().getConfiguration().orientation;
                    if (orientation == Configuration.ORIENTATION_PORTRAIT ? position % 4 != 0 : position != 0) {
                        outRect.left = spacePx;
                    }
                }
            });
        }
        textSelectRv.setAdapter(new StickerTextSelectAdapter(getStickerTextList()));
    }

    private List<StickerTextModel> getStickerTextList() {
        return Arrays.asList(stickerTextModels);
    }

    public static class StickerTextModel {
        public String text;
        public int style;
        public int textSize;
        public float textSpacing;

        StickerTextModel(String text, int style, int textSize, float textSpacing) {
            this.text = text;
            this.style = style;
            this.textSize = textSize;
            this.textSpacing = textSpacing;
        }
    }

    private class StickerTextSelectAdapter extends RecyclerView.Adapter<StickerTextSelectAdapter.ViewHolder> {
        private List<StickerTextModel> dataList;
        private int selectedPosition = 0;

        StickerTextSelectAdapter(List<StickerTextModel> dataList) {
            this.dataList = dataList;
        }

        public void setSelectedPosition(int selectedPosition) {
            if (this.selectedPosition == selectedPosition) {
                return;
            }
            this.selectedPosition = selectedPosition;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_live_room_setting_sticker_text_select_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final StickerTextModel model = dataList.get(position);
            holder.contentLayout.setSelected(selectedPosition == position);
            holder.iv.setVisibility(View.GONE);
            holder.iv.setTranslationY(0);
            holder.tv.setText(model.text);
            holder.tv.setStyle(model.style);
            holder.tv.setLetterSpacing(model.textSpacing);
            holder.tv.setTextSize(model.textSize);
            switch (model.style) {
                case 7:
                    holder.iv.setVisibility(VISIBLE);
                    holder.iv.setImageResource(R.drawable.plv_sticker_seven_ic);
                    break;
                case 8:
                    holder.iv.setVisibility(VISIBLE);
                    holder.iv.setImageResource(R.drawable.plv_sticker_eight_ic);
                    holder.iv.setTranslationY(-ConvertUtils.dp2px(3.6f));
                    break;
            }
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == holder.getAdapterPosition()) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.changeStyle(model.text, model.style);
                    }
                    selectedPosition = holder.getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewGroup contentLayout;
            PLVStrokeTextView tv;
            ImageView iv;

            ViewHolder(View itemView) {
                super(itemView);
                contentLayout = itemView.findViewById(R.id.plv_sticker_text_content_layout);
                tv = itemView.findViewById(R.id.plv_sticker_text_content);
                iv = itemView.findViewById(R.id.plv_sticker_text_left_icon);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        updateViewWithOrientation();
        setupRecyclerView();
        setVisibility(View.VISIBLE);
        if (onViewActionListener != null) {
            onViewActionListener.onShow(true);
        }
    }

    public void close() {
        setVisibility(View.GONE);
        if (onViewActionListener != null) {
            onViewActionListener.onShow(false);
        }
    }

    public void setSelectedPosition(int selectedPosition) {
        ((StickerTextSelectAdapter) textSelectRv.getAdapter()).setSelectedPosition(selectedPosition);
    }

    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        LayoutParams layoutParam = (LayoutParams) settingStickerLayoutRoot.getLayoutParams();

        if (PLVScreenUtils.isPortrait(getContext())) {
            layoutParam.height = LAYOUT_HEIGHT_PORT;
        } else {
            layoutParam.height = LAYOUT_HEIGHT_LAND;
        }

        settingStickerLayoutRoot.setLayoutParams(layoutParam);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onShow(boolean isShow);

        void cancel();

        void done();

        void changeStyle(String text, int style);
    }
    // </editor-fold>
}
