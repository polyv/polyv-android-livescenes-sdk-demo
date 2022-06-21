package com.easefun.polyv.livecommon.module.modules.reward.view.adapter;

import static com.easefun.polyv.livecommon.module.modules.reward.view.adapter.PLVRewardListAdapter.ITEM_GIFT_CASH_REWARD;
import static com.easefun.polyv.livecommon.module.modules.reward.view.adapter.PLVRewardListAdapter.ITEM_GIFT_POINT_REWARD;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.reward.view.vo.PLVRewardItemVO;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;

/**
 * 积分打赏礼物ViewHolder
 */
public class PLVRewardPointViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVRewardListAdapter> {
    private PLVRewardItemVO goodsBean;
    private ImageView plvIvRewardImage;
    private TextView plvTvRewardName;
    private TextView plvTvRewardPoint;

    public PLVRewardPointViewHolder(View itemView, PLVRewardListAdapter adapter) {
        super(itemView, adapter);

        plvIvRewardImage = itemView.findViewById(R.id.plv_iv_reward_image);
        plvTvRewardName = itemView.findViewById(R.id.plv_tv_reward_name);
        plvTvRewardPoint = itemView.findViewById(R.id.plv_tv_reward_point);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        goodsBean = (PLVRewardItemVO) data.getData();
        if (goodsBean == null) {
            return;
        }

        loadImage(goodsBean.getImg(), plvIvRewardImage);
        plvTvRewardName.setText(goodsBean.getName());
        if (data.getItemType() == ITEM_GIFT_POINT_REWARD) {
            plvTvRewardPoint.setText(goodsBean.getPrice() + goodsBean.getUnit());
        } else if (data.getItemType() == ITEM_GIFT_CASH_REWARD) {
            plvTvRewardPoint.setText("免费");
        }

    }

    private void loadImage(String url, ImageView iv) {
        if (!url.startsWith("http")) {
            url = "https:/" + url;
        }
        PLVImageLoader.getInstance().loadImage(itemView.getContext(), url, iv);
    }
}
