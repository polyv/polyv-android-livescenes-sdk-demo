package com.easefun.polyv.livecommon.module.modules.interact.entrance;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.plv.socket.event.interact.PLVCallAppEvent;

import java.util.List;

/**
 * 互动入口布局
 */
public class PLVInteractEntranceLayout extends FrameLayout {
    // <editor-folder defaultstate="collapsed" desc="变量">
    private LinearLayout plvQuestionLy;
    private ImageView plvQuestionIv;
    private TextView plvQuestionTv;

    private OnViewActionListener onViewActionListener;
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="构造器">
    public PLVInteractEntranceLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVInteractEntranceLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVInteractEntranceLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(View.GONE);
        LayoutInflater.from(context).inflate(R.layout.plv_interact_entrance_layout, this);
        initView();
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="初始化View">
    private void initView() {
        plvQuestionLy = (LinearLayout) findViewById(R.id.plv_question_ly);
        plvQuestionIv = (ImageView) findViewById(R.id.plv_question_iv);
        plvQuestionTv = (TextView) findViewById(R.id.plv_question_tv);

        plvQuestionLy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowQuestionnaire();
                }
            }
        });
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="对外API">
    public void changeLayoutStyle(boolean isCloudClassStyle) {
        if (isCloudClassStyle) {
            setBackgroundColor(Color.parseColor("#1A1B1F"));
            plvQuestionLy.setBackgroundResource(R.drawable.plv_interact_entrance_ly_bg_selector_lc);
        } else {
            setBackgroundColor(Color.parseColor("#00000000"));
            plvQuestionLy.setBackgroundResource(R.drawable.plv_interact_entrance_ly_bg_selector_ec);
        }
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public void acceptInteractEntranceData(List<PLVCallAppEvent.ValueBean.DataBean> dataBeans) {
        if (dataBeans == null || dataBeans.isEmpty()) {
            setVisibility(View.GONE);
            return;
        }
        boolean isShowLayout = false;
        for (PLVCallAppEvent.ValueBean.DataBean dataBean : dataBeans) {
            if (dataBean.isShowQuestionnaireEvent()) {
                plvQuestionLy.setVisibility(dataBean.isShow() ? View.VISIBLE : View.GONE);
                plvQuestionTv.setText(dataBean.getTitle());
                if (dataBean.isShow()) {
                    isShowLayout = true;
                }
                //暂时只处理问卷
                break;
            }
        }
        setVisibility(isShowLayout ? View.VISIBLE : View.GONE);
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="内部类">
    public interface OnViewActionListener {

        /**
         * 显示问卷
         */
        void onShowQuestionnaire();
    }
    // </editor-folder>
}
