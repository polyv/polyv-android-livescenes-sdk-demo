package com.easefun.polyv.liveecommerce.modules.commodity;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.app.Activity;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.product.PLVProductWebView;
import com.plv.livescenes.feature.pagemenu.product.vo.PLVInteractProductOnClickDataVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

/**
 * @author Hoshiiro
 */
public class PLVECCommodityPopupLayout2 extends FrameLayout {

    private PLVProductWebView commodityPopupWebView;
    private boolean isLandspace = false;

    @Nullable
    private PLVMenuDrawer menuDrawerPortrait;
    private PLVMenuDrawer menuDrawerLandspace;

    @Nullable
    private Observer<String> sessionIdObserver;

    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager;

    public PLVECCommodityPopupLayout2(@NonNull Context context) {
        this(context, null);
    }

    public PLVECCommodityPopupLayout2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECCommodityPopupLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_commodity_popup_layout, this);

        commodityPopupWebView = findViewById(R.id.plvec_commodity_popup_web_view);

        initWebView();
    }

    private void initWebView() {
        commodityPopupWebView
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
                        PLVECCommodityDetailActivity.start(getContext(), productLink);
                    }
                })
                .loadWeb();
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        observeLiveRoomDataManager();
    }

    public void show() {
        if (isLandspace) {
            showLandspaceMenDrawer();
        } else {
            showPortraitMenDrawer();
        }
    }

    private void showLandspaceMenDrawer() {
        menuDrawerLandspace = PLVMenuDrawer.attach(
                (Activity) getContext(),
                PLVMenuDrawer.Type.OVERLAY,
                Position.END,
                PLVMenuDrawer.MENU_DRAG_CONTAINER,
                ((Activity) getContext()).<ViewGroup>findViewById(R.id.plvec_popup_container)
        );
        menuDrawerLandspace.setMenuView(this);
        menuDrawerLandspace.setMenuSize((int) (ConvertUtils.dp2px(375)));
        menuDrawerLandspace.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
        menuDrawerLandspace.setDrawOverlay(false);
        menuDrawerLandspace.setDropShadowEnabled(false);
        menuDrawerLandspace.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    menuDrawerLandspace.detachToContainer();
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });
        if (menuDrawerPortrait != null) {
            menuDrawerPortrait.closeMenu();
        }
        menuDrawerLandspace.openMenu();

        if (commodityPopupWebView != null) {
            commodityPopupWebView.sendOpenProductEvent();
        }
    }

    private void showPortraitMenDrawer() {

        menuDrawerPortrait = PLVMenuDrawer.attach(
                (Activity) getContext(),
                PLVMenuDrawer.Type.OVERLAY,
                Position.BOTTOM,
                PLVMenuDrawer.MENU_DRAG_CONTAINER,
                ((Activity) getContext()).<ViewGroup>findViewById(R.id.plvec_popup_container)
        );
        menuDrawerPortrait.setMenuView(this);
        menuDrawerPortrait.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
        menuDrawerPortrait.setDrawOverlay(false);
        menuDrawerPortrait.setDropShadowEnabled(false);
        menuDrawerPortrait.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    menuDrawerPortrait.detachToContainer();
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });
        if (menuDrawerLandspace != null) {
            menuDrawerLandspace.closeMenu();
        }
        menuDrawerPortrait.openMenu();

        if (commodityPopupWebView != null) {
            commodityPopupWebView.sendOpenProductEvent();
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
                || commodityPopupWebView == null) {
            return;
        }
        commodityPopupWebView.updateNativeAppParamsInfo(generateAppParams());
    }

    @Nullable
    private PLVInteractNativeAppParams generateAppParams() {
        if (liveRoomDataManager == null) {
            return null;
        }
        return PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager);
    }

    public void setLandspace(boolean isLandspace){
        this.isLandspace = isLandspace;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (PLVScreenUtils.isLandscape(getContext())) {
            isLandspace = true;
            if (menuDrawerPortrait != null && menuDrawerPortrait.isMenuVisible()) {
                menuDrawerPortrait.closeMenu();
                showLandspaceMenDrawer();
            }
        } else {
            isLandspace = false;
            if (menuDrawerLandspace != null && menuDrawerLandspace.isMenuVisible()) {
                menuDrawerLandspace.closeMenu();
                showPortraitMenDrawer();
            }
        }
    }
}
