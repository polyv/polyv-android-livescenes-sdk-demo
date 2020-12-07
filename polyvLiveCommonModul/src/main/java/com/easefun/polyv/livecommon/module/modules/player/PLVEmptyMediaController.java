package com.easefun.polyv.livecommon.module.modules.player;

import android.view.View;
import android.widget.MediaController;

import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;

/**
 * date: 2020-04-29
 * author: hwj
 * description:
 */
public class PLVEmptyMediaController implements IPolyvMediaController<PolyvLiveVideoView> {

    @Override
    public void onPrepared(PolyvLiveVideoView mp) {

    }

    @Override
    public void onLongBuffering(String tip) {

    }

    @Override
    public void hide() {

    }

    @Override
    public boolean isShowing() {
        return false;
    }

    @Override
    public void setAnchorView(View view) {

    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void show() {

    }
}
