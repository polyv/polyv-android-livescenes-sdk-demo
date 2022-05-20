package com.easefun.polyv.liveecommerce.modules.commodity;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.commodity.PLVProductContentBean;

/**
 * 商品推送布局
 */
public class PLVECCommodityPushLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private ImageView commodityCoverIv;
    private TextView commodityNumberTv;
    private TextView commodityNameTv;
    private TextView commodityRealPriceTv;
    private TextView commoditySrcPriceTv;
    private ImageView closeIv;
    private ImageView enterIv;

    private Runnable runnable;

    private int productId;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECCommodityPushLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECCommodityPushLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECCommodityPushLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_commodity_push_layout, this, true);

        commodityCoverIv = findViewById(R.id.commodity_cover_iv);
        commodityNumberTv = findViewById(R.id.commodity_number_tv);
        commodityNameTv = findViewById(R.id.commodity_name_tv);
        commodityRealPriceTv = findViewById(R.id.commodity_real_price_tv);
        commoditySrcPriceTv = findViewById(R.id.commodity_src_price_tv);
        closeIv = findViewById(R.id.plvec_playback_more_dialog_close_iv);
        enterIv = findViewById(R.id.enter_iv);

        closeIv.setOnClickListener(this);
        enterIv.setOnClickListener(this);

        commoditySrcPriceTv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void updateView(PLVProductContentBean productContentBean) {
        this.productId = productContentBean.getProductId();
        commodityNumberTv.setText(String.valueOf(productContentBean.getShowId()));
        PLVImageLoader.getInstance().loadImage(productContentBean.getCover(), commodityCoverIv);
        commodityNameTv.setText(productContentBean.getName());
        commoditySrcPriceTv.setVisibility(
                (productContentBean.isRealPriceEqualsPrice() || productContentBean.isSrcPriceZero()) ? GONE : VISIBLE);
        commoditySrcPriceTv.setText("¥" + productContentBean.getPrice());
        commodityRealPriceTv.setText(productContentBean.isFreeForPay() ? "免费" : ("¥" + productContentBean.getRealPrice()));
    }

    public void show() {
        setVisibility(View.VISIBLE);
        removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        };
        postDelayed(runnable, 5000);
    }

    public void hide() {
        setVisibility(View.GONE);
        removeCallbacks(runnable);
    }

    public int getProductId() {
        return productId;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvec_playback_more_dialog_close_iv) {
            setVisibility(View.GONE);
        } else if (id == R.id.enter_iv) {
            if (viewActionListener != null) {
                viewActionListener.onEnterClick();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    private ViewActionListener viewActionListener;

    public void setViewActionListener(ViewActionListener listener) {
        this.viewActionListener = listener;
    }

    public interface ViewActionListener {
        void onEnterClick();
    }
    // </editor-fold>
}
