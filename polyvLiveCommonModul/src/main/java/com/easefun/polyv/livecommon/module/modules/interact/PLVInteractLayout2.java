package com.easefun.polyv.livecommon.module.modules.interact;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.pm.ActivityInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.feature.interact.PLVInteractWebView2;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.socket.event.interact.PLVCallAppEvent;
import com.plv.socket.event.interact.PLVShowPushCardEvent;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

import java.util.List;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

/**
 * 互动应用View - v2
 *
 */
public class PLVInteractLayout2 extends FrameLayout implements IPLVInteractLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVInteractLayout2.class.getSimpleName();

    private IPLVLiveRoomDataManager liveRoomDataManager;

    private PLVInteractWebView2 plvlcInteractWeb;

    private PLVLiveScene liveScene;
    private OnOpenInsideWebViewListener onOpenInsideWebViewListener;
    private PLVInsideWebViewLayout insideWebViewLayout;

    private static final List<String> JS_HANDLER = listOf(
            PLVInteractJSBridgeEventConst.V2_GET_NATIVE_APP_PARAMS_INFO,
            PLVInteractJSBridgeEventConst.V2_CLOSE_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_LINK_CLICK,
            PLVInteractJSBridgeEventConst.V2_WEB_VIEW_UPDATE_APP_STATUS,
            PLVInteractJSBridgeEventConst.V2_SHOW_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_LOCK_TO_PORTRAIT,
            PLVInteractJSBridgeEventConst.V2_CALL_APP_EVENT
    );
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVInteractLayout2(@NonNull Context context) {
        this(context, null);
    }

    public PLVInteractLayout2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVInteractLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_interact_layout_2, this, true);


        plvlcInteractWeb = findViewById(R.id.plvlc_interact_web);

        setVisibility(View.INVISIBLE);
        for (final String event : JS_HANDLER) {
            plvlcInteractWeb.registerMsgReceiverFromJs(event, new BridgeHandler() {
                @Override
                public void handler(String param, CallBackFunction callBackFunction) {
                    Log.d(TAG, event + ", param= " + param);
                    handlerJsCall(event, param, callBackFunction);
                }
            });
        }

        insideWebViewLayout = new PLVInsideWebViewLayout(getContext());
        insideWebViewLayout.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    if (onOpenInsideWebViewListener != null) {
                        onOpenInsideWebViewListener.onClosed();
                    }
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
            }
        });
    }

    private void handlerJsCall(String event, String param, CallBackFunction callBackFunction) {
        switch (event) {
            case PLVInteractJSBridgeEventConst.V2_GET_NATIVE_APP_PARAMS_INFO:
                processGetNativeAppParamsInfo(param, callBackFunction);
                break;
            case PLVInteractJSBridgeEventConst.V2_CLOSE_WEB_VIEW:
                processWebViewVisibility(true);
                break;
            case PLVInteractJSBridgeEventConst.V2_SHOW_WEB_VIEW:
                processWebViewVisibility(false);
                break;
            case PLVInteractJSBridgeEventConst.V2_WEB_VIEW_UPDATE_APP_STATUS:
                processWebViewUpdateAppStatus(param, callBackFunction);
                break;
            case PLVInteractJSBridgeEventConst.V2_LOCK_TO_PORTRAIT:
                lockToPortrait();
                break;
            case PLVInteractJSBridgeEventConst.V2_LINK_CLICK:
                PLVWebUtils.openWebLink(param, getContext());
                break;
            case PLVInteractJSBridgeEventConst.V2_CALL_APP_EVENT:
                processCallAppEvent(param, callBackFunction);
                break;

        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        init(liveRoomDataManager, null);
    }

    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager, @Nullable PLVLiveScene scene) {
        this.liveScene = scene;
        this.liveRoomDataManager = liveRoomDataManager;
        plvlcInteractWeb.setCardPushEnabled(true);
        // 回放暂时只支持卡片推送互动
        String watchStatus = liveRoomDataManager.getConfig().isLive() ? PLVInteractWebView2.WATCH_STATUS_LIVE : PLVInteractWebView2.WATCH_STATUS_PLAYBACK;
        plvlcInteractWeb.setWatchStatus(watchStatus);
        plvlcInteractWeb.loadWeb();
        observeLiveData();
    }

    @Override
    public void setOnOpenInsideWebViewListener(OnOpenInsideWebViewListener onOpenInsideWebViewListener) {
        this.onOpenInsideWebViewListener = onOpenInsideWebViewListener;
    }

    @Override
    public void showBulletin() {
        String data = "{\"event\" : \"SHOW_BULLETIN\"}";
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
            }
        });
    }

    @Override
    public void showCardPush(PLVShowPushCardEvent showPushCardEvent) {
        String data = PLVGsonUtil.toJsonSimple(showPushCardEvent);
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
            }
        });
    }

    @Override
    public void onCallDynamicFunction(String event) {
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, event, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
            }
        });
    }

    @Override
    public boolean onBackPress() {
        if (insideWebViewLayout != null && insideWebViewLayout.onBackPressed()) {
            return true;
        }
        if (getVisibility() == View.VISIBLE) {
            setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        if (plvlcInteractWeb != null) {
            plvlcInteractWeb.removeAllViews();
            ViewParent viewParent=plvlcInteractWeb.getParent();
            if (viewParent instanceof ViewGroup){
                ViewGroup viewGroup= (ViewGroup) viewParent;
                viewGroup.removeView(plvlcInteractWeb);
            }
            plvlcInteractWeb.destroy();
            plvlcInteractWeb = null;
        }
        if (insideWebViewLayout != null) {
            insideWebViewLayout.destroy();
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="订阅更新">
    private void observeLiveData() {
        //更新sessionId
        liveRoomDataManager.getSessionIdLiveData().observe((LifecycleOwner) getContext(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String sessionId) {
                if(!TextUtils.isEmpty(sessionId)){
                    plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO, getNativeAppPramsInfo(), new CallBackFunction() {
                        @Override
                        public void onCallBack(String s) {
                            PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO + " " + s);
                        }
                    });
                }
            }
        });
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private void processCallAppEvent(String param, CallBackFunction callBackFunction) {
        PLVCallAppEvent callAppEvent = PLVGsonUtil.fromJson(PLVCallAppEvent.class, param);
        if (callAppEvent == null) {
            return;
        }
        if (callAppEvent.isOpenLinkEvent()) {
            if (callAppEvent.isInsideOpen()) {
                if (onOpenInsideWebViewListener != null) {
                    OpenUrlParam openUrlParam = onOpenInsideWebViewListener.onOpenWithParam(PLVScreenUtils.isLandscape(getContext()));
                    insideWebViewLayout.open(openUrlParam.portraitTop, callAppEvent.getUrl(), openUrlParam.containerView);
                }
            } else if (callAppEvent.isOutsideOpen()) {
                PLVOutsideWebViewActivity.start(getContext(), callAppEvent.getUrl());
            }
        }
    }

    private void processGetNativeAppParamsInfo(String param, CallBackFunction callBackFunction) {
        String nativeAppPramsInfo = getNativeAppPramsInfo();
        PLVCommonLog.d(TAG, "processGetNativeAppParamsInfo= " + nativeAppPramsInfo);
        callBackFunction.onCallBack(nativeAppPramsInfo);
    }

    private void processWebViewVisibility(boolean close) {
        Log.d(TAG, "processWebViewVisibility close: " + close);
        setVisibility(close ? View.INVISIBLE : View.VISIBLE);
        if (close) {
            //隐藏的时候解锁屏幕方向锁定
            PLVOrientationManager.getInstance().unlockOrientation();
        }
    }


    private void processWebViewUpdateAppStatus(String data, CallBackFunction callBackFunction) {
        PLVWebviewUpdateAppStatusVO appStatusVO = PLVGsonUtil.fromJson(PLVWebviewUpdateAppStatusVO.class, data);
        liveRoomDataManager.getInteractStatusData().postValue(appStatusVO);
    }

    private String getNativeAppPramsInfo(){
        if(liveRoomDataManager != null) {
            PLVInteractNativeAppParams nativeAppParams = new PLVInteractNativeAppParams()
                    .setAppId(liveRoomDataManager.getConfig().getAccount().getAppId())
                    .setAppSecret(liveRoomDataManager.getConfig().getAccount().getAppSecret())
                    .setSessionId(liveRoomDataManager.getSessionId())
                    .setChannelInfo(
                            new PLVInteractNativeAppParams.ChannelInfoDTO()
                                    .setChannelId(liveRoomDataManager.getConfig().getChannelId())
                                    .setRoomId(liveRoomDataManager.getConfig().getChannelId())
                    )
                    .setUserInfo(
                            new PLVInteractNativeAppParams.UserInfoDTO()
                                    .setUserId(liveRoomDataManager.getConfig().getUser().getViewerId())
                                    .setNick(liveRoomDataManager.getConfig().getUser().getViewerName())
                                    .setPic(liveRoomDataManager.getConfig().getUser().getViewerAvatar())
                    );

            return PLVGsonUtil.toJsonSimple(nativeAppParams);
        }
        return "";
    }

    /**
     * 锁定到竖屏
     */
    private void lockToPortrait() {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity == null) {
            return;
        }
        if (topActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            PLVOrientationManager.getInstance().unlockOrientation();
            PLVOrientationManager.getInstance().setPortrait(topActivity);
        }
        PLVOrientationManager.getInstance().lockOrientation();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="内部类">
    public interface OnOpenInsideWebViewListener {
        OpenUrlParam onOpenWithParam(boolean isLandscape);

        void onClosed();
    }

    public static class OpenUrlParam {
        private int portraitTop;
        private ViewGroup containerView;

        public OpenUrlParam(int portraitTop, ViewGroup containerView) {
            this.portraitTop = portraitTop;
            this.containerView = containerView;
        }

        public int getPortraitTop() {
            return portraitTop;
        }

        public void setPortraitTop(int portraitTop) {
            this.portraitTop = portraitTop;
        }

        public ViewGroup getContainerView() {
            return containerView;
        }

        public void setContainerView(ViewGroup containerView) {
            this.containerView = containerView;
        }
    }
    // </editor-fold>
}
