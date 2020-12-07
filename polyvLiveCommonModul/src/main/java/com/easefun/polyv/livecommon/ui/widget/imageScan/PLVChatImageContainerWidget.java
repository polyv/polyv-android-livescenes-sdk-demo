package com.easefun.polyv.livecommon.ui.widget.imageScan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVCircleProgressView;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressStatusListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVUrlTag;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVMyProgressManager;


public class PLVChatImageContainerWidget extends FrameLayout {
    public static final String LOADIMG_MOUDLE_TAG = "PLVChatImageContainerWidget";
    private PLVUrlTag imgUrlTag;
    private PLVScaleImageView ivChatImg;
    private PLVCircleProgressView cpvImgLoading;
    private OnClickListener onClickListener;
    private int position;

    public PLVChatImageContainerWidget(@NonNull Context context) {
        this(context, null);
    }

    public PLVChatImageContainerWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVChatImageContainerWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_image_container_layout, this);
        cpvImgLoading = findViewById(R.id.cpv_img_loading);
        ivChatImg = findViewById(R.id.iv_chat_img);
        ivChatImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (imgUrlTag != null) {
            PLVMyProgressManager.removeListener(LOADIMG_MOUDLE_TAG, LOADIMG_MOUDLE_TAG + imgUrlTag);//can no use
        }
        if (ivChatImg.getDrawable() != null) {
            ivChatImg.setImageDrawable(null);
        }
        if (cpvImgLoading != null) {
            cpvImgLoading.setTag(null);//need
            cpvImgLoading.setVisibility(View.GONE);
            cpvImgLoading.setProgress(0);
        }
    }

    public void setData(final PLVUrlTag imgUrlTag, final int position) {
        this.imgUrlTag = imgUrlTag;
        this.position = position;
        if (imgUrlTag != null) {
            cpvImgLoading.setTag(imgUrlTag);
            PLVImageLoader.getInstance()
                    .loadImage(getContext(), LOADIMG_MOUDLE_TAG, LOADIMG_MOUDLE_TAG + imgUrlTag, R.drawable.plv_icon_image_load_err,
                            new PLVAbsProgressStatusListener(imgUrlTag.getUrl()) {

                                @Override
                                public void onStartStatus(String url) {
                                    if (cpvImgLoading.getTag() != imgUrlTag) {//addFistData position can replace
                                        return;
                                    }
                                    if (cpvImgLoading.getProgress() == 0 && cpvImgLoading.getVisibility() != View.VISIBLE) {
                                        cpvImgLoading.setVisibility(View.VISIBLE);
                                        ivChatImg.setImageDrawable(null);
                                    }
                                }

                                @Override
                                public void onResourceReadyStatus(Drawable drawable) {
                                    if (cpvImgLoading.getTag() != imgUrlTag) {
                                        return;
                                    }
                                    ivChatImg.drawablePrepared(drawable);
                                }

                                @Override
                                public void onProgressStatus(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                                    if (cpvImgLoading.getTag() != imgUrlTag) {
                                        return;
                                    }
                                    if (isComplete) {
                                        cpvImgLoading.setVisibility(View.GONE);
                                        cpvImgLoading.setProgress(100);
                                    } else {//onFailed之后可能触发onProgress
                                        if (ivChatImg.getDrawable() != null) {
                                            ivChatImg.setImageDrawable(null);
                                        }
                                        cpvImgLoading.setVisibility(View.VISIBLE);
                                        cpvImgLoading.setProgress(percentage);
                                    }
                                }

                                @Override
                                public void onFailedStatus(@Nullable Exception e, Object model) {
                                    if (cpvImgLoading.getTag() != imgUrlTag) {
                                        return;
                                    }
                                    cpvImgLoading.setVisibility(View.GONE);
                                    cpvImgLoading.setProgress(0);
                                    ivChatImg.setImageResource(R.drawable.plv_icon_image_load_err);//fail can no set
                                }
                            }, ivChatImg);
        }
    }

    public PLVChatImageContainerWidget setOnImgClickListener(OnClickListener l) {
        this.onClickListener = l;
        return this;
    }
}
