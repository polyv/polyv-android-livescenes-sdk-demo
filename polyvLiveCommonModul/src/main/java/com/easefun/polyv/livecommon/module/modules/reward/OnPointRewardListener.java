package com.easefun.polyv.livecommon.module.modules.reward;

public interface OnPointRewardListener {

    /**
     * 是否打开积分打赏
     * 当积分打赏设置和频道积分打赏设置都打开时，积分打赏功能才算打开
     * @param enable
     */
    void pointRewardEnable(boolean enable);
}
