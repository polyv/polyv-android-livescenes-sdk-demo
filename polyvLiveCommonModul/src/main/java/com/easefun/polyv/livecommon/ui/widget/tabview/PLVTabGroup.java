package com.easefun.polyv.livecommon.ui.widget.tabview;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVTabGroup {

    private List<View> childs = null;

    public void setChildByList(List<View> childs) {
        this.childs = childs;
    }

    public void setChildByParent(ViewGroup parent) {
        childs = new ArrayList<>(parent.getChildCount());
        for (int i = 0; i < parent.getChildCount(); i++) {
            childs.add(parent.getChildAt(i));
        }
    }

    public void setSelectedChild(View view) {
        if (childs == null) {
            return;
        }
        for (View child : childs) {
            child.setSelected(child == view);
        }
    }

}
