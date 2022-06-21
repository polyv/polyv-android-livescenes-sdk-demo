package com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo;

import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVBeautyOptionsUiState {

    public List<PLVBeautyOption> beautyOptions;
    public List<PLVFilterOption> filterOptions;
    public List<PLVBeautyOption> detailOptions;

    public PLVBeautyOptionsUiState copy() {
        final PLVBeautyOptionsUiState state = new PLVBeautyOptionsUiState();
        state.beautyOptions = beautyOptions;
        state.filterOptions = filterOptions;
        state.detailOptions = detailOptions;
        return state;
    }

}
