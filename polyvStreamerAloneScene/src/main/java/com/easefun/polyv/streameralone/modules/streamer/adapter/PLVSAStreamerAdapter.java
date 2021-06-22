package com.easefun.polyv.streameralone.modules.streamer.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.streamer.PLVSAStreamerMemberControlLayout;
import com.easefun.polyv.streameralone.modules.streamer.PLVSAStreamerMemberControlTipsLayout;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.List;
import java.util.Locale;

/**
 * 推流和连麦列表适配器
 */
public class PLVSAStreamerAdapter extends RecyclerView.Adapter<PLVSAStreamerAdapter.StreamerItemViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVLSStreamerAdapter";
    public static final int ITEM_TYPE_DEFAULT = -1;
    public static final int ITEM_TYPE_ONLY_TEACHER = 1;
    public static final int ITEM_TYPE_ONE_TO_ONE = 2;

    public static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    public static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";

    /**** data ****/
    private List<PLVLinkMicItemDataBean> dataList;
    /**** listener ****/
    private OnStreamerAdapterCallback adapterCallback;

    //是否是音频连麦
    private boolean isAudioLinkMic;
    //当前用户的连麦ID
    private String myLinkMicId;

    //rv
    private RecyclerView streamerRv;
    @Nullable
    private SurfaceView localRenderView;
    //window
    private PLVSAStreamerMemberControlTipsLayout controlTipsLayout;
    private PLVSAStreamerMemberControlLayout memberControlLayout;
    //curTouchItem
    private StreamerItemViewHolder currentTouchItemViewHolder;
    //gestureDetector
    private GestureDetector gestureDetector;

    private int itemType = ITEM_TYPE_DEFAULT;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStreamerAdapter(RecyclerView streamerRv, OnStreamerAdapterCallback adapterCallback) {
        this.streamerRv = streamerRv;
        this.adapterCallback = adapterCallback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public StreamerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resId = R.layout.plvsa_streamer_recycler_view_teacher_item;
        if (itemType == ITEM_TYPE_ONLY_TEACHER) {
            resId = R.layout.plvsa_streamer_recycler_view_teacher_item;
        } else if (itemType == ITEM_TYPE_ONE_TO_ONE) {
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
        String actor = itemDataBean.getActor();
        String pic = itemDataBean.getPic();

        //触摸事件
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (holder.getAdapterPosition() > 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
                    currentTouchItemViewHolder = holder;
                }
                return false;
            }
        });

        //显示管理成员上麦成员的指引弹层
        if (position == 1 && controlTipsLayout == null) {
            controlTipsLayout = new PLVSAStreamerMemberControlTipsLayout(holder.itemView.getContext());
            controlTipsLayout.open(holder.itemView);
        }
        if (dataList.size() <= 1 && controlTipsLayout != null && controlTipsLayout.isShow()) {
            controlTipsLayout.close();
        }

        //隐藏自己的头像、昵称、麦克风的图标
        int userInfoViewVisibility = myLinkMicId.equals(linkMicId) ? View.GONE : View.VISIBLE;
        if (holder.plvsaStreamerBottomLeftLy != null) {
            holder.plvsaStreamerBottomLeftLy.setVisibility(userInfoViewVisibility);
        }
        if (holder.plvsaStreamerMicStateIv != null) {
            holder.plvsaStreamerMicStateIv.setVisibility(userInfoViewVisibility);
        }

        //头像
        if (holder.plvsaStreamerAvatarIv != null) {
            PLVImageLoader.getInstance().loadImageNoDiskCache(
                    holder.itemView.getContext(),
                    pic,
                    R.drawable.plvsa_member_student_missing_face,
                    R.drawable.plvsa_member_student_missing_face,
                    holder.plvsaStreamerAvatarIv
            );
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
        //设置本地渲染器
        if (myLinkMicId.equals(linkMicId)) {
            localRenderView = holder.renderView;
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
                    bindVideoMute(holder, isMuteVideo, linkMicId);
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
        //如果是本地渲染器，那么也不要销毁，因为滑动列表的时候还是要保持一个本地摄像头推流的
        if (holder.renderView != null && holder.renderView != localRenderView) {
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

    //更新所有item
    public void updateAllItem() {
        notifyDataSetChanged();
        checkHideControlWindow();
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
                    if (currentTouchItemViewHolder != null && currentTouchItemViewHolder.getAdapterPosition() > 0) {
                        final StreamerItemViewHolder tempViewHolder = currentTouchItemViewHolder;
                        currentTouchItemViewHolder = null;
                        PLVLinkMicItemDataBean linkMicItemDataBean = dataList.get(tempViewHolder.getAdapterPosition());
                        if (memberControlLayout == null) {
                            memberControlLayout = new PLVSAStreamerMemberControlLayout(streamerRv.getContext());
                        }
                        memberControlLayout.setOnViewActionListener(new PLVSAStreamerMemberControlLayout.OnViewActionListener() {
                            @Override
                            public void onClickCamera(boolean isWillOpen) {
                                int pos = tempViewHolder.getAdapterPosition();
                                if (pos <= 0) {
                                    return;
                                }
                                if (adapterCallback != null) {
                                    adapterCallback.onCameraControl(pos, !isWillOpen);
                                }
                            }

                            @Override
                            public void onClickMic(boolean isWillOpen) {
                                int pos = tempViewHolder.getAdapterPosition();
                                if (pos <= 0) {
                                    return;
                                }
                                if (adapterCallback != null) {
                                    adapterCallback.onMicControl(pos, !isWillOpen);
                                }
                            }

                            @Override
                            public void onClickDownLinkMic() {
                                int pos = tempViewHolder.getAdapterPosition();
                                if (pos <= 0) {
                                    return;
                                }
                                if (adapterCallback != null) {
                                    adapterCallback.onControlUserLinkMic(pos, false);
                                }
                            }
                        });
                        memberControlLayout.bindViewData(linkMicItemDataBean);
                        memberControlLayout.open();
                    }
                    return false;
                }
            });
        }
        gestureDetector.onTouchEvent(ev);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表item绑定">
    private void bindVideoMute(@NonNull StreamerItemViewHolder holder, boolean isMuteVideo, String linkMicId) {
        //是否关闭摄像头
        if (isMuteVideo) {
            detachRenderViewInvisible(holder);
        } else {
            attachRenderViewVisible(holder);
        }
    }

    private void detachRenderViewInvisible(@NonNull StreamerItemViewHolder holder) {
        holder.plvsaStreamerRenderViewContainer.setVisibility(View.INVISIBLE);
        //一并改变渲染器的可见性
        if (holder.renderView != null) {
            holder.renderView.setVisibility(View.INVISIBLE);
        }
        //将渲染器从View tree中移除（在部分华为机型上发现渲染器的SurfaceView隐藏后还会叠加显示）
        if (holder.renderView != null && holder.renderView.getParent() != null) {
            holder.plvsaStreamerRenderViewContainer.removeView(holder.renderView);
        }
    }

    private void attachRenderViewVisible(@NonNull StreamerItemViewHolder holder) {
        holder.plvsaStreamerRenderViewContainer.setVisibility(View.VISIBLE);
        //一并改变渲染器的可见性
        if (holder.renderView != null) {
            holder.renderView.setVisibility(View.VISIBLE);
        }
        //将渲染器从View 添加到view tree中
        if (holder.renderView != null && holder.renderView.getParent() == null) {
            holder.plvsaStreamerRenderViewContainer.addView(holder.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
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
        private SurfaceView renderView;
        private PLVRoundRectLayout roundRectLayout;
        //是否被回收过（渲染器如果被回收过，则下一次复用的时候，必须重新渲染器）
        private boolean isViewRecycled = false;

        private boolean isRenderViewSetup = false;

        public StreamerItemViewHolder(View itemView) {
            super(itemView);
            plvsaStreamerRoundRectLy = itemView.findViewById(R.id.plvsa_streamer_round_rect_ly);
            plvsaStreamerRenderViewContainer = itemView.findViewById(R.id.plvsa_streamer_render_view_container);
            plvsaStreamerMicStateIv = itemView.findViewById(R.id.plvsa_streamer_mic_state_iv);
            plvsaStreamerNickTv = itemView.findViewById(R.id.plvsa_streamer_nick_tv);
            plvsaStreamerBottomLeftLy = itemView.findViewById(R.id.plvsa_streamer_bottom_left_ly);
            plvsaStreamerAvatarIv = itemView.findViewById(R.id.plvsa_streamer_avatar_iv);
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
    }
    // </editor-fold>
}
