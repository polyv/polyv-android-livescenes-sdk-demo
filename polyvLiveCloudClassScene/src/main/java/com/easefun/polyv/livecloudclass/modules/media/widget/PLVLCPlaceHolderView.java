package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.PLVViewLocationSensor;

/**
 * date: 2020/9/17
 * author: HWilliamgo
 * description: 占位图View
 */
public class PLVLCPlaceHolderView extends ConstraintLayout {

    // <editor-fold defaultstate="collapsed" desc="静态变量">
    //图片在横屏主屏中的比例
    private static final float PERCENT_WIDTH_IN_MAIN_LAND = 0.38f;
    //图片在竖屏主屏中的比例
    private static final float PERCENT_WIDTH_IN_MAIN_PORT = 0.48f;

    //图片在小窗中的比例
    private static final float IMG_PERCENT_WIDTH_IN_SMALL = 0.6f;

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实例变量">
    //View
    private ImageView ivPlaceholderImg;
    private TextView tvPlaceholderText;

    //位置监听器
    private PLVViewLocationSensor locationSensor;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCPlaceHolderView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCPlaceHolderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCPlaceHolderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initLocationSensor();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 设置占位图图片
     *
     * @param resId 图片ID
     */
    public void setPlaceHolderImg(@DrawableRes int resId) {
        ivPlaceholderImg.setImageResource(resId);
    }

    /**
     * 设置占位图文本
     *
     * @param text 文本
     */
    public void setPlaceHolderText(String text) {
        tvPlaceholderText.setText(text);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        setBackgroundColor(getResources().getColor(R.color.colorEbonyClay));
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_player_nostream_view, this, true);

        ivPlaceholderImg = findViewById(R.id.plvlc_iv_placeholder_img);
        tvPlaceholderText = findViewById(R.id.plvlc_tv_placeholder_text);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化位置监听器">
    private void initLocationSensor() {
        locationSensor = new PLVViewLocationSensor(this, new PLVViewLocationSensor.OnViewLocationSensorListener() {
            @Override
            public void onLandscapeSmall() {
                setLandscapeSmall();
            }

            @Override
            public void onLandscapeBig() {
                setLandscapeBig();
            }

            @Override
            public void onPortraitSmall() {
                setPortraitSmall();
            }

            @Override
            public void onPortraitBig() {
                setPortraitBig();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PLVViewLocationSensor调用，监听View位置">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        locationSensor.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSizeChanged(final int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        locationSensor.onSizeChanged(w, h, oldw, oldh);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕位置改变重置View属性">
    private void setPortraitBig() {
        LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = PERCENT_WIDTH_IN_MAIN_PORT;
        ivPlaceholderImg.setLayoutParams(lpOfImg);
        tvPlaceholderText.setVisibility(VISIBLE);
    }

    private void setPortraitSmall() {
        LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = IMG_PERCENT_WIDTH_IN_SMALL;
        ivPlaceholderImg.setLayoutParams(lpOfImg);
        tvPlaceholderText.setVisibility(GONE);
    }

    private void setLandscapeBig() {
        LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = PERCENT_WIDTH_IN_MAIN_LAND;
        ivPlaceholderImg.setLayoutParams(lpOfImg);
        tvPlaceholderText.setVisibility(VISIBLE);
    }

    private void setLandscapeSmall() {
        LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = IMG_PERCENT_WIDTH_IN_SMALL;
        ivPlaceholderImg.setLayoutParams(lpOfImg);
        tvPlaceholderText.setVisibility(GONE);
    }
// </editor-fold>
}
