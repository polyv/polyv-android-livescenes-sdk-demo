package com.easefun.polyv.livecommon.module.modules.chatroom.contract;

import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.WorkerThread;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.data.PLVChatroomData;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvBaseCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;

import java.util.List;

/**
 * mvp-聊天室契约接口
 * 定义了：
 * 1、mvp-聊天室view层接口
 * 2、mvp-聊天室presenter层接口
 */
public interface IPLVChatroomContract {

    // <editor-fold defaultstate="collapsed" desc="1、mvp-聊天室view接口">

    /**
     * mvp-聊天室view层接口
     */
    interface IChatroomView {
        /**
         * 设置presenter后的回调
         */
        void setPresenter(@NonNull IChatroomPresenter presenter);

        /**
         * 文本发言事件
         */
        @WorkerThread
        void onSpeakEvent(@NonNull PLVSpeakEvent speakEvent);

        /**
         * 获取聊天发言的表情图片的大小
         */
        @AnyThread
        int getSpeakEmojiSize();

        /**
         * 获取提问发言的表情图片的大小
         */
        @AnyThread
        int getQuizEmojiSize();

        /**
         * 图片事件
         */
        @WorkerThread
        void onImgEvent(@NonNull PLVChatImgEvent chatImgEvent);

        /**
         * 点赞事件
         */
        @WorkerThread
        void onLikesEvent(@NonNull PLVLikesEvent likesEvent);

        /**
         * 回答事件
         */
        @WorkerThread
        void onAnswerEvent(@NonNull PLVTAnswerEvent answerEvent);

        /**
         * 用户登录事件
         */
        @WorkerThread
        void onLoginEvent(@NonNull PLVLoginEvent loginEvent);

        /**
         * 用户退出事件
         */
        @WorkerThread
        void onLogoutEvent(@NonNull PLVLogoutEvent logoutEvent);

        /**
         * 发送公告事件
         */
        @WorkerThread
        void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO);

        /**
         * 移除公告事件
         */
        @WorkerThread
        void onRemoveBulletinEvent();

        /**
         * 商品上架/新增/编辑/推送事件
         */
        @WorkerThread
        void onProductControlEvent(@NonNull PLVProductControlEvent productControlEvent);

        /**
         * 商品下架/删除事件
         */
        @WorkerThread
        void onProductRemoveEvent(@NonNull PLVProductRemoveEvent productRemoveEvent);

        /**
         * 商品上移/下移事件
         */
        @WorkerThread
        void onProductMoveEvent(@NonNull PLVProductMoveEvent productMoveEvent);

        /**
         * 商品库开关事件
         */
        @WorkerThread
        void onProductMenuSwitchEvent(@NonNull PLVProductMenuSwitchEvent productMenuSwitchEvent);

        /**
         * 房间开启/关闭事件
         */
        @WorkerThread
        void onCloseRoomEvent(@NonNull PLVCloseRoomEvent closeRoomEvent);

        /**
         * 移除信息事件
         *
         * @param id          移除单条信息的id，如果是移除所有信息，那么必定为null
         * @param isRemoveAll true：移除所有信息，false：移除单条信息
         */
        @WorkerThread
        void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll);

        /**
         * 自定义事件中的送礼事件
         *
         * @param userBean       送礼用户
         * @param customGiftBean 礼物数据
         */
        @WorkerThread
        void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean);

        /**
         * 自己本地发送的文本聊天信息
         */
        void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage);

        /**
         * 自己本地发送的提问信息
         */
        void onLocalQuestionMessage(@Nullable PolyvQuestionMessage questionMessage);

        /**
         * 自己本地发送的图片信息
         */
        void onLocalImageMessage(@Nullable PolyvSendLocalImgEvent localImgEvent);

        /**
         * 违禁词触发
         *
         * @param prohibitedMessage 违规词
         * @param hintMsg           提示消息
         * @param status            状态
         */
        @MainThread
        void onSendProhibitedWord(@NonNull String prohibitedMessage, @NonNull String hintMsg, @NonNull String status);

        /**
         * 接收到的需要添加到列表的文本发言、图片信息
         */
        @WorkerThread
        void onSpeakImgDataList(@Size(min = 1) List<PLVBaseViewData> chatMessageDataList);

        /**
         * 历史记录数据
         *
         * @param chatMessageDataList 数据列表
         * @param requestSuccessTime  请求成功的次数
         * @param isNoMoreHistory     请求成功后，是否还有未加载的历史记录
         * @param viewIndex           请求历史记录的mvp-view索引
         */
        @MainThread
        void onHistoryDataList(@Size(min = 0) List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, int requestSuccessTime, boolean isNoMoreHistory, int viewIndex);

        /**
         * 历史记录请求失败回调
         *
         * @param errorMsg  错误信息
         * @param t         异常
         * @param viewIndex 请求历史记录的mvp-view索引
         */
        @MainThread
        void onHistoryRequestFailed(String errorMsg, Throwable t, int viewIndex);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、mvp-聊天室presenter接口">

    /**
     * mvp-聊天室presenter层接口
     */
    interface IChatroomPresenter {
        /**
         * 注册view，可以注册多个
         */
        void registerView(@NonNull IChatroomView v);

        /**
         * 解除注册的view
         */
        void unregisterView(IChatroomView v);

        /**
         * 获取view的索引
         */
        int getViewIndex(IChatroomView v);

        /**
         * 初始化聊天室配置，该方法内部会设置聊天室的信息监听器
         */
        void init();

        /**
         * 发送聊天文本信息
         *
         * @param textMessage 要发送的信息，不能为空
         * @return true：成功提交，收到{@link IChatroomView#onLocalSpeakMessage(PolyvLocalMessage)}回调时为发送成功，收到{@link IChatroomView#onSendProhibitedWord(String, String, String)}为触发严禁词。<br/>
         * false：发送失败。
         */
        Pair<Boolean, Integer> sendChatMessage(PolyvLocalMessage textMessage);

        /**
         * 发送回复信息
         *
         * @param textMessage 要发送的信息，不能为空
         * @param quoteId     信息Id
         * @return true：成功提交，收到{@link IChatroomView#onLocalSpeakMessage(PolyvLocalMessage)}回调时为发送成功，收到{@link IChatroomView#onSendProhibitedWord(String, String, String)}为触发严禁词。<br/>
         * false：发送失败。
         */
        Pair<Boolean, Integer> sendQuoteMessage(PolyvLocalMessage textMessage, String quoteId);

        /**
         * 发送提问信息
         *
         * @param questionMessage 要发送的提问信息，不能为空
         */
        int sendQuestionMessage(PolyvQuestionMessage questionMessage);

        /**
         * 发送点赞信息
         */
        void sendLikeMessage();

        /**
         * 发送聊天图片信息
         */
        void sendChatImage(PolyvSendLocalImgEvent localImgEvent);

        /**
         * 发送自定义信息
         *
         * @param baseCustomEvent 自定义信息事件
         */
        <DataBean> void sendCustomMsg(PolyvBaseCustomEvent<DataBean> baseCustomEvent);

        /**
         * 发送自定义信息，示例为发送自定义送礼信息
         *
         * @param customGiftBean 自定义信息实例
         * @param tip            信息提示文案
         */
        PolyvCustomEvent<PLVCustomGiftBean> sendCustomGiftMessage(PLVCustomGiftBean customGiftBean, String tip);

        /**
         * 设置每次获取历史记录的条数，默认20条
         */
        void setGetChatHistoryCount(int count);

        /**
         * 请求聊天历史记录
         *
         * @param viewIndex 调用该方法的view的索引，没有时传0
         */
        void requestChatHistory(int viewIndex);

        /**
         * 获取历史记录成功的次数
         */
        int getChatHistoryTime();

        /**
         * 获取聊天室的数据
         */
        @NonNull
        PLVChatroomData getData();

        /**
         * 销毁，包括销毁聊天室操作、解除view操作
         */
        void destroy();
    }
    // </editor-fold>
}
