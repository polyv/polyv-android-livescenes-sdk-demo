package com.easefun.polyv.streameralone.modules.streamer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.ui.widget.floating.PLVFloatingWindowManager;
import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.scenes.PLVSAStreamerAloneActivity;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.lang.ref.WeakReference;

/**
 * 纯视频开播悬浮窗，系统级别悬浮窗，需要申请悬浮窗权限。
 * 用于显示屏幕共享状态，仅在后台时显示
 */
public class PLVSAStreamerFloatWindow implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private Context context;

    private View plvsaStreamerWindowRoot;
    private ImageView plvsaStreamerWindowFold;
    private ImageView plvsaStreamerWindowStatusIv;
    private TextView plvsaStreamerWindowStatusTv;
    private View plvsaStreamerWindowContentView;

    private HomeKeyEventBroadCastReceiver receiver;

    View view;

    private boolean isNoNetWork = false;

    //是否展开
    private boolean isExpand = false;
    //是否正在屏幕共享
    private boolean isSharing;

    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    private Handler handler;
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造方法/初始化">
    public PLVSAStreamerFloatWindow(Context context) {
        this.context = context;
        handler = new Handler(context.getMainLooper());

        view = LayoutInflater.from(context).inflate(R.layout.plvsa_widget_floating_screen_share, null, false);

        PLVFloatingWindowManager.getInstance().createNewWindow((Activity) context)
                .setIsSystemWindow(true)
                .setContentView(view)
                .setSize(ViewGroup.LayoutParams.WRAP_CONTENT, ConvertUtils.dp2px(72))
                .setFloatLocation(ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(98), (int) (ScreenUtils.getScreenHeight() / 2.5))
                .setShowType(PLVFloatingEnums.ShowType.SHOW_ONLY_BACKGROUND)
                .setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType.AUTO_MOVE_TO_RIGHT)
                .build();

        plvsaStreamerWindowRoot = view.findViewById(R.id.plvsa_streamer_window_root);
        plvsaStreamerWindowFold = view.findViewById(R.id.plvsa_streamer_window_fold);
        plvsaStreamerWindowStatusIv = view.findViewById(R.id.plvsa_streamer_window_status_iv);
        plvsaStreamerWindowStatusTv = view.findViewById(R.id.plvsa_streamer_window_status_tv);
        plvsaStreamerWindowContentView = view.findViewById(R.id.plvsa_streamer_window_content);

        plvsaStreamerWindowFold.setSelected(false);
        plvsaStreamerWindowRoot.setSelected(false);
        plvsaStreamerWindowStatusIv.setSelected(false);

        plvsaStreamerWindowFold.setOnClickListener(this);
        plvsaStreamerWindowStatusIv.setOnClickListener(this);
        plvsaStreamerWindowStatusTv.setOnClickListener(this);

        receiver = new HomeKeyEventBroadCastReceiver(new WeakReference<>(this));
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        context.registerReceiver(receiver, homeFilter);


    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void show() {
        PLVFloatingWindowManager.getInstance().show((Activity) context);
    }

    public void close() {
        PLVFloatingWindowManager.getInstance().hide();
    }

    public void destroy() {
        PLVFloatingWindowManager.getInstance().destroy();
        context.unregisterReceiver(receiver);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="streamerView实现">

    public PLVAbsStreamerView getStreamerView() {
        return streamerView;
    }

    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            super.setPresenter(presenter);
            streamerPresenter = presenter;
        }

        @Override
        public void onNetworkQuality(int quality) {
            //处理断网后的回调错误
            if (!PLVNetworkUtils.isConnected(context)) {
                quality = PLVLinkMicConstant.NetQuality.NET_QUALITY_NO_CONNECTION;
            }

            if (quality == PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                if (!isNoNetWork) {
                    updateWhenStreamStop();
                    autoExpand(true, 0);
                    isNoNetWork = true;
                }
            } else {
                isNoNetWork = false;
                updateWhenShareScering();
            }
        }

        @Override
        public void onScreenShareChange(int position, boolean isShare, int extra) {
            super.onScreenShareChange(position, isShare, extra);
            //是否正在屏幕共享
            isSharing = streamerPresenter.isScreenSharing();
        }
    };
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.plvsa_streamer_window_fold) {
            if (isExpand) {
                expand(plvsaStreamerWindowContentView);
            } else {
                collapse(plvsaStreamerWindowContentView);
            }
            isExpand = !isExpand;
        } else if (v.getId() == R.id.plvsa_streamer_window_status_iv ||
                v.getId() == R.id.plvsa_streamer_window_status_tv) {
            exitScreencast();
        }

    }
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="功能方法">
    private void exitScreencast() {


        Intent intent = new Intent(context, PLVSAStreamerAloneActivity.class);
        //service context 6.0↓(test 7.0~9.0 no error) only use Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, report： Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                streamerPresenter.exitShareScreen();
                PLVFloatingWindowManager.getInstance().hide();
            }
        }, 300);

    }


    private void expand(final View view) {
        final int viewWidth = ConvertUtils.dp2px(74);
        view.getLayoutParams().width = 0;
        view.setVisibility(View.VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                Point floatLocation = PLVFloatingWindowManager.getInstance().getFloatLocation();
                int width = (int) (viewWidth * interpolatedTime);
                if (floatLocation != null) {
                    PLVFloatingWindowManager.getInstance().updateFloatLocation(floatLocation.x - width, floatLocation.y);
                }
                view.getLayoutParams().width = width;
                view.requestLayout();
            }
        };
        animation.setDuration(200);
        animation.setInterpolator(new FastOutLinearInInterpolator());
        view.startAnimation(animation);
        plvsaStreamerWindowFold.setSelected(false);
    }

    private void collapse(final View view) {
        final int viewWidth = ConvertUtils.dp2px(74);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.INVISIBLE);
                }
                Point floatLocation = PLVFloatingWindowManager.getInstance().getFloatLocation();
                int interpolatedWidth = (int) (viewWidth * interpolatedTime);
                if (floatLocation != null) {
                    PLVFloatingWindowManager.getInstance().updateFloatLocation(floatLocation.x + interpolatedWidth, floatLocation.y);
                }
                view.getLayoutParams().width = viewWidth - interpolatedWidth;
                view.requestLayout();

            }
        };
        animation.setDuration(200);
        animation.setInterpolator(new FastOutLinearInInterpolator());
        view.startAnimation(animation);
        plvsaStreamerWindowFold.setSelected(true);
    }


    private void autoExpand(final boolean expand, int delayMillis) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (expand) {
                    expand(plvsaStreamerWindowContentView);
                } else {
                    collapse(plvsaStreamerWindowContentView);
                }
                isExpand = !isExpand;
            }
        }, delayMillis);
    }

    private void updateWhenShareScering() {
        plvsaStreamerWindowRoot.setSelected(false);
        plvsaStreamerWindowStatusIv.setSelected(false);
        plvsaStreamerWindowStatusTv.setText("退出投屏");
    }

    private void updateWhenStreamStop() {
        plvsaStreamerWindowRoot.setSelected(true);
        plvsaStreamerWindowStatusIv.setSelected(false);
        plvsaStreamerWindowStatusTv.setText("直播已中断");
    }

    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="home 监听">
    public static class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
        static final String SYSTEM_REASON = "reason";
        static final String SYSTEM_HOME_KEY = "homekey";
        static final String SYSTEM_HOME_KEY_LONG = "recentapps";
        static final String SYSTEM_HOME__MIUI_GESTURE = "fs_gesture";//小米全面屏返回桌面触发事件

        private WeakReference<PLVSAStreamerFloatWindow> window;

        public HomeKeyEventBroadCastReceiver(WeakReference<PLVSAStreamerFloatWindow> window) {
            this.window = window;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                final PLVSAStreamerFloatWindow floatWindow = window.get();
                if (floatWindow == null) {
                    return;
                }
                if (reason != null) {
                    if (reason.equals(SYSTEM_HOME_KEY) || reason.equals(SYSTEM_HOME_KEY_LONG)
                            || reason.equals(SYSTEM_HOME__MIUI_GESTURE)) {
                        floatWindow.handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (floatWindow.isSharing) {
                                    if (!floatWindow.isExpand) {
                                        floatWindow.autoExpand(true, 0);
                                    }
                                    floatWindow.autoExpand(false, 3000);
                                }
                            }
                        }, 300);
                    }
                }
            }
        }
    }
    // </editor-fold >


}




