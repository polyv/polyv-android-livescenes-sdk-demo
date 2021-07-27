package com.plv.streameralone.scenes.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.plv.livecommon.ui.widget.PLVConfirmDialog;
import com.plv.livecommon.ui.window.PLVBaseFragment;
import com.plv.streameralone.R;
import com.plv.streameralone.ui.widget.PLVSAConfirmDialog;

/**
 * 清屏页
 */
public class PLVSAEmptyFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private ImageView plvsaEmptyCloseIv;

    // 停止直播确认对话框
    private PLVConfirmDialog stopLiveConfirmDialog;

    private OnViewActionListener onViewActionListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvsa_streamer_empty_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        findView();
        initCloseOnClickListener();
    }

    private void findView() {
        plvsaEmptyCloseIv = (ImageView) findViewById(R.id.plvsa_empty_close_iv);
    }

    private void initCloseOnClickListener() {
        plvsaEmptyCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStopLiveConfirmLayout();
            }
        });
    }

    private void openStopLiveConfirmLayout() {
        if (getContext() == null) {
            return;
        }
        if (stopLiveConfirmDialog == null) {
            stopLiveConfirmDialog = new PLVSAConfirmDialog(getContext())
                    .setTitleVisibility(View.GONE)
                    .setContent("确认结束直播吗？")
                    .setRightButtonText("确认")
                    .setRightBtnListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onViewActionListener != null) {
                                onViewActionListener.onStopLive();
                            }
                            stopLiveConfirmDialog.hide();
                        }
                    });
        }
        stopLiveConfirmDialog.show();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API 外部调用方法">

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnViewActionListener {

        void onViewCreated();

        void onStopLive();

    }

    // </editor-fold>
}
