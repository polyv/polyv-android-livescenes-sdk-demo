package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.view.MotionEvent;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.PLVLotteryManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.PLVWelfareLotteryManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.socket.IPLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.modules.socket.PLVAbsOnSocketEventListener;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.utils.PLVScreenshotHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.socket.event.interact.PLVCallAppEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.List;

/**
 * 直播和回放主页共同业务的fragment
 */
public class PLVECCommonHomeFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    protected IPLVLiveRoomDataManager liveRoomDataManager;
    //socket登录管理器
    protected IPLVSocketLoginManager socketLoginManager;
    //聊天室presenter
    protected IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    //卡片推送管理器
    protected PLVCardPushManager cardPushManager = new PLVCardPushManager();
    //抽奖挂件管理器
    protected PLVLotteryManager lotteryManager = new PLVLotteryManager();
    //有条件抽奖挂件管理器
    PLVWelfareLotteryManager welfareLotteryManager = PLVDependManager.getInstance().get(PLVWelfareLotteryManager.class);
    //截屏帮助类
    protected PLVScreenshotHelper screenshotHelper = new PLVScreenshotHelper();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //初始化聊天室
        chatroomPresenter = new PLVChatroomPresenter(liveRoomDataManager);
        chatroomPresenter.init();
        if (!isPlaybackFragment()) {
            registerChatroomView();
            //请求一次历史记录
            chatroomPresenter.setGetChatHistoryCount(10);
            chatroomPresenter.requestChatHistory(0);
        }
        //获取表情列表
        chatroomPresenter.getChatEmotionImages();
        //初始化socket并登录
        initSocketLoginManager();

        observeChatroomData();
        observeClassDetailVO();
        observeInteractEntranceData();
        observeInteractStatusData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        screenshotHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatroomPresenter != null) {
            chatroomPresenter.destroy();
        }
        if (cardPushManager != null) {
            cardPushManager.disposeCardPushAllTask();
        }
        if (lotteryManager != null) {
            lotteryManager.destroy();
        }
        destroySocketLoginManager();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    //是否是回放fragment
    protected boolean isPlaybackFragment() {
        return false;
    }

    //注册聊天室view
    protected void registerChatroomView() {
    }

    //更新观看信息
    protected void updateWatchInfo(String coverImage, String publisher) {
    }

    //更新观看信息
    protected void updateOnlineCount(long watchCount) {
    }

    //更新点赞数
    protected void updateLikesInfo(String likesString) {
    }

    //更新观看数量
    protected void updateWatchCount(long times) {
    }

    //处理商品打开
    protected void acceptOpenCommodity() {
    }

    //处理咨询提问菜单打开
    protected void acceptOpenQuiz(@NonNull PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
    }

    //处理咨询提问菜单关闭
    protected void acceptCloseQuiz() {
    }

    //处理获取到的聊天回放开关
    protected void acceptChatPlaybackEnable(boolean isChatPlaybackEnable) {
    }

    protected void acceptInteractEntranceData(List<PLVCallAppEvent.ValueBean.DataBean> dataBeans) {
    }

    protected void acceptInteractStatusData(PLVWebviewUpdateAppStatusVO webviewUpdateAppStatusVO) {
        acceptLotteryVO(webviewUpdateAppStatusVO);
    }

    private void acceptLotteryVO(PLVWebviewUpdateAppStatusVO  webviewUpdateAppStatusVO) {
        if (lotteryManager != null) {
            lotteryManager.acceptLotteryVo(webviewUpdateAppStatusVO);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public PLVCardPushManager getCardPushManager() {
        return cardPushManager;
    }

    public PLVLotteryManager getLotteryManager() {
        return lotteryManager;
    }

    //获取聊天室的公告信息
    public LiveData<PolyvBulletinVO> getBulletinVO() {
        return chatroomPresenter.getData().getBulletinVO();
    }

    //设置播放状态
    public void setPlayerState(PLVPlayerState state) {
    }

    //设置加入rtc频道状态
    public void setJoinRTCChannel(boolean isJoinRtcChannel) {
    }

    //设置加入连麦状态
    public void setJoinLinkMic(boolean isJoinLinkMic) {
    }

    //设置回放播放信息
    public void setPlaybackPlayInfo(PLVPlayInfoVO playInfoVO) {
    }

    public void onHasPreviousPage(boolean hasPreviousPage) {

    }

    //设置view交互事件监听器
    public void setOnViewActionListener(OnViewActionListener listener) {
    }

    public void acceptOnLowLatencyChange(boolean isLowLatency) {

    }

    public void acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {

    }

    public void showMorePopupWindow() {

    }

    /**
     * 回放视频准备完成
     *
     * @param sessionId sessionId
     * @param channelId 频道号
     * @param fileId    文件Id
     */
    public void onPlaybackVideoPrepared(String sessionId, String channelId, String fileId) {
    }

    /**
     * 回放视频seek完成
     *
     * @param time 时间，单位：毫秒
     */
    public void onPlaybackVideoSeekComplete(int time) {
    }

    /**
     * 主页fragment某些布局与videoLayout重叠，这里决定什么情况下进行拦截处理
     * @param motionEvent
     * @return
     */
    public boolean isInterceptViewAction(MotionEvent motionEvent){
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket - 初始化、登录、销毁">
    private void initSocketLoginManager() {
        socketLoginManager = new PLVSocketLoginManager(liveRoomDataManager);
        socketLoginManager.init();
        //直播带货需要允许使用分房间功能
        socketLoginManager.setAllowChildRoom(true);
        socketLoginManager.setOnSocketEventListener(onSocketEventListener);
        socketLoginManager.login();
    }

    private void destroySocketLoginManager() {
        if (socketLoginManager != null) {
            socketLoginManager.destroy();
        }
    }

    private IPLVSocketLoginManager.OnSocketEventListener onSocketEventListener = new PLVAbsOnSocketEventListener() {
        @Override
        public void handleLoginIng(boolean isReconnect) {
            super.handleLoginIng(isReconnect);
            if (isReconnect) {
                ToastUtils.showShort(R.string.plv_chat_toast_reconnecting);
            } else {
                ToastUtils.showShort(R.string.plv_chat_toast_logging);
            }
        }

        @Override
        public void handleLoginSuccess(boolean isReconnect) {
            super.handleLoginSuccess(isReconnect);
            if (isReconnect) {
                ToastUtils.showShort(R.string.plv_chat_toast_reconnect_success);
            } else {
                ToastUtils.showShort(R.string.plv_chat_toast_login_success);
            }
        }

        @Override
        public void handleLoginFailed(@NonNull Throwable throwable) {
            super.handleLoginFailed(throwable);
            ToastUtils.showShort(getResources().getString(R.string.plv_chat_toast_login_failed) + ":" + throwable.getMessage());
        }

        @Override
        public void onKickEvent(@NonNull PLVKickEvent kickEvent, boolean isOwn) {
            super.onKickEvent(kickEvent, isOwn);
            if (isOwn) {
                PLVToast.Builder.context(Utils.getApp())
                        .shortDuration()
                        .setText(R.string.plv_chat_toast_been_kicked)
                        .build()
                        .show();
                ((Activity)getContext()).finish();
            }
        }

        @Override
        public void onLoginRefuseEvent(@NonNull PLVLoginRefuseEvent loginRefuseEvent) {
            super.onLoginRefuseEvent(loginRefuseEvent);
            showExitDialog(R.string.plv_chat_toast_been_kicked);
        }

        @Override
        public void onReloginEvent(@NonNull PLVReloginEvent reloginEvent) {
            super.onReloginEvent(reloginEvent);
            PLVToast.Builder.context(Utils.getApp())
                    .shortDuration()
                    .setText(R.string.plv_chat_toast_account_login_elsewhere)
                    .build()
                    .show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        (getActivity()).finish();
                    }
                }
            },3000);
        }
    };

    private void showExitDialog(int messageId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(PLVAppUtils.getString(R.string.plv_common_dialog_tip_warm))
                .setMessage(messageId)
                .setPositiveButton(PLVAppUtils.getString(R.string.plv_common_dialog_confirm_2), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听直播详情、商品信息数据、互动入口数据">
    private void observeClassDetailVO() {
        //当前页面 监听 直播间数据管理器对象中的直播详情数据变化
        liveRoomDataManager.getClassDetailVO().observe(this, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> liveClassDetailVO) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (liveClassDetailVO != null && liveClassDetailVO.isSuccess()) {
                    PolyvLiveClassDetailVO classDetailVO = liveClassDetailVO.getData();
                    if (classDetailVO != null && classDetailVO.getData() != null) {
                        PolyvLiveClassDetailVO.DataBean dataBean = classDetailVO.getData();
                        updateWatchInfo(dataBean.getCoverImage(), dataBean.getPublisher());
                        //根据商品列表开关来显示/隐藏商品库按钮
                        if (classDetailVO.isOpenCommodity()) {
                            PLVDependManager.getInstance().get(PLVCommodityViewModel.class).notifyHasProductLayout(true);
                            acceptOpenCommodity();
                        }
                        //聊天回放开关
                        boolean isChatPlaybackEnable = classDetailVO.getData().isChatPlaybackEnabled();
                        acceptChatPlaybackEnable(isChatPlaybackEnable);
                        //频道菜单
                        List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean> channelMenusBeans = dataBean.getChannelMenus();
                        boolean isOpenQuiz = false;
                        for (PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : channelMenusBeans) {
                            if (channelMenusBean == null) {
                                continue;
                            }
                            //咨询提问菜单
                            if (PLVLiveClassDetailVO.MENUTYPE_QUIZ.equals(channelMenusBean.getMenuType())) {
                                isOpenQuiz = true;
                                acceptOpenQuiz(channelMenusBean);
                                break;
                            }
                        }
                        if (!isOpenQuiz) {
                            acceptCloseQuiz();
                        }
                    }
                }
            }
        });
    }

    private void observeInteractEntranceData() {
        liveRoomDataManager.getInteractEntranceData().observe(this, new Observer<List<PLVCallAppEvent.ValueBean.DataBean>>() {
            @Override
            public void onChanged(@Nullable List<PLVCallAppEvent.ValueBean.DataBean> dataBeans) {
                acceptInteractEntranceData(dataBeans);
            }
        });
    }

    private void observeInteractStatusData() {
        liveRoomDataManager.getInteractStatusData().observe(this, new Observer<PLVWebviewUpdateAppStatusVO>() {
            @Override
            public void onChanged(@Nullable PLVWebviewUpdateAppStatusVO webviewUpdateAppStatusVO) {
                acceptInteractStatusData(webviewUpdateAppStatusVO);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 观察聊天室数据">
    private void observeChatroomData() {
        chatroomPresenter.getData().getLikesCountData().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long l) {
                //点赞数
                String likesString = StringUtils.toKString(l);
                updateLikesInfo(likesString);
            }
        });
        chatroomPresenter.getData().getViewerCountData().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long l) {
                //观看热度
                updateWatchCount(l);
            }
        });
        chatroomPresenter.getData().getOnlineCountData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                //在线人数
                updateOnlineCount(integer.longValue());
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onViewCreated();
    }
    // </editor-fold>
}
