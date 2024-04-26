package com.easefun.polyv.livecommon.module.data;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.google.gson.JsonElement;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;

/**
 * @author Hoshiiro
 */
public class PLVLiveRoomDataMapper {

    private PLVLiveRoomDataMapper() {
        throw new IllegalStateException("Utility class");
    }

    @NonNull
    public static PLVInteractNativeAppParams toInteractNativeAppParams(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        return toInteractNativeAppParams(liveRoomDataManager, PLVLiveScene.CLOUDCLASS);
    }

    @NonNull
    public static PLVInteractNativeAppParams toInteractNativeAppParams(@NonNull IPLVLiveRoomDataManager liveRoomDataManager, PLVLiveScene liveScene) {
        JsonElement promotionDataBean = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).get(PLVChannelFeature.LIVE_PROMOTION_DATA_BEAN);
        boolean isLive = false;
        //在回放中，即使是直播间在直播依然是返回false
        if (liveRoomDataManager.getClassDetailVO().getValue() != null && liveRoomDataManager.getConfig().isLive()) {
            if (liveRoomDataManager.getClassDetailVO().getValue().getData() != null) {
                if (liveRoomDataManager.getClassDetailVO().getValue().getData().getData() != null) {
                    isLive = liveRoomDataManager.getClassDetailVO().getValue().getData().getData().isLiveStatus();
                }
            }
        }
        return new PLVInteractNativeAppParams()
                .setAppId(liveRoomDataManager.getConfig().getAccount().getAppId())
                .setAppSecret(liveRoomDataManager.getConfig().getAccount().getAppSecret())
                .setAccountId(liveRoomDataManager.getConfig().getAccount().getUserId())
                .setSessionId(liveRoomDataManager.getSessionId())
                .setLiveScene(PLVLiveScene.ECOMMERCE == liveScene ? PLVInteractNativeAppParams.LIVE_SCENE_LIVE_ECOMMERCE : PLVInteractNativeAppParams.LIVE_SCENE_CLOUD_CLASS)
                .setPromotionInfo(promotionDataBean)
                .setChannelInfo(
                        new PLVInteractNativeAppParams.ChannelInfoDTO()
                                .setChannelId(liveRoomDataManager.getConfig().getChannelId())
                                .setRoomId(liveRoomDataManager.getConfig().getChannelId())
                )
                .setUserInfo(
                        new PLVInteractNativeAppParams.UserInfoDTO()
                                .setUserId(liveRoomDataManager.getConfig().getUser().getViewerId())
                                .setNick(liveRoomDataManager.getConfig().getUser().getViewerName())
                                .setPic(liveRoomDataManager.getConfig().getUser().getViewerAvatar())
                )
                .setIsLive(isLive);
    }

}
