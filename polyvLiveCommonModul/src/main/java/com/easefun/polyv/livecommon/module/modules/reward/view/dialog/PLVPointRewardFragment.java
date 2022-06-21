package com.easefun.polyv.livecommon.module.modules.reward.view.dialog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.reward.view.adapter.PLVRewardListAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2021-03-05
 * author: ysh
 * description: 打赏礼物列表Fragment
 */
public class PLVPointRewardFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private RecyclerView rvPointRewardList;
    //礼物列表adapter
    private PLVRewardListAdapter adapter;
    //列数
    private static int spanCount = 5;
    //选中礼物item
    private PLVRewardListAdapter.OnCheckItemListener onCheckItemListener;
    //礼物列表数据
    private List<PLVBaseViewData> dataList;
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plv_point_reward_fragment, container, false);
        rvPointRewardList = findViewById(R.id.rv_point_reward_list);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        rvPointRewardList.setLayoutManager(gridLayoutManager);
        rvPointRewardList.setHasFixedSize(true);

        adapter = new PLVRewardListAdapter();
        adapter.setOnCheckItemListener(onCheckItemListener);
        adapter.setDataList(new ArrayList<PLVBaseViewData>(dataList));
        rvPointRewardList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">

    /**
     * 初始化礼物列表数据
     */
    public void init(List<PLVBaseViewData> dataList) {
        this.dataList = dataList;
    }

    /**
     * 清除选中状态
     */
    public void clearSelectState() {
        if (adapter != null) {
            adapter.clearSelectState();
        }
    }

    /**
     * 设置选择回调监听
     */
    public void setOnCheckItemListener(PLVRewardListAdapter.OnCheckItemListener onCheckItemListener) {
        this.onCheckItemListener = onCheckItemListener;
    }
    // </editor-fold >


}
