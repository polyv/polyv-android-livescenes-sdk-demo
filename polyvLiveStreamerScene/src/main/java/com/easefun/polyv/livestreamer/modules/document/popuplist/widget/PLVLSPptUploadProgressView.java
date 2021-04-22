package com.easefun.polyv.livestreamer.modules.document.popuplist.widget;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.roundview.PLVCircleProgressView;
import com.easefun.polyv.livestreamer.R;

/**
 * PPT文档列表视图项 上传状态指示视图
 *
 * @author suhongtao
 */
public class PLVLSPptUploadProgressView extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 常量枚举 - 显示类型
    public static final int SHOW_TYPE_NONE = 0;
    public static final int SHOW_TYPE_PROGRESS = 1;
    public static final int SHOW_TYPE_TEXT = 2;

    // 子View
    private View rootView;
    private PLVCircleProgressView plvlsDocumentUploadProgress;
    private LinearLayout plvlsDocumentUploadHintLl;
    private ImageView plvlsDocumentUploadHintAlertIv;
    private TextView plvlsDocumentUploadHintContentTv;
    private TextView plvlsDocumentUploadHintBtn;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSPptUploadProgressView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSPptUploadProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSPptUploadProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvls_document_ppt_upload_progress_layout, this);
        findView();
    }

    private void findView() {
        plvlsDocumentUploadProgress = (PLVCircleProgressView) rootView.findViewById(R.id.plvls_document_upload_progress);
        plvlsDocumentUploadHintLl = (LinearLayout) rootView.findViewById(R.id.plvls_document_upload_hint_ll);
        plvlsDocumentUploadHintAlertIv = (ImageView) rootView.findViewById(R.id.plvls_document_upload_hint_alert_iv);
        plvlsDocumentUploadHintContentTv = (TextView) rootView.findViewById(R.id.plvls_document_upload_hint_content_tv);
        plvlsDocumentUploadHintBtn = (TextView) rootView.findViewById(R.id.plvls_document_upload_hint_btn);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 视图显示内容设置">

    public void setShowType(int showType) {
        if (showType == SHOW_TYPE_NONE) {
            plvlsDocumentUploadProgress.setVisibility(GONE);
            plvlsDocumentUploadHintLl.setVisibility(GONE);
        } else if (showType == SHOW_TYPE_PROGRESS) {
            plvlsDocumentUploadProgress.setVisibility(VISIBLE);
            plvlsDocumentUploadHintLl.setVisibility(GONE);
        } else if (showType == SHOW_TYPE_TEXT) {
            plvlsDocumentUploadProgress.setVisibility(GONE);
            plvlsDocumentUploadHintLl.setVisibility(VISIBLE);
        }
    }

    public void setUploadProgress(@IntRange(from = 0, to = 100) int progress) {
        if (progress >= 0 && progress <= 100) {
            plvlsDocumentUploadProgress.setProgress(progress);
        }
    }

    public void setHintAlertIconVisibility(int visibility) {
        plvlsDocumentUploadHintAlertIv.setVisibility(visibility);
    }

    public void setHintContent(String hint) {
        plvlsDocumentUploadHintContentTv.setText(hint);
    }

    public void setHintContentTextColor(@ColorInt int color) {
        plvlsDocumentUploadHintContentTv.setTextColor(color);
    }

    public void setButtonVisibility(int visibility) {
        plvlsDocumentUploadHintBtn.setVisibility(visibility);
    }

    public void setButtonText(String buttonText) {
        plvlsDocumentUploadHintBtn.setText(buttonText);
    }

    public void setButtonOnClickListener(View.OnClickListener listener) {
        plvlsDocumentUploadHintBtn.setOnClickListener(listener);
    }

    // </editor-fold>

}
