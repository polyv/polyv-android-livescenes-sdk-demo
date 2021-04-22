package com.easefun.polyv.livestreamer.modules.document.popuplist.holder;

import android.support.annotation.IntRange;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.easefun.polyv.livestreamer.modules.document.popuplist.vo.PLVLSPptVO;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSAbsPptViewItem;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSPptCoverView;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSPptPageView;

/**
 * PPT文档列表和PPT页面列表的ViewHolder
 *
 * @author suhongtao
 */
public class PLVLSPptListViewHolder extends RecyclerView.ViewHolder {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 列表项视图
    private PLVLSAbsPptViewItem pptViewItem;

    // 列表项点击回调
    private OnPptItemClickListener onPptItemClickListener;
    // 列表项长按点击回调
    private OnPptItemLongClickListener onPptItemLongClickListener;
    // 列表项上传状态层按钮点击回调
    private OnUploadViewButtonClickListener onUploadViewButtonClickListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSPptListViewHolder(PLVLSAbsPptViewItem pptViewItem) {
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
    public void bindData(final PLVLSPptVO pptVO) {
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
        if (pptViewItem instanceof PLVLSPptCoverView) {
            ((PLVLSPptCoverView) pptViewItem).setOnUploadProgressViewButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onUploadViewButtonClickListener != null) {
                        onUploadViewButtonClickListener.onClick(pptVO);
                    }
                }
            });
        }
    }

    /**
     * 设置列表项索引
     *
     * @param index 索引序号，从0开始
     */
    public void setIndex(@IntRange(from = 0) int index) {
        if (pptViewItem instanceof PLVLSPptPageView) {
            ((PLVLSPptPageView) pptViewItem).setIndexText(String.valueOf(index + 1));
        }
    }

    /**
     * 设置列表项是否被选中
     *
     * @param selected 是否被选中
     */
    public void setSelected(boolean selected) {
        pptViewItem.setSelected(selected);
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
        void onClick(PLVLSPptVO pptVO);
    }

    // </editor-fold>
}
