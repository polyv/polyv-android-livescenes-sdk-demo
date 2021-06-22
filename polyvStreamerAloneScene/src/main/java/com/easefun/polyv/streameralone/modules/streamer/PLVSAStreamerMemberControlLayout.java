package com.easefun.polyv.streameralone.modules.streamer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.streamer.transfer.PLVSStreamerInnerDataTransfer;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.socket.user.PLVSocketUserConstant;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 管理连麦成员的布局
 */
public class PLVSAStreamerMemberControlLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private CircleImageView plvsaStreamerAvatarIv;
    private TextView plvsaStreamerUserTypeTv;
    private TextView plvsaStreamerNickTv;
    private ImageView plvsaStreamerCameraIv;
    private ImageView plvsaStreamerMicIv;
    private ImageView plvsaStreamerDownLinkmicIv;
    private TextView plvsaStreamerCameraTv;
    private TextView plvsaStreamerMicTv;
    private TextView plvsaStreamerDownLinkmicTv;

    //data
    private String linkMicUid;

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStreamerMemberControlLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStreamerMemberControlLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStreamerMemberControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_streamer_member_control_layout, this);

        plvsaStreamerAvatarIv = (CircleImageView) findViewById(R.id.plvsa_streamer_avatar_iv);
        plvsaStreamerUserTypeTv = (TextView) findViewById(R.id.plvsa_streamer_user_type_tv);
        plvsaStreamerNickTv = (TextView) findViewById(R.id.plvsa_streamer_nick_tv);
        plvsaStreamerCameraIv = (ImageView) findViewById(R.id.plvsa_streamer_camera_iv);
        plvsaStreamerMicIv = (ImageView) findViewById(R.id.plvsa_streamer_mic_iv);
        plvsaStreamerDownLinkmicIv = (ImageView) findViewById(R.id.plvsa_streamer_down_linkmic_iv);
        plvsaStreamerCameraTv = (TextView) findViewById(R.id.plvsa_streamer_camera_tv);
        plvsaStreamerMicTv = (TextView) findViewById(R.id.plvsa_streamer_mic_tv);
        plvsaStreamerDownLinkmicTv = (TextView) findViewById(R.id.plvsa_streamer_down_linkmic_tv);

        plvsaStreamerCameraIv.setOnClickListener(this);
        plvsaStreamerCameraTv.setOnClickListener(this);
        plvsaStreamerMicIv.setOnClickListener(this);
        plvsaStreamerMicTv.setOnClickListener(this);
        plvsaStreamerDownLinkmicIv.setOnClickListener(this);
        plvsaStreamerDownLinkmicTv.setOnClickListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void bindViewData(PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (linkMicItemDataBean == null) {
            return;
        }
        linkMicUid = linkMicItemDataBean.getLinkMicId();
        //头像
        String pic = linkMicItemDataBean.getPic();
        PLVImageLoader.getInstance().loadImageNoDiskCache(
                getContext(),
                pic,
                R.drawable.plvsa_member_student_missing_face,
                R.drawable.plvsa_member_student_missing_face,
                plvsaStreamerAvatarIv
        );
        //头衔
        String actor = linkMicItemDataBean.getActor();
        String userType = linkMicItemDataBean.getUserType();
        plvsaStreamerUserTypeTv.setText(actor);
        plvsaStreamerUserTypeTv.setVisibility(View.VISIBLE);
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
            plvsaStreamerUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_teacher_tv_bg_shape);
        } else if (PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(userType)) {
            plvsaStreamerUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_assistant_tv_bg_shape);
        } else if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
            plvsaStreamerUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_guest_tv_bg_shape);
        } else if (PLVSocketUserConstant.USERTYPE_MANAGER.equals(userType)) {
            plvsaStreamerUserTypeTv.setBackgroundResource(R.drawable.plvsa_member_manager_tv_bg_shape);
        } else {
            plvsaStreamerUserTypeTv.setVisibility(View.GONE);
        }
        //昵称
        String nick = linkMicItemDataBean.getNick();
        plvsaStreamerNickTv.setText(nick);
        //媒体状态
        plvsaStreamerCameraIv.setSelected(linkMicItemDataBean.isMuteVideo());
        plvsaStreamerMicIv.setSelected(linkMicItemDataBean.isMuteAudio());
        //下麦按钮
        if (PLVSStreamerInnerDataTransfer.getInstance().isAutoLinkToGuest() && linkMicItemDataBean.isGuest()) {
            plvsaStreamerDownLinkmicIv.setVisibility(View.GONE);
            plvsaStreamerDownLinkmicTv.setVisibility(View.GONE);
        } else {
            plvsaStreamerDownLinkmicIv.setVisibility(View.VISIBLE);
            plvsaStreamerDownLinkmicTv.setVisibility(View.VISIBLE);
        }
    }

    public String getLinkMicUid() {
        return linkMicUid;
    }

    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                    }

                    ViewGroup popupContainer = (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
                    View maskView = ((Activity) getContext()).findViewById(R.id.plvsa_popup_container_mask);
                    if (popupContainer.getChildCount() > 0) {
                        maskView.setVisibility(View.VISIBLE);
                    } else {
                        maskView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
            menuDrawer.openMenu();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public boolean isOpen() {
        return menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING);
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvsa_streamer_camera_iv
                || id == R.id.plvsa_streamer_camera_tv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickCamera(plvsaStreamerCameraIv.isSelected());
            }
        } else if (id == R.id.plvsa_streamer_mic_iv
                || id == R.id.plvsa_streamer_mic_tv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickMic(plvsaStreamerMicIv.isSelected());
            }
        } else if (id == R.id.plvsa_streamer_down_linkmic_iv
                || id == R.id.plvsa_streamer_down_linkmic_tv) {
            close();
            new PLVSAConfirmDialog(v.getContext())
                    .setTitle("确定下麦吗？")
                    .setContentVisibility(View.GONE)
                    .setLeftButtonText("取消")
                    .setRightButtonText("确定")
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            if (onViewActionListener != null) {
                                onViewActionListener.onClickDownLinkMic();
                            }
                        }
                    })
                    .show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onClickCamera(boolean isWillOpen);

        void onClickMic(boolean isWillOpen);

        void onClickDownLinkMic();
    }
    // </editor-fold>
}
