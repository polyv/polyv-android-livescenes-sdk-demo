package com.easefun.polyv.livestreamer.modules.chatroom.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;

public class PLVLSEmoPagerAdapter extends PagerAdapter {
    private List<View> lists;
    private Context context;

    public PLVLSEmoPagerAdapter(List<View> lists, Context context) {
        this.lists = lists;
        this.context = context;
    }

    @Override
    public Object instantiateItem(View container, int position) {
        View currentView = lists.get(position);
        //这里可以通过findViewById设置子view的属性
        ((ViewPager) container).addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView(lists.get(position));
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

}
