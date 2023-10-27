package com.easefun.polyv.livestreamer.modules.statusbar.widget;

import static com.plv.thirdpart.svga.PLVSvgaHelper.loadFromAssets;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.opensource.svgaplayer.SVGAImageView;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;

/**
 * @author Hoshiiro
 */
public class PLVLSLinkMicControlButton extends SVGAImageView {

    private final Lazy<PLVLSLinkMicTeacherControlWindow> linkMicTeacherControlWindow = new Lazy<PLVLSLinkMicTeacherControlWindow>() {
        @Override
        public PLVLSLinkMicTeacherControlWindow onLazyInit() {
            return new PLVLSLinkMicTeacherControlWindow(getContext());
        }
    };
    private final Lazy<PLVLSLinkMicGuestControlWindow> linkMicGuestControlWindow = new Lazy<PLVLSLinkMicGuestControlWindow>() {
        @Override
        public PLVLSLinkMicGuestControlWindow onLazyInit() {
            return new PLVLSLinkMicGuestControlWindow(getContext());
        }
    };
    private static final String LINK_MIC_REQUESTING_DRAWABLE_FILE_NAME = "plvls_linkmic_guest_requesting.svga";

    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    private OnViewActionListener onViewActionListener;

    private State currentState = initState();
    private boolean isLiveStart = false;
    private boolean isVideoLinkMicType = true;
    private boolean isOpenLinkMic = false;

    public PLVLSLinkMicControlButton(@NonNull Context context) {
        super(context);
    }

    public PLVLSLinkMicControlButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVLSLinkMicControlButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

    public PLVLSLinkMicControlButton setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    public final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            PLVLSLinkMicControlButton.this.streamerPresenter = presenter;
            observeLiveStreamStatus(presenter);
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
            PLVLSLinkMicControlButton.this.isVideoLinkMicType = isVideoLinkMic;
            PLVLSLinkMicControlButton.this.isOpenLinkMic = isOpen;
            if (onViewActionListener != null) {
                onViewActionListener.onLinkMicOpenStateChanged(isVideoLinkMicType, isOpenLinkMic);
            }
        }

        private void observeLiveStreamStatus(IPLVStreamerContract.IStreamerPresenter presenter) {
            presenter.getData().getStreamerStatus().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isLive) {
                    if (isLive == null || isLiveStart == isLive) {
                        return;
                    }
                    isLiveStart = isLive;
                    if (!isLiveStart) {
                        currentState = currentState.nextState(ChangeStateReason.LIVE_END);
                        linkMicTeacherControlWindow.get().resetLinkMicControlView();
                    } else {
                        currentState = currentState.nextState(ChangeStateReason.LIVE_START);
                    }
                }
            });
        }

    };

    private boolean changeLinkMicOpenState(final boolean isVideoLinkMicType, final boolean isOpenLinkMic) {
        if (onViewActionListener == null || !onViewActionListener.isStreamerStartSuccess() || streamerPresenter == null) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_toast_can_not_linkmic_before_the_class)
                    .build()
                    .show();
            return false;
        }
        boolean result = streamerPresenter.openLinkMic(isVideoLinkMicType, isOpenLinkMic, null);
        if (!result) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_linkmic_error_tip_have_not_opened)
                    .build()
                    .show();
        }
        return result;
    }

    private void viewerSendJoinRequest() {
        streamerPresenter.guestSendJoinRequest();
    }

    private void viewerHangUpLinkMic() {
        streamerPresenter.guestSendLeaveLinkMic();
    }

    public interface OnViewActionListener {

        /**
         * 是否推流开始成功
         *
         * @return true：成功，false：未成功
         */
        boolean isStreamerStartSuccess();

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
            setImageResource(R.drawable.plvls_status_bar_linkmic_def);
            linkMicTeacherControlWindow.get().resetLinkMicControlView();
        }

        @Override
        void onClick() {
            linkMicTeacherControlWindow.get()
                    .setOnViewActionListener(new PLVLSLinkMicTeacherControlWindow.OnViewActionListener() {
                        @Override
                        public boolean onRequestChangeLinkMicOpenState(boolean isVideoLinkMicType, boolean isOpen) {
                            final boolean success = changeLinkMicOpenState(isVideoLinkMicType, isOpen);
                            if (success) {
                                currentState = nextState(ChangeStateReason.ON_CLICK);
                            }
                            return success;
                        }
                    })
                    .show(PLVLSLinkMicControlButton.this);
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
            setImageResource(R.drawable.plvls_status_bar_linkmic_sel);
            linkMicTeacherControlWindow.get().dismiss();
        }

        @Override
        void onClick() {
            PLVLSConfirmDialog.Builder.context(getContext())
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
            setImageResource(R.drawable.plvls_status_bar_linkmic_def);
            setColorFilter(Color.GRAY);
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
            setImageResource(R.drawable.plvls_status_bar_linkmic_def);
            setColorFilter(Color.TRANSPARENT);
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
            setImageResource(R.drawable.plvls_status_bar_linkmic_sel);
            loadFromAssets(PLVLSLinkMicControlButton.this, LINK_MIC_REQUESTING_DRAWABLE_FILE_NAME);
        }

        @Override
        void onClick() {
            linkMicGuestControlWindow.get()
                    .setOnViewActionListener(new PLVLSLinkMicGuestControlWindow.OnViewActionListener() {
                        @Override
                        public void onClick() {
                            viewerHangUpLinkMic();
                            currentState = nextState(ChangeStateReason.ON_CLICK);
                        }
                    })
                    .show(PLVLSLinkMicGuestControlWindow.STATE_REQUESTING, PLVLSLinkMicControlButton.this);
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
            setImageResource(R.drawable.plvls_status_bar_linkmic_guest_connected);
        }

        @Override
        void onClick() {
            linkMicGuestControlWindow.get()
                    .setOnViewActionListener(new PLVLSLinkMicGuestControlWindow.OnViewActionListener() {
                        @Override
                        public void onClick() {
                            viewerHangUpLinkMic();
                            currentState = nextState(ChangeStateReason.ON_CLICK);
                        }
                    })
                    .show(PLVLSLinkMicGuestControlWindow.STATE_CONNECTED, PLVLSLinkMicControlButton.this);
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
