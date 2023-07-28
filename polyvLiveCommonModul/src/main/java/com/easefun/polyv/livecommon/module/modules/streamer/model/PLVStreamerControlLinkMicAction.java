package com.easefun.polyv.livecommon.module.modules.streamer.model;

/**
 * @author Hoshiiro
 */
public class PLVStreamerControlLinkMicAction {

    /**
     * 接受举手连麦
     */
    public static PLVStreamerControlLinkMicAction acceptRequest() {
        return new AcceptRequestAction();
    }

    /**
     * 发出连麦邀请
     */
    public static PLVStreamerControlLinkMicAction sendInvitation() {
        return new SendInvitationAction();
    }

    public static PLVStreamerControlLinkMicAction sendInvitation(boolean needAnswer) {
        SendInvitationAction action = new SendInvitationAction();
        action.needAnswer = needAnswer;
        return action;
    }

    /**
     * 挂断 下麦
     */
    public static PLVStreamerControlLinkMicAction hangUp() {
        return new HangUpAction();
    }

    public static final class AcceptRequestAction extends PLVStreamerControlLinkMicAction {}

    public static final class SendInvitationAction extends PLVStreamerControlLinkMicAction {
        public boolean needAnswer = true;
    }

    public static final class HangUpAction extends PLVStreamerControlLinkMicAction {}

}
