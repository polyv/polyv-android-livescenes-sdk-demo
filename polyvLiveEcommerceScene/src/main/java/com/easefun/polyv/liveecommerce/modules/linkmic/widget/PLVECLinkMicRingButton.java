package com.easefun.polyv.liveecommerce.modules.linkmic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.PLVNoConsumeTouchEventButton;
import com.easefun.polyv.liveecommerce.R;

/**
 * date: 2020/8/17
 * author: HWilliamgo
 * description: 连麦话筒按钮
 */
public class PLVECLinkMicRingButton extends PLVNoConsumeTouchEventButton {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int STATE_RING_UP = 0;
    private static final int STATE_RING_OFF = 2;
    private static final int STATE_RING_SETTING = 3;

    private OnPLVECLinkMicRingButtonClickListener onPLVECLinkMicRingButtonClickListener;

    private int state;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECLinkMicRingButton(Context context) {
        this(context, null);
    }

    public PLVECLinkMicRingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLinkMicRingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPLVECLinkMicRingButtonClickListener != null) {
                    switch (state) {
                        case STATE_RING_UP:
                            onPLVECLinkMicRingButtonClickListener.onClickRingUp();
                            break;
                        case STATE_RING_OFF:
                            onPLVECLinkMicRingButtonClickListener.onClickRingOff();
                            break;
                        case STATE_RING_SETTING:
                            onPLVECLinkMicRingButtonClickListener.onClickRingSetting();
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

    public void setOnLinkMicRingButtonClickListener(OnPLVECLinkMicRingButtonClickListener onLinkMicRingButtonClickListener) {
        this.onPLVECLinkMicRingButtonClickListener = onLinkMicRingButtonClickListener;
    }

    public void setRingUpState() {
        state = STATE_RING_UP;
        setBackgroundResource(R.drawable.plvec_linkmic_iv_ring_up);
    }

    public void setRingOffState() {
        state = STATE_RING_OFF;
        setBackgroundResource(R.drawable.plvec_linkmic_iv_ring_off);
    }

    public void setRingSettingState() {
        state = STATE_RING_SETTING;
        setBackgroundResource(R.drawable.plvec_linkmic_ring_setting);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">
    public interface OnPLVECLinkMicRingButtonClickListener {
        //点击拨打上麦
        void onClickRingUp();

        //点击挂断下麦
        void onClickRingOff();

        //点击设置
        void onClickRingSetting();
    }
    // </editor-fold>
}
