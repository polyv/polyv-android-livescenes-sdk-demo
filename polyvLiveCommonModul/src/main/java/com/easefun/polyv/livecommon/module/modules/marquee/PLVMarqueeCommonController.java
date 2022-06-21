package com.easefun.polyv.livecommon.module.modules.marquee;

import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.livecommon.module.modules.marquee.model.PLVMarqueeAnimationVO;
import com.easefun.polyv.livecommon.module.modules.marquee.model.PLVMarqueeModel;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.livescenes.marquee.PLVMarqueeSDKController;

import org.json.JSONObject;

/**
 * 对跑马灯 的控制操作类
 */
public class PLVMarqueeCommonController {

    // <editor-fold desc="变量">
    private static final String TAG = "PolyvMarqueeController";

    private static volatile PLVMarqueeCommonController instance = null;
    private String code = "";
    private String errorMsg = "跑马灯验证失败";
    // </editor-fold>

    // <editor-fold desc="单例">
    private PLVMarqueeCommonController() {

    }

    public static PLVMarqueeCommonController getInstance() {
        if (instance == null) {
            synchronized (PLVMarqueeCommonController.class) {
                if (instance == null) {
                    instance = new PLVMarqueeCommonController();
                }
            }
        }
        return instance;
    }
    // </editor-fold>

    // <editor-fold desc="对外API - 跑马灯功能设置">

    /**
     * 设置code，用于自定义url的code值传递
     *
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 验证跑马灯失败后的消息提示
     */
    public String getErrorMessage() {
        return errorMsg;
    }


    /**
     * 根据从channelJson 中获取的MarqueeVO，直接更新跑马灯样式
     */
    public void updateMarqueeView(PolyvLiveMarqueeVO channelJsonMarqueeVO,
                                  String nickName,
                                  IPLVMarqueeControllerCallback callback) {
        PLVMarqueeModel marqueeModel = new PLVMarqueeModel();
        marqueeModel.setAlwaysShowWhenRun(true)
                .setHiddenWhenPause(false);
        int defaultFontAlpha = 0;
        int defaultFontSize = 0;
        int defaultFontColor = 0;
        int defaultSetting = PLVMarqueeAnimationVO.ROLL;

        if (!channelJsonMarqueeVO.marqueeType.equals(PolyvLiveMarqueeVO.MARQUEETYPE_DIYURL)) {
            defaultFontAlpha = (int) (255 * PLVFormatUtils.parseFloat(channelJsonMarqueeVO.marqueeOpacity.replace("%", "")) * 0.01F);
            defaultFontSize = Math.min(channelJsonMarqueeVO.marqueeFontSize, 66);
            defaultFontColor = PLVFormatUtils.parseColor(channelJsonMarqueeVO.marqueeFontColor);
            defaultSetting = PLVFormatUtils.integerValueOf(channelJsonMarqueeVO.marqueeSetting, PLVMarqueeAnimationVO.ROLL);
        }

        switch (channelJsonMarqueeVO.marqueeType) {
            case PolyvLiveMarqueeVO.MARQUEETYPE_FIXED:
                setDefaultMarqueeParams(marqueeModel, channelJsonMarqueeVO.marquee,
                        defaultFontSize, defaultFontColor, defaultFontAlpha,
                        defaultSetting, callback);
                break;
            case PolyvLiveMarqueeVO.MARQUEETYPE_NICKNAME:
                marqueeModel.setInterval(0);
                marqueeModel.setLifeTime(1);
                marqueeModel.setTweenTime(0);
                setDefaultMarqueeParams(marqueeModel, nickName,
                        defaultFontSize, defaultFontColor, defaultFontAlpha,
                        defaultSetting, callback);
                break;
            case PolyvLiveMarqueeVO.MARQUEETYPE_DIYURL:
                setDiyUrlMarqueeParams(marqueeModel,
                        channelJsonMarqueeVO.marquee,
                        channelJsonMarqueeVO.getChannelId(),
                        channelJsonMarqueeVO.getUserId(), code, callback);
                break;
            default:
                break;
        }
    }
    // </editor-fold>

    // <editor-fold desc="模块功能">

    /**
     * 第一种、第二种方式设置的跑马灯样式
     *
     * @param marqueeModel
     * @param content
     * @param fontSize
     * @param fontColor
     * @param fontAlpha
     * @param callback
     */
    private void setDefaultMarqueeParams(PLVMarqueeModel marqueeModel,
                                         String content, int fontSize,
                                         int fontColor, int fontAlpha,
                                         int setting, IPLVMarqueeControllerCallback callback) {
        marqueeModel.setUserName(content)
                .setFontSize(fontSize)
                .setFontColor(fontColor)
                .setFontAlpha(fontAlpha)
                .setFilter(false)
                .setSetting(setting)
                .setSpeed(100);
        if (callback != null) {
            callback.onMarqueeModel(PLVMarqueeSDKController.ALLOW_PLAY_MARQUEE, marqueeModel);
        }
    }

    /**
     * 第三种方式设置的跑马灯样式
     *
     * @param marqueeModel
     * @param channelId
     * @param userId
     * @param code
     */
    private void setDiyUrlMarqueeParams(final PLVMarqueeModel marqueeModel, String url,
                                        String channelId, String userId, String code,
                                        final IPLVMarqueeControllerCallback callback) {
        PLVMarqueeSDKController.setMarqueeParams(url, channelId, userId, code,
                new PLVMarqueeSDKController.IPLVMarqueeRequestByDiyUrlCallback() {
                    @Override
                    public void onParams(@PLVMarqueeSDKController.MARQUEE_CONTROLLER_TIP int controllerTip,
                                         JSONObject jsonObject) {
                        if (marqueeModel != null) {
                            marqueeModel.setParamsByJsonObject(jsonObject);
                        }

                        if (callback != null) {
                            callback.onMarqueeModel(controllerTip, marqueeModel);
                        }
                    }
                });
    }


    // </editor-fold>

    // <editor-fold desc="监听接口回调的类">
    public interface IPLVMarqueeControllerCallback {
        void onMarqueeModel(@PLVMarqueeSDKController.MARQUEE_CONTROLLER_TIP int controllerTip, PLVMarqueeModel marqueeModel);
    }
    // </editor-fold>
}
