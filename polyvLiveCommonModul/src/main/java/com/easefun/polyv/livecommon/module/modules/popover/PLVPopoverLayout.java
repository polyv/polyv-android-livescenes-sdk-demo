package com.easefun.polyv.livecommon.module.modules.popover;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.commodity.PLVProductDetailLayout;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.interact.IPLVInteractLayout;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractLayout2;
import com.easefun.polyv.livecommon.module.modules.reward.OnPointRewardListener;
import com.easefun.polyv.livecommon.module.modules.reward.PLVPointRewardLayout;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.socket.event.interact.PLVShowProductDetailEvent;

import net.plv.android.jsbridge.CallBackFunction;

/**
 * date: 2021-2-24
 * author: ysh
 * description: 弹出窗口布局，该布局将专门用来展示'弹出类型的、需要覆盖在其他区域上'的一类视图
 * 目前封装了积分打赏、互动应用。
 */
public class PLVPopoverLayout extends RelativeLayout implements IPLVPopoverLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);
    private PLVPointRewardLayout plvLayoutReward;
    private IPLVInteractLayout plvLayoutInteract;
    private PLVProductDetailLayout plvLayoutProductDetail;
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">

    public PLVPopoverLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVPopoverLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPopoverLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口实现">

    @Override
    public void setOnPointRewardListener(OnPointRewardListener listener) {
        if (plvLayoutReward != null) {
            plvLayoutReward.setOnPointRewardListener(listener);
        }
    }

    @Override
    public boolean onBackPress() {
        if (plvLayoutProductDetail.onBackPress() || plvLayoutInteract.onBackPress() || plvLayoutReward.onBackPress()) {
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        plvLayoutInteract.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void init(PLVLiveScene scene, IPLVLiveRoomDataManager roomDataManager) {
        plvLayoutReward.initChannelConfig(PLVLiveChannelConfigFiller.generateNewChannelConfig(), roomDataManager);
        plvLayoutReward.changeScene(scene);
        plvLayoutInteract.init(roomDataManager, scene);
        plvLayoutProductDetail.init(roomDataManager);
    }

    @Override
    public void setOnOpenInsideWebViewListener(PLVInteractLayout2.OnOpenInsideWebViewListener listener) {
        plvLayoutInteract.setOnOpenInsideWebViewListener(listener);
    }


    @Override
    public void setOnClickProductListener(PLVInteractLayout2.OnClickProductListener listener) {
        plvLayoutInteract.setOnClickProductListener(listener);
    }

    @Override
    public IPLVInteractLayout getInteractLayout() {
        return plvLayoutInteract;
    }

    @Override
    public PLVPointRewardLayout getRewardView() {
        return plvLayoutReward;
    }

    @Override
    public PLVProductDetailLayout getProductDetailLayout() {
        return plvLayoutProductDetail;
    }

    @Override
    public void destroy() {
        if (plvLayoutInteract != null) {
            plvLayoutInteract.destroy();
        }
        if (plvLayoutProductDetail != null) {
            plvLayoutProductDetail.destroy();
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="私有接口">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_popover_layout, this, true);
        plvLayoutReward = findViewById(R.id.plv_layout_reward);

        plvLayoutInteract = findViewById(R.id.plv_layout_interact);
        plvLayoutInteract.setOnClickProductDetailListener(new PLVInteractLayout2.OnClickProductDetailListener() {
            @Override
            public void onClickProductDetail(PLVShowProductDetailEvent event) {
                plvLayoutProductDetail.showProductDetail(event.getProductId());
            }
        });

        plvLayoutProductDetail = findViewById(R.id.plv_layout_product_detail);
        plvLayoutProductDetail.setOnClickProductListener(new PLVProductDetailLayout.OnClickProductListener() {

            @Override
            public void onClickProduct(String param, CallBackFunction callBackFunction) {
                plvLayoutInteract.processClickProductEvent(param, callBackFunction);
            }
        });

        commodityViewModel.getProductDetailLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<Integer>() {
                    @Override
                    public void onChanged(@Nullable Integer integer) {
                        if (integer != null) {
                            plvLayoutProductDetail.showProductDetail(integer);
                        }
                    }
                });
    }
    // </editor-fold >


}
