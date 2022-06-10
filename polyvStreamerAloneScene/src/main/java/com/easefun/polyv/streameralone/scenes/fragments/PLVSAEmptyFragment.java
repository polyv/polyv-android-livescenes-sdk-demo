package com.easefun.polyv.streameralone.scenes.fragments;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.socket.user.PLVSocketUserConstant;

/**
 * 清屏页
 */
public class PLVSAEmptyFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private ConstraintLayout emptyFragmentLayout;
    private ImageView plvsaEmptyCloseIv;

    // 停止直播确认对话框
    private PLVConfirmDialog stopLiveConfirmDialog;

    private boolean isBeautyLayoutShowing = false;

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
        observeBeautyLayoutStatus();
    }

    private void findView() {
        emptyFragmentLayout = findViewById(R.id.plvsa_empty_fragment_layout);
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

    private void observeBeautyLayoutStatus() {
        if (getActivity() == null) {
            return;
        }
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe(getActivity(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        PLVSAEmptyFragment.this.isBeautyLayoutShowing = beautyUiState != null && beautyUiState.isBeautyMenuShowing;
                        updateVisibility();
                    }
                });
    }

    private void openStopLiveConfirmLayout() {
        if (getContext() == null) {
            return;
        }

        boolean isGuest = PLVSocketUserConstant.USERTYPE_GUEST.equals(PLVLiveChannelConfigFiller.generateNewChannelConfig().getUser().getViewerType());
        String content = isGuest ? getContext().getString(R.string.plv_live_room_dialog_exit_confirm_ask)
                : getContext().getString(R.string.plv_live_room_dialog_steamer_exit_confirm_ask);

        if (stopLiveConfirmDialog == null) {
            stopLiveConfirmDialog = new PLVSAConfirmDialog(getContext())
                    .setTitleVisibility(View.GONE)
                    .setContent(content)
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

    // <editor-fold defaultstate="collapsed" desc="内部处理">

    private void updateVisibility() {
        // 美颜布局显示时，不显示主页布局
        if (isBeautyLayoutShowing) {
            emptyFragmentLayout.setVisibility(View.GONE);
            return;
        }
        emptyFragmentLayout.setVisibility(View.VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnViewActionListener {

        void onViewCreated();

        void onStopLive();

    }

    // </editor-fold>
}
