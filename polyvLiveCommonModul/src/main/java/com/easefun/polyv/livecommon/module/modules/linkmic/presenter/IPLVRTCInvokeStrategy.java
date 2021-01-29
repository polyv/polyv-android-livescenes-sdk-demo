package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;

/**
 * date: 2020/12/23
 * author: HWilliamgo
 * description:
 * RTC调用策略抽象。
 * RTC方法调用可以根据当前模式的不同来进行不同的行为。
 * 对于外部总共抽象为以下几组方法：
 * 1. 设置直播状态
 * 2. 设置连麦状态
 * 3. 状态获取
 * 4. 设置监听器
 * <p>
 * 该接口的实现有不同的行为，例如有不同的RTC引擎的调用组合，不同的事件回调时机等。
 * 例如可参考：
 * {@link PLVRTCWatchEnabledStrategy}
 * {@link PLVRTCWatchDisabledStrategy}
 */
public interface IPLVRTCInvokeStrategy {

    // <editor-fold defaultstate="collapsed" desc="1. 设置直播状态">

    /**
     * 设置直播结束
     */
    void setLiveEnd();

    /**
     * 设置直播开始
     */
    void setLiveStart();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2. 设置连麦状态">

    /**
     * 设置加入连麦
     */
    void setJoinLinkMic();

    /**
     * 设置离开连麦
     */
    void setLeaveLinkMic();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="3. 状态获取和设置">

    /**
     * 是否加入RTC频道
     *
     * @return true表示是，false表示否
     */
    boolean isJoinChannel();

    /**
     * 是否加入连麦
     *
     * @return true表示是，false表示否
     */
    boolean isJoinLinkMic();

    /**
     * 设置第一画面的连麦ID
     *
     * @param linkMicId 第一画面连麦ID
     */
    void setFirstScreenLinkMicId(String linkMicId);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="4. 设置监听器">

    /**
     * 设置加入频道前的回调
     *
     * @param li li
     */
    void setOnBeforeJoinChannelListener(OnBeforeJoinChannelListener li);

    /**
     * 设置离开连麦的回调
     *
     * @param li li
     */
    void setOnLeaveLinkMicListener(OnLeaveLinkMicListener li);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口声明">

    /**
     * 加入连麦监听器
     */
    interface OnJoinLinkMicListener {
        /**
         * 当加入连麦时发生回调。
         *
         * @param data 是自己的加入连麦后的joinSuccess数据，调用处应处理为连麦列表数据
         */
        void onJoinLinkMic(PLVLinkMicJoinSuccess data);
    }

    /**
     * 加入频道前的监听器
     */
    interface OnBeforeJoinChannelListener {
        /**
         * 加入频道前发生回调
         * 调用处可通过该回调知晓当前rtc将要加入频道
         *
         * @param linkMicListShowMode 连麦列表显示模式
         */
        void onBeforeJoinChannel(PLVLinkMicListShowMode linkMicListShowMode);
    }

    /**
     * 离开连麦的回调
     */
    interface OnLeaveLinkMicListener {
        /**
         * 离开连麦
         */
        void onLeaveLinkMic();
    }
    // </editor-fold>

}
