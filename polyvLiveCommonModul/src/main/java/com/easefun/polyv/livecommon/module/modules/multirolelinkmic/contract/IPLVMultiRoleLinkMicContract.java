package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.presenter.data.PLVMultiRoleLinkMicData;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;
import com.plv.livescenes.hiclass.vo.PLVHCStudentLessonListVO;
import com.plv.livescenes.net.IPLVDataRequestListener;
import com.plv.socket.event.linkmic.PLVRemoveMicSiteEvent;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

import java.util.List;
import java.util.Map;

import io.socket.client.Ack;

/**
 * 多角色连麦业务mvp模式的契约类
 * 定义了：
 * 1、mvp-多角色连麦view层接口
 * 2、mvp-多角色连麦presenter层接口
 */
public interface IPLVMultiRoleLinkMicContract {

    // <editor-fold defaultstate="collapsed" desc="1、mvp-多角色连麦view层接口">

    /**
     * mvp-多角色连麦view层接口
     */
    interface IMultiRoleLinkMicView {
        /**
         * 设置presenter后的回调
         */
        void setPresenter(@NonNull IMultiRoleLinkMicPresenter presenter);

        /**
         * 连麦引擎创建成功
         */
        void onLinkMicEngineCreatedSuccess();

        /**
         * 响应连麦错误
         */
        void onLinkMicError(int errorCode, Throwable throwable);

        /**
         * 初始化连麦列表数据
         *
         *
         * @param myLinkMicId 我的连麦id
         * @param linkMicList 连麦列表。在实现中，要用该列表去渲染
         */
        void onInitLinkMicList(String myLinkMicId, List<PLVLinkMicItemDataBean> linkMicList);

        /**
         * 响应用户加入连麦频道
         *
         * @param position 连麦列表中的位置
         */
        void onUsersJoin(PLVLinkMicItemDataBean linkMicItemDataBean, int position);

        /**
         * 响应用户离开连麦频道
         *
         * @param position 连麦列表中的位置
         */
        void onUsersLeave(PLVLinkMicItemDataBean linkMicItemDataBean, int position);

        /**
         * 响应用户已存在连麦频道
         *
         * @param position 连麦列表中的位置
         */
        void onUserExisted(PLVLinkMicItemDataBean linkMicItemDataBean, int position);

        /**
         * 响应讲师的屏幕共享流
         *
         * @param isOpen true：打开，false：关闭
         */
        void onTeacherScreenStream(PLVLinkMicItemDataBean linkMicItemDataBean, boolean isOpen);

        /**
         * 更新连麦列表数据
         *
         * @param dataBeanList 连麦列表。在实现中，要该列表去渲染
         */
        void onLinkMicListChanged(List<PLVLinkMicItemDataBean> dataBeanList);

        /**
         * 更新成员列表数据
         *
         * @param dataBeanList 成员列表。在实现中，要用该列表去渲染
         */
        void onMemberListChanged(List<PLVMemberItemDataBean> dataBeanList);

        /**
         * 更新成员列表中的socket用户信息，包括禁言，解除禁言，设置昵称
         *
         * @param pos 成员列表中的位置
         */
        void onMemberItemChanged(int pos);

        /**
         * 添加成员列表数据
         *
         * @param pos 成员列表中的位置
         */
        void onMemberItemInsert(int pos);

        /**
         * 移除成员列表数据
         *
         * @param pos 成员列表中的位置
         */
        void onMemberItemRemove(int pos);

        /**
         * 响应用户举手
         *
         * @param raiseHandCount 举手数量
         * @param isRaiseHand    true：举手，false：结束举手
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserRaiseHand(int raiseHandCount, boolean isRaiseHand, int linkMicListPos, int memberListPos);

        /**
         * 用户获取到奖杯
         *
         * @param userNick       收到奖杯的用户昵称
         * @param isByEvent      是否是通过事件即时获取到的奖杯，可能是从历史数据中拿到的
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserGetCup(String userNick, boolean isByEvent, int linkMicListPos, int memberListPos);

        /**
         * 响应用户被授权画笔
         *
         * @param isMyself       是否是自己
         * @param isHasPaint     true：被授权，false：被取消授权
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserHasPaint(boolean isMyself, boolean isHasPaint, int linkMicListPos, int memberListPos);

        /**
         * 响应用户开关视频
         *
         * @param uid            用户id
         * @param mute           true表示关闭视频，false表示开启视频
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserMuteVideo(final String uid, final boolean mute, int linkMicListPos, int memberListPos);

        /**
         * 响应用户开关音频
         *
         * @param uid            用户id
         * @param mute           true表示关闭音频，false表示开启音频
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserMuteAudio(final String uid, final boolean mute, int linkMicListPos, int memberListPos);

        /**
         * 讲师控制我的媒体状态
         *
         * @param isVideoType true：视频，false：音频
         * @param isMute      true：禁用，false：启用
         */
        void onTeacherMuteMyMedia(boolean isVideoType, boolean isMute);

        /**
         * 讲师控制我的连麦状态
         *
         * @param isAllowJoin true：上麦，false：下麦
         */
        void onTeacherControlMyLinkMic(boolean isAllowJoin);

        /**
         * 用户需要响应连麦
         */
        boolean onUserNeedAnswerLinkMic();

        /**
         * 响应本地用户麦克风音量变化
         */
        void onLocalUserVolumeChanged(int volume);

        /**
         * 响应远端用户麦克风音量变化
         */
        void onRemoteUserVolumeChanged();

        /**
         * 达到最大连麦人数的回调，在达到最大连麦人数后再同意用户连麦时触发
         */
        void onReachTheInteractNumLimit();

        /**
         * 已在别处登录
         */
        void onRepeatLogin(String desc);

        /**
         * 重连rtc频道成功
         */
        void onRejoinRoomSuccess();

        /**
         * 连麦网络变化
         *
         * @param quality 网络状态常量
         */
        void onNetworkQuality(int quality);

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
         * 讲师昵称
         */
        void onTeacherInfo(String nick);

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
         *
         * @param inClassTime 上课时间
         * @param isFromApi   是否是从接口拿到的下课状态
         * @param dataVO      下节课信息
         */
        void onLessonEnd(long inClassTime, boolean isFromApi, @Nullable PLVHCStudentLessonListVO.DataVO dataVO);

        /**
         * 拖堂时间太长，最大拖堂时间默认4个小时，即在拖堂时间3分50秒的时候会触发该回调方法
         *
         * @param willAutoStopLessonTimeMs 还剩多少时间将要自动结束课节，默认10分钟
         */
        void onLessonLateTooLong(long willAutoStopLessonTimeMs);

        /**
         * 响应用户被授权组长
         *
         * @param isHasGroupLeader true：自己当前被授权，false：自己当前没有被授权
         * @param nick             组长的昵称
         * @param isGroupChanged   是否是切换分组，true：切换，false：加入
         * @param isLeaderChanged  是否是切换组长，true：切换，false：初始设置的组长
         * @param groupName        分组的名称
         * @param leaderId         组长Id，为null表示分组里没有组长
         */
        void onUserHasGroupLeader(boolean isHasGroupLeader, String nick, boolean isGroupChanged, boolean isLeaderChanged, String groupName, @Nullable String leaderId);

        /**
         * 即将加入讨论
         *
         * @param countdownTimeMs 倒计时时间
         */
        void onWillJoinDiscuss(long countdownTimeMs);

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
         *
         * @param switchRoomEvent 切换房间事件
         */
        void onLeaveDiscuss(@Nullable PLVSwitchRoomEvent switchRoomEvent);

        /**
         * 讲师加入分组讨论
         *
         * @param isJoin true：加入，false：离开
         */
        void onTeacherJoinDiscuss(boolean isJoin);

        /**
         * 讲师发送广播通知
         *
         * @param content 通知内容
         */
        void onTeacherSendBroadcast(String content);

        /**
         * 组长请求帮助
         */
        void onLeaderRequestHelp();

        /**
         * 组长取消帮助
         */
        void onLeaderCancelHelp();

        /**
         * 更新摄像头放大位置
         */
        void onUpdateLinkMicZoom(PLVUpdateMicSiteEvent updateMicSiteEvent);

        /**
         * 移除放大区域的摄像头画面
         */
        void onRemoveLinkMicZoom(PLVRemoveMicSiteEvent removeMicSiteEvent);

        /**
         * 更新所有摄像头放大画面位置
         *
         * @param updateMicSiteEventMap Key:连麦id，Value:事件
         */
        void onChangeLinkMicZoom(@Nullable Map<String, PLVUpdateMicSiteEvent> updateMicSiteEventMap);

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、mvp-多角色连麦presenter层接口">

    /**
     * mvp-多角色连麦presenter层接口
     */
    interface IMultiRoleLinkMicPresenter {
        /**
         * 注册view，可以注册多个
         */
        void registerView(@NonNull IMultiRoleLinkMicView v);

        /**
         * 解除注册的view
         */
        void unregisterView(IMultiRoleLinkMicView v);

        /**
         * 初始化多角色连麦配置
         */
        void init();

        /**
         * 加入rtc频道
         */
        void joinChannel();

        /**
         * 离开rtc频道
         */
        void leaveChannel();

        /**
         * 上课
         */
        void startLesson(IPLVDataRequestListener<String> listener);

        /**
         * 下课
         */
        void stopLesson(IPLVDataRequestListener<String> listener);

        /**
         * 设置连麦角色为：观众
         */
        void switchRoleToAudience();

        /**
         * 设置连麦角色为：主播
         */
        void switchRoleToBroadcaster();

        /**
         * 静音音频
         *
         * @param mute true表示静音，false表示打开
         */
        boolean muteAudio(boolean mute);

        /**
         * 禁用视频
         *
         * @param mute true表示禁用视频，false表示打开视频
         */
        boolean muteVideo(boolean mute);

        /**
         * 切换前后置摄像头方向
         */
        void switchCamera();

        /**
         * 切换前后置摄像头方向
         */
        void switchCamera(boolean front);

        /**
         * 设置推流画面类型
         *
         * @param type 类型
         */
        void setPushPictureResolutionType(@PLVLinkMicConstant.PushPictureResolutionType int type);

        /**
         * 获取成员列表数据
         */
        void requestMemberList();

        /**
         * 创建渲染器
         *
         * @param context 上下文
         * @return 渲染器
         */
        View createRenderView(Context context);

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
         * 为特定的连麦ID的用户设置连麦渲染器
         *
         * @param renderView 渲染器
         * @param linkMicId  连麦ID
         * @param streamType 流类型
         */
        void setupRenderView(View renderView, String linkMicId, @PLVLinkMicConstant.RenderStreamTypeAnnotation int streamType);

        /**
         * 发送奖杯给学员
         *
         * @param linkMicListPos 连麦列表索引
         * @param ack            监听回调
         */
        void sendCupEvent(int linkMicListPos, Ack ack);

        /**
         * 设置画笔权限
         *
         * @param memberListPos   成员列表索引
         * @param isHasPermission true：赋权，false：撤销
         * @param ack             监听回调
         */
        void setPaintPermission(int memberListPos, boolean isHasPermission, Ack ack);

        /**
         * 设置画笔权限
         *
         * @param linkMicListPos  连麦列表索引
         * @param isHasPermission true：赋权，false：撤销
         * @param ack             监听回调
         */
        void setPaintPermissionInLinkMicList(int linkMicListPos, boolean isHasPermission, Ack ack);

        /**
         * 禁/启用用户媒体权限
         *
         * @param memberListPos 成员列表中的位置
         * @param isVideoType   true：视频，false：音频
         * @param isMute        true：禁用，false：启用
         */
        void setMediaPermission(int memberListPos, boolean isVideoType, boolean isMute);

        /**
         * 禁/启用用户媒体权限
         *
         * @param memberListPos 成员列表中的位置
         * @param isVideoType   true：视频，false：音频
         * @param isMute        true：禁用，false：启用
         * @param ack           监听回调
         */
        void setMediaPermission(int memberListPos, boolean isVideoType, boolean isMute, Ack ack);

        /**
         * 禁/启用用户媒体权限
         *
         * @param linkMicListPos 连麦列表中的位置
         * @param isVideoType    true：视频，false：音频
         * @param isMute         true：禁用，false：启用
         * @param ack            监听回调
         */
        void setMediaPermissionInLinkMicList(int linkMicListPos, boolean isVideoType, boolean isMute, Ack ack);

        /**
         * 发送举手事件
         *
         * @param raiseHandTime 举手时间
         */
        void sendRaiseHandEvent(int raiseHandTime);

        /**
         * 控制成员列表中的用户加入或离开连麦
         *
         * @param memberListPos 成员列表中的位置
         * @param isAllowJoin   true：加入，false：离开
         */
        void controlUserLinkMic(int memberListPos, boolean isAllowJoin);

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
         * 同意连麦邀请
         */
        void answerLinkMicInvitation();

        /**
         * 是否是讲师类型
         */
        boolean isTeacherType();

        /**
         * 是否是我的连麦id
         */
        boolean isMyLinkMicId(String linkMicId);

        /**
         * 是否是上台状态
         */
        boolean isInClassStatus();

        /**
         * 课节上课状态
         */
        int getLessonStatus();

        /**
         * 获取限制的连麦人数，未能获取时为0，也可用 {@link PLVMultiRoleLinkMicData#getLimitLinkNumber()} 方法监听获取
         */
        int getLimitLinkNumber();

        /**
         * 是否加入了分组讨论
         */
        boolean isJoinDiscuss();

        /**
         * 获取连麦数据
         */
        @NonNull
        PLVMultiRoleLinkMicData getData();

        /**
         * 销毁，包括销毁连麦操作、解除view操作
         */
        void destroy();
    }
    // </editor-fold>
}
