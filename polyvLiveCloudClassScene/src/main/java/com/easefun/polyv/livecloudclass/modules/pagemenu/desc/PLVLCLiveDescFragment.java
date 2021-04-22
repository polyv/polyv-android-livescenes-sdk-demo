package com.easefun.polyv.livecloudclass.modules.pagemenu.desc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSafeWebView;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewContentUtils;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;

/**
 * 直播介绍tab页
 */
public class PLVLCLiveDescFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播详情信息
    private PolyvLiveClassDetailVO classDetailVO;

    //直播介绍webView
    private PLVSafeWebView descWebView;
    private ViewGroup parentLy;

    //观看热度
    private TextView viewerCountTv;
    private long viewerCount;
    //直播标题
    private TextView titleTv;
    //直播图标
    private ImageView liveCoverIV;
    //支持人名称
    private TextView publisherTv;
    //点赞数
    private TextView likesTv;
    private long likesCount;
    //直播开始时间
    private TextView startTimeTv;
    //直播状态
    private TextView statusTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_page_menu_desc_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (descWebView != null) {
            descWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (descWebView != null) {
            descWebView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //销毁webView
        if (descWebView != null) {
            descWebView.destroy();
            descWebView = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(PolyvLiveClassDetailVO classDetailVO) {
        this.classDetailVO = classDetailVO;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        parentLy = findViewById(R.id.parent_ly);
        viewerCountTv = findViewById(R.id.viewer_count_tv);
        titleTv = findViewById(R.id.title_tv);
        liveCoverIV = findViewById(R.id.live_cover_iv);
        publisherTv = findViewById(R.id.publisher_tv);
        likesTv = findViewById(R.id.likes_tv);
        startTimeTv = findViewById(R.id.start_time_tv);
        statusTv = findViewById(R.id.status_tv);

        if (classDetailVO != null) {
            //设置直播标题
            titleTv.setText(classDetailVO.getData().getName());
            //设置主持人名称
            String publisher = classDetailVO.getData().getPublisher();
            publisherTv.setText(TextUtils.isEmpty(classDetailVO.getData().getPublisher()) ? "主持人" : publisher);
            //设置直播图标
            PLVImageLoader.getInstance().loadImage(classDetailVO.getData().getCoverImage(), liveCoverIV);
            //设置直播状态
            updateStatusViewWithClassDetail();
            //设置直播开始时间
            String liveStartTime = "直播时间：" + (StringUtils.isEmpty(classDetailVO.getData().getStartTime()) ? "无" : classDetailVO.getData().getStartTime());
            startTimeTv.setText(liveStartTime);
            //设置点赞数
            if (likesCount == 0) {
                long newLikeCount = classDetailVO.getData().getLikes();
                updateLikesCount(newLikeCount);
            } else {
                updateLikesCount(0);
            }
/**
 *  设置观看热度
 *             if (viewerCount == 0) {
 *                 int viewerCount = classDetailVO.getData().getPageView();
 *                 updateViewerCount(viewerCount);
 *             } else {
 *                 updateViewerCount(0);
 *             }
 */

            //设置介绍内容
            setDescContent();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点赞 - 更新点赞view">
    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
        updateLikesCount(0);
    }

    private void updateLikesCount(long addCount) {
        likesCount = likesCount + addCount;
        String likesString = StringUtils.toWString(likesCount);
        if (likesTv != null) {
            likesTv.setText(likesString);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="观看热度 - 更新热度view、观察热度变化">
    public void setViewerCount(long viewerCount) {
        this.viewerCount = viewerCount;
        updateViewerCount(0);
    }

    private void updateViewerCount(long addCount) {
        viewerCount = viewerCount + addCount;
        if (viewerCountTv != null) {
            viewerCountTv.setText(StringUtils.toWString(viewerCount));
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播状态 - 更新状态view">
    private void updateStatusViewWithClassDetail() {
        if (classDetailVO != null) {
            if (classDetailVO.getData().isLiveStatus()) {
                updateStatusViewWithLive();
            } else if (classDetailVO.getData().isPlaybackStatus()
                    || classDetailVO.getData().isEndStatus()) {
                updateStatusViewWithNoLive();
            } else {
                updateStatusViewWithWaiting();
            }
        }
    }

    public void updateStatusViewWithNoLive() {
        if (statusTv != null) {
            statusTv.setText("暂无直播");
            statusTv.setTextColor(getResources().getColor(R.color.text_gray));
            statusTv.setBackgroundResource(R.drawable.plvlc_live_status_noactive);
        }
    }

    public void updateStatusViewWithLive() {
        if (statusTv != null) {
            statusTv.setText("直播中");
            statusTv.setTextColor(getResources().getColor(R.color.text_red));
            statusTv.setBackgroundResource(R.drawable.plvlc_live_status_live);
        }
    }

    public void updateStatusViewWithWaiting() {
        if (statusTv != null) {
            statusTv.setText("等待中");
            statusTv.setTextColor(getResources().getColor(R.color.text_green));
            statusTv.setBackgroundResource(R.drawable.plvlc_live_status_waitting);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播介绍内容 - 数据处理，webView加载">
    private void setDescContent() {
        if (classDetailVO == null) {
            return;
        }
        for (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : classDetailVO.getData().getChannelMenus()) {
            if (PolyvLiveClassDetailVO.MENUTYPE_DESC.equals(channelMenusBean.getMenuType())) {
                acceptIntroMsg(channelMenusBean.getContent());
                break;
            }
        }
    }

    private void acceptIntroMsg(String content) {
        if (TextUtils.isEmpty(content)) {
            if (descWebView != null && descWebView.getParent() != null) {
                ((ViewGroup) descWebView.getParent()).removeView(descWebView);
            }
            return;
        }
        content = PLVWebViewContentUtils.toWebViewContent(content, "#ADADC0");
        loadWebView(content);
    }

    private void loadWebView(String content) {
        if (descWebView == null) {
            descWebView = new PLVSafeWebView(getContext());
            descWebView.clearFocus();
            descWebView.setFocusable(false);
            descWebView.setFocusableInTouchMode(false);
            descWebView.setBackgroundColor(Color.TRANSPARENT);
            descWebView.setHorizontalScrollBarEnabled(false);
            descWebView.setVerticalScrollBarEnabled(false);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rlp.addRule(RelativeLayout.BELOW, R.id.top_ly);
            rlp.leftMargin = ConvertUtils.dp2px(6);
            rlp.topMargin = ConvertUtils.dp2px(6);
            rlp.rightMargin = ConvertUtils.dp2px(6);
            rlp.bottomMargin = ConvertUtils.dp2px(6);
            descWebView.setLayoutParams(rlp);
            parentLy.addView(descWebView);
            PLVWebViewHelper.initWebView(getContext(), descWebView);
            descWebView.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
        } else {
            if (descWebView.getParent() != null) {
                ((ViewGroup) descWebView.getParent()).removeView(descWebView);
            }
            parentLy.addView(descWebView);
            descWebView.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="是否拦截返回事件">
    public boolean onBackPressed() {
        if (descWebView == null || !descWebView.canGoBack()) {
            return false;
        }
        descWebView.goBack();
        return true;
    }
    // </editor-fold>
}
