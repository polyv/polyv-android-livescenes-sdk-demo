package com.easefun.polyv.livecommon.module.modules.commodity;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.PLVInteractWebView2;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.product.PLVProductAICardWebView;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

public class PLVProductAICardLayout extends FrameLayout {
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVProductAICardWebView productAICardWebView;
    private ImageView packUpIv;
    private OnClickListener onClickPackUpListener;

    public PLVProductAICardLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVProductAICardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVProductAICardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_product_ai_card_layout, this, true);

        packUpIv = findViewById(R.id.plv_pack_up_iv);
        packUpIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickPackUpListener != null) {
                    onClickPackUpListener.onClick(v);
                }
            }
        });


        productAICardWebView = findViewById(R.id.plv_product_ai_card_web);
        productAICardWebView.setLang(PLVLanguageUtil.isENLanguage() ? PLVInteractWebView2.LANG_EN : PLVInteractWebView2.LANG_ZH)
                .setOnNeedUpdateNativeAppParamsInfoHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        callBackFunction.onCallBack(getNativeAppPramsInfo());
                    }
                });
        setVisibility(View.INVISIBLE);
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
    }

    public void setOnClickPackUpListener(OnClickListener onClickPackUpListener) {
        this.onClickPackUpListener = onClickPackUpListener;
    }

    public void showProductAICard(int productId) {
        productAICardWebView.setProductId(productId);
        productAICardWebView.loadWeb();
        setVisibility(View.VISIBLE);
    }

    public void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        if (getVisibility() == View.VISIBLE) {
            setVisibility(View.INVISIBLE);
        }
    }

    public void hideAndStop() {
        hide();
        productAICardWebView.stopLoading();
        productAICardWebView.loadUrl("about:blank");
    }

    public void destroy() {
        if (productAICardWebView != null) {
            productAICardWebView.removeAllViews();
            ViewParent viewParent = productAICardWebView.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(productAICardWebView);
            }
            productAICardWebView.destroy();
            productAICardWebView = null;
        }
    }

    private String getNativeAppPramsInfo() {
        if (liveRoomDataManager != null) {
            PLVInteractNativeAppParams nativeAppParams = PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager, PLVLiveScene.CLOUDCLASS);
            return PLVGsonUtil.toJsonSimple(nativeAppParams);
        }
        return "";
    }
}
