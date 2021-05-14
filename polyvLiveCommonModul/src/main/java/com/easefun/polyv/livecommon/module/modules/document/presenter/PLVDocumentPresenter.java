package com.easefun.polyv.livecommon.module.modules.document.presenter;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.document.contract.IPLVDocumentContract;
import com.easefun.polyv.livecommon.module.modules.document.model.PLVDocumentRepository;
import com.easefun.polyv.livecommon.module.modules.document.model.PLVPptUploadLocalRepository;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMarkToolType;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livescenes.document.PLVSDocumentWebProcessor;
import com.easefun.polyv.livescenes.document.model.PLVSAssistantInfo;
import com.easefun.polyv.livescenes.document.model.PLVSChangePPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSEditTextInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.document.model.PLVSPPTPaintStatus;
import com.easefun.polyv.livescenes.document.model.PLVSPPTStatus;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVMessageBaseEvent;
import com.plv.socket.event.ppt.PLVOnSliceStartEvent;
import com.plv.socket.eventbus.ppt.PLVOnSliceStartEventBus;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVSocketUserBean;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author suhongtao
 */
public class PLVDocumentPresenter implements IPLVDocumentContract.Presenter {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private static PLVDocumentPresenter INSTANCE;

    private PLVDocumentPresenter() {
    }

    public static IPLVDocumentContract.Presenter getInstance() {
        if (INSTANCE == null) {
            synchronized (PLVDocumentPresenter.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVDocumentPresenter();
                }
            }
        }
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVDocumentPresenter.class.getSimpleName();

    // 标志位 是否已经经过初始化
    private boolean isInitialized = false;

    /**
     * MVP - View 弱引用
     */
    private final List<WeakReference<IPLVDocumentContract.View>> viewWeakReferenceList = new ArrayList<>();

    /**
     * rx disposables
     */
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * MVP - Model
     */
    @Nullable
    private PLVDocumentRepository plvDocumentRepository;
    @Nullable
    private PLVPptUploadLocalRepository plvPptUploadLocalRepository;

    /**
     * 标志位 是否正在推流
     * 非推流状态不上传画笔数据
     */
    private boolean isStreamStarted = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    @Override
    public void init(LifecycleOwner lifecycleOwner,
                     IPLVLiveRoomDataManager liveRoomDataManager,
                     PLVSDocumentWebProcessor documentWebProcessor) {
        initRepository(liveRoomDataManager, documentWebProcessor);
        initSocketListener();

        observeRefreshPptMessage(lifecycleOwner);
        observePptInfo(lifecycleOwner);
        observePptJsModel(lifecycleOwner);
        observePptStatus(lifecycleOwner);
        observePptPaintStatus(lifecycleOwner);
        observePptOnDeleteResponse(lifecycleOwner);

        observeOnSliceStartEvent();

        isInitialized = true;
    }

    /**
     * 初始化 MVP - Model
     * 初始化了WebView
     *
     * @param liveRoomDataManager
     * @param documentWebProcessor
     */
    private void initRepository(IPLVLiveRoomDataManager liveRoomDataManager, PLVSDocumentWebProcessor documentWebProcessor) {
        plvDocumentRepository = new PLVDocumentRepository(documentWebProcessor);
        plvDocumentRepository.init();

        PLVSocketUserBean userBean = new PLVSocketUserBean();
        userBean.setUserId(liveRoomDataManager.getConfig().getUser().getViewerId());
        userBean.setNick(liveRoomDataManager.getConfig().getUser().getViewerName());
        userBean.setPic(liveRoomDataManager.getConfig().getUser().getViewerAvatar());

        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.SETUSER, PLVGsonUtil.toJson(userBean));
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.AUTHORIZATION_PPT_PAINT, "{\"userType\":\"speaker\"}");
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.CHANGEPPT, "{\"autoId\":0,\"isCamClosed\":0}");
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.SETPAINTSTATUS, "{\"status\":\"open\"}");

        plvPptUploadLocalRepository = new PLVPptUploadLocalRepository();
    }

    /**
     * 初始化Socket监听
     * 监听助教切换PPT页面socket事件
     */
    private void initSocketListener() {
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (!PLVEventConstant.Ppt.ON_ASSISTANT_CONTROL.equals(listenEvent)) {
                    return;
                }
                PLVSAssistantInfo assistantInfo = PLVGsonUtil.fromJson(PLVSAssistantInfo.class, message);
                if (assistantInfo == null) {
                    return;
                }
                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onAssistantChangePptPage(assistantInfo.getData().getPageId());
                    }
                }
            }
        }, PLVEventConstant.Ppt.ON_ASSISTANT_CONTROL);
    }

    /**
     * 监听Model层PPT内容变更
     * 当PPT内容变更时，通过socket向服务端传递
     *
     * @param lifecycleOwner
     */
    private void observeRefreshPptMessage(LifecycleOwner lifecycleOwner) {
        plvDocumentRepository.getRefreshPptMessageLiveData().observe(lifecycleOwner, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String message) {
                PolyvSocketWrapper.getInstance().emit(PLVMessageBaseEvent.LISTEN_EVENT, message);
            }
        });
    }

    /**
     * 监听Model层所有PPT文档列表更新
     * 向view层回调
     *
     * @param lifecycleOwner
     */
    private void observePptInfo(LifecycleOwner lifecycleOwner) {
        plvDocumentRepository.getPptInfoLiveData().observe(lifecycleOwner, new Observer<PLVStatefulData<PLVSPPTInfo>>() {
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
     * 监听Model层单个PPT文档页面列表更新
     * 向view层回调
     *
     * @param lifecycleOwner
     */
    private void observePptJsModel(LifecycleOwner lifecycleOwner) {
        plvDocumentRepository.getPptJsModelLiveData().observe(lifecycleOwner, new Observer<PLVStatefulData<PLVSPPTJsModel>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PLVSPPTJsModel> plvsPptJsModel) {
                if (plvsPptJsModel == null || !plvsPptJsModel.isSuccess()) {
                    return;
                }
                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onPptPageList(plvsPptJsModel.getData());
                    }
                }
            }
        });
    }

    /**
     * 监听Model层Webview PPT状态变化
     * 向view层回调 PPTID变更 页面变更
     *
     * @param lifecycleOwner
     */
    private void observePptStatus(LifecycleOwner lifecycleOwner) {
        plvDocumentRepository.getPptStatusLiveData().observe(lifecycleOwner, new Observer<PLVSPPTStatus>() {
            @Override
            public void onChanged(@Nullable PLVSPPTStatus plvspptStatus) {
                if (plvspptStatus == null) {
                    return;
                }
                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onPptPageChange(plvspptStatus.getAutoId(), plvspptStatus.getPageId());
                    }
                }
            }
        });
    }

    /**
     * 监听Model层Webview 文本内容变化
     * 向view层回调
     *
     * @param lifecycleOwner
     */
    private void observePptPaintStatus(LifecycleOwner lifecycleOwner) {
        plvDocumentRepository.getPptPaintStatusLiveData().observe(lifecycleOwner, new Observer<PLVSPPTPaintStatus>() {
            @Override
            public void onChanged(@Nullable PLVSPPTPaintStatus plvspptPaintStatus) {
                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onPptPaintStatus(plvspptPaintStatus);
                    }
                }
            }
        });
    }

    /**
     * 监听Model层删除PPT回调事件
     *
     * @param lifecycleOwner
     */
    private void observePptOnDeleteResponse(LifecycleOwner lifecycleOwner) {
        plvDocumentRepository.getPptOnDeleteResponseLiveData().observe(lifecycleOwner, new Observer<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>>() {
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

    /**
     * 监听sliceStart事件，开播时触发，发送到webview
     * 会清空屏幕上的画笔数据
     */
    private void observeOnSliceStartEvent() {
        Disposable disposable = PLVOnSliceStartEventBus.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVOnSliceStartEvent>() {
                    @Override
                    public void accept(PLVOnSliceStartEvent plvOnSliceStartEvent) {
                        if (plvDocumentRepository != null) {
                            plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.ONSLICESTART, PLVGsonUtil.toJson(plvOnSliceStartEvent));
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Presenter实现">

    @Override
    public void registerView(IPLVDocumentContract.View view) {
        viewWeakReferenceList.add(new WeakReference<>(view));
    }

    @Override
    public void notifyStreamStatus(boolean isStreamStarted) {
        this.isStreamStarted = isStreamStarted;
    }

    @Override
    public void switchShowMode(PLVDocumentMode showMode) {
        for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
            IPLVDocumentContract.View view = viewWeakReference.get();
            if (view != null) {
                view.onSwitchShowMode(showMode);
            }
        }
    }

    @Override
    public void enableMarkTool(boolean enable) {
        if (!checkInitialized()) {
            return;
        }
        if (enable) {
            plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.SETPAINTSTATUS, "{\"status\":\"open\"}");
        } else {
            plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.SETPAINTSTATUS, "{\"status\":\"close\"}");
        }
    }

    @Override
    public void changeColor(String colorString) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.CHANGE_COLOR, colorString);
    }

    @Override
    public void changeMarkToolType(@PLVDocumentMarkToolType.Range String markToolType) {
        if (!checkInitialized()) {
            return;
        }
        if (PLVDocumentMarkToolType.CLEAR.equals(markToolType)) {
            plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.DELETEALLPAINT, "");
        } else if (PLVDocumentMarkToolType.ERASER.equals(markToolType)) {
            plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.ERASE_STATUS, "");
        } else if (PLVDocumentMarkToolType.BRUSH.equals(markToolType)
                || PLVDocumentMarkToolType.ARROW.equals(markToolType)
                || PLVDocumentMarkToolType.TEXT.equals(markToolType)) {
            String message = "{\"type\":\"" + markToolType + "\"}";
            plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.SETDRAWTYPE, message);
        }
    }

    @Override
    public void changeToWhiteBoard() {
        if (!checkInitialized()) {
            return;
        }
        changeWhiteBoardPage(0);
    }

    @Override
    public void changeWhiteBoardPage(int pageId) {
        if (!checkInitialized()) {
            return;
        }
        PLVSChangePPTInfo changePptInfo = new PLVSChangePPTInfo(0, pageId);
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.CHANGEPPT, PLVGsonUtil.toJson(changePptInfo));
    }

    @Override
    public void changePpt(int autoId) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.CHANGEPPT, "{\"autoId\":" + autoId + "}");
        // 打开PPT文档时，默认打开第一页
        changePptPage(autoId, 0);
    }

    @Override
    public void changePptPage(int autoId, int pageId) {
        if (!checkInitialized()) {
            return;
        }
        PLVSChangePPTInfo changePptInfo = new PLVSChangePPTInfo(autoId, pageId);
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.CHANGEPPT, PLVGsonUtil.toJson(changePptInfo));
    }

    @Override
    public void changeTextContent(String content) {
        if (!checkInitialized()) {
            return;
        }
        PLVSEditTextInfo textInfo = new PLVSEditTextInfo(content);
        plvDocumentRepository.sendWebMessage(PLVSDocumentWebProcessor.FILLEDITTEXT, PLVGsonUtil.toJson(textInfo));
    }

    @Override
    public void requestGetPptCoverList() {
        requestGetPptCoverList(false);
    }

    @Override
    public void requestGetPptCoverList(boolean forceRefresh) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.requestPptCoverList(forceRefresh);
    }

    @Override
    public void requestGetPptPageList(int autoId) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.requestGetCachedPptPageList(autoId);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void deleteDocument(int autoId) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.deleteDocument(autoId);
    }

    @Override
    public void deleteDocument(String fileId) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.deleteDocument(fileId);
    }

    @Override
    public void destroy() {
        if (plvDocumentRepository != null) {
            plvDocumentRepository.destroy();
        }
        isInitialized = false;
        viewWeakReferenceList.clear();
        compositeDisposable.dispose();
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
