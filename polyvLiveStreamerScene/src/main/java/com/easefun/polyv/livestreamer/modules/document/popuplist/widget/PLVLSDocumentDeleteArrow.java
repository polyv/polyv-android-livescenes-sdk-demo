package com.easefun.polyv.livestreamer.modules.document.popuplist.widget;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

/**
 * 文档删除浮层
 */
public class PLVLSDocumentDeleteArrow {

    private static final String TAG = PLVLSDocumentDeleteArrow.class.getSimpleName();

    private PopupWindow window;
    private View rootView;
    private TextView plvsDocumentDeleteText;

    private static final int POPUP_WIDTH = PLVScreenUtils.dip2px(64);
    private static final int POPUP_HEIGHT = PLVScreenUtils.dip2px(45);

    private View.OnClickListener onClickListener;

    public PLVLSDocumentDeleteArrow() {
        init();
    }

    private void init() {
        if (window != null) {
            return;
        }
        window = new PopupWindow();
        rootView = View.inflate(ActivityUtils.getTopActivity(), R.layout.plvls_document_delete_layout, null);
        initView();
        window.setContentView(rootView);
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        window.setBackgroundDrawable(new ColorDrawable());

        window.setWidth(POPUP_WIDTH);
        window.setHeight(POPUP_HEIGHT);
    }

    private void initView() {
        plvsDocumentDeleteText = (TextView) rootView.findViewById(R.id.plvs_document_delete_text);
    }

    public void showAtLocation(final View parent) {
        // 获取需要在其上方显示的控件的位置信息
        int width = parent.getMeasuredWidth();
        int height = parent.getMeasuredHeight();
        window.showAsDropDown(parent, (width - POPUP_WIDTH) / 2, -height + 5);

        plvsDocumentDeleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (onClickListener != null) {
                    onClickListener.onClick(window.getContentView());
                }
            }
        });
    }

    public void hide() {
        window.dismiss();
    }

    public void destroy() {
        window = null;
        plvsDocumentDeleteText = null;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
