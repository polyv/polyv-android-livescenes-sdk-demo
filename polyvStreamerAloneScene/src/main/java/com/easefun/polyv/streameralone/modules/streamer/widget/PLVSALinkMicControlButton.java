package com.easefun.polyv.streameralone.modules.streamer.widget;

import static com.plv.thirdpart.svga.PLVSvgaHelper.loadFromAssets;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.opensource.svgaplayer.SVGAImageView;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;

/**
 * @author Hoshiiro
 */
public class PLVSALinkMicControlButton extends SVGAImageView {

    private final Lazy<PLVSALinkMicControlWindow> linkMicControlWindow = new Lazy<PLVSALinkMicControlWindow>() {
        @Override
        public PLVSALinkMicControlWindow onLazyInit() {
            return new PLVSALinkMicControlWindow(getContext());
        }
    };
    private static final String LINK_MIC_REQUESTING_DRAWABLE_FILE_NAME = "plvsa_linkmic_guest_requesting.svga";

    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    private OnViewActionListener onViewActionListener;

    private State currentState = initState();
    private boolean isLiveStart = false;
    private boolean isVideoLinkMicType = true;
    private boolean isOpenLinkMic = false;

    public PLVSALinkMicControlButton(@NonNull Context context) {
        super(context);
    }

    public PLVSALinkMicControlButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVSALinkMicControlButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentState.onClick();
            }
        });
    }

    public PLVSALinkMicControlButton setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    public void performAutoOpenLinkMic(boolean isOpen, boolean isVideoLinkMicType, boolean isNewLinkMicStrategy) {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN)) {
            return;
        }
        if (currentState instanceof TeacherStateLinkMicClosed && isOpen) {
            boolean isOpenSuccess = changeLinkMicOpenState(isVideoLinkMicType, true);
            if (isOpenSuccess) {
                currentState = currentState.nextState(ChangeStateReason.ON_CLICK);
                if (!isNewLinkMicStrategy) {
                    PLVToast.Builder.context(getContext())
                            .setText(isVideoLinkMicType ? R.string.plv_streamer_open_video_linkmic_toast : R.string.plv_streamer_open_audio_linkmic_toast)
                            .show();
                }
            }
        } else if (currentState instanceof TeacherStateLinkMicOpened && !isOpen) {
            changeLinkMicOpenState(isVideoLinkMicType, false);
            currentState = currentState.nextState(ChangeStateReason.ON_CLICK);
        }
    }

    public final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            PLVSALinkMicControlButton.this.streamerPresenter = presenter;
        }

        @Override
        public void onStreamLiveStatusChanged(boolean isLive) {
            if (isLiveStart == isLive) {
                return;
            }
            isLiveStart = isLive;
            if (!isLiveStart) {
                currentState = currentState.nextState(ChangeStateReason.LIVE_END);
            } else {
                currentState = currentState.nextState(ChangeStateReason.LIVE_START);
            }
        }

        @Override
        public void onGuestRTCStatusChanged(int pos, boolean isJoinRTC) {
            if (isJoinRTC) {
                currentState = currentState.nextState(ChangeStateReason.JOIN_LINK_MIC);
            } else {
                currentState = currentState.nextState(ChangeStateReason.LEAVE_LINK_MIC);
            }
        }

        @Override
        public void onLinkMicOpenStateChanged(boolean isVideoLinkMic, boolean isOpen) {
            PLVSALinkMicControlButton.this.isVideoLinkMicType = isVideoLinkMic;
            PLVSALinkMicControlButton.this.isOpenLinkMic = isOpen;
            if (onViewActionListener != null) {
                onViewActionListener.onLinkMicOpenStateChanged(isVideoLinkMicType, isOpenLinkMic);
            }
        }
    };

    private boolean changeLinkMicOpenState(final boolean isVideoLinkMicType, final boolean isOpenLinkMic) {
        return streamerPresenter.openLinkMic(isVideoLinkMicType, isOpenLinkMic, null);
    }

    private void viewerSendJoinRequest() {
        streamerPresenter.guestSendJoinRequest();
    }

    private void viewerHangUpLinkMic() {
        streamerPresenter.guestSendLeaveLinkMic();
    }

    public interface OnViewActionListener {

        /**
         * 更新连麦媒体类型
         *
         * @param isVideoLinkMicType true：视频类型，false：音频类型
         * @param isOpenLinkMic      true：打开连麦，false：关闭连麦
         */
        void onLinkMicOpenStateChanged(boolean isVideoLinkMicType, boolean isOpenLinkMic);

    }

    // <editor-fold defaultstate="collapsed" desc="连麦按钮状态">

    private abstract class State {

        {
            onEnter();
        }

        protected abstract void onEnter();

        abstract void onClick();

        abstract State nextState(@NonNull ChangeStateReason cause);

    }

    private enum ChangeStateReason {
        ON_CLICK, JOIN_LINK_MIC, LEAVE_LINK_MIC, LIVE_START, LIVE_END
    }

    private State initState() {
        if (PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN)) {
            return new TeacherStateLinkMicClosed();
        } else {
            return new ViewerStateNoLive();
        }
    }

    private class TeacherStateLinkMicClosed extends State {
        @Override
        protected void onEnter() {
            setImageResource(R.drawable.plvsa_tool_bar_linkmic);
        }

        @Override
        void onClick() {
            linkMicControlWindow.get()
                    .setOnViewActionListener(new PLVSALinkMicControlWindow.OnViewActionListener() {
                        @Override
                        public void onRequestChangeLinkMicOpenState(boolean isVideoLinkMicType, boolean isOpen) {
                            final boolean success = changeLinkMicOpenState(isVideoLinkMicType, isOpen);
                            if (success) {
                                currentState = nextState(ChangeStateReason.ON_CLICK);
                            } else {
                                PLVToast.Builder.context(getContext())
                                        .setText(R.string.plv_linkmic_error_tip_have_not_opened)
                                        .build()
                                        .show();
                            }
                        }
                    })
                    .show(PLVSALinkMicControlButton.this);
        }

        @Override
        State nextState(@NonNull ChangeStateReason cause) {
            if (cause == ChangeStateReason.ON_CLICK) {
                return new TeacherStateLinkMicOpened();
            }
            return this;
        }
    }

    private class TeacherStateLinkMicOpened extends State {
        @Override
        protected void onEnter() {
            setImageResource(R.drawable.plvsa_tool_bar_linkmic_sel);
        }

        @Override
        void onClick() {
            new PLVSAConfirmDialog(getContext())
                    .setTitle(R.string.plv_linkmic_dialog_hang_off_confirm_ask_2)
                    .setContent(R.string.plv_linkmic_dialog_hang_off_confirm_hint)
                    .setLeftButtonText(R.string.plv_common_dialog_cancel)
                    .setRightButtonText(R.string.plv_common_dialog_confirm_2)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            changeLinkMicOpenState(isVideoLinkMicType, false);
                            currentState = nextState(ChangeStateReason.ON_CLICK);
                        }
                    })
                    .show();
        }

        @Override
        State nextState(@NonNull ChangeStateReason cause) {
            switch (cause) {
                case ON_CLICK:
                case LIVE_END:
                    return new TeacherStateLinkMicClosed();
                default:
                    return this;
            }
        }
    }

    private class ViewerStateNoLive extends State {
        @Override
        protected void onEnter() {
            setImageResource(R.drawable.plvsa_tool_bar_linkmic);
            setAlpha(0.6F);
        }

        @Override
        void onClick() {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_toast_can_not_linkmic_before_the_class)
                    .build()
                    .show();
        }

        @Override
        State nextState(@NonNull ChangeStateReason cause) {
            if (cause == ChangeStateReason.LIVE_START) {
                return new ViewerStateIdle();
            }
            return this;
        }
    }

    private class ViewerStateIdle extends State {
        @Override
        protected void onEnter() {
            setImageResource(R.drawable.plvsa_tool_bar_linkmic);
            setAlpha(1F);
        }

        @Override
        void onClick() {
            viewerSendJoinRequest();
            currentState = nextState(ChangeStateReason.ON_CLICK);
        }

        @Override
        State nextState(@NonNull ChangeStateReason cause) {
            switch (cause) {
                case ON_CLICK:
                    return new ViewerStateRequesting();
                case JOIN_LINK_MIC:
                    return new ViewerStateLinkMic();
                case LIVE_END:
                    return new ViewerStateNoLive();
                default:
                    return this;
            }
        }
    }

    private class ViewerStateRequesting extends State {
        @Override
        protected void onEnter() {
            loadFromAssets(PLVSALinkMicControlButton.this, LINK_MIC_REQUESTING_DRAWABLE_FILE_NAME);
        }

        @Override
        void onClick() {
            new PLVSAConfirmDialog(getContext())
                    .setTitle(R.string.plv_linkmic_cancel_ask)
                    .setContentVisibility(View.GONE)
                    .setLeftButtonText(R.string.plv_common_dialog_cancel_2)
                    .setRightButtonText(R.string.plv_common_dialog_confirm_3)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            viewerHangUpLinkMic();
                            currentState = nextState(ChangeStateReason.ON_CLICK);
                        }
                    })
                    .show();
        }

        @Override
        State nextState(@NonNull ChangeStateReason cause) {
            switch (cause) {
                case ON_CLICK:
                case LEAVE_LINK_MIC:
                    stopAnimation();
                    return new ViewerStateIdle();
                case LIVE_END:
                    stopAnimation();
                    return new ViewerStateNoLive();
                case JOIN_LINK_MIC:
                    stopAnimation();
                    return new ViewerStateLinkMic();
                default:
                    return this;
            }
        }
    }

    private class ViewerStateLinkMic extends State {
        @Override
        protected void onEnter() {
            setImageResource(R.drawable.plvsa_tool_bar_linkmic_sel);
        }

        @Override
        void onClick() {
            new PLVSAConfirmDialog(getContext())
                    .setTitle(R.string.plv_linkmic_dialog_hang_off_confirm_ask_3)
                    .setContentVisibility(View.GONE)
                    .setLeftButtonText(R.string.plv_common_dialog_cancel)
                    .setRightButtonText(R.string.plv_common_dialog_confirm_2)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            viewerHangUpLinkMic();
                            currentState = nextState(ChangeStateReason.ON_CLICK);
                        }
                    })
                    .show();
        }

        @Override
        State nextState(@NonNull ChangeStateReason cause) {
            switch (cause) {
                case ON_CLICK:
                case LEAVE_LINK_MIC:
                    return new ViewerStateIdle();
                case LIVE_END:
                    return new ViewerStateNoLive();
                default:
                    return this;
            }
        }
    }

    // </editor-fold>

}
