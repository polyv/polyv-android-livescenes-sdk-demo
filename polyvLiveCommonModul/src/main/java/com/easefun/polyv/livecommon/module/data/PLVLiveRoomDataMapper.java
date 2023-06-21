package com.easefun.polyv.livecommon.module.data;

import android.support.annotation.NonNull;

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
                );
    }

}
