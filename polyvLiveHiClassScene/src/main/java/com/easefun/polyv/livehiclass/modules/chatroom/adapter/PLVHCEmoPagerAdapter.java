package com.easefun.polyv.livehiclass.modules.chatroom.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PLVHCEmoPagerAdapter extends PagerAdapter {
    private List<View> lists;
    private Context context;

    public PLVHCEmoPagerAdapter(List<View> lists, Context context) {
        this.lists = lists;
        this.context = context;
    }

    @Override
    public Object instantiateItem(@NotNull ViewGroup container, int position) {
        View currentView = lists.get(position);
        //这里可以通过findViewById设置子view的属性
        container.addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        container.removeView(lists.get(position));
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View arg0, @NotNull Object arg1) {
        return arg0 == arg1;
    }

}
