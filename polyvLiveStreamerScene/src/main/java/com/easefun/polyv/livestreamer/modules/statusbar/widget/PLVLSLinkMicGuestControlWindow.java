package com.easefun.polyv.livestreamer.modules.statusbar.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.util.PLVPopupHelper;
import com.easefun.polyv.livestreamer.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLSLinkMicGuestControlWindow extends FrameLayout {

    private LinearLayout statusBarGuestLinkmicControlLl;
    private ImageView statusBarGuestLinkmicControlIv;
    private TextView statusBarGuestLinkmicControlTv;

    private OnViewActionListener onViewActionListener;

    @Nullable
    private PopupWindow popupWindow;

    public PLVLSLinkMicGuestControlWindow(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLSLinkMicGuestControlWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLSLinkMicGuestControlWindow(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_status_bar_guest_linkmic_control_layout, this);

        statusBarGuestLinkmicControlLl = findViewById(R.id.plvls_status_bar_guest_linkmic_control_ll);
        statusBarGuestLinkmicControlIv = findViewById(R.id.plvls_status_bar_guest_linkmic_control_iv);
        statusBarGuestLinkmicControlTv = findViewById(R.id.plvls_status_bar_guest_linkmic_control_tv);

        statusBarGuestLinkmicControlLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClick();
                }
                if (popupWindow != null) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
            }
        });
    }

    public void show(State currentState, View anchor) {
        currentState.update(this);
        popupWindow = PLVPopupHelper.show(anchor, this,
                new PLVPopupHelper.ShowPopupConfig()
                        .setPosition(PLVPopupHelper.PopupPosition.BOTTOM_CENTER)
                        .setFocusable(true)
                        .setOutsideTouchable(true)
                        .setMarginTop(ConvertUtils.dp2px(7))
        );
    }

    public PLVLSLinkMicGuestControlWindow setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    public interface OnViewActionListener {
        void onClick();
    }

    private static abstract class State {
        abstract void update(PLVLSLinkMicGuestControlWindow window);
    }

    public static final State STATE_REQUESTING = new State() {
        @Override
        void update(PLVLSLinkMicGuestControlWindow window) {
            window.statusBarGuestLinkmicControlIv.setVisibility(GONE);
            window.statusBarGuestLinkmicControlTv.setText("取消申请连麦");
        }
    };

    public static final State STATE_CONNECTED = new State() {
        @Override
        void update(PLVLSLinkMicGuestControlWindow window) {
            window.statusBarGuestLinkmicControlIv.setVisibility(VISIBLE);
            window.statusBarGuestLinkmicControlTv.setText("结束连麦");
        }
    };

}
