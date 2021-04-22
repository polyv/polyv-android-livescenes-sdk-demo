package com.easefun.polyv.livestreamer.modules.document.popuplist.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livestreamer.modules.document.popuplist.enums.PLVLSPptViewType;
import com.easefun.polyv.livestreamer.modules.document.popuplist.holder.PLVLSPptListViewHolder;
import com.easefun.polyv.livestreamer.modules.document.popuplist.vo.PLVLSPptVO;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSAbsPptViewItem;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSPptCoverView;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSPptPageView;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSPptUploadView;

import java.util.ArrayList;
import java.util.List;

/**
 * PPT文档列表和PPT页面列表的适配器
 *
 * @author suhongtao
 */
public class PLVLSPptListAdapter extends RecyclerView.Adapter<PLVLSPptListViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 视图数据列表
    private List<PLVLSPptVO> pptVOList;

    // 视图类型：PPT文档列表 或 PPT页面列表
    @PLVLSPptViewType.Range
    private int viewType = PLVLSPptViewType.COVER;

    // 列表项点击回调监听
    private PLVLSPptListViewHolder.OnPptItemClickListener onPptItemClickListener;
    // 列表项长按点击回调监听
    private PLVLSPptListViewHolder.OnPptItemLongClickListener onPptItemLongClickListener;
    // 列表项上传状态层按钮点击回调
    private PLVLSPptListViewHolder.OnUploadViewButtonClickListener onUploadViewButtonClickListener;

    // 当前选中的列表项id，文档列表为autoId，PPT页面列表为pageId
    private int currentSelectedId = -1;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSPptListAdapter(List<PLVLSPptVO> pptVOList, @PLVLSPptViewType.Range int viewType) {
        updatePptList(pptVOList, viewType);
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
    public PLVLSPptListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PLVLSAbsPptViewItem pptViewItem = null;
        switch (viewType) {
            case PLVLSPptViewType.COVER:
                pptViewItem = new PLVLSPptCoverView(parent.getContext());
                break;
            case PLVLSPptViewType.PAGE:
                pptViewItem = new PLVLSPptPageView(parent.getContext());
                break;
            case PLVLSPptViewType.UPLOAD:
                pptViewItem = new PLVLSPptUploadView(parent.getContext());
                break;
            default:
                break;
        }
        return new PLVLSPptListViewHolder(pptViewItem);
    }

    /**
     * ViewHolder数据绑定
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull PLVLSPptListViewHolder holder, int position) {
        int realListPosition = position;
        if (viewType == PLVLSPptViewType.COVER) {
            // 展示PPT文档列表时，第一项是上传文档按钮
            realListPosition = position - 1;
        }
        if (viewType == PLVLSPptViewType.COVER && position == 0) {
            // PPT文档列表 上传文档按钮
            // 不需要绑定数据
        } else {
            // 其它列表项
            holder.bindData(pptVOList.get(realListPosition));
            final boolean selected = pptVOList.get(realListPosition).getId() != null
                    && pptVOList.get(realListPosition).getId() == currentSelectedId;
            final boolean notUploadPpt = pptVOList.get(realListPosition).getUploadStatus() == null
                    || PLVPptUploadStatus.isStatusConvertSuccess(pptVOList.get(realListPosition).getUploadStatus());
            holder.setSelected(selected && notUploadPpt);
            holder.setIndex(realListPosition);
            holder.setOnPptItemClickListener(new PLVLSPptListViewHolder.OnPptItemClickListener() {
                @Override
                public void onClick(int id) {
                    // 只有已经转码完成的PPT才响应点击事件
                    if (onPptItemClickListener != null && notUploadPpt) {
                        onPptItemClickListener.onClick(id);
                    }
                }
            });
            holder.setOnPptItemLongClickListener(new PLVLSPptListViewHolder.OnPptItemLongClickListener() {
                @Override
                public void onLongClick(View view, int id, String fileId) {
                    if (onPptItemLongClickListener != null) {
                        onPptItemLongClickListener.onLongClick(view, id, fileId);
                    }
                }
            });
            holder.setOnUploadViewButtonClickListener(new PLVLSPptListViewHolder.OnUploadViewButtonClickListener() {
                @Override
                public void onClick(PLVLSPptVO pptVO) {
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
        if (viewType == PLVLSPptViewType.COVER) {
            // 多出一个位置给上传按钮
            return getRealItemCount() + 1;
        } else {
            return getRealItemCount();
        }
    }

    /**
     * 视图类型
     * 当展示PPT文档列表，并且是第一个位置时，返回上传文档按钮的类型
     * 需要获得实际视图类型时请使用{@link #getRealViewType()}
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (viewType == PLVLSPptViewType.COVER && position == 0) {
            return PLVLSPptViewType.UPLOAD;
        } else {
            return viewType;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 更新视图列表
     *
     * @param pptVOList 新的视图数据
     * @param newViewType 新的视图类型
     */
    public void updatePptList(List<PLVLSPptVO> pptVOList, @PLVLSPptViewType.Range int newViewType) {
        if (this.pptVOList == null) {
            this.pptVOList = new ArrayList<>();
        }
        if (pptVOList == null) {
            return;
        }

        setViewType(newViewType);
        this.pptVOList.clear();
        this.pptVOList.addAll(pptVOList);

        notifyDataSetChanged();
    }

    /**
     * 获取实际视图类型
     *
     * @return 视图类型
     * @see #getItemViewType(int)
     */
    public int getRealViewType() {
        return viewType;
    }

    /**
     * 设置当前选择的列表项
     *
     * @param currentSelectedId 文档列表为autoId，ppt页面列表为pageId
     */
    public void setCurrentSelectedId(int currentSelectedId) {
        this.currentSelectedId = currentSelectedId;
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

    // <editor-fold defaultstate="collapsed" desc="内部逻辑">

    /**
     * 设置视图类型
     *
     * @param viewType
     */
    private void setViewType(@PLVLSPptViewType.Range int viewType) {
        this.viewType = viewType;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置回调接口">

    /**
     * 设置列表项点击监听回调
     *
     * @param onPptItemClickListener
     */
    public void setOnPptItemClickListener(PLVLSPptListViewHolder.OnPptItemClickListener onPptItemClickListener) {
        this.onPptItemClickListener = onPptItemClickListener;
    }

    /**
     * 设置列表项长按点击监听回调
     *
     * @param onPptItemLongClickListener
     */
    public void setOnPptItemLongClickListener(PLVLSPptListViewHolder.OnPptItemLongClickListener onPptItemLongClickListener) {
        this.onPptItemLongClickListener = onPptItemLongClickListener;
    }

    /**
     * 设置列表项上传状态层按钮点击回调
     *
     * @param onUploadViewButtonClickListener
     */
    public void setOnUploadViewButtonClickListener(PLVLSPptListViewHolder.OnUploadViewButtonClickListener onUploadViewButtonClickListener) {
        this.onUploadViewButtonClickListener = onUploadViewButtonClickListener;
    }

    // </editor-fold>

}
