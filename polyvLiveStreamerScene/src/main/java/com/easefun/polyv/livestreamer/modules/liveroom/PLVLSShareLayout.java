package com.easefun.polyv.livestreamer.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewFragment;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.model.PLVInvitePosterVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 分享布局
 */
public class PLVLSShareLayout extends FrameLayout implements View.OnClickListener {
    // <editor-folder defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLSShareLayout.class.getSimpleName();
    private PLVBlurView blurLy;
    private ViewGroup plvlsSharePosterLy;
    private LinearLayout plvlsShareWeixinItemLayout;
    private LinearLayout plvlsSharePengyouquanItemLayout;
    private LinearLayout plvlsShareSaveItemLayout;
    private LinearLayout plvlsShareCopyItemLayout;
    private TextView plvlsShareExitTv;

    private String channelId;
    private String avatar;
    private String nickName;
    private String userId;
    private String watchUrl;
    private PosterFragment posterFragment;

    // 布局弹层
    private PLVMenuDrawer menuDrawer;
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;

    private OnViewActionListener onViewActionListener;

    private Disposable updateBlurViewDisposable;
    private Disposable getInvitePosterDisposable;
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="构造器">
    public PLVLSShareLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSShareLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSShareLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_share_layout, this);

        blurLy = (PLVBlurView) findViewById(R.id.blur_ly);
        plvlsSharePosterLy = findViewById(R.id.plvls_share_poster_ly);
        plvlsShareWeixinItemLayout = (LinearLayout) findViewById(R.id.plvls_share_weixin_item_layout);
        plvlsSharePengyouquanItemLayout = (LinearLayout) findViewById(R.id.plvls_share_pengyouquan_item_layout);
        plvlsShareSaveItemLayout = (LinearLayout) findViewById(R.id.plvls_share_save_item_layout);
        plvlsShareCopyItemLayout = (LinearLayout) findViewById(R.id.plvls_share_copy_item_layout);
        plvlsShareExitTv = (TextView) findViewById(R.id.plvls_share_exit_tv);

        PLVBlurUtils.initBlurView(blurLy);

        plvlsShareWeixinItemLayout.setOnClickListener(this);
        plvlsSharePengyouquanItemLayout.setOnClickListener(this);
        plvlsShareSaveItemLayout.setOnClickListener(this);
        plvlsShareCopyItemLayout.setOnClickListener(this);
        plvlsShareExitTv.setOnClickListener(this);

        findViewById(R.id.plvls_share_save_iv).setOnClickListener(this);
        findViewById(R.id.plvls_share_save_tv).setOnClickListener(this);
        findViewById(R.id.plvls_share_copy_iv).setOnClickListener(this);
        findViewById(R.id.plvls_share_copy_tv).setOnClickListener(this);
    }

    private void initSharePosterLyWH(int layoutHeight) {
        int height = layoutHeight - ConvertUtils.dp2px(32);
        int width = (int) (height * 0.56f);
        MarginLayoutParams lp = (MarginLayoutParams) plvlsSharePosterLy.getLayoutParams();
        lp.height = height;
        lp.width = width;
        plvlsSharePosterLy.setLayoutParams(lp);
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="初始化数据">
    public void init(final IPLVLiveRoomDataManager liveRoomDataManager) {
        channelId = liveRoomDataManager.getConfig().getChannelId();
        avatar = liveRoomDataManager.getConfig().getUser().getViewerAvatar();
        nickName = liveRoomDataManager.getConfig().getUser().getViewerName();
        userId = liveRoomDataManager.getConfig().getUser().getViewerId();
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="布局控制">
    public void open() {
        loadPoster();
        if (menuDrawer == null) {
            View containView = ((Activity) getContext()).findViewById(Window.ID_ANDROID_CONTENT);
            initSharePosterLyWH(Math.min(containView.getWidth(), containView.getHeight()));
            final int portraitHeight = Math.max(containView.getWidth(), containView.getHeight());
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setMenuSize(portraitHeight);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        close();
        stopUpdateBlurViewTimer();
        if (getInvitePosterDisposable != null) {
            getInvitePosterDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="加载海报">
    private void loadPoster() {
        if (posterFragment != null) {
            return;
        }
        if (getInvitePosterDisposable != null) {
            getInvitePosterDisposable.dispose();
        }
        getInvitePosterDisposable = PLVChatApiRequestHelper.getInvitePoster(channelId)
                .subscribe(new Consumer<PLVInvitePosterVO>() {
                    @Override
                    public void accept(PLVInvitePosterVO responseBody) throws Exception {
                        if (responseBody.getSuccess() == null || !responseBody.getSuccess()) {
                            PLVCommonLog.exception(new Exception("loadPoster error: " + responseBody.getCode()));
                            return;
                        }
                        String url = responseBody.buildInviteUrl(channelId, userId, nickName, avatar);
                        watchUrl = responseBody.getData().getWatchUrl();
                        FragmentTransaction fragmentTransaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                        posterFragment = new PosterFragment();
                        posterFragment.init(url);
                        fragmentTransaction.replace(R.id.plvls_share_poster_ly, posterFragment, "PosterFragment");
                        fragmentTransaction.commitAllowingStateLoss();
                        PLVCommonLog.d(TAG, "poster url：" + url);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="定时更新模糊背景view">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        blurLy.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == plvlsShareWeixinItemLayout.getId()) {
        } else if (id == plvlsSharePengyouquanItemLayout.getId()) {
        } else if (id == R.id.plvls_share_save_iv || id == R.id.plvls_share_save_tv) {
            if (posterFragment != null) {
                posterFragment.captureWebViewAndSave();
            }
        } else if (id == R.id.plvls_share_copy_iv || id == R.id.plvls_share_copy_tv) {
            PLVCopyBoardPopupWindow.copy(getContext(), watchUrl);
        } else if (id == plvlsShareExitTv.getId()) {
            close();
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {

    }

    public static class PosterFragment extends PLVSimpleWebViewFragment {
        private String url;

        public void init(String url) {
            this.url = url;
        }

        @Override
        protected boolean isLoadUrl() {
            return true;
        }

        @Override
        protected String urlOrHtmlText() {
            return url;
        }

        @Override
        protected boolean canTouch() {
            return false;
        }
    }
    // </editor-fold>
}
