package com.easefun.polyv.livecommon.module.modules.beauty.model;

import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.beauty.model.config.PLVBeautyOptionDefaultConfig;
import com.easefun.polyv.livecommon.module.modules.beauty.model.datasource.PLVBeautyLocalDataSource;
import com.easefun.polyv.livecommon.module.modules.beauty.model.datasource.PLVBeautySdkDataSource;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

/**
 * @author Hoshiiro
 */
public class PLVBeautyRepo {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVBeautyLocalDataSource localDataSource;
    private final PLVBeautySdkDataSource beautyModuleDataSource;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVBeautyRepo(
            final PLVBeautyLocalDataSource localDataSource,
            final PLVBeautySdkDataSource beautyModuleDataSource
    ) {
        this.localDataSource = localDataSource;
        this.beautyModuleDataSource = beautyModuleDataSource;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public List<PLVBeautyOption> getBeautyOptionList() {
        final Map<PLVBeautyOption, Float> source;
        final Map<PLVBeautyOption, Float> localBeautyIntensityMap = localDataSource.getBeautyOptionIntensityLiveData().getValue();
        if (localBeautyIntensityMap != null) {
            // 本地有缓存的强度数据，重新设置强度
            for (Map.Entry<PLVBeautyOption, Float> entry : localBeautyIntensityMap.entrySet()) {
                entry.getKey().intensity(entry.getValue());
            }
            source = localBeautyIntensityMap;
        } else {
            // 本地没有缓存，使用默认的数据
            source = createDefaultBeautyOptionIntensityMap();
            // 更新到本地缓存
            localDataSource.updateBeautyOptionIntensityMap(source);
        }
        return new ArrayList<>(source.keySet());
    }

    public List<PLVFilterOption> getFilterOptionList() {
        final List<PLVFilterOption> source = createDefaultFilterOptionList();
        final Map<String, Float> localFilterKeyIntensityMap = localDataSource.getFilterKeyIntensityLiveData().getValue();
        if (localFilterKeyIntensityMap != null) {
            // 本地有缓存的强度数据，重新设置强度
            for (PLVFilterOption filterOption : source) {
                if (localFilterKeyIntensityMap.containsKey(filterOption.getKey())) {
                    filterOption.intensity(localFilterKeyIntensityMap.get(filterOption.getKey()));
                }
            }
        }
        return new ArrayList<>(source);
    }

    @Nullable
    public PLVFilterOption getLastUsedFilterOption() {
        final String lastUsedFilterKey = localDataSource.getLastUsedFilterKeyLiveData().getValue();
        if (lastUsedFilterKey == null) {
            return null;
        }

        final List<PLVFilterOption> filterOptions = getFilterOptionList();
        for (PLVFilterOption filterOption : filterOptions) {
            if (lastUsedFilterKey.equals(filterOption.getKey())) {
                return filterOption;
            }
        }
        return null;
    }

    public Observable<Boolean> getBeautyInitFinishObservable() {
        return beautyModuleDataSource.beautyInitFinishObservable;
    }

    public LiveData<Boolean> getBeautySwitchLiveData() {
        return localDataSource.getBeautySwitchLiveData();
    }

    public LiveData<String> getLastUsedFilterKeyLiveData() {
        return localDataSource.getLastUsedFilterKeyLiveData();
    }

    public void updateBeautyOption(@NonNull PLVBeautyOption beautyOption, float intensity) {
        final Map<PLVBeautyOption, Float> beautyOptions;
        final Map<PLVBeautyOption, Float> localBeautyIntensityMap = localDataSource.getBeautyOptionIntensityLiveData().getValue();
        if (localBeautyIntensityMap != null) {
            beautyOptions = localBeautyIntensityMap;
        } else {
            beautyOptions = createDefaultBeautyOptionIntensityMap();
        }

        beautyOption.intensity(intensity);
        beautyOptions.put(beautyOption, intensity);
        localDataSource.updateBeautyOptionIntensityMap(beautyOptions);
        beautyModuleDataSource.updateBeautyOption(beautyOption);
    }

    public void updateFilterOption(PLVFilterOption filterOption, float intensity) {
        if (filterOption == null) {
            filterOption = PLVFilterOption.getNoEffectOption();
        }

        final Map<String, Float> filterKeyIntensityMap;
        final Map<String, Float> localFilterKeyIntensityMap = localDataSource.getFilterKeyIntensityLiveData().getValue();
        if (localFilterKeyIntensityMap != null) {
            filterKeyIntensityMap = localFilterKeyIntensityMap;
        } else {
            filterKeyIntensityMap = new HashMap<>(16);
        }

        filterOption.intensity(intensity);
        filterKeyIntensityMap.put(filterOption.getKey(), intensity);
        localDataSource.updateFilterKeyIntensityMap(filterKeyIntensityMap);
        localDataSource.updateLastUsedFilterKey(filterOption.getKey());
        beautyModuleDataSource.updateFilterOption(filterOption);
    }

    public void closeBeautyOption() {
        beautyModuleDataSource.clearBeautyOption();
    }

    public void closeFilterOption() {
        beautyModuleDataSource.updateFilterOption(null);
    }

    public void setBeautySwitch(boolean on) {
        localDataSource.updateBeautySwitch(on);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private static Map<PLVBeautyOption, Float> createDefaultBeautyOptionIntensityMap() {
        for (Map.Entry<PLVBeautyOption, Float> entry : PLVBeautyOptionDefaultConfig.DEFAULT_BEAUTY_OPTION_VALUE.entrySet()) {
            entry.getKey().intensity(entry.getValue());
        }
        return new EnumMap<>(PLVBeautyOptionDefaultConfig.DEFAULT_BEAUTY_OPTION_VALUE);
    }

    private List<PLVFilterOption> createDefaultFilterOptionList() {
        final List<PLVFilterOption> filterOptions = beautyModuleDataSource.getSupportFilterOptions();
        for (PLVFilterOption filterOption : filterOptions) {
            filterOption.intensity(PLVBeautyOptionDefaultConfig.DEFAULT_FILTER_VALUE);
        }
        return filterOptions;
    }

    // </editor-fold>

}
