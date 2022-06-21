package com.easefun.polyv.livehiclass.modules.chatroom;

import android.content.Intent;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;

/**
 * 聊天室布局的接口定义
 */
public interface IPLVHCChatroomLayout {
    int REQUEST_CODE_SELECT_IMG = 0x01;//选择图片请求标志

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
     * 获取聊天室presenter
     *
     * @return 聊天室presenter
     */
    IPLVChatroomContract.IChatroomPresenter getChatroomPresenter();

    /**
     * 课节准备中
     */
    void onLessonPreparing(long serverTime, long lessonStartTime);

    /**
     * 课节开始
     */
    void onLessonStarted();

    /**
     * 课节结束
     */
    void onLessonEnd(long inClassTime);

    /**
     * 加入讨论
     *
     * @param groupId 分组Id
     */
    void onJoinDiscuss(String groupId);

    /**
     * 离开讨论
     */
    void onLeaveDiscuss();

    /**
     * 处理图片选择结果
     *
     * @param data 数据
     */
    void handleImgSelectResult(Intent data);

    /**
     * 显示布局
     */
    void show(int viewWidth, int viewHeight, int[] viewLocation);

    /**
     * 隐藏
     */
    void hide();

    /**
     * 是否显示状态
     *
     * @return true：显示，false：不显示
     */
    boolean isShown();

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

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 可见性改变回调
         *
         * @param isVisible true：显示，false：隐藏
         */
        void onVisibilityChanged(boolean isVisible);

        /**
         * 未读信息数改变
         *
         * @param currentUnreadCount 未读信息数
         */
        void onUnreadMsgCountChanged(int currentUnreadCount);
    }
}
