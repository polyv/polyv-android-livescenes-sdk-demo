package com.easefun.polyv.streameralone.modules.liveroom;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.config.PolyvLiveSDKClient;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.commodity.PLVCommodityControlWebView;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

/**
 * @author Hoshiiro
 */
public class PLVSACommodityControlLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 弹层位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 弹层尺寸
    private static final float MENU_DRAWER_HEIGHT_RATIO_PORT = 0.6F;
    private static final float MENU_DRAWER_WIDTH_RATIO_LAND = 0.5F;
    private static final int MIN_DRAWER_WIDTH_LAND = ConvertUtils.dp2px(375);

    @Nullable
    private PLVCommodityControlWebView commodityControlWebView;

    // 布局弹层
    private PLVMenuDrawer menuDrawer;

    private IPLVLiveRoomDataManager liveRoomDataManager;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVSACommodityControlLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVSACommodityControlLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVSACommodityControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化布局">

    private void initView() {

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    public void show() {
        initWebView();
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuSize((int) (PLVScreenUtils.isPortrait(getContext()) ? ScreenUtils.getScreenOrientatedHeight() * MENU_DRAWER_HEIGHT_RATIO_PORT : Math.max(ScreenUtils.getScreenOrientatedWidth() * MENU_DRAWER_WIDTH_RATIO_LAND, MIN_DRAWER_WIDTH_LAND)));
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        removeAllViews();
                        if (commodityControlWebView != null) {
                            commodityControlWebView.destroy();
                            commodityControlWebView = null;
                        }
                        menuDrawer.detachToContainer();
                    }

                    ViewGroup popupContainer = ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
                    View maskView = ((Activity) getContext()).findViewById(R.id.plvsa_popup_container_mask);
                    if (popupContainer.getChildCount() > 0) {
                        maskView.setVisibility(View.VISIBLE);
                    } else {
                        maskView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {

                }
            });
            menuDrawer.openMenu();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    private void initWebView() {
        if (commodityControlWebView != null) {
            removeAllViews();
        }
        commodityControlWebView = new PLVCommodityControlWebView(getContext());
        addView(commodityControlWebView, MATCH_PARENT, MATCH_PARENT);
        commodityControlWebView
                .setOnNeedUpdateNativeAppParamsInfoHandler(new BridgeHandler() {
                    @Override
                    public void handler(String data, CallBackFunction callBackFunction) {
                        if (liveRoomDataManager == null) {
                            return;
                        }
                        callBackFunction.onCallBack(
                                PLVGsonUtil.toJsonSimple(
                                        PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager)
                                                .setAppId(PolyvLiveSDKClient.getInstance().getAppId())
                                                .setAppSecret(PolyvLiveSDKClient.getInstance().getAppSecret())
                                )
                        );
                    }
                })
                .loadWeb();
    }

    // </editor-fold>

}
