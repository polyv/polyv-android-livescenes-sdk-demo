package com.easefun.polyv.livehiclass.modules.document.popuplist.widget;

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
import com.easefun.polyv.livehiclass.R;

/**
 * PPT文档列表视图项 上传状态指示视图
 *
 * @author suhongtao
 */
public class PLVHCPptUploadProgressView extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 常量枚举 - 显示类型
    public static final int SHOW_TYPE_NONE = 0;
    public static final int SHOW_TYPE_PROGRESS = 1;
    public static final int SHOW_TYPE_TEXT = 2;

    // 子View
    private View rootView;
    private PLVCircleProgressView documentUploadProgress;
    private LinearLayout documentUploadHintLl;
    private ImageView documentUploadHintAlertIv;
    private TextView documentUploadHintContentTv;
    private TextView documentUploadHintBtn;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCPptUploadProgressView(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCPptUploadProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCPptUploadProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_document_ppt_upload_progress_layout, this);
        findView();
    }

    private void findView() {
        documentUploadProgress = (PLVCircleProgressView) rootView.findViewById(R.id.plvhc_document_upload_progress);
        documentUploadHintLl = (LinearLayout) rootView.findViewById(R.id.plvhc_document_upload_hint_ll);
        documentUploadHintAlertIv = (ImageView) rootView.findViewById(R.id.plvhc_document_upload_hint_alert_iv);
        documentUploadHintContentTv = (TextView) rootView.findViewById(R.id.plvhc_document_upload_hint_content_tv);
        documentUploadHintBtn = (TextView) rootView.findViewById(R.id.plvhc_document_upload_hint_btn);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 视图显示内容设置">

    public void setShowType(int showType) {
        if (showType == SHOW_TYPE_NONE) {
            documentUploadProgress.setVisibility(GONE);
            documentUploadHintLl.setVisibility(GONE);
        } else if (showType == SHOW_TYPE_PROGRESS) {
            documentUploadProgress.setVisibility(VISIBLE);
            documentUploadHintLl.setVisibility(GONE);
        } else if (showType == SHOW_TYPE_TEXT) {
            documentUploadProgress.setVisibility(GONE);
            documentUploadHintLl.setVisibility(VISIBLE);
        }
    }

    public void setUploadProgress(@IntRange(from = 0, to = 100) int progress) {
        if (progress >= 0 && progress <= 100) {
            documentUploadProgress.setProgress(progress);
        }
    }

    public void setHintAlertIconVisibility(int visibility) {
        documentUploadHintAlertIv.setVisibility(visibility);
    }

    public void setHintContent(String hint) {
        documentUploadHintContentTv.setText(hint);
    }

    public void setHintContentTextColor(@ColorInt int color) {
        documentUploadHintContentTv.setTextColor(color);
    }

    public void setButtonVisibility(int visibility) {
        documentUploadHintBtn.setVisibility(visibility);
    }

    public void setButtonText(String buttonText) {
        documentUploadHintBtn.setText(buttonText);
    }

    public void setButtonOnClickListener(OnClickListener listener) {
        documentUploadHintBtn.setOnClickListener(listener);
    }

    // </editor-fold>

}
