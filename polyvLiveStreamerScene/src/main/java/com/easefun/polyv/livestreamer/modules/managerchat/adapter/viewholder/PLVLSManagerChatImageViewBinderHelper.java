package com.easefun.polyv.livestreamer.modules.managerchat.adapter.viewholder;

import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livestreamer.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLSManagerChatImageViewBinderHelper {

    private static final String LOAD_IMAGE_MODULE_TAG = "PLVLSManagerChatroom";

    public static void fitChatImgWH(int width, int height, View view, int maxLengthDp, int minLengthDp) {
        int maxLength = ConvertUtils.dp2px(maxLengthDp);
        int minLength = ConvertUtils.dp2px(minLengthDp);
        //计算显示的图片大小
        float percentage = width * 1f / height;
        if (percentage == 1) {//方图
            if (width < minLength) {
                width = height = minLength;
            } else if (width > maxLength) {
                width = height = maxLength;
            }
        } else if (percentage < 1) {//竖图
            height = maxLength;
            width = (int) Math.max(minLength, height * percentage);
        } else {//横图
            width = maxLength;
            height = (int) Math.max(minLength, width / percentage);
        }
        ViewGroup.LayoutParams vlp = view.getLayoutParams();
        vlp.width = width;
        vlp.height = height;
        view.setLayoutParams(vlp);
    }

    public static void loadNetworkImage(ImageView imageView, ProgressBar loadingView, String url) {
        final int errorResId = R.drawable.plv_icon_image_load_err;
        PLVImageLoader.getInstance().loadImage(
                imageView.getContext(),
                LOAD_IMAGE_MODULE_TAG,
                LOAD_IMAGE_MODULE_TAG + url,
                errorResId,
                createProgressListener(imageView, loadingView, url, errorResId),
                imageView
        );
    }

    public static void bindCopyTextOnLongClickListener(final View view, final String contentToCopy) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PLVCopyBoardPopupWindow.showAndAnswer(v, true, true, contentToCopy, null);
                return true;
            }
        });
    }

    private static PLVAbsProgressListener createProgressListener(final ImageView imageView, final ProgressBar loadingView, final String url, @DrawableRes final int errorResId) {
        loadingView.setTag(url);
        return new PLVAbsProgressListener(url) {
            @Override
            public void onFailed(@Nullable Exception e, Object model) {
                if (!getUrl().equals(loadingView.getTag())) {
                    return;
                }
                loadingView.setVisibility(View.GONE);
                loadingView.setProgress(0);
                imageView.setImageResource(errorResId);
            }

            @Override
            public void onResourceReady(Drawable drawable) {
                if (!getUrl().equals(loadingView.getTag())) {
                    return;
                }
                imageView.setImageDrawable(drawable);
            }

            @Override
            public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                if (!getUrl().equals(loadingView.getTag())) {
                    return;
                }
                if (isComplete) {
                    loadingView.setProgress(100);
                    loadingView.setVisibility(View.GONE);
                } else {
                    if (imageView.getDrawable() != null) {
                        imageView.setImageDrawable(null);
                    }
                    loadingView.setVisibility(View.VISIBLE);
                    loadingView.setProgress(percentage);
                }
            }

            @Override
            public void onStart(String url) {
                if (!getUrl().equals(loadingView.getTag())) {
                    return;
                }
                if (loadingView.getProgress() == 0 && loadingView.getVisibility() != View.VISIBLE) {
                    loadingView.setVisibility(View.VISIBLE);
                    imageView.setImageDrawable(null);
                }
            }
        };
    }

}
