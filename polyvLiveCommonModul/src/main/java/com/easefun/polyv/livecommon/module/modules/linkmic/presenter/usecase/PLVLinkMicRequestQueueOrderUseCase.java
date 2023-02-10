package com.easefun.polyv.livecommon.module.modules.linkmic.presenter.usecase;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.linkmic.model.PLVJoinLeaveEvent;
import com.plv.linkmic.model.PLVJoinRequestSEvent;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;

/**
 * @author Hoshiiro
 */
public class PLVLinkMicRequestQueueOrderUseCase {

    private static final String TAG = PLVLinkMicRequestQueueOrderUseCase.class.getSimpleName();

    private final String myLinkMicId;
    private final MutableLiveData<Integer> linkMicRequestQueueOrder;

    public PLVLinkMicRequestQueueOrderUseCase(
            @NonNull String myLinkMicId,
            @NonNull MutableLiveData<Integer> linkMicRequestQueueOrder
    ) {
        this.myLinkMicId = myLinkMicId;
        this.linkMicRequestQueueOrder = linkMicRequestQueueOrder;
    }

    public void onUserJoinRequest(PLVJoinRequestSEvent joinRequestEvent) {
        logEvent(joinRequestEvent);
        if (joinRequestEvent == null || joinRequestEvent.getUser() == null || joinRequestEvent.getRequestIndex() == null) {
            return;
        }
        if (isMyLinkMicId(joinRequestEvent.getUser().getUserId())) {
            linkMicRequestQueueOrder.postValue(joinRequestEvent.getRequestIndex());
        } else {
            final int myLinkMicOrder = requireNotNull(linkMicRequestQueueOrder.getValue());
            final int eventLinkMicOrder = joinRequestEvent.getRequestIndex();
            if (eventLinkMicOrder >= 0 && myLinkMicOrder > eventLinkMicOrder) {
                // 不应该出现这个情况，插队？
                linkMicRequestQueueOrder.postValue(myLinkMicOrder + 1);
            }
        }
    }

    public void onUserJoinLeave(PLVJoinLeaveEvent joinLeaveEvent) {
        logEvent(joinLeaveEvent);
        if (joinLeaveEvent == null || joinLeaveEvent.getUser() == null || joinLeaveEvent.getRequestIndex() == null) {
            return;
        }
        if (isMyLinkMicId(joinLeaveEvent.getUser().getUserId())) {
            // 离开连麦
            linkMicRequestQueueOrder.postValue(-1);
            return;
        }
        if (joinLeaveEvent.getIsLeaveMic() == null || joinLeaveEvent.getIsLeaveMic() == 1) {
            // 离开连麦为1表明原来已经在连麦状态，不参与连麦排队，不影响其他人的排队顺序
            return;
        }
        final int myLinkMicOrder = requireNotNull(linkMicRequestQueueOrder.getValue());
        final int eventLinkMicOrder = joinLeaveEvent.getRequestIndex();
        if (eventLinkMicOrder >= 0 && myLinkMicOrder > eventLinkMicOrder) {
            linkMicRequestQueueOrder.postValue(myLinkMicOrder - 1);
        }
    }

    public void onUserJoinSuccess(PLVLinkMicJoinSuccess joinSuccessEvent) {
        logEvent(joinSuccessEvent);
        if (joinSuccessEvent == null || joinSuccessEvent.getUser() == null || joinSuccessEvent.getRequestIndex() == null) {
            return;
        }
        if (isMyLinkMicId(joinSuccessEvent.getUser().getUserId())) {
            // 已加入连麦
            linkMicRequestQueueOrder.postValue(-1);
            return;
        }
        final int myLinkMicOrder = requireNotNull(linkMicRequestQueueOrder.getValue());
        final int eventLinkMicOrder = joinSuccessEvent.getRequestIndex();
        if (eventLinkMicOrder >= 0 && myLinkMicOrder > eventLinkMicOrder) {
            linkMicRequestQueueOrder.postValue(myLinkMicOrder - 1);
        }
    }

    private boolean isMyLinkMicId(String linkMicId) {
        return myLinkMicId.equals(linkMicId);
    }

    private static void logEvent(Object any) {
        PLVCommonLog.i(TAG, format("on receive event: {}", any));
    }

}
