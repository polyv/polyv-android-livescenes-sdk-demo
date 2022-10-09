package com.easefun.polyv.livestreamer.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 设置布局
 */
public class PLVLSMoreSettingLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVBlurView blurLy;
    private ConstraintLayout moreSettingSelectLayout;
    private TextView moreSettingTitleTv;
    private View moreSettingTitleSeparator;
    private LinearLayout moreSettingBeautyItemLayout;
    private LinearLayout moreSettingBitrateItemLayout;
    private LinearLayout moreSettingShareItemLayout;
    private PLVLSBitrateLayout moreSettingBitrateLayout;
    private View moreSettingExitSeparator;
    private TextView moreSettingExitTv;

    // 分享布局
    private PLVLSShareLayout shareLayout;

    // 布局弹层
    private PLVMenuDrawer menuDrawer;
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;

    private OnViewActionListener onViewActionListener;

    private Disposable updateBlurViewDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSMoreSettingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSMoreSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSMoreSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_more_setting_layout, this);

        findView();

        initBitrateLayout();
        initShareLayout();
        observeBeautyModuleInitResult();

        PLVBlurUtils.initBlurView(blurLy);
    }

    private void findView() {
        blurLy = findViewById(R.id.blur_ly);
        moreSettingSelectLayout = findViewById(R.id.plvls_more_setting_select_layout);
        moreSettingTitleTv = findViewById(R.id.plvls_more_setting_title_tv);
        moreSettingTitleSeparator = findViewById(R.id.plvls_more_setting_title_separator);
        moreSettingBeautyItemLayout = findViewById(R.id.plvls_more_setting_beauty_item_layout);
        moreSettingBitrateItemLayout = findViewById(R.id.plvls_more_setting_bitrate_item_layout);
        moreSettingBitrateLayout = findViewById(R.id.plvls_more_setting_bitrate_layout);
        moreSettingShareItemLayout = findViewById(R.id.plvls_more_setting_share_item_layout);
        moreSettingExitSeparator = findViewById(R.id.plvls_more_setting_exit_separator);
        moreSettingExitTv = findViewById(R.id.plvls_more_setting_exit_tv);

        moreSettingExitTv.setOnClickListener(this);
        moreSettingBeautyItemLayout.setOnClickListener(this);
        moreSettingBitrateItemLayout.setOnClickListener(this);
        moreSettingShareItemLayout.setOnClickListener(this);
    }

    private void initBitrateLayout() {
        moreSettingBitrateLayout.setOnViewActionListener(new PLVLSBitrateLayout.OnViewActionListener() {
            @Override
            public void onBitrateClick(int bitrate) {
                if (PLVLSMoreSettingLayout.this.onViewActionListener != null) {
                    PLVLSMoreSettingLayout.this.onViewActionListener.onBitrateClick(bitrate);
                }
            }
        });
    }

    private void initShareLayout() {
        shareLayout = new PLVLSShareLayout(getContext());
    }

    private void observeBeautyModuleInitResult() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    private Boolean lastShowBeautyLayout = null;

                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        if (beautyUiState == null) {
                            return;
                        }
                        final boolean isBeautySupport = beautyUiState.isBeautySupport;
                        final boolean isInitSuccess = beautyUiState.isBeautyModuleInitSuccess;
                        final boolean showBeautyLayout = isBeautySupport && isInitSuccess;
                        if (lastShowBeautyLayout != null && lastShowBeautyLayout == showBeautyLayout) {
                            return;
                        }
                        lastShowBeautyLayout = showBeautyLayout;

                        moreSettingBeautyItemLayout.setVisibility(showBeautyLayout ? View.VISIBLE : View.GONE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            final GridLayout.LayoutParams lp = (GridLayout.LayoutParams) moreSettingBeautyItemLayout.getLayoutParams();
                            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, showBeautyLayout ? 1 : 0, 1F);
                            moreSettingBeautyItemLayout.setLayoutParams(lp);
                        }
                    }
                });
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (shareLayout != null) {
            shareLayout.init(liveRoomDataManager);
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="布局控制">
    public void open() {
        if (onViewActionListener != null && onViewActionListener.getBitrateInfo() != null) {
            moreSettingBitrateLayout.updateData(onViewActionListener.getBitrateInfo().first, onViewActionListener.getBitrateInfo().second);
        }
        showLayout(moreSettingSelectLayout);

        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setMenuSize((int) (landscapeWidth * 0.44));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public boolean onBackPressed() {
        if (shareLayout != null && shareLayout.onBackPressed()) {
            return true;
        }
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        close();
        stopUpdateBlurViewTimer();
        if (shareLayout != null) {
            shareLayout.destroy();
        }
    }
    // </editor-fold>

    private void showLayout(final View viewToShow) {
        final List<View> views = PLVSugarUtil.<View>listOf(moreSettingSelectLayout, moreSettingBitrateLayout);
        foreach(views, new PLVSugarUtil.Consumer<View>() {
            @Override
            public void accept(View view) {
                view.setVisibility(view == viewToShow ? VISIBLE : GONE);
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="定时更新模糊背景view">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        blurLy.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == moreSettingExitTv.getId()) {
            close();
            ((Activity) getContext()).onBackPressed();
        } else if (id == moreSettingBeautyItemLayout.getId()) {
            close();
            final boolean isLocalVideoEnable = onViewActionListener != null && onViewActionListener.isCurrentLocalVideoEnable();
            if (!isLocalVideoEnable) {
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plvls_beauty_need_open_camera))
                        .show();
                return;
            }
            PLVDependManager.getInstance().get(PLVBeautyViewModel.class).showBeautyMenu();
        } else if (id == moreSettingBitrateItemLayout.getId()) {
            showLayout(moreSettingBitrateLayout);
        } else if (id == moreSettingShareItemLayout.getId()) {
            close();
            shareLayout.open();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        Pair<Integer, Integer> getBitrateInfo();

        void onBitrateClick(int bitrate);

        boolean isCurrentLocalVideoEnable();
    }
    // </editor-fold>
}
