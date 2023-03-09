package com.easefun.polyv.livecommon.module.modules.chatroom.model.datasource;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.vo.PLVManagerChatHistoryLoadStatus;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.socket.event.history.PLVHistoryConstant;
import com.plv.socket.event.history.PLVSpeakHistoryEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVManagerChatHistoryDataSource {

    // <editor-fold defaultstate="collapsed" desc="对外 - 数据源">

    public final Observable<PLVBaseViewData<PLVBaseEvent>> chatEventObservable = Observable.create(
            new ObservableOnSubscribe<PLVBaseViewData<PLVBaseEvent>>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<PLVBaseViewData<PLVBaseEvent>> observableEmitter) {
                    PLVManagerChatHistoryDataSource.this.chatEventEmitter = observableEmitter;
                }
            }
    );

    public final Observable<PLVManagerChatHistoryLoadStatus> loadStatusObservable = Observable.create(
            new ObservableOnSubscribe<PLVManagerChatHistoryLoadStatus>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<PLVManagerChatHistoryLoadStatus> observableEmitter) {
                    PLVManagerChatHistoryDataSource.this.loadStatusEmitter = observableEmitter;
                }
            }
    );

    // </editor-fold>

    private ObservableEmitter<PLVBaseViewData<PLVBaseEvent>> chatEventEmitter;
    private ObservableEmitter<PLVManagerChatHistoryLoadStatus> loadStatusEmitter;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private PLVManagerChatHistoryLoadStatus lastLoadStatus = new PLVManagerChatHistoryLoadStatus();

    public void requestChatHistory(final String roomId, final int start, final int end) {
        updateLoadStatus(lastLoadStatus.copy().setLoading(true));
        final Disposable disposable = PLVChatroomManager.getExtendChatHistory2(roomId, start, end)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<String, JSONArray>() {
                    @Override
                    public JSONArray apply(@NonNull String responseBody) throws Exception {
                        return new JSONArray(responseBody);
                    }
                })
                .doOnNext(new Consumer<JSONArray>() {
                    @Override
                    public void accept(JSONArray jsonArray) {
                        final boolean canLoadMore = jsonArray != null && jsonArray.length() > 0;
                        updateLoadStatus(lastLoadStatus.copy().setLoading(false).setCanLoadMore(canLoadMore));
                    }
                })
                .map(new Function<JSONArray, List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public List<PLVBaseViewData<PLVBaseEvent>> apply(@NonNull JSONArray jsonArray) {
                        return acceptChatHistory(jsonArray);
                    }
                })
                .subscribe(new Consumer<List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public void accept(List<PLVBaseViewData<PLVBaseEvent>> plvBaseViewDatas) {
                        if (chatEventEmitter == null) {
                            return;
                        }
                        for (PLVBaseViewData<PLVBaseEvent> plvBaseViewData : plvBaseViewDatas) {
                            chatEventEmitter.onNext(plvBaseViewData);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    /**
     * @see com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter#acceptChatHistory(JSONArray, int[])
     */
    private List<PLVBaseViewData<PLVBaseEvent>> acceptChatHistory(JSONArray jsonArray) {
        final List<PLVBaseViewData<PLVBaseEvent>> tempChatItems = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                String msgType = jsonObject.optString("msgType");
                if (!TextUtils.isEmpty(msgType)) {
                    //custom message
                    continue;
                }
                String messageSource = jsonObject.optString("msgSource");
                JSONObject jsonObjectUser = jsonObject.optJSONObject("user");
                JSONObject jsonObjectContent = jsonObject.optJSONObject("content");
                if (!TextUtils.isEmpty(messageSource) && !"null".equals(messageSource)) {
                    //收/发红包/图片信息/打赏信息，这里仅取图片信息
                    if (PLVHistoryConstant.MSGSOURCE_CHATIMG.equals(messageSource)) {
                        PLVChatImgHistoryEvent chatImgHistory = PLVGsonUtil.fromJson(PLVChatImgHistoryEvent.class, jsonObject.toString());
                        //如果是当前用户，则使用当前用户的昵称
                        if (chatImgHistory != null && PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatImgHistory.getUser().getUserId())) {
                            chatImgHistory.getUser().setNick(PLVSocketWrapper.getInstance().getLoginVO().getNickName());
                        }
                        PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(chatImgHistory, PLVChatMessageItemType.ITEMTYPE_UNDEFINED);
                        tempChatItems.add(0, itemData);
                    }
                    continue;
                }
                if (jsonObjectUser != null) {
                    String uid = jsonObjectUser.optString("uid");
                    if (PLVHistoryConstant.UID_CUSTOMMSG.equals(uid)) {
                        //自定义信息，这里过滤掉
                        continue;
                    }
                    if (jsonObjectContent != null) {
                        //content不为字符串的信息，这里过滤掉
                        continue;
                    }
                    PLVSpeakHistoryEvent speakHistory = PLVGsonUtil.fromJson(PLVSpeakHistoryEvent.class, jsonObject.toString());
                    //如果是当前用户，则使用当前用户的昵称
                    if (speakHistory != null && PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(speakHistory.getUser().getUserId())) {
                        speakHistory.getUser().setNick(PLVSocketWrapper.getInstance().getLoginVO().getNickName());
                    }
                    PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(speakHistory, PLVChatMessageItemType.ITEMTYPE_UNDEFINED);
                    tempChatItems.add(0, itemData);
                }
            }
        }
        return tempChatItems;
    }

    private static String convertSpecialString(String input) {
        String output;
        output = input.replace("&lt;", "<");
        output = output.replace("&lt", "<");
        output = output.replace("&gt;", ">");
        output = output.replace("&gt", ">");
        output = output.replace("&yen;", "¥");
        output = output.replace("&yen", "¥");
        return output;
    }

    private void updateLoadStatus(PLVManagerChatHistoryLoadStatus loadStatus) {
        lastLoadStatus = loadStatus;
        if (loadStatusEmitter != null) {
            loadStatusEmitter.onNext(loadStatus);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        disposables.dispose();
        super.finalize();
    }
}
