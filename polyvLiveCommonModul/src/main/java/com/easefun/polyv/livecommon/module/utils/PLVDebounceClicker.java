package com.easefun.polyv.livecommon.module.utils;

import android.util.Pair;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * 点击事件防抖工具，防止快速点击引起表现异常
 *
 * @author suhongtao
 */
public class PLVDebounceClicker {

    // <editor-fold defaultstate="collapsed" desc="变量">

    /**
     * 默认防抖间隔时长
     */
    private static final long DEFAULT_DEBOUNCE_INTERVAL = TimeUnit.MILLISECONDS.toMillis(300);

    /**
     * K: 点击监听器弱引用
     * V: 时间戳，当前时间比该时间戳大时，监听器能再次响应点击事件
     */
    private static final LinkedList<Pair<WeakReference<View.OnClickListener>, Long>> LISTENER_LIST = new LinkedList<>();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 防抖直接调用">

    /**
     * 防抖过滤
     * <p>
     * 在 {@link View.OnClickListener#onClick(View)} 方法回调点击事件时，
     * 主动调用该方法判断是否需要继续触发点击事件
     *
     * @param onClickListener 点击监听器
     * @return {@code true} 继续触发点击事件，{@code false} 不触发点击事件
     */
    public static boolean tryClick(View.OnClickListener onClickListener) {
        return tryClick(onClickListener, DEFAULT_DEBOUNCE_INTERVAL);
    }

    /**
     * 防抖过滤
     * <p>
     * 在 {@link View.OnClickListener#onClick(View)} 方法回调点击事件时，
     * 主动调用该方法判断是否需要继续触发点击事件
     *
     * @param onClickListener    点击监听器
     * @param debounceIntervalMs 防抖间隔时长
     * @return {@code true} 继续触发点击事件，{@code false} 不触发点击事件
     */
    public static boolean tryClick(View.OnClickListener onClickListener, long debounceIntervalMs) {
        removeOutDateItem();
        final Pair<WeakReference<View.OnClickListener>, Long> item = find(onClickListener);
        if (item != null && item.second != null && item.second >= System.currentTimeMillis()) {
            return false;
        }
        putItem(onClickListener, debounceIntervalMs);
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 防抖点击监听包装类">

    /**
     * 防抖点击监听器
     * <p>
     * 使用该监听器进行点击监听设置 {@link View#setOnClickListener(View.OnClickListener)} 时，
     * 所有的点击事件会经过防抖过滤后才进行响应
     */
    public static class OnClickListener implements View.OnClickListener {

        private final View.OnClickListener target;
        private final long debounceMs;

        public OnClickListener(View.OnClickListener target) {
            this(target, DEFAULT_DEBOUNCE_INTERVAL);
        }

        public OnClickListener(View.OnClickListener target, long debounceMs) {
            this.target = target;
            this.debounceMs = debounceMs;
        }

        @Override
        public void onClick(View v) {
            if (tryClick(this, debounceMs)) {
                target.onClick(v);
            }
        }

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部逻辑处理">

    private static void removeOutDateItem() {
        Iterator<Pair<WeakReference<View.OnClickListener>, Long>> iterator = LISTENER_LIST.iterator();
        while (iterator.hasNext()) {
            Pair<WeakReference<View.OnClickListener>, Long> item = iterator.next();
            final View.OnClickListener onClickListener = item.first.get();
            final Long ts = item.second;
            if (onClickListener == null
                    || ts == null
                    || ts < System.currentTimeMillis()) {
                iterator.remove();
            }
        }
    }

    private static Pair<WeakReference<View.OnClickListener>, Long> find(View.OnClickListener onClickListener) {
        for (Pair<WeakReference<View.OnClickListener>, Long> item : LISTENER_LIST) {
            if (item.first.get() == onClickListener) {
                return item;
            }
        }
        return null;
    }

    private static void putItem(View.OnClickListener onClickListener, long debounceIntervalMs) {
        final WeakReference<View.OnClickListener> weakReference = new WeakReference<>(onClickListener);
        final long ts = System.currentTimeMillis() + debounceIntervalMs;
        LISTENER_LIST.addLast(new Pair<>(weakReference, ts));
    }

    // </editor-fold>

}
