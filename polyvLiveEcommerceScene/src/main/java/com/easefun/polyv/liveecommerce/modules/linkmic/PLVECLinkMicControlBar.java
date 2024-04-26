package com.easefun.polyv.liveecommerce.modules.linkmic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.utils.PLVDialogFactory;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.ui.widget.PLVNoConsumeTouchEventButton;
import com.easefun.polyv.livecommon.ui.widget.PLVTouchFloatingView;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.linkmic.widget.PLVECLinkMicRingButton;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.log.IPLVLinkMicTraceLogSender;
import com.plv.linkmic.log.PLVLinkMicTraceLogSender;
import com.plv.livescenes.log.linkmic.PLVLinkMicELog;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * date: 2020/8/14
 * author: HWilliamgo
 * description: 连麦控制条布局
 */
public class PLVECLinkMicControlBar extends FrameLayout implements IPLVECLinkMicControlBar {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVECLinkMicControlBar.class.getSimpleName();
    //连麦打开关闭动画持续时长
    private static final int DURATION_MS_LINK_MIC_OPEN_OFF = 300;
    //延迟后自动隐藏
    private static final int DELAY_AUTO_HIDE_WHEN_NOT_JOINED = 5000;
    private static final boolean AUTO_HIDE_WHEN_NOT_JOINED = true;
    private static final int DELAY_AUTO_HIDE_WHEN_JOINED = 3000;
    private static final boolean AUTO_HIDE_WHEN_JOINED = true;
    //竖屏下初始位置的y偏移
    private static final int DP_ORIGIN_MARGIN_TOP_PORTRAIT = 466;
    //初始化位置->横屏margin top
    private static final int DP_ORIGIN_MARGIN_TOP_LANDSCAPE = 87;

    //View
    //--竖屏
    private PLVTouchFloatingView floatingViewPortraitRoot;
    private PLVECLinkMicRingButton btnRingActionPortrait;
    private TextView tvRequestTip;
    private PLVNoConsumeTouchEventButton btnCameraOpenPortrait;
    private PLVNoConsumeTouchEventButton btnCameraFrontBackPortrait;
    private PLVNoConsumeTouchEventButton btnMicrophoneOpenPortrait;
    private PLVNoConsumeTouchEventButton btnCollapsePortrait;
    private LinearLayout ll4BtnParent;

    //3个状态的长度
    //竖屏控制条收起时的最小长度，只显示一个设置按钮的长度
    private int smallStateWidthPortrait;
    //当点击请求上麦时，显示请求上麦按钮和一段文案的长度
    private int middleStateWidthPortrait;
    //当上麦成功后，显示所有的功能按钮的长度
    private int biggestStateWidthPortrait;

    //State
    private PLVLCLinkMicControllerState state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE;

    private boolean isCameraOpen = false;
    private boolean isCameraFront = true;
    private boolean isMicrophoneOpen = true;
    private boolean isPortrait;
    private boolean isAudioState = false;
    private boolean isTeacherOpenLinkMic = false;

    //Listener
    private OnPLCLinkMicControlBarListener onPLCLinkMicControlBarListener;

    //Disposable
    private Disposable autoHideDisposable;

    //网络连接广播接收器
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            if (PLVNetworkUtils.isConnected(context)) {
                PLVCommonLog.d(TAG, "net work connected");
            } else {
                PLVCommonLog.d(TAG, "net work disconnected");
                onNetworkDisconnected();
            }
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECLinkMicControlBar(Context context) {
        this(context, null);
    }

    public PLVECLinkMicControlBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLinkMicControlBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initBroadcastReceiver(context);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_linkmic_controller_layout, this, true);

        //竖屏浮窗
        floatingViewPortraitRoot = findViewById(R.id.plvec_linkmic_controller_floating_view_portrait_root);
        btnRingActionPortrait = findViewById(R.id.plvec_linkmic_controlBar_btn_ring_action);
        tvRequestTip = findViewById(R.id.plvec_linkmic_controlBar_tv_request_tip);
        btnCameraOpenPortrait = findViewById(R.id.plvec_linkmic_controlBar_btn_camera_open);
        btnCameraFrontBackPortrait = findViewById(R.id.plvec_linkmic_controlBar_btn_camera_front_back);
        btnMicrophoneOpenPortrait = findViewById(R.id.plvec_linkmic_controlBar_btn_microphone_open);
        btnCollapsePortrait = findViewById(R.id.plvec_linkmic_controlBar_btn_collapse);
        ll4BtnParent = findViewById(R.id.plvec_linkmic_controlBar_ll_4_btn_parent);

        //初始向右偏移并隐藏
        floatingViewPortraitRoot.setIsInterceptTouchEvent(false);
        floatingViewPortraitRoot.enableHorizontalDrag(false);

        //获取3个宽度
        post(new Runnable() {
            @Override
            public void run() {
                MarginLayoutParams btnSettingLP = (MarginLayoutParams) btnRingActionPortrait.getLayoutParams();
                smallStateWidthPortrait = btnSettingLP.leftMargin + btnRingActionPortrait.getWidth() + btnSettingLP.rightMargin;
                MarginLayoutParams tvRequestTipLp = (MarginLayoutParams) tvRequestTip.getLayoutParams();
                middleStateWidthPortrait = btnSettingLP.leftMargin + btnRingActionPortrait.getWidth() +
                        tvRequestTipLp.leftMargin + tvRequestTip.getWidth() + tvRequestTipLp.rightMargin;
                biggestStateWidthPortrait = floatingViewPortraitRoot.getWidth();

                floatingViewPortraitRoot.setTranslationX(biggestStateWidthPortrait);
            }
        });

        //悬浮窗中的子View与悬浮窗共享点击事件
        btnRingActionPortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnCollapsePortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnCameraFrontBackPortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnCameraOpenPortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnMicrophoneOpenPortrait.setShareTouchEventView(floatingViewPortraitRoot);

        //设置竖屏点击监听器
        setPortraitClickListener();
        //设置竖屏初始化位置
        floatingViewPortraitRoot.setInitLocation(0, PLVScreenUtils.dip2px(DP_ORIGIN_MARGIN_TOP_PORTRAIT), 0, PLVScreenUtils.dip2px(DP_ORIGIN_MARGIN_TOP_LANDSCAPE));

        //设置屏幕方向
        isPortrait = PLVScreenUtils.isPortrait(getContext());
        setOrientation(isPortrait);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化广播监听器">
    private void initBroadcastReceiver(final Context context) {
        context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            activity.getLifecycle().addObserver(new GenericLifecycleObserver() {
                @Override
                public void onStateChanged(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
                    if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                        context.unregisterReceiver(receiver);
                    }
                }
            });
        } else {
            PLVCommonLog.e(TAG, "context not instance of AppCompatActivity, in danger of leak broadcast receiver");
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 外部直接调用的方法">

    //设置监听器
    @Override
    public void setOnPLCLinkMicControlBarListener(OnPLCLinkMicControlBarListener onPLCLinkMicControlBarListener) {
        this.onPLCLinkMicControlBarListener = onPLCLinkMicControlBarListener;
    }

    //设置讲师开启或关闭连麦
    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        this.isTeacherOpenLinkMic = isTeacherOpenLinkMic;
        if (isJoinLinkMic()) {
            return;
        }
        if (isTeacherOpenLinkMic) {
            if (state != PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
                return;
            }
            state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN;

            //设置竖屏UI
            btnRingActionPortrait.setRingUpState();
            tvRequestTip.setVisibility(VISIBLE);
            if (isAudioState) {
                tvRequestTip.setText(R.string.plv_linkmic_tip_request_audio_link_mic);
            } else {
                tvRequestTip.setText(R.string.plv_linkmic_tip_request_video_link_mic);
            }
            ll4BtnParent.setVisibility(INVISIBLE);

            //设置屏幕方向的UI
            setOrientation(isPortrait);

            startAutoHideCountDown();
        } else {
            if (state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
                return;
            }
            state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE;
            stopAutoHideCountDown();
        }

        animateLinkMicOpenOrClose(isTeacherOpenLinkMic);
    }

    @Override
    public void setCameraOpenOrClose(boolean toOpen) {
        setBtnCameraOpenState(toOpen);
    }

    @Override
    public void setMicrophoneOpenOrClose(boolean toOpen) {
        setBtnMicrophoneOpenState(toOpen);
    }

    //设置上麦成功
    @Override
    public void setJoinLinkMicSuccess() {
        state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS;
        isCameraOpen = false;
        isCameraFront = true;
        isMicrophoneOpen = true;

        setOrientation(isPortrait);
        animateMoveToShowBiggestWidth();

        tvRequestTip.postDelayed(new Runnable() {
            @Override
            public void run() {
                //更新竖屏UI
                tvRequestTip.setVisibility(INVISIBLE);
                ll4BtnParent.setVisibility(VISIBLE);
                btnRingActionPortrait.setRingOffState();
            }
        }, DURATION_MS_LINK_MIC_OPEN_OFF);

        startAutoHideCountDown();
    }

    @Override
    public void updateIsAudioWidth(boolean isAudio) {
        if (isAudio) {
            btnCameraOpenPortrait.setVisibility(GONE);
            btnCameraFrontBackPortrait.setVisibility(GONE);
        } else {
            btnCameraOpenPortrait.setVisibility(VISIBLE);
            btnCameraFrontBackPortrait.setVisibility(VISIBLE);
        }
        //设置了音频模式后，竖屏下有两个按钮隐藏了，因此最大长度会变小，要重新获取最大长度。
        post(new Runnable() {
            @Override
            public void run() {
                biggestStateWidthPortrait = floatingViewPortraitRoot.getWidth();
            }
        });
    }

    @Override
    public void setAudioState(boolean isAudio) {
        isAudioState = isAudio;

        if (state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN || state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE) {
            if (isAudioState) {
                tvRequestTip.setText(R.string.plv_linkmic_tip_request_audio_link_mic);
            } else {
                tvRequestTip.setText(R.string.plv_linkmic_tip_request_video_link_mic);
            }
        }
    }

    //设置下麦
    @Override
    public void setLeaveLinkMic() {
        isCameraOpen = false;
        isCameraFront = true;
        isMicrophoneOpen = true;

        if (state != PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
            state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN;
            if (isTeacherOpenLinkMic) {
                animateMoveToShowMiddleWidth();
                startAutoHideCountDown();
            }
        }

        if (isAudioState) {
            tvRequestTip.setText(R.string.plv_linkmic_tip_request_audio_link_mic);
        } else {
            tvRequestTip.setText(R.string.plv_linkmic_tip_request_video_link_mic);
        }
        tvRequestTip.postDelayed(new Runnable() {
            @Override
            public void run() {
                //更新竖屏UI
                tvRequestTip.setVisibility(VISIBLE);
                btnRingActionPortrait.setRingUpState();
                ll4BtnParent.setVisibility(INVISIBLE);
            }
        }, DURATION_MS_LINK_MIC_OPEN_OFF);

        if (!isTeacherOpenLinkMic) {
            setIsTeacherOpenLinkMic(false);
        }
    }

    @Override
    public void updateLinkMicQueueOrder(int orderIndex) {
        if (orderIndex < 0) {
            return;
        }
        final String orderText;
        if (orderIndex < 50) {
            orderText = String.valueOf(orderIndex + 1);
        } else {
            orderText = "50+";
        }
        if (state == PLVLCLinkMicControllerState.STATE_REQUESTING_JOIN_LINK_MIC) {
            tvRequestTip.setText(new PLVSpannableStringBuilder(getContext().getString(R.string.plv_linkmic_tip_requesting_link_mic))
                    .appendExclude(PLVAppUtils.formatString(R.string.plv_linkmic_apply_pending, orderText), new AbsoluteSizeSpan(ConvertUtils.sp2px(12)) {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(PLVFormatUtils.parseColor("#99FFFFFF"));
                        }
                    }));
        }
    }

    @Override
    public void show() {
        PLVCommonLog.d(TAG, "show");
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        PLVCommonLog.d(TAG, "hide");
        setVisibility(INVISIBLE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="动画">
    //连麦打开或关闭动画
    private void animateLinkMicOpenOrClose(boolean open) {
        //悬浮窗横向移动动画
        if (open) {
            animateMoveToShowMiddleWidth();
        } else {
            animateMoveToHide();
        }

        //提示文字透明度动画
        tipGradientShowOrHide(open, DURATION_MS_LINK_MIC_OPEN_OFF);
    }

    private void animateMoveToShowBiggestWidth() {
        animateMove(0);
    }

    private void animateMoveToShowMiddleWidth() {
        //显示请求上麦按钮和一段文案的长度前，需要重置一下最大长度，防止音频下麦后，最大长度变短导致位移不正确
        updateIsAudioWidth(false);
        post(new Runnable() {
            @Override
            public void run() {
                biggestStateWidthPortrait = floatingViewPortraitRoot.getWidth();
                MarginLayoutParams btnSettingLP = (MarginLayoutParams) btnRingActionPortrait.getLayoutParams();
                MarginLayoutParams tvRequestTipLp = (MarginLayoutParams) tvRequestTip.getLayoutParams();
                middleStateWidthPortrait = btnSettingLP.leftMargin + btnRingActionPortrait.getWidth() +
                        tvRequestTipLp.leftMargin + tvRequestTip.getWidth() + tvRequestTipLp.rightMargin;
                animateMove((float) (biggestStateWidthPortrait - middleStateWidthPortrait));
            }
        });

    }

    private void animateMoveToShowSmallestWidth() {
        animateMove((float) (biggestStateWidthPortrait - smallStateWidthPortrait));
    }

    private void animateMoveToHide() {
        animateMove(biggestStateWidthPortrait);
    }

    private void animateMove(float translationX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(floatingViewPortraitRoot, "translationX",
                floatingViewPortraitRoot.getTranslationX(), translationX);
        animator.setDuration(DURATION_MS_LINK_MIC_OPEN_OFF);
        animator.start();
    }


    //提示文本渐变显隐
    private void tipGradientShowOrHide(boolean show, int duration) {
        ObjectAnimator objectAnimator;
        if (show) {
            tvRequestTip.setVisibility(VISIBLE);
            objectAnimator = ObjectAnimator.ofFloat(tvRequestTip, "alpha", 0, 1);
        } else {
            objectAnimator = ObjectAnimator.ofFloat(tvRequestTip, "alpha", 1, 0);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvRequestTip.setVisibility(INVISIBLE);
                    tvRequestTip.setAlpha(1);
                }
            });
        }
        objectAnimator.setDuration(duration);
        objectAnimator.start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="自动隐藏">
    //开始自动隐藏倒计时
    private void startAutoHideCountDown() {
        dispose(autoHideDisposable);
        int delay;
        if (state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN && AUTO_HIDE_WHEN_NOT_JOINED) {
            delay = DELAY_AUTO_HIDE_WHEN_NOT_JOINED;
        } else if (state == PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS && AUTO_HIDE_WHEN_JOINED) {
            delay = DELAY_AUTO_HIDE_WHEN_JOINED;
        } else {
            return;
        }
        autoHideDisposable = PLVRxTimer.delay(delay, new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                animateMoveToShowSmallestWidth();
                tipGradientShowOrHide(false, DURATION_MS_LINK_MIC_OPEN_OFF);
                switch (state) {
                    case STATE_TEACHER_LINK_MIC_OPEN:
                        state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE;
                        break;
                    case STATE_JOIN_LINK_MIC_SUCCESS:
                        state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS_COLLAPSE;
                        btnRingActionPortrait.setRingSettingState();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //停止自动隐藏
    private void stopAutoHideCountDown() {
        dispose(autoHideDisposable);
    }

    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        post(new Runnable() {
            @Override
            public void run() {
                startAutoHideCountDown();
            }
        });
        return super.onInterceptTouchEvent(ev);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理屏幕旋转">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setOrientation(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    private void setOrientation(boolean isPortrait) {
        //如果讲师关闭了连麦，那么横竖屏样式都不显示
        if (state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
            return;
        }
        //直播带货场景的横竖屏都使用同一样式 连麦条
        floatingViewPortraitRoot.setVisibility(VISIBLE);
        //旋转到竖屏时，如果当前上麦了，那么将控制条拉到最长
        if (state.ordinal() >= PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS.ordinal()) {
            clickRingSetting();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理点击事件">
    private void setPortraitClickListener() {
        //设置按钮
        btnRingActionPortrait.setOnLinkMicRingButtonClickListener(new PLVECLinkMicRingButton.OnPLVECLinkMicRingButtonClickListener() {
            @Override
            public void onClickRingUp() {
                if (!PLVNetworkUtils.isConnected(getContext())) {
                    PLVCommonLog.w(TAG, "net work not available");
                    return;
                }
                if (toastWhenFloatingPlayerShowing()) {
                    return;
                }
                btnRingActionPortrait.setRingOffState();
                tvRequestTip.setText(R.string.plv_linkmic_tip_requesting_link_mic);

                switch (state) {
                    case STATE_TEACHER_LINK_MIC_OPEN:
                        animateMoveToShowMiddleWidth();
                        PLVCommonLog.d(TAG, "btnSetting.onClickRingUp->STATE_TEACHER_LINK_MIC_OPEN");
                        break;
                    case STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE:
                        //悬浮窗横向移动动画
                        animateMoveToShowMiddleWidth();
                        tipGradientShowOrHide(true, DURATION_MS_LINK_MIC_OPEN_OFF);
                        PLVCommonLog.d(TAG, "btnSetting.onClickRingUp->STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE");
                        break;
                    default:
                        break;
                }
                state = PLVLCLinkMicControllerState.STATE_REQUESTING_JOIN_LINK_MIC;

                // 连麦时不允许小窗播放
                PLVDependManager.getInstance().get(PLVECFloatingWindow.class).showByUser(false);

                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickRingUpLinkMic();
                }
            }

            @Override
            public void onClickRingOff() {
                PLVDialogFactory.createConfirmDialog(
                        getContext(),
                        getResources().getString(R.string.plv_linkmic_dialog_hang_off_confirm_ask),
                        getResources().getString(R.string.plv_linkmic_dialog_hang_off),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                IPLVLinkMicTraceLogSender iplvLinkMicTraceLogSender = new PLVLinkMicTraceLogSender();
                                iplvLinkMicTraceLogSender.setLogModuleClass(PLVLinkMicELog.class);
                                if (state.equals(PLVLCLinkMicControllerState.STATE_REQUESTING_JOIN_LINK_MIC)) {
                                    iplvLinkMicTraceLogSender.submitTraceLog(PLVLinkMicELog.LinkMicTraceLogEvent.USER_CANCEL_LINK_MIC, "waitingUserDidCancelLinkMic，state为" + state);// no need i18n
                                } else {
                                    iplvLinkMicTraceLogSender.submitTraceLog(PLVLinkMicELog.LinkMicTraceLogEvent.USER_CLOSE_LINK_MIC, "joinedUserDidCloseLinkMic，state为" + state);// no need i18n
                                }
                                handleRingOff();
                                dialog.dismiss();
                            }
                        }
                ).show();
            }

            @Override
            public void onClickRingSetting() {
                clickRingSetting();
            }
        });
        //摄像头开关
        btnCameraOpenPortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnCameraOpenState(!isCameraOpen);
                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickCameraOpenOrClose(!isCameraOpen);
                }
            }
        });
        //前后摄像头
        btnCameraFrontBackPortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isCameraFront = !isCameraFront;
                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickCameraFrontOfBack(isCameraFront);
                }
            }
        });
        //麦克风开关
        btnMicrophoneOpenPortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnMicrophoneOpenState(!isMicrophoneOpen);
                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickMicroPhoneOpenOrClose(!isMicrophoneOpen);
                }
            }
        });
        btnCollapsePortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                animateMoveToShowSmallestWidth();
                tipGradientShowOrHide(false, DURATION_MS_LINK_MIC_OPEN_OFF);
                btnRingActionPortrait.setRingSettingState();
                state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS_COLLAPSE;
                startAutoHideCountDown();
            }
        });
    }

    private void clickRingSetting() {
        btnRingActionPortrait.setRingOffState();
        animateMoveToShowBiggestWidth();
        state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS;
        startAutoHideCountDown();
    }

    private void setBtnCameraOpenState(boolean toOpen) {
        isCameraOpen = toOpen;
        if (isCameraOpen) {
            btnCameraOpenPortrait.setBackgroundResource(R.drawable.plvec_linkmic_iv_camera_open);
            btnCameraFrontBackPortrait.setEnabled(true);
            btnCameraFrontBackPortrait.setBackgroundResource(R.drawable.plvec_linkmic_iv_camera_front_back_enabled);
        } else {
            btnCameraOpenPortrait.setBackgroundResource(R.drawable.plvec_linkmic_iv_camera_close);
            btnCameraFrontBackPortrait.setEnabled(false);
            btnCameraFrontBackPortrait.setBackgroundResource(R.drawable.plvec_linkmic_iv_camera_front_back_disabled);
        }
    }

    private void setBtnMicrophoneOpenState(boolean toOpen) {
        isMicrophoneOpen = toOpen;
        if (isMicrophoneOpen) {
            btnMicrophoneOpenPortrait.setBackgroundResource(R.drawable.plvec_linkmic_iv_microphone_open);
        } else {
            btnMicrophoneOpenPortrait.setBackgroundResource(R.drawable.plvec_linkmic_iv_microphone_close);
        }
    }

    //处理点击事件：挂断
    private void handleRingOff() {
        setLeaveLinkMic();
        if (onPLCLinkMicControlBarListener != null) {
            onPLCLinkMicControlBarListener.onClickRingOffLinkMic();
        }
        startAutoHideCountDown();
    }

    private boolean toastWhenFloatingPlayerShowing() {
        if (PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing()) {
            PLVToast.Builder.context(getContext())
                    .setText(PLVAppUtils.getString(R.string.plv_linkmic_floating_player_showing_tips))
                    .show();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理断网">
    private void onNetworkDisconnected() {
        //如果断网时正在请求连麦，那么取消当前请求
        if (state == PLVLCLinkMicControllerState.STATE_REQUESTING_JOIN_LINK_MIC) {
            handleRingOff();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 连麦控制条状态">

    /**
     * 连麦控制条状态
     */
    private enum PLVLCLinkMicControllerState {
        /**
         * 讲师关闭连麦
         */
        STATE_TEACHER_LINK_MIC_CLOSE,

        /**
         * 讲师开启连麦
         */
        STATE_TEACHER_LINK_MIC_OPEN,

        /**
         * 讲师开启连麦，布局收起
         */
        STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE,

        /**
         * 请求加入连麦
         */
        STATE_REQUESTING_JOIN_LINK_MIC,

        /**
         * 加入连麦成功
         */
        STATE_JOIN_LINK_MIC_SUCCESS,

        /**
         * 加入连麦成功，布局收起
         */
        STATE_JOIN_LINK_MIC_SUCCESS_COLLAPSE,
    }

    private boolean isJoinLinkMic() {
        return state == PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS || state == PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS_COLLAPSE;
    }

    // </editor-fold>
}
