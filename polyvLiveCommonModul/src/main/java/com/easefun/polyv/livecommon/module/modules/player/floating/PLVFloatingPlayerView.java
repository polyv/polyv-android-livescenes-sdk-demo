package com.easefun.polyv.livecommon.module.modules.player.floating;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Space;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;

/**
 * @author Hoshiiro
 */
public class PLVFloatingPlayerView extends FrameLayout {

    private PLVSwitchViewAnchorLayout floatingContentSwitchAnchorLayout;
    private Space floatingPlaceholderSpace;
    private ImageView floatingCloseIv;

    public PLVFloatingPlayerView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_floating_content_layout, this);

        floatingContentSwitchAnchorLayout = findViewById(R.id.plv_floating_content_switch_anchor_layout);
        floatingPlaceholderSpace = findViewById(R.id.plv_floating_placeholder_space);
        floatingCloseIv = findViewById(R.id.plv_floating_close_iv);
    }

    public PLVSwitchViewAnchorLayout getAnchorLayout() {
        return floatingContentSwitchAnchorLayout;
    }

    @Nullable
    public PLVSwitchViewAnchorLayout getPlaceholderParentAnchorLayout() {
        if (floatingPlaceholderSpace.getParent() instanceof PLVSwitchViewAnchorLayout) {
            return (PLVSwitchViewAnchorLayout) floatingPlaceholderSpace.getParent();
        }
        return null;
    }

    public PLVFloatingPlayerView setOnClickGoBackListener(final View.OnClickListener onClickGoBackListener) {
        floatingContentSwitchAnchorLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickGoBackListener != null) {
                    onClickGoBackListener.onClick(v);
                }
            }
        });
        return this;
    }

    public PLVFloatingPlayerView setOnClickCloseListener(final View.OnClickListener onClickCloseListener) {
        floatingCloseIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickCloseListener != null) {
                    onClickCloseListener.onClick(v);
                }
            }
        });
        return this;
    }

}
