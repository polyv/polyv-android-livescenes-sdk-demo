package com.easefun.polyv.livecloudclass.modules.download.layout;

import static com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO.bytesToFitSizeString;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.download.PLVPlaybackCacheActivity;
import com.easefun.polyv.livecloudclass.modules.download.widget.PLVLCPlaybackCacheDownloadProgressButton;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheVideoViewModel;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVLCPlaybackCachePopupLayout extends FrameLayout implements View.OnClickListener {

    private final PLVPlaybackCacheVideoViewModel playbackCacheVideoViewModel = PLVDependManager.getInstance().get(PLVPlaybackCacheVideoViewModel.class);

    private TextView playbackCachePopupTitleTv;
    private ImageView playbackCachePopupCloseIv;
    private View playbackCacheSeparateLineView;
    private TextView playbackCacheVideoTitleTv;
    private TextView playbackCacheVideoSizeTv;
    private PLVLCPlaybackCacheDownloadProgressButton playbackCacheDownloadStatusBtn;
    private LinearLayout playbackCacheGoDownloadListLl;

    private PLVMenuDrawer menuDrawer;

    @Nullable
    private PLVPlaybackCacheVideoVO vo;

    public PLVLCPlaybackCachePopupLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_playback_cache_popup_layout, this);

        findView();
        observePlaybackCacheStatus();
    }

    private void findView() {
        playbackCachePopupTitleTv = findViewById(R.id.plvlc_playback_cache_popup_title_tv);
        playbackCachePopupCloseIv = findViewById(R.id.plvlc_playback_cache_popup_close_iv);
        playbackCacheSeparateLineView = findViewById(R.id.plvlc_playback_cache_separate_line_view);
        playbackCacheVideoTitleTv = findViewById(R.id.plvlc_playback_cache_video_title_tv);
        playbackCacheVideoSizeTv = findViewById(R.id.plvlc_playback_cache_video_size_tv);
        playbackCacheDownloadStatusBtn = findViewById(R.id.plvlc_playback_cache_download_status_btn);
        playbackCacheGoDownloadListLl = findViewById(R.id.plvlc_playback_cache_go_download_list_ll);

        this.setOnClickListener(this);
        playbackCachePopupCloseIv.setOnClickListener(this);
        playbackCacheGoDownloadListLl.setOnClickListener(new PLVDebounceClicker.OnClickListener(this));
        playbackCacheDownloadStatusBtn.setOnClickListener(new PLVDebounceClicker.OnClickListener(this));
    }

    private void observePlaybackCacheStatus() {
        playbackCacheVideoViewModel
                .getPlaybackCacheUpdateLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<PLVPlaybackCacheVideoVO>() {
                    @Override
                    public void onChanged(@Nullable PLVPlaybackCacheVideoVO videoVO) {
                        if (videoVO == null) {
                            return;
                        }
                        updateDownloadStatus(videoVO);
                    }
                });
    }

    public void show() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvlc_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setMenuSize(ScreenUtils.getScreenOrientatedHeight());
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {

                }
            });
            menuDrawer.openMenu();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void hide() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == playbackCachePopupCloseIv.getId() || id == this.getId()) {
            hide();
        } else if (id == playbackCacheGoDownloadListLl.getId()) {
            final Intent intent = new Intent(getContext(), PLVPlaybackCacheActivity.class);
            getContext().startActivity(intent);
        } else if (id == playbackCacheDownloadStatusBtn.getId()) {
            if (vo == null || vo.getDownloadStatusEnum() != PLVPlaybackCacheDownloadStatusEnum.NOT_IN_DOWNLOAD_LIST) {
                return;
            }
            requirePermissionThenRun(new Runnable() {
                @Override
                public void run() {
                    playbackCacheVideoViewModel.startDownload(vo);
                }
            });
        }
    }

    private void updateDownloadStatus(@NonNull PLVPlaybackCacheVideoVO vo) {
        this.vo = vo;
        playbackCacheVideoTitleTv.setText(vo.getTitle());
        playbackCacheVideoSizeTv.setText(bytesToFitSizeString(vo.getTotalBytes()));
        playbackCacheDownloadStatusBtn.update(vo);
    }

    private void requirePermissionThenRun(final Runnable runnable) {
        final List<String> requestPermissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PLVFastPermission.getInstance().start((Activity) getContext(), requestPermissions, new PLVOnPermissionCallback() {
            @Override
            public void onAllGranted() {
                runnable.run();
            }

            @Override
            public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {

            }
        });
    }
}
