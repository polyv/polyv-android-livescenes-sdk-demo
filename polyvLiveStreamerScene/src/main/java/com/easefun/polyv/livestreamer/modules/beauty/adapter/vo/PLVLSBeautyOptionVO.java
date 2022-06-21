package com.easefun.polyv.livestreamer.modules.beauty.adapter.vo;

import androidx.lifecycle.LiveData;

import com.plv.beauty.api.options.IPLVBeautyOption;

/**
 * @author Hoshiiro
 */
public class PLVLSBeautyOptionVO {

    private final IPLVBeautyOption option;
    private LiveData<PLVLSBeautyOptionVO> currentSelectedOptionVOLiveData;
    private LiveData<Boolean> currentEnableStateLiveData;
    private int optionItemIndex;
    private int optionGroupSize;
    private OnSelectedListener onSelectedListener;

    public PLVLSBeautyOptionVO(IPLVBeautyOption option) {
        this.option = option;
    }

    public IPLVBeautyOption getOption() {
        return option;
    }

    public PLVLSBeautyOptionVO setCurrentSelectedOptionVOLiveData(LiveData<PLVLSBeautyOptionVO> currentSelectedOptionVOLiveData) {
        this.currentSelectedOptionVOLiveData = currentSelectedOptionVOLiveData;
        return this;
    }

    public LiveData<PLVLSBeautyOptionVO> getCurrentSelectedOptionVOLiveData() {
        return currentSelectedOptionVOLiveData;
    }

    public PLVLSBeautyOptionVO setCurrentEnableStateLiveData(LiveData<Boolean> currentEnableStateLiveData) {
        this.currentEnableStateLiveData = currentEnableStateLiveData;
        return this;
    }

    public LiveData<Boolean> getCurrentEnableStateLiveData() {
        return currentEnableStateLiveData;
    }

    public PLVLSBeautyOptionVO setOptionItemIndex(int optionItemIndex) {
        this.optionItemIndex = optionItemIndex;
        return this;
    }

    public int getOptionItemIndex() {
        return optionItemIndex;
    }

    public PLVLSBeautyOptionVO setOptionGroupSize(int optionGroupSize) {
        this.optionGroupSize = optionGroupSize;
        return this;
    }

    public int getOptionGroupSize() {
        return optionGroupSize;
    }

    public PLVLSBeautyOptionVO setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
        return this;
    }

    public OnSelectedListener getOnSelectedListener() {
        return onSelectedListener;
    }

    public interface OnSelectedListener {
        void onSelected(PLVLSBeautyOptionVO optionVO);
    }

}
