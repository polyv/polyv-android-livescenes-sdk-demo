package com.easefun.polyv.livecloudclass.modules.pagemenu.commodity

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.easefun.polyv.livecloudclass.R
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager
import com.easefun.polyv.livecommon.module.modules.commodity.PLVProductExplainActivity
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel
import com.easefun.polyv.livecommon.module.modules.log.PLVTrackLogHelper
import com.easefun.polyv.livecommon.module.utils.PLVToast
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout
import com.plv.foundationsdk.component.di.PLVDependManager
import com.plv.foundationsdk.utils.PLVGsonUtil
import com.plv.foundationsdk.utils.fromJson
import com.plv.livescenes.access.PLVChannelFeature
import com.plv.livescenes.access.PLVChannelFeatureManager
import com.plv.livescenes.socket.PLVSocketWrapper
import com.plv.socket.event.PLVEventConstant
import com.plv.socket.event.commodity.PLVProductClickBean
import com.plv.socket.event.commodity.PLVProductClickTimesEvent
import com.plv.socket.event.commodity.PLVProductContentBean
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils

/**
 * @author Hoshiiro
 */
class PLVLCProductPushCardLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var liveRoomDataManager: IPLVLiveRoomDataManager
    private var showOnPortrait: Boolean = true
    private var showOnLandscape: Boolean = true
    private var pushCardLayout: AbsCardLayout? = null

    init {
        val typedArray: TypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVLCProductPushCardLayout)
        showOnPortrait = typedArray.getBoolean(
            R.styleable.PLVLCProductPushCardLayout_plv_show_on_portrait,
            showOnPortrait
        )
        showOnLandscape = typedArray.getBoolean(
            R.styleable.PLVLCProductPushCardLayout_plv_show_on_landscape,
            showOnLandscape
        )
        typedArray.recycle()
    }

    fun init(liveRoomDataManager: IPLVLiveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager
        val viewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel::class.java)
        val isProductExplainEnabled = PLVChannelFeatureManager.onChannel(liveRoomDataManager.config.channelId).get(PLVChannelFeature.LIVE_PRODUCT_EXPLAIN_ENABLED) ?: false
        val isProductOutLinkEnabled = PLVChannelFeatureManager.onChannel(liveRoomDataManager.config.channelId).get(PLVChannelFeature.LIVE_PRODUCT_OUT_LINK_ENABLED) ?: false

        viewModel.commodityUiStateLiveData.observe(context as LifecycleOwner, Observer { uiState ->
            val pushCardProduct = uiState?.productContentBeanPushToShow?.takeIf { !it.isBigProduct }
            removeAllViews()
            pushCardLayout = null
            if (pushCardProduct != null) {
                pushCardProduct.isProductExplainEnabled = isProductExplainEnabled
                pushCardProduct.isProductOutLinkEnabled = isProductOutLinkEnabled
                pushCardLayout = AbsCardLayout.newLayout(context, pushCardProduct).apply {
                    bindProduct(pushCardProduct)
                    onViewActionListener = object : AbsCardLayout.OnViewActionListener {
                        override fun onClickClose(product: PLVProductContentBean) {
                            viewModel.onCloseProductPush()
                        }

                        override fun onClickBuyAction(product: PLVProductContentBean) {
                            if (product.isNormalProduct && !product.isOpenPrice) {
                                return
                            }
                            if (enterCommodity(product)) {
                                viewModel.onCloseProductPush()
                            }
                        }

                        override fun onShowDetail(productId: Int) {
                            viewModel.onShowProductDetail(productId)
                        }

                        override fun onClickExplain(productId: Int) {
                            PLVProductExplainActivity.start(context, productId, liveRoomDataManager.nativeAppPramsInfo)
                        }
                    }
                }
                addView(pushCardLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            updateVisibility()
        })

        viewModel.productClickTimesLiveData.observe(context as LifecycleOwner, Observer { clickTimes ->
            clickTimes ?: return@Observer
            pushCardLayout?.bindClickTimes(clickTimes)
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        updateVisibility()
    }

    private fun updateVisibility() {
        val isPortrait = ScreenUtils.isPortrait()
        val hasProductCard = childCount > 0
        if (hasProductCard) {
            val show = (isPortrait && showOnPortrait) || (!isPortrait && showOnLandscape)
            visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    /**
     * @return `true` -> 跳转成功
     */
    private fun enterCommodity(product: PLVProductContentBean): Boolean {
        val link = product.linkByType
        if (link.isNullOrEmpty()) {
            PLVToast.Builder.context(context)
                .setText(R.string.plv_commodity_toast_empty_link)
                .show()
            return false
        }

        //追踪上报商品卡片点击事件
        PLVTrackLogHelper.trackClickProductPush(liveRoomDataManager, product)

        //发送点击卡片事件
        val clickBean = PLVProductClickBean().apply {
            data = PLVProductClickBean.DataBean().apply {
                type = product.productType
                positionName = product.name
                productId = product.productId
                nickName = liveRoomDataManager.config.user.viewerName
            }
            roomId = liveRoomDataManager.config.channelId
        }
        PLVSocketWrapper.getInstance().emit(PLVEventConstant.Chatroom.EVENT_PRODUCT, PLVGsonUtil.toJson(clickBean))

        PLVLCCommodityDetailActivity.start(context, link)
        return true
    }

}

private sealed class AbsCardLayout(
    context: Context
) : FrameLayout(context) {

    companion object {
        fun newLayout(context: Context, product: PLVProductContentBean): AbsCardLayout {
            return if (product.cover.isNullOrEmpty()) {
                NoImageProductCard(context)
            } else {
                ImageProductCard(context)
            }
        }
    }

    var onViewActionListener: OnViewActionListener? = null

    abstract fun bindProduct(product: PLVProductContentBean)

    abstract fun bindClickTimes(clickTimesEvent: PLVProductClickTimesEvent)

    protected fun bindHotEffectIcon(imageView: ImageView, product: PLVProductContentBean) {
        when {
            product.isNormalProduct -> imageView.setImageResource(R.drawable.plvlc_product_push_hot_effect_icon_normal)
            product.isFinanceProduct -> imageView.setImageResource(R.drawable.plvlc_product_push_hot_effect_icon_finance)
            product.isPositionProduct -> imageView.setImageResource(R.drawable.plvlc_product_push_hot_effect_icon_job)
            else -> imageView.setImageResource(R.drawable.plvlc_product_push_hot_effect_icon_normal)
        }
    }

    protected fun bindHotEffectTypeText(textView: TextView, product: PLVProductContentBean) {
        when {
            product.isNormalProduct -> textView.setText(R.string.plvlc_product_push_hot_effect_text_normal)
            product.isFinanceProduct -> textView.setText(R.string.plvlc_product_push_hot_effect_text_finance)
            product.isPositionProduct -> textView.setText(R.string.plvlc_product_push_hot_effect_text_job)
            else -> textView.setText(R.string.plvlc_product_push_hot_effect_text_normal)
        }
    }

    protected fun bindHotEffectCount(
        multiplyText: TextView,
        countText: TextView,
        clickTimesEvent: PLVProductClickTimesEvent
    ) {
        val times = clickTimesEvent.times
        when {
            times <= 1 -> {
                multiplyText.visibility = View.GONE
                countText.visibility = View.GONE
            }

            times >= 9999 -> {
                multiplyText.visibility = View.VISIBLE
                countText.visibility = View.VISIBLE
                countText.text = "9999+"
            }

            else -> {
                multiplyText.visibility = View.VISIBLE
                countText.visibility = View.VISIBLE
                countText.text = times.toString()
            }
        }
    }

    protected fun bindIndexOrExplain(textView: TextView, product: PLVProductContentBean) {
        textView.text = if (!product.isProductExplainEnabled) product.showId.toString()
        else if (product.isProductExplained) context.getString(R.string.plv_commodity_watch_now)
        else if (product.isProductExplaining) context.getString(R.string.plv_commodity_explaining)
        else context.getString(R.string.plv_commodity_pending)
    }

    protected fun bindCover(imageView: ImageView, product: PLVProductContentBean) {
        PLVImageLoader.getInstance().loadImage(product.cover, imageView)
    }

    protected fun bindTag(textView: TextView, product: PLVProductContentBean) {
        if (product.features == null) {
            textView.visibility = View.GONE
            return
        }
        val tags = fromJson<List<String>>(product.features) ?: emptyList()
        val showTag = tags.firstOrNull()?.takeIf { it.isNotBlank() }
        if (showTag == null) {
            textView.visibility = View.GONE
            return
        }
        textView.text = showTag
        textView.visibility = View.VISIBLE
    }

    protected fun bindName(textView: TextView, product: PLVProductContentBean) {
        textView.text = product.name
    }

    protected fun bindPrice(textView: TextView, product: PLVProductContentBean) {
        val priceText = when {
            product.isNormalProduct -> when {
                !product.isOpenPrice -> "¥??"
                !product.customPrice.isNullOrBlank() -> product.customPrice
                product.isFreeForPay -> context.getString(R.string.plv_commodity_free)
                else -> "¥${product.realPrice}"
            }
            product.isFinanceProduct -> product.yield
            product.isPositionProduct -> product.treatment
            else -> ""
        }
        val priceNotOpenedText = when {
            product.isNormalProduct && !product.isOpenPrice -> context.getString(R.string.plv_commodity_price_not_opened)
            else -> null
        }

        textView.text = PLVSpannableStringBuilder(priceText).apply {
            if (priceNotOpenedText != null) {
                append(" ")
                appendExclude(priceNotOpenedText, ForegroundColorSpan(0xFF999999.toInt()))
            }
        }
    }

    protected fun bindBuyText(textView: TextView, product: PLVProductContentBean) {
        if (product.isNormalProduct && !product.isOpenPrice) {
            textView.alpha = 0.6F
        } else {
            textView.alpha = 1F
        }
        textView.text = product.btnShow
    }

    interface OnViewActionListener {
        fun onClickClose(product: PLVProductContentBean)
        fun onClickBuyAction(product: PLVProductContentBean)
        fun onShowDetail(productId: Int)
        fun onClickExplain(productId: Int)
    }

}

private class ImageProductCard(context: Context) : AbsCardLayout(context) {

    private val productPushHotEffectLayout by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plvlc_product_push_hot_effect_layout) }
    private val productPushHotEffectIv by lazy { findViewById<ImageView>(R.id.plvlc_product_push_hot_effect_iv) }
    private val productPushHotEffectTypeTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_hot_effect_type_tv) }
    private val productPushHotEffectMultiplyTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_hot_effect_multiply_tv) }
    private val productPushHotEffectCountTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_hot_effect_count_tv) }
    private val productPushCard by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plvlc_product_push_card) }
    private val productPushImageLayout by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plvlc_product_push_image_layout) }
    private val productPushImage by lazy { findViewById<ImageView>(R.id.plvlc_product_push_image) }
    private val productPushIndexTv by lazy { findViewById<PLVRoundRectGradientTextView>(R.id.plvlc_product_push_index_tv) }
    private val productPushTagTv by lazy { findViewById<PLVRoundRectGradientTextView>(R.id.plvlc_product_push_tag_tv) }
    private val productPushNameTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_name_tv) }
    private val productPushPriceTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_price_tv) }
    private val productPushBuyActionTv by lazy { findViewById<PLVRoundRectGradientTextView>(R.id.plvlc_product_push_buy_action_tv) }
    private val productPushCloseIv by lazy { findViewById<ImageView>(R.id.plvlc_product_push_close_iv) }


    init {
        LayoutInflater.from(context).inflate(R.layout.plvlc_product_push_card_layout_with_image, this)
    }

    override fun bindProduct(product: PLVProductContentBean) {
        bindHotEffectIcon(productPushHotEffectIv, product)
        bindHotEffectTypeText(productPushHotEffectTypeTv, product)
        bindIndexOrExplain(productPushIndexTv, product)
        bindCover(productPushImage, product)
        bindTag(productPushTagTv, product)
        bindName(productPushNameTv, product)
        bindPrice(productPushPriceTv, product)
        bindBuyText(productPushBuyActionTv, product)
        setOnClickListener {
            if (product.isProductOutLinkEnabled) {
                onViewActionListener?.onClickBuyAction(product)
            } else {
                onViewActionListener?.onShowDetail(product.productId)
            }
        }
        productPushBuyActionTv.setOnClickListener { onViewActionListener?.onClickBuyAction(product) }
        productPushCloseIv.setOnClickListener { onViewActionListener?.onClickClose(product) }
        if (product.isProductExplainEnabled && product.isProductExplained) {
            productPushIndexTv.setOnClickListener {
                onViewActionListener?.onClickExplain(product.productId)
            }
        } else {
            productPushIndexTv.setOnClickListener(null)
        }
    }

    override fun bindClickTimes(clickTimesEvent: PLVProductClickTimesEvent) {
        bindHotEffectCount(productPushHotEffectMultiplyTv, productPushHotEffectCountTv, clickTimesEvent)
    }
}

private class NoImageProductCard(context: Context) : AbsCardLayout(context) {

    private val productPushHotEffectLayout by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plvlc_product_push_hot_effect_layout) }
    private val productPushHotEffectIv by lazy { findViewById<ImageView>(R.id.plvlc_product_push_hot_effect_iv) }
    private val productPushHotEffectTypeTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_hot_effect_type_tv) }
    private val productPushHotEffectMultiplyTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_hot_effect_multiply_tv) }
    private val productPushHotEffectCountTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_hot_effect_count_tv) }
    private val productPushCard by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plvlc_product_push_card) }
    private val productPushIndexTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_index_tv) }
    private val productPushTagTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_tag_tv) }
    private val productPushNameTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_name_tv) }
    private val productPushPriceTv by lazy { findViewById<TextView>(R.id.plvlc_product_push_price_tv) }
    private val productPushBuyActionTv by lazy { findViewById<PLVRoundRectGradientTextView>(R.id.plvlc_product_push_buy_action_tv) }
    private val productPushCloseIv by lazy { findViewById<ImageView>(R.id.plvlc_product_push_close_iv) }

    init {
        LayoutInflater.from(context).inflate(R.layout.plvlc_product_push_card_layout_no_image, this)
    }

    override fun bindProduct(product: PLVProductContentBean) {
        bindHotEffectIcon(productPushHotEffectIv, product)
        bindHotEffectTypeText(productPushHotEffectTypeTv, product)
        bindIndexOrExplain(productPushIndexTv, product)
        bindTag(productPushTagTv, product)
        bindName(productPushNameTv, product)
        bindPrice(productPushPriceTv, product)
        bindBuyText(productPushBuyActionTv, product)
        setOnClickListener {
            if (product.isProductOutLinkEnabled) {
                onViewActionListener?.onClickBuyAction(product)
            } else {
                onViewActionListener?.onShowDetail(product.productId)
            }
        }
        productPushBuyActionTv.setOnClickListener { onViewActionListener?.onClickBuyAction(product) }
        productPushCloseIv.setOnClickListener { onViewActionListener?.onClickClose(product) }
        if (product.isProductExplainEnabled && product.isProductExplained) {
            productPushIndexTv.setOnClickListener {
                onViewActionListener?.onClickExplain(product.productId)
            }
        } else {
            productPushIndexTv.setOnClickListener(null)
        }
    }

    override fun bindClickTimes(clickTimesEvent: PLVProductClickTimesEvent) {
        bindHotEffectCount(productPushHotEffectMultiplyTv, productPushHotEffectCountTv, clickTimesEvent)
    }

}