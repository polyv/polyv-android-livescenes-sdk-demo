package com.easefun.polyv.liveecommerce.modules.member;

import static com.plv.thirdpart.blankj.utilcode.util.ConvertUtils.dp2px;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.easefun.polyv.liveecommerce.R;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author Hoshiiro
 */
public class PLVECMemberListRuleDescriptionLayout extends FrameLayout {

    private PLVRoundRectConstraintLayout memberListRuleDescLayoutRoot;
    private ImageView memberListRuleDescBackIv;
    private TextView memberListRuleDescTitleTv;

    private PLVMenuDrawer menuDrawer;
    private boolean isPortraitOnShow = false;

    public PLVECMemberListRuleDescriptionLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVECMemberListRuleDescriptionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVECMemberListRuleDescriptionLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_member_list_rule_description_layout, this);

        memberListRuleDescLayoutRoot = findViewById(R.id.plvec_member_list_rule_desc_layout_root);
        memberListRuleDescBackIv = findViewById(R.id.plvec_member_list_rule_desc_back_iv);
        memberListRuleDescTitleTv = findViewById(R.id.plvec_member_list_rule_desc_title_tv);

        memberListRuleDescBackIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
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
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvec_popup_container)
            );
            menuDrawer.setMenuView(this);
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
        } else {
            menuDrawer.attachToContainer();
        }
        updateOrientationChanged();
        isPortraitOnShow = ScreenUtils.isPortrait();
        menuDrawer.openMenu();
    }

    public void hide() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    private void updateOrientationChanged() {
        final boolean isPortrait = ScreenUtils.isPortrait();
        if (menuDrawer != null) {
            menuDrawer.setPosition(isPortrait ? Position.BOTTOM : Position.END);
            menuDrawer.setMenuSize(isPortrait ? dp2px(512) : dp2px(375));
        }
        memberListRuleDescLayoutRoot.setRoundMode(isPortrait ? PLVRoundRectConstraintLayout.MODE_TOP : PLVRoundRectConstraintLayout.MODE_LEFT);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isPortraitOnShow != ScreenUtils.isPortrait()) {
            hide();
        }
    }

}
