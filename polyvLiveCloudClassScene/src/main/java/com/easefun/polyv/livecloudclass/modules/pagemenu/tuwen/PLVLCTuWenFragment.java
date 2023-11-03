package com.easefun.polyv.livecloudclass.modules.pagemenu.tuwen;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.player.live.enums.PLVLiveStateEnum;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.PLVTuWenWebView2;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 图文直播tab页
 */
public class PLVLCTuWenFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //图文webView
    private PLVTuWenWebView2 tuWenWebView;
    //webView的父控件
    private ViewGroup parentLy;
    //频道号
    private String channelId;
    //互动应用app需要的属性
    private PLVInteractNativeAppParams appParams;
    private PLVSocketIOObservable.OnConnectStatusListener onConnectStatusListener;
    private static final String REPLACEMENT = "\\\\u0027";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plv_horizontal_linear_layout, null);
        initView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnConnectStatusListener(onConnectStatusListener);
        if (tuWenWebView != null) {
            if (tuWenWebView.getParent() != null) {
                ((ViewGroup) tuWenWebView.getParent()).removeView(tuWenWebView);
            }
            tuWenWebView.removeAllViews();
            tuWenWebView.destroy();
            tuWenWebView = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(String channelId, PLVInteractNativeAppParams appParams) {
        this.channelId = channelId;
        this.appParams = appParams;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        parentLy = findViewById(com.easefun.polyv.livecommon.R.id.parent_ly);
        parentLy.setBackgroundColor(Color.parseColor("#141518"));
        tuWenWebView = new PLVTuWenWebView2(getContext());
        tuWenWebView.setAppParams(appParams);

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.bottomMargin = ConvertUtils.dp2px(8);
        tuWenWebView.setLayoutParams(llp);
        parentLy.addView(tuWenWebView);
        tuWenWebView.setLang(PLVLanguageUtil.isENLanguage() ? PLVTuWenWebView2.LANG_EN : PLVTuWenWebView2.LANG_ZH);
        tuWenWebView.loadWeb();

        //延迟3秒初始化webView
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tuWenWebView.callInit(channelId);
            }
        }, 3000);
        //数据监听
        observeDataChangedWithSocket();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 观察socket的连接状态、图文相关事件">
    private void observeDataChangedWithSocket() {
        onConnectStatusListener = new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                if (PLVSocketStatus.STATUS_RECONNECTSUCCESS == status.getStatus()) {
                    tuWenWebView.callRefresh();//socket重连成功，刷新图文
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(onConnectStatusListener);
    }
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听当前直播状态变化">
    public void updateLiveStatus(PLVLiveStateEnum liveStateEnum) {
        if (liveStateEnum.getStatus().equals(PLVLiveStateEnum.LIVE.getStatus())) {
            appParams.setIsLive(true);
        } else {
            appParams.setIsLive(false);
        }
        if (tuWenWebView != null) {
            tuWenWebView.setAppParams(appParams);
            tuWenWebView.updateNativeAppParamsInfo(appParams);
        }
    }
    // </editor-fold>
}
