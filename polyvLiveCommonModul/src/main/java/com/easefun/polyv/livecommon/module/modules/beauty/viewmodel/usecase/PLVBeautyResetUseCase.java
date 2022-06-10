package com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase;

import com.easefun.polyv.livecommon.module.modules.beauty.model.PLVBeautyRepo;
import com.easefun.polyv.livecommon.module.modules.beauty.model.config.PLVBeautyOptionDefaultConfig;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;

/**
 * @author Hoshiiro
 */
public class PLVBeautyResetUseCase {

    private final PLVBeautyRepo beautyRepo;

    public PLVBeautyResetUseCase(PLVBeautyRepo beautyRepo) {
        this.beautyRepo = beautyRepo;
    }

    public void reset() {
        for (PLVBeautyOption beautyOption : beautyRepo.getBeautyOptionList()) {
            beautyRepo.updateBeautyOption(beautyOption, PLVBeautyOptionDefaultConfig.DEFAULT_BEAUTY_OPTION_VALUE.get(beautyOption));
        }
        for (PLVFilterOption filterOption : beautyRepo.getFilterOptionList()) {
            beautyRepo.updateFilterOption(filterOption, PLVBeautyOptionDefaultConfig.DEFAULT_FILTER_VALUE);
        }
        beautyRepo.updateFilterOption(PLVFilterOption.getNoEffectOption(), PLVBeautyOptionDefaultConfig.DEFAULT_FILTER_VALUE);
    }

}
