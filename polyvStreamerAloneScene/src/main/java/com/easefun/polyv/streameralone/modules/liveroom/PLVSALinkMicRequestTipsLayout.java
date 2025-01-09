package com.easefun.polyv.streameralone.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.easefun.polyv.streameralone.R;


/**
 * 有人申请连麦时 提示条布局
 *
 * @author suhongtao
 */
public class PLVSALinkMicRequestTipsLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVTriangleIndicateLayout plvsaEmptyLinkmicTipsGroupLayout;
    private LinearLayout plvsaEmptyLinkmicTipsLl;
    private ImageView plvsaEmptyLinkmicTipsIconIv;
    private TextView plvsaEmptyLinkmicTipsTv;
    private Button plvsaEmptyLinkmicNavBtn;

    private OnTipsClickListener onTipsClickListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    public PLVSALinkMicRequestTipsLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSALinkMicRequestTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSALinkMicRequestTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_linkmic_request_tips_layout, this);
        findView();

        initLinkmicTipsOnClickListener();
    }

    private void findView() {
        plvsaEmptyLinkmicTipsGroupLayout = findViewById(R.id.plvsa_empty_linkmic_tips_group_layout);
        plvsaEmptyLinkmicTipsLl = findViewById(R.id.plvsa_empty_linkmic_tips_ll);
        plvsaEmptyLinkmicTipsIconIv = findViewById(R.id.plvsa_empty_linkmic_tips_icon_iv);
        plvsaEmptyLinkmicTipsTv = findViewById(R.id.plvsa_empty_linkmic_tips_tv);
        plvsaEmptyLinkmicNavBtn = findViewById(R.id.plvsa_empty_linkmic_nav_btn);
    }

    private void initLinkmicTipsOnClickListener() {
        plvsaEmptyLinkmicTipsGroupLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTipsClickListener != null) {
                    onTipsClickListener.onClickBar();
                }
            }
        });
        plvsaEmptyLinkmicNavBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTipsClickListener != null) {
                    onTipsClickListener.onClickNavBtn();
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 外部调用的方法">

    public void show() {
        PLVViewUtil.showViewForDuration(this, seconds(10).toMillis());
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void setOnTipsClickListener(OnTipsClickListener onTipsClickListener) {
        this.onTipsClickListener = onTipsClickListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnTipsClickListener {
        void onClickBar();

        void onClickNavBtn();
    }

    // </editor-fold>

}
