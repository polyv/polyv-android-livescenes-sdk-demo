package com.easefun.polyv.streameralone.modules.streamer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.ui.widget.PLVCoverDrawable;
import com.easefun.polyv.streameralone.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

/**
 * 管理连麦成员的指引提示弹层
 */
public class PLVSAStreamerMemberControlTipsLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //布局弹层容器
    private ViewGroup popupContainer;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStreamerMemberControlTipsLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStreamerMemberControlTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStreamerMemberControlTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open(final View view) {
        if (loadStatus()) {
            return;
        }
        view.post(new Runnable() {
            @Override
            public void run() {
                if (popupContainer == null) {
                    //add view
                    int[] location = new int[2];
                    view.getLocationInWindow(location);
                    location[1] = ConvertUtils.dp2px(78);//scr data include statusBarHeight

                    View childView = LayoutInflater.from(view.getContext()).inflate(R.layout.plvsa_streamer_member_control_tips_child_layout, null);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    lp.leftMargin = location[0];
                    lp.topMargin = location[1] + view.getHeight();
                    childView.setLayoutParams(lp);
                    addView(childView);
                    setBackground(new PLVCoverDrawable(new ColorDrawable(0xb2000000),
                            location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight(), 0, 0));
                    setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            close();
                        }
                    });
                    //find popupContainer
                    popupContainer = (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_control_tips_popup_container);
                    popupContainer.removeAllViews();
                    popupContainer.addView(PLVSAStreamerMemberControlTipsLayout.this);
                    saveStatus();
                } else {
                    popupContainer.removeAllViews();
                    popupContainer.addView(PLVSAStreamerMemberControlTipsLayout.this);
                    saveStatus();
                }
            }
        });
    }

    public boolean isShow() {
        return popupContainer != null && popupContainer.getChildCount() > 0;
    }

    public void close() {
        if (popupContainer != null) {
            popupContainer.removeAllViews();
        }
    }

    public boolean onBackPressed() {
        if (popupContainer != null
                && popupContainer.getChildCount() > 0) {
            popupContainer.removeAllViews();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void saveStatus() {
        SPUtils.getInstance().put("plv_key_streamer_tips_is_showed", true);
    }

    private boolean loadStatus() {
        return SPUtils.getInstance().getBoolean("plv_key_streamer_tips_is_showed", false);
    }
    // </editor-fold>
}