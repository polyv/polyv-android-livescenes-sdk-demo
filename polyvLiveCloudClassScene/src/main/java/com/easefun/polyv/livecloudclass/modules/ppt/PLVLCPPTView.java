package com.easefun.polyv.livecloudclass.modules.ppt;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvLivePPTProcessor;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTVodProcessor;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTWebView;
import com.easefun.polyv.businesssdk.web.IPolyvWebMessageProcessor;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.ppt.enums.PLVLCMarkToolEnums;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.ppt.contract.IPLVPPTContract;
import com.easefun.polyv.livecommon.module.modules.ppt.enums.PLVPPTLayoutEnum;
import com.easefun.polyv.livecommon.module.modules.ppt.presenter.PLVPPTPresenter;
import com.easefun.polyv.livecommon.ui.widget.PLVPlaceHolderView;
import com.easefun.polyv.livescenes.config.PolyvLiveSDKClient;
import com.easefun.polyv.livescenes.log.PolyvELogSender;
import com.easefun.polyv.livescenes.log.ppt.PolyvPPTElog;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.business.api.common.ppt.IPLVPPTWebViewListener;
import com.plv.business.api.common.ppt.PLVLivePPTProcessor;
import com.plv.business.api.common.ppt.PLVPPTWebView;
import com.plv.business.api.common.ppt.vo.PLVPPTLocalCacheVO;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.web.PLVWebview;
import com.plv.livescenes.document.model.PLVPPTPaintStatus;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

/**
 * date: 2020/8/6
 * author: hwj
 * description: ppt view
 */
public class PLVLCPPTView extends FrameLayout implements IPLVPPTContract.IPLVPPTView, IPLVLCPPTView {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLCPPTView.class.getSimpleName();

    //当前回放视频的进度
    private int curPlaybackPosition;

    //View
    @Nullable
    private PolyvPPTWebView pptWebView;
    private PLVPlaceHolderView pptPlaceHolderView;

    //Listener
    private OnPLVLCLivePPTViewListener onLivePPTViewListener;
    private OnPLVLCPlaybackPPTViewListener onPlaybackPPTViewListener;

    //Presenter
    private IPLVPPTContract.IPLVPPTPresenter presenter;

    private boolean isLowLatencyWatch = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();
    private boolean isRtcWatch = PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch() || PLVLinkMicConfig.getInstance().isLowLatencyMixRtcWatch();

    private boolean isPPTChannelType;

    // 当前是ppt模式
    private boolean isInPPTMode;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">

    public PLVLCPPTView(Context context) {
        this(context, null);
    }

    public PLVLCPPTView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCPPTView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView(context);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    //初始View
    private void initialView(Context context) {
        View.inflate(context, R.layout.plvlc_ppt_view_layout, this);
        pptWebView = findViewById(R.id.plvlc_ppt_web_view);
        pptPlaceHolderView = findViewById(R.id.plvlc_ppt_placeholder);
        //设置占位图
        pptPlaceHolderView.setPlaceHolderImg(R.drawable.plvlc_ppt_placeholder);
        //设置占位图文本
        pptPlaceHolderView.setPlaceHolderText(getResources().getString(R.string.plv_ppt_no_document));
    }

    private void loadWeb() {
        //loadWeb
        if (pptWebView != null) {
            pptWebView.addPageLoadCallback(new PLVWebview.WebPageLoadCallback() {
                @Override
                public void onLoadFinish(WebView view, String url) {
                    PolyvELogSender.send(PolyvPPTElog.class, PolyvPPTElog.PPTEvent.PPT_LOAD_FINISH, "load finish ");
                }

                @Override
                public void onLoadStart(WebView view, String url) {

                }

                @Override
                public void onLoadSslFailed(WebView view, String url) {
                    PolyvELogSender.send(PolyvPPTElog.class, PolyvPPTElog.PPTEvent.PPT_LOAD_FAILED, "load failed :");
                }
            });
        }
        PolyvELogSender.send(PolyvPPTElog.class, PolyvPPTElog.PPTEvent.PPT_LOAD_START, "load start :");
        registerHandler();
        //加载ppt的webView
        if (canLoadWeb()) {
            pptWebView.loadWeb();//"file:///android_asset/startForMobile.html"
        }
    }

    private boolean canLoadWeb() {
        return pptWebView != null && isPPTChannelType;
    }

    private void registerHandler() {
        if (pptWebView != null) {
            //注册一些公共的事件
            pptWebView.registerHandler(PLVPPTWebView.V2_GET_NATIVE_APP_PARAMS_INFO, new BridgeHandler() {
                @Override
                public void handler(String data, CallBackFunction function) {
                    String nativeAppPramsInfo = PLVGsonUtil.toJsonSimple(new PLVInteractNativeAppParams()
                            .setAppId(PolyvLiveSDKClient.getInstance().getAppId())
                            .setAppSecret(PolyvLiveSDKClient.getInstance().getAppSecret())
                            .setAccountId(PolyvLiveSDKClient.getInstance().getUserId()));
                    function.onCallBack(nativeAppPramsInfo);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 1. 外部直接调用的方法 - common部分，定义直播PPT和回放PPT通用的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        isPPTChannelType = liveRoomDataManager.getConfig().isPPTChannelType();

        presenter = new PLVPPTPresenter();
        presenter.init(this);
        observer(liveRoomDataManager);
        loadWeb();
    }

    @Override
    public void sendWebMessage(String event, String message) {
        if (pptWebView != null) {
            pptWebView.callMessage(event, message);
        }
    }

    @Override
    public void reLoad() {
        if (canLoadWeb()) {
            pptWebView.loadWeb();
        }
    }

    @Override
    public void destroy() {
        PLVCommonLog.d(TAG, "destroy ppt view");
        presenter.destroy();
        if (pptWebView != null) {
            ViewParent viewParent = pptWebView.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(pptWebView);
            }
            pptWebView.removeAllViews();
            pptWebView.destroy();
        }
        pptWebView = null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 1. 外部直接调用的方法 - live部分，定义 直播PPT独有的方法">
    @Override
    public void initLivePPT(OnPLVLCLivePPTViewListener onPLVLCPPTViewListener) {
        this.onLivePPTViewListener = onPLVLCPPTViewListener;
        addLiveJSEventListener();
    }


    @Override
    public void notifyStartRtcWatch() {
        isRtcWatch = true;
        updateMsgDelayTime();
    }

    @Override
    public void notifyStopRtcWatch() {
        isRtcWatch = false;
        updateMsgDelayTime();
    }

    @Override
    public void setIsLowLatencyWatch(boolean isLowLatencyWatch) {
        this.isLowLatencyWatch = isLowLatencyWatch;
        updateMsgDelayTime();
    }

    @Override
    public void sendSEIData(long ts) {
        PLVCommonLog.d(TAG, "ts=" + ts);
        sendWebMessage(PolyvLivePPTProcessor.SETSEIDATA, "{\"time\":" + ts + "}");
    }

    @Override
    public void turnPagePPT(String type) {
        PLVCommonLog.d(TAG, "turnPagePPT: " + type);
        sendWebMessage(PLVLivePPTProcessor.CHANGE_PPT_PAGE, "{\"type\":\"" + type + "\"}");
    }

    @Override
    public void notifyPaintModeStatus(boolean isInPaintMode) {
        if (pptWebView != null) {
            pptWebView.setNeedGestureAction(isInPaintMode);
            pptWebView.setFocusable(true);
            pptWebView.setFocusableInTouchMode(true);
            if (isInPaintMode) {
                pptWebView.requestFocus();
            } else {
                pptWebView.clearFocus();
            }
        }

        final String userType;
        if (isInPaintMode) {
            userType = "paint";
        } else {
            userType = "";
        }
        sendWebMessage(PLVLivePPTProcessor.AUTHORIZATION_PPT_PAINT, "{\"userType\":\"" + userType + "\"}");
    }

    @Override
    public void notifyPPTModeStatus(boolean isInPPTMode) {
        this.isInPPTMode = isInPPTMode;
        if (pptWebView != null) {
            pptWebView.setIsInPPTMode(isInPPTMode);
        }
    }


    @Override
    public void notifyPaintMarkToolChanged(PLVLCMarkToolEnums.MarkTool markTool) {
        if (PLVLCMarkToolEnums.MarkTool.CLEAR.equals(markTool)) {
            sendWebMessage(PLVLivePPTProcessor.DELETE_ALL_PAINT, "");
        } else if (PLVLCMarkToolEnums.MarkTool.ERASER.equals(markTool)) {
            sendWebMessage(PLVLivePPTProcessor.ERASE_STATUS, "");
        } else if (PLVLCMarkToolEnums.MarkTool.PEN.equals(markTool)
                || PLVLCMarkToolEnums.MarkTool.RECT.equals(markTool)
                || PLVLCMarkToolEnums.MarkTool.ARROW.equals(markTool)
                || PLVLCMarkToolEnums.MarkTool.TEXT.equals(markTool)) {
            final String message = "{\"type\":\"" + markTool.getMarkTool() + "\"}";
            sendWebMessage(PLVLivePPTProcessor.SET_DRAW_TYPE, message);
        }
    }

    @Override
    public void notifyPaintMarkToolColorChanged(PLVLCMarkToolEnums.Color color) {
        sendWebMessage(PLVLivePPTProcessor.CHANGE_COLOR, color.getColorString());
    }

    @Override
    public void notifyUndoLastPaint() {
        sendWebMessage(PLVLivePPTProcessor.UNDO, "");
    }

    @Override
    public void notifyPaintUpdateTextContent(String textContent) {
        sendWebMessage(PLVLivePPTProcessor.CHANGE_TEXT_CONTENT, textContent);
    }

    @Override
    public void setLCPPTGestureListener(IPLVPPTWebViewListener.OnPLVLCPPTGestureListener listener) {
        pptWebView.setPPTWebViewGestureListener(listener);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 1. 外部直接调用的方法 - playback部分，定义回放PPT独有的方法">
    @Override
    public void setPlaybackCurrentPosition(int position) {
        curPlaybackPosition = position;
    }

    @Override
    public void initPlaybackPPT(OnPLVLCPlaybackPPTViewListener onPlaybackPPTViewListener) {
        this.onPlaybackPPTViewListener = onPlaybackPPTViewListener;
        addPlaybackJSEventListener();
    }

    @Override
    public IPolyvPPTView getPlaybackPPTViewToBindInPlayer() {
        return new IPolyvPPTView() {
            @Override
            public void pptPrepare(final String message) {
                PLVCommonLog.d(TAG, "PLVLCPPTView.pptPrepare=" + message);
                hideLoading();
                if (canLoadWeb()) {
                    pptWebView.loadWeb();
                    pptWebView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (pptWebView != null) {
                                pptWebView.callPPTParams(message);
                            }
                        }
                    }, 1500);
                }
            }

            @Override
            public void onLoadLocalPpt(@NonNull PLVPPTLocalCacheVO localCacheVO) {
                PLVCommonLog.d(TAG, "PLVLCPPTView.onLoadLocalPpt=" + localCacheVO);
                hideLoading();
                if (canLoadWeb()) {
                    final WebSettings pptWebSetting = pptWebView.getSettings();
                    boolean allowFileAccess = true;
                    pptWebSetting.setAllowFileAccess(allowFileAccess);
                    pptWebSetting.setAllowFileAccessFromFileURLs(false);

                    pptWebView.loadLocalPpt(localCacheVO);
                }
            }

            @Override
            public void play(String message) {
                PLVCommonLog.d(TAG, "PLVLCPPTView.play=" + message);
                if (pptWebView != null) {
                    pptWebView.callStart(message);
                }
            }

            @Override
            public void pause(String message) {
                PLVCommonLog.d(TAG, "PLVLCPPTView.pause=" + message);
                if (pptWebView != null) {
                    pptWebView.callPause(message);
                }
            }

            @Override
            public void seek(String message) {
                PLVCommonLog.d(TAG, "PLVLCPPTView.seek" + message);
                if (pptWebView != null) {
                    pptWebView.callSeek(message);
                }
            }
        };
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPLVPPTView实现">
    @Override
    public void sendMsgToWebView(String message) {
        if (pptWebView != null) {
            pptWebView.callUpdateWebView(message);
        }
    }

    @Override
    public void sendMsgToWebView(String msg, String event) {
        sendWebMessage(event, msg);
    }

    @Override
    public void hideLoading() {
        pptPlaceHolderView.setVisibility(GONE);
    }

    @Override
    public void switchPPTViewLocation(boolean toMainScreen) {
        if (onLivePPTViewListener != null) {
            onLivePPTViewListener.onLiveSwitchPPTViewLocation(toMainScreen);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void updateMsgDelayTime() {
        presenter.notifyIsWatchLowLatency(isRtcWatch || isLowLatencyWatch);
    }

    private void observer(IPLVLiveRoomDataManager liveRoomDataManager) {

        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> data) {
                if (data == null || data.getData() == null || data.getData().getData() == null || data.getData().getData().getWatchThemeModel() == null) {
                    return;
                }
                String watchLayout = data.getData().getData().getWatchThemeModel().getWatchLayout();
                if (watchLayout == null) {
                    return;
                }
                PLVPPTLayoutEnum layoutEnum = PLVPPTLayoutEnum.FOLLOWTEACHER;
                if (PLVPPTLayoutEnum.PPT.getValue().equals(watchLayout)) {
                    layoutEnum = PLVPPTLayoutEnum.PPT;
                } else if (PLVPPTLayoutEnum.VIDEO.getValue().equals(watchLayout)) {
                    layoutEnum = PLVPPTLayoutEnum.VIDEO;
                }
                presenter.notifyIsWatchLayout(layoutEnum);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="添加直播或回放JS事件监听器">
    //添加回放JS事件监听器
    private void addPlaybackJSEventListener() {
        if (pptWebView == null) {
            return;
        }
        IPolyvWebMessageProcessor<PolyvPPTVodProcessor.PolyvVideoPPTCallback> processor = new
                PolyvPPTVodProcessor(pptWebView);
        processor.registerJSHandler(new PolyvPPTVodProcessor.PolyvVideoPPTCallback() {
            @Override
            public void callVideoDuration(CallBackFunction function) {
                if (onPlaybackPPTViewListener != null) {
                    int pos = curPlaybackPosition;
                    String time = "{\"time\":" + pos + "}";
                    function.onCallBack(time);
                    PLVCommonLog.d(TAG, "PLVLCPPTView.callVideoDuration time=" + time);
                }
            }

            @Override
            public void pptPrepare() {
                pptPlaceHolderView.setVisibility(INVISIBLE);
            }

            @Override
            public void pptPositionChange(boolean isVideoInMain) {
                if (onPlaybackPPTViewListener != null) {
                    onPlaybackPPTViewListener.onPlaybackSwitchPPTViewLocation(!isVideoInMain);
                }
            }
        });
        pptWebView.registerProcessor(processor);
    }

    //添加直播JS事件监听器
    private void addLiveJSEventListener() {
        if (pptWebView == null) {
            return;
        }
        IPolyvWebMessageProcessor<PolyvLivePPTProcessor.LiveJSCallback> processor =
                new PolyvLivePPTProcessor(pptWebView);
        processor.registerJSHandler(new PolyvLivePPTProcessor.LiveJSCallback() {
            @Override
            public void brushPPT(String message) {
                presenter.sendPPTBrushMsg(message);
            }

            @Override
            public void screenBSSwitch(boolean pptSubShow) {/**/}

            @Override
            public void screenPLSwitch(boolean landscapeShow) {
                if (onLivePPTViewListener != null) {
                    onLivePPTViewListener.onLiveChangeToLandscape(landscapeShow);
                }
            }

            @Override
            public void startOrPause(boolean start) {
                if (onLivePPTViewListener != null) {
                    onLivePPTViewListener.onLiveStartOrPauseVideoView(start);
                }
            }

            @Override
            public void reloadVideo() {
                if (onLivePPTViewListener != null) {
                    onLivePPTViewListener.onLiveRestartVideoView();
                }
            }

            @Override
            public void backTopActivity() {
                if (onLivePPTViewListener != null) {
                    onLivePPTViewListener.onLiveBackTopActivity();
                }
            }

            @Override
            public void onPPTStatusChange(String data) {
                PLVPPTStatus pptStatus = PLVGsonUtil.fromJson(PLVPPTStatus.class, data);
                if (pptStatus != null && pptStatus.getMaxTeacherOp() != null) {
                    if (onLivePPTViewListener != null) {
                        onLivePPTViewListener.onLivePPTStatusChange(pptStatus);
                    }
                }
            }

            @Override
            public void onEditTextChanged(String data) {
                final PLVPPTPaintStatus pptPaintStatus = PLVGsonUtil.fromJson(PLVPPTPaintStatus.class, data);
                if (onLivePPTViewListener != null) {
                    onLivePPTViewListener.onPaintEditText(pptPaintStatus);
                }
            }
        });
        pptWebView.registerProcessor(processor);
    }
// </editor-fold>
}
