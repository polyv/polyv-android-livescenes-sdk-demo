package com.easefun.polyv.livehiclass.modules.document.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;

import org.jetbrains.annotations.NotNull;

/**
 * @author suhongtao
 */
public class PLVHCDocumentMinimizeItemLayout extends FrameLayout {

    private View rootView;
    private LinearLayout plvhcDocumentMinimizeGroupItemLl;
    private TextView plvhcDocumentTitleTv;
    private ImageView plvhcDocumentCloseIv;

    private String containerId;
    private String pptName;

    private OnLayoutClickedListener onLayoutClickedListener;

    public PLVHCDocumentMinimizeItemLayout(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public PLVHCDocumentMinimizeItemLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCDocumentMinimizeItemLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_document_minimize_item, this);
        findView();
        initOnClickListener();
    }

    private void findView() {
        plvhcDocumentMinimizeGroupItemLl = (LinearLayout) rootView.findViewById(R.id.plvhc_document_minimize_group_item_ll);
        plvhcDocumentTitleTv = (TextView) rootView.findViewById(R.id.plvhc_document_title_tv);
        plvhcDocumentCloseIv = (ImageView) rootView.findViewById(R.id.plvhc_document_close_iv);
    }

    private void initOnClickListener() {
        plvhcDocumentMinimizeGroupItemLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLayoutClickedListener != null) {
                    onLayoutClickedListener.onClickLayout(PLVHCDocumentMinimizeItemLayout.this, containerId, pptName);
                }
            }
        });

        plvhcDocumentCloseIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLayoutClickedListener != null) {
                    onLayoutClickedListener.onClickClose(PLVHCDocumentMinimizeItemLayout.this, containerId, pptName);
                }
            }
        });
    }

    public void setPptData(String containerId, String pptName) {
        this.containerId = containerId;
        this.pptName = pptName;
        plvhcDocumentTitleTv.setText(pptName);
    }

    public void setOnLayoutClickedListener(OnLayoutClickedListener onLayoutClickedListener) {
        this.onLayoutClickedListener = onLayoutClickedListener;
    }

    public interface OnLayoutClickedListener {

        void onClickLayout(PLVHCDocumentMinimizeItemLayout layout, String containerId, String pptName);

        void onClickClose(PLVHCDocumentMinimizeItemLayout layout, String containerId, String pptName);

    }
}
