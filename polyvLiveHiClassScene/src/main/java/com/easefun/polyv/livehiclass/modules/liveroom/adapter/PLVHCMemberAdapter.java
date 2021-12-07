package com.easefun.polyv.livehiclass.modules.liveroom.adapter;

import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.net.model.PLVSocketLoginVO;
import com.plv.socket.user.PLVSocketUserBean;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Ack;

/**
 * 成员列表适配器
 */
public class PLVHCMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int VIEW_TYPE_EMPTY = 111;
    private static final String PAYLOAD_UPDATE_SOCKET_USER_DATA = "updateSocketUserData";
    private static final String PAYLOAD_UPDATE_USER_RAISE_HAND = "updateUserRaiseHand";
    private static final String PAYLOAD_UPDATE_USER_HAS_PAINT = "updateUserHasPaint";
    private static final String PAYLOAD_UPDATE_USER_GET_CUP = "updateUserGetCup";
    private static final String PAYLOAD_UPDATE_USER_MUTE_VIDEO = "updateUserMuteVideo";
    private static final String PAYLOAD_UPDATE_USER_MUTE_AUDIO = "updateUserMuteAudio";
    private static final String PAYLOAD_UPDATE_USER_COUNT_CHANGED = "updateUserCountChanged";
    //data
    private List<PLVMemberItemDataBean> dataBeanList;
    private boolean isSimpleLayout = false;
    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCMemberAdapter() {
        dataBeanList = new ArrayList<>();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 实现RecyclerView.Adapter的方法">
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvhc_live_room_member_list_empty_layout, parent, false)) {
            };
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvhc_live_room_member_list_item, parent, false);
        MemberViewHolder memberViewHolder = new MemberViewHolder(itemView);
        return memberViewHolder;
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
        return dataBeanList.size() == 0 ? 1 : dataBeanList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return dataBeanList.size() == 0 ? VIEW_TYPE_EMPTY : super.getItemViewType(position);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void bindData(List<PLVMemberItemDataBean> dataBeanList) {
        this.dataBeanList = dataBeanList;
        notifyDataSetChanged();
    }

    public void updateMemberListData(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_SOCKET_USER_DATA);
    }

    public void updateUserRaiseHand(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_USER_RAISE_HAND);
    }

    public void updateUserGetCup(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_USER_GET_CUP);
    }

    public void updateUserHasPaint(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_USER_HAS_PAINT);
    }

    public void updateUserMuteVideo(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_USER_MUTE_VIDEO);
    }

    public void updateUserMuteAudio(int pos) {
        notifyItemChanged(pos, PAYLOAD_UPDATE_USER_MUTE_AUDIO);
    }

    public void updateUserCountChanged() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_UPDATE_USER_COUNT_CHANGED);
    }

    public void setIsSimpleLayout() {
        this.isSimpleLayout = true;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        onViewActionListener = listener;
    }

    public List<PLVMemberItemDataBean> getDataBeanList() {
        return dataBeanList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - ViewHolder">
    public class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //view
        private ViewGroup plvhcMemberParentLy;
        private TextView plvhcMemberNickTv;
        private ImageView plvhcMemberHandsUpIv;
        private TextView plvhcMemberPublishControlTv;
        private ImageView plvhcMemberLinkmicConnectingIv;
        private ImageView plvhcMemberPaintIv;
        private ImageView plvhcMemberMicIv;
        private ImageView plvhcMemberCameraIv;
        private TextView plvhcMemberCupsTv;
        private ImageView plvhcMemberBanIv;
        private ImageView plvhcMemberKickIv;
        //data
        private PLVSocketUserBean socketUserBean;

        public MemberViewHolder(View itemView) {
            super(itemView);
            plvhcMemberParentLy = findViewById(R.id.plvhc_member_parent_ly);
            plvhcMemberNickTv = findViewById(R.id.plvhc_member_nick_tv);
            plvhcMemberHandsUpIv = findViewById(R.id.plvhc_member_hands_up_iv);
            plvhcMemberPublishControlTv = findViewById(R.id.plvhc_member_publish_control_tv);
            plvhcMemberLinkmicConnectingIv = findViewById(R.id.plvhc_member_linkmic_connecting_iv);
            plvhcMemberPaintIv = findViewById(R.id.plvhc_member_paint_iv);
            plvhcMemberMicIv = findViewById(R.id.plvhc_member_mic_iv);
            plvhcMemberCameraIv = findViewById(R.id.plvhc_member_camera_iv);
            plvhcMemberCupsTv = findViewById(R.id.plvhc_member_cups_tv);
            plvhcMemberBanIv = findViewById(R.id.plvhc_member_ban_iv);
            plvhcMemberKickIv = findViewById(R.id.plvhc_member_kick_iv);

            if (isSimpleLayout) {
                ((ViewGroup) plvhcMemberHandsUpIv.getParent()).setVisibility(View.GONE);
                ((ViewGroup) plvhcMemberCupsTv.getParent()).setVisibility(View.GONE);
                ((ViewGroup) plvhcMemberBanIv.getParent()).setVisibility(View.GONE);
                ((ViewGroup) plvhcMemberKickIv.getParent()).setVisibility(View.GONE);
            }

            AnimationDrawable animationDrawable = (AnimationDrawable) plvhcMemberLinkmicConnectingIv.getDrawable();
            animationDrawable.start();

            plvhcMemberPublishControlTv.setOnClickListener(this);
            plvhcMemberPaintIv.setOnClickListener(this);
            plvhcMemberMicIv.setOnClickListener(this);
            plvhcMemberCameraIv.setOnClickListener(this);
            plvhcMemberBanIv.setOnClickListener(this);
            plvhcMemberKickIv.setOnClickListener(this);
        }

        private void processData(PLVMemberItemDataBean memberItemDataBean) {
            socketUserBean = memberItemDataBean.getSocketUserBean();
            @Nullable
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            //设置parent背景
            plvhcMemberParentLy.setBackground(getAdapterPosition() % 2 != 0 ? itemView.getResources().getDrawable(R.drawable.plvhc_member_list_item_ly_shape) : null);
            //设置昵称
            plvhcMemberNickTv.setText(socketUserBean.getNick());
            if (linkMicItemDataBean != null) {
                //设置上下台状态、及麦克风摄像头状态
                if (linkMicItemDataBean.isJoiningStatus()) {
                    setLinkMicAffectViewEnabled(false);
                    plvhcMemberPublishControlTv.setVisibility(View.GONE);
                    plvhcMemberLinkmicConnectingIv.setVisibility(View.VISIBLE);
                } else if (linkMicItemDataBean.isRtcJoinStatus()) {
                    setLinkMicAffectViewEnabled(true);
                    plvhcMemberPublishControlTv.setVisibility(View.VISIBLE);
                    plvhcMemberPublishControlTv.setSelected(true);
                    plvhcMemberPublishControlTv.setText("下台");
                    plvhcMemberLinkmicConnectingIv.setVisibility(View.GONE);
                } else if (linkMicItemDataBean.isJoinStatus()
                        || linkMicItemDataBean.isWaitStatus()
                        || linkMicItemDataBean.isIdleStatus()) {
                    setLinkMicAffectViewEnabled(false);
                    plvhcMemberPublishControlTv.setVisibility(View.VISIBLE);
                    plvhcMemberPublishControlTv.setSelected(false);
                    plvhcMemberPublishControlTv.setText("上台");
                    plvhcMemberLinkmicConnectingIv.setVisibility(View.GONE);
                }
                //设置麦克风摄像头的开启关闭状态
                plvhcMemberMicIv.setSelected(linkMicItemDataBean.isMuteAudio());
                plvhcMemberCameraIv.setSelected(linkMicItemDataBean.isMuteVideo());
                //设置奖杯数
                plvhcMemberCupsTv.setText(String.valueOf(linkMicItemDataBean.getCupNum()));
                //设置画笔
                plvhcMemberPaintIv.setSelected(linkMicItemDataBean.isHasPaint());
                //设置举手状态
                plvhcMemberHandsUpIv.setVisibility(linkMicItemDataBean.isRaiseHand() ? View.VISIBLE : View.INVISIBLE);
            } else {
                setLinkMicAffectViewEnabled(false);
                plvhcMemberPublishControlTv.setVisibility(View.VISIBLE);
                plvhcMemberPublishControlTv.setSelected(false);
                plvhcMemberPublishControlTv.setText("上台");
                plvhcMemberLinkmicConnectingIv.setVisibility(View.GONE);
                plvhcMemberCupsTv.setText("0");
                plvhcMemberHandsUpIv.setVisibility(View.INVISIBLE);
            }
            //设置禁言状态
            plvhcMemberBanIv.setSelected(socketUserBean.isBanned());
        }

        private void setLinkMicAffectViewEnabled(boolean enabled) {
            int micDrawableId = enabled ? R.drawable.plvhc_member_list_mic_selector
                    : R.drawable.plvhc_member_mic_disable;
            int cameraDrawableId = enabled ? R.drawable.plvhc_member_list_camera_selector
                    : R.drawable.plvhc_member_camera_disable;
            int paintDrawableId = enabled ? R.drawable.plvhc_member_list_paint_selector
                    : R.drawable.plvhc_member_list_paint_gray;

            plvhcMemberMicIv.setImageResource(micDrawableId);
            plvhcMemberMicIv.setTag(micDrawableId);
            plvhcMemberCameraIv.setImageResource(cameraDrawableId);
            plvhcMemberCameraIv.setTag(cameraDrawableId);
            plvhcMemberPaintIv.setImageResource(paintDrawableId);
            plvhcMemberPaintIv.setTag(paintDrawableId);
        }

        private boolean checkLinkMicAffectViewEnabled(View view) {
            boolean enabled = true;
            if (view.getId() == R.id.plvhc_member_paint_iv) {
                if (view.getTag() instanceof Integer && ((int) view.getTag()) == R.drawable.plvhc_member_list_paint_gray) {
                    enabled = false;
                }
            } else if (view.getId() == R.id.plvhc_member_mic_iv) {
                if (view.getTag() instanceof Integer && ((int) view.getTag()) == R.drawable.plvhc_member_mic_disable) {
                    enabled = false;
                }
            } else if (view.getId() == R.id.plvhc_member_camera_iv) {
                if (view.getTag() instanceof Integer && ((int) view.getTag()) == R.drawable.plvhc_member_camera_disable) {
                    enabled = false;
                }
            }
            if (!enabled) {
                PLVHCToast.Builder.context(view.getContext())
                        .setText("该学生还没未上台")
                        .build()
                        .show();
            }
            return enabled;
        }

        private void processData(PLVMemberItemDataBean memberItemDataBean, @NonNull List<Object> payloads) {
            PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
            @Nullable
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            for (Object payload : payloads) {
                switch (payload.toString()) {
                    case PAYLOAD_UPDATE_SOCKET_USER_DATA:
                        plvhcMemberNickTv.setText(socketUserBean.getNick());
                        plvhcMemberBanIv.setSelected(socketUserBean.isBanned());
                        break;
                    case PAYLOAD_UPDATE_USER_RAISE_HAND:
                        if (linkMicItemDataBean != null) {
                            plvhcMemberHandsUpIv.setVisibility(linkMicItemDataBean.isRaiseHand() ? View.VISIBLE : View.INVISIBLE);
                        }
                        break;
                    case PAYLOAD_UPDATE_USER_HAS_PAINT:
                        if (linkMicItemDataBean != null) {
                            plvhcMemberPaintIv.setSelected(linkMicItemDataBean.isHasPaint());
                        }
                        break;
                    case PAYLOAD_UPDATE_USER_GET_CUP:
                        if (linkMicItemDataBean != null) {
                            plvhcMemberCupsTv.setText(String.valueOf(linkMicItemDataBean.getCupNum()));
                        }
                        break;
                    case PAYLOAD_UPDATE_USER_MUTE_VIDEO:
                        if (linkMicItemDataBean != null) {
                            plvhcMemberCameraIv.setSelected(linkMicItemDataBean.isMuteVideo());
                        }
                        break;
                    case PAYLOAD_UPDATE_USER_MUTE_AUDIO:
                        if (linkMicItemDataBean != null) {
                            plvhcMemberMicIv.setSelected(linkMicItemDataBean.isMuteAudio());
                        }
                        break;
                    case PAYLOAD_UPDATE_USER_COUNT_CHANGED:
                        plvhcMemberParentLy.setBackground(getAdapterPosition() % 2 != 0 ? itemView.getResources().getDrawable(R.drawable.plvhc_member_list_item_ly_shape) : null);
                        break;
                    default:
                        break;
                }
            }
        }

        private boolean isMyUserId() {
            PLVSocketLoginVO socketLoginVO = PLVSocketWrapper.getInstance().getLoginVO();
            if (socketLoginVO == null || socketLoginVO.getUserId() == null) {
                return false;
            }
            return socketUserBean != null && socketLoginVO.getUserId().equals(socketUserBean.getUserId());
        }

        private <T extends View> T findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }

        private void handleBanAction(View v) {
            String toastMsg = "";
            int toastDrawableId = 0;
            if (v.isSelected()) {
                int sendResult = PLVChatroomManager.getInstance().removeShield(socketUserBean.getUserId());
                if (sendResult > 0) {
                    toastMsg = "已对该学生关闭禁言";
                    toastDrawableId = R.drawable.plvhc_member_ban;
                    v.setSelected(!v.isSelected());
                } else {
                    toastMsg = "解除禁言失败" + "(" + sendResult + ")";
                }
            } else {
                int sendResult = PLVChatroomManager.getInstance().shield(socketUserBean.getUserId());
                if (sendResult > 0) {
                    toastMsg = "已对该学生开启禁言";
                    toastDrawableId = R.drawable.plvhc_member_ban_sel;
                    v.setSelected(!v.isSelected());
                } else {
                    toastMsg = "禁言失败" + "(" + sendResult + ")";
                }
            }
            PLVHCToast.Builder.context(v.getContext())
                    .setDrawable(toastDrawableId)
                    .setText(toastMsg)
                    .build()
                    .show();
        }

        private void handleKickAction(View v) {
            String toastMsg = "";
            int sendResult = PLVChatroomManager.getInstance().kick(socketUserBean.getUserId());
            if (sendResult > 0) {
                toastMsg = "已将学生移出教室";
                dataBeanList.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());
                updateUserCountChanged();
                if (onViewActionListener != null) {
                    onViewActionListener.onRemoveOnlineUserAction();
                }
            } else {
                toastMsg = "移出失败" + "(" + sendResult + ")";
            }
            PLVHCToast.Builder.context(v.getContext())
                    .setDrawable(R.drawable.plvhc_member_kick)
                    .setText(toastMsg)
                    .build()
                    .show();
        }

        @Override
        public void onClick(final View v) {
            int id = v.getId();
            if (id == R.id.plvhc_member_publish_control_tv) {
                boolean isNoSupportLinkMic = onViewActionListener != null && onViewActionListener.getLimitLinkNumber() == 0;
                if (!v.isSelected() && isNoSupportLinkMic) {
                    PLVHCToast.Builder.context(v.getContext())
                            .setText("该直播间尚不支持上台")
                            .build()
                            .show();
                    return;
                }
                if (onViewActionListener != null) {
                    onViewActionListener.onPublishControlAction(getAdapterPosition(), !v.isSelected());
                }
            } else if (id == R.id.plvhc_member_paint_iv) {
                if (!checkLinkMicAffectViewEnabled(v)) {
                    return;
                }
                if (onViewActionListener != null) {
                    final boolean isAllow = !v.isSelected();
                    onViewActionListener.onPaintControlAction(getAdapterPosition(), isAllow, new Ack() {
                        @Override
                        public void call(Object... args) {
                            if (isMyUserId()) {
                                return;
                            }
                            PLVHCToast.Builder.context(v.getContext())
                                    .setDrawable(isAllow ? R.drawable.plvhc_member_list_paint : R.drawable.plvhc_member_list_paint_disable)
                                    .setText(isAllow ? "已授权该学生画笔" : "已收回该学生画笔")
                                    .build()
                                    .show();
                        }
                    });
                }
            } else if (id == R.id.plvhc_member_mic_iv) {
                if (!checkLinkMicAffectViewEnabled(v)) {
                    return;
                }
                if (onViewActionListener != null) {
                    final boolean isMute = !v.isSelected();
                    onViewActionListener.onMicControlAction(getAdapterPosition(), isMute, new Ack() {
                        @Override
                        public void call(Object... args) {
                            if (isMyUserId()) {
                                return;
                            }
                            PLVHCToast.Builder.context(v.getContext())
                                    .setDrawable(isMute ? R.drawable.plvhc_member_mic_sel : R.drawable.plvhc_member_mic)
                                    .setText(isMute ? "已关闭该学生麦克风" : "已开启该学生麦克风")
                                    .build()
                                    .show();
                        }
                    });
                }
            } else if (id == R.id.plvhc_member_camera_iv) {
                if (!checkLinkMicAffectViewEnabled(v)) {
                    return;
                }
                if (onViewActionListener != null) {
                    final boolean isMute = !v.isSelected();
                    onViewActionListener.onCameraControlAction(getAdapterPosition(), isMute, new Ack() {
                        @Override
                        public void call(Object... args) {
                            if (isMyUserId()) {
                                return;
                            }
                            PLVHCToast.Builder.context(v.getContext())
                                    .setDrawable(isMute ? R.drawable.plvhc_member_camera_sel : R.drawable.plvhc_member_camera)
                                    .setText(isMute ? "已关闭该学生摄像头" : "已开启该学生摄像头")
                                    .build()
                                    .show();
                        }
                    });
                }
            } else if (id == R.id.plvhc_member_ban_iv) {
                handleBanAction(v);
            } else if (id == R.id.plvhc_member_kick_iv) {
                new PLVHCConfirmDialog(v.getContext())
                        .setTitle("移出学生")
                        .setContent("要将学生移出教室吗？")
                        .setLeftButtonText("取消")
                        .setRightButtonText("确定")
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v2) {
                                dialog.dismiss();
                                handleKickAction(v);
                            }
                        })
                        .show();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        /**
         * 上下台控制
         *
         * @param isAllow true：上台，false：下台
         */
        void onPublishControlAction(int position, boolean isAllow);

        /**
         * 画笔授权
         *
         * @param isAllow true：允许，false：撤销
         */
        void onPaintControlAction(int position, boolean isAllow, Ack ack);

        /**
         * 麦克风控制
         *
         * @param isMute true：禁用，false：开启
         */
        void onMicControlAction(int position, boolean isMute, Ack ack);

        /**
         * 摄像头控制
         *
         * @param isMute true：禁用，false：开启
         */
        void onCameraControlAction(int position, boolean isMute, Ack ack);

        /**
         * 踢出在线列表的用户
         */
        void onRemoveOnlineUserAction();

        /**
         * 获取限制的连麦人数
         */
        int getLimitLinkNumber();
    }
    // </editor-fold>
}
