package com.easefun.polyv.livecommon.module.modules.streamer.presenter.usecase;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.streamer.model.enums.PLVSipLinkMicState;
import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingInListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingOutListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicConnectedListState;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicMergeViewerUseCase {

    public boolean reduceCallingInState(PLVSipLinkMicCallingInListState callingInListState, PLVSipLinkMicViewerVO viewerVO) {
        if (callingInListState == null || viewerVO == null) {
            return false;
        }
        return reduceState(
                callingInListState.callingInViewerList,
                listOf(PLVSipLinkMicState.ON_CALLING_IN),
                viewerVO
        );
    }

    public boolean reduceCallingOutState(PLVSipLinkMicCallingOutListState callingOutListState, PLVSipLinkMicViewerVO viewerVO) {
        if (callingOutListState == null || viewerVO == null) {
            return false;
        }
        return reduceState(
                callingOutListState.callingOutViewerList,
                listOf(PLVSipLinkMicState.ON_CALLING_OUT, PLVSipLinkMicState.CALL_OUT_NOT_RESPONSE, PLVSipLinkMicState.CALL_OUT_REFUSED),
                viewerVO
        );
    }

    public boolean reduceConnectedState(PLVSipLinkMicConnectedListState connectedListState, PLVSipLinkMicViewerVO viewerVO) {
        if (connectedListState == null || viewerVO == null) {
            return false;
        }
        return reduceState(
                connectedListState.connectedViewerList,
                listOf(PLVSipLinkMicState.CONNECTED),
                viewerVO
        );
    }

    private boolean reduceState(
            final List<PLVSipLinkMicViewerVO> viewerList,
            final List<PLVSipLinkMicState> specStateForList,
            final PLVSipLinkMicViewerVO viewerVO
    ) {
        if (viewerList == null || specStateForList == null || viewerVO == null) {
            return false;
        }
        final PLVSipLinkMicViewerVO viewerInList = findViewerFromList(viewerList, viewerVO);
        mergeViewerData(viewerInList, viewerVO);
        if (viewerInList != null) {
            final int index = viewerList.indexOf(viewerInList);
            if (viewerMatchesState(viewerVO, specStateForList)) {
                viewerList.set(index, viewerVO);
                return true;
            } else {
                viewerList.remove(viewerInList);
                return true;
            }
        } else {
            if (viewerMatchesState(viewerVO, specStateForList)) {
                viewerList.add(viewerVO);
                return true;
            }
        }

        return false;
    }

    @Nullable
    private static PLVSipLinkMicViewerVO findViewerFromList(List<PLVSipLinkMicViewerVO> list, PLVSipLinkMicViewerVO target) {
        for (PLVSipLinkMicViewerVO vo : list) {
            if (vo.getPhone().equals(target.getPhone())) {
                return vo;
            }
        }
        return null;
    }

    private static void mergeViewerData(PLVSipLinkMicViewerVO origin, PLVSipLinkMicViewerVO newVo) {
        if (origin == null || newVo == null) {
            return;
        }
        if (newVo.getContactName() == null) {
            newVo.setContactName(origin.getContactName());
        }
        if (newVo.getAudioMuted() == null) {
            newVo.setAudioMuted(origin.getAudioMuted());
        }
        if (newVo.getSipLinkMicStatus() == null) {
            newVo.setSipLinkMicStatus(origin.getSipLinkMicStatus());
        }
    }

    private static boolean viewerMatchesState(PLVSipLinkMicViewerVO viewer, List<PLVSipLinkMicState> statusList) {
        return statusList.contains(viewer.getSipLinkMicStatus());
    }

}
