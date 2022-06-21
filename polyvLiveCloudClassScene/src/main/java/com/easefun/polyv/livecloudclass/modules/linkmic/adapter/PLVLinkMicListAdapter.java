package com.easefun.polyv.livecloudclass.modules.linkmic.adapter;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVLSNetworkQualityWidget;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * date: 2020/7/27
 * author: hwj
 * description: 连麦列表适配器
 */
public class PLVLinkMicListAdapter extends RecyclerView.Adapter<PLVLinkMicListAdapter.LinkMicItemViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLinkMicListAdapter.class.getSimpleName();
    public static final int HORIZONTAL_VISIBLE_COUNT = 3;

    /**** payload to bind data into view ****/
    private static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    private static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";
    private static final String PAYLOAD_UPDATE_CUP = "updateCup";
    private static final String PAYLOAD_UPDATE_NET_QUALITY = "updateNetQuality";
    private static final String PAYLOAD_UPDATE_COVER_IMAGE = "updateCoverImage";

    //默认的封面图
    private static final String DEFAULT_LIVE_STREAM_COVER_IMAGE = "https://s1.videocc.net/default-img/channel/default-splash.png";


    /**** data ****/
    private List<PLVLinkMicItemDataBean> dataList;
    private Map<String, Bitmap> linkMicIdSnapshotBitmapMap = new HashMap<>();

    /**** listener ****/
    @NotNull
    private OnPLVLinkMicAdapterCallback adapterCallback;
    @Nullable
    private OnTeacherSwitchViewBindListener onTeacherSwitchViewBindListener;

    /**** status ****/
    //是否是音频连麦
    private boolean isAudio;
    //当前用户的连麦ID
    private String myLinkMicId;
    //第一画面用户连麦ID
    private String firstScreenLinkMicId = "";
    //item是否显示圆角，横屏显示圆角，竖屏不显示圆角
    private boolean showRoundRect = false;
    //是否要隐藏所有渲染器
    private boolean shouldHideAllRenderView = false;
    //在列表中不可见item的连麦Id
    private String invisibleItemLinkMicId;
    //media在连麦列表中对应item的连麦id
    private String mediaInLinkMicListLinkMicId;
    //本地网络质量
    private int netQuality;
    //列表显示模式
    private PLVLinkMicListShowMode listShowMode = PLVLinkMicListShowMode.SHOW_ALL;
    //是否已经回调了绑定老师ViewHolder
    private boolean hasNotifyTeacherViewHolderBind = false;
    //封面图
    private String coverImage = DEFAULT_LIVE_STREAM_COVER_IMAGE;
    //水印logo
    private PLVPlayerLogoView plvPlayerLogoView;
    //是否是仅音频模式
    private boolean isOnlyAudio = false;
    // 是否正在暂停
    private volatile boolean isPausing = false;

    /**** View ****/
    //保存ppt(三分屏)/video(纯视频不支持RTC)/renderView(纯视频支持RTC)的switch View，不为null时，内部保存的是ppt/video/renderView，为null时，表示ppt/video/renderView不在连麦列表，或者调用了[releaseView]方法释放了引用。
    @Nullable
    private PLVSwitchViewAnchorLayout switchViewHasMedia;
    private final RecyclerView rv;
    private LinearLayoutManager linearLayoutManager;
    @Nullable
    private LinkMicItemViewHolder teacherViewHolder;
    @Nullable
    private SurfaceView localRenderView;

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLinkMicListAdapter(RecyclerView rv, LinearLayoutManager linearLayoutManager, @NotNull OnPLVLinkMicAdapterCallback adapterCallback) {
        this.rv = rv;
        this.linearLayoutManager = linearLayoutManager;
        this.adapterCallback = adapterCallback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - set & get">
    //设置数据源
    public void setDataList(List<PLVLinkMicItemDataBean> dataList) {
        this.dataList = dataList;
    }


    //设置item是否显示圆角
    public void setShowRoundRect(boolean showRoundRect) {
        this.showRoundRect = showRoundRect;
        notifyDataSetChanged();
    }

    //设置当前用户连麦id
    public void setMyLinkMicId(String myLinkMicId) {
        this.myLinkMicId = myLinkMicId;
    }

    //设置讲师ViewHolder绑定监听器
    public void setTeacherViewHolderBindListener(@Nullable OnTeacherSwitchViewBindListener teacherViewHolderBindListener) {
        this.onTeacherSwitchViewBindListener = teacherViewHolderBindListener;
    }

    //获取Item的宽度
    public int getItemWidth() {
        //item宽度为屏幕/HORIZONTAL_VISIBLE_COUNT
        int screenWidth = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        return screenWidth / HORIZONTAL_VISIBLE_COUNT;
    }

    //设置不可见的连麦item Id
    public void setInvisibleItemLinkMicId(String invisibleItemLinkMicId) {
        this.invisibleItemLinkMicId = invisibleItemLinkMicId;
    }

    //设置rv布局管理器
    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    //设置封面图
    public void setCoverImage(String coverImage) {
        if(TextUtils.isEmpty(coverImage)){
            coverImage = DEFAULT_LIVE_STREAM_COVER_IMAGE;
        } else if(coverImage.startsWith("//")){
            coverImage = "https:" + coverImage;
        }
        this.coverImage = coverImage;
    }

    //设置音频开播模式下的音频连麦
    public void setOnlyAudio(boolean onlyAudio) {
        isOnlyAudio = onlyAudio;
    }

    public void setHasNotifyTeacherViewHolderBind(boolean hasNotifyTeacherViewHolderBind) {
        this.hasNotifyTeacherViewHolderBind = hasNotifyTeacherViewHolderBind;
    }

    //设置播放器水印logo
    public void setPlvPlayerLogoView(PLVPlayerLogoView plvPlayerLogoView) {
        this.plvPlayerLogoView = plvPlayerLogoView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 第一画面">
    //获取第一画面的切换View
    public PLVSwitchViewAnchorLayout getFirstScreenSwitchView() {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getLinkMicId().equals(firstScreenLinkMicId)) {
                LinkMicItemViewHolder viewHolder = (LinkMicItemViewHolder) rv.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    return viewHolder.switchViewAnchorLayout;
                }
            }
        }
        return null;
    }

    //获取指定索引的switchView
    public PLVSwitchViewAnchorLayout getSwitchView(int index) {
        LinkMicItemViewHolder viewHolder = (LinkMicItemViewHolder) rv.findViewHolderForAdapterPosition(index);
        if (viewHolder != null) {
            return viewHolder.switchViewAnchorLayout;
        }
        return null;
    }

    //设置第一画面连麦Id
    public void setFirstScreenLinkMicId(String firstScreenLinkMicId) {
        this.firstScreenLinkMicId = firstScreenLinkMicId;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - media">

    //设置有media的switchView
    public void setSwitchViewHasMedia(@Nullable PLVSwitchViewAnchorLayout switchViewHasMedia) {
        this.switchViewHasMedia = switchViewHasMedia;
    }

    //设置media在连麦列表中对应item的连麦id
    public void setMediaInLinkMicListLinkMicId(String mediaInLinkMicListLinkMicId) {
        this.mediaInLinkMicListLinkMicId = mediaInLinkMicListLinkMicId;
    }

    //获取含有media的switchview
    @Nullable
    public PLVSwitchViewAnchorLayout getSwitchViewHasMedia() {
        return switchViewHasMedia;
    }

    //获取media Item的位置
    public int getMediaViewIndexInLinkMicList() {
        if (switchViewHasMedia == null) {
            return -1;
        }
        for (int i = 0; i < dataList.size(); i++) {
            LinkMicItemViewHolder viewHolder = (LinkMicItemViewHolder) rv.findViewHolderForAdapterPosition(i);
            if (viewHolder != null && viewHolder.switchViewAnchorLayout == switchViewHasMedia) {
                return i;
            }
        }
        return -1;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 操作渲染器">
    public void pauseAllRenderView() {
        isPausing = true;
        adapterCallback.muteAllAudioVideo(true);
        notifyDataSetChanged();
    }

    public void resumeAllRenderView() {
        isPausing = false;
        adapterCallback.muteAllAudioVideo(false);
        notifyDataSetChanged();
    }

    public boolean isPausing() {
        return isPausing;
    }

    //释放View
    public void releaseView() {
        //释放切换View。当连麦结束的时候，请调用该方法，释放掉之前记录的含有media的itemView。否则该ItemView会保留到下个连麦场次。
        switchViewHasMedia = null;
        teacherViewHolder = null;
        hasNotifyTeacherViewHolderBind = false;
    }

    //隐藏本地渲染器。在SurfaceView互相重叠时，只隐藏SurfaceView的父布局是无效的，需要隐藏SurfaceView本身。
    public void hideAllRenderView() {
        shouldHideAllRenderView = true;
        notifyDataSetChanged();
    }

    //显示本地渲染器
    public void showAllRenderView() {
        shouldHideAllRenderView = false;
        notifyDataSetChanged();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 更新列表">
    //更新关闭视频
    public void updateUserMuteVideo(final int pos) {
        rv.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(pos, PAYLOAD_UPDATE_VIDEO_MUTE);
            }
        });
    }

    //更新连麦列表的音量变化
    public void updateVolumeChanged() {
        if (linearLayoutManager == null) {
            return;
        }
        //只更新可见区域的item的音量变化。
        int firstVisibleItemPos = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPos = linearLayoutManager.findLastVisibleItemPosition();
        int count = lastVisibleItemPos - firstVisibleItemPos + 1;
        if (firstVisibleItemPos != RecyclerView.NO_POSITION && lastVisibleItemPos != RecyclerView.NO_POSITION && count > 0) {
            notifyItemRangeChanged(firstVisibleItemPos, count, PAYLOAD_UPDATE_VOLUME);
        }
        //找到隐藏的view holder，并更新其中的view（虽然view holder的item view隐藏了，但是子view切到别的地方去，还是能显示出来的，这里就是要更新切到别的地方去的子view）
        if (!TextUtils.isEmpty(invisibleItemLinkMicId)) {
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).getLinkMicId().equals(invisibleItemLinkMicId)) {
                    notifyItemChanged(i, PAYLOAD_UPDATE_VOLUME);
                }
            }
        }
    }

    //更新所有item
    public void updateAllItem() {
        PLVCommonLog.d(TAG, "PLVLinkMicListAdapter.updateAllItem");
        notifyDataSetChanged();
    }

    //指定linkMicId更新为不可见的item
    public void updateInvisibleItem(String linkMicId) {
        this.invisibleItemLinkMicId = linkMicId;
        notifyDataSetChanged();
    }

    //设置列表显示模式
    public void setListShowMode(PLVLinkMicListShowMode listShowMode) {
        PLVCommonLog.d(TAG, "PLVLinkMicListAdapter.setListShowMode");
        this.listShowMode = listShowMode;
        updateAllItem();
    }

    public void updateNetQuality(int quality) {
        netQuality = quality;
        //找到我的位置
        int myPos = 0;
        for (int i = 0; i < dataList.size(); i++) {
            PLVLinkMicItemDataBean index = dataList.get(i);
            if (index.getLinkMicId().equals(myLinkMicId)) {
                myPos = i;
                break;
            }
        }
        //仅更新我的网路质量
        notifyItemChanged(myPos, PAYLOAD_UPDATE_NET_QUALITY);
    }

    public void updateTeacherCoverImage(){
        if(dataList == null){
            return;
        }
        int teacherPosition = 0;
        for (int i = 0; i < dataList.size(); i++) {
            PLVLinkMicItemDataBean index = dataList.get(i);
            if (index.isTeacher()) {
                teacherPosition = i;
                break;
            }
        }
        //仅更新讲师封面图
        notifyItemChanged(teacherPosition, PAYLOAD_UPDATE_COVER_IMAGE);
    }

    //更新奖杯
    public void updateCup(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_CUP);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RecyclerView.Adapter方法实现">
    @NonNull
    @Override
    public LinkMicItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvlc_linkmic_scroll_item, parent, false);
        itemView.getLayoutParams().width = getItemWidth();
        itemView.requestLayout();

        SurfaceView renderView = adapterCallback.createLinkMicRenderView();

        final LinkMicItemViewHolder viewHolder = new LinkMicItemViewHolder(itemView);
        if (renderView != null) {
            viewHolder.renderView = renderView;
            viewHolder.flRenderViewContainer.addView(renderView, 0, getRenderViewLayoutParam());
        } else {
            PLVCommonLog.e(TAG, "create render view return null");
        }

        viewHolder.flRenderViewContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View v, final int left, int top, final int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                final int newWidth = right - left;
                final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(viewHolder.qualityWidget.getLayoutParams());
                //切换到主屏幕的时候，不要显示view holder的昵称和麦克风
                if (newWidth != getItemWidth()) {
                    viewHolder.tvNick.setVisibility(View.GONE);
                    viewHolder.ivMicState.setVisibility(View.GONE);
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            if (itemView.getContext() != null) {
                                if (PLVScreenUtils.isPortrait(itemView.getContext())) {
                                    updateNetQualityLayout(itemView.getContext(), layoutParams, 20, 12);
                                } else {
                                    updateNetQualityLayout(itemView.getContext(), layoutParams, 24, 16);
                                }
                                viewHolder.qualityWidget.setLayoutParams(layoutParams);
                            }
                        }
                    });
                } else {
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            if (v.getWidth() != getItemWidth()) {
                                return;
                            }
                            viewHolder.tvNick.setVisibility(View.VISIBLE);
                            viewHolder.ivMicState.setVisibility(View.VISIBLE);
                            if (itemView.getContext() != null) {
                                if (PLVScreenUtils.isPortrait(itemView.getContext())) {
                                    updateNetQualityLayout(itemView.getContext(), layoutParams, 12, 4);
                                } else {
                                    updateNetQualityLayout(itemView.getContext(), layoutParams, 16, 4);
                                }
                                viewHolder.qualityWidget.setLayoutParams(layoutParams);
                            }
                        }
                    });
                }
            }
        });
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getLinkMicId().hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull final LinkMicItemViewHolder holder, final int position) {
        if (holder.isViewRecycled) {
            holder.isViewRecycled = false;
            holder.renderView = adapterCallback.createLinkMicRenderView();
            if (holder.renderView != null) {
                holder.flRenderViewContainer.addView(holder.renderView, 0, getRenderViewLayoutParam());
                holder.isRenderViewSetup = false;
            } else {
                PLVCommonLog.e(TAG, String.format(Locale.US, "create render view return null at position:%d", position));
            }
        }
        PLVLinkMicItemDataBean itemDataBean = dataList.get(position);
        String linkMicId = itemDataBean.getLinkMicId();
        String nick = itemDataBean.getNick();
        int cupNum = itemDataBean.getCupNum();
        boolean isMuteVideo = itemDataBean.isMuteVideo();
        boolean isMuteAudio = itemDataBean.isMuteAudio();
        int curVolume = itemDataBean.getCurVolume();
        String actor = itemDataBean.getActor();
        boolean isTeacher = itemDataBean.isTeacher();
        boolean isGuest = itemDataBean.isGuest();
        boolean isFirstScreen = firstScreenLinkMicId.equals(linkMicId);
        boolean isSelf = myLinkMicId.equals(linkMicId);

        if (isPausing) {
            // 暂停时显示默认的占位图
            holder.linkMicRenderViewPausePlaceholder.setVisibility(View.VISIBLE);
            holder.linkMicRenderViewPausePlaceholder.bringToFront();
        } else {
            holder.linkMicRenderViewPausePlaceholder.setVisibility(View.GONE);
        }

        if (isTeacher) {
            teacherViewHolder = holder;
            if (onTeacherSwitchViewBindListener != null && !hasNotifyTeacherViewHolderBind) {
                hasNotifyTeacherViewHolderBind = true;
                onTeacherSwitchViewBindListener.onTeacherSwitchViewBind(holder.switchViewAnchorLayout);
            }

            bindCoverImage(holder, isOnlyAudio, isTeacher);
        }
        bindLogoView(holder, isFirstScreen);

        //调整item的宽高
        ViewGroup.LayoutParams vlp = holder.itemView.getLayoutParams();
        if (invisibleItemLinkMicId != null && invisibleItemLinkMicId.equals(linkMicId)) {
            if (vlp != null) {
                vlp.width = 0;
                vlp.height = 0;
            }
            holder.itemView.setVisibility(View.GONE);
        } else {
            if (vlp != null) {
                vlp.width = getItemWidth();
                vlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            holder.itemView.setVisibility(View.VISIBLE);
        }

        //设置标记
        holder.switchViewAnchorLayout.setTag(R.id.tag_link_mic_id, linkMicId);
        holder.plvPlayerLogoView.addLogo(plvPlayerLogoView.getParamZero());

        //圆角
        if (showRoundRect) {
            holder.roundRectLayout.setCornerRadius(PLVScreenUtils.dip2px(8));
        } else {
            holder.roundRectLayout.setCornerRadius(0);
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
        holder.tvNick.setText(nickString.toString());

        isMuteVideo = resolveListShowMode(holder, position);

        //设置麦克风状态
        setMicrophoneVolumeIcon(curVolume, isMuteAudio, holder);
        //是否关闭摄像头
        bindVideoMute(holder, isMuteVideo, linkMicId);
        //设置本地渲染器
        if (myLinkMicId.equals(linkMicId)) {
            localRenderView = holder.renderView;
        }

        //设置点击事件监听器
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取当前发生点击事件时，media item的位置，如果media item不在连麦列表，则为-1
                int posOfMedia = -1;
                if (switchViewHasMedia != null) {
                    PLVCommonLog.d(TAG, "onClick and media in link mic list");
                    try {
                        View itemView = (View) switchViewHasMedia.getParent().getParent();
                        RecyclerView.ViewHolder holderOfMedia = rv.getChildViewHolder(itemView);
                        posOfMedia = holderOfMedia.getAdapterPosition();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //如果点击的itemView的索引不为media在连麦列表的索引，则这里的是渲染器容器，那么说明待会这个渲染器会切到主屏，且主屏的media会切到这个ItemView
                int holderPos = holder.getAdapterPosition();
                boolean thisViewWillChangeToMainLater = holderPos != getMediaViewIndexInLinkMicList();
                adapterCallback.onClickItemListener(holderPos, switchViewHasMedia, holder.switchViewAnchorLayout);
                //如果点击的索引不是media所在的索引
                if (thisViewWillChangeToMainLater) {
                    switchViewHasMedia = holder.switchViewAnchorLayout;
                } else {
                    switchViewHasMedia = null;
                }
                updateAllItem();
            }
        });

        //如果渲染器不为空，并且渲染器没有切换到别处：
        if (holder.renderView != null && !holder.switchViewAnchorLayout.isViewSwitched()) {
            if (shouldHideAllRenderView) {
                holder.renderView.setVisibility(View.INVISIBLE);
            } else {
                holder.renderView.setVisibility(View.VISIBLE);
            }
        }

        //网络质量
        if (linkMicId.equals(myLinkMicId)) {
            holder.qualityWidget.setVisibility(View.VISIBLE);
        } else {
            holder.qualityWidget.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull LinkMicItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        PLVLinkMicItemDataBean itemDataBean = dataList.get(position);
        String linkMicId = itemDataBean.getLinkMicId();
        String nick = itemDataBean.getNick();
        String actor = itemDataBean.getActor();
        int cupNum = itemDataBean.getCupNum();
        boolean isMuteVideo = itemDataBean.isMuteVideo();
        boolean isMuteAudio = itemDataBean.isMuteAudio();
        int curVolume = itemDataBean.getCurVolume();
        boolean isTeacher = itemDataBean.isTeacher();
        boolean isGuest = itemDataBean.isGuest();
        boolean isFirstScreen = firstScreenLinkMicId.equals(linkMicId);
        boolean isSelf = myLinkMicId.equals(linkMicId);

        isMuteVideo = resolveListShowMode(holder, position);

        for (Object payload : payloads) {
            switch (payload.toString()) {
                case PAYLOAD_UPDATE_VOLUME:
                    //设置麦克风状态
                    setMicrophoneVolumeIcon(curVolume, isMuteAudio, holder);
                    break;
                case PAYLOAD_UPDATE_VIDEO_MUTE:
                    //是否关闭摄像头
                    bindVideoMute(holder, isMuteVideo, linkMicId);
                    break;
                case PAYLOAD_UPDATE_CUP:
                    //奖杯
                    if (cupNum != 0) {
                        holder.llCupLayout.setVisibility(View.VISIBLE);
                        holder.tvCupNumView.setText(cupNum > 99 ? "99+" : (cupNum + ""));
                    } else {
                        holder.llCupLayout.setVisibility(View.GONE);
                    }
                    break;
                case PAYLOAD_UPDATE_NET_QUALITY:
                    //更新网络质量
                    if (linkMicId.equals(myLinkMicId)) {
                        //netQuality
                        holder.qualityWidget.setNetQuality(netQuality);
                    }
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
    public void onViewRecycled(@NonNull LinkMicItemViewHolder holder) {
        super.onViewRecycled(holder);

        //如果渲染器没有被切换到主屏幕，那么标记view重新创建
        //如果切换到了主屏幕，不要移除渲染器，防止主屏幕的连麦画面消失或异常闪动
        //如果是本地渲染器，那么也不要销毁，因为滑动列表的时候还是要保持一个本地摄像头推流的
        if (holder.renderView != null && !holder.switchViewAnchorLayout.isViewSwitched() && holder.renderView != localRenderView) {
            holder.isViewRecycled = true;
            holder.flRenderViewContainer.removeView(holder.renderView);
            adapterCallback.releaseRenderView(holder.renderView);
            holder.renderView = null;
        }
        PLVCommonLog.d(TAG, "onViewRecycled pos=" + holder.getAdapterPosition() + " holder=" + holder.toString());
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表item绑定">
    private void bindVideoMute(@NonNull LinkMicItemViewHolder holder, boolean isMuteVideo, String linkMicId) {
        final boolean holderIsMuted = holder.flRenderViewContainer.getVisibility() != View.VISIBLE;
        if (holderIsMuted == isMuteVideo) {
            return;
        }
        //是否关闭摄像头
        if (isMuteVideo) {
            holder.flRenderViewContainer.setVisibility(View.INVISIBLE);
            // 移除渲染器
            if (holder.renderView != null) {
                holder.flRenderViewContainer.removeView(holder.renderView);
            }
        } else {
            holder.flRenderViewContainer.setVisibility(View.VISIBLE);
            // 重新配置渲染器
            if (holder.renderView != null) {
                adapterCallback.releaseRenderView(holder.renderView);
            }
            holder.renderView = adapterCallback.createLinkMicRenderView();
            adapterCallback.setupRenderView(holder.renderView, linkMicId);
            //将渲染器从View 添加到view tree中
            if (holder.renderView != null && holder.renderView.getParent() == null) {
                holder.flRenderViewContainer.addView(holder.renderView, 0, getRenderViewLayoutParam());
            }
        }
    }


    /**
     * 处理连麦列表的渲染模式
     *
     * @param holder   viewholder
     * @param position 位置
     * @return isMuteVideo
     */
    private boolean resolveListShowMode(LinkMicItemViewHolder holder, int position) {
        PLVLinkMicItemDataBean itemDataBean = dataList.get(position);
        String linkMicId = itemDataBean.getLinkMicId();
        String nick = itemDataBean.getNick();
        int cupNum = itemDataBean.getCupNum();
        boolean isMuteVideo = itemDataBean.isMuteVideo();
        boolean isMuteAudio = itemDataBean.isMuteAudio();
        int curVolume = itemDataBean.getCurVolume();
        boolean isTeacher = itemDataBean.isTeacher();
        boolean isGuest = itemDataBean.isGuest();
        boolean isFirstScreen = firstScreenLinkMicId.equals(linkMicId);
        boolean isSelf = myLinkMicId.equals(linkMicId);

        switch (listShowMode) {
            case SHOW_ALL:
                //如果是视频连麦，则渲染所有用户类型
                trySetupRenderView(holder, linkMicId);
                break;
            case SHOW_TEACHER_AND_GUEST:
                //如果是音频连麦，则只渲染用户类型：讲师、嘉宾
                if (isTeacher || isGuest) {
                    trySetupRenderView(holder, linkMicId);
                } else {
                    holder.flRenderViewContainer.setVisibility(View.GONE);
                    isMuteVideo = true;
                }
                break;
            case SHOW_FIRST_SCREEN_AND_SELF:
                if (isFirstScreen || isSelf) {
                    trySetupRenderView(holder, linkMicId);
                } else {
                    holder.flRenderViewContainer.setVisibility(View.GONE);
                    isMuteVideo = true;
                }
                break;
            case SHOW_FIRST_SCREEN:
                if (isFirstScreen) {
                    trySetupRenderView(holder, linkMicId);
                } else {
                    holder.flRenderViewContainer.setVisibility(View.GONE);
                    isMuteVideo = true;
                }
                break;
        }
        return isMuteVideo;
    }

    /**
     * 安装渲染器
     */
    private void trySetupRenderView(final LinkMicItemViewHolder holder, String linkMicId) {
        if (holder.renderView != null && !holder.isRenderViewSetup) {
            adapterCallback.setupRenderView(holder.renderView, linkMicId);
            holder.isRenderViewSetup = true;
        }
    }

    /**
     * 设置麦克风音量图片
     *
     * @param curVolume   当前音量
     * @param isMuteAudio 是否mute音频
     * @param holder      ViewHolder
     */
    private void setMicrophoneVolumeIcon(int curVolume, boolean isMuteAudio, @NonNull LinkMicItemViewHolder holder) {
        if (isMuteAudio) {
            holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_iv_mic_close);
        } else {
            if (intBetween(curVolume, 0, 5) || curVolume == 0) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_iv_mic_open);
            } else if (intBetween(curVolume, 5, 15)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_10);
            } else if (intBetween(curVolume, 15, 25)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_20);
            } else if (intBetween(curVolume, 25, 35)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_30);
            } else if (intBetween(curVolume, 35, 45)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_40);
            } else if (intBetween(curVolume, 45, 55)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_50);
            } else if (intBetween(curVolume, 55, 65)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_60);
            } else if (intBetween(curVolume, 65, 75)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_70);
            } else if (intBetween(curVolume, 75, 85)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_80);
            } else if (intBetween(curVolume, 85, 95)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_90);
            } else if (intBetween(curVolume, 95, 100)) {
                holder.ivMicState.setImageResource(R.drawable.plvlc_linkmic_mic_volume_100);
            }
        }
    }

    private void bindCoverImage(LinkMicItemViewHolder holder, boolean onlyAudio, boolean isTeacher){
        if(onlyAudio && isTeacher){
            holder.coverImageView.setVisibility(View.VISIBLE);
            PLVImageLoader.getInstance().loadImage(coverImage,holder.coverImageView);
        } else {
            holder.coverImageView.setVisibility(View.INVISIBLE);
        }
    }

    private void bindLogoView(LinkMicItemViewHolder holder, boolean isFirstScreen) {
        if (isFirstScreen && (holder != null)) {
            holder.plvPlayerLogoView.addLogo(plvPlayerLogoView.getParamZero());
            holder.plvPlayerLogoView.setVisibility(View.VISIBLE);
        } else if (!isFirstScreen && (holder != null)) {
            holder.plvPlayerLogoView.setVisibility(View.GONE);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    //判断value是否在左开右闭区间：(left, right]
    private boolean intBetween(int value, int left, int right) {
        return value > left && value <= right;
    }

    //获取渲染器的布局参数
    private FrameLayout.LayoutParams getRenderViewLayoutParam() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
    }

    /** 更新上面网络信号塔的布局尺寸，size是信号塔的尺寸，distance是右边距和上边距 */
    private void updateNetQualityLayout(Context context, FrameLayout.LayoutParams layoutParams, int size, int distance) {
        layoutParams.width = PLVScreenUtils.dip2px(context, size);
        layoutParams.height = PLVScreenUtils.dip2px(context, size);
        layoutParams.gravity = Gravity.END|Gravity.TOP;
        layoutParams.rightMargin = PLVScreenUtils.dip2px(context, distance);
        layoutParams.topMargin = PLVScreenUtils.dip2px(context, distance);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - LinkMicItemViewHolder定义">
    static class LinkMicItemViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout flRenderViewContainer;
        private ImageView linkMicRenderViewPausePlaceholder;
        private TextView tvNick;
        private TextView tvCupNumView;
        private LinearLayout llCupLayout;
        private ImageView ivMicState;
        @Nullable
        private SurfaceView renderView;
        private PLVSwitchViewAnchorLayout switchViewAnchorLayout;
        private PLVRoundRectLayout roundRectLayout;
        private PLVLSNetworkQualityWidget qualityWidget;
        private ImageView coverImageView;
        private PLVPlayerLogoView plvPlayerLogoView;
        private TextView liveLinkmicFloatingPlayingPlaceholderTv;

        //是否被回收过（渲染器如果被回收过，则下一次复用的时候，必须重新渲染器）
        private boolean isViewRecycled = false;

        private boolean isRenderViewSetup = false;

        public LinkMicItemViewHolder(View itemView) {
            super(itemView);
            flRenderViewContainer = itemView.findViewById(R.id.plvlc_link_mic_fl_render_view_container);
            linkMicRenderViewPausePlaceholder = itemView.findViewById(R.id.plvlc_link_mic_render_view_pause_placeholder);
            tvNick = itemView.findViewById(R.id.plvlc_link_mic_tv_nick);
            tvCupNumView = itemView.findViewById(R.id.plvlc_link_mic_tv_cup_num_view);
            llCupLayout = itemView.findViewById(R.id.plvlc_link_mic_ll_cup_layout);
            ivMicState = itemView.findViewById(R.id.plvlc_link_mic_iv_mic_state);
            roundRectLayout = itemView.findViewById(R.id.plvlc_linkmic_item_round_rect_layout);
            switchViewAnchorLayout = itemView.findViewById(R.id.plvlc_linkmic_switch_anchor_item);
            qualityWidget = itemView.findViewById(R.id.plvlc_link_mic_net_quality_view);
            coverImageView = itemView.findViewById(R.id.plvlc_link_mic_iv_cover_image);
            plvPlayerLogoView = itemView.findViewById(R.id.plvlc_link_mic_logo_view);
            liveLinkmicFloatingPlayingPlaceholderTv = itemView.findViewById(R.id.plvlc_live_linkmic_floating_playing_placeholder_tv);

            qualityWidget.shouldShowNoNetworkHint(false);
            qualityWidget.setNetQualityRes(
                    R.drawable.plv_network_signal_watcher_good,
                    R.drawable.plv_network_signal_watcher_middle,
                    R.drawable.plv_network_signal_watcher_poor
            );

            observeFloatingPlayer();
        }

        private void observeFloatingPlayer() {
            PLVFloatingPlayerManager.getInstance().getFloatingViewShowState()
                    .observe((LifecycleOwner) itemView.getContext(), new Observer<Boolean>() {
                        @Override
                        public void onChanged(@Nullable Boolean isShowingBoolean) {
                            final boolean isShowing = isShowingBoolean != null && isShowingBoolean;
                            liveLinkmicFloatingPlayingPlaceholderTv.setVisibility(isShowing ? View.VISIBLE : View.GONE);
                        }
                    });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 监听器定义">
    public interface OnPLVLinkMicAdapterCallback {
        /**
         * 创建连麦列表渲染器。
         * 该渲染器必须通过云课堂连麦SDK创建，不能直接构造。
         *
         * @return 渲染器
         */
        SurfaceView createLinkMicRenderView();

        /**
         * 安装SurfaceView。
         * 将创建好的SurfaceView与连麦ID关联，并设置到SDK
         *
         * @param surfaceView 渲染器
         * @param linkMicId   连麦ID
         */
        void setupRenderView(SurfaceView surfaceView, String linkMicId);

        /**
         * 释放渲染器
         *
         * @param surfaceView 渲染器
         */
        void releaseRenderView(SurfaceView surfaceView);

        /**
         * 静音音频和禁用视频
         */
        void muteAudioVideo(String linkMicId, boolean mute);

        /**
         * 静音音频和禁用视频
         */
        void muteAllAudioVideo(boolean mute);

        /**
         * 点击Item,准备和主屏的media切换位置
         *
         * @param pos                    位置
         * @param switchViewHasMedia     此前有media的item，如果不为空，则要先将这个有media的item切回到主屏幕，然后再将media和[switchViewGoMainScreen]切换
         * @param switchViewGoMainScreen 将要到主屏幕的switchView
         */
        void onClickItemListener(int pos, @Nullable PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen);
    }

    /**
     * 讲师ViewHolder中的switchView绑定监听器
     */
    public interface OnTeacherSwitchViewBindListener {
        /**
         * 讲师view holder的switch view绑定
         *
         * @param viewAnchorLayout 讲师view hodler中的switch view
         */
        void onTeacherSwitchViewBind(PLVSwitchViewAnchorLayout viewAnchorLayout);
    }


// </editor-fold>
}
