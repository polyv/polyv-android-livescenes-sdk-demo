package com.easefun.polyv.livecommon.module.modules.player.floating;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.PLVFloatingWindowManager;
import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Hoshiiro
 */
public class PLVFloatingPlayerManager {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private static final PLVFloatingPlayerManager INSTANCE = new PLVFloatingPlayerManager();

    private PLVFloatingPlayerManager() {
        floatingViewShowState.postValue(false);
    }

    public static PLVFloatingPlayerManager getInstance() {
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final Queue<Runnable> onClosedTaskQueue = new ArrayDeque<>();

    private final MutableLiveData<Boolean> floatingViewShowState = new MutableLiveData<>();

    @Nullable
    private String identifyTag = null;
    @Nullable
    private Intent savedLastIntent = null;
    @Nullable
    private Intent savedShowingFloatWindowIntent = null;
    @Nullable
    private PLVSwitchViewAnchorLayout contentOriginAnchorLayout = null;
    @Nullable
    private PLVFloatingPlayerView floatingView = null;
    @Nullable
    private PLVViewSwitcher viewSwitcher = null;

    private int left = 0;
    private int top = 0;
    private int width = 0;
    private int height = 0;
    private PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ALWAYS;

    @Nullable
    private OnCreateFloatingViewListener onCreateFloatingViewListener = null;
    @Nullable
    private OnGoBackListener onGoBackListener = null;
    @Nullable
    private OnCloseFloatingWindowListener onCloseFloatingWindowListener = null;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public PLVFloatingPlayerManager setTag(@Nullable String tag) {
        this.identifyTag = tag;
        return this;
    }

    public PLVFloatingPlayerManager saveIntent(@Nullable Intent intent) {
        savedLastIntent = intent;
        return this;
    }

    public PLVFloatingPlayerManager setOnCreateFloatingViewListener(@Nullable OnCreateFloatingViewListener onCreateFloatingViewListener) {
        this.onCreateFloatingViewListener = onCreateFloatingViewListener;
        return this;
    }

    public PLVFloatingPlayerManager setOnGoBackListener(@Nullable OnGoBackListener onGoBackListener) {
        this.onGoBackListener = onGoBackListener;
        return this;
    }

    public PLVFloatingPlayerManager setOnCloseFloatingWindowListener(@Nullable OnCloseFloatingWindowListener onCloseFloatingWindowListener) {
        this.onCloseFloatingWindowListener = onCloseFloatingWindowListener;
        return this;
    }

    public PLVFloatingPlayerManager bindContentLayout(@Nullable PLVSwitchViewAnchorLayout anchorLayout) {
        this.contentOriginAnchorLayout = anchorLayout;
        return this;
    }

    public PLVFloatingPlayerManager setFloatingPosition(int left, int top) {
        this.left = left;
        this.top = top;
        return this;
    }

    public PLVFloatingPlayerManager setFloatingSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public PLVFloatingPlayerManager updateShowType(PLVFloatingEnums.ShowType showType) {
        this.showType = showType;
        PLVFloatingWindowManager.getInstance().setShowType(showType);
        return this;
    }

    public PLVFloatingPlayerManager runOnFloatingWindowClosed(Runnable runnable) {
        if (!isFloatingWindowShowing()) {
            runnable.run();
            return this;
        }
        onClosedTaskQueue.add(runnable);
        return this;
    }

    public void show() {
        if (contentOriginAnchorLayout == null || isFloatingWindowShowing()) {
            return;
        }
        savedShowingFloatWindowIntent = savedLastIntent;
        floatingView = createFloatingView(contentOriginAnchorLayout.getContext());
        viewSwitcher = new PLVViewSwitcher();
        viewSwitcher.registerSwitchView(contentOriginAnchorLayout, floatingView.getAnchorLayout());
        viewSwitcher.switchView();
        showFloatingWindow();
        floatingViewShowState.postValue(true);
    }

    public void hide() {
        if (viewSwitcher == null || floatingView == null) {
            return;
        }
        closeFloatingWindow();
        final PLVSwitchViewAnchorLayout placeholderAnchorLayout = floatingView.getPlaceholderParentAnchorLayout();
        if (placeholderAnchorLayout != null) {
            viewSwitcher.registerSwitchView(placeholderAnchorLayout, floatingView.getAnchorLayout());
        }
        viewSwitcher.switchView();
        viewSwitcher = null;
        floatingView = null;

        runAllClosePendingTask();
        floatingViewShowState.postValue(false);
    }

    public void clear() {
        hide();
        contentOriginAnchorLayout = null;
        floatingView = null;
        viewSwitcher = null;
        onCreateFloatingViewListener = null;
        onGoBackListener = null;
        onCloseFloatingWindowListener = null;
    }

    public boolean isFloatingWindowShowing() {
        return viewSwitcher != null;
    }

    public LiveData<Boolean> getFloatingViewShowState() {
        return floatingViewShowState;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    @NonNull
    private PLVFloatingPlayerView createFloatingView(@NonNull Context context) {
        if (onCreateFloatingViewListener != null) {
            return onCreateFloatingViewListener.onCreateFloatingView(context);
        }
        return new PLVFloatingPlayerView(context)
                .setOnClickGoBackListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onGoBackListener != null) {
                            onGoBackListener.onGoBack(savedShowingFloatWindowIntent);
                        }
                    }
                })
                .setOnClickCloseListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onCloseFloatingWindowListener != null) {
                            onCloseFloatingWindowListener.onClosedFloatingWindow(identifyTag);
                        }
                    }
                });
    }

    private void showFloatingWindow() {
        if (floatingView == null) {
            return;
        }
        PLVFloatingWindowManager.getInstance().createNewWindow((Activity) floatingView.getContext())
                .setIsSystemWindow(true)
                .setContentView(floatingView)
                .setSize(width, height)
                .setFloatLocation(left, top)
                .setShowType(showType)
                .setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType.NO_AUTO_MOVE)
                .build()
                .show((Activity) floatingView.getContext());
    }

    private void closeFloatingWindow() {
        PLVFloatingWindowManager.getInstance().hide();
        PLVFloatingWindowManager.getInstance().destroy();
    }

    private void runAllClosePendingTask() {
        while (!onClosedTaskQueue.isEmpty()) {
            final Runnable task = onClosedTaskQueue.poll();
            if (task != null) {
                task.run();
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回调接口定义">

    public interface OnCreateFloatingViewListener {
        @NonNull
        PLVFloatingPlayerView onCreateFloatingView(@NonNull Context context);
    }

    public interface OnGoBackListener {
        void onGoBack(@Nullable Intent savedIntent);
    }

    public interface OnCloseFloatingWindowListener {
        void onClosedFloatingWindow(@Nullable String tag);
    }

    // </editor-fold>

}
