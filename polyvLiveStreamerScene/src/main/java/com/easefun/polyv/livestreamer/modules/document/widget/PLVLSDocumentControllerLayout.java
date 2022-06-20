package com.easefun.polyv.livestreamer.modules.document.widget;

import static com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter.AUTO_ID_WHITE_BOARD;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMarkToolType;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundBorderColorView;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.document.model.PLVSPPTStatus;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 文档区域控制栏布局
 *
 * @author suhongtao
 */
public class PLVLSDocumentControllerLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 子View
    private View rootView;
    private LinearLayout plvlsDocumentControllerLl;
    private ImageView plvlsDocumentLastPageIv;
    private ImageView plvlsDocumentNextPageIv;
    private ImageView plvlsDocumentFullscreenIv;
    private ImageView plvlsDocumentWhiteboardAddIv;
    private PLVLSDocumentControllerExpandMenu plvlsDocumentMarkMenu;
    private HorizontalScrollView plvlsDocumentMarkSv;
    private LinearLayout plvlsDocumentMarkLl;
    private PLVRoundBorderColorView plvlsDocumentPaintColorRedIv;
    private PLVRoundBorderColorView plvlsDocumentPaintColorBlueIv;
    private PLVRoundBorderColorView plvlsDocumentPaintColorGreenIv;
    private PLVRoundBorderColorView plvlsDocumentPaintColorYellowIv;
    private PLVRoundBorderColorView plvlsDocumentPaintColorGreyIv;
    private PLVRoundBorderColorView plvlsDocumentPaintColorWhiteIv;
    private View plvlsDocumentPaintSeparator;
    private ImageView plvlsDocumentMarkToolBrushIv;
    private ImageView plvlsDocumentMarkToolArrowIv;
    private ImageView plvlsDocumentMarkToolTextIv;
    private ImageView plvlsDocumentMarkToolEraserIv;
    private ImageView plvlsDocumentMarkToolClearIv;
    private TextView plvlsDocumentPageIndicateTv;

    // 子View列表 - 颜色选择控件
    private List<PLVRoundBorderColorView> colorSelectorViewList;
    // 子View列表 - 标注工具选择列表
    private List<ImageView> markToolSelectorViewList;

    // 回调接口 - 颜色选择改变
    private OnChangeColorListener onChangeColorListener;
    // 回调接口 - 标注工具选择改变
    private OnChangeMarkToolListener onChangeMarkToolListener;
    // 回调接口 - 白板页面改变
    private OnChangePptPageListener onChangePptPageListener;
    // 调用外部接口 - 切换全屏
    private SwitchFullScreenListener switchFullScreenListener;

    /**
     * MVP - View
     * 请勿改为局部变量，否则会被gc回收，引起无法响应Presenter调用
     */
    private PLVAbsDocumentView documentMvpView;

    // 当前文档ID
    private int currentAutoId;
    // 当前页面索引，从0开始，就是当前auto Id
    private int currentPageIndex = 0;
    /**
     * 页面总数
     * Key: autoId
     * Value: 总页面数，autoId为0时为总白板数
     */
    private SparseArray<Integer> autoId2PageCountMap = new SparseArray<>();
    private boolean isShowByGuest;

    private boolean isBeautyLayoutShowing = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSDocumentControllerLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSDocumentControllerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSDocumentControllerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvls_document_controller_layout, this);
        findView();
        initColorSelector();
        initMarkToolSelector();
        initPptPageSelector();
        initFullScreenOnClickListener();
        initDocumentMvpView();

        observeBeautyLayoutStatus();

        // 首次进入时收起标注工具栏
        plvlsDocumentMarkMenu.close();
    }

    private void findView() {
        plvlsDocumentControllerLl = (LinearLayout) rootView.findViewById(R.id.plvls_document_controller_ll);
        plvlsDocumentLastPageIv = (ImageView) rootView.findViewById(R.id.plvls_document_last_page_iv);
        plvlsDocumentNextPageIv = (ImageView) rootView.findViewById(R.id.plvls_document_next_page_iv);
        plvlsDocumentFullscreenIv = (ImageView) rootView.findViewById(R.id.plvls_document_fullscreen_iv);
        plvlsDocumentWhiteboardAddIv = (ImageView) rootView.findViewById(R.id.plvls_document_whiteboard_add_iv);
        plvlsDocumentMarkMenu = (PLVLSDocumentControllerExpandMenu) rootView.findViewById(R.id.plvls_document_mark_menu);
        plvlsDocumentMarkSv = (HorizontalScrollView) rootView.findViewById(R.id.plvls_document_mark_sv);
        plvlsDocumentMarkLl = (LinearLayout) rootView.findViewById(R.id.plvls_document_mark_ll);
        plvlsDocumentPaintColorRedIv = (PLVRoundBorderColorView) rootView.findViewById(R.id.plvls_document_paint_color_red_iv);
        plvlsDocumentPaintColorBlueIv = (PLVRoundBorderColorView) rootView.findViewById(R.id.plvls_document_paint_color_blue_iv);
        plvlsDocumentPaintColorGreenIv = (PLVRoundBorderColorView) rootView.findViewById(R.id.plvls_document_paint_color_green_iv);
        plvlsDocumentPaintColorYellowIv = (PLVRoundBorderColorView) rootView.findViewById(R.id.plvls_document_paint_color_yellow_iv);
        plvlsDocumentPaintColorGreyIv = (PLVRoundBorderColorView) rootView.findViewById(R.id.plvls_document_paint_color_grey_iv);
        plvlsDocumentPaintColorWhiteIv = (PLVRoundBorderColorView) rootView.findViewById(R.id.plvls_document_paint_color_white_iv);
        plvlsDocumentPaintSeparator = (View) rootView.findViewById(R.id.plvls_document_paint_separator);
        plvlsDocumentMarkToolBrushIv = (ImageView) rootView.findViewById(R.id.plvls_document_mark_tool_brush_iv);
        plvlsDocumentMarkToolArrowIv = (ImageView) rootView.findViewById(R.id.plvls_document_mark_tool_arrow_iv);
        plvlsDocumentMarkToolTextIv = (ImageView) rootView.findViewById(R.id.plvls_document_mark_tool_text_iv);
        plvlsDocumentMarkToolEraserIv = (ImageView) rootView.findViewById(R.id.plvls_document_mark_tool_eraser_iv);
        plvlsDocumentMarkToolClearIv = (ImageView) rootView.findViewById(R.id.plvls_document_mark_tool_clear_iv);
        plvlsDocumentPageIndicateTv = (TextView) rootView.findViewById(R.id.plvls_document_page_indicate_tv);
    }

    /**
     * 初始化颜色选择控件
     */
    private void initColorSelector() {
        if (colorSelectorViewList == null) {
            colorSelectorViewList = new ArrayList<>();
        } else {
            colorSelectorViewList.clear();
        }
        colorSelectorViewList.add(plvlsDocumentPaintColorRedIv);
        colorSelectorViewList.add(plvlsDocumentPaintColorBlueIv);
        colorSelectorViewList.add(plvlsDocumentPaintColorGreenIv);
        colorSelectorViewList.add(plvlsDocumentPaintColorYellowIv);
        colorSelectorViewList.add(plvlsDocumentPaintColorGreyIv);
        colorSelectorViewList.add(plvlsDocumentPaintColorWhiteIv);

        for (final PLVRoundBorderColorView clickedColorView : colorSelectorViewList) {
            clickedColorView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (PLVRoundBorderColorView colorView : colorSelectorViewList) {
                        colorView.setChecked(v.equals(colorView));
                    }
                    if (onChangeColorListener != null) {
                        onChangeColorListener.onChangeColor(clickedColorView.getBackgroundColorString());
                    }
                }
            });
        }
    }

    /**
     * 初始化标注工具控件
     */
    private void initMarkToolSelector() {
        if (markToolSelectorViewList == null) {
            markToolSelectorViewList = new ArrayList<>();
        } else {
            markToolSelectorViewList.clear();
        }
        markToolSelectorViewList.add(plvlsDocumentMarkToolBrushIv);
        markToolSelectorViewList.add(plvlsDocumentMarkToolArrowIv);
        markToolSelectorViewList.add(plvlsDocumentMarkToolTextIv);
        markToolSelectorViewList.add(plvlsDocumentMarkToolEraserIv);
        markToolSelectorViewList.add(plvlsDocumentMarkToolClearIv);

        // 画笔工具
        plvlsDocumentMarkToolBrushIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorSelectorVisibility(VISIBLE);
                changeMarkToolSelectedType(v);
                callbackMarkToolType(PLVDocumentMarkToolType.BRUSH);
                plvlsDocumentMarkMenu.requestLayout();
            }
        });

        // 箭头工具
        plvlsDocumentMarkToolArrowIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorSelectorVisibility(VISIBLE);
                changeMarkToolSelectedType(v);
                callbackMarkToolType(PLVDocumentMarkToolType.ARROW);
                plvlsDocumentMarkMenu.requestLayout();
            }
        });

        // 文本工具
        plvlsDocumentMarkToolTextIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorSelectorVisibility(VISIBLE);
                changeMarkToolSelectedType(v);
                callbackMarkToolType(PLVDocumentMarkToolType.TEXT);
                plvlsDocumentMarkMenu.requestLayout();
            }
        });

        // 橡皮擦工具
        plvlsDocumentMarkToolEraserIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorSelectorVisibility(GONE);
                changeMarkToolSelectedType(v);
                callbackMarkToolType(PLVDocumentMarkToolType.ERASER);
                plvlsDocumentMarkMenu.requestLayout();
            }
        });

        // 清空标注数据工具
        plvlsDocumentMarkToolClearIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorSelectorVisibility(GONE);
                changeMarkToolSelectedType(v);
                callbackMarkToolType(PLVDocumentMarkToolType.CLEAR);
                plvlsDocumentMarkMenu.requestLayout();
            }
        });
    }

    /**
     * 初始化白板页数控件
     */
    private void initPptPageSelector() {
        // 首次进入只有1个白板
        currentAutoId = AUTO_ID_WHITE_BOARD;
        currentPageIndex = 0;
        autoId2PageCountMap.put(AUTO_ID_WHITE_BOARD, 1);

        // 只有1个白板时不显示白板页面指示和上下页切换按钮
        plvlsDocumentPageIndicateTv.setVisibility(GONE);
        updatePageSelectorVisibility();

        plvlsDocumentWhiteboardAddIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点一次按钮多一个白板

                int pageCount = autoId2PageCountMap.get(AUTO_ID_WHITE_BOARD, 1);
                pageCount++;
                autoId2PageCountMap.put(AUTO_ID_WHITE_BOARD, pageCount);

                // 切换到新增的白板
                currentPageIndex = pageCount - 1;
                updatePageIndicator(AUTO_ID_WHITE_BOARD, currentPageIndex);
                //回调，发送到presenter来更新数据层
                if (onChangePptPageListener != null) {
                    onChangePptPageListener.onChangePage(currentPageIndex);
                }

                PLVToast.Builder.context(getContext())
                        .setText("新增白板成功")
                        .build()
                        .show();
            }
        });

        // 切换至上一步
        plvlsDocumentLastPageIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVDocumentPresenter.getInstance().changePptToLastStep();
            }
        });

        // 切换至下一步
        plvlsDocumentNextPageIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVDocumentPresenter.getInstance().changePptToNextStep();
            }
        });
    }

    /**
     * 初始化点击全屏按钮监听
     */
    private void initFullScreenOnClickListener() {
        plvlsDocumentFullscreenIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFullScreenListener == null) {
                    return;
                }
                boolean toFullScreen = switchFullScreenListener.switchFullScreen();
                notifyDocumentLayoutSizeChange(toFullScreen);
                plvlsDocumentMarkMenu.requestLayout();
            }
        });
    }

    /**
     * 初始化 MVP - View
     */
    private void initDocumentMvpView() {
        documentMvpView = new PLVAbsDocumentView() {

            @Override
            public void onPptPageList(@Nullable PLVSPPTJsModel plvspptJsModel) {
                if (plvspptJsModel == null) {
                    return;
                }
                int pageCount = plvspptJsModel.getPPTImages().size();
                autoId2PageCountMap.put(plvspptJsModel.getAutoId(), pageCount);
            }

            //用户交互操作发送到JS时，会收到来自JS的文档变化回调
            @Override
            public void onPptStatusChange(PLVSPPTStatus pptStatus) {
                currentAutoId = pptStatus.getAutoId();
                currentPageIndex = pptStatus.getPageId();
                autoId2PageCountMap.put(currentAutoId, pptStatus.getTotal());

                updatePageIndicator(currentAutoId, currentPageIndex);

                /*
                更新画笔和添加白板按钮状态
                 */
                boolean isWhiteBoard = currentAutoId == AUTO_ID_WHITE_BOARD;
                //添加白板控件和画笔控件
                if (isWhiteBoard) {
                    if (PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_WHITE_BOARD_ADD)) {
                        plvlsDocumentWhiteboardAddIv.setVisibility(VISIBLE);
                    }
                    plvlsDocumentMarkMenu.setRightIconResId(R.drawable.plvls_document_mark_active);
                } else {
                    // PPT文档模式 不显示白板相关按钮
                    plvlsDocumentWhiteboardAddIv.setVisibility(GONE);
                    plvlsDocumentMarkMenu.setRightIconResId(R.drawable.plvls_document_mark_inactive);
                }
            }

            @Override
            public void onUserPermissionChange() {
                updatePageSelectorVisibility();
            }
        };

        PLVDocumentPresenter.getInstance().registerView(documentMvpView);
    }

    private void observeBeautyLayoutStatus() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        PLVLSDocumentControllerLayout.this.isBeautyLayoutShowing = beautyUiState != null && beautyUiState.isBeautyMenuShowing;
                        updateVisibility();
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 初始化标注工具类型和颜色
     */
    public void initMarkToolAndColor() {
        // 选择为画笔工具
        plvlsDocumentMarkToolBrushIv.callOnClick();
        // 选择为红色
        plvlsDocumentPaintColorRedIv.callOnClick();
    }

    /**
     * 显示控制栏
     */
    public void show() {
        rootView.setVisibility(View.VISIBLE);
    }

    /**
     * 显示嘉宾控制器
     */
    public void showByGuest() {
        isShowByGuest = true;
        show();
        int childCount = plvlsDocumentControllerLl.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = plvlsDocumentControllerLl.getChildAt(i);
            if (view != null) {
                view.setVisibility(INVISIBLE);
            }
        }
        plvlsDocumentFullscreenIv.setVisibility(VISIBLE);
    }

    /**
     * 隐藏控制栏
     */
    public void hide() {
        // 隐藏时收起标注工具菜单
        closeMarkToolMenu();
        rootView.setVisibility(View.GONE);
    }

    /**
     * 折起标注工具菜单
     */
    public void closeMarkToolMenu() {
        plvlsDocumentMarkMenu.close();
    }

    /**
     * 文档布局大小改变
     *
     * @param toFullScreen true切换至文档全屏
     */
    public void notifyDocumentLayoutSizeChange(boolean toFullScreen) {
        if (toFullScreen) {
            plvlsDocumentFullscreenIv.setImageResource(R.drawable.plvls_document_small_screen);
        } else {
            plvlsDocumentFullscreenIv.setImageResource(R.drawable.plvls_document_full_screen);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    /**
     * 改变颜色选择控件可见性
     * 只有选中了画笔、箭头和文本工具才显示颜色选择控件
     *
     * @param visibility
     */
    private void setColorSelectorVisibility(int visibility) {
        if (colorSelectorViewList == null) {
            initColorSelector();
        }
        for (PLVRoundBorderColorView colorView : colorSelectorViewList) {
            colorView.setVisibility(visibility);
        }
        plvlsDocumentPaintSeparator.setVisibility(visibility);
        plvlsDocumentMarkMenu.requestLayout();
    }

    /**
     * 改变标注工具选中状态
     *
     * @param v 选中的标注工具视图
     */
    private void changeMarkToolSelectedType(View v) {
        if (markToolSelectorViewList == null) {
            initMarkToolSelector();
        }
        if (v == null) {
            return;
        }
        for (ImageView markToolImageView : markToolSelectorViewList) {
            markToolImageView.setSelected(v.equals(markToolImageView));
        }
    }

    /**
     * 更新上下页按钮状态
     * 首页不可再切上一页，末页不可再切下一页
     *
     * @param pageIndex 页面索引
     * @param pageCount 总页面
     */
    private void updatePageSelectorEnabledType(int pageIndex, int pageCount) {
        final boolean isFirstPage = pageIndex <= 0;
        final boolean isLastPage = pageIndex >= pageCount - 1;
        if (isFirstPage) {
            plvlsDocumentLastPageIv.setEnabled(false);
            plvlsDocumentNextPageIv.setEnabled(true);
        } else if (isLastPage) {
            plvlsDocumentLastPageIv.setEnabled(true);
            plvlsDocumentNextPageIv.setEnabled(false);
        } else {
            plvlsDocumentLastPageIv.setEnabled(true);
            plvlsDocumentNextPageIv.setEnabled(true);
        }
    }

    //更新页面指示器
    private void updatePageIndicator(int autoId, int pageIndex) {
        //翻页控件
        final int pageCount = getDocumentPageCount(autoId);
        final boolean showIndicateText = pageCount > 1;
        //pageIndex+1是因为返回的每次是数组index，要+1才正常。
        plvlsDocumentPageIndicateTv.setText(String.format(Locale.getDefault(), "%d/%d", pageIndex + 1, pageCount));
        plvlsDocumentPageIndicateTv.setVisibility(showIndicateText ? View.VISIBLE : View.GONE);
        updatePageSelectorVisibility();
        updatePageSelectorEnabledType(pageIndex, pageCount);
    }

    // 更新翻页指示器可见性
    private void updatePageSelectorVisibility() {
        boolean isShow = getCurrentDocumentPageCount() > 1 && PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_TURN_PAGE);
        plvlsDocumentLastPageIv.setVisibility(isShow ? VISIBLE : GONE);
        plvlsDocumentNextPageIv.setVisibility(isShow ? VISIBLE : GONE);
    }

    private int getDocumentPageCount(int autoId) {
        // 默认至少有1页
        return autoId2PageCountMap.get(autoId, 1);
    }

    private int getCurrentDocumentPageCount() {
        return getDocumentPageCount(currentAutoId);
    }

    private void updateVisibility() {
        if (isBeautyLayoutShowing) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回调接口相关">

    public void setOnChangeColorListener(OnChangeColorListener onChangeColorListener) {
        this.onChangeColorListener = onChangeColorListener;
    }

    public void setOnChangeMarkToolListener(OnChangeMarkToolListener onChangeMarkToolListener) {
        this.onChangeMarkToolListener = onChangeMarkToolListener;
    }

    public void setOnChangePptPageListener(OnChangePptPageListener onChangePptPageListener) {
        this.onChangePptPageListener = onChangePptPageListener;
    }

    public void setMarkToolMenuOnFoldExpandListener(PLVLSDocumentControllerExpandMenu.OnFoldExpandListener onFoldExpandListener) {
        plvlsDocumentMarkMenu.setOnFoldExpandListener(onFoldExpandListener);
    }

    public void setSwitchFullScreenListener(SwitchFullScreenListener switchFullScreenListener) {
        this.switchFullScreenListener = switchFullScreenListener;
    }

    private void callbackMarkToolType(@NonNull String markToolType) {
        if (onChangeMarkToolListener != null) {
            onChangeMarkToolListener.onChangeMarkTool(markToolType);
        }
    }

    /**
     * 切换选择的颜色控件回调
     */
    public interface OnChangeColorListener {
        /**
         * 切换颜色
         *
         * @param colorString 颜色16进制字符串，如#AABBCC
         */
        void onChangeColor(String colorString);
    }

    /**
     * 切换标注工具回调
     */
    public interface OnChangeMarkToolListener {
        /**
         * 切换选择的标注工具
         *
         * @param markToolType {@link PLVDocumentMarkToolType}
         */
        void onChangeMarkTool(@PLVDocumentMarkToolType.Range String markToolType);
    }

    /**
     * 切换文档页面
     */
    public interface OnChangePptPageListener {
        /**
         * 切换文档页面
         *
         * @param pageId 页面id
         */
        void onChangePage(int pageId);
    }

    /**
     * 切换全屏调用接口，由外部实现切换逻辑
     */
    public interface SwitchFullScreenListener {
        /**
         * 切换至全屏/正常大小
         *
         * @return true切换至全屏，false切换至正常大小
         */
        boolean switchFullScreen();
    }

    // </editor-fold>

}
