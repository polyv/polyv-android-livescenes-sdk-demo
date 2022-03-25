package com.plv.livecloudclass.modules.ppt;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.plv.business.api.common.ppt.IPLVPPTView;
import com.plv.business.api.common.ppt.PLVLivePPTProcessor;
import com.plv.business.api.common.ppt.PLVPPTVodProcessor;
import com.plv.business.api.common.ppt.PLVPPTWebView;
import com.plv.business.web.IPLVWebMessageProcessor;
import com.plv.livecloudclass.R;
import com.plv.livecommon.module.modules.ppt.contract.IPLVPPTContract;
import com.plv.livecommon.module.modules.ppt.presenter.PLVPPTPresenter;
import com.plv.livecommon.ui.widget.PLVPlaceHolderView;
import com.plv.livescenes.log.PLVELogSender;
import com.plv.livescenes.log.ppt.PLVPPTElog;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.business.api.common.ppt.PLVLivePPTProcessor;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.web.PLVWebview;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;

import static com.plv.livescenes.log.ppt.PLVPPTElog.PPTEvent.PPT_SEND_WEB_MESSAGE;

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
    private PLVPPTWebView pptWebView;
    private PLVPlaceHolderView pptPlaceHolderView;

    //Listener
    private OnPLVLCLivePPTViewListener onLivePPTViewListener;
    private OnPLVLCPlaybackPPTViewListener onPlaybackPPTViewListener;

    //Presenter
    private IPLVPPTContract.IPLVPPTPresenter presenter;

    private boolean isLowLatencyWatch = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();
    private boolean isRtcWatch = PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch();
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
        initData();
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
        //loadWeb
        if (pptWebView != null) {
            pptWebView.setPageLoadCallback(new PLVWebview.WebPageLoadCallback() {
                @Override
                public void onLoadFinish(WebView view, String url) {
                    PLVELogSender.send(PLVPPTElog.class, PLVPPTElog.PPTEvent.PPT_LOAD_FINISH, "load finish ");
                }

                @Override
                public void onLoadStart(WebView view, String url) {

                }

                @Override
                public void onLoadSslFailed(WebView view, String url) {
                    PLVELogSender.send(PLVPPTElog.class, PLVPPTElog.PPTEvent.PPT_LOAD_FAILED, "load failed :");
                }
            });
        }
        PLVELogSender.send(PLVPPTElog.class, PLVPPTElog.PPTEvent.PPT_LOAD_START, "load start :");
        //加载ppt的webView
        if (pptWebView != null) {
            pptWebView.loadWeb();//"file:///android_asset/startForMobile.html"
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    //初始化数据
    private void initData() {
        presenter = new PLVPPTPresenter();
        presenter.init(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 1. 外部直接调用的方法 - common部分，定义直播PPT和回放PPT通用的方法">
    @Override
    public void sendWebMessage(String event, String message) {
        if (pptWebView != null) {
            PLVELogSender.send(PLVPPTElog.class, PPT_SEND_WEB_MESSAGE, "send web message :" + message);
            pptWebView.callMessage(event, message);
        }
    }

    @Override
    public void reLoad() {
        if (pptWebView != null) {
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
    public void notifyJoinRtcChannel() {
        isRtcWatch = true;
        updateMsgDelayTime();
    }

    @Override
    public void notifyLeaveRtcChannel() {
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
        sendWebMessage(PLVLivePPTProcessor.SETSEIDATA, "{\"time\":" + ts + "}");
    }

    @Override
    public void turnPagePPT(String type) {
        PLVCommonLog.d(TAG, "turnPagePPT: "+type);
        sendWebMessage(PLVLivePPTProcessor.CHANGE_PPT_PAGE, "{\"type\":\"" + type + "\"}");
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
    public IPLVPPTView getPlaybackPPTViewToBindInPlayer() {
        return new IPLVPPTView() {
            @Override
            public void pptPrepare(String message) {
                PLVCommonLog.d(TAG, "PLVLCPPTView.pptPrepare=" + message);
                hideLoading();
                if (pptWebView != null) {
                    pptWebView.callPPTParams(message);
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
            PLVELogSender.send(PLVPPTElog.class, PPT_SEND_WEB_MESSAGE, "send web message :" + message);
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

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="添加直播或回放JS事件监听器">
    //添加回放JS事件监听器
    private void addPlaybackJSEventListener() {
        if (pptWebView == null) {
            return;
        }
        IPLVWebMessageProcessor<PLVPPTVodProcessor.PLVVideoPPTCallback> processor = new
                PLVPPTVodProcessor(pptWebView);
        processor.registerJSHandler(new PLVPPTVodProcessor.PLVVideoPPTCallback() {
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
        IPLVWebMessageProcessor<PLVLivePPTProcessor.LiveJSCallback> processor =
                new PLVLivePPTProcessor(pptWebView);
        processor.registerJSHandler(new PLVLivePPTProcessor.LiveJSCallback() {
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
                if(pptStatus != null && pptStatus.getMaxTeacherOp() != null){
                    if(onLivePPTViewListener != null){
                        onLivePPTViewListener.onLivePPTStatusChange(pptStatus);
                    }
                }
            }
        });
        pptWebView.registerProcessor(processor);
    }
// </editor-fold>
}
