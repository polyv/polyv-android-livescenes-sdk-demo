package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.socket.IPLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.modules.socket.PLVAbsOnSocketEventListener;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketLoginManager;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livescenes.model.commodity.saas.PolyvCommodityVO;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

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

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //初始化聊天室
        chatroomPresenter = new PLVChatroomPresenter(liveRoomDataManager);
        registerChatroomView();
        chatroomPresenter.init();
        //请求一次历史记录
        chatroomPresenter.setGetChatHistoryCount(10);
        chatroomPresenter.requestChatHistory(0);
        //初始化socket并登录
        initSocketLoginManager();

        observeChatroomData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatroomPresenter != null) {
            chatroomPresenter.destroy();
        }
        destroySocketLoginManager();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        observeClassDetailVO();
        observeCommodityVO();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    //注册聊天室view
    protected void registerChatroomView() {
    }

    //更新观看信息
    protected void updateWatchInfo(String coverImage, String publisher) {
    }

    //更新观看信息
    protected void updateWatchInfo(long watchCount) {
    }

    //更新点赞数
    protected void updateLikesInfo(String likesString) {
    }

    //处理商品打开
    protected void acceptOpenCommodity() {
    }

    //处理获取到的商品数据
    protected void acceptCommodityVO(PolyvCommodityVO commodityVO, boolean isAddOrSet) {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //获取聊天室的公告信息
    public LiveData<PolyvBulletinVO> getBulletinVO() {
        return chatroomPresenter.getData().getBulletinVO();
    }

    //设置播放状态
    public void setPlayerState(PLVPlayerState state) {
    }

    //跳转到购买商品页面
    public void jumpBuyCommodity() {

    }

    //设置回放播放信息
    public void setPlaybackPlayInfo(PLVPlayInfoVO playInfoVO) {
    }

    //设置view交互事件监听器
    public void setOnViewActionListener(OnViewActionListener listener) {
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
                showExitDialog(R.string.plv_chat_toast_been_kicked);
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
            showExitDialog(R.string.plv_chat_toast_account_login_elsewhere);
        }
    };

    private void showExitDialog(int messageId) {
        new AlertDialog.Builder(getActivity())
                .setTitle("温馨提示")
                .setMessage(messageId)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听直播详情、商品信息数据">
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
                            acceptOpenCommodity();
                        }
                    }
                }
            }
        });
    }

    private void observeCommodityVO() {
        //当前页面 监听 直播间数据管理器对象中的直播商品数据变化
        liveRoomDataManager.getCommodityVO().observe(this, new Observer<PLVStatefulData<PolyvCommodityVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvCommodityVO> commodityVO) {
                if (commodityVO != null && commodityVO.isSuccess()) {
                    boolean isAddOrSet = liveRoomDataManager.getCommodityRank() <= -1;
                    acceptCommodityVO(commodityVO.getData(), !isAddOrSet);
                }
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
                String likesString = StringUtils.toWString(l);
                updateLikesInfo(likesString);
            }
        });
        chatroomPresenter.getData().getViewerCountData().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long l) {
                //观看热度
            }
        });
        chatroomPresenter.getData().getOnlineCountData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                //在线人数
                updateWatchInfo(integer.longValue());
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
