package com.easefun.polyv.livecloudclass.modules.chatroom.widget;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.PLVRedpackViewModel;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.vo.PLVDelayRedpackVO;
import com.easefun.polyv.livecommon.ui.util.PLVPopupHelper;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.rx.PLVTimer;
import com.plv.foundationsdk.utils.PLVTimeUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVLCRedpackView extends FrameLayout {

    private static final int HINT_POSITION_LEFT = 1;
    private static final int HINT_POSITION_TOP = 2;

    private final PLVRedpackViewModel redpackViewModel = PLVDependManager.getInstance().get(PLVRedpackViewModel.class);

    private ImageView redPackIv;
    private TextView redPackCountDownTv;

    @Nullable
    private PLVTriangleIndicateLayout hintLayout = null;
    @Nullable
    private PopupWindow hintWindow = null;

    private int hintPosition = 0;

    private PLVDelayRedpackVO delayRedpackVO;

    private Disposable countDownDisposable;

    public PLVLCRedpackView(@NonNull Context context) {
        super(context);
        initView(null);
    }

    public PLVLCRedpackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public PLVLCRedpackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    // <editor-fold defaultstate="collapsed" desc="初始化">

    private void initView(@Nullable AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_red_pack_widget_layout, this);

        redPackIv = findViewById(R.id.plvlc_red_pack_iv);
        redPackCountDownTv = findViewById(R.id.plvlc_red_pack_count_down_tv);

        parseAttrs(attrs);
        setOnClickListener();

        observeDelayRedpack();
        observeLifecycle();
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVLCRedpackView);

        hintPosition = typedArray.getInt(R.styleable.PLVLCRedpackView_plvlc_red_pack_hint_position, hintPosition);

        typedArray.recycle();
    }

    private void setOnClickListener() {
        redPackIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRedpackHint();
            }
        });
    }

    private void observeDelayRedpack() {
        redpackViewModel.getDelayRedpackLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVDelayRedpackVO>() {
            @Override
            public void onChanged(@Nullable PLVDelayRedpackVO delayRedpackVO) {
                if (delayRedpackVO == null) {
                    return;
                }
                PLVLCRedpackView.this.delayRedpackVO = delayRedpackVO;
                if (delayRedpackVO.getRedpackSendTime() > System.currentTimeMillis()) {
                    start();
                }
            }
        });
    }

    private void observeLifecycle() {
        if (getContext() instanceof LifecycleOwner) {
            ((LifecycleOwner) getContext()).getLifecycle().addObserver(new GenericLifecycleObserver() {
                @Override
                public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        stop();
                    }
                }
            });
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private Integer lastOrientation = null;

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (lastOrientation != null && lastOrientation != newConfig.orientation) {
            if (hintWindow != null && hintWindow.isShowing()) {
                hintWindow.dismiss();
            }
        }
        lastOrientation = newConfig.orientation;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理">

    private void start() {
        stop();
        countDownDisposable = PLVTimer.timer((int) seconds(1).toMillis(), new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (delayRedpackVO == null || delayRedpackVO.getRedpackSendTime() <= System.currentTimeMillis()) {
                    stop();
                    return;
                }
                redPackCountDownTv.setText(PLVTimeUtils.generateTime(delayRedpackVO.getRedpackSendTime() - System.currentTimeMillis()));
                setVisibility(VISIBLE);
            }
        });
    }

    private void stop() {
        if (countDownDisposable != null) {
            countDownDisposable.dispose();
            countDownDisposable = null;
        }
        setVisibility(GONE);
    }

    private void showRedpackHint() {
        if (hintPosition == 0) {
            return;
        }
        if (hintLayout == null) {
            hintLayout = (PLVTriangleIndicateLayout) LayoutInflater.from(getContext()).inflate(R.layout.plvlc_red_pack_hint_item, null);
        }

        if (hintPosition == HINT_POSITION_LEFT) {
            hintLayout.setTrianglePosition(PLVTriangleIndicateLayout.POSITION_RIGHT);
            hintWindow = PLVPopupHelper.show(redPackIv, hintLayout,
                    new PLVPopupHelper.ShowPopupConfig()
                            .setAutoHide(seconds(3))
                            .setPosition(PLVPopupHelper.PopupPosition.LEFT_CENTER)
                            .setFocusable(true)
                            .setOutsideTouchable(true)
            );
        } else if (hintPosition == HINT_POSITION_TOP) {
            hintLayout.setTrianglePosition(PLVTriangleIndicateLayout.POSITION_BOTTOM);
            hintWindow = PLVPopupHelper.show(redPackIv, hintLayout,
                    new PLVPopupHelper.ShowPopupConfig()
                            .setAutoHide(seconds(3))
                            .setPosition(PLVPopupHelper.PopupPosition.TOP_CENTER)
                            .setFocusable(true)
                            .setOutsideTouchable(true)
            );
        }
    }

    // </editor-fold>

}
