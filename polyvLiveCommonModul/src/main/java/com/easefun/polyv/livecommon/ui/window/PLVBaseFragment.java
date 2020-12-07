package com.easefun.polyv.livecommon.ui.window;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * 基础fragment
 */
public abstract class PLVBaseFragment extends Fragment {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    protected View view;
    protected Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View相关">
    protected final <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment方法">
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
    // </editor-fold>
}
