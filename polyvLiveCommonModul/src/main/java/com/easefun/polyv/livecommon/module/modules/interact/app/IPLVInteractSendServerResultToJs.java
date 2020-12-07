package com.easefun.polyv.livecommon.module.modules.interact.app;

/**
 * date: 2020/9/2
 * author: HWilliamgo
 * description: 互动应用发送到server的结果再发送到JS的监听器
 */
public interface IPLVInteractSendServerResultToJs {
    /**
     * 将互动应用提交到server的结果也发送到JS，让JS显示提交成功或者提交失败
     *
     * @param msg 互动结果发送到server时，server返回的消息
     */
    void sendServerResultToJs(String msg);
}
