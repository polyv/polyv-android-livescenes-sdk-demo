package com.easefun.polyv.streameralone.modules.beauty;

import android.app.Activity;

import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;

/**
 * @author Hoshiiro
 */
public interface IPLVSABeautyLayout {

    /**
     * 回调接口，请勿直接调用
     * <p>
     * 如果需要调起美颜布局，调用 {@link PLVBeautyViewModel#showBeautyMenu()}
     */
    void onShow();

    /**
     * 回调接口，请勿直接调用
     */
    void onHide();

    /**
     * 重写返回键逻辑
     *
     * @see Activity#onBackPressed()
     */
    boolean onBackPressed();

    /**
     * 销毁方法
     */
    void destroy();

}
