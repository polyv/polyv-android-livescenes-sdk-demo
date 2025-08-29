package com.easefun.polyv.livestreamer.modules.liveroom.adapter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;
import static com.plv.thirdpart.svga.PLVSvgaHelper.loadFromAssets;

import android.arch.lifecycle.Observer;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.swipe.PLVSwipeMenu;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livestreamer.R;
import com.opensource.svgaplayer.SVGAImageView;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 成员列表适配器
 */
public class PLVLSMemberAdapter extends RecyclerView.Adapter<PLVLSMemberAdapter.MemberViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    public static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";
    public static final String PAYLOAD_UPDATE_CAMERA_DIRECTION = "updateCameraDirection";
    public static final String PAYLOAD_UPDATE_SOCKET_USER_DATA = "updateSocketUserData";
    public static final String PAYLOAD_UPDATE_LINK_MIC_MEDIA_TYPE = "updateLinkMicMediaType";
    public static final String PAYLOAD_UPDATE_LINK_MIC_CONTROL = "updateLinkMicControl";
    public static final String PAYLOAD_UPDATE_PERMISSION_CHANGE = "updatePermissionChange";
    public static final String PAYLOAD_UPDATE_FIRST_VIEW = "updateFirstView";

    private static final String LINK_MIC_INVITATION_DRAWABLE_FILE_NAME = "plvls_linkmic_invitation_waiting.svga";

    //是否是成员列表显示，true：成员列表，false：搜索列表
    private boolean isMemberListShow = true;
    //dataList
    private List<PLVMemberItemDataBean> dataList;
    private List<PLVMemberItemDataBean> srcDataList;
    //streamerStatus
    private boolean isStartedStatus;
    //初始打开连麦列表，当列表中存在非特殊身份用户时，显示左滑菜单，3秒后恢复原位
    private boolean isShowedSwipeMenu;
    private boolean isFirstOpenMemberLayout;
    //连麦类型(视频/音频)
    private boolean isVideoLinkMicType = true;
    private boolean isOpenLinkMic = false;
    private boolean isGuestAutoLinkMic;
    private boolean isGuest = false;
    private boolean isChannelAllowInviteLinkMic = false;
    //是否只显示音频连麦
    private boolean isOnlyShowAudioUI = false;
    // 是否允许嘉宾相互移交主讲权限
    private boolean allowGuestTransferSpeaker;

    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSMemberAdapter(IPLVLiveRoomDataManager liveRoomDataManager) {
        isGuestAutoLinkMic = liveRoomDataManager.getConfig().isAutoLinkToGuest();
        isGuest = liveRoomDataManager.getConfig().getUser().getViewerType().equals(PLVSocketUserConstant.USERTYPE_GUEST);
        allowGuestTransferSpeaker = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_GUEST_TRANSFER_SPEAKER_ENABLE);
        observeClassDetail(liveRoomDataManager);
    }

    private void observeClassDetail(final IPLVLiveRoomDataManager liveRoomDataManager) {
        liveRoomDataManager.getClassDetailVO().observeForever(new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> data) {
                if (data == null || data.getData() == null || data.getData().getData() == null) {
                    return;
                }
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                isChannelAllowInviteLinkMic = data.getData().getData().isInviteAudioEnabled();
                notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_LINK_MIC_CONTROL);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvls_live_room_member_list_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MemberViewHolder holder, int position) {
        PLVMemberItemDataBean memberItemDataBean = dataList.get(position);
        final PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        @Nullable
        PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        //是否是特殊身份类型
        boolean isSpecialType = PLVEventHelper.isSpecialType(socketUserBean.getUserType());
        //头衔
        String actor = PLVEventHelper.fixActor(socketUserBean.getActor(), socketUserBean.getUserType());

        holder.currentMemberItemDataBean = memberItemDataBean;

        boolean isMe = PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(socketUserBean.getUserId());

        //加载头像
        int defaultAvatar = isSpecialType ? R.drawable.plvls_member_teacher_missing_face : R.drawable.plvls_member_student_missing_face;
        PLVImageLoader.getInstance().loadImageNoDiskCache(
                holder.plvlsMemberAvatarIv.getContext(),
                socketUserBean.getPic(),
                defaultAvatar,
                defaultAvatar,
                holder.plvlsMemberAvatarIv
        );
        //设置头衔
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(socketUserBean.getUserType())) {
            holder.plvlsMemberUserTypeTv.setVisibility(View.VISIBLE);
            holder.plvlsMemberUserTypeTv.setBackgroundResource(R.drawable.plvls_member_teacher_tv_bg_shape);
            holder.plvlsMemberUserTypeTv.setText(actor);
        } else if (PLVSocketUserConstant.USERTYPE_GUEST.equals(socketUserBean.getUserType())) {
            holder.plvlsMemberUserTypeTv.setVisibility(View.VISIBLE);
            holder.plvlsMemberUserTypeTv.setBackgroundResource(R.drawable.plvls_member_guest_tv_bg_shape);
            holder.plvlsMemberUserTypeTv.setText(actor);
        } else if (PLVSocketUserConstant.USERTYPE_MANAGER.equals(socketUserBean.getUserType())) {
            holder.plvlsMemberUserTypeTv.setVisibility(View.VISIBLE);
            holder.plvlsMemberUserTypeTv.setBackgroundResource(R.drawable.plvls_member_manager_tv_bg_shape);
            holder.plvlsMemberUserTypeTv.setText(actor);
        } else if (PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(socketUserBean.getUserType())) {
            holder.plvlsMemberUserTypeTv.setVisibility(View.VISIBLE);
            holder.plvlsMemberUserTypeTv.setBackgroundResource(R.drawable.plvls_member_assistant_tv_bg_shape);
            holder.plvlsMemberUserTypeTv.setText(actor);
        } else {
            holder.plvlsMemberUserTypeTv.setVisibility(View.GONE);
        }
        //设置昵称
        final SpannableStringBuilder nickSpan = new SpannableStringBuilder(socketUserBean.getNick());
        if (isMe) {
            nickSpan.append(PLVAppUtils.getString(R.string.plv_chat_me_2));
        }
        holder.plvlsMemberNickTv.setText(nickSpan);
        //设置麦克风、摄像头的view显示状态
        updateMicControlStatus(holder, position);
        //授权
        updatePermissionStatus(holder, linkMicItemDataBean);
        // 第一画面
        updateFirstViewStatus(holder, linkMicItemDataBean);
        //滑动view的设置
        if (isMe || isSpecialType || isGuest) {
            holder.plvlsMemberSwipeMenu.enabledSwipe(false);
        } else {
            holder.plvlsMemberSwipeMenu.enabledSwipe(true);
            holder.showSwipeMenuInFirstOpenMemberLayout();
        }
        //设置是否只显示音频连麦
        if (isOnlyShowAudioUI) {
            holder.plvlsMemberCamIv.setVisibility(isOnlyShowAudioUI ? View.GONE : View.VISIBLE);
            holder.plvlsMemberCamFrontIv.setVisibility(isOnlyShowAudioUI ? View.GONE : View.VISIBLE);
        }
        //设置禁言状态
        holder.plvlsMemberBanTv.setVisibility(socketUserBean.isBanned() ? View.VISIBLE : View.GONE);
        //禁言操作
        holder.plvlsMemberDoBanTv.setText(socketUserBean.isBanned() ? R.string.plv_chat_unban_2 : R.string.plv_chat_ban);
        holder.plvlsMemberDoBanTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isShield = PLVAppUtils.getString(R.string.plv_chat_ban).equals(holder.plvlsMemberDoBanTv.getText().toString());
                if (isShield) {
                    holder.banConfirmViewChange(true);
                } else {
                    PLVSwipeMenu.closeMenu();
                    int sendResult = PolyvChatroomManager.getInstance().removeShield(socketUserBean.getUserId());
                    if (sendResult > 0) {
                        PLVToast.Builder.context(v.getContext())
                                .setText(R.string.plv_chat_unban_success)
                                .build()
                                .show();
                        holder.updateShieldView(false);
                        socketUserBean.setBanned(false);
                    } else {
                        PLVToast.Builder.context(v.getContext())
                                .setText(PLVAppUtils.formatString(R.string.plv_chat_unban_fail_2, sendResult + ""))
                                .build()
                                .show();
                    }
                }
            }
        });
        holder.plvlsMemberBanConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVSwipeMenu.closeMenu();
                int sendResult = PolyvChatroomManager.getInstance().shield(socketUserBean.getUserId());
                if (sendResult > 0) {
                    PLVToast.Builder.context(v.getContext())
                            .setText(R.string.plv_chat_ban_success)
                            .build()
                            .show();
                    holder.updateShieldView(true);
                    socketUserBean.setBanned(true);
                } else {
                    PLVToast.Builder.context(v.getContext())
                            .setText(PLVAppUtils.formatString(R.string.plv_chat_ban_fail_2, sendResult + ""))
                            .build()
                            .show();
                }
            }
        });
        //踢人操作
        holder.plvlsMemberKickTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.kickConfirmViewChange(true);
            }
        });
        holder.plvlsMemberKickConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVSwipeMenu.closeMenu();
                int sendResult = PolyvChatroomManager.getInstance().kick(socketUserBean.getUserId());
                if (sendResult > 0) {
                    PLVToast.Builder.context(v.getContext())
                            .setText(R.string.plv_chat_kick_success_2)
                            .build()
                            .show();
                    int position = removeData(socketUserBean.getUserId());
                    if (position >= 0) {
                        notifyItemRemoved(position);
                        PolyvChatroomManager.getInstance().setOnlineCount(PolyvChatroomManager.getInstance().getOnlineCount() - 1);
                    }
                } else {
                    PLVToast.Builder.context(v.getContext())
                            .setText(PLVAppUtils.formatString(R.string.plv_chat_kick_fail_2, sendResult + ""))
                            .build()
                            .show();
                }
            }
        });

        final TextView tempUserTypeTv= holder.plvlsMemberUserTypeTv;
        final TextView tempNickTv = holder.plvlsMemberNickTv;
        final String finalActor = actor;
        //如果昵称+头衔超过长度，限制头衔长度
        tempNickTv.post(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(finalActor) || TextUtils.isEmpty(nickSpan)){
                    return;
                }
                int actorTvWidth = tempUserTypeTv.getWidth();
                int nickTvWidth = tempNickTv.getWidth();
                float actorTxWidth = tempUserTypeTv.getPaint().measureText(finalActor);
                float nickTxWidth = tempNickTv.getPaint().measureText(nickSpan.toString());
                int tvTotalWidth = actorTvWidth + nickTvWidth;
                float painWidth = actorTxWidth + nickTxWidth;
                tempUserTypeTv.setMaxEms(Integer.MAX_VALUE);
                if(painWidth >= tvTotalWidth){
                    tempUserTypeTv.setMaxEms(5);
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        PLVMemberItemDataBean memberItemDataBean = dataList.get(position);
        final PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        boolean isMe = PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(socketUserBean.getUserId());
        @Nullable
        PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        for (Object payload : payloads) {
            switch (payload.toString()) {
                case PAYLOAD_UPDATE_VOLUME:
                    if (linkMicItemDataBean != null) {
                        if (linkMicItemDataBean.isMuteAudio()) {
                            holder.plvlsMemberMicIv.setSelected(true);
                            holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_close);
                        } else {
                            holder.plvlsMemberMicIv.setSelected(false);
                            int curVolume = linkMicItemDataBean.getCurVolume();
                            if (intBetween(curVolume, 0, 5) || curVolume == 0) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_open);
                            } else if (intBetween(curVolume, 5, 15)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_10);
                            } else if (intBetween(curVolume, 15, 25)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_20);
                            } else if (intBetween(curVolume, 25, 35)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_30);
                            } else if (intBetween(curVolume, 35, 45)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_40);
                            } else if (intBetween(curVolume, 45, 55)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_50);
                            } else if (intBetween(curVolume, 55, 65)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_60);
                            } else if (intBetween(curVolume, 65, 75)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_70);
                            } else if (intBetween(curVolume, 75, 85)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_80);
                            } else if (intBetween(curVolume, 85, 95)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_90);
                            } else if (intBetween(curVolume, 95, 100)) {
                                holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_streamer_mic_volume_100);
                            }
                        }
                    }
                    break;
                case PAYLOAD_UPDATE_VIDEO_MUTE:
                    if (linkMicItemDataBean != null) {
                        holder.plvlsMemberCamIv.setSelected(linkMicItemDataBean.isMuteVideo());
                        holder.plvlsMemberCamFrontIv.setSelected(holder.plvlsMemberCamIv.isSelected());
                    }
                    break;
                case PAYLOAD_UPDATE_CAMERA_DIRECTION:
                    holder.plvlsMemberCamFrontIv.setTag(memberItemDataBean.isFrontCamera() ? null : "back");
                    break;
                case PAYLOAD_UPDATE_SOCKET_USER_DATA:
                    holder.plvlsMemberNickTv.setText(socketUserBean.getNick());
                    holder.updateShieldView(socketUserBean.isBanned());
                    break;
                case PAYLOAD_UPDATE_LINK_MIC_MEDIA_TYPE:
                    if (holder.plvlsMemberMicIv.getVisibility() == View.VISIBLE && !isMe) {
                        if (isVideoLinkMicType || isGuestUserType(socketUserBean.getUserType())) {
                            holder.plvlsMemberCamIv.setVisibility(View.VISIBLE);
                        } else {
                            holder.plvlsMemberCamIv.setVisibility(View.GONE);
                        }
                    }
                    break;
                case PAYLOAD_UPDATE_LINK_MIC_CONTROL:
                    updateMicControlStatus(holder, position);
                    break;
                case PAYLOAD_UPDATE_PERMISSION_CHANGE:
                    updatePermissionStatus(holder, linkMicItemDataBean);
                    break;
                case PAYLOAD_UPDATE_FIRST_VIEW:
                    updateFirstViewStatus(holder, linkMicItemDataBean);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //更新所有数据
    public void update(List<PLVMemberItemDataBean> dataList) {
        this.srcDataList = dataList;
        this.dataList = new ArrayList<>(dataList);
        notifyDataSetChanged();
    }

    public void setMemberListShow(boolean isMemberListShow) {
        this.isMemberListShow = isMemberListShow;
    }

    //移除指定userId的数据
    public int removeData(String userId) {
        int pos = removeData(userId, dataList);
        removeData(userId, srcDataList);
        return pos;
    }

    private int removeData(String userId, List<PLVMemberItemDataBean> dataBeans) {
        int startIndex = isMemberListShow ? 1 : 0;
        for (int i = startIndex; i < dataBeans.size(); i++) {
            PLVSocketUserBean socketUserBean = dataBeans.get(i).getSocketUserBean();
            if (userId != null && userId.equals(socketUserBean.getUserId())) {
                dataBeans.remove(i);
                return i;
            }
        }
        return -1;
    }

    //更新关闭视频
    public void updateUserMuteVideo(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_VIDEO_MUTE);
    }

    //更新关闭视频
    public void updateUserMuteVideo(String uid) {
        Pair<Integer, PLVMemberItemDataBean> pair = getMemberItemByOnlyLinkMicId(uid);
        if (pair != null) {
            notifyItemChanged(pair.first, PAYLOAD_UPDATE_VIDEO_MUTE);
        }
    }

    //更新连麦列表的音量变化
    public void updateVolumeChanged() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_VOLUME);
    }

    //更新摄像头方向
    public void updateCameraDirection(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_CAMERA_DIRECTION);
    }

    //更新摄像头方向
    public void updateCameraDirection(String uid) {
        Pair<Integer, PLVMemberItemDataBean> pair = getMemberItemByOnlyLinkMicId(uid);
        if (pair != null) {
            notifyItemChanged(pair.first, PAYLOAD_UPDATE_CAMERA_DIRECTION);
        }
    }

    //更新socket用户数据
    public void updateSocketUserData(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_SOCKET_USER_DATA);
    }

    //更新连麦媒体类型
    public void updateLinkMicMediaType(boolean isVideoLinkMicType, boolean isOpenLinkMic) {
        if (this.isVideoLinkMicType != isVideoLinkMicType) {
            this.isVideoLinkMicType = isVideoLinkMicType;
            notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_LINK_MIC_MEDIA_TYPE);
        }
        if (this.isOpenLinkMic != isOpenLinkMic) {
            this.isOpenLinkMic = isOpenLinkMic;
            notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_LINK_MIC_CONTROL);
        }
    }

    public void updatePermissionChange(){
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_PERMISSION_CHANGE);
    }

    public void onFirstViewUpdated() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_FIRST_VIEW);
    }

    //添加用户数据
    public void insertUserData(int pos) {
        notifyItemInserted(pos);
    }

    //移除用户数据
    public void removeUserData(int pos) {
        notifyItemRemoved(pos);
    }

    //设置推流状态
    public void setStreamerStatus(boolean isStartedStatus) {
        this.isStartedStatus = isStartedStatus;
        notifyDataSetChanged();
    }

    //设置view交互事件监听器
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    //设置成员列表布局的状态
    public void setIsFirstOpenMemberLayout() {
        this.isFirstOpenMemberLayout = true;
        notifyDataSetChanged();
    }

    public void setOnlyShowAudioUI(boolean isOnlyAudio) {
        this.isOnlyShowAudioUI = isOnlyAudio;
        notifyDataSetChanged();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表item绑定">
    private void updateMicControlStatus(MemberViewHolder holder, int position) {
        PLVMemberItemDataBean memberItemDataBean = dataList.get(position);
        final PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        boolean isMe = PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(socketUserBean.getUserId());
        @Nullable
        PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(PLVSocketWrapper.getInstance().getLoginVO().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        final boolean canInviteLinkMicOld = !isNewLinkMicStrategy
                && isChannelAllowInviteLinkMic
                && isStartedStatus
                && isOpenLinkMic
                && PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_INVITE_LINK_MIC);
        final boolean canInviteLinkMicNew = isNewLinkMicStrategy
                && isChannelAllowInviteLinkMic
                && isStartedStatus
                && PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_INVITE_LINK_MIC);
        final boolean canInviteLinkMic = canInviteLinkMicNew || canInviteLinkMicOld;
        if (isMe) {
            holder.plvlsMemberSplitView.setVisibility(View.GONE);
            holder.plvlsMemberMicIv.setVisibility(View.VISIBLE);
            holder.plvlsMemberCamIv.setVisibility(View.VISIBLE);
            holder.plvlsMemberCamFrontIv.setVisibility(View.VISIBLE);
            holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
            holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
        } else {
            holder.plvlsMemberSplitView.setVisibility(View.VISIBLE);
            holder.plvlsMemberMicIv.setVisibility(View.GONE);
            holder.plvlsMemberCamIv.setVisibility(View.GONE);
            holder.plvlsMemberCamFrontIv.setVisibility(View.GONE);
            holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
            holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
        }
        if (linkMicItemDataBean != null) {
            holder.plvlsMemberMicIv.setImageResource(R.drawable.plvls_member_mic_iv_selector);
            holder.plvlsMemberMicIv.setSelected(linkMicItemDataBean.isMuteAudio());
            holder.plvlsMemberCamIv.setSelected(linkMicItemDataBean.isMuteVideo());
            holder.plvlsMemberCamFrontIv.setSelected(holder.plvlsMemberCamIv.isSelected());
        }
        holder.plvlsMemberCamFrontIv.setTag(memberItemDataBean.isFrontCamera() ? null : "back");
        //设置连麦控制按钮状态
        if (!isMe) {
            if (!isStartedStatus || isGuest) {
                holder.plvlsMemberMicIv.setVisibility(View.GONE);
                holder.plvlsMemberCamIv.setVisibility(View.GONE);
                holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
                holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
            } else {
                if (memberItemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.JOINING) {
                    holder.plvlsMemberMicIv.setVisibility(View.GONE);
                    holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.VISIBLE);
                } else if (memberItemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.RTC_JOIN) {
                    holder.plvlsMemberMicIv.setVisibility(View.VISIBLE);
                    if (isVideoLinkMicType || isGuestUserType(socketUserBean.getUserType())) {
                        holder.plvlsMemberCamIv.setVisibility(View.VISIBLE);
                    } else {
                        holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    }
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.VISIBLE);
                    holder.plvlsMemberLinkmicControlIv.stopAnimation();
                    holder.plvlsMemberLinkmicControlIv.setImageResource(R.drawable.plvls_member_hangup_linkmic);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                } else if (memberItemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION) {
                    holder.plvlsMemberMicIv.setVisibility(View.GONE);
                    holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.VISIBLE);
                    loadFromAssets(holder.plvlsMemberLinkmicControlIv, LINK_MIC_INVITATION_DRAWABLE_FILE_NAME);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                } else if (memberItemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.JOIN
                        || memberItemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP
                        || isViewerUserType(socketUserBean.getUserType())
                        || isGuestUserType(socketUserBean.getUserType())
                        || canInviteLinkMic) {
                    holder.plvlsMemberMicIv.setVisibility(View.GONE);
                    holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.VISIBLE);
                    holder.plvlsMemberLinkmicControlIv.stopAnimation();
                    holder.plvlsMemberLinkmicControlIv.setImageResource(R.drawable.plvls_member_join_response);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                } else {
                    holder.plvlsMemberMicIv.setVisibility(View.GONE);
                    holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                }

                final boolean isMyselfTeacher = PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER);
                final boolean isHandUp = memberItemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP;
                holder.memberLinkmicHandUpIv.setVisibility(isMyselfTeacher && isHandUp ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void updatePermissionStatus(MemberViewHolder holder, PLVLinkMicItemDataBean linkMicItemDataBean) {
        final boolean canControlSpeaker = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_MEMBER_CONTROL_SPEAKER_PERMISSION);
        final boolean canTransferSpeaker = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_MEMBER_TRANSFER_SPEAKER_PERMISSION);
        if (!canControlSpeaker && !canTransferSpeaker) {
            holder.plvlsMemberGrantSpeakerIv.setVisibility(View.GONE);
            return;
        }
        if (linkMicItemDataBean != null
                && linkMicItemDataBean.isGuest()
                && linkMicItemDataBean.isRtcJoinStatus()) {
            if (canControlSpeaker || allowGuestTransferSpeaker) {
                holder.plvlsMemberGrantSpeakerIv.setVisibility(View.VISIBLE);
            } else {
                holder.plvlsMemberGrantSpeakerIv.setVisibility(View.GONE);
            }
        } else {
            holder.plvlsMemberGrantSpeakerIv.setVisibility(View.GONE);
        }
        if (linkMicItemDataBean != null) {
            holder.plvlsMemberGrantSpeakerIv.setSelected(linkMicItemDataBean.isHasSpeaker());
        }
    }

    private void updateFirstViewStatus(MemberViewHolder holder, PLVLinkMicItemDataBean linkMicItemDataBean) {
        final boolean canControlFirstView = PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER);
        if (!canControlFirstView || linkMicItemDataBean == null) {
            holder.memberSetFirstViewIv.setVisibility(View.GONE);
            return;
        }

        final boolean canSetToFirstView = listOf(
                PLVSocketUserConstant.USERTYPE_GUEST,
                PLVSocketUserConstant.USERTYPE_SLICE,
                PLVSocketUserConstant.USERTYPE_STUDENT
        ).contains(linkMicItemDataBean.getUserType());
        if (canSetToFirstView && linkMicItemDataBean.isRtcJoinStatus()) {
            holder.memberSetFirstViewIv.setVisibility(View.VISIBLE);
            holder.memberSetFirstViewIv.setSelected(linkMicItemDataBean.isFirstScreen());
        } else {
            holder.memberSetFirstViewIv.setVisibility(View.GONE);
            holder.memberSetFirstViewIv.setSelected(false);
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    //判断value是否在左开右闭区间：(left, right]
    private boolean intBetween(int value, int left, int right) {
        return value > left && value <= right;
    }

    private boolean isGuestUserType(String userType) {
        return PLVSocketUserConstant.USERTYPE_GUEST.equals(userType);
    }

    private boolean isViewerUserType(String userType) {
        return PLVSocketUserConstant.USERTYPE_VIEWER.equals(userType);
    }

    private Pair<Integer, PLVMemberItemDataBean> getMemberItemByOnlyLinkMicId(String linkMicId) {
        if (dataList == null) {
            return null;
        }
        for (int i = 0; i < dataList.size(); i++) {
            PLVMemberItemDataBean memberItemDataBean = dataList.get(i);
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            if (linkMicItemDataBean != null) {
                String linkMicIdForIndex = linkMicItemDataBean.getLinkMicId();
                if (linkMicId != null && linkMicId.equals(linkMicIdForIndex)) {
                    return new Pair<>(i, memberItemDataBean);
                }
            }
        }
        return null;
    }

    private PLVMemberItemDataBean getMemberItemByPosition(int pos) {
        if (pos < 0 || pos >= dataList.size()) {
            return null;
        }
        return dataList.get(pos);
    }

    private int getPositionFromMemberList(int pos) {
        if (!isMemberListShow) {
            if (onViewActionListener != null) {
                pos = onViewActionListener.getPosition(getMemberItemByPosition(pos));
            }
        }
        return pos;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - ViewHolder">
    public class MemberViewHolder extends RecyclerView.ViewHolder {
        private View plvlsMemberSplitView;
        private PLVSwipeMenu plvlsMemberSwipeMenu;
        private CircleImageView plvlsMemberAvatarIv;
        private TextView plvlsMemberUserTypeTv;
        private TextView plvlsMemberNickTv;
        private TextView plvlsMemberBanTv;
        private ImageView plvlsMemberMicIv;
        private ImageView plvlsMemberCamIv;
        private ImageView plvlsMemberCamFrontIv;
        private ImageView memberLinkmicHandUpIv;
        private SVGAImageView plvlsMemberLinkmicControlIv;
        private ImageView plvlsMemberLinkmicConnectingIv;
        private ImageView plvlsMemberGrantSpeakerIv;
        private ImageView memberSetFirstViewIv;
        private TextView plvlsMemberDoBanTv;
        private TextView plvlsMemberKickTv;
        private TextView plvlsMemberBanConfirmTv;
        private TextView plvlsMemberKickConfirmTv;
        private long lastTimeClickFrontCameraControl = 0;

        @Nullable
        private PLVMemberItemDataBean currentMemberItemDataBean;

        public MemberViewHolder(View itemView) {
            super(itemView);
            plvlsMemberSplitView = findViewById(R.id.plvls_member_split_view);
            plvlsMemberSwipeMenu = findViewById(R.id.plvls_member_swipe_menu);
            plvlsMemberAvatarIv = findViewById(R.id.plvls_member_avatar_iv);
            plvlsMemberUserTypeTv = findViewById(R.id.plvls_member_user_type_tv);
            plvlsMemberNickTv = findViewById(R.id.plvls_member_nick_tv);
            plvlsMemberBanTv = findViewById(R.id.plvls_member_ban_tv);
            plvlsMemberMicIv = findViewById(R.id.plvls_member_mic_iv);
            plvlsMemberCamIv = findViewById(R.id.plvls_member_cam_iv);
            plvlsMemberCamFrontIv = findViewById(R.id.plvls_member_cam_front_iv);
            memberLinkmicHandUpIv = findViewById(R.id.plvls_member_linkmic_hand_up_iv);
            plvlsMemberLinkmicControlIv = findViewById(R.id.plvls_member_linkmic_control_iv);
            plvlsMemberLinkmicConnectingIv = findViewById(R.id.plvls_member_linkmic_connecting_iv);
            plvlsMemberDoBanTv = findViewById(R.id.plvls_member_do_ban_tv);
            plvlsMemberKickTv = findViewById(R.id.plvls_member_kick_tv);
            plvlsMemberBanConfirmTv = findViewById(R.id.plvls_member_ban_confirm_tv);
            plvlsMemberKickConfirmTv = findViewById(R.id.plvls_member_kick_confirm_tv);
            plvlsMemberGrantSpeakerIv = findViewById(R.id.plvls_member_grant_speaker_iv);
            memberSetFirstViewIv = findViewById(R.id.plvls_member_set_first_view_iv);

            AnimationDrawable animationDrawable = (AnimationDrawable) plvlsMemberLinkmicConnectingIv.getDrawable();
            animationDrawable.start();

            plvlsMemberLinkmicControlIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    int memberListPos = getPositionFromMemberList(pos);
                    if (pos < 0 || memberListPos < 0 || onViewActionListener == null || currentMemberItemDataBean == null) {
                        return;
                    }
                    final boolean isGuest = currentMemberItemDataBean.getSocketUserBean().isGuest();
                    final boolean isViewer = isViewerUserType(currentMemberItemDataBean.getSocketUserBean().getUserType());
                    final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(PLVSocketWrapper.getInstance().getLoginVO().getChannelId())
                            .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
                    final boolean canInviteLinkMicOld = !isNewLinkMicStrategy
                            && PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_INVITE_LINK_MIC)
                            && (isOpenLinkMic || isGuest || isViewer)
                            && (isChannelAllowInviteLinkMic || isGuest || isViewer);
                    final boolean canInviteLinkMicNew = isNewLinkMicStrategy
                            && PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_INVITE_LINK_MIC)
                            && (isChannelAllowInviteLinkMic || isGuest || isViewer);
                    final boolean canInviteLinkMic = canInviteLinkMicNew || canInviteLinkMicOld;
                    final boolean notNeedAnswer = !isChannelAllowInviteLinkMic && (isGuest || isViewer);
                    switch (currentMemberItemDataBean.getLinkMicStatus()) {
                        case WAIT_ACCEPT_HAND_UP:
                            onViewActionListener.onControlUserLinkMic(memberListPos, PLVStreamerControlLinkMicAction.acceptRequest());
                            break;
                        case RTC_JOIN:
                            onViewActionListener.onControlUserLinkMic(memberListPos, PLVStreamerControlLinkMicAction.hangUp());
                            PLVToast.Builder.context(v.getContext())
                                    .setText(PLVAppUtils.formatString(R.string.plv_linkmic_hangup_user, dataList.get(pos).getSocketUserBean().getNick()))
                                    .build()
                                    .show();
                            break;
                        default:
                            if (canInviteLinkMic) {
                                onViewActionListener.onControlUserLinkMic(memberListPos, PLVStreamerControlLinkMicAction.sendInvitation(!notNeedAnswer));
                            }
                            break;
                    }
                }
            });

            plvlsMemberSwipeMenu.setOnShowRightChangedListener(new PLVSwipeMenu.OnShowRightChangedListener() {
                @Override
                public void onChanged(boolean haveShowRight) {
                    if (!haveShowRight) {
                        plvlsMemberDoBanTv.setVisibility(View.VISIBLE);
                        plvlsMemberKickTv.setVisibility(View.VISIBLE);
                        plvlsMemberKickConfirmTv.setVisibility(View.GONE);
                        plvlsMemberBanConfirmTv.setVisibility(View.GONE);
                    }
                }
            });

            plvlsMemberMicIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int memberListPos = getPositionFromMemberList(getAdapterPosition());
                    if (memberListPos < 0) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onMicControl(memberListPos, !v.isSelected());
                    }
                }
            });
            plvlsMemberCamIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int memberListPos = getPositionFromMemberList(getAdapterPosition());
                    if (memberListPos < 0) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onCameraControl(memberListPos, !v.isSelected());
                    }
                }
            });
            plvlsMemberCamFrontIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int memberListPos = getPositionFromMemberList(getAdapterPosition());
                    if (memberListPos < 0) {
                        return;
                    }
                    if (!v.isSelected()) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastTimeClickFrontCameraControl > 1000) {
                            if (onViewActionListener != null) {
                                onViewActionListener.onFrontCameraControl(memberListPos, v.getTag() != null);
                            }
                            lastTimeClickFrontCameraControl = currentTime;
                        }
                    }
                }
            });

            plvlsMemberGrantSpeakerIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    int memberListPos = getPositionFromMemberList(pos);
                    if (pos < 0 || memberListPos < 0) {
                        return;
                    }
                    if (PLVDebounceClicker.tryClick(this, 1000)) {
                        if (onViewActionListener != null) {
                            String userId = dataList.get(pos).getSocketUserBean().getUserId();
                            onViewActionListener.onGrantSpeakerPermission(memberListPos, userId, !v.isSelected());
                        }
                    }
                }
            });

            memberSetFirstViewIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    int memberListPos = getPositionFromMemberList(pos);
                    if (pos < 0 || memberListPos < 0) {
                        return;
                    }
                    if (PLVDebounceClicker.tryClick(this, 1000)) {
                        if (onViewActionListener != null) {
                            String userId = dataList.get(pos).getSocketUserBean().getUserId();
                            onViewActionListener.onSetFirstView(memberListPos, userId, !v.isSelected());
                        }
                    }
                }
            });
        }

        private <T extends View> T findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }

        private void updateShieldView(boolean isShield) {
            plvlsMemberBanTv.setVisibility(isShield ? View.VISIBLE : View.GONE);
            plvlsMemberDoBanTv.setText(isShield ? R.string.plv_chat_unban_2 : R.string.plv_chat_ban);
        }

        private void kickConfirmViewChange(boolean isShowConfirm) {
            plvlsMemberDoBanTv.setVisibility(isShowConfirm ? View.GONE : View.VISIBLE);
            plvlsMemberKickTv.setVisibility(isShowConfirm ? View.GONE : View.VISIBLE);
            plvlsMemberBanConfirmTv.setVisibility(View.GONE);
            plvlsMemberKickConfirmTv.setVisibility(isShowConfirm ? View.VISIBLE : View.GONE);
        }

        private void banConfirmViewChange(boolean isShowConfirm) {
            plvlsMemberDoBanTv.setVisibility(isShowConfirm ? View.GONE : View.VISIBLE);
            plvlsMemberKickTv.setVisibility(isShowConfirm ? View.GONE : View.VISIBLE);
            plvlsMemberKickConfirmTv.setVisibility(View.GONE);
            plvlsMemberBanConfirmTv.setVisibility(isShowConfirm ? View.VISIBLE : View.GONE);
        }

        private void showSwipeMenuInFirstOpenMemberLayout() {
            if (!isShowedSwipeMenu && isFirstOpenMemberLayout) {
                isShowedSwipeMenu = true;
                plvlsMemberSwipeMenu.post(new Runnable() {
                    @Override
                    public void run() {
                        plvlsMemberSwipeMenu.openMenus();
                    }
                });
                plvlsMemberSwipeMenu.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        plvlsMemberSwipeMenu.closeMenus();
                    }
                }, 3000);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件">
    public interface OnViewActionListener {
        /**
         * 麦克风控制
         */
        void onMicControl(int position, boolean isMute);

        /**
         * 摄像机控制
         */
        void onCameraControl(int position, boolean isMute);

        /**
         * 前后置摄像机控制
         */
        void onFrontCameraControl(int position, boolean isFront);

        /**
         * 用户加入或离开连麦控制
         */
        void onControlUserLinkMic(int position, PLVStreamerControlLinkMicAction action);

        /**
         * 赋予主讲权限
         */
        void onGrantSpeakerPermission(int position, String userId, boolean isGrant);

        /**
         * 设置第一画面
         */
        void onSetFirstView(int position, String userId, boolean isSetFirstView);

        /**
         * 获取成员列表中的索引
         */
        int getPosition(PLVMemberItemDataBean memberItemDataBean);
    }
    // </editor-fold>
}
