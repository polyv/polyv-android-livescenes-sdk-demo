package com.easefun.polyv.livecommon.module.modules.log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.livescenes.log.PLVTrackLog;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.PLVRedPaperHistoryEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * trackLog辅助类
 */
public class PLVTrackLogHelper {
    private static final Map<Object, Disposable> READ_REDPACK_DISPOSABLE_MAP = new HashMap<>();
    private static WeakReference<Object> lastFirstRedpackRecyclerViewData;
    private static int lastRedpackRecyclerViewDataCount;
    private static PLVTrackLog.TrackProductPushData lastTrackProductPushData;

    /**
     * 追踪上报商品卡片曝光事件，当商品卡片 出现并且界面内可见 或者 从不可见到可见，则会上报
     */
    public static void trackReadProductPush(final View productLayout, boolean canHideByScroll, final IPLVLiveRoomDataManager liveRoomDataManager) {
        if (productLayout == null || liveRoomDataManager == null) {
            return;
        }
        PLVDependManager.getInstance().get(PLVCommodityViewModel.class).getCommodityUiStateLiveData()
                .observe((LifecycleOwner) productLayout.getContext(), new Observer<PLVCommodityUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVCommodityUiState plvCommodityUiState) {
                        if (plvCommodityUiState == null || plvCommodityUiState.productContentBeanPushToShow == null || !plvCommodityUiState.isPush) {
                            return;
                        }
                        lastTrackProductPushData = getTrackProductPushData(plvCommodityUiState.productContentBeanPushToShow);
                        productLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (PLVViewUtil.isViewVisible(productLayout) && productLayout.isShown()) {
                                    PLVTrackLog.TrackLogBaseData trackLogBaseData = getTrackLogBaseData(liveRoomDataManager);
                                    // 上报商品卡片曝光事件
                                    PLVTrackLog.getInstance().reportReadProductPushEvent(trackLogBaseData, lastTrackProductPushData);
                                }
                            }
                        }, 100);
                    }
                });
        if (!canHideByScroll) {
            return;
        }
        setupViewScrollChangedListener(productLayout, new ViewTreeObserver.OnScrollChangedListener() {
            private boolean isChangedToOtherTab = false;

            @Override
            public void onScrollChanged() {
                boolean isViewVisible = PLVViewUtil.isViewVisible(productLayout);
                boolean isViewShown = productLayout.isShown();
                if (!isViewVisible && isViewShown) {
                    isChangedToOtherTab = true;
                } else if (isViewVisible && isViewShown && isChangedToOtherTab && lastTrackProductPushData != null) {
                    isChangedToOtherTab = false;
                    PLVTrackLog.TrackLogBaseData trackLogBaseData = getTrackLogBaseData(liveRoomDataManager);
                    // 上报商品卡片曝光事件
                    PLVTrackLog.getInstance().reportReadProductPushEvent(trackLogBaseData, lastTrackProductPushData);
                }
            }
        });
    }

    /**
     * 追踪上报商品卡片点击事件，当点击商品卡片时，则会上报
     */
    public static void trackClickProductPush(IPLVLiveRoomDataManager liveRoomDataManager, PLVProductContentBean productContentBean) {
        PLVTrackLog.TrackLogBaseData trackLogBaseData = getTrackLogBaseData(liveRoomDataManager);
        // 上报商品卡片点击事件
        PLVTrackLog.getInstance().reportClickProductPushEvent(trackLogBaseData, productContentBean);
    }

    /**
     * 追踪上报红包曝光事件，当红包消息在 列表中出现并且界面内可见 或者 从不可见到可见，并且停留时间为500ms{@link PLVTrackLog#READ_REDPACK_NEED_TIME}时，则会上报
     */
    public static void trackReadRedpack(final RecyclerView chatRecyclerView, final List<PLVBaseViewData> dataList, final IPLVLiveRoomDataManager liveRoomDataManager) {
        if (chatRecyclerView == null || !(chatRecyclerView.getLayoutManager() instanceof LinearLayoutManager)
                || dataList == null || liveRoomDataManager == null) {
            return;
        }
        setupViewScrollChangedListener(chatRecyclerView, new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                checkTrackReadRedpack(chatRecyclerView, dataList, liveRoomDataManager);
            }
        });
    }

    private static void checkTrackReadRedpack(RecyclerView chatRecyclerView, List<PLVBaseViewData> dataList, final IPLVLiveRoomDataManager liveRoomDataManager) {
        if (!PLVViewUtil.isViewVisible(chatRecyclerView) || !chatRecyclerView.isShown()) {
            return;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) chatRecyclerView.getLayoutManager();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        if (dataList == null || dataList.size() == 0
                || firstVisibleItemPosition <= -1 || lastVisibleItemPosition <= -1 || lastVisibleItemPosition >= dataList.size()) {
            return;
        }
        Object firstVisibleItemData = dataList.get(firstVisibleItemPosition).getData();
        int visibleItemCount = lastVisibleItemPosition - firstVisibleItemPosition + 1;
        if (lastFirstRedpackRecyclerViewData != null && lastFirstRedpackRecyclerViewData.get() == firstVisibleItemData && lastRedpackRecyclerViewDataCount == visibleItemCount) {
            return;
        } else {
            lastFirstRedpackRecyclerViewData = new WeakReference<>(firstVisibleItemData);
            lastRedpackRecyclerViewDataCount = visibleItemCount;
        }
        checkTrackReadRedpackNext(firstVisibleItemPosition, lastVisibleItemPosition, dataList, liveRoomDataManager);
    }

    private static void checkTrackReadRedpackNext(int firstVisibleItemPosition, int lastVisibleItemPosition, List<PLVBaseViewData> dataList, final IPLVLiveRoomDataManager liveRoomDataManager) {
        List<Object> removeKeyList = new ArrayList<>();
        for (Map.Entry<Object, Disposable> entry : READ_REDPACK_DISPOSABLE_MAP.entrySet()) {
            boolean isInVisibleRange = false;
            for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
                if (entry.getKey() == dataList.get(i).getData()) {
                    isInVisibleRange = true;
                    break;
                }
            }
            if (!isInVisibleRange) {
                if (entry.getValue() != null) {
                    entry.getValue().dispose();
                }
                removeKeyList.add(entry.getKey());
            }
        }
        for (Object removeKey : removeKeyList) {
            READ_REDPACK_DISPOSABLE_MAP.remove(removeKey);
        }
        for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
            final Object data = dataList.get(i).getData();
            if (data instanceof PLVRedPaperEvent || data instanceof PLVRedPaperHistoryEvent) {
                if (READ_REDPACK_DISPOSABLE_MAP.containsKey(data)) {
                    continue;
                }
                Disposable disposable = Observable.just(1).delay(PLVTrackLog.READ_REDPACK_NEED_TIME, TimeUnit.MILLISECONDS)
                        .compose(new PLVRxBaseTransformer<Integer, Integer>())
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) throws Exception {
                                PLVTrackLog.TrackLogBaseData trackLogBaseData = getTrackLogBaseData(liveRoomDataManager);
                                String redpackId = null;
                                if (data instanceof PLVRedPaperEvent) {
                                    redpackId = ((PLVRedPaperEvent) data).getRedpackId();
                                } else if (data instanceof PLVRedPaperHistoryEvent) {
                                    redpackId = ((PLVRedPaperHistoryEvent) data).getRedpackId();
                                }
                                // 上报红包曝光事件
                                PLVTrackLog.getInstance().reportReadRedpackEvent(trackLogBaseData, redpackId);
                                // 这里不能使用remove，因为key还需要用来判断是否已经加入到发送队列/已经发送过
                                READ_REDPACK_DISPOSABLE_MAP.put(data, null);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                READ_REDPACK_DISPOSABLE_MAP.remove(data);
                                PLVCommonLog.exception(throwable);
                            }
                        });
                READ_REDPACK_DISPOSABLE_MAP.put(data, disposable);
            }
        }
    }

    private static void setupViewScrollChangedListener(final View view, final ViewTreeObserver.OnScrollChangedListener viewTreeObserverListener) {
        view.getViewTreeObserver().addOnScrollChangedListener(viewTreeObserverListener);
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            boolean isRemoveOnScrollChangedListener = false;

            @Override
            public void onViewAttachedToWindow(View v) {
                if (isRemoveOnScrollChangedListener) {
                    view.getViewTreeObserver().addOnScrollChangedListener(viewTreeObserverListener);
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                view.getViewTreeObserver().removeOnScrollChangedListener(viewTreeObserverListener);
                isRemoveOnScrollChangedListener = true;
            }
        });
    }

    private static PLVTrackLog.TrackLogBaseData getTrackLogBaseData(IPLVLiveRoomDataManager liveRoomDataManager) {
        PLVTrackLog.TrackLogBaseData trackLogBaseData = new PLVTrackLog.TrackLogBaseData();
        trackLogBaseData.channelId = liveRoomDataManager.getConfig().getChannelId();
        trackLogBaseData.sessionId = liveRoomDataManager.getSessionId();
        trackLogBaseData.viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
        trackLogBaseData.nickName = liveRoomDataManager.getConfig().getUser().getViewerName();
        trackLogBaseData.userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        return trackLogBaseData;
    }

    private static PLVTrackLog.TrackProductPushData getTrackProductPushData(PLVProductContentBean productContentBean) {
        PLVTrackLog.TrackProductPushData trackProductPushData = new PLVTrackLog.TrackProductPushData();
        trackProductPushData.name = productContentBean.getName();
        trackProductPushData.productId = productContentBean.getProductId() + "";
        trackProductPushData.realPrice = productContentBean.getRealPrice();
        trackProductPushData.price = productContentBean.getPrice();
        trackProductPushData.productType = productContentBean.getProductType();
        trackProductPushData.pushId = productContentBean.getLogId();
        return trackProductPushData;
    }
}
