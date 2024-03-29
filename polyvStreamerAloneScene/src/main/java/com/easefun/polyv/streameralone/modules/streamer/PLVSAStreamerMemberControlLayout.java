package com.easefun.polyv.streameralone.modules.streamer;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicLocalShareData;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVAutoLineLayoutManager;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.streamer.adapter.PLVSAControlAdapter;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 管理连麦成员的布局
 */
public class PLVSAStreamerMemberControlLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;

    private CircleImageView plvsaStreamerAvatarIv;
    private TextView plvsaStreamerUserTypeTv;
    private TextView plvsaStreamerNickTv;

    private RecyclerView plvsaStreamerControlRv;
    private PLVSAControlAdapter controlAdapter;
    //成员控制的图片资源和文本
    private List<Pair<Integer, String>> list = new ArrayList(Arrays.asList(
            new Pair<Integer, String>(R.drawable.plvsa_more_camera_selector, PLVAppUtils.getString(R.string.plv_linkmic_camera)),
            new Pair<Integer, String>(R.drawable.plvsa_more_mic_selector, PLVAppUtils.getString(R.string.plv_linkmic_microphone)),
            new Pair<Integer, String>(R.drawable.plvsa_streamer_down_linkmic, PLVAppUtils.getString(R.string.plv_linkmic_hang_off)),
            new Pair<Integer, String>(R.drawable.plvsa_streamer_speaker, PLVAppUtils.getString(R.string.plv_streamer_grant_speaker_permission_3)),
            new Pair<Integer, String>(R.drawable.plvsa_streamer_fullscreen, PLVAppUtils.getString(R.string.plv_live_full_screen))
    ));
    //对应功能在list中的position
    private int CONTROL_CAMERA = 0;
    private int CONTROL_MIC = 1;
    private int CONTROL_DOWN_LINKMIC = 2;
    private int CONTROL_GRANT_SPEAKER = 3;
    private int CONTROL_FULLSCREEN = 4;


    //data
    private String linkMicUid;
    private boolean isGuestAutoLinkMic;
    private boolean allowGuestTransferSpeaker;

    private boolean isNeedPermissionDialogShow = false;

    //主讲权限用户
    private PLVSocketUserBean speakerUser;

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    private int menuSize;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;

    private final PLVLinkMicLocalShareData linkMicLocalShareData = PLVDependManager.getInstance().get(PLVLinkMicLocalShareData.class);

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

        ViewStub viewStub;
        if (PLVScreenUtils.isPortrait(getContext())) {
            viewStub = (ViewStub) findViewById(R.id.plvsa_member_control_layout_viewstub_port);
        } else {
            viewStub = (ViewStub) findViewById(R.id.plvsa_member_control_layout_viewstub_land);
        }
        viewStub.inflate();

        plvsaStreamerAvatarIv = (CircleImageView) findViewById(R.id.plvsa_streamer_avatar_iv);
        plvsaStreamerUserTypeTv = (TextView) findViewById(R.id.plvsa_streamer_user_type_tv);
        plvsaStreamerNickTv = (TextView) findViewById(R.id.plvsa_streamer_nick_tv);

        plvsaStreamerControlRv = findViewById(R.id.plvsa_streamer_control_rv);
        plvsaStreamerControlRv.setLayoutManager(new PLVAutoLineLayoutManager());
        controlAdapter = new PLVSAControlAdapter();
        controlAdapter.setData(list);
        controlAdapter.setOnItemClickListener(new PLVSAControlAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, boolean isSelected) {
                if (position == CONTROL_CAMERA) {
                    close();
                    if (onViewActionListener != null) {
                        onViewActionListener.onClickCamera(isSelected);
                    }
                    controlAdapter.updateItemSelectStatus(position, !isSelected);
                } else if (position == CONTROL_MIC) {
                    close();
                    if (onViewActionListener != null) {
                        onViewActionListener.onClickMic(isSelected);
                    }
                    controlAdapter.updateItemSelectStatus(position, !isSelected);
                } else if (position == CONTROL_DOWN_LINKMIC) {
                    close();
                    final int pos = position;
                    final boolean isDownLinkmic = isSelected;
                    new PLVSAConfirmDialog(view.getContext())
                            .setTitle(R.string.plv_linkmic_confirm_hang_off)
                            .setContentVisibility(View.GONE)
                            .setLeftButtonText(R.string.plv_common_dialog_cancel)
                            .setRightButtonText(R.string.plv_common_dialog_confirm_2)
                            .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, View v) {
                                    dialog.dismiss();
                                    if (onViewActionListener != null) {
                                        onViewActionListener.onClickDownLinkMic();
                                    }
                                    controlAdapter.updateItemSelectStatus(pos, !isDownLinkmic);
                                }
                            })
                            .show();
                } else if (position == CONTROL_GRANT_SPEAKER) {
                    close();
                    final boolean isGrant = isSelected;
                    final int pos = position;
                    if (!isNeedPermissionDialogShow) {
                        String text = PLVAppUtils.getString(isGrant ? R.string.plv_streamer_grant_speaker_permission_3 : R.string.plv_streamer_remove_speaker_permission_3);
                        list.set(pos, new Pair<Integer, String>(list.get(pos).first, text));
                        if (onViewActionListener != null) {
                            onViewActionListener.onClickGrantSpeaker(!isGrant);
                        }
                        controlAdapter.updateItemSelectStatus(pos, !isGrant);
                        return;
                    }
                    String title = PLVAppUtils.getString(isGrant ? R.string.plv_streamer_remove_speaker_permission_4 : R.string.plv_streamer_grant_speaker_permission_4);
                    String content = PLVAppUtils.getString(isGrant ? R.string.plv_streamer_remove_speaker_permission_5 : R.string.plv_streamer_grant_speaker_permission_5);
                    new PLVSAConfirmDialog(view.getContext())
                            .setTitle(title)
                            .setContent(content)
                            .setLeftButtonText(R.string.plv_common_dialog_cancel)
                            .setRightButtonText(R.string.plv_common_dialog_confirm_2)
                            .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, View v) {
                                    dialog.dismiss();
                                    String text = PLVAppUtils.getString(isGrant ? R.string.plv_streamer_grant_speaker_permission_3 : R.string.plv_streamer_remove_speaker_permission_3);
                                    list.set(pos, new Pair<Integer, String>(list.get(pos).first, text));
                                    if (onViewActionListener != null) {
                                        onViewActionListener.onClickGrantSpeaker(!isGrant);
                                    }
                                    controlAdapter.updateItemSelectStatus(pos, !isGrant);
                                }
                            })
                            .show();
                } else if (position == CONTROL_FULLSCREEN) {
                    close();
                    if (onViewActionListener != null) {
                        onViewActionListener.onClickFullScreen();
                    }
                    controlAdapter.updateItemSelectStatus(position, !isSelected);
                }

            }
        });

        plvsaStreamerControlRv.setAdapter(controlAdapter);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (PLVScreenUtils.isPortrait(getContext())) {
            controlAdapter.setItemWidth(getMeasuredWidth() / 4);
        } else {
            controlAdapter.setItemWidth(getMeasuredWidth() / 3);
        }

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.isGuestAutoLinkMic = liveRoomDataManager.getConfig().isAutoLinkToGuest();
        this.allowGuestTransferSpeaker = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_GUEST_TRANSFER_SPEAKER_ENABLE);

        boolean isShow = PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER);
        //主讲才可以操作下麦等
        controlAdapter.updateItemVisibility(CONTROL_CAMERA, isShow);
        controlAdapter.updateItemVisibility(CONTROL_MIC, isShow);
        controlAdapter.updateItemVisibility(CONTROL_DOWN_LINKMIC, isShow);
        controlAdapter.updateItemVisibility(CONTROL_GRANT_SPEAKER, isShow);
        //全屏不做限制
        controlAdapter.updateItemVisibility(CONTROL_FULLSCREEN, true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void bindViewData(final PLVLinkMicItemDataBean linkMicItemDataBean) {
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

        // 媒体状态
        controlAdapter.updateItemSelectStatus(CONTROL_CAMERA, linkMicItemDataBean.isMuteVideo());
        controlAdapter.updateItemSelectStatus(CONTROL_MIC, linkMicItemDataBean.isMuteAudio());

        // 主讲权限
        if (PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER) && linkMicItemDataBean.isGuest()) {
            // 讲师授权
            String text = PLVAppUtils.getString(!linkMicItemDataBean.isHasSpeaker() ? R.string.plv_streamer_grant_speaker_permission_3 : R.string.plv_streamer_remove_speaker_permission_3);
            list.set(CONTROL_GRANT_SPEAKER, new Pair<>(list.get(CONTROL_GRANT_SPEAKER).first, text));
            controlAdapter.updateItemSelectStatus(CONTROL_GRANT_SPEAKER, linkMicItemDataBean.isHasSpeaker());
            if (linkMicItemDataBean.isHasSpeaker()) {
                // FIXME 屏幕共享中应该提示，目前没有状态判断
                isNeedPermissionDialogShow = false;
            } else {
                isNeedPermissionDialogShow = speakerUser != null;
            }
            controlAdapter.updateItemVisibility(CONTROL_GRANT_SPEAKER, true);
        } else if (PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_GRANTED_SPEAKER_USER)
                && linkMicItemDataBean.isGuest()
                && allowGuestTransferSpeaker) {
            // 嘉宾授权
            String text = PLVAppUtils.getString(!linkMicItemDataBean.isHasSpeaker() ? R.string.plv_streamer_change_speaker_permission_2 : R.string.plv_streamer_remove_speaker_permission_3);
            list.set(CONTROL_GRANT_SPEAKER, new Pair<>(list.get(CONTROL_GRANT_SPEAKER).first, text));
            controlAdapter.updateItemSelectStatus(CONTROL_GRANT_SPEAKER, linkMicItemDataBean.isHasSpeaker());
            controlAdapter.updateItemVisibility(CONTROL_GRANT_SPEAKER, true);
            isNeedPermissionDialogShow = false;
        } else {
            controlAdapter.updateItemVisibility(CONTROL_GRANT_SPEAKER, false);
        }

        // 下麦按钮
        if (PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER)) {
            if (isGuestAutoLinkMic && linkMicItemDataBean.isGuest()) {
                controlAdapter.updateItemVisibility(CONTROL_DOWN_LINKMIC, false);
            } else {
                controlAdapter.updateItemVisibility(CONTROL_DOWN_LINKMIC, true);
            }
        } else {
            controlAdapter.updateItemVisibility(CONTROL_DOWN_LINKMIC, false);
        }

        // 音频连麦隐藏摄像头开关
        final boolean myselfIsTeacher = PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER);
        final boolean isVideoLinkMicType = getOrDefault(linkMicLocalShareData.isVideoLinkMic, true);
        controlAdapter.updateItemVisibility(CONTROL_CAMERA, myselfIsTeacher && (linkMicItemDataBean.isGuest() || isVideoLinkMicType));
    }

    public void setHasSpeakerUser(PLVSocketUserBean user) {
        this.speakerUser = user;
    }

    public String getLinkMicUid() {
        return linkMicUid;
    }

    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuSize = menuDrawer.getMenuSize();
            if (controlAdapter.getVisibilityItem() > PLVAutoLineLayoutManager.span) {
                menuDrawer.setMenuSize((int) (menuSize * 1.3));
            }
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
            if (controlAdapter.getVisibilityItem() > PLVAutoLineLayoutManager.span) {
                menuDrawer.setMenuSize((int) (menuSize * 1.3));
            } else {
                menuDrawer.setMenuSize(menuSize);
            }
            menuDrawer.attachToContainer();
            menuDrawer.requestLayout();
            menuDrawer.invalidate();
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

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Position menuPosition;

        if (PLVScreenUtils.isPortrait(getContext())) {
            menuPosition = MENU_DRAWER_POSITION_PORT;
        } else {
            menuPosition = MENU_DRAWER_POSITION_LAND;
        }

        if (menuDrawer != null) {
            menuDrawer.setPosition(menuPosition);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onClickCamera(boolean isWillOpen);

        void onClickMic(boolean isWillOpen);

        void onClickDownLinkMic();

        void onClickGrantSpeaker(boolean isGrant);

        void onClickFullScreen();
    }
    // </editor-fold>
}
