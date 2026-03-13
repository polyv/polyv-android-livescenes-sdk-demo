package com.easefun.polyv.livecommon.module.modules.interact;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.interact.info.PLVInteractInfo;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.PLVIARWebView;
import com.plv.livescenes.feature.interact.PLVInteractWebView2;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.socket.event.interact.PLVCallAppEvent;
import com.plv.socket.event.interact.PLVOpenOtherAppEvent;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

import java.util.List;

/**
 * 互动应用View - 我的奖励
 */
public class PLVIARLayout extends FrameLayout implements IPLVIARLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVIARLayout.class.getSimpleName();
    private IPLVLiveRoomDataManager liveRoomDataManager;

    private PLVIARWebView iarWeb;

    private PLVLiveScene liveScene;

    //是否锁定到竖屏
    private boolean isLockPortrait = true;

    private static final List<String> JS_HANDLER = listOf(
            PLVInteractJSBridgeEventConst.V2_GET_NATIVE_APP_PARAMS_INFO,
            PLVInteractJSBridgeEventConst.V2_CLOSE_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_LINK_CLICK,
            PLVInteractJSBridgeEventConst.V2_WEB_VIEW_UPDATE_APP_STATUS,
            PLVInteractJSBridgeEventConst.V2_SHOW_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_LOCK_TO_PORTRAIT,
            PLVInteractJSBridgeEventConst.V2_CALL_APP_EVENT,
            PLVInteractJSBridgeEventConst.V2_GET_INTERACT_INFO,
            PLVInteractJSBridgeEventConst.V2_CLICK_PRODUCT_BUTTON,
            PLVInteractJSBridgeEventConst.V2_SHOW_PRODUCT_DETAIL,
            PLVInteractJSBridgeEventConst.V2_WELFARE_LOTTERY_COMMENT_SUCCESS,
            PLVInteractJSBridgeEventConst.V2_WELFARE_LOTTERY_ENTRANCE_CHANGE,
            PLVInteractJSBridgeEventConst.V2_SIGN_IN_TIMEOUT_RECV
    );
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVIARLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVIARLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVIARLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_iar_layout, this, true);


        iarWeb = findViewById(R.id.plv_iar_web);

        setVisibility(View.INVISIBLE);
        for (final String event : JS_HANDLER) {
            iarWeb.registerMsgReceiverFromJs(event, new BridgeHandler() {
                @Override
                public void handler(String param, CallBackFunction callBackFunction) {
                    Log.d(TAG, event + ", param= " + param);
                    handlerJsCall(event, param, callBackFunction);
                }
            });
        }
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
            case PLVInteractJSBridgeEventConst.V2_GET_INTERACT_INFO:
                processGetInteractInfo(param, callBackFunction);
                break;
        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager, @Nullable PLVLiveScene scene) {
        this.liveScene = scene;
        this.liveRoomDataManager = liveRoomDataManager;
        String watchStatus = liveRoomDataManager.getConfig().isLive() ? PLVInteractWebView2.WATCH_STATUS_LIVE : PLVInteractWebView2.WATCH_STATUS_PLAYBACK;
        iarWeb.setWatchStatus(watchStatus);
        iarWeb.setLang(PLVLanguageUtil.isENLanguage() ? PLVInteractWebView2.LANG_EN : PLVInteractWebView2.LANG_ZH);
        observeLiveData();
    }

    @Override
    public void showLotteryRecord() {
        String data = "{\"event\" : \"SHOW_LOTTERY_RECORD_POPUP\"}";
        iarWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
            }
        });
    }

    @Override
    public boolean onBackPress() {
        if (getVisibility() == View.VISIBLE) {
            setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        if (iarWeb != null) {
            iarWeb.removeAllViews();
            ViewParent viewParent = iarWeb.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(iarWeb);
            }
            iarWeb.destroy();
            iarWeb = null;
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="订阅更新">
    private void observeLiveData() {
        //更新sessionId
        liveRoomDataManager.getSessionIdLiveData().observe((LifecycleOwner) getContext(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String sessionId) {
                if (!TextUtils.isEmpty(sessionId)) {
                    iarWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO, getNativeAppPramsInfo(), new CallBackFunction() {
                        @Override
                        public void onCallBack(String s) {
                            PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO + " " + s);
                        }
                    });
                }
            }
        });
        //更新chatToken
        liveRoomDataManager.getChatTokenLiveData().observe((LifecycleOwner) getContext(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String chatToken) {
                if (!TextUtils.isEmpty(chatToken)) {
                    iarWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO, getNativeAppPramsInfo(), new CallBackFunction() {
                        @Override
                        public void onCallBack(String s) {
                            PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO + " " + s);
                        }
                    });
                }
            }
        });

        //频道详情信息
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                if (polyvLiveClassDetailVOPLVStatefulData != null && polyvLiveClassDetailVOPLVStatefulData.getData() != null) {
                    // 切换语言环境后，所有接口需要重新请求，等接口请求完毕后，再重新加载webview
                    iarWeb.loadWeb();

                    String info = updateChannelInfo(polyvLiveClassDetailVOPLVStatefulData.getData().getData());
                    PLVCommonLog.d(TAG, "=== send:" + info);
                    iarWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_GET_INTERACT_INFO, info, new CallBackFunction() {
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
        switch (callAppEvent.getEvent()) {
            case PLVOpenOtherAppEvent.WEIXIN_EVENT:
                processOpenOtherAppEvent(param);
                return;
            default:
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
        if (close && !isLockPortrait) {
            //隐藏的时候解锁屏幕方向锁定
            PLVOrientationManager.getInstance().unlockOrientation();
        }
    }


    private void processWebViewUpdateAppStatus(String data, CallBackFunction callBackFunction) {
        PLVWebviewUpdateAppStatusVO appStatusVO = PLVGsonUtil.fromJson(PLVWebviewUpdateAppStatusVO.class, data);
        liveRoomDataManager.getInteractStatusData().setValue(appStatusVO);
    }

    private String getNativeAppPramsInfo() {
        if (liveRoomDataManager != null) {
            PLVInteractNativeAppParams nativeAppParams = PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager, liveScene);
            return PLVGsonUtil.toJsonSimple(nativeAppParams);
        }
        return "";
    }

    private void processGetInteractInfo(String param, final CallBackFunction callBackFunction) {
        if (liveRoomDataManager != null && liveRoomDataManager.getClassDetailVO().getValue().getData() != null) {
            String jsonInfo = updateChannelInfo(liveRoomDataManager.getClassDetailVO().getValue().getData().getData());
            callBackFunction.onCallBack(jsonInfo);
            PLVCommonLog.d(TAG, "processGetInteractInfo: " + jsonInfo);
        }
    }

    private String updateChannelInfo(PLVLiveClassDetailVO.DataBean data) {
        final PLVInteractInfo info = new PLVInteractInfo();
        final PLVInteractInfo.LotteryData lotteryData = new PLVInteractInfo.LotteryData();
        lotteryData.setLotteryTextCN(data.getLotteryGiftButtonTextCH());
        lotteryData.setLotteryTextEN(data.getLotteryGiftButtonTextEN());
        info.setLotteryData(lotteryData);
        String jsonInfo = PLVGsonUtil.toJsonSimple(info);
        return jsonInfo;
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

    // <editor-fold defaultstate="collapsed" desc="js调用native事件分发">
    private void processOpenOtherAppEvent(String message) {

        PLVOpenOtherAppEvent openOtherAppEvent = PLVGsonUtil.fromJson(PLVOpenOtherAppEvent.class, message);
        String value = "";
        if (openOtherAppEvent == null || openOtherAppEvent.getValue() == null) {
            return;
        } else {
            value = openOtherAppEvent.getValue().getUrl();
        }
        try {
            // 复制字段
            ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", value);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);

            //跳转到微信
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("weixin://"));
            ActivityUtils.startActivity(intent);

        } catch (Exception e) {
            PLVCommonLog.exception(e);
            ToastUtils.showLong(R.string.plv_chat_copy_success);
        }


    }

    // </editor-fold>
}
