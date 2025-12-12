package com.easefun.polyv.liveecommerce.modules.linkmic.adapter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVLSNetworkQualityWidget;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    public static final int ITEM_TYPE_DEFAULT = -1;
    public static final int ITEM_TYPE_ONLY_ONE = 1;
    //四人以内，包括四人
    public static final int ITEM_TYPE_LESS_THAN_FOUR = 2;
    //超过四人
    public static final int ITEM_TYPE_MORE_THAN_FOUR = 3;

    /**** payload to bind data into view ****/
    private static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    private static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";
    private static final String PAYLOAD_UPDATE_CUP = "updateCup";
    private static final String PAYLOAD_UPDATE_NET_QUALITY = "updateNetQuality";
    private static final String PAYLOAD_UPDATE_COVER_IMAGE = "updateCoverImage";
    private static final String PAYLOAD_UPDATE_VIDEO_SIZE = "updateVideoSize";

    //默认的封面图
    private static final String DEFAULT_LIVE_STREAM_COVER_IMAGE = "https://s1.videocc.net/default-img/channel/default-splash.png";


    /**** data ****/
    private List<PLVLinkMicItemDataBean> dataList;
    private Map<String, Bitmap> linkMicIdSnapshotBitmapMap = new HashMap<>();
    private Map<String, Rect> linkMicIdVideoSizeMap = new HashMap<>();

    /**** listener ****/
    @NotNull
    private OnPLVLinkMicAdapterCallback adapterCallback;
    @Nullable
    private OnTeacherSwitchViewBindListener onTeacherSwitchViewBindListener;

    /**** status ****/
    // 连麦时单独显示自己的摄像头
    private final boolean myselfShowSeparately;
    // 连麦需要单独显示的item
    @Nullable
    private PLVLinkMicItemDataBean linkMicItemDataBeanRemovedByShowSeparate;
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
    private PLVLinkMicConstant.NetworkQuality netQuality;
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
    private GridLayoutManager gridLayoutManager;
    @Nullable
    private LinkMicItemViewHolder teacherViewHolder;
    @Nullable
    private View localRenderView;
    private View checkClickView;

    private int itemType = ITEM_TYPE_DEFAULT;

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLinkMicListAdapter(RecyclerView rv, GridLayoutManager gridLayoutManager, boolean myselfShowSeparately, @NotNull OnPLVLinkMicAdapterCallback adapterCallback) {
        this.rv = rv;
        this.gridLayoutManager = gridLayoutManager;
        this.myselfShowSeparately = myselfShowSeparately;
        this.adapterCallback = adapterCallback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - set & get">
    //设置数据源
    public void setDataList(List<PLVLinkMicItemDataBean> dataList) {
        this.dataList = dataList;
    }

    public List<PLVLinkMicItemDataBean> getDataList() {
        return dataList;
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
    public void setGridLayoutManager(GridLayoutManager gridLayoutManager) {
        this.gridLayoutManager = gridLayoutManager;
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
        for (int i = 0; i < getDataListFiltered().size(); i++) {
            if (getDataListFiltered().get(i).getLinkMicId().equals(firstScreenLinkMicId)) {
                LinkMicItemViewHolder viewHolder = (LinkMicItemViewHolder) rv.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    return viewHolder.switchViewAnchorLayoutParent;
                }
            }
        }
        return null;
    }

    //获取指定索引的switchView
    public PLVSwitchViewAnchorLayout getSwitchView(int index) {
        LinkMicItemViewHolder viewHolder = (LinkMicItemViewHolder) rv.findViewHolderForAdapterPosition(index);
        if (viewHolder != null) {
            return viewHolder.switchViewAnchorLayoutParent;
        }
        return null;
    }

    //设置第一画面连麦Id
    public void setFirstScreenLinkMicId(String firstScreenLinkMicId) {
        this.firstScreenLinkMicId = firstScreenLinkMicId;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getItemType() {
        return itemType;
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
        for (int i = 0; i < getDataListFiltered().size(); i++) {
            LinkMicItemViewHolder viewHolder = (LinkMicItemViewHolder) rv.findViewHolderForAdapterPosition(i);
            if (viewHolder != null && viewHolder.switchViewAnchorLayoutParent == switchViewHasMedia) {
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
        if (gridLayoutManager == null) {
            return;
        }
        //只更新可见区域的item的音量变化。
        int firstVisibleItemPos = gridLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPos = gridLayoutManager.findLastVisibleItemPosition();
        int count = lastVisibleItemPos - firstVisibleItemPos + 1;
        if (firstVisibleItemPos != RecyclerView.NO_POSITION && lastVisibleItemPos != RecyclerView.NO_POSITION && count > 0) {
            notifyItemRangeChanged(firstVisibleItemPos, count, PAYLOAD_UPDATE_VOLUME);
        }
        //找到隐藏的view holder，并更新其中的view（虽然view holder的item view隐藏了，但是子view切到别的地方去，还是能显示出来的，这里就是要更新切到别的地方去的子view）
        if (!TextUtils.isEmpty(invisibleItemLinkMicId)) {
            for (int i = 0; i < getDataListFiltered().size(); i++) {
                if (getDataListFiltered().get(i).getLinkMicId().equals(invisibleItemLinkMicId)) {
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

    public void updateNetQuality(PLVLinkMicConstant.NetworkQuality quality) {
        netQuality = quality;
        if (dataList == null) {
            return;
        }
        //找到我的位置
        int myPos = 0;
        for (int i = 0; i < getDataListFiltered().size(); i++) {
            PLVLinkMicItemDataBean index = getDataListFiltered().get(i);
            if (index.getLinkMicId().equals(myLinkMicId)) {
                myPos = i;
                break;
            }
        }
        //仅更新我的网路质量
        notifyItemChanged(myPos, PAYLOAD_UPDATE_NET_QUALITY);
    }

    public void updateVideoSizeChanged(String linkMicId, int width, int height) {
        if (linkMicIdVideoSizeMap.containsKey(linkMicId)) {
            Rect videoSize = linkMicIdVideoSizeMap.get(linkMicId);
            if (videoSize != null && videoSize.width() == width && videoSize.height() == height) {
                return;
            }
        }
        linkMicIdVideoSizeMap.put(linkMicId, new Rect(0, 0, width, height));
        if (itemType == ITEM_TYPE_ONLY_ONE) {
            notifyItemRangeChanged(0, getDataListFiltered().size(), PAYLOAD_UPDATE_VIDEO_SIZE);
        }
    }

    public void updateTeacherCoverImage(){
        if(dataList == null){
            return;
        }
        int teacherPosition = 0;
        for (int i = 0; i < getDataListFiltered().size(); i++) {
            PLVLinkMicItemDataBean index = getDataListFiltered().get(i);
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

    public void checkClickItemView(MotionEvent ev) {
        if (checkClickView != null && ev.getAction() != MotionEvent.ACTION_DOWN) {
            checkClickView.dispatchTouchEvent(ev);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RecyclerView.Adapter方法实现">
    @NonNull
    @Override
    public LinkMicItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_linkmic_recycler_view_multi_item, parent, false);

        View renderView = adapterCallback.createLinkMicRenderView();

        final LinkMicItemViewHolder viewHolder = new LinkMicItemViewHolder(itemView);
        viewHolder.renderView = renderView;
        if (renderView != null) {
            viewHolder.flRenderViewContainer.addView(renderView, 0, getRenderViewLayoutParam());
        } else {
            PLVCommonLog.e(TAG, "create render view return null");
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return getDataListFiltered().get(position).getLinkMicId().hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull final LinkMicItemViewHolder holder, final int position) {
        if (holder.isViewRecycled) {
            holder.isViewRecycled = false;
            holder.renderView = adapterCallback.createLinkMicRenderView();
            if (holder.renderView != null) {
                holder.flRenderViewContainer.addView(holder.renderView, 0, getRenderViewLayoutParam());
            } else {
                PLVCommonLog.e(TAG, String.format(Locale.US, "create render view return null at position:%d", position));
            }
        }
        PLVLinkMicItemDataBean itemDataBean = getDataListFiltered().get(position);
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
                onTeacherSwitchViewBindListener.onTeacherSwitchViewBind(holder.switchViewAnchorLayoutParent);
            }

            bindCoverImage(holder, isOnlyAudio, isTeacher);
        }
        bindLogoView(holder, isFirstScreen);

        //调整item的宽高
        processVideoSizeChanged(holder, itemDataBean);
        if (position == 0) {
            final PLVECFloatingWindow floatingWindow = PLVDependManager.getInstance().get(PLVECFloatingWindow.class);
            floatingWindow.bindContentView(holder.switchViewAnchorLayoutParent);
        }

        //设置标记
        holder.switchViewAnchorLayoutParent.setTag(R.id.tag_link_mic_id, linkMicId);
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
            nickString.append(PLVAppUtils.getString(R.string.plv_chat_me));
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
            }
        });

        //如果渲染器不为空，并且渲染器没有切换到别处：
        if (holder.renderView != null && !holder.switchViewAnchorLayoutParent.isViewSwitched()) {
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
        PLVLinkMicItemDataBean itemDataBean = getDataListFiltered().get(position);
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
                case PAYLOAD_UPDATE_VIDEO_SIZE:
                    processVideoSizeChanged(holder, itemDataBean);
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
        if (holder.renderView != null && !holder.switchViewAnchorLayoutParent.isViewSwitched() && holder.renderView != localRenderView) {
            holder.isViewRecycled = true;
            holder.flRenderViewContainer.removeView(holder.renderView);
            adapterCallback.releaseRenderView(holder.renderView);
            holder.renderView = null;
        }
        PLVCommonLog.d(TAG, "onViewRecycled pos=" + holder.getAdapterPosition() + " holder=" + holder.toString());
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : getDataListFiltered().size();
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
//            // 移除渲染器
//            if (holder.renderView != null) {
//                holder.flRenderViewContainer.removeView(holder.renderView);
//            }
        } else {
            holder.flRenderViewContainer.setVisibility(View.VISIBLE);
//            // 重新配置渲染器
//            if (holder.renderView != null) {
//                adapterCallback.releaseRenderView(holder.renderView);
//            }
//            holder.renderView = adapterCallback.createLinkMicRenderView();
//            adapterCallback.setupRenderView(holder.renderView, linkMicId);
//            //将渲染器从View 添加到view tree中
//            if (holder.renderView != null && holder.renderView.getParent() == null) {
//                holder.flRenderViewContainer.addView(holder.renderView, 0, getRenderViewLayoutParam());
//            }
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
        PLVLinkMicItemDataBean itemDataBean = getDataListFiltered().get(position);
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
        if (holder.renderView != null) {
            adapterCallback.setupRenderView(holder.renderView, linkMicId);
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
            holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_iv_mic_close);
        } else {
            if (intBetween(curVolume, 0, 5) || curVolume == 0) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_iv_mic_open);
            } else if (intBetween(curVolume, 5, 15)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_10);
            } else if (intBetween(curVolume, 15, 25)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_20);
            } else if (intBetween(curVolume, 25, 35)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_30);
            } else if (intBetween(curVolume, 35, 45)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_40);
            } else if (intBetween(curVolume, 45, 55)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_50);
            } else if (intBetween(curVolume, 55, 65)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_60);
            } else if (intBetween(curVolume, 65, 75)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_70);
            } else if (intBetween(curVolume, 75, 85)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_80);
            } else if (intBetween(curVolume, 85, 95)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_90);
            } else if (intBetween(curVolume, 95, 100)) {
                holder.ivMicState.setImageResource(R.drawable.plvec_linkmic_mic_volume_100);
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

    private void processVideoSizeChanged(LinkMicItemViewHolder holder, PLVLinkMicItemDataBean itemDataBean) {
        Rect videoSize = itemDataBean.getLinkMicId() == null ? null : linkMicIdVideoSizeMap.get(itemDataBean.getLinkMicId());
        boolean isLandscape = videoSize != null && videoSize.width() > videoSize.height();
        ViewGroup.LayoutParams switchViewAnchorLayoutLp = holder.switchViewAnchorLayout.getLayoutParams();
        ConstraintLayout.LayoutParams vlp = (ConstraintLayout.LayoutParams) holder.roundRectLayout.getLayoutParams();
        if (itemType == ITEM_TYPE_ONLY_ONE) {
            switchViewAnchorLayoutLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            if (!isLandscape) {
                vlp.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
                vlp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                vlp.topMargin = 0;
                vlp.dimensionRatio = "H,2:3";
            } else {
                vlp.height = 0;
                vlp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
                vlp.topMargin = (int) (ScreenUtils.getScreenOrientatedHeight() * 0.166);
                vlp.dimensionRatio = format("H,{}:{}", videoSize.width(), videoSize.height());
            }
            holder.tvNick.setVisibility(View.GONE);
            holder.ivMicState.setVisibility(View.GONE);
        } else {
            switchViewAnchorLayoutLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            vlp.height = 0;
            vlp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            vlp.topMargin = 0;
            vlp.dimensionRatio = "H,2:3";
            holder.tvNick.setVisibility(View.VISIBLE);
            holder.ivMicState.setVisibility(View.VISIBLE);
        }
        holder.switchViewAnchorLayout.setLayoutParams(switchViewAnchorLayoutLp);
        holder.roundRectLayout.setLayoutParams(vlp);
    }

    private void bindLogoView(LinkMicItemViewHolder holder, boolean isFirstScreen) {
        if (isFirstScreen && (holder != null)) {
            holder.plvPlayerLogoView.addLogo(plvPlayerLogoView.getParamZero());
            holder.plvPlayerLogoView.setVisibility(View.VISIBLE);
            checkClickView = holder.itemView;
        } else if (!isFirstScreen && (holder != null)) {
            holder.plvPlayerLogoView.setVisibility(View.GONE);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法 - 列表过滤">

    private List<PLVLinkMicItemDataBean> getDataListFiltered() {
        return filterShowMyselfSeparate(dataList);
    }

    private List<PLVLinkMicItemDataBean> filterShowMyselfSeparate(List<PLVLinkMicItemDataBean> upstream) {
        if (upstream.size() != 2 || !myselfShowSeparately) {
            linkMicItemDataBeanRemovedByShowSeparate = null;
            callLinkMicShowSeparateChanged();
            return upstream;
        }
        final List<PLVLinkMicItemDataBean> result = new ArrayList<>(upstream);
        PLVLinkMicItemDataBean myLinkMicItem = null;
        PLVLinkMicItemDataBean teacherLinkMicItem = null;
        PLVLinkMicItemDataBean firstLinkMicItem = null;
        for (PLVLinkMicItemDataBean linkMicItemDataBean : upstream) {
            if (myLinkMicId != null && myLinkMicId.equals(linkMicItemDataBean.getLinkMicId())) {
                myLinkMicItem = linkMicItemDataBean;
            }
            if (linkMicItemDataBean.isTeacher()) {
                teacherLinkMicItem = linkMicItemDataBean;
            }
            if (firstLinkMicItem == null && linkMicItemDataBean != myLinkMicItem) {
                firstLinkMicItem = linkMicItemDataBean;
            }
        }
        if (myLinkMicItem == null) {
            linkMicItemDataBeanRemovedByShowSeparate = null;
            callLinkMicShowSeparateChanged();
            return result;
        }
        if (myLinkMicItem.isFirstScreen()) {
            if (teacherLinkMicItem != null) {
                result.remove(teacherLinkMicItem);
                linkMicItemDataBeanRemovedByShowSeparate = teacherLinkMicItem;
            } else {
                result.remove(firstLinkMicItem);
                linkMicItemDataBeanRemovedByShowSeparate = firstLinkMicItem;
            }
        } else {
            result.remove(myLinkMicItem);
            linkMicItemDataBeanRemovedByShowSeparate = myLinkMicItem;
        }
        callLinkMicShowSeparateChanged();
        return result;
    }

    private void callLinkMicShowSeparateChanged() {
        adapterCallback.onLinkMicItemShowSeparateChanged(linkMicItemDataBeanRemovedByShowSeparate);
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
        private View renderView;
        private PLVSwitchViewAnchorLayout switchViewAnchorLayout;
        private PLVSwitchViewAnchorLayout switchViewAnchorLayoutParent;
        private PLVRoundRectLayout roundRectLayout;
        private PLVLSNetworkQualityWidget qualityWidget;
        private ImageView coverImageView;
        private PLVPlayerLogoView plvPlayerLogoView;
        private TextView liveLinkmicFloatingPlayingPlaceholderTv;

        //是否被回收过（渲染器如果被回收过，则下一次复用的时候，必须重新渲染器）
        private boolean isViewRecycled = false;

        public LinkMicItemViewHolder(View itemView) {
            super(itemView);
            flRenderViewContainer = itemView.findViewById(R.id.plvec_link_mic_fl_render_view_container);
            linkMicRenderViewPausePlaceholder = itemView.findViewById(R.id.plvec_link_mic_render_view_pause_placeholder);
            tvNick = itemView.findViewById(R.id.plvec_link_mic_tv_nick);
            tvCupNumView = itemView.findViewById(R.id.plvec_link_mic_tv_cup_num_view);
            llCupLayout = itemView.findViewById(R.id.plvec_link_mic_ll_cup_layout);
            ivMicState = itemView.findViewById(R.id.plvec_link_mic_iv_mic_state);
            roundRectLayout = itemView.findViewById(R.id.plvec_linkmic_item_round_rect_layout);
            switchViewAnchorLayout = itemView.findViewById(R.id.plvec_linkmic_switch_anchor_item);
            switchViewAnchorLayoutParent = itemView.findViewById(R.id.plvec_linkmic_switch_anchor_item_parent);
            qualityWidget = itemView.findViewById(R.id.plvec_link_mic_net_quality_view);
            coverImageView = itemView.findViewById(R.id.plvec_link_mic_iv_cover_image);
            plvPlayerLogoView = itemView.findViewById(R.id.plvec_link_mic_logo_view);
            liveLinkmicFloatingPlayingPlaceholderTv = itemView.findViewById(R.id.plvec_live_linkmic_floating_playing_placeholder_tv);

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
        View createLinkMicRenderView();

        /**
         * 安装renderView。
         * 将创建好的renderView与连麦ID关联，并设置到SDK
         *
         * @param renderView 渲染器
         * @param linkMicId   连麦ID
         */
        void setupRenderView(View renderView, String linkMicId);

        /**
         * 释放渲染器
         *
         * @param view 渲染器
         */
        void releaseRenderView(View view);

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


        /**
         * 单独显示的连麦视图变更回调
         *
         * @param linkMicItemDataBean 需要单独显示的视图
         */
        void onLinkMicItemShowSeparateChanged(PLVLinkMicItemDataBean linkMicItemDataBean);
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
