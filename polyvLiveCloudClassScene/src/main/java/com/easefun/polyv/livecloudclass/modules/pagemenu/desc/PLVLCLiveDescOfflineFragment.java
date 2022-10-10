package com.easefun.polyv.livecloudclass.modules.pagemenu.desc;

import androidx.lifecycle.Observer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.player.live.enums.PLVLiveStateEnum;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheVideoViewModel;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.plv.foundationsdk.component.di.PLVDependManager;

/**
 * @author Hoshiiro
 */
public class PLVLCLiveDescOfflineFragment extends PLVBaseFragment {

    private final PLVPlaybackCacheVideoViewModel playbackCacheVideoViewModel = PLVDependManager.getInstance().get(PLVPlaybackCacheVideoViewModel.class);

    private View rootView;
    private RelativeLayout parentLy;
    private RelativeLayout topLy;
    private ImageView liveCoverIv;
    private TextView titleTv;
    private TextView statusTv;
    private View splitView;

    private boolean isCachedPlaybackVideo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) {
            return rootView;
        }
        rootView = inflater.inflate(R.layout.plvlc_page_menu_desc_offline_fragment, null);
        initView();
        return rootView;
    }

    private void initView() {
        findView();
        observeCacheVideo();
    }

    private void findView() {
        parentLy = rootView.findViewById(R.id.parent_ly);
        topLy = rootView.findViewById(R.id.top_ly);
        liveCoverIv = rootView.findViewById(R.id.live_cover_iv);
        titleTv = rootView.findViewById(R.id.title_tv);
        statusTv = rootView.findViewById(R.id.status_tv);
        splitView = rootView.findViewById(R.id.split_view);
    }

    private void observeCacheVideo() {
        playbackCacheVideoViewModel.getPlaybackCacheUpdateLiveData()
                .observe(this, new Observer<PLVPlaybackCacheVideoVO>() {
                    @Override
                    public void onChanged(@Nullable PLVPlaybackCacheVideoVO playbackCacheVideoVO) {
                        if (playbackCacheVideoVO == null) {
                            return;
                        }
                        titleTv.setText(playbackCacheVideoVO.getTitle());
                        isCachedPlaybackVideo = playbackCacheVideoVO.getDownloadStatusEnum() == PLVPlaybackCacheDownloadStatusEnum.DOWNLOADED;
                        updateLiveStatus();
                    }
                });
    }

    private void updateLiveStatus() {
        final PLVLiveStateEnum liveStateEnum = isCachedPlaybackVideo ? PLVLiveStateEnum.PLAYBACK_CACHED : PLVLiveStateEnum.PLAYBACK;
        statusTv.setText(liveStateEnum.getDescription());
        if (isCachedPlaybackVideo) {
            statusTv.setTextColor(getResources().getColor(R.color.plvlc_live_desc_playback_cached_text_color));
            statusTv.setBackgroundResource(R.drawable.plvlc_live_status_playback_cached);
        } else {
            statusTv.setTextColor(getResources().getColor(R.color.text_red));
            statusTv.setBackgroundResource(R.drawable.plvlc_live_status_live);
        }
    }

}
