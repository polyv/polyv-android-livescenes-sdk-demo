package com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicCallingOutListState {

    @NonNull
    public List<PLVSipLinkMicViewerVO> callingOutViewerList = Collections.emptyList();

    public PLVSipLinkMicCallingOutListState copy() {
        PLVSipLinkMicCallingOutListState state = new PLVSipLinkMicCallingOutListState();
        state.callingOutViewerList = new ArrayList<>(callingOutViewerList);
        return state;
    }

}
