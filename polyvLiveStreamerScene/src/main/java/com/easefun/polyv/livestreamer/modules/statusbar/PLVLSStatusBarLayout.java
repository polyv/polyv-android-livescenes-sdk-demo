package com.easefun.polyv.livestreamer.modules.statusbar;

import static com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter.AUTO_ID_WHITE_BOARD;
import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVSipLinkMicViewModel;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingInListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingOutListState;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerNetworkStatusLayout;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVLiveLocalActionHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.document.popuplist.PLVLSPptListLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.IPLVLSCountDownView;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSChannelInfoLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSClassBeginCountDownWindow;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSLinkMicRequestTipsWindow;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSMemberLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSMoreSettingLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.widget.PLVLSNewLinkMicFirstIntroLayout;
import com.easefun.polyv.livestreamer.modules.statusbar.widget.PLVLSLinkMicControlButton;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.foundationsdk.component.proxy.PLVDynamicProxy;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.user.PLVSocketUserConstant;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import io.socket.client.Ack;

/**
 * 状态栏布局
 */
public class PLVLSStatusBarLayout extends FrameLayout implements IPLVLSStatusBarLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLSStatusBarLayout.class.getSimpleName();

    private static final int WHAT_HIDE_USER_REQUEST_TIPS = 1;
    private static final int WHAT_NO_SUCCESS_CALLBACK_AFTER_START_CLASS = 2;

    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager = null;
    @Nullable
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter = null;

    //view
    private TextView plvlsStatusBarChannelInfoTv;
    private TextView plvlsStatusBarStreamerTimeTv;
    private PLVStreamerNetworkStatusLayout statusBarNetQualityView;
    private TextView plvlsStatusBarClassControlTv;
    private ImageView plvlsStatusBarShareIv;
    private ImageView plvlsStatusBarSettingIv;
    private ImageView plvlsStatusBarMemberIv;
    private View plvlsStatusBarMemberLinkmicRequestTipsIv;
    private PLVLSLinkMicControlButton plvlsStatusBarLinkmicIv;
    private ImageView plvlsStatusBarDocumentIv;
    private ImageView plvlsStatusBarWhiteboardIv;
    private ImageView statusBarAllowViewerLinkmicIv;

    //频道信息布局
    private PLVLSChannelInfoLayout channelInfoLayout;
    //设置布局
    private PLVLSMoreSettingLayout moreSettingLayout;
    //成员列表布局
    private PLVLSMemberLayout memberLayout;
    //连麦请求提示布局
    private PLVLSLinkMicRequestTipsWindow linkMicRequestTipsWindow;
    //推流开始倒计时布局
    private IPLVLSCountDownView countDownView;

    // PPT文档布局
    private PLVLSPptListLayout pptListLayout;
    // 引导布局
    private Lazy<PLVLSNewLinkMicFirstIntroLayout> newLinkMicFirstIntroLayout = new Lazy<PLVLSNewLinkMicFirstIntroLayout>() {
        @Override
        public PLVLSNewLinkMicFirstIntroLayout onLazyInit() {
            return new PLVLSNewLinkMicFirstIntroLayout(getContext());
        }
    };

    private final PLVSipLinkMicViewModel sipLinkMicViewModel = PLVDependManager.getInstance().get(PLVSipLinkMicViewModel.class);

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
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_HIDE_USER_REQUEST_TIPS:
                    hideUserRequestTips();
                    break;
                case WHAT_NO_SUCCESS_CALLBACK_AFTER_START_CLASS:
                    if (pendingTaskOnNoSuccessCallbackAfterStartClass != null) {
                        pendingTaskOnNoSuccessCallbackAfterStartClass.run();
                        pendingTaskOnNoSuccessCallbackAfterStartClass = null;
                    }
                    break;
                default:
            }
        }
    };
    @Nullable
    private Runnable pendingTaskOnNoSuccessCallbackAfterStartClass;
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
        statusBarNetQualityView = findViewById(R.id.plvls_status_bar_net_quality_view);
        plvlsStatusBarClassControlTv = findViewById(R.id.plvls_status_bar_class_control_tv);
        plvlsStatusBarShareIv = findViewById(R.id.plvls_status_bar_share_iv);
        plvlsStatusBarSettingIv = findViewById(R.id.plvls_status_bar_setting_iv);
        plvlsStatusBarMemberIv = findViewById(R.id.plvls_status_bar_member_iv);
        plvlsStatusBarMemberLinkmicRequestTipsIv = (View) findViewById(R.id.plvls_status_bar_member_linkmic_request_tips_iv);
        plvlsStatusBarLinkmicIv = findViewById(R.id.plvls_status_bar_linkmic_iv);
        plvlsStatusBarDocumentIv = findViewById(R.id.plvls_status_bar_document_iv);
        plvlsStatusBarWhiteboardIv = findViewById(R.id.plvls_status_bar_whiteboard_iv);
        statusBarAllowViewerLinkmicIv = findViewById(R.id.plvls_status_bar_allow_viewer_linkmic_iv);

        channelInfoLayout = new PLVLSChannelInfoLayout(getContext());
        moreSettingLayout = new PLVLSMoreSettingLayout(getContext());
        memberLayout = new PLVLSMemberLayout(getContext());
        linkMicRequestTipsWindow = new PLVLSLinkMicRequestTipsWindow(this);
        countDownView = new PLVLSClassBeginCountDownWindow(this);
        pptListLayout = new PLVLSPptListLayout(getContext());

        plvlsStatusBarChannelInfoTv.setOnClickListener(this);
        plvlsStatusBarClassControlTv.setOnClickListener(this);
        plvlsStatusBarShareIv.setOnClickListener(this);
        plvlsStatusBarSettingIv.setOnClickListener(this);
        plvlsStatusBarMemberIv.setOnClickListener(this);
        plvlsStatusBarDocumentIv.setOnClickListener(this);
        plvlsStatusBarWhiteboardIv.setOnClickListener(this);
        statusBarAllowViewerLinkmicIv.setOnClickListener(new PLVDebounceClicker.OnClickListener(this, 1000));

        initCountDownView();
        initLinkMicControlView();
        initSettingLayout();
        initMemberLayout();
        initDocumentMvpView();
        initOnUserAbilityChangeListener();

        observeSipLinkMicListUpdate();

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
                pendingTaskOnNoSuccessCallbackAfterStartClass = new Runnable() {
                    @Override
                    public void run() {
                        if (plvlsStatusBarClassControlTv != null) {
                            plvlsStatusBarClassControlTv.setEnabled(true);
                        }
                    }
                };
                handler.sendMessageDelayed(handler.obtainMessage(WHAT_NO_SUCCESS_CALLBACK_AFTER_START_CLASS), seconds(3).toMillis());
            }

            @Override
            public void onCountDownCanceled() {
                changeStatesToClassOver();
            }
        });
    }

    private void initLinkMicControlView() {
        plvlsStatusBarLinkmicIv.setOnViewActionListener(new PLVLSLinkMicControlButton.OnViewActionListener() {
            @Override
            public boolean isStreamerStartSuccess() {
                return onViewActionListener != null && onViewActionListener.isStreamerStartSuccess();
            }

            @Override
            public void onLinkMicOpenStateChanged(boolean isVideoLinkMicType, boolean isOpenLinkMic) {
                memberLayout.updateLinkMicMediaType(isVideoLinkMicType, isOpenLinkMic);
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
            public PLVStreamerConfig.MixLayoutType getMixLayoutType() {
                return onViewActionListener.getMixLayoutType();
            }

            @Override
            public void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix) {
                onViewActionListener.onChangeMixLayoutType(mix);
            }

            @Override
            public boolean isCurrentLocalVideoEnable() {
                return onViewActionListener.isCurrentLocalVideoEnable();
            }

            @Nullable
            @Override
            public PLVPushDowngradePreference getCurrentDowngradePreference() {
                if (onViewActionListener != null) {
                    return onViewActionListener.getCurrentDowngradePreference();
                }
                return null;
            }

            @Override
            public void onDowngradePreferenceChanged(@NonNull PLVPushDowngradePreference preference) {
                if (onViewActionListener != null) {
                    onViewActionListener.onDowngradePreferenceChanged(preference);
                }
            }

            @Override
            public void onShowSignInAction() {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowSignInAction();
                }
            }
        });
    }

    private void initMemberLayout() {
        //初始化成员变量页的监听器
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
            public void onControlUserLinkMic(int position, PLVStreamerControlLinkMicAction action) {
                if (onViewActionListener != null) {
                    onViewActionListener.onControlUserLinkMic(position, action);
                }
            }

            @Override
            public void onGrantSpeakerPermission(int position, String userId, boolean isGrant) {
                if (onViewActionListener != null) {
                    onViewActionListener.onGrantSpeakerPermission(position, userId, isGrant);
                }
            }

            @Override
            public int getPosition(PLVMemberItemDataBean memberItemDataBean) {
                if (onViewActionListener != null) {
                    return onViewActionListener.getPosition(memberItemDataBean);
                }
                return -1;
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

    private void initAutoOpenLinkMic(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN)) {
            return;
        }
        final String channelId = liveRoomDataManager.getConfig().getChannelId();
        final boolean isAutoOpenLinkMic = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.STREAMER_DEFAULT_OPEN_LINKMIC_ENABLE);
        final String autoOpenLinkMicType = PLVChannelFeatureManager.onChannel(channelId).get(PLVChannelFeature.STREAMER_DEFAULT_OPEN_LINKMIC_TYPE);
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        plvlsStatusBarLinkmicIv.performAutoOpenLinkMic(isAutoOpenLinkMic, "video".equals(autoOpenLinkMicType), isNewLinkMicStrategy);
    }

    private void observeSipLinkMicListUpdate() {
        sipLinkMicViewModel.getCallingInListStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicCallingInListState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicCallingInListState sipLinkMicCallingInListState) {
                if (sipLinkMicCallingInListState == null || sipLinkMicCallingInListState.callingInViewerList.isEmpty()) {
                    return;
                }
                if (!plvlsStatusBarMemberIv.isSelected()) {
                    plvlsStatusBarMemberLinkmicRequestTipsIv.setVisibility(VISIBLE);
                }
            }
        });
        sipLinkMicViewModel.getCallingOutListStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicCallingOutListState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicCallingOutListState sipLinkMicCallingOutListState) {
                if (sipLinkMicCallingOutListState == null || sipLinkMicCallingOutListState.callingOutViewerList.isEmpty()) {
                    return;
                }
                if (!plvlsStatusBarMemberIv.isSelected()) {
                    plvlsStatusBarMemberLinkmicRequestTipsIv.setVisibility(VISIBLE);
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Streamer - Mvp View">

    private final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            streamerPresenter = presenter;
            presenter.getData().getStreamerStatus().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isLive) {
                    if (Boolean.TRUE.equals(isLive)) {
                        if (liveRoomDataManager != null) {
                            initAutoOpenLinkMic(liveRoomDataManager);
                        }
                    }
                }
            });
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现父类IPLVLSStatusBarLayout的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        channelInfoLayout.init(liveRoomDataManager);
        memberLayout.init(liveRoomDataManager);
        moreSettingLayout.init(liveRoomDataManager);
        userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
            plvlsStatusBarClassControlTv.setVisibility(GONE);
            final boolean isAutoLinkMic = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_GUEST_AUTO_LINKMIC_ENABLE);
            plvlsStatusBarLinkmicIv.setVisibility(isAutoLinkMic ? GONE : VISIBLE);
        }
        updateLinkMicShowType(liveRoomDataManager.isOnlyAudio());
        updateLinkMicStrategy(liveRoomDataManager);
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        onViewActionListener = listener;
    }

    @Override
    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return PLVDynamicProxy.forClass(IPLVStreamerContract.IStreamerView.class)
                .proxyAll(
                        streamerView,
                        memberLayout.getStreamerView(),
                        plvlsStatusBarLinkmicIv.streamerView,
                        moreSettingLayout.getStreamerView()
                );
    }

    @Override
    public void showAlertDialogNoNetwork() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.plv_streamer_dialog_no_network)
                .setPositiveButton(R.string.plv_common_dialog_confirm_5, new DialogInterface.OnClickListener() {
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
        sipLinkMicViewModel.requestSipChannelInfo();
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
    public void updateNetworkQuality(PLVLinkMicConstant.NetworkQuality networkQuality) {
        statusBarNetQualityView.onNetworkQuality(networkQuality);
    }

    @Override
    public void updateNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
        statusBarNetQualityView.onNetworkStatus(networkStatusVO);
    }

    @Override
    public void setOnlineCount(int onlineCount) {
        memberLayout.setOnlineCount(onlineCount);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (moreSettingLayout != null) {
            moreSettingLayout.onActivityResult(requestCode, resultCode, data);
        }
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
        pendingTaskOnNoSuccessCallbackAfterStartClass = null;

        plvlsStatusBarClassControlTv.setText(R.string.plv_streamer_stop);
        plvlsStatusBarClassControlTv.setEnabled(true);
        plvlsStatusBarClassControlTv.setSelected(true);

        plvlsStatusBarStreamerTimeTv.setVisibility(View.VISIBLE);
        plvlsStatusBarStreamerTimeTv.setText("00:00:00");
    }

    private void changeStatesToClassOver() {
        plvlsStatusBarClassControlTv.setText(R.string.plv_streamer_start);
        plvlsStatusBarClassControlTv.setSelected(false);
        plvlsStatusBarClassControlTv.setEnabled(true);

        plvlsStatusBarStreamerTimeTv.setVisibility(View.GONE);
    }

    private void toggleClassStates(boolean isWillStart) {
        if (isWillStart) {
            if (onViewActionListener != null) {
                PLVLinkMicConstant.NetworkQuality currentNetworkQuality = onViewActionListener.getCurrentNetworkQuality();
                if (currentNetworkQuality == PLVLinkMicConstant.NetworkQuality.DISCONNECT) {
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

    // <editor-fold defaultstate="collapsed" desc="连麦配置">
    private void updateLinkMicShowType(boolean isOnlyAudio) {
        linkMicShowType = isOnlyAudio ? 1 : 0;
    }

    private void updateLinkMicStrategy(IPLVLiveRoomDataManager liveRoomDataManager) {
        final boolean canControlLinkMic = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN);
        if (!canControlLinkMic) {
            return;
        }
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        if (!isNewLinkMicStrategy) {
            plvlsStatusBarLinkmicIv.setVisibility(View.VISIBLE);
            statusBarAllowViewerLinkmicIv.setVisibility(View.GONE);
        } else {
            plvlsStatusBarLinkmicIv.setVisibility(View.GONE);
            statusBarAllowViewerLinkmicIv.setVisibility(View.VISIBLE);
            if (!PLVLSNewLinkMicFirstIntroLayout.hasShownFirstIntro()) {
                newLinkMicFirstIntroLayout.get().show();
            }
        }
    }

    private void switchAllowViewerLinkMic(final boolean toAllow) {
        if (streamerPresenter == null) {
            return;
        }
        final boolean isNetworkConnected = PLVNetworkUtils.isConnected(getContext());
        if (!isNetworkConnected) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_allow_viewer_linkmic_fail_toast)
                    .show();
            return;
        }

        boolean success;
        if (toAllow) {
            success = streamerPresenter.allowViewerRaiseHand(new Ack() {
                @Override
                public void call(Object... args) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_allow_viewer_linkmic_toast)
                            .show();
                    statusBarAllowViewerLinkmicIv.setActivated(true);
                }
            });
        } else {
            success = streamerPresenter.disallowViewerRaiseHand(new Ack() {
                @Override
                public void call(Object... args) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_disallow_viewer_linkmic_toast)
                            .show();
                    statusBarAllowViewerLinkmicIv.setActivated(false);
                }
            });
        }

        if (!success) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_allow_viewer_linkmic_fail_toast)
                    .show();
            PLVCommonLog.d(TAG, "switchAllowViewerLinkMic fail, toAllow:" + toAllow);
        }
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
        if (memberLayout.isOpen() || !PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER)) {
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
            memberLayout.open();
            hideUserRequestTips();
        } else if (id == R.id.plvls_status_bar_document_iv) {
            processSelectDocument();
        } else if (id == R.id.plvls_status_bar_whiteboard_iv) {
            processSelectWhiteBoard();
        } else if (id == statusBarAllowViewerLinkmicIv.getId()) {
            switchAllowViewerLinkMic(!statusBarAllowViewerLinkmicIv.isActivated());
        }
    }
    // </editor-fold>
}
