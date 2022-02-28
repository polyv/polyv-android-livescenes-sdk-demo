package com.easefun.polyv.livehiclass.modules.linkmic.list.item;

import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

/**
 * 连麦画面item接口
 *
 * @author suhongtao
 */
public interface IPLVHCLinkMicItem {

    /**
     * 初始化方法
     *
     * @param isLargeLayout 是否大尺寸布局
     * @param callback      渲染器相关回调注册
     */
    void init(final boolean isLargeLayout, IPLVHCLinkMicItem.OnRenderViewCallback callback);

    /**
     * 绑定数据
     *
     * @param linkMicItemDataBean 连麦数据对象
     */
    void bindData(PLVLinkMicItemDataBean linkMicItemDataBean);

    /**
     * 获取连麦数据对象
     */
    @Nullable
    PLVLinkMicItemDataBean getLinkMicItemDataBean();

    /**
     * 获取连麦id
     */
    @Nullable
    String getLinkMicId();

    /**
     * 找到{@link PLVHCLinkMicItemContainer}类型容器item
     */
    @Nullable
    PLVHCLinkMicItemContainer findContainerParent();

    /**
     * item之间交换位置
     */
    void switchWithItemView(IPLVHCLinkMicItem linkMicItemView);

    /**
     * this移动到参数指定的item位置
     */
    void moveToItemView(IPLVHCLinkMicItem linkMicItemView);

    /**
     * 移除item的视图
     */
    View removeItemView();

    /**
     * 添加item的视图
     */
    void addItemView(View rootView);

    /**
     * 释放渲染器
     */
    void releaseRenderView();

    /**
     * 从视图层级中移除渲染器，同时也会释放渲染器
     */
    void removeRenderView();

    /**
     * 配置渲染器
     */
    void setupRenderView();

    /**
     * 更新讲师准备状态
     *
     * @param isPreparing 是否正在准备中
     */
    void updateTeacherPreparingStatus(boolean isPreparing);

    /**
     * 更新分组组长状态
     *
     * @param isHasLeader 是否组长
     */
    void updateLeaderStatus(boolean isHasLeader);

    /**
     * 更新视频状态
     */
    void updateVideoStatus();

    /**
     * 更新音频状态
     */
    void updateAudioStatus();

    /**
     * 更新举手状态
     */
    void updateHandsUp();

    /**
     * 更新画笔状态
     */
    void updateHasPaint();

    /**
     * 更新奖杯数
     */
    void updateCupNum();

    /**
     * 更新摄像头放大状态
     *
     * @param updateMicSiteEvent 摄像头画面放大事件
     */
    void updateZoom(PLVUpdateMicSiteEvent updateMicSiteEvent);

    /**
     * 设置可见性
     *
     * @param visibility
     */
    void setVisibility(int visibility);

    /**
     * @see android.view.ViewGroup#addView(View)
     */
    void addView(View view);

    /**
     * @see android.view.ViewGroup#removeView(View)
     */
    void removeView(View view);

    /**
     * @see View#setOnClickListener(View.OnClickListener)
     */
    void setOnClickListener(View.OnClickListener listener);

    /**
     * 渲染器回调
     */
    interface OnRenderViewCallback {
        /**
         * 创建连麦列表渲染器。
         * 该渲染器必须通过多场景连麦SDK创建，不能直接构造。
         *
         * @return 渲染器
         */
        View createLinkMicRenderView();

        /**
         * 释放渲染器
         *
         * @param renderView 渲染器
         */
        void releaseLinkMicRenderView(View renderView);

        /**
         * 安装RenderView。
         * 将创建好的RenderView与连麦ID关联，并设置到SDK
         *
         * @param renderView 渲染器
         * @param linkMicId  连麦ID
         * @param streamType 流类型
         */
        void setupRenderView(View renderView, String linkMicId, int streamType);
    }

}
