package com.easefun.polyv.livecommon.module.modules.chatroom.presenter;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.mutableLiveData;
import static com.plv.foundationsdk.utils.PLVAppUtils.getString;
import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static com.plv.foundationsdk.utils.PLVSugarUtil.transformList;

import android.arch.lifecycle.Observer;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Pair;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataRequester;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.enums.PLVRedPaperType;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.data.PLVChatroomData;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.PLVWelfareLotteryManager;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.PLVMultiRoomTransmitRepo;
import com.easefun.polyv.livecommon.module.modules.redpack.model.PLVRedpackRepo;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketMessage;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.PLVImageUtils;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.gif.RelativeImageSpan;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.IPolyvChatroomManager;
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
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.exts.AsyncLazy;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.log.elog.logcode.chat.PLVErrorCodeChatroomStatus;
import com.plv.foundationsdk.net.PLVResponseApiBean2;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.rx.PLVRxBus;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.chatroom.send.custom.PLVCustomEvent;
import com.plv.livescenes.model.PLVKickUsersVO;
import com.plv.livescenes.model.PLVLiveViewerListVO;
import com.plv.livescenes.model.interact.PLVCardPushVO;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVCancelTopEvent;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVFocusModeEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVOverLengthMessageEvent;
import com.plv.socket.event.chat.PLVRemoveContentEvent;
import com.plv.socket.event.chat.PLVRemoveHistoryEvent;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.chat.PLVToTopEvent;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.socket.event.history.PLVFileShareHistoryEvent;
import com.plv.socket.event.history.PLVHistoryConstant;
import com.plv.socket.event.history.PLVSpeakHistoryEvent;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.event.ppt.PLVPptShareFileVO;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.PLVRedPaperForDelayEvent;
import com.plv.socket.event.redpack.PLVRedPaperHistoryEvent;
import com.plv.socket.event.redpack.PLVRedPaperResultEvent;
import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.log.PLVELogSender;
import com.plv.socket.socketio.PLVSocketIOClient;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;
import kotlin.jvm.functions.Function1;

/**
 * mvp-聊天室presenter层实现，实现 IPLVChatroomContract.IChatroomPresenter 接口
 */
public class PLVChatroomPresenter implements IPLVChatroomContract.IChatroomPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVChatroomPresenter";
    //聊天消息最大数量
    public static final int CHAT_MESSAGE_MAX_LENGTH = 500;
    //定时检查聊天消息最大数量间隔
    public static final int CHECK_CHAT_MESSAGE_MAX_LENGTH_TIMESPAN = 30;
    //默认获取的历史记录条数
    public static final int GET_CHAT_HISTORY_COUNT = 10;
    //聊天信息处理间隔
    private static final int CHAT_MESSAGE_TIMESPAN = 500;
    //定时获取观看热度间隔
    private static final int GET_PAGE_VIEW_TIMESPAN = 60;

    // model
    private final PLVMultiRoomTransmitRepo multiRoomTransmitRepo = PLVDependManager.getInstance().get(PLVMultiRoomTransmitRepo.class);
    private final PLVRedpackRepo redpackRepo = PLVDependManager.getInstance().get(PLVRedpackRepo.class);
    private final PLVWelfareLotteryManager welfareLotteryManager = PLVDependManager.getInstance().get(PLVWelfareLotteryManager.class);

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
    //踢出人数
    private int kickCount;
    //是否专注特殊身份发言模式
    private boolean isFocusMode;

    //每次获取的历史记录条数
    private int getChatHistoryCount = GET_CHAT_HISTORY_COUNT;
    //获取历史记录成功的次数
    private int getChatHistoryTime;
    //获取提问记录的页数
    private int getQuizHistoryPage = 1;
    //是否没有更多历史记录
    private boolean isNoMoreChatHistory;
    //是否有请求历史记录的事件
    private boolean hasRequestHistoryEvent;
    //请求历史记录的viewIndex
    private int requestHistoryViewIndex;
    //获取历史记录的disposable
    private Disposable chatHistoryDisposable;
    private Disposable quizHistoryDisposable;
    //历史记录是否包含打赏事件
    private boolean isHistoryContainRewardEvent;
    // 最旧一条聊天消息的时间戳
    private Long oldestChatHistoryTimestamp = null;
    // 跟最旧一条聊天消息时间戳相同的消息数量（包含最旧一条）
    private Long oldestChatHistoryTimestampCount = null;

    //分组Id
    private String groupId;

    //图片表情列表的disposable
    private Disposable chatEmotionImagesDisposable;

    //请求踢出的用户列表的disposable
    private Disposable kickUsersDisposable;
    //定时获取观看热度的disposable
    private Disposable getPageViewDisposable;
    //定时检查聊天消息最大数量disposable
    private Disposable observeChatMessageListMaxLengthDisposable;
    // destroy时销毁
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    //聊天室功能开关数据观察者
    private Observer<PLVStatefulData<PolyvChatFunctionSwitchVO>> functionSwitchObserver;
    //直播详情数据观察者
    private Observer<PLVStatefulData<PolyvLiveClassDetailVO>> classDetailVOObserver;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="公共静态方法">
    public static String convertSpecialString(String input) {
        return input.replace("&lt;", "<")
                .replace("&lt", "<")
                .replace("&gt;", ">")
                .replace("&gt", ">")
                .replace("&yen;", "¥")
                .replace("&yen", "¥")
                .replace("&quot;", "\"")
                .replace("&nbsp;", " ")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVChatroomPresenter(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        chatroomData = new PLVChatroomData();
        subscribeChatroomMessage();
        requestPageViewTimer();
        observeLiveRoomData();
        observeChatMessageListMaxLength();
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
                PLVCommonLog.d(TAG, "chatroom onSendProhibitedWord: 发送的消息涉及违禁词");// no need i18n
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
        //添加房间开启关闭监听器
        PolyvChatroomManager.getInstance().addOnRoomStatusListener(new IPolyvChatroomManager.RoomStatusListener() {
            @Override
            public void onStatus(final boolean isClose) {
                PLVCommonLog.d(TAG, "chatroom onRoomStatus: " + isClose);
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onCloseRoomStatusChanged(isClose);
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
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    PLVRxBus.get().post(new PLVSocketMessage(listenEvent, message, event));
                }
            }
        });
        //添加socket登录状态监听器
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                if (status.getStatus() == PLVSocketStatus.STATUS_LOGINSUCCESS
                    || status.getStatus() == PLVSocketStatus.STATUS_RECONNECTSUCCESS) {
                    if (hasRequestHistoryEvent) {//登录成功后可以获取到分房间id，如果之前存在请求历史的事件，则请求历史记录
                        requestChatHistory(requestHistoryViewIndex);
                    }
                }
                if (status.getStatus() == PLVSocketStatus.STATUS_LOGINSUCCESS) {
                    liveRoomDataManager.setChatToken(PolyvSocketWrapper.getInstance().getChatToken());
                }
            }
        });
        //监听互动应用评论信息
        welfareLotteryManager.setJSLotteryCommentListener(new PLVWelfareLotteryManager.OnJSLotteryCommentListener() {
            @Override
            public void onJSLotteryComment(String comment) {
                PolyvLocalMessage message = new PolyvLocalMessage(comment);
                acceptLocalChatMessage(message, "");
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
                final String messageId = String.valueOf(args[0]);
                if (args.length == 1) {
                    acceptLocalChatMessage(textMessage, messageId);
                    return;
                }

                // 后台选择严禁词替换为*发出时，触发严禁词后会在args[1]返回实际发出内容
                final JsonObject jsonObject = PLVGsonUtil.fromJson(JsonObject.class, String.valueOf(args[1]));
                if (jsonObject != null && jsonObject.has("content")) {
                    textMessage.setSpeakMessage(jsonObject.get("content").getAsString());
                }
                acceptLocalChatMessage(textMessage, messageId);
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
                final String messageId = String.valueOf(args[0]);
                if (args.length == 1) {
                    acceptLocalChatMessage(textMessage, messageId);
                    return;
                }

                // 后台选择严禁词替换为*发出时，触发严禁词后会在args[1]返回实际发出内容
                final JsonObject jsonObject = PLVGsonUtil.fromJson(JsonObject.class, String.valueOf(args[1]));
                if (jsonObject != null && jsonObject.has("content")) {
                    textMessage.setSpeakMessage(jsonObject.get("content").getAsString());
                }
                acceptLocalChatMessage(textMessage, messageId);
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
    public Pair<Boolean, Integer> sendChatEmotionImage(final PLVChatEmotionEvent emotionEvent) {
        PLVCommonLog.d(TAG, "chatroom sendChatEmotionImage: " + liveRoomDataManager.getSessionId());
        int sendValue = PolyvChatroomManager.getInstance().sendEmotionImage(emotionEvent, new Ack() {
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
                 *    PLVCommonLog.d(TAG, "chatroom sendTextMessage: 发送的消息涉及违禁词");// no need i18n
                 *    return;
                 *}
                 */

                String id = String.valueOf(args[0]);
                final JsonObject jsonObject = PLVGsonUtil.fromJson(JsonObject.class, id);
                if (jsonObject != null && jsonObject.has("data")) {
                    JsonObject jsonObject1 = jsonObject.getAsJsonObject("data");
                    if (jsonObject1.has("messageId")) {
                        id = jsonObject1.get("messageId").getAsString();
                    }
                }
                acceptEmotionMessage(emotionEvent, id);
            }
        });
        if (sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//被禁言也认为发送成功，但不会广播给其他用户
            acceptEmotionMessage(emotionEvent, "");
        }
        PLVCommonLog.d(TAG, "chatroom sendChatEmotionImage: " + emotionEvent.getId() + ", sendValue: " + sendValue);
        return new Pair<>(sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP, sendValue);
    }

    @Override
    public void sendChatImage(final PolyvSendLocalImgEvent localImgEvent) {
        PLVCommonLog.d(TAG, "chatroom sendChatImage: " + localImgEvent.getImageFilePath() + ", sessionId: " + liveRoomDataManager.getSessionId());
        Disposable disposable = Observable.just(localImgEvent)
                .observeOn(Schedulers.computation())
                .doOnNext(new Consumer<PolyvSendLocalImgEvent>() {
                    @Override
                    public void accept(PolyvSendLocalImgEvent event) throws Exception {
                        event.setImageFilePath(PLVImageUtils.compressImage(PLVAppUtils.getApp(), event.getImageFilePath()));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PolyvSendLocalImgEvent>() {
                    @Override
                    public void accept(final PolyvSendLocalImgEvent event) throws Exception {
                        if (liveRoomDataManager == null) {
                            return;
                        }
                        PolyvChatroomManager.getInstance().sendChatImage(event, liveRoomDataManager.getSessionId());
                        event.setTime(System.currentTimeMillis());
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onLocalImageMessage(event);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
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
        /**
         * 设置自定义消息是否加入历史聊天记录，默认设置为加入
         * PLVCustomEvent.JOIN_HISTORY_TRUE为加入
         * PLVCustomEvent.JOIN_HISTORY_FALSE为不加入
         * */
        customEvent.setJoinHistory(PLVCustomEvent.JOIN_HISTORY_TRUE);
        customEvent.setTime(System.currentTimeMillis());
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
        // socket登录成功之后才能获取历史记录(适配聊天室分组模式)
        if (!PolyvSocketWrapper.getInstance().isOnlineStatus()) {
            hasRequestHistoryEvent = true;
            requestHistoryViewIndex = viewIndex;
            return;
        }
        hasRequestHistoryEvent = false;
        isNoMoreChatHistory = false;
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
        final String userId = liveRoomDataManager.getConfig().getUser().getViewerId();
        final String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        final String groupId = liveRoomDataManager.getConfig().getUser().getParam4();
        chatHistoryDisposable = PLVChatApiRequestHelper.getInstance().getChatHistory(getRoomIdCombineDiscuss(), userId, userType, groupId, oldestChatHistoryTimestamp, oldestChatHistoryTimestampCount, getChatHistoryCount)
                .map(new Function<String, JSONArray>() {
                    @Override
                    public JSONArray apply(String responseBody) throws Exception {
                        return new JSONArray(responseBody);
                    }
                })
                .compose(new PLVRxBaseTransformer<JSONArray, JSONArray>())
                .map(new Function<JSONArray, JSONArray>() {
                    @Override
                    public JSONArray apply(JSONArray jsonArray) throws Exception {
                        if (jsonArray.length() < getChatHistoryCount) {
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
    public int[] getSpeakEmojiSizes() {
        return getEmojiSizes(1);
    }

    @Override
    public void requestQuizHistory() {
        final String roomId = getRoomIdCombineDiscuss();
        final String viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
        final int page = getQuizHistoryPage;
        // end index included
        final int size = getChatHistoryCount;

        if (quizHistoryDisposable != null) {
            quizHistoryDisposable.dispose();
        }
        quizHistoryDisposable = PLVChatroomManager.getInstance().getHistoryQuestionMessage(roomId, viewerId, page, size)
                .doOnNext(new Consumer<List<PLVTAnswerEvent>>() {
                    @Override
                    public void accept(List<PLVTAnswerEvent> answerEvents) throws Exception {
                        foreach(answerEvents, new PLVSugarUtil.Consumer<PLVTAnswerEvent>() {
                            @Override
                            public void accept(PLVTAnswerEvent answerEvent) {
                                answerEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(answerEvent.getContent()), getQuizEmojiSizes(), Utils.getApp()));
                            }
                        });
                    }
                })
                .map(new Function<List<PLVTAnswerEvent>, List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public List<PLVBaseViewData<PLVBaseEvent>> apply(@NonNull List<PLVTAnswerEvent> answerEvents) throws Exception {
                        return transformList(answerEvents, new PLVSugarUtil.Function<PLVTAnswerEvent, PLVBaseViewData<PLVBaseEvent>>() {
                            @Override
                            public PLVBaseViewData<PLVBaseEvent> apply(PLVTAnswerEvent answerEvent) {
                                int itemType = isSendByMe(answerEvent) ? PLVChatMessageItemType.ITEMTYPE_SEND_QUIZ : PLVChatMessageItemType.ITEMTYPE_RECEIVE_QUIZ;
                                itemType = answerEvent.isImgEvent() ? PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG : itemType;
                                return new PLVBaseViewData<PLVBaseEvent>(answerEvent, itemType, new PLVSpecialTypeTag(answerEvent.getUserId()));
                            }
                        });
                    }

                    private /*static*/ boolean isSendByMe(final PLVTAnswerEvent answerEvent) {
                        final String eventUserId = nullable(new PLVSugarUtil.Supplier<String>() {
                            @Override
                            public String get() {
                                return answerEvent.getUser().getUserId();
                            }
                        });
                        return viewerId.equals(eventUserId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public void accept(final List<PLVBaseViewData<PLVBaseEvent>> answerEventViewData) throws Exception {
                        final boolean noMoreQuizHistory = answerEventViewData.size() < getChatHistoryCount;
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onQuizHistoryDataList(answerEventViewData, noMoreQuizHistory);
                            }
                        });
                        getQuizHistoryPage++;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(final Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onQuizHistoryRequestFailed(throwable);
                            }
                        });
                    }
                });
    }

    @Override
    public void requestKickUsers() {
        if (kickUsersDisposable != null) {
            kickUsersDisposable.dispose();
        }
        String loginRoomId = PolyvSocketWrapper.getInstance().getLoginRoomId();//分房间开启，在获取到后为分房间id，其他情况为频道号
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = getConfig().getChannelId();//socket未登录时，使用频道号
        }
        kickUsersDisposable = PLVChatApiRequestHelper.getKickUsers(loginRoomId)
                .subscribe(new Consumer<PLVKickUsersVO>() {
                    @Override
                    public void accept(final PLVKickUsersVO plvsKickUsersVO) throws Exception {
                        if (plvsKickUsersVO.getCode() == 200) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    kickCount = plvsKickUsersVO.getData().size();
                                    chatroomData.postKickCountData(kickCount);
                                    view.onKickUsersList(plvsKickUsersVO.getData());
                                }
                            });
                        } else {
                            PLVCommonLog.exception(new Throwable(plvsKickUsersVO.toString()));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //发送错误日志，便于排查问题
                        PLVELogSender.send(PolyvChatroomELog.class, PolyvChatroomELog.Event.GET_KICKUSERS_FAIL, throwable);
                    }
                });
    }

    @Override
    public int getChatHistoryTime() {
        return getChatHistoryTime;
    }

    @Override
    public void setHistoryContainRewardEvent(boolean historyContainRewardEvent) {
        this.isHistoryContainRewardEvent = historyContainRewardEvent;
    }

    @Override
    public void getChatEmotionImages() {
        //由于目前后端限制个性图片表情上限为50个，故此暂不做分页实现，默认写死
        int size = 100;
        int page = 1;
        String channel = PLVSocketIOClient.getInstance().getChannelId();
        String accountId = PLVSocketIOClient.getInstance().getAccountUserId();
        if (chatEmotionImagesDisposable != null){
            chatEmotionImagesDisposable.dispose();
        }
        chatEmotionImagesDisposable = PLVChatApiRequestHelper.getInstance().getEmotionImages(channel, accountId, page, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVResponseApiBean2<com.easefun.polyv.livescenes.model.PLVEmotionImageVO>>() {
                    @Override
                    public void accept(PLVResponseApiBean2<com.easefun.polyv.livescenes.model.PLVEmotionImageVO> polyvEmotionImageVO) throws Exception {
                        if (polyvEmotionImageVO != null && polyvEmotionImageVO.getData() != null && polyvEmotionImageVO.getData().getList() != null) {
                            List<PLVEmotionImageVO.EmotionImage> emotionImages = polyvEmotionImageVO.getData().getList();
                            chatroomData.postEmotionImages(emotionImages);
                            //初始化PLVFaceManager
                            PLVFaceManager.getInstance().initEmotionList(emotionImages);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    @Override
    public Observable<PLVCardPushVO> getCardPushInfo(String cardId) {
        return PLVChatApiRequestHelper.requestCardPushInfo(getConfig().getChannelId(), cardId);
    }

    @Override
    public boolean isCloseRoom() {
        return PolyvChatroomManager.getInstance().isCloseRoom();
    }

    @Override
    public void toggleRoom(boolean isClose, IPolyvChatroomManager.RequestApiListener<String> listener) {
        PolyvChatroomManager.getInstance().toggleRoom(isClose, listener);
    }

    @Override
    public void onJoinDiscuss(String groupId) {
        this.groupId = groupId;
        clearHistoryInfo();
    }

    @Override
    public void onLeaveDiscuss() {
        groupId = null;
        clearHistoryInfo();
    }

    @NonNull
    @Override
    public PLVChatroomData getData() {
        return chatroomData;
    }

    @Override
    public void setChatNickName(String nickName) {
        PLVChatroomManager.getInstance().setNickName(nickName);
    }

    @Override
    public void requestUpdateLiveViewerList() {
        String loginRoomId = PLVSocketWrapper.getInstance().getLoginRoomId();
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = getConfig().getChannelId();
        }
        final boolean isChatViewerGroup = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_CHAT_VIEWER_GROUP_ENABLE);
        final String groupId;
        if (isChatViewerGroup) {
            groupId = liveRoomDataManager.getConfig().getUser().getParam4();
        } else {
            groupId = null;
        }
        final Disposable disposable = PLVChatApiRequestHelper.getLiveViewerList(loginRoomId, groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<PLVLiveViewerListVO, List<PLVLiveViewerListVO.Data.LiveViewer>>() {
                    @Override
                    public List<PLVLiveViewerListVO.Data.LiveViewer> apply(@NonNull PLVLiveViewerListVO liveViewerListVO) throws Exception {
                        return liveViewerListVO.getSortedList(new Function1<PLVLiveViewerListVO.Data.LiveViewer, Integer>() {
                            private final List<String> sortOrder = listOf(
                                    "isMe-placeholder",
                                    PLVSocketUserConstant.USERTYPE_TEACHER,
                                    PLVSocketUserConstant.USERTYPE_GUEST,
                                    PLVSocketUserConstant.USERTYPE_MANAGER,
                                    PLVSocketUserConstant.USERTYPE_VIEWER,
                                    PLVSocketUserConstant.USERTYPE_ASSISTANT
                            );

                            @Override
                            public Integer invoke(PLVLiveViewerListVO.Data.LiveViewer liveViewer) {
                                final boolean isMe = PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(liveViewer.getUserId());
                                if (isMe) {
                                    return 0;
                                }
                                final int sortIndex = sortOrder.indexOf(liveViewer.getUserType());
                                if (sortIndex != -1) {
                                    return sortIndex;
                                }
                                return sortOrder.size();
                            }
                        });
                    }
                })
                .onErrorReturn(new Function<Throwable, List<PLVLiveViewerListVO.Data.LiveViewer>>() {
                    @Override
                    public List<PLVLiveViewerListVO.Data.LiveViewer> apply(@NonNull Throwable throwable) throws Exception {
                        PLVCommonLog.e(TAG, throwable.getMessage());
                        PLVCommonLog.exception(throwable);
                        return Collections.emptyList();
                    }
                })
                .map(new Function<List<PLVLiveViewerListVO.Data.LiveViewer>, List<PLVLiveViewerListVO.Data.LiveViewer>>() {
                    @Override
                    public List<PLVLiveViewerListVO.Data.LiveViewer> apply(@NonNull List<PLVLiveViewerListVO.Data.LiveViewer> liveViewers) throws Exception {
                        final PLVLiveViewerListVO.Data.LiveViewer first = liveViewers.isEmpty() ? null : liveViewers.get(0);
                        final boolean firstIsMe = first != null && PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(first.getUserId());
                        if (firstIsMe) {
                            first.setMe(true);
                            return liveViewers;
                        }

                        final PLVLiveViewerListVO.Data.LiveViewer liveViewerMe = new PLVLiveViewerListVO.Data.LiveViewer(
                                true,
                                PLVSocketWrapper.getInstance().getLoginVO().getActor(),
                                false,
                                PLVSocketWrapper.getInstance().getLoginVO().getChannelId(),
                                null,
                                null,
                                null,
                                PLVSocketWrapper.getInstance().getLoginVO().getNickName(),
                                PLVSocketWrapper.getInstance().getLoginVO().getParam4(),
                                PLVSocketWrapper.getInstance().getLoginVO().getParam5(),
                                PLVSocketWrapper.getInstance().getLoginVO().getAvatarUrl(),
                                PLVSocketWrapper.getInstance().getLoginRoomId(),
                                null,
                                null,
                                null,
                                PLVSocketWrapper.getInstance().getLoginVO().getUserId(),
                                PLVSocketWrapper.getInstance().getLoginVO().getUserType()
                        );
                        final List<PLVLiveViewerListVO.Data.LiveViewer> result = new ArrayList<>();
                        result.add(liveViewerMe);
                        result.addAll(liveViewers);
                        return result;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<List<PLVLiveViewerListVO.Data.LiveViewer>>() {
                            @Override
                            public void accept(final List<PLVLiveViewerListVO.Data.LiveViewer> liveViewers) throws Exception {
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                        view.onLiveViewerListUpdate(liveViewers);
                                    }
                                });
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                PLVCommonLog.e(TAG, throwable.getMessage());
                                PLVCommonLog.exception(throwable);
                            }
                        }
                );
        compositeDisposable.add(disposable);
    }

    @Override
    public void destroy() {
        clearHistoryInfo();
        compositeDisposable.dispose();
        if (iChatroomViews != null) {
            iChatroomViews.clear();
        }
        if (messageDisposable != null) {
            messageDisposable.dispose();
        }
        if (chatEmotionImagesDisposable != null){
            chatEmotionImagesDisposable.dispose();
        }
        if (getPageViewDisposable != null) {
            getPageViewDisposable.dispose();
        }
        if (kickUsersDisposable != null) {
            kickUsersDisposable.dispose();
        }
        if (observeChatMessageListMaxLengthDisposable != null) {
            observeChatMessageListMaxLengthDisposable.dispose();
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
                JSONObject jsonObject_user = jsonObject.optJSONObject("user");
                JSONObject jsonObject_content = jsonObject.optJSONObject("content");
                Long messageTimestamp = jsonObject.optLong("time");
                if (oldestChatHistoryTimestamp == null || messageTimestamp < oldestChatHistoryTimestamp) {
                    oldestChatHistoryTimestamp = messageTimestamp;
                    oldestChatHistoryTimestampCount = 1L;
                } else if (oldestChatHistoryTimestamp.equals(messageTimestamp)) {
                    oldestChatHistoryTimestampCount++;
                }
                if (!TextUtils.isEmpty(messageSource)) {
                    //收/发红包/图片信息/打赏信息，这里仅取图片信息
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
                        PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(chatImgHistory, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(chatImgHistory.getUser().getUserId()) : null);
                        tempChatItems.add(0, itemData);
                    } else if (PLVHistoryConstant.MSGSOURCE_REWARD.equals(messageSource)) {
                        //打赏信息
                        PLVRewardEvent.ContentBean rewardContentsBean = PolyvEventHelper.gson.fromJson(jsonObject_content.toString(), PLVRewardEvent.ContentBean.class);
                        PLVRewardEvent historyRewardEvent = new PLVRewardEvent();
                        if (rewardContentsBean != null) {
                            historyRewardEvent.setContent(rewardContentsBean);
                            historyRewardEvent.setRoomId(jsonObject_user.optInt("roomId"));

                            PLVCustomGiftEvent customGiftEvent = PLVCustomGiftEvent.generateCustomGiftEvent(historyRewardEvent);
                            PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(customGiftEvent, PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT);
                            if (isHistoryContainRewardEvent) {
                                tempChatItems.add(0, itemData);
                            }
                        }
                        //todo 处理打赏，积分打赏和普通打赏，都是同一个event，且都不为空，无法判断类型
                        PLVRewardEvent rewardEvent = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PLVRewardEvent.class);
                        if(rewardEvent != null){
                            if (jsonObject_user != null) {
                                rewardEvent.setRoomId(jsonObject_user.optInt("roomId"));
                                PLVCustomGiftEvent customGiftEvent = PLVCustomGiftEvent.generateCustomGiftEvent(rewardEvent);
                                PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(customGiftEvent, PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT);
                                if (isHistoryContainRewardEvent) {
                                    tempChatItems.add(0, itemData);
                                    continue;
                                }
                                String goodImage = rewardEvent.getContent().getGimg();
                                String nickName = rewardEvent.getContent().getUnick();
                                int goodNum = rewardEvent.getContent().getGoodNum();
                                Spannable rewardSpan = generateRewardSpan(nickName, goodImage, goodNum);
                                if (rewardSpan != null) {
                                    rewardEvent.setObjects(rewardSpan);
                                    int itemType = PLVChatMessageItemType.ITEMTYPE_REWARD;
                                    PLVBaseViewData chatTypeItem = new PLVBaseViewData<>(rewardEvent, itemType, false);
                                    tempChatItems.add(0, chatTypeItem);
                                }
                            }
                        }
                    } else if (PLVHistoryConstant.MSGSOURCE_FILE.equals(messageSource)) {
                        int itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                        PLVFileShareHistoryEvent fileShareEvent = PLVGsonUtil.fromJson(PLVFileShareHistoryEvent.class, jsonObject.toString());
                        if (fileShareEvent != null) {
                            parseFileShareEventFileData(fileShareEvent);
                            //如果是当前用户，则使用当前用户的昵称
                            if (PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(fileShareEvent.getUser().getUserId())) {
                                fileShareEvent.getUser().setNick(PLVSocketWrapper.getInstance().getLoginVO().getNickName());
                                itemType = PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK;
                            }
                            //把带表情的信息解析保存下来
                            fileShareEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(fileShareEvent.getContent()), speakEmojiSizes, Utils.getApp()));
                            PLVChatQuoteVO chatQuoteVO = fileShareEvent.getQuote();
                            if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                                chatQuoteVO.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(chatQuoteVO.getContent()), speakEmojiSizes, Utils.getApp()));
                            }
                            boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(fileShareEvent.getUser().getUserType())
                                    || PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(fileShareEvent.getUser().getUserId());
                            PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(fileShareEvent, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(fileShareEvent.getUser().getUserId()) : null);
                            tempChatItems.add(0, itemData);
                        }
                    } else if (PLVHistoryConstant.MSGSOURCE_RED_PAPER.equals(messageSource)) {
                        final PLVRedPaperHistoryEvent redPaperHistoryEvent = PLVGsonUtil.fromJson(PLVRedPaperHistoryEvent.class, jsonObject.toString());
                        if (redPaperHistoryEvent != null && PLVRedPaperType.isSupportType(redPaperHistoryEvent.getType())) {
                            final PLVRedPaperReceiveType cachedRedPaperReceiveType = redpackRepo.getCachedReceiveStatus(redPaperHistoryEvent.getRedpackId(), liveRoomDataManager.getConfig().getUser().getViewerId());
                            redPaperHistoryEvent.setReceiveTypeLiveData(mutableLiveData(cachedRedPaperReceiveType));
                            redpackRepo.cacheRedPaper(redPaperHistoryEvent.asRedPaperEvent());

                            PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(redPaperHistoryEvent, PLVChatMessageItemType.ITEMTYPE_RECEIVE_RED_PAPER, null);
                            tempChatItems.add(0, itemData);
                        }
                    }
                    continue;
                }
                if (jsonObject_user != null) {
                    String uid = jsonObject_user.optString("uid");
                    if (PLVHistoryConstant.UID_CUSTOMMSG.equals(uid)) {
                        //自定义信息，这里过滤掉
                        continue;
                    }
                    if (jsonObject_content != null) {
                        //content不为字符串的信息，这里过滤掉
                        continue;
                    }
                    int itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                    PLVSpeakHistoryEvent speakHistory = PLVGsonUtil.fromJson(PLVSpeakHistoryEvent.class, jsonObject.toString());
                    parseSpeakEventOverLength(speakHistory);
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
                    PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(speakHistory, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(speakHistory.getUser().getUserId()) : null);
                    tempChatItems.add(0, itemData);
                }
            }
        }
        return tempChatItems;
    }

    private void clearHistoryInfo() {
        getChatHistoryTime = 0;
        hasRequestHistoryEvent = false;
        isNoMoreChatHistory = false;
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
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
            String specialTypeUserId = null;
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
                            parseSpeakEventFileData(speakEvent);
                            parseSpeakEventOverLength(speakEvent);
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
                                specialTypeUserId = speakEvent.getUser().getUserId();
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(IPLVChatroomContract.IChatroomView view) {
                                        view.onSpeakEvent(speakEvent);
                                    }
                                });
                                if (!speakEvent.isFileShareEvent()) {
                                    chatroomData.postSpeakMessageData((CharSequence) speakEvent.getObjects()[0], isSpecialType);
                                }
                            }
                        }
                        break;
                    //图片类型发言
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_CHAT_IMG:
                        final PLVChatImgEvent chatImgEvent = PLVEventHelper.toMessageEventModel(message, PLVChatImgEvent.class);
                        if (chatImgEvent != null && chatImgEvent.getUser() != null &&
                                !PolyvSocketWrapper.getInstance().getLoginVO().getUserId().
                                        equals(chatImgEvent.getUser().getUserId())) {
                            chatMessage = chatImgEvent;
                            itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG;
                            isSpecialType = PLVEventHelper.isSpecialType(chatImgEvent.getUser().getUserType());
                            specialTypeUserId = chatImgEvent.getUser().getUserId();
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
                            final boolean isMyLoginEvent = PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(loginEvent.getUser().getUserId());
                            final long restrictMaxViewer = getOrDefault(PLVChannelFeatureManager.onChannel(getConfig().getChannelId()).get(PLVChannelFeature.LIVE_CHATROOM_RESTRICT_MAX_VIEWER), 0L);
                            final boolean restrictedByMaxViewer = restrictMaxViewer > 0 && loginEvent.getOnlineUserNumber() > restrictMaxViewer;
                            if (isMyLoginEvent && restrictedByMaxViewer) {
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                        view.onLoginError(
                                                loginEvent,
                                                getString(R.string.plv_chat_restrict_max_viewer_hint),
                                                PLVErrorCodeChatroomStatus.getCode(PLVErrorCodeChatroomStatus.SecondCode.CHATROOM_RESTRICT_MAX_VIEWER)
                                        );
                                    }
                                });
                                break;
                            }

                            chatroomData.postLoginEventData(loginEvent);
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onLoginEvent(loginEvent);
                                }
                            });
                            //如果不是自己的socket登录事件，则观看热度+1
                            if (!isMyLoginEvent) {
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
                            tAnswerEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(tAnswerEvent.getContent()), getQuizEmojiSizes(), Utils.getApp()));
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onAnswerEvent(tAnswerEvent);
                                }
                            });
                        }
                        break;
                    //打赏事件
                    case PLVEventConstant.Chatroom.EVENT_REWARD:
                        final PLVRewardEvent rewardEvent = PLVEventHelper.toMessageEventModel(message, PLVRewardEvent.class);
                        if(rewardEvent != null){
                            if (rewardEvent.getContent() != null) {
                                String goodImage = rewardEvent.getContent().getGimg();
                                String nickName = rewardEvent.getContent().getUnick();
                                int goodNum = rewardEvent.getContent().getGoodNum();
                                Spannable rewardSpan = generateRewardSpan(nickName, goodImage, goodNum);
                                if (rewardSpan != null) {
                                    rewardEvent.setObjects(rewardSpan);
                                }
                            }
                            itemType = PLVChatMessageItemType.ITEMTYPE_REWARD;
                            chatMessage = rewardEvent;
                            chatroomData.postRewardEvent(rewardEvent);
                            liveRoomDataManager.getRewardEventData().postValue(rewardEvent);
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    view.onRewardEvent(rewardEvent);
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
                    //onSliceId事件
                    case PLVEventConstant.Ppt.ON_SLICE_ID_EVENT:
                        final PLVOnSliceIDEvent onSliceIDEvent = PLVEventHelper.toMessageEventModel(message, PLVOnSliceIDEvent.class);
                        if (onSliceIDEvent != null && onSliceIDEvent.getData() != null) {
                            if (isFocusMode != onSliceIDEvent.getData().isFocusMode()) {
                                isFocusMode = onSliceIDEvent.getData().isFocusMode();
                                final PLVFocusModeEvent focusModeEvent = new PLVFocusModeEvent(isFocusMode);
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                        view.onFocusModeEvent(focusModeEvent);
                                    }
                                });
                            }
                            if (onSliceIDEvent.getData().getOnlineUserNumber() != 0) {
                                PLVChatroomManager.getInstance().setOnlineCount(onSliceIDEvent.getData().getOnlineUserNumber());
                            }
                            if (onSliceIDEvent.getData().getSpeakTop() != null) {
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                        view.onToTopEvent(onSliceIDEvent.getData().getSpeakTop());
                                    }
                                });
                            } else {
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                        view.onCancelTopEvent(new PLVCancelTopEvent());
                                    }
                                });
                            }
                        }
                        break;
                    case PLVRedPaperEvent.EVENT:
                        final PLVRedPaperEvent redPaperEvent = PLVEventHelper.toMessageEventModel(message, PLVRedPaperEvent.class);
                        if (redPaperEvent != null && PLVRedPaperType.isSupportType(redPaperEvent.getType())) {
                            final PLVRedPaperReceiveType cachedRedPaperReceiveType = redpackRepo.getCachedReceiveStatus(redPaperEvent.getRedpackId(), liveRoomDataManager.getConfig().getUser().getViewerId());
                            redPaperEvent.setReceiveTypeLiveData(mutableLiveData(cachedRedPaperReceiveType));
                            redpackRepo.cacheRedPaper(redPaperEvent);

                            chatMessage = redPaperEvent;
                            itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_RED_PAPER;
                        }
                        break;
                    case PLVRedPaperResultEvent.EVENT:
                        final PLVRedPaperResultEvent redPaperResultEvent = PLVEventHelper.toMessageEventModel(message, PLVRedPaperResultEvent.class);
                        if (redPaperResultEvent != null && PLVRedPaperType.isSupportType(redPaperResultEvent.getType())) {
                            redpackRepo.onRedPaperResultEvent(redPaperResultEvent);

//                            chatMessage = redPaperResultEvent;
//                            itemType = PLVChatMessageItemType.ITEMTYPE_RED_PAPER_RESULT;
                        }
                        break;
                    case PLVRedPaperForDelayEvent.EVENT:
                        final PLVRedPaperForDelayEvent redPaperForDelayEvent = PLVEventHelper.toMessageEventModel(message, PLVRedPaperForDelayEvent.class);
                        if (redPaperForDelayEvent != null && PLVRedPaperType.isSupportType(redPaperForDelayEvent.getType())) {
                            redpackRepo.onRedPaperForDelayEvent(redPaperForDelayEvent);
                        }
                        break;
                    default:
                        break;
                }
                if (chatMessage != null) {
                    chatMessageDataList.add(new PLVBaseViewData<>(chatMessage, itemType, isSpecialType ? new PLVSpecialTypeTag(specialTypeUserId) : null));
                }
            } else if (PLVEventConstant.EMOTION_EVENT.equals(listenEvent)) {
                final PLVChatEmotionEvent emotionEvent = PLVGsonUtil.fromJson(PLVChatEmotionEvent.class, message);
                if (emotionEvent != null) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onLoadEmotionMessage(emotionEvent);
                        }
                    });
                }
            } else if (PLVEventConstant.Interact.NEWS_PUSH.equals(listenEvent)) {
                //卡片推送事件
                if (PLVEventConstant.Interact.NEWS_PUSH_START.equals(event)) {
                    final PLVNewsPushStartEvent newsPushStartEvent = PLVGsonUtil.fromJson(PLVNewsPushStartEvent.class, message);
                    if (newsPushStartEvent != null) {
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onNewsPushStartMessage(newsPushStartEvent);
                            }
                        });
                    }
                } else if (PLVEventConstant.Interact.NEWS_PUSH_CANCEL.equals(event)) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onNewsPushCancelMessage();
                        }
                    });
                }
            } else if (PLVEventConstant.Chatroom.SE_FOCUS.equals(listenEvent)) {
                //专注特殊身份发言事件
                if (PLVEventConstant.Chatroom.FOCUS_EVENT_FOCUS_SPECIAL_SPEAK.equals(event)) {
                    final PLVFocusModeEvent focusModeEvent = PLVGsonUtil.fromJson(PLVFocusModeEvent.class, message);
                    if (focusModeEvent != null && isFocusMode != focusModeEvent.isOpen()) {
                        isFocusMode = focusModeEvent.isOpen();
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onFocusModeEvent(focusModeEvent);
                            }
                        });
                    }
                }
            } else if (PLVEventConstant.Chatroom.MESSAGE_EVENT_SPEAK_LOWERCASE.equals(listenEvent)) {
                if (PLVToTopEvent.EVENT.equals(event)) {
                    final PLVToTopEvent toTopEvent = PLVGsonUtil.fromJson(PLVToTopEvent.class, message);
                    if (toTopEvent != null) {
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onToTopEvent(toTopEvent);
                            }
                        });
                    }
                } else if (PLVCancelTopEvent.EVENT.equals(event)) {
                    final PLVCancelTopEvent cancelTopEvent = PLVGsonUtil.fromJson(PLVCancelTopEvent.class, message);
                    if (cancelTopEvent != null) {
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onCancelTopEvent(cancelTopEvent);
                            }
                        });
                    }
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

    private String getRoomIdCombineDiscuss() {
        if (!TextUtils.isEmpty(groupId)) {
            return groupId;
        }
        String loginRoomId = PolyvSocketWrapper.getInstance().getLoginRoomId();//分房间开启，在获取到后为分房间id，其他情况为频道号
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = getConfig().getChannelId();//socket未登录时，使用频道号
        }
        return loginRoomId;
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
        //生成当前消息的时间
        textMessage.setTime(System.currentTimeMillis());
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NotNull IPLVChatroomContract.IChatroomView view) {
                view.onLocalSpeakMessage(textMessage);
            }
        });
        welfareLotteryManager.sendCommentForLottery(textMessage.getSpeakMessage());

        chatroomData.postSpeakMessageData((CharSequence) textMessage.getObjects()[0], true);
    }

    private Spannable generateRewardSpan(String nickName, String goodImageUrl, int goodNum) {
        if(goodImageUrl.startsWith("//")){
            goodImageUrl = "https:"+goodImageUrl;
        }
        SpannableStringBuilder span = new SpannableStringBuilder(nickName + " " + PLVAppUtils.getString(R.string.plv_reward_give) + " p");
        int drawableSpanStart = span.length() - 1;
        int drawableSpanEnd = span.length();
        if (goodNum != 1) {
            span.append(" x" + goodNum);
        }
        Drawable drawable = PLVImageLoader.getInstance().getImageAsDrawable(Utils.getApp(), goodImageUrl);
        if (drawable == null) {
            return null;
        }
        int textSize = ConvertUtils.dp2px(12);
        drawable.setBounds(0, 0, textSize * 2, textSize * 2);
        span.setSpan(new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER), drawableSpanStart, drawableSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private void acceptEmotionMessage(final PLVChatEmotionEvent emotionEvent, String messageId) {
        emotionEvent.setMessageId(messageId);
        emotionEvent.setTime(System.currentTimeMillis());
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull @NotNull IPLVChatroomContract.IChatroomView view) {
                view.onLoadEmotionMessage(emotionEvent);
            }
        });
    }

    private static void parseSpeakEventFileData(@NonNull PLVSpeakEvent speakEvent) {
        if (!speakEvent.isFileShareEvent()) {
            return;
        }
        speakEvent.setFileData(createFileData(speakEvent.getValues().get(0)));
    }

    private static void parseFileShareEventFileData(@NonNull PLVFileShareHistoryEvent fileShareEvent) {
        fileShareEvent.setFileData(createFileData(fileShareEvent.getContent()));
    }

    @Nullable
    private static PLVPptShareFileVO createFileData(@NonNull String jsonContent) {
        final Map<String, String> content = PLVGsonUtil.fromJson(new TypeToken<Map<String, String>>() {}, jsonContent);
        if (content == null) {
            return null;
        }
        final String url = content.get("url");
        final String name = content.get("name");
        return new PLVPptShareFileVO()
                .setUrl(url)
                .setName(name)
                .setSuffix(name == null ? "" : name.substring(name.lastIndexOf(".") + 1));
    }

    private static void parseSpeakEventOverLength(final PLVSpeakEvent speakEvent) {
        if (speakEvent == null || !speakEvent.isOverLength() || speakEvent.getId() == null) {
            return;
        }
        speakEvent.setOverLengthFullMessage(new AsyncLazy<String>() {
            @Override
            public void onLazyInit(@NonNull final PLVSugarUtil.Consumer<String> initializer) {
                onInitOverLengthFullMessage(speakEvent.getId(), initializer);
            }
        });
    }

    private static void parseSpeakEventOverLength(final PLVSpeakHistoryEvent speakHistoryEvent) {
        if (speakHistoryEvent == null || !speakHistoryEvent.isOverLength() || speakHistoryEvent.getId() == null) {
            return;
        }
        speakHistoryEvent.setOverLengthFullMessage(new AsyncLazy<String>() {
            @Override
            public void onLazyInit(@NonNull final PLVSugarUtil.Consumer<String> initializer) {
                onInitOverLengthFullMessage(speakHistoryEvent.getId(), initializer);
            }
        });
    }

    private static void onInitOverLengthFullMessage(@NonNull final String msgId, @NonNull final PLVSugarUtil.Consumer<String> initializer) {
        PLVChatroomManager.getInstance().getOverLengthFullMessage(msgId, new Ack() {
            @Override
            public void call(Object... args) {
                if (args == null || args.length == 0 || args[0] == null || !(args[0] instanceof String)) {
                    return;
                }
                final PLVOverLengthMessageEvent overLengthMessageEvent;
                try {
                    overLengthMessageEvent = PLVGsonUtil.fromJson(PLVOverLengthMessageEvent.class, (String) args[0]);
                } catch (Exception e) {
                    PLVCommonLog.exception(e);
                    return;
                }
                if (!PLVOverLengthMessageEvent.validate(overLengthMessageEvent)) {
                    return;
                }
                initializer.accept(overLengthMessageEvent.getData().getContent());
            }
        });
    }
    // </editor-fold>
    
    // <editor-folder defaultstate="collapsed" desc="数据获取 - 定时获取更新观看热度">
    private void requestPageViewTimer() {
        getPageViewDisposable = Observable.interval(0, GET_PAGE_VIEW_TIMESPAN, TimeUnit.SECONDS)
                .flatMap(new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Long aLong) throws Exception {
                        return PLVChatApiRequestHelper.getPageView(liveRoomDataManager.getConfig().getChannelId());
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        viewerCount = aLong;
                        chatroomData.postViewerCountData(viewerCount);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }
    // </editor-folder>

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
                multiRoomTransmitRepo.updateChannelDetail(classDetailVO);
            }
        };
        liveRoomDataManager.getClassDetailVO().observeForever(classDetailVOObserver);
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="检查聊天消息最大数量">
    private void observeChatMessageListMaxLength() {
        if (observeChatMessageListMaxLengthDisposable != null) {
            observeChatMessageListMaxLengthDisposable.dispose();
        }
        observeChatMessageListMaxLengthDisposable = Observable.interval(CHECK_CHAT_MESSAGE_MAX_LENGTH_TIMESPAN, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        callbackToView(new ViewRunnable() {

                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onCheckMessageMaxLength(CHAT_MESSAGE_MAX_LENGTH);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }
    // </editor-folder>

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
