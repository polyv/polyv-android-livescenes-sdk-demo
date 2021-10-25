package com.easefun.polyv.livehiclass.modules.document.popuplist.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.easefun.polyv.livehiclass.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;

/**
 * 文档删除浮层
 */
public class PLVHCDocumentDeleteArrow {

    private static final String TAG = PLVHCDocumentDeleteArrow.class.getSimpleName();

    private PopupWindow window;
    private View rootView;
    private ImageView deleteIv;

    private int popupWidth;
    private int popupHeight;

    private View.OnClickListener onClickListener;

    public PLVHCDocumentDeleteArrow(Context context) {
        init(context);
    }

    private void init(Context context) {
        popupWidth = PLVScreenUtils.dip2px(context, 64);
        popupHeight = PLVScreenUtils.dip2px(context, 45);

        rootView = View.inflate(context, R.layout.plvhc_document_delete_layout, null);
        findView();

        window = new PopupWindow();
        window.setContentView(rootView);
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        window.setBackgroundDrawable(new ColorDrawable());
        window.setWidth(popupWidth);
        window.setHeight(popupHeight);
    }

    private void findView() {
        deleteIv = (ImageView) rootView.findViewById(R.id.plvs_document_delete_iv);
    }

    public void showAtLocation(final View parent) {
        // 获取需要在其上方显示的控件的位置信息
        int width = parent.getMeasuredWidth();
        int height = parent.getMeasuredHeight();
        window.showAsDropDown(parent, (width - popupWidth) / 2, -height + 5);

        deleteIv.setOnClickListener(new View.OnClickListener() {
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

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

}
