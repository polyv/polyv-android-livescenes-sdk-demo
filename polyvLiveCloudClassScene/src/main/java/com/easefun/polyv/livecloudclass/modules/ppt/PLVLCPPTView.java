package com.easefun.polyv.livecloudclass.modules.ppt;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvLivePPTProcessor;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTVodProcessor;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTWebView;
import com.easefun.polyv.businesssdk.web.IPolyvWebMessageProcessor;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCPlaceHolderView;
import com.easefun.polyv.livecommon.module.modules.ppt.contract.IPLVPPTContract;
import com.easefun.polyv.livecommon.module.modules.ppt.presenter.PLVPPTPresenter;
import com.easefun.polyv.livescenes.log.PolyvELogSender;
import com.easefun.polyv.livescenes.log.ppt.PolyvPPTElog;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.web.PLVWebview;

import static com.easefun.polyv.livescenes.log.ppt.PolyvPPTElog.PPTEvent.PPT_SEND_WEB_MESSAGE;

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
    private PLVLCPlaceHolderView pptPlaceHolderView;

    //Listener
    private OnPLVLCLivePPTViewListener onLivePPTViewListener;
    private OnPLVLCPlaybackPPTViewListener onPlaybackPPTViewListener;

    //Presenter
    private IPLVPPTContract.IPLVPPTPresenter presenter;
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
            PolyvELogSender.send(PolyvPPTElog.class, PPT_SEND_WEB_MESSAGE, "send web message :" + message);
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
    public void removeDelayTime() {
        presenter.removeMsgDelayTime();
    }

    @Override
    public void recoverDelayTime() {
        presenter.recoverMsgDelayTime();
    }

    @Override
    public void sendSEIData(long ts) {
        PLVCommonLog.d(TAG, "ts=" + ts);
        sendWebMessage(PolyvLivePPTProcessor.SETSEIDATA, "{\"time\":" + ts + "}");
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
            PolyvELogSender.send(PolyvPPTElog.class, PPT_SEND_WEB_MESSAGE, "send web message :" + message);
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
        });
        pptWebView.registerProcessor(processor);
    }
// </editor-fold>
}
