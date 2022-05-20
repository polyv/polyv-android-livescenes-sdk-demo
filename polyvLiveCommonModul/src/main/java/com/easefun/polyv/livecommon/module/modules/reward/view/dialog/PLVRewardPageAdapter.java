package com.easefun.polyv.livecommon.module.modules.reward.view.dialog;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.easefun.polyv.livecommon.module.modules.reward.view.adapter.PLVRewardListAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2021-03-05
 * author: ysh
 * description: 将打赏礼物列表分页，分页数量由pageSize决定，展示由{@link PLVPointRewardFragment#spanCount}决定效果
 */
public class PLVRewardPageAdapter extends FragmentStatePagerAdapter {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private List<PLVBaseViewData> dataList;
    private boolean enablePage;
    private int pageSize;
    private int fragmentCount;
    //实际礼物列表选中的index
    private int selectPosition = -1;
    //分页的fragment-list
    private List<PLVPointRewardFragment> fragmentList = new ArrayList<>();
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVRewardPageAdapter(FragmentManager fm, List<PLVBaseViewData> list, boolean enablePage, int pageSize) {
        super(fm);
        this.enablePage = enablePage;
        this.dataList = list;
        this.pageSize = pageSize;
        if (list.size() % pageSize == 0) {
            fragmentCount = list.size() / pageSize;
        } else {
            fragmentCount = list.size() / pageSize + 1;
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="生命周期，重写方法">

    @Override
    public Fragment getItem(final int position) {
        PLVPointRewardFragment fragment = new PLVPointRewardFragment();
        if (!enablePage) {
            fragment.init(dataList);
        } else {
            if (dataList.size() <= pageSize) {
                fragment.init(dataList);
            } else {
                int startIndex = position * pageSize;
                int endIndex = Math.min((position + 1) * pageSize, dataList.size());
                List<PLVBaseViewData> subList = dataList.subList(startIndex, endIndex);
                fragment.init(subList);
            }
        }
        fragment.setOnCheckItemListener(new PLVRewardListAdapter.OnCheckItemListener() {
            @Override
            public void onItemCheck(PLVBaseViewData selectData, int itemPosition) {
                //当前选中的实际列表position
                selectPosition = position * pageSize + itemPosition;
                for (int i = 0; i < fragmentCount; i++) {
                    if(position != i){
                        fragmentList.get(i).clearSelectState();
                    }
                }
            }
        });
        if(!fragmentList.contains(fragment)){
            fragmentList.add(fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return fragmentCount;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外方法">

    /**
     * 获取选中礼物 item 的 data 数据
     */
    public PLVBaseViewData getSelectData(){
        if(selectPosition != -1) {
            return dataList.get(selectPosition);
        }
        return null;
    }

    /**
     * 返回分页数量
     */
    public int getPageCount() {
        return fragmentCount;
    }
    // </editor-fold >

}
