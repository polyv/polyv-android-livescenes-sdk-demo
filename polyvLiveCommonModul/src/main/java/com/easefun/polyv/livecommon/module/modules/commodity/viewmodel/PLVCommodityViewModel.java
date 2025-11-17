package com.easefun.polyv.livecommon.module.modules.commodity.viewmodel;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getNullableOrDefault;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.WorkerThread;

import com.easefun.polyv.livecommon.module.modules.commodity.PLVProductExplainActivity;
import com.easefun.polyv.livecommon.module.modules.commodity.model.PLVCommodityRepo;
import com.easefun.polyv.livecommon.module.modules.commodity.model.vo.PLVCommodityProductVO;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBus;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.commodity.PLVProductClickTimesEvent;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVCommodityViewModel implements IPLVLifecycleAwareDependComponent {

    private final PLVCommodityRepo commodityRepo;

    private final MutableLiveData<PLVCommodityUiState> commodityUiStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PLVProductClickTimesEvent> productClickTimesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> productDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> productExplainLiveData = new MutableLiveData<>();
    private final MutableLiveData<PLVProductContentBean> productRedactLiveData = new MutableLiveData<>();
    private final PLVCommodityUiState commodityUiState = new PLVCommodityUiState();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PLVCommodityViewModel(
            final PLVCommodityRepo commodityRepo
    ) {
        this.commodityRepo = commodityRepo;

        initUiState();
        observeProductEvent();
        observeProductExplainEvent();
    }

    private void initUiState() {
        commodityUiState.productContentBeanPushToShow = null;
        commodityUiStateLiveData.postValue(commodityUiState.copy());
    }

    private void observeProductEvent() {
        final Disposable disposable = commodityRepo.productObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnNext(new Consumer<PLVCommodityProductVO>() {
                    @Override
                    public void accept(PLVCommodityProductVO commodityProductVO) throws Exception {
                        reduceProductEventUpdate(commodityProductVO);
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

        final Disposable clickTimesDisposable = commodityRepo.productClickTimesObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnNext(new Consumer<PLVProductClickTimesEvent>() {
                    @Override
                    public void accept(PLVProductClickTimesEvent plvProductClickTimesEvent) throws Exception {
                        productClickTimesLiveData.postValue(plvProductClickTimesEvent);
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
        compositeDisposable.add(clickTimesDisposable);
    }

    private void observeProductExplainEvent() {
        Disposable disposable = PLVRxBus.get().toObservable(PLVProductExplainActivity.PLVProductExplainEvent.class)
                .subscribe(new Consumer<PLVProductExplainActivity.PLVProductExplainEvent>() {
                    @Override
                    public void accept(PLVProductExplainActivity.PLVProductExplainEvent plvProductExplainEvent) throws Exception {
                        productExplainLiveData.postValue(plvProductExplainEvent.isShow);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        // ignore
                    }
                });
        compositeDisposable.add(disposable);
    }

    @WorkerThread
    private void reduceProductEventUpdate(final PLVCommodityProductVO commodityProductVO) {
        final String message = commodityProductVO.getMessage().getMessage();
        final PLVProductEvent productEvent = PLVGsonUtil.fromJson(PLVProductEvent.class, message);
        if (productEvent == null) {
            return;
        }
        if (productEvent.isProductControlEvent()) {
            // 商品上架/新增/编辑/推送事件
            final PLVProductControlEvent productControlEvent = PLVEventHelper.toMessageEventModel(message, PLVProductControlEvent.class);
            if (productControlEvent == null || productControlEvent.getContent() == null) {
                return;
            }
            if (productControlEvent.isRedact()
                    && commodityUiState.productContentBeanPushToShow != null
                    && commodityUiState.productContentBeanPushToShow.getProductId() == productControlEvent.getContent().getProductId()) {
                // 编辑
                if (productControlEvent.getContent().getProductPushRule() == null) {
                    productControlEvent.getContent().setProductPushRule(commodityUiState.productContentBeanPushToShow.getProductPushRule());
                }
                commodityUiState.productContentBeanPushToShow = productControlEvent.getContent();
                commodityUiStateLiveData.postValue(commodityUiState.copy());
            }
            if (productControlEvent.isPush()) {
                // 推送
                commodityUiState.productContentBeanPushToShow = productControlEvent.getContent();
                commodityUiStateLiveData.postValue(commodityUiState.copyWithPushState());
            }
            if (productControlEvent.isRedact()) {
                // 编辑/AI卡片
                productRedactLiveData.postValue(productControlEvent.getContent());
            }
        } else if (productEvent.isProductRemoveEvent()) {
            // 商品下架/删除事件
            final PLVProductRemoveEvent productRemoveEvent = PLVEventHelper.toMessageEventModel(message, PLVProductRemoveEvent.class);
            if (productRemoveEvent == null || productRemoveEvent.getContent() == null || commodityUiState.productContentBeanPushToShow == null) {
                return;
            }
            if (commodityUiState.productContentBeanPushToShow.getProductId() == productRemoveEvent.getContent().getProductId()) {
                commodityUiState.productContentBeanPushToShow = null;
                commodityUiStateLiveData.postValue(commodityUiState.copy());
            }
        } else if (productEvent.isProductMoveEvent()) {
            // 商品上移/下移事件
//            final PLVProductMoveEvent productMoveEvent = PLVEventHelper.toMessageEventModel(message, PLVProductMoveEvent.class);
        } else if (productEvent.isProductMenuSwitchEvent()) {
            // 商品库开关事件
            final PLVProductMenuSwitchEvent productMenuSwitchEvent = PLVEventHelper.toMessageEventModel(message, PLVProductMenuSwitchEvent.class);
            final boolean enable = getNullableOrDefault(new PLVSugarUtil.Supplier<Boolean>() {
                @Override
                public Boolean get() {
                    return productMenuSwitchEvent.getContent().isEnabled();
                }
            }, false);
            if (commodityUiState.hasProductView == enable) {
                return;
            }
            commodityUiState.hasProductView = enable;
            if (!commodityUiState.hasProductView) {
                commodityUiState.productContentBeanPushToShow = null;
            }
            commodityUiStateLiveData.postValue(commodityUiState.copy());
        } else if (productEvent.isProductCancelPushEvent()) {
            if (commodityUiState.productContentBeanPushToShow != null) {
                commodityUiState.productContentBeanPushToShow = null;
                commodityUiStateLiveData.postValue(commodityUiState.copy());
            }
        }
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
    }

    public void onCloseProductPush() {
        commodityUiState.productContentBeanPushToShow = null;
        commodityUiStateLiveData.postValue(commodityUiState.copy());
    }

    public void notifyHasProductLayout(boolean hasProductLayout) {
        commodityUiState.hasProductView = hasProductLayout;
        commodityUiStateLiveData.postValue(commodityUiState.copy());
    }

    public void showProductLayoutOnLandscape() {
        commodityUiState.showProductViewOnLandscape = true;
        commodityUiStateLiveData.postValue(commodityUiState.copy());
    }

    public void onLandscapeProductLayoutHide() {
        commodityUiState.showProductViewOnLandscape = false;
        commodityUiStateLiveData.postValue(commodityUiState.copy());
    }

    public void onShowProductDetail(int productId) {
        productDetailLiveData.postValue(productId);
    }

    public LiveData<PLVCommodityUiState> getCommodityUiStateLiveData() {
        return commodityUiStateLiveData;
    }

    public LiveData<PLVProductClickTimesEvent> getProductClickTimesLiveData() {
        return productClickTimesLiveData;
    }

    public LiveData<Integer> getProductDetailLiveData() {
        return productDetailLiveData;
    }

    public LiveData<Boolean> getProductExplainLiveData() {
        return productExplainLiveData;
    }

    public LiveData<PLVProductContentBean> getProductRedactLiveData() {
        return productRedactLiveData;
    }
}
