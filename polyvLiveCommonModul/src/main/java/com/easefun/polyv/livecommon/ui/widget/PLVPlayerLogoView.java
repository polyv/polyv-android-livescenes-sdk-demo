package com.easefun.polyv.livecommon.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSimpleUrlWebViewActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 播放器logo布局
 */
public class PLVPlayerLogoView extends FrameLayout {

    private final List<LogoParam> logoParams = new ArrayList<>();
    private final Map<String, int[]> bitmapSizeCache = new HashMap<>();
    private final Map<LogoParam, ImageView> imageViewMap = new HashMap<>();
    private OnClickLogoListener onClickLogoListener = new OnClickLogoListener();

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVPlayerLogoView(@NonNull Context context) {
        this(context, null);
    }

    public PLVPlayerLogoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPlayerLogoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    public void addLogo(final LogoParam logoParam) {
        addLogo(logoParam, false);
    }

    public LogoParam getParamZero() {
        if (logoParams.size() >= 1) {
            return logoParams.get(0);
        }
        return null;
    }

    public void setOnClickLogoListener(OnClickLogoListener onClickLogoListener) {
        this.onClickLogoListener = onClickLogoListener;
    }

    private void addLogo(final LogoParam logoParam, boolean posted) {
        if (logoParam == null) {
            return;
        }
        if (!posted && (logoParam.width < 1 || logoParam.height < 1)) {
            if (getWidth() == 0 || getHeight() == 0) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        addLogo(logoParam, true);
                    }
                });
                return;
            }
        }

        final ImageView imageView;
        if (imageViewMap.containsKey(logoParam)) {
            imageView = imageViewMap.get(logoParam);
        } else {
            imageView = new ImageView(getContext());
        }

        //获取Bitmap的尺寸
        getBitmapSize(logoParam, new OnGetBitmapSizeCallback() {
            @Override
            public void onGetBitmapSize(int bitmapWidth, int bitmapHeight) {
                //处理输入的宽和高
                float inputLogoWidth = logoParam.width;
                float inputLogoHeight = logoParam.height;
                if (inputLogoWidth <= 1) {//百分比
                    inputLogoWidth = getWidth() * inputLogoWidth;
                }
                if (inputLogoHeight <= 1) {
                    inputLogoHeight = getHeight() * inputLogoHeight;
                }

                //定义剪裁后的宽高
                float trimmedWidth;
                float trimmedHeight;

                //对输入宽高和bitmap宽高做一个等比例的裁剪
                float bitmapAspectRatio = (float) bitmapWidth / (float) bitmapHeight;
                float inputAspectRatio = inputLogoWidth / inputLogoHeight;
                if (bitmapAspectRatio == inputAspectRatio) {
                    //do noting
                    trimmedWidth = inputLogoWidth;
                    trimmedHeight = inputLogoHeight;
                } else if (bitmapAspectRatio > inputAspectRatio) {
                    //param中的height太长了，要裁掉部分height
                    trimmedHeight = bitmapHeight * inputLogoWidth / bitmapWidth;
                    trimmedWidth = inputLogoWidth;
                } else {
                    //param中的width太长了，要裁掉w部分width
                    trimmedWidth = bitmapWidth * inputLogoHeight / bitmapHeight;
                    trimmedHeight = inputLogoHeight;
                }

                //设置可见度
                if (logoParam.pos == 0) {
                    imageView.setVisibility(View.GONE);
                }
                imageView.setAlpha(logoParam.alpha / 100f);

                //设置位置
                LayoutParams flp = makeLPForLogo(logoParam, trimmedWidth, trimmedHeight);

                imageView.setLayoutParams(flp);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickLogoListener != null) {
                            onClickLogoListener.onClickLogo(v, logoParam);
                        }
                    }
                });
                if (imageView.getParent() != null) {
                    ((ViewGroup) imageView.getParent()).removeView(imageView);
                }
                addView(imageView);

                //加载图片
                if (logoParam.resId != 0) {
                    PLVImageLoader.getInstance().loadImage(getContext(), logoParam.resId, new ViewTarget<ImageView, Drawable>(imageView) {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource);
                        }
                    });
                } else {
                    PLVImageLoader.getInstance().loadImage(getContext(), logoParam.resUrl, new ViewTarget<ImageView, Drawable>(imageView) {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource);
                        }
                    });
                }
                imageViewMap.put(logoParam, imageView);

                if (!logoParams.contains(logoParam)) {
                    logoParams.add(logoParam);
                }
            }
        });
    }

    /**
     * 获取bitmap，并获取缩放和裁剪后的logo的尺寸
     */
    private void getBitmapSize(final LogoParam logoParam, final OnGetBitmapSizeCallback cb) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (logoParam.resId != 0) {
            BitmapFactory.decodeResource(getResources(), logoParam.resId, options);
            cb.onGetBitmapSize(options.outWidth, options.outHeight);
        } else {
            //此处用glide获取图片的尺寸方式可以优化，见issue:https://github.com/bumptech/glide/issues/781
            if (bitmapSizeCache.containsKey(logoParam.resUrl)) {
                int[] bitmapSize = bitmapSizeCache.get(logoParam.resUrl);
                if (bitmapSize != null) {
                    cb.onGetBitmapSize(bitmapSize[0], bitmapSize[1]);
                    return;
                }
            }
            if (getContext() instanceof Activity && ((Activity) getContext()).isFinishing()) {
                return;
            }
            Glide.with(getContext()).asBitmap().load(logoParam.resUrl).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    bitmapSizeCache.put(logoParam.resUrl, new int[]{resource.getWidth(), resource.getHeight()});
                    cb.onGetBitmapSize(resource.getWidth(), resource.getHeight());
                }
            });
        }
    }


    /**
     * 生成logo的LayoutParams
     */
    private LayoutParams makeLPForLogo(LogoParam logoParam, float logoWidth, float logoHeight) {
        LayoutParams flp = new LayoutParams((int) logoWidth, (int) logoHeight);
        if (logoParam.pos == 1) {
            flp.gravity = Gravity.LEFT | Gravity.TOP;
            if (logoParam.offsetX <= 1) {
                flp.leftMargin = (int) (getWidth() * logoParam.offsetX);
            } else {
                flp.leftMargin = (int) logoParam.offsetX;
            }
            if (logoParam.offsetY <= 1) {
                flp.topMargin = (int) (getHeight() * logoParam.offsetY);
            } else {
                flp.topMargin = (int) logoParam.offsetY;
            }
        } else if (logoParam.pos == 2) {
            flp.gravity = Gravity.RIGHT | Gravity.TOP;
            if (logoParam.offsetX <= 1) {
                flp.rightMargin = (int) (getWidth() * logoParam.offsetX);
            } else {
                flp.rightMargin = (int) logoParam.offsetX;
            }
            if (logoParam.offsetY <= 1) {
                flp.topMargin = (int) (getHeight() * logoParam.offsetY);
            } else {
                flp.topMargin = (int) logoParam.offsetY;
            }
        } else if (logoParam.pos == 3) {
            flp.gravity = Gravity.LEFT | Gravity.BOTTOM;
            if (logoParam.offsetX <= 1) {
                flp.leftMargin = (int) (getWidth() * logoParam.offsetX);
            } else {
                flp.leftMargin = (int) logoParam.offsetX;
            }
            if (logoParam.offsetY <= 1) {
                flp.bottomMargin = (int) (getHeight() * logoParam.offsetY);
            } else {
                flp.bottomMargin = (int) logoParam.offsetY;
            }
        } else if (logoParam.pos == 4) {
            flp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            if (logoParam.offsetX <= 1) {
                flp.rightMargin = (int) (getWidth() * logoParam.offsetX);
            } else {
                flp.rightMargin = (int) logoParam.offsetX;
            }
            if (logoParam.offsetY <= 1) {
                flp.bottomMargin = (int) (getHeight() * logoParam.offsetY);
            } else {
                flp.bottomMargin = (int) logoParam.offsetY;
            }
        }
        return flp;
    }

    public void removeAllLogo() {
        removeAllViews();
        logoParams.clear();
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        logoParams.clear();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (!logoParams.isEmpty()) {
                        PLVPlayerLogoView.super.removeAllViews();
                        for (LogoParam logoParam : logoParams) {
                            addLogo(logoParam);
                        }
                    }
                }
            });
        }
    }

    public static class LogoParam {
        private float width = 80;//logo宽，支持像素和百分比两种单位，如 100px 或 10%。<=1则是百分比
        private float height = 100;
        private int resId;//logo图片资源id
        private String resUrl;//logo图片url，和resId选1个即可，同时存在取resId
        private int pos = 1;//位置 0,1,2,3,4 (隐藏、左上、右上、左下、右下)
        private int alpha = 100;//透明度(0-100)，0即完全透明
        private float offsetX = 0;//logo偏移，支持像素和百分比两种单位。<=1则是百分比
        private float offsetY = 0;
        private String logoHref;

        public LogoParam() {
        }

        public float getWidth() {
            return width;
        }

        public LogoParam setWidth(float width) {
            this.width = width;
            return this;
        }

        public float getHeight() {
            return height;
        }

        public LogoParam setHeight(float height) {
            this.height = height;
            return this;
        }

        public int getResId() {
            return resId;
        }

        public LogoParam setResId(int resId) {
            this.resId = resId;
            return this;
        }

        public String getResUrl() {
            return resUrl;
        }

        public LogoParam setResUrl(String resUrl) {
            this.resUrl = resUrl;
            return this;
        }

        public int getPos() {
            return pos;
        }

        public LogoParam setPos(int pos) {
            this.pos = pos;
            return this;
        }

        public int getAlpha() {
            return alpha;
        }

        public LogoParam setAlpha(int alpha) {
            this.alpha = alpha;
            return this;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public LogoParam setOffsetX(float offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public LogoParam setOffsetY(float offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        public String getLogoHref() {
            return logoHref;
        }

        public LogoParam setLogoHref(String logoHref) {
            this.logoHref = logoHref;
            return this;
        }
    }

    public static class OnClickLogoListener {

        public void onClickLogo(final View v, @NonNull final LogoParam logoParam) {
            if (TextUtils.isEmpty(logoParam.getLogoHref())) {
                return;
            }
            PLVSimpleUrlWebViewActivity.start(v.getContext(), logoParam.getLogoHref());
        }

    }

    private interface OnGetBitmapSizeCallback {
        void onGetBitmapSize(int bitmapWidth, int bitmapHeight);
    }
}
