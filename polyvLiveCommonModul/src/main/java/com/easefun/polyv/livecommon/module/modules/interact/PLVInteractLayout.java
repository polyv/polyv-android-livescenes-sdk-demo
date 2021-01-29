package com.easefun.polyv.livecommon.module.modules.interact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.interact.app.PLVInteractAnswer;
import com.easefun.polyv.livecommon.module.modules.interact.app.PLVInteractBulletin;
import com.easefun.polyv.livecommon.module.modules.interact.app.PLVInteractCommonControl;
import com.easefun.polyv.livecommon.module.modules.interact.app.PLVInteractLottery;
import com.easefun.polyv.livecommon.module.modules.interact.app.PLVInteractQuestionnaire;
import com.easefun.polyv.livecommon.module.modules.interact.app.PLVInteractSignIn;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livescenes.feature.interact.PLVInteractAppAbs;
import com.easefun.polyv.livescenes.feature.interact.PLVInteractWebView;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * date: 2020/8/31
 * author: HWilliamgo
 * description: 互动应用布局。是多个场景都可以共用的一个功能模块，
 * 包含了如下互动应用：答题，问卷，公告，抽奖，签到。
 */
public class PLVInteractLayout extends FrameLayout implements IPLVInteractLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final boolean LOAD_WEB_URL = true;

    @Nullable
    private PLVInteractWebView interactWebView;
    private LinearLayout ll;
    private ImageView ivClose;
    private ScrollView scroll;
    private FrameLayout flContainer;

    private PLVInteractLottery interactLottery;
    private PLVInteractBulletin interactBulletin;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVInteractLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVInteractLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVInteractLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_interact_layout, this, true);
        interactWebView = findViewById(R.id.plvlc_interact_web);
        ll = findViewById(R.id.plvlc_interact_ll);
        ivClose = findViewById(R.id.plvlc_interact_iv_close);
        scroll = findViewById(R.id.plvlc_interact_scroll);
        flContainer = findViewById(R.id.plvlc_interact_fl_container);

        setClickListener();
        handleKeyboardOrientation();
    }

    private void handleKeyboardOrientation() {
        Context context = getContext();
        if (context instanceof Activity) {
            final Activity activity = (Activity) getContext();
            post(new Runnable() {
                @Override
                public void run() {
                    new PolyvAnswerKeyboardHelper(activity);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 1. 外部直接调用的方法">
    @Override
    public void init() {
        if (interactWebView == null) {
            return;
        }
        //加载互动应用资源
        if (LOAD_WEB_URL) {
            interactWebView.loadWeb();
        } else {
            interactWebView.loadUrl("file:///android_asset/index.html");
        }

        //设置要显示的互动应用
        setupInteractApp();
    }

    @Override
    public void showBulletin() {
        if (interactBulletin != null) {
            interactBulletin.showBulletin();
        }
    }

    @Override
    public void destroy() {
        if (interactWebView != null) {
            interactWebView.removeAllViews();
            ViewParent viewParent=interactWebView.getParent();
            if (viewParent instanceof ViewGroup){
                ViewGroup viewGroup= (ViewGroup) viewParent;
                viewGroup.removeView(interactWebView);
            }
            interactWebView.destroy();
            interactWebView = null;
        }
    }

    @Override
    public boolean onBackPress() {
        if (interactLottery.onBackPress()) {
            return true;
        }
        if (isVisible()) {
            hide();
            return true;
        } else {
            return false;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="添加互动应用">
    private void setupInteractApp() {
        if (interactWebView == null) {
            return;
        }
        //通用控制
        PLVInteractCommonControl commonControl = new PLVInteractCommonControl();
        commonControl.setOnInteractCommonControlListener(new PLVInteractCommonControl.OnInteractCommonControlListener() {
            @Override
            public void onWebViewLoadFinished() {
                ivClose.setVisibility(INVISIBLE);
            }

            @Override
            public void onWebViewHide() {
                hide();
            }
        });
        commonControl.setOnShowListener(new PLVInteractAppAbs.OnInteractAppShowListener() {
            @Override
            public void onShow() {
                show();
            }
        });

        //互动答题
        PLVInteractAnswer interactAnswer = new PLVInteractAnswer();
        interactAnswer.setOnShowListener(new PLVInteractAppAbs.OnInteractAppShowListener() {
            @Override
            public void onShow() {
                lockToPortrait();
                show();
            }
        });

        //互动问卷调查
        PLVInteractQuestionnaire interactQuestionnaire = new PLVInteractQuestionnaire();
        interactQuestionnaire.setOnShowListener(new PLVInteractAppAbs.OnInteractAppShowListener() {
            @Override
            public void onShow() {
                lockToPortrait();
                show();
            }
        });

        //互动抽奖
        interactLottery = new PLVInteractLottery(commonControl);
        interactLottery.setOnShowListener(new PLVInteractAppAbs.OnInteractAppShowListener() {
            @Override
            public void onShow() {
                lockToPortrait();
                show();
            }
        });

        //互动签到
        PLVInteractSignIn interactSignIn = new PLVInteractSignIn();
        interactSignIn.setOnShowListener(new PLVInteractAppAbs.OnInteractAppShowListener() {
            @Override
            public void onShow() {
                show();
            }
        });

        //互动公告
        interactBulletin = new PLVInteractBulletin();
        interactBulletin.setOnShowListener(new PLVInteractAppAbs.OnInteractAppShowListener() {
            @Override
            public void onShow() {
                show();
            }
        });
        interactBulletin.setOnPLVInteractBulletinListener(new PLVInteractBulletin.OnPLVInteractBulletinListener() {
            @Override
            public void onBulletinDelete() {
                hide();
            }
        });

        interactWebView.addInteractApp(commonControl);
        interactWebView.addInteractApp(interactAnswer);
        interactWebView.addInteractApp(interactQuestionnaire);
        interactWebView.addInteractApp(interactLottery);
        interactWebView.addInteractApp(interactSignIn);
        interactWebView.addInteractApp(interactBulletin);

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制显示和隐藏">
    private void show() {
        if (interactWebView == null) {
            return;
        }

        //隐藏软键盘
        KeyboardUtils.hideSoftInput(this);
        //请求焦点
        interactWebView.requestFocus();
        //显示
        flContainer.setVisibility(VISIBLE);
    }

    private boolean isVisible() {
        return flContainer.isShown();
    }

    private void hide() {
        flContainer.setVisibility(GONE);
        //隐藏的时候解锁屏幕方向锁定
        PLVOrientationManager.getInstance().unlockOrientation();
    }

    /**
     * 锁定到竖屏
     */
    private void lockToPortrait() {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity == null) {
            return;
        }
        if (topActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            PLVOrientationManager.getInstance().unlockOrientation();
            PLVOrientationManager.getInstance().setPortrait(topActivity);
        }
        PLVOrientationManager.getInstance().lockOrientation();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onClick">
    private void setClickListener() {
        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPress();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - PolyvAnswerKeyboardHelper">

    /**
     * 横屏全屏键盘遮挡帮助类
     * 作用：检测到键盘弹起时，将互动应用的输入框上移到可见区域，不要被键盘挡住
     * 需要用到的互动应用：调查问卷，抽奖
     */
    private class PolyvAnswerKeyboardHelper {

        private View mChildOfContent;

        private int usableHeightPrevious;

        private View bottomPlaceHolder;

        private PolyvAnswerKeyboardHelper(Activity activity) {
            FrameLayout content = activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            });
        }

        @SuppressLint("ClickableViewAccessibility")
        private void possiblyResizeChildOfContent() {
            //当不可见时，不处理任何键盘事件
            if (!isVisible()) {
                return;
            }

            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != usableHeightPrevious) {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - usableHeightNow;
                if (heightDifference > (usableHeightSansKeyboard / 4)) {
                    scroll.setOnTouchListener(null);
                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    //键盘弹起
                    if (ScreenUtils.isPortrait()) {
                        return;
                    }
                    if (bottomPlaceHolder == null) {
                        bottomPlaceHolder = new View(getContext());
                    }
                    if (bottomPlaceHolder.getParent() == null) {
                        ll.addView(bottomPlaceHolder, ViewGroup.LayoutParams.MATCH_PARENT, heightDifference - 100);
                    }
                    ll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                } else {
                    //键盘收缩
                    if (ll.indexOfChild(bottomPlaceHolder) > 0) {
                        ll.removeView(bottomPlaceHolder);
                    }
                    scroll.setOnTouchListener(new OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });
                }
                usableHeightPrevious = usableHeightNow;
            }
        }

        //计算剩余高度
        private int computeUsableHeight() {
            Rect r = new Rect();
            mChildOfContent.getWindowVisibleDisplayFrame(r);
            return (r.bottom - r.top);// 全屏模式下： return r.bottom
        }
    }
// </editor-fold>

}
