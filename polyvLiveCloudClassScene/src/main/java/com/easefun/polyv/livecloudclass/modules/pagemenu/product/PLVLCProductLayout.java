package com.easefun.polyv.livecloudclass.modules.pagemenu.product;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.pagemenu.commodity.PLVLCCommodityDetailActivity;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.modules.commodity.PLVProductExplainActivity;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.product.PLVProductWebView;
import com.plv.livescenes.feature.pagemenu.product.vo.PLVInteractProductOnClickDataVO;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.interact.PLVShowProductDetailEvent;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

/**
 * @author Hoshiiro
 */
public class PLVLCProductLayout extends FrameLayout {
    private static final String TAG = PLVLCProductLayout.class.getSimpleName();
    private PLVProductWebView productWebView;

    @Nullable
    private Observer<String> sessionIdObserver;

    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager;

    private OnViewActionListener listener;

    public PLVLCProductLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCProductLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCProductLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_product_layout, this);

        productWebView = findViewById(R.id.plvlc_product_web_view);

        initWebView();
    }

    private void initWebView() {
        productWebView
                .setLang(PLVLanguageUtil.isENLanguage() ? PLVProductWebView.LANG_EN : PLVProductWebView.LANG_ZH)
                .setOnNeedUpdateNativeAppParamsInfoHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        callBackFunction.onCallBack(PLVGsonUtil.toJsonSimple(generateAppParams()));
                    }
                })
                .setOnReceiveEventClickProductButtonHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        if (!PLVDebounceClicker.tryClick(this.getClass().getName(), seconds(1).toMillis())) {
                            return;
                        }
                        final PLVInteractProductOnClickDataVO onClickDataVO = PLVGsonUtil.fromJson(PLVInteractProductOnClickDataVO.class, s);
                        if (onClickDataVO == null || onClickDataVO.getData() == null || getContext() == null) {
                            return;
                        }
                        if (onClickDataVO.getData().isInnerBuy()) {
                            if (listener != null) {
                                listener.onShowOpenLink();
                            }
                            return;
                        }
                        final String productLink = onClickDataVO.getData().getLinkByType();
                        if (TextUtils.isEmpty(productLink)) {
                            PLVToast.Builder.context(getContext())
                                    .setText(R.string.plv_commodity_toast_empty_link)
                                    .show();
                            return;
                        }
                        PLVLCCommodityDetailActivity.start(getContext(), productLink, liveRoomDataManager);
                    }
                })
                .setOnReceiveEventClickProductExplainButtonHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        if (!PLVDebounceClicker.tryClick(this.getClass().getName(), seconds(1).toMillis())) {
                            return;
                        }
                        final PLVInteractProductOnClickDataVO onClickDataVO = PLVGsonUtil.fromJson(PLVInteractProductOnClickDataVO.class, s);
                        if (onClickDataVO == null || onClickDataVO.getData() == null || getContext() == null) {
                            return;
                        }
                        PLVProductExplainActivity.start(getContext(), onClickDataVO.getData().getProductId(), getNativeAppPramsInfo());
                    }
                })
                .setOnReceiverEventShowJobDetailHandler(new BridgeHandler() {
                    @Override
                    public void handler(String data, CallBackFunction function) {
                        if (listener != null) {
                            PLVShowJobDetailEvent event =PLVGsonUtil.fromJson(PLVShowJobDetailEvent.class, data);
                            listener.onShowJobDetail(event);
                        }
                    }
                })
                .setOnReceiveEventShowProductDetailHandler(new BridgeHandler() {
                    @Override
                    public void handler(String data, CallBackFunction function) {
                        if (listener != null) {
                            PLVShowProductDetailEvent event = PLVGsonUtil.fromJson(PLVShowProductDetailEvent.class, data);
                            if (event == null) {
                                return;
                            }
                            listener.onShowProductDetail(event);
                        }
                    }
                })
                .loadWeb();
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        observeLiveRoomDataManager();
    }

    public void sendOpenProductEvent() {
        if (productWebView != null) {
            productWebView.sendOpenProductEvent();
        }
    }

    public void setOnViewShowListener(OnViewActionListener listener) {
        this.listener = listener;
    }

    public void destroy() {
        if (productWebView != null) {
            productWebView.destroy();
        }
    }

    private void observeLiveRoomDataManager() {
        if (liveRoomDataManager == null) {
            return;
        }
        if (sessionIdObserver != null) {
            liveRoomDataManager.getSessionIdLiveData().removeObserver(sessionIdObserver);
        }
        liveRoomDataManager.getSessionIdLiveData().observeForever(sessionIdObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                updateNativeAppParamToWebView();
            }
        });
        //更新chatToken
        liveRoomDataManager.getChatTokenLiveData().observe((LifecycleOwner) getContext(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String chatToken) {
                if (!TextUtils.isEmpty(chatToken) && productWebView != null) {
                    productWebView.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO, getNativeAppPramsInfo(), new CallBackFunction() {
                        @Override
                        public void onCallBack(String s) {
                            PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO + " " + s);
                        }
                    });
                }
            }
        });
    }

    private void updateNativeAppParamToWebView() {
        if (liveRoomDataManager == null
                || TextUtils.isEmpty(liveRoomDataManager.getSessionId())
                || productWebView == null) {
            return;
        }
        productWebView.updateNativeAppParamsInfo(generateAppParams());
    }

    @Nullable
    private PLVInteractNativeAppParams generateAppParams() {
        if (liveRoomDataManager == null) {
            return null;
        }
        return PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager);
    }

    private String getNativeAppPramsInfo() {
        if (liveRoomDataManager != null) {
            PLVInteractNativeAppParams nativeAppParams = PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager);
            return PLVGsonUtil.toJsonSimple(nativeAppParams);
        }
        return "";
    }

    public interface OnViewActionListener {
        void onShowJobDetail(PLVShowJobDetailEvent param);

        void onShowProductDetail(PLVShowProductDetailEvent param);

        void onShowOpenLink();
    }

}
