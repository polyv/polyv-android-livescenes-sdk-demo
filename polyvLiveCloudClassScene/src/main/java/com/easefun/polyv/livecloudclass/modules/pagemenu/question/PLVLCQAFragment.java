package com.easefun.polyv.livecloudclass.modules.pagemenu.question;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.plv.livescenes.feature.pagemenu.PLVQAWebView2;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 问答tab页
 */

public class PLVLCQAFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private PLVQAWebView2 qaWebView;
    //webView的父控件
    private ViewGroup parentLy;

    private String socketMsg;
    private PopupWindow popupWindow;
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plv_horizontal_linear_layout, parentLy, false);
        initView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (qaWebView != null) {
            if (qaWebView.getParent() != null) {
                ((ViewGroup) qaWebView.getParent()).removeView(qaWebView);
            }
            qaWebView.removeAllViews();
            qaWebView.destroy();
            qaWebView = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(String socketMsg, IPLVLiveRoomDataManager liveRoomDataManager) {
        this.socketMsg = socketMsg;
        this.liveRoomDataManager = liveRoomDataManager;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        parentLy = findViewById(com.easefun.polyv.livecommon.R.id.parent_ly);
        parentLy.setBackgroundColor(Color.parseColor("#141518"));

        qaWebView = new PLVQAWebView2(getContext());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.bottomMargin = ConvertUtils.dp2px(8);
        qaWebView.setLayoutParams(llp);
        qaWebView.setLang(PLVLanguageUtil.isENLanguage() ? PLVLiveClassDetailVO.DataBean.QADataBean.LOCALE_EN : PLVLiveClassDetailVO.DataBean.QADataBean.LOCALE_ZH);
        if (liveRoomDataManager != null) {
            qaWebView.setAppParams(PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager));
        }
        parentLy.addView(qaWebView);

        //对PopupWindow的状态监听
        View contentView = getActivity().getLayoutInflater().inflate(R.layout.plvlc_empty_popup, parentLy, false);

        popupWindow = new PopupWindow(contentView, 1, 1, false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                qaWebView.clearFocus();
                qaWebView.setFocusableInTouchMode(false);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
            }
        });

        qaWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    popupWindow.showAtLocation(qaWebView, Gravity.END, 0, 0);
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);
                    v.requestFocus();
                }
                return false;
            }
        });
        qaWebView.loadWeb();

        //延迟2秒初始化webView
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                qaWebView.callInit(socketMsg);
            }
        }, 2000);
    }
    // </editor-fold>
}
