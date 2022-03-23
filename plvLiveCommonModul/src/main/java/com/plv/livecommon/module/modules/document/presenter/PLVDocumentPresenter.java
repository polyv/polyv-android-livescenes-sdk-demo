package com.plv.livecommon.module.modules.document.presenter;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.plv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.livecommon.module.data.PLVStatefulData;
import com.plv.livecommon.module.modules.document.contract.IPLVDocumentContract;
import com.plv.livecommon.module.modules.document.model.PLVDocumentRepository;
import com.plv.livecommon.module.modules.document.model.PLVPptUploadLocalRepository;
import com.plv.livecommon.module.modules.document.model.enums.PLVDocumentMarkToolType;
import com.plv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.plv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.plv.livescenes.document.PLVDocumentWebProcessor;
import com.plv.livescenes.document.model.PLVAssistantInfo;
import com.plv.livescenes.document.model.PLVChangePPTInfo;
import com.plv.livescenes.document.model.PLVEditTextInfo;
import com.plv.livescenes.document.model.PLVPPTJsModel;
import com.plv.livescenes.document.model.PLVPPTPaintStatus;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.upload.OnPLVDocumentUploadListener;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVMessageBaseEvent;
import com.plv.socket.event.ppt.PLVOnSliceStartEvent;
import com.plv.socket.eventbus.ppt.PLVOnSliceStartEventBus;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 文档操作相关全局单例Presenter
 * 涉及处理白板&ppt的信息交互逻辑
 * <p>
 * 对于文档列表获取，上传文档，删除文档等涉及网络的操作，转发到{@link PLVDocumentNetPresenter}处理
 *
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

    // 白板autoId为0
    public static final int AUTO_ID_WHITE_BOARD = 0;

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

    @Nullable
    private PLVUserAbilityManager.OnUserAbilityChangedListener onUserAbilityChangeCallback;

    /**
     * 标志位 是否正在推流
     * 非推流状态不上传画笔数据
     */
    private boolean isStreamStarted = false;

    //是否是嘉宾
    private boolean isGuest = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    @Override
    public void init(LifecycleOwner lifecycleOwner,
                     IPLVLiveRoomDataManager liveRoomDataManager,
                     PLVDocumentWebProcessor documentWebProcessor) {
        isGuest = liveRoomDataManager.getConfig().getUser().getViewerType().equals(PLVSocketUserConstant.USERTYPE_GUEST);
        initRepository(liveRoomDataManager, documentWebProcessor);
        initOnUserAbilityChangeListener();
        initSocketListener();

        observeRefreshPptMessage(lifecycleOwner);
        observePptJsModel(lifecycleOwner);
        observePptStatus(lifecycleOwner);
        observePptPaintStatus(lifecycleOwner);

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
    private void initRepository(IPLVLiveRoomDataManager liveRoomDataManager, PLVDocumentWebProcessor documentWebProcessor) {
        plvDocumentRepository = new PLVDocumentRepository(documentWebProcessor);
        plvDocumentRepository.init(liveRoomDataManager);

        PLVSocketUserBean userBean = new PLVSocketUserBean();
        userBean.setUserId(liveRoomDataManager.getConfig().getUser().getViewerId());
        userBean.setNick(liveRoomDataManager.getConfig().getUser().getViewerName());
        userBean.setPic(liveRoomDataManager.getConfig().getUser().getViewerAvatar());

        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.SETUSER, PLVGsonUtil.toJson(userBean));
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.AUTHORIZATION_PPT_PAINT, "{\"userType\":\"speaker\"}");
        if (!isGuest) {
            plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.CHANGEPPT, "{\"autoId\":0,\"isCamClosed\":0}");
        }
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.SETPAINTSTATUS, "{\"status\":\"open\"}");

        plvPptUploadLocalRepository = new PLVPptUploadLocalRepository();
    }

    /**
     * 初始化用户角色能力变化监听
     */
    private void initOnUserAbilityChangeListener() {
        this.onUserAbilityChangeCallback = new PLVUserAbilityManager.OnUserAbilityChangedListener() {
            @Override
            public void onUserAbilitiesChanged(@NonNull List<PLVUserAbility> addedAbilities, @NonNull List<PLVUserAbility> removedAbilities) {
                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onUserPermissionChange();
                    }
                }
            }
        };
        PLVUserAbilityManager.myAbility().addUserAbilityChangeListener(onUserAbilityChangeCallback);
    }

    /**
     * 初始化Socket监听
     * 监听助教切换PPT页面socket事件
     */
    private void initSocketListener() {
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (!PLVEventConstant.Ppt.ON_ASSISTANT_CONTROL.equals(event)) {
                    return;
                }
                PLVAssistantInfo assistantInfo = PLVGsonUtil.fromJson(PLVAssistantInfo.class, message);
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
        });
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
                if (isStreamStarted) {
                    PLVSocketWrapper.getInstance().emit(PLVMessageBaseEvent.LISTEN_EVENT, message);
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
        plvDocumentRepository.getPptJsModelLiveData().observe(lifecycleOwner, new Observer<PLVStatefulData<PLVPPTJsModel>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PLVPPTJsModel> plvsPptJsModel) {
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
        plvDocumentRepository.getPptStatusLiveData().observe(lifecycleOwner, new Observer<PLVPPTStatus>() {
            @Override
            public void onChanged(@Nullable PLVPPTStatus plvspptStatus) {
                if (plvspptStatus == null) {
                    return;
                }
                for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
                    IPLVDocumentContract.View view = viewWeakReference.get();
                    if (view != null) {
                        view.onPptPageChange(plvspptStatus.getAutoId(), plvspptStatus.getPageId());
                        view.onPptStatusChange(plvspptStatus);
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
        plvDocumentRepository.getPptPaintStatusLiveData().observe(lifecycleOwner, new Observer<PLVPPTPaintStatus>() {
            @Override
            public void onChanged(@Nullable PLVPPTPaintStatus plvspptPaintStatus) {
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
                            plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.ONSLICESTART, PLVGsonUtil.toJson(plvOnSliceStartEvent));
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Presenter实现">

    @Override
    public void registerView(IPLVDocumentContract.View view) {
        PLVDocumentNetPresenter.getInstance().registerView(view);
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
            plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.SETPAINTSTATUS, "{\"status\":\"open\"}");
        } else {
            plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.SETPAINTSTATUS, "{\"status\":\"close\"}");
        }
    }

    @Override
    public void changeColor(String colorString) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.CHANGE_COLOR, colorString);
    }

    @Override
    public void changeMarkToolType(@PLVDocumentMarkToolType.Range String markToolType) {
        if (!checkInitialized()) {
            return;
        }
        if (PLVDocumentMarkToolType.CLEAR.equals(markToolType)) {
            plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.DELETEALLPAINT, "");
        } else if (PLVDocumentMarkToolType.ERASER.equals(markToolType)) {
            plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.ERASE_STATUS, "");
        } else if (PLVDocumentMarkToolType.BRUSH.equals(markToolType)
                || PLVDocumentMarkToolType.ARROW.equals(markToolType)
                || PLVDocumentMarkToolType.TEXT.equals(markToolType)) {
            String message = "{\"type\":\"" + markToolType + "\"}";
            plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.SETDRAWTYPE, message);
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
        PLVChangePPTInfo changePptInfo = new PLVChangePPTInfo(AUTO_ID_WHITE_BOARD, pageId);
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.CHANGEPPT, PLVGsonUtil.toJson(changePptInfo));
    }

    @Override
    public void changePpt(int autoId) {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.CHANGEPPT, "{\"autoId\":" + autoId + "}");
        // 打开PPT文档时，默认打开第一页
        changePptPage(autoId, 0);
    }

    @Override
    public void changePptPage(int autoId, int pageId) {
        if (!checkInitialized()) {
            return;
        }
        PLVChangePPTInfo changePptInfo = new PLVChangePPTInfo(autoId, pageId);
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.CHANGEPPT, PLVGsonUtil.toJson(changePptInfo));
    }

    @Override
    public void changePptToLastStep() {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.CHANGEPPTPAGE, "{\"type\":\"gotoPreviousStep\"}");
    }

    @Override
    public void changePptToNextStep() {
        if (!checkInitialized()) {
            return;
        }
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.CHANGEPPTPAGE, "{\"type\":\"gotoNextStep\"}");
    }

    @Override
    public void changeTextContent(String content) {
        if (!checkInitialized()) {
            return;
        }
        PLVEditTextInfo textInfo = new PLVEditTextInfo(content);
        plvDocumentRepository.sendWebMessage(PLVDocumentWebProcessor.FILLEDITTEXT, PLVGsonUtil.toJson(textInfo));
    }

    @Override
    public void requestGetPptCoverList() {
        PLVDocumentNetPresenter.getInstance().requestGetPptCoverList();
    }

    @Override
    public void requestGetPptCoverList(boolean forceRefresh) {
        PLVDocumentNetPresenter.getInstance().requestGetPptCoverList(forceRefresh);
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
        PLVDocumentNetPresenter.getInstance().onSelectUploadFile(fileUri);
    }

    @Override
    public void uploadFile(Context context, File uploadFile, final String convertType, final OnPLVDocumentUploadListener listener) {
        PLVDocumentNetPresenter.getInstance().uploadFile(context, uploadFile, convertType, listener);
    }

    @Override
    public void restartUploadFromCache(Context context, String fileId, OnPLVDocumentUploadListener listener) {
        PLVDocumentNetPresenter.getInstance().restartUploadFromCache(context, fileId, listener);
    }

    @Override
    public void checkUploadFileStatus() {
        PLVDocumentNetPresenter.getInstance().checkUploadFileStatus();
    }

    @Override
    public void removeUploadCache(int autoId) {
        PLVDocumentNetPresenter.getInstance().removeUploadCache(autoId);
    }

    @Override
    public void removeUploadCache(List<PLVPptUploadLocalCacheVO> localCacheVOS) {
        PLVDocumentNetPresenter.getInstance().removeUploadCache(localCacheVOS);
    }

    @Override
    public void removeUploadCache(String fileId) {
        PLVDocumentNetPresenter.getInstance().removeUploadCache(fileId);
    }

    @Override
    public void deleteDocument(int autoId) {
        PLVDocumentNetPresenter.getInstance().deleteDocument(autoId);
    }

    @Override
    public void deleteDocument(String fileId) {
        PLVDocumentNetPresenter.getInstance().deleteDocument(fileId);
    }

    @Override
    public void requestOpenPptView(int pptId, String pptName) {
        for (WeakReference<IPLVDocumentContract.View> viewWeakReference : viewWeakReferenceList) {
            IPLVDocumentContract.View view = viewWeakReference.get();
            if (view == null) {
                continue;
            }
            if (view.onRequestOpenPptView(pptId, pptName)) {
                // consume
                break;
            }
        }
    }

    @Override
    public void destroy() {
        if (plvDocumentRepository != null) {
            plvDocumentRepository.destroy();
        }
        PLVDocumentNetPresenter.getInstance().destroy();
        isInitialized = false;
        viewWeakReferenceList.clear();
        compositeDisposable.dispose();
        onUserAbilityChangeCallback = null;
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

    // </editor-fold>
}
