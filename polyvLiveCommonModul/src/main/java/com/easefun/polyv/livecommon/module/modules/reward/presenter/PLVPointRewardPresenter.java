package com.easefun.polyv.livecommon.module.modules.reward.presenter;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.modules.reward.contract.IPLVPointRewardContract;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.net.PLVResponseApiBean;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.pointreward.IPLVPointRewardDataSource;
import com.plv.livescenes.feature.pointreward.PLVRewardDataSource;
import com.plv.livescenes.feature.pointreward.vo.PLVDonateGoodResponseVO;
import com.plv.livescenes.model.pointreward.PLVRewardSettingVO;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.HttpException;

/**
 * 打赏功能实现
 * TODO 尚未支持现金打赏
 * 支持
 * 礼物打赏 - 现金支付（免费模式）
 * 礼物打赏 - 积分打赏
 * <p>
 * 注意事项：由于SDK尚未支持支付接口，所以仅支持无关支付的实现。开发者可以自行修改相关。增加支付的支持。
 */
public class PLVPointRewardPresenter implements IPLVPointRewardContract.IPointRewardPresenter {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVPointRewardPresenter.class.getSimpleName();

    //礼物打赏 - 现金支付 - 是否是免费打赏。
    //暂时强制为true，暂不支持支付接口回调
    private static final boolean isFreeDonateInGiftCash = true;


    private String currentChannel;
    private PLVLiveChannelConfig.User currentUser;

    private PLVRewardDataSource rewardManager;

    private WeakReference<IPLVPointRewardContract.IPointRewardView> viewRef;
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">

    public PLVPointRewardPresenter() {
        this.rewardManager = new PLVRewardDataSource();
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="MVP - P层实现">

    @Override
    public void registerView(@NonNull IPLVPointRewardContract.IPointRewardView v) {
        viewRef = new WeakReference<>(v);
    }

    @Override
    public void unregisterView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    @Override
    public void init(String channel, PLVLiveChannelConfig.User user) {
        this.currentChannel = channel;
        this.currentUser = user;
    }

    @Override
    public void getPointRewardSetting() {
        if (viewRef == null || viewRef.get() == null) {
            return;
        }
        rewardManager.getPointRewardSetting(currentChannel, new IPLVPointRewardDataSource.IPointRewardListener<PLVRewardSettingVO>() {
            @Override
            public void onSuccess(PLVRewardSettingVO plvRewardSettingVO) {
                if (plvRewardSettingVO == null) {
                    return;
                }

                if (plvRewardSettingVO.getDonateCashEnabled()) {
                    //TODO 尚未实现支持
                    handleCashReward(plvRewardSettingVO.getCashDonate());
                }

                if (plvRewardSettingVO.getDonateGiftEnabled()) {
                    if (plvRewardSettingVO.getGiftDonate() != null) {
                        PLVRewardSettingVO.GiftDonateDTO giftDonate = plvRewardSettingVO.getGiftDonate();
                        if ("POINT".equals(giftDonate.getPayWay())) {
                            //积分打赏
                            handleGiftPointReward(giftDonate, giftDonate.getPointUnit());
                        } else if ("CASH".equals(giftDonate.getPayWay())) {
                            //现金支付
                            handleGiftCashReward(giftDonate, giftDonate.getCashUnit(), isFreeDonateInGiftCash);
                        }

                        if (viewRef != null && viewRef.get() != null) {
                            //初始化弹窗礼物
                            viewRef.get().initGiftRewardSetting(giftDonate.getPayWay(), giftDonate);
                            //显示小button可点击选择弹出View
                            viewRef.get().showPointRewardEnable(true);
                        }
                    }
                }
            }

            @Override
            public void onFailed(Throwable throwable) {
                //获取积分打赏设置，不显示错误提示
                PLVCommonLog.exception(throwable);
                if (viewRef != null && viewRef.get() != null) {
                    viewRef.get().showPointRewardEnable(false);
                }
            }
        });
    }

    @Override
    public void makeGiftPointReward(int goodId, int goodNum) {
        if (viewRef == null || viewRef.get() == null) {
            return;
        }
        rewardManager.makePointReward(currentChannel, goodId, goodNum, currentUser.getViewerId(),
                currentUser.getViewerName(), currentUser.getViewerAvatar(), new IPLVPointRewardDataSource.IPointRewardListener<String>() {
                    @Override
                    public void onSuccess(String point) {
                        if (viewRef != null && viewRef.get() != null) {
                            viewRef.get().updateRemainingPoint(point);
                        }
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        PLVToast.Builder.create().setText(createRewardErrorMessageFromException(throwable)).show();
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void makeGiftCashReward(int goodId, int goodNum, String sessionId) {
        if (viewRef == null || viewRef.get() == null) {
            return;
        }

        rewardManager.makeGiftCashReward(currentChannel, goodId, goodNum, currentUser.getViewerId(),
                currentUser.getViewerName(), currentUser.getViewerAvatar(), sessionId, new IPLVPointRewardDataSource.IPointRewardListener<PLVDonateGoodResponseVO>() {
                    @Override
                    public void onSuccess(PLVDonateGoodResponseVO vo) {

                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        PLVToast.Builder.create().setText(createRewardErrorMessageFromException(throwable)).show();
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void getRemainingRewardPoint() {
        if (viewRef == null || viewRef.get() == null) {
            return;
        }
        rewardManager.getRemainingRewardPoint(currentChannel, currentUser.getViewerId(), currentUser.getViewerName(),
                new IPLVPointRewardDataSource.IPointRewardListener<String>() {
                    @Override
                    public void onSuccess(String point) {
                        if (viewRef != null && viewRef.get() != null) {
                            viewRef.get().updateRemainingPoint(point);
                        }
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        PLVToast.Builder.create().setText(createRewardErrorMessageFromException(throwable)).show();
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void destroy() {
        if (viewRef != null && viewRef.get() != null) {
            viewRef.get().destroy();
        }
        rewardManager.destroy();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="现金打赏">
    private void handleCashReward(PLVRewardSettingVO.CashDonateDTO plvRewardSettingVO) {

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="礼物打赏">

    private void handleGiftCashReward(PLVRewardSettingVO.GiftDonateDTO cashPays, String cashUnit, boolean isFreeDonate) {
        //只有enable的道具才显示在打赏弹窗
        List<PLVRewardSettingVO.GiftDonateDTO.GiftDetailDTO> enabledGoods = new ArrayList<>();
        int index = 1;
        for (PLVRewardSettingVO.GiftDonateDTO.GiftDetailDTO good : cashPays.getCashPays()) {
            if ("Y".equals(good.getEnabled())) {
                good.setGoodId(index++);
                if (good.isFree()) {
                    //只显示免费的礼物
                    enabledGoods.add(good);
                }
            }
        }

        cashPays.setCashPays(enabledGoods);
    }

    private void handleGiftPointReward(PLVRewardSettingVO.GiftDonateDTO pointPays, String pointUnit) {
        //只有enable的道具才显示在打赏弹窗
        List<PLVRewardSettingVO.GiftDonateDTO.GiftDetailDTO> enabledGoods = new ArrayList<>();
        int index = 1;
        for (PLVRewardSettingVO.GiftDonateDTO.GiftDetailDTO good : pointPays.getPointPays()) {
            if ("Y".equals(good.getEnabled())) {
                good.setGoodId(index++);
                enabledGoods.add(good);
            }
        }

        pointPays.setPointPays(enabledGoods);


    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 获取错误信息">

    private static String createRewardErrorMessageFromException(Throwable e) {
        if (!(e instanceof HttpException)) {
            return e.getMessage();
        }
        final HttpException exception = (HttpException) e;
        try {
            if (exception.response().errorBody() == null) {
                return "";
            }
            String errorBodyString = exception.response().errorBody().string();
            PLVResponseApiBean bean = PLVGsonUtil.fromJson(PLVResponseApiBean.class, errorBodyString);
            if (bean == null) {
                return "";
            }
            if (bean.getCode() == 400) {
                return bean.getMessage();
            } else {
                return PLVAppUtils.getString(R.string.plv_reward_make_fail);
            }
        } catch (Exception ex) {
            PLVCommonLog.e(TAG, "createErrorMessageFromException:" + ex.getMessage());
            return "消息解析错误";
        }
    }

    // </editor-fold>

}
