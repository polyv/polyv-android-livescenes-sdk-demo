package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.support.annotation.NonNull;

/**
 * @author Hoshiiro
 */
public abstract class PLVViewerLinkMicState {

    public static PLVViewerLinkMicState initState() {
        return new NoLinkMicState();
    }

    /**
     * 未连麦
     */
    public static final class NoLinkMicState extends PLVViewerLinkMicState {

        private NoLinkMicState() {
        }

        private NoLinkMicState(PLVViewerLinkMicState src) {
            this.onStateActionListener = src.onStateActionListener;
        }

        @Override
        public void onRequestJoinLinkMic() {
            final PLVViewerLinkMicState current = this;
            requireNotNull(onStateActionListener).checkLinkMicLimited(new OnCheckLinkMicLimitedCallback() {
                @Override
                public void onSuccess(PLVViewerLinkMicState currentState) {
                    if (currentState != current) {
                        currentState.onRequestJoinLinkMic();
                        return;
                    }

                    onStateActionListener.sendJoinRequest(current);
                    notifyStateChanged(current, new RequestingJoinLinkMicState(current));
                }

                @Override
                public void onLimited(PLVViewerLinkMicState currentState) {

                }
            });
        }

        @Override
        public void onTeacherInviteToJoin() {
            notifyStateChanged(this, new InvitingLinkMicState(this));
        }
    }

    /**
     * 正在请求连麦
     */
    public static final class RequestingJoinLinkMicState extends PLVViewerLinkMicState {

        private RequestingJoinLinkMicState(PLVViewerLinkMicState src) {
            this.onStateActionListener = src.onStateActionListener;
        }

        @Override
        public void onCancelLinkMic() {
            requireNotNull(onStateActionListener).sendJoinLeave(this);
            notifyStateChanged(this, new NoLinkMicState(this));
        }

        @Override
        public void onTeacherAllowToJoin() {
            notifyStateChanged(this, new JoinedLinkMicState(this));
        }

        @Override
        public void onTeacherInviteToJoin() {
            final PLVViewerLinkMicState current = this;
            requireNotNull(onStateActionListener).checkLinkMicLimited(new OnCheckLinkMicLimitedCallback() {
                @Override
                public void onSuccess(PLVViewerLinkMicState currentState) {
                    if (currentState != current) {
                        currentState.onTeacherInviteToJoin();
                        return;
                    }

                    onStateActionListener.sendAcceptJoinInvite(current);
                    notifyStateChanged(current, new WaitingJoinResponseState(current));
                }

                @Override
                public void onLimited(PLVViewerLinkMicState currentState) {
                    if (currentState != current) {
                        currentState.onTeacherInviteToJoin();
                        return;
                    }

                    notifyStateChanged(current, new NoLinkMicState(current));
                }
            });
        }

        @Override
        public void onTeacherNotAllowToJoin() {
            notifyStateChanged(this, new NoLinkMicState(this));
        }
    }

    /**
     * 正在被邀请连麦
     */
    public static final class InvitingLinkMicState extends PLVViewerLinkMicState {

        private InvitingLinkMicState(PLVViewerLinkMicState src) {
            this.onStateActionListener = src.onStateActionListener;
        }

        @Override
        public void onRequestJoinLinkMic() {
            onAcceptInviteLinkMic();
        }

        @Override
        public void onAcceptInviteLinkMic() {
            final PLVViewerLinkMicState current = this;
            requireNotNull(onStateActionListener).checkLinkMicLimited(new OnCheckLinkMicLimitedCallback() {
                @Override
                public void onSuccess(PLVViewerLinkMicState currentState) {
                    if (currentState != current) {
                        currentState.onAcceptInviteLinkMic();
                        return;
                    }

                    onStateActionListener.sendAcceptJoinInvite(current);
                    notifyStateChanged(current, new WaitingJoinResponseState(current));
                }

                @Override
                public void onLimited(PLVViewerLinkMicState currentState) {
                    if (currentState != current) {
                        currentState.onAcceptInviteLinkMic();
                        return;
                    }

                    notifyStateChanged(current, new NoLinkMicState(current));
                }
            });
        }

        @Override
        public void onCancelLinkMic() {
            notifyStateChanged(this, new NoLinkMicState(this));
        }

        @Override
        public void onTeacherAllowToJoin() {
            notifyStateChanged(this, new JoinedLinkMicState(this));
        }

        @Override
        public void onTeacherNotAllowToJoin() {
            notifyStateChanged(this, new NoLinkMicState(this));
        }
    }

    /**
     * 已同意讲师的邀请连麦，等待服务器返回上麦成功回调
     */
    public static final class WaitingJoinResponseState extends PLVViewerLinkMicState {

        private WaitingJoinResponseState(PLVViewerLinkMicState src) {
            this.onStateActionListener = src.onStateActionListener;
        }

        @Override
        public void onRequestJoinLinkMic() {
            final PLVViewerLinkMicState current = this;
            requireNotNull(onStateActionListener).checkLinkMicLimited(new OnCheckLinkMicLimitedCallback() {
                @Override
                public void onSuccess(PLVViewerLinkMicState currentState) {
                    if (currentState != current) {
                        currentState.onRequestJoinLinkMic();
                        return;
                    }

                    onStateActionListener.sendJoinRequest(current);
                    notifyStateChanged(current, new RequestingJoinLinkMicState(current));
                }

                @Override
                public void onLimited(PLVViewerLinkMicState currentState) {

                }
            });
        }

        @Override
        public void onTeacherInviteToJoin() {
            notifyStateChanged(this, new InvitingLinkMicState(this));
        }

        @Override
        public void onTeacherAllowToJoin() {
            notifyStateChanged(this, new JoinedLinkMicState(this));
        }
    }

    /**
     * 正在连麦
     */
    public static final class JoinedLinkMicState extends PLVViewerLinkMicState {

        private JoinedLinkMicState(PLVViewerLinkMicState src) {
            this.onStateActionListener = src.onStateActionListener;
        }

        @Override
        public void onCancelLinkMic() {
            requireNotNull(onStateActionListener).sendJoinLeave(this);
            notifyStateChanged(this, new NoLinkMicState(this));
        }

        @Override
        public void onTeacherNotAllowToJoin() {
            notifyStateChanged(this, new NoLinkMicState(this));
        }
    }

    protected OnStateActionListener onStateActionListener = null;

    public PLVViewerLinkMicState setOnStateActionListener(OnStateActionListener onStateActionListener) {
        this.onStateActionListener = onStateActionListener;
        return this;
    }

    /**
     * 观众请求上麦
     */
    public void onRequestJoinLinkMic() {
    }

    /**
     * 观众同意邀请上麦
     */
    public void onAcceptInviteLinkMic() {
    }

    /**
     * 观众取消连麦
     */
    public void onCancelLinkMic() {
    }

    /**
     * 讲师同意上麦 joinResponse, needAnswer = false
     */
    public void onTeacherAllowToJoin() {
    }

    /**
     * 讲师邀请上麦 joinResponse, needAnswer = true
     */
    public void onTeacherInviteToJoin() {
    }

    /**
     * 讲师不同意上麦 joinResponse, value = 0
     */
    public void onTeacherNotAllowToJoin() {
    }

    protected PLVViewerLinkMicState notifyStateChanged(PLVViewerLinkMicState fromState, PLVViewerLinkMicState newState) {
        requireNotNull(onStateActionListener).onStateChanged(fromState, newState);
        return this;
    }

    interface OnStateActionListener {

        void sendJoinRequest(PLVViewerLinkMicState state);

        void sendJoinLeave(PLVViewerLinkMicState state);

        void sendAcceptJoinInvite(PLVViewerLinkMicState state);

        void onStateChanged(PLVViewerLinkMicState oldState, PLVViewerLinkMicState newState);

        void checkLinkMicLimited(@NonNull OnCheckLinkMicLimitedCallback onCheckLinkMicLimitedCallback);

    }

    interface OnCheckLinkMicLimitedCallback {

        void onSuccess(PLVViewerLinkMicState currentState);

        void onLimited(PLVViewerLinkMicState currentState);

    }

}
