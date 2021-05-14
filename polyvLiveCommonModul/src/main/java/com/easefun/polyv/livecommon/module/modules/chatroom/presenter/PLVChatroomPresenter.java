package com.easefun.polyv.livecommon.module.modules.chatroom.presenter;

import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataRequester;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.data.PLVChatroomData;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketMessage;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.IPolyvOnlineCountListener;
import com.easefun.polyv.livescenes.chatroom.IPolyvProhibitedWordListener;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.easefun.polyv.livescenes.chatroom.event.PolyvEventHelper;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvBaseCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.log.chat.PolyvChatroomELog;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livescenes.net.PolyvApiManager;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.google.gson.reflect.TypeToken;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.rx.PLVRxBus;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVRemoveContentEvent;
import com.plv.socket.event.chat.PLVRemoveHistoryEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.socket.event.history.PLVHistoryConstant;
import com.plv.socket.event.history.PLVSpeakHistoryEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.log.PLVELogSender;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;
import okhttp3.ResponseBody;

/**
 * mvp-聊天室presenter层实现，实现 IPLVChatroomContract.IChatroomPresenter 接口
 */
public class PLVChatroomPresenter implements IPLVChatroomContract.IChatroomPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVChatroomPresenter";
    //默认获取的历史记录条数
    public static final int GET_CHAT_HISTORY_COUNT = 20;
    //聊天信息处理间隔
    private static final int CHAT_MESSAGE_TIMESPAN = 500;
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //聊天室数据
    private PLVChatroomData chatroomData;
    //聊天室mvp模式的view
    private List<IPLVChatroomContract.IChatroomView> iChatroomViews;
    //聊天信息处理的disposable
    private Disposable messageDisposable;

    //点赞数
    private long likesCount;
    //观看热度数
    private long viewerCount;
    //在线人数
    private int onlineCount;

    //每次获取的历史记录条数
    private int getChatHistoryCount = GET_CHAT_HISTORY_COUNT;
    //获取历史记录成功的次数
    private int getChatHistoryTime;
    //是否没有更多历史记录
    private boolean isNoMoreChatHistory;
    //是否有请求历史记录的事件
    private boolean hasRequestHistoryEvent;
    //请求历史记录的viewIndex
    private int requestHistoryViewIndex;
    //获取历史记录的disposable
    private Disposable chatHistoryDisposable;

    //聊天室功能开关数据观察者
    private Observer<PLVStatefulData<PolyvChatFunctionSwitchVO>> functionSwitchObserver;
    //直播详情数据观察者
    private Observer<PLVStatefulData<PolyvLiveClassDetailVO>> classDetailVOObserver;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="公共静态方法">
    public static String convertSpecialString(String input) {
        String output;
        output = input.replace("&lt;", "<");
        output = output.replace("&lt", "<");
        output = output.replace("&gt;", ">");
        output = output.replace("&gt", ">");
        output = output.replace("&yen;", "¥");
        output = output.replace("&yen", "¥");
        return output;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVChatroomPresenter(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        chatroomData = new PLVChatroomData();
        subscribeChatroomMessage();
        observeLiveRoomData();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVChatroomContract.IChatroomPresenter定义的方法">
    @Override
    public void registerView(@NonNull IPLVChatroomContract.IChatroomView v) {
        if (iChatroomViews == null) {
            iChatroomViews = new ArrayList<>();
        }
        if (!iChatroomViews.contains(v)) {
            iChatroomViews.add(v);
        }
        v.setPresenter(this);
    }

    @Override
    public void unregisterView(IPLVChatroomContract.IChatroomView v) {
        if (iChatroomViews != null) {
            iChatroomViews.remove(v);
        }
    }

    @Override
    public int getViewIndex(IPLVChatroomContract.IChatroomView v) {
        return iChatroomViews == null ? -1 : iChatroomViews.indexOf(v);
    }

    @Override
    public void init() {
        //初始化聊天室
        PolyvChatroomManager.getInstance().init();
        //添加严禁词监听器
        PolyvChatroomManager.getInstance().setProhibitedWordListener(new IPolyvProhibitedWordListener() {
            @Override
            public void onSendProhibitedWord(@NonNull final String prohibitedMessage, @NonNull final String hintMsg, @NonNull final String status) {
                PLVCommonLog.d(TAG, "chatroom onSendProhibitedWord: 发送的消息涉及违禁词");
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onSendProhibitedWord(prohibitedMessage, hintMsg, status);
                        }
                    });
                }
            }
        });
        //添加在线人数监听器
        PolyvChatroomManager.getInstance().setOnlineCountListener(new IPolyvOnlineCountListener() {
            @Override
            public void onCall(int onlineCount) {
                PLVChatroomPresenter.this.onlineCount = onlineCount;
                chatroomData.postOnlineCountData(onlineCount);
            }
        });
        //添加socket信息监听器
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                PLVCommonLog.d(TAG, "chatroom receiveMessage: " + message + ", event: " + event + ", listenEvent: " + listenEvent);
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    PLVRxBus.get().post(new PLVSocketMessage(listenEvent, message, event));
                }
            }
        });
        //添加socket登录状态监听器
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                if (status.getStatus() == PLVSocketStatus.STATUS_LOGINSUCCESS) {
                    if (hasRequestHistoryEvent) {//登录成功后可以获取到分房间id，如果之前存在请求历史的事件，则请求历史记录
                        requestChatHistory(requestHistoryViewIndex);
                    }
                }
            }
        });
    }

    @Override
    public Pair<Boolean, Integer> sendChatMessage(final PolyvLocalMessage textMessage) {
        int sendValue = PolyvChatroomManager.getInstance().sendChatMessage(textMessage, liveRoomDataManager.getSessionId(), true, new Ack() {
            @Override
            public void call(Object... args) {
                PLVCommonLog.d(TAG, "chatroom sendTextMessage call: " + Arrays.toString(args));
                if (args == null || args.length == 0 || args[0] == null) {
                    return;
                }
                /**
                 * ///通过注释暂时保留代码，触发严禁词也认为发送成功，但不会广播给其他用户
                 *if ("".equals(args[0])) {
                 *    // 触发严禁词时，args[0]为""
                 *    PLVCommonLog.d(TAG, "chatroom sendTextMessage: 发送的消息涉及违禁词");
                 *    return;
                 *}
                 */

                acceptLocalChatMessage(textMessage, String.valueOf(args[0]));
            }
        });
        if (sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//被禁言也认为发送成功，但不会广播给其他用户
            acceptLocalChatMessage(textMessage, "");
        }
        PLVCommonLog.d(TAG, "chatroom sendTextMessage: " + textMessage.getSpeakMessage() + ", sendValue: " + sendValue);
        return new Pair<>(sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP, sendValue);
    }


    @Override
    public Pair<Boolean, Integer> sendQuoteMessage(final PolyvLocalMessage textMessage, String quoteId) {
        int sendValue = PolyvChatroomManager.getInstance().sendQuoteMessage(textMessage, liveRoomDataManager.getSessionId(), true, new Ack() {
            @Override
            public void call(Object... args) {
                PLVCommonLog.d(TAG, "chatroom sendQuoteMessage call: " + Arrays.toString(args));
                if (args == null || args.length == 0 || args[0] == null) {
                    return;
                }
                /**
                 * ///通过注释暂时保留代码，触发严禁词也认为发送成功，但不会广播给其他用户
                 *if ("".equals(args[0])) {
                 *    // 触发严禁词时，args[0]为""
                 *    PLVCommonLog.d(TAG, "chatroom sendTextMessage: 发送的消息涉及违禁词");
                 *    return;
                 *}
                 */

                acceptLocalChatMessage(textMessage, String.valueOf(args[0]));
            }
        }, quoteId);
        if (sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//被禁言也认为发送成功，但不会广播给其他用户
            acceptLocalChatMessage(textMessage, "");
        }
        PLVCommonLog.d(TAG, "chatroom sendQuoteMessage: " + textMessage.getSpeakMessage() + ", sendValue: " + sendValue);
        return new Pair<>(sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP, sendValue);
    }

    @Override
    public int sendQuestionMessage(final PolyvQuestionMessage questionMessage) {
        int sendValue = PolyvChatroomManager.getInstance().sendQuestionMessage(questionMessage);
        if (sendValue > 0) {
            //把带表情的信息解析保存下来
            questionMessage.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(questionMessage.getQuestionMessage(), getQuizEmojiSizes(), Utils.getApp()));
            callbackToView(new ViewRunnable() {
                @Override
                public void run(IPLVChatroomContract.IChatroomView view) {
                    view.onLocalQuestionMessage(questionMessage);
                }
            });
        }
        PLVCommonLog.d(TAG, "chatroom sendQuestionMessage: " + questionMessage.getQuestionMessage() + ", sendValue: " + sendValue);
        return sendValue;
    }

    @Override
    public void sendLikeMessage() {
        PLVCommonLog.d(TAG, "chatroom sendLikeMessage: " + liveRoomDataManager.getSessionId());
        PolyvChatroomManager.getInstance().sendLikes(liveRoomDataManager.getSessionId());
        likesCount++;
        chatroomData.postLikesCountData(likesCount);
    }

    @Override
    public void sendChatImage(final PolyvSendLocalImgEvent localImgEvent) {
        PLVCommonLog.d(TAG, "chatroom sendChatImage: " + localImgEvent.getImageFilePath() + ", sessionId: " + liveRoomDataManager.getSessionId());
        PolyvChatroomManager.getInstance().sendChatImage(localImgEvent, liveRoomDataManager.getSessionId());
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                view.onLocalImageMessage(localImgEvent);
            }
        });
    }

    @Override
    public <DataBean> void sendCustomMsg(PolyvBaseCustomEvent<DataBean> baseCustomEvent) {
        PLVCommonLog.d(TAG, "chatroom sendCustomMsg: " + baseCustomEvent);
        PolyvChatroomManager.getInstance().sendCustomMsg(baseCustomEvent);
    }

    @Override
    public PolyvCustomEvent<PLVCustomGiftBean> sendCustomGiftMessage(PLVCustomGiftBean customGiftBean, String tip) {
        PolyvCustomEvent<PLVCustomGiftBean> customEvent = new PolyvCustomEvent<>(PLVCustomGiftBean.EVENT/*自定义信息事件名*/, customGiftBean);
        customEvent.setTip(tip);
        customEvent.setEmitMode(PolyvBaseCustomEvent.EMITMODE_ALL);//设置广播方式，EMITMODE_ALL为广播给包括自己的所有用户，EMITMODE_OTHERS为广播给不包括自己的所有用户
        customEvent.setVersion(PolyvCustomEvent.VERSION_1);//设置信息的版本号，对该版本号的信息才进行处理
        PLVCommonLog.d(TAG, "chatroom sendCustomGiftMessage: " + customEvent);
        PolyvChatroomManager.getInstance().sendCustomMsg(customEvent);
        return customEvent;
    }

    @Override
    public void setGetChatHistoryCount(int getChatHistoryCount) {
        this.getChatHistoryCount = getChatHistoryCount;
    }

    @Override
    public void requestChatHistory(final int viewIndex) {
        if (PolyvSocketWrapper.getInstance().isAllowChildRoom()) {
            if (!PolyvSocketWrapper.getInstance().canGetChildRoomIdStatus()) {
                //如果允许分房间，但是还未获取到分房间id时，需等待获取成功后才能用分房间id请求历史记录
                hasRequestHistoryEvent = true;
                requestHistoryViewIndex = viewIndex;
                return;
            }
        }
        hasRequestHistoryEvent = false;
        isNoMoreChatHistory = false;
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
        int start = getChatHistoryTime * getChatHistoryCount;
        int end = (getChatHistoryTime + 1) * getChatHistoryCount;
        String loginRoomId = PolyvSocketWrapper.getInstance().getLoginRoomId();//分房间开启，在获取到后为分房间id，其他情况为频道号
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = getConfig().getChannelId();//socket未登陆时，使用频道号
        }
        chatHistoryDisposable = PolyvApiManager.getPolyvApichatApi().getChatHistory(loginRoomId, start, end, 1)
                .map(new Function<ResponseBody, JSONArray>() {
                    @Override
                    public JSONArray apply(ResponseBody responseBody) throws Exception {
                        return new JSONArray(responseBody.string());
                    }
                })
                .compose(new PLVRxBaseTransformer<JSONArray, JSONArray>())
                .map(new Function<JSONArray, JSONArray>() {
                    @Override
                    public JSONArray apply(JSONArray jsonArray) throws Exception {
                        if (jsonArray.length() <= getChatHistoryCount) {
                            isNoMoreChatHistory = true;
                        }
                        return jsonArray;
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Function<JSONArray, List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public List<PLVBaseViewData<PLVBaseEvent>> apply(JSONArray jsonArray) throws Exception {
                        return acceptChatHistory(jsonArray, getSpeakEmojiSizes());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public void accept(final List<PLVBaseViewData<PLVBaseEvent>> dataList) throws Exception {
                        getChatHistoryTime++;
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onHistoryDataList(dataList, getChatHistoryTime, isNoMoreChatHistory, viewIndex);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(final Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //发送错误日志，便于排查问题
                        PLVELogSender.send(PolyvChatroomELog.class, PolyvChatroomELog.Event.LOAD_HISTORY_FAIL, throwable);
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onHistoryRequestFailed(PLVLiveRoomDataRequester.getErrorMessage(throwable), throwable, viewIndex);
                            }
                        });
                    }
                });
    }

    @Override
    public int getChatHistoryTime() {
        return getChatHistoryTime;
    }

    @NonNull
    @Override
    public PLVChatroomData getData() {
        return chatroomData;
    }

    @Override
    public void destroy() {
        getChatHistoryTime = 0;
        hasRequestHistoryEvent = false;
        isNoMoreChatHistory = false;
        if (iChatroomViews != null) {
            iChatroomViews.clear();
        }
        if (messageDisposable != null) {
            messageDisposable.dispose();
        }
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
        liveRoomDataManager.getFunctionSwitchVO().removeObserver(functionSwitchObserver);
        liveRoomDataManager.getClassDetailVO().removeObserver(classDetailVOObserver);
        PolyvChatroomManager.getInstance().destroy();//销毁，会移除实例及所有的监听器
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 历史信息处理">
    private List<PLVBaseViewData<PLVBaseEvent>> acceptChatHistory(JSONArray jsonArray, int[] speakEmojiSizes) {
        if (speakEmojiSizes == null) {
            speakEmojiSizes = new int[1];
            speakEmojiSizes[0] = ConvertUtils.dp2px(12);
        }
        List<PLVBaseViewData<PLVBaseEvent>> tempChatItems = new ArrayList<>();
        for (int i = 0; i < (jsonArray.length() <= getChatHistoryCount ? jsonArray.length() : jsonArray.length() - 1); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                String msgType = jsonObject.optString("msgType");
                if (!TextUtils.isEmpty(msgType)) {
                    if (PLVHistoryConstant.MSGTYPE_CUSTOMMESSAGE.equals(msgType)) {
                        //custom message
                    }
                    continue;
                }
                String messageSource = jsonObject.optString("msgSource");
                if (!TextUtils.isEmpty(messageSource)) {
                    //收/发红包/图片信息，这里仅取图片信息
                    if (PLVHistoryConstant.MSGSOURCE_CHATIMG.equals(messageSource)) {
                        int itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG;
                        PLVChatImgHistoryEvent chatImgHistory = PLVGsonUtil.fromJson(PLVChatImgHistoryEvent.class, jsonObject.toString());
                        //如果是当前用户，则使用当前用户的昵称
                        if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatImgHistory.getUser().getUserId())) {
                            chatImgHistory.getUser().setNick(PolyvSocketWrapper.getInstance().getLoginVO().getNickName());
                            itemType = PLVChatMessageItemType.ITEMTYPE_SEND_IMG;
                        }
                        boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(chatImgHistory.getUser().getUserType())
                                || PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatImgHistory.getUser().getUserId());
                        PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(chatImgHistory, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag() : null);
                        tempChatItems.add(0, itemData);
                    }
                    continue;
                }
                JSONObject jsonObject_user = jsonObject.optJSONObject("user");
                if (jsonObject_user != null) {
                    String uid = jsonObject_user.optString("uid");
                    if (PLVHistoryConstant.UID_REWARD.equals(uid) || PLVHistoryConstant.UID_CUSTOMMSG.equals(uid)) {
                        //打赏/自定义信息，这里过滤掉
                        continue;
                    }
                    JSONObject jsonObject_content = jsonObject.optJSONObject("content");
                    if (jsonObject_content != null) {
                        //content不为字符串的信息，这里过滤掉
                        continue;
                    }
                    int itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                    PLVSpeakHistoryEvent speakHistory = PLVGsonUtil.fromJson(PLVSpeakHistoryEvent.class, jsonObject.toString());
                    //如果是当前用户，则使用当前用户的昵称
                    if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(speakHistory.getUser().getUserId())) {
                        speakHistory.getUser().setNick(PolyvSocketWrapper.getInstance().getLoginVO().getNickName());
                        itemType = PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK;
                    }
                    //把带表情的信息解析保存下来
                    speakHistory.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(speakHistory.getContent()), speakEmojiSizes, Utils.getApp()));
                    PLVChatQuoteVO chatQuoteVO = speakHistory.getQuote();
                    if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                        chatQuoteVO.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(chatQuoteVO.getContent()), speakEmojiSizes, Utils.getApp()));
                    }
                    boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(speakHistory.getUser().getUserType())
                            || PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(speakHistory.getUser().getUserId());
                    PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(speakHistory, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag() : null);
                    tempChatItems.add(0, itemData);
                }
            }
        }
        return tempChatItems;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 事件订阅及处理">
    private void subscribeChatroomMessage() {
        messageDisposable = PLVRxBus.get().toObservable(PLVSocketMessage.class)
                .buffer(CHAT_MESSAGE_TIMESPAN, TimeUnit.MILLISECONDS)//500ms更新一次数据，避免聊天信息刷得太频繁
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<PLVSocketMessage>, List<PLVSocketMessage>>() {
                    @Override
                    public List<PLVSocketMessage> apply(List<PLVSocketMessage> chatroomMessages) throws Exception {
                        //主线程调用PolyvSocketWrapper.getInstance()
                        if (PolyvSocketWrapper.getInstance().getLoginVO() == null) {
                            return new ArrayList<>();
                        }
                        return getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId()) ? chatroomMessages : null;
                    }
                })
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<List<PLVSocketMessage>>() {
                    @Override
                    public void accept(List<PLVSocketMessage> chatroomMessages) throws Exception {
                        acceptChatroomMessage(chatroomMessages);//在子线程解析数据
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    private void acceptChatroomMessage(List<PLVSocketMessage> socketMessages) {
        if (socketMessages == null || socketMessages.isEmpty()) {
            return;
        }
        final List<PLVBaseViewData> chatMessageDataList = new ArrayList<>();
        for (PLVSocketMessage chatroomMessage : socketMessages) {
            String message = chatroomMessage.getMessage();
            String event = chatroomMessage.getEvent();
            String listenEvent = chatroomMessage.getListenEvent();

            Object chatMessage = null;
            int itemType = PLVChatMessageItemType.ITEMTYPE_UNDEFINED;
            boolean isSpecialType = false;
            if (PLVEventConstant.SE_CUSTOMMESSAGE.equals(listenEvent)) {
                //自定义信息
                switch (event) {
                    //自定义送礼信息解析示例
                    case PLVCustomGiftBean.EVENT:
                        Type giftType = new TypeToken<PolyvCustomEvent<PLVCustomGiftBean>>() {
                        }.getType();
                        final PolyvCustomEvent<PLVCustomGiftBean> customGiftEvent = PolyvEventHelper.gson.fromJson(message, giftType);
                        if (customGiftEvent != null && PolyvCustomEvent.VERSION_1 == customGiftEvent.getVersion()
                                && customGiftEvent.getData() != null && customGiftEvent.getUser() != null) {
                            if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(customGiftEvent.getUser().getUserId())) {
                                //自己的送礼信息
                            } else {
                                //其他用户的送礼信息
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(IPLVChatroomContract.IChatroomView view) {
                                        view.onCustomGiftEvent(customGiftEvent.getUser(), customGiftEvent.getData());
                                    }
                                });
                            }
                        }
                        break;
                    default:
                        break;
                }
            } else if (PLVEventConstant.MESSAGE_EVENT.equals(listenEvent)) {
                //非自定义信息
                switch (event) {
                    //文本类型发言
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_SPEAK:
                        final PLVSpeakEvent speakEvent = PLVEventHelper.toMessageEventModel(message, PLVSpeakEvent.class);
                        if (speakEvent != null && speakEvent.getUser() != null) {
                            if (!PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(speakEvent.getUser().getUserId())) {
                                //把带表情的信息解析保存下来
                                speakEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(speakEvent.getValues().get(0)), getSpeakEmojiSizes(), Utils.getApp()));
                                //被回复人信息
                                PLVChatQuoteVO chatQuoteVO = speakEvent.getQuote();
                                if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                                    chatQuoteVO.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(chatQuoteVO.getContent()), getSpeakEmojiSizes(), Utils.getApp()));
                                }
                                chatMessage = speakEvent;
                                itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                                isSpecialType = PLVEventHelper.isSpecialType(speakEvent.getUser().getUserType());
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(IPLVChatroomContract.IChatroomView view) {
                                        view.onSpeakEvent(speakEvent);
                                    }
                                });
                                chatroomData.postSpeakMessageData((CharSequence) speakEvent.getObjects()[0], isSpecialType);
                            }
                        }
                        break;
                    //图片类型发言
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_CHAT_IMG:
                        final PLVChatImgEvent chatImgEvent = PLVEventHelper.toMessageEventModel(message, PLVChatImgEvent.class);
                        if (chatImgEvent != null &&
                                !PolyvSocketWrapper.getInstance().getLoginVO().getUserId().
                                        equals(chatImgEvent.getUser().getUserId())) {
                            chatMessage = chatImgEvent;
                            itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG;
                            isSpecialType = PLVEventHelper.isSpecialType(chatImgEvent.getUser().getUserType());
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onImgEvent(chatImgEvent);
                                }
                            });
                        }
                        break;
                    //点赞事件
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_LIKES:
                        final PLVLikesEvent likesEvent = PLVEventHelper.toMessageEventModel(message, PLVLikesEvent.class);
                        if (likesEvent != null &&
                                !PolyvSocketWrapper.getInstance().getLoginVO().getUserId().
                                        equals(likesEvent.getUserId())) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onLikesEvent(likesEvent);
                                }
                            });
                            likesCount = likesCount + likesEvent.getCount();
                            chatroomData.postLikesCountData(likesCount);
                        }
                        break;
                    //用户登录信息
                    case PLVEventConstant.MESSAGE_EVENT_LOGIN:
                        final PLVLoginEvent loginEvent = PLVEventHelper.toMessageEventModel(message, PLVLoginEvent.class);
                        if (loginEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onLoginEvent(loginEvent);
                                }
                            });
                            //如果不是自己的socket登录事件，则观看热度+1
                            if (!PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(loginEvent.getUser().getUserId())) {
                                viewerCount++;
                                chatroomData.postViewerCountData(viewerCount);
                            }

                        }
                        break;
                    //用户登出信息
                    case PLVEventConstant.MESSAGE_EVENT_LOGOUT:
                        final PLVLogoutEvent logoutEvent = PLVEventHelper.toMessageEventModel(message, PLVLogoutEvent.class);
                        if (logoutEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onLogoutEvent(logoutEvent);
                                }
                            });
                        }
                        break;
                    //发布公告事件
                    case PLVEventConstant.Interact.BULLETIN_SHOW:
                        final PolyvBulletinVO bulletinVO = PLVGsonUtil.fromJson(PolyvBulletinVO.class, message);
                        if (bulletinVO != null) {
                            chatroomData.postBulletinVO(bulletinVO);
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onBulletinEvent(bulletinVO);
                                }
                            });
                        }
                        break;
                    //删除公告事件
                    case PLVEventConstant.Interact.BULLETIN_REMOVE:
                        chatroomData.postBulletinVO(null);
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(IPLVChatroomContract.IChatroomView view) {
                                view.onRemoveBulletinEvent();
                            }
                        });
                        break;
                    //商品操作事件
                    case PLVEventConstant.Chatroom.EVENT_PRODUCT_MESSAGE:
                        PLVProductEvent productEvent = PLVGsonUtil.fromJson(PLVProductEvent.class, message);
                        if (productEvent != null) {
                            if (productEvent.isProductControlEvent()) { //商品上架/新增/编辑/推送事件
                                final PLVProductControlEvent productControlEvent = PLVEventHelper.toMessageEventModel(message, PLVProductControlEvent.class);
                                if (productControlEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductControlEvent(productControlEvent);
                                        }
                                    });
                                }
                            } else if (productEvent.isProductRemoveEvent()) { //商品下架/删除事件
                                final PLVProductRemoveEvent productRemoveEvent = PLVEventHelper.toMessageEventModel(message, PLVProductRemoveEvent.class);
                                if (productRemoveEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductRemoveEvent(productRemoveEvent);
                                        }
                                    });
                                }
                            } else if (productEvent.isProductMoveEvent()) { //商品上移/下移事件
                                final PLVProductMoveEvent productMoveEvent = PLVEventHelper.toMessageEventModel(message, PLVProductMoveEvent.class);
                                if (productMoveEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductMoveEvent(productMoveEvent);
                                        }
                                    });
                                }
                            } else if (productEvent.isProductMenuSwitchEvent()) { //商品库开关事件
                                final PLVProductMenuSwitchEvent productMenuSwitchEvent = PLVEventHelper.toMessageEventModel(message, PLVProductMenuSwitchEvent.class);
                                if (productMenuSwitchEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductMenuSwitchEvent(productMenuSwitchEvent);
                                        }
                                    });
                                }
                            }
                        }
                        break;
                    //聊天室房间开启/关闭事件
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_CLOSEROOM:
                        final PLVCloseRoomEvent closeRoomEvent = PLVEventHelper.toMessageEventModel(message, PLVCloseRoomEvent.class);
                        if (closeRoomEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onCloseRoomEvent(closeRoomEvent);
                                }
                            });
                        }
                        break;
                    //回答事件
                    case PLVEventConstant.Chatroom.EVENT_T_ANSWER:
                        final PLVTAnswerEvent tAnswerEvent = PLVEventHelper.toMessageEventModel(message, PLVTAnswerEvent.class);
                        //判断是否是回复自己的
                        if (tAnswerEvent != null &&
                                PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(tAnswerEvent.getS_userId())) {
                            //把带表情的信息解析保存下来
                            tAnswerEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(tAnswerEvent.getContent(), getQuizEmojiSizes(), Utils.getApp()));
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onAnswerEvent(tAnswerEvent);
                                }
                            });
                        }
                        break;
                    //管理员删除某条聊天信息事件
                    case PLVRemoveContentEvent.EVENT:
                        final PLVRemoveContentEvent removeContentEvent = PLVEventHelper.toMessageEventModel(message, PLVRemoveContentEvent.class);
                        if (removeContentEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    view.onRemoveMessageEvent(removeContentEvent.getId(), false);
                                }
                            });
                        }
                        break;
                    //管理员清空所有聊天信息事件
                    case PLVRemoveHistoryEvent.EVENT:
                        PLVRemoveHistoryEvent removeHistoryEvent = PLVEventHelper.toMessageEventModel(message, PLVRemoveHistoryEvent.class);
                        if (removeHistoryEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    view.onRemoveMessageEvent(null, true);
                                }
                            });
                        }
                        break;
                    default:
                        break;
                }
                if (chatMessage != null) {
                    chatMessageDataList.add(new PLVBaseViewData<>(chatMessage, itemType, isSpecialType ? new PLVSpecialTypeTag() : null));
                }
            }
        }
        if (!chatMessageDataList.isEmpty()) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(IPLVChatroomContract.IChatroomView view) {
                    view.onSpeakImgDataList(chatMessageDataList);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private PLVLiveChannelConfig getConfig() {
        return liveRoomDataManager.getConfig();
    }

    private int[] getSpeakEmojiSizes() {
        return getEmojiSizes(1);
    }

    private int[] getQuizEmojiSizes() {
        return getEmojiSizes(2);
    }

    private int[] getEmojiSizes(int textSizeType) {//1：发言，2：提问
        List<Integer> textSizes = new ArrayList<>();
        if (iChatroomViews != null) {
            for (IPLVChatroomContract.IChatroomView view : iChatroomViews) {
                int textSize;
                switch (textSizeType) {
                    case 1:
                        textSize = view.getSpeakEmojiSize();
                        break;
                    case 2:
                        textSize = view.getQuizEmojiSize();
                        break;
                    default:
                        textSize = 0;
                        break;
                }
                if (view == null || textSize <= 0) {
                    textSizes.add(ConvertUtils.dp2px(12));
                } else {
                    textSizes.add(textSize);
                }
            }
        }
        Integer[] array = textSizes.toArray(new Integer[0]);
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private void acceptLocalChatMessage(final PolyvLocalMessage textMessage, String messageId) {
        //信息发送成功后，保存信息id
        textMessage.setId(messageId);
        //把带表情的信息解析保存下来
        textMessage.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(textMessage.getSpeakMessage(), getSpeakEmojiSizes(), Utils.getApp()));
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NotNull IPLVChatroomContract.IChatroomView view) {
                view.onLocalSpeakMessage(textMessage);
            }
        });
        chatroomData.postSpeakMessageData((CharSequence) textMessage.getObjects()[0], true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 观察直播间的数据">
    private void observeLiveRoomData() {
        //观察直播间的聊天室开关数据
        functionSwitchObserver = new Observer<PLVStatefulData<PolyvChatFunctionSwitchVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvChatFunctionSwitchVO> chatFunctionSwitchStateData) {
                liveRoomDataManager.getFunctionSwitchVO().removeObserver(this);
                if (chatFunctionSwitchStateData == null || !chatFunctionSwitchStateData.isSuccess()) {
                    return;
                }
                PolyvChatFunctionSwitchVO functionSwitchVO = chatFunctionSwitchStateData.getData();
                if (functionSwitchVO == null || functionSwitchVO.getData() == null) {
                    return;
                }
                List<PolyvChatFunctionSwitchVO.DataBean> dataBeanList = functionSwitchVO.getData();
                if (dataBeanList == null) {
                    return;
                }
                chatroomData.postFunctionSwitchData(dataBeanList);
            }
        };
        liveRoomDataManager.getFunctionSwitchVO().observeForever(functionSwitchObserver);
        //观察直播间的直播详情数据
        classDetailVOObserver = new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> classDetailVOStateData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (classDetailVOStateData == null || !classDetailVOStateData.isSuccess()) {
                    return;
                }
                PolyvLiveClassDetailVO classDetailVO = classDetailVOStateData.getData();
                if (classDetailVO == null || classDetailVO.getData() == null) {
                    return;
                }
                likesCount = classDetailVO.getData().getLikes();
                viewerCount = classDetailVO.getData().getPageView();
                chatroomData.postLikesCountData(likesCount);
                chatroomData.postViewerCountData(viewerCount);
            }
        };
        liveRoomDataManager.getClassDetailVO().observeForever(classDetailVOObserver);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view回调">
    private void callbackToView(ViewRunnable runnable) {
        if (iChatroomViews != null) {
            for (IPLVChatroomContract.IChatroomView view : iChatroomViews) {
                if (view != null && runnable != null) {
                    runnable.run(view);
                }
            }
        }
    }

    private interface ViewRunnable {
        void run(@NonNull IPLVChatroomContract.IChatroomView view);
    }
    // </editor-fold>
}
