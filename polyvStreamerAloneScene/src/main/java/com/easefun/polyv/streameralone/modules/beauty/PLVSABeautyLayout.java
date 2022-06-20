package com.easefun.polyv.streameralone.modules.beauty;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getNullableOrDefault;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;

/**
 * 封装具体美颜设置布局
 *
 * @author Hoshiiro
 */
public class PLVSABeautyLayout implements IPLVSABeautyLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final SparseArray<IPLVSABeautyLayout> layoutImpls = new SparseArray<>(2);

    private final Context context;

    private PLVBeautyViewModel viewModel;
    private PLVBeautyUiState uiState;

    private PLVOrientationManager.OnConfigurationChangedListener orientationChangedListener;
    private int orientation;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVSABeautyLayout(@NonNull Context context) {
        this.context = context;
        this.orientation = PLVScreenUtils.isPortrait(context) ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        initViewModel();
        observeOrientation();
        observeBeautyInitStatusChange();
        observeShowMenu();
    }

    private void initViewModel() {
        viewModel = PLVDependManager.getInstance().get(PLVBeautyViewModel.class);
    }

    /**
     * 监听横竖屏旋转，切换横竖屏时自动切换美颜布局实现
     */
    private void observeOrientation() {
        orientationChangedListener = new PLVOrientationManager.OnConfigurationChangedListener() {
            @Override
            public void onCall(Context context, boolean isLandscape) {
                final int oldOrientation = orientation;
                final int newOrientation = isLandscape ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
                if (oldOrientation == newOrientation) {
                    return;
                }
                PLVSABeautyLayout.this.orientation = newOrientation;

                final boolean menuShowing = getNullableOrDefault(new PLVSugarUtil.Supplier<Boolean>() {
                    @Override
                    public Boolean get() {
                        return viewModel.getUiState().getValue().isBeautyMenuShowing;
                    }
                }, false);

                if (!menuShowing) {
                    return;
                }

                final IPLVSABeautyLayout oldLayout = getBeautyLayoutImpl(oldOrientation);
                if (oldLayout != null) {
                    oldLayout.onHide();
                }
                final IPLVSABeautyLayout newLayout = getBeautyLayoutImpl(newOrientation);
                if (newLayout != null) {
                    newLayout.onShow();
                }
            }
        };
        PLVOrientationManager.getInstance().addOnConfigurationChangedListener(orientationChangedListener);
    }

    /**
     * 监听美颜模块初始化
     */
    private void observeBeautyInitStatusChange() {
        viewModel.getUiState().observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
            private boolean lastInitSuccess = false;

            @Override
            public void onChanged(@Nullable PLVBeautyUiState uiState) {
                if (uiState == null) {
                    return;
                }
                final boolean newInitSuccess = uiState.isBeautyModuleInitSuccess;
                if (lastInitSuccess == newInitSuccess) {
                    return;
                }
                lastInitSuccess = newInitSuccess;
                if (newInitSuccess) {
                    viewModel.updateBeautyOptionList();
                }
            }
        });
    }

    /**
     * 监听显示隐藏美颜布局事件
     */
    private void observeShowMenu() {
        viewModel.getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        PLVSABeautyLayout.this.uiState = beautyUiState;
                        if (beautyUiState == null || beautyUiState.requestingShowMenu == null) {
                            return;
                        }
                        final Boolean requestShowing = beautyUiState.requestingShowMenu.get();
                        if (requestShowing == null) {
                            return;
                        }
                        final boolean newShowMenu = beautyUiState.isBeautySupport && requestShowing;
                        if (beautyUiState.isBeautyMenuShowing == newShowMenu) {
                            return;
                        }
                        if (newShowMenu) {
                            onShow();
                        } else {
                            onHide();
                        }
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口实现">

    @Override
    public void onShow() {
        if (uiState == null || !uiState.isBeautySupport || !uiState.isBeautyModuleInitSuccess) {
            return;
        }
        final IPLVSABeautyLayout layoutImpl = getBeautyLayoutImpl();
        if (layoutImpl != null) {
            layoutImpl.onShow();
        }
    }

    @Override
    public void onHide() {
        final IPLVSABeautyLayout layoutImpl = getBeautyLayoutImpl();
        if (layoutImpl != null) {
            layoutImpl.onHide();
        }
    }

    @Override
    public boolean onBackPressed() {
        final IPLVSABeautyLayout layoutImpl = getBeautyLayoutImpl();
        if (layoutImpl != null) {
            return layoutImpl.onBackPressed();
        }
        return false;
    }

    @Override
    public void destroy() {
        PLVOrientationManager.getInstance().removeOnConfigurationChangedListener(orientationChangedListener);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    private Context getContext() {
        return context;
    }

    @Nullable
    private IPLVSABeautyLayout getBeautyLayoutImpl() {
        return getBeautyLayoutImpl(orientation);
    }

    @Nullable
    private IPLVSABeautyLayout getBeautyLayoutImpl(int orientation) {
        checkInitLayoutImpl(orientation);
        return layoutImpls.get(orientation);
    }

    private void checkInitLayoutImpl(int orientation) {
        if (layoutImpls.get(orientation) != null) {
            return;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutImpls.put(orientation, new PLVSAVerticalBeautyLayout(getContext()));
        } else {
            layoutImpls.put(orientation, new PLVSAHorizonBeautyLayout(getContext()));
        }
    }

    // </editor-fold>

}
