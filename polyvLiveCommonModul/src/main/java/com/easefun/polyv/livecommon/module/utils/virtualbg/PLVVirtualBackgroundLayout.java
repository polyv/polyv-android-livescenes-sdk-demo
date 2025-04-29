package com.easefun.polyv.livecommon.module.utils.virtualbg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.google.gson.reflect.TypeToken;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PLVVirtualBackgroundLayout {
    private static final String KEY_UPLOAD_VIRTUAL_BACKGROUND = "key_upload_virtual_background";
    private static final String KEY_SELECT_VIRTUAL_BACKGROUND = "key_select_virtual_background";
    private static final String NUMBER_PREFIX = "本地0";
    private static final int CAN_UPLOAD_SIZE = 3;
    private static final int NORMAL_BUTTON_COUNT = 3;
    private static PLVVirtualBackgroundLayout instance;
    private PopupWindow popupWindow;
    private View view;
    private PLVRoundRectLayout widgetRoundLy;
    private TextView virtualBgTitleTv;
    private View titleSeparator;
    private RecyclerView virtualBgTopRv;
    private RecyclerView virtualBgBottomRv;
    private List<VirtualBgItemData> virtualBgTopDataList;
    private VirtualBgAdapter virtualBgTopAdapter;
    private VirtualBgAdapter virtualBgBottomAdapter;
    private List<Integer> virtualBgImageIdList = Arrays.asList(
            R.drawable.plv_virtual_bg_1,
            R.drawable.plv_virtual_bg_2,
            R.drawable.plv_virtual_bg_3,
            R.drawable.plv_virtual_bg_4,
            R.drawable.plv_virtual_bg_5,
            R.drawable.plv_virtual_bg_6,
            R.drawable.plv_virtual_bg_7,
            R.drawable.plv_virtual_bg_8,
            R.drawable.plv_virtual_bg_9,
            R.drawable.plv_virtual_bg_10
    );
    private List<Integer> virtualBgImageIdLandList = Arrays.asList(
            R.drawable.plv_virtual_bg_1_land,
            R.drawable.plv_virtual_bg_2_land,
            R.drawable.plv_virtual_bg_3_land,
            R.drawable.plv_virtual_bg_4_land,
            R.drawable.plv_virtual_bg_5_land,
            R.drawable.plv_virtual_bg_6_land,
            R.drawable.plv_virtual_bg_7_land,
            R.drawable.plv_virtual_bg_8_land,
            R.drawable.plv_virtual_bg_9_land,
            R.drawable.plv_virtual_bg_10
    );
    private List<String> uploadVirtualBackgroundList = new ArrayList<>();
    private int selectedPosition = -1;
    private boolean currentIsPortrait = false;
    private OnViewActionListener onViewActionListener;
    private boolean isUseBlackStyle;

    public static PLVVirtualBackgroundLayout init(View anchorView, OnViewActionListener listener) {
        if (instance == null) {
            instance = new PLVVirtualBackgroundLayout(anchorView, listener);
        }
        return instance;
    }

    @Nullable
    public static PLVVirtualBackgroundLayout useInstance() {
        return instance;
    }

    public static void tryShow() {
        if (instance != null) {
            instance.show();
        }
    }

    public static void destroy() {
        instance = null;
    }

    public PLVVirtualBackgroundLayout(View anchorView, OnViewActionListener listener) {
        this.currentIsPortrait = ScreenUtils.isPortrait();
        this.onViewActionListener = listener;
        String uploadVirtualBackgroundString = SPUtils.getInstance().getString(KEY_UPLOAD_VIRTUAL_BACKGROUND, "");
        selectedPosition = SPUtils.getInstance().getInt(KEY_SELECT_VIRTUAL_BACKGROUND, -1);
        if (!TextUtils.isEmpty(uploadVirtualBackgroundString)) {
            uploadVirtualBackgroundList = PLVGsonUtil.fromJson(new TypeToken<List<String>>() {
            }, uploadVirtualBackgroundString);
        }
        popupWindow = new PopupWindow(anchorView.getContext());
        view = PLVViewInitUtils.initPopupWindow(anchorView, R.layout.plv_virtual_background_layout, popupWindow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        widgetRoundLy = view.findViewById(R.id.plv_setting_virtual_bg_rl);
        virtualBgTitleTv = view.findViewById(R.id.plv_setting_virtual_bg_tv);
        titleSeparator = view.findViewById(R.id.plv_setting_title_separator);
        // top
        virtualBgTopRv = view.findViewById(R.id.plv_setting_virtual_bg_top_rv);
        virtualBgTopRv.setLayoutManager(new GridLayoutManager(anchorView.getContext(), 5));
        virtualBgTopDataList = new ArrayList<>();
        virtualBgTopDataList.add(new VirtualBgItemData(PLVAppUtils.getString(R.string.plv_live_nothing), R.drawable.plv_setting_cancel, null, false));
        virtualBgTopDataList.add(new VirtualBgItemData(PLVAppUtils.getString(R.string.plv_streamer_upload), R.drawable.plv_setting_add, null, false));
        virtualBgTopDataList.add(new VirtualBgItemData(PLVAppUtils.getString(R.string.plv_streamer_blur_bg), R.drawable.plv_setting_blur_bg, null, false));
        int j = 0;
        for (String path : uploadVirtualBackgroundList) {
            j++;
            String name = NUMBER_PREFIX + j;
            virtualBgTopDataList.add(new VirtualBgItemData(name, 0, path, true));
        }
        virtualBgTopAdapter = new VirtualBgAdapter(virtualBgTopDataList);
        virtualBgTopAdapter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(int position) {
                selectedPosition = position;
                virtualBgBottomAdapter.updateSelected(-1);
                SPUtils.getInstance().put(KEY_SELECT_VIRTUAL_BACKGROUND, position);
                onCallBySelectedBg(position, virtualBgTopAdapter.dataList, true);
            }
        });
        virtualBgTopRv.setAdapter(virtualBgTopAdapter);
        if (selectedPosition >= 0 && selectedPosition < CAN_UPLOAD_SIZE + NORMAL_BUTTON_COUNT) {
            virtualBgTopAdapter.updateSelected(selectedPosition);
            onCallBySelectedBg(selectedPosition, virtualBgTopAdapter.dataList, true);
        }
        // bottom
        virtualBgBottomRv = view.findViewById(R.id.plv_setting_virtual_bg_bottom_rv);
        virtualBgBottomRv.setLayoutManager(new GridLayoutManager(anchorView.getContext(), 5));
        List<VirtualBgItemData> virtualBgBottomDataList = new ArrayList<>();
        for (int i = 0; i < virtualBgImageIdList.size(); i++) {
            virtualBgBottomDataList.add(new VirtualBgItemData(null, virtualBgImageIdList.get(i), null, false));
        }
        virtualBgBottomAdapter = new VirtualBgAdapter(virtualBgBottomDataList);
        virtualBgBottomAdapter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(int position) {
                selectedPosition = position + CAN_UPLOAD_SIZE + NORMAL_BUTTON_COUNT;
                virtualBgTopAdapter.updateSelected(-1);
                SPUtils.getInstance().put(KEY_SELECT_VIRTUAL_BACKGROUND, position + CAN_UPLOAD_SIZE + NORMAL_BUTTON_COUNT);
                onCallBySelectedBg(position, virtualBgBottomAdapter.dataList, false);
            }
        });
        virtualBgBottomRv.setAdapter(virtualBgBottomAdapter);
        if (selectedPosition >= CAN_UPLOAD_SIZE + NORMAL_BUTTON_COUNT) {
            virtualBgBottomAdapter.updateSelected(selectedPosition - CAN_UPLOAD_SIZE - NORMAL_BUTTON_COUNT);
            onCallBySelectedBg(selectedPosition - CAN_UPLOAD_SIZE - NORMAL_BUTTON_COUNT, virtualBgBottomAdapter.dataList, false);
        }
        if (isUseBlackStyle) {
            setUseBlackStyle();
        }
    }

    public void setUseBlackStyle() {
        this.isUseBlackStyle = true;
        widgetRoundLy.setPadding(ConvertUtils.dp2px(16), 0, ConvertUtils.dp2px(16), 0);
        widgetRoundLy.setBackground(null);
        widgetRoundLy.setRoundMode(PLVRoundRectLayout.MODE_NONE);

        ViewGroup.MarginLayoutParams titleTvLayoutParams = (ViewGroup.MarginLayoutParams) virtualBgTitleTv.getLayoutParams();
        titleTvLayoutParams.topMargin = ConvertUtils.dp2px(14);
        titleTvLayoutParams.bottomMargin = ConvertUtils.dp2px(23);
        virtualBgTitleTv.setLayoutParams(titleTvLayoutParams);

        titleSeparator.setVisibility(View.VISIBLE);

        PLVBlurView blurView = (PLVBlurView) this.view.findViewById(R.id.blur_ly);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) blurView.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = (int) (Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()) * 0.44f);
        layoutParams.gravity = Gravity.RIGHT;
        blurView.setLayoutParams(layoutParams);
        blurView.setVisibility(View.VISIBLE);
        PLVBlurUtils.initBlurView(blurView);
    }

    public void addImage(String path) {
        String name = NUMBER_PREFIX + (uploadVirtualBackgroundList.size() + 1);
        virtualBgTopAdapter.add(new VirtualBgItemData(name, 0, path, true));
        uploadVirtualBackgroundList.add(path);
        SPUtils.getInstance().put(KEY_UPLOAD_VIRTUAL_BACKGROUND, PLVGsonUtil.toJson(uploadVirtualBackgroundList));
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public void confirmDeleteBg(int position) {
        virtualBgTopAdapter.delete(position);
    }

    public void onOrientationChanged(boolean isPortrait) {
        if (currentIsPortrait != isPortrait) {
            if (isPortrait) {
                onPortrait();
            } else {
                onLandscape();
            }
            updateSelectedBgWhenOrientation();
            currentIsPortrait = isPortrait;
        }
    }

    public void onPortrait() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) widgetRoundLy.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        widgetRoundLy.setLayoutParams(layoutParams);
    }

    public void onLandscape() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) widgetRoundLy.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = isUseBlackStyle ? (int) (landscapeWidth * 0.44f) : Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        layoutParams.gravity = Gravity.RIGHT;
        widgetRoundLy.setLayoutParams(layoutParams);
    }

    public void show() {
        if (ScreenUtils.isPortrait()) {
            onPortrait();
        } else {
            onLandscape();
        }
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    private void updateSelectedBgWhenOrientation() {
        if (selectedPosition >= 0 && selectedPosition < CAN_UPLOAD_SIZE + NORMAL_BUTTON_COUNT) {
            onCallBySelectedBg(selectedPosition, virtualBgTopAdapter.dataList, true);
        } else if (selectedPosition >= CAN_UPLOAD_SIZE + NORMAL_BUTTON_COUNT) {
            onCallBySelectedBg(selectedPosition - CAN_UPLOAD_SIZE - NORMAL_BUTTON_COUNT, virtualBgBottomAdapter.dataList, false);
        }
    }

    private void onCallBySelectedBg(int position, List<VirtualBgItemData> dataList, boolean isTop) {
        if (isTop && position == 0) {
            if (onViewActionListener != null) {
                onViewActionListener.onCancelBgAndBlur();
            }
        } else if (isTop && position == 2) {
            if (onViewActionListener != null) {
                onViewActionListener.onSelectedBlur();
            }
        } else {
            Bitmap bitmap = null;
            if (dataList.get(position).drawableId != 0) {
                int drawableId = dataList.get(position).drawableId;
                if (ScreenUtils.isLandscape()) {
                    drawableId = virtualBgImageIdLandList.get(position);
                }
                bitmap = PLVBitmapUtil.getBitmapFromResource(view.getContext(), drawableId);
            } else if (!TextUtils.isEmpty(dataList.get(position).drawablePath)) {
                bitmap = PLVBitmapUtil.getBitmapFromPath(dataList.get(position).drawablePath);
            }
            if (onViewActionListener != null) {
                onViewActionListener.onSelectedBg(bitmap);
            }
        }
    }

    private class VirtualBgAdapter extends RecyclerView.Adapter<VirtualBgAdapter.ViewHolder> {
        private List<VirtualBgItemData> dataList;
        private int selectedPosition = -1;
        private OnClickListener onClickListener;

        public VirtualBgAdapter(List<VirtualBgItemData> dataList) {
            this.dataList = dataList;
        }

        public List<VirtualBgItemData> getDataList() {
            return dataList;
        }

        public void add(VirtualBgItemData data) {
            dataList.add(data);
            selectedPosition = dataList.size() - 1;
            if (onClickListener != null) {
                onClickListener.onClick(selectedPosition);
            }
            notifyDataSetChanged();
        }

        public void updateSelected(int position) {
            if (this.selectedPosition != position) {
                this.selectedPosition = position;
                notifyDataSetChanged();
            }
        }

        public void delete(int position) {
            boolean needUpdateNumber = position != dataList.size() - 1;
            String path = dataList.get(position).drawablePath;
            dataList.remove(position);
            uploadVirtualBackgroundList.remove(path);
            SPUtils.getInstance().put(KEY_UPLOAD_VIRTUAL_BACKGROUND, PLVGsonUtil.toJson(uploadVirtualBackgroundList));
            // 更新序号
            if (needUpdateNumber) {
                for (int i = NORMAL_BUTTON_COUNT; i < dataList.size(); i++) {
                    dataList.get(i).name = NUMBER_PREFIX + (i - NORMAL_BUTTON_COUNT + 1);
                }
            }
            // 更新选择
            if (position == selectedPosition) {
                selectedPosition = 0;
                if (onClickListener != null) {
                    onClickListener.onClick(selectedPosition);
                }
            } else if (position < selectedPosition) {
                selectedPosition--;
                PLVVirtualBackgroundLayout.this.selectedPosition = selectedPosition;
                // 只有本地图片可以删除，因此这里直接使用selectedPosition
                SPUtils.getInstance().put(KEY_SELECT_VIRTUAL_BACKGROUND, selectedPosition);
            }
            notifyDataSetChanged();
        }

        public void setOnClickListener(OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_virtual_background_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            final VirtualBgItemData data = dataList.get(position);
            if (data.drawableId != 0) {
                PLVImageLoader.getInstance().loadImage(holder.primaryIv.getContext(), data.drawableId, holder.primaryIv);
            } else if (!TextUtils.isEmpty(data.drawablePath)) {
                PLVImageLoader.getInstance().loadImage(data.drawablePath, holder.primaryIv);
            } else {
                holder.primaryIv.setImageDrawable(null);
            }
            if (TextUtils.isEmpty(data.name)) {
                holder.nameTv.setVisibility(View.GONE);
            } else {
                holder.nameTv.setVisibility(View.VISIBLE);
                holder.nameTv.setText(data.name);
            }
            if (position == selectedPosition) {
                ((ViewGroup) holder.primaryIv.getParent()).setBackgroundResource(R.drawable.plv_setting_virtual_bg_bg);
            } else {
                ((ViewGroup) holder.primaryIv.getParent()).setBackground(null);
            }
            if (!data.canDelete) {
                holder.cancelIv.setVisibility(View.GONE);
            } else {
                holder.cancelIv.setVisibility(View.VISIBLE);
            }

            holder.primaryIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (data.drawableId == R.drawable.plv_setting_add) {
                        if (uploadVirtualBackgroundList.size() >= CAN_UPLOAD_SIZE) {
                            ToastUtils.showShort("最多上传" + CAN_UPLOAD_SIZE + "张图片"); // no need i18n
                            return;
                        }
                        PLVImageSelectorUtil.openGallery((Activity) view.getContext());
                    } else if (position != selectedPosition) {
                        updateSelected(position);
                        if (onClickListener != null) {
                            onClickListener.onClick(position);
                        }
                    }
                }
            });

            holder.cancelIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onConfirmDeleteBg(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList == null ? 0 : dataList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView primaryIv;
            public TextView nameTv;
            public ImageView cancelIv;

            public ViewHolder(View itemView) {
                super(itemView);
                primaryIv = itemView.findViewById(R.id.plv_virtual_bg_primary_iv);
                nameTv = itemView.findViewById(R.id.plv_setting_virtual_bg_name_tv);
                cancelIv = itemView.findViewById(R.id.plv_virtual_bg_cancel_iv);
            }
        }
    }

    public interface OnClickListener {
        void onClick(int position);
    }

    public interface OnViewActionListener {
        void onConfirmDeleteBg(int position);

        void onSelectedBg(Bitmap bitmap);

        void onCancelBgAndBlur();

        void onSelectedBlur();
    }

    private static class VirtualBgItemData {
        public String name;
        public int drawableId;
        public String drawablePath;
        public boolean canDelete;

        public VirtualBgItemData(String name, int drawableId, String drawablePath, boolean canDelete) {
            this.name = name;
            this.drawableId = drawableId;
            this.drawablePath = drawablePath;
            this.canDelete = canDelete;
        }
    }
}
