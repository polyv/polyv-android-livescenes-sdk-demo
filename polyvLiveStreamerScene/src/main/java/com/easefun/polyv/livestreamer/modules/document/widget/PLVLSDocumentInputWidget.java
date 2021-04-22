package com.easefun.polyv.livestreamer.modules.document.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livescenes.document.model.PLVSPPTPaintStatus;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

/**
 * 文档区域 文本标注类型输入框
 */
public class PLVLSDocumentInputWidget extends FrameLayout {
    private TextView inputCancle;
    private TextView inputSure;
    private EditText inputContent;

    public PLVLSDocumentInputWidget(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSDocumentInputWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PLVLSDocumentInputWidget(@NonNull Context context, @Nullable AttributeSet attrs,
                                    int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    private void initView() {
        View.inflate(getContext(), R.layout.plvls_document_input_widget, this);
        inputCancle = (TextView) findViewById(R.id.input_cancle);
        inputSure = (TextView) findViewById(R.id.input_sure);
        inputContent = (EditText) findViewById(R.id.input_content);

        inputCancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputContent.clearFocus();
                KeyboardUtils.hideSoftInput(inputContent);
                removeThis();

            }
        });

        inputSure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputContent.clearFocus();
                KeyboardUtils.hideSoftInput(inputContent);
                removeThis();

                PLVDocumentPresenter.getInstance().changeTextContent(inputContent.getText().toString());

                inputContent.setText("");

            }
        });

    }

    private void removeThis() {
        ViewGroup parent = (ViewGroup) getParent();
        parent.removeView(PLVLSDocumentInputWidget.this);
    }

    public void setText(final PLVSPPTPaintStatus paintStatus) {

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

    }
}
