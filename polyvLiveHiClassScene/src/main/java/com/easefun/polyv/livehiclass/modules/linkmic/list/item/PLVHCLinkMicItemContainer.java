package com.easefun.polyv.livehiclass.modules.linkmic.list.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.linkmic.zoom.IPLVHCZoomItemContainer;
import com.easefun.polyv.livehiclass.modules.linkmic.zoom.PLVHCLinkMicZoomManager;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

import java.lang.ref.WeakReference;

import static com.plv.foundationsdk.log.PLVCommonLog.format;

/**
 * @author suhongtao
 */

public class PLVHCLinkMicItemContainer extends FrameLayout implements IPLVHCLinkMicItem, IPLVHCZoomItemContainer {

    // <editor-fold defaultstate="collapsed" desc="变量">

    @Nullable
    private View layoutRoot;
    @Nullable
    private ImageView linkMicPlaceholderIv;
    @Nullable
    private TextView linkMicPlaceholderTv;
    @Nullable
    private Group linkMicPlaceholderGroup;
    @Nullable
    private PLVSwitchViewAnchorLayout linkMicSwitchAnchorLayout;
    @Nullable
    private IPLVHCLinkMicItem linkMicItemView;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLinkMicItemContainer(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCLinkMicItemContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLinkMicItemContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_linkmic_view_item_container, this);

        findView();
        initSwitchAnchorLayout();
    }

    private void findView() {
        layoutRoot = findViewById(R.id.plvhc_linkmic_item_container_layout_root);
        linkMicPlaceholderIv = findViewById(R.id.plvhc_linkmic_placeholder_iv);
        linkMicPlaceholderTv = findViewById(R.id.plvhc_linkmic_placeholder_tv);
        linkMicPlaceholderGroup = findViewById(R.id.plvhc_linkmic_placeholder_group);
        linkMicSwitchAnchorLayout = findViewById(R.id.plvhc_linkmic_switch_anchor_layout);
        linkMicItemView = findViewById(R.id.plvhc_linkmic_item_view);
    }

    private void initSwitchAnchorLayout() {
        if (linkMicSwitchAnchorLayout == null) {
            return;
        }
        linkMicSwitchAnchorLayout.setOnSwitchListener(new SwitchAnchorLayoutListener(linkMicSwitchAnchorLayout));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口实现 - IPLVHCLinkMicItem">

    @Override
    public void init(boolean isLargeLayout, OnRenderViewCallback callback) {
        if (linkMicItemView != null) {
            linkMicItemView.init(isLargeLayout, callback);
        }
    }

    @Override
    public void bindData(PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (linkMicItemView != null) {
            linkMicItemView.bindData(linkMicItemDataBean);
        }
        final String placeHolderStr = format("{}的位置", linkMicItemDataBean.getNick());
        if (linkMicPlaceholderTv != null) {
            linkMicPlaceholderTv.setText(placeHolderStr);
        }
    }

    @Nullable
    @Override
    public PLVLinkMicItemDataBean getLinkMicItemDataBean() {
        if (linkMicItemView != null) {
            return linkMicItemView.getLinkMicItemDataBean();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getLinkMicId() {
        if (linkMicItemView != null) {
            return linkMicItemView.getLinkMicId();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public PLVHCLinkMicItemContainer findContainerParent() {
        return this;
    }

    @Override
    public void switchWithItemView(IPLVHCLinkMicItem linkMicItemView) {
        if (linkMicItemView instanceof PLVHCLinkMicItemContainer) {
            final View myOldRootView = layoutRoot;
            final View myNewRootView = ((PLVHCLinkMicItemContainer) linkMicItemView).layoutRoot;
            removeView(myOldRootView);
            linkMicItemView.removeView(myNewRootView);
            addView(myNewRootView);
            linkMicItemView.addView(myOldRootView);
        }
    }

    @Override
    public void moveToItemView(IPLVHCLinkMicItem linkMicItemView) {
        if (linkMicItemView instanceof PLVHCLinkMicItemContainer) {
            final View myRootView = layoutRoot;
            removeView(myRootView);
            linkMicItemView.addView(myRootView);
        }
    }

    @Override
    public View removeItemView() {
        releaseRenderView();

        final View view = layoutRoot;
        removeView(layoutRoot);
        return view;
    }

    @Override
    public void addItemView(View rootView) {
        addView(rootView);
    }

    @Override
    public void releaseRenderView() {
        if (linkMicItemView != null) {
            linkMicItemView.releaseRenderView();
        }
    }

    @Override
    public void removeRenderView() {
        if (linkMicItemView != null) {
            linkMicItemView.removeRenderView();
        }
    }

    @Override
    public void setupRenderView() {
        if (linkMicItemView != null) {
            linkMicItemView.setupRenderView();
        }
    }

    @Override
    public void updateTeacherPreparingStatus(boolean isPreparing) {
        if (linkMicItemView != null) {
            linkMicItemView.updateTeacherPreparingStatus(isPreparing);
        }
    }

    @Override
    public void updateLeaderStatus(boolean isHasLeader) {
        if (linkMicItemView != null) {
            linkMicItemView.updateLeaderStatus(isHasLeader);
        }
    }

    @Override
    public void updateVideoStatus() {
        if (linkMicItemView != null) {
            linkMicItemView.updateVideoStatus();
        }
    }

    @Override
    public void updateAudioStatus() {
        if (linkMicItemView != null) {
            linkMicItemView.updateAudioStatus();
        }
    }

    @Override
    public void updateHandsUp() {
        if (linkMicItemView != null) {
            linkMicItemView.updateHandsUp();
        }
    }

    @Override
    public void updateHasPaint() {
        if (linkMicItemView != null) {
            linkMicItemView.updateHasPaint();
        }
    }

    @Override
    public void updateCupNum() {
        if (linkMicItemView != null) {
            linkMicItemView.updateCupNum();
        }
    }

    @Override
    public void updateZoom(PLVUpdateMicSiteEvent updateMicSiteEvent) {
        if (updateMicSiteEvent == null
                || !updateMicSiteEvent.checkIsValid()
                || !updateMicSiteEvent.getLinkMicIdFromEventId().equals(getLinkMicId())) {
            return;
        }
        final boolean isZoomIn = PLVHCLinkMicZoomManager.getInstance().isZoomIn(updateMicSiteEvent.getLinkMicIdFromEventId());
        final boolean canZoomIn = PLVHCLinkMicZoomManager.getInstance().canZoomInItem();
        if (!isZoomIn && canZoomIn) {
            PLVHCLinkMicZoomManager.getInstance().zoom(this, true);
        }
        PLVHCLinkMicZoomManager.getInstance().notifyUpdateMicSite(updateMicSiteEvent);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口实现 - IPLVHCZoomItemContainer">

    @Override
    public PLVSwitchViewAnchorLayout getSwitchAnchorLayout() {
        return linkMicSwitchAnchorLayout;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ViewGroup父类方法重写">

    @Override
    public void addView(View child) {
        super.addView(child);
        updateViewReference();
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        updateViewReference();
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        if (linkMicItemView != null) {
            linkMicItemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (l != null) {
                        l.onClick(v);
                    }
                }
            });
        } else {
            super.setOnClickListener(l);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void updateViewReference() {
        findView();
    }

    private void updatePlaceHolder() {
        if (linkMicPlaceholderGroup == null) {
            return;
        }
        if (getLinkMicId() == null || PLVHCLinkMicZoomManager.getInstance().isZoomIn(getLinkMicId())) {
            linkMicPlaceholderGroup.setVisibility(View.VISIBLE);
        } else {
            linkMicPlaceholderGroup.setVisibility(View.GONE);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 交换布局事件监听器">

    private static class SwitchAnchorLayoutListener extends PLVSwitchViewAnchorLayout.IPLVSwitchViewAnchorLayoutListener {

        private final PLVSwitchViewAnchorLayout switchViewAnchorLayout;

        private WeakReference<PLVHCLinkMicItemView> itemView;
        private WeakReference<PLVHCLinkMicItemViewProxy> itemViewProxy;

        private SwitchAnchorLayoutListener(PLVSwitchViewAnchorLayout switchViewAnchorLayout) {
            this.switchViewAnchorLayout = switchViewAnchorLayout;
        }

        @Override
        protected void onSwitchElsewhereBefore() {
            PLVHCLinkMicItemContainer container = findContainer();
            if (container == null) {
                return;
            }

            if (container.linkMicItemView instanceof PLVHCLinkMicItemView) {
                itemView = new WeakReference<>((PLVHCLinkMicItemView) container.linkMicItemView);
            }
        }

        @Override
        protected void onSwitchElsewhereAfter() {
            PLVHCLinkMicItemContainer container = findContainer();
            if (container == null) {
                return;
            }

            container.updateViewReference();
            if (container.linkMicItemView instanceof PLVHCLinkMicItemViewProxy) {
                itemViewProxy = new WeakReference<>((PLVHCLinkMicItemViewProxy) container.linkMicItemView);
            }
            final PLVHCLinkMicItemView item = itemView == null ? null : itemView.get();
            final PLVHCLinkMicItemViewProxy proxy = itemViewProxy == null ? null : itemViewProxy.get();
            if (item != null && proxy != null) {
                proxy.bindView(item);
            }
            if (item != null) {
                refreshRenderView(item);
            }
            container.updatePlaceHolder();
        }

        @Override
        protected void onSwitchBackAfter() {
            PLVHCLinkMicItemContainer container = findContainer();
            if (container == null) {
                return;
            }

            container.updateViewReference();
            final PLVHCLinkMicItemView item = itemView == null ? null : itemView.get();
            final PLVHCLinkMicItemViewProxy proxy = itemViewProxy == null ? null : itemViewProxy.get();
            if (proxy != null) {
                proxy.bindView(null);
            }
            if (item != null) {
                refreshRenderView(item);
            }
            container.updatePlaceHolder();
        }

        @Nullable
        private PLVHCLinkMicItemContainer findContainer() {
            if (switchViewAnchorLayout == null) {
                return null;
            }
            View view = switchViewAnchorLayout;
            while (!(view instanceof PLVHCLinkMicItemContainer)) {
                if (!(view.getParent() instanceof View)) {
                    return null;
                }
                view = (View) view.getParent();
            }
            return (PLVHCLinkMicItemContainer) view;
        }

        private static void refreshRenderView(@Nullable IPLVHCLinkMicItem linkMicItem) {
            if (linkMicItem == null) {
                return;
            }
            linkMicItem.releaseRenderView();
            linkMicItem.setupRenderView();
        }

    }

    // </editor-fold>

}
