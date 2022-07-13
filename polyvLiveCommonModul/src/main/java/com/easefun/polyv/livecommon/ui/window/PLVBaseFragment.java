package com.easefun.polyv.livecommon.ui.window;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 基础fragment
 */
public abstract class PLVBaseFragment extends Fragment {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    protected View view;
    protected Handler handler = new Handler(Looper.getMainLooper());

    private boolean afterOnActivityCreated = false;
    private final Queue<Runnable> pendingTaskOnActivityCreated = new LinkedList<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View相关">
    protected final <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment方法">

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler.post(new Runnable() {
            @Override
            public void run() {
                afterOnActivityCreated = true;
                while (!pendingTaskOnActivityCreated.isEmpty()) {
                    pendingTaskOnActivityCreated.poll().run();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // </editor-fold>

    protected void runAfterOnActivityCreated(@NonNull final Runnable runnable) {
        if (afterOnActivityCreated) {
            runnable.run();
        } else {
            pendingTaskOnActivityCreated.add(runnable);
        }
    }
}
