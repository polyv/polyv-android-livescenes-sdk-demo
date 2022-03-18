package com.easefun.polyv.livehiclass.modules.linkmic.zoom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.ui.widget.PLVDragScaleLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.linkmic.list.item.PLVHCLinkMicItemViewProxy;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.socket.event.linkmic.PLVRemoveMicSiteEvent;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

import java.util.concurrent.atomic.AtomicInteger;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

/**
 * 连麦摄像头放大区域容器布局
 *
 * @author suhongtao
 */
public class PLVHCLinkMicZoomItemContainer extends PLVDragScaleLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    /**
     * 全局计数，下一次摄像头画面更新层级高度时应该使用的z-index
     * <p>
     * 放大区域布局内存在多个摄像头画面，摄像头画面容器之间具有层级高度的区别，
     * 每个容器使用z-index来确定自己的层级，z-index越大表明层级越高。
     * <p>
     * 这里使用静态的全局计数器提供z-index的存储和获取，初始化从1000开始，
     * 每次将任意摄像头画面置顶，或者有新增到放大区域的摄像头画面时+1。
     */
    private static final AtomicInteger GLOBAL_INDEX_COUNTER = new AtomicInteger(1000);

    /**
     * 全局计数，当前处于最高层级摄像头画面的z-index
     */
    private static int globalLargestIndex = -1;

    private PLVSwitchViewAnchorLayout switchViewAnchorLayout;
    private PLVHCLinkMicItemViewProxy linkMicItemViewProxy;

    /**
     * 该容器自身当前所处层级的z-index
     */
    private int zIndex;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLinkMicZoomItemContainer(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCLinkMicZoomItemContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLinkMicZoomItemContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        initSwitchAnchor();
        initLinkMicItemProxy();
        initDragScaleMode();
        updateZIndex(false);
    }

    private void initSwitchAnchor() {
        switchViewAnchorLayout = new PLVSwitchViewAnchorLayout(getContext());
        addView(switchViewAnchorLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        switchViewAnchorLayout.setOnSwitchListener(new PLVSwitchViewAnchorLayout.IPLVSwitchViewAnchorLayoutListener() {

            @Override
            protected void onSwitchElsewhereAfter() {
                // proxy切换到elsewhere，即摄像头放大
                // 依赖于itemView绑定到proxy上面，需要post进行延迟
                // see also PLVHCLinkMicItemContainer#initSwitchAnchorLayout()
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateZIndex(true);
                        sendUpdateZoomLayoutEvent();
                    }
                });
            }

            @Override
            protected void onSwitchBackBefore() {
                sendRemoveZoomLayoutEvent();
            }
        });
    }

    /**
     * init after {@link #initSwitchAnchor()}
     */
    private void initLinkMicItemProxy() {
        linkMicItemViewProxy = new PLVHCLinkMicItemViewProxy(getContext());
        linkMicItemViewProxy.setId(R.id.plvhc_linkmic_item_view);
        switchViewAnchorLayout.addView(linkMicItemViewProxy, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void initDragScaleMode() {
        setDragScaleMode(PLVDragScaleLayout.FLAG_CENTER, PLVDragScaleLayout.FLAG_MULTI_TOUCH);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.HI_CLASS_ZOOM_CAN_DRAG_ITEM_VIEW)) {
            dispatchTouchEventToChildren(event);
            return true;
        }

        boolean consume = super.onTouch(v, event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            bringToFront();
            updateZIndex(true);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            bringToFront();
            updateZIndex(true);
            sendUpdateZoomLayoutEvent();
        }

        return consume;
    }

    @Override
    protected void updateLayoutParam(int leftMargin, int topMargin, int width, int height) {
        final int[] ints = new int[]{leftMargin, topMargin, width, height};
        fitRatio(ints);
        super.updateLayoutParam(ints[0], ints[1], ints[2], ints[3]);
    }

    @Override
    protected void dispatchTouchEventToChildren(MotionEvent ev) {
        if (!isIndexAtTopBetweenSiblingView()) {
            return;
        }
        super.dispatchTouchEventToChildren(ev);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API方法">

    /**
     * 初始化方法
     */
    public void init() {
        if (!(getParent() instanceof ViewGroup)) {
            return;
        }
        updateScaleRange();

        final int parentWidth = ((ViewGroup) getParent()).getWidth();
        final int parentHeight = ((ViewGroup) getParent()).getHeight();
        final int height = parentHeight / 2;
        final int width = height * 16 / 9;
        final int left = (parentWidth - width) / 2;
        final int top = 0;
        updateLayout(left, top, width, height);
    }

    /**
     * 获取交换布局
     */
    public PLVSwitchViewAnchorLayout getSwitchViewAnchorLayout() {
        return switchViewAnchorLayout;
    }

    /**
     * 更新容器布局位置
     */
    public void updateLayout(final int left, final int top, final int width, final int height) {
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        lp.topMargin = top;
        lp.leftMargin = left;
        lp.height = height;
        lp.width = width;
        setLayoutParams(lp);
    }

    /**
     * 获取当前层级z-index
     */
    public int getZIndex() {
        return zIndex;
    }

    /**
     * 设置z-index
     */
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
        if (zIndex > globalLargestIndex) {
            globalLargestIndex = zIndex;
        }
    }

    /**
     * 响应父布局尺寸变更
     */
    public void notifyOnParentSizeChanged() {
        updateScaleRange();
        sendUpdateZoomLayoutEvent();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    /**
     * 更新拖动和缩放的范围
     */
    private void updateScaleRange() {
        final int parentWidth = ((ViewGroup) getParent()).getWidth();
        final int parentHeight = ((ViewGroup) getParent()).getHeight();
        setDragRange(0, parentWidth, 0, parentHeight);

        final int minScaleHeight = parentHeight / 4;
        final int minScaleWidth = minScaleHeight * 16 / 9;
        final int maxScaleWidth = parentHeight * 16 / 9;
        setMinSize(minScaleWidth, minScaleHeight);
        setMaxSize(maxScaleWidth, parentHeight);
    }

    /**
     * z-index 置顶
     *
     * @param checkTop 是否检查已在顶部，如果已在顶部则不更新
     */
    private void updateZIndex(boolean checkTop) {
        if (checkTop && zIndex == globalLargestIndex) {
            return;
        }
        int nextIndex = GLOBAL_INDEX_COUNTER.getAndIncrement();
        while (nextIndex <= globalLargestIndex) {
            nextIndex = GLOBAL_INDEX_COUNTER.getAndIncrement();
        }
        zIndex = globalLargestIndex = nextIndex;
    }

    /**
     * 保持16:9比例
     *
     * @param ints int[4]{ left, top, width, height }
     */
    private void fitRatio(int[] ints) {
        final int srcLeft = ints[0];
        final int srcTop = ints[1];
        final int srcWidth = ints[2];
        final int srcHeight = ints[3];

        int centerX = srcLeft + srcWidth / 2;
        int centerY = srcTop + srcHeight / 2;

        int maxHeight = Math.max(srcHeight, srcWidth * 9 / 16);
        int maxWidth = maxHeight * 16 / 9;
        if (maxHeight == srcHeight && maxWidth == srcWidth) {
            return;
        }

        int left = centerX - maxWidth / 2;
        int top = centerY - maxHeight / 2;
        left = Math.min((int) getMaxX(), left);
        left = Math.max((int) getMinX(), left);
        top = Math.min((int) getMaxY(), top);
        top = Math.max((int) getMinY(), top);
        ints[0] = left;
        ints[1] = top;
        ints[2] = maxWidth;
        ints[3] = maxHeight;
    }

    /**
     * 是否在父布局的顶部
     */
    private boolean isIndexAtTopBetweenSiblingView() {
        if (!(getParent() instanceof ViewGroup)) {
            return false;
        }
        final int index = ((ViewGroup) getParent()).indexOfChild(this);
        final int childCount = ((ViewGroup) getParent()).getChildCount();
        return index == childCount - 1;
    }

    /**
     * 发送更新摄像头画面容器位置的事件
     */
    private void sendUpdateZoomLayoutEvent() {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.HI_CLASS_ZOOM_CAN_SEND_UPDATE_ITEM_VIEW)) {
            return;
        }

        final String linkMicId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return linkMicItemViewProxy.getLinkMicId();
            }
        });
        final Integer parentWidth = nullable(new PLVSugarUtil.Supplier<Integer>() {
            @Override
            public Integer get() {
                return ((ViewGroup) getParent()).getWidth();
            }
        });
        final Integer parentHeight = nullable(new PLVSugarUtil.Supplier<Integer>() {
            @Override
            public Integer get() {
                return ((ViewGroup) getParent()).getHeight();
            }
        });
        if (linkMicId == null || parentWidth == null || parentHeight == null) {
            return;
        }

        new PLVUpdateMicSiteEvent()
                .setEventIdByLinkMicId(linkMicId)
                .setLeft((float) getLeft())
                .setTop((float) getTop())
                .setWidth((float) getWidth())
                .setHeight((float) getHeight())
                .setParentWidth(Float.valueOf(parentWidth))
                .setParentHeight(Float.valueOf(parentHeight))
                .setIndex(zIndex)
                .emitToSocket();
    }

    /**
     * 发送移除摄像头画面容器的事件
     */
    private void sendRemoveZoomLayoutEvent() {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.HI_CLASS_ZOOM_CAN_SEND_REMOVE_ITEM_VIEW)) {
            return;
        }

        final String linkMicId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return linkMicItemViewProxy.getLinkMicId();
            }
        });
        if (linkMicId == null) {
            return;
        }

        new PLVRemoveMicSiteEvent()
                .setEventIdByLinkMicId(linkMicId)
                .emitToSocket();
    }

    // </editor-fold>

}
