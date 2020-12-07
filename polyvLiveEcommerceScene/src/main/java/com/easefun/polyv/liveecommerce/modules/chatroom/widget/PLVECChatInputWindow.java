package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import com.easefun.polyv.livecommon.ui.window.PLVInputWindow;
import com.easefun.polyv.liveecommerce.R;

public class PLVECChatInputWindow extends PLVInputWindow {

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
}
