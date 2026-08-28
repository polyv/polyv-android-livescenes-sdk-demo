package com.easefun.polyv.livecommon.module.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.LifecycleOwner;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.plv.livescenes.model.PLVComplianceContentVO;

/**
 * 手机开播合规提醒。确认状态按频道及角色永久保存在本地，清除应用数据后失效。
 */
public class PLVComplianceReminderHelper {

    private static final String PREFERENCES_NAME = "plv_streamer_compliance_reminder";
    private static final String CONFIRMED_PREFIX = "confirmed_";

    private final Activity activity;
    private final IPLVLiveRoomDataManager liveRoomDataManager;
    private final String channelId;
    private final String viewerType;
    private final SharedPreferences preferences;

    @Nullable
    private PLVComplianceContentVO complianceContent;
    private boolean requestFinished;
    @Nullable
    private Runnable pendingAction;
    private boolean showWhenLoaded;
    @Nullable
    private AlertDialog complianceDialog;
    @Nullable
    private AlertDialog disagreeDialog;

    public PLVComplianceReminderHelper(Activity activity, IPLVLiveRoomDataManager liveRoomDataManager) {
        this.activity = activity;
        this.liveRoomDataManager = liveRoomDataManager;
        channelId = liveRoomDataManager.getConfig().getChannelId();
        viewerType = liveRoomDataManager.getConfig().getUser().getViewerType();
        preferences = activity.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);

        liveRoomDataManager.getComplianceContentVO().observe((LifecycleOwner) activity, new Observer<PLVStatefulData<PLVComplianceContentVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PLVComplianceContentVO> data) {
                if (data == null || data.isLoading()) {
                    return;
                }
                requestFinished = true;
                complianceContent = data.isSuccess() ? data.getData() : null;
                if (pendingAction != null) {
                    Runnable action = pendingAction;
                    pendingAction = null;
                    runAfterConfirmed(action);
                } else if (showWhenLoaded) {
                    showWhenLoaded = false;
                    showIfNeeded();
                }
            }
        });
    }

    public void requestComplianceContent() {
        requestFinished = false;
        liveRoomDataManager.requestComplianceContent();
    }

    /** 嘉宾进入开播页面时调用；配置返回后立即展示。 */
    public void showIfNeededWhenLoaded() {
        if (requestFinished) {
            showIfNeeded();
        } else {
            showWhenLoaded = true;
        }
    }

    /** 主播点击开播/上课时调用；读取失败或配置未开启时直接执行 action。 */
    public void runAfterConfirmed(Runnable action) {
        if (!requestFinished) {
            pendingAction = action;
            return;
        }
        if (!needsConfirm()) {
            action.run();
            return;
        }
        showComplianceDialog(action);
    }

    private void showIfNeeded() {
        if (needsConfirm()) {
            showComplianceDialog(null);
        }
    }

    private boolean needsConfirm() {
        if (complianceContent == null || !complianceContent.isRemindEnabled()) {
            return false;
        }
        return !preferences.getBoolean(preferenceKey(), false);
    }

    private String preferenceKey() {
        return CONFIRMED_PREFIX + viewerType + channelId;
    }

    private void showComplianceDialog(@Nullable final Runnable action) {
        if (activity.isFinishing() || (complianceDialog != null && complianceDialog.isShowing())) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(complianceContent == null ? "" : complianceContent.getTitle())
                .setNegativeButton(R.string.plv_streamer_compliance_disagree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDisagreeDialog(action);
                    }
                })
                .setPositiveButton(R.string.plv_streamer_compliance_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (complianceContent != null) {
                            preferences.edit().putBoolean(preferenceKey(), true).apply();
                        }
                        if (action != null) {
                            action.run();
                        }
                    }
                })
                .setCancelable(false);
        String htmlContent = complianceContent == null ? "" : complianceContent.getContent();
        if (!TextUtils.isEmpty(htmlContent)) {
            final WebView webView = new WebView(activity);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.getSettings().setJavaScriptEnabled(false);
            // 追加底部安全间距，避免最后一行紧贴 WebView 边缘而显示不完整。
            String contentWithBottomSpace = htmlContent
                    + "<div style=\"height:24px;width:100%;clear:both;\"></div>";
            webView.loadDataWithBaseURL(null, contentWithBottomSpace, "text/html", "UTF-8", null);
            int horizontalPadding = dp(20);
            FrameLayout wrapper = new FrameLayout(activity);
            wrapper.setPadding(horizontalPadding, 0, horizontalPadding, 0);
            wrapper.addView(webView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, complianceContentHeight()));
            builder.setView(wrapper);
        }
        complianceDialog = builder.create();
        complianceDialog.setCanceledOnTouchOutside(false);
        complianceDialog.show();
    }

    private void showDisagreeDialog(@Nullable final Runnable action) {
        if (activity.isFinishing()) {
            return;
        }
        disagreeDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.plv_common_dialog_tip)
                .setMessage(R.string.plv_streamer_compliance_disagree_tips)
                .setPositiveButton(R.string.plv_streamer_compliance_back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showComplianceDialog(action);
                    }
                })
                .setCancelable(false)
                .create();
        disagreeDialog.setCanceledOnTouchOutside(false);
        disagreeDialog.show();
    }

    private int dp(int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5F);
    }

    /**
     * 横屏设备高度较小时，固定 260dp 会让 AlertDialog 裁剪 WebView 的底部；
     * 被裁剪区域仍计入 WebView 可视范围，从而出现“滚动到底仍看不全”。
     */
    private int complianceContentHeight() {
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int availableHeight = screenHeight - dp(180); // 预留标题、按钮及弹窗上下边距
        return Math.max(dp(120), Math.min(dp(260), availableHeight));
    }
}
