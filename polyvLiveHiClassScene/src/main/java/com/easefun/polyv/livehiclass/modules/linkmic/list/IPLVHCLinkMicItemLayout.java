package com.easefun.polyv.livehiclass.modules.linkmic.list;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livehiclass.modules.linkmic.list.item.IPLVHCLinkMicItem;
import com.plv.socket.event.linkmic.PLVRemoveMicSiteEvent;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

import java.util.List;

/**
 * 连麦item布局的接口定义
 */
public interface IPLVHCLinkMicItemLayout {

    /**
     * 绑定数据
     *
     * @param dataBeanList  数据列表
     * @param isJoinDiscuss 是否加入了分组讨论
     */
    void bindData(List<PLVLinkMicItemDataBean> dataBeanList, boolean isJoinDiscuss);

    /**
     * 清除数据
     *
     * @param isJoinDiscuss 是否加入讨论，true：加入,false：离开
     */
    void clearData(boolean isJoinDiscuss);

    /**
     * 设置占位Item
     *
     * @param placeLinkMicItem   占位数据
     * @param isTeacherPreparing 是否讲师准备中
     */
    void setPlaceLinkMicItem(PLVLinkMicItemDataBean placeLinkMicItem, boolean isTeacherPreparing);

    /**
     * 更新占位Item的昵称
     *
     * @param nick 昵称
     */
    void updatePlaceLinkMicItemNick(String nick);

    /**
     * 获取数据
     *
     * @return 数据
     */
    List<PLVLinkMicItemDataBean> getDataBeanList();

    /**
     * 用户加入
     *
     * @param dataBean 数据
     * @param position 索引
     */
    void onUserJoin(PLVLinkMicItemDataBean dataBean, int position);

    /**
     * 用户离开
     *
     * @param dataBean 数据
     * @param position 索引
     */
    void onUserLeave(PLVLinkMicItemDataBean dataBean, int position);

    /**
     * 用户已存在
     *
     * @param dataBean 数据
     * @param position 索引
     */
    void onUserExisted(PLVLinkMicItemDataBean dataBean, int position);

    /**
     * 更新列表数据
     *
     * @param dataBeanList 数据列表
     */
    void updateListData(List<PLVLinkMicItemDataBean> dataBeanList);

    /**
     * 通知rtc频道重连
     */
    void notifyRejoinRoom();

    /**
     * 更新关闭视频
     */
    void updateUserMuteVideo(int position);

    /**
     * 更新关闭音频
     */
    void updateUserMuteAudio(int position);

    /**
     * 更新音量变化
     */
    void updateVolumeChanged();

    /**
     * 用户举手变化
     */
    void onUserRaiseHand(int position);

    /**
     * 用户获得奖杯变化
     */
    void onUserGetCup(int position);

    /**
     * 用户画笔授权变化
     */
    void onUserHasPaint(int position);

    /**
     * 用户组长变化
     *
     * @param leaderId 组长Id
     */
    void onUserHasLeader(String leaderId);

    /**
     * 用户摄像头放大位置更新
     */
    void onUserUpdateZoom(PLVUpdateMicSiteEvent updateMicSiteEvent);

    /**
     * 用户画面从摄像头放大区域移出
     */
    void onUserRemoveZoom(PLVRemoveMicSiteEvent removeMicSiteEvent);

    /**
     * 设置renderView监听回调
     *
     * @param onRenderViewCallback 监听回调
     */
    void setOnRenderViewCallback(IPLVHCLinkMicItem.OnRenderViewCallback onRenderViewCallback);

    /**
     * 设置view交互事件监听器
     *
     * @param onViewActionListener 监听器
     */
    void setOnViewActionListener(OnViewActionListener onViewActionListener);

    /**
     * 销毁方法
     */
    void destroy();

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 点击item
         *
         * @param position    索引
         * @param linkMicItem item
         */
        void onClickItemView(int position, IPLVHCLinkMicItem linkMicItem);
    }
}
