package com.easefun.polyv.livehiclass.modules.toolbar;

import android.content.Intent;
import android.view.View;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.easefun.polyv.livehiclass.modules.toolbar.enums.PLVHCMarkToolEnums;
import com.plv.livescenes.net.IPLVDataRequestListener;

/**
 * 工具栏布局的接口定义
 */
public interface IPLVHCToolBarLayout {

    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 处理图片选择结果
     *
     * @param data 数据
     */
    void handleImgSelectResult(Intent data);

    /**
     * 设置view交互事件监听器
     *
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 初始化默认的媒体状态
     *
     * @param isMuteAudio   是否禁用麦克风
     * @param isMuteVideo   是否禁用摄像头
     * @param isFrontCamera 是否是前置摄像头
     */
    void initDefaultMediaStatus(boolean isMuteAudio, boolean isMuteVideo, boolean isFrontCamera);

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
     * 用户获取组长权限
     *
     * @param isHasGroupLeader true：自己有组长权限，false：自己没有组长权限
     */
    void onUserHasGroupLeader(boolean isHasGroupLeader);

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
     * 组长请求帮助
     */
    void onLeaderRequestHelp();

    /**
     * 组长取消帮助
     */
    void onLeaderCancelHelp();

    /**
     * 调整布局
     */
    void adjustLayout();

    /**
     * 接收用户举手变化
     *
     * @param raiseHandCount 举手数量
     * @param isRaiseHand    是否举手
     */
    void acceptUserRaiseHand(int raiseHandCount, boolean isRaiseHand);

    /**
     * 接收我的画笔权限变化
     *
     * @param isHasPaint
     */
    void acceptHasPaintToMe(boolean isHasPaint);

    /**
     * 更新标注工具按钮状态
     *
     * @param showUndoButton   是否显示撤销按钮
     * @param showDeleteButton 是否显示删除按钮
     */
    void changeMarkToolState(boolean showUndoButton, boolean showDeleteButton);

    /**
     * 获取成员列表布局的连麦View
     *
     * @return linkMicView
     */
    IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView getMemberLayoutLinkMicView();

    /**
     * 获取设置布局的连麦View
     *
     * @return linkMicView
     */
    IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView getSettingLayoutLinkMicView();

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
         * 发送举手事件
         *
         * @param raiseHandTime 举手时间
         */
        void onSendRaiseHandEvent(int raiseHandTime);

        /**
         * 全屏控制
         *
         * @param isFullScreen true：全屏，false：退出全屏
         */
        void onFullScreenControl(boolean isFullScreen);

        /**
         * 初始化上课下课按钮后回调
         *
         * @param classImageView 上下课按钮
         */
        void onInitClassImageView(View classImageView);

        /**
         * 上课
         *
         * @param listener 监听器
         */
        void onStartLesson(IPLVDataRequestListener<String> listener);

        /**
         * 下课
         *
         * @param listener 监听器
         */
        void onStopLesson(IPLVDataRequestListener<String> listener);

        /**
         * 切换标注工具
         *
         * @param newMarkTool 新的工具
         */
        void onRequestChangeDocumentMarkTool(PLVHCMarkToolEnums.MarkTool newMarkTool);

        /**
         * 切换标注工具颜色
         *
         * @param newColor 新的颜色
         */
        void onRequestChangeDocumentColor(PLVHCMarkToolEnums.Color newColor);

        /**
         * 撤销画笔操作
         */
        void onRequestUndo();

        /**
         * 删除标注内容
         */
        void onRequestDelete();
    }
}
