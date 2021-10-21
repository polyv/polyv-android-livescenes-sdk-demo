package com.easefun.polyv.livehiclass.modules.liveroom;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;

/**
 * 学生举手动画布局
 */
public class PLVHCStudentHandsLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private ImageView plvhcMemberHandsUpIv;
    private TextView plvhcMemberHandsUpCountTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCStudentHandsLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCStudentHandsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCStudentHandsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_member_hands_up_layout, this);

        plvhcMemberHandsUpIv = findViewById(R.id.plvhc_member_hands_up_iv);
        plvhcMemberHandsUpCountTv = findViewById(R.id.plvhc_member_hands_up_count_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void acceptUserRaiseHand(int raiseHandCount, boolean isRaiseHand) {
        if (raiseHandCount == 0) {
            ((AnimationDrawable) plvhcMemberHandsUpIv.getDrawable()).stop();
            setVisibility(View.GONE);
            return;
        }
        plvhcMemberHandsUpCountTv.setText(String.valueOf(raiseHandCount));
        ((AnimationDrawable) plvhcMemberHandsUpIv.getDrawable()).start();
        setVisibility(View.VISIBLE);
    }
    // </editor-fold>
}
