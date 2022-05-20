package com.easefun.polyv.livecloudclass.modules.pagemenu.product;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.pagemenu.commodity.PLVLCCommodityDetailActivity;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.product.PLVProductWebView;
import com.plv.livescenes.feature.pagemenu.product.vo.PLVInteractProductOnClickDataVO;

/**
 * @author Hoshiiro
 */
public class PLVLCProductFragment extends Fragment {

    private View view;
    private PLVProductWebView pageMenuProductWebView;

    private Observer<String> sessionIdObserver;

    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }
        return view = inflater.inflate(R.layout.plvlc_page_menu_product_fragment, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pageMenuProductWebView.destroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (liveRoomDataManager != null && sessionIdObserver != null) {
            liveRoomDataManager.getSessionIdLiveData().removeObserver(sessionIdObserver);
            sessionIdObserver = null;
        }
    }

    private void initView() {
        findView();

        initWebView();
    }

    private void initWebView() {
        pageMenuProductWebView
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

    private void findView() {
        pageMenuProductWebView = view.findViewById(R.id.plvlc_page_menu_product_web_view);
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        observeLiveRoomDataManager();
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
                || pageMenuProductWebView == null) {
            return;
        }
        pageMenuProductWebView.updateNativeAppParamsInfo(generateAppParams());
    }

    @Nullable
    private PLVInteractNativeAppParams generateAppParams() {
        if (liveRoomDataManager == null) {
            return null;
        }
        return new PLVInteractNativeAppParams()
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
    }

}
