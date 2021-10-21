package com.easefun.polyv.livehiclass.modules.linkmic.item;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;

import java.util.List;

/**
 * 连麦item布局的抽象类
 */
public abstract class PLVHCAbsLinkMicItemLayout extends ConstraintLayout implements IPLVHCLinkMicItemLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //data
    private List<PLVLinkMicItemDataBean> dataBeanList;
    //viewList
    private List<PLVHCLinkMicItemView> itemViewList;
    //占位item
    private PLVLinkMicItemDataBean placeDataBean;
    //listener
    protected PLVHCLinkMicItemView.OnRenderViewCallback onRenderViewCallback;
    protected OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCAbsLinkMicItemLayout(Context context) {
        super(context);
    }

    public PLVHCAbsLinkMicItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVHCAbsLinkMicItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVHCLinkMicItemLayout定义的方法">
    @Override
    public void bindData(List<PLVLinkMicItemDataBean> dataBeanList) {
        this.dataBeanList = dataBeanList;
        for (int i = 0; i < Math.min(getDataCount(), dataBeanList.size()); i++) {
            itemViewList.get(i).bindData(dataBeanList.get(i));
        }
    }

    @Override
    public void setPlaceLinkMicItem(PLVLinkMicItemDataBean placeLinkMicItem, boolean isTeacherPreparing) {
        if ((dataBeanList != null && dataBeanList.isEmpty())
                || placeDataBean != null) {
            placeDataBean = placeLinkMicItem;
            itemViewList.get(0).setVisibility(View.VISIBLE);
            itemViewList.get(0).bindData(placeDataBean);
            itemViewList.get(0).updateTeacherPreparingStatus(isTeacherPreparing);
            itemViewList.get(0).removeRenderView();
        }
    }

    @Override
    public List<PLVLinkMicItemDataBean> getDataBeanList() {
        return dataBeanList;
    }

    @Override
    public void onUserJoin(PLVLinkMicItemDataBean dataBean, int position) {
        if (dataBean == null) {
            return;
        }
        if (placeDataBean != null
                && dataBean.isTeacher()
                && position == 0) {
            itemViewList.get(0).bindData(dataBean);
            itemViewList.get(0).updateTeacherPreparingStatus(false);
            placeDataBean = null;
            return;
        }
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        PLVHCLinkMicItemView newLastDataItemView = itemViewList.get(Math.max(getDataCount() - 1, 0));
        newLastDataItemView.setVisibility(View.VISIBLE);
        PLVHCLinkMicItemView.ViewParam viewParam = new PLVHCLinkMicItemView.ViewParam();
        newLastDataItemView.removeItemView(viewParam);
        for (int i = getDataCount() - 2; i >= position; i--) {
            itemViewList.get(i).moveToItemView(itemViewList.get(i + 1));
        }
        itemViewList.get(position).addItemView(viewParam);
        itemViewList.get(position).bindData(dataBean);
    }

    @Override
    public void onUserLeave(PLVLinkMicItemDataBean dataBean, int position) {
        if (dataBean == null) {
            return;
        }
        if (placeDataBean == null
                && dataBean.isTeacher()
                && position == 0) {
            placeDataBean = dataBean;
            itemViewList.get(0).removeRenderView();
            return;
        }
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        PLVHCLinkMicItemView.ViewParam viewParam = new PLVHCLinkMicItemView.ViewParam();
        itemViewList.get(position).removeItemView(viewParam);
        for (int i = position + 1; i < Math.min(getDataCount() + 1, getMaxItemCount()); i++) {
            itemViewList.get(i).moveToItemView(itemViewList.get(i - 1));
        }
        PLVHCLinkMicItemView oldLastDataItemView = itemViewList.get(Math.min(getDataCount(), getMaxItemCount() - 1));
        oldLastDataItemView.setVisibility(getHideItemMode());
        oldLastDataItemView.addItemView(viewParam);
    }

    @Override
    public void onUserExisted(PLVLinkMicItemDataBean dataBean, int position) {

    }

    @Override
    public void updateListData(List<PLVLinkMicItemDataBean> dataBeanList) {
        int startPosition = placeDataBean == null ? 0 : 1;
        int dataBeanListIndex = -1;
        for (int i = startPosition; i < Math.min(getDataCount(), getMaxItemCount()); i++) {
            dataBeanListIndex++;
            String dataBeanLinkMicId = dataBeanList.get(dataBeanListIndex).getLinkMicId();
            String viewItemLinkMicId = itemViewList.get(i).getLinkMicId();
            if (dataBeanLinkMicId == null || dataBeanLinkMicId.equals(viewItemLinkMicId)) {
                continue;
            } else {
                for (int j = i + 1; j < Math.min(getDataCount(), getMaxItemCount()); j++) {
                    String findViewItemLinkMicId = itemViewList.get(j).getLinkMicId();
                    if (dataBeanLinkMicId.equals(findViewItemLinkMicId)) {
                        itemViewList.get(i).replaceItemView(itemViewList.get(j));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void updateUserMuteVideo(int position) {
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        itemViewList.get(position).updateVideoStatus();
    }

    @Override
    public void updateUserMuteAudio(int position) {
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        itemViewList.get(position).updateAudioStatus();
    }

    @Override
    public void updateVolumeChanged() {
        for (int i = 0; i < getDataCount(); i++) {
            itemViewList.get(i).updateAudioStatus();
        }
    }

    @Override
    public void onUserRaiseHand(int position) {
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        itemViewList.get(position).updateHandsUp();
    }

    @Override
    public void onUserGetCup(int position) {
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        itemViewList.get(position).updateCupNum();
    }

    @Override
    public void onUserHasPaint(int position) {
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        itemViewList.get(position).updateHasPaint();
    }

    @Override
    public void setOnRenderViewCallback(PLVHCLinkMicItemView.OnRenderViewCallback onRenderViewCallback) {
        this.onRenderViewCallback = onRenderViewCallback;
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    protected void setItemViewList(List<PLVHCLinkMicItemView> itemViewList) {
        this.itemViewList = itemViewList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="抽象方法">
    public abstract int getMaxItemCount();

    public abstract int getHideItemMode();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private int getDataCount() {
        //包括额外添加的不在dataBeanList的占位item
        return dataBeanList == null ? 0 : Math.min(dataBeanList.size() + (placeDataBean == null ? 0 : 1), getMaxItemCount());
    }

    private boolean checkPosition(int position) {
        if (position < (placeDataBean == null ? 0 : 1) || position > getMaxItemCount() - 1) {
            return false;
        }
        return true;
    }
    // </editor-fold>
}
