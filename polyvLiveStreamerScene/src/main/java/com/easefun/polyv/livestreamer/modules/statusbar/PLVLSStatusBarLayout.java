package com.easefun.polyv.livestreamer.modules.statusbar;

import static com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter.AUTO_ID_WHITE_BOARD;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.utils.PLVLiveLocalActionHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVLSNetworkQualityWidget;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.document.popuplist.PLVLSPptListLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.IPLVLSCountDownView;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSChannelInfoLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSClassBeginCountDownWindow;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSLinkMicControlWindow;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSLinkMicRequestTipsWindow;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSMemberLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSMoreSettingLayout;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.socket.user.PLVSocketUserConstant;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

/**
 * 状态栏布局
 */
public class PLVLSStatusBarLayout extends FrameLayout implements IPLVLSStatusBarLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int WHAT_HIDE_USER_REQUEST_TIPS = 1;

    //view
    private TextView plvlsStatusBarChannelInfoTv;
    private TextView plvlsStatusBarStreamerTimeTv;
    private PLVLSNetworkQualityWidget plvlsStatusBarNetQualityView;
    private TextView plvlsStatusBarClassControlTv;
    private ImageView plvlsStatusBarShareIv;
    private ImageView plvlsStatusBarSettingIv;
    private ImageView plvlsStatusBarMemberIv;
    private View plvlsStatusBarMemberLinkmicRequestTipsIv;
    private ImageView plvlsStatusBarLinkmicIv;
    private ImageView plvlsStatusBarDocumentIv;
    private ImageView plvlsStatusBarWhiteboardIv;

    //频道信息布局
    private PLVLSChannelInfoLayout channelInfoLayout;
    //设置布局
    private PLVLSMoreSettingLayout moreSettingLayout;
    //成员列表布局
    private PLVLSMemberLayout memberLayout;
    //连麦开关布局
    private PLVLSLinkMicControlWindow linkMicControlWindow;
    //连麦请求提示布局
    private PLVLSLinkMicRequestTipsWindow linkMicRequestTipsWindow;
    //推流开始倒计时布局
    private IPLVLSCountDownView countDownView;

    // PPT文档布局
    private PLVLSPptListLayout pptListLayout;

    //view交互事件监听器
    private OnViewActionListener onViewActionListener;

    /**
     * PPT文档 MVP-View
     * 请勿改为局部变量，否则会被gc回收，引起无法响应Presenter调用
     */
    private PLVAbsDocumentView documentMvpView;

    private PLVUserAbilityManager.OnUserAbilityChangedListener onUserAbilityChangeCallback;

    private int lastAutoId;
    // 最后一次打开的非白板文档ID和对应的页面ID
    private int lastOpenNotWhiteBoardAutoId;
    private int lastOpenNotWhiteBoardPageId;
    //角色
    private String userType;

    /**
     * 连麦显示类型，0-默认都显示，1-只显示音频连麦；2-只显示视频连麦
     */
    private int linkMicShowType = 0;

    //handler
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (WHAT_HIDE_USER_REQUEST_TIPS == msg.what) {
                hideUserRequestTips();
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSStatusBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSStatusBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSStatusBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_status_bar_layout, this);

        plvlsStatusBarChannelInfoTv = findViewById(R.id.plvls_status_bar_channel_info_tv);
        plvlsStatusBarStreamerTimeTv = findViewById(R.id.plvls_status_bar_streamer_time_tv);
        plvlsStatusBarNetQualityView = findViewById(R.id.plvls_status_bar_net_quality_view);
        plvlsStatusBarClassControlTv = findViewById(R.id.plvls_status_bar_class_control_tv);
        plvlsStatusBarShareIv = findViewById(R.id.plvls_status_bar_share_iv);
        plvlsStatusBarSettingIv = findViewById(R.id.plvls_status_bar_setting_iv);
        plvlsStatusBarMemberIv = findViewById(R.id.plvls_status_bar_member_iv);
        plvlsStatusBarMemberLinkmicRequestTipsIv = (View) findViewById(R.id.plvls_status_bar_member_linkmic_request_tips_iv);
        plvlsStatusBarLinkmicIv = findViewById(R.id.plvls_status_bar_linkmic_iv);
        plvlsStatusBarDocumentIv = findViewById(R.id.plvls_status_bar_document_iv);
        plvlsStatusBarWhiteboardIv = findViewById(R.id.plvls_status_bar_whiteboard_iv);

        channelInfoLayout = new PLVLSChannelInfoLayout(getContext());
        moreSettingLayout = new PLVLSMoreSettingLayout(getContext());
        memberLayout = new PLVLSMemberLayout(getContext());
        linkMicControlWindow = new PLVLSLinkMicControlWindow(this);
        linkMicRequestTipsWindow = new PLVLSLinkMicRequestTipsWindow(this);
        countDownView = new PLVLSClassBeginCountDownWindow(this);
        pptListLayout = new PLVLSPptListLayout(getContext());

        plvlsStatusBarChannelInfoTv.setOnClickListener(this);
        plvlsStatusBarClassControlTv.setOnClickListener(this);
        plvlsStatusBarShareIv.setOnClickListener(this);
        plvlsStatusBarSettingIv.setOnClickListener(this);
        plvlsStatusBarMemberIv.setOnClickListener(this);
        plvlsStatusBarLinkmicIv.setOnClickListener(this);
        plvlsStatusBarDocumentIv.setOnClickListener(this);
        plvlsStatusBarWhiteboardIv.setOnClickListener(this);

        initCountDownView();
        initLinkMicControlView();
        initSettingLayout();
        initMemberLayout();
        initDocumentMvpView();
        initOnUserAbilityChangeListener();

        checkUserDocumentPermission();
    }

    private void initCountDownView() {
        //初始化倒计时监听器
        countDownView.setOnCountDownListener(new IPLVLSCountDownView.OnCountDownListener() {
            @Override
            public void onCountDownFinished() {
                if (onViewActionListener != null) {
                    onViewActionListener.onClassControl(true);
                }
            }

            @Override
            public void onCountDownCanceled() {
                changeStatesToClassOver();
            }
        });
    }

    private void initLinkMicControlView() {
        //初始化连麦开关页监听器
        linkMicControlWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                plvlsStatusBarLinkmicIv.setSelected(false);
            }
        });
        linkMicControlWindow.setOnViewActionListener(new PLVLSLinkMicControlWindow.OnViewActionListener() {
            @Override
            public boolean isStreamerStartSuccess() {
                return onViewActionListener != null && onViewActionListener.isStreamerStartSuccess();
            }

            @Override
            public void updateLinkMicMediaType(boolean isVideoLinkMicType) {
                memberLayout.updateLinkMicMediaType(isVideoLinkMicType);
            }
        });
    }

    private void initSettingLayout() {
        //初始化设置页的监听器
        moreSettingLayout.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    plvlsStatusBarSettingIv.setSelected(false);
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
            }
        });
        moreSettingLayout.setOnViewActionListener(new PLVLSMoreSettingLayout.OnViewActionListener() {
            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return onViewActionListener == null ? null : onViewActionListener.getBitrateInfo();
            }

            @Override
            public void onBitrateClick(int bitrate) {
                onViewActionListener.onBitrateClick(bitrate);
            }

            @Override
            public boolean isCurrentLocalVideoEnable() {
                return onViewActionListener.isCurrentLocalVideoEnable();
            }
        });
    }

    private void initMemberLayout() {
        //初始化成员变量页的监听器
        memberLayout.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    plvlsStatusBarMemberIv.setSelected(false);
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
            }
        });
        memberLayout.setOnViewActionListener(new PLVLSMemberLayout.OnViewActionListener() {
            @Override
            public void onMicControl(int position, boolean isMute) {
                if (onViewActionListener != null) {
                    onViewActionListener.onMicControl(position, isMute);
                }
            }

            @Override
            public void onCameraControl(int position, boolean isMute) {
                if (onViewActionListener != null) {
                    onViewActionListener.onCameraControl(position, isMute);
                }
            }

            @Override
            public void onFrontCameraControl(int position, boolean isFront) {
                if (onViewActionListener != null) {
                    onViewActionListener.onFrontCameraControl(position, isFront);
                }
            }

            @Override
            public void onControlUserLinkMic(int position, boolean isAllowJoin) {
                if (onViewActionListener != null) {
                    onViewActionListener.onControlUserLinkMic(position, isAllowJoin);
                }
            }

            @Override
            public void onGrantSpeakerPermission(int position, String userId, boolean isGrant) {
                if (onViewActionListener != null) {
                    onViewActionListener.onGrantSpeakerPermission(position, userId, isGrant);
                }
            }

            @Override
            public void closeAllUserLinkMic() {
                if (onViewActionListener != null) {
                    onViewActionListener.closeAllUserLinkMic();
                }
            }

            @Override
            public void muteAllUserAudio(boolean isMute) {
                if (onViewActionListener != null) {
                    onViewActionListener.muteAllUserAudio(isMute);
                }
            }
        });
    }

    /**
     * 初始化文档部分 MVP - View 实现
     */
    private void initDocumentMvpView() {
        documentMvpView = new PLVAbsDocumentView() {

            @Override
            public void onSwitchShowMode(PLVDocumentMode showMode) {
                plvlsStatusBarWhiteboardIv.setSelected(showMode == PLVDocumentMode.WHITEBOARD);
                plvlsStatusBarDocumentIv.setSelected(showMode != PLVDocumentMode.WHITEBOARD);
                if (showMode == PLVDocumentMode.WHITEBOARD) {
                    lastAutoId = AUTO_ID_WHITE_BOARD;
                }
            }

            @Override
            public void onPptPageChange(int autoId, int pageId) {
                lastAutoId = autoId;
                if (autoId != AUTO_ID_WHITE_BOARD) {
                    lastOpenNotWhiteBoardAutoId = autoId;
                    lastOpenNotWhiteBoardPageId = pageId;
                }

                plvlsStatusBarWhiteboardIv.setSelected(autoId == AUTO_ID_WHITE_BOARD);
                plvlsStatusBarDocumentIv.setSelected(autoId != AUTO_ID_WHITE_BOARD);
            }
        };

        PLVDocumentPresenter.getInstance().registerView(documentMvpView);
    }

    /**
     * 初始化用户角色能力变化监听
     */
    private void initOnUserAbilityChangeListener() {
        this.onUserAbilityChangeCallback = new PLVUserAbilityManager.OnUserAbilityChangedListener() {
            @Override
            public void onUserAbilitiesChanged(@NonNull List<PLVUserAbility> addedAbilities, @NonNull List<PLVUserAbility> removedAbilities) {
                checkUserDocumentPermission();
            }
        };

        PLVUserAbilityManager.myAbility().addUserAbilityChangeListener(new WeakReference<>(onUserAbilityChangeCallback));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现父类IPLVLSStatusBarLayout的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        channelInfoLayout.init(liveRoomDataManager);
        memberLayout.init(liveRoomDataManager);
        moreSettingLayout.init(liveRoomDataManager);
        userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
            plvlsStatusBarClassControlTv.setVisibility(GONE);
            plvlsStatusBarLinkmicIv.setVisibility(GONE);
        }
        updateLinkMicShowType(liveRoomDataManager.isOnlyAudio());
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        onViewActionListener = listener;
    }

    @Override
    public IPLVStreamerContract.IStreamerView getMemberLayoutStreamerView() {
        return memberLayout.getStreamerView();
    }

    @Override
    public void showAlertDialogNoNetwork() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.plv_streamer_dialog_no_network)
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void updateUserRequestStatus(String uid) {
        showUserRequestTips(uid);
    }

    @Override
    public void updateStreamerTime(int secondsSinceStartTiming) {
        int seconds = secondsSinceStartTiming % 60;
        int minutes = (secondsSinceStartTiming % (60 * 60)) / 60;
        int hours = (secondsSinceStartTiming % (60 * 60 * 24)) / (60 * 60);

        String secondString = String.format(Locale.getDefault(), "%02d", seconds);
        String minuteString = String.format(Locale.getDefault(), "%02d", minutes);
        String hourString = String.format(Locale.getDefault(), "%02d", hours);

        final String timingText = hourString + ":" + minuteString + ":" + secondString;

        plvlsStatusBarStreamerTimeTv.setText(timingText);
    }

    @Override
    public void setStreamerStatus(boolean isStartedStatus) {
        memberLayout.setStreamerStatus(isStartedStatus);
        if (isStartedStatus) {
            changeStatesToClassStarted();
        } else {
            changeStatesToClassOver();
        }
    }

    @Override
    public void switchPptType(int pptType){
        if(pptType == PLVDocumentMode.WHITEBOARD.ordinal()){
            PLVDocumentPresenter.getInstance().switchShowMode(PLVDocumentMode.WHITEBOARD);
        } else if(pptType == PLVDocumentMode.PPT.ordinal()){
            PLVDocumentPresenter.getInstance().switchShowMode(PLVDocumentMode.PPT);
        }
    }

    @Override
    public void updateNetworkQuality(int networkQuality) {
        plvlsStatusBarNetQualityView.setNetQuality(networkQuality);
    }

    @Override
    public void setOnlineCount(int onlineCount) {
        memberLayout.setOnlineCount(onlineCount);
    }

    @Override
    public boolean onBackPressed() {
        return channelInfoLayout.onBackPressed()
                || moreSettingLayout.onBackPressed()
                || memberLayout.onBackPressed()
                || pptListLayout.onBackPressed();
    }

    @Override
    public void destroy() {
        onUserAbilityChangeCallback = null;
        channelInfoLayout.destroy();
        moreSettingLayout.destroy();
        memberLayout.destroy();
        pptListLayout.destroy();
        //停止倒计时
        countDownView.stopCountDown();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="上下课开关控制">
    private void changeStatesToClassStarted() {
        plvlsStatusBarClassControlTv.setText("下课");
        plvlsStatusBarClassControlTv.setEnabled(true);
        plvlsStatusBarClassControlTv.setSelected(true);

        plvlsStatusBarStreamerTimeTv.setVisibility(View.VISIBLE);
        plvlsStatusBarStreamerTimeTv.setText("00:00:00");
    }

    private void changeStatesToClassOver() {
        plvlsStatusBarClassControlTv.setText("上课");
        plvlsStatusBarClassControlTv.setSelected(false);
        plvlsStatusBarClassControlTv.setEnabled(true);

        plvlsStatusBarStreamerTimeTv.setVisibility(View.GONE);

        //下课后，由于会清除连麦开关状态，因此需重置相关view的状态
        linkMicControlWindow.resetLinkMicControlView();
    }

    private void toggleClassStates(boolean isWillStart) {
        if (isWillStart) {
            if (onViewActionListener != null) {
                int currentNetworkQuality = onViewActionListener.getCurrentNetworkQuality();
                if (currentNetworkQuality == PLVSStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                    //如果断网，则不上课，显示弹窗。
                    showAlertDialogNoNetwork();
                    return;
                }
            }
            countDownView.startCountDown();
            plvlsStatusBarClassControlTv.setEnabled(false);
        } else {
            PLVLSConfirmDialog.Builder.context(getContext())
                    .setTitleVisibility(View.GONE)
                    .setContent(R.string.plv_streamer_dialog_stop_class_ask)
                    .setRightButtonText(R.string.plv_common_dialog_confirm)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            if (onViewActionListener != null) {
                                onViewActionListener.onClassControl(false);
                            }
                        }
                    })
                    .show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="音频开播控制">
    private void updateLinkMicShowType(boolean isOnlyAudio) {
        linkMicShowType = isOnlyAudio ? 1 : 0;
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="文档选择处理">

    private void checkUserDocumentPermission() {
        final boolean canUseDocument = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_SWITCH_PPT_WHITEBOARD);
        final int color = canUseDocument ? Color.TRANSPARENT : Color.GRAY;
        if (plvlsStatusBarDocumentIv != null) {
            plvlsStatusBarDocumentIv.setColorFilter(color);
        }
        if (plvlsStatusBarWhiteboardIv != null) {
            plvlsStatusBarWhiteboardIv.setColorFilter(color);
        }
    }

    private void processSelectDocument() {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_SWITCH_PPT_WHITEBOARD)
                || PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plvls_document_usage_not_permeitted)
                    .show();
            return;
        }

        PLVLiveLocalActionHelper.getInstance().updatePptType(PLVDocumentMode.PPT.ordinal());
        PLVDocumentPresenter.getInstance().switchShowMode(PLVDocumentMode.PPT);
        if (lastAutoId == AUTO_ID_WHITE_BOARD && lastOpenNotWhiteBoardAutoId != AUTO_ID_WHITE_BOARD) {
            // 如果当前是白板模式，上次已经打开过PPT文档，直接切到上次的PPT文档
            PLVDocumentPresenter.getInstance().changePptPage(lastOpenNotWhiteBoardAutoId, lastOpenNotWhiteBoardPageId);
        } else {
            if (pptListLayout != null) {
                pptListLayout.open(true);
            }
        }
    }

    private void processSelectWhiteBoard() {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_SWITCH_PPT_WHITEBOARD)) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plvls_document_usage_not_permeitted)
                    .show();
            return;
        }

        PLVLiveLocalActionHelper.getInstance().updatePptType(PLVDocumentMode.WHITEBOARD.ordinal());
        PLVDocumentPresenter.getInstance().switchShowMode(PLVDocumentMode.WHITEBOARD);
        PLVDocumentPresenter.getInstance().changeToWhiteBoard();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户请求连麦的提示处理">
    private void showUserRequestTips(String uid) {
        if (memberLayout.isOpen()) {
            return;
        }
        plvlsStatusBarMemberLinkmicRequestTipsIv.setVisibility(View.VISIBLE);
        linkMicRequestTipsWindow.show(plvlsStatusBarMemberIv);
        handler.removeMessages(WHAT_HIDE_USER_REQUEST_TIPS);
        handler.sendEmptyMessageDelayed(WHAT_HIDE_USER_REQUEST_TIPS, 3000);
    }

    private void hideUserRequestTips() {
        plvlsStatusBarMemberLinkmicRequestTipsIv.setVisibility(View.GONE);
        linkMicRequestTipsWindow.hide();
        handler.removeMessages(WHAT_HIDE_USER_REQUEST_TIPS);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvls_status_bar_channel_info_tv) {
            channelInfoLayout.open();
        } else if (id == R.id.plvls_status_bar_class_control_tv) {
            toggleClassStates(!v.isSelected());
        } else if (id == R.id.plvls_status_bar_share_iv) {
            v.setSelected(!v.isSelected());
        } else if (id == R.id.plvls_status_bar_setting_iv) {
            v.setSelected(!v.isSelected());
            moreSettingLayout.open();
        } else if (id == R.id.plvls_status_bar_member_iv) {
            v.setSelected(!v.isSelected());
            memberLayout.open();
            hideUserRequestTips();
        } else if (id == R.id.plvls_status_bar_linkmic_iv) {
            if (onViewActionListener != null && !onViewActionListener.isStreamerStartSuccess() && !v.isSelected()) {
                PLVToast.Builder.context(getContext())
                        .setText(R.string.plv_streamer_toast_can_not_linkmic_before_the_class)
                        .build()
                        .show();
                return;
            }
            v.setSelected(!v.isSelected());
            linkMicControlWindow.show(v, linkMicControlWindow.getShowType());
        } else if (id == R.id.plvls_status_bar_document_iv) {
            processSelectDocument();
        } else if (id == R.id.plvls_status_bar_whiteboard_iv) {
            processSelectWhiteBoard();
        }
    }
    // </editor-fold>
}
