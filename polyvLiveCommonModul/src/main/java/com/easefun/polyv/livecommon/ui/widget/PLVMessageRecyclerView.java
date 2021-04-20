package com.easefun.polyv.livecommon.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PLVMessageRecyclerView extends RecyclerView {
    //未读item数
    private int unreadCount;
    private List<View> unreadViews;

    private boolean isScrolledEnd;//true -> end
    private boolean heightZero;
    private boolean hasScrollEvent;

    private int lastVerticalScrollRange;
    private int lastVerticalScrollExtent;
    private int lastVerticalScrollOffset;

    private List<OnUnreadCountChangeListener> unreadCountChangeListeners;
    private OnScrollToLastItemListener scrollToLastItemListener;

    private static final int FLAG_SCROLL = 1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FLAG_SCROLL) {
                if (getAdapter() != null) {
                    scrollToPosition(getAdapter().getItemCount() - 1);
                }
            }
        }
    };

    public PLVMessageRecyclerView(Context context) {
        this(context, null);
    }

    public PLVMessageRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVMessageRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addOnUnreadListener();
    }

    public static LinearLayoutManager setLayoutManager(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        return mLinearLayoutManager;
    }

    public static void closeDefaultAnimator(RecyclerView recyclerView) {
        recyclerView.getItemAnimator().setAddDuration(0);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.getItemAnimator().setMoveDuration(0);
        recyclerView.getItemAnimator().setRemoveDuration(0);
        if (recyclerView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return 0;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeMessages(FLAG_SCROLL);
    }

    public void addUnreadView(final View unreadView) {
        if (unreadViews == null) {
            unreadViews = new ArrayList<>();
        }
        unreadViews.add(unreadView);
        if (unreadView != null) {
            unreadView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unreadCount = 0;
                    if (unreadViews != null) {
                        for (View unreadView : unreadViews) {
                            unreadView.setVisibility(View.GONE);
                        }
                    }
                    callOnUnreadChange(unreadCount);
                    if (getAdapter() != null && getAdapter().getItemCount() > 0) {
                        if ((getAdapter().getItemCount() - 1) - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition() <= 10)
                            smoothScrollToPosition(getAdapter().getItemCount() - 1);
                        else
                            scrollToPosition(getAdapter().getItemCount() - 1);
                    }
                }
            });
        }
    }

    private void addOnUnreadListener() {
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (lastVerticalScrollRange != 0
                        && lastVerticalScrollRange == computeVerticalScrollRange()
                        && lastVerticalScrollOffset != 0
                        && lastVerticalScrollOffset == computeVerticalScrollOffset()
                        && lastVerticalScrollExtent != 0
                        && lastVerticalScrollExtent > computeVerticalScrollExtent()
                        && isScrolledEnd) {
                    return;
                }
                lastVerticalScrollRange = computeVerticalScrollRange();
                lastVerticalScrollExtent = computeVerticalScrollExtent();
                lastVerticalScrollOffset = computeVerticalScrollOffset();
                setIsScrolledEnd(!canScrollVertically(1));//1.原本为false，键盘弹出不超出列表高度，会改为true，并且更多view出现在顶部//2.非键盘弹出，更多view不会出现，并且出现的位置在底部并被覆盖
                if (unreadCount >= 2 && getAdapter() != null) {
                    int tempUnreadCount = getAdapter().getItemCount() - 1 - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
                    if (tempUnreadCount < unreadCount) {
                        unreadCount = tempUnreadCount;
                        /**
                         * ///暂时保留该代码
                         * if (unreadView != null) {
                         *     unreadView.setText("有" + unreadCount + "条新信息，点击查看");
                         * }
                         */

                        if (unreadCount == 0) {
                            if (unreadViews != null) {
                                for (View unreadView : unreadViews) {
                                    unreadView.setVisibility(View.GONE);
                                }
                            }
                        }
                        callOnUnreadChange(unreadCount);
                    }
                }
                if (isScrolledEnd) {
                    unreadCount = 0;
                    if (unreadViews != null) {
                        for (View unreadView : unreadViews) {
                            unreadView.setVisibility(View.GONE);
                        }
                    }
                    callOnUnreadChange(unreadCount);
                }
            }
        });
    }


    private void setIsScrolledEnd(boolean isScrolledEnd) {
        this.isScrolledEnd = isScrolledEnd;//该控件及父控件不能使用padding(可以使用margin)，键盘高度超过控件时，键盘收回时会滚动顶部
    }

    public void setStackFromEnd(boolean stackFromEnd) {
        if (getLayoutManager() instanceof LinearLayoutManager) {
            ((LinearLayoutManager) getLayoutManager()).setStackFromEnd(stackFromEnd);
        }
    }

    private void processStackFromEnd() {
        if (canScrollVertically(1) || canScrollVertically(-1)) {
            if (getLayoutManager() instanceof LinearLayoutManager
                    && ((LinearLayoutManager) getLayoutManager()).getStackFromEnd()) {
                ((LinearLayoutManager) getLayoutManager()).setStackFromEnd(false);
                unreadCount = 0;
                if (unreadViews != null) {
                    for (View unreadView : unreadViews) {
                        unreadView.setVisibility(View.GONE);
                    }
                }
                callOnUnreadChange(unreadCount);
                scrollToPosition(getAdapter().getItemCount() - 1);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (t >= b/*input > recyclerHeight, input sh to top*/) {
            heightZero = true;
        } else {
            heightZero = false;
            if (hasScrollEvent) {
                hasScrollEvent = false;
                handler.sendEmptyMessage(FLAG_SCROLL);
            }
        }
    }

    public void addOnUnreadCountChangeListener(OnUnreadCountChangeListener listener) {
        if (unreadCountChangeListeners == null) {
            unreadCountChangeListeners = new ArrayList<>();
        }
        unreadCountChangeListeners.add(listener);
    }

    private void callOnUnreadChange(int currentUnreadCount) {
        if (unreadCountChangeListeners != null) {
            for (OnUnreadCountChangeListener unreadCountChangeListener : unreadCountChangeListeners) {
                unreadCountChangeListener.onChange(currentUnreadCount);
            }
        }
    }

    public interface OnUnreadCountChangeListener {
        void onChange(int currentUnreadCount);
    }

    public void setOnScrollToLastItemListener(OnScrollToLastItemListener listener) {
        this.scrollToLastItemListener = listener;
    }

    public interface OnScrollToLastItemListener {
        void onCall();
    }

    @SuppressLint("SetTextI18n")
    private void changeUnreadViewWithCount(int count) {
        unreadCount += count;
        if (unreadViews != null) {
            for (View unreadView : unreadViews) {
                unreadView.setVisibility(View.VISIBLE);
                /**
                 * ///暂时保留该代码
                 * unreadView.setText("有" + unreadCount + "条新信息，点击查看");
                 */
            }
        } else {
            super.scrollToPosition(getAdapter().getItemCount() - 1);
            if (scrollToLastItemListener != null) {
                scrollToLastItemListener.onCall();
            }
        }
        callOnUnreadChange(unreadCount);
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void clear() {
        unreadCount = 0;
        if (unreadViews != null) {
            for (View unreadView : unreadViews) {
                unreadView.setVisibility(View.GONE);
            }
        }
        callOnUnreadChange(unreadCount);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (heightZero) {
            hasScrollEvent = true;
        } else {
            super.smoothScrollToPosition(position);
            if (getAdapter() != null && position == getAdapter().getItemCount() - 1) {
                if (scrollToLastItemListener != null) {
                    scrollToLastItemListener.onCall();
                }
            }
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if (heightZero) {
            hasScrollEvent = true;
        } else {
            super.scrollToPosition(position);
            if (getAdapter() != null && position == getAdapter().getItemCount() - 1) {
                if (scrollToLastItemListener != null) {
                    scrollToLastItemListener.onCall();
                }
            }
        }
    }

    //boolean isShowMore
    public boolean scrollToBottomOrShowMore(int count) {
        if (heightZero) {
            if (isScrolledEnd) {
                hasScrollEvent = true;
            } else {
                changeUnreadViewWithCount(count);
                return true;
            }
        } else if (isScrolledEnd) {
            if (getAdapter() != null) {
                super.scrollToPosition(getAdapter().getItemCount() - 1);
                if (scrollToLastItemListener != null) {
                    scrollToLastItemListener.onCall();
                }
            }
        } else if (getHeight() - getPaddingBottom() - getPaddingTop() < computeVerticalScrollRange()) {//排除item数为0的情况
            changeUnreadViewWithCount(count);
            return true;
        }
        return false;
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int firstTop;
        private int space;
        private int noTopLayoutId;

        public SpacesItemDecoration(int space) {
            this(space, 0);
        }

        public SpacesItemDecoration(int space, int firstTop) {
            this.space = space;
            this.firstTop = firstTop;
        }

        public SpacesItemDecoration(int space, int firstTop, int noTopLayoutId) {
            this.space = space;
            this.firstTop = firstTop;
            this.noTopLayoutId = noTopLayoutId;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = 0;
            outRect.right = 0;
            outRect.bottom = 0;

            if (noTopLayoutId != 0 && noTopLayoutId == view.getId()) {
                outRect.top = 0;
                return;
            }

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = firstTop;
            } else {
                outRect.top = space;
            }
        }
    }

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;
        private int topSpacing;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge, int topSpacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
            this.topSpacing = topSpacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = topSpacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = topSpacing; // item top
                }
            }
        }
    }
}
