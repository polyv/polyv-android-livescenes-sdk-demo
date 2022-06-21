package com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo;

import androidx.annotation.Nullable;

import com.plv.beauty.api.options.PLVFilterOption;
import com.plv.foundationsdk.component.livedata.Event;

/**
 * @author Hoshiiro
 */
public class PLVBeautyUiState {

    public boolean isBeautySupport;
    public boolean isBeautyModuleInitSuccess;
    public boolean isBeautyMenuShowing;
    public boolean isBeautyOn;
    public Event<Boolean> requestingShowMenu = null;
    @Nullable
    public PLVFilterOption lastUsedFilterOption;

    public PLVBeautyUiState copy() {
        final PLVBeautyUiState state = new PLVBeautyUiState();
        state.isBeautySupport = isBeautySupport;
        state.isBeautyModuleInitSuccess = isBeautyModuleInitSuccess;
        state.isBeautyMenuShowing = isBeautyMenuShowing;
        state.isBeautyOn = isBeautyOn;
        state.requestingShowMenu = requestingShowMenu;
        state.lastUsedFilterOption = lastUsedFilterOption;
        return state;
    }

}
