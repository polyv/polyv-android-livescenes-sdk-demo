package com.easefun.polyv.livecommon.module.modules.interact;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;
import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
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
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.interact.info.PLVInteractInfo;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.PLVWelfareLotteryManager;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.PLVRedpackViewModel;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.google.gson.GsonBuilder;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVJsonUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.feature.interact.PLVInteractWebView2;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.product.vo.PLVInteractProductOnClickDataVO;
import com.plv.livescenes.model.PLVChatFunctionSwitchVO;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.livescenes.model.interact.PLVWelfareLotteryVO;
import com.plv.socket.event.interact.PLVCallAppEvent;
import com.plv.socket.event.interact.PLVChangeRedpackStatusEvent;
import com.plv.socket.event.interact.PLVCheckLotteryCommentEvent;
import com.plv.socket.event.interact.PLVOpenOtherAppEvent;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.interact.PLVShowLotteryEvent;
import com.plv.socket.event.interact.PLVShowOpenLinkEvent;
import com.plv.socket.event.interact.PLVShowPushCardEvent;
import com.plv.socket.event.interact.PLVShowWelfareLotteryEvent;
import com.plv.socket.event.interact.PLVUpdateChannelSwitchEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 互动应用View - v2
 */
public class PLVInteractLayout2 extends FrameLayout implements IPLVInteractLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVInteractLayout2.class.getSimpleName();

    private final PLVRedpackViewModel redpackViewModel = PLVDependManager.getInstance().get(PLVRedpackViewModel.class);
    private final PLVWelfareLotteryManager welfareLotteryManager = PLVDependManager.getInstance().get(PLVWelfareLotteryManager.class);

    private IPLVLiveRoomDataManager liveRoomDataManager;

    private PLVInteractWebView2 plvlcInteractWeb;

    private PLVLiveScene liveScene;
    private OnOpenInsideWebViewListener onOpenInsideWebViewListener;
    private PLVInsideWebViewLayout insideWebViewLayout;
    private OnClickProductListener onClickProductListener;

    //是否锁定到竖屏
    private boolean isLockPortrait = false;

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
            PLVInteractJSBridgeEventConst.V2_WELFARE_LOTTERY_COMMENT_SUCCESS,
            PLVInteractJSBridgeEventConst.V2_WELFARE_LOTTERY_ENTRANCE_CHANGE,
            PLVInteractJSBridgeEventConst.V2_SIGN_IN_TIMEOUT_RECV
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
            case PLVInteractJSBridgeEventConst.V2_GET_INTERACT_INFO:
                processGetInteractInfo(param, callBackFunction);
                break;
            case PLVInteractJSBridgeEventConst.V2_CLICK_PRODUCT_BUTTON:
                processClickProductEvent(param, callBackFunction);
                break;
            case PLVInteractJSBridgeEventConst.V2_WELFARE_LOTTERY_COMMENT_SUCCESS:
                processWelfareLotteryComment(param);
                break;
            case PLVInteractJSBridgeEventConst.V2_WELFARE_LOTTERY_ENTRANCE_CHANGE:
                processWelfareLottery(param);
                break;
            case PLVInteractJSBridgeEventConst.V2_SIGN_IN_TIMEOUT_RECV:
                processSignInTimeout();
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

        redpackViewModel.initData(liveRoomDataManager);

        plvlcInteractWeb.setCardPushEnabled(true);
        // 回放暂时只支持卡片推送互动
        String watchStatus = liveRoomDataManager.getConfig().isLive() ? PLVInteractWebView2.WATCH_STATUS_LIVE : PLVInteractWebView2.WATCH_STATUS_PLAYBACK;
        plvlcInteractWeb.setWatchStatus(watchStatus);
        plvlcInteractWeb.setLang(PLVLanguageUtil.isENLanguage() ? PLVInteractWebView2.LANG_EN : PLVInteractWebView2.LANG_ZH);
        observeLiveData();
        initListener();
    }

    @Override
    public void setOnOpenInsideWebViewListener(OnOpenInsideWebViewListener onOpenInsideWebViewListener) {
        this.onOpenInsideWebViewListener = onOpenInsideWebViewListener;
    }

    @Override
    public void setOnClickProductListener(OnClickProductListener listener) {
        this.onClickProductListener = listener;
    }

    @Override
    public void onShowJobDetail(PLVShowJobDetailEvent event) {
        String data = PLVGsonUtil.toJsonSimple(event);
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + data);
            }
        });
    }

    @Override
    public void onShowOpenLink() {
        if (liveRoomDataManager != null && liveRoomDataManager.getClassDetailVO().getValue().getData() != null) {
            PLVLiveClassDetailVO.DataBean classDetail = liveRoomDataManager.getClassDetailVO().getValue().getData().getData();
            String link = classDetail.getWatchUrl();

            boolean isOpenPay = classDetail.isProductPayOrderEnabled();
            if (!isOpenPay) {
                ToastUtils.showLong(getResources().getString(R.string.plv_interact_no_support_buy));
                return;
            }

            PLVShowOpenLinkEvent linkEvent = new PLVShowOpenLinkEvent();
            PLVShowOpenLinkEvent.ShowOpenLinkTypeBean bean = new PLVShowOpenLinkEvent.ShowOpenLinkTypeBean();
            bean.setUrl(link);
            bean.setTitle(getResources().getString(R.string.plv_interact_open_weixin_buy));
            // 目前只支持copyToWeixin 跳转到微信
            bean.setBtnType("copyToWeixin");
            linkEvent.setData(bean);
            String data = PLVGsonUtil.toJsonSimple(linkEvent);
            // SDK不处理，由集成用户自己处理
//            plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
//                @Override
//                public void onCallBack(String data) {
//                    PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + data);
//                }
//            });
        }
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
    public void showQuestionnaire() {
        String data = "{\"event\" : \"SHOW_QUESTIONNAIRE\"}";
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
    public void showLottery(PLVShowLotteryEvent showLotteryEvent) {
        String data = PLVGsonUtil.toJsonSimple(showLotteryEvent);
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
            }
        });
    }

    public void showWelfareLottery(PLVShowWelfareLotteryEvent event) {
        String data = PLVGsonUtil.toJsonSimple(event);
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
            }
        });
    }

    public void checkWelfareLotteryComment(final PLVCheckLotteryCommentEvent event) {

        PLVAppUtils.postToMainThread(new Runnable() {
            @Override
            public void run() {
                String data = PLVGsonUtil.toJsonSimple(event);
                plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
                    @Override
                    public void onCallBack(String s) {
                        PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
                    }
                });
            }
        });

    }

    @Override
    public void updateChannelSwitch(List<PLVChatFunctionSwitchVO.DataBean> dataBeanList) {
        if (dataBeanList == null || dataBeanList.isEmpty()) {
            return;
        }
        PLVUpdateChannelSwitchEvent updateChannelSwitchEvent = new PLVUpdateChannelSwitchEvent();
        List<PLVUpdateChannelSwitchEvent.ValueBean> valueBeans = new ArrayList<>();
        for (PLVChatFunctionSwitchVO.DataBean dataBean : dataBeanList) {
            PLVUpdateChannelSwitchEvent.ValueBean valueBean = new PLVUpdateChannelSwitchEvent.ValueBean();
            valueBean.setEnabled(dataBean.getEnabled());
            valueBean.setType(dataBean.getType());
            valueBeans.add(valueBean);
        }
        updateChannelSwitchEvent.setValue(valueBeans);
        String data = PLVGsonUtil.toJsonSimple(updateChannelSwitchEvent);
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_CHANNEL_CONFIG, data, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_CHANNEL_CONFIG + " " + s);
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
    public void receiveRedPaper(PLVRedPaperEvent redPaperEvent) {
        redpackViewModel.receiveRedPaper(redPaperEvent, liveRoomDataManager.getConfig().getChannelId(), liveRoomDataManager.getConfig().getUser().getViewerId());

        final String payload = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .create()
                .toJson(
                        mapOf(
                                pair("event", PLVInteractJSBridgeEventConst.EVENT_OPEN_RED_PAPER),
                                pair("data", redPaperEvent)
                        )
                );
        plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, payload, new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + data);
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (plvlcInteractWeb != null) {
            plvlcInteractWeb.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void updateOrientationLock(boolean isLock) {
        this.isLockPortrait = isLock;
    }

    @Override
    public void destroy() {
        if (plvlcInteractWeb != null) {
            plvlcInteractWeb.removeAllViews();
            ViewParent viewParent = plvlcInteractWeb.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
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
                if (!TextUtils.isEmpty(sessionId)) {
                    plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO, getNativeAppPramsInfo(), new CallBackFunction() {
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
                    plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO, getNativeAppPramsInfo(), new CallBackFunction() {
                        @Override
                        public void onCallBack(String s) {
                            PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO + " " + s);
                        }
                    });
                }
            }
        });
        //频道开关
        liveRoomDataManager.getFunctionSwitchVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvChatFunctionSwitchVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvChatFunctionSwitchVO> chatFunctionSwitchVOPLVStatefulData) {
                if (chatFunctionSwitchVOPLVStatefulData != null && chatFunctionSwitchVOPLVStatefulData.getData() != null) {
                    updateChannelSwitch(chatFunctionSwitchVOPLVStatefulData.getData().getData());
                }
            }
        });

        //频道详情信息
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                if (polyvLiveClassDetailVOPLVStatefulData != null && polyvLiveClassDetailVOPLVStatefulData.getData() != null) {
                    // 切换语言环境后，所有接口需要重新请求，等接口请求完毕后，再重新加载webview
                    plvlcInteractWeb.loadWeb();

                    String info = updateChannelInfo(polyvLiveClassDetailVOPLVStatefulData.getData().getData());
                    PLVCommonLog.d(TAG, "=== send:" + info);
                    plvlcInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_GET_INTERACT_INFO, info, new CallBackFunction() {
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

    // <editor-fold defaultstate="collapsed" desc="监听注册">
    void initListener() {
        welfareLotteryManager.setOnWelfareLotteryEnterClickListener(new PLVWelfareLotteryManager.OnWelfareLotteryEnterClickListener() {
            @Override
            public void onClick(PLVShowWelfareLotteryEvent event) {
                showWelfareLottery(event);
            }
        });

        welfareLotteryManager.setOnWelfareLotteryCommendListener(new PLVWelfareLotteryManager.OnWelfareLotteryCommendListener() {
            @Override
            public void onCommendMessage(PLVCheckLotteryCommentEvent event) {
                checkWelfareLotteryComment(event);
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
            case PLVChangeRedpackStatusEvent.EVENT:
                processChangeRedpackStatusEvent(param);
                return;
            case PLVOpenOtherAppEvent.WEIXIN_EVENT:
                processOpenOtherAppEvent(param);
                return;
            default:
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
        } else if (callAppEvent.isUpdateIarEntranceEvent()) {
            liveRoomDataManager.getInteractEntranceData().postValue(callAppEvent.getDataArray());
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

    private void processClickProductEvent(String param, final CallBackFunction callBackFunction) {
        if (onClickProductListener != null) {
            if (!PLVDebounceClicker.tryClick(this.getClass().getName(), seconds(1).toMillis())) {
                return;
            }
            final PLVInteractProductOnClickDataVO onClickDataVO = PLVGsonUtil.fromJson(PLVInteractProductOnClickDataVO.class, param);
            if (onClickDataVO == null || onClickDataVO.getData() == null || getContext() == null) {
                return;
            }

            if (onClickDataVO.getData().isInnerBuy()) {
                onShowOpenLink();
                return;
            }

            final String productLink = onClickDataVO.getData().getLinkByType();
            if (TextUtils.isEmpty(productLink)) {
                PLVToast.Builder.context(getContext())
                        .setText(R.string.plv_commodity_toast_empty_link)
                        .show();
                return;
            }
            onClickProductListener.onClickProduct(productLink);

        }
    }

    private void processWelfareLottery(String param) {
        PLVWelfareLotteryVO welfareLotteryVO = PLVGsonUtil.fromJson(PLVWelfareLotteryVO.class, param);
        welfareLotteryManager.acceptWelfareLotteryVO(welfareLotteryVO);
    }

    private void processWelfareLotteryComment(String param) {
        String comment = "";
        try {
            JSONObject jsonObject = new JSONObject(param);
            comment = PLVJsonUtils.getString(jsonObject, "comment", "");
        } catch (Exception e) {

        }
        welfareLotteryManager.handleLotteryComment(comment);

    }

    private void processSignInTimeout() {
        final Context context = getContext();
        if (context instanceof Activity) {
            PLVCommonLog.i(TAG, "kick out by notCheckIn");
            PLVToast.Builder.context(context)
                    .setText(R.string.plv_interact_sign_in_timeout_toast)
                    .longDuration()
                    .show();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((Activity) context).finish();
                }
            }, 3000);
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

    private void processChangeRedpackStatusEvent(final String message) {
        final PLVChangeRedpackStatusEvent changeRedpackStatusEvent = PLVGsonUtil.fromJson(PLVChangeRedpackStatusEvent.class, message);
        if (changeRedpackStatusEvent == null || !changeRedpackStatusEvent.isValid()) {
            return;
        }
        redpackViewModel.updateRedPaperReceiveStatus(changeRedpackStatusEvent.getValue().getRedpackId(), PLVRedPaperReceiveType.match(changeRedpackStatusEvent.getValue().getStatus()));
    }

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

    public interface OnClickProductListener {
        void onClickProduct(String link);
    }
    // </editor-fold>
}
