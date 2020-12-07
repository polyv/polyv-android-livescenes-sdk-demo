package com.easefun.polyv.livecommon.module.modules.socket;

import android.support.annotation.NonNull;

import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;

/**
 * socket登录管理器的接口，聊天、连麦、PPT、互动等功能都依赖于socket通信
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVSocketLoginManager {
    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - 定义了 socket登录管理器 外部可以直接调用的方法">

    /**
     * 初始化
     */
    void init();

    /**
     * 设置socket事件监听器
     */
    void setOnSocketEventListener(OnSocketEventListener listener);

    /**
     * 是否允许使用分房间功能，需要在登录前设置，默认为false。直播带货场景需设置为true。<br/>
     * false：不管后台是否开启分房间功能，都不使用分房间功能。<br/>
     * true：并且后台也开启分房间功能，才会使用分房间功能。
     */
    void setAllowChildRoom(boolean allow);

    /**
     * 登录
     */
    void login();

    /**
     * 断开连接，断开后可以再次登录
     */
    void disconnect();

    /**
     * 销毁，断开连接并销毁所有socket信息的监听器
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器 - 定义了 socket登录管理器 触发的交互事件的回调方法">

    /**
     * socket事件监听器
     */
    interface OnSocketEventListener {
        /**
         * 登录/重连中
         *
         * @param isReconnect true：重连中，false：登录中
         */
        void handleLoginIng(boolean isReconnect);

        /**
         * 登录/重连成功
         *
         * @param isReconnect true：重连成功，false：登录成功
         */
        void handleLoginSuccess(boolean isReconnect);

        /**
         * 登录失败
         *
         * @param throwable 异常
         */
        void handleLoginFailed(@NonNull Throwable throwable);

        /**
         * 用户被踢事件
         *
         * @param kickEvent 踢人事件
         * @param isOwn     是否是自己被踢
         */
        void onKickEvent(@NonNull PLVKickEvent kickEvent, boolean isOwn);

        /**
         * 自己由于被踢后的登录被拒事件
         */
        void onLoginRefuseEvent(@NonNull PLVLoginRefuseEvent loginRefuseEvent);

        /**
         * 自己的重新登录事件
         */
        void onReloginEvent(@NonNull PLVReloginEvent reloginEvent);
    }
    // </editor-fold>
}
