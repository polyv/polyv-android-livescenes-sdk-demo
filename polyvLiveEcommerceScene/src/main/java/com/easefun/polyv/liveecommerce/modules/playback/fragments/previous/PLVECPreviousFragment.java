package com.easefun.polyv.liveecommerce.modules.playback.fragments.previous;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;

/**
 * 纯视频显示往期视频的Fragment
 */
public class PLVECPreviousFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //回放列表view
    private PLVPreviousView plvPreviousView;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plvec_playback_previous_fragment, container, false);
        this.view = view;
        initView();
        initData();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Fragment销毁的时候移除掉plvPreviousView
        ((ViewGroup) view).removeView(plvPreviousView);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        //将回放列表view添加进来
        if (plvPreviousView != null) {
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ((ViewGroup) view).addView(plvPreviousView, layoutParams);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">

    /**
     * 初始化数据，目前是空实现，留下空白提供给以后改动的地方
     */
    private void initData() {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外提供api">

    /**
     * 设置plvPreviousView
     *
     * @param plvPreviousView
     */
    public void setPrviousView(PLVPreviousView plvPreviousView) {
        this.plvPreviousView = plvPreviousView;
    }

    // </editor-fold>

}
