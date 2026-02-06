package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.PLVRedpackViewModel;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.vo.PLVDelayRedpackVO;
import com.easefun.polyv.livecommon.ui.util.PLVPopupHelper;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.rx.PLVTimer;
import com.plv.foundationsdk.utils.PLVTimeUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVECRedpackView extends FrameLayout {

    private final PLVRedpackViewModel redpackViewModel = PLVDependManager.getInstance().get(PLVRedpackViewModel.class);

    private ImageView redPackIv;
    private TextView redPackCountDownTv;

    @Nullable
    private PLVTriangleIndicateLayout hintLayout = null;

    private PLVDelayRedpackVO delayRedpackVO;

    private Disposable countDownDisposable;

    public PLVECRedpackView(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVECRedpackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVECRedpackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // <editor-fold defaultstate="collapsed" desc="初始化">

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_red_pack_widget_layout, this);

        redPackIv = findViewById(R.id.plvec_red_pack_iv);
        redPackCountDownTv = findViewById(R.id.plvec_red_pack_count_down_tv);

        redPackIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRedpackHint();
            }
        });

        observeDelayRedpack();
        observeLifecycle();
    }

    private void observeDelayRedpack() {
        redpackViewModel.getDelayRedpackLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVDelayRedpackVO>() {
            @Override
            public void onChanged(@Nullable PLVDelayRedpackVO delayRedpackVO) {
                if (delayRedpackVO == null) {
                    return;
                }
                PLVECRedpackView.this.delayRedpackVO = delayRedpackVO;
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

    // <editor-fold defaultstate="collapsed" desc="初始化数据">

    public void initData(IPLVLiveRoomDataManager liveRoomDataManager) {
        redpackViewModel.updateDelayRedpackStatus(liveRoomDataManager.getConfig().getChannelId());
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
        if (hintLayout == null) {
            hintLayout = (PLVTriangleIndicateLayout) LayoutInflater.from(getContext()).inflate(R.layout.plvec_red_pack_hint_item, null);
        }

        hintLayout.setTrianglePosition(PLVTriangleIndicateLayout.POSITION_RIGHT);
        PLVPopupHelper.show(redPackIv, hintLayout,
                new PLVPopupHelper.ShowPopupConfig()
                        .setAutoHide(seconds(3))
                        .setPosition(PLVPopupHelper.PopupPosition.LEFT_CENTER)
                        .setFocusable(true)
                        .setOutsideTouchable(true)
        );
    }

    // </editor-fold>

}
