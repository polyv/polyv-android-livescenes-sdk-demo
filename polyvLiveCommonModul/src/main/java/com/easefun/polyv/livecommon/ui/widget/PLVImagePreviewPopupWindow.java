package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationListener;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import pl.droidsonroids.gif.GifImageView;

/**
 * @author ysh
 * @desc 悬浮图片预览效果，支持Gif显示加载
 */
public class PLVImagePreviewPopupWindow extends FrameLayout {

    private Context context;
    private PopupWindow previewWindow;
    private GifImageView gifImageView;
    private PLVTriangleIndicateLayout triangleIndicateLayout;

    //弹窗显示时，如果贴近边缘，将会触发minX， maxX，用以留出安全距离
    private int minX;
    private int maxX;

    public PLVImagePreviewPopupWindow(Context context) {
        super(context);
        this.context = context;

        minX = ConvertUtils.dp2px(8);
        if(PLVScreenUtils.isLandscape(context)){
            maxX = ScreenUtils.getScreenHeight() - ConvertUtils.dp2px(8);
        } else {
            maxX = ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(8);
        }

        View inflate = LayoutInflater.from(context).inflate(R.layout.plv_emotion_preview_window, null);
        previewWindow = new PopupWindow(inflate, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gifImageView = inflate.findViewById(R.id.plv_gif_image_view);
        triangleIndicateLayout = inflate.findViewById(R.id.plv_triangle_layout);
        previewWindow.setContentView(inflate);
        previewWindow.setOutsideTouchable(true);
    }




    /**
     * 在 parent 的正上方水平居中显示
     * @param url
     * @param parent
     */
    public void showInTopCenter(String url, View parent){
        PLVImageLoader.getInstance().loadImage(url, gifImageView);

        int[] location = new int[2];
        parent.getLocationInWindow(location);

        previewWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        previewWindow.setWidth(previewWindow.getContentView().getMeasuredWidth());
        previewWindow.setHeight(previewWindow.getContentView().getMeasuredHeight());



        int locationX = (location[0]+parent.getWidth()/2) - previewWindow.getWidth()/2;
        int locationY = location[1] - previewWindow.getHeight();
        if(locationX < minX){
            locationX = minX;
        } else if (locationX + previewWindow.getWidth() > maxX){
            locationX = maxX - previewWindow.getWidth();
        }

        previewWindow.showAtLocation(parent, Gravity.NO_GRAVITY, locationX, locationY);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            maxX = ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(8);
        } else {
            maxX = ScreenUtils.getScreenHeight() - ConvertUtils.dp2px(8);
        }
    }
}
