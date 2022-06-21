package com.easefun.polyv.livecloudclass.modules.download.adapter.viewholder;

import static com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO.bytesToFitSizeString;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class PLVDownloadedCacheViewHolder extends PLVAbsPlaybackCacheViewHolder implements View.OnClickListener {

    public static View createView(ViewGroup viewGroup) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plv_playback_cache_downloaded_item_layout, viewGroup, false);
    }

    private final PLVPlaybackCacheListViewModel playbackCacheListViewModel = PLVDependManager.getInstance().get(PLVPlaybackCacheListViewModel.class);

    private TextView playbackCacheDownloadedVideoTitleTv;
    private TextView playbackCacheDownloadedSizeTv;
    private PLVRoundImageView playbackCacheDownloadedDeleteIv;

    private PLVPlaybackCacheVideoVO videoVO;

    public PLVDownloadedCacheViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        playbackCacheDownloadedVideoTitleTv = itemView.findViewById(R.id.plv_playback_cache_downloaded_video_title_tv);
        playbackCacheDownloadedSizeTv = itemView.findViewById(R.id.plv_playback_cache_downloaded_size_tv);
        playbackCacheDownloadedDeleteIv = itemView.findViewById(R.id.plv_playback_cache_downloaded_delete_iv);
    }

    @Override
    public void bind(PLVPlaybackCacheVideoVO vo) {
        this.videoVO = vo;
        playbackCacheDownloadedVideoTitleTv.setText(vo.getTitle());
        if (vo.getDownloadStatusEnum() == PLVPlaybackCacheDownloadStatusEnum.DOWNLOADED) {
            playbackCacheDownloadedSizeTv.setText(bytesToFitSizeString(vo.getTotalBytes()));
        } else {
            playbackCacheDownloadedSizeTv.setText(vo.getDownloadStatusEnum().getStatusName());
        }

        itemView.setOnClickListener(this);
        playbackCacheDownloadedDeleteIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == playbackCacheDownloadedDeleteIv.getId()) {
            playbackCacheListViewModel.deleteDownload(videoVO);
        } else if (id == itemView.getId()) {
            playbackCacheListViewModel.requestLaunchDownloadedPlayback(videoVO);
        }
    }
}
