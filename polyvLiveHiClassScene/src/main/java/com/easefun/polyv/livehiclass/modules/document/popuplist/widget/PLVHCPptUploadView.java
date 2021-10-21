package com.easefun.polyv.livehiclass.modules.document.popuplist.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.document.popuplist.vo.PLVHCPptVO;

/**
 * PPT文档上传按钮列表项视图
 *
 * @author suhongtao
 */
public class PLVHCPptUploadView extends PLVHCAbsPptViewItem {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private View rootView;
    private ImageView documentPptItemIv;
    private LinearLayout documentPptItemLl;

    private PopupWindow popupWindow;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCPptUploadView(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCPptUploadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCPptUploadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_document_ppt_upload_item, this);
        findView();
        setOnClickListener();
    }

    private void findView() {
        documentPptItemIv = (ImageView) rootView.findViewById(R.id.plvhc_document_ppt_item_iv);
        documentPptItemLl = (LinearLayout) rootView.findViewById(R.id.plvhc_document_ppt_item_ll);
    }

    /**
     * 设置点击监听
     */
    private void setOnClickListener() {
        documentPptItemIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity) {
                    PLVFileChooseUtils.chooseFile((Activity) getContext(), PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT);
                }
            }
        });

        documentPptItemLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow == null) {
                    popupWindow = new PopupWindow(v.getContext());
                    View root = LayoutInflater.from(v.getContext()).inflate(R.layout.plvhc_document_upload_tips, null, false);
                    popupWindow.setContentView(root);
                    popupWindow.setFocusable(true);
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setBackgroundDrawable(new ColorDrawable());
                    popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                    popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                    root.findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });
                }
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onMeasure重写 - 动态调整尺寸">

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        GridLayoutManager.LayoutParams rlLp = (GridLayoutManager.LayoutParams) rootView.getLayoutParams();
        rlLp.leftMargin = rlLp.rightMargin = ITEM_MARGIN_LEFT;

        RelativeLayout.LayoutParams ivLp = (RelativeLayout.LayoutParams) documentPptItemIv.getLayoutParams();
        ivLp.width = ITEM_PICTURE_WIDTH;
        ivLp.height = ITEM_PICTURE_HEIGHT;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    @Override
    public void processData(PLVHCPptVO pptVO) {
        // Not implemented.
    }

    // </editor-fold>

}
