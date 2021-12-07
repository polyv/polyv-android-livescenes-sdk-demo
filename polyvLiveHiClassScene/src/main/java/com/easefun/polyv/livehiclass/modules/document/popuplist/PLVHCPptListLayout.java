package com.easefun.polyv.livehiclass.modules.document.popuplist;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVOutsideTouchableLayout;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.document.popuplist.adapter.PLVHCPptListAdapter;
import com.easefun.polyv.livehiclass.modules.document.popuplist.holder.PLVHCPptListViewHolder;
import com.easefun.polyv.livehiclass.modules.document.popuplist.vo.PLVHCPptVO;
import com.easefun.polyv.livehiclass.modules.document.popuplist.widget.PLVHCDocumentDeleteArrow;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.plv.livescenes.document.model.PLVPPTInfo;
import com.plv.livescenes.upload.PLVDocumentUploadConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PPT文档列表选择弹层布局
 *
 * @author suhongtao
 */
public class PLVHCPptListLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVHCPptListLayout.class.getSimpleName();

    // 子View
    private View rootView;
    private LinearLayout documentTitleLl;
    private TextView documentNameTv;
    private TextView documentPageTv;
    private RecyclerView documentPptRv;

    // 布局外层容器
    private PLVOutsideTouchableLayout container;

    // PPT文档上传 选择转码方式 对话框
    private PLVConfirmDialog pptConvertSelectDialog;

    // PPT文档列表 长按删除按钮
    private PLVHCDocumentDeleteArrow documentDeleteArrow;
    // PPT文档 删除确认弹窗
    private PLVConfirmDialog documentDeleteConfirmDialog;
    // PPT文档 上次上传文件失败 再次上传弹窗
    private PLVConfirmDialog documentUploadAgainConfirmDialog;
    // PPT文档 转码失败 再次上传弹窗
    private PLVConfirmDialog documentConvertFailConfirmDialog;

    /**
     * MVP - View
     * 请勿改为局部变量，否则会被gc回收，引起无法响应Presenter调用
     */
    private PLVAbsDocumentView mvpView;

    // 列表适配器
    private PLVHCPptListAdapter pptListAdapter;
    // 列表每行显示列表个数
    private static final int PPT_ITEMS_EACH_ROW = 4;

    /**
     * ppt文档id对应ppt文件名称
     * Key: autoId
     * Value: ppt文件名
     */
    private SparseArray<String> pptAutoIdMapToFullName = new SparseArray<>();

    // PPT文档列表 视图缓存
    private List<PLVHCPptVO> lastPptCoverVOList = null;
    // PPT文档列表 本地上传文档缓存
    private List<PLVHCPptVO> uploadPptCoverVOList = new ArrayList<>();
    // PPT文档列表 转码动效丢失fileId缓存
    private Set<String> pptConvertAnimateLossFileIdSet = new HashSet<>();

    // PPT文档上传监听接口实现
    private OnPLVSDocumentUploadListener documentUploadListener;

    private OnViewActionListener onViewActionListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCPptListLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_document_ppt_list_layout, this);
        findView();
        initRecyclerView();
        initPptConvertSelectDialog();
        initPptDeleteConfirmDialog();
        initPptUploadAgainConfirmDialog();
        initPptConvertFailConfirmDialog();
        initDocumentUploadListener();

        initMvpView();
    }

    private void findView() {
        documentTitleLl = (LinearLayout) rootView.findViewById(R.id.plvhc_document_title_ll);
        documentNameTv = (TextView) rootView.findViewById(R.id.plvhc_document_name_tv);
        documentPageTv = (TextView) rootView.findViewById(R.id.plvhc_document_page_tv);
        documentPptRv = (RecyclerView) rootView.findViewById(R.id.plvhc_document_ppt_rv);

        documentDeleteArrow = new PLVHCDocumentDeleteArrow(getContext());
    }

    /**
     * 初始化列表
     */
    private void initRecyclerView() {
        initRecyclerViewAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), PPT_ITEMS_EACH_ROW);
        documentPptRv.setLayoutManager(gridLayoutManager);
        documentPptRv.setAdapter(pptListAdapter);
    }

    /**
     * 初始化列表适配器
     */
    private void initRecyclerViewAdapter() {
        // 初始化Adapter，首次进入显示PPT文档列表
        pptListAdapter = new PLVHCPptListAdapter(null);
        // 设置列表项点击监听
        pptListAdapter.setOnPptItemClickListener(new PLVHCPptListViewHolder.OnPptItemClickListener() {
            @Override
            public void onClick(int id) {
                PLVDocumentPresenter.getInstance().requestOpenPptView(id, pptAutoIdMapToFullName.get(id, ""));
                hide();
            }
        });
        // 设置列表项长按点击监听
        pptListAdapter.setOnPptItemLongClickListener(new PLVHCPptListViewHolder.OnPptItemLongClickListener() {
            @Override
            public void onLongClick(View view, final int id, final String fileId) {
                if (documentDeleteArrow != null) {
                    // 长按显示删除箭头
                    documentDeleteArrow.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 点击删除箭头后 弹窗提示是否删除
                            documentDeleteConfirmDialog
                                    .setRightBtnListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            PLVDocumentPresenter.getInstance().deleteDocument(fileId);
                                            documentDeleteConfirmDialog.hide();
                                        }
                                    })
                                    .show();
                        }
                    });
                    documentDeleteArrow.showAtLocation(view);
                }
            }
        });
        // 设置列表项上传状态层按钮点击回调监听
        initOnUploadLayoutButtonClickListener();
    }

    /**
     * 初始化PPT转码选择视图
     */
    private void initPptConvertSelectDialog() {
        pptConvertSelectDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(getResources().getString(R.string.plvhc_document_upload_choose_convert_type))
                .setContent(getResources().getString(R.string.plvhc_document_upload_choose_convert_type_hint));
    }

    /**
     * 初始化删除PPT前提示弹窗
     */
    private void initPptDeleteConfirmDialog() {
        documentDeleteConfirmDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(R.string.plvhc_document_delete_confirm_title)
                .setContent(R.string.plvhc_document_delete_confirm_content)
                .setLeftButtonText(R.string.plvhc_document_delete_confirm_cancel)
                .setLeftBtnListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        documentDeleteConfirmDialog.hide();
                    }
                })
                .setRightButtonText(R.string.plvhc_document_delete_confirm);
    }

    /**
     * 初始化重新上传PPT前提示弹窗
     */
    private void initPptUploadAgainConfirmDialog() {
        documentUploadAgainConfirmDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(R.string.plvhc_document_upload_retry_title)
                .setContent(R.string.plvhc_document_upload_retry_content);
    }

    /**
     * 初始化无法解码提示弹窗
     */
    private void initPptConvertFailConfirmDialog() {
        documentConvertFailConfirmDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(R.string.plvhc_document_upload_convert_fail_title)
                .setContent(R.string.plvhc_document_upload_convert_fail_content);
    }

    /**
     * 初始化 MVP - View
     */
    private void initMvpView() {
        mvpView = new PLVAbsDocumentView() {
            @Override
            public void onPptCoverList(@Nullable PLVSPPTInfo pptInfo) {
                processPptCoverList(pptInfo);
            }

            @Override
            public boolean requestSelectUploadFileConvertType(final Uri fileUri) {
                if (fileUri == null) {
                    Log.w(TAG, "file uri is null.");
                    return false;
                }

                String filePath = null;
                if (fileUri.toString().startsWith("content")) {
                    filePath = PLVUriPathHelper.getPath(getContext(), fileUri);
                } else if (fileUri.getPath() != null) {
                    filePath = fileUri.getPath().substring(fileUri.getPath().indexOf("/") + 1);
                }
                if (TextUtils.isEmpty(filePath)) {
                    Log.w(TAG, "file path is empty.");
                    return false;
                }

                final File uploadFile = new File(filePath);
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(filePath);
                String fileMimeType = null;
                if (!TextUtils.isEmpty(fileExtension)) {
                    fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                }
                // 不支持的上传格式，弹toast取消上传
                if (TextUtils.isEmpty(fileMimeType)) {
                    PLVHCToast.Builder.context(getContext())
                            .setText(R.string.plvhc_document_upload_not_support_file_type_hint)
                            .build()
                            .show();
                    return false;
                } else if (!PLVFileChooseUtils.isSupportMimeType(fileMimeType)) {
                    PLVHCToast.Builder.context(getContext())
                            .setText(R.string.plvhc_document_upload_not_support_file_type_hint)
                            .build()
                            .show();
                    return false;
                }

                // 弹窗提示选择转码方式
                pptConvertSelectDialog
                        .setLeftButtonText(R.string.plvhc_document_upload_convert_type_quick)
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVDocumentUploadConstant.PPTConvertType.COMMON, documentUploadListener);
                                pptConvertSelectDialog.hide();
                            }
                        })
                        .setRightButtonText(R.string.plvhc_document_upload_convert_type_animate)
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVDocumentUploadConstant.PPTConvertType.ANIMATE, documentUploadListener);
                                pptConvertSelectDialog.hide();
                            }
                        })
                        .show();

                return true;
            }

            @Override
            public boolean notifyFileUploadNotSuccess(@NonNull final List<PLVPptUploadLocalCacheVO> cacheVOS) {
                if (cacheVOS.size() == 0) {
                    return true;
                }
                // 弹窗提示是否重新上传文档
                documentUploadAgainConfirmDialog
                        .setLeftButtonText(R.string.plvhc_document_upload_retry_cancel)
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().removeUploadCache(cacheVOS);
                                documentUploadAgainConfirmDialog.hide();
                            }
                        })
                        .setRightButtonText(R.string.plvhc_document_upload_retry_confirm)
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (PLVPptUploadLocalCacheVO localCacheVO : cacheVOS) {
                                    File file = new File(localCacheVO.getFilePath());
                                    if (!file.exists()) {
                                        continue;
                                    }
                                    PLVDocumentPresenter.getInstance().uploadFile(getContext(), file, localCacheVO.getConvertType(), documentUploadListener);
                                }
                                documentUploadAgainConfirmDialog.hide();
                            }
                        })
                        .show();
                return true;
            }

            @Override
            public boolean notifyFileConvertAnimateLoss(@NonNull List<PLVPptUploadLocalCacheVO> cacheVOS) {
                for (PLVPptUploadLocalCacheVO localCacheVO : cacheVOS) {
                    pptConvertAnimateLossFileIdSet.add(localCacheVO.getFileId());
                }
                updatePptCoverViewContent();
                return false;
            }

            @Override
            public void onPptDelete(boolean success, @Nullable PLVSPPTInfo.DataBean.ContentsBean deletedPptBean) {
                if (!success) {
                    return;
                }
                if (deletedPptBean != null) {
                    // 未完成上传转码的PPT文档需要遍历列表删除视图项
                    PLVHCPptVO uploadDeletedPptVO = null;
                    for (PLVHCPptVO pptVO : uploadPptCoverVOList) {
                        if (pptVO.getFileId() != null && pptVO.getFileId().equalsIgnoreCase(deletedPptBean.getFileId())) {
                            uploadDeletedPptVO = pptVO;
                            break;
                        }
                    }
                    if (uploadDeletedPptVO != null) {
                        uploadPptCoverVOList.remove(uploadDeletedPptVO);
                    }
                }
                // 已经完成上传转码的PPT文档可以通过拉取服务器数据刷新视图
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);

                PLVHCToast.Builder.context(getContext())
                        .setText("已删除课件")
                        .setDrawable(R.drawable.plvhc_document_ppt_deleted_icon)
                        .build().show();
            }
        };

        PLVDocumentPresenter.getInstance().registerView(mvpView);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法 - 文档上传状态相关回调设置">

    /**
     * 设置列表项上传状态层按钮点击回调监听
     * <p>
     * 正在上传状态的PPT文档视图列表项，在PPT预览图上叠加有上传状态指示层
     * 当文档处于上传失败状态时，点击按钮重试上传
     * 当文档处于转码失败状态时，点击按钮提示重新选择文件上传
     * 当文档转码完毕，但属于动效丢失状态时，点击按钮确认动效丢失，认为转码成功
     */
    private void initOnUploadLayoutButtonClickListener() {
        pptListAdapter.setOnUploadViewButtonClickListener(new PLVHCPptListViewHolder.OnUploadViewButtonClickListener() {
            @Override
            public void onClick(final PLVHCPptVO pptVO) {
                if (pptVO.getUploadStatus() == null) {
                    updatePptCoverViewContent();
                    return;
                }
                if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS) {
                    // 确认动效丢失，移除上传进度缓存
                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                    // 确认动效丢失认为转码成功
                    for (PLVHCPptVO uploadListPptVO : mergePptCoverList()) {
                        if (uploadListPptVO.getFileId().equalsIgnoreCase(pptVO.getFileId())) {
                            uploadListPptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_SUCCESS);
                            break;
                        }
                    }
                    pptConvertAnimateLossFileIdSet.remove(pptVO.getFileId());
                    // 更新视图
                    updatePptCoverViewContent();
                } else if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_CONVERT_FAILED) {
                    // 转码失败 重新上传
                    documentConvertFailConfirmDialog
                            .setLeftButtonText(R.string.plvhc_document_upload_convert_fail_cancel)
                            .setLeftBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 点击取消时，清除上传进度缓存，下次不再提示
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    documentConvertFailConfirmDialog.hide();
                                }
                            })
                            .setRightButtonText(R.string.plvhc_document_upload_convert_fail_retry)
                            .setRightBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    // 重新选择文件上传
                                    if (getContext() instanceof Activity) {
                                        PLVFileChooseUtils.chooseFile((Activity) getContext(), PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT);
                                    }
                                    documentConvertFailConfirmDialog.hide();
                                }
                            })
                            .show();
                } else if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_UPLOAD_FAILED) {
                    // 上传失败 原文件重新上传
                    uploadPptCoverVOList.remove(pptVO);
                    PLVDocumentPresenter.getInstance().restartUploadFromCache(getContext(), pptVO.getFileId(), documentUploadListener);
                } else {
                    updatePptCoverViewContent();
                }
            }
        });
    }

    /**
     * 初始化上传文档监听回调
     * <p>
     * 关于文档上传状态含义，可查看{@link PLVPptUploadStatus}
     */
    private void initDocumentUploadListener() {
        documentUploadListener = new OnPLVSDocumentUploadListener() {
            @Override
            public void onPrepared(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onPrepared");
                // onPrepared 添加上传中的PPT文档视图项
                PLVHCPptVO pptVO = new PLVHCPptVO(documentBean.getPreviewImage(), documentBean.getFileName(), documentBean.getFileType(), documentBean.getAutoId());
                pptVO.setFileId(documentBean.getFileId());
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_PREPARED);
                uploadPptCoverVOList.add(pptVO);
                updatePptCoverViewContent();
            }

            @Override
            public void onUploadProgress(PLVSPPTInfo.DataBean.ContentsBean documentBean, int progress) {
                Log.i(TAG, "document upload onUploadProgress, progress:" + progress);
                // 上传进度回调 更新视图显示
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOADING);
                pptVO.setUploadProgress(progress);
                updatePptCoverViewContent();
            }

            @Override
            public void onUploadSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onUploadSuccess");
                // 上传成功回调 更新视图显示
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS);
                updatePptCoverViewContent();
            }

            @Override
            public void onUploadFailed(@Nullable PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onUploadFailed");
                // 上传失败回调 更新视图显示
                if (documentBean == null) {
                    return;
                }
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_FAILED);
                updatePptCoverViewContent();
            }

            @Override
            public void onConvertSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onConvertSuccess");
                // 转码成功回调 移除上传列表的视图项 向服务器获取新的PPT文档列表 通过回调更新视图列表
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
            }

            @Override
            public void onConvertFailed(PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onConvertFailed");
                // 转码失败回调 更新视图显示
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_FAILED);
                updatePptCoverViewContent();
            }

            @Override
            public void onDocumentExist(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentExist");
                // 文件已存在回调 移除上传任务视图列表项
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                PLVHCToast.Builder.context(getContext())
                        .setText("文件已存在")
                        .build().show();
            }

            @Override
            public void onDocumentConverting(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentConverting");
                // 转码中回调 更新视图显示
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERTING);
                updatePptCoverViewContent();
            }
        };
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    public void show(int viewWidth, int viewHeight, int[] viewLocation) {
        if (container == null) {
            container = ((Activity) getContext()).findViewById(R.id.plvhc_live_room_popup_container);
            container.addOnDismissListener(new PLVOutsideTouchableLayout.OnOutsideDismissListener(this) {
                @Override
                public void onDismiss() {
                    hide();
                }
            });
        }

        final int screenWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        final int screenHeight = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());

        int height = screenHeight - viewLocation[1] - ConvertUtils.dp2px(16);
        int width = (int) (screenWidth * (656F / 812F));

        FrameLayout.LayoutParams lp = new LayoutParams(width, height);
        lp.rightMargin = ConvertUtils.dp2px(66);
        lp.bottomMargin = ConvertUtils.dp2px(8);
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        setLayoutParams(lp);

        container.removeAllViews();
        container.addView(this);

        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(true);
        }

        initViewByShowType();
    }

    public void hide() {
        if (container != null) {
            container.removeAllViews();
        }
        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(false);
        }
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    /**
     * 销毁方法
     */
    public void destroy() {
        PLVDocumentPresenter.getInstance().destroy();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI部分">

    /**
     * 初始化显示样式并请求更新视图数据
     */
    private void initViewByShowType() {
        updatePptCoverViewContent();
        requestUpdateData();
        // 首次打开检查上次是否有PPT文档上传中断，文档转码动效丢失情况
        PLVDocumentPresenter.getInstance().checkUploadFileStatus();
    }

    /**
     * 更新PPT文档列表视图数据
     */
    private void updatePptCoverViewContent() {
        List<PLVHCPptVO> mergedCoverList = mergePptCoverList();
        checkAnimateLossStatus(mergedCoverList);
        documentNameTv.setText("所有文档");
        documentPageTv.setText("共" + mergedCoverList.size() + "个");
        pptListAdapter.updatePptList(mergedCoverList);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 数据部分">

    /**
     * 根据显示样式，向Presenter请求更新列表数据
     */
    private void requestUpdateData() {
        //分组后各个房间的文档公用一个，需要强制刷新
        PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
    }

    /**
     * 处理返回的PPT文档列表数据
     *
     * @param pptInfo
     */
    private void processPptCoverList(PLVPPTInfo pptInfo) {
        if (pptInfo == null) {
            return;
        }
        List<PLVHCPptVO> pptVOList = new ArrayList<>();
        for (PLVPPTInfo.DataBean.ContentsBean contentsBean : pptInfo.getData().getContents()) {
            String imageUrl = contentsBean.getPreviewImage();
            String type = contentsBean.getFileType();
            if (type == null) {
                type = "";
            }
            String name = contentsBean.getFileName();

            PLVHCPptVO pptVO = new PLVHCPptVO(imageUrl, name, type, contentsBean.getAutoId());
            pptVO.setFileId(contentsBean.getFileId());
            pptVO.setUploadStatus(mapServerUploadStatus(contentsBean.getStatus()));
            pptVOList.add(pptVO);

            pptAutoIdMapToFullName.put(contentsBean.getAutoId(), name + type);
        }
        lastPptCoverVOList = pptVOList;
        updatePptCoverViewContent();
    }

    /**
     * 将服务器PPT状态字段转为本地枚举
     *
     * @param beanServerStatus 服务器PPT状态
     * @return 枚举 {@link PLVPptUploadStatus}
     */
    private static Integer mapServerUploadStatus(String beanServerStatus) {
        switch (beanServerStatus) {
            case PLVDocumentUploadConstant.ConvertStatus.NORMAL:
                return PLVPptUploadStatus.STATUS_CONVERT_SUCCESS;
            case PLVDocumentUploadConstant.ConvertStatus.WAIT_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOADING;
            case PLVDocumentUploadConstant.ConvertStatus.FAIL_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOAD_FAILED;
            case PLVDocumentUploadConstant.ConvertStatus.WAIT_CONVERT:
                return PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS;
            case PLVDocumentUploadConstant.ConvertStatus.FAIL_CONVERT:
                return PLVPptUploadStatus.STATUS_CONVERT_FAILED;
            default:
                return null;
        }
    }

    /**
     * 根据fileId从上传中的PPT文件视图列表中获取视图VO
     *
     * @param fileId
     * @return
     */
    @Nullable
    private PLVHCPptVO getPptVOFromUploadCache(@NonNull String fileId) {
        if (fileId == null) {
            return null;
        }
        for (PLVHCPptVO pptVO : uploadPptCoverVOList) {
            if (fileId.equalsIgnoreCase(pptVO.getFileId())) {
                return pptVO;
            }
        }
        return null;
    }

    /**
     * 合并2个PPT文件列表：上传中的PPT文件 和 服务端返回的PPT文件
     *
     * @return
     */
    private List<PLVHCPptVO> mergePptCoverList() {
        Set<String> fileIdSet = new HashSet<>();
        List<PLVHCPptVO> resultList = new ArrayList<>();
        for (PLVHCPptVO uploadPptVO : uploadPptCoverVOList) {
            if (fileIdSet.add(uploadPptVO.getFileId().toLowerCase())) {
                resultList.add(uploadPptVO);
            }
        }
        if (lastPptCoverVOList == null) {
            return resultList;
        }
        for (PLVHCPptVO serverPptVO : lastPptCoverVOList) {
            if (fileIdSet.add(serverPptVO.getFileId().toLowerCase())) {
                resultList.add(serverPptVO);
            }
        }
        return resultList;
    }

    /**
     * 检查动效丢失状态，更新视图数据
     *
     * @param pptVOList
     */
    private void checkAnimateLossStatus(List<PLVHCPptVO> pptVOList) {
        if (pptVOList == null) {
            return;
        }
        for (PLVHCPptVO pptVO : pptVOList) {
            if (pptConvertAnimateLossFileIdSet.contains(pptVO.getFileId())) {
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">

    public interface OnViewActionListener {

        void onVisibilityChanged(boolean isVisible);

    }

    // </editor-fold>

}
