package com.easefun.polyv.streameralone.modules.statusbar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.streameralone.R;

/**
 * @author suhongtao
 */
public class PLVSAStopLiveConfirmLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private TextView stopLiveTv;
    private TextView cancelStopLiveTv;

    private OnClickListener onClickListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVSAStopLiveConfirmLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStopLiveConfirmLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStopLiveConfirmLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_status_bar_stop_live_confirm_layout, this);
        stopLiveTv = findViewById(R.id.plvsa_status_bar_stop_live_tv);
        cancelStopLiveTv = findViewById(R.id.plvsa_status_bar_cancel_stop_live_tv);

        stopLiveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onStopLive();
                }
            }
        });

        cancelStopLiveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onCancel();
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回调设置 & 回调接口定义">

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onStopLive();

        void onCancel();
    }

    // </editor-fold>

}
