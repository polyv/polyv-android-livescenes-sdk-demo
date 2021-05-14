package com.easefun.polyv.livestreamer.modules.liveroom.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.swipe.PLVSwipeMenu;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livescenes.streamer.transfer.PLVSStreamerInnerDataTransfer;
import com.easefun.polyv.livestreamer.R;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

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

    //dataList
    private List<PLVMemberItemDataBean> dataList;
    //streamerStatus
    private boolean isStartedStatus;
    //初始打开连麦列表，当列表中存在非特殊身份用户时，显示左滑菜单，3秒后恢复原位
    private boolean isShowedSwipeMenu;
    private boolean isFirstOpenMemberLayout;

    //listener
    private OnViewActionListener onViewActionListener;
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
        SpannableStringBuilder nickSpan = new SpannableStringBuilder(socketUserBean.getNick());
        if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(socketUserBean.getUserId())) {
            nickSpan.append("(我)");
        }
        holder.plvlsMemberNickTv.setText(nickSpan);
        //设置麦克风、摄像头的view显示状态
        if (position == 0) {
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
        if (linkMicItemDataBean != null && position > 0) {
            if (!isStartedStatus) {
                holder.plvlsMemberMicIv.setVisibility(View.GONE);
                holder.plvlsMemberCamIv.setVisibility(View.GONE);
                holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
                holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
            } else {
                if (linkMicItemDataBean.isJoiningStatus()) {
                    holder.plvlsMemberMicIv.setVisibility(View.GONE);
                    holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.VISIBLE);
                } else if (linkMicItemDataBean.isRtcJoinStatus()) {
                    holder.plvlsMemberMicIv.setVisibility(View.VISIBLE);
                    holder.plvlsMemberCamIv.setVisibility(View.VISIBLE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.VISIBLE);
                    holder.plvlsMemberLinkmicControlIv.setSelected(true);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                } else if (linkMicItemDataBean.isJoinStatus()
                        || linkMicItemDataBean.isWaitStatus()
                        || isViewerUserType(socketUserBean.getUserType())
                        || isGuestUserType(socketUserBean.getUserType())) {
                    holder.plvlsMemberMicIv.setVisibility(View.GONE);
                    holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.VISIBLE);
                    holder.plvlsMemberLinkmicControlIv.setSelected(false);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                } else {
                    holder.plvlsMemberMicIv.setVisibility(View.GONE);
                    holder.plvlsMemberCamIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                }
                //如果是自动连麦的嘉宾，则把连麦相关的按钮隐藏
                if (PLVSStreamerInnerDataTransfer.getInstance().isAutoLinkToGuest() && isGuestUserType(socketUserBean.getUserType())) {
                    holder.plvlsMemberLinkmicControlIv.setVisibility(View.GONE);
                    holder.plvlsMemberLinkmicConnectingIv.setVisibility(View.GONE);
                }
            }
        }
        //滑动view的设置
        if (position == 0 || isSpecialType) {
            holder.plvlsMemberSwipeMenu.enabledSwipe(false);
        } else {
            holder.plvlsMemberSwipeMenu.enabledSwipe(true);
            holder.showSwipeMenuInFirstOpenMemberLayout();
        }
        //设置禁言状态
        holder.plvlsMemberBanTv.setVisibility(socketUserBean.isBanned() ? View.VISIBLE : View.GONE);
        //禁言操作
        holder.plvlsMemberDoBanTv.setText(socketUserBean.isBanned() ? "解除禁言" : "禁言");
        holder.plvlsMemberDoBanTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isShield = "禁言".equals(holder.plvlsMemberDoBanTv.getText().toString());
                if (isShield) {
                    holder.banConfirmViewChange(true);
                } else {
                    PLVSwipeMenu.closeMenu();
                    int sendResult = PolyvChatroomManager.getInstance().removeShield(socketUserBean.getUserId());
                    if (sendResult > 0) {
                        PLVToast.Builder.context(v.getContext())
                                .setText("解除禁言成功")
                                .build()
                                .show();
                        holder.updateShieldView(false);
                        socketUserBean.setBanned(false);
                    } else {
                        PLVToast.Builder.context(v.getContext())
                                .setText("解除禁言失败" + "(" + sendResult + ")")
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
                            .setText("禁言成功")
                            .build()
                            .show();
                    holder.updateShieldView(true);
                    socketUserBean.setBanned(true);
                } else {
                    PLVToast.Builder.context(v.getContext())
                            .setText("禁言失败" + "(" + sendResult + ")")
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
                            .setText("踢出成功")
                            .build()
                            .show();
                    int position = removeData(socketUserBean.getUserId());
                    if (position >= 0) {
                        notifyItemRemoved(position);
                        PolyvChatroomManager.getInstance().setOnlineCount(PolyvChatroomManager.getInstance().getOnlineCount() - 1);
                    }
                } else {
                    PLVToast.Builder.context(v.getContext())
                            .setText("踢出失败(" + sendResult + ")")
                            .build()
                            .show();
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
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    //移除指定userId的数据
    public int removeData(String userId) {
        for (int i = 1; i < dataList.size(); i++) {
            PLVSocketUserBean socketUserBean = dataList.get(i).getSocketUserBean();
            if (userId != null && userId.equals(socketUserBean.getUserId())) {
                dataList.remove(i);
                return i;
            }
        }
        return -1;
    }

    //更新关闭视频
    public void updateUserMuteVideo(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_VIDEO_MUTE);
    }

    //更新连麦列表的音量变化
    public void updateVolumeChanged() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_VOLUME);
    }

    //更新摄像头方向
    public void updateCameraDirection(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_CAMERA_DIRECTION);
    }

    //更新socket用户数据
    public void updateSocketUserData(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_SOCKET_USER_DATA);
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
    // </editor-fold>

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
        private ImageView plvlsMemberLinkmicControlIv;
        private ImageView plvlsMemberLinkmicConnectingIv;
        private TextView plvlsMemberDoBanTv;
        private TextView plvlsMemberKickTv;
        private TextView plvlsMemberBanConfirmTv;
        private TextView plvlsMemberKickConfirmTv;

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
            plvlsMemberLinkmicControlIv = findViewById(R.id.plvls_member_linkmic_control_iv);
            plvlsMemberLinkmicConnectingIv = findViewById(R.id.plvls_member_linkmic_connecting_iv);
            plvlsMemberDoBanTv = findViewById(R.id.plvls_member_do_ban_tv);
            plvlsMemberKickTv = findViewById(R.id.plvls_member_kick_tv);
            plvlsMemberBanConfirmTv = findViewById(R.id.plvls_member_ban_confirm_tv);
            plvlsMemberKickConfirmTv = findViewById(R.id.plvls_member_kick_confirm_tv);

            AnimationDrawable animationDrawable = (AnimationDrawable) plvlsMemberLinkmicConnectingIv.getDrawable();
            animationDrawable.start();

            plvlsMemberLinkmicControlIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onControlUserLinkMic(pos, !v.isSelected());
                    }
                    if (v.isSelected()) {
                        PLVToast.Builder.context(v.getContext())
                                .setText("已取消" + dataList.get(pos).getSocketUserBean().getNick() + "的连麦")
                                .build()
                                .show();
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
                    int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onMicControl(pos, !v.isSelected());
                    }
                }
            });
            plvlsMemberCamIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onCameraControl(pos, !v.isSelected());
                    }
                }
            });
            plvlsMemberCamFrontIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (!v.isSelected()) {
                        if (onViewActionListener != null) {
                            onViewActionListener.onFrontCameraControl(pos, v.getTag() != null);
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
            plvlsMemberDoBanTv.setText(isShield ? "解除禁言" : "禁言");
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
        void onControlUserLinkMic(int position, boolean isAllowJoin);
    }
    // </editor-fold>
}
