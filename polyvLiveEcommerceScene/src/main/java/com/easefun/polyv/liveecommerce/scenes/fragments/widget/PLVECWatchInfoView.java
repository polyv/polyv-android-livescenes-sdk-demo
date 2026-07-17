package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.liveecommerce.R;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;

/**
 * 直播信息view：推流logo，讲师名称，观看热度
 */
public class PLVECWatchInfoView extends FrameLayout {
    private ImageView avatarIv;
    private TextView nickTv;
    private TextView watchCountTv;
    private boolean watchCountEnabled = true;
    private int nickOriginTopMargin;

    public PLVECWatchInfoView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECWatchInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECWatchInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_widget_watch_info_layout, this);
        avatarIv = findViewById(R.id.avatar_iv);
        nickTv = findViewById(R.id.nick_tv);
        watchCountTv = findViewById(R.id.watch_count_tv);
        nickOriginTopMargin = ((RelativeLayout.LayoutParams) nickTv.getLayoutParams()).topMargin;
    }

    @SuppressLint("SetTextI18n")
    public void updateWatchCount(final Long watchCount) {
        if (!watchCountEnabled) {
            return;
        }
        String likesString = StringUtils.toKString(watchCount);
        watchCountTv.setText(likesString);
    }

    @SuppressLint("SetTextI18n")
    public void updateWatchInfo(String imageUrl, String publisherName) {
        PLVImageLoader.getInstance().loadImage(getContext(), imageUrl, avatarIv);
        nickTv.setText(publisherName);
    }

    @SuppressLint("SetTextI18n")
    public void updateWatchInfo(String imageUrl, String publisherName, long watchCount) {
        PLVImageLoader.getInstance().loadImage(getContext(), imageUrl, avatarIv);
        nickTv.setText(publisherName);
        updateWatchCount(watchCount);
    }

    public void setWatchCountEnabled(boolean enabled) {
        watchCountEnabled = enabled;
        watchCountTv.setVisibility(enabled ? View.VISIBLE : View.GONE);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) nickTv.getLayoutParams();
        if (enabled) {
            lp.removeRule(RelativeLayout.CENTER_VERTICAL);
            lp.topMargin = nickOriginTopMargin;
        } else {
            lp.addRule(RelativeLayout.CENTER_VERTICAL);
            lp.topMargin = 0;
        }
        nickTv.setLayoutParams(lp);
    }
}
