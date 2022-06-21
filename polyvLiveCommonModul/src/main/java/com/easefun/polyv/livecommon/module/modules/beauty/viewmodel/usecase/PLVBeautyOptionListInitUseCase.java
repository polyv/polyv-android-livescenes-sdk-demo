package com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase;

import com.easefun.polyv.livecommon.module.modules.beauty.model.PLVBeautyRepo;
import com.easefun.polyv.livecommon.module.modules.beauty.model.config.PLVBeautyEnums;
import com.easefun.polyv.livecommon.module.modules.beauty.model.config.PLVBeautyOptionDefaultConfig;
import com.plv.beauty.api.PLVBeautyManager;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVBeautyOptionListInitUseCase {

    private final PLVBeautyRepo beautyRepo;

    public PLVBeautyOptionListInitUseCase(PLVBeautyRepo beautyRepo) {
        this.beautyRepo = beautyRepo;
    }

    public List<PLVBeautyOption> initBeautyOptionList() {
        final List<PLVBeautyOption> repoBeautyOptionList = beautyRepo.getBeautyOptionList();
        final List<PLVBeautyOption> result = new ArrayList<>(PLVBeautyEnums.BeautyOption.values().length);
        for (PLVBeautyEnums.BeautyOption beautyOptionEnum : PLVBeautyEnums.BeautyOption.values()) {
            if (repoBeautyOptionList.contains(beautyOptionEnum.beautyOption)
                    && PLVBeautyManager.getInstance().isBeautyOptionSupport(beautyOptionEnum.beautyOption)) {
                result.add(beautyOptionEnum.beautyOption);
            }
        }
        return result;
    }

    public List<PLVFilterOption> initFilterOptionList() {
        final List<PLVFilterOption> repoFilterOptionList = beautyRepo.getFilterOptionList();
        Collections.sort(repoFilterOptionList, new Comparator<PLVFilterOption>() {
            @Override
            public int compare(PLVFilterOption o1, PLVFilterOption o2) {
                final int k1 = PLVBeautyOptionDefaultConfig.DEFAULT_FILTER_KEY_ORDER.indexOf(o1.getName());
                final int k2 = PLVBeautyOptionDefaultConfig.DEFAULT_FILTER_KEY_ORDER.indexOf(o2.getName());
                final int key1 = k1 < 0 ? Integer.MAX_VALUE : k1;
                final int key2 = k2 < 0 ? Integer.MAX_VALUE : k2;
                return key1 == key2 ? 0 : key1 < key2 ? -1 : 1;
            }
        });
        return repoFilterOptionList;
    }

    public List<PLVBeautyOption> initDetailOptionList() {
        final List<PLVBeautyOption> repoBeautyOptionList = beautyRepo.getBeautyOptionList();
        final List<PLVBeautyOption> result = new ArrayList<>(PLVBeautyEnums.DetailOption.values().length);
        for (PLVBeautyEnums.DetailOption detailOptionEnum : PLVBeautyEnums.DetailOption.values()) {
            if (repoBeautyOptionList.contains(detailOptionEnum.beautyOption)
                    && PLVBeautyManager.getInstance().isBeautyOptionSupport(detailOptionEnum.beautyOption)) {
                result.add(detailOptionEnum.beautyOption);
            }
        }
        return result;
    }

}
