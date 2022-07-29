package com.easefun.polyv.streameralone.modules.streamer;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.streameralone.R;

import java.util.List;

public class PLVSAStreamerFullscreenLayout extends RelativeLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int PLV_EXIT_FULLSCREEN_TIP_COUNT = 2;
    private static final int PLV_ENTER_FULLSCREEN_TIP_COUNT = 1;

    private PLVSwitchViewAnchorLayout plvsaStreamerFullscreenView;
    private ImageView plvsaStreamerExitFullscreenIv;
    //item切换全屏的切换器
    private final PLVViewSwitcher fullscreenSwitcher = new PLVViewSwitcher();
    //全屏的用户
    private PLVLinkMicItemDataBean linkmicItem;

    private OnViewActionListener listener;

    private int exitFullscreenTipCount = 0;
    private int enterFullscreenTipCount = 0;

    private boolean isFullscreened = false;

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStreamerFullscreenLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStreamerFullscreenLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStreamerFullscreenLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_streamer_fullscreen_layout, this);

        plvsaStreamerFullscreenView = findViewById(R.id.plvsa_streamer_fullscreen_view);
        plvsaStreamerExitFullscreenIv = findViewById(R.id.plvsa_streamer_exit_fullscreen_iv);

        plvsaStreamerFullscreenView.setOnClickListener(this);
        plvsaStreamerExitFullscreenIv.setOnClickListener(this);

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public PLVSwitchViewAnchorLayout getAnchorLayout() {
        return plvsaStreamerFullscreenView;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.listener = listener;
    }

    public void changeViewToFullscreen(PLVSwitchViewAnchorLayout switchViewAnchorLayout, @NonNull PLVLinkMicItemDataBean linkmicItem) {
        if (isFullScreened()) {
            return;
        }
        this.linkmicItem = linkmicItem;
        linkmicItem.setFullScreen(true);
        changeSurfaceViewOnZMediaOverlay(switchViewAnchorLayout, true);
        fullscreenSwitcher.registerSwitchView(switchViewAnchorLayout, plvsaStreamerFullscreenView);
        fullscreenSwitcher.switchView();
        setVisibility(View.VISIBLE);
        isFullscreened = true;

        if(exitFullscreenTipCount < PLV_EXIT_FULLSCREEN_TIP_COUNT){
            exitFullscreenTipCount++;
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plvsa_streamer_double_click_exit_fullscreen)
                    .show();
        }

    }

    public void exitFullscreen() {
        if(!isFullScreened()){
            return;
        }
        linkmicItem.setFullScreen(false);
        changeSurfaceViewOnZMediaOverlay(plvsaStreamerFullscreenView, false);

        if(listener != null){
            listener.onExitFullscreen(linkmicItem, fullscreenSwitcher);
        }
        if (fullscreenSwitcher.isViewSwitched()) {
            fullscreenSwitcher.switchView();
        }
        isFullscreened = false;
        setVisibility(View.INVISIBLE);
        linkmicItem = null;

        if(enterFullscreenTipCount < PLV_ENTER_FULLSCREEN_TIP_COUNT){
            enterFullscreenTipCount++;
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plvsa_streamer_double_click_enter_fullscreen)
                    .show();
        }
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    public boolean isFullScreened(){
        return isFullscreened;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="推流 MVP - View">

    private IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersLeave(dataBeanList);

            //如果全屏的用户下麦了，退出全屏
            if (linkmicItem != null && linkmicItem.getUserId() != null) {
                for (PLVLinkMicItemDataBean leaveItem : dataBeanList) {
                    if (linkmicItem.getUserId().equals(leaveItem.getUserId())) {
                        exitFullscreen();
                        linkmicItem = null;
                        return;
                    }
                }
            }
        }

        @Override
        public void onStreamLiveStatusChanged(boolean isLive) {
            super.onStreamLiveStatusChanged(isLive);
            if(!isLive){
                exitFullscreen();
                linkmicItem = null;
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">

    private void changeSurfaceViewOnZMediaOverlay(ViewGroup viewGroup, boolean zOrderMediaOverlay){
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if(viewGroup.getChildAt(i) instanceof ViewGroup){
                changeSurfaceViewOnZMediaOverlay((ViewGroup) viewGroup.getChildAt(i), zOrderMediaOverlay);
            } else if (viewGroup.getChildAt(i) instanceof SurfaceView) {
                SurfaceView renderView = (SurfaceView) viewGroup.getChildAt(i);
                renderView.setZOrderMediaOverlay(zOrderMediaOverlay);
            }
        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="触摸点击事件">

    private final PointF downPoint = new PointF();
    private final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (linkmicItem != null && listener != null) {
                final float scaleFactor = detector.getScaleFactor();
                listener.onScaleStreamerView(linkmicItem, scaleFactor);
            }
            return true;
        }
    });

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        scaleGestureDetector.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downPoint.x = ev.getX();
                downPoint.y = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(ev.getX() - downPoint.x) > 1 || Math.abs(ev.getY() - downPoint.y) > 1) {
                    return true;
                }
                break;
            default:
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.plvsa_streamer_fullscreen_view) {
            if (!PLVDebounceClicker.tryClick(this, 800)) {
                exitFullscreen();
            }
        } else if (v.getId() == R.id.plvsa_streamer_exit_fullscreen_iv) {
            exitFullscreen();
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="view 交互事件监听">

    /**
     * view交互事件监听器
     */
    public interface OnViewActionListener {
        /**
         * 缩放推流画面
         */
        void onScaleStreamerView(PLVLinkMicItemDataBean linkMicItemDataBean, float scaleFactor);

        /**
         * 退出全屏
         */
        void onExitFullscreen(PLVLinkMicItemDataBean linkmicItem, PLVViewSwitcher fullscreenSwitcher);
    }
    // </editor-fold >


}
