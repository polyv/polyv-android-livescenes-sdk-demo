package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.liveecommerce.R;

/**
 * 直播信息view：推流logo，讲师名称，观看热度
 */
public class PLVECWatchInfoView extends FrameLayout {
    private ImageView avatarIv;
    private TextView nickTv;
    private TextView watchCountTv;

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
    }

    @SuppressLint("SetTextI18n")
    public void updateWatchCount(final Long watchCount) {
        watchCountTv.setText(watchCount + "");
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
        watchCountTv.setText(watchCount + "");
    }
}
