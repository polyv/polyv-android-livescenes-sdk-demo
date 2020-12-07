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
import com.plv.foundationsdk.utils.PLVTimeUtils;


public class PLVLCProgressTipsView extends FrameLayout {
    //progressView
    private View view;
    private TextView tvProgress;

    public PLVLCProgressTipsView(Context context) {
        this(context, null);
    }

    public PLVLCProgressTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCProgressTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.plvlc_tips_view_progress, this);
        initView();
    }

    private void initView() {
        hide();
        tvProgress = view.findViewById(R.id.tv_progress);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void delayHide() {
        handler.removeMessages(View.GONE);
        handler.sendEmptyMessageDelayed(View.GONE, 300);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == View.GONE)
                setVisibility(View.GONE);
        }
    };

    public void setProgressPercent(int fastForwardPos, int totaltime, boolean slideEnd, boolean isRightSwipe) {
        handler.removeMessages(View.GONE);
        if (slideEnd) {
            handler.sendEmptyMessageDelayed(View.GONE, 300);
        } else {
            setVisibility(View.VISIBLE);
            if (fastForwardPos < 0)
                fastForwardPos = 0;
            if (fastForwardPos > totaltime)
                fastForwardPos = totaltime;
            tvProgress.setText(PLVTimeUtils.generateTime(fastForwardPos, true) + "/" + PLVTimeUtils.generateTime(totaltime, true));
        }
    }
}
