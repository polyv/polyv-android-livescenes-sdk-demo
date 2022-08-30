package com.easefun.polyv.streameralone.modules.streamer.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.streamer.PLVSAStreamerMemberControlLayout;
import com.easefun.polyv.streameralone.modules.streamer.PLVSAStreamerMemberControlTipsLayout;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.List;
import java.util.Locale;

/**
 * 推流和连麦列表适配器
 */
public class PLVSAStreamerAdapter extends RecyclerView.Adapter<PLVSAStreamerAdapter.StreamerItemViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVSAStreamerAdapter.class.getSimpleName();
    public static final int ITEM_TYPE_DEFAULT = -1;
    public static final int ITEM_TYPE_ONLY_TEACHER = 1;
    //一对一，两人
    public static final int ITEM_TYPE_ONE_TO_ONE = 2;
    //四人以内，包括四人
    public static final int ITEM_TYPE_LESS_THAN_FOUR = 3;
    //超过四人
    public static final int ITEM_TYPE_MORE_THAN_FOUR = 4;

    //一对多模式，主讲模式
    public static final int ITEM_TYPE_ONE_TO_MORE = 10;

    public static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    public static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";
    private static final String PAYLOAD_UPDATE_GUEST_STATUS = "updateGuestStatus";
    private static final String PAYLOAD_UPDATE_SCREEN_SHARED = "updateScreenShared";
    private static final String PAYLOAD_UPDATE_PERMISSION_CHANGE = "updatePermissionChange";

    /**** data ****/
    private List<PLVLinkMicItemDataBean> dataList;
    //直播间数管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    /**** listener ****/
    private OnStreamerAdapterCallback adapterCallback;

    //是否是音频连麦
    private boolean isAudioLinkMic;
    //当前用户的连麦ID
    private String myLinkMicId;

    //需要隐藏的item。仅当 itemType = ITEM_TYPE_ONE_BY_MORE 时生效
    private int hideItemIndex = -1;
    //主讲模式下，放在主界面的ViewHolder
    private StreamerItemViewHolder mainViewHolder;
    private boolean isClickMainViewHolder = false;

    //rv
    private RecyclerView streamerRv;
    //window
    private PLVSAStreamerMemberControlTipsLayout controlTipsLayout;
    private PLVSAStreamerMemberControlLayout memberControlLayout;
    //curTouchItem
    private StreamerItemViewHolder currentTouchItemViewHolder;
    private StreamerItemViewHolder myselfViewHolder;
    private PLVLinkMicItemDataBean myselfDataBean;
    //gestureDetector
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private int itemType = ITEM_TYPE_DEFAULT;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStreamerAdapter(RecyclerView streamerRv, IPLVLiveRoomDataManager liveRoomDataManager, OnStreamerAdapterCallback adapterCallback) {
        this.streamerRv = streamerRv;
        this.liveRoomDataManager = liveRoomDataManager;
        this.adapterCallback = adapterCallback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public StreamerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resId;
        if (itemType == ITEM_TYPE_ONLY_TEACHER) {
            resId = R.layout.plvsa_streamer_recycler_view_teacher_item;
        } else {
            resId = R.layout.plvsa_streamer_recycler_view_multi_item;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);

        SurfaceView renderView = adapterCallback.createLinkMicRenderView();

        StreamerItemViewHolder viewHolder = new StreamerItemViewHolder(itemView);
        viewHolder.renderView = renderView;
        if (renderView != null) {
            viewHolder.plvsaStreamerRenderViewContainer.addView(renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            PLVCommonLog.e(TAG, "create render view return null");
        }
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return dataList.get(position).getLinkMicId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getLinkMicId().hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull final StreamerItemViewHolder holder, int position) {
        if (getItemType() == ITEM_TYPE_ONE_TO_MORE) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (position == hideItemIndex) {
                //主讲模式时，隐藏mainView对应的item
                if (layoutParams != null) {
                    layoutParams.width = 0;
                    layoutParams.height = 0;
                }
                if (holder.renderView != null) {
                    holder.isViewRecycled = true;
                    holder.plvsaStreamerRenderViewContainer.removeView(holder.renderView);
                    adapterCallback.releaseLinkMicRenderView(holder.renderView);
                    holder.renderView = null;
                }
                updateMainViewHolder();
                return;
            }
        }

        if (holder.isViewRecycled) {
            holder.isViewRecycled = false;
            holder.renderView = adapterCallback.createLinkMicRenderView();
            if (holder.renderView != null) {
                holder.plvsaStreamerRenderViewContainer.addView(holder.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                holder.isRenderViewSetup = false;
            } else {
                PLVCommonLog.d(TAG, String.format(Locale.US, "create render view return null at position:%d", position));
            }
        }
        PLVLinkMicItemDataBean itemDataBean = dataList.get(position);
        String linkMicId = itemDataBean.getLinkMicId();
        String nick = itemDataBean.getNick();
        boolean isMuteVideo = itemDataBean.isMuteVideo();
        boolean isMuteAudio = itemDataBean.isMuteAudio();
        int curVolume = itemDataBean.getCurVolume();
        boolean isTeacher = itemDataBean.isTeacher();
        boolean isGuest = itemDataBean.isGuest();
        boolean isScreenShare = itemDataBean.isScreenShare();
        boolean isMe = linkMicId != null && linkMicId.equals(myLinkMicId);
        String actor = itemDataBean.getActor();
        String pic = itemDataBean.getPic();

        if (linkMicId != null && linkMicId.equals(myLinkMicId)) {
            myselfViewHolder = holder;
            myselfDataBean = itemDataBean;
        }

        //触摸事件
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (itemType != ITEM_TYPE_ONLY_TEACHER && event.getAction() == MotionEvent.ACTION_DOWN) {
                    currentTouchItemViewHolder = holder;
                    isClickMainViewHolder = false;
                }
                return false;
            }
        });

        //如果当前用户是讲师，显示管理成员上麦成员的指引弹层
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(myLinkMicId)) {
            if (position == 1 && controlTipsLayout == null) {
                controlTipsLayout = new PLVSAStreamerMemberControlTipsLayout(holder.itemView.getContext());
                controlTipsLayout.open(holder.itemView);
            }
        }
        if (dataList.size() <= 1 && controlTipsLayout != null && controlTipsLayout.isShow()) {
            controlTipsLayout.close();
        }

        //隐藏自己的头像、昵称、麦克风的图标
        int userInfoViewVisibility = myLinkMicId.equals(linkMicId) ? View.GONE : View.VISIBLE;
        if (isGuest) {
            userInfoViewVisibility = View.VISIBLE;
        }
        if (holder.plvsaStreamerBottomLeftLy != null) {
            holder.plvsaStreamerBottomLeftLy.setVisibility(userInfoViewVisibility);
        }
        if (holder.plvsaStreamerMicStateIv != null) {
            holder.plvsaStreamerMicStateIv.setVisibility(userInfoViewVisibility);
        }

        //昵称
        StringBuilder nickString = new StringBuilder();
        if (!TextUtils.isEmpty(actor)) {
            nickString.append(actor).append("-");
        }
        nickString.append(nick);
        if (myLinkMicId.equals(linkMicId)) {
            nickString.append("（我）");
        }
        if (holder.plvsaStreamerNickTv != null) {
            holder.plvsaStreamerNickTv.setText(nickString.toString());
        }

        // 更新渲染视图层级 SurfaceView
        updateRenderViewLayer(holder.renderView, itemDataBean);

        if (!isAudioLinkMic) {
            //如果是视频连麦，则渲染所有用户类型
            if (holder.renderView != null && !holder.isRenderViewSetup) {
                adapterCallback.setupRenderView(holder.renderView, linkMicId);
                holder.isRenderViewSetup = true;
            }
        } else {
            //如果是音频连麦，则只渲染用户类型：讲师、嘉宾
            if (isTeacher || isGuest) {
                if (holder.renderView != null && !holder.isRenderViewSetup) {
                    adapterCallback.setupRenderView(holder.renderView, linkMicId);
                    holder.isRenderViewSetup = true;
                }
            } else {
                holder.plvsaStreamerRenderViewContainer.setVisibility(View.GONE);
                isMuteVideo = true;
            }
        }

        //是否关闭摄像头
        bindVideoMute(holder, isMuteVideo, linkMicId);

        //更新嘉宾连麦状态
        updateGuestViewStatus(holder, itemDataBean);

        //更新主讲权限状态
        updatePermissionChange(holder, itemDataBean);

        //更新屏幕共享占位图显示状态
        if (isMe) {
            holder.plvsaScreenSharePlaceholderView.setVisibility(isScreenShare ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StreamerItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        PLVLinkMicItemDataBean itemDataBean = dataList.get(position);
        String linkMicId = itemDataBean.getLinkMicId();
        boolean isMuteVideo = itemDataBean.isMuteVideo();
        boolean isMuteAudio = itemDataBean.isMuteAudio();
        int curVolume = itemDataBean.getCurVolume();
        boolean isTeacher = itemDataBean.isTeacher();
        boolean isGuest = itemDataBean.isGuest();
        boolean isScreenShare = itemDataBean.isScreenShare();
        boolean isMe = linkMicId != null && linkMicId.equals(myLinkMicId);

        //如果是音频连麦，则只渲染用户类型：讲师、嘉宾
        if (isAudioLinkMic && !isTeacher && !isGuest) {
            isMuteVideo = true;
        }

        for (Object payload : payloads) {
            switch (payload.toString()) {
                case PAYLOAD_UPDATE_VOLUME:
                    if (holder.plvsaStreamerMicStateIv == null) {
                        return;
                    }
                    //设置麦克风状态
                    if (isMuteAudio) {
                        holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_close);
                    } else {
                        if (intBetween(curVolume, 0, 5) || curVolume == 0) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_open);
                        } else if (intBetween(curVolume, 5, 15)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_10);
                        } else if (intBetween(curVolume, 15, 25)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_20);
                        } else if (intBetween(curVolume, 25, 35)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_30);
                        } else if (intBetween(curVolume, 35, 45)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_40);
                        } else if (intBetween(curVolume, 45, 55)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_50);
                        } else if (intBetween(curVolume, 55, 65)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_60);
                        } else if (intBetween(curVolume, 65, 75)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_70);
                        } else if (intBetween(curVolume, 75, 85)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_80);
                        } else if (intBetween(curVolume, 85, 95)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_90);
                        } else if (intBetween(curVolume, 95, 100)) {
                            holder.plvsaStreamerMicStateIv.setImageResource(R.drawable.plvsa_streamer_mic_volume_100);
                        }
                    }
                    break;
                case PAYLOAD_UPDATE_VIDEO_MUTE:
                    //是否关闭摄像头
                    if (itemType == ITEM_TYPE_ONE_TO_MORE && position == hideItemIndex) {
                        bindVideoMute(mainViewHolder, isMuteVideo, linkMicId);
                    } else {
                        bindVideoMute(holder, isMuteVideo, linkMicId);
                    }
                    break;
                case PAYLOAD_UPDATE_GUEST_STATUS:
                    updateGuestViewStatus(holder, itemDataBean);
                    break;
                case PAYLOAD_UPDATE_SCREEN_SHARED:
                    if (!isMe) {
                        break;
                    }
                    if (itemType == ITEM_TYPE_ONE_TO_MORE && position == hideItemIndex) {
                        mainViewHolder.plvsaScreenSharePlaceholderView.setVisibility(isScreenShare ? View.VISIBLE : View.INVISIBLE);
                    } else {
                        holder.plvsaScreenSharePlaceholderView.setVisibility(isScreenShare ? View.VISIBLE : View.INVISIBLE);
                    }
                    break;
                case PAYLOAD_UPDATE_PERMISSION_CHANGE:
                    if (itemType == ITEM_TYPE_ONE_TO_MORE && position == hideItemIndex) {
                        updatePermissionChange(mainViewHolder, itemDataBean);
                    } else {
                        updatePermissionChange(holder, itemDataBean);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull StreamerItemViewHolder holder) {
        super.onViewRecycled(holder);

        //标记view重新创建
        if (holder.renderView != null) {
            holder.isViewRecycled = true;
            holder.plvsaStreamerRenderViewContainer.removeView(holder.renderView);
            adapterCallback.releaseLinkMicRenderView(holder.renderView);
            holder.renderView = null;
        }
        PLVCommonLog.d(TAG, "onViewRecycled pos=" + holder.getAdapterPosition() + " holder=" + holder.toString());
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //设置是否是音频连麦
    public void setIsAudio(boolean isAudioLinkMic) {
        this.isAudioLinkMic = isAudioLinkMic;
    }

    //设置当前用户连麦id
    public void setMyLinkMicId(String myLinkMicId) {
        this.myLinkMicId = myLinkMicId;
    }

    //设置数据源
    public void setDataList(List<PLVLinkMicItemDataBean> dataList) {
        this.dataList = dataList;
    }

    //更新关闭视频
    public void updateUserMuteVideo(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_VIDEO_MUTE);
        checkUpdateControlWindow(pos);
    }

    //更新关闭音频
    public void updateUserMuteAudio(int pos) {
        updateVolumeChanged();
        checkUpdateControlWindow(pos);
    }

    //更新连麦列表的音量变化
    public void updateVolumeChanged() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_VOLUME);
    }

    //更新屏幕共享状态
    public void updateUserScreenSharing(int pos, boolean isShare) {
        notifyItemChanged(pos);
    }

    public void updatePermissionChange() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_PERMISSION_CHANGE);
    }

    //更新所有item
    public void updateAllItem() {
        notifyDataSetChanged();
        checkHideControlWindow();
    }

    //更新嘉宾状态
    public void updateGuestStatus(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_GUEST_STATUS);
    }

    //更新主讲模式主视图
    public void updateMainViewHolder() {
        if (itemType == ITEM_TYPE_ONE_TO_MORE && hideItemIndex < dataList.size()) {
            bindMainHolderView(mainViewHolder, hideItemIndex);
        }
    }

    public void setHasSpeakerUser(PLVSocketUserBean user) {
        if (memberControlLayout == null) {
            memberControlLayout = new PLVSAStreamerMemberControlLayout(streamerRv.getContext());
            memberControlLayout.init(liveRoomDataManager);
        }
        this.memberControlLayout.setHasSpeakerUser(user);
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getItemType() {
        return itemType;
    }

    public boolean onBackPressed() {
        return (controlTipsLayout != null && controlTipsLayout.onBackPressed())
                || (memberControlLayout != null && memberControlLayout.onBackPressed());
    }

    public void checkClickItemView(MotionEvent ev) {
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(streamerRv.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (currentTouchItemViewHolder != null) {
                        final StreamerItemViewHolder tempViewHolder = currentTouchItemViewHolder;
                        currentTouchItemViewHolder = null;
                        final int adapterIndex = isClickMainViewHolder ? hideItemIndex : tempViewHolder.getAdapterPosition();
                        if (adapterIndex < 0) {
                            return false;
                        }
                        final PLVLinkMicItemDataBean linkMicItemDataBean = dataList.get(adapterIndex);
                        if ((linkMicItemDataBean != null) && linkMicItemDataBean.isTeacher()
                                && PLVSocketUserConstant.USERTYPE_TEACHER.equals(liveRoomDataManager.getConfig().getUser().getViewerType())) {
                            //讲师不需要弹出悬浮窗
                            return false;
                        }
                        if (memberControlLayout == null) {
                            memberControlLayout = new PLVSAStreamerMemberControlLayout(streamerRv.getContext());
                            memberControlLayout.init(liveRoomDataManager);
                        }
                        memberControlLayout.setOnViewActionListener(new PLVSAStreamerMemberControlLayout.OnViewActionListener() {
                            @Override
                            public void onClickCamera(boolean isWillOpen) {
                                int pos = adapterIndex;
                                if (pos <= 0) {
                                    return;
                                }
                                if (adapterCallback != null) {
                                    adapterCallback.onCameraControl(pos, !isWillOpen);
                                }
                            }

                            @Override
                            public void onClickMic(boolean isWillOpen) {
                                int pos = adapterIndex;
                                if (pos <= 0) {
                                    return;
                                }
                                if (adapterCallback != null) {
                                    adapterCallback.onMicControl(pos, !isWillOpen);
                                }
                            }

                            @Override
                            public void onClickDownLinkMic() {
                                int pos = adapterIndex;
                                if (pos <= 0) {
                                    return;
                                }
                                if (adapterCallback != null) {
                                    adapterCallback.onControlUserLinkMic(pos, false);
                                }
                            }

                            @Override
                            public void onClickGrantSpeaker(boolean isGrant) {
                                if (adapterIndex < 0) {
                                    return;
                                }
                                if (adapterCallback != null && linkMicItemDataBean != null) {
                                    adapterCallback.onGrantUserSpeakerPermission(adapterIndex, linkMicItemDataBean, isGrant);
                                }
                            }

                            @Override
                            public void onClickFullScreen() {
                                int pos = adapterIndex;
                                if (pos < 0) {
                                    return;
                                }
                                if (adapterCallback != null) {
                                    adapterCallback.onControlFullScreen(pos, linkMicItemDataBean, tempViewHolder.switchViewAnchorLayout);
                                }
                            }
                        });
                        memberControlLayout.bindViewData(linkMicItemDataBean);
                        memberControlLayout.open();
                    }
                    return false;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (currentTouchItemViewHolder != null) {
                        final int pos = isClickMainViewHolder ? hideItemIndex : currentTouchItemViewHolder.getAdapterPosition();
                        if (pos < 0) {
                            return false;
                        }
                        final StreamerItemViewHolder tempViewHolder = currentTouchItemViewHolder;
                        currentTouchItemViewHolder = null;
                        final PLVLinkMicItemDataBean linkMicItemDataBean = dataList.get(pos);
                        if (adapterCallback != null) {
                            adapterCallback.onControlFullScreen(pos, linkMicItemDataBean, tempViewHolder.switchViewAnchorLayout);
                        }
                    }
                    return false;
                }
            });
        }
        gestureDetector.onTouchEvent(ev);
    }

    public void checkScaleCamera(final MotionEvent event) {
        if (myselfViewHolder == null || myselfViewHolder.renderView == null) {
            return;
        }
        final int[] location = new int[2];
        myselfViewHolder.renderView.getLocationOnScreen(location);
        final int x = (int) event.getRawX();
        final int y = (int) event.getRawY();
        if (x < location[0] || x > location[0] + myselfViewHolder.renderView.getWidth() || y < location[1] || y > location[1] + myselfViewHolder.renderView.getHeight()) {
            return;
        }

        if (scaleGestureDetector == null) {
            scaleGestureDetector = new ScaleGestureDetector(myselfViewHolder.renderView.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    final float scaleFactor = detector.getScaleFactor();
                    if (adapterCallback != null) {
                        adapterCallback.onStreamerViewScale(myselfDataBean, scaleFactor);
                    }
                    return true;
                }
            });
        }
        scaleGestureDetector.onTouchEvent(event);
    }

    public void setItemHide(int position) {
        this.hideItemIndex = position;
    }

    public int getHideItemIndex() {
        return hideItemIndex;
    }

    public StreamerItemViewHolder createMainViewHolder(ViewGroup parent) {
        if (mainViewHolder == null) {
            mainViewHolder = onCreateViewHolder(parent, ITEM_TYPE_ONE_TO_MORE);
        }
        mainViewHolder.isViewRecycled = true;
        return mainViewHolder;
    }

    /**
     * 释放ViewHolder，以及持有的render
     */
    public void releaseMainViewHolder() {
        if (mainViewHolder != null && mainViewHolder.renderView != null) {
            mainViewHolder.isViewRecycled = true;
            mainViewHolder.plvsaStreamerRenderViewContainer.removeView(mainViewHolder.renderView);
            adapterCallback.releaseLinkMicRenderView(mainViewHolder.renderView);
            mainViewHolder.renderView = null;
        }
    }

    public void callUserFullscreen(String userId, RecyclerView streamerRv) {
        for (int i = 0; i < dataList.size(); i++) {
            if (userId.equals(dataList.get(i).getUserId())) {
                streamerRv.scrollToPosition(i);

                final StreamerItemViewHolder holder = (StreamerItemViewHolder) streamerRv.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    if (adapterCallback != null) {
                        adapterCallback.onControlFullScreen(i, dataList.get(i), holder.switchViewAnchorLayout);
                    }
                }
                return;
            }
        }
    }

    public void clearFullscreenHolder(RecyclerView streamerRv, PLVLinkMicItemDataBean linkmicItem) {

        if (linkmicItem != null) {
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).getLinkMicId().equals(linkmicItem.getLinkMicId())) {
                    StreamerItemViewHolder holder = (StreamerItemViewHolder) streamerRv.findViewHolderForAdapterPosition(i);
                    if (holder != null && holder.isViewRecycled) {
//                        adapterCallback.releaseLinkMicRenderView(holder.renderView);
//                        holder.isViewRecycled = true;
                        notifyItemChanged(i);
                    }
                    return;
                }
            }
        }


    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表item绑定">
    private void bindVideoMute(@NonNull StreamerItemViewHolder holder, boolean isMuteVideo, String linkMicId) {
        //是否关闭摄像头
        if (holder.plvsaPlaceholderView != null) {
            if (isMuteVideo) {
                holder.plvsaPlaceholderView.setVisibility(View.VISIBLE);
            } else {
                holder.plvsaPlaceholderView.setVisibility(View.INVISIBLE);
            }
        }

    }

    //更新嘉宾状态
    private void updateGuestViewStatus(StreamerItemViewHolder holder, PLVLinkMicItemDataBean itemDataBean) {
        if (getItemType() == ITEM_TYPE_ONLY_TEACHER || holder.plvsaStreamerGuestLinkStatusTv == null) {
            return;
        }
        if (itemDataBean.isGuest() && myLinkMicId.equals(itemDataBean.getLinkMicId())) {
            holder.plvsaStreamerGuestLinkStatusTv.setVisibility(View.VISIBLE);
            if (PLVLinkMicItemDataBean.STATUS_RTC_JOIN.equals(itemDataBean.getStatus())) {
                holder.plvsaStreamerGuestLinkStatusTv.setText("连麦中");
                holder.plvsaStreamerGuestLinkStatusTv.setSelected(true);
            } else {
                holder.plvsaStreamerGuestLinkStatusTv.setText("未连麦");
                holder.plvsaStreamerGuestLinkStatusTv.setSelected(false);
            }
        } else {
            holder.plvsaStreamerGuestLinkStatusTv.setVisibility(View.GONE);
        }
    }

    private void updatePermissionChange(StreamerItemViewHolder holder, PLVLinkMicItemDataBean bean) {
        if (holder.plvsaStreamerGrantSpeakerIv == null) {
            return;
        }
        if (bean.isTeacher()) {
            holder.plvsaStreamerGrantSpeakerIv.setVisibility(View.INVISIBLE);
        } else {
            holder.plvsaStreamerGrantSpeakerIv.setVisibility(
                    bean.isHasSpeaker() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateRenderViewLayer(View renderView, PLVLinkMicItemDataBean bean) {
        if (renderView instanceof SurfaceView) {
            ((SurfaceView) renderView).setZOrderMediaOverlay(bean.isFullScreen());
        }
    }

    /**
     * 用来绑定数据源和主讲模式第一画面ViewHolder，相当于onBindViewHolder
     *
     * @param holder   主讲模式下的第一画面的 ViewHolder
     * @param position 需要与adapter数据源绑定的position
     */
    private void bindMainHolderView(final StreamerItemViewHolder holder, final int position) {
        if (holder == null) {
            return;
        }
        if (holder.isViewRecycled) {
            holder.isViewRecycled = false;
            holder.renderView = adapterCallback.createLinkMicRenderView();
            if (holder.renderView != null) {
                holder.plvsaStreamerRenderViewContainer.addView(holder.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                holder.isRenderViewSetup = false;
            } else {
                PLVCommonLog.d(TAG, String.format(Locale.US, "create render view return null at position:%d", position));
            }
        }

        if (holder.plvsaStreamerBottomLeftLy != null) {
            holder.plvsaStreamerBottomLeftLy.setVisibility(View.GONE);
        }
        PLVLinkMicItemDataBean itemDataBean = dataList.get(position);
        String linkMicId = itemDataBean.getLinkMicId();
        String nick = itemDataBean.getNick();
        boolean isMuteVideo = itemDataBean.isMuteVideo();
        boolean isMuteAudio = itemDataBean.isMuteAudio();
        int curVolume = itemDataBean.getCurVolume();
        boolean isTeacher = itemDataBean.isTeacher();
        boolean isGuest = itemDataBean.isGuest();
        boolean isScreenShare = itemDataBean.isScreenShare();
        boolean isMe = linkMicId != null && linkMicId.equals(myLinkMicId);
        String actor = itemDataBean.getActor();
        String pic = itemDataBean.getPic();

        if (linkMicId != null && linkMicId.equals(myLinkMicId)) {
            myselfViewHolder = holder;
            myselfDataBean = itemDataBean;
        }

        //触摸事件
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (itemType != ITEM_TYPE_ONLY_TEACHER && event.getAction() == MotionEvent.ACTION_DOWN) {
                    currentTouchItemViewHolder = holder;
                    isClickMainViewHolder = true;
                }
                return false;
            }
        });


        if (!isAudioLinkMic) {
            //如果是视频连麦，则渲染所有用户类型
            if (holder.renderView != null && !holder.isRenderViewSetup) {
                adapterCallback.setupRenderView(holder.renderView, linkMicId);
                holder.isRenderViewSetup = true;
            }
        } else {
            //如果是音频连麦，则只渲染用户类型：讲师、嘉宾
            if (isTeacher || isGuest) {
                if (holder.renderView != null && !holder.isRenderViewSetup) {
                    adapterCallback.setupRenderView(holder.renderView, linkMicId);
                    holder.isRenderViewSetup = true;
                }
            } else {
                holder.plvsaStreamerRenderViewContainer.setVisibility(View.GONE);
                isMuteVideo = true;
            }
        }

        //是否关闭摄像头
        bindVideoMute(holder, isMuteVideo, linkMicId);

        //更新嘉宾连麦状态
        updateGuestViewStatus(holder, itemDataBean);

        //更新主讲权限状态
        updatePermissionChange(holder, itemDataBean);

        //更新屏幕共享占位图显示状态
        holder.plvsaScreenSharePlaceholderView.setVisibility(isMe && isScreenShare ? View.VISIBLE : View.INVISIBLE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更新成员控制弹层">
    private void checkHideControlWindow() {
        if (memberControlLayout != null && memberControlLayout.isOpen()) {
            for (int i = 0; i < dataList.size(); i++) {
                PLVLinkMicItemDataBean linkMicItemDataBean = dataList.get(i);
                if (linkMicItemDataBean.getLinkMicId() != null && linkMicItemDataBean.getLinkMicId().equals(memberControlLayout.getLinkMicUid())) {
                    return;
                }
            }
            memberControlLayout.close();
        }
    }

    private void checkUpdateControlWindow(int pos) {
        if (memberControlLayout != null && memberControlLayout.isOpen()) {
            PLVLinkMicItemDataBean linkMicItemDataBean = dataList.get(pos);
            if (linkMicItemDataBean.getLinkMicId() != null && linkMicItemDataBean.getLinkMicId().equals(memberControlLayout.getLinkMicUid())) {
                memberControlLayout.bindViewData(linkMicItemDataBean);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    //判断value是否在左开右闭区间：(left, right]
    private boolean intBetween(int value, int left, int right) {
        return value > left && value <= right;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - StreamerItemViewHolder定义">
    static class StreamerItemViewHolder extends RecyclerView.ViewHolder {
        private PLVSwitchViewAnchorLayout switchViewAnchorLayout;
        @Nullable
        private PLVRoundRectLayout plvsaStreamerRoundRectLy;
        private FrameLayout plvsaStreamerRenderViewContainer;
        @Nullable
        private ImageView plvsaStreamerMicStateIv;
        @Nullable
        private TextView plvsaStreamerNickTv;
        @Nullable
        private ViewGroup plvsaStreamerBottomLeftLy;
        @Nullable
        private ImageView plvsaStreamerAvatarIv;
        @Nullable
        private TextView plvsaStreamerGuestLinkStatusTv;
        @Nullable
        private SurfaceView renderView;
        //关闭摄像头或者没有流时候的占位图
        private View plvsaPlaceholderView;
        //屏幕分享时的占位图
        private View plvsaScreenSharePlaceholderView;
        @Nullable
        private ImageView plvsaStreamerGrantSpeakerIv;
        //是否被回收过（渲染器如果被回收过，则下一次复用的时候，必须重新渲染器）
        private boolean isViewRecycled = false;

        private boolean isRenderViewSetup = false;

        public StreamerItemViewHolder(final View itemView) {
            super(itemView);
            switchViewAnchorLayout = itemView.findViewById(R.id.plvsa_streamer_anchor_view);
            plvsaStreamerRoundRectLy = itemView.findViewById(R.id.plvsa_streamer_round_rect_ly);
            plvsaStreamerRenderViewContainer = itemView.findViewById(R.id.plvsa_streamer_render_view_container);
            plvsaStreamerMicStateIv = itemView.findViewById(R.id.plvsa_streamer_mic_state_iv);
            plvsaStreamerNickTv = itemView.findViewById(R.id.plvsa_streamer_nick_tv);
            plvsaStreamerBottomLeftLy = itemView.findViewById(R.id.plvsa_streamer_bottom_left_ly);
            plvsaStreamerAvatarIv = itemView.findViewById(R.id.plvsa_streamer_avatar_iv);
            plvsaStreamerGuestLinkStatusTv = itemView.findViewById(R.id.plvsa_streamer_guest_link_status_tv);
            plvsaPlaceholderView = itemView.findViewById(R.id.plvsa_no_streamer_placeholder);
            plvsaScreenSharePlaceholderView = itemView.findViewById(R.id.plvsa_streamer_screen_share_placeholder);
            plvsaStreamerGrantSpeakerIv = itemView.findViewById(R.id.plvsa_streamer_grant_speaker_iv);
            if (plvsaStreamerRoundRectLy != null) {
                plvsaStreamerRoundRectLy.setOnOrientationChangedListener(new PLVRoundRectLayout.OnOrientationChangedListener() {
                    @Override
                    public void onChanged(boolean isPortrait) {
                        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) plvsaStreamerRoundRectLy.getLayoutParams();
                        if (isPortrait) {
                            lp.dimensionRatio = "H,2:3";
                        } else {
                            lp.dimensionRatio = "H,3:2";

                        }
                        plvsaStreamerRoundRectLy.requestLayout();
                    }
                });
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 监听器定义">
    public interface OnStreamerAdapterCallback {
        /**
         * 创建连麦列表渲染器。
         * 该渲染器必须通过云课堂连麦SDK创建，不能直接构造。
         *
         * @return 渲染器
         */
        SurfaceView createLinkMicRenderView();

        /**
         * 释放渲染器
         *
         * @param renderView 渲染器
         */
        void releaseLinkMicRenderView(SurfaceView renderView);

        /**
         * 安装SurfaceView。
         * 将创建好的SurfaceView与连麦ID关联，并设置到SDK
         *
         * @param surfaceView 渲染器
         * @param linkMicId   连麦ID
         */
        void setupRenderView(SurfaceView surfaceView, String linkMicId);

        /**
         * 麦克风控制
         */
        void onMicControl(int position, boolean isMute);

        /**
         * 摄像机控制
         */
        void onCameraControl(int position, boolean isMute);

        /**
         * 用户加入或离开连麦控制
         */
        void onControlUserLinkMic(int position, boolean isAllowJoin);

        /**
         * 授予用户主讲权限
         */
        void onGrantUserSpeakerPermission(int position, PLVLinkMicItemDataBean user, boolean isGrant);

        /**
         * 控制全屏
         *
         * @param position
         * @param itemDataBean
         * @param view
         */
        void onControlFullScreen(int position, PLVLinkMicItemDataBean itemDataBean, PLVSwitchViewAnchorLayout view);

        /**
         * 推流画面缩放
         */
        void onStreamerViewScale(PLVLinkMicItemDataBean itemDataBean, float scaleFactor);
    }
    // </editor-fold>
}
