package com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase;

import com.easefun.polyv.livecommon.module.modules.beauty.model.PLVBeautyRepo;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;

/**
 * @author Hoshiiro
 */
public class PLVBeautySwitchUseCase {

    private final PLVBeautyRepo beautyRepo;

    public PLVBeautySwitchUseCase(PLVBeautyRepo beautyRepo) {
        this.beautyRepo = beautyRepo;
    }

    public void switchBeauty(boolean switchOn) {
        if (switchOn) {
            beautySwitchOn();
        } else {
            beautySwitchOff();
        }
    }

    private void beautySwitchOn() {
        beautyRepo.setBeautySwitch(true);

        for (PLVBeautyOption beautyOption : beautyRepo.getBeautyOptionList()) {
            beautyRepo.updateBeautyOption(beautyOption, beautyOption.getIntensity());
        }

        final PLVFilterOption filterOption = beautyRepo.getLastUsedFilterOption();
        if (filterOption != null) {
            beautyRepo.updateFilterOption(filterOption, filterOption.getIntensity());
        }
    }

    private void beautySwitchOff() {
        beautyRepo.setBeautySwitch(false);
        beautyRepo.closeBeautyOption();
        beautyRepo.closeFilterOption();
    }

}
