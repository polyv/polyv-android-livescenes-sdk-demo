package com.easefun.polyv.livecommon.module.utils;

import android.graphics.drawable.ColorDrawable;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVViewPagerAdapter;
import com.plv.thirdpart.blankj.utilcode.util.BarUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * view初始化相关工具类
 */
public class PLVViewInitUtils {

    public static void initViewPager(FragmentManager fragmentManager, ViewPager viewPager, int selItem, Fragment... fragments) {
        List<Fragment> fragmentList = new ArrayList<>(Arrays.asList(fragments));
        PLVViewPagerAdapter pagerAdapter = new PLVViewPagerAdapter(fragmentManager, fragmentList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(fragmentList.size() - 1);
        viewPager.setCurrentItem(selItem);
    }

    public static View initPopupWindow(View v, @LayoutRes int resource, final PopupWindow popupWindow, View.OnClickListener listener) {
        View root = LayoutInflater.from(v.getContext()).inflate(resource, null, false);
        popupWindow.setContentView(root);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        root.setOnClickListener(listener);
        return root;
    }

    public static void initMarginTopWithStatusBar(View view, int defaultStatusBarHeightDp) {
        int statusBarHeight = BarUtils.getStatusBarHeight();//port land can unequal，and statusHeight can unequal notchHeight
        ViewGroup.MarginLayoutParams viewLp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        viewLp.topMargin = Math.max(statusBarHeight, ConvertUtils.dp2px(defaultStatusBarHeightDp));
        view.setLayoutParams(viewLp);
    }

    public static void initHeightWithStatusBar(View view, int defaultViewHeightDp, int defaultStatusBarHeightDp) {
        int statusBarHeight = BarUtils.getStatusBarHeight();
        ViewGroup.LayoutParams viewLp = view.getLayoutParams();
        viewLp.height = ConvertUtils.dp2px(defaultViewHeightDp) + Math.max(statusBarHeight, ConvertUtils.dp2px(defaultStatusBarHeightDp));
        view.setLayoutParams(viewLp);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
