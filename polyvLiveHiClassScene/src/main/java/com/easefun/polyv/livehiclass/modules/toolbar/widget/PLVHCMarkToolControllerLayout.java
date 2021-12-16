package com.easefun.polyv.livehiclass.modules.toolbar.widget;

import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.ui.widget.PLVOutsideTouchableLayout;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.toolbar.enums.PLVHCMarkToolEnums;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.Map;

/**
 * @author suhongtao
 */
public class PLVHCMarkToolControllerLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private View rootView;
    private LinearLayout plvhcToolbarMarkToolGroup;
    private PLVRoundImageView plvhcToolbarMarkToolMoveIv;
    private PLVRoundImageView plvhcToolbarMarkToolSelectIv;
    private PLVRoundImageView plvhcToolbarMarkToolPenIv;
    private PLVRoundImageView plvhcToolbarMarkToolArrowIv;
    private PLVRoundImageView plvhcToolbarMarkToolTextIv;
    private PLVRoundImageView plvhcToolbarMarkToolEraserIv;
    private PLVRoundImageView plvhcToolbarMarkToolClearIv;
    private LinearLayout plvhcToolbarMarkToolColorGroup;
    private PLVRoundColorView plvhcToolbarMarkToolColorRedView;
    private PLVRoundColorView plvhcToolbarMarkToolColorBlueView;
    private PLVRoundColorView plvhcToolbarMarkToolColorGreenView;
    private PLVRoundColorView plvhcToolbarMarkToolColorYellowView;
    private PLVRoundColorView plvhcToolbarMarkToolColorBlackView;
    private PLVRoundColorView plvhcToolbarMarkToolColorWhiteView;

    // 布局外层容器
    private PLVOutsideTouchableLayout container;

    private Map<PLVHCMarkToolEnums.MarkTool, PLVRoundImageView> markToolViewMap;
    private Map<PLVHCMarkToolEnums.Color, PLVRoundColorView> colorViewMap;

    private PLVHCMarkToolEnums.ControllerShowType currentShowType = PLVHCMarkToolEnums.ControllerShowType.NONE;
    private OnChangeMarkToolStateListener onChangeMarkToolStateListener;

    private boolean isTeacherType = false;
    private boolean isLeader = false;

    private int addBottomMargin;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCMarkToolControllerLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCMarkToolControllerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCMarkToolControllerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_toolbar_mark_tool_controller_layout, this);
        findView();
        initEnumToViewMap();
        setOnClickMarkTool();
        setOnClickColor();

        hide();
    }

    private void findView() {
        plvhcToolbarMarkToolGroup = (LinearLayout) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_group);
        plvhcToolbarMarkToolMoveIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_move_iv);
        plvhcToolbarMarkToolSelectIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_select_iv);
        plvhcToolbarMarkToolPenIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_pen_iv);
        plvhcToolbarMarkToolArrowIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_arrow_iv);
        plvhcToolbarMarkToolTextIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_text_iv);
        plvhcToolbarMarkToolEraserIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_eraser_iv);
        plvhcToolbarMarkToolClearIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_clear_iv);
        plvhcToolbarMarkToolColorGroup = (LinearLayout) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_color_group);
        plvhcToolbarMarkToolColorRedView = (PLVRoundColorView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_color_red_view);
        plvhcToolbarMarkToolColorBlueView = (PLVRoundColorView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_color_blue_view);
        plvhcToolbarMarkToolColorGreenView = (PLVRoundColorView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_color_green_view);
        plvhcToolbarMarkToolColorYellowView = (PLVRoundColorView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_color_yellow_view);
        plvhcToolbarMarkToolColorBlackView = (PLVRoundColorView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_color_black_view);
        plvhcToolbarMarkToolColorWhiteView = (PLVRoundColorView) rootView.findViewById(R.id.plvhc_toolbar_mark_tool_color_white_view);
    }

    private void initEnumToViewMap() {
        markToolViewMap = mapOf(
                pair(PLVHCMarkToolEnums.MarkTool.MOVE, plvhcToolbarMarkToolMoveIv),
                pair(PLVHCMarkToolEnums.MarkTool.SELECT, plvhcToolbarMarkToolSelectIv),
                pair(PLVHCMarkToolEnums.MarkTool.PEN, plvhcToolbarMarkToolPenIv),
                pair(PLVHCMarkToolEnums.MarkTool.ARROW, plvhcToolbarMarkToolArrowIv),
                pair(PLVHCMarkToolEnums.MarkTool.TEXT, plvhcToolbarMarkToolTextIv),
                pair(PLVHCMarkToolEnums.MarkTool.ERASER, plvhcToolbarMarkToolEraserIv),
                pair(PLVHCMarkToolEnums.MarkTool.CLEAR, plvhcToolbarMarkToolClearIv)
        );

        colorViewMap = mapOf(
                pair(PLVHCMarkToolEnums.Color.RED, plvhcToolbarMarkToolColorRedView),
                pair(PLVHCMarkToolEnums.Color.BLUE, plvhcToolbarMarkToolColorBlueView),
                pair(PLVHCMarkToolEnums.Color.GREEN, plvhcToolbarMarkToolColorGreenView),
                pair(PLVHCMarkToolEnums.Color.YELLOW, plvhcToolbarMarkToolColorYellowView),
                pair(PLVHCMarkToolEnums.Color.BLACK, plvhcToolbarMarkToolColorBlackView),
                pair(PLVHCMarkToolEnums.Color.WHITE, plvhcToolbarMarkToolColorWhiteView)
        );
    }

    private void setOnClickMarkTool() {
        foreach(markToolViewMap.entrySet(), new PLVSugarUtil.Consumer<Map.Entry<PLVHCMarkToolEnums.MarkTool, PLVRoundImageView>>() {
            @Override
            public void accept(Map.Entry<PLVHCMarkToolEnums.MarkTool, PLVRoundImageView> entry) {
                final PLVHCMarkToolEnums.MarkTool markTool = entry.getKey();
                final PLVRoundImageView view = entry.getValue();
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!PLVHCMarkToolEnums.MarkTool.CLEAR.equals(markTool)) {
                            updateCurrentSelectedMarkTool(markTool);
                        }
                        if (onChangeMarkToolStateListener != null) {
                            onChangeMarkToolStateListener.onChangeMarkTool(markTool);
                        }
                        hide();
                    }
                });
            }
        });
    }

    private void setOnClickColor() {
        foreach(colorViewMap.entrySet(), new PLVSugarUtil.Consumer<Map.Entry<PLVHCMarkToolEnums.Color, PLVRoundColorView>>() {
            @Override
            public void accept(Map.Entry<PLVHCMarkToolEnums.Color, PLVRoundColorView> entry) {
                final PLVHCMarkToolEnums.Color color = entry.getKey();
                final PLVRoundColorView view = entry.getValue();
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCurrentSelectedColor(color);
                        if (onChangeMarkToolStateListener != null) {
                            onChangeMarkToolStateListener.onChangeColor(color);
                        }
                        hide();
                    }
                });
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法 - 外部调用">

    public void init(final IPLVLiveRoomDataManager liveRoomDataManager, boolean isLeader) {
        final String userType = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return liveRoomDataManager.getConfig().getUser().getViewerType();
            }
        });
        isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        this.isLeader = isLeader;

        initControllerState();
    }

    private void initControllerState() {
        updateCurrentSelectedMarkTool(PLVHCMarkToolEnums.MarkTool.getDefaultMarkTool(isTeacherType));
        updateCurrentSelectedColor(PLVHCMarkToolEnums.Color.getDefaultColor(isTeacherType));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void setAddBottomMargin(int addBottomMargin) {
        this.addBottomMargin = addBottomMargin;
    }

    public void show(@NonNull PLVHCMarkToolEnums.ControllerShowType showType) {
        if (PLVHCMarkToolEnums.ControllerShowType.MARK_TOOL == showType
                || PLVHCMarkToolEnums.ControllerShowType.COLOR == showType) {
            currentShowType = showType;
            updateControllerMarkToolVisibility();
        } else {
            hide();
        }
    }

    public void hide() {
        currentShowType = PLVHCMarkToolEnums.ControllerShowType.NONE;
        updateControllerMarkToolVisibility();
    }

    public PLVHCMarkToolEnums.ControllerShowType getCurrentShowType() {
        return currentShowType;
    }

    public void setOnChangeMarkToolStateListener(OnChangeMarkToolStateListener onChangeMarkToolStateListener) {
        this.onChangeMarkToolStateListener = onChangeMarkToolStateListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI更新">

    private void updateCurrentSelectedMarkTool(PLVHCMarkToolEnums.MarkTool currentMarkTool) {
        foreach(markToolViewMap.values(), new PLVSugarUtil.Consumer<PLVRoundImageView>() {
            @Override
            public void accept(PLVRoundImageView it) {
                it.setSelected(false);
            }
        });
        markToolViewMap.get(currentMarkTool).setSelected(true);
    }

    private void updateCurrentSelectedColor(PLVHCMarkToolEnums.Color currentColor) {
        final int selectedColor = Color.parseColor("#2D3452");

        foreach(colorViewMap.values(), new PLVSugarUtil.Consumer<PLVRoundColorView>() {
            @Override
            public void accept(PLVRoundColorView it) {
                it.updateBackgroundColor(Color.TRANSPARENT);
            }
        });
        colorViewMap.get(currentColor).updateBackgroundColor(selectedColor);
    }

    private void updateControllerMarkToolVisibility() {
        setVisibility(currentShowType != PLVHCMarkToolEnums.ControllerShowType.NONE ? VISIBLE : GONE);
        showToContainer(currentShowType != PLVHCMarkToolEnums.ControllerShowType.NONE);

        plvhcToolbarMarkToolGroup.setVisibility(currentShowType == PLVHCMarkToolEnums.ControllerShowType.MARK_TOOL ? VISIBLE : GONE);
        plvhcToolbarMarkToolColorGroup.setVisibility(currentShowType == PLVHCMarkToolEnums.ControllerShowType.COLOR ? VISIBLE : GONE);

        plvhcToolbarMarkToolMoveIv.setVisibility((isTeacherType || isLeader) ? VISIBLE : GONE);
    }

    private void showToContainer(boolean show) {
        if (container == null) {
            container = ((Activity) getContext()).findViewById(R.id.plvhc_live_room_popup_container);
            container.addOnDismissListener(new PLVOutsideTouchableLayout.OnOutsideDismissListener(this) {
                @Override
                public void onDismiss() {
                    hide();
                }
            });
        }

        if (show && getParent() == null) {
            FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = ConvertUtils.dp2px(66);
            lp.bottomMargin = ConvertUtils.dp2px(74) + addBottomMargin;
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            setLayoutParams(lp);

            container.removeAllViews();
            container.addView(this);
        } else {
            container.removeAllViews();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnChangeMarkToolStateListener {

        /**
         * 标注工具类型变更回调
         *
         * @param newMarkTool
         */
        void onChangeMarkTool(PLVHCMarkToolEnums.MarkTool newMarkTool);

        /**
         * 标注工具颜色变更回调
         *
         * @param newColor
         */
        void onChangeColor(PLVHCMarkToolEnums.Color newColor);

    }

    // </editor-fold>

}
