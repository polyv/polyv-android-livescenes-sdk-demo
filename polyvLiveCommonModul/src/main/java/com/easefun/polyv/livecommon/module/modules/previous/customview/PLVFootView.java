package com.easefun.polyv.livecommon.module.modules.previous.customview;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;

import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * Author:lzj
 * Time:2021/12/28
 * Description: 刷新框架中上拉加载更多使用到的footView，当上拉加载更多的时候就会显示下面这个View
 */
public class PLVFootView extends FrameLayout implements IRefreshView<IIndicator> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private final TextView loadMoreTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVFootView(@NonNull Context context) {
        this(context, null);
    }

    public PLVFootView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVFootView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.plv_previous_loadmore_foot, this, true);
        loadMoreTv = findViewById(R.id.plv_foot_load_more_tv);

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实现的接口方法">
    @Override
    public int getType() {
        return TYPE_FOOTER;
    }

    @Override
    public int getStyle() {
        return STYLE_DEFAULT;
    }

    @Override
    public int getCustomHeight() {
        return 0;
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {

    }

    @Override
    public void onReset(SmoothRefreshLayout layout) {

    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {

    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {

    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {
        if (!isSuccessful) {
            loadMoreTv.setText(R.string.plv_previous_load_error);
            return;
        }
        if (layout.isEnabledNoMoreData()) {
            loadMoreTv.setText(R.string.plv_previous_no_more_data);
        }
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {

    }

    @Override
    public void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="自己定义的方法">
    public void setText(String string) {
        loadMoreTv.setText(string);
    }
    // </editor-fold>
}
