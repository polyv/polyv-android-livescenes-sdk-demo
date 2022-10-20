package com.easefun.polyv.livecommon.module.modules.player.live.view;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.livecommon.module.modules.marquee.IPLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;

/**
 * mvp-直播播放器view层抽象类
 */
public abstract class PLVAbsLivePlayerView implements IPLVLivePlayerContract.ILivePlayerView {
    @Override
    public void setPresenter(@NonNull IPLVLivePlayerContract.ILivePlayerPresenter presenter) {

    }

    @Override
    public PolyvLiveVideoView getLiveVideoView() {
        return null;
    }

    @Override
    public PolyvAuxiliaryVideoview getSubVideoView() {
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
    public View getPlayErrorIndicator() {
        return null;
    }

    @Override
    public PLVPlayerLogoView getLogo() {
        return null;
    }

    @Override
    public IPLVMarqueeView getMarqueeView(){
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

    @Override
    public void onSubVideoViewVisiblityChanged(boolean isOpenAdHead, boolean isShow) {

    }

    @Override
    public void onPlayError(PolyvPlayError error, String tips) {

    }

    @Override
    public void onLoadSlow(int loadedTime, boolean isBufferEvent) {

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
    public void updatePlayInfo(PLVPlayInfoVO playInfoVO) { }

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

    @Override
    public void onOnlyAudio(boolean isOnlyAudio) {

    }

    @Override
    public void onLowLatencyNetworkQuality(int networkQuality) {

    }
}
