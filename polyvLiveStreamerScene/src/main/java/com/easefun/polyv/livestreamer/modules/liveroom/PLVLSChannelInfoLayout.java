package com.easefun.polyv.livestreamer.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.imageScan.PLVChatImageViewerFragment;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSafeWebView;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewContentUtils;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livestreamer.R;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 频道信息布局
 */
public class PLVLSChannelInfoLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //布局弹层
    private PLVMenuDrawer menuDrawer;
    //直播介绍webView
    private PLVSafeWebView descWebView;

    //聊天图片查看fragment
    private PLVChatImageViewerFragment chatImageViewerFragment;

    //模糊背景view
    private PLVBlurView blurView;
    private Disposable updateBlurViewDisposable;

    //view
    private TextView plvlsChannelInfoChannelNameTv;
    private TextView plvlsChannelInfoStartTimeTv;
    private TextView plvlsChannelInfoChannelIdTv;
    private RelativeLayout plvlsChannelInfoParentLy;
    private ScrollView plvlsChannelInfoSv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSChannelInfoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSChannelInfoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSChannelInfoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        observeLiveRoomData();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_channel_info_layout, this);

        plvlsChannelInfoChannelNameTv = findViewById(R.id.plvls_channel_info_channel_name_tv);
        plvlsChannelInfoStartTimeTv = findViewById(R.id.plvls_channel_info_start_time_tv);
        plvlsChannelInfoChannelIdTv = findViewById(R.id.plvls_channel_info_channel_id_tv);
        plvlsChannelInfoParentLy = findViewById(R.id.plvls_channel_info_parent_ly);
        plvlsChannelInfoSv = findViewById(R.id.plvls_channel_info_sv);

        blurView = findViewById(R.id.blur_ly);
        PLVBlurUtils.initBlurView(blurView);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="布局控制">
    public void open() {
        final int landscapeHeight = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_FULLSCREEN);
            menuDrawer.setMenuSize((int) (landscapeHeight * 0.75));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                }
            });
            plvlsChannelInfoSv.post(new Runnable() {
                @Override
                public void run() {
                    menuDrawer.setDragAreaMenuBottom((int) (plvlsChannelInfoSv.getTop() + landscapeHeight * 0.25));
                }
            });
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public boolean onBackPressed() {
        if (chatImageViewerFragment != null && chatImageViewerFragment.isVisible()) {
            chatImageViewerFragment.hide();
            return true;
        }
        if (descWebView != null && descWebView.canGoBack()) {
            descWebView.goBack();
            return true;
        }
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        close();
        //销毁webView
        if (descWebView != null) {
            descWebView.destroy();
            descWebView = null;
        }
        stopUpdateBlurViewTimer();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播介绍内容 - 数据处理，webView加载">
    private void setDescContent(PolyvLiveClassDetailVO.DataBean dataBean) {
        if (dataBean == null) {
            return;
        }
        for (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : dataBean.getChannelMenus()) {
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
        content = PLVWebViewContentUtils.toWebViewContent(content, "#CFD1D6");
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
            descWebView.addJavascriptInterface(new ScriptInterface(), "imagelistner");
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);/*nest wrap*/
            rlp.addRule(RelativeLayout.BELOW, R.id.plvls_channel_info_top_ly);
            rlp.topMargin = ConvertUtils.dp2px(8);
            rlp.leftMargin = ConvertUtils.dp2px(24);
            descWebView.setLayoutParams(rlp);
            plvlsChannelInfoParentLy.addView(descWebView);
            PLVWebViewHelper.initWebView(getContext(), descWebView);
            descWebView.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
        } else {
            if (descWebView.getParent() != null) {
                ((ViewGroup) descWebView.getParent()).removeView(descWebView);
            }
            plvlsChannelInfoParentLy.addView(descWebView);
            descWebView.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="定时更新模糊背景view">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        blurView.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听直播详情数据">
    private void observeLiveRoomData() {
        //监听 直播间数据管理器对象中的直播详情数据变化
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> statefulData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (statefulData == null || !statefulData.isSuccess()) {
                    return;
                }
                PolyvLiveClassDetailVO liveClassDetailVO = statefulData.getData();
                if (liveClassDetailVO == null || liveClassDetailVO.getData() == null) {
                    return;
                }
                PolyvLiveClassDetailVO.DataBean dataBean = liveClassDetailVO.getData();
                String channelName = dataBean.getName();//频道名称
                String liveStartTime = StringUtils.isEmpty(dataBean.getStartTime()) ? "无" : dataBean.getStartTime();//直播开始时间
                String channelId = dataBean.getChannelId() + "";//频道id

                plvlsChannelInfoChannelNameTv.setText(channelName);
                plvlsChannelInfoStartTimeTv.setText(liveStartTime);
                plvlsChannelInfoChannelIdTv.setText(channelId);

                //设置直播介绍webView加载的内容
                setDescContent(dataBean);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - JS通信接口">
    // js通信接口
    private class ScriptInterface {
        private Map<String, PLVBaseViewData<PLVBaseEvent>> imgDataMap = new HashMap<>();

        @JavascriptInterface
        public void openImage(final String img) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (!imgDataMap.containsKey(img)) {
                        PLVChatQuoteVO.ImageBean imageBean = new PLVChatQuoteVO.ImageBean();
                        imageBean.setUrl(img);
                        PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                        chatQuoteVO.setImage(imageBean);
                        PLVSpeakEvent speakEvent = new PLVSpeakEvent();
                        speakEvent.setQuote(chatQuoteVO);
                        PLVBaseViewData<PLVBaseEvent> selData = new PLVBaseViewData<PLVBaseEvent>(speakEvent, PLVBaseViewData.ITEMTYPE_UNDEFINED);
                        imgDataMap.put(img, selData);
                    }
                    chatImageViewerFragment = PLVChatImageViewerFragment.show((AppCompatActivity) getContext(), imgDataMap.get(img), Window.ID_ANDROID_CONTENT);
                }
            });
        }
    }
    // </editor-fold>
}
