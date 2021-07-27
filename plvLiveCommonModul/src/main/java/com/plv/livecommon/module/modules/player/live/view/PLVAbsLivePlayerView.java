package com.plv.livecommon.module.modules.player.live.view;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.plv.business.api.auxiliary.PLVAuxiliaryVideoview;
import com.plv.business.api.common.player.PLVPlayError;
import com.plv.business.model.video.PLVLiveMarqueeVO;
import com.plv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.plv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO;
import com.plv.livecommon.ui.widget.PLVPlayerLogoView;
import com.plv.livescenes.video.PLVLiveVideoView;

/**
 * mvp-直播播放器view层抽象类
 */
public abstract class PLVAbsLivePlayerView implements IPLVLivePlayerContract.ILivePlayerView {
    @Override
    public void setPresenter(@NonNull IPLVLivePlayerContract.ILivePlayerPresenter presenter) {

    }

    @Override
    public PLVLiveVideoView getLiveVideoView() {
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
    public View getNoStreamIndicator() {
        return null;
    }

    @Override
    public PLVPlayerLogoView getLogo() {
        return null;
    }

    @Override
    public void onSubVideoViewPlay(boolean isFirst) {

    }

    @Override
    public void onSubVideoViewLoadImage(String imageUrl, ImageView imageView) {

    }

    @Override
    public void onSubVideoViewClick(boolean mainPlayerIsPlaying) {

    }

    @Override
    public void onSubVideoViewCountDown(boolean isOpenAdHead, int totalTime, int remainTime, int adStage) {

    }

    public void onSubVideoViewVisiblityChanged(boolean isOpenAdHead, boolean isShow) {

    }

    @Override
    public void onPlayError(PLVPlayError error, String tips) {

    }

    @Override
    public void onNoLiveAtPresent() {

    }

    @Override
    public void onLiveStop() {

    }

    @Override
    public void onLiveEnd() {

    }

    @Override
    public void onPrepared(int mediaPlayMode) {

    }

    @Override
    public void onLinesChanged(int linesPos) {

    }

    @Override
    public void onGetMarqueeVo(PLVLiveMarqueeVO marqueeVo, String viewerName) {

    }

    @Override
    public void onRestartPlay() {

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
    public void updatePlayInfo(PLVPlayInfoVO playInfoVO) {
    }

    @Override
    public void onServerDanmuOpen(boolean isServerDanmuOpen) {

    }

    @Override
    public void onShowPPTView(int visible) {

    }

    @Override
    public boolean onNetworkRecover() {
        return false;
    }
}
