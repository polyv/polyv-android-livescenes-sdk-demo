package com.easefun.polyv.livecommon.module.modules.streamer.contract;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.data.PLVStreamerData;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.linkmic.screenshare.vo.PLVCustomScreenShareData;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.event.linkmic.PLVJoinAnswerSEvent;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.user.PLVSocketUserBean;

import java.util.List;

import io.socket.client.Ack;

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
        void onLocalUserMicVolumeChanged(int volume);

        /**
         * 响应远端用户麦克风音量变化
         */
        void onRemoteUserVolumeChanged(List<PLVMemberItemDataBean> linkMicList);

        /**
         * 响应用户加入连麦频道
         *
         * @param dataBeanList
         */
        void onUsersJoin(List<PLVLinkMicItemDataBean> dataBeanList);

        /**
         * 响应用户离开连麦频道
         *
         * @param dataBeanList
         */
        void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList);

        /**
         * 推流网络变化
         *
         * @param quality 网络状态常量
         */
        void onNetworkQuality(PLVLinkMicConstant.NetworkQuality quality);

        /**
         * 推流网络统计
         */
        void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatus);

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
         * @deprecated
         * @see #onUpdateMemberListData(List)
         * @param pos 成员列表中的位置
         */
        void onUpdateSocketUserData(int pos);

        /**
         * 添加成员列表数据
         *
         * @deprecated
         * @see #onUpdateMemberListData(List)
         * @param pos 成员列表中的位置
         */
        void onAddMemberListData(int pos);

        /**
         * 移除成员列表数据
         *
         * @deprecated
         * @see #onUpdateMemberListData(List)
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

        /**
         * 直播状态改变
         */
        void onStreamLiveStatusChanged(boolean isLive);

        /**
         * 嘉宾RTC状态改变
         */
        void onGuestRTCStatusChanged(int pos, boolean isJoinRTC);

        /**
         * 嘉宾多媒体状态改变
         */
        void onGuestMediaStatusChanged(int pos);

        /**
         * 权限响应
         */
        void onSetPermissionChange(String type, boolean isGranted, boolean isCurrentUser, PLVSocketUserBean user);

        void onFirstScreenChange(String linkMicUserId, boolean isFirstScreen);

        void onDocumentStreamerViewChange(boolean documentInMainScreen);

        /**
         * 屏幕共享状态变更
         *
         * @param position
         * @param isShare  是否开始屏幕共享
         * @param extra    附加信息，如错误码
         */
        void onScreenShareChange(int position, boolean isShare, int extra, String userId, boolean isMyself);

        /**
         * 讲师邀请上麦
         */
        void onTeacherInviteMeJoinLinkMic(PLVJoinResponseSEvent event);

        /**
         * 观众响应连麦邀请
         */
        void onViewerJoinAnswer(PLVJoinAnswerSEvent joinAnswerEvent, PLVMemberItemDataBean member);

        /**
         * 连麦开关状态变化回调
         *
         * @param isVideoLinkMic true->视频连麦，false->音频连麦
         * @param isOpen         是否开启连麦
         */
        void onLinkMicOpenStateChanged(boolean isVideoLinkMic, boolean isOpen);
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
        PLVLinkMicConstant.NetworkQuality getNetworkQuality();

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
         * 是否恢复上一场直播流来推流
         */
        boolean isRecoverStream();

        /**
         * 是否允许录制声音
         *
         * @param enable true：允许，false：不允许
         */
        boolean enableRecordingAudioVolume(boolean enable);

        /**
         * 当前是否打开麦克风
         */
        boolean isLocalAudioEnabled();

        /**
         * 是否允许显示本地摄像头画面
         *
         * @param enable true：允许，false：不允许
         */
        boolean enableLocalVideo(boolean enable);

        /**
         * 当前是否显示摄像头画面
         */
        boolean isLocalVideoEnabled();

        /**
         * 是否允许本地摄像头画面采集
         */
        void enableLocalVideoCapture(boolean enable);

        /**
         * 开关手电筒，如果前置摄像头没有手电筒，那么前置摄像头是无法打开手电筒的。
         *
         * @param enable 开关
         * @return true表示打开成功，false表示打开失败
         */
        boolean enableTorch(boolean enable);

        /**
         * 设置相机方向
         *
         * @param front true：前置，false：后置
         */
        boolean setCameraDirection(boolean front);

        /**
         * 设置前置摄像头画面镜像
         *
         * @param enable true表示镜像，false表示非镜像
         */
        void setFrontCameraMirror(boolean enable);

        /**
         * 缩放本地摄像头
         *
         * @param scaleFactor >1表示放大，<1表示缩小
         */
        void zoomLocalCamera(float scaleFactor);

        /**
         * 设置推流画面类型
         *
         * @param type 类型
         */
        void setPushPictureResolutionType(@PLVLinkMicConstant.PushPictureResolutionType int type);

        /**
         * 设置推流画面比例
         */
        void setPushResolutionRatio(PLVLinkMicConstant.PushResolutionRatio resolutionRatio);

        /**
         * 设置混流画面布局类型
         *
         * @param mixLayoutType 混流布局类型
         */
        void setMixLayoutType(PLVStreamerConfig.MixLayoutType mixLayoutType);

        /**
         * 获取混流画面布局类型
         *
         * @return 混流布局类型
         */
        PLVStreamerConfig.MixLayoutType getMixLayoutType();

        /**
         * 设置直播推流，是否需要恢复上一场的流继续推流
         */
        void setRecoverStream(boolean recoverStream);

        /**
         * 创建渲染器
         *
         * @param context 上下文
         * @return 渲染器
         */
        SurfaceView createRenderView(Context context);

        /**
         * 创建渲染器
         *
         * @param context 上下文
         * @return 渲染器
         */
        TextureView createTextureRenderView(Context context);

        /**
         * 释放渲染器
         *
         * @param renderView 渲染器
         */
        void releaseRenderView(View renderView);

        /**
         * 为特定的连麦ID的用户设置连麦渲染器
         *
         * @param renderView 渲染器
         * @param linkMicId  连麦ID
         */
        void setupRenderView(View renderView, String linkMicId);

        /**
         * 开始推流
         */
        void startLiveStream();

        /**
         * 停止推流
         */
        void stopLiveStream();

        /**
         * 停止屏幕共享
         */
        void exitShareScreen();

        /**
         * 请求屏幕共享
         *
         * @param activity
         */
        void requestShareScreen(Activity activity, PLVCustomScreenShareData customScreenShareData);

        /**
         * 是否正在屏幕共享
         */
        boolean isScreenSharing();

        /**
         * 开启连麦
         *
         * @param isVideoType 是否视频连麦
         * @param isOpen      是否开启连麦
         * @param ack         回调
         * @return 是否调用成功
         */
        boolean openLinkMic(boolean isVideoType, boolean isOpen, Ack ack);

        /**
         * 关闭连麦
         */
        boolean closeLinkMic(Ack ack);

        /**
         * 允许观众举手连麦
         */
        boolean allowViewerRaiseHand(Ack ack);

        /**
         * 关闭观众举手连麦
         */
        boolean disallowViewerRaiseHand(Ack ack);

        /**
         * 更改连麦类型 音频/视频
         *
         * @param isVideoType 是否视频连麦
         */
        boolean changeLinkMicType(boolean isVideoType);

        /**
         * 控制成员列表中的用户加入或离开连麦
         *
         * @param position 成员列表中的位置
         * @param action   具体操作
         */
        void controlUserLinkMic(int position, PLVStreamerControlLinkMicAction action);

        /**
         * 控制连麦列表中的用户加入或离开连麦
         *
         * @param position    连麦列表中的位置
         * @param action 具体操作
         */
        void controlUserLinkMicInLinkMicList(int position, PLVStreamerControlLinkMicAction action);

        /**
         * 禁/启用用户媒体
         *
         * @param position    成员列表中的位置
         * @param isVideoType true：视频，false：音频
         * @param isMute      true：禁用，false：启用
         */
        void muteUserMedia(int position, boolean isVideoType, boolean isMute);

        /**
         * 禁/启用用户媒体
         *
         * @param position    连麦列表中的位置
         * @param isVideoType true：视频，false：音频
         * @param isMute      true：禁用，false：启用
         */
        void muteUserMediaInLinkMicList(int position, boolean isVideoType, boolean isMute);

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
         * 嘉宾尝试上麦
         */
        void guestTryJoinLinkMic();

        /**
         * 嘉宾发起举手上麦
         */
        void guestSendJoinRequest();

        /**
         * 嘉宾离开连麦
         */
        void guestSendLeaveLinkMic();

        /**
         * 设置用户主讲权限
         *
         * @param userId          用户的userId
         * @param isSetPermission
         * @param ack
         */
        void setUserPermissionSpeaker(String userId, boolean isSetPermission, Ack ack);

        void setDocumentAndStreamerViewPosition(boolean documentInMainScreen);

        /**
         * 获取推流和连麦的数据
         */
        @NonNull
        PLVStreamerData getData();

        /**
         * 设置弱网条件下的降级策略
         *
         * @param pushDowngradePreference 降级策略
         */
        void setPushDowngradePreference(@NonNull PLVPushDowngradePreference pushDowngradePreference);

        /**
         * @return 当前降级策略，当引擎未初始化或不支持降级策略时返回 null
         */
        @Nullable
        PLVPushDowngradePreference getPushDowngradePreference();

        /**
         * 响应邀请连麦
         */
        void answerLinkMicInvitation(boolean accept, boolean isTimeout, boolean openCamera, boolean openMicrophone);

        /**
         * 获取邀请连麦接受邀请的剩余时间
         */
        void getJoinAnswerTimeLeft(PLVSugarUtil.Consumer<Integer> callback);

        /**
         * 获取连麦用户数量
         *
         * @param userTypes 指定用户类型，传空时表示所有用户类型
         * @return 指定用户类型的连麦用户数量
         */
        int countLinkMicUser(@Nullable List<String> userTypes);

        /**
         * 销毁，包括销毁推流和连麦操作、解除view操作
         */
        void destroy();
    }
    // </editor-fold>
}
