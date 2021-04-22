package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.webview.PLVSafeWebView;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewContentUtils;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * 直播详情页： 公告、直播介绍
 */
public class PLVECLiveDetailFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //公告布局
    private ViewGroup bulletinBlurLy;
    private TextView bulletinMsgTv;
    //直播介绍布局
    private ViewGroup introBlurLy;
    private PLVSafeWebView introWebView;
    private TextView introEmtTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_live_page_detail_fragment, null);
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

    @Override
    public void onPause() {
        super.onPause();
        if (introWebView != null) {
            introWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (introWebView != null) {
            introWebView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (introWebView != null) {
            introWebView.destroy();
            introWebView = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        bulletinBlurLy = findViewById(R.id.bulletin_ly);
        bulletinMsgTv = findViewById(R.id.bulletin_msg_tv);
        introBlurLy = findViewById(R.id.intro_ly);
        introEmtTv = findViewById(R.id.intro_emt_tv);

        //按百分比调整滚动区域的高度
        ScrollView detailSv = findViewById(R.id.detail_sv);
        ViewGroup.LayoutParams detailSvLp = detailSv.getLayoutParams();
        detailSvLp.height = (int) (Math.max(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth()) * 0.66);
        detailSv.setLayoutParams(detailSvLp);
        //调整占位view的高度
        View solidView = findViewById(R.id.solid_view);
        ViewGroup.LayoutParams solidViewLp = solidView.getLayoutParams();
        solidViewLp.height = (int) (detailSvLp.height * 0.15);
        solidView.setLayoutParams(solidViewLp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void setClassDetailVO(PolyvLiveClassDetailVO liveClassDetailVO) {
        if (liveClassDetailVO == null) {
            return;
        }
        for (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : liveClassDetailVO.getData().getChannelMenus()) {
            if (PolyvLiveClassDetailVO.MENUTYPE_DESC.equals(channelMenusBean.getMenuType())) {
                acceptIntroMsg(channelMenusBean.getContent());
                break;
            }
        }
    }

    public void setBulletinVO(PolyvBulletinVO bulletinVO) {
        if (bulletinVO != null) {
            showBulletin(bulletinVO);
        } else {
            hideBulletin();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="公告 - 显示、隐藏">
    private void showBulletin(PolyvBulletinVO bulletinVO) {
        bulletinMsgTv.setText(Html.fromHtml(bulletinVO.getContent()));
        bulletinMsgTv.setMovementMethod(LinkMovementMethod.getInstance());
        bulletinBlurLy.setVisibility(View.VISIBLE);
    }

    private void hideBulletin() {
        bulletinBlurLy.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播介绍 - 数据处理，webView加载">
    private void acceptIntroMsg(String content) {
        if (TextUtils.isEmpty(content)) {
            introEmtTv.setVisibility(View.VISIBLE);
            if (introWebView != null && introWebView.getParent() != null) {
                ((ViewGroup) introWebView.getParent()).removeView(introWebView);
            }
            return;
        }
        content = PLVWebViewContentUtils.toWebViewContent(content);
        loadWebView(content);
    }

    private void loadWebView(String content) {
        if (introWebView == null) {
            introWebView = new PLVSafeWebView(getContext());
            introWebView.clearFocus();
            introWebView.setFocusable(false);
            introWebView.setFocusableInTouchMode(false);
            introWebView.setBackgroundColor(Color.TRANSPARENT);
            introWebView.setHorizontalScrollBarEnabled(false);
            introWebView.setVerticalScrollBarEnabled(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.topMargin = ConvertUtils.dp2px(16);
            introWebView.setLayoutParams(lp);
            introBlurLy.addView(introWebView);
            PLVWebViewHelper.initWebView(getContext(), introWebView);
            introWebView.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
        } else {
            if (introWebView.getParent() != null) {
                ((ViewGroup) introWebView.getParent()).removeView(introWebView);
            }
            introBlurLy.addView(introWebView);
            introWebView.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onViewCreated();
    }
    // </editor-fold>
}
