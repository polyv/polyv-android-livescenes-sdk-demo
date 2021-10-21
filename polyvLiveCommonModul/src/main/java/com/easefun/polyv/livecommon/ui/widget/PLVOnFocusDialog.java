package com.easefun.polyv.livecommon.ui.widget;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 能获取到windowFocus状态的dialog
 */
public class PLVOnFocusDialog extends Dialog {
    private OnWindowFocusChangeListener onWindowFocusChangeListener;

    public PLVOnFocusDialog(@NonNull Context context) {
        super(context);
    }

    public PLVOnFocusDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PLVOnFocusDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (onWindowFocusChangeListener != null) {
            onWindowFocusChangeListener.onWindowFocusChanged(hasFocus);
        }
    }

    public void setOnWindowFocusChangedListener(OnWindowFocusChangeListener listener) {
        onWindowFocusChangeListener = listener;
    }

    public interface OnWindowFocusChangeListener {
        void onWindowFocusChanged(boolean hasFocus);
    }
}
