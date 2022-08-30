package com.easefun.polyv.livecommon.module.modules.document.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livescenes.document.PLVSDocumentWebProcessor;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.document.model.PLVSPPTPaintStatus;
import com.easefun.polyv.livescenes.document.model.PLVSPPTStatus;
import com.easefun.polyv.livescenes.upload.IPLVSDocumentUploadManager;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.business.api.common.ppt.PLVLivePPTProcessor;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.document.PLVDocumentWebProcessor;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVSocketUserConstant;

import java.io.File;
import java.lang.ref.WeakReference;

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

    private PLVSocketMessageObserver.OnMessageListener onMessageListener;

    // PPT文档上传管理器
    @Nullable
    private IPLVSDocumentUploadManager documentUploadManager;
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // 数据 - PPT文档列表变更
    private final MutableLiveData<PLVStatefulData<PLVSPPTInfo>> plvsPptInfoLiveData = new MutableLiveData<>();
    // 数据 - PPT页面列表变更
    private final MutableLiveData<PLVStatefulData<PLVSPPTJsModel>> plvsPptJsModelLiveData = new MutableLiveData<>();
    // 数据 - 文档缩放比例变更
    private final MutableLiveData<String> documentZoomValueLiveData = new MutableLiveData<>();
    // 事件 - PPT内容变更
    private final MutableLiveData<String> refreshPptMessageLiveData = new MutableLiveData<>();
    // 事件 - PPT页面状态变更
    private final MutableLiveData<PLVSPPTStatus> plvsPptStatusLiveData = new MutableLiveData<>();
    // 事件 - PPT文本标注内容变更
    private final MutableLiveData<PLVSPPTPaintStatus> plvsPptPaintStatusLiveData = new MutableLiveData<>();

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
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager=liveRoomDataManager;
        initMsgListener();
        initWebProcessor();
    }

    private void initMsgListener() {
        //接收PPT等信息更新状态
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (PLVEventConstant.Ppt.ON_SLICE_START_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_DRAW_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_CONTROL_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_OPEN_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_ID_EVENT.equals(event)) {
                    PLVCommonLog.d(TAG, "receive ppt message: delay" + message);
                    if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(liveRoomDataManager.getConfig().getUser().getViewerType())
                            && PLVEventConstant.Ppt.ON_SLICE_ID_EVENT.equals(event)) {
                        return;
                    }
                    PLVDocumentWebProcessor webProcessor = documentWebProcessorWeakReference.get();
                    if (webProcessor != null) {
                        webProcessor.getWebview().callMessage(PLVLivePPTProcessor.UPDATE_PPT, message);
                    }
                }

                //如果开播端异常退出，需要恢复直播的话，更新退出前的数据
                if(PLVEventConstant.Ppt.ON_SLICE_ID_EVENT.equals(event) &&
                        liveRoomDataManager.isNeedStreamRecover()){
                    updatePPTStatusByOnSliceID(message);
                }
            }
        };
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener);
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

            @Override
            public void onZoomChange(String zoomValue) {
                documentZoomValueLiveData.postValue(zoomValue);
            }
        });
    }

    /**
     * 通过onSliceID更新pptStatus信息
     * @param message
     */
    private void updatePPTStatusByOnSliceID(String message){
        PLVOnSliceIDEvent event = PLVGsonUtil.fromJson(PLVOnSliceIDEvent.class, message);
        if(event != null && event.getData() != null){
            PLVOnSliceIDEvent.DataBean data = event.getData();
            PLVSPPTStatus pptStatus = new PLVSPPTStatus();
            pptStatus.setAutoId(data.getAutoId());
            pptStatus.setStep(PLVFormatUtils.integerValueOf(data.getStep(), 0));
            pptStatus.setPageId(data.getPageId());
            plvsPptStatusLiveData.postValue(pptStatus);
        }
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
        PLVCommonLog.d(TAG,"event="+event+" msg="+message);
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
        PLVDocumentNetRepo.getInstance().requestPptCoverList();
    }

    /**
     * 请求更新PPT文档列表数据
     * 回调 {@link #getPptInfoLiveData()}
     *
     * @param forceRefresh 是否强制从服务器获取数据以刷新列表
     */
    public void requestPptCoverList(boolean forceRefresh) {
        PLVDocumentNetRepo.getInstance().requestPptCoverList(forceRefresh);
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
     * @param context  context
     * @param file     需要上传的文件
     * @param type     转码类型 {@link com.easefun.polyv.livescenes.upload.PLVSDocumentUploadConstant.PPTConvertType}
     * @param listener 上传监听回调
     */
    public void uploadPptFile(final Context context, final File file, final String type, final OnPLVSDocumentUploadListener listener) {
        PLVDocumentNetRepo.getInstance().uploadPptFile(context, file, type, listener);
    }

    /**
     * 删除PPT文档
     *
     * @param autoId 需要删除的文档ID
     */
    public void deleteDocument(final int autoId) {
        PLVDocumentNetRepo.getInstance().deleteDocument(autoId);
    }

    /**
     * 删除PPT文档
     *
     * @param fileId 需要删除的ppt文件ID
     */
    public void deleteDocument(final String fileId) {
        PLVDocumentNetRepo.getInstance().deleteDocument(fileId);
    }

    /**
     * 销毁方法
     */
    public void destroy() {
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
        cachePptInfo = null;
        cachePptJsModel.clear();
        cachePptJsModel = null;
        PLVDocumentNetRepo.getInstance().destroy();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="LiveData回调获取">

    public LiveData<String> getRefreshPptMessageLiveData() {
        return refreshPptMessageLiveData;
    }

    public LiveData<PLVStatefulData<PLVSPPTInfo>> getPptInfoLiveData() {
        return PLVDocumentNetRepo.getInstance().getPptInfoLiveData();
    }

    public LiveData<PLVStatefulData<PLVSPPTJsModel>> getPptJsModelLiveData() {
        return plvsPptJsModelLiveData;
    }

    public LiveData<String> getDocumentZoomValueLiveData() {
        return documentZoomValueLiveData;
    }

    public LiveData<PLVSPPTStatus> getPptStatusLiveData() {
        return plvsPptStatusLiveData;
    }

    public LiveData<PLVSPPTPaintStatus> getPptPaintStatusLiveData() {
        return plvsPptPaintStatusLiveData;
    }

    public LiveData<PLVStatefulData<PLVSPPTInfo.DataBean.ContentsBean>> getPptOnDeleteResponseLiveData() {
        return PLVDocumentNetRepo.getInstance().getPptOnDeleteResponseLiveData();
    }

    // </editor-fold>
}
