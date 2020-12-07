package com.easefun.polyv.livecommon.module.modules.socket;

import android.support.annotation.NonNull;

import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;

/**
 * socket事件监听器抽象类
 */
public abstract class PLVAbsOnSocketEventListener implements IPLVSocketLoginManager.OnSocketEventListener {
    @Override
    public void handleLoginIng(boolean isReconnect) {

    }

    @Override
    public void handleLoginSuccess(boolean isReconnect) {

    }

    @Override
    public void handleLoginFailed(@NonNull Throwable throwable) {

    }

    @Override
    public void onKickEvent(@NonNull PLVKickEvent kickEvent, boolean isOwn) {

    }

    @Override
    public void onLoginRefuseEvent(@NonNull PLVLoginRefuseEvent loginRefuseEvent) {

    }

    @Override
    public void onReloginEvent(@NonNull PLVReloginEvent reloginEvent) {

    }
}
