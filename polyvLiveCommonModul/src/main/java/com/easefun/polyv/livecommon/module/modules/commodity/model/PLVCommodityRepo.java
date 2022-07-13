package com.easefun.polyv.livecommon.module.modules.commodity.model;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.commodity.model.vo.PLVCommodityProductVO;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketMessage;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBus;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.commodity.PLVProductEvent;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVCommodityRepo implements IPLVLifecycleAwareDependComponent {

    public Observable<PLVCommodityProductVO> productObservable = Observable.create(new ObservableOnSubscribe<PLVCommodityProductVO>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<PLVCommodityProductVO> emitter) throws Exception {
            productEmitter = emitter;
        }
    });

    private Emitter<PLVCommodityProductVO> productEmitter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PLVCommodityRepo() {
        observeSocketMessage();
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
    }

    private void observeSocketMessage() {
        final Disposable disposable = PLVRxBus.get().toObservable(PLVSocketMessage.class)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .filter(new Predicate<PLVSocketMessage>() {
                    @Override
                    public boolean test(@NonNull PLVSocketMessage message) throws Exception {
                        final boolean isMessageEvent = PLVEventConstant.MESSAGE_EVENT.equals(message.getListenEvent());
                        final boolean isProductEvent = PLVEventConstant.Chatroom.EVENT_PRODUCT_MESSAGE.equals(message.getEvent());
                        final boolean checkSocketValid = nullable(new PLVSugarUtil.Supplier<String>() {
                            @Override
                            public String get() {
                                return PLVSocketWrapper.getInstance().getLoginVO().getChannelId();
                            }
                        }) != null;
                        return isMessageEvent && isProductEvent && checkSocketValid;
                    }
                })
                .doOnNext(new Consumer<PLVSocketMessage>() {
                    @Override
                    public void accept(PLVSocketMessage message) throws Exception {
                        final PLVProductEvent productEvent = PLVGsonUtil.fromJson(PLVProductEvent.class, message.getMessage());
                        productEmitter.onNext(new PLVCommodityProductVO(productEvent, message));
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                })
                .retry()
                .subscribe();
        compositeDisposable.add(disposable);
    }

}
