package com.easefun.polyv.livestreamer.modules.streamer.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.List;
import java.util.Locale;

/**
 * 推流和连麦列表适配器
 */
public class PLVLSStreamerAdapter extends RecyclerView.Adapter<PLVLSStreamerAdapter.StreamerItemViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVLSStreamerAdapter";

    private static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    private static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";
    private static final String PAYLOAD_UPDATE_GUEST_STATUS = "updateGuestStatus";
    public static final String PAYLOAD_UPDATE_COVER_IMAGE = "updateCoverImage";

    //默认的直播间封面图
    private static final String DEFAULT_LIVE_STREAM_COVER_IMAGE = "https://s1.videocc.net/default-img/channel/default-splash.png";


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

    //音频开播模式，只允许音频开播与连麦
    private boolean isOnlyAudio = false;
    //封面图
    private String coverImage = DEFAULT_LIVE_STREAM_COVER_IMAGE;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSStreamerAdapter(RecyclerView streamerRv, OnStreamerAdapterCallback adapterCallback) {
        this.streamerRv = streamerRv;
        this.adapterCallback = adapterCallback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public StreamerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvls_streamer_recycler_view_item, parent, false);

        SurfaceView renderView = adapterCallback.createLinkMicRenderView();

        StreamerItemViewHolder viewHolder = new StreamerItemViewHolder(itemView);
        viewHolder.renderView = renderView;
        if (renderView != null) {
            viewHolder.plvlsStreamerRenderViewContainer.addView(renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
    public void onBindViewHolder(@NonNull StreamerItemViewHolder holder, int position) {
        if (holder.isViewRecycled) {
            holder.isViewRecycled = false;
            holder.renderView = adapterCallback.createLinkMicRenderView();
            if (holder.renderView != null) {
                holder.plvlsStreamerRenderViewContainer.addView(holder.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
        String status = itemDataBean.getStatus();

        //昵称
        StringBuilder nickString = new StringBuilder();
        if (!TextUtils.isEmpty(actor)) {
            nickString.append(actor).append("-");
        }
        nickString.append(nick);
        if (myLinkMicId.equals(linkMicId)) {
            nickString.append("（我）");
        }
        holder.plvlsStreamerNickTv.setText(nickString.toString());

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
                holder.plvlsStreamerRenderViewContainer.setVisibility(View.GONE);
                isMuteVideo = true;
            }
        }

        bindCoverImage(holder, isOnlyAudio, isTeacher);

        //是否关闭摄像头
        bindVideoMute(holder, isMuteVideo, linkMicId);

        //设置本地渲染器
        if (myLinkMicId.equals(linkMicId)) {
            localRenderView = holder.renderView;
        }

        updateGuestViewStatus(holder, itemDataBean);

        //设置点击事件监听器
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nothing
            }
        });
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
        String status = itemDataBean.getStatus();

        //如果是音频连麦，则只渲染用户类型：讲师、嘉宾
        if (isAudioLinkMic && !isTeacher && !isGuest) {
            isMuteVideo = true;
        }

        for (Object payload : payloads) {
            switch (payload.toString()) {
                case PAYLOAD_UPDATE_VOLUME:
                    //设置麦克风状态
                    if (isMuteAudio) {
                        holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_close);
                    } else {
                        if (intBetween(curVolume, 0, 5) || curVolume == 0) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_open);
                        } else if (intBetween(curVolume, 5, 15)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_10);
                        } else if (intBetween(curVolume, 15, 25)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_20);
                        } else if (intBetween(curVolume, 25, 35)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_30);
                        } else if (intBetween(curVolume, 35, 45)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_40);
                        } else if (intBetween(curVolume, 45, 55)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_50);
                        } else if (intBetween(curVolume, 55, 65)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_60);
                        } else if (intBetween(curVolume, 65, 75)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_70);
                        } else if (intBetween(curVolume, 75, 85)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_80);
                        } else if (intBetween(curVolume, 85, 95)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_90);
                        } else if (intBetween(curVolume, 95, 100)) {
                            holder.plvlsStreamerMicStateIv.setImageResource(R.drawable.plvls_streamer_mic_volume_100);
                        }
                    }
                    break;
                case PAYLOAD_UPDATE_VIDEO_MUTE:
                    //是否关闭摄像头
                    bindVideoMute(holder, isMuteVideo, linkMicId);
                    break;
                case PAYLOAD_UPDATE_GUEST_STATUS:
                    updateGuestViewStatus(holder, itemDataBean);
                    break;
                case PAYLOAD_UPDATE_COVER_IMAGE:
                    bindCoverImage(holder, isOnlyAudio, isTeacher);
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
            holder.plvlsStreamerRenderViewContainer.removeView(holder.renderView);
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
    }

    //更新连麦列表的音量变化
    public void updateVolumeChanged() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_VOLUME);
    }

    //更新嘉宾状态
    public void updateGuestStatus(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_GUEST_STATUS);
    }

    //更新所有item
    public void updateAllItem() {
        notifyDataSetChanged();
    }

    /**
     * 设置是否是音频开播。
     * @param isOnlyAudio
     */
    public void setIsOnlyAudio(boolean isOnlyAudio) {
        this.isOnlyAudio = isOnlyAudio;
    }

    /**
     * 设置封面图
     * 仅在音频开播模式下({@link #isOnlyAudio = true})，将该封面图设置到讲师摄像头占位
     */
    public void updateCoverImage(String coverImage){
        if(TextUtils.isEmpty(coverImage)){
            coverImage = DEFAULT_LIVE_STREAM_COVER_IMAGE;
        } else if(coverImage.startsWith("//")){
            coverImage = "https:"+coverImage;
        }
        this.coverImage = coverImage;
        notifyItemChanged(0, PAYLOAD_UPDATE_COVER_IMAGE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表item绑定">
    private void bindVideoMute(@NonNull StreamerItemViewHolder holder, boolean isMuteVideo, String linkMicId) {
        //是否关闭摄像头
        if (isMuteVideo) {
            holder.plvlsStreamerRenderViewContainer.setVisibility(View.INVISIBLE);
            //一并改变渲染器的可见性
            if (holder.renderView != null) {
                holder.renderView.setVisibility(View.INVISIBLE);
            }
            //将渲染器从View tree中移除（在部分华为机型上发现渲染器的SurfaceView隐藏后还会叠加显示）
            if (holder.renderView != null && holder.renderView.getParent() != null) {
                holder.plvlsStreamerRenderViewContainer.removeView(holder.renderView);
            }
        } else {
            holder.plvlsStreamerRenderViewContainer.setVisibility(View.VISIBLE);
            //一并改变渲染器的可见性
            if (holder.renderView != null) {
                holder.renderView.setVisibility(View.VISIBLE);
            }
            //将渲染器从View 添加到view tree中
            if (holder.renderView != null && holder.renderView.getParent() == null) {
                holder.plvlsStreamerRenderViewContainer.addView(holder.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
    }

    //更新嘉宾状态
    private void updateGuestViewStatus(StreamerItemViewHolder holder, PLVLinkMicItemDataBean itemDataBean) {
        if (itemDataBean.isGuest()&&myLinkMicId.equals(itemDataBean.getLinkMicId())) {
            holder.plvsStreamerGuestLinkStatusTv.setVisibility(View.VISIBLE);
            if (PLVLinkMicItemDataBean.STATUS_RTC_JOIN.equals(itemDataBean.getStatus())) {
                holder.plvsStreamerGuestLinkStatusTv.setText("连麦中");
                holder.plvsStreamerGuestLinkStatusTv.setSelected(true);
            } else {
                holder.plvsStreamerGuestLinkStatusTv.setText("未连麦");
                holder.plvsStreamerGuestLinkStatusTv.setSelected(false);
            }
        } else {
            holder.plvsStreamerGuestLinkStatusTv.setVisibility(View.GONE);
        }
    }

    private void bindCoverImage(@NonNull StreamerItemViewHolder holder, boolean onlyAudio, boolean isTeacher){
        //如果是音频开播（音频连麦），则将讲师的占位图改为封面图
        if(onlyAudio && isTeacher){
            holder.plvlsStreamerCoverImage.setVisibility(View.VISIBLE);
            PLVImageLoader.getInstance().loadImage(coverImage, holder.plvlsStreamerCoverImage);
        } else {
            holder.plvlsStreamerCoverImage.setVisibility(View.INVISIBLE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    //判断value是否在左开右闭区间：(left, right]
    private boolean intBetween(int value, int left, int right) {
        return value > left && value <= right;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - LinkMicItemViewHolder定义">
    static class StreamerItemViewHolder extends RecyclerView.ViewHolder {
        private PLVRoundRectLayout plvlsStreamerRoundRectLy;
        private FrameLayout plvlsStreamerRenderViewContainer;
        private ImageView plvlsStreamerMicStateIv;
        private TextView plvlsStreamerNickTv;
        private ImageView plvlsStreamerCoverImage;
        @Nullable
        private SurfaceView renderView;
        private PLVRoundRectLayout roundRectLayout;
        private TextView plvsStreamerGuestLinkStatusTv;
        //是否被回收过（渲染器如果被回收过，则下一次复用的时候，必须重新渲染器）
        private boolean isViewRecycled = false;

        private boolean isRenderViewSetup = false;

        public StreamerItemViewHolder(View itemView) {
            super(itemView);
            plvlsStreamerRoundRectLy = itemView.findViewById(R.id.plvls_streamer_round_rect_ly);
            plvlsStreamerRenderViewContainer = itemView.findViewById(R.id.plvls_streamer_render_view_container);
            plvlsStreamerMicStateIv = itemView.findViewById(R.id.plvls_streamer_mic_state_iv);
            plvlsStreamerNickTv = itemView.findViewById(R.id.plvls_streamer_nick_tv);
            plvsStreamerGuestLinkStatusTv = itemView.findViewById(R.id.plvls_streamer_guest_link_status_tv);
            plvlsStreamerCoverImage = itemView.findViewById(R.id.plvls_streamer_cover_image);

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
    }
    // </editor-fold>
}
