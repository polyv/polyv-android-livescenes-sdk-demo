package com.easefun.polyv.livecommon.module.modules.interact;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.PLVInteractWebView2;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 互动应用View - v2
 *
 */
public class PLVInteractLayout2 extends FrameLayout implements IPLVInteractLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private final String TAG = this.getClass().getSimpleName();

    private IPLVLiveRoomDataManager liveRoomDataManager;

    private PLVInteractWebView2 plvlcInteractWeb;

    private static final List<String> jsHandler = new ArrayList(Arrays.asList(
            PLVInteractJSBridgeEventConst.V2_GET_NATIVE_APP_PARAMS_INFO,
            PLVInteractJSBridgeEventConst.V2_CLOSE_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_LINK_CLICK,
            PLVInteractJSBridgeEventConst.V2_WEB_VIEW_UPDATE_APP_STATUS,
            PLVInteractJSBridgeEventConst.V2_SHOW_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_LOCK_TO_PORTRAIT
    ));
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
        for (final String event : jsHandler) {
            plvlcInteractWeb.registerMsgReceiverFromJs(event, new BridgeHandler() {
                @Override
                public void handler(String result, CallBackFunction callBackFunction) {
                    Log.d(TAG, event + "result: "+ result);
                    handlerJsCall(event, result, callBackFunction);
                }
            });
        }
    }

    private void handlerJsCall(String event, String result, CallBackFunction callBackFunction) {
        switch(event){
            case PLVInteractJSBridgeEventConst.V2_GET_NATIVE_APP_PARAMS_INFO:
                processGetNativeAppParamsInfo(result, callBackFunction);
                break;
            case PLVInteractJSBridgeEventConst.V2_CLOSE_WEB_VIEW:
                processWebViewVisibility(true);
                break;
            case PLVInteractJSBridgeEventConst.V2_SHOW_WEB_VIEW:
                processWebViewVisibility(false);
                break;
            case PLVInteractJSBridgeEventConst.V2_WEB_VIEW_UPDATE_APP_STATUS:
                processWebViewUpdateAppStatus(result, callBackFunction);
                break;
            case PLVInteractJSBridgeEventConst.V2_LOCK_TO_PORTRAIT:
                lockToPortrait();
                break;
            case PLVInteractJSBridgeEventConst.V2_LINK_CLICK:
                //预留
                break;

        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager){
        this.liveRoomDataManager = liveRoomDataManager;
        plvlcInteractWeb.loadWeb();
        observeLiveData();
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
    public void showMessage() {
        String data = "{\"event\" : \"SHOW_LOTTERY_RECORD\"}";//显示抽奖记录消息
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
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
                            PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO + " "+s);
                        }
                    });
                }
            }
        });
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private void processGetNativeAppParamsInfo(String result, CallBackFunction callBackFunction) {
        String nativeAppPramsInfo = getNativeAppPramsInfo();
        PLVCommonLog.d(TAG, "processGetNativeAppParamsInfo= " + nativeAppPramsInfo);
        callBackFunction.onCallBack(nativeAppPramsInfo);
    }

    private void processWebViewVisibility(boolean close){
        Log.d(TAG,"processWebViewVisibility close: "+close );
        setVisibility(close ? View.INVISIBLE : View.VISIBLE);
        if(close) {
            //隐藏的时候解锁屏幕方向锁定
            PLVOrientationManager.getInstance().unlockOrientation();
        }
    }


    private void processWebViewUpdateAppStatus(String data, CallBackFunction callBackFunction) {
        PLVWebviewUpdateAppStatusVO appStatusVO = PLVGsonUtil.fromJson(PLVWebviewUpdateAppStatusVO.class, data);
        liveRoomDataManager.getInteractStatusData().postValue(appStatusVO);
    }

    private String getNativeAppPramsInfo(){
        if(liveRoomDataManager != null){
            NativeAppParams nativeAppParams = new NativeAppParams();
            nativeAppParams.sessionId = liveRoomDataManager.getSessionId();
            nativeAppParams.channelInfo = new NativeAppParams.ChannelInfoDTO();
            nativeAppParams.channelInfo.channelId = liveRoomDataManager.getConfig().getChannelId();
            nativeAppParams.channelInfo.roomId = liveRoomDataManager.getConfig().getChannelId();
            nativeAppParams.userInfo = new NativeAppParams.UserInfoDTO();
            nativeAppParams.userInfo.nick = liveRoomDataManager.getConfig().getUser().getViewerName();
            nativeAppParams.userInfo.userId = liveRoomDataManager.getConfig().getUser().getViewerId();
            nativeAppParams.userInfo.pic = liveRoomDataManager.getConfig().getUser().getViewerAvatar();
            nativeAppParams.appId = liveRoomDataManager.getConfig().getAccount().getAppId();
            nativeAppParams.appSecret = liveRoomDataManager.getConfig().getAccount().getAppSecret();
            //todo 自定义参数p4 p5
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

    // <editor-fold defaultstate="collapsed" desc="内部类 - bean结构">

    static class NativeAppParams{

        private UserInfoDTO userInfo;
        private ChannelInfoDTO channelInfo;
        private String sessionId;
        private String appId;
        private String appSecret;


        public static class UserInfoDTO {
            private String nick;
            private String pic;
            private String userId;
        }


        public static class ChannelInfoDTO {
            private String channelId;
            private String roomId;
        }
    }
    // </editor-fold >

}
