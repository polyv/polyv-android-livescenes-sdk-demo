package com.easefun.polyv.liveecommerce.modules.player.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
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

import com.easefun.polyv.livecommon.module.utils.PLVViewLocationSensor;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveAudioModeView;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 音频模式view
 */
public class PLVECLiveAudioModeView extends FrameLayout implements IPolyvLiveAudioModeView {
    private AnimationDrawable animationDrawable;
    private Disposable animationDisposable;
    private ViewGroup parentLy;
    private ImageView audioModeIv;
    private TextView audioModeTv;
    private static final int ANIMATION_TOTAL_DURATION = 1000;

    private PLVViewLocationSensor locationSensor;
    private float parentWHRatio = 1.78f;
    private float imageHRatio = 0.5f;
    private float imageWRatio = 0.4f;

    public PLVECLiveAudioModeView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECLiveAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLiveAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initLocationSensor();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_player_audio_mode_layout, this);
        setVisibility(View.GONE);
        parentLy = findViewById(R.id.parent_ly);
        audioModeIv = findViewById(R.id.audio_mode_iv);
        audioModeTv = findViewById(R.id.audio_mode_tv);
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

    private void startAnimation() {
        if (animationDrawable == null) {
            animationDrawable = new AnimationDrawable();
            animationDrawable.setOneShot(false);
        }
        if (animationDisposable == null) {
            animationDisposable = Observable.just(1).observeOn(Schedulers.io())
                    .doOnNext(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            int drawableCount = 3;
                            for (int i = 1; i <= drawableCount; i++) {
                                String firstDrawableName = getResources().getResourceName(R.drawable.plvec_audio_effect_1);
                                String drawableName = firstDrawableName.substring(0, firstDrawableName.length() - 1) + i;
                                int drawableId = getResources().getIdentifier(drawableName, getResources().getResourceTypeName(R.drawable.plvec_audio_effect_1), getContext().getPackageName());
                                animationDrawable.addFrame(getResources().getDrawable(drawableId), ANIMATION_TOTAL_DURATION / drawableCount);
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            audioModeIv.setImageDrawable(animationDrawable);
                            animationDrawable.start();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                        }
                    });
        }
    }

    private void stopAnimation() {
        if (animationDisposable != null) {
            animationDisposable.dispose();
            animationDisposable = null;
        }
        if (animationDrawable != null) {
            animationDrawable.stop();
            audioModeIv.setImageDrawable(null);
            animationDrawable = null;
        }
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
    public void onShow() {
        setVisibility(View.VISIBLE);
        startAnimation();
    }

    @Override
    public void onHide() {
        setVisibility(View.GONE);
        stopAnimation();
    }

    @Override
    public View getRoot() {
        return this;
    }

    public void acceptPortraitSmall() {
        post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams flp = (LayoutParams) parentLy.getLayoutParams();
                flp.height = (int) (getWidth() / parentWHRatio);
                flp.width = -1;
                parentLy.setLayoutParams(flp);

                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) audioModeIv.getLayoutParams();
                llp.height = (int) (flp.height * imageHRatio);
                llp.width = (int) (llp.height * imageWRatio);
                llp.topMargin = 0;
                audioModeIv.setLayoutParams(llp);

                LinearLayout.LayoutParams tvLLP = (LinearLayout.LayoutParams) audioModeTv.getLayoutParams();
                tvLLP.topMargin = ConvertUtils.dp2px(6);
                audioModeTv.setLayoutParams(tvLLP);

                audioModeTv.setTextSize(10);
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

                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) audioModeIv.getLayoutParams();
                llp.height = ConvertUtils.dp2px(120);
                llp.width = ConvertUtils.dp2px(48);
                llp.topMargin = ConvertUtils.dp2px(10);
                audioModeIv.setLayoutParams(llp);

                LinearLayout.LayoutParams tvLLP = (LinearLayout.LayoutParams) audioModeTv.getLayoutParams();
                tvLLP.topMargin = ConvertUtils.dp2px(14);
                audioModeTv.setLayoutParams(tvLLP);

                audioModeTv.setTextSize(14);
            }
        });
    }
}
