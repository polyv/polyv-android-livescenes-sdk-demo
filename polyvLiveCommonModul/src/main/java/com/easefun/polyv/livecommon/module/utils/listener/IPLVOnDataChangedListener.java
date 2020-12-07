package com.easefun.polyv.livecommon.module.utils.listener;

import android.arch.lifecycle.Observer;

/**
 * 数据改变监听器
 *
 * @param <T> 数据
 */
public interface IPLVOnDataChangedListener<T> extends Observer<T> {
}