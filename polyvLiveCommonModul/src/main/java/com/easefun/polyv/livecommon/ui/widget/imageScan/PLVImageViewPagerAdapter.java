package com.easefun.polyv.livecommon.ui.widget.imageScan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVUrlTag;

import java.util.LinkedList;
import java.util.List;

public class PLVImageViewPagerAdapter<D extends PLVUrlTag, V extends PLVChatImageContainerWidget> extends PagerAdapter {
    private LinkedList<V> recycledViews;
    private List<D> datas;
    private Context context;
    private View.OnClickListener onClickListener;

    public PLVImageViewPagerAdapter(Context context) {
        this.context = context;
        this.recycledViews = new LinkedList<>();
    }

    public void bindData(List<D> datas) {
        this.datas = datas;
    }

    public void setOnImgClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //第二次更新datas时调用notify方法，会先触发instantiateItem再触发destroyItem，这时currentView的宽高会为0(如果第二次更新datas前没有触发过destroyItem，需要在getViewTreeObserver里面获取宽高/不能用post)
        //第二次更新datas时调用setAdapter方法，会先触发destroyItem再触发instantiateItem，这时currentVIew的宽高会为0(对比上面只有当前显示的item宽高为0，相邻的正常，同上)
        PLVChatImageContainerWidget currentView;
        if (!recycledViews.isEmpty()) {
            currentView = recycledViews.removeFirst();
        } else {
            currentView = new PLVChatImageContainerWidget(context);
        }
        currentView.setOnImgClickListener(onClickListener);
        currentView.setData(datas.get(position), position);//item个数变动notify时如果setCurrent不变，这里不会触发，可以使用POSITION_NONE触发(先触发destroyItem(仅已添加的item)，再触发instantiateItem(仅需缓存的item))
        //这里可以通过findViewById设置子view的属性
        container.addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        V currentView = (V) object;
        container.removeView(currentView);
        recycledViews.addLast(currentView);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

}