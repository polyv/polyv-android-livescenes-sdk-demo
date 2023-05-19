package com.easefun.polyv.livecloudclass.modules.media.danmu;


import org.jetbrains.annotations.NotNull;

public enum PLVDanmuSpeedType {
    DANMU_SLOWER("缓慢",340,0),
    DANMU_SLOW("较慢",270,1),
    DANMU_NORMAL("普通",200,2),
    DANMU_QUICK("较快",140,3),
    DANMU_FAST("快速",60,4);

    public final String speedType;
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
