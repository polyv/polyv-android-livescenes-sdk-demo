package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveAudioModeView;
import com.easefun.polyv.livecommon.module.utils.PLVViewLocationSensor;
import com.plv.foundationsdk.utils.PLVScreenUtils;

/**
 * date: 2019/6/14 0014
 *
 * @author hwj
 * description 只听音频View
 */
public class PLVLCLiveAudioModeView extends ConstraintLayout implements IPolyvLiveAudioModeView {

    // <editor-fold defaultstate="collapsed" desc="静态变量">
    private static final String TAG = PLVLCLiveAudioModeView.class.getSimpleName();
    //图片在竖屏主屏中的比例
    private static final float PERCENT_WIDTH_IN_MAIN_PORT_AUDIO_MODE = 0.44f;
    //图片在横屏主屏中的比例
    private static final float PERCENT_WIDTH_IN_MAIN_LAND = 0.38f;

    //图片在小窗中的比例
    private static final float IMG_PERCENT_WIDTH_IN_SMALL = 0.6f;

    private static final int DP_TV_WIDTH_LAND = 96;
    private static final int DP_TV_HEIGHT_LAND = 30;
    private static final int DP_TV_WIDTH_PORT = 80;
    private static final int DP_TV_HEIGHT_PORT = 24;

    private static final int DP_TV_MARGIN_BOTTOM_LAND = 16;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实例变量">
    //listener
    private OnChangeVideoModeListener onChangeVideoModeListener;

    //View
    private ImageView ivAudioModeImg;
    private TextView tvPlaceholderAudioModePlayVideo;

    private PLVViewLocationSensor locationSensor;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCLiveAudioModeView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCLiveAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLiveAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(INVISIBLE);
        initView();
        initLocationSensor();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void setOnChangeVideoModeListener(OnChangeVideoModeListener li) {
        onChangeVideoModeListener = li;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        setBackgroundColor(getResources().getColor(R.color.colorEbonyClay));
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_player_audio_mode_view, this);
        ivAudioModeImg = findViewById(R.id.plvlc_iv_audio_mode_img);
        tvPlaceholderAudioModePlayVideo = findViewById(R.id.plvlc_tv_placeholder_audio_mode_play_video);

        tvPlaceholderAudioModePlayVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onChangeVideoModeListener != null) {
                    onChangeVideoModeListener.onClickPlayVideo();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化位置监听器">
    private void initLocationSensor() {
        locationSensor = new PLVViewLocationSensor(this, new PLVViewLocationSensor.OnViewLocationSensorListener() {
            @Override
            public void onLandscapeSmall() {
                //这里都用post来改变子View属性，直接调用会在初次改变属性时失效。
                post(new Runnable() {
                    @Override
                    public void run() {
                        setLandscapeSmall();

                    }
                });
            }

            @Override
            public void onLandscapeBig() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setLandscapeBig();
                    }
                });
            }

            @Override
            public void onPortraitSmall() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setPortraitSmall();
                    }
                });
            }

            @Override
            public void onPortraitBig() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setPortraitBig();
                    }
                });
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕位置改变重置View属性">
    private void setPortraitBig() {
        LayoutParams lpOfImg = (LayoutParams) ivAudioModeImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = PERCENT_WIDTH_IN_MAIN_PORT_AUDIO_MODE;
        ivAudioModeImg.setLayoutParams(lpOfImg);

        tvPlaceholderAudioModePlayVideo.setVisibility(VISIBLE);
        MarginLayoutParams lpOfTv = (MarginLayoutParams) tvPlaceholderAudioModePlayVideo.getLayoutParams();
        lpOfTv.bottomMargin = 0;
        lpOfTv.width = PLVScreenUtils.dip2px(DP_TV_WIDTH_PORT);
        lpOfTv.height = PLVScreenUtils.dip2px(DP_TV_HEIGHT_PORT);
        tvPlaceholderAudioModePlayVideo.setLayoutParams(lpOfTv);
    }

    private void setPortraitSmall() {
        LayoutParams lpOfImg = (LayoutParams) ivAudioModeImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = IMG_PERCENT_WIDTH_IN_SMALL;
        ivAudioModeImg.setLayoutParams(lpOfImg);

        tvPlaceholderAudioModePlayVideo.setVisibility(GONE);
    }

    private void setLandscapeBig() {
        LayoutParams lpOfImg = (LayoutParams) ivAudioModeImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = PERCENT_WIDTH_IN_MAIN_LAND;
        ivAudioModeImg.setLayoutParams(lpOfImg);

        tvPlaceholderAudioModePlayVideo.setVisibility(VISIBLE);
        MarginLayoutParams lpOfTv = (MarginLayoutParams) tvPlaceholderAudioModePlayVideo.getLayoutParams();
        lpOfTv.bottomMargin = PLVScreenUtils.dip2px(DP_TV_MARGIN_BOTTOM_LAND);
        lpOfTv.width = PLVScreenUtils.dip2px(DP_TV_WIDTH_LAND);
        lpOfTv.height = PLVScreenUtils.dip2px(DP_TV_HEIGHT_LAND);
        tvPlaceholderAudioModePlayVideo.setLayoutParams(lpOfTv);
    }

    private void setLandscapeSmall() {
        LayoutParams lpOfImg = (LayoutParams) ivAudioModeImg.getLayoutParams();
        lpOfImg.matchConstraintPercentWidth = IMG_PERCENT_WIDTH_IN_SMALL;
        ivAudioModeImg.setLayoutParams(lpOfImg);

        tvPlaceholderAudioModePlayVideo.setVisibility(GONE);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PLVViewLocationSensor调用，监听View位置">
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
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPolyvCloudClassAudioModeView接口实现">
    @Override
    public void onShow() {
        setVisibility(VISIBLE);
        requestLayout();
    }

    @Override
    public void onHide() {
        setVisibility(GONE);
    }


    @Override
    public View getRoot() {
        return this;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">
    public interface OnChangeVideoModeListener {
        void onClickPlayVideo();
    }
// </editor-fold>
}
