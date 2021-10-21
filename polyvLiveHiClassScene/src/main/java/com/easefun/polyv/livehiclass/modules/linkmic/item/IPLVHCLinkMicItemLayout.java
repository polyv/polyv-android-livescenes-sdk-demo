package com.easefun.polyv.livehiclass.modules.linkmic.item;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;

import java.util.List;

/**
 * 连麦item布局的接口定义
 */
public interface IPLVHCLinkMicItemLayout {

    /**
     * 绑定数据
     *
     * @param dataBeanList 数据列表
     */
    void bindData(List<PLVLinkMicItemDataBean> dataBeanList);

    /**
     * 设置占位Item
     *
     * @param placeLinkMicItem   占位数据
     * @param isTeacherPreparing 是否讲师准备中
     */
    void setPlaceLinkMicItem(PLVLinkMicItemDataBean placeLinkMicItem, boolean isTeacherPreparing);

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
     * 设置renderView监听回调
     *
     * @param onRenderViewCallback 监听回调
     */
    void setOnRenderViewCallback(PLVHCLinkMicItemView.OnRenderViewCallback onRenderViewCallback);

    /**
     * 设置view交互事件监听器
     *
     * @param onViewActionListener 监听器
     */
    void setOnViewActionListener(OnViewActionListener onViewActionListener);

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 点击item
         *
         * @param position            索引
         * @param linkMicItemDataBean 连麦数据
         */
        void onClickItemView(int position, PLVLinkMicItemDataBean linkMicItemDataBean);
    }
}
