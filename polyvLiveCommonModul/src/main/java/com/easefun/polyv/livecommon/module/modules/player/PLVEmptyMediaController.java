package com.easefun.polyv.livecommon.module.modules.player;

import android.view.View;
import android.widget.MediaController;

import com.easefun.polyv.businesssdk.api.common.meidacontrol.IPolyvMediaController;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.plv.foundationsdk.log.PLVCommonLog;

/**
 * date: 2020-04-29
 * author: hwj
 * description:
 */
public class PLVEmptyMediaController implements IPolyvMediaController<PolyvLiveVideoView> {
    private static final String TAG = "PLVEmptyMediaController";
    @Override
    public void onPrepared(PolyvLiveVideoView mp) {
        PLVCommonLog.d(TAG,"onPrepared");
    }

    @Override
    public void onLongBuffering(String tip) {
        PLVCommonLog.d(TAG,"onLongBuffering");
    }

    @Override
    public void hide() {
        PLVCommonLog.d(TAG,"hide");
    }

    @Override
    public boolean isShowing() {
        return false;
    }

    @Override
    public void setAnchorView(View view) {
        PLVCommonLog.d(TAG,"setAnchorView");
    }

    @Override
    public void setEnabled(boolean enabled) {
        PLVCommonLog.d(TAG,"setEnabled:"+enabled);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void show() {

    }
}
