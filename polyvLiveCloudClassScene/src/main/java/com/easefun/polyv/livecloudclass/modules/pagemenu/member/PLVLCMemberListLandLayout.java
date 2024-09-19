package com.easefun.polyv.livecloudclass.modules.pagemenu.member;

import static com.plv.foundationsdk.utils.PLVTimeUnit.minutes;
import static com.plv.thirdpart.blankj.utilcode.util.ConvertUtils.dp2px;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.pagemenu.member.adapter.PLVLCMemberListAdapter;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.livescenes.model.PLVLiveViewerListVO;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVLCMemberListLandLayout extends FrameLayout {

    private TextView pageMenuMemberListRuleTv;
    private RecyclerView pageMenuMemberListRv;

    private final PLVLCMemberListRuleDescriptionLayout ruleDescriptionLayout = new PLVLCMemberListRuleDescriptionLayout(getContext());
    @Nullable
    private PLVMenuDrawer menuDrawer = null;

    private final PLVLCMemberListAdapter memberListAdapter = new PLVLCMemberListAdapter();

    @Nullable
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    private Disposable updateViewerListDisposable = null;

    public PLVLCMemberListLandLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLCMemberListLandLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLCMemberListLandLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_member_list_land_layout, this);
        pageMenuMemberListRuleTv = findViewById(R.id.plvlc_page_menu_member_list_rule_tv);
        pageMenuMemberListRv = findViewById(R.id.plvlc_page_menu_member_list_rv);

        pageMenuMemberListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        pageMenuMemberListRv.setAdapter(memberListAdapter);

        pageMenuMemberListRuleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                ruleDescriptionLayout.show();
            }
        });
    }

    public void show() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.END,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvlc_popup_container)
            );
            menuDrawer.setMenuSize(dp2px(375));
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateViewerList();
                    } else if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        stopUpdateViewerList();
                        menuDrawer.detachToContainer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {

                }
            });
        } else {
            menuDrawer.attachToContainer();
        }
        menuDrawer.openMenu();
    }

    public void hide() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void init(IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        this.chatroomPresenter = chatroomPresenter;
        if (chatroomPresenter != null) {
            observeLiveViewerListUpdated(chatroomPresenter);
        }
    }

    private void observeLiveViewerListUpdated(@NonNull IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        chatroomPresenter.registerView(new PLVAbsChatroomView() {
            @Override
            public void onLiveViewerListUpdate(List<PLVLiveViewerListVO.Data.LiveViewer> liveViewerList) {
                memberListAdapter.updateList(liveViewerList);
            }
        });
    }

    private void startUpdateViewerList() {
        stopUpdateViewerList();
        updateViewerListDisposable = PLVRxTimer.timer((int) minutes(1).toMillis(), new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (chatroomPresenter != null) {
                    chatroomPresenter.requestUpdateLiveViewerList();
                }
            }
        });
    }

    private void stopUpdateViewerList() {
        if (updateViewerListDisposable != null) {
            updateViewerListDisposable.dispose();
        }
        updateViewerListDisposable = null;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (ScreenUtils.isPortrait()) {
            hide();
        }
    }

}
