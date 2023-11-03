package com.easefun.polyv.livecloudclass.modules.media.danmu;


import com.easefun.polyv.livecloudclass.R;
import com.plv.foundationsdk.utils.PLVAppUtils;

import org.jetbrains.annotations.NotNull;

public enum PLVDanmuSpeedType {
    DANMU_SLOWER("",340,0) {
        @Override
        public String getSpeedType() {
            return PLVAppUtils.getString(R.string.plv_danmu_speed_slower);
        }
    },
    DANMU_SLOW("",270,1) {
        @Override
        public String getSpeedType() {
            return PLVAppUtils.getString(R.string.plv_danmu_speed_slow);
        }
    },
    DANMU_NORMAL("",200,2) {
        @Override
        public String getSpeedType() {
            return PLVAppUtils.getString(R.string.plv_danmu_speed_normal);
        }
    },
    DANMU_QUICK("",140,3) {
        @Override
        public String getSpeedType() {
            return PLVAppUtils.getString(R.string.plv_danmu_speed_quick);
        }
    },
    DANMU_FAST("",60,4) {
        @Override
        public String getSpeedType() {
            return PLVAppUtils.getString(R.string.plv_danmu_speed_fast);
        }
    };

    private final String speedType;
    public final int speed;
    public final int level;
    PLVDanmuSpeedType(
            String speedType,
            int speed,
            int level
    ){
        this.speedType = speedType;
        this.speed = speed;
        this.level = level;
    }

    public String getSpeedType() {
        return speedType;
    }

    @NotNull
    public static PLVDanmuSpeedType matchBySpeed(@NotNull int speed){
        PLVDanmuSpeedType speedType = DANMU_FAST;
        for (PLVDanmuSpeedType value : values()) {
            if(speed <= value.speed){
                speedType = value;
            } else {
                break;
            }
        }
        return speedType;
    }

    @NotNull
    public static PLVDanmuSpeedType matchByLevel(@NotNull int level){
        PLVDanmuSpeedType speedType = DANMU_NORMAL;
        for (PLVDanmuSpeedType value : values()) {
            if(level == value.level){
                return value;
            }
        }
        return speedType;
    }
}
