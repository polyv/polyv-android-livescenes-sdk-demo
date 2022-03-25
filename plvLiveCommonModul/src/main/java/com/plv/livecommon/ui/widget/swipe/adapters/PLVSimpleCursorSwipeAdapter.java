package com.plv.livecommon.ui.widget.swipe.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.plv.livecommon.ui.widget.swipe.PLVSwipeLayout;
import com.plv.livecommon.ui.widget.swipe.implments.PLVSwipeItemMangerImpl;
import com.plv.livecommon.ui.widget.swipe.interfaces.PLVSwipeAdapterInterface;
import com.plv.livecommon.ui.widget.swipe.interfaces.PLVSwipeItemMangerInterface;
import com.plv.livecommon.ui.widget.swipe.util.PLVAttributes;

import java.util.List;

public abstract class PLVSimpleCursorSwipeAdapter extends SimpleCursorAdapter implements PLVSwipeItemMangerInterface, PLVSwipeAdapterInterface {

    private PLVSwipeItemMangerImpl mItemManger = new PLVSwipeItemMangerImpl(this);

    protected PLVSimpleCursorSwipeAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    protected PLVSimpleCursorSwipeAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        mItemManger.bind(v, position);
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
