package com.easefun.polyv.streameralone.modules.statusbar;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * 状态栏布局
 */
public class PLVSAStatusBarLayout extends FrameLayout implements IPLVSAStatusBarLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private View rootView;
    private ImageView plvsaStatusBarCloseIv;
    private LinearLayout plvsaStatusBarInfoLl;
    private PLVRoundRectLayout plvsaStatusBarChannelInfoRl;
    private ImageView plvsaStatusBarChannelInfoIv;
    private TextView plvsaStatusBarChannelInfoTv;
    private ImageView plvsaStatusBarChannelInfoNavIv;
    private PLVRoundRectLayout plvsaStatusBarMemberCountRl;
    private ImageView plvsaStatusBarMemberCountIv;
    private TextView plvsaStatusBarMemberCountTv;
    private PLVRoundRectLayout plvsaStatusBarStreamerTimeRl;
    private PLVRoundImageView plvsaStatusBarStreamerStatusIv;
    private TextView plvsaStatusBarStreamerTimeTv;
    private PLVRoundRectLayout plvsaStatusBarStreamerTeacherLayout;
    private LinearLayout plvsaStatusBarStreamerTeacherLl;
    private TextView plvsaStatusBarTeacherNameTv;
    private ImageView plvsaStatusBarStreamerMicIv;
    private PLVRoundRectLayout plvsaStatusBarNetworkStatusLayout;
    private LinearLayout plvsaStatusBarNetworkStatusLl;
    private ImageView plvsaStatusBarNetworkStatusIv;
    private TextView plvsaStatusBarNetworkStatusTv;
    private PLVRoundRectLayout plvsaStatusBarNotificationLayout;
    private TextView plvsaStatusBarNotificationLabel;
    private TextView plvsaStatusBarNotificationTv;

    private PLVSAChannelInfoLayout channelInfoLayout;

    // 停止直播确认对话框
    private PLVConfirmDialog stopLiveConfirmDialog;

    private PLVLinkMicItemDataBean localLinkMicItemDataBeanRef;

    private IPLVLiveRoomDataManager liveRoomDataManager;

    // 是否打开麦克风、摄像头
    private boolean isOpenAudio = true;
    private boolean isOpenVideo = true;

    private OnStopLiveListener stopLiveListener;

    //关闭直播提示的文案
    private String closeContentString;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStatusBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStatusBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStatusBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvsa_status_bar_layout, this);
        findView();
        initChannelInfoOnClickListener();
        initCloseIconOnClickListener();
    }

    private void findView() {
        plvsaStatusBarCloseIv = (ImageView) findViewById(R.id.plvsa_status_bar_close_iv);
        plvsaStatusBarInfoLl = (LinearLayout) findViewById(R.id.plvsa_status_bar_info_ll);
        plvsaStatusBarChannelInfoRl = (PLVRoundRectLayout) findViewById(R.id.plvsa_status_bar_channel_info_rl);
        plvsaStatusBarChannelInfoIv = (ImageView) findViewById(R.id.plvsa_status_bar_channel_info_iv);
        plvsaStatusBarChannelInfoTv = (TextView) findViewById(R.id.plvsa_status_bar_channel_info_tv);
        plvsaStatusBarChannelInfoNavIv = (ImageView) findViewById(R.id.plvsa_status_bar_channel_info_nav_iv);
        plvsaStatusBarMemberCountRl = (PLVRoundRectLayout) findViewById(R.id.plvsa_status_bar_member_count_rl);
        plvsaStatusBarMemberCountIv = (ImageView) findViewById(R.id.plvsa_status_bar_member_count_iv);
        plvsaStatusBarMemberCountTv = (TextView) findViewById(R.id.plvsa_status_bar_member_count_tv);
        plvsaStatusBarStreamerTimeRl = (PLVRoundRectLayout) findViewById(R.id.plvsa_status_bar_streamer_time_rl);
        plvsaStatusBarStreamerStatusIv = (PLVRoundImageView) findViewById(R.id.plvsa_status_bar_streamer_status_iv);
        plvsaStatusBarStreamerTimeTv = (TextView) findViewById(R.id.plvsa_status_bar_streamer_time_tv);
        plvsaStatusBarStreamerTeacherLayout = (PLVRoundRectLayout) findViewById(R.id.plvsa_status_bar_streamer_teacher_layout);
        plvsaStatusBarStreamerTeacherLl = (LinearLayout) findViewById(R.id.plvsa_status_bar_streamer_teacher_ll);
        plvsaStatusBarTeacherNameTv = (TextView) findViewById(R.id.plvsa_status_bar_teacher_name_tv);
        plvsaStatusBarStreamerMicIv = (ImageView) findViewById(R.id.plvsa_status_bar_streamer_mic_iv);
        plvsaStatusBarNetworkStatusLayout = (PLVRoundRectLayout) findViewById(R.id.plvsa_status_bar_network_status_layout);
        plvsaStatusBarNetworkStatusLl = (LinearLayout) findViewById(R.id.plvsa_status_bar_network_status_ll);
        plvsaStatusBarNetworkStatusIv = (ImageView) findViewById(R.id.plvsa_status_bar_network_status_iv);
        plvsaStatusBarNetworkStatusTv = (TextView) findViewById(R.id.plvsa_status_bar_network_status_tv);
        plvsaStatusBarNotificationLayout = (PLVRoundRectLayout) findViewById(R.id.plvsa_status_bar_notification_layout);
        plvsaStatusBarNotificationLabel = (TextView) findViewById(R.id.plvsa_status_bar_notification_label);
        plvsaStatusBarNotificationTv = (TextView) findViewById(R.id.plvsa_status_bar_notification_tv);
    }

    private void initChannelInfoOnClickListener() {
        plvsaStatusBarChannelInfoRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelInfoLayout != null) {
                    channelInfoLayout.open();
                }
            }
        });
    }

    private void initCloseIconOnClickListener() {
        plvsaStatusBarCloseIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showStopLiveConfirmLayout();
            }
        });
    }

    private void showStopLiveConfirmLayout() {
        if (stopLiveConfirmDialog == null) {
            stopLiveConfirmDialog = new PLVSAConfirmDialog(getContext())
                    .setTitleVisibility(GONE)
                    .setContent(closeContentString)
                    .setRightButtonText("确认")
                    .setRightBtnListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (stopLiveListener != null) {
                                stopLiveListener.onStopLive();
                            }
                            stopLiveConfirmDialog.hide();
                        }
                    });
        }
        stopLiveConfirmDialog.show();
    }

    private void initTeacherName(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (liveRoomDataManager != null && liveRoomDataManager.getConfig().getUser() != null) {
            String actor = liveRoomDataManager.getConfig().getUser().getActor();
            String name = liveRoomDataManager.getConfig().getUser().getViewerName();
            name = trimStringLength(name, 15);
            plvsaStatusBarTeacherNameTv.setText(actor + "-" + name);
        }
    }

    private void initChannelInfoLayout(IPLVLiveRoomDataManager liveRoomDataManager) {
        channelInfoLayout = new PLVSAChannelInfoLayout(getContext());
        channelInfoLayout.init(liveRoomDataManager);
    }

    private void initCloseTipContent(IPLVLiveRoomDataManager liveRoomDataManager) {
        boolean isGuest = PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType());
        closeContentString = isGuest ? getContext().getString(R.string.plv_live_room_dialog_exit_confirm_ask)
                : getContext().getString(R.string.plv_live_room_dialog_steamer_exit_confirm_ask);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSAStatusBarLayout定义的方法">

    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        initTeacherName(liveRoomDataManager);
        initChannelInfoLayout(liveRoomDataManager);
        initCloseTipContent(liveRoomDataManager);
    }

    @Override
    public void setOnlineCount(int onlineCount) {
        plvsaStatusBarMemberCountTv.setText(StringUtils.toWString(onlineCount).toUpperCase());
    }

    @Override
    public void updateChannelName(String channelName) {
        if (channelInfoLayout != null) {
            channelInfoLayout.updateChannelName(channelName);
        }
    }

    @Override
    public void setOnStopLiveListener(OnStopLiveListener stopLiveListener) {
        this.stopLiveListener = stopLiveListener;
    }

    @Override
    public boolean onBackPressed() {
        if (channelInfoLayout != null) {
            return channelInfoLayout.onBackPressed();
        }
        return false;
    }

    @Override
    public void destroy() {

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流 MVP - View">

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    private IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            presenter.getData().getEnableAudio().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isAudioEnable) {
                    if (isAudioEnable == null) {
                        return;
                    }
                    isOpenAudio = isAudioEnable;
                    updateNotificationBar();
                }
            });
            presenter.getData().getEnableVideo().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isVideoEnable) {
                    if (isVideoEnable == null) {
                        return;
                    }
                    isOpenVideo = isVideoEnable;
                    updateNotificationBar();
                }
            });
            presenter.getData().getStreamerStatus().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isStartedStatus) {
                    if(isStartedStatus == null){
                        return;
                    }
                    if(isGuest()){
                        if(isStartedStatus){
                            plvsaStatusBarStreamerTimeRl.setVisibility(VISIBLE);
                            plvsaStatusBarNetworkStatusLayout.setVisibility(VISIBLE);
                        } else {
                            plvsaStatusBarStreamerTimeRl.setVisibility(INVISIBLE);
                            plvsaStatusBarNetworkStatusLayout.setVisibility(INVISIBLE);
                        }
                    }
                }
            });
        }

        @Override
        public void onUpdateStreamerTime(int secondsSinceStartTiming) {
            int seconds = secondsSinceStartTiming % 60;
            int minutes = (secondsSinceStartTiming % (60 * 60)) / 60;
            int hours = (secondsSinceStartTiming % (60 * 60 * 24)) / (60 * 60);

            String secondString = String.format(Locale.getDefault(), "%02d", seconds);
            String minuteString = String.format(Locale.getDefault(), "%02d", minutes);
            String hourString = String.format(Locale.getDefault(), "%02d", hours);

            final String timingText = hourString + ":" + minuteString + ":" + secondString;

            plvsaStatusBarStreamerTimeTv.setText(timingText);
        }

        @Override
        public void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList) {
            for (PLVLinkMicItemDataBean linkMicItemDataBean : linkMicList) {
                if (linkMicUid.equals(linkMicItemDataBean.getLinkMicId())) {
                    // 保存自己的连麦数据对象，后续更新连麦音量变化
                    localLinkMicItemDataBeanRef = linkMicItemDataBean;
                    break;
                }
            }
        }

        @Override
        public void onNetworkQuality(int quality) {
            if (!PLVNetworkUtils.isConnected(getContext())) {
                quality = PLVLinkMicConstant.NetQuality.NET_QUALITY_NO_CONNECTION;
            }

            switch (quality) {
                case PLVLinkMicConstant.NetQuality.NET_QUALITY_GOOD:
                    plvsaStatusBarNetworkStatusIv.setImageResource(R.drawable.plvsa_network_signal_3);
                    plvsaStatusBarNetworkStatusTv.setText("网络良好");
                    break;
                case PLVLinkMicConstant.NetQuality.NET_QUALITY_MIDDLE:
                    plvsaStatusBarNetworkStatusIv.setImageResource(R.drawable.plvsa_network_signal_2);
                    plvsaStatusBarNetworkStatusTv.setText("网络一般");
                    break;
                case PLVLinkMicConstant.NetQuality.NET_QUALITY_POOR:
                case PLVLinkMicConstant.NetQuality.NET_QUALITY_NO_CONNECTION:
                default:
                    plvsaStatusBarNetworkStatusIv.setImageResource(R.drawable.plvsa_network_signal_1);
                    plvsaStatusBarNetworkStatusTv.setText("网络异常");
                    break;
            }
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteAudio(uid, mute, streamerListPos, memberListPos);
            updateVolumeChanged();
        }

        @Override
        public void onLocalUserMicVolumeChanged(int volume) {
            updateVolumeChanged();
        }


    };

    private void updateVolumeChanged(){
        if (localLinkMicItemDataBeanRef == null) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_close);
            return;
        }
        final boolean isMuteAudio = localLinkMicItemDataBeanRef.isMuteAudio();
        final int curVolume = localLinkMicItemDataBeanRef.getCurVolume();
        if (isMuteAudio) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_close);
        } else if (curVolume <= 5) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_open);
        } else if (curVolume <= 15) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_10);
        } else if (curVolume <= 25) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_20);
        } else if (curVolume <= 35) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_30);
        } else if (curVolume <= 45) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_40);
        } else if (curVolume <= 55) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_50);
        } else if (curVolume <= 65) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_60);
        } else if (curVolume <= 75) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_70);
        } else if (curVolume <= 85) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_80);
        } else if (curVolume <= 95) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_90);
        } else if (curVolume <= 100) {
            plvsaStatusBarStreamerMicIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_100);
        }
    }

    private void updateNotificationBar() {
        boolean notificationBarVisible = !isOpenVideo || !isOpenAudio;
        if (notificationBarVisible) {
            plvsaStatusBarNotificationLayout.setVisibility(VISIBLE);
        } else {
            plvsaStatusBarNotificationLayout.setVisibility(GONE);
            return;
        }

        String notificationText;
        if (!isOpenAudio && !isOpenVideo) {
            notificationText = "你的摄像头和麦克风已关闭";
        } else if (!isOpenAudio) {
            notificationText = "你的麦克风已关闭";
        } else {
            // !isOpenVideo
            notificationText = "你的摄像头已关闭";
        }
        plvsaStatusBarNotificationTv.setText(notificationText);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewWithOrientation();
    }

    private void updateViewWithOrientation() {
        moveNetworkStatusLayout();
    }

    /**
     * 竖屏：网络状态控件在关闭按钮下方
     * 横屏：网络状态控件在关闭按钮左侧
     */
    private void moveNetworkStatusLayout() {
        final int idStatusBarCloseIv = plvsaStatusBarCloseIv.getId();
        final int idNetworkStatusLayout = plvsaStatusBarNetworkStatusLayout.getId();

        ConstraintLayout.LayoutParams networkStatusLayoutParam = (ConstraintLayout.LayoutParams) plvsaStatusBarNetworkStatusLayout.getLayoutParams();
        ConstraintLayout.LayoutParams teacherLayoutParam = (ConstraintLayout.LayoutParams) plvsaStatusBarStreamerTeacherLayout.getLayoutParams();
        if (PLVScreenUtils.isPortrait(getContext())) {
            networkStatusLayoutParam.topToBottom = idStatusBarCloseIv;
            networkStatusLayoutParam.rightToRight = idStatusBarCloseIv;
            networkStatusLayoutParam.endToEnd = idStatusBarCloseIv;
            networkStatusLayoutParam.rightToLeft = ConstraintLayout.LayoutParams.UNSET;
            networkStatusLayoutParam.endToStart = ConstraintLayout.LayoutParams.UNSET;
            networkStatusLayoutParam.topToTop = ConstraintLayout.LayoutParams.UNSET;
            networkStatusLayoutParam.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;

            networkStatusLayoutParam.topMargin = ConvertUtils.dp2px(12);
            networkStatusLayoutParam.rightMargin = 0;
            networkStatusLayoutParam.setMarginEnd(0);

            teacherLayoutParam.rightToLeft = idNetworkStatusLayout;
            teacherLayoutParam.endToStart = idNetworkStatusLayout;
        } else {
            networkStatusLayoutParam.topToBottom = ConstraintLayout.LayoutParams.UNSET;
            networkStatusLayoutParam.rightToRight = ConstraintLayout.LayoutParams.UNSET;
            networkStatusLayoutParam.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            networkStatusLayoutParam.rightToLeft = idStatusBarCloseIv;
            networkStatusLayoutParam.endToStart = idStatusBarCloseIv;
            networkStatusLayoutParam.topToTop = idStatusBarCloseIv;
            networkStatusLayoutParam.bottomToBottom = idStatusBarCloseIv;

            networkStatusLayoutParam.topMargin = 0;
            networkStatusLayoutParam.rightMargin = ConvertUtils.dp2px(8);
            networkStatusLayoutParam.setMarginEnd(ConvertUtils.dp2px(8));

            teacherLayoutParam.rightToLeft = ConstraintLayout.LayoutParams.UNSET;
            teacherLayoutParam.endToStart = ConstraintLayout.LayoutParams.UNSET;
        }

        plvsaStatusBarNetworkStatusLayout.setLayoutParams(networkStatusLayoutParam);
        plvsaStatusBarStreamerTeacherLayout.setLayoutParams(teacherLayoutParam);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private static String trimStringLength(String oriString, int specLength) {
        if (oriString.length() <= specLength) {
            return oriString;
        }
        return oriString.substring(0, specLength) + "...";
    }

    private boolean isGuest(){
        if(liveRoomDataManager != null){
            return PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType());
        }
        return false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    /**
     * 停止直播回调，由上层停止直播
     */
    public interface OnStopLiveListener {
        void onStopLive();
    }

    // </editor-fold>

}
