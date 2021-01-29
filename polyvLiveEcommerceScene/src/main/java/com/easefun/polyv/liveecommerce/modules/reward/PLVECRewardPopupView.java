package com.easefun.polyv.liveecommerce.modules.reward;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 打赏弹层view
 */
public class PLVECRewardPopupView {
    private PopupWindow popupWindow;
    private PLVECRewardGiftAdapter giftAdapter;
    private List<PLVCustomGiftBean> giftBeanList;

    public void showRewardLayout(View v, final PLVECRewardGiftAdapter.OnViewActionListener listener) {
        generateRewardGiftVO();
        if (popupWindow == null) {
            popupWindow = new PopupWindow(v.getContext());

            View.OnClickListener handleHideListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            };
            View view = PLVViewInitUtils.initPopupWindow(v, R.layout.plvec_live_reward_layout, popupWindow, handleHideListener);
            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            view.findViewById(R.id.close_iv).setOnClickListener(handleHideListener);
            final RecyclerView giftRv = view.findViewById(R.id.gift_rv);
            giftRv.setHasFixedSize(true);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(giftRv.getContext(), 5);
            giftRv.setLayoutManager(gridLayoutManager);
            giftAdapter = new PLVECRewardGiftAdapter();
            giftAdapter.setOnViewActionListener(new PLVECRewardGiftAdapter.OnViewActionListener() {
                @Override
                public void onRewardClick(View view, PLVCustomGiftBean giftBean) {
                    if (listener != null) {
                        listener.onRewardClick(view, giftBean);
                    }
                }
            });
            giftAdapter.setDataList(toViewDataList(giftBeanList));
            giftRv.setAdapter(giftAdapter);
        }
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    //生成打赏的礼物信息数据
    private void generateRewardGiftVO() {
        if (giftBeanList == null) {
            String[] giftTypes = new String[]{
                    PLVCustomGiftBean.GIFTTYPE_FLOWER,
                    PLVCustomGiftBean.GIFTTYPE_COFFEE,
                    PLVCustomGiftBean.GIFTTYPE_LIKES,
                    PLVCustomGiftBean.GIFTTYPE_CLAP,
                    PLVCustomGiftBean.GIFTTYPE_666,
                    PLVCustomGiftBean.GIFTTYPE_STARLET,
                    PLVCustomGiftBean.GIFTTYPE_DIAMOND,
                    PLVCustomGiftBean.GIFTTYPE_SPORTSCAR,
                    PLVCustomGiftBean.GIFTTYPE_ROCKET
            };
            List<PLVCustomGiftBean> giftBeanTempList = new ArrayList<>();
            for (String giftType : giftTypes) {
                giftBeanTempList.add(new PLVCustomGiftBean(giftType, PLVCustomGiftBean.getGiftName(giftType), 1));
            }
            this.giftBeanList = giftBeanTempList;
        }
    }

    private void setRewardGiftVO(List<PLVCustomGiftBean> giftBeanList) {
        this.giftBeanList = giftBeanList;
        if (giftAdapter != null) {
            giftAdapter.setDataList(toViewDataList(giftBeanList));
            giftAdapter.notifyDataSetChanged();
        }
    }

    private List<PLVBaseViewData> toViewDataList(List<PLVCustomGiftBean> giftBeanList) {
        List<PLVBaseViewData> viewDataList = new ArrayList<>();
        if (giftBeanList != null) {
            for (PLVCustomGiftBean giftBean : giftBeanList) {
                viewDataList.add(new PLVBaseViewData<>(giftBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));
            }
        }
        return viewDataList;
    }

    public void hide() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }
}
