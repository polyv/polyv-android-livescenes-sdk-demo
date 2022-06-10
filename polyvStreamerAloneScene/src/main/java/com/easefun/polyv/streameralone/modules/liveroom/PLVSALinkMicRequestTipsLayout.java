package com.easefun.polyv.streameralone.modules.liveroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;


/**
 * 有人申请连麦时 提示条布局
 *
 * @author suhongtao
 */
public class PLVSALinkMicRequestTipsLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVRoundRectLayout plvsaEmptyLinkmicTipsGroupLayout;
    private LinearLayout plvsaEmptyLinkmicTipsLl;
    private ImageView plvsaEmptyLinkmicTipsIconIv;
    private TextView plvsaEmptyLinkmicTipsTv;
    private Button plvsaEmptyLinkmicNavBtn;

    // 有人申请连麦提示条的宽度+右边距
    private int linkmicTipsGroupWidthWithMargin;

    // 连麦提示条动画
    private ObjectAnimator linkmicTipsMoveInAnimator = new ObjectAnimator();
    private ObjectAnimator linkmicTipsMoveOutAnimator = new ObjectAnimator();
    private boolean isLinkmicTipsAnimating = false;
    private boolean isLinkmicTipsShowing = false;

    // 连麦提示条动画 消息处理Handler
    private static final int MSG_CANCEL = 0;
    private static final int MSG_MOVE_IN = 1;
    private static final int MSG_MOVE_OUT = 2;
    private final Handler animatorHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CANCEL:
                    animatorHandler.removeMessages(MSG_MOVE_IN);
                    animatorHandler.removeMessages(MSG_MOVE_OUT);
                    linkmicTipsMoveInAnimator.cancel();
                    linkmicTipsMoveOutAnimator.cancel();
                    plvsaEmptyLinkmicTipsGroupLayout.setAlpha(1);
                    plvsaEmptyLinkmicTipsGroupLayout.setTranslationX(linkmicTipsGroupWidthWithMargin);
                    isLinkmicTipsShowing = false;
                    isLinkmicTipsAnimating = false;
                    break;
                case MSG_MOVE_IN:
                    if (!isLinkmicTipsShowing && !isLinkmicTipsAnimating) {
                        linkmicTipsMoveInAnimator.start();
                    }
                    break;
                case MSG_MOVE_OUT:
                    if (isLinkmicTipsShowing && !isLinkmicTipsAnimating) {
                        linkmicTipsMoveOutAnimator.start();
                    }
                    break;
                default:
            }
        }
    };

    private boolean isBeautyLayoutShowing = false;

    private OnTipsClickListener onTipsClickListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    public PLVSALinkMicRequestTipsLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSALinkMicRequestTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSALinkMicRequestTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_linkmic_request_tips_layout, this);
        findView();

        initLinkmicTipsAnimator();
        initLinkmicTipsOnClickListener();

        observeBeautyLayoutStatus();
    }

    private void findView() {
        plvsaEmptyLinkmicTipsGroupLayout = (PLVRoundRectLayout) findViewById(R.id.plvsa_empty_linkmic_tips_group_layout);
        plvsaEmptyLinkmicTipsLl = (LinearLayout) findViewById(R.id.plvsa_empty_linkmic_tips_ll);
        plvsaEmptyLinkmicTipsIconIv = (ImageView) findViewById(R.id.plvsa_empty_linkmic_tips_icon_iv);
        plvsaEmptyLinkmicTipsTv = (TextView) findViewById(R.id.plvsa_empty_linkmic_tips_tv);
        plvsaEmptyLinkmicNavBtn = (Button) findViewById(R.id.plvsa_empty_linkmic_nav_btn);
    }

    private void initLinkmicTipsAnimator() {
        plvsaEmptyLinkmicTipsGroupLayout.post(new Runnable() {
            @Override
            public void run() {
                final int tipsWidth = plvsaEmptyLinkmicTipsGroupLayout.getWidth();
                final int tipsWidthWithMargin = tipsWidth + ConvertUtils.dp2px(8);
                linkmicTipsGroupWidthWithMargin = tipsWidthWithMargin;

                plvsaEmptyLinkmicTipsGroupLayout.setTranslationX(tipsWidthWithMargin);

                // 移入：从屏幕右侧移入
                linkmicTipsMoveInAnimator = ObjectAnimator.ofFloat(plvsaEmptyLinkmicTipsGroupLayout, "translationX", tipsWidthWithMargin, 0);
                linkmicTipsMoveInAnimator.setDuration(500);

                // 移出：渐隐
                linkmicTipsMoveOutAnimator = ObjectAnimator.ofFloat(plvsaEmptyLinkmicTipsGroupLayout, "alpha", 1, 0);
                linkmicTipsMoveOutAnimator.setDuration(1000);

                linkmicTipsMoveInAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation, boolean isReverse) {
                        isLinkmicTipsShowing = true;
                        isLinkmicTipsAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isLinkmicTipsAnimating = false;
                        animatorHandler.sendMessageDelayed(Message.obtain(animatorHandler, MSG_MOVE_OUT), 10 * 1000);
                    }
                });

                linkmicTipsMoveOutAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isLinkmicTipsAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                        isLinkmicTipsShowing = false;
                        isLinkmicTipsAnimating = false;
                        plvsaEmptyLinkmicTipsGroupLayout.setAlpha(1);
                        plvsaEmptyLinkmicTipsGroupLayout.setTranslationX(tipsWidthWithMargin);
                    }
                });
            }
        });
    }

    private void initLinkmicTipsOnClickListener() {
        plvsaEmptyLinkmicTipsGroupLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTipsClickListener != null) {
                    onTipsClickListener.onClickBar();
                }
            }
        });
        plvsaEmptyLinkmicNavBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTipsClickListener != null) {
                    onTipsClickListener.onClickNavBtn();
                }
            }
        });
    }

    private void observeBeautyLayoutStatus() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        PLVSALinkMicRequestTipsLayout.this.isBeautyLayoutShowing = beautyUiState != null && beautyUiState.isBeautyMenuShowing;
                        updateVisibility();
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 外部调用的方法">

    public void show() {
        animatorHandler.sendMessage(Message.obtain(animatorHandler, MSG_MOVE_IN));
    }

    public void hide() {
        animatorHandler.sendMessage(Message.obtain(animatorHandler, MSG_MOVE_OUT));
    }

    public void cancel() {
        animatorHandler.sendMessage(Message.obtain(animatorHandler, MSG_CANCEL));
    }

    public void setOnTipsClickListener(OnTipsClickListener onTipsClickListener) {
        this.onTipsClickListener = onTipsClickListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理">

    private void updateVisibility() {
        // 美颜布局显示时，不显示连麦提示条
        if (isBeautyLayoutShowing) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnTipsClickListener {
        void onClickBar();

        void onClickNavBtn();
    }

    // </editor-fold>

}
