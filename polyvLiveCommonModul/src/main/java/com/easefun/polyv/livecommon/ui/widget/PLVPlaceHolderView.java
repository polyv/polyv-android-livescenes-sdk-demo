package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.player.IPLVPlayErrorView;
import com.easefun.polyv.livecommon.module.utils.PLVViewLocationSensor;
import com.plv.foundationsdk.annos.Sp;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * date: 2020/9/17
 * author: HWilliamgo
 * description: 占位图View
 */
public class PLVPlaceHolderView extends ConstraintLayout implements IPLVPlayErrorView {

    // <editor-fold defaultstate="collapsed" desc="静态变量">
    //图片在横屏主屏中的比例
    private static final float PERCENT_WIDTH_IN_MAIN_LAND = 0.38f;
    //图片在竖屏主屏中的比例
    private static final float PERCENT_WIDTH_IN_MAIN_PORT = 0.48f;

    //图片在小窗中的比例
    private static final float IMG_PERCENT_WIDTH_IN_SMALL = 0.6f;

    private static final int RIGHT_MARGIN_IN_MAIN_LAND = ConvertUtils.dp2px(24);
    private static final int RIGHT_MARGIN_IN_MAIN_PORT = ConvertUtils.dp2px(16);

    private static final int PADDING_IN_MAIN_LAND = ConvertUtils.dp2px(8);
    private static final int PADDING_IN_MAIN_PORT = ConvertUtils.dp2px(4);

    private static final int TEXT_SIZE_IN_MAIN_LAND = 14;
    private static final int TEXT_SIZE_IN_MAIN_PORT = 12;

    private static final float VERTICAL_BIAS_LAND = 0.72f;
    private static final float VERTICAL_BIAS_PORT = 0.80f;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实例变量">
    //View
    private ImageView ivPlaceholderImg;
    private TextView tvPlaceholderText;

    private TextView changeLinesTv;
    private TextView refreshTv;
    private boolean isShowChangeLinesView = false;
    private boolean isShowRefreshView = false;

    //位置监听器
    private PLVViewLocationSensor locationSensor;

    private boolean respondLocationSensor = true;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVPlaceHolderView(@NonNull Context context) {
        this(context, null);
    }

    public PLVPlaceHolderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPlaceHolderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
    @Override
    public void setPlaceHolderImg(@DrawableRes int resId) {
        ivPlaceholderImg.setImageResource(resId);
    }

    /**
     * 设置占位图文本
     *
     * @param text 文本
     */
    @Override
    public void setPlaceHolderText(String text) {
        tvPlaceholderText.setText(text);
    }

    @Override
    public void setPlaceHolderTextSize(@Sp float size) {
        tvPlaceholderText.setTextSize(size);
    }

    @Override
    public void setChangeLinesViewVisibility(int visibility) {
        changeLinesTv.setVisibility(visibility);
        isShowChangeLinesView = visibility == View.VISIBLE;
    }

    @Override
    public void setRefreshViewVisibility(int visibility) {
        refreshTv.setVisibility(visibility);
        isShowRefreshView = visibility == View.VISIBLE;
    }

    public void setOnChangeLinesViewClickListener(OnClickListener listener) {
        changeLinesTv.setOnClickListener(listener);
    }

    public void setOnRefreshViewClickListener(OnClickListener listener) {
        refreshTv.setOnClickListener(listener);
    }

    @Override
    public void setViewVisibility(int visibility) {
        setVisibility(visibility);
    }

    /**
     * 设置响应位置监听器状态，默认响应。
     *
     * @param enable enable
     */
    public void enableRespondLocationSensor(boolean enable) {
        respondLocationSensor = enable;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        setBackgroundColor(getResources().getColor(R.color.colorEbonyClay));
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_player_nostream_view, this, true);

        ivPlaceholderImg = findViewById(R.id.plvlc_iv_placeholder_img);
        tvPlaceholderText = findViewById(R.id.plvlc_tv_placeholder_text);
        changeLinesTv = findViewById(R.id.plv_change_lines_tv);
        refreshTv = findViewById(R.id.plv_refresh_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化位置监听器">
    private void initLocationSensor() {
        locationSensor = new PLVViewLocationSensor(this, new PLVViewLocationSensor.OnViewLocationSensorListener() {
            @Override
            public void onLandscapeSmall() {
                if (respondLocationSensor) {
                    setLandscapeSmall();
                }
            }

            @Override
            public void onLandscapeBig() {
                if (respondLocationSensor) {
                    setLandscapeBig();
                }
            }

            @Override
            public void onPortraitSmall() {
                if (respondLocationSensor) {
                    setPortraitSmall();
                }
            }

            @Override
            public void onPortraitBig() {
                if (respondLocationSensor) {
                    setPortraitBig();
                }
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
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
                lpOfImg.matchConstraintPercentWidth = PERCENT_WIDTH_IN_MAIN_PORT;
                ivPlaceholderImg.setLayoutParams(lpOfImg);
                tvPlaceholderText.setVisibility(View.VISIBLE);
                tvPlaceholderText.setTextSize(TEXT_SIZE_IN_MAIN_PORT);

                LayoutParams lpOfRefreshTv = (LayoutParams) refreshTv.getLayoutParams();
                lpOfRefreshTv.verticalBias = VERTICAL_BIAS_PORT;
                LayoutParams lpOfChangeLinesTv = (LayoutParams) changeLinesTv.getLayoutParams();
                lpOfChangeLinesTv.verticalBias = VERTICAL_BIAS_PORT;
                lpOfChangeLinesTv.rightMargin = RIGHT_MARGIN_IN_MAIN_PORT;
                if (isShowChangeLinesView) {
                    changeLinesTv.setVisibility(View.VISIBLE);
                }
                if (isShowRefreshView) {
                    refreshTv.setVisibility(View.VISIBLE);
                }
                changeLinesTv.setPadding(0, PADDING_IN_MAIN_PORT, 0, PADDING_IN_MAIN_PORT);
                refreshTv.setPadding(0, PADDING_IN_MAIN_PORT, 0, PADDING_IN_MAIN_PORT);
                changeLinesTv.setTextSize(TEXT_SIZE_IN_MAIN_PORT);
                refreshTv.setTextSize(TEXT_SIZE_IN_MAIN_PORT);
            }
        });
    }

    private void setPortraitSmall() {
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
                lpOfImg.matchConstraintPercentWidth = IMG_PERCENT_WIDTH_IN_SMALL;
                ivPlaceholderImg.setLayoutParams(lpOfImg);
                tvPlaceholderText.setVisibility(View.GONE);

                changeLinesTv.setVisibility(View.GONE);
                refreshTv.setVisibility(View.GONE);
            }
        });
    }

    private void setLandscapeBig() {
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
                lpOfImg.matchConstraintPercentWidth = PERCENT_WIDTH_IN_MAIN_LAND;
                ivPlaceholderImg.setLayoutParams(lpOfImg);
                tvPlaceholderText.setVisibility(View.VISIBLE);
                tvPlaceholderText.setTextSize(TEXT_SIZE_IN_MAIN_LAND);

                LayoutParams lpOfRefreshTv = (LayoutParams) refreshTv.getLayoutParams();
                lpOfRefreshTv.verticalBias = VERTICAL_BIAS_LAND;
                LayoutParams lpOfChangeLinesTv = (LayoutParams) changeLinesTv.getLayoutParams();
                lpOfChangeLinesTv.verticalBias = VERTICAL_BIAS_LAND;
                lpOfChangeLinesTv.rightMargin = RIGHT_MARGIN_IN_MAIN_LAND;
                if (isShowChangeLinesView) {
                    changeLinesTv.setVisibility(View.VISIBLE);
                }
                if (isShowRefreshView) {
                    refreshTv.setVisibility(View.VISIBLE);
                }
                changeLinesTv.setPadding(0, PADDING_IN_MAIN_LAND, 0, PADDING_IN_MAIN_LAND);
                refreshTv.setPadding(0, PADDING_IN_MAIN_LAND, 0, PADDING_IN_MAIN_LAND);
                changeLinesTv.setTextSize(TEXT_SIZE_IN_MAIN_LAND);
                refreshTv.setTextSize(TEXT_SIZE_IN_MAIN_LAND);
            }
        });
    }

    private void setLandscapeSmall() {
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams lpOfImg = (LayoutParams) ivPlaceholderImg.getLayoutParams();
                lpOfImg.matchConstraintPercentWidth = IMG_PERCENT_WIDTH_IN_SMALL;
                ivPlaceholderImg.setLayoutParams(lpOfImg);
                tvPlaceholderText.setVisibility(View.GONE);

                changeLinesTv.setVisibility(View.GONE);
                refreshTv.setVisibility(View.GONE);
            }
        });
    }
// </editor-fold>
}
