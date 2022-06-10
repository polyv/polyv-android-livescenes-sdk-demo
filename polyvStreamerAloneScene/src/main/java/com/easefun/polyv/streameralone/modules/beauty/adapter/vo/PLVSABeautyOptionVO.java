package com.easefun.polyv.streameralone.modules.beauty.adapter.vo;

import android.arch.lifecycle.LiveData;

import com.plv.beauty.api.options.IPLVBeautyOption;

/**
 * @author Hoshiiro
 */
public class PLVSABeautyOptionVO {

    private final IPLVBeautyOption option;
    private LiveData<PLVSABeautyOptionVO> currentSelectedOptionVOLiveData;
    private LiveData<Boolean> currentEnableStateLiveData;
    private int optionItemIndex;
    private int optionGroupSize;
    private OnSelectedListener onSelectedListener;

    public PLVSABeautyOptionVO(IPLVBeautyOption option) {
        this.option = option;
    }

    public IPLVBeautyOption getOption() {
        return option;
    }

    public PLVSABeautyOptionVO setCurrentSelectedOptionVOLiveData(LiveData<PLVSABeautyOptionVO> currentSelectedOptionVOLiveData) {
        this.currentSelectedOptionVOLiveData = currentSelectedOptionVOLiveData;
        return this;
    }

    public LiveData<PLVSABeautyOptionVO> getCurrentSelectedOptionVOLiveData() {
        return currentSelectedOptionVOLiveData;
    }

    public PLVSABeautyOptionVO setCurrentEnableStateLiveData(LiveData<Boolean> currentEnableStateLiveData) {
        this.currentEnableStateLiveData = currentEnableStateLiveData;
        return this;
    }

    public LiveData<Boolean> getCurrentEnableStateLiveData() {
        return currentEnableStateLiveData;
    }

    public PLVSABeautyOptionVO setOptionItemIndex(int optionItemIndex) {
        this.optionItemIndex = optionItemIndex;
        return this;
    }

    public int getOptionItemIndex() {
        return optionItemIndex;
    }

    public PLVSABeautyOptionVO setOptionGroupSize(int optionGroupSize) {
        this.optionGroupSize = optionGroupSize;
        return this;
    }

    public int getOptionGroupSize() {
        return optionGroupSize;
    }

    public PLVSABeautyOptionVO setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
        return this;
    }

    public OnSelectedListener getOnSelectedListener() {
        return onSelectedListener;
    }

    public interface OnSelectedListener {
        void onSelected(PLVSABeautyOptionVO optionVO);
    }

}
