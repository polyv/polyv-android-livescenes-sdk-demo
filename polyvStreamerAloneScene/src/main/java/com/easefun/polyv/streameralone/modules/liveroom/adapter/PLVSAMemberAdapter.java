package com.easefun.polyv.streameralone.modules.liveroom.adapter;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
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
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSAMemberControlWindow;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.user.PLVAuthorizationBean;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 成员列表适配器
 */
public class PLVSAMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final String PAYLOAD_UPDATE_VOLUME = "updateVolume";
    public static final String PAYLOAD_UPDATE_VIDEO_MUTE = "updateVideoMute";
    public static final String PAYLOAD_UPDATE_CAMERA_DIRECTION = "updateCameraDirection";
    public static final String PAYLOAD_UPDATE_SOCKET_USER_DATA = "updateSocketUserData";

    //dataList
    private List<PLVMemberItemDataBean> dataBeanList;
    //streamerStatus
    private boolean isStartedStatus;
    private boolean isGuestAutoLinkMic;
    //是否有全县控制连麦和更多操作
    private boolean isHasPermission = true;

    private PLVSAMemberControlWindow lastShowControlWindow;

    private PLVSocketUserBean speakerUser;

    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAMemberAdapter(boolean isGuestAutoLinkMic) {
        this.isGuestAutoLinkMic = isGuestAutoLinkMic;
        dataBeanList = new ArrayList<>();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现RecyclerView.Adapter定义的方法">
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvsa_live_room_member_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MemberViewHolder) {
            ((MemberViewHolder) holder).processData(dataBeanList.get(position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        if (holder instanceof MemberViewHolder) {
            ((MemberViewHolder) holder).processData(dataBeanList.get(position), payloads);
        }
    }

    @Override
    public int getItemCount() {
        return dataBeanList.size();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //更新所有数据
    public void update(List<PLVMemberItemDataBean> dataList) {
        this.dataBeanList = dataList;
        notifyDataSetChanged();
        checkHideControlWindow();
    }

    //获取数据列表
    public List<PLVMemberItemDataBean> getDataBeanList() {
        return dataBeanList;
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

    //更新用户加入连麦
    public void updateUserJoin(List<PLVLinkMicItemDataBean> dataBeanList) {
        checkUpdateControlWindow(dataBeanList);
    }

    //更新用户离开连麦
    public void updateUserLeave(List<PLVLinkMicItemDataBean> dataBeanList) {
        checkUpdateControlWindow(dataBeanList);
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
        checkUpdateControlWindow(pos);
    }

    //添加用户数据
    public void insertUserData(int pos) {
        notifyItemInserted(pos);
        checkHideControlWindow();
    }

    //移除用户数据
    public void removeUserData(int pos) {
        notifyItemRemoved(pos);
        checkHideControlWindow();
    }

    //获取当前列表中是否有非嘉宾申请连麦
    public boolean hasUserRequestLinkMic() {
        for (PLVMemberItemDataBean memberItemDataBean : dataBeanList) {
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            if (linkMicItemDataBean != null
                    && linkMicItemDataBean.isWaitStatus()
                    && !linkMicItemDataBean.isGuest()) {
                return true;
            }
        }
        return false;
    }

    //设置推流状态
    public void setStreamerStatus(boolean isStartedStatus) {
        this.isStartedStatus = isStartedStatus;
        notifyDataSetChanged();
    }

    //设置有主讲权限的用户
    public void setHasSpeakerUser(PLVSocketUserBean user){
        speakerUser = user;
    }

    //设置view交互事件监听器
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    /**
     * 赋予主讲权限
     */
    public void setTeacherPermission(boolean grant) {
        isHasPermission = grant;
    }

    public void hideControlWindow(){
        if(lastShowControlWindow != null && lastShowControlWindow.isShowing()){
            lastShowControlWindow.dismiss();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更新成员控制弹层">
    private void checkHideControlWindow() {
        if (lastShowControlWindow != null && lastShowControlWindow.isShowing()) {
            for (int i = 0; i < dataBeanList.size(); i++) {
                PLVSocketUserBean socketUserBean = dataBeanList.get(i).getSocketUserBean();
                if (lastShowControlWindow.getUserId() != null && lastShowControlWindow.getUserId().equals(socketUserBean.getUserId())) {
                    if (i != lastShowControlWindow.getPosition()) {
                        lastShowControlWindow.dismiss();
                    }
                    return;
                }
            }
            lastShowControlWindow.dismiss();
        }
    }

    private void checkUpdateControlWindow(int pos) {
        if (lastShowControlWindow != null && lastShowControlWindow.isShowing()) {
            PLVSocketUserBean socketUserBean = dataBeanList.get(pos).getSocketUserBean();
            if (pos == lastShowControlWindow.getPosition()
                    && socketUserBean.getUserId() != null && socketUserBean.getUserId().equals(lastShowControlWindow.getUserId())) {
                @Nullable
                PLVLinkMicItemDataBean linkMicItemDataBean = dataBeanList.get(pos).getLinkMicItemDataBean();
                boolean isRTCJoin = linkMicItemDataBean != null && linkMicItemDataBean.isRtcJoinStatus();
                boolean isOpenCamera = linkMicItemDataBean == null || !linkMicItemDataBean.isMuteVideo();
                boolean isOpenMic = linkMicItemDataBean == null || !linkMicItemDataBean.isMuteAudio();
                boolean isBan = socketUserBean.isBanned();
                boolean isSpecialType = PLVEventHelper.isSpecialType(socketUserBean.getUserType());
                boolean isGuest = linkMicItemDataBean != null && linkMicItemDataBean.isGuest();
                boolean isHasSpeaker = linkMicItemDataBean != null && linkMicItemDataBean.isHasSpeaker();
                if (isSpecialType && !isRTCJoin) {
                    lastShowControlWindow.dismiss();
                } else {
                    lastShowControlWindow.update(isRTCJoin, isOpenCamera, isOpenMic, isBan, isSpecialType,isGuest, isHasSpeaker, speakerUser);
                }
            }
        }
    }

    private void checkUpdateControlWindow(List<PLVLinkMicItemDataBean> dataBeans) {
        if (lastShowControlWindow != null && lastShowControlWindow.isShowing()) {
            for (PLVLinkMicItemDataBean itemDataBean : dataBeans) {
                for (int i = 0; i < dataBeanList.size(); i++) {
                    PLVMemberItemDataBean memberItemDataBean = dataBeanList.get(i);
                    @Nullable
                    PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
                    if (linkMicItemDataBean != null
                            && linkMicItemDataBean.getLinkMicId() != null
                            && linkMicItemDataBean.getLinkMicId().equals(itemDataBean.getLinkMicId())) {
                        checkUpdateControlWindow(i);
                        break;
                    }
                }
            }
        }
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
    private class MemberViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView plvsaMemberAvatarIv;
        private ImageView plvsaMemberBanIv;
        private TextView plvsaMemberUserTypeTv;
        private TextView plvsaMemberNickTv;
        private ImageView plvsaMemberLinkmicControlIv;
        private ImageView plvsaMemberLinkmicConnectingIv;
        private ImageView plvsaMemberMoreIv;
        private PLVSAMemberControlWindow memberControlWindow;
        private ObjectAnimator connectingAnimator;

        public MemberViewHolder(final View itemView) {
            super(itemView);
            plvsaMemberAvatarIv = (CircleImageView) findViewById(R.id.plvsa_member_avatar_iv);
            plvsaMemberBanIv = findViewById(R.id.plvsa_member_ban_iv);
            plvsaMemberUserTypeTv = (TextView) findViewById(R.id.plvsa_member_user_type_tv);
            plvsaMemberNickTv = (TextView) findViewById(R.id.plvsa_member_nick_tv);
            plvsaMemberLinkmicControlIv = (ImageView) findViewById(R.id.plvsa_member_linkmic_control_iv);
            plvsaMemberLinkmicConnectingIv = (ImageView) findViewById(R.id.plvsa_member_linkmic_connecting_iv);
            plvsaMemberMoreIv = (ImageView) findViewById(R.id.plvsa_member_more_iv);

            connectingAnimator = ObjectAnimator.ofFloat(plvsaMemberLinkmicConnectingIv, "alpha", 1f, 0.2f);
            connectingAnimator.setDuration(1000);
            connectingAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            connectingAnimator.setRepeatMode(ObjectAnimator.RESTART);

            plvsaMemberMoreIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showControlWindow();
                }
            });

            plvsaMemberLinkmicControlIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (v.isSelected()) {
                        new PLVSAConfirmDialog(itemView.getContext())
                                .setTitle("确定挂断连麦吗？")
                                .setContentVisibility(View.GONE)
                                .setLeftButtonText("取消")
                                .setRightButtonText("确定")
                                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, View v) {
                                        dialog.dismiss();
                                        if (onViewActionListener != null) {
                                            int pos = getAdapterPosition();
                                            if (pos < 0) {
                                                return;
                                            }
                                            onViewActionListener.onControlUserLinkMic(pos, false);
                                        }
                                    }
                                })
                                .show();
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onControlUserLinkMic(pos, !v.isSelected());
                    }
                }
            });

            initControlWindow();
        }

        private void initControlWindow() {
            memberControlWindow = new PLVSAMemberControlWindow(plvsaMemberMoreIv);
            memberControlWindow.setOnViewActionListener(new PLVSAMemberControlWindow.OnViewActionListener() {

                @Override
                public void onClickCamera(boolean isWillOpen) {
                    final int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onCameraControl(pos, !isWillOpen);
                    }
                }

                @Override
                public void onClickMic(boolean isWillOpen) {
                    final int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onMicControl(pos, !isWillOpen);
                    }
                }

                @Override
                public void onClickKick() {
                    final int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    PLVMemberItemDataBean dataBean = dataBeanList.get(pos);
                    PLVSocketUserBean socketUserBean = dataBean.getSocketUserBean();
                    String toastMsg = "";
                    int sendResult = PolyvChatroomManager.getInstance().kick(socketUserBean.getUserId());
                    if (sendResult > 0) {
                        toastMsg = "移出成功";
                        dataBeanList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                    } else {
                        toastMsg = "移出失败" + "(" + sendResult + ")";
                    }
                    PLVToast.Builder.context(itemView.getContext())
                            .setText(toastMsg)
                            .build()
                            .show();
                }

                @Override
                public void onClickBan(boolean isWillBan) {
                    final int pos = getAdapterPosition();
                    if (pos < 0) {
                        return;
                    }
                    PLVMemberItemDataBean dataBean = dataBeanList.get(pos);
                    PLVSocketUserBean socketUserBean = dataBean.getSocketUserBean();
                    String toastMsg = "";
                    if (!isWillBan) {
                        int sendResult = PolyvChatroomManager.getInstance().removeShield(socketUserBean.getUserId());
                        if (sendResult > 0) {
                            toastMsg = "解除禁言成功";
                        } else {
                            toastMsg = "解除禁言失败" + "(" + sendResult + ")";
                        }
                    } else {
                        int sendResult = PolyvChatroomManager.getInstance().shield(socketUserBean.getUserId());
                        if (sendResult > 0) {
                            toastMsg = "禁言成功";
                        } else {
                            toastMsg = "禁言失败" + "(" + sendResult + ")";
                        }
                    }
                    PLVToast.Builder.context(itemView.getContext())
                            .setText(toastMsg)
                            .build()
                            .show();
                }

                @Override
                public void onClickGrantSpeaker(boolean isGrant) {
                    final int pos = getAdapterPosition();
                    if (pos < 0) {
                        return ;
                    }

                    if(onViewActionListener != null){
                        onViewActionListener.onGrantUserSpeakerPermission(pos, dataBeanList.get(pos).getSocketUserBean(), isGrant);
                    }
                }

                @Override
                public String getNick() {
                    final int pos = getAdapterPosition();
                    if (pos < 0) {
                        return "";
                    }
                    PLVMemberItemDataBean dataBean = dataBeanList.get(pos);
                    return dataBean.getSocketUserBean().getNick();
                }
            });
        }

        private void showControlWindow() {
            final int pos = getAdapterPosition();
            if (pos < 0) {
                return;
            }
            PLVMemberItemDataBean dataBean = dataBeanList.get(pos);
            final PLVSocketUserBean socketUserBean = dataBean.getSocketUserBean();
            @Nullable
            PLVLinkMicItemDataBean linkMicItemDataBean = dataBean.getLinkMicItemDataBean();
            boolean isRTCJoin = linkMicItemDataBean != null && linkMicItemDataBean.isRtcJoinStatus();
            boolean isOpenCamera = linkMicItemDataBean == null || !linkMicItemDataBean.isMuteVideo();
            boolean isOpenMic = linkMicItemDataBean == null || !linkMicItemDataBean.isMuteAudio();
            boolean isBan = socketUserBean.isBanned();
            boolean isSpecialType = PLVEventHelper.isSpecialType(socketUserBean.getUserType());
            boolean isGuest = linkMicItemDataBean != null && linkMicItemDataBean.isGuest();
            boolean isHasSpeaker = linkMicItemDataBean != null && linkMicItemDataBean.isHasSpeaker();
            String userId = socketUserBean.getUserId();
            memberControlWindow.bindData(userId, pos);
            memberControlWindow.show(plvsaMemberMoreIv, isRTCJoin, isOpenCamera, isOpenMic, isBan, isSpecialType,isGuest, isHasSpeaker, speakerUser);
            lastShowControlWindow = memberControlWindow;
        }

        private void processData(PLVMemberItemDataBean dataBean) {
            final PLVSocketUserBean socketUserBean = dataBean.getSocketUserBean();
            @Nullable
            PLVLinkMicItemDataBean linkMicItemDataBean = dataBean.getLinkMicItemDataBean();
            String userType = socketUserBean.getUserType();
            //是否是特殊身份类型
            boolean isSpecialType = PLVEventHelper.isSpecialType(socketUserBean.getUserType());
            boolean isMyself = PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(socketUserBean.getUserId());
            //加载头像
            int defaultAvatar = isSpecialType ? R.drawable.plvsa_member_teacher_missing_face : R.drawable.plvsa_member_student_missing_face;
            PLVImageLoader.getInstance().loadImageNoDiskCache(
                    plvsaMemberAvatarIv.getContext(),
                    socketUserBean.getPic(),
                    defaultAvatar,
                    defaultAvatar,
                    plvsaMemberAvatarIv
            );
            //设置头衔
            String actor = socketUserBean.getActor();
            PLVAuthorizationBean authorizationBean = socketUserBean.getAuthorization();
            if (authorizationBean != null) {
                actor = authorizationBean.getActor();
            }
            plvsaMemberUserTypeTv.setText(actor);
            plvsaMemberUserTypeTv.setVisibility(View.VISIBLE);
            if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
                plvsaMemberUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_teacher_tv_bg_shape);
            } else if (PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(userType)) {
                plvsaMemberUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_assistant_tv_bg_shape);
            } else if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
                plvsaMemberUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_guest_tv_bg_shape);
            } else if (PLVSocketUserConstant.USERTYPE_MANAGER.equals(userType)) {
                plvsaMemberUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_manager_tv_bg_shape);
            } else {
                plvsaMemberUserTypeTv.setVisibility(View.GONE);
            }
            //设置昵称
            SpannableStringBuilder nickSpan = new SpannableStringBuilder(socketUserBean.getNick());
            if (isMyself) {
                nickSpan.append("(我)");
            }
            plvsaMemberNickTv.setText(nickSpan);
            //设置禁言显示状态
            plvsaMemberBanIv.setVisibility(socketUserBean.isBanned() ? View.VISIBLE : View.GONE);
            //设置连麦按钮的状态
            if (isMyself || !isHasPermission) {
                //自己和无主讲权限时，不需要显示
                plvsaMemberLinkmicControlIv.setVisibility(View.GONE);
                plvsaMemberLinkmicConnectingIv.setVisibility(View.GONE);
                connectingAnimator.cancel();
                plvsaMemberMoreIv.setVisibility(View.GONE);
            } else {
                plvsaMemberMoreIv.setVisibility(isSpecialType ? View.GONE : View.VISIBLE);
                if (!isStartedStatus) {
                    plvsaMemberLinkmicControlIv.setVisibility(View.GONE);
                    plvsaMemberLinkmicConnectingIv.setVisibility(View.GONE);
                } else {
                    if (linkMicItemDataBean == null) {
                        plvsaMemberLinkmicControlIv.setVisibility(View.GONE);
                        plvsaMemberLinkmicConnectingIv.setVisibility(View.GONE);
                        connectingAnimator.cancel();
                    } else if (linkMicItemDataBean.isJoiningStatus()) {
                        plvsaMemberLinkmicControlIv.setVisibility(View.GONE);
                        plvsaMemberLinkmicConnectingIv.setVisibility(View.VISIBLE);
                        connectingAnimator.start();
                    } else if (linkMicItemDataBean.isRtcJoinStatus()) {
                        plvsaMemberLinkmicControlIv.setVisibility(View.VISIBLE);
                        plvsaMemberLinkmicControlIv.setSelected(true);
                        plvsaMemberLinkmicConnectingIv.setVisibility(View.GONE);
                        connectingAnimator.cancel();
                        //加入连麦状态把更多按钮设置为可见
                        plvsaMemberMoreIv.setVisibility(View.VISIBLE);
                    } else if (linkMicItemDataBean.isJoinStatus()
                            || linkMicItemDataBean.isWaitStatus()
                            || isViewerUserType(socketUserBean.getUserType())
                            || isGuestUserType(socketUserBean.getUserType())) {
                        plvsaMemberLinkmicControlIv.setVisibility(View.VISIBLE);
                        plvsaMemberLinkmicControlIv.setSelected(false);
                        plvsaMemberLinkmicConnectingIv.setVisibility(View.GONE);
                        connectingAnimator.cancel();
                    } else {
                        plvsaMemberLinkmicControlIv.setVisibility(View.GONE);
                        plvsaMemberLinkmicConnectingIv.setVisibility(View.GONE);
                        connectingAnimator.cancel();
                    }
                    //如果是自动连麦的嘉宾，则把连麦相关的按钮隐藏
                    if (isGuestAutoLinkMic && isGuestUserType(socketUserBean.getUserType())) {
                        plvsaMemberLinkmicControlIv.setVisibility(View.GONE);
                        plvsaMemberLinkmicConnectingIv.setVisibility(View.GONE);
                        connectingAnimator.cancel();
                    }
                }
            }
        }

        private void processData(PLVMemberItemDataBean memberItemDataBean, @NonNull List<Object> payloads) {
            final PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
            @Nullable
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            for (Object payload : payloads) {
                switch (payload.toString()) {
                    case PAYLOAD_UPDATE_VOLUME:
                        break;
                    case PAYLOAD_UPDATE_VIDEO_MUTE:
                        break;
                    case PAYLOAD_UPDATE_CAMERA_DIRECTION:
                        break;
                    case PAYLOAD_UPDATE_SOCKET_USER_DATA:
                        boolean isMyself = PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(socketUserBean.getUserId());
                        //设置昵称
                        SpannableStringBuilder nickSpan = new SpannableStringBuilder(socketUserBean.getNick());
                        if (isMyself) {
                            nickSpan.append("(我)");
                        }
                        plvsaMemberNickTv.setText(nickSpan);
                        //设置禁言显示状态
                        plvsaMemberBanIv.setVisibility(socketUserBean.isBanned() ? View.VISIBLE : View.GONE);
                        break;
                    default:
                        break;
                }
            }
        }

        private <T extends View> T findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
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
         * 用户加入或离开连麦控制
         */
        void onControlUserLinkMic(int position, boolean isAllowJoin);

        /**
         * 授予用户主讲权限
         */
        void onGrantUserSpeakerPermission(int position, PLVSocketUserBean user, boolean isGrant);
    }
    // </editor-fold>
}
