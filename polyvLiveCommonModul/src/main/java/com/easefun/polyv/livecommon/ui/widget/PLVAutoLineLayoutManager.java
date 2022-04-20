package com.easefun.polyv.livecommon.ui.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class PLVAutoLineLayoutManager extends RecyclerView.LayoutManager {

    public static int span = 4;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
//        super.onLayoutChildren(recycler, state);
        detachAndScrapAttachedViews(recycler);
        int sumWidth = getWidth();

        int curLineWidth = 0, curLineTop = 0;
        int lastLineMaxHeight = 0;
        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);

            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            curLineWidth += width;
            if (curLineWidth <= sumWidth) {//不需要换行
                layoutDecorated(view, curLineWidth - width, curLineTop, curLineWidth, curLineTop + height);
                //比较当前行多有item的最大高度
                lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
            } else {//换行
                curLineWidth = width;
                if (lastLineMaxHeight == 0) {
                    lastLineMaxHeight = height;
                }
                //记录当前行top
                curLineTop += lastLineMaxHeight;

                layoutDecorated(view, 0, curLineTop, width, curLineTop + height);
                lastLineMaxHeight = height;
            }
        }
    }
}
