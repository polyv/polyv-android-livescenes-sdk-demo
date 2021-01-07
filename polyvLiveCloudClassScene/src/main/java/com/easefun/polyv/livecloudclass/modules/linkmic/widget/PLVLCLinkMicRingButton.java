package com.easefun.polyv.livecloudclass.modules.linkmic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.PLVNoConsumeTouchEventButton;

/**
 * date: 2020/8/17
 * author: HWilliamgo
 * description: 连麦话筒按钮
 */
public class PLVLCLinkMicRingButton extends PLVNoConsumeTouchEventButton {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int STATE_RING_UP = 0;
    private static final int STATE_RING_OFF = 2;
    private static final int STATE_RING_SETTING = 3;

    private OnPLVLCLinkMicRingButtonClickListener onPLVLCLinkMicRingButtonClickListener;

    private int state;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCLinkMicRingButton(Context context) {
        this(context, null);
    }

    public PLVLCLinkMicRingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLinkMicRingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPLVLCLinkMicRingButtonClickListener != null) {
                    switch (state) {
                        case STATE_RING_UP:
                            onPLVLCLinkMicRingButtonClickListener.onClickRingUp();
                            break;
                        case STATE_RING_OFF:
                            onPLVLCLinkMicRingButtonClickListener.onClickRingOff();
                            break;
                        case STATE_RING_SETTING:
                            onPLVLCLinkMicRingButtonClickListener.onClickRingSetting();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    public void setOnLinkMicRingButtonClickListener(OnPLVLCLinkMicRingButtonClickListener onLinkMicRingButtonClickListener) {
        this.onPLVLCLinkMicRingButtonClickListener = onLinkMicRingButtonClickListener;
    }

    public void setRingUpState() {
        state = STATE_RING_UP;
        setBackgroundResource(R.drawable.plvlc_linkmic_iv_ring_up);
    }

    public void setRingOffState() {
        state = STATE_RING_OFF;
        setBackgroundResource(R.drawable.plvlc_linkmic_iv_ring_off);
    }

    public void setRingSettingState() {
        state = STATE_RING_SETTING;
        setBackgroundResource(R.drawable.plvlc_linkmic_ring_setting);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">
    public interface OnPLVLCLinkMicRingButtonClickListener {
        //点击拨打上麦
        void onClickRingUp();

        //点击挂断下麦
        void onClickRingOff();

        //点击设置
        void onClickRingSetting();
    }
    // </editor-fold>
}
