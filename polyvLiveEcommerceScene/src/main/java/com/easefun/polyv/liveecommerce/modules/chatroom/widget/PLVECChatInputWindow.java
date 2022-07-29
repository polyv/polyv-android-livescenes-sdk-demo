package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.ui.window.PLVInputWindow;
import com.easefun.polyv.liveecommerce.R;

public class PLVECChatInputWindow extends PLVInputWindow {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (inputListener instanceof MessageSendListener) {
            ((MessageSendListener) inputListener).onInputContext(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void finish() {
        if (inputListener instanceof MessageSendListener) {
            ((MessageSendListener) inputListener).onInputContext(null);
        }
        super.finish();
    }

    @Override
    public boolean firstShowInput() {
        return true;
    }

    @Override
    public int layoutId() {
        return R.layout.plvec_chat_input_layout;
    }

    @Override
    public int bgViewId() {
        return R.id.chat_input_bg;
    }

    @Override
    public int inputViewId() {
        return R.id.chat_input_et;
    }

    // <editor-fold defaultstate="collapsed" desc="内部类 - 信息发送监听器">
    public interface MessageSendListener extends InputListener {
        void onInputContext(PLVECChatInputWindow inputWindow);
    }
    // </editor-fold>
}
