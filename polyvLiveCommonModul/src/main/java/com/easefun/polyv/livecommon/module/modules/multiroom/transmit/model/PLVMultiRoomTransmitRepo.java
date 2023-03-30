package com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model;

import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.enums.PLVMultiRoomTransmitMode;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.vo.PLVMultiRoomTransmitVO;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.transmit.PLVChangeDoubleModeEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.socketio.PLVSocketIOObservable;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author Hoshiiro
 */
public class PLVMultiRoomTransmitRepo {

    public final Observable<PLVMultiRoomTransmitVO> transmitObservable = Observable.create(new ObservableOnSubscribe<PLVMultiRoomTransmitVO>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<PLVMultiRoomTransmitVO> emitter) throws Exception {
            transmitDataEmitter = emitter;
        }
    }).publish().autoConnect();

    @Nullable
    private ObservableEmitter<PLVMultiRoomTransmitVO> transmitDataEmitter;
    private PLVMultiRoomTransmitVO lastTransmitData = new PLVMultiRoomTransmitVO.Builder().build();

    public PLVMultiRoomTransmitRepo() {
        observeLoginResult();
        observeTransmitModeChangeEvent();
    }

    private void observeLoginResult() {
        PLVSocketWrapper.getInstance().getSocketObserver().addOnSocketEventListener(new PLVSocketIOObservable.OnSocketEventListener() {
            @Override
            public void onMessage(String listenEvent, Object... args) {
                if (!PLVEventConstant.LOGIN_ACK_EVENT.equals(listenEvent) || args.length < 2) {
                    return;
                }
                try {
                    final JSONObject transmitData = (JSONObject) args[1];
                    final String transmitDoubleMode = transmitData.getString(PLVEventConstant.LOGIN_ACK_KEY_TRANSMIT_DOUBLE_MODE);
                    final PLVMultiRoomTransmitMode transmitMode = requireNotNull(PLVMultiRoomTransmitMode.match(transmitDoubleMode));
                    onLoginUpdateTransmitMode(transmitMode);
                } catch (Exception e) {
                    PLVCommonLog.exception(e);
                }
            }
        }, PLVEventConstant.LOGIN_ACK_EVENT);
    }

    private void observeTransmitModeChangeEvent() {
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (!PLVEventConstant.Transmit.SOCKET_EVENT_TRANSMIT.equals(listenEvent)
                        || !PLVEventConstant.Transmit.EVENT_CHANGE_DOUBLE_MODE.equals(event)) {
                    return;
                }
                final PLVChangeDoubleModeEvent changeDoubleModeEvent = PLVGsonUtil.fromJson(PLVChangeDoubleModeEvent.class, message);
                if (changeDoubleModeEvent == null) {
                    return;
                }
                onTransmitModeChanged(changeDoubleModeEvent);
            }
        }, PLVEventConstant.Transmit.SOCKET_EVENT_TRANSMIT);
    }

    public void updateChannelDetail(@NonNull PLVLiveClassDetailVO liveClassDetailVO) {
        lastTransmitData = new PLVMultiRoomTransmitVO.Builder().copy(lastTransmitData)
                .setMainRoomChannelId(String.valueOf(liveClassDetailVO.getData().getMasterChannelId()))
                .setMainRoomStream(liveClassDetailVO.getData().getMasterStream())
                .build();
        if (transmitDataEmitter != null) {
            transmitDataEmitter.onNext(lastTransmitData);
        }
    }

    private void onLoginUpdateTransmitMode(@NonNull PLVMultiRoomTransmitMode transmitMode) {
        lastTransmitData = new PLVMultiRoomTransmitVO.Builder().copy(lastTransmitData)
                .setTransmitMode(transmitMode)
                .build();
        if (transmitDataEmitter != null) {
            transmitDataEmitter.onNext(lastTransmitData);
        }
    }

    private void onTransmitModeChanged(@NonNull PLVChangeDoubleModeEvent changeDoubleModeEvent) {
        final PLVMultiRoomTransmitMode transmitMode = PLVMultiRoomTransmitMode.match(changeDoubleModeEvent.getMode());
        if (transmitMode == null) {
            return;
        }

        lastTransmitData = new PLVMultiRoomTransmitVO.Builder().copy(lastTransmitData)
                .setTransmitMode(transmitMode)
                .setMainRoomSessionId(changeDoubleModeEvent.getSessionId())
                .build();
        if (transmitDataEmitter != null) {
            transmitDataEmitter.onNext(lastTransmitData);
        }
    }

}
