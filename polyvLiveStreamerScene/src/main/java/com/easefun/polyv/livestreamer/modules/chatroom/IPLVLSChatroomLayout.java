package com.easefun.polyv.livestreamer.modules.chatroom;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;

/**
 * 聊天室布局的接口定义
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVLSChatroomLayout {

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法">
    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 设置view交互事件监听器
     *
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 添加聊天室在线人数变化监听器
     *
     * @param listener 监听器
     */
    void addOnOnlineCountListener(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 设置麦克风开关按钮状态
     *
     * @param isOpen true：打开，false：关闭
     */
    void setOpenMicViewStatus(boolean isOpen);

    /**
     * 设置摄像头开关按钮状态
     *
     * @param isOpen true：打开，false：关闭
     */
    void setOpenCameraViewStatus(boolean isOpen);

    /**
     * 设置前置摄像头按钮状态
     *
     * @param isFront true：前置，false：后置
     */
    void setFrontCameraViewStatus(boolean isFront);

    /**
     * 改变聊天室布局可见性
     *
     * @param visibility 可见性
     */
    void setVisibility(int visibility);

    /**
     * 是否拦截返回事件
     *
     * @return true：拦截，false：不拦截
     */
    boolean onBackPressed();

    /**
     * 销毁，释放资源
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器">
    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 麦克风控制
         *
         * @param isMute true：禁用，false：启用
         */
        boolean onMicControl(boolean isMute);

        /**
         * 摄像头控制
         *
         * @param isMute true：禁用，false：启用
         */
        boolean onCameraControl(boolean isMute);

        /**
         * 前置摄像头控制
         *
         * @param isFront true：前置，false：后置
         */
        boolean onFrontCameraControl(boolean isFront);
    }
    // </editor-fold>
}
