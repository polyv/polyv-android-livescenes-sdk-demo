package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;

public class PLVLCLightTipsView extends FrameLayout {
    //lightView
    private View view;
    private TextView tvPercent;

    public PLVLCLightTipsView(Context context) {
        this(context, null);
    }

    public PLVLCLightTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLightTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.plvlc_player_light_tips_layout, this);
        initView();
    }

    private void initView() {
        hide();
        tvPercent = (TextView) view.findViewById(R.id.tv_percent);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == View.GONE)
                setVisibility(View.GONE);
        }
    };

    public void setLightPercent(int brightness, boolean slideEnd) {
        handler.removeMessages(View.GONE);
        if (slideEnd) {
            handler.sendEmptyMessageDelayed(View.GONE, 300);
        } else {
            setVisibility(View.VISIBLE);
            tvPercent.setText(brightness + "%");
        }
    }
}
