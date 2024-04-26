package com.easefun.polyv.livecloudclass.modules.pagemenu.product;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

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
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.product.PLVProductWebView;
import com.plv.livescenes.feature.pagemenu.product.vo.PLVInteractProductOnClickDataVO;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

/**
 * @author Hoshiiro
 */
public class PLVLCProductLayout extends FrameLayout {

    private PLVProductWebView productWebView;

    @Nullable
    private Observer<String> sessionIdObserver;

    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager;

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
                        final String productLink = onClickDataVO.getData().getLinkByType();
                        if (TextUtils.isEmpty(productLink)) {
                            PLVToast.Builder.context(getContext())
                                    .setText(R.string.plv_commodity_toast_empty_link)
                                    .show();
                            return;
                        }
                        PLVLCCommodityDetailActivity.start(getContext(), productLink);
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

}
