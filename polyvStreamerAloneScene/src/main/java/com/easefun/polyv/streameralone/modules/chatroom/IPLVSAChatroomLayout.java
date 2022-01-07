package com.easefun.polyv.streameralone.modules.chatroom;

import android.support.v7.widget.RecyclerView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.login.PLVLoginEvent;

import java.util.List;

/**
 * 聊天室布局的接口定义
 */
public interface IPLVSAChatroomLayout {

    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 聊天室登陆
     */
    void loginAndLoadHistory();

    /**
     * 呼出聊天信息输入窗口
     */
    void callInputWindow();

    /**
     * 添加信息到聊天列表中
     *
     * @param chatMessageDataList 信息
     * @param isScrollEnd         添加信息之后是否滚到底部
     */
    void addChatMessageToChatList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd);

    /**
     * 添加聊天室在线人数变化监听器
     *
     * @param listener 监听器
     */
    void addOnOnlineCountListener(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 添加聊天室用户登录事件监听器
     *
     * @param listener 监听器
     */
    void addOnLoginEventListener(IPLVOnDataChangedListener<PLVLoginEvent> listener);

    /**
     * 添加聊天室打赏事件监听器
     *
     * @param listener 监听器
     */
    void addOnRewardEventListener(IPLVOnDataChangedListener<PLVRewardEvent> listener);

    /**
     * 注册聊天数据观察者
     *
     * @param adapterDataObserver
     */
    void addObserverToChatMessageAdapter(RecyclerView.AdapterDataObserver adapterDataObserver);

    /**
     * 取消注册聊天数据观察者
     *
     * @param adapterDataObserver
     */
    void removeObserverFromChatMessageAdapter(RecyclerView.AdapterDataObserver adapterDataObserver);

    /**
     * 获取聊天数据列表大小
     *
     * @return 聊天数据列表大小
     */
    int getChatMessageListSize();

    /**
     * 是否拦截返回事件
     *
     * @return true：拦截，false：不拦截
     */
    boolean onBackPressed();

    /**
     * 销毁
     */
    void destroy();
}
