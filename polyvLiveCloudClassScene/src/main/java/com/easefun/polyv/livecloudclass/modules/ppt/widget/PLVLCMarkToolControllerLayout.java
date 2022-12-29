package com.easefun.polyv.livecloudclass.modules.ppt.widget;

import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.ppt.enums.PLVLCMarkToolEnums;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVLCMarkToolControllerLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVRoundRectLayout pptExitMarkToolLayout;
    private ImageView pptExitMarkToolIv;
    private LinearLayout pptMarkToolGroup;
    private PLVRoundImageView pptMarkToolPenIv;
    private PLVRoundImageView pptMarkToolRectIv;
    private PLVRoundImageView pptMarkToolArrowIv;
    private PLVRoundImageView pptMarkToolTextIv;
    private PLVRoundImageView pptMarkToolEraserIv;
    private PLVRoundImageView pptMarkToolClearIv;
    private LinearLayout pptMarkToolColorGroup;
    private PLVRoundColorView pptMarkToolColorRedView;
    private PLVRoundColorView pptMarkToolColorBlueView;
    private PLVRoundColorView pptMarkToolColorGreenView;
    private PLVRoundColorView pptMarkToolColorYellowView;
    private PLVRoundColorView pptMarkToolColorBlackView;
    private PLVRoundColorView pptMarkToolColorWhiteView;
    private LinearLayout pptMarkToolStateLl;
    private PLVRoundImageView pptMarkUndoIv;
    private PLVRoundColorView pptMarkToolCurrentColorView;
    private PLVRoundImageView pptCurrentMarkToolIv;

    private Map<PLVLCMarkToolEnums.MarkTool, PLVRoundImageView> markToolViewMap;
    private Map<PLVLCMarkToolEnums.Color, PLVRoundColorView> colorViewMap;

    private PLVLCMarkToolEnums.MarkToolGroupShowType currentMarkToolGroupShowType = PLVLCMarkToolEnums.MarkToolGroupShowType.NONE;
    private PLVLCMarkToolEnums.MarkTool currentMarkTool = PLVLCMarkToolEnums.MarkTool.getDefaultMarkTool();
    private PLVLCMarkToolEnums.Color currentColor = PLVLCMarkToolEnums.Color.getDefaultColor();
    private OnMarkToolActionListener onMarkToolActionListener;

    private PLVUserAbilityManager.OnUserAbilityChangedListener userAbilityChangedListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLCMarkToolControllerLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCMarkToolControllerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCMarkToolControllerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_ppt_mark_tool_controller_layout, this);
        findView();
        initEnumToViewMap();
        setOnClickMarkToolSelector();
        setOnClickColorSelector();
        setOnClickMarkToolGroup();

        initControllerState();
        observePaintAbility();
    }

    private void findView() {
        pptExitMarkToolLayout = findViewById(R.id.plvlc_ppt_exit_mark_tool_layout);
        pptExitMarkToolIv = findViewById(R.id.plvlc_ppt_exit_mark_tool_iv);
        pptMarkToolGroup = findViewById(R.id.plvlc_ppt_mark_tool_group);
        pptMarkToolPenIv = findViewById(R.id.plvlc_ppt_mark_tool_pen_iv);
        pptMarkToolRectIv = findViewById(R.id.plvlc_ppt_mark_tool_rect_iv);
        pptMarkToolArrowIv = findViewById(R.id.plvlc_ppt_mark_tool_arrow_iv);
        pptMarkToolTextIv = findViewById(R.id.plvlc_ppt_mark_tool_text_iv);
        pptMarkToolEraserIv = findViewById(R.id.plvlc_ppt_mark_tool_eraser_iv);
        pptMarkToolClearIv = findViewById(R.id.plvlc_ppt_mark_tool_clear_iv);
        pptMarkToolColorGroup = findViewById(R.id.plvlc_ppt_mark_tool_color_group);
        pptMarkToolColorRedView = findViewById(R.id.plvlc_ppt_mark_tool_color_red_view);
        pptMarkToolColorBlueView = findViewById(R.id.plvlc_ppt_mark_tool_color_blue_view);
        pptMarkToolColorGreenView = findViewById(R.id.plvlc_ppt_mark_tool_color_green_view);
        pptMarkToolColorYellowView = findViewById(R.id.plvlc_ppt_mark_tool_color_yellow_view);
        pptMarkToolColorBlackView = findViewById(R.id.plvlc_ppt_mark_tool_color_black_view);
        pptMarkToolColorWhiteView = findViewById(R.id.plvlc_ppt_mark_tool_color_white_view);
        pptMarkToolStateLl = findViewById(R.id.plvlc_ppt_mark_tool_state_ll);
        pptMarkUndoIv = findViewById(R.id.plvlc_ppt_mark_undo_iv);
        pptMarkToolCurrentColorView = findViewById(R.id.plvlc_ppt_mark_tool_current_color_view);
        pptCurrentMarkToolIv = findViewById(R.id.plvlc_ppt_current_mark_tool_iv);
    }

    private void initEnumToViewMap() {
        markToolViewMap = mapOf(
                pair(PLVLCMarkToolEnums.MarkTool.PEN, pptMarkToolPenIv),
                pair(PLVLCMarkToolEnums.MarkTool.RECT, pptMarkToolRectIv),
                pair(PLVLCMarkToolEnums.MarkTool.ARROW, pptMarkToolArrowIv),
                pair(PLVLCMarkToolEnums.MarkTool.TEXT, pptMarkToolTextIv),
                pair(PLVLCMarkToolEnums.MarkTool.ERASER, pptMarkToolEraserIv),
                pair(PLVLCMarkToolEnums.MarkTool.CLEAR, pptMarkToolClearIv)
        );

        colorViewMap = mapOf(
                pair(PLVLCMarkToolEnums.Color.RED, pptMarkToolColorRedView),
                pair(PLVLCMarkToolEnums.Color.BLUE, pptMarkToolColorBlueView),
                pair(PLVLCMarkToolEnums.Color.GREEN, pptMarkToolColorGreenView),
                pair(PLVLCMarkToolEnums.Color.YELLOW, pptMarkToolColorYellowView),
                pair(PLVLCMarkToolEnums.Color.BLACK, pptMarkToolColorBlackView),
                pair(PLVLCMarkToolEnums.Color.WHITE, pptMarkToolColorWhiteView)
        );
    }

    private void setOnClickMarkToolSelector() {
        foreach(markToolViewMap.entrySet(), new PLVSugarUtil.Consumer<Map.Entry<PLVLCMarkToolEnums.MarkTool, PLVRoundImageView>>() {
            @Override
            public void accept(Map.Entry<PLVLCMarkToolEnums.MarkTool, PLVRoundImageView> entry) {
                final PLVLCMarkToolEnums.MarkTool markTool = entry.getKey();
                final PLVRoundImageView view = entry.getValue();
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCurrentSelectedMarkTool(markTool);
                        if (onMarkToolActionListener != null) {
                            onMarkToolActionListener.onChangeMarkTool(markTool);
                        }
                        currentMarkToolGroupShowType = PLVLCMarkToolEnums.MarkToolGroupShowType.NONE;
                        updateMarkToolGroupVisibility();
                    }
                });
            }
        });

        pptMarkToolClearIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new PLVConfirmDialog(getContext())
                        .setTitleVisibility(GONE)
                        .setContent("清屏后笔迹将无法恢复，确定清屏吗")
                        .setLeftButtonText("按错了")
                        .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                            }
                        })
                        .setRightButtonText("确定")
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                if (onMarkToolActionListener != null) {
                                    onMarkToolActionListener.onChangeMarkTool(PLVLCMarkToolEnums.MarkTool.CLEAR);
                                }
                                currentMarkToolGroupShowType = PLVLCMarkToolEnums.MarkToolGroupShowType.NONE;
                                updateMarkToolGroupVisibility();
                            }
                        })
                        .show();
            }
        });
    }

    private void setOnClickColorSelector() {
        foreach(colorViewMap.entrySet(), new PLVSugarUtil.Consumer<Map.Entry<PLVLCMarkToolEnums.Color, PLVRoundColorView>>() {
            @Override
            public void accept(Map.Entry<PLVLCMarkToolEnums.Color, PLVRoundColorView> entry) {
                final PLVLCMarkToolEnums.Color color = entry.getKey();
                final PLVRoundColorView view = entry.getValue();
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCurrentSelectedColor(color);
                        if (onMarkToolActionListener != null) {
                            onMarkToolActionListener.onChangeColor(color);
                        }
                        currentMarkToolGroupShowType = PLVLCMarkToolEnums.MarkToolGroupShowType.NONE;
                        updateMarkToolGroupVisibility();
                    }
                });
            }
        });
    }

    private void setOnClickMarkToolGroup() {
        pptExitMarkToolLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                exitPaintMode();
            }
        });
        pptMarkUndoIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMarkToolActionListener != null) {
                    onMarkToolActionListener.onUndo();
                }
            }
        });
        pptMarkToolCurrentColorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMarkToolGroupShowType = currentMarkToolGroupShowType == PLVLCMarkToolEnums.MarkToolGroupShowType.NONE ?
                        PLVLCMarkToolEnums.MarkToolGroupShowType.COLOR : PLVLCMarkToolEnums.MarkToolGroupShowType.NONE;
                updateMarkToolGroupVisibility();
            }
        });
        pptCurrentMarkToolIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMarkToolGroupShowType = currentMarkToolGroupShowType == PLVLCMarkToolEnums.MarkToolGroupShowType.NONE ?
                        PLVLCMarkToolEnums.MarkToolGroupShowType.MARK_TOOL : PLVLCMarkToolEnums.MarkToolGroupShowType.NONE;
                updateMarkToolGroupVisibility();
            }
        });
    }

    private void callbackCurrentMarkToolState() {
        if (onMarkToolActionListener != null) {
            onMarkToolActionListener.onChangeMarkTool(currentMarkTool);
            onMarkToolActionListener.onChangeColor(currentColor);
        }
    }

    private void initControllerState() {
        updateCurrentSelectedMarkTool(PLVLCMarkToolEnums.MarkTool.getDefaultMarkTool());
        updateCurrentSelectedColor(PLVLCMarkToolEnums.Color.getDefaultColor());
    }

    private void observePaintAbility() {
        PLVUserAbilityManager.myAbility().addUserAbilityChangeListener(new WeakReference<>(userAbilityChangedListener = new PLVUserAbilityManager.OnUserAbilityChangedListener() {
            @Override
            public void onUserAbilitiesChanged(@NonNull List<PLVUserAbility> addedAbilities, @NonNull List<PLVUserAbility> removedAbilities) {
                if (removedAbilities.contains(PLVUserAbility.LIVE_DOCUMENT_ALLOW_USE_PAINT_ON_LINKMIC)) {
                    exitPaintMode();
                }
            }
        }));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void enterPaintMode() {
        if (!PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.LIVE_DOCUMENT_ALLOW_USE_PAINT_ON_LINKMIC)) {
            return;
        }
        if (isInPaintMode()) {
            return;
        }

        setVisibility(View.VISIBLE);
        if (onMarkToolActionListener != null) {
            onMarkToolActionListener.onPaintModeChanged(true);
        }
        callbackCurrentMarkToolState();
        PLVToast.Builder.context(getContext())
                .setText("进入画笔模式")
                .show();
    }

    public void exitPaintMode() {
        if (!isInPaintMode()) {
            return;
        }

        setVisibility(View.GONE);
        if (onMarkToolActionListener != null) {
            onMarkToolActionListener.onPaintModeChanged(false);
        }
        PLVToast.Builder.context(getContext())
                .setText("已退出画笔模式")
                .show();
    }

    public boolean isInPaintMode() {
        return getVisibility() == View.VISIBLE;
    }

    public void setOnMarkToolActionListener(OnMarkToolActionListener onMarkToolActionListener) {
        this.onMarkToolActionListener = onMarkToolActionListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && isInPaintMode()) {
            exitPaintMode();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI更新">

    private void updateCurrentSelectedMarkTool(PLVLCMarkToolEnums.MarkTool currentMarkTool) {
        this.currentMarkTool = currentMarkTool;
        foreach(markToolViewMap.values(), new PLVSugarUtil.Consumer<PLVRoundImageView>() {
            @Override
            public void accept(PLVRoundImageView it) {
                it.setSelected(false);
            }
        });
        markToolViewMap.get(currentMarkTool).setSelected(true);
        pptCurrentMarkToolIv.setImageDrawable(markToolViewMap.get(currentMarkTool).getDrawable());
        pptMarkToolCurrentColorView.setVisibility(currentMarkTool.isShowColor() ? View.VISIBLE : View.GONE);
    }

    private void updateCurrentSelectedColor(PLVLCMarkToolEnums.Color currentColor) {
        this.currentColor = currentColor;
        final int selectedSecondColor = Color.WHITE;
        final int selectedBackgroundColor = Color.parseColor("#99000000");

        foreach(colorViewMap.values(), new PLVSugarUtil.Consumer<PLVRoundColorView>() {
            @Override
            public void accept(PLVRoundColorView it) {
                it.updateSecondColor(Color.TRANSPARENT);
                it.updateBackgroundColor(Color.TRANSPARENT);
            }
        });
        colorViewMap.get(currentColor).updateSecondColor(selectedSecondColor);
        colorViewMap.get(currentColor).updateBackgroundColor(selectedBackgroundColor);
        pptMarkToolCurrentColorView.updateMainColor(Color.parseColor(currentColor.getColorString()));
    }

    private void updateMarkToolGroupVisibility() {
        pptMarkToolGroup.setVisibility(currentMarkToolGroupShowType == PLVLCMarkToolEnums.MarkToolGroupShowType.MARK_TOOL ? VISIBLE : GONE);
        pptMarkToolColorGroup.setVisibility(currentMarkToolGroupShowType == PLVLCMarkToolEnums.MarkToolGroupShowType.COLOR ? VISIBLE : GONE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnMarkToolActionListener {

        /**
         * 进出画笔模式回调
         */
        void onPaintModeChanged(boolean isInPaintMode);

        /**
         * 画笔工具类型变更回调
         */
        void onChangeMarkTool(PLVLCMarkToolEnums.MarkTool newMarkTool);

        /**
         * 画笔工具颜色变更回调
         */
        void onChangeColor(PLVLCMarkToolEnums.Color newColor);

        /**
         * 撤回上一次画笔操作
         */
        void onUndo();

    }

    // </editor-fold>

}
