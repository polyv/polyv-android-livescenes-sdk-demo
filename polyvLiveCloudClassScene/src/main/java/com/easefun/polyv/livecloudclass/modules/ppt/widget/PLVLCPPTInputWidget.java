package com.easefun.polyv.livecloudclass.modules.ppt.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.document.model.PLVPPTPaintStatus;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

/**
 * 文本画笔类型输入框
 */
public class PLVLCPPTInputWidget extends FrameLayout {

    private RelativeLayout pptInputLayout;
    private TextView pptInputCancel;
    private TextView pptInputConfirm;
    private EditText inputContent;

    private OnViewActionListener onViewActionListener;

    public PLVLCPPTInputWidget(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCPPTInputWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PLVLCPPTInputWidget(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_ppt_input_widget, this);
        pptInputLayout = findViewById(R.id.plvlc_ppt_input_layout);
        pptInputCancel = findViewById(R.id.plvlc_ppt_input_cancel);
        pptInputConfirm = findViewById(R.id.plvlc_ppt_input_confirm);
        inputContent = findViewById(R.id.input_content);

        pptInputCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputContent.clearFocus();
                KeyboardUtils.hideSoftInput(inputContent);
                removeThis();
            }
        });

        pptInputConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputContent.clearFocus();
                KeyboardUtils.hideSoftInput(inputContent);
                removeThis();

                if (onViewActionListener != null) {
                    onViewActionListener.onFinishChangeTextContent(inputContent.getText().toString());
                }

                inputContent.setText("");
            }
        });

    }

    public PLVLCPPTInputWidget setContent(final PLVPPTPaintStatus paintStatus) {
        post(new Runnable() {
            @Override
            public void run() {
                try {
                    inputContent.setText(paintStatus.getContent());
                    inputContent.setTextColor(Color.parseColor(paintStatus.getColor()));

                    inputContent.requestFocus();
                    inputContent.setSelection(paintStatus.getContent().length());
                    KeyboardUtils.showSoftInput(inputContent);
                } catch (Exception e) {
                    PLVCommonLog.exception(e);
                }
            }
        });

        return this;
    }

    public PLVLCPPTInputWidget setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    public void show(ViewGroup parent) {
        parent.addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void removeThis() {
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    public interface OnViewActionListener {
        void onFinishChangeTextContent(String content);
    }
}
