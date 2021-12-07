package com.easefun.polyv.livehiclass.modules.linkmic.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.livehiclass.R;

/**
 * 分组讨论的组长 请求讲师帮助的布局
 */
public class PLVHCGroupLeaderRequestHelpLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private ImageView plvhcGroupRequestHelpIv;
    private ImageView plvhcGroupRequestHelpStatusIv;
    //listener
    private OnHelpLayoutClickListener listener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCGroupLeaderRequestHelpLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCGroupLeaderRequestHelpLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCGroupLeaderRequestHelpLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_linkmic_group_leader_request_help_layout, this);

        plvhcGroupRequestHelpIv = findViewById(R.id.plvhc_group_request_help_iv);
        plvhcGroupRequestHelpStatusIv = findViewById(R.id.plvhc_group_request_help_status_iv);
        plvhcGroupRequestHelpIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(true);
                }
                ((AnimationDrawable) plvhcGroupRequestHelpStatusIv.getDrawable()).start();
                plvhcGroupRequestHelpStatusIv.setVisibility(View.VISIBLE);
            }
        });
        plvhcGroupRequestHelpStatusIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(false);
                }
                ((AnimationDrawable) plvhcGroupRequestHelpStatusIv.getDrawable()).stop();
                plvhcGroupRequestHelpStatusIv.setVisibility(View.GONE);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void setOnLayoutClickListener(OnHelpLayoutClickListener listener) {
        this.listener = listener;
    }

    public void onRequestHelp() {
        ((AnimationDrawable) plvhcGroupRequestHelpStatusIv.getDrawable()).start();
        plvhcGroupRequestHelpStatusIv.setVisibility(View.VISIBLE);
    }

    public void onCancelHelp() {
        ((AnimationDrawable) plvhcGroupRequestHelpStatusIv.getDrawable()).stop();
        plvhcGroupRequestHelpStatusIv.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 监听器">
    public interface OnHelpLayoutClickListener {
        void onClick(boolean isRequest);
    }
    // </editor-fold>
}
