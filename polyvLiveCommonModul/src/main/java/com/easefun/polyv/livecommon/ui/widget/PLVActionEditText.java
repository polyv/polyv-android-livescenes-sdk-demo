package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * @author suhongtao
 */
public class PLVActionEditText extends AppCompatEditText {
    public PLVActionEditText(Context context) {
        super(context);
    }

    public PLVActionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVActionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        int imeActions = outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION;
        if ((imeActions & EditorInfo.IME_ACTION_DONE) != 0) {
            // clear the existing action
            outAttrs.imeOptions ^= imeActions;
            // set the DONE action
            outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
        }
        if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }
        return connection;
    }
}
