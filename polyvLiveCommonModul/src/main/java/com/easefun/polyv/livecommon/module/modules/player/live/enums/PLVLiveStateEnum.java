package com.easefun.polyv.livecommon.module.modules.player.live.enums;

import static com.plv.foundationsdk.utils.PLVAppUtils.getString;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.R;

import java.util.List;

/**
 * @author Hoshiiro
 */
public enum PLVLiveStateEnum {

    UNSTART("unStart", getString(R.string.plv_live_state_un_start)),

    LIVE("live", getString(R.string.plv_live_state_live)) {
        @Override
        protected List<PLVLiveStateEnum> getSpecNextStates() {
            return listOf(STOP, END, WAITING, PLAYBACK, PLAYBACK_CACHED);
        }
    },

    STOP("stop", getString(R.string.plv_live_state_stop)) {
        @Override
        protected List<PLVLiveStateEnum> getSpecNextStates() {
            return listOf(LIVE, END, WAITING, PLAYBACK, PLAYBACK_CACHED);
        }
    },

    END("end", getString(R.string.plv_live_state_end)) {
        @Override
        protected List<PLVLiveStateEnum> getSpecNextStates() {
            return listOf(LIVE, WAITING, PLAYBACK, PLAYBACK_CACHED);
        }
    },

    WAITING("waiting", getString(R.string.plv_live_state_waiting)) {
        @Override
        protected List<PLVLiveStateEnum> getSpecNextStates() {
            return listOf(LIVE, END, PLAYBACK, PLAYBACK_CACHED);
        }
    },

    PLAYBACK("playback", getString(R.string.plv_live_state_playback)) {
        @Override
        protected List<PLVLiveStateEnum> getSpecNextStates() {
            return listOf(LIVE, PLAYBACK_CACHED);
        }
    },

    PLAYBACK_CACHED("playback_cached", getString(R.string.plv_live_state_playback_cached)),

    ;

    private final String status;
    private final String description;

    PLVLiveStateEnum(String status, String description) {
        this.status = status;
        this.description = description;
    }

    /**
     * 指定能够跳转到的下个状态
     *
     * @return null时允许跳转到任意状态，非空时只允许跳转到列表内的状态
     * @see #toState(PLVLiveStateEnum)
     */
    protected List<PLVLiveStateEnum> getSpecNextStates() {
        return null;
    }

    public PLVLiveStateEnum toState(PLVLiveStateEnum preferNextState) {
        final List<PLVLiveStateEnum> specNextStates = getSpecNextStates();
        if (specNextStates == null) {
            return preferNextState == null ? this : preferNextState;
        }
        if (specNextStates.contains(preferNextState)) {
            return preferNextState;
        }
        return this;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static PLVLiveStateEnum parse(@NonNull String status) {
        for (PLVLiveStateEnum liveStateEnum : values()) {
            if (liveStateEnum.getStatus().equals(status)) {
                return liveStateEnum;
            }
        }
        return WAITING;
    }
}
