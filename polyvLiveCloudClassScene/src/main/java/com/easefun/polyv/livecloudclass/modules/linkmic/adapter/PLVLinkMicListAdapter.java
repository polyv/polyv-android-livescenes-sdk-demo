package com.easefun.polyv.livecloudclass.modules.linkmic.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * date: 2020/7/27
 * author: hwj
 * description: 连麦列表适配器
 */
public class PLVLinkMicListAdapter extends RecyclerView.Adapter<PLVLinkMicListAdapter.LinkMicItemViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLinkMicListAdapter.class.getSimpleName();
    public static final int HORIZONTAL_VISIBLE_COUNT = 3;

    private static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    private static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";
    private static final String PAYLOAD_UPDATE_CUP = "updateCup";

    /**** data ****/
    private List<PLVLinkMicItemDataBean> dataList;

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
    //列表显示模式
    private PLVLinkMicListShowMode listShowMode = PLVLinkMicListShowMode.SHOW_ALL;


    private boolean hasNotifyTeacherViewHolderBind = false;


    /**** View ****/
    //保存ppt(三分屏)/video(纯视频不支持RTC)/renderView(纯视频支持RTC)的switch View，不为null时，内部保存的是ppt/video/renderView，为null时，表示ppt/video/renderView不在连麦列表，或者调用了[releaseView]方法释放了引用。
    @Nullable
    private PLVSwitchViewAnchorLayout switchViewHasMedia;
    private RecyclerView rv;
    @Nullable
    private LinkMicItemViewHolder teacherViewHolder;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLinkMicListAdapter(RecyclerView rv, @NotNull OnPLVLinkMicAdapterCallback adapterCallback) {
        this.rv = rv;
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
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_VOLUME);
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

    //更新奖杯
    public void updateCup(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_CUP);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RecyclerView.Adapter方法实现">
    @NonNull
    @Override
    public LinkMicItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvlc_linkmic_scroll_item, parent, false);
        itemView.getLayoutParams().width = getItemWidth();
        itemView.requestLayout();

        SurfaceView renderView = adapterCallback.createLinkMicRenderView();

        LinkMicItemViewHolder viewHolder = new LinkMicItemViewHolder(itemView);
        if (renderView != null) {
            viewHolder.renderView = renderView;
            viewHolder.flRenderViewContainer.addView(renderView, getRenderViewLayoutParam());
        } else {
            PLVCommonLog.e(TAG, "create render view return null");
        }
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
                holder.flRenderViewContainer.addView(holder.renderView, getRenderViewLayoutParam());
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

        if (isTeacher) {
            teacherViewHolder = holder;
            if (onTeacherSwitchViewBindListener != null && !hasNotifyTeacherViewHolderBind) {
                hasNotifyTeacherViewHolderBind = true;
                onTeacherSwitchViewBindListener.onTeacherSwitchViewBind(holder.switchViewAnchorLayout);
            }
        }

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

        //设置昵称可见性
        boolean isViewSwitched = mediaInLinkMicListLinkMicId != null && mediaInLinkMicListLinkMicId.equals(linkMicId);
        if (isViewSwitched) {
            //如果View切换到别出去了，说明该item此时是media。media在这里时，不要显示昵称
            holder.tvNick.setVisibility(View.GONE);
        } else {
            holder.tvNick.setVisibility(View.VISIBLE);
        }

        isMuteVideo = resolveListShowMode(holder, position);

        //设置麦克风状态
        setMicrophoneVolumeIcon(curVolume, isMuteAudio, holder);
        //是否关闭摄像头
        bindVideoMute(holder, isMuteVideo, linkMicId);

        //设置点击事件监听器
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果点击的itemView的索引不为media在连麦列表的索引，则这里的是渲染器容器，那么说明待会这个渲染器会切到主屏，且主屏的media会切到这个ItemView
                boolean thisViewWillChangeToMainLater = holder.getAdapterPosition() != getMediaViewIndexInLinkMicList();
                adapterCallback.onClickItemListener(holder.getAdapterPosition(), switchViewHasMedia, holder.switchViewAnchorLayout);
                //如果点击的索引不是media所在的索引
                if (thisViewWillChangeToMainLater) {
                    switchViewHasMedia = holder.switchViewAnchorLayout;
                } else {
                    switchViewHasMedia = null;
                }
                updateAllItem();//如果有切换一次/切换多次，更新昵称的显示/隐藏
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
    }

    private void trySetupRenderView(final LinkMicItemViewHolder holder, String linkMicId) {
        if (holder.renderView != null && !holder.isRenderViewSetup) {
            adapterCallback.setupRenderView(holder.renderView, linkMicId);
            holder.isRenderViewSetup = true;
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
        if (holder.renderView != null && !holder.switchViewAnchorLayout.isViewSwitched()) {
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
        //是否关闭摄像头
        if (isMuteVideo) {
            holder.flRenderViewContainer.setVisibility(View.INVISIBLE);
            //一并改变渲染器的可见性
            if (holder.renderView != null) {
                holder.renderView.setVisibility(View.INVISIBLE);
            }
            //将渲染器从View tree中移除（在部分华为机型上发现渲染器的SurfaceView隐藏后还会叠加显示）
            holder.flRenderViewContainer.removeView(holder.renderView);
        } else {
            holder.flRenderViewContainer.setVisibility(View.VISIBLE);
            //一并改变渲染器的可见性
            if (holder.renderView != null) {
                holder.renderView.setVisibility(View.VISIBLE);
            }
            //将渲染器从View 添加到view tree中
            if (holder.renderView != null && holder.renderView.getParent() == null) {
                holder.flRenderViewContainer.addView(holder.renderView, getRenderViewLayoutParam());
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
        private TextView tvNick;
        private TextView tvCupNumView;
        private LinearLayout llCupLayout;
        private ImageView ivMicState;
        @Nullable
        private SurfaceView renderView;
        private PLVSwitchViewAnchorLayout switchViewAnchorLayout;
        private PLVRoundRectLayout roundRectLayout;
        //是否被回收过（渲染器如果被回收过，则下一次复用的时候，必须重新渲染器）
        private boolean isViewRecycled = false;

        private boolean isRenderViewSetup = false;

        public LinkMicItemViewHolder(View itemView) {
            super(itemView);
            flRenderViewContainer = itemView.findViewById(R.id.plvlc_link_mic_fl_render_view_container);
            tvNick = itemView.findViewById(R.id.plvlc_link_mic_tv_nick);
            tvCupNumView = itemView.findViewById(R.id.plvlc_link_mic_tv_cup_num_view);
            llCupLayout = itemView.findViewById(R.id.plvlc_link_mic_ll_cup_layout);
            ivMicState = itemView.findViewById(R.id.plvlc_link_mic_iv_mic_state);
            switchViewAnchorLayout = itemView.findViewById(R.id.plvlc_linkmic_switch_anchor_item);
            roundRectLayout = itemView.findViewById(R.id.plvlc_linkmic_item_round_rect_layout);
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
         * 点击Item,准备和主屏的media切换位置
         *
         * @param pos                    位置
         * @param switchViewHasMedia     此前有media的item，如果不为空，则要先将这个有media的item切回到主屏幕，然后再将media和[switchViewGoMainScreen]切换
         * @param switchViewGoMainScreen 将要到主屏幕的switchView
         */
        void onClickItemListener(int pos, @Nullable PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen);
    }

    public interface OnTeacherSwitchViewBindListener {
        void onTeacherSwitchViewBind(PLVSwitchViewAnchorLayout viewAnchorLayout);
    }


// </editor-fold>
}
