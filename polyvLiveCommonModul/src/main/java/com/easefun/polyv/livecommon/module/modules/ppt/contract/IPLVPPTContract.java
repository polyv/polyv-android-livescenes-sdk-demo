package com.easefun.polyv.livecommon.module.modules.ppt.contract;

/**
 * date: 2020/9/16
 * author: HWilliamgo
 * description: PPT业务MVP
 */
public interface IPLVPPTContract {
    /**
     * PPTView
     */
    interface IPLVPPTView {

        /**
         * 发送消息到webView
         *
         * @param msg 消息
         */
        void sendMsgToWebView(String msg);

        /**
         * 发送消息到webView
         *
         * @param msg   消息
         * @param event 事件
         */
        void sendMsgToWebView(String msg, String event);

        /**
         * 隐藏加载中图片
         * 直播时，则聊天室登录后，收到PPT控制消息时，隐藏加载中图片。
         * 回放时，在收到PPT的prepare回调后，异常加载中图片。
         */
        void hideLoading();

        /**
         * 切换PPT View的位置
         *
         * @param toMainScreen true表示切换到主屏幕，false表示切回到悬浮窗
         */
        void switchPPTViewLocation(boolean toMainScreen);

    }

    /**
     * PPT Presenter
     */
    interface IPLVPPTPresenter {
        /**
         * 初始化
         *
         * @param view view
         */
        void init(IPLVPPTView view);

        /**
         * 移除消息延迟时间
         */
        void removeMsgDelayTime();

        /**
         * 恢复消息延迟时间
         */
        void recoverMsgDelayTime();

        /**
         * 发送画笔消息
         */
        void sendPPTBrushMsg(String msg);

        /**
         * 销毁
         */
        void destroy();
    }
}
