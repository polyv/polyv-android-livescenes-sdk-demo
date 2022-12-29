package com.easefun.polyv.livecommon.module.data;

import android.support.annotation.NonNull;

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
        return new PLVInteractNativeAppParams()
                .setAppId(liveRoomDataManager.getConfig().getAccount().getAppId())
                .setAppSecret(liveRoomDataManager.getConfig().getAccount().getAppSecret())
                .setSessionId(liveRoomDataManager.getSessionId())
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
