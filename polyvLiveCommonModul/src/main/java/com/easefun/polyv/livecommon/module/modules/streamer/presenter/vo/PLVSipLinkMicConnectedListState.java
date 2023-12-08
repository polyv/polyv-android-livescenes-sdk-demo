package com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicConnectedListState {

    @NonNull
    public List<PLVSipLinkMicViewerVO> connectedViewerList = Collections.emptyList();

    public PLVSipLinkMicConnectedListState copy() {
        PLVSipLinkMicConnectedListState state = new PLVSipLinkMicConnectedListState();
        state.connectedViewerList = new ArrayList<>(connectedViewerList);
        return state;
    }

}
