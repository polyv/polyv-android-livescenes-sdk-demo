package com.easefun.polyv.livecommon.module.utils.network;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;

import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import java.util.ArrayList;
import java.util.List;

import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;

/**
 * @author Hoshiiro
 */
public class PLVNetworkObserver {

    private final List<NetworkCallback> networkCallbacks = new ArrayList<>();

    @Nullable
    private Context context;
    private boolean started = false;

    public void start(final Context context, final LifecycleOwner lifecycleOwner) {
        if (started) {
            return;
        }

        started = true;
        this.context = context;
        startObserveNetwork();
        autoStopByLifecycle(lifecycleOwner);
    }

    public void stop() {
        if (!started) {
            return;
        }

        networkCallbacks.clear();
        stopObserveNetwork();
        started = false;
        context = null;
    }

    public void addNetworkCallback(NetworkCallback callback) {
        if (callback == null) {
            return;
        }
        networkCallbacks.add(callback);
    }

    public void removeNetworkCallback(NetworkCallback callback) {
        networkCallbacks.remove(callback);
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                return;
            }
            if (PLVNetworkUtils.isConnected(context)) {
                notifyNetworkConnected();
            } else {
                notifyNetworkDisconnected();
            }
        }
    };

    private void startObserveNetwork() {
        if (context == null) {
            return;
        }
        context.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void stopObserveNetwork() {
        if (context == null) {
            return;
        }
        context.unregisterReceiver(networkReceiver);
    }

    private void autoStopByLifecycle(final LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(new GenericLifecycleObserver() {
            @Override
            public void onStateChanged(LifecycleOwner owner, Lifecycle.Event event) {
                if (lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                    stop();
                    lifecycleOwner.getLifecycle().removeObserver(this);
                }
            }
        });
    }

    private void notifyNetworkConnected() {
        foreach(networkCallbacks, new PLVSugarUtil.Consumer<NetworkCallback>() {
            @Override
            public void accept(NetworkCallback networkCallback) {
                if (networkCallback != null) {
                    networkCallback.onNetworkConnected();
                }
            }
        });
    }

    private void notifyNetworkDisconnected() {
        foreach(networkCallbacks, new PLVSugarUtil.Consumer<NetworkCallback>() {
            @Override
            public void accept(NetworkCallback networkCallback) {
                if (networkCallback != null) {
                    networkCallback.onNetworkDisconnected();
                }
            }
        });
    }

    public interface NetworkCallback {
        void onNetworkConnected();

        void onNetworkDisconnected();
    }

}
