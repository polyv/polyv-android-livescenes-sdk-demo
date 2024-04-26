package com.easefun.polyv.livestreamer.modules.liveroom.widget;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.component.kv.PLVAutoSaveKV;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLSNewLinkMicFirstIntroLayout extends FrameLayout implements View.OnClickListener {

    private static final PLVAutoSaveKV<Boolean> HAS_SHOWN_FIRST_INTRO = new PLVAutoSaveKV<Boolean>("PLVLSNewLinkMicFirstIntroLayout_HAS_SHOWED_FIRST_INTRO") {};

    private PLVRoundRectConstraintLayout liveRoomNewLinkMicFirstIntroMicIcon;
    private PLVRoundRectGradientTextView liveRoomNewLinkMicFirstIntroConfirmTv;

    public PLVLSNewLinkMicFirstIntroLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLSNewLinkMicFirstIntroLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLSNewLinkMicFirstIntroLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_new_link_mic_first_intro_layout, this);

        findView();
    }

    private void findView() {
        liveRoomNewLinkMicFirstIntroMicIcon = findViewById(R.id.plvls_live_room_new_link_mic_first_intro_mic_icon);
        liveRoomNewLinkMicFirstIntroConfirmTv = findViewById(R.id.plvls_live_room_new_link_mic_first_intro_confirm_tv);

        liveRoomNewLinkMicFirstIntroMicIcon.setOnClickListener(this);
        liveRoomNewLinkMicFirstIntroConfirmTv.setOnClickListener(this);
    }

    public static boolean hasShownFirstIntro() {
        return Boolean.TRUE.equals(HAS_SHOWN_FIRST_INTRO.get());
    }

    public void show() {
        if (hasShownFirstIntro()) {
            hide();
            return;
        }

        postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (bindAnchor()) {
                    Activity activity = (Activity) getContext();
                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    decorView.addView(PLVLSNewLinkMicFirstIntroLayout.this);
                    HAS_SHOWN_FIRST_INTRO.set(true);
                } else {
                    hide();
                }
            }
        });
    }

    public void hide() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    private boolean bindAnchor() {
        Activity activity = (Activity) getContext();
        View decorView = activity.getWindow().getDecorView();
        View anchor = decorView.findViewById(R.id.plvls_status_bar_allow_viewer_linkmic_iv);
        if (anchor == null) {
            return false;
        }
        Rect anchorRect = new Rect();
        boolean anchorVisible = anchor.getGlobalVisibleRect(anchorRect);
        if (!anchorVisible) {
            return false;
        }

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) liveRoomNewLinkMicFirstIntroMicIcon.getLayoutParams();
        lp.width = anchorRect.width() + ConvertUtils.dp2px(10);
        lp.height = anchorRect.height() + ConvertUtils.dp2px(10);
        lp.leftMargin = anchorRect.left - ConvertUtils.dp2px(4);
        lp.topMargin = anchorRect.top - ConvertUtils.dp2px(5);
        liveRoomNewLinkMicFirstIntroMicIcon.setLayoutParams(lp);
        return true;
    }

    @Override
    public void onClick(View v) {
        hide();
    }

}
