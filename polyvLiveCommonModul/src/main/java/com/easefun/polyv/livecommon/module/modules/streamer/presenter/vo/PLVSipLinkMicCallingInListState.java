package com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicCallingInListState {

    @NonNull
    public List<PLVSipLinkMicViewerVO> callingInViewerList = Collections.emptyList();

    public PLVSipLinkMicCallingInListState copy() {
        PLVSipLinkMicCallingInListState state = new PLVSipLinkMicCallingInListState();
        state.callingInViewerList = new ArrayList<>(callingInViewerList);
        return state;
    }

}
