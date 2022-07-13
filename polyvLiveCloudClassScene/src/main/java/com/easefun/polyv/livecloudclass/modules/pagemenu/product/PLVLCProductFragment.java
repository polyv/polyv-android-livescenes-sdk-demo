package com.easefun.polyv.livecloudclass.modules.pagemenu.product;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLCProductFragment extends PLVBaseFragment {

    private View rootView;
    private PLVLCProductLayout pageMenuProductLayout;

    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);

    private final ProductLandscapeLayoutHelper landscapeLayoutHelper = new ProductLandscapeLayoutHelper();

    @Nullable
    private PLVOrientationManager.OnConfigurationChangedListener onConfigurationChangedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) {
            return rootView;
        }
        return rootView = inflater.inflate(R.layout.plvlc_page_menu_product_fragment, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (onConfigurationChangedListener != null) {
            PLVOrientationManager.getInstance().removeOnConfigurationChangedListener(onConfigurationChangedListener);
            onConfigurationChangedListener = null;
        }
    }

    private void initView() {
        findView();
        setOnLandscapeMenuHideListener();
        observeOnOrientationChanged();
        observeShowProductViewOnLandscape();
    }

    private void findView() {
        pageMenuProductLayout = rootView.findViewById(R.id.plvlc_page_menu_product_layout);
    }

    private void setOnLandscapeMenuHideListener() {
        landscapeLayoutHelper.setOnMenuHide(new PLVSugarUtil.Consumer<View>() {
            @Override
            public void accept(View view) {
                commodityViewModel.onLandscapeProductLayoutHide();
                ((ViewGroup) rootView).addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });
    }

    private void observeOnOrientationChanged() {
        if (onConfigurationChangedListener != null) {
            PLVOrientationManager.getInstance().removeOnConfigurationChangedListener(onConfigurationChangedListener);
        }
        PLVOrientationManager.getInstance().addOnConfigurationChangedListener(
                onConfigurationChangedListener = new PLVOrientationManager.OnConfigurationChangedListener() {
                    @Override
                    public void onCall(Context context, boolean isLandscape) {
                        if (!isLandscape && landscapeLayoutHelper.isShowing()) {
                            landscapeLayoutHelper.hide();
                        }
                    }
                });
    }

    private void observeShowProductViewOnLandscape() {
        commodityViewModel.getCommodityUiStateLiveData()
                .observe((LifecycleOwner) rootView.getContext(), new Observer<PLVCommodityUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVCommodityUiState uiState) {
                        if (uiState == null) {
                            return;
                        }
                        if (uiState.hasProductView && uiState.showProductViewOnLandscape && !landscapeLayoutHelper.isShowing()) {
                            landscapeLayoutHelper.show(((Activity) rootView.getContext()).<ViewGroup>findViewById(R.id.plvlc_popup_container), pageMenuProductLayout);
                        } else if (landscapeLayoutHelper.isShowing()) {
                            landscapeLayoutHelper.hide();
                        }
                    }
                });
    }

    public void init(final IPLVLiveRoomDataManager liveRoomDataManager) {
        runAfterOnActivityCreated(new Runnable() {
            @Override
            public void run() {
                pageMenuProductLayout.init(liveRoomDataManager);
            }
        });
    }

    private static class ProductLandscapeLayoutHelper {

        @Nullable
        private PLVMenuDrawer menuDrawer;
        @Nullable
        private PLVSugarUtil.Consumer<View> onMenuHideCallback;

        public void show(ViewGroup container, View productLayout) {
            if (productLayout.getParent() != null) {
                ((ViewGroup) productLayout.getParent()).removeView(productLayout);
            }
            if (menuDrawer == null) {
                menuDrawer = PLVMenuDrawer.attach(
                        (Activity) container.getContext(),
                        PLVMenuDrawer.Type.OVERLAY,
                        Position.END,
                        PLVMenuDrawer.MENU_DRAG_CONTAINER,
                        container
                );
                menuDrawer.setMenuView(productLayout);
                menuDrawer.setMenuSize(ConvertUtils.dp2px(375));
                menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
                menuDrawer.setDrawOverlay(false);
                menuDrawer.setDropShadowEnabled(false);
                menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                    @Override
                    public void onDrawerStateChange(int oldState, int newState) {
                        if (newState == PLVMenuDrawer.STATE_CLOSED) {
                            menuDrawer.detachToContainer();
                            final View menuView = menuDrawer.getMenuView();
                            if (menuView.getParent() != null) {
                                ((ViewGroup) menuView.getParent()).removeView(menuView);
                            }
                            if (onMenuHideCallback != null) {
                                onMenuHideCallback.accept(menuView);
                            }
                        }
                    }

                    @Override
                    public void onDrawerSlide(float openRatio, int offsetPixels) {

                    }
                });
                menuDrawer.openMenu();
            } else {
                menuDrawer.setMenuView(productLayout);
                menuDrawer.attachToContainer();
                menuDrawer.openMenu();
            }
        }

        public void hide() {
            if (menuDrawer != null) {
                menuDrawer.closeMenu();
            }
        }

        public boolean isShowing() {
            if (menuDrawer == null) {
                return false;
            }
            return menuDrawer.getDrawerState() != PLVMenuDrawer.STATE_CLOSED;
        }

        public void setOnMenuHide(final PLVSugarUtil.Consumer<View> onMenuHideCallback) {
            this.onMenuHideCallback = onMenuHideCallback;
        }

    }

}
