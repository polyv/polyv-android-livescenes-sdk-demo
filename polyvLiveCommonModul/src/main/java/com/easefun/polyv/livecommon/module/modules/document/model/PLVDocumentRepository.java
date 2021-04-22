package com.easefun.polyv.livecommon.module.modules.document.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livescenes.document.PLVDocumentDataManager;
import com.easefun.polyv.livescenes.document.PLVSDocumentWebProcessor;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.document.model.PLVSPPTPaintStatus;
import com.easefun.polyv.livescenes.document.model.PLVSPPTStatus;
import com.easefun.polyv.livescenes.upload.IPLVSDocumentUploadManager;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadSDKInitErrorListener;
import com.easefun.polyv.livescenes.upload.manager.PLVSDocumentUploadManagerFactory;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.net.PLVrResponseCallback;

import java.io.File;
import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;

/**
 * 开播文档模块 Model层实现
 *
 * @author suhongtao
 */
public class PLVDocumentRepository {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVDocumentRepository.class.getSimpleName();

    // WebProcessor 弱引用
    private WeakReference<PLVSDocumentWebProcessor> documentWebProcessorWeakReference;

    // PPT文档上传管理器
    @Nullable
    private IPLVSDocumentUploadManager documentUploadManager;

    // 数据 - PPT文档列表变更
    private final MutableLiveData<PLVStatefulData<PLVSPPTInfo>> plvsPptInfoLiveData = new MutableLiveData<>();
    // 数据 - PPT页面列表变更
    private final MutableLiveData<PLVStatefulData<PLVSPPTJsModel>> plvsPptJsModelLiveData = new MutableLiveData<>();
    // 事件 - PPT内容变更
    private final MutableLiveData<String> refreshPptMessageLiveData = new MutableLiveData<>();
    // 事件 - PPT页面状态变更
    private final MutableLiveData<PLVSPPTStatus> plvsPptStatusLiveData = new MutableLiveData<>();
    // 事件 - PPT文本标注内容变更
    private final MutableLiveData<PLVSPPTPaintStatus> plvsPptPaintStatusLiveData = new MutableLiveData<>();
    // 事件 - PPT文档删除响应回调
    private final MutableLiveData<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>> pptOnDeleteResponseLiveData = new MutableLiveData<>();

    /**
     * 缓存 PPT文档列表
     */
    private PLVSPPTInfo cachePptInfo = null;

    /**
     * 缓存 PPT页面列表
     * Key: autoId(即pptid)
     * Value: ppt页面列表
     */
    private SparseArray<PLVSPPTJsModel> cachePptJsModel = new SparseArray<>();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVDocumentRepository(PLVSDocumentWebProcessor documentWebProcessor) {
        documentWebProcessorWeakReference = new WeakReference<>(documentWebProcessor);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    /**
     * 初始化方法
     */
    public void init() {
        initWebProcessor();
        initDocumentUploadManager();
    }

    /**
     * 初始化注册Webview回调监听
     */
    private void initWebProcessor() {
        PLVSDocumentWebProcessor webProcessor = documentWebProcessorWeakReference.get();
        if (webProcessor == null) {
            return;
        }

        webProcessor.getWebview().registerProcessor(webProcessor);
        webProcessor.registerJSHandler(new PLVSDocumentWebProcessor.CloudClassJSCallback() {
            @Override
            public void refreshPPT(String message) {
                super.refreshPPT(message);
                refreshPptMessageLiveData.postValue(message);
            }

            @Override
            public void getUser(CallBackFunction function) {
                super.getUser(function);
            }

            @Override
            public void getPPTImagesList(PLVSPPTJsModel jsModel) {
                super.getPPTImagesList(jsModel);
                cachePptJsModel.put(jsModel.getAutoId(), jsModel);
                plvsPptJsModelLiveData.postValue(PLVStatefulData.success(jsModel));
            }

            @Override
            public void getPPTChangeStatus(PLVSPPTStatus plvspptStatus) {
                super.getPPTChangeStatus(plvspptStatus);
                plvsPptStatusLiveData.postValue(plvspptStatus);
            }

            @Override
            public void getEditContent(PLVSPPTPaintStatus content) {
                super.getEditContent(content);
                plvsPptPaintStatusLiveData.postValue(content);
            }
        });
    }

    /**
     * 初始化PPT文档上传管理器
     */
    private void initDocumentUploadManager() {
        documentUploadManager = PLVSDocumentUploadManagerFactory.createDocumentUploadManager();
        documentUploadManager.init(new OnPLVSDocumentUploadSDKInitErrorListener() {
            @Override
            public void onInitError(int errCode, String msg, Throwable throwable) {
                Log.e(TAG, "documentUploadManager init error. [code = " + errCode + ", msg = " + msg + "]", throwable);
                documentUploadManager = null;
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - Webview">

    /**
     * 向Webview发送信息
     *
     * @param event
     * @param message
     */
    public void sendWebMessage(String event, String message) {
        PLVSDocumentWebProcessor webProcessor = documentWebProcessorWeakReference.get();
        if (webProcessor == null) {
            return;
        }
        webProcessor.getWebview().callMessage(event, message.replace("\n", ""));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - PPT相关">

    /**
     * 请求更新PPT文档列表数据
     * 回调 {@link #getPptInfoLiveData()}
     */
    public void requestPptCoverList() {
        requestPptCoverList(false);
    }

    /**
     * 请求更新PPT文档列表数据
     * 回调 {@link #getPptInfoLiveData()}
     *
     * @param forceRefresh 是否强制从服务器获取数据以刷新列表
     */
    public void requestPptCoverList(boolean forceRefresh) {
        if (!forceRefresh) {
            if (cachePptInfo != null) {
                plvsPptInfoLiveData.postValue(PLVStatefulData.success(cachePptInfo));
                return;
            }
        }

        PLVDocumentDataManager.getDocumentList(new PLVrResponseCallback<PLVSPPTInfo>() {
            @Override
            public void onSuccess(PLVSPPTInfo plvspptInfo) {
                cachePptInfo = plvspptInfo;
                plvsPptInfoLiveData.postValue(PLVStatefulData.success(cachePptInfo));
            }

            @Override
            public void onFinish() {

            }
        });
    }

    /**
     * 获取缓存的PPT文档列表数据
     *
     * @return 本地缓存的ppt文档列表数据
     */
    @Nullable
    public PLVSPPTInfo getCachePptCoverList() {
        return cachePptInfo;
    }

    /**
     * 请求更新PPT页面列表数据
     * 回调 {@link #getPptJsModelLiveData()}
     *
     * @param autoId PPTID
     */
    public void requestGetCachedPptPageList(int autoId) {
        PLVSPPTJsModel jsModel = cachePptJsModel.get(autoId);
        if (jsModel != null) {
            plvsPptJsModelLiveData.postValue(PLVStatefulData.success(jsModel));
        } else {
            plvsPptJsModelLiveData.postValue(PLVStatefulData.<PLVSPPTJsModel>error("没有缓存对应的文档列表，请先打开该PPT文档"));
        }
    }

    /**
     * 上传PPT文档
     *
     * @param context context
     * @param file 需要上传的文件
     * @param type 转码类型 {@link com.easefun.polyv.livescenes.upload.PLVSDocumentUploadConstant.PPTConvertType}
     * @param listener 上传监听回调
     */
    public void uploadPptFile(final Context context, final File file, final String type, final OnPLVSDocumentUploadListener listener) {
        if (documentUploadManager == null) {
            return;
        }
        documentUploadManager.startPollingConvertStatus();
        documentUploadManager.addUploadTask(context, type, file, listener);
    }

    /**
     * 删除PPT文档
     *
     * @param autoId 需要删除的文档ID
     */
    public void deleteDocument(final int autoId) {
        if (cachePptInfo == null || cachePptInfo.getData() == null || cachePptInfo.getData().getContents() == null) {
            Log.w(TAG, "delete document failed. ppt list is null.");
            return;
        }

        PLVSPPTInfo.DataBean.ContentsBean deleteBean = null;
        // 根据autoId遍历获取需要删除的文档vo
        for (PLVSPPTInfo.DataBean.ContentsBean bean : cachePptInfo.getData().getContents()) {
            if (bean.getAutoId() == autoId) {
                deleteBean = bean;
                break;
            }
        }

        if (deleteBean == null) {
            Log.w(TAG, "delete document failed. ppt bean is null.");
            return;
        }

        final PLVSPPTInfo.DataBean.ContentsBean finalDeleteBean = deleteBean;
        PLVDocumentDataManager.delDocument(deleteBean, new PLVrResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                pptOnDeleteResponseLiveData.postValue(PLVStatefulData.success(finalDeleteBean));
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onError(Throwable e) {
                pptOnDeleteResponseLiveData.postValue(PLVStatefulData.<PLVSPPTInfo.DataBean.ContentsBean>error(e.getMessage(), e));
            }
        });
    }

    /**
     * 删除PPT文档
     *
     * @param fileId 需要删除的ppt文件ID
     */
    public void deleteDocument(final String fileId) {
        if (cachePptInfo == null || cachePptInfo.getData() == null || cachePptInfo.getData().getContents() == null) {
            Log.w(TAG, "delete document failed. ppt list is null.");
            return;
        }
        if (TextUtils.isEmpty(fileId)) {
            Log.w(TAG, "delete document failed. fileId is empty.");
            return;
        }

        PLVSPPTInfo.DataBean.ContentsBean deleteBean = null;
        // 根据fileId遍历获取需要删除的文档vo
        for (PLVSPPTInfo.DataBean.ContentsBean bean : cachePptInfo.getData().getContents()) {
            if (fileId.equalsIgnoreCase(bean.getFileId())) {
                deleteBean = bean;
                break;
            }
        }

        if (deleteBean == null) {
            Log.w(TAG, "delete document failed. ppt bean is null.");
            return;
        }

        final PLVSPPTInfo.DataBean.ContentsBean finalDeleteBean = deleteBean;
        PLVDocumentDataManager.delDocument(deleteBean, new PLVrResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                pptOnDeleteResponseLiveData.postValue(PLVStatefulData.success(finalDeleteBean));
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onError(Throwable e) {
                pptOnDeleteResponseLiveData.postValue(PLVStatefulData.<PLVSPPTInfo.DataBean.ContentsBean>error(e.getMessage(), e));
            }
        });
    }

    /**
     * 销毁方法
     */
    public void destroy() {
        if (documentUploadManager != null) {
            documentUploadManager.destroy();
            documentUploadManager = null;
        }
        cachePptInfo = null;
        cachePptJsModel.clear();
        cachePptJsModel = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="LiveData回调获取">

    public LiveData<String> getRefreshPptMessageLiveData() {
        return refreshPptMessageLiveData;
    }

    public LiveData<PLVStatefulData<PLVSPPTInfo>> getPptInfoLiveData() {
        return plvsPptInfoLiveData;
    }

    public LiveData<PLVStatefulData<PLVSPPTJsModel>> getPptJsModelLiveData() {
        return plvsPptJsModelLiveData;
    }

    public LiveData<PLVSPPTStatus> getPptStatusLiveData() {
        return plvsPptStatusLiveData;
    }

    public LiveData<PLVSPPTPaintStatus> getPptPaintStatusLiveData() {
        return plvsPptPaintStatusLiveData;
    }

    public LiveData<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>> getPptOnDeleteResponseLiveData() {
        return pptOnDeleteResponseLiveData;
    }

    // </editor-fold>
}
