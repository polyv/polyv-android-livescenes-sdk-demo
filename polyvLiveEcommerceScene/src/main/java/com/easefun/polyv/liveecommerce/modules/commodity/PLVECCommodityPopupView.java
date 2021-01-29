package com.easefun.polyv.liveecommerce.modules.commodity;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataRequester;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.model.commodity.saas.PolyvCommodityVO;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * 商品弹层view
 */
public class PLVECCommodityPopupView {
    private static final String TAG = "PLVECCommodityPopupView";
    // <editor-fold defaultstate="collapsed" desc="变量">
    private PopupWindow popupWindow;
    private ViewGroup emptyCommodityLy;
    private TextView commodityCountTv;
    //刷新视图view
    private SmoothRefreshLayout smoothRefreshLy;
    private RecyclerView commodityRv;
    private PLVECCommodityAdapter commodityAdapter;
    private PolyvCommodityVO commodityVO;
    private int totalCommodity;//商品总数
    private boolean isNoMoreData;//是否没有更多数据
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 显示、隐藏布局">
    public void showCommodityLayout(final View v, final PLVECCommodityAdapter.OnViewActionListener listener) {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(v.getContext());

            List<PLVBaseViewData> viewDataList = toViewDataList(commodityVO);

            View.OnClickListener handleHideListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            };
            View view = PLVViewInitUtils.initPopupWindow(v, R.layout.plvec_live_commodity_layout, popupWindow, handleHideListener);
            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            view.findViewById(R.id.close_iv).setOnClickListener(handleHideListener);

            emptyCommodityLy = view.findViewById(R.id.empty_commodity_ly);
            commodityCountTv = view.findViewById(R.id.commodity_count_tv);

            smoothRefreshLy = view.findViewById(R.id.smooth_refresh_ly);
            ClassicFooter<IIndicator> footerView = new ClassicFooter<>(view.getContext());
            footerView.setTitleTextColor(Color.parseColor("#B2B2B2"));
            smoothRefreshLy.setFooterView(footerView);
            smoothRefreshLy.setOnRefreshListener(new SmoothRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefreshing() {
                    PLVCommonLog.d(TAG,"smoothRefreshLy onRefreshing");
                }

                @Override
                public void onLoadingMore() {
                    if (listener != null) {
                        listener.onLoadMoreData(commodityAdapter.getLastExistedRank());
                    }
                }
            });
            smoothRefreshLy.setDisableRefresh(true);//禁用header刷新
            smoothRefreshLy.setEnableOverScroll(false);//禁用滚动回弹
            smoothRefreshLy.setLoadingMinTime(50);//设置最小关闭刷新动画的时间
            smoothRefreshLy.setDurationToCloseFooter(0);//设置关闭footer的时间
            smoothRefreshLy.setEnableCompatSyncScroll(false);//关闭footer刷新时会显示下一个item
            smoothRefreshLy.setDisableLoadMoreWhenContentNotFull(false);//数据没满时关闭footer

            commodityRv = view.findViewById(R.id.commodity_rv);
            commodityRv.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(commodityRv.getContext(), LinearLayoutManager.VERTICAL, false);
            commodityRv.setLayoutManager(linearLayoutManager);
            commodityRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(20), 0));

            commodityAdapter = new PLVECCommodityAdapter();
            commodityAdapter.setOnViewActionListener(new PLVECCommodityAdapter.OnViewActionListener() {
                @Override
                public void onBuyCommodityClick(View view, PLVProductContentBean contentsBean) {
                    if (listener != null) {
                        listener.onBuyCommodityClick(view, contentsBean);
                    }
                }

                @Override
                public void onLoadMoreData(int rank) {
                    if (listener != null) {
                        listener.onLoadMoreData(rank);
                    }
                }
            });
            commodityAdapter.setDataList(viewDataList);

            commodityRv.setAdapter(commodityAdapter);

            if (commodityAdapter.getItemCount() == 0) {
                emptyCommodityLy.setVisibility(commodityVO == null ? View.GONE : View.VISIBLE);
            }

            updateNoMoreDataStatus(viewDataList);
            updateCountMessageView();
        }
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    //隐藏
    public void hide() {
        if (popupWindow != null) {
            popupWindow.dismiss();

            smoothRefreshLy.refreshComplete();//刷新完成
            smoothRefreshLy.setDisableLoadMore(false);
            smoothRefreshLy.setEnableAutoLoadMore(false);//禁用自动加载更多
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 商品操作">
    //新增/上架商品
    public void add(PLVProductContentBean productContentBean, boolean isNewly) {
        if (popupWindow == null || !popupWindow.isShowing()) {
            return;
        }
        if (productContentBean == null || productContentBean.isPullOffShelvesStatus()) {//下架的不处理
            return;
        }
        if (isNewly) {//新增商品，直接添加到列表顶部
            commodityAdapter.add(0, productContentBean);
            commodityRv.scrollToPosition(0);
            totalCommodity++;
            updateCountMessageView();
            emptyCommodityLy.setVisibility(View.GONE);
        } else {//上架商品
            //列表中最后item的rank
            int lastExistedRank = commodityAdapter.getLastExistedRank();
            int rank = productContentBean.getRank();//上架商品的rank
            if ((lastExistedRank == -1 && !isNoMoreData) || (rank < lastExistedRank && !isNoMoreData)) {//列表未初始化||上架商品的rank小于列表最后的rank，并且还可以加载更多数据，则不处理
                return;
            }
            if (commodityAdapter.getItemCount() == 0 || //如果列表中没有数据
                    (rank == lastExistedRank && commodityAdapter.isExistProduct(productContentBean.getProductId()) == -1)) {//如果和列表最后的rank相同，并且不存在列表中
                commodityAdapter.add(commodityAdapter.getItemCount(), productContentBean);
                totalCommodity++;
                updateCountMessageView();
                emptyCommodityLy.setVisibility(View.GONE);
                return;
            }
            int index = -1;
            for (PLVBaseViewData baseViewData : commodityAdapter.getDataList()) {
                index++;
                PLVProductContentBean contentBean = (PLVProductContentBean) baseViewData.getData();
                if ((rank > contentBean.getRank()) ||//上架商品rank比当前比较商品的rank大
                        (index == commodityAdapter.getItemCount() - 1 && rank < contentBean.getRank())) {//不能加载更多数据，并比最后一条数据的rank还小
                    commodityAdapter.add(rank > contentBean.getRank() ? index : index + 1, productContentBean);
                    totalCommodity++;
                    updateCountMessageView();
                    emptyCommodityLy.setVisibility(View.GONE);
                    break;
                }
            }
        }
    }

    //更新商品
    public void update(PLVProductContentBean productContentBean) {
        if (popupWindow == null || !popupWindow.isShowing()) {
            return;
        }
        if (productContentBean == null) {
            return;
        }
        if (productContentBean.isPutOnShelvesStatus()) {//如果是更新上架商品
            int index;
            if ((index = commodityAdapter.isExistProduct(productContentBean.getProductId())) != -1) {//如果列表中存在该商品
                commodityAdapter.update(index, productContentBean);
            } else {//列表中不存在该商品
                add(productContentBean, false);
            }
        } else if (productContentBean.isPullOffShelvesStatus()) {//如果是更新下架商品
            delete(productContentBean.getProductId());
        }
    }

    //删除/下架商品
    public void delete(int productId) {
        if (popupWindow == null || !popupWindow.isShowing()) {
            return;
        }
        int index;
        if ((index = commodityAdapter.isExistProduct(productId)) != -1) {//如果列表中存在该商品
            commodityAdapter.delete(index);
            totalCommodity--;
            updateCountMessageView();
            if (commodityAdapter.getItemCount() <= 0 && isNoMoreData) {
                emptyCommodityLy.setVisibility(View.VISIBLE);
            }
        }
    }

    //上/下移商品
    public void move(PLVProductMoveEvent productMoveEvent) {
        if (popupWindow == null || !popupWindow.isShowing()) {
            return;
        }
        if (productMoveEvent == null || productMoveEvent.getContent() == null || productMoveEvent.getContent().size() < 2) {
            return;
        }
        PLVProductContentBean handleContent = productMoveEvent.getContent().get(0);//操作的商品
        PLVProductContentBean contentBean = productMoveEvent.getContent().get(1);
        commodityAdapter.move(handleContent, contentBean);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 商品设置、添加数据">
    public void setCommodityVO(PolyvCommodityVO commodityVO) {
        this.commodityVO = commodityVO;

        List<PLVBaseViewData> viewDataList = toViewDataList(commodityVO);

        if (commodityVO == null) {//清空数据
            isNoMoreData = false;
            totalCommodity = -1;
        }

        if (commodityVO != null && commodityVO.getData() != null) {
            totalCommodity = commodityVO.getData().getTotal();
        }

        if (commodityAdapter != null) {
            commodityAdapter.setDataList(viewDataList);
            commodityAdapter.notifyDataSetChanged();
        }

        if (emptyCommodityLy != null && commodityAdapter != null && commodityAdapter.getItemCount() == 0) {
            emptyCommodityLy.setVisibility(commodityVO == null ? View.GONE : View.VISIBLE);
        }

        updateNoMoreDataStatus(viewDataList);
        updateCountMessageView();
    }

    public void addCommodityVO(PolyvCommodityVO commodityVO) {
        if (commodityVO == null) {
            if (smoothRefreshLy != null) {
                smoothRefreshLy.refreshComplete();
            }
            return;
        }

        List<PLVBaseViewData> viewDataList = toViewDataList(commodityVO);

        if (commodityVO.getData() != null) {
            totalCommodity = commodityVO.getData().getTotal();
        }

        if (commodityAdapter != null) {
            commodityAdapter.addDataList(viewDataList);
        }

        updateNoMoreDataStatus(viewDataList);
        updateCountMessageView();
    }

    private List<PLVBaseViewData> toViewDataList(PolyvCommodityVO commodityVO) {
        List<PLVBaseViewData> viewDataList = new ArrayList<>();
        if (commodityVO != null && commodityVO.getData() != null) {
            List<PLVProductContentBean> contentsBeanList = commodityVO.getData().getContent();
            if (contentsBeanList != null) {
                for (PLVProductContentBean contentsBean : contentsBeanList) {
                    viewDataList.add(new PLVBaseViewData<>(contentsBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));
                }
            }
        }
        return viewDataList;
    }

    private void updateNoMoreDataStatus(List<PLVBaseViewData> viewDataList) {
        if (commodityVO != null && viewDataList.size() < PLVLiveRoomDataRequester.GET_COMMODITY_COUNT) {//每次加载20条数据，<20表示没有更多数据了
            isNoMoreData = true;
            if (smoothRefreshLy != null) {
                smoothRefreshLy.refreshComplete();
                smoothRefreshLy.setDisableLoadMore(true);
            }
        } else {
            if (smoothRefreshLy != null) {
                smoothRefreshLy.refreshComplete();
                smoothRefreshLy.setEnableAutoLoadMore(true);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="商品 - 更新商品数view">
    private void updateCountMessageView() {
        if (commodityCountTv != null) {
            if (totalCommodity > -1) {
                SpannableStringBuilder span = new SpannableStringBuilder("共" + totalCommodity + "件商品");
                span.setSpan(new ForegroundColorSpan(Color.parseColor("#FFA611")), 1, (totalCommodity + "").length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                commodityCountTv.setText(span);
            } else {
                commodityCountTv.setText("");
            }
        }
    }
    // </editor-fold>
}
