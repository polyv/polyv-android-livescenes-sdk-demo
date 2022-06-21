package com.easefun.polyv.livecloudclass.modules.download.widget;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Region;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.plv.foundationsdk.utils.PLVFormatUtils;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVLCPlaybackCacheDownloadProgressButton extends FrameLayout {

    private static final int COLOR_DOWNLOAD_STATUS_BACKGROUND_NOT_DOWNLOAD = PLVFormatUtils.parseColor("#3082FE");
    private static final int COLOR_DOWNLOAD_STATUS_BACKGROUND_DOWNLOADED = PLVFormatUtils.parseColor("#98c1ff");

    private PLVRoundRectGradientTextView playbackCacheDownloadStatusTv;
    private TextView playbackCacheDownloadProgressBackgroundTv;
    private TextView playbackCacheDownloadProgressForegroundTv;

    private PLVPlaybackCacheDownloadStatusEnum statusEnum = PLVPlaybackCacheDownloadStatusEnum.NOT_IN_DOWNLOAD_LIST;
    private int progress = 0;

    public PLVLCPlaybackCacheDownloadProgressButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLCPlaybackCacheDownloadProgressButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLCPlaybackCacheDownloadProgressButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        findView();
    }

    private void findView() {
        ViewGroup view = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.plvlc_playback_cache_download_progress_button, this, false);

        playbackCacheDownloadStatusTv = view.findViewById(R.id.plvlc_playback_cache_download_status_tv);
        playbackCacheDownloadProgressBackgroundTv = view.findViewById(R.id.plvlc_playback_cache_download_progress_background_tv);
        playbackCacheDownloadProgressForegroundTv = view.findViewById(R.id.plvlc_playback_cache_download_progress_foreground_tv);

        view.removeAllViews();
        this.addView(playbackCacheDownloadStatusTv);
        this.addView(playbackCacheDownloadProgressBackgroundTv);
        this.addView(playbackCacheDownloadProgressForegroundTv);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child != playbackCacheDownloadProgressForegroundTv) {
            return super.drawChild(canvas, child, drawingTime);
        }
        final int left = (int) (getWidth() * progress / 100F);
        final int state = canvas.save();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutRect(left, 0, getWidth(), getHeight());
        } else {
            canvas.clipRect(left, 0, getWidth(), getHeight(), Region.Op.DIFFERENCE);
        }
        final boolean res = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(state);
        return res;
    }

    public void update(PLVPlaybackCacheVideoVO vo) {
        this.progress = getOrDefault(vo.getProgress(), 0);
        this.statusEnum = vo.getDownloadStatusEnum();
        updateViewProperties();
        invalidate();
    }

    private void updateViewProperties() {
        final List<PLVPlaybackCacheDownloadStatusEnum> downloadingStatusList = listOf(
                PLVPlaybackCacheDownloadStatusEnum.WAITING,
                PLVPlaybackCacheDownloadStatusEnum.PAUSING,
                PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING
        );
        final boolean isDownloading = downloadingStatusList.contains(statusEnum);

        playbackCacheDownloadStatusTv.setVisibility(isDownloading ? View.GONE : View.VISIBLE);
        playbackCacheDownloadProgressBackgroundTv.setVisibility(isDownloading ? View.VISIBLE : View.GONE);
        playbackCacheDownloadProgressForegroundTv.setVisibility(isDownloading ? View.VISIBLE : View.GONE);
        if (!isDownloading) {
            if (statusEnum == PLVPlaybackCacheDownloadStatusEnum.NOT_IN_DOWNLOAD_LIST) {
                playbackCacheDownloadStatusTv.setText("立即下载");
                playbackCacheDownloadStatusTv.updateBackgroundColor(COLOR_DOWNLOAD_STATUS_BACKGROUND_NOT_DOWNLOAD);
            } else if (statusEnum == PLVPlaybackCacheDownloadStatusEnum.DOWNLOADED) {
                playbackCacheDownloadStatusTv.setText("已下载");
                playbackCacheDownloadStatusTv.updateBackgroundColor(COLOR_DOWNLOAD_STATUS_BACKGROUND_DOWNLOADED);
            } else if (statusEnum == PLVPlaybackCacheDownloadStatusEnum.DOWNLOAD_FAIL) {
                playbackCacheDownloadStatusTv.setText("下载失败");
                playbackCacheDownloadStatusTv.updateBackgroundColor(COLOR_DOWNLOAD_STATUS_BACKGROUND_DOWNLOADED);
            }
        }
        if (isDownloading) {
            if (PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING.equals(statusEnum)) {
                playbackCacheDownloadProgressBackgroundTv.setText(format("下载中 {}%", progress));
                playbackCacheDownloadProgressForegroundTv.setText(format("下载中 {}%", progress));
            } else {
                playbackCacheDownloadProgressBackgroundTv.setText(statusEnum.getStatusName());
                playbackCacheDownloadProgressForegroundTv.setText(statusEnum.getStatusName());
            }
        }
    }

}
