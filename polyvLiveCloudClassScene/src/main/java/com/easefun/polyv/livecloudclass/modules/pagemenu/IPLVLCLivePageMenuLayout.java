package com.easefun.polyv.livecloudclass.modules.pagemenu;

import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCChatCommonMessageList;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;

/**
 * 直播页面菜单布局的接口
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVLCLivePageMenuLayout {

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - 定义 页面菜单布局中 外部可以直接调用的方法">
    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 获取横竖屏共用的聊天信息列表
     *
     * @return 聊天信息列表
     */
    PLVLCChatCommonMessageList getChatCommonMessageList();

    /**
     * 获取聊天室presenter
     *
     * @return 聊天室presenter
     */
    IPLVChatroomContract.IChatroomPresenter getChatroomPresenter();

    /**
     * 设置view交互事件监听器
     *
     * @param listener
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 添加观看热度监听器
     *
     * @param listener 监听器
     */
    void addOnViewerCountListener(IPLVOnDataChangedListener<Long> listener);

    /**
     * 更新直播状态为直播中
     */
    void updateLiveStatusWithLive();

    /**
     * 更新直播状态为未直播中
     */
    void updateLiveStatusWithNoLive();

    /**
     * 是否拦截返回事件，拦截的情况有：
     * 1.当前显示的tab是推广外链，并且其webView可以返回
     * 2.当前显示的tab是聊天，并且表情等布局是显示状态
     * 3.当前显示的tab是提问，并且表情等布局是显示状态
     * 4.当前显示的tab是介绍，并且其webView可以返回
     * 5.当前显示的tab是自定义图文菜单，并且器webView可以返回
     * 6.当前是在查看聊天信息列表里的图片大图
     *
     * @return true：拦截，false：不拦截
     */
    boolean onBackPressed();

    /**
     * 销毁
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器 - 定义 页面菜单布局中UI控件 触发的交互事件的回调方法">
    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 显示公告
         */
        void onShowBulletinAction();

        /**
         * 发送弹幕动作，在本地发送的聊天信息/接收到发言信息时需要发送弹幕
         *
         * @param message 弹幕信息
         */
        void onSendDanmuAction(CharSequence message);
    }
    // </editor-fold>
}
