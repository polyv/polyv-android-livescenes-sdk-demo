package com.easefun.polyv.livehiclass.modules.linkmic.zoom;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livehiclass.modules.linkmic.list.item.PLVHCLinkMicItemContainer;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 连麦摄像头画面放大管理器
 *
 * @author suhongtao
 */
public class PLVHCLinkMicZoomManager {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private PLVHCLinkMicZoomManager() {
    }

    private static class InstanceHolder {
        private static final PLVHCLinkMicZoomManager INSTANCE = new PLVHCLinkMicZoomManager();
    }

    public static PLVHCLinkMicZoomManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    /**
     * 保存画面切换相关数据
     * Key: 连麦ID
     * Value: 画面切换相关数据 {@link SwitcherParam}
     */
    private final Map<String, SwitcherParam> switcherMap = new HashMap<>((int) (PLVHCLinkMicZoomLayout.MAX_ZOOM_ITEM_COUNT / 0.75F + 1));

    /**
     * 连麦放大区域布局弱引用
     */
    @Nullable
    private WeakReference<IPLVHCLinkMicZoomLayout> zoomLayoutWeakReference;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 注册放大区域布局
     */
    public void registerZoomLayout(IPLVHCLinkMicZoomLayout zoomLayout) {
        this.zoomLayoutWeakReference = new WeakReference<>(zoomLayout);
    }

    /**
     * 切换连麦摄像头画面缩放状态
     *
     * @param linkMicItemContainer 连麦区摄像头容器
     * @param zoomIn               {@code true} 放大，{@code false} 缩小
     */
    public void zoom(PLVHCLinkMicItemContainer linkMicItemContainer, boolean zoomIn) {
        if (!canZoomInItem()) {
            return;
        }
        final String linkMicId = linkMicItemContainer.getLinkMicId();
        if (linkMicId == null) {
            return;
        }

        PLVViewSwitcher switcher = getZoomSwitcher(linkMicId);
        if (switcher == null && zoomLayoutWeakReference != null && zoomLayoutWeakReference.get() != null) {
            switcher = createSwitcher(linkMicId, linkMicItemContainer, zoomLayoutWeakReference.get().createZoomItemContainer());
        }
        zoom(linkMicId, switcher, zoomIn);
    }

    /**
     * 收回放大区域的连麦摄像头
     *
     * @param linkMicId 连麦ID
     */
    public void zoomOut(String linkMicId) {
        PLVViewSwitcher switcher = getZoomSwitcher(linkMicId);
        if (switcher == null || !switcher.isViewSwitched()) {
            return;
        }
        zoom(linkMicId, switcher, false);
    }

    /**
     * 收回传入连麦ID列表以外的放大区域连麦摄像头
     *
     * @param linkMicIds 连麦ID列表，列表内摄像头不会收回
     */
    public void zoomOutAllExcept(Set<String> linkMicIds) {
        for (String linkMicId : new ArrayList<>(switcherMap.keySet())) {
            if (!linkMicIds.contains(linkMicId)) {
                zoomOut(linkMicId);
            }
        }
    }

    /**
     * 收回所有放大区域的连麦摄像头
     */
    public void zoomOutAll() {
        for (String linkMicId : new ArrayList<>(switcherMap.keySet())) {
            zoomOut(linkMicId);
        }
    }

    /**
     * 是否可以放大连麦摄像头
     *
     * @return {@code true} 可以放大，{@code false} 不可以放大，已到达最大放大数量
     */
    public boolean canZoomInItem() {
        if (zoomLayoutWeakReference == null || zoomLayoutWeakReference.get() == null) {
            return false;
        }
        return zoomLayoutWeakReference.get().canZoomMoreItem();
    }

    /**
     * 是否已经处于放大状态
     *
     * @param linkMicItemDataBean 连麦数据对象
     */
    public boolean isZoomIn(PLVLinkMicItemDataBean linkMicItemDataBean) {
        final String linkMicId = linkMicItemDataBean.getLinkMicId();
        if (linkMicId == null) {
            return false;
        }
        return isZoomIn(linkMicId);
    }

    /**
     * 是否已经处于放大状态
     *
     * @param linkMicId 连麦id
     */
    public boolean isZoomIn(String linkMicId) {
        PLVViewSwitcher switcher = getZoomSwitcher(linkMicId);
        if (switcher == null) {
            return false;
        }
        return switcher.isViewSwitched();
    }

    /**
     * 响应摄像头放大画面位置更新事件
     */
    public void notifyUpdateMicSite(PLVUpdateMicSiteEvent updateMicSiteEvent) {
        if (updateMicSiteEvent == null || updateMicSiteEvent.getLinkMicIdFromEventId() == null) {
            return;
        }
        final String linkMicId = updateMicSiteEvent.getLinkMicIdFromEventId();
        if (!isZoomIn(linkMicId)) {
            return;
        }
        if (zoomLayoutWeakReference != null && zoomLayoutWeakReference.get() != null) {
            zoomLayoutWeakReference.get().onUpdateMicSite(updateMicSiteEvent, switcherMap.get(linkMicId).zoomItemContainer.get());
        }
    }

    /**
     * 销毁方法
     */
    public void destroy() {
        switcherMap.clear();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    @Nullable
    private PLVViewSwitcher getZoomSwitcher(final String linkMicId) {
        return nullable(new PLVSugarUtil.Supplier<PLVViewSwitcher>() {
            @Override
            public PLVViewSwitcher get() {
                return switcherMap.get(linkMicId).switcher;
            }
        });
    }

    private PLVViewSwitcher createSwitcher(String linkMicId, PLVHCLinkMicItemContainer linkMicItemContainer, PLVHCLinkMicZoomItemContainer zoomItemContainer) {
        PLVViewSwitcher viewSwitcher = new PLVViewSwitcher();
        viewSwitcher.registerSwitchView(linkMicItemContainer.getSwitchAnchorLayout(), zoomItemContainer.getSwitchViewAnchorLayout());

        SwitcherParam switcherParam = new SwitcherParam();
        switcherParam.switcher = viewSwitcher;
        switcherParam.linkMicItemContainer = new WeakReference<>(linkMicItemContainer);
        switcherParam.zoomItemContainer = new WeakReference<>(zoomItemContainer);
        switcherMap.put(linkMicItemContainer.getLinkMicId(), switcherParam);

        return viewSwitcher;
    }

    private void zoom(String linkMicId, PLVViewSwitcher switcher, boolean zoomIn) {
        if (linkMicId == null) {
            return;
        }
        if (zoomIn == isZoomIn(linkMicId)) {
            return;
        }

        switcher.switchView();

        if (!zoomIn) {
            notifyRemoveZoomItemContainer(linkMicId);
            switcherMap.remove(linkMicId);
        }
    }

    private void notifyRemoveZoomItemContainer(final String linkMicId) {
        PLVHCLinkMicZoomItemContainer zoomItemContainer = nullable(new PLVSugarUtil.Supplier<PLVHCLinkMicZoomItemContainer>() {
            @Override
            public PLVHCLinkMicZoomItemContainer get() {
                return switcherMap.get(linkMicId).zoomItemContainer.get();
            }
        });
        if (zoomItemContainer != null && zoomLayoutWeakReference != null && zoomLayoutWeakReference.get() != null) {
            zoomLayoutWeakReference.get().removeZoomItemContainer(zoomItemContainer);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 保存画面切换相关数据">

    private static class SwitcherParam {
        private PLVViewSwitcher switcher;
        private WeakReference<PLVHCLinkMicItemContainer> linkMicItemContainer;
        private WeakReference<PLVHCLinkMicZoomItemContainer> zoomItemContainer;
    }

    // </editor-fold>

}
