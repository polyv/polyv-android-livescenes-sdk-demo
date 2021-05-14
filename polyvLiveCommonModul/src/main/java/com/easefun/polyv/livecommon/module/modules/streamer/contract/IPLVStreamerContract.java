package com.easefun.polyv.livecommon.module.modules.streamer.contract;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.data.PLVStreamerData;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;

import java.util.List;

/**
 * mvp-推流和连麦契约协议
 * 定义了：
 * 1、mvp-推流和连麦view层接口
 * 2、mvp-推流和连麦presenter层接口
 */
public interface IPLVStreamerContract {

    // <editor-fold defaultstate="collapsed" desc="1、mvp-推流和连麦view层接口">

    /**
     * mvp-推流和连麦view层接口
     */
    interface IStreamerView {
        /**
         * 设置presenter后的回调
         */
        void setPresenter(@NonNull IStreamerPresenter presenter);

        /**
         * 推流引擎创建成功，View层应创建连麦适配器和初始化连麦布局
         *
         * @param linkMicUid  我的连麦id
         * @param linkMicList 连麦列表。在实现中，要用该列表去渲染
         */
        void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList);

        /**
         * 响应用户开关视频
         *
         * @param uid             用户id
         * @param mute            true表示关闭视频，false表示开启视频
         * @param streamerListPos 推流和连麦列表中的位置
         * @param memberListPos   成员列表中的位置
         */
        void onUserMuteVideo(final String uid, final boolean mute, int streamerListPos, int memberListPos);

        /**
         * 响应用户开关音频
         *
         * @param uid             用户id
         * @param mute            true表示关闭音频，false表示开启音频
         * @param streamerListPos 列表中的位置
         * @param memberListPos   成员列表中的位置
         */
        void onUserMuteAudio(final String uid, final boolean mute, int streamerListPos, int memberListPos);

        /**
         * 响应本地用户麦克风音量变化
         */
        void onLocalUserMicVolumeChanged();

        /**
         * 响应远端用户麦克风音量变化
         */
        void onRemoteUserVolumeChanged(List<PLVMemberItemDataBean> linkMicList);

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
         * 推流网络变化
         *
         * @param quality 网络状态常量
         */
        void onNetworkQuality(int quality);

        /**
         * 更新推流时间
         *
         * @param secondsSinceStartTiming 推流时间，单位：秒
         */
        void onUpdateStreamerTime(int secondsSinceStartTiming);

        /**
         * 因断网延迟20s断流
         */
        void onShowNetBroken();

        /**
         * 当前为结束推流状态
         */
        void onStatesToStreamEnded();

        /**
         * 当前为成功推流状态
         */
        void onStatesToStreamStarted();

        /**
         * 响应推流和连麦错误
         */
        void onStreamerError(int errorCode, Throwable throwable);

        /**
         * 更新成员列表数据
         *
         * @param dataBeanList 成员列表。在实现中，要用该列表去渲染
         */
        void onUpdateMemberListData(List<PLVMemberItemDataBean> dataBeanList);

        /**
         * 相机方向改变
         *
         * @param front true：前置，false：后置
         * @param pos   成员列表中的位置
         */
        void onCameraDirection(boolean front, int pos);

        /**
         * 更新成员列表中的socket用户信息
         *
         * @param pos 成员列表中的位置
         */
        void onUpdateSocketUserData(int pos);

        /**
         * 添加成员列表数据
         *
         * @param pos 成员列表中的位置
         */
        void onAddMemberListData(int pos);

        /**
         * 移除成员列表数据
         *
         * @param pos 成员列表中的位置
         */
        void onRemoveMemberListData(int pos);

        /**
         * 达到最大连麦人数的回调，在达到最大连麦人数后再同意用户连麦时触发
         */
        void onReachTheInteractNumLimit();

        /**
         * 用户请求连麦触发
         *
         * @param uid 连麦的id
         */
        void onUserRequest(String uid);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、mvp-推流和连麦presenter层接口">

    /**
     * mvp-推流和连麦presenter层接口
     */
    interface IStreamerPresenter {
        /**
         * 注册view，可以注册多个
         */
        void registerView(@NonNull IStreamerView v);

        /**
         * 解除注册的view
         */
        void unregisterView(IStreamerView v);

        /**
         * 初始化推流和连麦配置
         */
        void init();

        /**
         * 获取网络质量
         *
         * @return 网络质量常量
         */
        int getNetworkQuality();

        /**
         * 设置推流码率
         *
         * @param bitrate 码率
         */
        void setBitrate(@PLVSStreamerConfig.BitrateType int bitrate);

        /**
         * 获取设置的推流码率
         *
         * @return 设置的推流码率
         */
        int getBitrate();

        /**
         * 获取最大支持的推流码率
         *
         * @return 最大支持的推流码率
         */
        int getMaxBitrate();

        /**
         * 是否允许录制声音
         *
         * @param enable true：允许，false：不允许
         */
        boolean enableRecordingAudioVolume(boolean enable);

        /**
         * 是否允许录制视频/打开摄像头
         *
         * @param enable true：允许，false：不允许
         */
        boolean enableLocalVideo(boolean enable);

        /**
         * 设置相机方向
         *
         * @param front true：前置，false：后置
         */
        boolean setCameraDirection(boolean front);

        /**
         * 创建渲染器
         *
         * @param context 上下文
         * @return 渲染器
         */
        SurfaceView createRenderView(Context context);

        /**
         * 释放渲染器
         *
         * @param renderView 渲染器
         */
        void releaseRenderView(SurfaceView renderView);

        /**
         * 为特定的连麦ID的用户设置连麦渲染器
         *
         * @param renderView 渲染器
         * @param linkMicId  连麦ID
         */
        void setupRenderView(SurfaceView renderView, String linkMicId);

        /**
         * 开始推流
         */
        void startLiveStream();

        /**
         * 停止推流
         */
        void stopLiveStream();

        /**
         * 控制成员列表中的用户加入或离开连麦
         *
         * @param position    成员列表中的位置
         * @param isAllowJoin true：加入，false：离开
         */
        void controlUserLinkMic(int position, boolean isAllowJoin);

        /**
         * 禁/启用用户媒体
         *
         * @param position    成员列表中的位置
         * @param isVideoType true：视频，false：音频
         * @param isMute      true：禁用，false：启用
         */
        void muteUserMedia(int position, boolean isVideoType, boolean isMute);

        /**
         * 下麦全体连麦用户
         */
        void closeAllUserLinkMic();

        /**
         * 全体连麦用户禁用/开启声音
         *
         * @param isMute true：禁用，false：开启
         */
        void muteAllUserAudio(boolean isMute);

        /**
         * 请求成员列表数据
         */
        void requestMemberList();

        /**
         * 获取推流状态
         *
         * @return 推流状态常量
         */
        int getStreamerStatus();

        /**
         * 获取推流和连麦的数据
         */
        @NonNull
        PLVStreamerData getData();

        /**
         * 销毁，包括销毁推流和连麦操作、解除view操作
         */
        void destroy();
    }
    // </editor-fold>
}
