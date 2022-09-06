package com.easefun.polyv.liveecommerce.modules.player.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.player.IPLVPlayErrorView;
import com.easefun.polyv.livecommon.module.utils.PLVViewLocationSensor;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.annos.Sp;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 暂无直播view
 */
public class PLVECLiveNoStreamView extends FrameLayout implements IPLVPlayErrorView {
    private ViewGroup parentLy;
    private ImageView nostreamIv;
    private TextView nostreamTv;

    private TextView changeLinesTv;
    private TextView refreshTv;
    private boolean isShowChangeLinesView = false;
    private boolean isShowRefreshView = false;

    private boolean isSmallLayout;
    private boolean isFullLayout;

    private PLVViewLocationSensor locationSensor;
    private float parentWHRatio = 1.78f;
    private float imageHRatio = 0.5f;
    private float imageWRatio = 1.28f;

    public PLVECLiveNoStreamView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECLiveNoStreamView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLiveNoStreamView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initLocationSensor();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_player_nostream_layout, this);
        parentLy = findViewById(R.id.parent_ly);
        nostreamIv = findViewById(R.id.nostream_iv);
        nostreamTv = findViewById(R.id.nostream_tv);

        changeLinesTv = findViewById(com.easefun.polyv.livecommon.R.id.plv_change_lines_tv);
        refreshTv = findViewById(com.easefun.polyv.livecommon.R.id.plv_refresh_tv);
    }

    private void initLocationSensor() {
        locationSensor = new PLVViewLocationSensor(this, new PLVViewLocationSensor.OnViewLocationSensorListener() {
            @Override
            public void onLandscapeSmall() {
            }

            @Override
            public void onLandscapeBig() {
            }

            @Override
            public void onPortraitSmall() {
                acceptPortraitSmall();
            }

            @Override
            public void onPortraitBig() {
                acceptPortraitBig();
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        locationSensor.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        locationSensor.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void setPlaceHolderImg(@DrawableRes int resId) {
        nostreamIv.setImageResource(resId);
    }

    @Override
    public void setPlaceHolderText(String text) {
        nostreamTv.setText(text);
    }

    @Override
    public void setPlaceHolderTextSize(@Sp float size) {
        nostreamTv.setTextSize(size);
    }

    @Override
    public void setChangeLinesViewVisibility(int visibility) {
        changeLinesTv.setVisibility(isSmallLayout ? View.GONE : visibility);
        isShowChangeLinesView = visibility == View.VISIBLE;
    }

    @Override
    public void setRefreshViewVisibility(int visibility) {
        refreshTv.setVisibility(isSmallLayout ? View.GONE : visibility);
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
        isFullLayout = false;
        updateLayoutSize();
    }

    public void setFullLayout() {
        isFullLayout = true;
        updateLayoutSize();
    }

    private int updateLayoutSize() {
        if (isFullLayout) {
            return updateLayoutSize(-1);
        }
        return updateLayoutSize(isSmallLayout ? (int) (getWidth() / parentWHRatio) : ConvertUtils.dp2px(210));
    }

    private int updateLayoutSize(int height) {
        LayoutParams flp = (LayoutParams) parentLy.getLayoutParams();
        flp.height = height;
        flp.width = -1;
        parentLy.setLayoutParams(flp);
        return height;
    }

    public void acceptPortraitSmall() {
        post(new Runnable() {
            @Override
            public void run() {
                isSmallLayout = true;
                int layoutHeight = updateLayoutSize();

                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) nostreamIv.getLayoutParams();
                llp.height = (int) (layoutHeight * imageHRatio);
                llp.width = (int) (llp.height * imageWRatio);
                llp.topMargin = 0;
                nostreamIv.setLayoutParams(llp);

                LinearLayout.LayoutParams tvLLP = (LinearLayout.LayoutParams) nostreamTv.getLayoutParams();
                tvLLP.topMargin = ConvertUtils.dp2px(6);
                nostreamTv.setLayoutParams(tvLLP);

                nostreamTv.setTextSize(10);
                boolean isErrorLayout = isShowRefreshView || isShowChangeLinesView || nostreamTv.getText().length() > 10;
                if (isErrorLayout) {
                    nostreamTv.setVisibility(View.GONE);
                }

                changeLinesTv.setVisibility(View.GONE);
                refreshTv.setVisibility(View.GONE);
            }
        });
    }

    public void acceptPortraitBig() {
        post(new Runnable() {
            @Override
            public void run() {
                isSmallLayout = false;
                updateLayoutSize();

                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) nostreamIv.getLayoutParams();
                llp.height = ConvertUtils.dp2px(116);
                llp.width = ConvertUtils.dp2px(150);
                llp.topMargin = 0;
                nostreamIv.setLayoutParams(llp);

                LinearLayout.LayoutParams tvLLP = (LinearLayout.LayoutParams) nostreamTv.getLayoutParams();
                boolean isErrorLayout = isShowRefreshView || isShowChangeLinesView || nostreamTv.getText().length() > 10;
                tvLLP.topMargin = isErrorLayout ? -ConvertUtils.dp2px(16) : ConvertUtils.dp2px(12);
                nostreamTv.setLayoutParams(tvLLP);

                nostreamTv.setTextSize(12);
                nostreamTv.setVisibility(View.VISIBLE);

                if (isShowChangeLinesView) {
                    changeLinesTv.setVisibility(View.VISIBLE);
                }
                if (isShowRefreshView) {
                    refreshTv.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
