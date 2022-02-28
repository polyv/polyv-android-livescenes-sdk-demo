package com.easefun.polyv.livehiclass.modules.linkmic.list;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livehiclass.modules.linkmic.list.item.IPLVHCLinkMicItem;
import com.plv.socket.event.linkmic.PLVRemoveMicSiteEvent;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 连麦item布局的抽象类
 */
public abstract class PLVHCAbsLinkMicItemLayout extends ConstraintLayout implements IPLVHCLinkMicItemLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //data
    private List<PLVLinkMicItemDataBean> dataBeanList;
    private boolean isJoinDiscuss;
    private String leaderId;
    //viewList
    private List<IPLVHCLinkMicItem> itemViewList;
    //占位item
    private PLVLinkMicItemDataBean placeDataBean;
    private boolean isTeacherPreparing;
    //listener
    protected IPLVHCLinkMicItem.OnRenderViewCallback onRenderViewCallback;
    protected OnViewActionListener onViewActionListener;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * 缓存更新摄像头放大事件，避免有时候先收到事件，然后用户才加入到连麦列表
     * Key: linkMicId
     * Value: Runnable 处理摄像头放大逻辑
     */
    private final Map<String, Runnable> pendingUpdateZoomEventMap = new ConcurrentHashMap<>();
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
    public void bindData(List<PLVLinkMicItemDataBean> dataBeanList, boolean isJoinDiscuss) {
        this.dataBeanList = dataBeanList;
        this.isJoinDiscuss = isJoinDiscuss;
        for (int i = 0; i < Math.min(getDataCount(), dataBeanList.size()); i++) {
            itemViewList.get(i).bindData(dataBeanList.get(i));
        }
    }

    @Override
    public void clearData(boolean isJoinDiscuss) {
        for (int i = 0; i < getDataCount(); i++) {
            itemViewList.get(i).removeRenderView();
            itemViewList.get(i).setVisibility(getHideItemMode());
        }
        dataBeanList = null;
        placeDataBean = null;
        if (!isJoinDiscuss) {
            leaderId = null;
        }
    }

    @Override
    public void setPlaceLinkMicItem(PLVLinkMicItemDataBean placeLinkMicItem, boolean isTeacherPreparing) {
        if ((dataBeanList != null && dataBeanList.isEmpty())
                || placeDataBean != null) {
            this.placeDataBean = placeLinkMicItem;
            this.isTeacherPreparing = isTeacherPreparing;
            itemViewList.get(0).setVisibility(View.VISIBLE);
            itemViewList.get(0).bindData(placeDataBean);
            itemViewList.get(0).removeRenderView();
            itemViewList.get(0).updateTeacherPreparingStatus(isTeacherPreparing);
        }
    }

    @Override
    public void updatePlaceLinkMicItemNick(String nick) {
        if (placeDataBean != null && TextUtils.isEmpty(placeDataBean.getNick())) {
            placeDataBean.setNick(nick);
            setPlaceLinkMicItem(placeDataBean, isTeacherPreparing);
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
                && position == 0
                && !isJoinDiscuss) {
            itemViewList.get(0).bindData(dataBean);
            itemViewList.get(0).updateTeacherPreparingStatus(false);
            placeDataBean = null;
            return;
        }
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        // 取出列表最后一个
        IPLVHCLinkMicItem newLastDataItemView = itemViewList.get(Math.max(getDataCount() - 1, 0));
        newLastDataItemView.setVisibility(View.VISIBLE);
        View itemView = newLastDataItemView.removeItemView();
        // position 至 列表倒数第二个，按倒序依次往后移动
        for (int i = getDataCount() - 2; i >= position; i--) {
            itemViewList.get(i).moveToItemView(itemViewList.get(i + 1));
        }
        // position位置插入新加入的用户
        itemViewList.get(position).addItemView(itemView);
        itemViewList.get(position).bindData(dataBean);
        if (leaderId != null && leaderId.equals(dataBean.getLinkMicId())) {
            itemViewList.get(position).updateLeaderStatus(true);
        }
    }

    @Override
    public void onUserLeave(PLVLinkMicItemDataBean dataBean, int position) {
        if (dataBean == null) {
            return;
        }
        if (placeDataBean == null
                && dataBean.isTeacher()
                && position == 0
                && !isJoinDiscuss) {
            placeDataBean = dataBean;
            itemViewList.get(0).removeRenderView();
            itemViewList.get(0).updateTeacherPreparingStatus(false);
            return;
        }
        position = position + (placeDataBean == null ? 0 : 1);
        if (!checkPosition(position)) {
            return;
        }
        // 移出 position 位置
        View itemView = itemViewList.get(position).removeItemView();
        // position+1 至 列表最后一个，按顺序依次往前移动
        int lastIndex = position;
        for (int i = position + 1; i < Math.min(getDataCount() + 1, getMaxItemCount()); i++) {
            if (i >= itemViewList.size() || i - 1 >= dataBeanList.size()) {
                break;
            }
            lastIndex = i;
            itemViewList.get(i).moveToItemView(itemViewList.get(i - 1));
            itemViewList.get(i - 1).bindData(dataBeanList.get(i - 1));
        }
        // 将移出的视图重新添加回去，并隐藏
        IPLVHCLinkMicItem oldLastDataItemView = itemViewList.get(lastIndex);
        oldLastDataItemView.setVisibility(getHideItemMode());
        oldLastDataItemView.addItemView(itemView);
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
                        itemViewList.get(i).switchWithItemView(itemViewList.get(j));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void notifyRejoinRoom() {
        final int startPosition = placeDataBean == null ? 0 : 1;
        for (int i = startPosition; i < Math.min(getDataCount(), getMaxItemCount()); i++) {
            final IPLVHCLinkMicItem itemView = itemViewList.get(i);
            if (itemView.getLinkMicId() == null) {
                continue;
            }
            // 重连后刷新画面
            itemView.releaseRenderView();
            itemView.setupRenderView();
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
    public void onUserHasLeader(String leaderId) {
        String oldLeaderId = this.leaderId;
        this.leaderId = leaderId;
        for (int i = 0; i < getDataCount(); i++) {
            String linkMicId = itemViewList.get(i).getLinkMicId();
            if (linkMicId != null) {
                if (linkMicId.equals(this.leaderId)) {
                    itemViewList.get(i).updateLeaderStatus(true);
                } else if (linkMicId.equals(oldLeaderId)) {
                    itemViewList.get(i).updateLeaderStatus(false);
                }
            }
        }
    }

    @Override
    public void onUserUpdateZoom(final PLVUpdateMicSiteEvent updateMicSiteEvent) {
        if (updateMicSiteEvent == null || !updateMicSiteEvent.checkIsValid()) {
            return;
        }
        final String eventLinkMicId = updateMicSiteEvent.getLinkMicIdFromEventId();
        IPLVHCLinkMicItem linkMicItem = getItemByLinkMicId(eventLinkMicId);
        if (linkMicItem != null) {
            linkMicItem.updateZoom(updateMicSiteEvent);
            return;
        }

        // 没有找到对应的item，3秒后重试
        final Runnable pendingUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                final IPLVHCLinkMicItem linkMicItemRetry = getItemByLinkMicId(eventLinkMicId);
                if (linkMicItemRetry != null) {
                    linkMicItemRetry.updateZoom(updateMicSiteEvent);
                }
            }
        };
        pendingUpdateZoomEventMap.put(eventLinkMicId, pendingUpdateRunnable);
        mainHandler.postDelayed(pendingUpdateRunnable, TimeUnit.SECONDS.toMillis(3));
    }

    @Override
    public void onUserRemoveZoom(PLVRemoveMicSiteEvent removeMicSiteEvent) {
        if (removeMicSiteEvent == null || removeMicSiteEvent.getLinkMicIdFromEventId() == null) {
            return;
        }
        final String linkMicId = removeMicSiteEvent.getLinkMicIdFromEventId();
        if (pendingUpdateZoomEventMap.containsKey(linkMicId)) {
            mainHandler.removeCallbacks(pendingUpdateZoomEventMap.get(linkMicId));
            pendingUpdateZoomEventMap.remove(linkMicId);
        }
    }

    @Override
    public void setOnRenderViewCallback(IPLVHCLinkMicItem.OnRenderViewCallback onRenderViewCallback) {
        this.onRenderViewCallback = onRenderViewCallback;
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    @Override
    public void destroy() {
        for (IPLVHCLinkMicItem linkMicItem : itemViewList) {
            linkMicItem.releaseRenderView();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    protected void setItemViewList(List<IPLVHCLinkMicItem> itemViewList) {
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

    @Nullable
    protected IPLVHCLinkMicItem getItemByLinkMicId(String linkMicId) {
        if (linkMicId == null) {
            return null;
        }
        for (IPLVHCLinkMicItem linkMicItem : itemViewList) {
            if (linkMicId.equals(linkMicItem.getLinkMicId())) {
                return linkMicItem;
            }
        }
        return null;
    }
    // </editor-fold>
}
