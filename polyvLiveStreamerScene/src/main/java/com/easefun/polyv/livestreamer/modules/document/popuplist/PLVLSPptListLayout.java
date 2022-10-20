package com.easefun.polyv.livestreamer.modules.document.popuplist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.document.model.PLVSPPTDetail;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.easefun.polyv.livescenes.upload.PLVSDocumentUploadConstant;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.document.popuplist.adapter.PLVLSPptListAdapter;
import com.easefun.polyv.livestreamer.modules.document.popuplist.enums.PLVLSPptViewType;
import com.easefun.polyv.livestreamer.modules.document.popuplist.holder.PLVLSPptListViewHolder;
import com.easefun.polyv.livestreamer.modules.document.popuplist.vo.PLVLSPptVO;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSDocumentDeleteArrow;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * PPT文档和PPT页面列表选择弹层布局
 * 弹层布局请勿直接在布局文件引入，请通过{@link #open(boolean)}方法显示弹层
 *
 * @author suhongtao
 */
public class PLVLSPptListLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVLSPptListLayout.class.getSimpleName();

    /**
     * SharePreference Key
     * 是否曾经显示过PPT页面列表返回按键指示
     * 返回按键指示控件只有在首次进入PPT页面列表时显示一次
     */
    private static final String SP_KEY_HAS_SHOW_INDICATOR = "key_has_show_ppt_page_list_indicator";

    // 子View
    private View rootView;
    private LinearLayout plvlsDocumentTitleLl;
    private ImageView plvlsDocumentListBackIv;
    private TextView plvlsDocumentNameTv;
    private TextView plvlsDocumentPageTv;
    private TextView plvlsDocumentRefreshTv;
    private View plvlsDocumentSeparatorView;
    private RecyclerView plvlsDocumentPptRv;
    private PLVTriangleIndicateTextView plvlsDocumentBackIndicator;

    // 模糊背景
    private PLVBlurView plvlsBlurView;
    // 模糊背景定时更新
    private Disposable updateBlurViewDisposable;

    // 弹层布局
    private PLVMenuDrawer menuDrawer;

    // PPT文档上传 选择转码方式 对话框
    private PLVConfirmDialog pptConvertSelectDialog;

    // PPT文档列表 长按删除按钮
    private PLVLSDocumentDeleteArrow documentDeleteArrow;
    // PPT文档 删除确认弹窗
    private PLVConfirmDialog documentDeleteConfirmDialog;
    // PPT文档 上次上传文件失败 再次上传弹窗
    private PLVConfirmDialog documentUploadAgainConfirmDialog;

    /**
     * MVP - View
     * 请勿改为局部变量，否则会被gc回收，引起无法响应Presenter调用
     */
    private PLVAbsDocumentView mvpView;

    // 列表适配器
    private PLVLSPptListAdapter pptListAdapter;
    // 列表每行显示列表个数
    private static final int PPT_ITEMS_EACH_ROW = 4;
    // 列表显示样式
    @PLVLSPptViewType.Range
    private int showViewType = PLVLSPptViewType.COVER;

    /**
     * ppt文档id对应ppt文件名称
     * Key: autoId
     * Value: ppt文件名
     */
    private SparseArray<String> pptAutoIdMapToFullName = new SparseArray<>();
    // 当前ppt文档id
    private int currentAutoId = 0;
    // 当前ppt页面id
    private int currentPageId = 0;

    //直播恢复初始化tag
    private boolean recoverTag = false;

    // PPT文档详情 视图缓存
    private String lastPptName = null;
    private int lastPptPageCount = 0;
    private List<PLVLSPptVO> lastPptPageVOList = null;

    // PPT文档列表 视图缓存
    private List<PLVLSPptVO> lastPptCoverVOList = null;
    // PPT文档列表 本地上传文档缓存
    private List<PLVLSPptVO> uploadPptCoverVOList = new ArrayList<>();
    // PPT文档列表 转码动效丢失fileId缓存
    private Set<String> pptConvertAnimateLossFileIdSet = new HashSet<>();

    // PPT文档上传监听接口实现
    private OnPLVSDocumentUploadListener documentUploadListener;

    @Nullable
    private PLVUserAbilityManager.OnUserAbilityChangedListener onUserAbilityChangeCallback;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSPptListLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvls_document_ppt_list_layout, this);
        findView();
        initRecyclerView();
        initPptConvertSelectDialog();
        initPptDeleteConfirmDialog();
        initPptUploadAgainConfirmDialog();
        initDocumentUploadListener();
        initOnClickBackListener();
        initOnClickRefreshListener();
        PLVBlurUtils.initBlurView(plvlsBlurView);

        initMvpView();
        initOnUserAbilityChangeListener();
    }

    private void findView() {
        plvlsDocumentTitleLl = (LinearLayout) rootView.findViewById(R.id.plvls_document_title_ll);
        plvlsDocumentListBackIv = (ImageView) rootView.findViewById(R.id.plvls_document_list_back_iv);
        plvlsDocumentNameTv = (TextView) rootView.findViewById(R.id.plvls_document_name_tv);
        plvlsDocumentPageTv = (TextView) rootView.findViewById(R.id.plvls_document_page_tv);
        plvlsDocumentRefreshTv = (TextView) rootView.findViewById(R.id.plvls_document_refresh_tv);
        plvlsDocumentSeparatorView = (View) rootView.findViewById(R.id.plvls_document_separator_view);
        plvlsDocumentPptRv = (RecyclerView) rootView.findViewById(R.id.plvls_document_ppt_rv);
        plvlsDocumentBackIndicator = (PLVTriangleIndicateTextView) rootView.findViewById(R.id.plvls_document_back_indicator);

        plvlsBlurView = (PLVBlurView) rootView.findViewById(R.id.blur_ly);

        documentDeleteArrow = new PLVLSDocumentDeleteArrow();
    }

    /**
     * 初始化列表
     */
    private void initRecyclerView() {
        initRecyclerViewAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), PPT_ITEMS_EACH_ROW);
        plvlsDocumentPptRv.setLayoutManager(gridLayoutManager);
        plvlsDocumentPptRv.setAdapter(pptListAdapter);
    }

    /**
     * 初始化列表适配器
     */
    private void initRecyclerViewAdapter() {
        // 初始化Adapter，首次进入显示PPT文档列表
        pptListAdapter = new PLVLSPptListAdapter(null, PLVLSPptViewType.COVER);
        // 设置列表项点击监听
        pptListAdapter.setOnPptItemClickListener(new PLVLSPptListViewHolder.OnPptItemClickListener() {
            @Override
            public void onClick(int id) {
                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
                    return;
                }
                if (pptListAdapter.getRealViewType() == PLVLSPptViewType.COVER) {
                    // 显示为PPT文档列表
                    if (currentAutoId == id) {
                        // 选择原来选中的文档，切换进入PPT页面列表
                        updatePptPageViewContent();
                    } else {
                        // 选择其它文档，切换显示新的PPT文档，并关闭弹层
                        PLVDocumentPresenter.getInstance().changePpt(id);
                        currentAutoId = id;
                        showViewType = PLVLSPptViewType.PAGE;
                        close();
                    }
                } else if (pptListAdapter.getRealViewType() == PLVLSPptViewType.PAGE) {
                    // 显示为PPT页面列表时，点击列表项，切换PPT页面
                    PLVDocumentPresenter.getInstance().changePptPage(currentAutoId, id);
                    currentPageId = id;
                }
            }
        });
        // 设置列表项长按点击监听
        pptListAdapter.setOnPptItemLongClickListener(new PLVLSPptListViewHolder.OnPptItemLongClickListener() {
            @Override
            public void onLongClick(View view, final int id, final String fileId) {
                if (pptListAdapter.getRealViewType() != PLVLSPptViewType.COVER) {
                    return;
                }
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
                                            if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_DELETE_PPT)) {
                                                return;
                                            }
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
        pptConvertSelectDialog = PLVLSConfirmDialog.Builder.context(getContext())
                .setTitle(getResources().getString(R.string.plvls_document_upload_choose_convert_type))
                .setContent(getResources().getString(R.string.plvls_document_upload_choose_convert_type_hint))
                .build();
    }

    /**
     * 初始化删除PPT前提示弹窗
     */
    private void initPptDeleteConfirmDialog() {
        documentDeleteConfirmDialog = PLVLSConfirmDialog.Builder.context(getContext())
                .setTitleVisibility(View.GONE)
                .setContent("删除后文档将无法恢复")
                .setLeftButtonText("按错了")
                .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        documentDeleteConfirmDialog.hide();
                    }
                })
                .setRightButtonText("确定")
                .build();
    }

    /**
     * 初始化重新上传PPT前提示弹窗
     */
    private void initPptUploadAgainConfirmDialog() {
        documentUploadAgainConfirmDialog = PLVLSConfirmDialog.Builder.context(getContext())
                .setTitleVisibility(View.GONE)
                .build();
    }

    /**
     * 初始化返回按钮点击监听
     */
    private void initOnClickBackListener() {
        plvlsDocumentListBackIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 只有显示PPT页面列表才会显示返回键，点击返回键切换为显示PPT文档列表
                showViewType = PLVLSPptViewType.COVER;
                updatePptCoverViewContent();
                requestUpdateData();
            }
        });
    }

    /**
     * 初始化刷新按钮点击监听
     */
    private void initOnClickRefreshListener() {
        plvlsDocumentRefreshTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_PULL_PPT)) {
                    return;
                }
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
            }
        });
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
            public void onPptPageList(@Nullable PLVSPPTJsModel plvspptJsModel) {

                if(!recoverTag && PLVLiveChannelConfigFiller.generateNewChannelConfig().isLiveStreamingWhenLogin()){
                    //如果是恢复直播，需要更新一下状态
                    if(plvspptJsModel != null && !TextUtils.isEmpty(plvspptJsModel.getFileName())){
                        lastPptName = plvspptJsModel.getFileName();
                        currentAutoId = plvspptJsModel.getAutoId();
                        showViewType = PLVLSPptViewType.PAGE;
                        refreshPptPageStatus(plvspptJsModel);
                        recoverTag = true;
                        return;
                    }
                }
                processPptPageList(plvspptJsModel);
            }

            @Override
            public void onAssistantChangePptPage(int pageId) {
                PLVDocumentPresenter.getInstance().changePptPage(currentAutoId, pageId);
            }

            @Override
            public void onPptPageChange(int autoId, int pageId) {
                if (showViewType == PLVLSPptViewType.COVER) {
                    pptListAdapter.setCurrentSelectedId(autoId);
                }
                if (showViewType == PLVLSPptViewType.PAGE) {
                    pptListAdapter.setCurrentSelectedId(pageId);
                }
                pptListAdapter.notifyDataSetChanged();

                currentAutoId = autoId;
                currentPageId = pageId;
            }

            @Override
            public boolean requestSelectUploadFileConvertType(final Uri fileUri) {
                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_UPLOAD_PPT)) {
                    return true;
                }
                if (fileUri == null) {
                    Log.w(TAG, "file uri is null.");
                    PLVToast.Builder.context(getContext())
                            .setText("无法访问文件所在路径")
                            .build().show();
                    return false;
                }

                String filePath = null;
                PLVUriPathHelper.copyFile(getContext(), fileUri,
                        new File(getContext().getExternalFilesDir(""),
                                PLVUriPathHelper.getRealFileName(getContext(), fileUri)));
                File file = new File(getContext().getExternalFilesDir(""),
                        PLVUriPathHelper.getRealFileName(getContext(), fileUri));
                if (fileUri.toString().startsWith("content")) {
                    filePath = PLVUriPathHelper.getPath(getContext(), fileUri);
                } else if (fileUri.getPath() != null) {
                    filePath = fileUri.getPath().substring(fileUri.getPath().indexOf("/") + 1);
                }
                if (TextUtils.isEmpty(filePath)) {
                    Log.w(TAG, "file path is empty.");
                    PLVToast.Builder.context(getContext())
                            .setText("无法访问文件所在路径")
                            .build().show();
                    return false;
                }

                final File uploadFile = file;

                // 弹窗提示选择转码方式
                pptConvertSelectDialog
                        .setLeftButtonText("快速转码")
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVSDocumentUploadConstant.PPTConvertType.COMMON, documentUploadListener);
                                pptConvertSelectDialog.hide();
                            }
                        })
                        .setRightButtonText("动画转码（较慢）")
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVSDocumentUploadConstant.PPTConvertType.ANIMATE, documentUploadListener);
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
                        .setContent("本地有文档上次上传中断，是否重新上传")
                        .setLeftButtonText("取消")
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().removeUploadCache(cacheVOS);
                                documentUploadAgainConfirmDialog.hide();
                            }
                        })
                        .setRightButtonText("确定")
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_UPLOAD_PPT)) {
                                    // 没有对应权限，取消上传
                                    PLVDocumentPresenter.getInstance().removeUploadCache(cacheVOS);
                                    documentUploadAgainConfirmDialog.hide();
                                    return;
                                }

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
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
                return false;
            }

            @Override
            public void onPptDelete(boolean success, @Nullable PLVSPPTInfo.DataBean.ContentsBean deletedPptBean) {
                if (!success) {
                    return;
                }
                if (deletedPptBean != null) {
                    // 未完成上传转码的PPT文档需要遍历列表删除视图项
                    PLVLSPptVO uploadDeletedPptVO = null;
                    for (PLVLSPptVO pptVO : uploadPptCoverVOList) {
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
            }
        };

        PLVDocumentPresenter.getInstance().registerView(mvpView);
    }

    /**
     * 初始化用户角色能力变化监听
     */
    private void initOnUserAbilityChangeListener() {
        this.onUserAbilityChangeCallback = new PLVUserAbilityManager.OnUserAbilityChangedListener() {
            @Override
            public void onUserAbilitiesChanged(@NonNull List<PLVUserAbility> addedAbilities, @NonNull List<PLVUserAbility> removedAbilities) {
                if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
                    close();
                }
            }
        };

        PLVUserAbilityManager.myAbility().addUserAbilityChangeListener(new WeakReference<>(onUserAbilityChangeCallback));
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
        pptListAdapter.setOnUploadViewButtonClickListener(new PLVLSPptListViewHolder.OnUploadViewButtonClickListener() {
            @Override
            public void onClick(final PLVLSPptVO pptVO) {
                if (showViewType != PLVLSPptViewType.COVER) {
                    return;
                }
                if (pptVO.getUploadStatus() == null) {
                    updatePptCoverViewContent();
                    return;
                }
                if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS) {
                    // 确认动效丢失，移除上传进度缓存
                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                    // 确认动效丢失认为转码成功
                    for (PLVLSPptVO uploadListPptVO : mergePptCoverList()) {
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
                    documentUploadAgainConfirmDialog
                            .setContent("暂不支持加密文档，请确保文档已解密 或 转为PDF文件 重试。如无法解决请联系客服。")
                            .setLeftButtonText("取消")
                            .setLeftBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 点击取消时，清除上传进度缓存，下次不再提示
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    documentUploadAgainConfirmDialog.hide();
                                }
                            })
                            .setRightButtonText("重新上传")
                            .setRightBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    // 重新选择文件上传
                                    if (getContext() instanceof Activity) {
                                        PLVFileChooseUtils.chooseFile((Activity) getContext(), PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT);
                                    }
                                    documentUploadAgainConfirmDialog.hide();
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
                PLVLSPptVO pptVO = new PLVLSPptVO(documentBean.getPreviewImage(), documentBean.getFileName(), documentBean.getFileType(), documentBean.getAutoId());
                pptVO.setFileId(documentBean.getFileId());
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_PREPARED);
                uploadPptCoverVOList.add(pptVO);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onUploadProgress(PLVSPPTInfo.DataBean.ContentsBean documentBean, int progress) {
                Log.i(TAG, "document upload onUploadProgress, progress:" + progress);
                // 上传进度回调 更新视图显示
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOADING);
                pptVO.setUploadProgress(progress);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onUploadSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onUploadSuccess");
                // 上传成功回调 更新视图显示
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onUploadFailed(@Nullable PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onUploadFailed");
                String message = msg;
                if (TextUtils.isEmpty(message)) {
                    message = throwable.getMessage();
                }
                PLVToast.Builder.context(getContext())
                        .setText(errorCode + "-" + message)
                        .build().show();

                // 上传失败回调 更新视图显示
                if (documentBean == null) {
                    return;
                }
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_FAILED);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onConvertSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onConvertSuccess");
                // 转码成功回调 移除上传列表的视图项 向服务器获取新的PPT文档列表 通过回调更新视图列表
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
            }

            @Override
            public void onConvertFailed(PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onConvertFailed");
                // 转码失败回调 更新视图显示
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_FAILED);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onDocumentExist(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentExist");
                // 文件已存在回调 移除上传任务视图列表项
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                // TODO 文件已存在，应进行提醒
            }

            @Override
            public void onDocumentConverting(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentConverting");
                // 转码中回调 更新视图显示
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERTING);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }
        };
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 打开弹层
     */
    public void open(boolean refresh) {
        if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
            return;
        }
        if (refresh) {
            PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
        }
        final int landscapeHeight = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (menuDrawer == null) {
            // 弹层初始化
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_FULLSCREEN);
            menuDrawer.setMenuSize((int) (landscapeHeight * 0.75));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                }
            });
            plvlsDocumentPptRv.post(new Runnable() {
                @Override
                public void run() {
                    menuDrawer.setDragAreaMenuBottom((int) (plvlsDocumentPptRv.getTop() + landscapeHeight * 0.25));
                }
            });
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
        initViewByShowType();
    }

    /**
     * 关闭弹层
     */
    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    /**
     * 返回按键逻辑
     *
     * @return consume
     */
    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    /**
     * 销毁方法
     */
    public void destroy() {
        onUserAbilityChangeCallback = null;
        stopUpdateBlurViewTimer();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI部分">

    /**
     * 初始化显示样式并请求更新视图数据
     */
    private void initViewByShowType() {
        if (currentAutoId == 0) {
            showViewType = PLVLSPptViewType.COVER;
        } else {
            showViewType = PLVLSPptViewType.PAGE;
        }

        if (showViewType == PLVLSPptViewType.COVER) {
            updatePptCoverViewContent();
        } else {
            updatePptPageViewContent();
        }
        requestUpdateData();
        // 首次打开检查上次是否有PPT文档上传中断，文档转码动效丢失情况
        PLVDocumentPresenter.getInstance().checkUploadFileStatus();
    }

    /**
     * 更新PPT文档列表视图数据
     */
    private void updatePptCoverViewContent() {
        List<PLVLSPptVO> mergedCoverList = mergePptCoverList();
        checkAnimateLossStatus(mergedCoverList);
        showViewType = PLVLSPptViewType.COVER;
        plvlsDocumentListBackIv.setVisibility(GONE);
        plvlsDocumentBackIndicator.setVisibility(GONE);
        plvlsDocumentPageTv.setVisibility(GONE);
        plvlsDocumentRefreshTv.setVisibility(VISIBLE);

        String name = String.format("所有文档 共%s个", mergedCoverList.size());
        SpannableString spannableString = new SpannableString(name);
        spannableString.setSpan(new AbsoluteSizeSpan(ConvertUtils.sp2px(12f)),
                name.lastIndexOf("共"), name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        plvlsDocumentNameTv.setText(spannableString);
        pptListAdapter.setCurrentSelectedId(currentAutoId);
        pptListAdapter.updatePptList(mergedCoverList, PLVLSPptViewType.COVER);
        plvlsDocumentPptRv.scrollToPosition(currentAutoId);
    }

    /**
     * 更新PPT页面列表视图数据
     */
    private void updatePptPageViewContent() {
        showViewType = PLVLSPptViewType.PAGE;
        plvlsDocumentListBackIv.setVisibility(VISIBLE);
        plvlsDocumentPageTv.setVisibility(VISIBLE);
        plvlsDocumentRefreshTv.setVisibility(GONE);

        if (lastPptName == null) {
            plvlsDocumentNameTv.setText("");
        } else {
            int suffixDotIndex = lastPptName.lastIndexOf('.');
            if (suffixDotIndex <= 22) {
                // 文档名称显示22个字，超过时截断
                plvlsDocumentNameTv.setText(lastPptName);
            } else {
                String suffix = lastPptName.substring(suffixDotIndex);
                String truncatedPptName = lastPptName.substring(0, 22);
                plvlsDocumentNameTv.setText(truncatedPptName + ".." + suffix);
            }
        }
        plvlsDocumentPageTv.setText("共" + lastPptPageCount + "页");
        pptListAdapter.setCurrentSelectedId(currentPageId);
        pptListAdapter.updatePptList(lastPptPageVOList, PLVLSPptViewType.PAGE);
        plvlsDocumentPptRv.scrollToPosition(currentPageId);

        // 返回指示 只显示一次
        if (!hasShowBackIndicatorBefore()) {
            // 保存状态 已经出现过返回指示 下次不再提示
            setHasShowIndicator();

            plvlsDocumentBackIndicator.setVisibility(VISIBLE);
            // 3秒后自动隐藏
            plvlsDocumentBackIndicator.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (plvlsDocumentBackIndicator != null) {
                        plvlsDocumentBackIndicator.setVisibility(GONE);
                    }
                }
            }, 3000);
        }
    }

    private boolean checkHasDocumentPermissionOrToast(PLVUserAbility documentAbility) {
        if (PLVUserAbilityManager.myAbility().notHasAbility(documentAbility)) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plvls_document_usage_not_permeitted)
                    .show();
            return false;
        }
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 数据部分">

    /**
     * 根据显示样式，向Presenter请求更新列表数据
     */
    private void requestUpdateData() {
        if (showViewType == PLVLSPptViewType.COVER) {
            PLVDocumentPresenter.getInstance().requestGetPptCoverList();
        } else if (showViewType == PLVLSPptViewType.PAGE) {
            PLVDocumentPresenter.getInstance().requestGetPptPageList(currentAutoId);
        }
    }

    /**
     * 处理返回的PPT文档列表数据
     *
     * @param pptInfo
     */
    private void processPptCoverList(PLVSPPTInfo pptInfo) {
        if (pptInfo == null) {
            return;
        }
        List<PLVLSPptVO> pptVOList = new ArrayList<>();
        for (PLVSPPTInfo.DataBean.ContentsBean contentsBean : pptInfo.getData().getContents()) {
            String imageUrl = contentsBean.getPreviewImage();
            String type = contentsBean.getFileType();
            if (type == null) {
                type = "";
            }
            String name = contentsBean.getFileName();

            PLVLSPptVO pptVO = new PLVLSPptVO(imageUrl, name, type, contentsBean.getAutoId());
            pptVO.setFileId(contentsBean.getFileId());
            pptVO.setUploadStatus(mapServerUploadStatus(contentsBean.getStatus()));
            pptVOList.add(pptVO);

            pptAutoIdMapToFullName.put(contentsBean.getAutoId(), name + type);
        }
        lastPptCoverVOList = pptVOList;
        if (showViewType == PLVLSPptViewType.COVER) {
            updatePptCoverViewContent();
        }
    }

    /**
     * 将服务器PPT状态字段转为本地枚举
     *
     * @param beanServerStatus 服务器PPT状态
     * @return 枚举 {@link PLVPptUploadStatus}
     */
    private static Integer mapServerUploadStatus(String beanServerStatus) {
        switch (beanServerStatus) {
            case PLVSDocumentUploadConstant.ConvertStatus.NORMAL:
                return PLVPptUploadStatus.STATUS_CONVERT_SUCCESS;
            case PLVSDocumentUploadConstant.ConvertStatus.WAIT_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOADING;
            case PLVSDocumentUploadConstant.ConvertStatus.FAIL_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOAD_FAILED;
            case PLVSDocumentUploadConstant.ConvertStatus.WAIT_CONVERT:
                return PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS;
            case PLVSDocumentUploadConstant.ConvertStatus.FAIL_CONVERT:
                return PLVPptUploadStatus.STATUS_CONVERT_FAILED;
            default:
                return null;
        }
    }

    /**
     * 处理返回的PPT页面列表数据
     *
     * @param jsModel
     */
    private void processPptPageList(PLVSPPTJsModel jsModel) {
        if (jsModel == null || currentAutoId != jsModel.getAutoId()) {
            return;
        }
        refreshPptPageStatus(jsModel);
    }

    /**
     * 刷新ppt状态
     */
    private void refreshPptPageStatus(PLVSPPTJsModel jsModel){
        List<PLVLSPptVO> pptVOList = new ArrayList<>();
        for (PLVSPPTDetail pptDetail : jsModel.getPPTImages()) {
            String imageUrl = pptDetail.getImageUrl();
            int pptPageId = pptDetail.getPos();
            PLVLSPptVO pptVO = new PLVLSPptVO(imageUrl, pptPageId);
            pptVOList.add(pptVO);
        }
        lastPptPageCount = pptVOList.size();
        lastPptPageVOList = pptVOList;
        lastPptName = pptAutoIdMapToFullName.get(jsModel.getAutoId());
        if (showViewType == PLVLSPptViewType.PAGE) {
            updatePptPageViewContent();
        }
    }

    /**
     * 是否曾经显示过返回指示
     *
     * @return
     */
    private boolean hasShowBackIndicatorBefore() {
        return SPUtils.getInstance().getBoolean(SP_KEY_HAS_SHOW_INDICATOR, false);
    }

    /**
     * 设置已经显示过返回指示
     */
    private void setHasShowIndicator() {
        SPUtils.getInstance().put(SP_KEY_HAS_SHOW_INDICATOR, true);
    }

    /**
     * 根据fileId从上传中的PPT文件视图列表中获取视图VO
     *
     * @param fileId
     * @return
     */
    @Nullable
    private PLVLSPptVO getPptVOFromUploadCache(@NonNull String fileId) {
        if (fileId == null) {
            return null;
        }
        for (PLVLSPptVO pptVO : uploadPptCoverVOList) {
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
    private List<PLVLSPptVO> mergePptCoverList() {
        Set<String> fileIdSet = new HashSet<>();
        List<PLVLSPptVO> resultList = new ArrayList<>();
        for (PLVLSPptVO uploadPptVO : uploadPptCoverVOList) {
            if (fileIdSet.add(uploadPptVO.getFileId().toLowerCase())) {
                resultList.add(uploadPptVO);
            }
        }
        if (lastPptCoverVOList == null) {
            return resultList;
        }
        for (PLVLSPptVO serverPptVO : lastPptCoverVOList) {
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
    private void checkAnimateLossStatus(List<PLVLSPptVO> pptVOList) {
        if (pptVOList == null) {
            return;
        }
        for (PLVLSPptVO pptVO : pptVOList) {
            if (pptConvertAnimateLossFileIdSet.contains(pptVO.getFileId())) {
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 定时更新模糊背景">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        plvlsBlurView.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
            updateBlurViewDisposable = null;
        }
    }
    // </editor-fold>

}
