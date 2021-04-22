package com.easefun.polyv.livestreamer.modules.document.popuplist.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.document.popuplist.vo.PLVLSPptVO;

/**
 * PPT文档列表项视图
 *
 * @author suhongtao
 */
public class PLVLSPptCoverView extends PLVLSAbsPptViewItem {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVLSPptCoverView.class.getSimpleName();

    // 子View
    private View rootView;
    private PLVRoundRectLayout plvlsDocumentPptItemRoundLayout;
    private ImageView plvlsDocumentPptItemIv;
    private View plvlsDocumentPptSelectedMask;
    private ImageView plvlsDocumentPptItemPlaceholderIv;
    private PLVLSPptUploadProgressView plvlsDocumentPptUploadProgressView;
    private LinearLayout plvlsDocumentPptItemLl;
    private TextView plvlsDocumentPptNameTv;
    private TextView plvlsDocumentPptSuffixTv;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSPptCoverView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSPptCoverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSPptCoverView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvls_document_ppt_cover_item, this);
        findView();
    }

    private void findView() {
        plvlsDocumentPptItemRoundLayout = (PLVRoundRectLayout) rootView.findViewById(R.id.plvls_document_ppt_item_round_layout);
        plvlsDocumentPptItemIv = (ImageView) rootView.findViewById(R.id.plvls_document_ppt_item_iv);
        plvlsDocumentPptSelectedMask = (View) rootView.findViewById(R.id.plvls_document_ppt_selected_mask);
        plvlsDocumentPptItemPlaceholderIv = (ImageView) rootView.findViewById(R.id.plvls_document_ppt_item_placeholder_iv);
        plvlsDocumentPptUploadProgressView = (PLVLSPptUploadProgressView) rootView.findViewById(R.id.plvls_document_ppt_upload_progress_view);
        plvlsDocumentPptItemLl = (LinearLayout) rootView.findViewById(R.id.plvls_document_ppt_item_ll);
        plvlsDocumentPptNameTv = (TextView) rootView.findViewById(R.id.plvls_document_ppt_name_tv);
        plvlsDocumentPptSuffixTv = (TextView) rootView.findViewById(R.id.plvls_document_ppt_suffix_tv);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onMeasure重写 - 动态调整尺寸">

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        GridLayoutManager.LayoutParams rlLp = (GridLayoutManager.LayoutParams) rootView.getLayoutParams();
        rlLp.leftMargin = rlLp.rightMargin = ITEM_MARGIN_LEFT;

        RelativeLayout.LayoutParams ivLp = (RelativeLayout.LayoutParams) plvlsDocumentPptItemIv.getLayoutParams();
        ivLp.width = ITEM_PICTURE_WIDTH;
        ivLp.height = ITEM_PICTURE_HEIGHT;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    @Override
    public void processData(PLVLSPptVO pptVO) {
        plvlsDocumentPptItemPlaceholderIv.setVisibility(VISIBLE);
        String urlTag = pptVO.getImage();
        if (urlTag == null) {
            urlTag = pptVO.getFileId();
        }
        if (urlTag == null) {
            urlTag = "";
        }
        PLVImageLoader.getInstance().loadImage(getContext(), TAG, urlTag, R.drawable.plvls_document_list_ppt_placeholder,
                new PLVAbsProgressListener(pptVO.getImage()) {
                    @Override
                    public void onFailed(@Nullable Exception e, Object model) {
                        plvlsDocumentPptItemIv.setImageResource(R.drawable.plvls_document_list_ppt_placeholder);
                    }

                    @Override
                    public void onResourceReady(Drawable drawable) {
                        plvlsDocumentPptItemPlaceholderIv.setVisibility(GONE);
                        plvlsDocumentPptItemIv.setImageDrawable(drawable);
                    }

                    @Override
                    public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {

                    }

                    @Override
                    public void onStart(String url) {

                    }
                }, plvlsDocumentPptItemIv);

        String displayName = pptVO.getName();
        String displaySuffix = pptVO.getSuffix();
        if (displaySuffix != null && displaySuffix.startsWith(".")) {
            displayName = displayName + ".";
            displaySuffix = displaySuffix.replaceFirst("\\.", "");
        }
        plvlsDocumentPptNameTv.setText(displayName);
        plvlsDocumentPptSuffixTv.setText(displaySuffix);

        processUploadStatus(pptVO);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        plvlsDocumentPptNameTv.setSelected(selected);
        plvlsDocumentPptSuffixTv.setSelected(selected);
        if (selected) {
            plvlsDocumentPptSelectedMask.setVisibility(VISIBLE);
        } else {
            plvlsDocumentPptSelectedMask.setVisibility(GONE);
        }
    }

    public void setOnUploadProgressViewButtonClickListener(View.OnClickListener onClickListener) {
        plvlsDocumentPptUploadProgressView.setButtonOnClickListener(onClickListener);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    /**
     * 处理上传状态的视图展示
     *
     * @param pptVO ppt视图对象
     */
    private void processUploadStatus(PLVLSPptVO pptVO) {
        if (pptVO.getUploadStatus() == null) {
            plvlsDocumentPptUploadProgressView.setVisibility(GONE);
            return;
        }

        UploadStatusHelper.withStatus(pptVO.getUploadStatus())
                .parsePptVO(pptVO)
                .dispatchTo(plvlsDocumentPptUploadProgressView);
    }

    /**
     * 上传状态处理
     * 策略模式
     */
    private static class UploadStatusHelper {

        public static Status withStatus(@PLVPptUploadStatus.Range int status) {
            switch (status) {
                case PLVPptUploadStatus.STATUS_UNPREPARED:
                    return new UnpreparedStatus();
                case PLVPptUploadStatus.STATUS_PREPARED:
                    return new PreparedStatus();
                case PLVPptUploadStatus.STATUS_UPLOADING:
                    return new UploadingStatus();
                case PLVPptUploadStatus.STATUS_UPLOAD_FAILED:
                    return new UploadFailedStatus();
                case PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS:
                    return new UploadSuccessStatus();
                case PLVPptUploadStatus.STATUS_CONVERTING:
                    return new ConvertingStatus();
                case PLVPptUploadStatus.STATUS_CONVERT_FAILED:
                    return new ConvertFailStatus();
                case PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS:
                    return new ConvertAnimateLossStatus();
                case PLVPptUploadStatus.STATUS_CONVERT_SUCCESS:
                default:
                    return new ConvertSuccessStatus();
            }
        }

        private static class ProgressViewResult {
            private int progressViewVisibility = View.VISIBLE;
            private int showType = PLVLSPptUploadProgressView.SHOW_TYPE_NONE;
            private int hintAlertIconVisibility = View.VISIBLE;
            private String hintContent;
            private int hintContentTextColor = Color.WHITE;
            private int buttonVisibility = View.VISIBLE;
            private String buttonText;
            private int uploadProgress = 0;

            public void dispatchTo(PLVLSPptUploadProgressView pptUploadProgressView) {
                pptUploadProgressView.setVisibility(progressViewVisibility);
                pptUploadProgressView.setShowType(showType);
                pptUploadProgressView.setHintAlertIconVisibility(hintAlertIconVisibility);
                pptUploadProgressView.setHintContent(hintContent);
                pptUploadProgressView.setHintContentTextColor(hintContentTextColor);
                pptUploadProgressView.setButtonVisibility(buttonVisibility);
                pptUploadProgressView.setButtonText(buttonText);
                pptUploadProgressView.setUploadProgress(uploadProgress);
            }
        }

        private static abstract class Status {
            abstract ProgressViewResult parsePptVO(PLVLSPptVO pptVO);
        }

        private static class UnpreparedStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_NONE;
                return result;
            }
        }

        private static class PreparedStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_NONE;
                return result;
            }
        }

        private static class UploadingStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_PROGRESS;
                if (pptVO.getUploadProgress() != null) {
                    result.uploadProgress = pptVO.getUploadProgress();
                }
                return result;
            }
        }

        private static class UploadFailedStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_TEXT;
                result.hintContent = "上传失败";
                result.hintContentTextColor = Color.parseColor("#F24453");
                result.buttonText = "重试";
                return result;
            }
        }

        private static class UploadSuccessStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_TEXT;
                result.hintAlertIconVisibility = View.GONE;
                result.hintContent = "上传成功，待转码";
                result.buttonVisibility = GONE;
                return result;
            }
        }

        private static class ConvertingStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_TEXT;
                result.hintAlertIconVisibility = View.GONE;
                result.hintContent = "正在转码...";
                result.buttonVisibility = GONE;
                return result;
            }
        }

        private static class ConvertFailStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_TEXT;
                result.hintContent = "无法解码";
                result.hintContentTextColor = Color.parseColor("#F24453");
                result.buttonText = "帮助";
                return result;
            }
        }

        private static class ConvertAnimateLossStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.showType = PLVLSPptUploadProgressView.SHOW_TYPE_TEXT;
                result.hintAlertIconVisibility = GONE;
                result.hintContent = "动效丢失，不影响使用";
                result.buttonText = "知道了";
                return result;
            }
        }

        private static class ConvertSuccessStatus extends Status {
            @Override
            ProgressViewResult parsePptVO(PLVLSPptVO pptVO) {
                ProgressViewResult result = new ProgressViewResult();
                result.progressViewVisibility = GONE;
                return result;
            }
        }

    }

    // </editor-fold>
}
