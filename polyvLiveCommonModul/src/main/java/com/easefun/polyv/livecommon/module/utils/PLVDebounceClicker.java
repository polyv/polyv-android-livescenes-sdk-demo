package com.easefun.polyv.livecommon.module.utils;

import android.util.Pair;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * @author suhongtao
 */
public class PLVDebounceClicker {

    private static final long DEFAULT_DEBOUNCE_INTERVAL = TimeUnit.MILLISECONDS.toMillis(300);

    /**
     * K: 点击监听器弱引用
     * V: 时间戳，当前时间比该时间戳大时，监听器能再次响应点击事件
     */
    private static final LinkedList<Pair<WeakReference<View.OnClickListener>, Long>> LISTENER_LIST = new LinkedList<>();

    public static boolean tryClick(View.OnClickListener onClickListener) {
        return tryClick(onClickListener, DEFAULT_DEBOUNCE_INTERVAL);
    }

    public static boolean tryClick(View.OnClickListener onClickListener, long debounceIntervalMs) {
        removeOutDateItem();
        final Pair<WeakReference<View.OnClickListener>, Long> item = find(onClickListener);
        if (item != null && item.second != null && item.second >= System.currentTimeMillis()) {
            return false;
        }
        putItem(onClickListener, debounceIntervalMs);
        return true;
    }

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
        public final void onClick(View v) {
            if (tryClick(this, debounceMs)) {
                target.onClick(v);
            }
        }

    }

}
