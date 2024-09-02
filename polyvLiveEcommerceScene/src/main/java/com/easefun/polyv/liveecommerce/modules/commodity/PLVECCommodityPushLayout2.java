package com.easefun.polyv.liveecommerce.modules.commodity;

import static com.plv.thirdpart.blankj.utilcode.util.ConvertUtils.dp2px;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.easefun.polyv.livecommon.module.modules.log.PLVTrackLogHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.easefun.polyv.livecommon.ui.widget.gif.RelativeImageSpan;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.commodity.PLVProductClickBean;
import com.plv.socket.event.commodity.PLVProductClickTimesEvent;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品推送布局
 */
public class PLVECCommodityPushLayout2 extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    private final String NORMAL = "normal";
    private final String FINANCE = "finance";
    private final String POSITION = "position";

    private PLVTriangleIndicateLayout commodityPushLayoutRoot;
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
    private LinearLayout commodityPriceLl;


    private RelativeLayout productEffectImgRl;
    private TextView productEffectImgTv;
    private TextView productEffectImgNumTv;
    private TextView productEffectImgMulTv;

    private PLVRoundRectLayout productEffectTitleRl;
    private TextView productEffectTitleTv;
    private TextView productEffectTitleNumTv;
    private TextView productEffectTitleMulTv;

    private TextView commodityPositionKnowTv;
    private PLVRoundRectGradientTextView commodityPositionEnterTv;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    boolean productHotEffectEnable = false;
    private PLVLiveClassDetailVO.DataBean.ProductHotEffectBean productHotEffectBean;

    private ICommodityPushListener listener;

    private View anchorView;

    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);

    @Nullable
    private PLVProductContentBean productContentBean;

    private boolean isNeedShow = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECCommodityPushLayout2(@NonNull Context context) {
        this(context, null);
    }

    public PLVECCommodityPushLayout2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECCommodityPushLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_commodity_push_layout_2, this, true);

        findView();

        commoditySrcPriceTv.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | commoditySrcPriceTv.getPaintFlags());

        observeCommodityViewModel();
    }

    private void findView() {
        commodityPushLayoutRoot = findViewById(R.id.plvec_commodity_push_layout_root);
        commodityCoverLy = findViewById(R.id.plvec_commodity_cover_ly);
        commodityCoverIv = findViewById(R.id.plvec_commodity_cover_iv);
        commodityCoverNumberTv = findViewById(R.id.plvec_commodity_cover_number_tv);
        commodityNameLl = findViewById(R.id.plvec_commodity_name_ll);
        commodityNameNumberTv = findViewById(R.id.plvec_commodity_name_number_tv);
        commodityNameTv = findViewById(R.id.plvec_commodity_name_tv);
        commodityFeatureTagLl = findViewById(R.id.plvec_commodity_feature_tag_ll);
        commodityProductDescTv = findViewById(R.id.plvec_commodity_product_desc_tv);
        commodityRealPriceTv = findViewById(R.id.plvec_commodity_real_price_tv);
        commoditySrcPriceTv = findViewById(R.id.plvec_commodity_src_price_tv);
        commodityDialogCloseIv = findViewById(R.id.plvec_commodity_dialog_close_iv);
        commodityEnterIv = findViewById(R.id.plvec_commodity_enter_iv);
        commodityPriceLl = findViewById(R.id.plvec_commodity_price_ll);


        productEffectImgRl = findViewById(R.id.plvec_product_image_effect_rl);
        productEffectImgTv = findViewById(R.id.plvec_product_image_effect_tv);
        productEffectImgNumTv = findViewById(R.id.plvec_product_image_effect_num);
        productEffectImgMulTv = findViewById(R.id.plvec_product_image_effect_mul);

        productEffectTitleRl = findViewById(R.id.plvec_product_title_effect_rl);
        productEffectTitleTv = findViewById(R.id.plvec_product_title_effect_tv);
        productEffectTitleNumTv = findViewById(R.id.plvec_product_title_effect_num);
        productEffectTitleMulTv = findViewById(R.id.plvec_product_title_effect_mul);

        commodityPositionEnterTv = findViewById(R.id.plvec_commodity_position_entry_tv);
        commodityPositionKnowTv = findViewById(R.id.plvec_commodity_position_know_tv);

        commodityDialogCloseIv.setOnClickListener(this);
        commodityEnterIv.setOnClickListener(this);
        this.setOnClickListener(this);
        commodityPositionEnterTv.setOnClickListener(this);
        commodityPositionKnowTv.setOnClickListener(this);
    }

    public void setCommodityPushListener(ICommodityPushListener listener) {
        this.listener = listener;
    }

    private void observeCommodityViewModel() {
        commodityViewModel.getCommodityUiStateLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<PLVCommodityUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVCommodityUiState uiState) {
                        if (uiState == null) {
                            return;
                        }
                        if (anchorView != null) {
                            anchorView.setVisibility(uiState.hasProductView ? View.VISIBLE : View.GONE);
                        }
                        if (uiState.productContentBeanPushToShow != null) {
                            updateProduct(uiState.productContentBeanPushToShow);
                        }
                        isNeedShow = uiState.productContentBeanPushToShow != null;
                        if (uiState.productContentBeanPushToShow != null) {
                            // 推送大卡片时不要显示
                            isNeedShow &= !uiState.productContentBeanPushToShow.isBigProduct();
                        }
                        checkUpdateVisibility();
                    }
                });


        commodityViewModel.getProductClickTimesLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<PLVProductClickTimesEvent>() {
                    @Override
                    public void onChanged(@Nullable PLVProductClickTimesEvent plvProductClickTimesEvent) {
                        updateProductClickTimes(plvProductClickTimesEvent);
                    }
                });
    }

    private void observeOnProductHotEffect() {
        if (liveRoomDataManager != null) {
            liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
                @Override
                public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                    liveRoomDataManager.getClassDetailVO().removeObserver(this);
                    if (polyvLiveClassDetailVOPLVStatefulData == null || !polyvLiveClassDetailVOPLVStatefulData.isSuccess()) {
                        return;
                    }
                    PLVLiveClassDetailVO liveClassDetail = polyvLiveClassDetailVOPLVStatefulData.getData();
                    if (liveClassDetail == null || liveClassDetail.getData() == null) {
                        return;
                    }

                    productHotEffectEnable = liveClassDetail.getData().isProductHotEffectEnabled();
                    productHotEffectBean = liveClassDetail.getData().getProductHotEffectTips();
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setAnchor(View view) {
        this.anchorView = view;
        commodityPushLayoutRoot.setMarginAnchor(view);
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        observeOnProductHotEffect();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI更新">

    @MainThread
    private void updateProduct(PLVProductContentBean productContentBean) {
        this.productContentBean = productContentBean;

        bindCover(productContentBean);
        bindProductName(productContentBean);

        final boolean hasProductDesc = !TextUtils.isEmpty(productContentBean.getProductDesc()) && !productContentBean.isPositionProduct();
        commodityProductDescTv.setVisibility(hasProductDesc ? View.VISIBLE : View.GONE);
        commodityProductDescTv.setText(productContentBean.getProductDesc());

        parseProductFeatureTag(productContentBean);
        bindPrice(productContentBean);
        bindEnterIcon(productContentBean);

        bindProductEffect(productContentBean);
        processCommodityPosition(productContentBean);
    }

    @MainThread
    private void checkUpdateVisibility() {
        setVisibility(isNeedShow ? View.VISIBLE : View.GONE);
    }

    private void updateProductClickTimes(PLVProductClickTimesEvent productClickTimesEvent) {
        if (productContentBean != null) {
            long times = productClickTimesEvent.getTimes();
            if (productContentBean.getProductId() == productClickTimesEvent.getProductId()) {
                times = Math.min(9999, times);
                String timesStr = times >= 9999 ? "9999+" : times + "";
                productEffectTitleNumTv.setVisibility(VISIBLE);
                productEffectImgNumTv.setVisibility(VISIBLE);
                productEffectImgMulTv.setVisibility(VISIBLE);
                productEffectTitleMulTv.setVisibility(VISIBLE);
                productEffectTitleNumTv.setText(timesStr);
                productEffectImgNumTv.setText(timesStr);
            }
        }
    }


    private Spannable generateRewardSpan(String text) {
        SpannableStringBuilder span = new SpannableStringBuilder(" " + text);

        Drawable drawable;
        ImageSpan imageSpan;

        drawable = this.getResources().getDrawable(R.drawable.plvec_product_hot_effect_icon);
        imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);

        drawable.setBounds(0, 0, ConvertUtils.dp2px(10), ConvertUtils.dp2px(12));
        span.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
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
        } else if (id == commodityPositionEnterTv.getId()) {
            if (enterCommodity()) {
                isNeedShow = false;
                commodityViewModel.onCloseProductPush();
                checkUpdateVisibility();
            }
        } else if (id == commodityPositionKnowTv.getId()) {
            if (listener != null) {
                listener.showJobDetail(productContentBean);
            }
        } else if (id == commodityEnterIv.getId() || v == this) {
            if (enterCommodity()) {
                isNeedShow = false;
                commodityViewModel.onCloseProductPush();
                checkUpdateVisibility();
            }
        }
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
        commodityPositionKnowTv.setText(getResources().getString(R.string.plv_commodity_position_details));
        commodityPositionEnterTv.setText(productContentBean.getBtnShow());
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

        int allowShowFeatureTagCount = 2;
        if (productContentBean.isPositionProduct()) {
            allowShowFeatureTagCount = 3;
        }
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
            commodityRealPriceTv.setText(productContentBean.isFreeForPay() ? PLVAppUtils.getString(R.string.plv_commodity_free) : ("¥" + productContentBean.getRealPrice()));
        } else if (productContentBean.isFinanceProduct()) {
            commodityRealPriceTv.setText(productContentBean.getYield());
        }
    }

    private void bindEnterIcon(PLVProductContentBean productContentBean) {
        commodityEnterIv.setImageResource(TextUtils.isEmpty(getProductLink(productContentBean)) ? R.drawable.plvec_commodity_enter_disabled : R.drawable.plvec_commodity_enter);
    }

    private void bindProductEffect(PLVProductContentBean productContentBean) {
        if (productHotEffectEnable && productHotEffectBean != null) {
            String productEffectTitle = "";
            productEffectImgNumTv.setVisibility(GONE);
            productEffectTitleNumTv.setVisibility(GONE);
            productEffectImgMulTv.setVisibility(GONE);
            productEffectTitleMulTv.setVisibility(GONE);
            String type = productContentBean.getProductType();
            if (type.equals(NORMAL)) {
                productEffectTitle = productHotEffectBean.getNormalProductTips();
            } else if (type.equals(FINANCE)) {
                productEffectTitle = productHotEffectBean.getFinanceProductTips();
            } else if (type.equals(POSITION)) {
                productEffectTitle = productHotEffectBean.getJobProductTips();
            }
            if (TextUtils.isEmpty(productEffectTitle)) {
                return;
            }

            // 处理带有图片的情况
            if (!TextUtils.isEmpty(productContentBean.getCover())) {
                productEffectImgRl.setVisibility(VISIBLE);
                productEffectTitleRl.setVisibility(GONE);
                commodityCoverNumberTv.setVisibility(GONE);
                productEffectImgTv.setText(generateRewardSpan(productEffectTitle));
            } else {
                //没有带有图片的情况
                productEffectImgRl.setVisibility(GONE);
                productEffectTitleRl.setVisibility(VISIBLE);
                productEffectTitleTv.setText(generateRewardSpan(productEffectTitle));
            }
        }
    }

    private void processCommodityPosition(PLVProductContentBean productContentBean) {
        boolean isPosition = productContentBean.isPositionProduct();
        int idCommodityPriceLl = commodityPriceLl.getId();
        int idCommodityCoverLy = commodityCoverLy.getId();
        ConstraintLayout.LayoutParams commodityPositionEnterLayoutParam = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        ConstraintLayout.LayoutParams commodityPriceLlParam = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        // 处理带有图片的情况
        if (!TextUtils.isEmpty(productContentBean.getCover())) {
            commodityPositionEnterLayoutParam.topToBottom = idCommodityPriceLl;
            commodityPositionEnterLayoutParam.topMargin = ConvertUtils.dp2px(4);
            commodityPositionEnterLayoutParam.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            commodityPositionEnterLayoutParam.rightMargin = ConvertUtils.dp2px(22);

        } else {
            // 处理没有图片的情况
            commodityPositionEnterLayoutParam.topToTop = idCommodityPriceLl;
            commodityPositionEnterLayoutParam.topMargin = 0;
            commodityPositionEnterLayoutParam.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            commodityPositionEnterLayoutParam.rightMargin = ConvertUtils.dp2px(22);
        }

        commodityPriceLlParam.startToEnd = idCommodityCoverLy;
        commodityPriceLlParam.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        commodityPriceLlParam.leftMargin = ConvertUtils.dp2px(8);
        commodityPriceLlParam.topMargin = ConvertUtils.dp2px(8);

        if (isPosition && !TextUtils.isEmpty(productContentBean.getCover())) {
            commodityPriceLlParam.bottomMargin = ConvertUtils.dp2px(40);
        } else {
            commodityPriceLlParam.bottomMargin = ConvertUtils.dp2px(12);
        }

        commodityPositionEnterTv.setLayoutParams(commodityPositionEnterLayoutParam);
        commodityPriceLl.setLayoutParams(commodityPriceLlParam);

        commodityPositionKnowTv.setVisibility(isPosition ? VISIBLE : GONE);
        commodityPositionEnterTv.setVisibility(isPosition ? VISIBLE : GONE);
        commodityEnterIv.setVisibility(isPosition ? GONE : VISIBLE);
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

        //追踪上报商品卡片点击事件
        PLVTrackLogHelper.trackClickProductPush(liveRoomDataManager, productContentBean);

        //发送点击卡片事件
        PLVProductClickBean clickBean = new PLVProductClickBean();
        PLVProductClickBean.DataBean dataBean = new PLVProductClickBean.DataBean();
        dataBean.setType(productContentBean.getProductType());
        dataBean.setPositionName(productContentBean.getName());
        dataBean.setProductId(productContentBean.getProductId());
        dataBean.setNickName(liveRoomDataManager.getConfig().getUser().getViewerName());
        clickBean.setData(dataBean);
        clickBean.setRoomId(liveRoomDataManager.getConfig().getChannelId());
        PLVSocketWrapper.getInstance().emit(PLVEventConstant.Chatroom.EVENT_PRODUCT, new Gson().toJson(clickBean));

        PLVECCommodityDetailActivity.start(getContext(), link);
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

    // <editor-fold defaultstate="collapsed" desc="接口">
    public interface ICommodityPushListener {
        void showJobDetail(PLVProductContentBean bean);
    }
    // </editor-fold>
}
