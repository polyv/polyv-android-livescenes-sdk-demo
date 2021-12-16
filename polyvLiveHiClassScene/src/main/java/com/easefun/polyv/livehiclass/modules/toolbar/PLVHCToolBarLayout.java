package com.easefun.polyv.livehiclass.modules.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.chatroom.IPLVHCChatroomLayout;
import com.easefun.polyv.livehiclass.modules.chatroom.PLVHCChatroomLayout;
import com.easefun.polyv.livehiclass.modules.document.popuplist.PLVHCPptListLayout;
import com.easefun.polyv.livehiclass.modules.linkmic.widget.PLVHCGroupLeaderGuideLayout;
import com.easefun.polyv.livehiclass.modules.linkmic.widget.PLVHCGroupLeaderRequestHelpLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCExitConfirmDialog;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCMemberLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCSettingLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCStudentHandsLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCTeacherBeginCountDownWindow;
import com.easefun.polyv.livehiclass.modules.toolbar.enums.PLVHCMarkToolEnums;
import com.easefun.polyv.livehiclass.modules.toolbar.widget.PLVHCMarkToolControllerLayout;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.livescenes.net.IPLVDataRequestListener;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.linkmic.PLVLinkMicEventSender;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 工具栏布局
 */
public class PLVHCToolBarLayout extends FrameLayout implements IPLVHCToolBarLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int RAISE_HAND_TIME = 10;
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //聊天室布局
    private IPLVHCChatroomLayout chatroomLayout;
    //成员列表布局
    private PLVHCMemberLayout memberLayout;
    //设置布局
    private PLVHCSettingLayout settingLayout;
    //文档管理布局
    private PLVHCPptListLayout pptListLayout;
    //组长请求帮助的高亮指引布局
    @Nullable
    private PLVHCGroupLeaderGuideLayout leaderGuideLayout;

    //student view
    private ViewGroup plvhcToolbarStudentHandsUpLy;
    private ImageView plvhcToolbarStudentHandsUpIv;
    private TextView plvhcToolbarStudentHandsUpCdTv;
    private ImageView plvhcToolbarStudentSettingIv;
    private ViewGroup plvhcToolbarStudentChatroomLy;
    private ImageView plvhcToolbarStudentChatroomIv;
    private View plvhcToolbarStudentChatroomMsgTipsView;
    private LinearLayout plvhcToolbarStudentMarkToolControlGroup;
    //teacher/leader view
    private ImageView plvhcToolbarClassIv;
    private ImageView plvhcToolbarDocumentIv;
    private ImageView plvhcToolbarMemberListIv;
    private ViewGroup plvhcToolbarChatroomLy;
    private ImageView plvhcToolbarChatroomIv;
    private View plvhcToolbarChatroomMsgTipsView;
    private ImageView plvhcToolbarSettingIv;
    private PLVHCStudentHandsLayout plvhcToolbarMemberHandsUpLy;
    private LinearLayout plvhcToolbarMarkToolControlGroup;
    private PLVHCGroupLeaderRequestHelpLayout leaderRequestHelpLayout;
    // student&teacher&leader share view
    private PLVHCMarkToolControllerLayout plvhcToolbarMarkToolControllerLayout;
    private ImageView plvhcToolbarMarkUndoIv;
    private ImageView plvhcToolbarMarkDeleteIv;
    private PLVRoundColorView plvhcToolbarMarkToolCurrentColorView;
    private PLVRoundImageView plvhcToolbarCurrentMarkToolIv;

    //是否是讲师布局
    private boolean isTeacherLayout;
    //是否是组长布局
    private boolean isLeaderLayout;
    //组长是否在请求帮助
    private boolean isLeaderRequestHelp;
    //上课按钮是否可用
    private boolean isClassButtonEnable;
    //是否能调整布局
    private boolean isCanAdjustLayout;
    //记录学生布局的举手按钮的可见状态
    private int studentHandsUpLyVisibility = View.INVISIBLE;

    private boolean isFullScreen;
    private int smallScreenHeight;
    private int[] smallScreenLocation = new int[2];
    private int addBottomMargin;

    //listener
    private OnViewActionListener onViewActionListener;
    //Disposable
    private Disposable handsUpDisposable;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCToolBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCToolBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCToolBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_toolbar_layout, this);

        initChatroomLayout();
        initMemberLayout();
        initSettingLayout();
        initPptListLayout();
    }

    private void initStudentLayout() {
        isTeacherLayout = false;
        isLeaderLayout = false;
        removeAllViews();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_toolbar_student_layout, null);
        addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //student view
        plvhcToolbarStudentHandsUpLy = findViewById(R.id.plvhc_toolbar_student_hands_up_ly);
        plvhcToolbarStudentHandsUpLy.setVisibility(studentHandsUpLyVisibility);
        plvhcToolbarStudentHandsUpIv = findViewById(R.id.plvhc_toolbar_student_hands_up_iv);
        plvhcToolbarStudentHandsUpCdTv = findViewById(R.id.plvhc_toolbar_student_hands_up_cd_tv);
        plvhcToolbarStudentChatroomLy = findViewById(R.id.plvhc_toolbar_student_chatroom_ly);
        plvhcToolbarStudentChatroomIv = findViewById(R.id.plvhc_toolbar_student_chatroom_iv);
        plvhcToolbarStudentChatroomMsgTipsView = findViewById(R.id.plvhc_toolbar_student_chatroom_msg_tips_view);
        plvhcToolbarStudentSettingIv = findViewById(R.id.plvhc_toolbar_student_setting_iv);
        plvhcToolbarMarkUndoIv = (ImageView) findViewById(R.id.plvhc_toolbar_student_mark_undo_iv);
        plvhcToolbarMarkDeleteIv = (ImageView) findViewById(R.id.plvhc_toolbar_student_mark_delete_iv);
        plvhcToolbarMarkToolCurrentColorView = (PLVRoundColorView) findViewById(R.id.plvhc_toolbar_student_mark_tool_current_color_view);
        plvhcToolbarCurrentMarkToolIv = (PLVRoundImageView) findViewById(R.id.plvhc_toolbar_student_current_mark_tool_iv);
        plvhcToolbarStudentMarkToolControlGroup = (LinearLayout) findViewById(R.id.plvhc_toolbar_student_mark_tool_control_group);
        plvhcToolbarMarkToolControllerLayout = new PLVHCMarkToolControllerLayout(getContext());

        //初始化点击事件
        plvhcToolbarStudentHandsUpIv.setOnClickListener(this);
        plvhcToolbarStudentChatroomIv.setOnClickListener(this);
        plvhcToolbarStudentSettingIv.setOnClickListener(this);
        plvhcToolbarMarkUndoIv.setOnClickListener(this);
        plvhcToolbarMarkDeleteIv.setOnClickListener(this);
        plvhcToolbarMarkToolCurrentColorView.setOnClickListener(this);
        plvhcToolbarCurrentMarkToolIv.setOnClickListener(this);

        initMarkToolControllerLayout();
        adjustToolbarLayout();
        memberLayout.hideWindow();
        if (leaderGuideLayout != null) {
            leaderGuideLayout.hide();
        }
    }

    private void initTeacherLayout() {
        isTeacherLayout = true;
        isLeaderLayout = false;
        removeAllViews();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_toolbar_teacher_layout, null);
        addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //teacher view
        plvhcToolbarMemberHandsUpLy = (PLVHCStudentHandsLayout) findViewById(R.id.plvhc_toolbar_member_hands_up_ly);
        plvhcToolbarMarkUndoIv = (ImageView) findViewById(R.id.plvhc_toolbar_mark_undo_iv);
        plvhcToolbarMarkDeleteIv = (ImageView) findViewById(R.id.plvhc_toolbar_mark_delete_iv);
        plvhcToolbarMarkToolCurrentColorView = (PLVRoundColorView) findViewById(R.id.plvhc_toolbar_mark_tool_current_color_view);
        plvhcToolbarCurrentMarkToolIv = (PLVRoundImageView) findViewById(R.id.plvhc_toolbar_current_mark_tool_iv);
        plvhcToolbarClassIv = (ImageView) findViewById(R.id.plvhc_toolbar_class_iv);
        plvhcToolbarDocumentIv = (ImageView) findViewById(R.id.plvhc_toolbar_document_iv);
        plvhcToolbarMemberListIv = (ImageView) findViewById(R.id.plvhc_toolbar_member_list_iv);
        plvhcToolbarChatroomLy = findViewById(R.id.plvhc_toolbar_chatroom_ly);
        plvhcToolbarChatroomIv = (ImageView) findViewById(R.id.plvhc_toolbar_chatroom_iv);
        plvhcToolbarChatroomMsgTipsView = findViewById(R.id.plvhc_toolbar_chatroom_msg_tips_view);
        plvhcToolbarSettingIv = (ImageView) findViewById(R.id.plvhc_toolbar_setting_iv);
        plvhcToolbarMarkToolControllerLayout = new PLVHCMarkToolControllerLayout(getContext());

        //初始化点击事件
        plvhcToolbarMemberHandsUpLy.setOnClickListener(this);
        plvhcToolbarClassIv.setOnClickListener(this);
        plvhcToolbarDocumentIv.setOnClickListener(this);
        plvhcToolbarMemberListIv.setOnClickListener(this);
        plvhcToolbarChatroomIv.setOnClickListener(this);
        plvhcToolbarSettingIv.setOnClickListener(this);
        plvhcToolbarMarkUndoIv.setOnClickListener(this);
        plvhcToolbarMarkDeleteIv.setOnClickListener(this);
        plvhcToolbarMarkToolCurrentColorView.setOnClickListener(this);
        plvhcToolbarCurrentMarkToolIv.setOnClickListener(this);

        // 回调上下课按钮初始化结束
        if (onViewActionListener != null) {
            onViewActionListener.onInitClassImageView(plvhcToolbarClassIv);
        }

        initMarkToolControllerLayout();
        adjustToolbarLayout();
        memberLayout.hideWindow();
        if (leaderGuideLayout != null) {
            leaderGuideLayout.hide();
        }
    }

    private void initGroupStudentLayout() {
        isTeacherLayout = false;
        isLeaderLayout = false;
        removeAllViews();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_toolbar_group_student_layout, null);
        addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //student view
        plvhcToolbarStudentChatroomLy = findViewById(R.id.plvhc_toolbar_student_chatroom_ly);
        plvhcToolbarStudentChatroomIv = findViewById(R.id.plvhc_toolbar_student_chatroom_iv);
        plvhcToolbarStudentChatroomMsgTipsView = findViewById(R.id.plvhc_toolbar_student_chatroom_msg_tips_view);
        plvhcToolbarStudentSettingIv = findViewById(R.id.plvhc_toolbar_student_setting_iv);
        plvhcToolbarMarkUndoIv = (ImageView) findViewById(R.id.plvhc_toolbar_student_mark_undo_iv);
        plvhcToolbarMarkDeleteIv = (ImageView) findViewById(R.id.plvhc_toolbar_student_mark_delete_iv);
        plvhcToolbarMarkToolCurrentColorView = (PLVRoundColorView) findViewById(R.id.plvhc_toolbar_student_mark_tool_current_color_view);
        plvhcToolbarCurrentMarkToolIv = (PLVRoundImageView) findViewById(R.id.plvhc_toolbar_student_current_mark_tool_iv);
        plvhcToolbarStudentMarkToolControlGroup = (LinearLayout) findViewById(R.id.plvhc_toolbar_student_mark_tool_control_group);
        plvhcToolbarMarkToolControllerLayout = new PLVHCMarkToolControllerLayout(getContext());

        //初始化点击事件
        plvhcToolbarStudentChatroomIv.setOnClickListener(this);
        plvhcToolbarStudentSettingIv.setOnClickListener(this);
        plvhcToolbarMarkUndoIv.setOnClickListener(this);
        plvhcToolbarMarkDeleteIv.setOnClickListener(this);
        plvhcToolbarMarkToolCurrentColorView.setOnClickListener(this);
        plvhcToolbarCurrentMarkToolIv.setOnClickListener(this);

        initMarkToolControllerLayout();
        adjustToolbarLayout();
        memberLayout.hideWindow();
        if (leaderGuideLayout != null) {
            leaderGuideLayout.hide();
        }
    }

    private void initGroupLeaderLayout() {
        isTeacherLayout = false;
        isLeaderLayout = true;
        removeAllViews();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_toolbar_group_leader_layout, null);
        addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //teacher view
        plvhcToolbarMarkUndoIv = (ImageView) findViewById(R.id.plvhc_toolbar_mark_undo_iv);
        plvhcToolbarMarkDeleteIv = (ImageView) findViewById(R.id.plvhc_toolbar_mark_delete_iv);
        plvhcToolbarMarkToolCurrentColorView = (PLVRoundColorView) findViewById(R.id.plvhc_toolbar_mark_tool_current_color_view);
        plvhcToolbarCurrentMarkToolIv = (PLVRoundImageView) findViewById(R.id.plvhc_toolbar_current_mark_tool_iv);
        leaderRequestHelpLayout = findViewById(R.id.plvhc_toolbar_leader_help_ly);
        plvhcToolbarDocumentIv = (ImageView) findViewById(R.id.plvhc_toolbar_document_iv);
        plvhcToolbarMemberListIv = (ImageView) findViewById(R.id.plvhc_toolbar_member_list_iv);
        plvhcToolbarChatroomLy = findViewById(R.id.plvhc_toolbar_chatroom_ly);
        plvhcToolbarChatroomIv = (ImageView) findViewById(R.id.plvhc_toolbar_chatroom_iv);
        plvhcToolbarChatroomMsgTipsView = findViewById(R.id.plvhc_toolbar_chatroom_msg_tips_view);
        plvhcToolbarSettingIv = (ImageView) findViewById(R.id.plvhc_toolbar_setting_iv);
        plvhcToolbarMarkToolControlGroup = findViewById(R.id.plvhc_toolbar_mark_tool_control_group);
        plvhcToolbarMarkToolControllerLayout = new PLVHCMarkToolControllerLayout(getContext());

        //初始化点击事件
        plvhcToolbarDocumentIv.setOnClickListener(this);
        plvhcToolbarMemberListIv.setOnClickListener(this);
        plvhcToolbarChatroomIv.setOnClickListener(this);
        plvhcToolbarSettingIv.setOnClickListener(this);
        plvhcToolbarMarkUndoIv.setOnClickListener(this);
        plvhcToolbarMarkDeleteIv.setOnClickListener(this);
        plvhcToolbarMarkToolCurrentColorView.setOnClickListener(this);
        plvhcToolbarCurrentMarkToolIv.setOnClickListener(this);
        leaderRequestHelpLayout.setOnLayoutClickListener(new PLVHCGroupLeaderRequestHelpLayout.OnHelpLayoutClickListener() {
            @Override
            public void onClick(boolean isRequest) {
                if (isRequest) {
                    PLVLinkMicEventSender.getInstance().groupRequestHelp(null);
                    if (leaderGuideLayout != null) {
                        leaderGuideLayout.showCancelHelpGuide(leaderRequestHelpLayout);
                    }
                } else {
                    PLVLinkMicEventSender.getInstance().groupCancelHelp(null);
                }
                PLVHCToast.Builder.context(getContext())
                        .setText(isRequest ? "请求已发起" : "请求已取消")
                        .build()
                        .show();
            }
        });
        if (isLeaderRequestHelp) {
            leaderRequestHelpLayout.onRequestHelp();
        }

        initMarkToolControllerLayout();
        adjustToolbarLayout();
        initLeaderGuideLayout();
        memberLayout.setIsSimpleLayout();
        memberLayout.hideWindow();
        if (leaderGuideLayout != null) {
            leaderGuideLayout.showRequestHelpGuide(leaderRequestHelpLayout);
        }
    }

    private void initChatroomLayout() {
        chatroomLayout = new PLVHCChatroomLayout(getContext());
        chatroomLayout.setOnViewActionListener(new IPLVHCChatroomLayout.OnViewActionListener() {
            @Override
            public void onVisibilityChanged(boolean isVisible) {
                if (plvhcToolbarChatroomIv != null) {
                    plvhcToolbarChatroomIv.setSelected(isVisible);
                }
                if (plvhcToolbarStudentChatroomIv != null) {
                    plvhcToolbarStudentChatroomIv.setSelected(isVisible);
                }
            }

            @Override
            public void onUnreadMsgCountChanged(int currentUnreadCount) {
                if (!chatroomLayout.isShown() && currentUnreadCount > 0) {
                    if (plvhcToolbarChatroomMsgTipsView != null) {
                        plvhcToolbarChatroomMsgTipsView.setVisibility(View.VISIBLE);
                    }
                    if (plvhcToolbarStudentChatroomMsgTipsView != null) {
                        plvhcToolbarStudentChatroomMsgTipsView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void initMemberLayout() {
        memberLayout = new PLVHCMemberLayout(getContext());
        memberLayout.setOnViewActionListener(new PLVHCMemberLayout.OnViewActionListener() {
            @Override
            public void onVisibilityChanged(boolean isVisible) {
                if (plvhcToolbarMemberListIv != null) {
                    plvhcToolbarMemberListIv.setSelected(isVisible);
                }
            }
        });
    }

    private void initSettingLayout() {
        settingLayout = new PLVHCSettingLayout(getContext());
        settingLayout.setOnViewActionListener(new PLVHCSettingLayout.OnViewActionListener() {
            @Override
            public void onVisibilityChanged(boolean isVisible) {
                if (plvhcToolbarSettingIv != null) {
                    plvhcToolbarSettingIv.setSelected(isVisible);
                }
                if (plvhcToolbarStudentSettingIv != null) {
                    plvhcToolbarStudentSettingIv.setSelected(isVisible);
                }
            }

            @Override
            public void onFullScreenControl(boolean isFullScreen) {
                PLVHCToolBarLayout.this.isFullScreen = isFullScreen;
                if (onViewActionListener != null) {
                    onViewActionListener.onFullScreenControl(isFullScreen);
                }
            }
        });
    }

    private void initPptListLayout() {
        pptListLayout = new PLVHCPptListLayout(getContext());
        pptListLayout.setOnViewActionListener(new PLVHCPptListLayout.OnViewActionListener() {
            @Override
            public void onVisibilityChanged(boolean isVisible) {
                if (plvhcToolbarDocumentIv != null) {
                    plvhcToolbarDocumentIv.setSelected(isVisible);
                }
            }
        });
    }

    private void initLeaderGuideLayout() {
        if (leaderGuideLayout == null) {
            leaderGuideLayout = ((Activity) getContext()).findViewById(R.id.plvhc_leader_request_help_guide_layout);
        }
    }

    private void initMarkToolControllerLayout() {
        plvhcToolbarMarkToolControllerLayout.init(liveRoomDataManager, isLeaderLayout);
        // 进入时初始化标注工具状态
        handleChangeMarkTool(PLVHCMarkToolEnums.MarkTool.getDefaultMarkTool(isTeacherLayout));
        handleChangeColor(PLVHCMarkToolEnums.Color.getDefaultColor(isTeacherLayout));
        changeMarkToolState(false, false);

        plvhcToolbarMarkToolControllerLayout.setOnChangeMarkToolStateListener(new PLVHCMarkToolControllerLayout.OnChangeMarkToolStateListener() {
            @Override
            public void onChangeMarkTool(PLVHCMarkToolEnums.MarkTool newMarkTool) {
                handleChangeMarkTool(newMarkTool);
            }

            @Override
            public void onChangeColor(PLVHCMarkToolEnums.Color newColor) {
                handleChangeColor(newColor);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVHCToolBarLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
            initTeacherLayout();
        } else {
            initStudentLayout();
        }

        chatroomLayout.init(liveRoomDataManager);
        //register after init
        chatroomLayout.getChatroomPresenter().registerView(memberLayout.getChatroomView());
        chatroomLayout.getChatroomPresenter().requestKickUsers();
    }

    @Override
    public void handleImgSelectResult(Intent data) {
        chatroomLayout.handleImgSelectResult(data);
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;

        // 回调上下课按钮初始化结束
        if (onViewActionListener != null && plvhcToolbarClassIv != null) {
            onViewActionListener.onInitClassImageView(plvhcToolbarClassIv);
        }
    }

    @Override
    public void initDefaultMediaStatus(boolean isMuteAudio, boolean isMuteVideo, boolean isFrontCamera) {
        settingLayout.initDefaultMediaStatus(isMuteAudio, isMuteVideo, isFrontCamera);
    }

    @Override
    public void onLessonPreparing(long serverTime, long lessonStartTime) {
        if (plvhcToolbarClassIv != null) {
            plvhcToolbarClassIv.setSelected(false);
        }
        if (plvhcToolbarStudentHandsUpLy != null) {
            plvhcToolbarStudentHandsUpLy.setVisibility(View.INVISIBLE);
        }
        isClassButtonEnable = true;
        studentHandsUpLyVisibility = View.INVISIBLE;
        chatroomLayout.onLessonPreparing(serverTime, lessonStartTime);
    }

    @Override
    public void onLessonStarted() {
        if (plvhcToolbarClassIv != null) {
            plvhcToolbarClassIv.setSelected(true);
        }
        if (plvhcToolbarStudentHandsUpLy != null) {
            plvhcToolbarStudentHandsUpLy.setVisibility(View.VISIBLE);
        }
        isClassButtonEnable = true;
        studentHandsUpLyVisibility = View.VISIBLE;
        chatroomLayout.onLessonStarted();
    }

    @Override
    public void onLessonEnd(long inClassTime) {
        if (plvhcToolbarClassIv != null) {
            plvhcToolbarClassIv.setSelected(false);
        }
        if (plvhcToolbarStudentHandsUpLy != null) {
            plvhcToolbarStudentHandsUpLy.setVisibility(View.INVISIBLE);
        }
        if (leaderGuideLayout != null) {
            leaderGuideLayout.hide();
        }
        isClassButtonEnable = true;
        studentHandsUpLyVisibility = View.INVISIBLE;
        chatroomLayout.onLessonEnd(inClassTime);
    }

    @Override
    public void onUserHasGroupLeader(boolean isHasGroupLeader) {
        if (isHasGroupLeader) {
            initGroupLeaderLayout();
        } else {
            initGroupStudentLayout();
        }
    }

    @Override
    public void onJoinDiscuss(String groupId) {
        isLeaderRequestHelp = false;
        chatroomLayout.onJoinDiscuss(groupId);
    }

    @Override
    public void onLeaveDiscuss() {
        chatroomLayout.onLeaveDiscuss();
        if (liveRoomDataManager != null) {
            String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
            if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
                initTeacherLayout();
            } else {
                initStudentLayout();
            }
        }
    }

    @Override
    public void onLeaderRequestHelp() {
        isLeaderRequestHelp = true;
        if (leaderRequestHelpLayout != null) {
            leaderRequestHelpLayout.onRequestHelp();
        }
    }

    @Override
    public void onLeaderCancelHelp() {
        isLeaderRequestHelp = false;
        if (leaderRequestHelpLayout != null) {
            leaderRequestHelpLayout.onCancelHelp();
        }
    }

    @Override
    public void adjustLayout() {
        isCanAdjustLayout = true;
        adjustToolbarLayout();
    }

    @Override
    public void acceptUserRaiseHand(int raiseHandCount, boolean isRaiseHand) {
        if (plvhcToolbarMemberHandsUpLy != null) {
            plvhcToolbarMemberHandsUpLy.acceptUserRaiseHand(raiseHandCount, isRaiseHand);
        }
    }

    @Override
    public void acceptHasPaintToMe(boolean isHasPaint) {
        if (isHasPaint) {
            if (plvhcToolbarStudentMarkToolControlGroup != null) {
                plvhcToolbarStudentMarkToolControlGroup.setVisibility(View.VISIBLE);
            }
            if (isLeaderLayout && plvhcToolbarMarkToolControlGroup != null) {
                plvhcToolbarMarkToolControlGroup.setVisibility(View.VISIBLE);
            }
        } else {
            if (plvhcToolbarStudentMarkToolControlGroup != null) {
                plvhcToolbarStudentMarkToolControlGroup.setVisibility(View.GONE);
            }
            if (isLeaderLayout && plvhcToolbarMarkToolControlGroup != null) {
                plvhcToolbarMarkToolControlGroup.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void changeMarkToolState(boolean showUndoButton, boolean showDeleteButton) {
        plvhcToolbarMarkUndoIv.setVisibility(showUndoButton ? VISIBLE : GONE);
        plvhcToolbarMarkDeleteIv.setVisibility(showDeleteButton ? VISIBLE : GONE);
    }

    @Override
    public IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView getMemberLayoutLinkMicView() {
        return memberLayout.getLinkMicView();
    }

    @Override
    public IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView getSettingLayoutLinkMicView() {
        return settingLayout.getLinkMicView();
    }

    @Override
    public boolean onBackPressed() {
        return chatroomLayout.onBackPressed()
                || memberLayout.onBackPressed()
                || settingLayout.onBackPressed();
    }

    @Override
    public void destroy() {
        chatroomLayout.destroy();
        pptListLayout.destroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现View方法">
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handsUpDisposable != null) {
            handsUpDisposable.dispose();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isFullScreen) {
            smallScreenHeight = getHeight();
            getLocationInWindow(smallScreenLocation);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void adjustToolbarLayout() {
        if (!isCanAdjustLayout) {
            return;
        }
        if (!isTeacherLayout && !isLeaderLayout) {
            int lackStudentLyHeight = getHeight() - ConvertUtils.dp2px(228);
            if (lackStudentLyHeight < 0) {
                addBottomMargin = lackStudentLyHeight / 2;
                addBottomMargin(plvhcToolbarStudentHandsUpLy, addBottomMargin);
                addBottomMargin(plvhcToolbarStudentChatroomLy, addBottomMargin);
            }
        } else {
            int lackTeacherLyHeight = getHeight() - ConvertUtils.dp2px(276);
            if (lackTeacherLyHeight < 0) {
                addBottomMargin = lackTeacherLyHeight / 4;
                addBottomMargin(plvhcToolbarClassIv, addBottomMargin);
                addBottomMargin(plvhcToolbarDocumentIv, addBottomMargin);
                addBottomMargin(plvhcToolbarMemberListIv, addBottomMargin);
                addBottomMargin(plvhcToolbarChatroomLy, addBottomMargin);
                addBottomMargin(leaderRequestHelpLayout, addBottomMargin);

                addBottomMargin(plvhcToolbarMemberHandsUpLy, addBottomMargin * 2);
            }
        }
        plvhcToolbarMarkToolControllerLayout.setAddBottomMargin(addBottomMargin);
    }

    private void addBottomMargin(View view, int addMargin) {
        if (view == null) {
            return;
        }
        MarginLayoutParams viewLp = (MarginLayoutParams) view.getLayoutParams();
        viewLp.bottomMargin = viewLp.bottomMargin + addMargin;
        view.setLayoutParams(viewLp);
    }

    private void handleHandsUpClick(final View v) {
        boolean isOnline = PLVSocketWrapper.getInstance().isOnlineStatus();
        PLVHCToast.Builder.context(getContext())
                .setText(isOnline ? "举手成功" : "举手失败")
                .build()
                .show();
        if (!isOnline) {
            return;
        }
        v.setEnabled(false);
        v.setSelected(true);
        plvhcToolbarStudentHandsUpCdTv.setVisibility(View.VISIBLE);
        if (handsUpDisposable != null) {
            handsUpDisposable.dispose();
        }
        handsUpDisposable = Observable.intervalRange(0, RAISE_HAND_TIME + 1, 0, 1, TimeUnit.SECONDS)
                .compose(new PLVRxBaseTransformer<Long, Long>())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        plvhcToolbarStudentHandsUpCdTv.setText((RAISE_HAND_TIME - aLong) + "s");
                        if (aLong == RAISE_HAND_TIME) {
                            plvhcToolbarStudentHandsUpCdTv.setVisibility(View.GONE);
                            v.setEnabled(true);
                            v.setSelected(false);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
        if (onViewActionListener != null) {
            onViewActionListener.onSendRaiseHandEvent(RAISE_HAND_TIME * 1000);
        }
    }

    private void handleClassClick(final View v) {
        if (!isClassButtonEnable) {
            PLVHCToast.Builder.context(getContext())
                    .setText("上课失败，请重试")
                    .build()
                    .show();
            return;
        }
        if (v.isSelected()) {
            new PLVHCExitConfirmDialog(getContext())
                    .setContent("确定要下课吗？")
                    .setOnPositiveListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (onViewActionListener != null) {
                                v.setEnabled(false);
                                onViewActionListener.onStopLesson(new IPLVDataRequestListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        v.setEnabled(true);
                                    }

                                    @Override
                                    public void onFailed(String msg, Throwable throwable) {
                                        v.setEnabled(true);
                                        PLVHCToast.Builder.context(getContext())
                                                .setText("下课失败\n" + msg)
                                                .build()
                                                .show();
                                    }
                                });
                            }
                        }
                    })
                    .show();
        } else {
            PLVHCTeacherBeginCountDownWindow countDownWindow = new PLVHCTeacherBeginCountDownWindow(this);
            countDownWindow.setOnCountDownListener(new PLVHCTeacherBeginCountDownWindow.OnCountDownListener() {
                @Override
                public void onCountDownFinished() {
                    if (onViewActionListener != null) {
                        v.setEnabled(false);
                        onViewActionListener.onStartLesson(new IPLVDataRequestListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                v.setEnabled(true);
                                PLVHCToast.Builder.context(getContext())
                                        .setText("课程开始")
                                        .build()
                                        .show();
                            }

                            @Override
                            public void onFailed(String msg, Throwable throwable) {
                                v.setEnabled(true);
                                PLVHCToast.Builder.context(getContext())
                                        .setText("上课失败\n" + msg)
                                        .build()
                                        .show();
                            }
                        });
                    }
                }

                @Override
                public void onCountDownCanceled() {
                }
            });
            countDownWindow.startCountDown();
        }
    }

    /**
     * 切换标注工具栏显示类型
     * 当前显示类型与传入类型一致时隐藏工具类，不一致时切换至传入类型
     *
     * @param showType 工具类显示类型
     */
    private void showOrHideControllerToType(PLVHCMarkToolEnums.ControllerShowType showType) {
        if (plvhcToolbarMarkToolControllerLayout.getCurrentShowType() != showType) {
            plvhcToolbarMarkToolControllerLayout.show(showType);
        } else {
            plvhcToolbarMarkToolControllerLayout.hide();
        }
    }

    private void handleChangeMarkTool(PLVHCMarkToolEnums.MarkTool markTool) {
        if (onViewActionListener != null) {
            onViewActionListener.onRequestChangeDocumentMarkTool(markTool);
        }
        if (PLVHCMarkToolEnums.MarkTool.CLEAR.equals(markTool)) {
            return;
        }
        if (markTool.isShowColor()) {
            plvhcToolbarMarkToolCurrentColorView.setVisibility(VISIBLE);
        } else {
            plvhcToolbarMarkToolCurrentColorView.setVisibility(GONE);
        }
        switch (markTool) {
            case MOVE:
                plvhcToolbarCurrentMarkToolIv.setImageResource(R.drawable.plvhc_document_move_icon);
                break;
            case SELECT:
                plvhcToolbarCurrentMarkToolIv.setImageResource(R.drawable.plvhc_document_select_icon);
                break;
            case PEN:
                plvhcToolbarCurrentMarkToolIv.setImageResource(R.drawable.plvhc_document_pen_icon);
                break;
            case ARROW:
                plvhcToolbarCurrentMarkToolIv.setImageResource(R.drawable.plvhc_document_arrow_icon);
                break;
            case TEXT:
                plvhcToolbarCurrentMarkToolIv.setImageResource(R.drawable.plvhc_document_text_icon);
                break;
            case ERASER:
                plvhcToolbarCurrentMarkToolIv.setImageResource(R.drawable.plvhc_document_eraser_icon);
                break;
            default:
        }
    }

    private void handleChangeColor(PLVHCMarkToolEnums.Color color) {
        if (onViewActionListener != null) {
            onViewActionListener.onRequestChangeDocumentColor(color);
        }
        plvhcToolbarMarkToolCurrentColorView.updateMainColor(Color.parseColor(color.getColorString()));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.plvhc_toolbar_student_chatroom_iv
                || id == R.id.plvhc_toolbar_chatroom_iv) {
            chatroomLayout.show(getWidth(), smallScreenHeight, smallScreenLocation);
            if (plvhcToolbarChatroomMsgTipsView != null) {
                plvhcToolbarChatroomMsgTipsView.setVisibility(View.GONE);
            }
            if (plvhcToolbarStudentChatroomMsgTipsView != null) {
                plvhcToolbarStudentChatroomMsgTipsView.setVisibility(View.GONE);
            }
        } else if (id == R.id.plvhc_toolbar_member_list_iv
                || id == R.id.plvhc_toolbar_member_hands_up_ly) {
            memberLayout.show(getWidth(), smallScreenHeight, smallScreenLocation);
        } else if (id == R.id.plvhc_toolbar_student_setting_iv
                || id == R.id.plvhc_toolbar_setting_iv) {
            settingLayout.show(getWidth(), smallScreenHeight, smallScreenLocation);
        } else if (id == R.id.plvhc_toolbar_document_iv) {
            pptListLayout.show(getWidth(), smallScreenHeight, smallScreenLocation);
        } else if (id == R.id.plvhc_toolbar_student_hands_up_iv) {
            handleHandsUpClick(v);
        } else if (id == R.id.plvhc_toolbar_class_iv) {
            handleClassClick(v);
        } else if (id == R.id.plvhc_toolbar_current_mark_tool_iv
                || id == R.id.plvhc_toolbar_student_current_mark_tool_iv) {
            showOrHideControllerToType(PLVHCMarkToolEnums.ControllerShowType.MARK_TOOL);
        } else if (id == R.id.plvhc_toolbar_mark_tool_current_color_view
                || id == R.id.plvhc_toolbar_student_mark_tool_current_color_view) {
            showOrHideControllerToType(PLVHCMarkToolEnums.ControllerShowType.COLOR);
        } else if (id == R.id.plvhc_toolbar_mark_undo_iv
                || id == R.id.plvhc_toolbar_student_mark_undo_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onRequestUndo();
            }
        } else if (id == R.id.plvhc_toolbar_mark_delete_iv
                || id == R.id.plvhc_toolbar_student_mark_delete_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onRequestDelete();
            }
        }
    }
    // </editor-fold>
}
