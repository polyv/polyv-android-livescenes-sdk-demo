package com.easefun.polyv.livehiclass.modules.document.popuplist.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livehiclass.modules.document.popuplist.enums.PLVHCPptViewType;
import com.easefun.polyv.livehiclass.modules.document.popuplist.holder.PLVHCPptListViewHolder;
import com.easefun.polyv.livehiclass.modules.document.popuplist.vo.PLVHCPptVO;
import com.easefun.polyv.livehiclass.modules.document.popuplist.widget.PLVHCAbsPptViewItem;
import com.easefun.polyv.livehiclass.modules.document.popuplist.widget.PLVHCPptCoverView;
import com.easefun.polyv.livehiclass.modules.document.popuplist.widget.PLVHCPptUploadView;

import java.util.ArrayList;
import java.util.List;

/**
 * PPT文档列表和PPT页面列表的适配器
 *
 * @author suhongtao
 */
public class PLVHCPptListAdapter extends RecyclerView.Adapter<PLVHCPptListViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 视图数据列表
    private List<PLVHCPptVO> pptVOList;

    // 列表项点击回调监听
    private PLVHCPptListViewHolder.OnPptItemClickListener onPptItemClickListener;
    // 列表项长按点击回调监听
    private PLVHCPptListViewHolder.OnPptItemLongClickListener onPptItemLongClickListener;
    // 列表项上传状态层按钮点击回调
    private PLVHCPptListViewHolder.OnUploadViewButtonClickListener onUploadViewButtonClickListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCPptListAdapter(List<PLVHCPptVO> pptVOList) {
        updatePptList(pptVOList);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adapter重写方法">

    /**
     * ViewHolder创建
     *
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public PLVHCPptListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PLVHCAbsPptViewItem pptViewItem = null;
        switch (viewType) {
            case PLVHCPptViewType.COVER:
                pptViewItem = new PLVHCPptCoverView(parent.getContext());
                break;
            case PLVHCPptViewType.UPLOAD:
                pptViewItem = new PLVHCPptUploadView(parent.getContext());
                break;
            default:
                break;
        }
        return new PLVHCPptListViewHolder(pptViewItem);
    }

    /**
     * ViewHolder数据绑定
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull PLVHCPptListViewHolder holder, int position) {
        int realListPosition = position - 1;
        if (position != 0) {
            // position == 0 -> 上传文档按钮不需要绑定数据

            holder.bindData(pptVOList.get(realListPosition));
            final boolean notUploadedPpt = pptVOList.get(realListPosition).getUploadStatus() == null
                    || PLVPptUploadStatus.isStatusConvertSuccess(pptVOList.get(realListPosition).getUploadStatus());
            holder.setOnPptItemClickListener(new PLVHCPptListViewHolder.OnPptItemClickListener() {
                @Override
                public void onClick(int id) {
                    // 只有已经转码完成的PPT才响应点击事件
                    if (onPptItemClickListener != null && notUploadedPpt) {
                        onPptItemClickListener.onClick(id);
                    }
                }
            });
            holder.setOnPptItemLongClickListener(new PLVHCPptListViewHolder.OnPptItemLongClickListener() {
                @Override
                public void onLongClick(View view, int id, String fileId) {
                    if (onPptItemLongClickListener != null) {
                        onPptItemLongClickListener.onLongClick(view, id, fileId);
                    }
                }
            });
            holder.setOnUploadViewButtonClickListener(new PLVHCPptListViewHolder.OnUploadViewButtonClickListener() {
                @Override
                public void onClick(PLVHCPptVO pptVO) {
                    if (onUploadViewButtonClickListener != null) {
                        onUploadViewButtonClickListener.onClick(pptVO);
                    }
                }
            });
        }
    }

    /**
     * 数据列表个数
     * 当展示PPT文档列表时，多出一个位置留给上传按钮
     * 需要获得实际列表项个数时请使用{@link #getRealItemCount()}
     *
     * @return
     */
    @Override
    public int getItemCount() {
        // 多出一个位置给上传按钮
        return getRealItemCount() + 1;
    }

    /**
     * 视图类型
     * 当展示PPT文档列表，并且是第一个位置时，返回上传文档按钮的类型
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return PLVHCPptViewType.UPLOAD;
        } else {
            return PLVHCPptViewType.COVER;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 更新视图列表
     *
     * @param pptVOList 新的视图数据
     */
    public void updatePptList(List<PLVHCPptVO> pptVOList) {
        if (this.pptVOList == null) {
            this.pptVOList = new ArrayList<>();
        }
        if (pptVOList == null) {
            return;
        }

        this.pptVOList.clear();
        this.pptVOList.addAll(pptVOList);

        notifyDataSetChanged();
    }

    /**
     * 获取实际列表项个数
     *
     * @return item count
     */
    public int getRealItemCount() {
        if (this.pptVOList == null) {
            return 0;
        } else {
            return this.pptVOList.size();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置回调接口">

    /**
     * 设置列表项点击监听回调
     *
     * @param onPptItemClickListener
     */
    public void setOnPptItemClickListener(PLVHCPptListViewHolder.OnPptItemClickListener onPptItemClickListener) {
        this.onPptItemClickListener = onPptItemClickListener;
    }

    /**
     * 设置列表项长按点击监听回调
     *
     * @param onPptItemLongClickListener
     */
    public void setOnPptItemLongClickListener(PLVHCPptListViewHolder.OnPptItemLongClickListener onPptItemLongClickListener) {
        this.onPptItemLongClickListener = onPptItemLongClickListener;
    }

    /**
     * 设置列表项上传状态层按钮点击回调
     *
     * @param onUploadViewButtonClickListener
     */
    public void setOnUploadViewButtonClickListener(PLVHCPptListViewHolder.OnUploadViewButtonClickListener onUploadViewButtonClickListener) {
        this.onUploadViewButtonClickListener = onUploadViewButtonClickListener;
    }

    // </editor-fold>

}
