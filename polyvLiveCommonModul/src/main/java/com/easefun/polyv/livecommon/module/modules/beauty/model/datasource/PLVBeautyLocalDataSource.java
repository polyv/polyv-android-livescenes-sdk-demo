package com.easefun.polyv.livecommon.module.modules.beauty.model.datasource;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.foundationsdk.component.livedata.PLVAutoSaveLiveData;

import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVBeautyLocalDataSource {

    private final MutableLiveData<Map<PLVBeautyOption, Float>> beautyOptionIntensityLiveData = new PLVAutoSaveLiveData<Map<PLVBeautyOption, Float>>("plv_beauty_option_intensity") {};
    private final MutableLiveData<Map<String, Float>> filterKeyIntensityLiveData = new PLVAutoSaveLiveData<Map<String, Float>>("plv_filter_option_intensity") {};
    private final MutableLiveData<String> lastUsedFilterKeyLiveData = new PLVAutoSaveLiveData<String>("plv_last_used_filter_key") {};
    private final MutableLiveData<Boolean> beautySwitchLiveData = new PLVAutoSaveLiveData<Boolean>("plv_beauty_switch") {};

    public LiveData<Map<PLVBeautyOption, Float>> getBeautyOptionIntensityLiveData() {
        return beautyOptionIntensityLiveData;
    }

    public void updateBeautyOptionIntensityMap(Map<PLVBeautyOption, Float> beautyOptionIntensityMap) {
        beautyOptionIntensityLiveData.postValue(beautyOptionIntensityMap);
    }

    public LiveData<Map<String, Float>> getFilterKeyIntensityLiveData() {
        return filterKeyIntensityLiveData;
    }

    public void updateFilterKeyIntensityMap(Map<String, Float> filterKeyIntensityMap) {
        filterKeyIntensityLiveData.postValue(filterKeyIntensityMap);
    }

    public LiveData<String> getLastUsedFilterKeyLiveData() {
        return lastUsedFilterKeyLiveData;
    }

    public void updateLastUsedFilterKey(String filterKey) {
        lastUsedFilterKeyLiveData.postValue(filterKey);
    }

    public LiveData<Boolean> getBeautySwitchLiveData() {
        return beautySwitchLiveData;
    }

    public void updateBeautySwitch(boolean beautySwitch) {
        beautySwitchLiveData.postValue(beautySwitch);
    }
}
