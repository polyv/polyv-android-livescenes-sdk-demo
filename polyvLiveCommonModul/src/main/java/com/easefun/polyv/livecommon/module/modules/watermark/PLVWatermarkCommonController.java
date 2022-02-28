package com.easefun.polyv.livecommon.module.modules.watermark;

import com.plv.business.model.video.PLVWatermarkVO;
import com.plv.foundationsdk.log.PLVCommonLog;

/**
 * author: fangfengrui
 * date: 2021/12/27
 */
public class PLVWatermarkCommonController {
    //<editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVWatermarkCommonController: ";
    private static volatile PLVWatermarkCommonController instance = null;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="单例">
    private PLVWatermarkCommonController() {

    }

    public static PLVWatermarkCommonController getInstance() {
        if (instance == null) {
            synchronized (PLVWatermarkCommonController.class) {
                if (instance == null) {
                    instance = new PLVWatermarkCommonController();
                }
            }
        }
        return instance;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="转换工具方法">
    private int stringToInt(String fontSize) {
        switch (fontSize) {
            case "large":
                return 60;
            case "middle":
                return 45;
            case "small":
                return 30;
            default:
                return 0;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="对外API-水印设置">

    /**
     * 直接从channeljson中获取水印的更新
     *
     * @param channelJsonWaterMarkVO channelJson中的watermark参数
     * @param nickname               昵称
     */
    public void updateWatermarkView(PLVWatermarkVO channelJsonWaterMarkVO,
                                    String nickname) {
        PLVWatermarkTextVO plvWatermarkTextVO = new PLVWatermarkTextVO();

        switch (channelJsonWaterMarkVO.watermarkType) {
            case PLVWatermarkVO.WATERMARK_TYPE_FIXED:
                setDefaultWatermarkParam(plvWatermarkTextVO,
                        channelJsonWaterMarkVO.watermarkContent,
                        channelJsonWaterMarkVO.watermarkFontSize,
                        channelJsonWaterMarkVO.watermarkOpacity);
                break;
            case PLVWatermarkVO.WATERMARK_TYPE_NICKNAME:
                setDefaultWatermarkParam(plvWatermarkTextVO,
                        nickname,
                        channelJsonWaterMarkVO.watermarkFontSize,
                        channelJsonWaterMarkVO.watermarkOpacity);
                break;
            default:
                PLVCommonLog.d(TAG,"channelJsonWaterMarkVO.watermarkType 类别出错");
                break;
        }

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="模块功能">

    //设置水印，又两种类型，nickname和fixed
    public void setDefaultWatermarkParam(PLVWatermarkTextVO plvWatermarkTextVO,
                                         String content,
                                         String fontSize,
                                         String fontAlpha) {
        plvWatermarkTextVO.setContent(content)
                .setFontSize(fontSize)
                .setFontAlpha(fontAlpha);
    }
    //</editor-fold>

}
