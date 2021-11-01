package com.easefun.polyv.streameralone.modules.liveroom;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * 清屏指引布局
 *
 * @author suhongtao
 */
public class PLVSACleanUpLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private TextView plvsaLiveRoomCleanUpLabel;
    private ImageView plvsaLiveRoomCleanUpIconIv;
    private Button plvsaLiveRoomCleanUpConfirmBtn;

    // 清屏指引动画对象
    private ObjectAnimator cleanUpIconAnimator;

    // 是否正在直播
    private boolean isStreaming = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造函数">
    public PLVSACleanUpLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSACleanUpLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSACleanUpLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_clean_up_layout, this);
        findView();
        initConfirmBtnOnClickListener();

        setVisibility(GONE);
    }

    private void findView() {
        plvsaLiveRoomCleanUpLabel = (TextView) findViewById(R.id.plvsa_live_room_clean_up_label);
        plvsaLiveRoomCleanUpIconIv = (ImageView) findViewById(R.id.plvsa_live_room_clean_up_icon_iv);
        plvsaLiveRoomCleanUpConfirmBtn = (Button) findViewById(R.id.plvsa_live_room_clean_up_confirm_btn);
    }

    private void initConfirmBtnOnClickListener() {
        plvsaLiveRoomCleanUpConfirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) PLVSACleanUpLayout.this.getParent()).removeView(PLVSACleanUpLayout.this);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流 MVP - View">
    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    private IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void onStatesToStreamEnded() {
            setStreamerStatus(false);
        }

        @Override
        public void onStatesToStreamStarted() {
            setStreamerStatus(true);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View父类方法重写">
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (cleanUpIconAnimator != null) {
            cleanUpIconAnimator.cancel();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API 外部调用方法">
    public void setStreamerStatus(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    /**
     * 显示清屏指引布局
     *
     * @return 是否成功显示
     */
    public boolean show() {
        // 非直播状态，不显示清屏指引布局，避免一点击开播按钮就出现清屏指引提示
        if (!isStreaming) {
            return false;
        }
        if (cleanUpIconAnimator == null) {
            setupCleanUpIconAnimation();
        }
        cleanUpIconAnimator.start();
        setVisibility(VISIBLE);
        return true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理 - 动效初始化">

    /**
     * 初始化清屏指引动画
     * 清屏指引图标左右平移
     * <p>
     * 屏幕左右安全区域大小：竖屏50dp、横屏180dp
     */
    private void setupCleanUpIconAnimation() {
        final int safeMarginDp = PLVScreenUtils.isPortrait(getContext()) ? 50 : 180;
        final int cleanUpIconIvWidthDp = 123;
        final int availableTranslationWidth = ScreenUtils.getScreenOrientatedWidth() - ConvertUtils.dp2px(2 * safeMarginDp + cleanUpIconIvWidthDp);
        cleanUpIconAnimator = ObjectAnimator.ofFloat(plvsaLiveRoomCleanUpIconIv, "translationX", -availableTranslationWidth / 2F, availableTranslationWidth / 2F, -availableTranslationWidth / 2F);
        cleanUpIconAnimator.setDuration(2000);
        cleanUpIconAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    // </editor-fold>

}
