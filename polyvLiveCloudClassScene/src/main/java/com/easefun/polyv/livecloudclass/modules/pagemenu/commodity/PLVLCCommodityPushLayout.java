package com.easefun.polyv.livecloudclass.modules.pagemenu.commodity;

import static com.plv.thirdpart.blankj.utilcode.util.ConvertUtils.dp2px;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Paint;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.socket.event.commodity.PLVProductContentBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品推送布局
 */
public class PLVLCCommodityPushLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private PLVRoundRectLayout commodityCoverLy;
    private ImageView commodityCoverIv;
    private TextView commodityCoverNumberTv;
    private LinearLayout commodityNameLl;
    private PLVRoundRectGradientTextView commodityNameNumberTv;
    private TextView commodityNameTv;
    private LinearLayout commodityFeatureTagLl;
    private TextView commodityProductDescTv;
    private TextView commodityRealPriceTv;
    private TextView commoditySrcPriceTv;
    private ImageView commodityDialogCloseIv;
    private ImageView commodityEnterIv;

    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);

    @Nullable
    private PLVProductContentBean productContentBean;

    private boolean showOnPortrait = true;
    private boolean showOnLandscape = true;

    private boolean isNeedShow = false;
    private boolean isLandscape = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCCommodityPushLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCCommodityPushLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCCommodityPushLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView(@Nullable AttributeSet attributeSet) {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_page_menu_commodity_push_layout, this, true);

        findView();
        parseAttrs(attributeSet);

        commoditySrcPriceTv.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | commoditySrcPriceTv.getPaintFlags());

        observeCommodityViewModel();
    }

    private void findView() {
        commodityCoverLy = findViewById(R.id.plvlc_commodity_cover_ly);
        commodityCoverIv = findViewById(R.id.plvlc_commodity_cover_iv);
        commodityCoverNumberTv = findViewById(R.id.plvlc_commodity_cover_number_tv);
        commodityNameLl = findViewById(R.id.plvlc_commodity_name_ll);
        commodityNameNumberTv = findViewById(R.id.plvlc_commodity_name_number_tv);
        commodityNameTv = findViewById(R.id.plvlc_commodity_name_tv);
        commodityFeatureTagLl = findViewById(R.id.plvlc_commodity_feature_tag_ll);
        commodityProductDescTv = findViewById(R.id.plvlc_commodity_product_desc_tv);
        commodityRealPriceTv = findViewById(R.id.plvlc_commodity_real_price_tv);
        commoditySrcPriceTv = findViewById(R.id.plvlc_commodity_src_price_tv);
        commodityDialogCloseIv = findViewById(R.id.plvlc_commodity_dialog_close_iv);
        commodityEnterIv = findViewById(R.id.plvlc_commodity_enter_iv);

        commodityDialogCloseIv.setOnClickListener(this);
        commodityEnterIv.setOnClickListener(this);
        this.setOnClickListener(this);
    }

    private void parseAttrs(@Nullable AttributeSet attributeSet) {
        if (attributeSet == null) {
            return;
        }
        final TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PLVLCCommodityPushLayout);
        showOnPortrait = typedArray.getBoolean(R.styleable.PLVLCCommodityPushLayout_plv_show_on_portrait, showOnPortrait);
        showOnLandscape = typedArray.getBoolean(R.styleable.PLVLCCommodityPushLayout_plv_show_on_landscape, showOnLandscape);
        typedArray.recycle();
    }

    private void observeCommodityViewModel() {
        commodityViewModel.getCommodityUiStateLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<PLVCommodityUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVCommodityUiState uiState) {
                        if (uiState == null) {
                            return;
                        }
                        if (uiState.productContentBeanPushToShow != null) {
                            updateProduct(uiState.productContentBeanPushToShow);
                        }
                        isNeedShow = uiState.productContentBeanPushToShow != null;
                        checkUpdateVisibility();
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI更新">

    @MainThread
    private void updateProduct(PLVProductContentBean productContentBean) {
        this.productContentBean = productContentBean;

        bindCover(productContentBean);
        bindProductName(productContentBean);

        final boolean hasProductDesc = !TextUtils.isEmpty(productContentBean.getProductDesc());
        commodityProductDescTv.setVisibility(hasProductDesc ? View.VISIBLE : View.GONE);
        commodityProductDescTv.setText(productContentBean.getProductDesc());

        parseProductFeatureTag(productContentBean);
        bindPrice(productContentBean);
        bindEnterIcon(productContentBean);
    }

    @MainThread
    private void checkUpdateVisibility() {
        final boolean show = isNeedShow;
        final boolean fitOrientation = (!isLandscape && showOnPortrait) || (isLandscape && showOnLandscape);
        final boolean needShow = show && fitOrientation;
        setVisibility(needShow ? View.VISIBLE : View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == commodityDialogCloseIv.getId()) {
            isNeedShow = false;
            commodityViewModel.onCloseProductPush();
            checkUpdateVisibility();
        } else if (id == commodityEnterIv.getId() || v == this) {
            if (enterCommodity()) {
                isNeedShow = false;
                commodityViewModel.onCloseProductPush();
                checkUpdateVisibility();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        checkUpdateVisibility();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void bindCover(PLVProductContentBean productContentBean) {
        final boolean showCover = !TextUtils.isEmpty(productContentBean.getCover());
        if (!showCover) {
            commodityCoverLy.setVisibility(GONE);
            return;
        }
        commodityCoverLy.setVisibility(VISIBLE);
        commodityCoverNumberTv.setText(String.valueOf(productContentBean.getShowId()));
        PLVImageLoader.getInstance().loadImage(productContentBean.getCover(), commodityCoverIv);
    }

    private void bindProductName(PLVProductContentBean productContentBean) {
        final boolean showCover = !TextUtils.isEmpty(productContentBean.getCover());
        commodityNameNumberTv.setText(String.valueOf(productContentBean.getShowId()));
        commodityNameNumberTv.setVisibility(showCover ? GONE : VISIBLE);

        commodityNameTv.setText(productContentBean.getName());
    }

    private void parseProductFeatureTag(PLVProductContentBean productContentBean) {
        boolean hasFeatureTag = !TextUtils.isEmpty(productContentBean.getFeatures());
        if (!hasFeatureTag) {
            commodityFeatureTagLl.setVisibility(GONE);
            return;
        }
        final List<String> featureTags = new Gson().fromJson(productContentBean.getFeatures(), new TypeToken<List<String>>() {}.getType());
        if (featureTags == null || featureTags.size() <= 0) {
            commodityFeatureTagLl.setVisibility(GONE);
            return;
        }

        final int allowShowFeatureTagCount = 2;
        final List<String> validFeatureTags = new ArrayList<>(allowShowFeatureTagCount);
        for (String featureTag : featureTags) {
            if (TextUtils.isEmpty(featureTag)) {
                continue;
            }
            validFeatureTags.add(featureTag);
            if (validFeatureTags.size() >= allowShowFeatureTagCount) {
                break;
            }
        }

        if (validFeatureTags.isEmpty()) {
            commodityFeatureTagLl.setVisibility(GONE);
            return;
        }
        commodityFeatureTagLl.setVisibility(VISIBLE);
        commodityFeatureTagLl.removeAllViews();
        for (String validFeatureTag : validFeatureTags) {
            addFeatureTagView(commodityFeatureTagLl, validFeatureTag);
        }
    }

    private void bindPrice(PLVProductContentBean productContentBean) {
        final boolean hideSrcPrice = productContentBean.isRealPriceEqualsPrice() || productContentBean.isSrcPriceZero() || productContentBean.isFinanceProduct();
        commoditySrcPriceTv.setVisibility(hideSrcPrice ? GONE : VISIBLE);
        if (productContentBean.isNormalProduct()) {
            commoditySrcPriceTv.setText("¥" + productContentBean.getPrice());
            commodityRealPriceTv.setText(productContentBean.isFreeForPay() ? "免费" : ("¥" + productContentBean.getRealPrice()));
        } else if (productContentBean.isFinanceProduct()) {
            commodityRealPriceTv.setText(productContentBean.getYield());
        }
    }

    private void bindEnterIcon(PLVProductContentBean productContentBean) {
        commodityEnterIv.setImageResource(TextUtils.isEmpty(getProductLink(productContentBean)) ? R.drawable.plvlc_commodity_enter_disabled : R.drawable.plvlc_commodity_enter);
    }

    /**
     * @return {@code true} -> 跳转成功
     */
    private boolean enterCommodity() {
        final String link = getProductLink(productContentBean);
        if (TextUtils.isEmpty(link)) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_commodity_toast_empty_link)
                    .show();
            return false;
        }
        PLVLCCommodityDetailActivity.start(getContext(), link);
        return true;
    }

    private void addFeatureTagView(final LinearLayout viewGroup, final String tag) {
        final View featureTagView = new PLVRoundRectGradientTextView(getContext()) {{
            setText(tag);
            setTextSize(10/*sp*/);
            setTextColor(PLVFormatUtils.parseColor("#FF8F11"));
            setSingleLine(true);
            setPadding(dp2px(6), dp2px(2), dp2px(6), dp2px(2));
            updateBackgroundColor(PLVFormatUtils.parseColor("#14FF8F11"));
            updateRadius(dp2px(4), dp2px(4), dp2px(4), dp2px(4));
        }};

        viewGroup.addView(featureTagView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT) {{
                    setMargins(0, 0, dp2px(8), 0);
                }}
        );
    }

    @Nullable
    private static String getProductLink(@Nullable PLVProductContentBean productContentBean) {
        if (productContentBean == null) {
            return null;
        }
        return productContentBean.getLinkByType();
    }

    // </editor-fold>
}
