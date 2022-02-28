package com.easefun.polyv.livehiclass.modules.linkmic.zoom;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 连麦摄像头放大区域布局
 *
 * @author suhongtao
 */
public class PLVHCLinkMicZoomLayout extends FrameLayout implements IPLVHCLinkMicZoomLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    /**
     * 最多可放大的摄像头画面数量
     */
    public static final int MAX_ZOOM_ITEM_COUNT = 4;

    /**
     * 摄像头放大容器布局 缓存池
     */
    private final Pools.SimplePool<PLVHCLinkMicZoomItemContainer> zoomItemContainerPool = new Pools.SimplePool<>(MAX_ZOOM_ITEM_COUNT);

    private int lastWidth = 0;
    private int lastHeight = 0;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLinkMicZoomLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCLinkMicZoomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLinkMicZoomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        PLVHCLinkMicZoomManager.getInstance().registerZoomLayout(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API方法 - 接口IPLVHCLinkMicZoomLayout实现">

    @Override
    public PLVHCLinkMicZoomItemContainer createZoomItemContainer() {
        PLVHCLinkMicZoomItemContainer zoomItemContainer = zoomItemContainerPool.acquire();
        if (zoomItemContainer == null) {
            zoomItemContainer = new PLVHCLinkMicZoomItemContainer(getContext());
        }
        addView(zoomItemContainer);
        zoomItemContainer.init();
        return zoomItemContainer;
    }

    @Override
    public void removeZoomItemContainer(PLVHCLinkMicZoomItemContainer zoomItemContainer) {
        removeView(zoomItemContainer);
        zoomItemContainerPool.release(zoomItemContainer);
    }

    @Override
    public boolean canZoomMoreItem() {
        return getChildCount() < MAX_ZOOM_ITEM_COUNT;
    }

    @Override
    public void onUpdateMicSite(final PLVUpdateMicSiteEvent event, final PLVHCLinkMicZoomItemContainer zoomItemContainer) {
        if (event == null || !event.checkIsValid()) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post(new Runnable() {
                @Override
                public void run() {
                    onUpdateMicSite(event, zoomItemContainer);
                }
            });
        }
        // Main thread
        final float myParentWidth = getWidth();
        final float myParentHeight = getHeight();
        final float srcParentWidth = event.getParentWidth();
        final float srcParentHeight = event.getParentHeight();
        final float verticalScale = myParentHeight / srcParentHeight;
        final float horizontalScale = myParentWidth / srcParentWidth;
        final int left = (int) (event.getLeft() * horizontalScale);
        final int top = (int) (event.getTop() * verticalScale);
        final int width = (int) (event.getWidth() * horizontalScale);
        final int height = (int) (event.getHeight() * verticalScale);
        final int containerLastIndex = zoomItemContainer.getZIndex();
        zoomItemContainer.updateLayout(left, top, width, height);
        zoomItemContainer.setZIndex(event.getIndex());
        if (containerLastIndex != event.getIndex()) {
            updateChildrenIndex();
        }
    }

    @Override
    public void destroy() {
        PLVHCLinkMicZoomManager.getInstance().destroy();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ViewGroup方法重写">

    // 文档区域&放大区域 宽 : 高 = 2.1 : 1
    private static final float CONTAINER_RATIO_WIDTH_BY_HEIGHT = 2.1F;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);

        int preferWidth = (int) (specSizeHeight * CONTAINER_RATIO_WIDTH_BY_HEIGHT);
        int measureWidth = preferWidth;
        if (preferWidth > specSizeWidth) {
            measureWidth = 0;
        }

        ViewGroup.LayoutParams layoutParam = getLayoutParams();
        if (layoutParam.width != measureWidth) {
            layoutParam.width = measureWidth;
            setLayoutParams(layoutParam);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            post(new Runnable() {
                @Override
                public void run() {
                    notifyChildrenUpdateSize();
                }
            });
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void notifyChildrenUpdateSize() {
        final List<PLVHCLinkMicZoomItemContainer> zoomItemContainers = getContainerList();
        for (int i = 0; i < zoomItemContainers.size(); i++) {
            zoomItemContainers.get(i).notifyOnParentSizeChanged();
        }
    }

    private void updateChildrenIndex() {
        final List<PLVHCLinkMicZoomItemContainer> zoomItemContainers = getSortedContainerList();
        for (int i = 0; i < zoomItemContainers.size(); i++) {
            zoomItemContainers.get(i).bringToFront();
        }
    }

    @NonNull
    private List<PLVHCLinkMicZoomItemContainer> getContainerList() {
        List<PLVHCLinkMicZoomItemContainer> zoomItemContainers = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof PLVHCLinkMicZoomItemContainer) {
                zoomItemContainers.add((PLVHCLinkMicZoomItemContainer) child);
            }
        }
        return zoomItemContainers;
    }

    @NonNull
    private List<PLVHCLinkMicZoomItemContainer> getSortedContainerList() {
        List<PLVHCLinkMicZoomItemContainer> zoomItemContainers = getContainerList();
        Collections.sort(zoomItemContainers, new Comparator<PLVHCLinkMicZoomItemContainer>() {
            @Override
            public int compare(PLVHCLinkMicZoomItemContainer o1, PLVHCLinkMicZoomItemContainer o2) {
                int index1 = o1.getZIndex();
                int index2 = o2.getZIndex();
                // Integer.compare
                return (index1 < index2) ? -1 : ((index1 == index2) ? 0 : 1);
            }
        });
        return zoomItemContainers;
    }

    // </editor-fold>

}
