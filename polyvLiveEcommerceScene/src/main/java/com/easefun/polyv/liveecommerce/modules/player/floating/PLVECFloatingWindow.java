package com.easefun.polyv.liveecommerce.modules.player.floating;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.PLVFloatPermissionUtils;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVLiveLoginResult;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author Hoshiiro
 */
public class PLVECFloatingWindow {

    private static final String TAG = PLVECFloatingWindow.class.getSimpleName();

    private boolean requestShowByCommodityPage = false;
    private boolean requestShowByUser = false;
    @Nullable
    private PLVSwitchViewAnchorLayout contentAnchorLayout;
    @Nullable
    private View contentChild;
    @Nullable
    private IPLVLiveRoomDataManager liveRoomDataManager;

    private PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ONLY_FOREGROUND;

    private ViewGroup.LayoutParams originContentLayoutParam;

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
        this.contentChild = anchorLayout.getChildAt(0);
    }

    public void setLiveRoomData(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
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
            restoreContentLayoutParam();
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
                PLVFloatingPlayerManager.getInstance()
                        .setFloatingSize(
                                ConvertUtils.dp2px(90),
                                ConvertUtils.dp2px(160)
                        )
                        .setFloatingPosition(
                                ScreenUtils.getScreenOrientatedWidth() - ConvertUtils.dp2px(90 + 16),
                                ScreenUtils.getScreenOrientatedHeight() - ConvertUtils.dp2px(160 + 34)
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
                                restoreContentLayoutParam();
                                PLVFloatingPlayerManager.getInstance().clear();
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
                                requestShowByCommodityPage = false;
                                requestShowByUser = false;
                                restoreContentLayoutParam();
                                PLVFloatingPlayerManager.getInstance().clear();
                            }
                        })
                        .bindContentLayout(contentAnchorLayout)
                        .show();
                setContentMatchParent();
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
            new AlertDialog.Builder(context)
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

    private void setContentMatchParent() {
        if (contentChild == null) {
            return;
        }
        originContentLayoutParam = contentChild.getLayoutParams();
        contentChild.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void restoreContentLayoutParam() {
        if (contentChild == null || originContentLayoutParam == null) {
            return;
        }
        contentChild.setLayoutParams(originContentLayoutParam);
        originContentLayoutParam = null;
    }

    private boolean isNeedShow() {
        return requestShowByUser || requestShowByCommodityPage;
    }

}
