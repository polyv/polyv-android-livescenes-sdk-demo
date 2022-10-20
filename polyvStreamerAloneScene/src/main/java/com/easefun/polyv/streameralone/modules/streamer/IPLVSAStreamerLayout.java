package com.easefun.polyv.streameralone.modules.streamer;

import android.util.Pair;
import android.view.MotionEvent;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;

/**
 * 推流和连麦布局的接口定义
 */
public interface IPLVSAStreamerLayout {

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
     * 设置相机方向
     *
     * @param front true：前置，false：后置
     */
    boolean setCameraDirection(boolean front);

    /**
     * 设置镜像
     *
     * @param isMirror true：镜像，false：取消
     */
    void setMirrorMode(boolean isMirror);

    /**
     * 缩放推流画面
     */
    void scaleStreamerView(PLVLinkMicItemDataBean itemDataBean, float scaleFactor);

    /**
     * 切换连麦布局类型
     */
    void changeLinkMicLayoutType();

    /**
     * 设置推流码率
     *
     * @param bitrate 码率
     */
    void setBitrate(@PLVSStreamerConfig.BitrateType int bitrate);

    /**
     * 获取推流的码率信息
     *
     * @return 码率信息<最大支持码率, 选择码率>
     */
    Pair<Integer, Integer> getBitrateInfo();

    /**
     * 获取网络质量
     *
     * @return 网络质量常量
     */
    int getNetworkQuality();

    /**
     * 添加因断网延迟20s断流的状态监听器
     *
     * @param listener 监听器
     */
    void addOnShowNetBrokenListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 添加用户请求连麦的监听器
     *
     * @param listener 监听器
     */
    void addOnUserRequestListener(IPLVOnDataChangedListener<String> listener);

    /**
     * 添加摄像头方向监听器
     *
     * @param listener 监听器
     */
    void addOnIsFrontCameraListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 添加前置摄像头镜像启用监听器
     *
     * @param listener 监听器
     */
    void addOnIsFrontMirrorModeListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 添加直播时长监听器
     *
     * @param listener 监听器
     */
    void addStreamerTimeListener(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 添加连麦人数监听器
     *
     * @param listener 监听器
     */
    void addLinkMicCountListener(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 开始直播
     */
    void startLive();

    /**
     * 暂停直播
     */
    void stopLive();

    /**
     * 进入直播间
     */
    void enterLive();

    /**
     * 清除全屏状态
     */
    void clearFullscreenState(PLVLinkMicItemDataBean linkmicItem);

    /**
     * 获取推流和连麦presenter
     *
     * @return streamerPresenter
     */
    IPLVStreamerContract.IStreamerPresenter getStreamerPresenter();

    /**
     * 是否拦截返回事件
     *
     * @return true：拦截，false：不拦截
     */
    boolean onBackPressed();

    /**
     * 传递触摸事件
     */
    boolean onRvSuperTouchEvent(MotionEvent ev);

    /**
     * 销毁
     */
    void destroy();

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 重新开播
         */
        void onRestartLiveAction();

        /**
         * 全屏
         */
        void onFullscreenAction(PLVLinkMicItemDataBean itemDataBean, PLVSwitchViewAnchorLayout switchItemView);
    }
}
