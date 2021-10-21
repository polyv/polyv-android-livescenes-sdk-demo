package com.easefun.polyv.livehiclass.modules.document.popuplist.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.easefun.polyv.livehiclass.modules.document.popuplist.vo.PLVHCPptVO;
import com.easefun.polyv.livehiclass.modules.document.popuplist.widget.PLVHCAbsPptViewItem;
import com.easefun.polyv.livehiclass.modules.document.popuplist.widget.PLVHCPptCoverView;

/**
 * PPT文档列表和PPT页面列表的ViewHolder
 *
 * @author suhongtao
 */
public class PLVHCPptListViewHolder extends RecyclerView.ViewHolder {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 列表项视图
    private PLVHCAbsPptViewItem pptViewItem;

    // 列表项点击回调
    private OnPptItemClickListener onPptItemClickListener;
    // 列表项长按点击回调
    private OnPptItemLongClickListener onPptItemLongClickListener;
    // 列表项上传状态层按钮点击回调
    private OnUploadViewButtonClickListener onUploadViewButtonClickListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCPptListViewHolder(PLVHCAbsPptViewItem pptViewItem) {
        super(pptViewItem);
        this.pptViewItem = pptViewItem;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 视图数据绑定
     *
     * @param pptVO
     */
    public void bindData(final PLVHCPptVO pptVO) {
        pptViewItem.processData(pptVO);
        pptViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPptItemClickListener != null && pptVO.getId() != null) {
                    onPptItemClickListener.onClick(pptVO.getId());
                }
            }
        });
        pptViewItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onPptItemLongClickListener != null && pptVO.getId() != null) {
                    onPptItemLongClickListener.onLongClick(v, pptVO.getId(), pptVO.getFileId());
                }
                return true;
            }
        });
        if (pptViewItem instanceof PLVHCPptCoverView) {
            ((PLVHCPptCoverView) pptViewItem).setOnUploadProgressViewButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onUploadViewButtonClickListener != null) {
                        onUploadViewButtonClickListener.onClick(pptVO);
                    }
                }
            });
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置回调接口 & 接口定义">

    /**
     * 设置列表项点击监听回调
     *
     * @param onPptItemClickListener
     */
    public void setOnPptItemClickListener(OnPptItemClickListener onPptItemClickListener) {
        this.onPptItemClickListener = onPptItemClickListener;
    }

    /**
     * 设置列表项长按点击回调
     *
     * @param onPptItemLongClickListener
     */
    public void setOnPptItemLongClickListener(OnPptItemLongClickListener onPptItemLongClickListener) {
        this.onPptItemLongClickListener = onPptItemLongClickListener;
    }

    /**
     * 设置列表项上传状态层按钮点击回调
     *
     * @param onUploadViewButtonClickListener
     */
    public void setOnUploadViewButtonClickListener(OnUploadViewButtonClickListener onUploadViewButtonClickListener) {
        this.onUploadViewButtonClickListener = onUploadViewButtonClickListener;
    }

    public interface OnPptItemClickListener {
        void onClick(int id);
    }

    public interface OnPptItemLongClickListener {
        void onLongClick(View view, int id, String fileId);
    }

    public interface OnUploadViewButtonClickListener {
        void onClick(PLVHCPptVO pptVO);
    }

    // </editor-fold>
}
