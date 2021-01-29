package com.easefun.polyv.liveecommerce.modules.reward;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.liveecommerce.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 打赏礼物viewHolder
 */
public class PLVECRewardGiftViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVECRewardGiftAdapter> {
    private PLVCustomGiftBean giftBean;
    private ImageView giftIv;
    private TextView giftTv;
    private TextView rewardTv;

    public PLVECRewardGiftViewHolder(final View itemView, final PLVECRewardGiftAdapter adapter) {
        super(itemView, adapter);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getLastSelectView() != null && adapter.getLastSelectView() != v) {
                    adapter.getLastSelectView().setSelected(false);
                    View rewardView = adapter.getLastSelectView().findViewById(R.id.reward_tv);
                    View giftView = adapter.getLastSelectView().findViewById(R.id.gift_tv);
                    if (rewardView != null) {
                        rewardView.setVisibility(View.GONE);
                    }
                    if (giftView != null) {
                        giftView.setTranslationY(ConvertUtils.dp2px(6));
                    }
                }
                if (v.isSelected()) {
                    adapter.callOnRewardClick(v, giftBean);
                }
                itemView.setSelected(true);
                adapter.setLastSelectView(v);
                rewardTv.setVisibility(View.VISIBLE);
                giftTv.setTranslationY(-ConvertUtils.dp2px(2));
            }
        });
        giftIv = findViewById(R.id.gift_iv);
        giftTv = findViewById(R.id.gift_tv);
        rewardTv = findViewById(R.id.reward_tv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        giftBean = (PLVCustomGiftBean) data.getData();
        int drawableId = itemView.getContext().getResources().getIdentifier("plvec_gift_" + giftBean.getGiftType(), "drawable", itemView.getContext().getPackageName());
        giftIv.setImageResource(drawableId);
        giftTv.setText(giftBean.getGiftName());
        if (itemView.isSelected()) {
            rewardTv.setVisibility(View.VISIBLE);
            giftTv.setTranslationY(-ConvertUtils.dp2px(2));
        } else {
            rewardTv.setVisibility(View.GONE);
            giftTv.setTranslationY(ConvertUtils.dp2px(6));
        }
    }
}
