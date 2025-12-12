package com.easefun.polyv.liveecommerce.modules.linkmic.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.ui.widget.PLVDragScaleLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVGradientView;
import com.easefun.polyv.livecommon.ui.widget.PLVLSNetworkQualityWidget;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.thirdpart.blankj.utilcode.util.BarUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author Hoshiiro
 */
public class PLVECSeparateLinkMicView extends PLVDragScaleLayout {

    private PLVRoundRectConstraintLayout linkmicSeparateViewLayoutBg;
    private PLVRoundRectConstraintLayout linkmicSeparateViewLayoutRoot;
    private FrameLayout linkmicSeparateRenderContainer;
    private FrameLayout linkmicSeparateMuteVideoMask;
    private PLVGradientView linkmicSeparateMask;
    private PLVLSNetworkQualityWidget linkmicSepareteNetworkQualityView;
    private ImageView linkmicSepareteMicVolumeIv;
    private TextView linkmicSepareteNameTv;
    private ImageView linkmicSeparateMaskIv;

    private boolean isAudio = false;

    @Nullable
    private PLVLinkMicItemDataBean itemDataBeanShowSeparate = null;

    @Nullable
    private View renderView = null;

    @Nullable
    private OnViewActionListener onViewActionListener = null;

    @Nullable
    private String myLinkMicId = null;
    private boolean isFirstShow = false;

    public PLVECSeparateLinkMicView(@NonNull Context context) {
        super(context);
    }

    public PLVECSeparateLinkMicView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVECSeparateLinkMicView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_linkmic_separate_view_layout, this);
        linkmicSeparateViewLayoutBg = findViewById(R.id.plvec_linkmic_separate_view_layout_bg);
        linkmicSeparateViewLayoutRoot = findViewById(R.id.plvec_linkmic_separate_view_layout_root);
        linkmicSeparateRenderContainer = findViewById(R.id.plvec_linkmic_separate_render_container);
        linkmicSeparateMuteVideoMask = findViewById(R.id.plvec_linkmic_separate_mute_video_mask);
        linkmicSeparateMask = findViewById(R.id.plvec_linkmic_separate_mask);
        linkmicSepareteNetworkQualityView = findViewById(R.id.plvec_linkmic_separete_network_quality_view);
        linkmicSepareteMicVolumeIv = findViewById(R.id.plvec_linkmic_separete_mic_volume_iv);
        linkmicSepareteNameTv = findViewById(R.id.plvec_linkmic_separete_name_tv);
        linkmicSeparateMaskIv = findViewById(R.id.plvec_linkmic_separate_mask_iv);

        linkmicSepareteNetworkQualityView.shouldShowNoNetworkHint(false);
        linkmicSepareteNetworkQualityView.setNetQualityRes(
                R.drawable.plv_network_signal_watcher_good,
                R.drawable.plv_network_signal_watcher_middle,
                R.drawable.plv_network_signal_watcher_poor
        );

        int margin = ConvertUtils.dp2px(8);
        setDragRange(margin, ScreenUtils.getScreenOrientatedWidth() - margin, margin, ScreenUtils.getScreenOrientatedHeight() + BarUtils.getStatusBarHeight() - margin);
        setAutoAttachEdgeResponseSize((ScreenUtils.getScreenOrientatedWidth() - margin - ConvertUtils.dp2px(90)) / 2);
        setFixSize(ConvertUtils.dp2px(90), ConvertUtils.dp2px(160));
    }

    public PLVECSeparateLinkMicView setMyLinkMicId(@Nullable String myLinkMicId) {
        this.myLinkMicId = myLinkMicId;
        return this;
    }

    public void updateLinkMicShowSeparateChanged(PLVLinkMicItemDataBean itemDataBeanShowSeparate) {
        if (this.itemDataBeanShowSeparate == itemDataBeanShowSeparate) {
            return;
        }
        removeSeparateLinkMicView();
        this.itemDataBeanShowSeparate = itemDataBeanShowSeparate;
        setVisibility(itemDataBeanShowSeparate == null ? View.GONE : View.VISIBLE);
        setupSeparateLinkMicView();
        setupPositionWhenFirstShow();
        updateBindingProperties(null);
    }

    public void setIsAudio(boolean isAudio) {
        this.isAudio = isAudio;
        if (isAudio) {
            linkmicSeparateMaskIv.setImageResource(R.drawable.plvec_linkmic_mute_video_audio);
        } else {
            linkmicSeparateMaskIv.setImageResource(R.drawable.plvec_linkmic_mute_video);
        }
    }

    public void updateBindingProperties(@Nullable String linkMicId) {
        if (itemDataBeanShowSeparate == null) {
            return;
        }
        if (linkMicId != null && !linkMicId.equals(itemDataBeanShowSeparate.getLinkMicId())) {
            return;
        }
        linkmicSeparateRenderContainer.setVisibility(itemDataBeanShowSeparate.isMuteVideo() ? View.GONE : View.VISIBLE);
        linkmicSeparateMuteVideoMask.setVisibility(itemDataBeanShowSeparate.isMuteVideo() ? View.VISIBLE : View.GONE);
        linkmicSepareteNetworkQualityView.setVisibility(myLinkMicId != null && myLinkMicId.equals(itemDataBeanShowSeparate.getLinkMicId()) ? View.VISIBLE : View.GONE);
        linkmicSepareteNameTv.setText(itemDataBeanShowSeparate.getNick());
        linkmicSepareteMicVolumeIv.setImageResource(imageResourceForVolumeState(itemDataBeanShowSeparate));
    }

    public void updateLocalLinkMicNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
        linkmicSepareteNetworkQualityView.setNetQuality(quality);
    }

    public PLVECSeparateLinkMicView setOnViewActionListener(@Nullable OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    private void setupSeparateLinkMicView() {
        if (onViewActionListener == null || itemDataBeanShowSeparate == null) {
            return;
        }
        renderView = onViewActionListener.createLinkMicRenderView();
        linkmicSeparateRenderContainer.addView(renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (renderView instanceof SurfaceView) {
            ((SurfaceView) renderView).setZOrderMediaOverlay(true);
        }
        updateShadowCheckWhetherRenderSupportClip();
        onViewActionListener.setupRenderView(renderView, itemDataBeanShowSeparate.getLinkMicId());
    }

    private void removeSeparateLinkMicView() {
        if (onViewActionListener == null || renderView == null) {
            return;
        }
        onViewActionListener.releaseRenderView(renderView);
        linkmicSeparateRenderContainer.removeAllViews();
        renderView = null;
    }

    private void updateShadowCheckWhetherRenderSupportClip() {
        if (renderView instanceof SurfaceView) {
            linkmicSeparateViewLayoutBg.setBackgroundColor(Color.TRANSPARENT);
        } else {
            linkmicSeparateViewLayoutBg.setBackgroundColor(PLVFormatUtils.parseColor("#29000000"));
        }
    }

    @DrawableRes
    private int imageResourceForVolumeState(@NonNull PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (linkMicItemDataBean.isMuteAudio()) {
            return R.drawable.plvec_linkmic_iv_mic_close;
        }
        int reducedVolume = ((linkMicItemDataBean.getCurVolume() + 5) / 10) * 10;
        try {
            return R.drawable.class.getDeclaredField("plvec_linkmic_mic_volume_" + reducedVolume).getInt(null);
        } catch (Throwable e) {
            return R.drawable.plvec_linkmic_mic_volume_10;
        }
    }

    private void setupPositionWhenFirstShow() {
        if (isFirstShow) {
            return;
        }
        isFirstShow = true;

        if (getParent() == null || !(getParent() instanceof ViewGroup)) {
            return;
        }
        ViewGroup parent = (ViewGroup) getParent();
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        lp.leftMargin = parent.getWidth() - lp.width - ConvertUtils.dp2px(8);
        lp.topMargin = parent.getHeight() - lp.height - ConvertUtils.dp2px(148);
        setLayoutParams(lp);
    }

    public interface OnViewActionListener {

        /**
         * 创建渲染器
         */
        View createLinkMicRenderView();

        /**
         * 配置渲染器
         */
        void setupRenderView(View view, String linkMicId);

        /**
         * 释放渲染器
         */
        void releaseRenderView(View view);

    }

}
