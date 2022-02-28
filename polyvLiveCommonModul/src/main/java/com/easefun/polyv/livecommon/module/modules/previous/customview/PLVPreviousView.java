package com.easefun.polyv.livecommon.module.modules.previous.customview;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.view.PLVAbsPreviousView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livescenes.model.PLVPlaybackListVO;

import java.util.ArrayList;
import java.util.List;

import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.header.MaterialHeader;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * Author:lzj
 * Time:2021/12/28
 * Description: 回放视频列表layout
 */
public class PLVPreviousView extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private SmoothRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private TextView errorTv;
    private PLVPreviousAdapter previousAdapter;
    //回放视频列表信息
    private PLVPlaybackListVO plvPlaybackInfo;
    //presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPresenter;
    //切换vid前的监听
    private PLVPreviousViewInterface.OnPrepareChangeVideoVidListener prepareChangeVideoVidListener;

    //回放视频的视频列表
    private final List<PLVPlaybackListVO.DataBean.ContentsBean> dataList = new ArrayList<>();
    //加载更多的view
    private PLVFootView footView;
    //刷新框架的刷新view
    private MaterialHeader<IIndicator> header;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVPreviousView(Context context) {
        this(context, null);
    }

    public PLVPreviousView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPreviousView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.plv_playback_previous_layout, this, true);
        refreshLayout = view.findViewById(R.id.plv_previous_refreshLy);
        recyclerView = view.findViewById(R.id.plv_previous_rv);
        errorTv = view.findViewById(R.id.plv_previous_request_error_tv);

        footView = new PLVFootView(getContext());
        //默认不显示，加载时才显示
        footView.setVisibility(View.INVISIBLE);
        //默认禁用刷新，开启加载更多
        refreshLayout.setDisableRefresh(true);
        refreshLayout.setDisableLoadMore(false);
        refreshLayout.setEnableOverScroll(false);//禁用滚动回弹
        refreshLayout.setDisableLoadMoreWhenContentNotFull(false);//数据没满时关闭footer
        refreshLayout.setFooterView(footView);
        header = new MaterialHeader<>(getContext());
        header.setColorSchemeColors(new int[]{getContext().getResources().getColor(R.color.dodgerBlue)});
        refreshLayout.setHeaderView(header);
        refreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshing() {
                if (previousPresenter != null) {
                    previousPresenter.requestPreviousList();
                }
            }

            @Override
            public void onLoadingMore() {
                footView.setVisibility(View.VISIBLE);
                if (previousPresenter != null) {
                    previousPresenter.requestLoadMorePreviousVideo();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外提供的方法">

    /**
     * 为PLVPreviousView设置参数
     *
     * @param builder builder
     */
    public void setParams(Builder builder) {
        if (builder.layoutManager != null) {
            recyclerView.setLayoutManager(builder.layoutManager);
        }
        if (builder.itemDecoration != null) {
            recyclerView.addItemDecoration(builder.itemDecoration);
        }
        if (builder.adapter != null) {
            previousAdapter = builder.adapter;
            recyclerView.setAdapter(previousAdapter);
        }
        if (builder.prepareChangeVideoVidListener != null) {
            this.prepareChangeVideoVidListener = builder.prepareChangeVideoVidListener;
        }
        if (!TextUtils.isEmpty(builder.themeColor)) {
            errorTv.setTextColor(Color.parseColor(builder.themeColor));
            header.setColorSchemeColors(new int[]{Color.parseColor(builder.themeColor)});
        }
    }

    /**
     * 改变视频vid
     *
     * @param vid 视频的vid
     */
    public void changePlaybackVideoVid(String vid) {
        if (prepareChangeVideoVidListener != null) {
            prepareChangeVideoVidListener.onPrepareChangeVideoVid(vid);
        }
        if (previousPresenter != null) {
            previousPresenter.changePlaybackVideoVid(vid);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回放-往期-view层的实现">
    private final IPLVPreviousPlaybackContract.IPreviousPlaybackView previousView = new PLVAbsPreviousView() {

        @Override
        public void updatePreviousVideoList(PLVPlaybackListVO playbackList) {
            errorTv.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            footView.setVisibility(View.INVISIBLE);

            //关闭刷新
            refreshLayout.setEnableAutoRefresh(false);
            refreshLayout.setDisablePerformRefresh(true);
            refreshLayout.setDisableRefresh(true);
            //开启加载更多
            refreshLayout.setDisableLoadMore(false);

            if (playbackList == null || playbackList.getData().getContents().size() <= 0) {
                //没有数据
                previousNoMoreData();
                return;
            }

            //当获取到数据的时候，播放第一条视频,注意这里是没有视频列表的时候才会播放第一条视频
            //防止加载更多视频发生错误，刷新导致视频重播
            if (previousPresenter != null) {
                plvPlaybackInfo = playbackList;
                if (!dataList.isEmpty()) {
                    dataList.clear();
                    dataList.addAll(playbackList.getData().getContents());
                } else {
                    dataList.addAll(playbackList.getData().getContents());
                    changePlaybackVideoVid(dataList.get(0).getVideoPoolId());
                }
            }
            if (previousAdapter != null) {
                List<PLVBaseViewData> plvBaseViewData = toPlayBackList(playbackList);
                previousAdapter.setDataList(plvBaseViewData);
            }
        }

        @Override
        public void requestPreviousError() {
            errorTv.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            refreshLayout.setDisableRefresh(false);
            refreshLayout.setDisablePerformRefresh(false);
            refreshLayout.setDisableLoadMore(true);
        }

        @Override
        public void previousNoMoreData() {
            refreshLayout.setEnableNoMoreData(true);
            footView.setText(getResources().getString(R.string.plv_previous_no_more_data));
            footView.setVisibility(View.VISIBLE);
            refreshLayout.refreshComplete(true);
        }

        @Override
        public void previousLoadModreData(PLVPlaybackListVO listVO) {
            if (previousAdapter != null) {
                dataList.addAll(listVO.getData().getContents());
                List<PLVBaseViewData> datas = toPlayBackList(listVO);
                previousAdapter.loadMore(datas);
                refreshLayout.refreshComplete();
            }
        }

        @Override
        public void previousLoadMoreError() {
            requestPreviousError();
        }

        @Override
        public void setPresenter(IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter presenter) {
            previousPresenter = presenter;
        }

        @Override
        public void onPlayComplete() {
            playNextOne();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="提供方法">

    /**
     * 将MVP-V返回出去
     *
     * @return previousView
     */
    public IPLVPreviousPlaybackContract.IPreviousPlaybackView getPreviousView() {
        return previousView;
    }

    /**
     * 请求往期视频列表
     */
    public void requestPreviousList() {
        if (previousPresenter != null) {
            previousPresenter.requestPreviousList();
        }
    }

    /**
     * 销毁view
     */
    public void onDestroy() {
        if (previousPresenter != null) {
            previousPresenter.unregisterView(previousView);
            previousPresenter.onDestroy();
        }
    }

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="自己的内部方法">

    /**
     * 將list数据转为List<PLVBaseViewData>
     *
     * @param listVO 回放视频列表数据
     * @return List<PLVBaseViewData>
     */
    private List<PLVBaseViewData> toPlayBackList(PLVPlaybackListVO listVO) {
        List<PLVBaseViewData> playbackList = new ArrayList<>();
        if (listVO != null && listVO.getData() != null) {
            List<PLVPlaybackListVO.DataBean.ContentsBean> contents = listVO.getData().getContents();
            if (contents != null) {
                for (PLVPlaybackListVO.DataBean.ContentsBean contentBean : contents) {
                    playbackList.add(new PLVBaseViewData<>(contentBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));
                }
            }
        }
        return playbackList;
    }

    /**
     * 播放下一个视频
     */
    private void playNextOne() {
        if (!dataList.isEmpty() && previousAdapter != null && previousPresenter != null) {
            int old = previousAdapter.getCurrentPosition();
            int currentPosition = old;
            //当为最后一个视频的时候，回到第一个视频
            if (old >= dataList.size() - 1) {
                currentPosition = 0;
            } else {
                currentPosition++;
            }
            previousAdapter.setCurrentPosition(currentPosition);
            previousAdapter.notifyItemChanged(old);
            previousAdapter.notifyItemChanged(currentPosition);
            changePlaybackVideoVid(dataList.get(currentPosition).getVideoPoolId());
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类">

    // <editor-fold defaultstate="collapsed" desc="内部类-PLVPreviousViewInterface">

    /**
     * PLVPreviousView的各种方法设置，目前有切换vid前的listener，需要其他时机做某些事件可以在这里设置
     */
    public interface PLVPreviousViewInterface {
        /**
         * 切换vid时的interface
         */
        interface OnPrepareChangeVideoVidListener {
            /**
             * 在切换vid前的监听
             */
            void onPrepareChangeVideoVid(String vid);
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类-Build">

    /**
     * PLVPreviousView内部参数设置类
     * 可以通过这个内部类来为PLVPreviousView设置相应的参数
     */
    public static class Builder {
        private final Context context;
        private RecyclerView.ItemDecoration itemDecoration;
        private RecyclerView.LayoutManager layoutManager;
        private PLVPreviousAdapter adapter;
        private String themeColor;
        //切换vid前的listener
        private PLVPreviousViewInterface.OnPrepareChangeVideoVidListener prepareChangeVideoVidListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置recyclerView的layoutManager
         *
         * @param manager layoutManager
         * @return Builder
         */
        public Builder setRecyclerViewLayoutManager(RecyclerView.LayoutManager manager) {
            this.layoutManager = manager;
            return this;
        }

        /**
         * 设置RecyclerViewItemDecoration
         *
         * @param itemDecoration itemDecoration
         * @return Builder
         */
        public Builder setRecyclerViewItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
            this.itemDecoration = itemDecoration;
            return this;
        }

        /**
         * 设置RecycleView.Adapter
         *
         * @param adapter 适配器
         * @return Builder
         */
        public Builder setAdapter(PLVPreviousAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * 设置主题颜色，这里改变的是错误提示、刷新颜色
         *
         * @param color 颜色
         * @return Builder
         */
        public Builder setThemeColor(String color) {
            this.themeColor = color;
            return this;
        }

        /**
         * 设置在改变vid前的监听
         *
         * @param listener 回调监听
         * @return Builder
         */
        public Builder setOnPrepareChangeVidListener(PLVPreviousViewInterface.OnPrepareChangeVideoVidListener listener) {
            this.prepareChangeVideoVidListener = listener;
            return this;
        }

        /**
         * 可以通过这个Build来创建PLVPreviousView
         *
         * @return PLVPreviousView
         */
        public PLVPreviousView create() {
            PLVPreviousView plvPreviousView = new PLVPreviousView(context);
            plvPreviousView.setParams(this);
            return plvPreviousView;
        }
    }

    // </editor-fold>

    // </editor-fold>
}
