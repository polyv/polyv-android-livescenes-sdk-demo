package com.easefun.polyv.livehiclass.modules.linkmic.item;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livehiclass.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import static com.easefun.polyv.livehiclass.modules.linkmic.item.PLVHCLinkMicItemView.ViewParam.moveViewParam;

/**
 * 连麦item
 */
public class PLVHCLinkMicItemView extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVHCLinkMicItemView";
    //viewParam
    private ViewParam viewParam;
    private boolean isLargeLayout;

    //listener
    private OnRenderViewCallback onRenderViewCallback;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCLinkMicItemView(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCLinkMicItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLinkMicItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_linkmic_view_item, this, true);

        viewParam = new ViewParam(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void init(final boolean isLargeLayout, OnRenderViewCallback callback) {
        this.isLargeLayout = isLargeLayout;
        onRenderViewCallback = callback;

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getWidth() != 0) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ViewGroup.LayoutParams hasPaintIvLp = viewParam.plvhcLinkmicHasPaintIv.getLayoutParams();
                    hasPaintIvLp.height = ConvertUtils.dp2px(isLargeLayout ? 10 : 14);
                    hasPaintIvLp.width = ConvertUtils.dp2px(isLargeLayout ? 10 : 14);
                    viewParam.plvhcLinkmicHasPaintIv.setLayoutParams(hasPaintIvLp);

                    ViewGroup.LayoutParams cupLyLp = viewParam.plvhcLinkmicCupLy.getLayoutParams();
                    cupLyLp.height = ConvertUtils.dp2px(isLargeLayout ? 10 : 14);
                    viewParam.plvhcLinkmicCupLy.setLayoutParams(cupLyLp);

                    viewParam.plvhcLinkmicCupCountTv.setIncludeFontPadding(!isLargeLayout);

                    MarginLayoutParams handsUpIvLp = (MarginLayoutParams) viewParam.plvhcLinkmicHandsUpIv.getLayoutParams();
                    handsUpIvLp.width = ConvertUtils.dp2px(isLargeLayout ? 20 : 22);
                    handsUpIvLp.height = ConvertUtils.dp2px(isLargeLayout ? 22 : 24);
                    handsUpIvLp.rightMargin = (int) (getWidth() * (isLargeLayout ? 0.1f : 0.13f));
                    viewParam.plvhcLinkmicHandsUpIv.setLayoutParams(handsUpIvLp);
                }
            }
        });
    }

    public void bindData(PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (onRenderViewCallback == null ||
                linkMicItemDataBean == null) {
            return;
        }
        viewParam.linkMicItemDataBean = linkMicItemDataBean;

        boolean isMuteVideo = linkMicItemDataBean.isMuteVideo();
        boolean isMuteAudio = linkMicItemDataBean.isMuteAudio();
        int curVolume = linkMicItemDataBean.getCurVolume();
        String nick = linkMicItemDataBean.getNick();
        int cupNum = linkMicItemDataBean.getCupNum();
        boolean hasPaint = linkMicItemDataBean.isHasPaint();
        boolean isHandsUp = linkMicItemDataBean.isRaiseHand();

        //set video
        setupRenderView();
        setVideoStatus(isMuteVideo);
        //set audio
        setAudioStatus(isMuteAudio, curVolume);
        //set nick
        if (TextUtils.isEmpty(nick)) {
            viewParam.plvhcLinkmicNickTv.setText("");
        } else {
            viewParam.plvhcLinkmicNickTv.setText((linkMicItemDataBean.isTeacher() ? "老师-" : "") + nick);
        }
        //set cup
        setCupNum(cupNum);
        //set paint
        setHasPaint(hasPaint);
        //set handsUp
        setHandsUp(isHandsUp);
    }

    public PLVLinkMicItemDataBean getLinkMicItemDataBean() {
        return viewParam == null ? null : viewParam.linkMicItemDataBean;
    }

    public String getLinkMicId() {
        return getLinkMicItemDataBean() == null ? null : getLinkMicItemDataBean().getLinkMicId();
    }

    public void replaceItemView(PLVHCLinkMicItemView linkMicItemView) {
        ViewParam viewParam = new ViewParam();
        moveViewParam(viewParam, linkMicItemView.viewParam);
        linkMicItemView.removeView(viewParam.plvhcLinkmicParentLy);
        moveToItemView(linkMicItemView);
        addItemView(viewParam);
    }

    public void moveToItemView(PLVHCLinkMicItemView linkMicItemView) {
        removeView(viewParam.plvhcLinkmicParentLy);
        linkMicItemView.addView(viewParam.plvhcLinkmicParentLy);
        moveViewParam(linkMicItemView.viewParam, this.viewParam);
    }

    public void removeItemView(ViewParam vp) {
        removeView(viewParam.plvhcLinkmicParentLy);
        if (onRenderViewCallback != null) {
            if (viewParam.renderView != null) {
                onRenderViewCallback.releaseLinkMicRenderView(viewParam.renderView);
                viewParam.renderView = null;
            }
        }
        moveViewParam(vp, this.viewParam);
    }

    public void addItemView(ViewParam viewParam) {
        addView(viewParam.plvhcLinkmicParentLy);
        moveViewParam(this.viewParam, viewParam);
    }

    public void removeRenderView() {
        viewParam.plvhcLinkmicRenderViewContainer.setVisibility(View.INVISIBLE);
        //一并改变渲染器的可见性
        if (viewParam.renderView != null) {
            viewParam.renderView.setVisibility(View.INVISIBLE);
        }
        //将渲染器从View tree中移除（在部分华为机型上发现渲染器的SurfaceView隐藏后还会叠加显示）
        if (viewParam.renderView != null && viewParam.renderView.getParent() != null) {
            viewParam.plvhcLinkmicRenderViewContainer.removeView(viewParam.renderView);
        }

        if (onRenderViewCallback != null) {
            if (viewParam.renderView != null) {
                onRenderViewCallback.releaseLinkMicRenderView(viewParam.renderView);
                viewParam.renderView = null;
            }
        }
    }

    public void setupRenderView() {
        if (viewParam.linkMicItemDataBean == null) {
            return;
        }
        String linkMicId = viewParam.linkMicItemDataBean.getLinkMicId();
        if (viewParam.renderView == null) {
            viewParam.renderView = onRenderViewCallback.createLinkMicRenderView();
            if (viewParam.renderView != null) {
                viewParam.plvhcLinkmicRenderViewContainer.addView(viewParam.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            } else {
                PLVCommonLog.d(TAG, "create render view return null at position:" + getTag());
            }
        }
        if (viewParam.renderView != null && !TextUtils.isEmpty(linkMicId)) {
            onRenderViewCallback.setupRenderView(viewParam.renderView, linkMicId, viewParam.linkMicItemDataBean.getStreamType());
        }
    }

    public void updateTeacherPreparingStatus(boolean isPreparing) {
        viewParam.plvhcLinkmicTeacherAvatarPlaceholderIv.setVisibility(isPreparing ? View.VISIBLE : View.GONE);
        viewParam.plvhcLinkmicTeacherPrepareTv.setVisibility(isPreparing ? View.VISIBLE : View.GONE);
    }

    public void updateVideoStatus() {
        if (viewParam.linkMicItemDataBean == null) {
            return;
        }
        boolean isMuteVideo = viewParam.linkMicItemDataBean.isMuteVideo();
        setVideoStatus(isMuteVideo);
    }

    public void updateAudioStatus() {
        if (viewParam.linkMicItemDataBean == null) {
            return;
        }
        boolean isMuteAudio = viewParam.linkMicItemDataBean.isMuteAudio();
        int curVolume = viewParam.linkMicItemDataBean.getCurVolume();
        setAudioStatus(isMuteAudio, curVolume);
    }

    public void updateHandsUp() {
        if (viewParam.linkMicItemDataBean == null) {
            return;
        }
        boolean isHandsUp = viewParam.linkMicItemDataBean.isRaiseHand();
        setHandsUp(isHandsUp);
    }

    public void updateHasPaint() {
        if (viewParam.linkMicItemDataBean == null) {
            return;
        }
        boolean isHasPaint = viewParam.linkMicItemDataBean.isHasPaint();
        setHasPaint(isHasPaint);
    }

    public void updateCupNum() {
        if (viewParam.linkMicItemDataBean == null) {
            return;
        }
        int cupNum = viewParam.linkMicItemDataBean.getCupNum();
        setCupNum(cupNum);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现View方法">
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (onRenderViewCallback != null) {
            if (viewParam.renderView != null) {
                onRenderViewCallback.releaseLinkMicRenderView(viewParam.renderView);
                viewParam.renderView = null;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void setHandsUp(boolean isHandsUp) {
        viewParam.plvhcLinkmicHandsUpIv.setVisibility(isHandsUp ? View.VISIBLE : View.GONE);
    }

    private void setHasPaint(boolean isHasPaint) {
        viewParam.plvhcLinkmicHasPaintIv.setVisibility(isHasPaint ? View.VISIBLE : View.GONE);
    }

    private void setCupNum(int cupNum) {
        viewParam.plvhcLinkmicCupLy.setVisibility(cupNum > 0 ? View.VISIBLE : View.GONE);
        viewParam.plvhcLinkmicCupCountTv.setText(String.valueOf(cupNum));
    }

    private void setAudioStatus(boolean isMuteAudio, int curVolume) {
        //设置麦克风状态
        if (isMuteAudio) {
            viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_close);
        } else {
            if (intBetween(curVolume, 0, 5) || curVolume == 0) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_open);
            } else if (intBetween(curVolume, 5, 15)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_10);
            } else if (intBetween(curVolume, 15, 25)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_20);
            } else if (intBetween(curVolume, 25, 35)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_30);
            } else if (intBetween(curVolume, 35, 45)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_40);
            } else if (intBetween(curVolume, 45, 55)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_50);
            } else if (intBetween(curVolume, 55, 65)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_60);
            } else if (intBetween(curVolume, 65, 75)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_70);
            } else if (intBetween(curVolume, 75, 85)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_80);
            } else if (intBetween(curVolume, 85, 95)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_90);
            } else if (intBetween(curVolume, 95, 100)) {
                viewParam.plvhcLinkmicMicStateIv.setImageResource(R.drawable.plvhc_linkmic_mic_volume_100);
            }
        }
    }

    private void setVideoStatus(boolean isMuteVideo) {
        //是否关闭摄像头
        if (isMuteVideo) {
            viewParam.plvhcLinkmicRenderViewContainer.setVisibility(View.INVISIBLE);
            //一并改变渲染器的可见性
            if (viewParam.renderView != null) {
                viewParam.renderView.setVisibility(View.INVISIBLE);
            }
            //将渲染器从View tree中移除（在部分华为机型上发现渲染器的SurfaceView隐藏后还会叠加显示）
            if (viewParam.renderView != null && viewParam.renderView.getParent() != null) {
                viewParam.plvhcLinkmicRenderViewContainer.removeView(viewParam.renderView);
            }
        } else {
            viewParam.plvhcLinkmicRenderViewContainer.setVisibility(View.VISIBLE);
            //一并改变渲染器的可见性
            if (viewParam.renderView != null) {
                viewParam.renderView.setVisibility(View.VISIBLE);
            }
            //将渲染器从View 添加到view tree中
            if (viewParam.renderView != null && viewParam.renderView.getParent() == null) {
                viewParam.plvhcLinkmicRenderViewContainer.addView(viewParam.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
        FrameLayout.LayoutParams nickTvParams = (LayoutParams) viewParam.plvhcLinkmicNickTv.getLayoutParams();
        if (nickTvParams != null) {
            nickTvParams.gravity = isMuteVideo ? Gravity.CENTER : Gravity.BOTTOM;
            nickTvParams.leftMargin = ConvertUtils.dp2px(isMuteVideo ? 8 : 12);
            nickTvParams.rightMargin = ConvertUtils.dp2px(isMuteVideo ? 8 : 4);
            viewParam.plvhcLinkmicNickTv.setTextSize(isMuteVideo ? 13 : 8);
            viewParam.plvhcLinkmicNickTv.setLayoutParams(nickTvParams);
        }
    }

    private boolean intBetween(int value, int left, int right) {
        return value > left && value <= right;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="内部类 - 监听器">
    public interface OnRenderViewCallback {
        /**
         * 创建连麦列表渲染器。
         * 该渲染器必须通过多场景连麦SDK创建，不能直接构造。
         *
         * @return 渲染器
         */
        SurfaceView createLinkMicRenderView();

        /**
         * 释放渲染器
         *
         * @param renderView 渲染器
         */
        void releaseLinkMicRenderView(SurfaceView renderView);

        /**
         * 安装SurfaceView。
         * 将创建好的SurfaceView与连麦ID关联，并设置到SDK
         *
         * @param surfaceView 渲染器
         * @param linkMicId   连麦ID
         * @param streamType  流类型
         */
        void setupRenderView(SurfaceView surfaceView, String linkMicId, int streamType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - ViewParams">
    public static class ViewParam {
        //view
        private ViewGroup plvhcLinkmicParentLy;
        private ImageView plvhcLinkmicTeacherAvatarPlaceholderIv;
        private TextView plvhcLinkmicTeacherPrepareTv;
        private FrameLayout plvhcLinkmicRenderViewContainer;
        private ImageView plvhcLinkmicMicStateIv;
        private TextView plvhcLinkmicNickTv;
        private ViewGroup plvhcLinkmicCupLy;
        private TextView plvhcLinkmicCupCountTv;
        private ImageView plvhcLinkmicHasPaintIv;
        private ImageView plvhcLinkmicHandsUpIv;
        private SurfaceView renderView;

        //data
        private PLVLinkMicItemDataBean linkMicItemDataBean;

        public ViewParam() {
        }

        public ViewParam(View itemView) {
            plvhcLinkmicParentLy = itemView.findViewById(R.id.plvhc_linkmic_parent_ly);
            plvhcLinkmicRenderViewContainer = itemView.findViewById(R.id.plvhc_linkmic_render_view_container);
            plvhcLinkmicTeacherAvatarPlaceholderIv = itemView.findViewById(R.id.plvhc_linkmic_teacher_avatar_placeholder_iv);
            plvhcLinkmicTeacherPrepareTv = itemView.findViewById(R.id.plvhc_linkmic_teacher_prepare_tv);
            plvhcLinkmicMicStateIv = itemView.findViewById(R.id.plvhc_linkmic_mic_state_iv);
            plvhcLinkmicNickTv = itemView.findViewById(R.id.plvhc_linkmic_nick_tv);
            plvhcLinkmicCupLy = itemView.findViewById(R.id.plvhc_linkmic_cup_ly);
            plvhcLinkmicCupCountTv = itemView.findViewById(R.id.plvhc_linkmic_cup_count_tv);
            plvhcLinkmicHasPaintIv = itemView.findViewById(R.id.plvhc_linkmic_has_paint_iv);
            plvhcLinkmicHandsUpIv = itemView.findViewById(R.id.plvhc_linkmic_hands_up_iv);
        }

        public static void moveViewParam(ViewParam dest, ViewParam src) {
            dest.plvhcLinkmicParentLy = src.plvhcLinkmicParentLy;
            dest.linkMicItemDataBean = src.linkMicItemDataBean;
            dest.renderView = src.renderView;
            dest.plvhcLinkmicRenderViewContainer = src.plvhcLinkmicRenderViewContainer;
            dest.plvhcLinkmicTeacherAvatarPlaceholderIv = src.plvhcLinkmicTeacherAvatarPlaceholderIv;
            dest.plvhcLinkmicTeacherPrepareTv = src.plvhcLinkmicTeacherPrepareTv;
            dest.plvhcLinkmicMicStateIv = src.plvhcLinkmicMicStateIv;
            dest.plvhcLinkmicNickTv = src.plvhcLinkmicNickTv;
            dest.plvhcLinkmicCupLy = src.plvhcLinkmicCupLy;
            dest.plvhcLinkmicCupCountTv = src.plvhcLinkmicCupCountTv;
            dest.plvhcLinkmicHasPaintIv = src.plvhcLinkmicHasPaintIv;
            dest.plvhcLinkmicHandsUpIv = src.plvhcLinkmicHandsUpIv;

            src.linkMicItemDataBean = null;
            src.renderView = null;
            src.plvhcLinkmicRenderViewContainer = null;
            src.plvhcLinkmicTeacherAvatarPlaceholderIv = null;
            src.plvhcLinkmicTeacherPrepareTv = null;
            src.plvhcLinkmicMicStateIv = null;
            src.plvhcLinkmicNickTv = null;
            src.plvhcLinkmicCupLy = null;
            src.plvhcLinkmicCupCountTv = null;
            src.plvhcLinkmicHasPaintIv = null;
            src.plvhcLinkmicHandsUpIv = null;
        }
    }
    // </editor-fold>
}
