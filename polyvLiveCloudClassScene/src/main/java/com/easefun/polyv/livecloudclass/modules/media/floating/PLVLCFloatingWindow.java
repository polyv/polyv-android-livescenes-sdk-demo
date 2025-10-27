package com.easefun.polyv.livecloudclass.modules.media.floating;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.cast.manager.PLVCastBusinessManager;
import com.easefun.polyv.livecommon.module.modules.player.floating.IPLVFloatingWindow;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerConfig;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.utils.PLVVideoSizeUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.PLVFloatPermissionUtils;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.business.api.common.player.PLVBaseVideoView;
import com.plv.business.api.common.player.listener.IPLVVideoViewListenerEvent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVLiveLoginResult;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.AppUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author Hoshiiro
 */
public class PLVLCFloatingWindow implements IPLVFloatingWindow {

    private static final String TAG = PLVLCFloatingWindow.class.getSimpleName();
    private static final int IS_JOIN_LINK_MIC = 1 << 1;
    private static final int IS_JOIN_REQUEST_LINK_MIC = 1 << 2;
    private static final int IS_PLAYER_PREPARED = 1 << 3;
    private static final int IS_OPEN_FLOAT_ENABLE = 1 << 4;

    private int canShowFloatingWindowStatus = IS_OPEN_FLOAT_ENABLE;
    private boolean requestShowByGoHome = false;
    private boolean requestShowByExitPage = false;
    private boolean requestShowByCommodityPage = false;
    private boolean requestShowByUser = false;
    @Nullable
    private PLVSwitchViewAnchorLayout contentAnchorLayout;
    @Nullable
    private PLVBaseVideoView baseVideoView;
    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager;

    private AlertDialog alertDialog;

    private PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ONLY_FOREGROUND;
    private PLVFloatingEnums.Orientation orientation = PLVFloatingEnums.Orientation.AUTO;

    public void updateWhenJoinLinkMic(boolean isJoinLinkMic) {
        updateWhenJoinRequestLinkMic(false);
        if (isJoinLinkMic) {
            canShowFloatingWindowStatus |= IS_JOIN_LINK_MIC;
        } else {
            canShowFloatingWindowStatus &= ~IS_JOIN_LINK_MIC;
        }
    }

    public void updateWhenJoinRequestLinkMic(boolean isRequestLinkMic) {
        if (isRequestLinkMic) {
            canShowFloatingWindowStatus |= IS_JOIN_REQUEST_LINK_MIC;
        } else {
            canShowFloatingWindowStatus &= ~IS_JOIN_REQUEST_LINK_MIC;
        }
    }

    public void updateWhenPlayerPrepared(boolean prepared) {
        if (prepared) {
            canShowFloatingWindowStatus |= IS_PLAYER_PREPARED;
        } else {
            canShowFloatingWindowStatus &= ~IS_PLAYER_PREPARED;
        }
    }

    public void updateWhenOpenFloat(boolean isOpenFloatEnable) {
        if (isOpenFloatEnable) {
            canShowFloatingWindowStatus |= IS_OPEN_FLOAT_ENABLE;
        } else {
            canShowFloatingWindowStatus &= ~IS_OPEN_FLOAT_ENABLE;
        }
    }

    public boolean canShowFloatingWindowStatus() {
        return (canShowFloatingWindowStatus & IS_PLAYER_PREPARED) != 0
                && (canShowFloatingWindowStatus & IS_OPEN_FLOAT_ENABLE) != 0
                && (canShowFloatingWindowStatus & IS_JOIN_LINK_MIC) == 0
                && (canShowFloatingWindowStatus & IS_JOIN_REQUEST_LINK_MIC) == 0
                && !PLVCastBusinessManager.getInstance().isCasting();
    }

    public void hideWhenOnlyShowByGoHome() {
        // 1.前后台切换触发小窗 – 切回后自动全屏
        // 2.客户手动触发小窗 – 切回后不自动全屏
        if (requestShowByGoHome && !requestShowByUser && !requestShowByCommodityPage && !requestShowByExitPage) {
            close();
        }
    }

    public void showByGoHomeWhenEnabled() {
        if (!PLVFloatingPlayerConfig.isAutoFloatingWhenGoHome()) {
            return;
        }
        PLVAppUtils.postToMainThread(200, new Runnable() { // 小米悬浮窗兼容
            @Override
            public void run() {
                if (AppUtils.isAppForeground()) {
                    return;
                }
                showByGoHome(true);
            }
        });
    }

    public void showByExitPageWhenEnabled(@NonNull Callback onBackPressedRunnable) {
        if (!PLVFloatingPlayerConfig.isAutoFloatingWhenExitPage()) {
            onBackPressedRunnable.run(null);
            return;
        }
        showByExitPage(true, onBackPressedRunnable);
    }

    public void showByExitPage(boolean toShow, @NonNull Callback onBackPressedRunnable) {
        if (!canShowFloatingWindowStatus()) {
            onBackPressedRunnable.run(null);
            return;
        }
        requestShowByExitPage = toShow;
        onRequestShowChanged(onBackPressedRunnable);
    }

    public void showByGoHome(boolean toShow) {
        // 避免 跳到权限设置页面申请权限时 再次触发悬浮窗申请权限
        if (isNeedShow()
                && contentAnchorLayout != null
                && !PLVFloatPermissionUtils.checkPermission((Activity) contentAnchorLayout.getContext())) {
            return;
        }
        if (!canShowFloatingWindowStatus()) {
            return;
        }
        requestShowByGoHome = toShow;
        onRequestShowChanged();
    }

    public void showByCommodityPage(boolean toShow) {
        if (!canShowFloatingWindowStatus()) {
            return;
        }
        requestShowByCommodityPage = toShow;
        onRequestShowChanged();
    }

    public void showByUser(boolean toShow) {
        showByUser(toShow, null);
    }

    public void showByUser(boolean toShow, @Nullable Callback onBackPressedRunnable) {
        if (!canShowFloatingWindowStatus()) {
            if (onBackPressedRunnable != null) {
                onBackPressedRunnable.run(null);
            }
            return;
        }
        requestShowByUser = toShow;
        onRequestShowChanged(onBackPressedRunnable);
    }

    public boolean isRequestingShowByUser() {
        return requestShowByUser;
    }

    public void bindContentView(PLVSwitchViewAnchorLayout anchorLayout) {
        this.contentAnchorLayout = anchorLayout;
        if (contentAnchorLayout != null) {
            findBaseVideoView((View) contentAnchorLayout.getParent());
        }
    }

    public void setLiveRoomData(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        observerDataToFloatingWindow();
    }

    private void observerDataToFloatingWindow() {
        if (liveRoomDataManager == null || contentAnchorLayout == null) {
            return;
        }
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) contentAnchorLayout.getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> plvLiveClassDetailVOPLVStatefulData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (plvLiveClassDetailVOPLVStatefulData == null || !plvLiveClassDetailVOPLVStatefulData.isSuccess()) {
                    return;
                }
                PLVLiveClassDetailVO liveClassDetail = plvLiveClassDetailVOPLVStatefulData.getData();
                if (liveClassDetail == null || liveClassDetail.getData() == null) {
                    return;
                }
                updateWhenOpenFloat(liveClassDetail.getData().getGlobalRtcRecordSetting().isFenestrulePlayEnabled());
            }
        });
    }

    /**
     * 静音播放器/恢复音量
     *
     * @param mute true：静音，false：恢复音量
     */
    @Override
    public void mutePlayer(boolean mute) {
        if (baseVideoView != null) {
            int currentPlayerVolume = baseVideoView.getPlayerVolume();
            float setVolume = mute ? 0 : currentPlayerVolume / 100.f;
            if (baseVideoView.getIjkMediaPlayer() != null) {
                baseVideoView.getIjkMediaPlayer().setVolume(setVolume, setVolume);
            }
        }
    }

    /**
     * 设置由音频焦点引起的播放状态改变监听器
     *
     * @param listener 监听器
     */
    @Override
    public void setOnPlayStatusChangeByAudioFocusListener(IPLVVideoViewListenerEvent.OnPlayStatusChangeByAudioFocusListener listener) {
        if (baseVideoView != null) {
            baseVideoView.setOnPlayStatusChangeByAudioFocusListener(listener);
        }
    }

    /**
     * 设置窗口方向
     *
     * @param orientation 方向
     */
    @Override
    public void setOrientation(PLVFloatingEnums.Orientation orientation) {
        this.orientation = orientation;
    }

    /**
     * 关闭悬浮窗
     */
    @Override
    public void close() {
        resetStatus();
        PLVFloatingPlayerManager.getInstance().clear();
    }

    private void resetStatus() {
        requestShowByGoHome = false;
        requestShowByExitPage = false;
        requestShowByCommodityPage = false;
        requestShowByUser = false;
    }

    // 根据视频宽高适配悬浮窗宽高
    private int[] getFloatingSize() {
        int width = ConvertUtils.dp2px(200);
        int height = ConvertUtils.dp2px(112.5F);
        boolean isLandscape = orientation != PLVFloatingEnums.Orientation.PORTRAIT;
        if (baseVideoView != null && orientation == PLVFloatingEnums.Orientation.AUTO) {
            int[] wh = PLVVideoSizeUtils.getVideoWH(baseVideoView);
            if (wh[0] != 0 && wh[0] < wh[1]) {
                isLandscape = false;
            }
        }
        return new int[]{isLandscape ? width : height, isLandscape ? height : width};
    }

    private boolean findBaseVideoView(View view) {
        if (view instanceof PLVBaseVideoView) {
            baseVideoView = (PLVBaseVideoView) view;
            return true;
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0, size = viewGroup.getChildCount(); i < size; i++) {
                boolean result = findBaseVideoView(viewGroup.getChildAt(i));
                if (result) {
                    return true;
                }
            }
        }
        return false;
    }

    private void onRequestShowChanged() {
        onRequestShowChanged(null);
    }

    private void onRequestShowChanged(@Nullable Callback onBackPressedRunnable) {
        if (PLVFloatingPlayerConfig.isAutoFloatingWhenGoHome()) {
            showType = PLVFloatingEnums.ShowType.SHOW_ALWAYS;
        } else {
            showType = PLVFloatingEnums.ShowType.SHOW_ONLY_FOREGROUND;
        }
        PLVFloatingPlayerManager.getInstance().updateShowType(showType);

        final boolean needShow = isNeedShow();
        final boolean isShowing = PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing();
        if (needShow && !isShowing) {
            playOnFloatingWindow(onBackPressedRunnable);
        } else if (!needShow && isShowing) {
            PLVFloatingPlayerManager.getInstance().hide();
        } else {
            if (onBackPressedRunnable != null) {
                onBackPressedRunnable.run(null);
            }
        }
    }

    private void playOnFloatingWindow(@Nullable Callback onBackPressedRunnable) {
        if (contentAnchorLayout == null) {
            if (onBackPressedRunnable != null) {
                onBackPressedRunnable.run(null);
            }
            return;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!isNeedShow()) {
                    return;
                }
                int[] floatingSize = getFloatingSize();
                PLVFloatingPlayerManager.getInstance()
                        .setFloatingSize(
                                floatingSize[0],
                                floatingSize[1]
                        )
                        .setFloatingPosition(
                                ScreenUtils.getScreenOrientatedWidth() - floatingSize[0] - ConvertUtils.dp2px(16),
                                ScreenUtils.getScreenOrientatedHeight() - floatingSize[1] - ConvertUtils.dp2px(34)
                        )
                        .updateShowType(showType)
                        .setOnGoBackListener(new PLVFloatingPlayerManager.OnGoBackListener() {
                            @Override
                            public void onGoBack(@Nullable final Intent savedIntent) {
                                if (savedIntent == null) {
                                    return;
                                }
                                resetStatus();
                                PLVFloatingPlayerManager.getInstance().clear();
                                boolean result = AppUtils.bring2Front(contentAnchorLayout.getContext());
                                if (!result) {
                                    PLVAppUtils.postToMainThread(600, new Runnable() {
                                        @Override
                                        public void run() {
                                            AppUtils.bring2Front(contentAnchorLayout.getContext());
                                        }
                                    });
                                }
                                savedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                contentAnchorLayout.getContext().startActivity(savedIntent);
                            }
                        })
                        .setOnCloseFloatingWindowListener(new PLVFloatingPlayerManager.OnCloseFloatingWindowListener() {
                            @Override
                            public void onClosedFloatingWindow(@Nullable String tag) {
                                close();
                            }
                        })
                        .setFloatingWindow(PLVLCFloatingWindow.this)
                        .bindContentLayout(contentAnchorLayout)
                        .show();
            }
        };
        checkFloatingWindowPermissionThenRun(contentAnchorLayout.getContext(), runnable, onBackPressedRunnable);
    }

    private void reLoginWatchThenRun(final Runnable onLoginSuccess) {
        if (liveRoomDataManager == null) {
            return;
        }
        new PLVSceneLoginManager().loginLiveNew(
                liveRoomDataManager.getConfig().getAccount().getAppId(),
                liveRoomDataManager.getConfig().getAccount().getAppSecret(),
                liveRoomDataManager.getConfig().getAccount().getUserId(),
                liveRoomDataManager.getConfig().getChannelId(),
                new IPLVSceneLoginManager.OnLoginListener<PLVLiveLoginResult>() {
                    @Override
                    public void onLoginSuccess(PLVLiveLoginResult plvLiveLoginResult) {
                        onLoginSuccess.run();
                    }

                    @Override
                    public void onLoginFailed(String msg, Throwable throwable) {
                        PLVCommonLog.w(TAG, "onLoginFailed: " + msg + " " + throwable);
                    }
                }
        );
    }

    private void checkFloatingWindowPermissionThenRun(final Context context, final Runnable runnable, @Nullable final Callback onBackPressedRunnable) {
        if (PLVFloatPermissionUtils.checkPermission((Activity) context)) {
            if (onBackPressedRunnable != null) {
                onBackPressedRunnable.run(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                });
            } else {
                runnable.run();
            }
        } else {
            final Activity topActivity = ActivityUtils.getTopActivity();
            alertDialog = new AlertDialog.Builder(topActivity == null ? context : topActivity)
                    .setMessage(PLVAppUtils.getString(R.string.plv_player_floating_permission_apply_tips))
                    .setPositiveButton(PLVAppUtils.getString(R.string.plv_common_dialog_confirm_3), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PLVFloatPermissionUtils.requestPermission((Activity) context, new PLVFloatPermissionUtils.IPLVOverlayPermissionListener() {
                                @Override
                                public void onResult(final boolean isGrant) {
                                    if (onBackPressedRunnable != null) {
                                        onBackPressedRunnable.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isGrant) {
                                                    runnable.run();
                                                }
                                            }
                                        });
                                    } else {
                                        if (isGrant) {
                                            runnable.run();
                                        }
                                    }
                                    if (!isGrant) {
                                        resetStatus();
                                    }
                                    // 由后台小窗的权限申请跳到权限页时，会再触发一次后台小窗的权限申请，因此这里需隐藏
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    alertDialog = null;
                                }
                            });
                        }
                    })
                    .setNegativeButton(PLVAppUtils.getString(R.string.plv_common_dialog_cancel_2), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (onBackPressedRunnable != null) {
                                onBackPressedRunnable.run(null);
                            }
                            resetStatus();
                            alertDialog = null;
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (onBackPressedRunnable != null) {
                                onBackPressedRunnable.run(null);
                            }
                            resetStatus();
                            alertDialog = null;
                        }
                    })
                    .show();

        }
    }

    private boolean isNeedShow() {
        return requestShowByUser || requestShowByCommodityPage || requestShowByGoHome || requestShowByExitPage;
    }

    public interface Callback {
        // callback 不为null表示可进入小窗状态
        void run(@Nullable Runnable callback);
    }
}
