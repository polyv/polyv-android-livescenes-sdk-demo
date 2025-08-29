package com.easefun.polyv.livecloudclass.modules.pagemenu.aisummary;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.PLVAISummaryWebView;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.plv.android.jsbridge.CallBackFunction;

/**
 * AI看tab页
 */

public class PLVLCAISummaryFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVLCAISummaryFragment";
    private PLVAISummaryWebView aiSummaryWebView;
    //webView的父控件
    private ViewGroup parentLy;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    private Context context;
    private String playDataId;
    private String playDataType;
    private PLVAISummaryWebView.OnSeekVideoListener onSeekVideoListener;
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
        if (aiSummaryWebView != null) {
            if (aiSummaryWebView.getParent() != null) {
                ((ViewGroup) aiSummaryWebView.getParent()).removeView(aiSummaryWebView);
            }
            aiSummaryWebView.removeAllViews();
            aiSummaryWebView.destroy();
            aiSummaryWebView = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager, Context context) {
        this.liveRoomDataManager = liveRoomDataManager;
        this.context = context;
        observeLiveData();
    }

    public void setPlayDataIdAndType(String playDataId, String playDataType) {
        this.playDataId = playDataId;
        this.playDataType = playDataType;
        if (aiSummaryWebView != null) {
            aiSummaryWebView.callSetupVideo(playDataId, playDataType);
        }
    }

    public void setOnSeekVideoListener(PLVAISummaryWebView.OnSeekVideoListener onSeekVideoListener) {
        this.onSeekVideoListener = onSeekVideoListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        parentLy = findViewById(com.easefun.polyv.livecommon.R.id.parent_ly);
        parentLy.setBackgroundColor(Color.parseColor("#141518"));

        aiSummaryWebView = new PLVAISummaryWebView(getContext());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.bottomMargin = ConvertUtils.dp2px(8);
        aiSummaryWebView.setLayoutParams(llp);
        aiSummaryWebView.setLang(PLVLanguageUtil.isENLanguage() ? PLVAISummaryWebView.LANG_EN : PLVAISummaryWebView.LANG_ZH);
        if (liveRoomDataManager != null) {
            aiSummaryWebView.setAppParams(PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager));
        }
        parentLy.addView(aiSummaryWebView);

        aiSummaryWebView.setOnSeekVideoListener(new PLVAISummaryWebView.OnSeekVideoListener() {
            @Override
            public void onSeekVideo(int timeSecond) {
                if (onSeekVideoListener != null) {
                    onSeekVideoListener.onSeekVideo(timeSecond);
                }
            }
        });
        aiSummaryWebView.loadWeb();
        aiSummaryWebView.callSetupVideo(playDataId, playDataType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅更新">
    private void observeLiveData() {
        //更新chatToken
        liveRoomDataManager.getChatTokenLiveData().observe((LifecycleOwner) context, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String chatToken) {
                if (!TextUtils.isEmpty(chatToken) && aiSummaryWebView != null) {
                    if (liveRoomDataManager != null) {
                        aiSummaryWebView.setAppParams(PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager));
                    }
                    aiSummaryWebView.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO, getNativeAppPramsInfo(), new CallBackFunction() {
                        @Override
                        public void onCallBack(String s) {
                            PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_UPDATE_NATIVE_APP_PARAMS_INFO + " " + s);
                        }
                    });
                }
            }
        });
    }

    private String getNativeAppPramsInfo() {
        if (liveRoomDataManager != null) {
            PLVInteractNativeAppParams nativeAppParams = PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager);
            return PLVGsonUtil.toJsonSimple(nativeAppParams);
        }
        return "";
    }
    // </editor-fold >
}
