package com.easefun.polyv.livehiclass.modules.linkmic.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;

import com.easefun.polyv.livehiclass.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 1v16的连麦item布局
 */
public class PLVHCLinkMicItemLargeLayout extends PLVHCAbsLinkMicItemLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int MAX_ITEM_COUNT = 17;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCLinkMicItemLargeLayout(Context context) {
        this(context, null);
    }

    public PLVHCLinkMicItemLargeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLinkMicItemLargeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_linkmic_large_layout, this, true);

        List<PLVHCLinkMicItemView> itemViewList = new ArrayList<>();
        for (int i = 0; i < MAX_ITEM_COUNT; i++) {
            int id = getContext().getResources().getIdentifier("plvhc_linkmic_item_" + i, "id", getContext().getPackageName());
            final PLVHCLinkMicItemView linkMicItemView = findViewById(id);
            linkMicItemView.setVisibility(getHideItemMode());
            final int finalI = i;
            linkMicItemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onClickItemView(finalI, linkMicItemView.getLinkMicItemDataBean());
                    }
                }
            });
            linkMicItemView.init(true, new PLVHCLinkMicItemView.OnRenderViewCallback() {
                @Override
                public SurfaceView createLinkMicRenderView() {
                    return onRenderViewCallback != null ? onRenderViewCallback.createLinkMicRenderView() : null;
                }

                @Override
                public void releaseLinkMicRenderView(SurfaceView renderView) {
                    if (onRenderViewCallback != null) {
                        onRenderViewCallback.releaseLinkMicRenderView(renderView);
                    }
                }

                @Override
                public void setupRenderView(SurfaceView surfaceView, String linkMicId, int streamType) {
                    if (onRenderViewCallback != null) {
                        onRenderViewCallback.setupRenderView(surfaceView, linkMicId, streamType);
                    }
                }
            });
            itemViewList.add(linkMicItemView);
        }
        setItemViewList(itemViewList);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现PLVHCAbsLinkMicItemLayout定义的方法">
    @Override
    public int getMaxItemCount() {
        return MAX_ITEM_COUNT;
    }

    @Override
    public int getHideItemMode() {
        return View.INVISIBLE;
    }
    // </editor-fold>
}
