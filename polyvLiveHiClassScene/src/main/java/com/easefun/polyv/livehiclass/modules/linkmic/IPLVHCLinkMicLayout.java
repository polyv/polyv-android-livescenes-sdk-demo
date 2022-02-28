package com.easefun.polyv.livehiclass.modules.linkmic;

import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;
import com.plv.livescenes.net.IPLVDataRequestListener;

/**
 * 连麦布局的接口定义
 */
public interface IPLVHCLinkMicLayout {

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
     * 静音音频
     *
     * @param mute true表示静音，false表示打开
     */
    void muteAudio(boolean mute);

    /**
     * 禁用视频
     *
     * @param mute true表示禁用视频，false表示打开视频
     */
    void muteVideo(boolean mute);

    /**
     * 切换前后置摄像头方向
     */
    void switchCamera(boolean front);

    /**
     * 上课
     */
    void startLesson(IPLVDataRequestListener<String> listener);

    /**
     * 下课
     */
    void stopLesson(IPLVDataRequestListener<String> listener);

    /**
     * 发送举手事件
     *
     * @param raiseHandTime 举手时间
     */
    void sendRaiseHandEvent(int raiseHandTime);

    /**
     * 设置布局的可见性
     *
     * @param visibility One of {@link android.view.View#VISIBLE}, {@link android.view.View#INVISIBLE}, or {@link android.view.View#GONE}.
     */
    void setVisibility(int visibility);

    /**
     * 是否是课节上课状态
     *
     * @return true：是课节上课状态，false：不是课节上课状态
     */
    boolean isLessonStarted();

    /**
     * 获取连麦Presenter
     *
     * @return linkMicPresenter
     */
    IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter getLinkMicPresenter();

    /**
     * 销毁，释放资源
     */
    void destroy();

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 布局大小变化
         */
        void onLayoutSizeChanged();

        /**
         * 用户举手变化
         *
         * @param raiseHandCount 举手数量
         * @param isRaiseHand    是否举手
         */
        void onUserRaiseHand(int raiseHandCount, boolean isRaiseHand);

        /**
         * 我的画笔权限变化
         *
         * @param isHasPaint 是否有画笔权限
         */
        void onHasPaintToMe(boolean isHasPaint);

        /**
         * 设置连麦renderView
         */
        void onSetupLinkMicRenderView(View renderView, String linkMicId, int streamType);

        /**
         * 学生获取奖杯
         *
         * @param userName 用户名称
         */
        void onGetCup(String userName);

        /**
         * 网络质量回调
         *
         * @param networkQuality {@link com.plv.linkmic.PLVLinkMicConstant.NetQuality}
         */
        void onNetworkQuality(int networkQuality);

        /**
         * 上行流量网络状态
         *
         * @param networkStatusVO
         */
        void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO);

        /**
         * 远端连麦用户网络状态
         *
         * @param networkStatusVO
         */
        void onRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO);

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
        void onLessonEnd(long inClassTime, boolean isTeacherType, boolean hasNextClass);

        /**
         * 用户获取组长权限
         *
         * @param isHasGroupLeader true：自己有组长权限，false：自己没有组长权限
         */
        void onUserHasGroupLeader(boolean isHasGroupLeader);

        /**
         * 加入讨论
         *
         * @param groupId         分组Id
         * @param groupName       分组名称
         * @param switchRoomEvent 切换房间事件
         */
        void onJoinDiscuss(String groupId, String groupName, @Nullable PLVSwitchRoomEvent switchRoomEvent);

        /**
         * 离开讨论
         */
        void onLeaveDiscuss(@Nullable PLVSwitchRoomEvent switchRoomEvent);

        /**
         * 组长请求帮助
         */
        void onLeaderRequestHelp();

        /**
         * 组长取消帮助
         */
        void onLeaderCancelHelp();
    }
}
