package com.easefun.polyv.livecommon.module.modules.reward.contract;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.plv.livescenes.model.pointreward.PLVRewardSettingVO;

/**
 * mvp - 契约类接口
 * 定义了积分打赏业务相关的VP层接口
 */
public interface IPLVPointRewardContract {

    // <editor-fold defaultstate="collapsed" desc="积分打赏view接口">

    interface IPointRewardView{

        /**
         * 初始化礼物打赏配置
         * @param payWay 支付方式
         * @param giftReward 礼物打赏实体
         */
        void initGiftRewardSetting(String payWay, PLVRewardSettingVO.GiftDonateDTO giftReward);

        /**
         * 显示打赏弹窗
         * @param enable
         */
        void showPointRewardDialog(boolean enable);

        /**
         * 是否显示打赏按钮
         * @param enable true表示显示
         */
        void showPointRewardEnable(boolean enable);

        /**
         * 更新剩余积分
         *
         * @param remainingPoint 剩余积分
         */
        void updateRemainingPoint(String remainingPoint);

        void destroy();

        /**
         * 点击返回
         *
         * @return 返回true表示拦截事件
         */
        boolean onBackPress();

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="积分打赏presenter接口">

    interface IPointRewardPresenter{

        /**
         * 注册view
         */
        void registerView(@NonNull IPointRewardView v);

        /**
         * 解除注册的view
         */
        void unregisterView();

        /**
         * 配置信息
         * @param channel
         * @param user
         */
        void init(String channel, PLVLiveChannelConfig.User user);

        /**
         * 获取聊天室积分打赏配置
         */
        void getPointRewardSetting();

        /**
         * 当前用户使用指定的礼物进行积分打赏
         * @param goodId 礼物id
         * @param goodNum 礼物数量
         */
        void makeGiftPointReward(int goodId, int goodNum);

        /**
         * 当前用户使用指定的礼物进行道具打赏
         * @param goodId
         * @param goodNum
         * @param sessionId
         */
        void makeGiftCashReward(int goodId, int goodNum, String sessionId);

        /**
         * 获取当前用户的积分
         */
        void getRemainingRewardPoint();

        /**
         * 销毁
         */
        void destroy();

    }

    // </editor-fold >

}
