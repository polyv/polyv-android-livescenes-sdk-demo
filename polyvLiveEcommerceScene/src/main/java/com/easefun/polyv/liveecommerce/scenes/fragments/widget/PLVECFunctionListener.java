package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.view.View;

/**
 * 更多 - 功能回调监听
 */
public interface PLVECFunctionListener {

    /**
     * 功能回调
     *
     * @param type     功能类型
     * @param data     数据
     * @param iconView view
     */
    void onFunctionCallback(String type, String data, View iconView);

}
