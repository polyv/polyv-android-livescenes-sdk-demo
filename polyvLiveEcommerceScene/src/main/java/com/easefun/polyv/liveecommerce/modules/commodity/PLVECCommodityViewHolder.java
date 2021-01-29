package com.easefun.polyv.liveecommerce.modules.commodity;

import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.commodity.PLVProductContentBean;

import java.text.NumberFormat;

/**
 * 商品viewHolder
 */
public class PLVECCommodityViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter> {
    private PLVProductContentBean contentsBean;
    private ImageView commodityCoverIv;
    private TextView commodityNumberTv;
    private TextView commodityNameTv;
    private TextView commodityRealPriceTv;
    private TextView commoditySrcPriceTv;
    private TextView commodityShelfTv;

    public PLVECCommodityViewHolder(View itemView, final PLVECCommodityAdapter adapter) {
        super(itemView, adapter);
        commodityCoverIv = findViewById(R.id.commodity_cover_iv);
        commodityNumberTv = findViewById(R.id.commodity_number_tv);
        commodityNameTv = findViewById(R.id.commodity_name_tv);
        commodityRealPriceTv = findViewById(R.id.commodity_real_price_tv);
        commoditySrcPriceTv = findViewById(R.id.commodity_src_price_tv);
        commoditySrcPriceTv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        commodityShelfTv = findViewById(R.id.commodity_shelf_tv);
        commodityShelfTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    adapter.callOnBuyCommodityClick(v, contentsBean);
                }
            }
        });
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        contentsBean = (PLVProductContentBean) data.getData();
        PLVImageLoader.getInstance().loadImage(contentsBean.getCover(), commodityCoverIv);
        commodityNumberTv.setText(String.valueOf(contentsBean.getShowId()));
        commodityNameTv.setText(contentsBean.getName());
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        commoditySrcPriceTv.setVisibility(contentsBean.getPrice() == contentsBean.getRealPrice() ? View.GONE : View.VISIBLE);
        commoditySrcPriceTv.setText("¥" + trimZero(nf.format(contentsBean.getPrice())));
        commodityRealPriceTv.setText(contentsBean.getRealPrice() == 0 ? "免费" : ("¥" + trimZero(nf.format(contentsBean.getRealPrice()) + "")));
        commodityShelfTv.setText("去购买");
        commodityShelfTv.setSelected(true);
    }

    public void updateNumberView(int showId) {
        commodityNumberTv.setText(String.valueOf(showId));
    }

    private String trimZero(String s) {
        if (s != null && s.indexOf('.') > 0) {
            // 去掉多余的0
            s = s.replaceAll("0+?$", "");
            // 如最后一位是.则去掉
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }
}
