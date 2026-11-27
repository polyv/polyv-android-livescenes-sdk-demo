package com.easefun.polyv.livecommon.module.modules.player.playback.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;

/** 只占用三个按钮的触摸区域，背景透明，不拦截其他互动浮层。 */
public class PLVPlaybackSeekControls extends LinearLayout {
    private final ImageButton backward;
    private final ImageButton playPause;
    private final ImageButton forward;
    private IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter presenter;
    private boolean controlsVisible = true;
    private boolean playButtonEnabled = true;
    private int renderedStep;
    private Runnable onControlAction;

    public PLVPlaybackSeekControls(Context context) {
        this(context, null);
    }

    public PLVPlaybackSeekControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        backward = addButton();
        playPause = addButton();
        forward = addButton();
        setVisibility(GONE);
        backward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                step(false);
            }
        });
        forward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                step(true);
            }
        });
        playPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter == null || !playButtonEnabled || presenter.isSubVideoViewShow()) {
                    return;
                }
                if (presenter.isPlaying()) {
                    presenter.pause();
                } else {
                    presenter.resume();
                }
                afterControl();
            }
        });
    }

    private ImageButton addButton() {
        ImageButton button = new ImageButton(getContext());
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        LayoutParams params = new LayoutParams(dp(56), dp(48));
        params.setMargins(dp(4), 0, dp(4), 0);
        addView(button, params);
        return button;
    }

    public void bind(IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter presenter, String channelId) {
        this.presenter = presenter;
        playButtonEnabled = PLVChannelFeatureManager.onChannel(channelId)
                .isFeatureSupport(PLVChannelFeature.LIVE_PLAYBACK_PLAY_BUTTON_ENABLE);
        refresh();
    }

    public void setOnControlAction(Runnable action) {
        onControlAction = action;
    }

    public void setControlsVisible(boolean visible) {
        controlsVisible = visible;
        refresh();
    }

    public void refresh() {
        boolean enabled = presenter != null && presenter.isPlaybackSeekButtonsEnabled();
        setVisibility(enabled && controlsVisible && !presenter.isSubVideoViewShow()
                && presenter.getDuration() > 0 && presenter.isInPlaybackState() ? VISIBLE : GONE);
        if (!enabled) {
            return;
        }
        int step = presenter.getPlaybackSeekStepSeconds();
        if (renderedStep != step) {
            renderedStep = step;
            backward.setImageDrawable(new SeekIcon(step, false));
            forward.setImageDrawable(new SeekIcon(step, true));
            backward.setContentDescription(getResources().getString(R.string.plv_playback_seek_backward_seconds, step));
            forward.setContentDescription(getResources().getString(R.string.plv_playback_seek_forward_seconds, step));
        }
        boolean playing = presenter.isPlaying();
        playPause.setVisibility(playButtonEnabled ? VISIBLE : GONE);
        playPause.setImageResource(playing ? R.drawable.plv_playback_seek_pause : R.drawable.plv_playback_seek_play);
        playPause.setContentDescription(getResources().getString(playing
                ? R.string.plv_playback_seek_pause : R.string.plv_playback_seek_play));
    }

    private void step(boolean forward) {
        if (presenter != null) {
            presenter.stepPlayback(forward);
            afterControl();
        }
    }

    private void afterControl() {
        refresh();
        if (onControlAction != null) {
            onControlAction.run();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class SeekIcon extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int step;
        private final boolean forward;

        SeekIcon(int step, boolean forward) {
            this.step = step;
            this.forward = forward;
            paint.setColor(Color.WHITE);
        }

        @Override
        public void draw(Canvas canvas) {
            int saved = canvas.save();
            canvas.translate(getBounds().left, getBounds().top);
            canvas.scale(getBounds().width() / 32F, getBounds().height() / 32F);
            int arrow = canvas.save();
            // 只旋转圆弧和箭头：快进缺口朝右，快退缺口朝左。
            canvas.rotate(forward ? 90 : -90, 16, 17);
            if (!forward) {
                canvas.scale(-1, 1, 16, 16);
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawArc(new RectF(4, 5, 28, 29), -45, 270, false, paint);
            float diagonal = 12F / (float) Math.sqrt(2);
            float tipX = 16F - diagonal;
            float tipY = 17F - diagonal;
            float tangent = 1F / (float) Math.sqrt(2);
            float baseX = tipX - 4F * tangent;
            float baseY = tipY + 4F * tangent;
            Path head = new Path();
            // 以圆弧切线为箭头中线，两翼对称分布；快退镜像到右侧。
            head.moveTo(baseX - 4F * tangent, baseY - 4F * tangent);
            head.lineTo(tipX, tipY);
            head.lineTo(baseX + 4F * tangent, baseY + 4F * tangent);
            canvas.drawPath(head, paint);
            canvas.restoreToCount(arrow);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(12);
            paint.setFakeBoldText(true);
            canvas.drawText(String.valueOf(step), 16, 21, paint);
            canvas.restoreToCount(saved);
        }

        @Override
        public void setAlpha(int alpha) { paint.setAlpha(alpha); }

        @Override
        public void setColorFilter(ColorFilter colorFilter) { paint.setColorFilter(colorFilter); }

        @Override
        public int getOpacity() { return PixelFormat.TRANSLUCENT; }

        @Override
        public int getIntrinsicWidth() { return 32; }

        @Override
        public int getIntrinsicHeight() { return 32; }
    }
}
