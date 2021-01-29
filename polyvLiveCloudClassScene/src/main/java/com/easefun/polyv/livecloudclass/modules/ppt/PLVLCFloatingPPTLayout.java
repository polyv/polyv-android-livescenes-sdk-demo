package com.easefun.polyv.livecloudclass.modules.ppt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.ppt.contract.IPLVLiveFloatingContract;
import com.easefun.polyv.livecommon.module.modules.ppt.presenter.PLVLiveFloatingPresenter;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVTouchFloatingView;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * date: 2020/8/6
 * author: hwj
 * description: 悬浮PPT布局，包裹着[PLVLCPPTView]，并将其变成可悬浮移动的小窗
 */
public class PLVLCFloatingPPTLayout extends FrameLayout implements IPLVLiveFloatingContract.IPLVLiveFloatingView, IPLVLCFloatingPPTLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLCFloatingPPTLayout.class.getSimpleName();

    //悬浮窗竖屏尺寸
    private static final int DP_FLOATING_PPT_WIDTH_PORT = 150;
    private static final int DP_FLOATING_PPT_HEIGHT_PORT = 85;
    //悬浮窗横屏尺寸
    private static final int DP_FLOATING_PPT_WIDTH_LAND = 202;
    private static final int DP_FLOATING_PPT_HEIGHT_LAND = 114;
    //初始化位置->竖屏margin top
    private static final int DP_ORIGIN_MARGIN_TOP_PORTRAIT = 373;
    //初始化位置->横屏margin top
    private static final int DP_ORIGIN_MARGIN_TOP_LANDSCAPE = 16;
    //初始化位置->横屏margin right
    private static final int DP_ORIGIN_MARGIN_RIGHT_LANDSCAPE = 16;

    /**** View ****/
    //ppt
    private IPLVLCPPTView pptView;
    //悬浮窗  [PLVTouchFloatingView]是在他的父布局中可以移动，因此他的父布局要铺满全屏
    private PLVTouchFloatingView floatingView;
    //讲师名字
    private TextView tvTeacherName;
    //关闭按钮
    private ImageView ivClose;
    //要切换出去的SwitchView
    private PLVSwitchViewAnchorLayout switchViewAnchorLayout;

    //服务端的ppt开关
    private boolean isServerEnablePPT = false;

    //Listener
    private OnClickListener onFloatingViewClickListener;
    private IPLVOnClickCloseFloatingView onClickCloseListener;

    //Presenter
    private IPLVLiveFloatingContract.IPLVLiveFloatingPresenter presenter;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCFloatingPPTLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCFloatingPPTLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCFloatingPPTLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initData();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">

    //初始化View
    private void initView() {
        //add View
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_ppt_floating_layout, this, true);
        //find view
        pptView = findViewById(R.id.plvlc_ppt_ppt_view);
        floatingView = findViewById(R.id.plvlc_ppt_floating_view);
        switchViewAnchorLayout = findViewById(R.id.plvlc_ppt_switch_view_anchor);
        tvTeacherName = findViewById(R.id.plvlc_ppt_teacher_name);
        ivClose = findViewById(R.id.plvlc_ppt_iv_close);

        //设置初始化位置
        int screenWidth = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        int screenHeight = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        floatingView.setInitLocation(
                screenWidth - PLVScreenUtils.dip2px(DP_FLOATING_PPT_WIDTH_PORT),
                PLVScreenUtils.dip2px(DP_ORIGIN_MARGIN_TOP_PORTRAIT),
                screenHeight - PLVScreenUtils.dip2px(DP_FLOATING_PPT_WIDTH_LAND) - PLVScreenUtils.dip2px(DP_ORIGIN_MARGIN_RIGHT_LANDSCAPE),
                PLVScreenUtils.dip2px(DP_ORIGIN_MARGIN_TOP_LANDSCAPE)
        );
        floatingView.setIsInterceptTouchEvent(false);
        //设置初始化屏幕方向
        if (PLVScreenUtils.isLandscape(getContext())) {
            setLandscape();
        } else {
            setPortrait();
        }

        //设置点击监听器
        setClickListener();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    //初始化数据
    private void initData() {
        presenter = new PLVLiveFloatingPresenter();
        presenter.init(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 外部直接调用的方法">
    @Override
    public void setServerEnablePPT(boolean enable) {
        isServerEnablePPT = enable;
        if (enable) {
            show();
        } else {
            hide();
        }
    }

    @Override
    public void show() {
        PLVCommonLog.d(TAG, "show");
        if (isServerEnablePPT && !PolyvLinkMicConfig.getInstance().isPureRtcWatchEnabled()) {
            setVisibility(VISIBLE);
        }
    }

    @Override
    public void hide() {
        PLVCommonLog.d(TAG, "hide");
        setVisibility(GONE);
    }

    @Override
    public boolean isPPTInFloatingLayout() {
        return floatingView.findViewById(R.id.plvlc_ppt_ppt_view) != null;
    }

    @Override
    public IPLVLCPPTView getPPTView() {
        if (pptView == null) {
            PLVCommonLog.w(TAG, "getPPTView return null");
        }
        return pptView;
    }

    @Override
    public void setOnFloatingViewClickListener(OnClickListener li) {
        this.onFloatingViewClickListener = li;
    }

    @Override
    public void setOnClickCloseListener(IPLVOnClickCloseFloatingView onClickCloseListener) {
        this.onClickCloseListener = onClickCloseListener;
    }

    @Override
    public PLVSwitchViewAnchorLayout getPPTSwitchView() {
        return switchViewAnchorLayout;
    }

    @Override
    public void destroy() {
        presenter.destroy();
        pptView.destroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPLVLiveFloatingView实现">
    @Override
    public void updateTeacherInfo(String actor, String nick) {
        String name = actor + "-" + nick;
        tvTeacherName.setText(name);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理屏幕旋转">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscape();
        } else {
            setPortrait();
        }
    }

    private void setLandscape() {
        ViewGroup.LayoutParams lpOfFloating = floatingView.getLayoutParams();
        lpOfFloating.width = PLVScreenUtils.dip2px(DP_FLOATING_PPT_WIDTH_LAND);
        lpOfFloating.height = PLVScreenUtils.dip2px(DP_FLOATING_PPT_HEIGHT_LAND);
        floatingView.setLayoutParams(lpOfFloating);

        ivClose.setVisibility(GONE);
    }

    private void setPortrait() {
        ViewGroup.LayoutParams lpOfFloating = floatingView.getLayoutParams();
        lpOfFloating.width = PLVScreenUtils.dip2px(DP_FLOATING_PPT_WIDTH_PORT);
        lpOfFloating.height = PLVScreenUtils.dip2px(DP_FLOATING_PPT_HEIGHT_PORT);
        floatingView.setLayoutParams(lpOfFloating);

        ivClose.setVisibility(VISIBLE);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onClick">
    @SuppressLint("ClickableViewAccessibility")
    private void setClickListener() {
        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickCloseListener != null) {
                    onClickCloseListener.onClickCloseFloatingView();
                }
            }
        });

        //设置悬浮窗点击事件监听器，让外界监听悬浮窗的点击事件。注意，不可设置OnClickListener，否则将拦截事件，导致PPT收不到事件，无法做出画笔动作。
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (onFloatingViewClickListener != null) {
                    onFloatingViewClickListener.onClick(floatingView);
                }
                return true;
            }
        });
        floatingView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }
    // </editor-fold>

}
