package com.easefun.polyv.livecommon.module.modules.beauty.viewmodel;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.beauty.model.PLVBeautyRepo;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase.PLVBeautyOptionListInitUseCase;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase.PLVBeautyResetUseCase;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase.PLVBeautySwitchUseCase;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyOptionsUiState;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.plv.beauty.api.PLVBeautyManager;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVBeautyViewModel implements IPLVLifecycleAwareDependComponent {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVBeautyRepo beautyRepo;

    private final PLVBeautyOptionListInitUseCase optionListInitUseCase;
    private final PLVBeautySwitchUseCase switchUseCase;
    private final PLVBeautyResetUseCase resetUseCase;

    private final MutableLiveData<PLVBeautyUiState> beautyUiStateLiveData = new MutableLiveData<>();
    private final PLVBeautyUiState uiState = new PLVBeautyUiState();
    private final MutableLiveData<PLVBeautyOptionsUiState> beautyOptionsUiStateLiveData = new MutableLiveData<>();
    private final PLVBeautyOptionsUiState optionsUiState = new PLVBeautyOptionsUiState();

    private final CompositeDisposable disposables = new CompositeDisposable();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVBeautyViewModel(
            final PLVBeautyRepo beautyRepo,
            final PLVBeautyOptionListInitUseCase optionListInitUseCase,
            final PLVBeautySwitchUseCase switchUseCase,
            final PLVBeautyResetUseCase resetUseCase
    ) {
        this.beautyRepo = beautyRepo;
        this.optionListInitUseCase = optionListInitUseCase;
        this.switchUseCase = switchUseCase;
        this.resetUseCase = resetUseCase;

        initUiState();
        observeBeautyInit();
        observeBeautySwitch();
        observeLastUsedFilterOption();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initUiState() {
        uiState.isBeautySupport = PLVBeautyManager.getInstance().isBeautySupport();
        uiState.isBeautyModuleInitSuccess = false;
        uiState.isBeautyMenuShowing = false;
        uiState.isBeautyOn = getOrDefault(beautyRepo.getBeautySwitchLiveData().getValue(), true);
        uiState.lastUsedFilterOption = beautyRepo.getLastUsedFilterOption();
        beautyUiStateLiveData.postValue(uiState.copy());

        updateBeautyOptionList();
        switchBeautyOption(uiState.isBeautyOn);
    }

    private void observeBeautyInit() {
        Disposable disposable = beautyRepo.getBeautyInitFinishObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean initSuccess) throws Exception {
                        uiState.isBeautyModuleInitSuccess = initSuccess != null && initSuccess;
                        beautyUiStateLiveData.postValue(uiState.copy());
                    }
                })
                .retry()
                // 美颜底层可能未完全初始化完毕，需要延时等待
                .delay(200, TimeUnit.MILLISECONDS, Schedulers.computation(), false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean initSuccess) throws Exception {
                        // 美颜初始化完成后，更新本地保存的美颜配置
                        if (initSuccess != null && initSuccess) {
                            switchBeautyOption(uiState.isBeautyOn);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });

        disposables.add(disposable);
    }

    private void observeBeautySwitch() {
        beautyRepo.getBeautySwitchLiveData().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean switchOn) {
                uiState.isBeautyOn = switchOn == null || switchOn;
                beautyUiStateLiveData.postValue(uiState.copy());
            }
        });
    }

    private void observeLastUsedFilterOption() {
        beautyRepo.getLastUsedFilterKeyLiveData().observeForever(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                uiState.lastUsedFilterOption = beautyRepo.getLastUsedFilterOption();
                beautyUiStateLiveData.postValue(uiState.copy());
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @Override
    public void onCleared() {
        disposables.dispose();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    /**
     * 请求打开美颜布局
     */
    public void showBeautyMenu() {
        uiState.requestingShowMenu = new Event<>(true);
        beautyUiStateLiveData.postValue(uiState.copy());
    }

    /**
     * 请求关闭美颜布局
     */
    public void hideBeautyMenu() {
        uiState.requestingShowMenu = new Event<>(false);
        beautyUiStateLiveData.postValue(uiState.copy());
    }

    /**
     * 更新美颜布局显示状态
     */
    public void setMenuShowing(boolean showing) {
        uiState.isBeautyMenuShowing = showing;
        beautyUiStateLiveData.postValue(uiState.copy());
    }

    /**
     * 修改美颜选项及其强度，美颜特效和面部细节共用此方法
     * <p>
     * 多个美颜选项会共同作用于推流画面
     */
    public void updateBeautyOption(PLVBeautyOption option, float intensity) {
        if (!uiState.isBeautyOn) {
            return;
        }
        beautyRepo.updateBeautyOption(option, intensity);
    }

    /**
     * 修改滤镜选项及其强度
     * <p>
     * 同时只能选择其中一种滤镜，多次调用传入的 option 不同时，只有最近一次调用的滤镜起作用
     */
    public void updateFilterOption(PLVFilterOption option, float intensity) {
        if (!uiState.isBeautyOn) {
            return;
        }
        beautyRepo.updateFilterOption(option, intensity);
    }

    /**
     * 更新美颜选项列表
     */
    public void updateBeautyOptionList() {
        optionsUiState.beautyOptions = optionListInitUseCase.initBeautyOptionList();
        optionsUiState.filterOptions = optionListInitUseCase.initFilterOptionList();
        optionsUiState.detailOptions = optionListInitUseCase.initDetailOptionList();
        beautyOptionsUiStateLiveData.postValue(optionsUiState.copy());
    }

    /**
     * 切换美颜开关
     *
     * @param switchOn {@code true} 打开美颜，{@code false} 关闭美颜
     */
    public void switchBeautyOption(boolean switchOn) {
        switchUseCase.switchBeauty(switchOn);
    }

    /**
     * 重置所有美颜选项
     */
    public void resetAllOption() {
        resetUseCase.reset();
    }

    public LiveData<PLVBeautyUiState> getUiState() {
        return beautyUiStateLiveData;
    }

    public LiveData<PLVBeautyOptionsUiState> getBeautyOptionsUiState() {
        return beautyOptionsUiStateLiveData;
    }

    // </editor-fold>

}
