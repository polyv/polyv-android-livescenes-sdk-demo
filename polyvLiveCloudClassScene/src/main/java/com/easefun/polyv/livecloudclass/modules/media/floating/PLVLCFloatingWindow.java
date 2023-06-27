package com.easefun.polyv.livecloudclass.modules.media.floating;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.utils.PLVVideoSizeUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.PLVFloatPermissionUtils;
import com.plv.business.api.common.player.PLVBaseVideoView;
import com.plv.business.api.common.player.listener.IPLVVideoViewListenerEvent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVLiveLoginResult;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.AppUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author Hoshiiro
 */
public class PLVLCFloatingWindow {

    private static final String TAG = PLVLCFloatingWindow.class.getSimpleName();

    private boolean requestShowByCommodityPage = false;
    private boolean requestShowByUser = false;
    @Nullable
    private PLVSwitchViewAnchorLayout contentAnchorLayout;
    @Nullable
    private PLVBaseVideoView baseVideoView;
    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager;

    private PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ONLY_FOREGROUND;
    private PLVFloatingEnums.Orientation orientation = PLVFloatingEnums.Orientation.AUTO;

    public void showByCommodityPage(boolean toShow) {
        requestShowByCommodityPage = toShow;
        onRequestShowChanged();
    }

    public void showByUser(boolean toShow) {
        requestShowByUser = toShow;
        onRequestShowChanged();
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
    }

    /**
     * 设置悬浮窗里的播放器音量
     *
     * @param volume 音量值，范围：[0,100]
     */
    public void setPlayerVolume(int volume) {
        if (baseVideoView != null) {
            baseVideoView.setPlayerVolume(volume);
        }
    }

    /**
     * 设置系统音量
     *
     * @param volume 音量值，范围：[0,100]
     */
    public void setVolume(int volume) {
        if (baseVideoView != null) {
            baseVideoView.setVolume(volume);
        }
    }

    /**
     * 设置由音频焦点引起的播放状态改变监听器
     *
     * @param listener 监听器
     */
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
    public void setOrientation(PLVFloatingEnums.Orientation orientation) {
        this.orientation = orientation;
    }

    /**
     * 关闭悬浮窗
     */
    public void close() {
        requestShowByCommodityPage = false;
        requestShowByUser = false;
        PLVFloatingPlayerManager.getInstance().clear();
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
        if (requestShowByUser) {
            showType = PLVFloatingEnums.ShowType.SHOW_ALWAYS;
        } else if (requestShowByCommodityPage) {
            showType = PLVFloatingEnums.ShowType.SHOW_ONLY_FOREGROUND;
        }
        PLVFloatingPlayerManager.getInstance().updateShowType(showType);

        final boolean needShow = isNeedShow();
        final boolean isShowing = PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing();
        if (needShow && !isShowing) {
            playOnFloatingWindow();
        } else if (!needShow && isShowing) {
            PLVFloatingPlayerManager.getInstance().hide();
        }
    }

    private void playOnFloatingWindow() {
        if (contentAnchorLayout == null) {
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
                                requestShowByCommodityPage = false;
                                requestShowByUser = false;
                                PLVFloatingPlayerManager.getInstance().clear();
                                AppUtils.bring2Front(contentAnchorLayout.getContext());
                                savedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                final boolean needReLogin = ((Activity) contentAnchorLayout.getContext()).isFinishing();
                                if (needReLogin) {
                                    reLoginWatchThenRun(new Runnable() {
                                        @Override
                                        public void run() {
                                            contentAnchorLayout.getContext().startActivity(savedIntent);
                                        }
                                    });
                                } else {
                                    contentAnchorLayout.getContext().startActivity(savedIntent);
                                }
                            }
                        })
                        .setOnCloseFloatingWindowListener(new PLVFloatingPlayerManager.OnCloseFloatingWindowListener() {
                            @Override
                            public void onClosedFloatingWindow(@Nullable String tag) {
                                close();
                            }
                        })
                        .bindContentLayout(contentAnchorLayout)
                        .show();
            }
        };
        checkFloatingWindowPermissionThenRun(contentAnchorLayout.getContext(), runnable);
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

    private void checkFloatingWindowPermissionThenRun(final Context context, final Runnable runnable) {
        if (PLVFloatPermissionUtils.checkPermission((Activity) context)) {
            runnable.run();
        } else {
            final Activity topActivity = ActivityUtils.getTopActivity();
            new AlertDialog.Builder(topActivity == null ? context : topActivity)
                    .setMessage("悬浮小窗播放功能需要在应用设置中开启悬浮窗权限，是否前往开启权限？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PLVFloatPermissionUtils.requestPermission((Activity) context, new PLVFloatPermissionUtils.IPLVOverlayPermissionListener() {
                                @Override
                                public void onResult(boolean isGrant) {
                                    if (isGrant) {
                                        runnable.run();
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("否", null)
                    .show();

        }
    }

    private boolean isNeedShow() {
        return requestShowByUser || requestShowByCommodityPage;
    }

}
