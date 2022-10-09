package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewFragment;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.model.PLVInvitePosterVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 分享布局
 */
public class PLVSAShareLayout extends FrameLayout implements View.OnClickListener {
    // <editor-folder defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVSAShareLayout.class.getSimpleName();
    // 按钮表格每行显示数量
    private static final int GRID_COLUMN_COUNT_PORT = 4;
    private static final int GRID_COLUMN_COUNT_LAND = 2;
    // 弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 分享布局高度
    private static final int MORE_LAYOUT_HEIGHT_PORT = ConvertUtils.dp2px(200);
    private static final int MORE_LAYOUT_HEIGHT_LAND = ViewGroup.LayoutParams.MATCH_PARENT;
    // 分享布局位置
    private static final int MORE_LAYOUT_GRAVITY_PORT = Gravity.BOTTOM;
    private static final int MORE_LAYOUT_GRAVITY_LAND = Gravity.END;
    // 分享布局背景
    private static final int MORE_LAYOUT_BACKGROUND_RES_PORT = R.drawable.plvsa_more_ly_shape;
    private static final int MORE_LAYOUT_BACKGROUND_RES_LAND = R.drawable.plvsa_more_ly_shape_land;

    // view
    private LinearLayout plvsaShareParentLy;
    private FrameLayout plvsaShareContentLy;
    private ViewGroup plvsaSharePosterLy;
    private ConstraintLayout plvsaShareLayout;
    private GridLayout plvsaShareSettingsLayout;
    private LinearLayout plvsaShareWeixinLy;
    private LinearLayout plvsaSharePengyouquanLy;
    private LinearLayout plvsaShareSaveLy;
    private LinearLayout plvsaShareCopyLy;
    private TextView plvsaShareCancelTv;

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private PLVSAMoreLayout.OnViewActionListener onViewActionListener;

    private String channelId;
    private String avatar;
    private String nickName;
    private String userId;
    private String watchUrl;
    private PosterFragment posterFragment;

    private Disposable getInvitePosterDisposable;
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="构造器">
    public PLVSAShareLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAShareLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAShareLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_share_layout, this);

        plvsaShareParentLy = (LinearLayout) findViewById(R.id.plvsa_share_parent_ly);
        plvsaShareContentLy = findViewById(R.id.plvsa_share_content_ly);
        plvsaSharePosterLy = findViewById(R.id.plvsa_share_poster_ly);
        plvsaShareLayout = (ConstraintLayout) findViewById(R.id.plvsa_share_layout);
        plvsaShareSettingsLayout = (GridLayout) findViewById(R.id.plvsa_share_settings_layout);
        plvsaShareWeixinLy = (LinearLayout) findViewById(R.id.plvsa_share_weixin_ly);
        plvsaSharePengyouquanLy = (LinearLayout) findViewById(R.id.plvsa_share_pengyouquan_ly);
        plvsaShareSaveLy = (LinearLayout) findViewById(R.id.plvsa_share_save_ly);
        plvsaShareCopyLy = (LinearLayout) findViewById(R.id.plvsa_share_copy_ly);
        plvsaShareCancelTv = (TextView) findViewById(R.id.plvsa_share_cancel_tv);

        plvsaShareWeixinLy.setOnClickListener(this);
        plvsaSharePengyouquanLy.setOnClickListener(this);
        plvsaShareSaveLy.setOnClickListener(this);
        plvsaShareCopyLy.setOnClickListener(this);
        plvsaShareCancelTv.setOnClickListener(this);

        findViewById(R.id.plvsa_share_save_iv).setOnClickListener(this);
        findViewById(R.id.plvsa_share_save_tv).setOnClickListener(this);
        findViewById(R.id.plvsa_share_copy_iv).setOnClickListener(this);
        findViewById(R.id.plvsa_share_copy_tv).setOnClickListener(this);
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

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        loadPoster();
        if (menuDrawer == null) {
            View containView = ((Activity) getContext()).findViewById(Window.ID_ANDROID_CONTENT);
            final int portraitHeight = Math.max(containView.getWidth(), containView.getHeight());
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setMenuSize(portraitHeight);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                    }

                    ViewGroup popupContainer = (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
                    View maskView = ((Activity) getContext()).findViewById(R.id.plvsa_popup_container_mask);
                    if (popupContainer.getChildCount() > 0) {
                        maskView.setVisibility(View.VISIBLE);
                    } else {
                        maskView.setVisibility(View.GONE);
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
        }

        updateViewWithOrientation();
        menuDrawer.openMenu();
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(PLVSAMoreLayout.OnViewActionListener listener) {
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
                        fragmentTransaction.replace(R.id.plvsa_share_poster_ly, posterFragment, "PosterFragment");
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

    // <editor-folder defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvsa_share_weixin_ly) {
        } else if (id == R.id.plvsa_share_pengyouquan_ly) {
        } else if (id == R.id.plvsa_share_save_iv || id == R.id.plvsa_share_save_tv) {
            if (posterFragment != null) {
                posterFragment.captureWebViewAndSave();
            }
        } else if (id == R.id.plvsa_share_copy_iv || id == R.id.plvsa_share_copy_tv) {
            PLVCopyBoardPopupWindow.copy(getContext(), watchUrl);
        } else if (id == R.id.plvsa_share_cancel_tv) {
            close();
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        Position menuPosition;
        GridLayout.LayoutParams gridLayoutParam1 = (GridLayout.LayoutParams) plvsaShareWeixinLy.getLayoutParams();
        GridLayout.LayoutParams gridLayoutParam2 = (GridLayout.LayoutParams) plvsaSharePengyouquanLy.getLayoutParams();
        GridLayout.LayoutParams gridLayoutParam3 = (GridLayout.LayoutParams) plvsaShareSaveLy.getLayoutParams();
        GridLayout.LayoutParams gridLayoutParam4 = (GridLayout.LayoutParams) plvsaShareCopyLy.getLayoutParams();
        LinearLayout.LayoutParams contentLayoutParam = (LinearLayout.LayoutParams) plvsaShareContentLy.getLayoutParams();
        LinearLayout.LayoutParams shareLayoutParam = (LinearLayout.LayoutParams) plvsaShareLayout.getLayoutParams();
        FrameLayout.LayoutParams sharePosterLayoutParam = (FrameLayout.LayoutParams) plvsaSharePosterLy.getLayoutParams();
        View containView = ((Activity) getContext()).findViewById(Window.ID_ANDROID_CONTENT);
        final int portraitHeight = Math.max(containView.getWidth(), containView.getHeight());
        final int portraitWidth = Math.min(containView.getWidth(), containView.getHeight());

        if (PLVScreenUtils.isPortrait(getContext())) {
            plvsaShareParentLy.setOrientation(LinearLayout.VERTICAL);
            contentLayoutParam.height = 0;
            contentLayoutParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
            setGridLayoutMargin(ConvertUtils.dp2px(4), ConvertUtils.dp2px(6), gridLayoutParam1, gridLayoutParam2, gridLayoutParam3, gridLayoutParam4);

            menuPosition = MENU_DRAWER_POSITION_PORT;
            shareLayoutParam.height = MORE_LAYOUT_HEIGHT_PORT;
            shareLayoutParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
            shareLayoutParam.gravity = MORE_LAYOUT_GRAVITY_PORT;
            plvsaShareSettingsLayout.setColumnCount(GRID_COLUMN_COUNT_PORT);
            plvsaShareLayout.setBackgroundResource(MORE_LAYOUT_BACKGROUND_RES_PORT);

            sharePosterLayoutParam.height = portraitHeight - ConvertUtils.dp2px(228);
            sharePosterLayoutParam.width = (int) (sharePosterLayoutParam.height * 0.56f);
            sharePosterLayoutParam.topMargin = ConvertUtils.dp2px(4);
            sharePosterLayoutParam.bottomMargin = ConvertUtils.dp2px(24);
            sharePosterLayoutParam.gravity = Gravity.CENTER_HORIZONTAL;
        } else {
            plvsaShareParentLy.setOrientation(LinearLayout.HORIZONTAL);
            contentLayoutParam.height = ViewGroup.LayoutParams.MATCH_PARENT;
            contentLayoutParam.width = 0;
            setGridLayoutMargin(ConvertUtils.dp2px(26), ConvertUtils.dp2px(26), gridLayoutParam1, gridLayoutParam2, gridLayoutParam3, gridLayoutParam4);

            menuPosition = MENU_DRAWER_POSITION_LAND;
            shareLayoutParam.height = MORE_LAYOUT_HEIGHT_LAND;
            shareLayoutParam.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            shareLayoutParam.gravity = MORE_LAYOUT_GRAVITY_LAND;
            plvsaShareSettingsLayout.setColumnCount(GRID_COLUMN_COUNT_LAND);
            plvsaShareLayout.setBackgroundResource(MORE_LAYOUT_BACKGROUND_RES_LAND);

            sharePosterLayoutParam.height = portraitWidth - ConvertUtils.dp2px(32);
            sharePosterLayoutParam.width = (int) (sharePosterLayoutParam.height * 0.56f);
            sharePosterLayoutParam.topMargin = ConvertUtils.dp2px(16);
            sharePosterLayoutParam.bottomMargin = ConvertUtils.dp2px(16);
            sharePosterLayoutParam.gravity = Gravity.CENTER;
        }

        if (menuDrawer != null) {
            menuDrawer.setPosition(menuPosition);
        }
        plvsaShareContentLy.setLayoutParams(contentLayoutParam);
        plvsaShareLayout.setLayoutParams(shareLayoutParam);
        plvsaSharePosterLy.setLayoutParams(sharePosterLayoutParam);
    }

    private void setGridLayoutMargin(int startEndValue, int bottomValue, GridLayout.LayoutParams... layoutParams) {
        for (GridLayout.LayoutParams layoutParam : layoutParams) {
            layoutParam.setMarginStart(startEndValue);
            layoutParam.setMarginEnd(startEndValue);
            layoutParam.bottomMargin = bottomValue;
        }
    }
    // </editor-fold>

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
