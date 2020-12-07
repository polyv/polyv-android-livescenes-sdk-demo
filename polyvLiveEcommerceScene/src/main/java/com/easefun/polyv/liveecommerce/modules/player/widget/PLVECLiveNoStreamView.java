package com.easefun.polyv.liveecommerce.modules.player.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVViewLocationSensor;
import com.easefun.polyv.liveecommerce.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 暂无直播view
 */
public class PLVECLiveNoStreamView extends FrameLayout {
    private ViewGroup parentLy;
    private ImageView nostreamIv;
    private TextView nostreamTv;

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

    public void acceptPortraitSmall() {
        post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams flp = (LayoutParams) parentLy.getLayoutParams();
                flp.height = (int) (getWidth() / parentWHRatio);
                flp.width = -1;
                parentLy.setLayoutParams(flp);

                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) nostreamIv.getLayoutParams();
                llp.height = (int) (flp.height * imageHRatio);
                llp.width = (int) (llp.height * imageWRatio);
                llp.topMargin = 0;
                nostreamIv.setLayoutParams(llp);

                LinearLayout.LayoutParams tvLLP = (LinearLayout.LayoutParams) nostreamTv.getLayoutParams();
                tvLLP.topMargin = ConvertUtils.dp2px(6);
                nostreamTv.setLayoutParams(tvLLP);

                nostreamTv.setTextSize(10);
            }
        });
    }

    public void acceptPortraitBig() {
        post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams flp = (LayoutParams) parentLy.getLayoutParams();
                flp.height = ConvertUtils.dp2px(210);
                flp.width = -1;
                parentLy.setLayoutParams(flp);

                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) nostreamIv.getLayoutParams();
                llp.height = ConvertUtils.dp2px(116);
                llp.width = ConvertUtils.dp2px(150);
                llp.topMargin = ConvertUtils.dp2px(12);
                nostreamIv.setLayoutParams(llp);

                LinearLayout.LayoutParams tvLLP = (LinearLayout.LayoutParams) nostreamTv.getLayoutParams();
                tvLLP.topMargin = ConvertUtils.dp2px(14);
                nostreamTv.setLayoutParams(tvLLP);

                nostreamTv.setTextSize(14);
            }
        });
    }
}
