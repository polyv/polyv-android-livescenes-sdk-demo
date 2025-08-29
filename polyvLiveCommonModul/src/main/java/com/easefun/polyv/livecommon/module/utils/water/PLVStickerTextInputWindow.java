package com.easefun.polyv.livecommon.module.utils.water;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.window.PLVInputWindow;

public class PLVStickerTextInputWindow extends PLVInputWindow {
    public static final int MAX_TEXT_LENGTH = 8;
    private TextView stickerInputTv;
    private View sendView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        if (inputListener instanceof InputWindowListener) {
            ((InputWindowListener) inputListener).onInputContext(this);
        }
    }

    @Override
    public void finish() {
        if (inputListener instanceof InputWindowListener) {
            ((InputWindowListener) inputListener).onInputContext(null);
        }
        super.finish();
        lastInputText = null;
    }

    private void initView() {
        stickerInputTv = findViewById(R.id.sticker_input_tv);
        inputView.setSelection(inputView.getText().length());
        inputView.requestFocus();
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                int titleLength = processTextLength(str) / 2;
                stickerInputTv.setText((titleLength >= MAX_TEXT_LENGTH ? MAX_TEXT_LENGTH : titleLength) + "/" + MAX_TEXT_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                // 防止一次输入多个字符导致超出限制
                int length = processTextLength(str) / 2;
                if (length > MAX_TEXT_LENGTH) {
                    int diff = length - MAX_TEXT_LENGTH;
                    String strdiff = str.substring(0, str.length() - diff);
                    inputView.setText(strdiff);
                    inputView.setSelection(inputView.getText().length());
                    str = strdiff;
                }
                if (inputListener instanceof InputWindowListener) {
                    ((InputWindowListener) inputListener).afterTextChanged(str);
                }
            }
        });
    }

    public void setInputText(String text) {
        inputView.setText(text);
        inputView.setSelection(inputView.getText().length());
    }

    @Override
    public View sendView() {
        if (sendView == null) {
            sendView = findViewById(R.id.sticker_finish_tv);
        }
        return sendView;
    }

    @Override
    public boolean firstShowInput() {
        return true;
    }

    @Override
    public int layoutId() {
        return R.layout.plv_sticker_text_input_layout;
    }

    @Override
    public int bgViewId() {
        return R.id.sticker_input_bg;
    }

    @Override
    public int inputViewId() {
        return R.id.sticker_input_et;
    }

    public interface InputWindowListener extends SoftKeyboardListener {
        void onInputContext(PLVStickerTextInputWindow inputWindow);

        void afterTextChanged(String s);
    }

    /**
     * 处理文本长度，使用Unicode块判断中文字符
     */
    private int processTextLength(String text) {
        int length = 0;
        // 判断是否是中文
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(chars[i]);
            boolean isChinese = block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                    || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                    || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                    || block == Character.UnicodeBlock.GENERAL_PUNCTUATION;

            if (isChinese) {
                length += 2;
            } else {
                length++;
            }
        }
        return length;
    }
}
