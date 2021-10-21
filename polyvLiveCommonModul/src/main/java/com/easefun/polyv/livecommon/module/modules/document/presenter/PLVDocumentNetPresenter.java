package com.easefun.polyv.livecommon.module.modules.document.presenter;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.document.contract.IPLVDocumentContract;
import com.easefun.polyv.livecommon.module.modules.document.model.PLVDocumentNetRepo;
import com.easefun.polyv.livecommon.module.modules.document.model.PLVPptUploadLocalRepository;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档管理相关全局单例Presenter
 * 涉及文档列表获取，上传文档，删除文档功能
 *
 * @author suhongtao
 * @see IPLVDocumentContract.Presenter
 */
public class PLVDocumentNetPresenter {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private static PLVDocumentNetPresenter INSTANCE;

    private PLVDocumentNetPresenter() {
        init();
    }

    /**
     * 保持单例仅对{@link PLVDocumentPresenter}可见，外部调用应通过转发
     */
    static PLVDocumentNetPresenter getInstance() {
        if (INSTANCE == null) {
            synchronized (PLVDocumentNetPresenter.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVDocumentNetPresenter();
                }
            }
        }
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVDocumentNetPresenter.class.getSimpleName();

    // 标志位 是否已经经过初始化
    private boolean isInitialized = false;

    /**
     * MVP - View 弱引用
     */
    private final List<WeakReference<IPLVDocumentContract.View>> viewWeakReferenceList = new ArrayList<>();

    /**
     * MVP - Model
     */
    private PLVDocumentNetRepo plvDocumentRepository;
    @Nullable
    private PLVPptUploadLocalRepository plvPptUploadLocalRepository;

    private Observer<PLVStatefulData<PLVSPPTInfo>> pptInfoObserver;
    private Observer<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>> pptOnDeleteObserver;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void init() {
        initRepository();

        observePptInfo();
        observePptOnDeleteResponse();

        isInitialized = true;
    }

    /**
     * 初始化 MVP - Model
     */
    private void initRepository() {
        plvDocumentRepository = PLVDocumentNetRepo.getInstance();

        plvPptUploadLocalRepository = new PLVPptUploadLocalRepository();
    }

    /**
     * 监听Model层所有PPT文档列表更新
     * 向view层回调
     */
    private void observePptInfo() {
        plvDocumentRepository.getPptInfoLiveData().observeForever(pptInfoObserver = new Observer<PLVStatefulData<PLVSPPTInfo>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PLVSPPTInfo> plvspptInfo) {
                if (plvspptInfo == null || !plvspptInfo.isSuccess()) {
                    return;
                }
                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onPptCoverList(plvspptInfo.getData());
                    }
                }
            }
        });
    }

    /**
     * 监听Model层删除PPT回调事件
     */
    private void observePptOnDeleteResponse() {
        plvDocumentRepository.getPptOnDeleteResponseLiveData().observeForever(pptOnDeleteObserver = new Observer<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean> response) {
                if (response == null) {
                    return;
                }

                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onPptDelete(response.isSuccess(), response.getData());
                    }
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Presenter实现">

    public void registerView(IPLVDocumentContract.View view) {
        viewWeakReferenceList.add(new WeakReference<>(view));
    }

    public void requestGetPptCoverList() {
        requestGetPptCoverList(false);
    }

    public void requestGetPptCoverList(boolean forceRefresh) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.requestPptCoverList(forceRefresh);
    }

    public void onSelectUploadFile(Uri fileUri) {
        for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
            IPLVDocumentContract.View view = viewWeakReference.get();
            if (view != null) {
                boolean consume = view.requestSelectUploadFileConvertType(fileUri);
                if (consume) {
                    return;
                }
            }
        }
    }

    public void uploadFile(Context context, File uploadFile, final String convertType, final OnPLVSDocumentUploadListener listener) {
        if (!checkInitialized()) {
            return;
        }

        final PLVPptUploadLocalCacheVO localCacheVO = new PLVPptUploadLocalCacheVO();
        localCacheVO.setStatus(PLVPptUploadStatus.STATUS_UNPREPARED);
        localCacheVO.setFileName(uploadFile.getName());
        localCacheVO.setFilePath(uploadFile.getAbsolutePath());
        localCacheVO.setConvertType(convertType);

        plvDocumentRepository.uploadPptFile(context, uploadFile, convertType, new OnPLVSDocumentUploadListener() {
            @Override
            public void onPrepared(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                if (listener != null) {
                    listener.onPrepared(documentBean);
                }
                // onPrepared 本地保存上传任务进度
                localCacheVO.setFileId(documentBean.getFileId());
                localCacheVO.setStatus(PLVPptUploadStatus.STATUS_PREPARED);
                plvPptUploadLocalRepository.saveCache(localCacheVO);
            }

            @Override
            public void onUploadProgress(PLVSPPTInfo.DataBean.ContentsBean documentBean, int progress) {
                if (listener != null) {
                    listener.onUploadProgress(documentBean, progress);
                }
                // 更新本地上传任务进度
                localCacheVO.setStatus(PLVPptUploadStatus.STATUS_UPLOADING);
                plvPptUploadLocalRepository.saveCache(localCacheVO);
            }

            @Override
            public void onUploadSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                if (listener != null) {
                    listener.onUploadSuccess(documentBean);
                }
                // 更新本地上传任务进度 上传完成 下一步服务器转码
                localCacheVO.setStatus(PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS);
                plvPptUploadLocalRepository.saveCache(localCacheVO);
                // 向服务器拉取新的PPT文档列表数据
                plvDocumentRepository.requestPptCoverList(true);
            }

            @Override
            public void onUploadFailed(@Nullable PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                if (listener != null) {
                    listener.onUploadFailed(documentBean, errorCode, msg, throwable);
                }
                // 更新本地上传任务进度 上传失败
                localCacheVO.setStatus(PLVPptUploadStatus.STATUS_UPLOAD_FAILED);
                plvPptUploadLocalRepository.saveCache(localCacheVO);
            }

            @Override
            public void onConvertSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                if (listener != null) {
                    listener.onConvertSuccess(documentBean);
                }
                if (convertType.equals(documentBean.getConvertType())) {
                    localCacheVO.setStatus(PLVPptUploadStatus.STATUS_CONVERT_SUCCESS);
                } else {
                    // 上传时传入的convertType与服务端返回不一致 动效丢失（普通转码必定不会出现）
                    localCacheVO.setStatus(PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS);
                }
                plvPptUploadLocalRepository.saveCache(localCacheVO);

                // 动效丢失 向View回调
                if (localCacheVO.getStatus() == PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS) {
                    List<PLVPptUploadLocalCacheVO> convertAnimateLossVOList = new ArrayList<>();
                    convertAnimateLossVOList.add(localCacheVO);
                    for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                        IPLVDocumentContract.View view = viewWeakReference.get();
                        if (view != null) {
                            boolean consume = view.notifyFileConvertAnimateLoss(convertAnimateLossVOList);
                            if (consume) {
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onConvertFailed(PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                if (listener != null) {
                    listener.onConvertFailed(documentBean, errorCode, msg, throwable);
                }
                // 更新本地上传任务进度 转码失败
                localCacheVO.setStatus(PLVPptUploadStatus.STATUS_CONVERT_FAILED);
                plvPptUploadLocalRepository.saveCache(localCacheVO);
            }

            @Override
            public void onDocumentExist(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                if (listener != null) {
                    listener.onDocumentExist(documentBean);
                }
                // 文件已存在 移除本地上传任务进度
                plvPptUploadLocalRepository.removeCache(localCacheVO.getFileId());
            }

            @Override
            public void onDocumentConverting(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                if (listener != null) {
                    listener.onDocumentConverting(documentBean);
                }
                // 更新本地上传任务进度 已经在转码中
                localCacheVO.setStatus(PLVPptUploadStatus.STATUS_CONVERTING);
                plvPptUploadLocalRepository.saveCache(localCacheVO);
            }
        });
    }

    public void restartUploadFromCache(Context context, String fileId, OnPLVSDocumentUploadListener listener) {
        if (!checkInitialized()) {
            return;
        }
        // 根据fileId拿到本地上传进度缓存
        PLVPptUploadLocalCacheVO localCacheVO = plvPptUploadLocalRepository.getCache(fileId);
        if (localCacheVO == null) {
            return;
        }
        File file = new File(localCacheVO.getFilePath());
        if (!file.exists()) {
            Log.w(TAG, "file is not exist.");
            return;
        }
        uploadFile(context, file, localCacheVO.getConvertType(), listener);
    }

    public void checkUploadFileStatus() {
        if (!checkInitialized()) {
            return;
        }
        List<PLVPptUploadLocalCacheVO> uploadNotSuccessVOList = new ArrayList<>();
        List<PLVPptUploadLocalCacheVO> convertAnimateLossVOList = new ArrayList<>();
        for (PLVPptUploadLocalCacheVO vo : plvPptUploadLocalRepository.listCache()) {
            if (vo.getStatus() == null) {
                plvPptUploadLocalRepository.removeCache(vo.getFileId());
                continue;
            }
            if (!PLVPptUploadStatus.isStatusUploadSuccess(vo.getStatus())) {
                // 上传失败
                File file = new File(vo.getFilePath());
                if (!file.exists()) {
                    Log.i(TAG, "上次上传失败的文件已经不存在");
                    // 文件不存在时直接清除本地缓存
                    plvPptUploadLocalRepository.removeCache(vo.getFileId());
                } else {
                    // 上传失败，在确认重新上传或取消上传后才清除本地缓存
                    uploadNotSuccessVOList.add(vo);
                    continue;
                }
            }
            if (PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS == vo.getStatus()) {
                // 转码动画丢失，在手动确认后才清除本地缓存
                convertAnimateLossVOList.add(vo);
                continue;
            }

            // 其它状态不需要回调，清除本地缓存
            plvPptUploadLocalRepository.removeCache(vo.getFileId());
        }

        // 向View回调 上传失败
        if (uploadNotSuccessVOList.size() > 0) {
            for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                IPLVDocumentContract.View view = viewWeakReference.get();
                if (view != null) {
                    boolean consume = view.notifyFileUploadNotSuccess(uploadNotSuccessVOList);
                    if (consume) {
                        break;
                    }
                }
            }
        }

        // 向View回调 动效丢失
        if (convertAnimateLossVOList.size() > 0) {
            for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                IPLVDocumentContract.View view = viewWeakReference.get();
                if (view != null) {
                    boolean consume = view.notifyFileConvertAnimateLoss(convertAnimateLossVOList);
                    if (consume) {
                        break;
                    }
                }
            }
        }
    }

    public void removeUploadCache(int autoId) {
        if (!checkInitialized()) {
            return;
        }
        PLVSPPTInfo.DataBean.ContentsBean contentsBean = getPptContentsBeanFromAutoId(autoId);
        if (contentsBean == null) {
            return;
        }
        plvPptUploadLocalRepository.removeCache(contentsBean.getFileId());
    }

    public void removeUploadCache(List<PLVPptUploadLocalCacheVO> localCacheVOS) {
        if (!checkInitialized()) {
            return;
        }
        if (localCacheVOS == null) {
            return;
        }
        for (PLVPptUploadLocalCacheVO localCacheVO : localCacheVOS) {
            plvPptUploadLocalRepository.removeCache(localCacheVO.getFileId());
        }
    }

    public void removeUploadCache(String fileId) {
        if (!checkInitialized()) {
            return;
        }
        plvPptUploadLocalRepository.removeCache(fileId);
    }

    public void deleteDocument(int autoId) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.deleteDocument(autoId);
    }

    public void deleteDocument(String fileId) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.deleteDocument(fileId);
    }

    public void destroy() {
        if (plvDocumentRepository != null) {
            if (pptInfoObserver != null) {
                plvDocumentRepository.getPptInfoLiveData().removeObserver(pptInfoObserver);
            }
            if (pptOnDeleteObserver != null) {
                plvDocumentRepository.getPptOnDeleteResponseLiveData().removeObserver(pptOnDeleteObserver);
            }
            plvDocumentRepository.destroy();
        }
        isInitialized = false;
        viewWeakReferenceList.clear();
        INSTANCE = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    /**
     * 检查是否已经经过初始化
     * 未初始化时记录日志
     *
     * @return isInitialized
     */
    private boolean checkInitialized() {
        if (!isInitialized
                || plvPptUploadLocalRepository == null
                || plvDocumentRepository == null) {
            Log.w(TAG, "Call PLVLSDocumentPresenter.init() first!");
        }
        return isInitialized;
    }

    /**
     * 根据autoId获取PPT文档VO
     * 从本地缓存中获取
     *
     * @param autoId 文档id
     * @return 返回PPT文档VO，当本地缓存没有对应autoId的ppt文档vo时，返回null
     */
    @Nullable
    private PLVSPPTInfo.DataBean.ContentsBean getPptContentsBeanFromAutoId(int autoId) {
        if (!checkInitialized()) {
            return null;
        }
        PLVSPPTInfo pptInfo = plvDocumentRepository.getCachePptCoverList();
        if (pptInfo == null || pptInfo.getData() == null || pptInfo.getData().getContents() == null) {
            Log.w(TAG, "cache ppt cover list is null.");
            return null;
        }
        List<PLVSPPTInfo.DataBean.ContentsBean> contentsBeans = pptInfo.getData().getContents();
        for (PLVSPPTInfo.DataBean.ContentsBean contentsBean : contentsBeans) {
            if (contentsBean.getAutoId() == autoId) {
                return contentsBean;
            }
        }
        return null;
    }

    // </editor-fold>
}
