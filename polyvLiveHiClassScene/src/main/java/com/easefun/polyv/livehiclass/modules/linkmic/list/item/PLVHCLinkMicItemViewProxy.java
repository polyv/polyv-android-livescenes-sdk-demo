package com.easefun.polyv.livehiclass.modules.linkmic.list.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

/**
 * 连麦item转发类
 *
 * @author suhongtao
 */
public class PLVHCLinkMicItemViewProxy extends FrameLayout implements IPLVHCLinkMicItem {

    @Nullable
    private PLVHCLinkMicItemView target;

    public PLVHCLinkMicItemViewProxy(@NonNull Context context) {
        super(context);
    }

    public PLVHCLinkMicItemViewProxy(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVHCLinkMicItemViewProxy(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 注册需要转发到的item
     *
     * @param view 连麦item，null时为清空绑定
     */
    public void bindView(PLVHCLinkMicItemView view) {
        this.target = view;
    }

    @Override
    public void init(boolean isLargeLayout, OnRenderViewCallback callback) {
        if (target != null) {
            target.init(isLargeLayout, callback);
        }
    }

    @Override
    public void bindData(PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (target != null) {
            target.bindData(linkMicItemDataBean);
        }
    }

    @Nullable
    @Override
    public PLVLinkMicItemDataBean getLinkMicItemDataBean() {
        if (target != null) {
            return target.getLinkMicItemDataBean();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getLinkMicId() {
        if (target != null) {
            return target.getLinkMicId();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public PLVHCLinkMicItemContainer findContainerParent() {
        View view = this;
        while (!(view instanceof PLVHCLinkMicItemContainer)) {
            if (!(view.getParent() instanceof View)) {
                return null;
            }
            view = (View) view.getParent();
        }
        return (PLVHCLinkMicItemContainer) view;
    }

    @Override
    public void releaseRenderView() {
        if (target != null) {
            target.releaseRenderView();
        }
    }

    @Override
    public void removeRenderView() {
        if (target != null) {
            target.removeRenderView();
        }
    }

    @Override
    public void setupRenderView() {
        if (target != null) {
            target.setupRenderView();
        }
    }

    @Override
    public void updateTeacherPreparingStatus(boolean isPreparing) {
        if (target != null) {
            target.updateTeacherPreparingStatus(isPreparing);
        }
    }

    @Override
    public void updateLeaderStatus(boolean isHasLeader) {
        if (target != null) {
            target.updateLeaderStatus(isHasLeader);
        }
    }

    @Override
    public void updateVideoStatus() {
        if (target != null) {
            target.updateVideoStatus();
        }
    }

    @Override
    public void updateAudioStatus() {
        if (target != null) {
            target.updateAudioStatus();
        }
    }

    @Override
    public void updateHandsUp() {
        if (target != null) {
            target.updateHandsUp();
        }
    }

    @Override
    public void updateHasPaint() {
        if (target != null) {
            target.updateHasPaint();
        }
    }

    @Override
    public void updateCupNum() {
        if (target != null) {
            target.updateCupNum();
        }
    }

    @Override
    public void updateZoom(PLVUpdateMicSiteEvent updateMicSiteEvent) {
        if (target != null) {
            target.updateZoom(updateMicSiteEvent);
        }
    }

    @Override
    public void switchWithItemView(IPLVHCLinkMicItem linkMicItemView) {
        if (target != null) {
            target.switchWithItemView(linkMicItemView);
        }
    }

    @Override
    public void moveToItemView(IPLVHCLinkMicItem linkMicItemView) {
        if (target != null) {
            target.moveToItemView(linkMicItemView);
        }
    }

    @Override
    public View removeItemView() {
        if (target != null) {
            return target.removeItemView();
        } else {
            return null;
        }
    }

    @Override
    public void addItemView(View rootView) {
        if (target != null) {
            target.addItemView(rootView);
        }
    }

    @Override
    public void addView(View child) {
        if (target != null) {
            target.addView(child);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (this.target != null) {
            this.target.setOnClickListener(l);
        }
    }

    @Override
    public void removeView(View view) {
        if (this.target != null) {
            this.target.removeView(view);
        }
    }
}
