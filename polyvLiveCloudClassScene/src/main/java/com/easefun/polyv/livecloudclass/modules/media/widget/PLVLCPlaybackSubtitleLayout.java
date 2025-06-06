package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVLCPlaybackSubtitleLayout extends FrameLayout {

    private static final int MARGIN_BOTTOM_PORTRAIT = ConvertUtils.dp2px(40);
    private static final int MARGIN_BOTTOM_LANDSCAPE = ConvertUtils.dp2px(80);

    private PLVRoundRectConstraintLayout playbackSubtitleContainer;
    private TextView playbackSubtitleTv;

    public PLVLCPlaybackSubtitleLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLCPlaybackSubtitleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLCPlaybackSubtitleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_playback_subtitle_layout, this);
        playbackSubtitleContainer = findViewById(R.id.plvlc_playback_subtitle_container);
        playbackSubtitleTv = findViewById(R.id.plvlc_playback_subtitle_tv);
    }

    public void initData(IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter presenter) {
        presenter.getData().getPlayInfoVO().observe((LifecycleOwner) getContext(), new Observer<PLVPlayInfoVO>() {
            @Override
            public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                if (playInfoVO == null || playInfoVO.getSubtitles().isEmpty()) {
                    playbackSubtitleContainer.setVisibility(View.GONE);
                    return;
                }
                final String showSubtitleText = PLVSequenceWrapper.wrap(playInfoVO.getSubtitles())
                        .filter(new Function1<String, Boolean>() {
                            @Override
                            public Boolean invoke(String s) {
                                return s != null && !s.isEmpty();
                            }
                        })
                        .joinToString("\n");
                if (TextUtils.isEmpty(showSubtitleText.trim())) {
                    playbackSubtitleContainer.setVisibility(View.GONE);
                } else {
                    playbackSubtitleContainer.setVisibility(View.VISIBLE);
                    playbackSubtitleTv.setText(showSubtitleText);
                }
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MarginLayoutParams layoutParams = (MarginLayoutParams) playbackSubtitleContainer.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.bottomMargin = MARGIN_BOTTOM_PORTRAIT;
        } else {
            layoutParams.bottomMargin = MARGIN_BOTTOM_LANDSCAPE;
        }
        playbackSubtitleContainer.setLayoutParams(layoutParams);
    }
}
