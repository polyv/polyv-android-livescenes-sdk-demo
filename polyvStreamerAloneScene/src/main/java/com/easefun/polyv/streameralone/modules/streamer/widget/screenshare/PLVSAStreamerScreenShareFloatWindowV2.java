package com.easefun.polyv.streameralone.modules.streamer.widget.screenshare;

import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.ui.widget.floating.PLVFloatingWindowManager;
import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.scenes.PLVSAStreamerAloneActivity;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.Map;

/**
 * 纯视频开播悬浮窗，系统级别悬浮窗，需要申请悬浮窗权限。
 * 用于显示屏幕共享状态，仅在后台时显示
 */
public class PLVSAStreamerScreenShareFloatWindowV2 extends FrameLayout implements IPLVSAStreamerScreenShareFloatWindow, View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final Map<PLVLinkMicConstant.NetworkQuality, Integer> QUALITY_IMAGE_MAP = mapOf(
            pair(PLVLinkMicConstant.NetworkQuality.EXCELLENT, R.drawable.plv_streamer_network_status_good_icon),
            pair(PLVLinkMicConstant.NetworkQuality.GOOD, R.drawable.plv_streamer_network_status_good_icon),
            pair(PLVLinkMicConstant.NetworkQuality.POOR, R.drawable.plv_streamer_network_status_moderate_icon),
            pair(PLVLinkMicConstant.NetworkQuality.BAD, R.drawable.plv_streamer_network_status_moderate_icon),
            pair(PLVLinkMicConstant.NetworkQuality.VERY_BAD, R.drawable.plv_streamer_network_status_bad_icon),
            pair(PLVLinkMicConstant.NetworkQuality.DISCONNECT, R.drawable.plv_streamer_network_status_bad_icon)
    );

    private static final Map<PLVLinkMicConstant.NetworkQuality, Integer> QUALITY_DESCRIPTION_MAP = mapOf(
            pair(PLVLinkMicConstant.NetworkQuality.EXCELLENT, R.string.plv_streamer_network_excellent),
            pair(PLVLinkMicConstant.NetworkQuality.GOOD, R.string.plv_streamer_network_good),
            pair(PLVLinkMicConstant.NetworkQuality.POOR, R.string.plv_streamer_network_poor),
            pair(PLVLinkMicConstant.NetworkQuality.BAD, R.string.plv_streamer_network_bad_3),
            pair(PLVLinkMicConstant.NetworkQuality.VERY_BAD, R.string.plv_streamer_network_very_bad),
            pair(PLVLinkMicConstant.NetworkQuality.DISCONNECT, R.string.plv_streamer_network_disconnect),
            pair(PLVLinkMicConstant.NetworkQuality.UNKNOWN, R.string.plv_streamer_network_unknown)
    );

    private ConstraintLayout streamerScreenShareFloatWindowRoot;
    private PLVRoundRectConstraintLayout streamerScreenShareFloatWindowContentLayout;
    private ImageView streamerScreenShareFloatWindowCloseIv;
    private ImageView streamerScreenShareFloatWindowNetworkIv;
    private TextView streamerScreenShareFloatWindowNetworkTv;
    private ImageView streamerScreenShareFloatWindowBackIv;
    private TextView streamerScreenShareFloatWindowTextTv;
    private ConstraintLayout streamerScreenShareFloatWindowCollapsedLayout;
    private ImageView streamerScreenShareFloatWindowCollapsedLeftIv;
    private ImageView streamerScreenShareFloatWindowCollapsedRightIv;

    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造方法/初始化">
    public PLVSAStreamerScreenShareFloatWindowV2(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.plvsa_widget_floating_screen_share_v2, this);

        streamerScreenShareFloatWindowRoot = findViewById(R.id.plvsa_streamer_screen_share_float_window_root);
        streamerScreenShareFloatWindowContentLayout = findViewById(R.id.plvsa_streamer_screen_share_float_window_content_layout);
        streamerScreenShareFloatWindowCloseIv = findViewById(R.id.plvsa_streamer_screen_share_float_window_close_iv);
        streamerScreenShareFloatWindowNetworkIv = findViewById(R.id.plvsa_streamer_screen_share_float_window_network_iv);
        streamerScreenShareFloatWindowNetworkTv = findViewById(R.id.plvsa_streamer_screen_share_float_window_network_tv);
        streamerScreenShareFloatWindowBackIv = findViewById(R.id.plvsa_streamer_screen_share_float_window_back_iv);
        streamerScreenShareFloatWindowTextTv = findViewById(R.id.plvsa_streamer_screen_share_float_window_text_tv);
        streamerScreenShareFloatWindowCollapsedLayout = findViewById(R.id.plvsa_streamer_screen_share_float_window_collapsed_layout);
        streamerScreenShareFloatWindowCollapsedLeftIv = findViewById(R.id.plvsa_streamer_screen_share_float_window_collapsed_left_iv);
        streamerScreenShareFloatWindowCollapsedRightIv = findViewById(R.id.plvsa_streamer_screen_share_float_window_collapsed_right_iv);

        streamerScreenShareFloatWindowCloseIv.setOnClickListener(this);
        streamerScreenShareFloatWindowBackIv.setOnClickListener(this);
        streamerScreenShareFloatWindowCollapsedLeftIv.setOnClickListener(this);
        streamerScreenShareFloatWindowCollapsedRightIv.setOnClickListener(this);
        setClickable(true);

        registerOnTouchEvent();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void registerOnTouchEvent() {
        streamerScreenShareFloatWindowContentLayout.setOnTouchListener(new OnTouchListener() {
            private float startX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        streamerScreenShareFloatWindowContentLayout.setTranslationX(event.getRawX() - startX);
                        break;
                    case MotionEvent.ACTION_UP:
                        moveToOutsideOrCenter();
                        break;
                    default:
                }
                return false;
            }

            private void moveToOutsideOrCenter() {
                final float screenWidth = (float) ScreenUtils.getScreenOrientatedWidth();
                final float translationX = streamerScreenShareFloatWindowContentLayout.getTranslationX();
                final float viewCenterX = translationX + screenWidth / 2;
                if (viewCenterX <= screenWidth * 0.3F) {
                    moveContentLayout(-screenWidth);
                } else if (viewCenterX >= screenWidth * 0.7F) {
                    moveContentLayout(screenWidth);
                } else {
                    moveContentLayout(0);
                }
            }
        });
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API">
    @Override
    public void show() {
        PLVFloatingWindowManager.getInstance().createNewWindow((Activity) getContext())
                .setIsSystemWindow(true)
                .setContentView(this)
                .setSize(ViewGroup.LayoutParams.MATCH_PARENT, ConvertUtils.dp2px(58))
                .setFloatLocation(0, 0)
                .setShowType(PLVFloatingEnums.ShowType.SHOW_ONLY_BACKGROUND)
                .setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType.NO_AUTO_MOVE)
                .setEnableDragX(false)
                .setConsumeTouchEventOnMove(false)
                .build();
        PLVFloatingWindowManager.getInstance().show((Activity) getContext());
    }

    @Override
    public void close() {
        PLVFloatingWindowManager.getInstance().hide();
    }

    @Override
    public void destroy() {
        PLVFloatingWindowManager.getInstance().destroy();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="presenter view实现">

    public final PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            streamerPresenter = presenter;
        }

        @Override
        public void onNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
            Integer imgRes = QUALITY_IMAGE_MAP.get(quality);
            if (imgRes != null) {
                streamerScreenShareFloatWindowNetworkIv.setImageResource(imgRes);
            }
            Integer textRes = QUALITY_DESCRIPTION_MAP.get(quality);
            if (textRes != null) {
                streamerScreenShareFloatWindowNetworkTv.setText(textRes);
            }
        }
    };

    public final IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        private ShowMessageTask pendingShowMessageTask;

        @Override
        public void onSpeakEvent(@NonNull final PLVSpeakEvent speakEvent) {
            post(new Runnable() {
                @Override
                public void run() {
                    final ShowMessageTask showMessageTask = new ShowMessageTask();
                    showMessageTask.onShowMessage = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Spannable span = new PLVSpannableStringBuilder()
                                        .append(speakEvent.getUser().getNick())
                                        .append(": ")
                                        .appendExclude(speakEvent.getValues().get(0), new ForegroundColorSpan(Color.WHITE));
                                streamerScreenShareFloatWindowTextTv.setText(span);
                            } catch (Exception e) {
                                PLVCommonLog.exception(e);
                            }
                        }
                    };
                    showMessageTask.keepShowingDuration = 4000;
                    tryShowMessage(showMessageTask);
                }
            });
        }

        @Override
        public void onImgEvent(@NonNull final PLVChatImgEvent chatImgEvent) {
            post(new Runnable() {
                @Override
                public void run() {
                    final ShowMessageTask showMessageTask = new ShowMessageTask();
                    showMessageTask.onShowMessage = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Spannable span = new PLVSpannableStringBuilder()
                                        .append(chatImgEvent.getUser().getNick())
                                        .append(": ")
                                        .appendExclude(getContext().getString(R.string.plv_chat_image_alt_text), new ForegroundColorSpan(Color.WHITE));
                                streamerScreenShareFloatWindowTextTv.setText(span);
                            } catch (Exception e) {
                                PLVCommonLog.exception(e);
                            }
                        }
                    };
                    showMessageTask.keepShowingDuration = 4000;
                    tryShowMessage(showMessageTask);
                }
            });
        }

        @Override
        public void onLoginEvent(@NonNull final PLVLoginEvent loginEvent) {
            post(new Runnable() {
                @Override
                public void run() {
                    final ShowMessageTask showMessageTask = new ShowMessageTask();
                    showMessageTask.onShowMessage = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Spannable span = new PLVSpannableStringBuilder()
                                        .append(loginEvent.getUser().getNick())
                                        .append(" ")
                                        .appendExclude(getContext().getString(R.string.plv_member_enter_live_room), new ForegroundColorSpan(Color.WHITE));
                                streamerScreenShareFloatWindowTextTv.setText(span);
                            } catch (Exception e) {
                                PLVCommonLog.exception(e);
                            }
                        }
                    };
                    showMessageTask.keepShowingDuration = 2000;
                    tryShowMessage(showMessageTask);
                }
            });
        }

        @Override
        public void onLogoutEvent(@NonNull final PLVLogoutEvent logoutEvent) {
            post(new Runnable() {
                @Override
                public void run() {
                    final ShowMessageTask showMessageTask = new ShowMessageTask();
                    showMessageTask.onShowMessage = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Spannable span = new PLVSpannableStringBuilder()
                                        .append(logoutEvent.getNick())
                                        .append(" ")
                                        .appendExclude(getContext().getString(R.string.plv_member_leave_live_room), new ForegroundColorSpan(Color.WHITE));
                                streamerScreenShareFloatWindowTextTv.setText(span);
                            } catch (Exception e) {
                                PLVCommonLog.exception(e);
                            }
                        }
                    };
                    showMessageTask.keepShowingDuration = 2000;
                    tryShowMessage(showMessageTask);
                }
            });
        }

        private void tryShowMessage(@Nullable ShowMessageTask task) {
            if (task == null) {
                return;
            }
            pendingShowMessageTask = task;
            final boolean success = PLVDebounceClicker.tryClick("PLVSAStreamerScreenShareFloatWindowV2.tryShowMessage", task.keepShowingDuration);
            if (success) {
                task.onShowMessage.run();
                pendingShowMessageTask = null;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tryShowMessage(pendingShowMessageTask);
                    }
                }, task.keepShowingDuration);
            }
        }

    };

    private static class ShowMessageTask {
        Runnable onShowMessage;
        long keepShowingDuration;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == streamerScreenShareFloatWindowCloseIv.getId()) {
            close();
        } else if (id == streamerScreenShareFloatWindowBackIv.getId()) {
            exitScreenShare();
        } else if (id == streamerScreenShareFloatWindowCollapsedLeftIv.getId()
                || id == streamerScreenShareFloatWindowCollapsedRightIv.getId()) {
            if (floatEquals(streamerScreenShareFloatWindowCollapsedLeftIv.getAlpha(), 1)) {
                streamerScreenShareFloatWindowCollapsedLeftIv.setAlpha(0F);
                onLayoutCollapsedChanged(0);
                moveContentLayout(0);
            }
            if (floatEquals(streamerScreenShareFloatWindowCollapsedRightIv.getAlpha(), 1)) {
                streamerScreenShareFloatWindowCollapsedRightIv.setAlpha(0F);
                onLayoutCollapsedChanged(0);
                moveContentLayout(0);
            }
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="功能方法">

    private void exitScreenShare() {
        Intent intent = new Intent(getContext(), PLVSAStreamerAloneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        getContext().startActivity(intent);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                streamerPresenter.exitShareScreen();
                close();
            }
        }, 300);
    }

    private void moveContentLayout(final float targetX) {
        final float screenWidth = (float) ScreenUtils.getScreenOrientatedWidth();
        final float currentX = streamerScreenShareFloatWindowContentLayout.getTranslationX();
        final ObjectAnimator anim = ObjectAnimator.ofFloat(streamerScreenShareFloatWindowContentLayout, "translationX", currentX, targetX);
        final float speed = 5;
        anim.setDuration((long) (Math.abs(currentX - targetX) / speed));
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (floatEquals(targetX, screenWidth)) {
                    onLayoutCollapsedChanged(1);
                } else if (floatEquals(targetX, -screenWidth)) {
                    onLayoutCollapsedChanged(-1);
                } else {
                    onLayoutCollapsedChanged(0);
                }
                animUpdateCollapsedLayoutAlpha();
            }
        });
        anim.start();
    }

    /**
     * @param flag -1 左侧折叠，0 展开，1 右侧折叠
     */
    private void onLayoutCollapsedChanged(int flag) {
        final float screenWidth = (float) ScreenUtils.getScreenOrientatedWidth();
        final Point location = PLVFloatingWindowManager.getInstance().getFloatLocation();
        if (location == null) {
            return;
        }
        if (flag == -1) {
            PLVFloatingWindowManager.getInstance().updateFloatLocation(0, location.y);
            PLVFloatingWindowManager.getInstance().updateFloatSize(ConvertUtils.dp2px(24), ConvertUtils.dp2px(58));
            PLVFloatingWindowManager.getInstance().setConsumeTouchEventOnMove(true);
        } else if (flag == 1) {
            PLVFloatingWindowManager.getInstance().updateFloatLocation((int) screenWidth, location.y);
            PLVFloatingWindowManager.getInstance().updateFloatSize(ConvertUtils.dp2px(24), ConvertUtils.dp2px(58));
            PLVFloatingWindowManager.getInstance().setConsumeTouchEventOnMove(true);
        } else {
            PLVFloatingWindowManager.getInstance().updateFloatLocation(0, location.y);
            PLVFloatingWindowManager.getInstance().updateFloatSize(ViewGroup.LayoutParams.MATCH_PARENT, ConvertUtils.dp2px(58));
            PLVFloatingWindowManager.getInstance().setConsumeTouchEventOnMove(false);
        }
    }

    private void animUpdateCollapsedLayoutAlpha() {
        final float screenWidth = (float) ScreenUtils.getScreenOrientatedWidth();
        final float translationX = streamerScreenShareFloatWindowContentLayout.getTranslationX();
        final float leftAlpha = streamerScreenShareFloatWindowCollapsedLeftIv.getAlpha();
        ObjectAnimator anim = ObjectAnimator.ofFloat(streamerScreenShareFloatWindowCollapsedLeftIv, "alpha", leftAlpha, -translationX / screenWidth);
        anim.setDuration(300);
        anim.setStartDelay(200);
        anim.start();
        final float rightAlpha = streamerScreenShareFloatWindowCollapsedRightIv.getAlpha();
        anim = ObjectAnimator.ofFloat(streamerScreenShareFloatWindowCollapsedRightIv, "alpha", rightAlpha, translationX / screenWidth);
        anim.setDuration(300);
        anim.setStartDelay(200);
        anim.start();
    }

    // </editor-fold>

    private static boolean floatEquals(float a, float b) {
        return Math.abs(a - b) < 1E-6F;
    }

}




