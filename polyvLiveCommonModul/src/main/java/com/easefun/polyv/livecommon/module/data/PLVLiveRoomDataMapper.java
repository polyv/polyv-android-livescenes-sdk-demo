package com.easefun.polyv.livecommon.module.data;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
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
        return new PLVInteractNativeAppParams()
                .setAppId(liveRoomDataManager.getConfig().getAccount().getAppId())
                .setAppSecret(liveRoomDataManager.getConfig().getAccount().getAppSecret())
                .setSessionId(liveRoomDataManager.getSessionId())
                .setLiveScene(PLVLiveScene.ECOMMERCE == liveScene ? PLVInteractNativeAppParams.LIVE_SCENE_LIVE_ECOMMERCE : PLVInteractNativeAppParams.LIVE_SCENE_CLOUD_CLASS)
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
