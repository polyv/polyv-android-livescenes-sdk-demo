package com.easefun.polyv.livecloudclass.modules.download.adapter.viewholder;

import static com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO.bytesToFitSizeString;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheListViewModel;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.plv.foundationsdk.component.di.PLVDependManager;

/**
 * @author Hoshiiro
 */
public class PLVDownloadingCacheViewHolder extends PLVAbsPlaybackCacheViewHolder implements View.OnClickListener {

    public static View createView(ViewGroup viewGroup) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plv_playback_cache_downloading_item_layout, viewGroup, false);
    }

    private final PLVPlaybackCacheListViewModel playbackCacheListViewModel = PLVDependManager.getInstance().get(PLVPlaybackCacheListViewModel.class);

    private TextView playbackCacheDownloadingVideoTitleTv;
    private ProgressBar playbackCacheDownloadingProgressBar;
    private TextView playbackCacheDownloadingStatusTv;
    private TextView playbackCacheDownloadingSizeTv;
    private PLVRoundImageView playbackCacheDownloadingDeleteIv;
    private PLVRoundImageView playbackCacheDownloadingStartOrPauseIv;

    private PLVPlaybackCacheVideoVO videoVO;

    public PLVDownloadingCacheViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        playbackCacheDownloadingVideoTitleTv = itemView.findViewById(R.id.plv_playback_cache_downloading_video_title_tv);
        playbackCacheDownloadingProgressBar = itemView.findViewById(R.id.plv_playback_cache_downloading_progress_bar);
        playbackCacheDownloadingStatusTv = itemView.findViewById(R.id.plv_playback_cache_downloading_status_tv);
        playbackCacheDownloadingSizeTv = itemView.findViewById(R.id.plv_playback_cache_downloading_size_tv);
        playbackCacheDownloadingDeleteIv = itemView.findViewById(R.id.plv_playback_cache_downloading_delete_iv);
        playbackCacheDownloadingStartOrPauseIv = itemView.findViewById(R.id.plv_playback_cache_downloading_start_or_pause_iv);
    }

    @Override
    public void bind(PLVPlaybackCacheVideoVO vo) {
        this.videoVO = vo;
        playbackCacheDownloadingVideoTitleTv.setText(vo.getTitle());
        playbackCacheDownloadingProgressBar.setProgress(getOrDefault(vo.getProgress(), 0));
        playbackCacheDownloadingStatusTv.setText(vo.getDownloadStatusEnum().getStatusName());
        playbackCacheDownloadingSizeTv.setText(format("{}/{}", bytesToFitSizeString(vo.getDownloadedBytes()), bytesToFitSizeString(vo.getTotalBytes())));
        playbackCacheDownloadingStartOrPauseIv.setImageResource(isDownloading(vo) ? R.drawable.plv_playback_cache_pause_icon : R.drawable.plv_playback_cache_start_icon);

        playbackCacheDownloadingDeleteIv.setOnClickListener(this);
        playbackCacheDownloadingStartOrPauseIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == playbackCacheDownloadingDeleteIv.getId()) {
            playbackCacheListViewModel.deleteDownload(videoVO);
        } else if (id == playbackCacheDownloadingStartOrPauseIv.getId()) {
            final boolean isDownloading = videoVO.getDownloadStatusEnum() == PLVPlaybackCacheDownloadStatusEnum.WAITING || videoVO.getDownloadStatusEnum() == PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING;
            if (isDownloading) {
                playbackCacheListViewModel.pauseDownload(videoVO);
            } else {
                playbackCacheListViewModel.startDownload(videoVO);
            }
        }
    }

    private static boolean isDownloading(PLVPlaybackCacheVideoVO vo) {
        return vo.getDownloadStatusEnum() == PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING;
    }

}
