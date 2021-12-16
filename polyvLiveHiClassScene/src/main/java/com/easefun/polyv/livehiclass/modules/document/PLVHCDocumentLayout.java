package com.easefun.polyv.livehiclass.modules.document;

import android.content.Context;
import android.content.Intent;
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
import com.easefun.polyv.livecommon.module.modules.document.contract.IPLVDocumentContract;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.document.widget.PLVHCDocumentInputWidget;
import com.easefun.polyv.livehiclass.modules.document.widget.PLVHCDocumentMinimizeItemLayout;
import com.easefun.polyv.livehiclass.modules.toolbar.enums.PLVHCMarkToolEnums;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.document.IPLVDocumentContainerView;
import com.plv.livescenes.document.event.PLVCancelEditTextEvent;
import com.plv.livescenes.document.event.PLVChangeApplianceEvent;
import com.plv.livescenes.document.event.PLVChangeStrokeStyleEvent;
import com.plv.livescenes.document.event.PLVDoClearEvent;
import com.plv.livescenes.document.event.PLVDoDeleteEvent;
import com.plv.livescenes.document.event.PLVDoUndoEvent;
import com.plv.livescenes.document.event.PLVFinishEditTextEvent;
import com.plv.livescenes.document.event.PLVGivePaintBrushAuthEvent;
import com.plv.livescenes.document.event.PLVOpenPptEvent;
import com.plv.livescenes.document.event.PLVOperateContainerEvent;
import com.plv.livescenes.document.event.PLVRefreshMinimizeContainerDataEvent;
import com.plv.livescenes.document.event.PLVRefreshPptContainerTotalEvent;
import com.plv.livescenes.document.event.PLVRemovePaintBrushAuthEvent;
import com.plv.livescenes.document.event.PLVResetZoomEvent;
import com.plv.livescenes.document.event.PLVSendSocketDataEvent;
import com.plv.livescenes.document.event.PLVSetGroupLeaderEvent;
import com.plv.livescenes.document.event.PLVStartEditTextEvent;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;
import com.plv.livescenes.document.event.PLVToggleOperationStatusEvent;
import com.plv.livescenes.document.event.PLVZoomPercentChangeEvent;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.socket.client.Socket;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

/**
 * 文档布局
 */
public class PLVHCDocumentLayout extends FrameLayout implements IPLVHCDocumentLayout, IPLVDocumentContainerView.OnReceiveEventListener, View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVHCDocumentLayout.class.getSimpleName();

    private static final int MAX_OPEN_DOCUMENT_LIMIT = 5;
    private static final int ZOOM_DEFAULT_PERCENT = 100;
    private static final long ZOOM_HINT_SHOW_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

    private View rootView;
    private IPLVDocumentContainerView containerView;
    private PLVRoundRectLayout plvhcDocumentZoomLayout;
    private ImageView plvhcDocumentZoomIv;
    private PLVRoundRectGradientTextView plvhcDocumentZoomHintTv;
    private ImageView plvhcDocumentMinimizeGroupIv;
    private TextView plvhcDocumentMinimizeGroupTv;
    private PLVTriangleIndicateLayout plvhcDocumentMinimizeGroupRootLayout;
    private LinearLayout plvhcDocumentMinimizeGroupListLl;

    private PLVConfirmDialog plvClearMarkConfirmDialog;
    private PLVHCDocumentInputWidget documentInputWidget;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    private String userType;

    private IPLVDocumentContract.View documentMvpView;

    private OnViewActionListener onViewActionListener;

    private int documentOpenCount = 0;
    private long zoomHintTextLastShowTimestamp = 0;
    private boolean hasPaint = false;
    private boolean isLeader;
    private int zoomPercent = ZOOM_DEFAULT_PERCENT;

    private PLVHCMarkToolEnums.MarkTool lastSelectMarkTool;
    private PLVHCMarkToolEnums.Color lastSelectColor;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCDocumentLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCDocumentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCDocumentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_document_layout, this);
        findView();
    }

    private void findView() {
        containerView = (IPLVDocumentContainerView) rootView.findViewById(R.id.plvhc_document_container_view);
        plvhcDocumentZoomLayout = (PLVRoundRectLayout) rootView.findViewById(R.id.plvhc_document_zoom_layout);
        plvhcDocumentZoomIv = (ImageView) rootView.findViewById(R.id.plvhc_document_zoom_iv);
        plvhcDocumentZoomHintTv = (PLVRoundRectGradientTextView) rootView.findViewById(R.id.plvhc_document_zoom_hint_tv);
        plvhcDocumentMinimizeGroupIv = (ImageView) rootView.findViewById(R.id.plvhc_document_minimize_group_iv);
        plvhcDocumentMinimizeGroupTv = (TextView) rootView.findViewById(R.id.plvhc_document_minimize_group_tv);
        plvhcDocumentMinimizeGroupRootLayout = (PLVTriangleIndicateLayout) rootView.findViewById(R.id.plvhc_document_minimize_group_root_layout);
        plvhcDocumentMinimizeGroupListLl = (LinearLayout) rootView.findViewById(R.id.plvhc_document_minimize_group_list_ll);

        plvhcDocumentMinimizeGroupIv.setOnClickListener(this);
        plvhcDocumentZoomLayout.setOnClickListener(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法 - 外部调用一次">

    @Override
    public void init(final IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        this.userType = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return liveRoomDataManager.getConfig().getUser().getViewerType();
            }
        });
        initMvpView();
        initContainerView(liveRoomDataManager);
        updateMinimizeGroupCount();
    }

    private void initMvpView() {
        documentMvpView = new PLVAbsDocumentView() {
            @Override
            public boolean onRequestOpenPptView(final int pptId, final String pptName) {
                processOpenPptView(pptId, pptName);
                return true;
            }
        };

        PLVDocumentPresenter.getInstance().registerView(documentMvpView);
    }

    private void initContainerView(final IPLVLiveRoomDataManager liveRoomDataManager) {
        final String viewerId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return liveRoomDataManager.getConfig().getUser().getViewerId();
            }
        });
        final String viewerName = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return liveRoomDataManager.getConfig().getUser().getViewerName();
            }
        });
        final String sessionId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return liveRoomDataManager.getSessionId();
            }
        });

        containerView.setViewerId(viewerId);
        containerView.setViewerName(viewerName);
        containerView.setUserType(userType);
        containerView.setSessionId(sessionId);
        containerView.setOnReceiveEventListener(this);
        containerView.loadWeb();

        // 初始化工具和颜色
        final boolean isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        changeMarkTool(PLVHCMarkToolEnums.MarkTool.getDefaultMarkTool(isTeacherType));
        changeColor(PLVHCMarkToolEnums.Color.getDefaultColor(isTeacherType));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    @Override
    public void onSelectUploadDocument(Intent intent) {
        if (intent == null) {
            return;
        }
        PLVDocumentPresenter.getInstance().onSelectUploadFile(intent.getData());
    }

    @Override
    public void changeMarkTool(PLVHCMarkToolEnums.MarkTool markTool) {
        if (markTool != PLVHCMarkToolEnums.MarkTool.CLEAR) {
            lastSelectMarkTool = markTool;
            if (isHasPaint()) {
                containerView.sendEvent(new PLVChangeApplianceEvent(markTool.getAppliances()));
            }
        } else {
            // 清屏
            if (plvClearMarkConfirmDialog == null) {
                plvClearMarkConfirmDialog = new PLVHCConfirmDialog(getContext())
                        .setTitleVisibility(GONE)
                        .setContent("清屏后笔迹将无法恢复，确定清屏吗")
                        .setLeftButtonText("按错了")
                        .setRightButtonText("确定")
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                plvClearMarkConfirmDialog.hide();
                            }
                        })
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                containerView.sendEvent(new PLVDoClearEvent());
                                plvClearMarkConfirmDialog.hide();
                            }
                        });
            }
            plvClearMarkConfirmDialog.show();
        }
    }

    @Override
    public void changeColor(PLVHCMarkToolEnums.Color color) {
        lastSelectColor = color;
        if (isHasPaint()) {
            containerView.sendEvent(new PLVChangeStrokeStyleEvent(color.getColorString()));
        }
    }

    @Override
    public void operateUndo() {
        containerView.sendEvent(new PLVDoUndoEvent());
    }

    @Override
    public void operateDelete() {
        containerView.sendEvent(new PLVDoDeleteEvent());
    }

    @Override
    public void acceptHasPaintToMe(boolean isHasPaint) {
        this.hasPaint = isHasPaint;
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
            return;
        }
        if (isHasPaint) {
            containerView.sendEvent(new PLVGivePaintBrushAuthEvent());
            changeMarkTool(getOrDefault(lastSelectMarkTool, PLVHCMarkToolEnums.MarkTool.getDefaultMarkTool(false)));
            changeColor(getOrDefault(lastSelectColor, PLVHCMarkToolEnums.Color.getDefaultColor(false)));
        } else {
            containerView.sendEvent(new PLVRemovePaintBrushAuthEvent());
        }
    }

    @Override
    public void onUserHasGroupLeader(boolean isHasGroupLeader) {
        this.isLeader = isHasGroupLeader;
        this.hasPaint = isHasGroupLeader;
        updateWhenLeaderChanged(false);
        containerView.sendEvent(new PLVSetGroupLeaderEvent(isHasGroupLeader));
        if (isHasGroupLeader) {
            changeMarkTool(getOrDefault(lastSelectMarkTool, PLVHCMarkToolEnums.MarkTool.getDefaultMarkTool(false)));
            changeColor(getOrDefault(lastSelectColor, PLVHCMarkToolEnums.Color.getDefaultColor(false)));
        }
    }

    @Override
    public void onJoinDiscuss(PLVSwitchRoomEvent switchRoomEvent) {
        isLeader = false;
        hasPaint = false;
        updateWhenLeaderChanged(false);
        if (switchRoomEvent != null) {
            containerView.sendEvent(switchRoomEvent);
        }
    }

    @Override
    public void onLeaveDiscuss(PLVSwitchRoomEvent switchRoomEvent) {
        isLeader = false;
        hasPaint = false;
        updateWhenLeaderChanged(true);
        containerView.sendEvent(new PLVSetGroupLeaderEvent(false));
        if (switchRoomEvent != null) {
            containerView.sendEvent(switchRoomEvent);
        }
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View方法重写 - onMeasure修改ContainerView尺寸">

    // 白板文档容器 宽 : 高 = 2.1 : 1
    private static final float CONTAINER_RATIO_WIDTH_BY_HEIGHT = 2.1F;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);

        int containerPreferWidth = (int) (specSizeHeight * CONTAINER_RATIO_WIDTH_BY_HEIGHT);
        int containerWidth = containerPreferWidth;
        if (containerPreferWidth > specSizeWidth) {
            containerWidth = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        ViewGroup.LayoutParams containerLayoutParam = ((View) containerView).getLayoutParams();
        if (containerLayoutParam.width != containerWidth) {
            containerLayoutParam.width = containerWidth;
            ((View) containerView).setLayoutParams(containerLayoutParam);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    private void processOpenPptView(int pptId, String pptName) {
        if (documentOpenCount >= MAX_OPEN_DOCUMENT_LIMIT) {
            PLVHCToast.Builder.context(getContext())
                    .setText("只支持同时打开 " + MAX_OPEN_DOCUMENT_LIMIT + " 个文档")
                    .setDrawable(R.drawable.plvhc_document_status_webic_att)
                    .build().show();
            return;
        }

        containerView.sendEvent(new PLVOpenPptEvent(pptId));
    }

    private void updateDocumentOpenCount(int count) {
        documentOpenCount = Math.max(count, 0);
    }

    private void updateMinimizeGroupCount() {
        final boolean isStudent = PLVSocketUserConstant.USERTYPE_SCSTUDENT.equals(liveRoomDataManager.getConfig().getUser().getViewerType());
        final int minimizeCount = plvhcDocumentMinimizeGroupListLl.getChildCount();
        plvhcDocumentMinimizeGroupTv.setText(String.valueOf(minimizeCount));
        if ((isStudent && !isLeader) || minimizeCount <= 0) {
            plvhcDocumentMinimizeGroupIv.setVisibility(GONE);
            plvhcDocumentMinimizeGroupTv.setVisibility(GONE);
            plvhcDocumentMinimizeGroupRootLayout.setVisibility(GONE);
        } else {
            plvhcDocumentMinimizeGroupIv.setVisibility(VISIBLE);
            plvhcDocumentMinimizeGroupTv.setVisibility(VISIBLE);
        }
    }

    private boolean isHasPaint() {
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
            return true;
        }
        return hasPaint;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 文档Webview事件处理">

    @Override
    public void onReceive(String eventType, String data) {
        if (eventType == null) {
            return;
        }
        PLVCommonLog.d(TAG, "on receive webview message, eventType = " + eventType + ", data = " + data);
        switch (eventType) {
            case PLVRefreshMinimizeContainerDataEvent.TYPE:
                // 最小化PPT容器数据
                processRefreshMinimizeContainerDataEvent(data);
                break;
            case PLVRefreshPptContainerTotalEvent.TYPE:
                // 当前已打开PPT容器数量
                processRefreshPptContainerTotalEvent(data);
                break;
            case PLVStartEditTextEvent.TYPE:
                // 编辑文本
                processStartEditText(data);
                break;
            case PLVToggleOperationStatusEvent.TYPE:
                // 更新标注工具操作按钮状态
                processToggleOperationStatus(data);
                break;
            case PLVSendSocketDataEvent.TYPE:
                // 发生socket消息到服务器端
                processSendSocketDataEvent(data);
                break;
            case PLVZoomPercentChangeEvent.TYPE:
                // 白板缩放比例变化
                processZoomPercentChange(data);
            default:
        }
    }

    private void processRefreshMinimizeContainerDataEvent(String data) {
        PLVRefreshMinimizeContainerDataEvent event = PLVRefreshMinimizeContainerDataEvent.fromJson(data);
        if (event != null) {
            handleSetMinimizeItemList(event.getList());
        }
    }

    private void processRefreshPptContainerTotalEvent(String data) {
        PLVRefreshPptContainerTotalEvent event = PLVRefreshPptContainerTotalEvent.fromJson(data);
        if (event != null) {
            updateDocumentOpenCount(event.getTotal());
        }
    }

    private void processStartEditText(String data) {
        if (documentInputWidget == null) {
            documentInputWidget = new PLVHCDocumentInputWidget(getContext());
            documentInputWidget.setOnFinishEditTextListener(new PLVHCDocumentInputWidget.OnFinishEditTextListener() {

                @Override
                public void onCancelEdit() {
                    containerView.sendEvent(new PLVCancelEditTextEvent());
                }

                @Override
                public void onFinishEdit(String content) {
                    containerView.sendEvent(new PLVFinishEditTextEvent(content));
                }

            });
        }

        ((ViewGroup) rootView).addView(documentInputWidget, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        PLVStartEditTextEvent event = PLVStartEditTextEvent.fromJson(data);
        if (event != null) {
            documentInputWidget.setEditTextEvent(event);
        }
    }

    private void processToggleOperationStatus(String data) {
        PLVToggleOperationStatusEvent event = PLVToggleOperationStatusEvent.fromJson(data);
        if (event == null) {
            return;
        }
        final boolean showUndoButton = event.getUndoStatus() != null ? event.getUndoStatus() : false;
        final boolean showDeleteButton = event.getDeleteStatus() != null ? event.getDeleteStatus() : false;
        if (onViewActionListener != null) {
            onViewActionListener.onChangeMarkToolOperationButtonState(showUndoButton, showDeleteButton);
        }
    }

    private void processSendSocketDataEvent(String data) {
        PLVSendSocketDataEvent event = PLVSendSocketDataEvent.fromJson(data);
        if (event != null) {
            PLVSocketWrapper.getInstance().emit(Socket.EVENT_MESSAGE, event.getSocketData());
        }
    }

    private void processZoomPercentChange(String data) {
        PLVZoomPercentChangeEvent event = PLVZoomPercentChangeEvent.fromJson(data);
        if (event == null || event.getZoomPercent() == null) {
            return;
        }
        zoomPercent = (int) (event.getZoomPercent() * 100);

        final boolean isTeacher = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        if (!isTeacher && !isLeader) {
            // 仅讲师和组长弹出缩放提示
            return;
        }

        plvhcDocumentZoomLayout.setVisibility(zoomPercent == ZOOM_DEFAULT_PERCENT ? View.GONE : View.VISIBLE);

        final String zoomPercentHintText = zoomPercent + "%";
        plvhcDocumentZoomHintTv.setText(zoomPercentHintText);
        plvhcDocumentZoomHintTv.setVisibility(VISIBLE);
        // 延时 ZOOM_HINT_SHOW_TIMEOUT 后隐藏
        zoomHintTextLastShowTimestamp = System.currentTimeMillis();
        plvhcDocumentZoomHintTv.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - zoomHintTextLastShowTimestamp >= ZOOM_HINT_SHOW_TIMEOUT
                        && plvhcDocumentZoomHintTv != null) {
                    plvhcDocumentZoomHintTv.setVisibility(GONE);
                }
            }
        }, ZOOM_HINT_SHOW_TIMEOUT);
    }

    private void handleSetMinimizeItemList(List<PLVRefreshMinimizeContainerDataEvent.ContainerData> minimizeItemList) {
        plvhcDocumentMinimizeGroupListLl.removeAllViews();
        final PLVHCDocumentMinimizeItemLayout.OnLayoutClickedListener listener = new PLVHCDocumentMinimizeItemLayout.OnLayoutClickedListener() {
            @Override
            public void onClickLayout(PLVHCDocumentMinimizeItemLayout layout, String containerId, String pptName) {
                containerView.sendEvent(new PLVOperateContainerEvent(containerId, PLVOperateContainerEvent.OperateType.OPEN));
                plvhcDocumentMinimizeGroupListLl.removeView(layout);
                plvhcDocumentMinimizeGroupRootLayout.setVisibility(GONE);
                updateMinimizeGroupCount();
            }

            @Override
            public void onClickClose(PLVHCDocumentMinimizeItemLayout layout, String containerId, String pptName) {
                containerView.sendEvent(new PLVOperateContainerEvent(containerId, PLVOperateContainerEvent.OperateType.CLOSE));
                plvhcDocumentMinimizeGroupListLl.removeView(layout);
                updateDocumentOpenCount(documentOpenCount - 1);
                updateMinimizeGroupCount();
            }
        };

        for (PLVRefreshMinimizeContainerDataEvent.ContainerData item : minimizeItemList) {
            if (item.getMinimize() == null || !item.getMinimize()) {
                continue;
            }
            PLVHCDocumentMinimizeItemLayout itemLayout = new PLVHCDocumentMinimizeItemLayout(getContext());
            itemLayout.setPptData(item.getContainerId(), item.getTitle());
            itemLayout.setOnLayoutClickedListener(listener);
            plvhcDocumentMinimizeGroupListLl.addView(itemLayout);
        }

        updateMinimizeGroupCount();
    }

    private void updateWhenLeaderChanged(boolean isLeaveDiscuss) {
        if (isLeaveDiscuss) {
            zoomPercent = ZOOM_DEFAULT_PERCENT;
            plvhcDocumentMinimizeGroupListLl.removeAllViews();
        }
        if (!isLeader) {
            plvhcDocumentZoomHintTv.setVisibility(View.GONE);
            plvhcDocumentZoomLayout.setVisibility(View.GONE);
        } else if (zoomPercent != ZOOM_DEFAULT_PERCENT) {
            plvhcDocumentZoomLayout.setVisibility(View.VISIBLE);
        }
        updateMinimizeGroupCount();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    @Override
    public void onClick(View v) {
        if (v.getId() == plvhcDocumentMinimizeGroupIv.getId()) {
            if (plvhcDocumentMinimizeGroupRootLayout.getVisibility() != VISIBLE) {
                plvhcDocumentMinimizeGroupRootLayout.setVisibility(VISIBLE);
            } else {
                plvhcDocumentMinimizeGroupRootLayout.setVisibility(GONE);
            }
        } else if (v.getId() == plvhcDocumentZoomLayout.getId()) {
            containerView.sendEvent(new PLVResetZoomEvent());
        }
    }

    // </editor-fold>

}
