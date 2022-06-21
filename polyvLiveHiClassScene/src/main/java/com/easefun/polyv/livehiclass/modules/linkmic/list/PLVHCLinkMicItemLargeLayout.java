package com.easefun.polyv.livehiclass.modules.linkmic.list;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.linkmic.list.item.IPLVHCLinkMicItem;

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

        List<IPLVHCLinkMicItem> itemViewList = new ArrayList<>();
        for (int i = 0; i < MAX_ITEM_COUNT; i++) {
            int id = getContext().getResources().getIdentifier("plvhc_linkmic_item_" + i, "id", getContext().getPackageName());
            final IPLVHCLinkMicItem linkMicItemView = findViewById(id);
            linkMicItemView.setVisibility(getHideItemMode());
            linkMicItemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final IPLVHCLinkMicItem linkMicItem = (IPLVHCLinkMicItem) v;
                    final int position = getDataBeanList().indexOf(linkMicItem.getLinkMicItemDataBean());
                    if (onViewActionListener != null) {
                        onViewActionListener.onClickItemView(position, linkMicItem);
                    }
                }
            });
            linkMicItemView.init(true, new IPLVHCLinkMicItem.OnRenderViewCallback() {
                @Override
                public View createLinkMicRenderView() {
                    return onRenderViewCallback != null ? onRenderViewCallback.createLinkMicRenderView() : null;
                }

                @Override
                public void releaseLinkMicRenderView(View renderView) {
                    if (onRenderViewCallback != null) {
                        onRenderViewCallback.releaseLinkMicRenderView(renderView);
                    }
                }

                @Override
                public void setupRenderView(View renderView, String linkMicId, int streamType) {
                    if (onRenderViewCallback != null) {
                        onRenderViewCallback.setupRenderView(renderView, linkMicId, streamType);
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
