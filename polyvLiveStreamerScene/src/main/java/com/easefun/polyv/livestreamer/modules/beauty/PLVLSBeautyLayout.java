package com.easefun.polyv.livestreamer.modules.beauty;

import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import com.easefun.polyv.livecommon.module.modules.beauty.model.config.PLVBeautyOptionDefaultConfig;
import com.easefun.polyv.livecommon.module.modules.beauty.view.ui.widget.PLVBeautyItemSelectorTextView;
import com.easefun.polyv.livecommon.module.modules.beauty.view.ui.widget.PLVBeautySeekBar;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyOptionsUiState;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.textview.PLVShadowTextView;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.beauty.adapter.PLVLSBeautyOptionAdapter;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.beauty.api.options.IPLVBeautyOption;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Hoshiiro
 */
public class PLVLSBeautyLayout extends FrameLayout implements IPLVLSBeautyLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVShadowTextView beautyFilterUpdateHintTv;
    private PLVBeautySeekBar beautyIntensityControlBar;
    private PLVShadowTextView beautyIntensityHintTv;
    private PLVBlurView beautyBlurView;
    private RelativeLayout beautyItemLayout;
    private LinearLayout beautyItemSelectorLayout;
    private PLVBeautyItemSelectorTextView beautySelectorBeautyTv;
    private PLVBeautyItemSelectorTextView beautySelectorFilterTv;
    private PLVBeautyItemSelectorTextView beautySelectorDetailTv;
    private LinearLayout beautyResetLayout;
    private LinearLayout beautySwitchLayout;
    private Switch beautySwitch;
    private RecyclerView beautyOptionRv;

    private PLVMenuDrawer menuDrawer;

    private final PLVLSBeautyOptionAdapter beautyOptionAdapter = new PLVLSBeautyOptionAdapter();

    private PLVBeautyViewModel viewModel;
    private PLVBeautyUiState uiState;

    private IPLVBeautyOption currentBeautyOption = null;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSBeautyLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLSBeautyLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLSBeautyLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_beauty_layout, this, true);

        findView();
        initViewModel();
        initRecyclerView();
        initSelector();
        initSwitch();
        initControlBar();
        initReset();

        PLVBlurUtils.initBlurView(beautyBlurView);

        setHideMenuOnTouchOutside();

        observeShowMenu();
        observeBeautyInitStatusChange();
        observeBeautyOptionList();
        observeUiState();
    }

    private void findView() {
        beautyFilterUpdateHintTv = findViewById(R.id.plvls_beauty_filter_update_hint_tv);
        beautyIntensityControlBar = findViewById(R.id.plvls_beauty_intensity_control_bar);
        beautyIntensityHintTv = findViewById(R.id.plvls_beauty_intensity_hint_tv);
        beautyBlurView = findViewById(R.id.plvls_beauty_blur_view);
        beautyItemLayout = findViewById(R.id.plvls_beauty_item_layout);
        beautyItemSelectorLayout = findViewById(R.id.plvls_beauty_item_selector_layout);
        beautySelectorBeautyTv = findViewById(R.id.plvls_beauty_selector_beauty_tv);
        beautySelectorFilterTv = findViewById(R.id.plvls_beauty_selector_filter_tv);
        beautySelectorDetailTv = findViewById(R.id.plvls_beauty_selector_detail_tv);
        beautyResetLayout = findViewById(R.id.plvls_beauty_reset_layout);
        beautySwitchLayout = findViewById(R.id.plvls_beauty_switch_layout);
        beautySwitch = findViewById(R.id.plvls_beauty_switch);
        beautyOptionRv = findViewById(R.id.plvls_beauty_option_rv);
    }

    private void initViewModel() {
        viewModel = PLVDependManager.getInstance().get(PLVBeautyViewModel.class);
    }

    private void initRecyclerView() {
        beautyOptionAdapter.setOnSelectedListener(new PLVLSBeautyOptionAdapter.OnSelectedListener() {
            @Override
            public void onSelected(@Nullable IPLVBeautyOption beautyOption) {
                showFilterOptionUpdateIfNeeded(currentBeautyOption, beautyOption);

                PLVLSBeautyLayout.this.currentBeautyOption = beautyOption;
                if (beautyOption instanceof PLVBeautyOption) {
                    viewModel.updateBeautyOption((PLVBeautyOption) beautyOption, ((PLVBeautyOption) beautyOption).getIntensity());
                }
                if (beautyOption instanceof PLVFilterOption) {
                    viewModel.updateFilterOption((PLVFilterOption) beautyOption, ((PLVFilterOption) beautyOption).getIntensity());
                }
                updateControlBarState();
            }
        });

        beautyOptionRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        beautyOptionRv.setAdapter(beautyOptionAdapter);
    }

    private void initSelector() {
        final Map<PLVBeautyItemSelectorTextView, Integer> selectorItemTypeMap = mapOf(
                pair(beautySelectorBeautyTv, PLVLSBeautyOptionAdapter.ItemType.TYPE_BEAUTY),
                pair(beautySelectorFilterTv, PLVLSBeautyOptionAdapter.ItemType.TYPE_FILTER),
                pair(beautySelectorDetailTv, PLVLSBeautyOptionAdapter.ItemType.TYPE_DETAIL)
        );
        foreach(selectorItemTypeMap.keySet(), new PLVSugarUtil.Consumer<PLVBeautyItemSelectorTextView>() {
            @Override
            public void accept(final PLVBeautyItemSelectorTextView selectedTextView) {
                selectedTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        foreach(selectorItemTypeMap.keySet(), new PLVSugarUtil.Consumer<PLVBeautyItemSelectorTextView>() {
                            @Override
                            public void accept(PLVBeautyItemSelectorTextView textView) {
                                textView.setSelected(textView.equals(selectedTextView));
                            }
                        });
                        beautyOptionAdapter.changeOptionList(selectorItemTypeMap.get(selectedTextView));
                    }
                });
            }
        });

        // 进入时，初始化选中美颜列表
        beautySelectorBeautyTv.callOnClick();
    }

    private void initSwitch() {
        beautySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.switchBeautyOption(isChecked);
            }
        });
    }

    private void initControlBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            beautyIntensityControlBar.setMin(0);
        }
        beautyIntensityControlBar.setMax(100);
        beautyIntensityControlBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currentBeautyOption == null) {
                    return;
                }
                if (currentBeautyOption instanceof PLVBeautyOption) {
                    viewModel.updateBeautyOption((PLVBeautyOption) currentBeautyOption, progress / 100F);
                }
                if (currentBeautyOption instanceof PLVFilterOption) {
                    viewModel.updateFilterOption((PLVFilterOption) currentBeautyOption, progress / 100F);
                }
                updateControlBarHintText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initReset() {
        final PLVConfirmDialog onResetConfirmDialog = PLVLSConfirmDialog.Builder.context(getContext())
                .setTitle(R.string.plvls_beauty_reset_confirm_title)
                .setContent(R.string.plvls_beauty_reset_confirm_content)
                .setLeftButtonText(R.string.plvls_beauty_reset_confirm_cancel)
                .setRightButtonText(R.string.plvls_beauty_reset_confirm_reset)
                .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                        beautyOptionAdapter.onReset();
                        PLVToast.Builder.context(getContext())
                                .setText(getContext().getString(R.string.plvls_beauty_reset_success))
                                .show();
                        viewModel.resetAllOption();
                    }
                })
                .build();

        beautyResetLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetConfirmDialog.show();
            }
        });
    }

    private void setHideMenuOnTouchOutside() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuDrawer != null) {
                    menuDrawer.closeMenu();
                }
            }
        });

        beautyItemLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拦截点击事件
            }
        });
    }

    private void observeShowMenu() {
        viewModel.getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
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

    private void observeBeautyOptionList() {
        viewModel.getBeautyOptionsUiState().observe((LifecycleOwner) getContext(), new Observer<PLVBeautyOptionsUiState>() {
            @Override
            public void onChanged(@Nullable PLVBeautyOptionsUiState beautyOptionsUiState) {
                if (beautyOptionsUiState == null) {
                    return;
                }
                beautyOptionAdapter.setBeautyOptionList(PLVLSBeautyOptionAdapter.ItemType.TYPE_BEAUTY, beautyOptionsUiState.beautyOptions);
                beautyOptionAdapter.setBeautyOptionList(PLVLSBeautyOptionAdapter.ItemType.TYPE_FILTER, beautyOptionsUiState.filterOptions);
                beautyOptionAdapter.setBeautyOptionList(PLVLSBeautyOptionAdapter.ItemType.TYPE_DETAIL, beautyOptionsUiState.detailOptions);
                beautyOptionAdapter.notifyDataSetChanged();
                beautyOptionAdapter.updateCurrentSelectedOption();
            }
        });
    }

    private void observeUiState() {
        viewModel.getUiState().observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
            @Override
            public void onChanged(@Nullable PLVBeautyUiState uiState) {
                if (uiState == null) {
                    return;
                }

                PLVLSBeautyLayout.this.uiState = uiState;
                beautyOptionAdapter.setEnableState(uiState.isBeautySupport && uiState.isBeautyOn);
                beautyOptionAdapter.setLastSelectFilterOption(uiState.lastUsedFilterOption);
                beautySwitch.setEnabled(uiState.isBeautySupport);
                beautySwitch.setChecked(uiState.isBeautySupport && uiState.isBeautyOn);
                beautyResetLayout.setEnabled(uiState.isBeautySupport && uiState.isBeautyOn);
                beautyResetLayout.setAlpha(uiState.isBeautySupport && uiState.isBeautyOn ? 1F : 0.4F);
                updateControlBarState();
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

        viewModel.setMenuShowing(true);

        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setMenuSize(ScreenUtils.getScreenOrientatedHeight());
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    }
                    viewModel.setMenuShowing(newState != PLVMenuDrawer.STATE_CLOSED);
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

    @Override
    public void onHide() {
        viewModel.setMenuShowing(false);

        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            menuDrawer.closeMenu();
            return true;
        }
        return false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void showFilterOptionUpdateIfNeeded(final IPLVBeautyOption oldOption, final IPLVBeautyOption newOption) {
        final boolean needShowFilterUpdate =
                oldOption != newOption
                        && oldOption instanceof PLVFilterOption
                        && newOption instanceof PLVFilterOption;
        if (!needShowFilterUpdate) {
            return;
        }

        final SpannableStringBuilder sb = new PLVSpannableStringBuilder()
                .appendExclude(((PLVFilterOption) newOption).getName(), new AbsoluteSizeSpan(ConvertUtils.sp2px(22)))
                .append("丨")
                .append(getContext().getString(R.string.plvls_beauty_selector_filter_title));
        beautyFilterUpdateHintTv.setText(sb);
        PLVViewUtil.showViewForDuration(beautyFilterUpdateHintTv, TimeUnit.SECONDS.toMillis(3));
    }

    private void updateControlBarState() {
        final boolean isBeautyOn = uiState == null || uiState.isBeautyOn;
        final boolean isBeautyOption = currentBeautyOption instanceof PLVBeautyOption;
        final boolean isAdjustableFilterOption = currentBeautyOption instanceof PLVFilterOption && ((PLVFilterOption) currentBeautyOption).canAdjustIntensity();
        final boolean showControlBar = isBeautyOn && (isBeautyOption || isAdjustableFilterOption);
        beautyIntensityControlBar.setVisibility(showControlBar ? VISIBLE : GONE);
        beautyIntensityHintTv.setVisibility(showControlBar ? VISIBLE : GONE);

        final float intensity = getBeautyOptionIntensity(currentBeautyOption);
        final float indicateProgress = getBeautyOptionIndicateProgress(currentBeautyOption);

        beautyIntensityControlBar.setProgress((int) (intensity * 100));
        beautyIntensityControlBar.setIndicatorProgress((int) (indicateProgress * 100));
        beautyIntensityHintTv.post(new Runnable() {
            @Override
            public void run() {
                updateControlBarHintText();
            }
        });
    }

    private void updateControlBarHintText() {
        final float intensity = getBeautyOptionIntensity(currentBeautyOption);
        beautyIntensityHintTv.setText(String.valueOf((int) (intensity * 100)));
        beautyIntensityHintTv.setTranslationX(calculateHintTvOffsetX(intensity));
    }

    private int calculateHintTvOffsetX(final float intensity) {
        final int barWidth = beautyIntensityControlBar.getWidth() - beautyIntensityControlBar.getPaddingLeft() - beautyIntensityControlBar.getPaddingRight();
        return (int) ((intensity - 0.5F) * barWidth);
    }

    private static float getBeautyOptionIntensity(IPLVBeautyOption beautyOption) {
        if (beautyOption instanceof PLVBeautyOption) {
            return ((PLVBeautyOption) beautyOption).getIntensity();
        } else if (beautyOption instanceof PLVFilterOption) {
            return ((PLVFilterOption) beautyOption).getIntensity();
        }
        return 0F;
    }

    private static float getBeautyOptionIndicateProgress(IPLVBeautyOption beautyOption) {
        // 默认值返回-1，表示不显示指示器
        if (beautyOption instanceof PLVBeautyOption) {
            return getOrDefault(PLVBeautyOptionDefaultConfig.DEFAULT_BEAUTY_OPTION_VALUE.get(beautyOption), -1F);
        } else if (beautyOption instanceof PLVFilterOption) {
            return PLVBeautyOptionDefaultConfig.DEFAULT_FILTER_VALUE;
        }
        return -1F;
    }

    // </editor-fold>

}
