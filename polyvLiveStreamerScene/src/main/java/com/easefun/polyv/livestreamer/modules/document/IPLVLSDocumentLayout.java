package com.easefun.polyv.livestreamer.modules.document;

import android.content.Intent;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livestreamer.modules.document.widget.PLVLSDocumentControllerExpandMenu;

/**
 * @author suhongtao
 */
public interface IPLVLSDocumentLayout {

    /**
     * 必须调用，初始化方法
     *
     * @param liveRoomDataManager
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 是否全屏模式
     *
     * @return
     */
    boolean isFullScreen();

    /**
     * 选择需要上传的文档
     *
     * @param intent onActivityResult回调intent
     */
    void onSelectUploadDocument(Intent intent);

    /**
     * 设置推流状态
     *
     * @param isStartedStatus 是否正在推流
     */
    void setStreamerStatus(boolean isStartedStatus);

    /**
     * 返回按键处理
     *
     * @return consume
     */
    boolean onBackPressed();

    /**
     * 销毁方法
     */
    void destroy();

    /**
     * 设置标注工具栏 折起/展开 回调
     *
     * @param onFoldExpandListener
     */
    void setMarkToolOnFoldExpandListener(PLVLSDocumentControllerExpandMenu.OnFoldExpandListener onFoldExpandListener);

    /**
     * 设置文档布局区域 全屏/正常 回调
     *
     * @param onSwitchFullScreenListener
     */
    void setOnSwitchFullScreenListener(PLVLSDocumentLayout.OnSwitchFullScreenListener onSwitchFullScreenListener);
}
