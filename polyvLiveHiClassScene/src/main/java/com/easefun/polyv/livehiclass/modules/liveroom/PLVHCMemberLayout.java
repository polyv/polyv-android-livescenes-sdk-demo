package com.easefun.polyv.livehiclass.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleMemberList;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.view.PLVAbsMultiRoleLinkMicView;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVOutsideTouchableLayout;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.liveroom.adapter.PLVHCKickListAdapter;
import com.easefun.polyv.livehiclass.modules.liveroom.adapter.PLVHCMemberAdapter;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;

import io.socket.client.Ack;

/**
 * 成员列表布局
 */
public class PLVHCMemberLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private TextView plvhcMemberListMuteAllAudioTv;
    private TextView plvhcMemberListCloseAllLinkmicTv;
    private LinearLayout plvhcMemberListLeftLy;
    private LinearLayout plvhcMemberListRightLy;
    private TextView plvhcMemberListDataTypeTv;
    private ImageView plvhcMemberListPackUpIv;
    private TextView plvhcMemberListHandsUpTv;
    private TextView plvhcMemberListLinkmicTv;
    private TextView plvhcMemberListPaintTv;
    private TextView plvhcMemberListMicTv;
    private TextView plvhcMemberListCameraTv;
    private TextView plvhcMemberListCupsTv;
    private TextView plvhcMemberListBanTv;
    private TextView plvhcMemberListKickTv;
    private RecyclerView plvhcMemberListRv;
    private RecyclerView plvhcMemberListKickRv;
    //presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    private IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter linkMicPresenter;
    //window
    private PLVHCMemberTypeWindow memberTypeWindow;
    //adapter
    private PLVHCMemberAdapter memberAdapter;
    private PLVHCKickListAdapter kickListAdapter;
    //container
    private PLVOutsideTouchableLayout container;
    //listener
    private OnViewActionListener onViewActionListener;
    //data
    private int onlineCount;
    private int kickCount;
    private boolean isSelectOnlineList = true;
    private boolean isSimpleLayout = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCMemberLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_member_layout, this, true);

        //findView
        plvhcMemberListMuteAllAudioTv = findViewById(R.id.plvhc_member_list_mute_all_audio_tv);
        plvhcMemberListCloseAllLinkmicTv = findViewById(R.id.plvhc_member_list_close_all_linkmic_tv);
        plvhcMemberListLeftLy = findViewById(R.id.plvhc_member_list_left_ly);
        plvhcMemberListRightLy = findViewById(R.id.plvhc_member_list_right_ly);
        plvhcMemberListDataTypeTv = findViewById(R.id.plvhc_member_list_data_type_tv);
        plvhcMemberListPackUpIv = findViewById(R.id.plvhc_member_list_pack_up_iv);
        plvhcMemberListHandsUpTv = findViewById(R.id.plvhc_member_list_hands_up_tv);
        plvhcMemberListLinkmicTv = findViewById(R.id.plvhc_member_list_linkmic_tv);
        plvhcMemberListPaintTv = findViewById(R.id.plvhc_member_list_paint_tv);
        plvhcMemberListMicTv = findViewById(R.id.plvhc_member_list_mic_tv);
        plvhcMemberListCameraTv = findViewById(R.id.plvhc_member_list_camera_tv);
        plvhcMemberListCupsTv = findViewById(R.id.plvhc_member_list_cups_tv);
        plvhcMemberListBanTv = findViewById(R.id.plvhc_member_list_ban_tv);
        plvhcMemberListKickTv = findViewById(R.id.plvhc_member_list_kick_tv);
        plvhcMemberListRv = findViewById(R.id.plvhc_member_list_rv);
        plvhcMemberListKickRv = findViewById(R.id.plvhc_member_list_kick_rv);

        //setListener
        plvhcMemberListDataTypeTv.setOnClickListener(this);
        plvhcMemberListPackUpIv.setOnClickListener(this);
        plvhcMemberListMuteAllAudioTv.setOnClickListener(this);
        plvhcMemberListCloseAllLinkmicTv.setOnClickListener(this);

        //init window
        initMemberTypeWindow();
        //初始化成员列表
        initOnlineList();
        //初始化踢出列表
        initKickList();
    }

    private void initOnlineList() {
        plvhcMemberListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        plvhcMemberListRv.setHasFixedSize(true);
        memberAdapter = new PLVHCMemberAdapter();
        memberAdapter.setOnViewActionListener(new PLVHCMemberAdapter.OnViewActionListener() {
            @Override
            public void onPublishControlAction(int position, boolean isAllow) {
                if (linkMicPresenter != null) {
                    linkMicPresenter.controlUserLinkMic(position, isAllow);
                }
            }

            @Override
            public void onPaintControlAction(int position, boolean isAllow, Ack ack) {
                if (linkMicPresenter != null) {
                    linkMicPresenter.setPaintPermission(position, isAllow, ack);
                }
            }

            @Override
            public void onMicControlAction(int position, boolean isMute, Ack ack) {
                if (linkMicPresenter != null) {
                    linkMicPresenter.setMediaPermission(position, false, isMute, ack);
                }
            }

            @Override
            public void onCameraControlAction(int position, boolean isMute, Ack ack) {
                if (linkMicPresenter != null) {
                    linkMicPresenter.setMediaPermission(position, true, isMute, ack);
                }
            }

            @Override
            public void onRemoveOnlineUserAction() {
                PLVChatroomManager.getInstance().setOnlineCount(PLVChatroomManager.getInstance().getOnlineCount() - 1);
                updateKickCount(++kickCount);
            }

            @Override
            public int getLimitLinkNumber() {
                if (linkMicPresenter != null) {
                    return linkMicPresenter.getLimitLinkNumber();
                }
                return 0;
            }
        });
        plvhcMemberListRv.setAdapter(memberAdapter);
    }

    private void initKickList() {
        plvhcMemberListKickRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        plvhcMemberListKickRv.setHasFixedSize(true);
        kickListAdapter = new PLVHCKickListAdapter();
        kickListAdapter.setOnViewActionListener(new PLVHCKickListAdapter.OnViewActionListener() {
            @Override
            public void onRemoveKickUserAction() {
                updateKickCount(Math.max(0, --kickCount));
            }
        });
        plvhcMemberListKickRv.setAdapter(kickListAdapter);
    }

    private void initMemberTypeWindow() {
        memberTypeWindow = new PLVHCMemberTypeWindow(this);
        memberTypeWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                plvhcMemberListPackUpIv.setSelected(false);
            }
        });
        memberTypeWindow.setOnViewActionListener(new PLVHCMemberTypeWindow.OnViewActionListener() {
            @Override
            public void onSelectOnlineList() {
                isSelectOnlineList = true;
                plvhcMemberListRv.setVisibility(View.VISIBLE);
                plvhcMemberListRightLy.setVisibility(View.VISIBLE);
                plvhcMemberListKickRv.setVisibility(View.GONE);
                plvhcMemberListDataTypeTv.setText("在线学生 ( " + onlineCount + " ) ");
            }

            @Override
            public void onSelectKickList() {
                if (chatroomPresenter != null) {
                    chatroomPresenter.requestKickUsers();
                }
                isSelectOnlineList = false;
                plvhcMemberListRv.setVisibility(View.GONE);
                plvhcMemberListRightLy.setVisibility(View.GONE);
                plvhcMemberListKickRv.setVisibility(View.VISIBLE);
                plvhcMemberListDataTypeTv.setText("移出学生 ( " + kickCount + " ) ");
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void hideWindow() {
        memberTypeWindow.hide();
    }

    public void setIsSimpleLayout() {
        this.isSimpleLayout = true;
        plvhcMemberListHandsUpTv.setVisibility(View.GONE);
        plvhcMemberListCupsTv.setVisibility(View.GONE);
        plvhcMemberListBanTv.setVisibility(View.GONE);
        plvhcMemberListKickTv.setVisibility(View.GONE);
        memberTypeWindow.setIsSimpleLayout();
        memberAdapter.setIsSimpleLayout();
    }

    public void show(int viewWidth, int viewHeight, int[] viewLocation) {
        if (container == null) {
            container = ((Activity) getContext()).findViewById(R.id.plvhc_live_room_popup_container);
            container.addOnDismissListener(new PLVOutsideTouchableLayout.OnOutsideDismissListener(this) {
                @Override
                public void onDismiss() {
                    hide();
                }
            });
        }

        final int screenWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());

        int height = viewHeight - ConvertUtils.dp2px(16);
        int width = isSimpleLayout ? ConvertUtils.dp2px(408) : (screenWidth - ConvertUtils.dp2px(50 + 66));

        FrameLayout.LayoutParams lp = new LayoutParams(width, height);
        lp.rightMargin = ConvertUtils.dp2px(66);
        lp.bottomMargin = ConvertUtils.dp2px(8);
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        setLayoutParams(lp);

        container.removeAllViews();
        container.addView(this);

        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(true);
        }
    }

    public void hide() {
        if (container != null) {
            container.removeAllViews();
        }
        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(false);
        }
    }

    public boolean onBackPressed() {
        if (isShown()) {
            hide();
            return true;
        }
        return false;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView getLinkMicView() {
        return linkMicView;
    }

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="成员列表 - MVP模式的view层实现">
    private PLVAbsMultiRoleLinkMicView linkMicView = new PLVAbsMultiRoleLinkMicView() {
        @Override
        public void setPresenter(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter presenter) {
            linkMicPresenter = presenter;
        }

        @Override
        public void onMemberListChanged(List<PLVMemberItemDataBean> dataBeanList) {
            memberAdapter.bindData(dataBeanList);
            amendUpdateOnlineCount();
        }

        @Override
        public void onMemberItemChanged(int pos) {
            memberAdapter.updateMemberListData(pos);
        }

        @Override
        public void onMemberItemInsert(int pos) {
            if (memberAdapter.getDataBeanList().size() == 1) {
                memberAdapter.notifyDataSetChanged();//need notifyDataSetChanged, empty viewHolder
            } else {
                memberAdapter.notifyItemInserted(pos);
                memberAdapter.updateUserCountChanged();
            }
            amendUpdateOnlineCount();
        }

        @Override
        public void onMemberItemRemove(int pos) {
            memberAdapter.notifyItemRemoved(pos);
            memberAdapter.updateUserCountChanged();
            amendUpdateOnlineCount();
        }

        @Override
        public void onUserRaiseHand(int raiseHandCount, boolean isRaiseHand, int linkMicListPos, int memberListPos) {
            plvhcMemberListHandsUpTv.setText("举手(" + raiseHandCount + ")");
            memberAdapter.updateUserRaiseHand(memberListPos);
        }

        @Override
        public void onUserGetCup(String userNick, boolean isByEvent, int linkMicListPos, int memberListPos) {
            memberAdapter.updateUserGetCup(memberListPos);
        }

        @Override
        public void onUserHasPaint(boolean isMyself, boolean isHasPaint, int linkMicListPos, int memberListPos) {
            memberAdapter.updateUserHasPaint(memberListPos);
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute, int linkMicListPos, int memberListPos) {
            memberAdapter.updateUserMuteVideo(memberListPos);
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute, int linkMicListPos, int memberListPos) {
            memberAdapter.updateUserMuteAudio(memberListPos);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void setPresenter(@NonNull IPLVChatroomContract.IChatroomPresenter presenter) {
            super.setPresenter(presenter);
            chatroomPresenter = presenter;
            chatroomPresenter.getData().getOnlineCountData().observe((LifecycleOwner) getContext(), new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {
                    if (integer == null) {
                        return;
                    }
                    int onlineCount = Math.max(0, integer);
                    updateOnlineCount(onlineCount);
                }
            });
            chatroomPresenter.getData().getKickCountData().observe((LifecycleOwner) getContext(), new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {
                    if (integer == null) {
                        return;
                    }
                    updateKickCount(integer);
                }
            });
        }

        @Override
        public void onKickUsersList(List<PLVSocketUserBean> dataList) {
            super.onKickUsersList(dataList);
            kickListAdapter.bindData(dataList);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="在线列表 - 人数更新">
    private int amendOnlineCount(int onlineCount) {
        if (memberAdapter.getDataBeanList().isEmpty()) {
            onlineCount = 0;
        } else if (onlineCount < memberAdapter.getDataBeanList().size()) {
            onlineCount = memberAdapter.getDataBeanList().size();
        } else if (onlineCount > memberAdapter.getDataBeanList().size() && onlineCount < PLVMultiRoleMemberList.MEMBER_LENGTH_MORE) {
            onlineCount = memberAdapter.getDataBeanList().size();
        }
        return onlineCount;
    }

    private void amendUpdateOnlineCount() {
        updateOnlineCount(onlineCount);
    }

    private void updateOnlineCount(int onlineCount) {
        onlineCount = amendOnlineCount(onlineCount);
        if (this.onlineCount == onlineCount) {
            return;
        }
        this.onlineCount = onlineCount;
        memberTypeWindow.updateOnlineCount(onlineCount);
        if (isSelectOnlineList) {
            plvhcMemberListDataTypeTv.setText("在线学生 ( " + onlineCount + " ) ");
        }
    }

    private void updateKickCount(int kickCount) {
        this.kickCount = kickCount;
        memberTypeWindow.updateKickCount(kickCount);
        if (!isSelectOnlineList) {
            plvhcMemberListDataTypeTv.setText("移出学生 ( " + kickCount + " ) ");
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvhc_member_list_pack_up_iv
                || id == R.id.plvhc_member_list_data_type_tv) {
            memberTypeWindow.show(plvhcMemberListDataTypeTv);
            plvhcMemberListPackUpIv.setSelected(true);
        } else if (id == R.id.plvhc_member_list_mute_all_audio_tv) {
            plvhcMemberListMuteAllAudioTv.setSelected(!v.isSelected());
            plvhcMemberListMuteAllAudioTv.setText(v.isSelected() ? "取消全体禁麦" : "全体禁麦");
            if (linkMicPresenter != null) {
                linkMicPresenter.muteAllUserAudio(v.isSelected());
            }
            if (v.isSelected()) {
                PLVHCToast.Builder.context(getContext())
                        .setDrawable(R.drawable.plvhc_member_mic_sel)
                        .setText("已全体禁麦")
                        .build()
                        .show();
            }
        } else if (id == R.id.plvhc_member_list_close_all_linkmic_tv) {
            new PLVHCConfirmDialog(getContext())
                    .setTitle("学生下台")
                    .setContent("要将所有学生下台吗？")
                    .setLeftButtonText("取消")
                    .setRightButtonText("确定")
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            if (linkMicPresenter != null) {
                                linkMicPresenter.closeAllUserLinkMic();
                            }
                            PLVHCToast.Builder.context(getContext())
                                    .setDrawable(R.drawable.plvhc_member_down_all)
                                    .setText("已全体下台")
                                    .build()
                                    .show();
                        }
                    })
                    .show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">

    /**
     * view交互事件监听器
     */
    public interface OnViewActionListener {
        /**
         * 可见性改变回调
         *
         * @param isVisible true：显示，false：隐藏
         */
        void onVisibilityChanged(boolean isVisible);
    }
    // </editor-fold>
}
