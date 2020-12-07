package com.easefun.polyv.livecommon.module.modules.linkmic.contract;

import android.content.Context;
import android.view.SurfaceView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;

import java.util.List;

/**
 * date: 2020/7/16
 * author: hwj
 * description: 连麦业务MVP模式的合约类
 */
public interface IPLVLinkMicContract {

    interface IPLVLinkMicView {

        /**
         * 响应连麦错误
         */
        void onLinkMicError(int errorCode, Throwable throwable);

        /**
         * 响应讲师开启连麦
         */
        void onTeacherOpenLinkMic();

        /**
         * 响应讲师关闭连麦
         */
        void onTeacherCloseLinkMic();

        /**
         * 响应讲师允许连麦
         */
        void onTeacherAllowJoin();

        /**
         * 响应加入连麦频道超时
         */
        void onAllowButJoinTimeout();

        /**
         * 响应在加入频道之前，View层应创建连麦适配器和初始化连麦布局
         *
         * @param linkMicUid  我的连麦Id
         * @param isAudio     是否是音频连麦
         * @param linkMicList 连麦列表。在实现中，要用该列表去渲染
         */
        void onBeforeJoinChannel(String linkMicUid, boolean isAudio, List<PLVLinkMicItemDataBean> linkMicList);

        /**
         * 响应加入连麦频道成功
         */
        void onJoinChannelSuccess();

        /**
         * 响应离开连麦频道
         *
         * @param shouldStartPlay 是否应该在离开连麦后播放视频
         */
        void onLeaveChannel(boolean shouldStartPlay);

        /**
         * 响应用户加入连麦频道
         *
         * @param uids
         */
        void onUsersJoin(List<String> uids);

        /**
         * 响应用户离开连麦频道
         *
         * @param uids
         */
        void onUsersLeave(List<String> uids);

        /**
         * 回调我当前不在连麦列表
         */
        void onNotInLinkMicList();

        /**
         * 响应用户开关视频
         *
         * @param uid  用户id
         * @param mute true表示关闭视频，false表示开启视频
         * @param pos  列表中的位置
         */
        void onUserMuteVideo(final String uid, final boolean mute, int pos);

        /**
         * 响应用户开关音频
         *
         * @param uid  用户id
         * @param mute true表示关闭音频，false表示开启音频
         * @param pos  列表中的位置
         */
        void onUserMuteAudio(final String uid, final boolean mute, int pos);

        /**
         * 响应本地用户麦克风音量变化
         */
        void onLocalUserMicVolumeChanged();

        /**
         * 响应远端用户麦克风音量变化
         */
        void onRemoteUserVolumeChanged(List<PLVLinkMicItemDataBean> linkMicList);

        /**
         * 切换第一画面
         *
         * @param linkMicId 新的第一画面的连麦Id
         */
        void onSwitchFirstScreen(String linkMicId);

        /**
         * 设置第一画面的连麦ID
         *
         * @param linkMicId 新的第一画面的连麦Id
         */
        void setFirstScreenLinkMicId(String linkMicId);

        /**
         * 切换PPT View的位置
         *
         * @param toMainScreen true表示切换到主屏幕，false表示切回到悬浮窗
         */
        void onSwitchPPTViewLocation(boolean toMainScreen);

        /**
         * PPT是否被切换到连麦列表了
         *
         * @return true表示PPT在连麦列表，false表示PPT不在连麦列表
         */
        boolean isPPTShowInLinkMicList();

        /**
         * 获取PPTView在连麦列表中的位置index
         *
         * @return ppt在连麦列表中的位置
         */
        int getPPTViewIndexInLinkMicList();

        /**
         * 点击连麦列表[index]位置上的画面
         *
         * @param index 连麦列表中的位置
         */
        void performClickInLinkMicListItem(int index);

        /**
         * 更新所有整个连麦列表
         */
        void updateAllLinkMicList();
    }

    interface IPLVLinkMicPresenter {

        /**
         * 销毁
         */
        void destroy();

        /**
         * 请求上麦
         */
        void requestJoinLinkMic();

        /**
         * 取消请求上麦
         */
        void cancelRequestJoinLinkMic();

        /**
         * 下麦
         */
        void leaveLinkMic();

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
        void switchCamera();

        /**
         * 创建渲染器
         *
         * @param context 上下文
         * @return 渲染器
         */
        SurfaceView createRenderView(Context context);

        /**
         * 获取当前用户的连麦ID
         *
         * @return 连麦ID
         */
        String getLinkMicId();

        /**
         * 为特定的连麦ID的用户设置连麦渲染器
         *
         * @param renderView 渲染器
         * @param linkMicId  连麦ID
         */
        void setupRenderView(SurfaceView renderView, String linkMicId);

        /**
         * 是否加入连麦
         */
        boolean isJoinLinkMic();

        /**
         * 设置当前是音频连麦还是视频连麦
         *
         * @param isAudioLinkMic true表示是音频连麦，false表示是视频连麦
         */
        void setIsAudioLinkMic(boolean isAudioLinkMic);

        /**
         * 设置讲师否是打开连麦
         *
         * @param isTeacherOpenLinkMic true表示讲师打开连麦，false表示讲师关闭连麦
         */
        void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic);

        /**
         * 讲师是否打开连麦
         *
         * @return true表示讲师打开连麦，false表示讲师关闭连麦
         */
        boolean isTeacherOpenLinkMic();
    }
}
