package com.plv.livecommon.module.modules.player.playback.view;

import android.support.annotation.NonNull;
import android.view.View;

import com.plv.business.api.auxiliary.PLVAuxiliaryVideoview;
import com.plv.business.api.common.player.PLVPlayError;
import com.plv.business.model.video.PLVLiveMarqueeVO;
import com.plv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.plv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.plv.livecommon.ui.widget.PLVPlayerLogoView;
import com.plv.livescenes.playback.video.PLVPlaybackVideoView;

/**
 * mvp-回放播放器view层抽象类
 */
public abstract class PLVAbsPlaybackPlayerView implements IPLVPlaybackPlayerContract.IPlaybackPlayerView {
    @Override
    public void setPresenter(@NonNull IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter presenter) {

    }

    @Override
    public PLVPlaybackVideoView getPlaybackVideoView() {
        return null;
    }

    @Override
    public PLVAuxiliaryVideoview getSubVideoView() {
        return null;
    }

    @Override
    public View getBufferingIndicator() {
        return null;
    }

    @Override
    public View getRetryLayout() {
        return null;
    }

    @Override
    public PLVPlayerLogoView getLogo() {
        return null;
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onPlayError(PLVPlayError error, String tips) {

    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onVideoPlay(boolean isFirst) {

    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onSubVideoViewCountDown(boolean isOpenAdHead, int totalTime, int remainTime, int adStage) {

    }

    @Override
    public void onSubVideoViewVisiblityChanged(boolean isOpenAdHead, boolean isShow) {

    }

    @Override
    public void onSubVideoViewPlay(boolean isFirst) {

    }

    @Override
    public void onBufferStart() {

    }

    @Override
    public void onBufferEnd() {

    }

    @Override
    public boolean onLightChanged(int changeValue, boolean isEnd) {
        return false;
    }

    @Override
    public boolean onVolumeChanged(int changeValue, boolean isEnd) {
        return false;
    }

    @Override
    public boolean onProgressChanged(int seekTime, int totalTime, boolean isEnd, boolean isRightSwipe) {
        return false;
    }

    @Override
    public void onDoubleClick() {

    }

    @Override
    public void onGetMarqueeVo(PLVLiveMarqueeVO marqueeVo, String viewerName) {

    }

    @Override
    public void onServerDanmuOpen(boolean isServerDanmuOpen) {

    }

    @Override
    public void onShowPPTView(int visible) {

    }

    @Override
    public void updatePlayInfo(PLVPlayInfoVO playInfoVO) {

    }
}
