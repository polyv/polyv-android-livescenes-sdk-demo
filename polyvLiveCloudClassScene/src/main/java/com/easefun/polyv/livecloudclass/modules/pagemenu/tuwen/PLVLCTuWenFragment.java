package com.easefun.polyv.livecloudclass.modules.pagemenu.tuwen;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livescenes.video.PolyvTuWenWebView;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 图文直播tab页
 */
public class PLVLCTuWenFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //图文webView
    private PolyvTuWenWebView tuWenWebView;
    //webView的父控件
    private ViewGroup parentLy;
    //频道号
    private String channelId;
    //socket监听器
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;
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
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
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
    public void init(String channelId) {
        this.channelId = channelId;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        parentLy = findViewById(com.easefun.polyv.livecommon.R.id.parent_ly);
        parentLy.setBackgroundColor(Color.parseColor("#141518"));
        tuWenWebView = new PolyvTuWenWebView(getContext());

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.bottomMargin = ConvertUtils.dp2px(8);
        tuWenWebView.setLayoutParams(llp);
        parentLy.addView(tuWenWebView);
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
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (PLVEventConstant.TuWen.EVENT_CREATE_IMAGE_TEXT.equals(event)) {//创建图文
                    tuWenWebView.callCreate(message.replaceAll("'", REPLACEMENT));
                } else if (PLVEventConstant.TuWen.EVENT_DELETE_IMAGE_TEXT.equals(event)) {//删除图文
                    tuWenWebView.callDelete(message.replaceAll("'", REPLACEMENT));
                } else if (PLVEventConstant.TuWen.EVENT_SET_TOP_IMAGE_TEXT.equals(event)) {//置顶图文
                    tuWenWebView.callSetTop(message.replaceAll("'", REPLACEMENT));
                } else if (PLVEventConstant.TuWen.EVENT_SET_IMAGE_TEXT_MSG.equals(event)) {//更新图文
                    tuWenWebView.callUpdate(message.replaceAll("'", REPLACEMENT));
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener);

        onConnectStatusListener = new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                if (PLVSocketStatus.STATUS_RECONNECTSUCCESS == status.getStatus()) {
                    tuWenWebView.callRefresh(channelId);//socket重连成功，刷新图文
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(onConnectStatusListener);
    }
    // </editor-fold>
}
