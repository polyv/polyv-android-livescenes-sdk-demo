package com.easefun.polyv.livecommon.module.modules.commodity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.PLVInteractWebView2;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.product.PLVProductDetailWebView;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

public class PLVProductDetailLayout extends FrameLayout {
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVProductDetailWebView productDetailWebView;
    private OnClickProductListener onClickProductListener;

    public PLVProductDetailLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVProductDetailLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVProductDetailLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_product_detail_layout, this, true);

        productDetailWebView = findViewById(R.id.plv_product_detail_web);
        productDetailWebView.setLang(PLVLanguageUtil.isENLanguage() ? PLVInteractWebView2.LANG_EN : PLVInteractWebView2.LANG_ZH)
                .setOnReceiverEventCloseCurrentWebviewHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        setVisibility(View.INVISIBLE);
                    }
                })
                .setOnNeedUpdateNativeAppParamsInfoHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        callBackFunction.onCallBack(getNativeAppPramsInfo());
                    }
                })
                .setOnReceiveEventClickProductButtonHandler(new BridgeHandler() {

                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        if (onClickProductListener != null) {
                            onClickProductListener.onClickProduct(s, callBackFunction);
                        }
                    }
                });
        setVisibility(View.INVISIBLE);
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
    }

    public void setOnClickProductListener(OnClickProductListener onClickProductListener) {
        this.onClickProductListener = onClickProductListener;
    }

    public void showProductDetail(int productId) {
        productDetailWebView.setProductId(productId);
        productDetailWebView.loadWeb();
        setVisibility(View.VISIBLE);
    }

    public boolean onBackPress() {
        if (getVisibility() == View.VISIBLE) {
            setVisibility(View.INVISIBLE);
            productDetailWebView.stopLoading();
            productDetailWebView.loadUrl("about:blank");
            return true;
        } else {
            return false;
        }
    }

    public void destroy() {
        if (productDetailWebView != null) {
            productDetailWebView.removeAllViews();
            ViewParent viewParent = productDetailWebView.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(productDetailWebView);
            }
            productDetailWebView.destroy();
            productDetailWebView = null;
        }
    }

    private String getNativeAppPramsInfo() {
        if (liveRoomDataManager != null) {
            PLVInteractNativeAppParams nativeAppParams = PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager, PLVLiveScene.CLOUDCLASS);
            return PLVGsonUtil.toJsonSimple(nativeAppParams);
        }
        return "";
    }

    public interface OnClickProductListener {
        void onClickProduct(String param, CallBackFunction callBackFunction);
    }
}
