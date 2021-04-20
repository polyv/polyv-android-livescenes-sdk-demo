package com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress;

import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PLVMyProgressManager {
    //moduleTag, urlTag, urlListener
    private static ConcurrentHashMap<String, ConcurrentHashMap<Object, PLVOnProgressListener>> listenersMap = new ConcurrentHashMap<>();
    private static final PLVProgressResponseBody.InternalProgressListener LISTENER =
            new PLVProgressResponseBody.InternalProgressListener() {
                @Override
                public void onProgress(String url, long bytesRead, long totalBytes) {
                    List<PLVOnProgressListener> onProgressListenerList = getProgressListener(url);
                    if (onProgressListenerList != null) {
                        int percentage = (int) ((bytesRead * 1f / totalBytes) * 100f);
                        boolean isComplete = percentage >= 100;
                        if (!isComplete) {//交给glide回调(可以回调，因为多个相同url请求时glide可能只会回调其中一个，但是回调的话图片不会显示)。应该是其中一个url加载失败了(没有重新加载)，然后另外一个(重新加载了)又出现加载进度导致的！可以在url后面拼接参数解决(但是会加载两次/其他下载保存文件问题)。
                            for (PLVOnProgressListener onProgressListener : onProgressListenerList)
                                onProgressListener.onProgress(url, isComplete, percentage, bytesRead, totalBytes);
                        }
                        /***
                         * 暂时保留该代码
                         *if (isComplete) {
                         *    removeListener(url);
                         *}
                         */
                    }
                }
            };
    private static OkHttpClient okHttpClient;

    private PLVMyProgressManager() {
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            final String url = request.url().toString();
                            final List<PLVOnProgressListener> onProgressListenerList = getProgressListener(url);
                            if (onProgressListenerList != null) {
                                PLVProgressResponseBody.mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (PLVOnProgressListener onProgressListener : onProgressListenerList)
                                            onProgressListener.onStart(url);
                                    }
                                });
                            }
                            Response response = chain.proceed(request);
                            return response.newBuilder()
                                    .body(new PLVProgressResponseBody(url, LISTENER,
                                            response.body()))
                                    .build();
                        }
                    })
                    .build();
        }
        return okHttpClient;
    }

    public static boolean isContainsListener(String moduleTag, Object urlTag) {
        Map<Object, PLVOnProgressListener> objectList = getProgressListenerList(moduleTag);
        if (objectList != null) {
            return objectList.containsKey(urlTag);
        }
        return false;
    }

    public static void addListener(String moduleTag, Object urlTag, PLVOnProgressListener listener) {
        if (listener != null && !TextUtils.isEmpty(listener.getUrl())) {
            ConcurrentHashMap<Object, PLVOnProgressListener> objectList = getProgressListenerList(moduleTag);
            if (objectList != null) {
                if (!objectList.containsKey(urlTag)) {
                    objectList.put(urlTag, listener);
                } else {
                    objectList.get(urlTag).transListener(null);
                    objectList.remove(urlTag);
                    objectList.put(urlTag, listener);
                }
            } else {
                objectList = new ConcurrentHashMap<>();
                objectList.put(urlTag, listener);
                listenersMap.put(moduleTag, objectList);
            }
//            listener.onProgress(url, false, 1, 0, 0);//slide list, progress yet, no call
        }
    }

    public static void removeAllListener() {
        listenersMap.clear();
    }

    public static void removeModuleListener(String moduleTag) {
        Map<Object, PLVOnProgressListener> objectList = getProgressListenerList(moduleTag);
        if (objectList != null) {
            for (PLVOnProgressListener listener : objectList.values()) {
                listener.transListener(null);
            }
            objectList.clear();
        }
    }

    public static void removeListener(String moduleTag, Object urlTag) {
        Map<Object, PLVOnProgressListener> objectList = getProgressListenerList(moduleTag);
        if (objectList != null && objectList.containsKey(urlTag)) {
            objectList.get(urlTag).transListener(null);
            objectList.remove(urlTag);
        }
    }

    public static ConcurrentHashMap<Object, PLVOnProgressListener> getProgressListenerList(String moduleTag) {
        if (TextUtils.isEmpty(moduleTag) || listenersMap == null || listenersMap.size() == 0
                || listenersMap.get(moduleTag) == null || listenersMap.get(moduleTag).size() == 0) {
            return null;
        }

        return listenersMap.get(moduleTag);
    }

    public static ConcurrentHashMap<String, ConcurrentHashMap<Object, PLVOnProgressListener>> getListenersMap() {
        return listenersMap;
    }

    public static List<PLVOnProgressListener> getProgressListener(String url) {
        //ConcurrentHashMap， 解决多线程迭代，出现ConcurrentModificationException
        List<PLVOnProgressListener> onProgressListenerList = new ArrayList<>();
        if (listenersMap != null && !TextUtils.isEmpty(url)) {
            for (String moduleTag : listenersMap.keySet()) {
                Map<Object, PLVOnProgressListener> objectList = getProgressListenerList(moduleTag);
                if (objectList != null) {
                    for (PLVOnProgressListener listener : objectList.values()) {
                        if (url.equals(listener.getUrl())) {
                            onProgressListenerList.add(listener);
                        }
                    }
                }
            }
        }
        return (!onProgressListenerList.isEmpty()) ? onProgressListenerList : null;
    }

    public static PLVOnProgressListener getProgressListener(String moduleTag, Object urlTag) {
        Map<Object, PLVOnProgressListener> objectList = getProgressListenerList(moduleTag);
        if (objectList != null && objectList.containsKey(urlTag)) {
            return objectList.get(urlTag).getTransListener();
        }
        return null;
    }
}
