package com.easefun.polyv.livecloudclass.modules.media.danmu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 弹幕包装器
 * <p>
 * DanmuController可见性：1.由横竖屏结合开发者竖屏开关决定。2.用户点击Danmu按钮决定
 * Danmu按钮可见性：由服务端开关决定。
 */
public class PLVLCDanmuWrapper {
    //弹幕按钮是否被打开
    boolean isDanmuToggleOpen = true;
    //弹幕竖屏开关(竖屏是否显示弹幕)
    boolean isEnableDanmuInPortrait = false;
    //弹幕在服务端的开关
    boolean isServerDanmuOpen = false;

    //弹幕控制器
    IPLVLCDanmuController danmuController;
    //anchorView
    View anchorView;
    //横屏弹幕开关按钮
    View danmuSwitchLandView;

    //页面方向改变监听器
    PLVOrientationManager.OnConfigurationChangedListener onConfigurationChangedListener;

    public PLVLCDanmuWrapper(@NonNull final View anchorView) {
        this.anchorView = anchorView;
        this.onConfigurationChangedListener = new PLVOrientationManager.OnConfigurationChangedListener() {
            @Override
            public void onCall(Context context, boolean isLandscape) {
                if (context == anchorView.getContext()) {
                    postRefreshDanmuStatus();
                }
            }
        };
        PLVOrientationManager.getInstance().addOnConfigurationChangedListener(onConfigurationChangedListener);
    }

    public void setDanmuController(@NonNull IPLVLCDanmuController danmuController) {
        this.danmuController = danmuController;
        init();
    }

    public void setDanmuSwitchLandView(@NonNull View danmuSwitchLandView) {
        this.danmuSwitchLandView = danmuSwitchLandView;
        danmuSwitchLandView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDanmu();
            }
        });
        init();
    }

    void init() {
        anchorView.post(new Runnable() {
            @Override
            public void run() {
                setupToggleDanmuStatus();
                refreshDanmuStatus();
            }
        });
    }

    public void release() {
        PLVOrientationManager.getInstance().removeOnConfigurationChangedListener(onConfigurationChangedListener);
    }

    // <editor-fold defaultstate="collapsed" desc="弹幕toggle">
    public void toggleDanmu() {
        isDanmuToggleOpen = !isDanmuToggleOpen;
        setupToggleDanmuStatus();
    }

    void setupToggleDanmuStatus() {
        if (danmuSwitchLandView != null) {
            danmuSwitchLandView.setSelected(!isDanmuToggleOpen);//select false -> open
        }
        if (isDanmuToggleOpen && isServerDanmuOpen) {
            if (danmuController != null) {
                danmuController.show();
            }
        } else {
            if (danmuController != null) {
                danmuController.hide();
            }
        }
    }
    // </editor-fold>

    public void setOnServerDanmuOpen(boolean isServerDanmuOpen) {
        this.isServerDanmuOpen = isServerDanmuOpen;
        refreshDanmuStatus();
    }

    /**
     * 在竖屏下也显示弹幕（默认不显示）
     */
    public void enableDanmuInPortrait() {
        isEnableDanmuInPortrait = true;
        refreshDanmuStatus();
    }

    public void postRefreshDanmuStatus() {
        anchorView.post(new Runnable() {
            @Override
            public void run() {
                refreshDanmuStatus();
            }
        });
    }

    public void refreshDanmuStatus() {
        if (isServerDanmuOpen) {
            if (danmuSwitchLandView != null) {
                danmuSwitchLandView.setVisibility(VISIBLE);
            }
            if (isEnableDanmuInPortrait) {
                if (isDanmuToggleOpen) {
                    if (danmuController != null) {
                        danmuController.show();
                    }
                } else {
                    if (danmuController != null) {
                        danmuController.hide();
                    }
                }
            } else {
                if (ScreenUtils.isPortrait()) {
                    if (danmuController != null) {
                        danmuController.hide();
                    }
                } else {
                    if (isDanmuToggleOpen) {
                        if (danmuController != null) {
                            danmuController.show();
                        }
                    } else {
                        if (danmuController != null) {
                            danmuController.hide();
                        }
                    }
                }
            }
        } else {
            //后台弹幕关闭
            if (danmuController != null) {
                danmuController.hide();
            }
            if (danmuSwitchLandView != null) {
                danmuSwitchLandView.setVisibility(GONE);
            }
        }
    }
}
