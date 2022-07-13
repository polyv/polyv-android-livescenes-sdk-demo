package com.easefun.polyv.livecommon.module.modules.reward;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.reward.contract.IPLVPointRewardContract;
import com.easefun.polyv.livecommon.module.modules.reward.presenter.PLVPointRewardPresenter;
import com.easefun.polyv.livecommon.module.modules.reward.view.adapter.PLVRewardListAdapter;
import com.easefun.polyv.livecommon.module.modules.reward.view.dialog.PLVRewardDialogView;
import com.easefun.polyv.livecommon.module.modules.reward.view.vo.PLVRewardItemVO;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livescenes.model.pointreward.PLVRewardSettingVO;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2021-2-22
 * author: ysh
 * description: 积分打赏layout，用于封装积分打赏功能，包括积分打赏选择弹窗、item动画
 */
public class PLVPointRewardLayout extends FrameLayout implements IPLVPointRewardContract.IPointRewardView {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //打赏弹窗
    private PLVRewardDialogView rewardDialogView;

    private IPLVPointRewardContract.IPointRewardPresenter presenter;
    //回调给外部的监听器
    private OnPointRewardListener rewardListener;

    private String sessionId;

    //是否开启打赏
    private boolean isEnablePointReward = false;

    private static final int REWARD_GIFT_POINT = 1;
    private static final int REWARD_GIFT_CASH = 2;
    private int rewardType = REWARD_GIFT_POINT;

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVPointRewardLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVPointRewardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPointRewardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">

    private void init() {

        //打赏弹窗初始化
        rewardDialogView = new PLVRewardDialogView((AppCompatActivity) getContext(), this);
        rewardDialogView.setMakeRewardListener(new PLVRewardDialogView.OnMakeRewardListener() {
            @Override
            public void onMakeReward(PLVBaseViewData data, int rewardNum) {
                makeReward(data, rewardNum);
            }
        });
        rewardDialogView.setShowListener(new PLVRewardDialogView.OnShowListener() {
            @Override
            public void onShow() {
                if(rewardType == REWARD_GIFT_POINT){
                    presenter.getRemainingRewardPoint();
                }
            }
        });


        presenter = new PLVPointRewardPresenter();
        presenter.registerView(this);

    }

    private void makeReward(PLVBaseViewData data, int rewardNum){
        if (data == null) {
            return;
        }
        Object item = data.getData();
        if (item instanceof PLVRewardItemVO) {
            PLVRewardItemVO goodBean = (PLVRewardItemVO) item;
            if (rewardType == REWARD_GIFT_POINT) {
                presenter.makeGiftPointReward(goodBean.getGoodId(), rewardNum);
            } else if (rewardType == REWARD_GIFT_CASH) {
                presenter.makeGiftCashReward(goodBean.getGoodId(), rewardNum, sessionId);
            }
        }

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(presenter != null){
            presenter.unregisterView();
            presenter.destroy();
        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="横竖屏切换">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(rewardDialogView != null && isEnablePointReward) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //横屏
                rewardDialogView.changeToLandscape();
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                //竖屏
                rewardDialogView.changeToPortrait();
            }
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">
    /**
     * 设置打赏配置回调监听
     * @param listener
     */
    public void setOnPointRewardListener(OnPointRewardListener listener){
        rewardListener = listener;
    }

    /**
     * 配置频道信息、用户信息
     */
    public void initChannelConfig(@NonNull PLVLiveChannelConfig config, IPLVLiveRoomDataManager roomDataManager) {
        if(presenter != null && config != null) {
            presenter.init(config.getChannelId(), config.getUser());
            // 直播才需要获取打赏配置
            if (config.isLive()) {
                //获取积分打赏配置
                presenter.getPointRewardSetting();
            }
        }

        if(roomDataManager != null){
            sessionId = roomDataManager.getSessionId();
            roomDataManager.getSessionIdLiveData().observe((LifecycleOwner) getContext(), new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    sessionId = s;
                }
            });
        }
    }

    /**
     * 切换场景，对不同场景做UI的微调。
     */
    public void changeScene(PLVLiveScene currentScene){
        if(currentScene == PLVLiveScene.CLOUDCLASS){
            if(rewardDialogView != null){//打赏UI
                rewardDialogView.getCloseButton().setVisibility(GONE);
                rewardDialogView.getMakeRewardButton().setBackgroundResource(R.drawable.plv_shape_point_reward_point_to_send_btn_pink);
                rewardDialogView.changeDialogTop(true);
            }

        } else if(currentScene == PLVLiveScene.ECOMMERCE){
            if(rewardDialogView != null){
                rewardDialogView.getCloseButton().setVisibility(VISIBLE);
                rewardDialogView.getMakeRewardButton().setBackgroundResource(R.drawable.plv_shape_point_reward_point_to_send_btn_orange);
                rewardDialogView.changeDialogTop(false);

            }
        }
    }
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="MVP - V层实现">

    @Override
    public void initGiftRewardSetting(String payWay, PLVRewardSettingVO.GiftDonateDTO giftReward) {
        List<PLVBaseViewData> viewDataList = new ArrayList<>();
        if (giftReward != null) {
            if("POINT".equals(giftReward.getPayWay())){
                //积分打赏
                rewardType = REWARD_GIFT_POINT;
                exchangeGiftPointRewardLayout();
                for (PLVRewardSettingVO.GiftDonateDTO.GiftDetailDTO good : giftReward.getPointPays()) {
                    viewDataList.add(new PLVBaseViewData<>(new PLVRewardItemVO(good, giftReward.getPointUnit()), PLVRewardListAdapter.ITEM_GIFT_POINT_REWARD));
                }
            } else if("CASH".equals(giftReward.getPayWay())){
                //道具打赏
                rewardType = REWARD_GIFT_CASH;
                exchangeGiftCashRewardLayout();
                for (PLVRewardSettingVO.GiftDonateDTO.GiftDetailDTO good : giftReward.getCashPays()) {
                    viewDataList.add(new PLVBaseViewData<>(new PLVRewardItemVO(good, giftReward.getCashUnit()), PLVRewardListAdapter.ITEM_GIFT_CASH_REWARD));
                }
            }

        }
        rewardDialogView.init(viewDataList);
    }

    @Override
    public void showPointRewardDialog(boolean enable) {
        if(rewardDialogView != null){
            if(enable) {
                rewardDialogView.show();
            } else {
                rewardDialogView.hide();
            }
        }
    }

    @Override
    public void showPointRewardEnable(boolean enable) {
        if(rewardListener != null){
            rewardListener.pointRewardEnable(enable);
        }
        isEnablePointReward = enable;
    }

    @Override
    public void updateRemainingPoint(String remainingPoint) {
        if (rewardDialogView != null) {
            rewardDialogView.updateRemainingPoint(remainingPoint);
        }
    }

    @Override
    public boolean onBackPress() {
        if(rewardDialogView != null && rewardDialogView.isShown()){
            rewardDialogView.hide();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="更新弹窗UI">

    /**
     * 切换到礼物打赏 - 积分
     */
    private void exchangeGiftPointRewardLayout(){
        if(rewardDialogView != null){
            rewardDialogView.getRewardTitleTextView().setText("积分打赏");
            rewardDialogView.getRemainingPointTextView().setVisibility(VISIBLE);
        }
    }

    /**
     * 切换到礼物打赏-现金支付
     */
    private void exchangeGiftCashRewardLayout(){
        if(rewardDialogView != null){
            rewardDialogView.getRewardTitleTextView().setText("道具打赏");
            rewardDialogView.getRemainingPointTextView().setVisibility(INVISIBLE);
        }
    }

    // </editor-fold >


}
