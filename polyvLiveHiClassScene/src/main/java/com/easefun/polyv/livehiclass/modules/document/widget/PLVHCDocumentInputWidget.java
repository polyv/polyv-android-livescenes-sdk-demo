package com.easefun.polyv.livehiclass.modules.document.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.document.event.PLVStartEditTextEvent;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

/**
 * 文档区域 文本标注类型输入框
 */
public class PLVHCDocumentInputWidget extends FrameLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private TextView inputCancel;
    private TextView inputSure;
    private EditText inputContent;

    private OnFinishEditTextListener onFinishEditTextListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCDocumentInputWidget(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCDocumentInputWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PLVHCDocumentInputWidget(@NonNull Context context, @Nullable AttributeSet attrs,
                                    int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        View.inflate(getContext(), R.layout.plvhc_document_input_widget, this);
        inputCancel = (TextView) findViewById(R.id.input_cancel);
        inputSure = (TextView) findViewById(R.id.input_sure);
        inputContent = (EditText) findViewById(R.id.input_content);

        inputCancel.setOnClickListener(this);
        inputSure.setOnClickListener(this);

        inputContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputSure.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void removeThis() {
        ViewGroup parent = (ViewGroup) getParent();
        parent.removeView(PLVHCDocumentInputWidget.this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setEditTextEvent(final PLVStartEditTextEvent event) {
        post(new Runnable() {
            @Override
            public void run() {
                try {
                    inputContent.setText(event.getContent());
                    inputContent.setTextColor(Color.parseColor(event.getStrokeStyle()));
                    inputContent.requestFocus();
                    inputContent.setSelection(event.getContent().length());
                    KeyboardUtils.showSoftInput(inputContent);
                } catch (Exception e) {
                    PLVCommonLog.exception(e);
                }
            }
        });
    }

    public void setOnFinishEditTextListener(OnFinishEditTextListener onFinishEditTextListener) {
        this.onFinishEditTextListener = onFinishEditTextListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    @Override
    public void onClick(View v) {
        if (v.getId() == inputCancel.getId()) {
            inputContent.clearFocus();
            KeyboardUtils.hideSoftInput(inputContent);
            removeThis();

            if (onFinishEditTextListener != null) {
                onFinishEditTextListener.onCancelEdit();
            }
        } else if (v.getId() == inputSure.getId()) {
            inputContent.clearFocus();
            KeyboardUtils.hideSoftInput(inputContent);
            removeThis();

            if (onFinishEditTextListener != null) {
                onFinishEditTextListener.onFinishEdit(inputContent.getText().toString());
            }

            inputContent.setText("");
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回调接口定义">

    public interface OnFinishEditTextListener {
        void onCancelEdit();

        void onFinishEdit(String content);
    }

    // </editor-fold>

}
