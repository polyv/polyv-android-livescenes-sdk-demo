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
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.upload.IPLVSDocumentUploadManager;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.easefun.polyv.livescenes.upload.manager.PLVSDocumentUploadManagerFactory;
import com.plv.foundationsdk.net.PLVrResponseCallback;
import com.plv.foundationsdk.utils.PLVReflectionUtil;
import com.plv.livescenes.document.model.PLVPPTInfo;

import java.io.File;

import okhttp3.ResponseBody;

/**
 * 开播文档模块 Model层实现
 * 远端仓库，涉及文档列表获取，上传文档，删除文档功能
 *
 * @author suhongtao
 */
public class PLVDocumentNetRepo {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private static PLVDocumentNetRepo INSTANCE = null;

    private PLVDocumentNetRepo() {
        init();
    }

    public static PLVDocumentNetRepo getInstance() {
        if (INSTANCE == null) {
            synchronized (PLVDocumentNetRepo.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVDocumentNetRepo();
                }
            }
        }
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVDocumentNetRepo.class.getSimpleName();

    // PPT文档上传管理器
    @Nullable
    private IPLVSDocumentUploadManager documentUploadManager;

    // 数据 - PPT文档列表变更
    protected final MutableLiveData<PLVStatefulData<PLVSPPTInfo>> plvsPptInfoLiveData = new MutableLiveData<>();
    // 事件 - PPT文档删除响应回调
    protected final MutableLiveData<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>> pptOnDeleteResponseLiveData = new MutableLiveData<>();

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

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    /**
     * 初始化方法
     */
    private void init() {
        initDocumentUploadManager();
    }

    /**
     * 初始化PPT文档上传管理器
     */
    private void initDocumentUploadManager() {
        documentUploadManager = PLVSDocumentUploadManagerFactory.createDocumentUploadManager();
        documentUploadManager.init();
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

        PLVDocumentDataManager.getDocumentList(new PLVrResponseCallback<PLVPPTInfo>() {
            @Override
            public void onSuccess(PLVPPTInfo plvspptInfo) {
                cachePptInfo = PLVReflectionUtil.copyField(plvspptInfo, new PLVSPPTInfo());
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
     * 上传PPT文档
     *
     * @param context  context
     * @param file     需要上传的文件
     * @param type     转码类型 {@link com.easefun.polyv.livescenes.upload.PLVSDocumentUploadConstant.PPTConvertType}
     * @param listener 上传监听回调
     */
    public void uploadPptFile(final Context context, final File file, final String type, final OnPLVSDocumentUploadListener listener) {
        if (documentUploadManager == null) {
            initDocumentUploadManager();
            if (documentUploadManager == null) {
                return;
            }
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
        if (cachePptJsModel != null) {
            cachePptJsModel.clear();
            cachePptJsModel = null;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="LiveData回调获取">

    public LiveData<PLVStatefulData<PLVSPPTInfo>> getPptInfoLiveData() {
        return plvsPptInfoLiveData;
    }

    public LiveData<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>> getPptOnDeleteResponseLiveData() {
        return pptOnDeleteResponseLiveData;
    }

    // </editor-fold>
}
