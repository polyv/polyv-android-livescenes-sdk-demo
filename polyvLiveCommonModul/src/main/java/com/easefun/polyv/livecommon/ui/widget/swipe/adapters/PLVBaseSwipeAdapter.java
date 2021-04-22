package com.easefun.polyv.livecommon.ui.widget.swipe.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import com.easefun.polyv.livecommon.ui.widget.swipe.PLVSwipeLayout;
import com.easefun.polyv.livecommon.ui.widget.swipe.implments.PLVSwipeItemMangerImpl;
import com.easefun.polyv.livecommon.ui.widget.swipe.interfaces.PLVSwipeAdapterInterface;
import com.easefun.polyv.livecommon.ui.widget.swipe.interfaces.PLVSwipeItemMangerInterface;
import com.easefun.polyv.livecommon.ui.widget.swipe.util.PLVAttributes;

import java.util.List;

public abstract class PLVBaseSwipeAdapter extends BaseAdapter implements PLVSwipeItemMangerInterface, PLVSwipeAdapterInterface {

    protected PLVSwipeItemMangerImpl mItemManger = new PLVSwipeItemMangerImpl(this);

    /**
     * @param position
     * @return
     */
    public abstract int getSwipeLayoutResourceId(int position);

    /**
     * generate a new view item.
     * Never bind SwipeListener or fill values here, every item has a chance to fill value or bind
     * listeners in fillValues.
     * to fill it in {@code fillValues} method.
     * @param position
     * @param parent
     * @return
     */
    public abstract View generateView(int position, ViewGroup parent);

    /**
     * fill values or bind listeners to the view.
     * @param position
     * @param convertView
     */
    public abstract void fillValues(int position, View convertView);

    @Override
    public void notifyDatasetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null){
            v = generateView(position, parent);
        }
        mItemManger.bind(v, position);
        fillValues(position, v);
        return v;
    }

    @Override
    public void openItem(int position) {
        mItemManger.openItem(position);
    }

    @Override
    public void closeItem(int position) {
        mItemManger.closeItem(position);
    }

    @Override
    public void closeAllExcept(PLVSwipeLayout layout) {
        mItemManger.closeAllExcept(layout);
    }

    @Override
    public void closeAllItems() {
        mItemManger.closeAllItems();
    }

    @Override
    public List<Integer> getOpenItems() {
        return mItemManger.getOpenItems();
    }

    @Override
    public List<PLVSwipeLayout> getOpenLayouts() {
        return mItemManger.getOpenLayouts();
    }

    @Override
    public void removeShownLayouts(PLVSwipeLayout layout) {
        mItemManger.removeShownLayouts(layout);
    }

    @Override
    public boolean isOpen(int position) {
        return mItemManger.isOpen(position);
    }

    @Override
    public PLVAttributes.Mode getMode() {
        return mItemManger.getMode();
    }

    @Override
    public void setMode(PLVAttributes.Mode mode) {
        mItemManger.setMode(mode);
    }
}
