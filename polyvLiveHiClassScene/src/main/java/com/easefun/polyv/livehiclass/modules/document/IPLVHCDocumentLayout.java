package com.easefun.polyv.livehiclass.modules.document;

import android.content.Intent;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livehiclass.modules.toolbar.enums.PLVHCMarkToolEnums;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;

/**
 * @author suhongtao
 */
public interface IPLVHCDocumentLayout {

    /**
     * 初始化方法
     *
     * @param liveRoomDataManager
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 上传选择的文件
     *
     * @param intent
     */
    void onSelectUploadDocument(Intent intent);

    /**
     * 修改标注工具类型
     *
     * @param markTool
     */
    void changeMarkTool(PLVHCMarkToolEnums.MarkTool markTool);

    /**
     * 修改标注工具颜色
     *
     * @param color
     */
    void changeColor(PLVHCMarkToolEnums.Color color);

    /**
     * 画笔操作撤销
     */
    void operateUndo();

    /**
     * 画笔删除
     */
    void operateDelete();

    /**
     * 接收我的画笔权限变化
     *
     * @param isHasPaint
     */
    void acceptHasPaintToMe(boolean isHasPaint);

    /**
     * 用户获取组长权限
     *
     * @param isHasGroupLeader true：自己有组长权限，false：自己没有组长权限
     */
    void onUserHasGroupLeader(boolean isHasGroupLeader);

    /**
     * 加入讨论
     */
    void onJoinDiscuss(PLVSwitchRoomEvent switchRoomEvent);

    /**
     * 离开讨论
     */
    void onLeaveDiscuss(PLVSwitchRoomEvent switchRoomEvent);

    /**
     * 设置view交互事件监听器
     *
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {

        /**
         * 回调标注工具按钮状态变更
         *
         * @param showUndoButton   是否显示撤销按钮
         * @param showDeleteButton 是否显示删除按钮
         */
        void onChangeMarkToolOperationButtonState(boolean showUndoButton,
                                                  boolean showDeleteButton);

    }

}
