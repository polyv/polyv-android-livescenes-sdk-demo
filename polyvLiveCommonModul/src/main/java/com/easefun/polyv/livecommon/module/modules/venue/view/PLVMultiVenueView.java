package com.easefun.polyv.livecommon.module.modules.venue.view;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVFootView;
import com.easefun.polyv.livecommon.module.modules.venue.viewmodel.PLVMultiVenueViewModel;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.config.PolyvLiveSDKClient;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.livescenes.feature.venues.model.PLVVenueDataVO;
import com.plv.livescenes.feature.venues.model.PLVVenueStatusVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;

public class PLVMultiVenueView extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private final PLVMultiVenueViewModel venueViewModel = PLVDependManager.getInstance().get(PLVMultiVenueViewModel.class);
    private final String titleIndex = getResources().getString(R.string.plv_multi_venue_head_index);
    private String channelId;
    private String mainVenueId;

    private TextView indexText;
    private SmoothRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private PLVMultiVenueAdapter venueAdapter;
    // 切换场所的监听
    private PLVMultiVenueViewInterface.OnChangeVenueListener onChangeVenueListener;

    //加载更多的view
    private PLVFootView footView;

    private List<PLVBaseViewData> dataList = new ArrayList<>();
    private Map<String ,PLVVenueDataVO> tempVenueDataMap = new HashMap();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVMultiVenueView(@NonNull Context context) {
        this(context, null);
    }

    public PLVMultiVenueView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVMultiVenueView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.plv_multi_venue_layout, this, true);
        refreshLayout = view.findViewById(R.id.plv_multi_venue_refreshLy);
        recyclerView = view.findViewById(R.id.plv_multi_venue_rv);
        indexText = view.findViewById(R.id.plv_multi_venue_index_tv);

        footView = new PLVFootView(getContext());
        //默认不显示，加载时才显示
        footView.setVisibility(View.VISIBLE);
        footView.setText(getResources().getString(R.string.plv_multi_venue_not_mord));
        //默认禁用刷新，开启加载更多
        refreshLayout.setDisableRefresh(true);
        refreshLayout.setDisableLoadMore(false);
        refreshLayout.setEnableOverScroll(false);//禁用滚动回弹
        refreshLayout.setFooterView(footView);
        refreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshing() {

            }

            @Override
            public void onLoadingMore() {
                footView.setVisibility(View.VISIBLE);
                // 加载更多操作

            }
        });

        observerMultiVenueData();

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
     public void observerMultiVenueData() {
        venueViewModel.getVenueListLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<List<PLVVenueDataVO>>() {
                    @Override
                    public void onChanged(@Nullable List<PLVVenueDataVO> list) {
                        if(list.isEmpty()) {
                            return;
                        }
                        dataList.clear();
                        tempVenueDataMap.clear();
                        List<PLVBaseViewData> plvBaseViewData = toVenueList(list);
                        dataList.addAll(plvBaseViewData);
                        for (PLVVenueDataVO dataVO : list) {
                            tempVenueDataMap.put(dataVO.getChannelId().toString(), dataVO);
                        }
                        updateVenueList();
                        // 获取到数据，开始轮询分会场状态
                        venueViewModel.updateVenueStatusList(mainVenueId);
                    }
                });

        venueViewModel.getVenueStatusLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<List<PLVVenueStatusVO>>() {
                    @Override
                    public void onChanged(@Nullable List<PLVVenueStatusVO> statusVOS) {
                        if (statusVOS.isEmpty()) {
                            return;
                        }
                        List<PLVVenueDataVO> dataVOS = new ArrayList<>();
                        boolean isNeedUpdate = false;
                        for (PLVVenueStatusVO statusVO : statusVOS) {
                            PLVVenueDataVO dataVO = tempVenueDataMap.get(statusVO.getChannelId().toString());
                            if (dataVO == null) {
                                return;
                            }
                            PLVVenueDataVO newData = dataVO.clone(dataVO);
                            if (!dataVO.getLiveStatus().equals(statusVO.getLiveStatus())) {
                                newData.setLiveStatus(statusVO.getLiveStatus());
                                newData.setLiveStatusDesc(statusVO.getLiveStatusDesc());
                                dataVOS.add(newData);
                                tempVenueDataMap.put(statusVO.getChannelId().toString(), newData);
                                isNeedUpdate = true;
                            } else {
                                dataVOS.add(dataVO);
                            }
                        }
                        if (isNeedUpdate) {
                            updateVenueStatus(dataVOS);
                        }
                    }
                });
     }

     private void updateVenueList() {
         if (venueAdapter != null) {
             venueAdapter.setDataList(dataList);
         }
     }

     private void updateVenueStatus(List<PLVVenueDataVO> status) {
        if (venueAdapter != null) {
            venueAdapter.updateStatusList(status);
        }
     }

     private List<PLVBaseViewData> toVenueList(List<PLVVenueDataVO> list) {
        List<PLVBaseViewData> venueList = new ArrayList<>();
         for (PLVVenueDataVO s : list) {
             if (s.getChannelId().toString().equals(channelId)) {
                 setIndexText(list.indexOf(s), list.size());
             }
             venueList.add(new PLVBaseViewData(s, PLVBaseViewData.ITEMTYPE_UNDEFINED));
         }
        return venueList;
     }

    public void setIndexText(int index, int total) {
        indexText.setText(String.format(titleIndex + " %d/%d", index + 1, total));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外提供的方法">

    public void setParams(Builder builder) {
        if (builder.layoutManager != null) {
            recyclerView.setLayoutManager(builder.layoutManager);
        }
        if (builder.itemDecoration != null) {
            recyclerView.addItemDecoration(builder.itemDecoration);
        }
        if (builder.adapter != null) {
            venueAdapter = builder.adapter;
            recyclerView.setAdapter(venueAdapter);
        }
        if (builder.onChangeVenueListener != null) {
            this.onChangeVenueListener = builder.onChangeVenueListener;
        }

        venueAdapter.setOnViewActionListener(new PLVMultiVenueAdapter.OnViewActionListener() {
            @Override
            public void changeVenueClick(String channelId, boolean isPlayback) {
                if (PolyvLiveSDKClient.getInstance().getChannelId().equals(channelId)) {
                    return;
                }
                onChangeVenueListener.onChangeVenue(channelId, isPlayback);
                venueViewModel.setOnRequestLaunchOtherVenue(channelId, isPlayback);
            }
        });

    }

    public void init(String channelId, String mainVenueId) {
        this.channelId = channelId;
        this.mainVenueId = mainVenueId;
        venueViewModel.getMultipleVenueData(channelId, mainVenueId);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类-PLVMultiVenueViewInterface">
    public interface PLVMultiVenueViewInterface {

        interface OnChangeVenueListener {
            /**
             * 切换频道
             * @param channelId
             * @param isPlayback
             */
            void onChangeVenue(String channelId, boolean isPlayback);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类-Build">

    public static class Builder {
        private RecyclerView.ItemDecoration itemDecoration;
        private RecyclerView.LayoutManager layoutManager;
        private PLVMultiVenueAdapter adapter;
        private String themeColor;
        // 切换场所的监听
        private PLVMultiVenueViewInterface.OnChangeVenueListener onChangeVenueListener;

        public Builder setRecyclerViewLayoutManager(RecyclerView.LayoutManager manager) {
            this.layoutManager = manager;
            return this;
        }

        public Builder setRecyclerViewItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
            this.itemDecoration = itemDecoration;
            return this;
        }

        public Builder setAdapter(PLVMultiVenueAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder setThemeColor(String color) {
            this.themeColor = color;
            return this;
        }

        public Builder setOnChangeVenueListener(PLVMultiVenueViewInterface.OnChangeVenueListener listener) {
            this.onChangeVenueListener = listener;
            return this;
        }

        /**
         * 可以通过这个Build来创建PLVMultiVenueView
         * @param context
         * @return PLVMultiVenueView
         */
        public PLVMultiVenueView create(Context context) {
            PLVMultiVenueView plvMultiVenueView = new PLVMultiVenueView(context);
            plvMultiVenueView.setParams(this);
            return plvMultiVenueView;
        }
    }
    // </editor-fold>

}
