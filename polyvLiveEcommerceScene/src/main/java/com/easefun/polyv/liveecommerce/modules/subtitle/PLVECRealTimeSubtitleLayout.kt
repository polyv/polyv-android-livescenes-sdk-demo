package com.easefun.polyv.liveecommerce.modules.subtitle

import android.content.Context
import android.content.res.Configuration
import android.support.constraint.ConstraintLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager
import com.easefun.polyv.livecommon.ui.widget.PLVDragScaleLayout
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.findViewById
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position
import com.easefun.polyv.livecommon.ui.widget.menudrawer.hidePopupMenu
import com.easefun.polyv.livecommon.ui.widget.menudrawer.showPopupMenu
import com.easefun.polyv.liveecommerce.R
import com.plv.foundationsdk.utils.PLVAppUtils
import com.plv.livescenes.access.PLVChannelFeature
import com.plv.livescenes.access.PLVChannelFeatureManager
import com.plv.livescenes.video.subtitle.vo.PLVLiveSubtitleSettingVO
import com.plv.livescenes.video.subtitle.vo.PLVLiveSubtitleTranslation
import com.plv.thirdpart.blankj.utilcode.util.BarUtils
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils
import net.polyv.android.common.libs.lang.getString
import net.polyv.android.common.libs.lang.graphic.dp
import net.polyv.android.common.libs.lang.state.MutableObserver
import net.polyv.android.common.libs.lang.state.MutableObserver.Companion.disposeAll
import net.polyv.android.common.libs.lang.state.MutableState
import net.polyv.android.common.libs.lang.state.State
import net.polyv.android.common.libs.lang.state.mutableStateOf
import net.polyv.android.common.libs.lang.state.watchStates
import net.polyv.android.common.libs.lang.ui.updateLayoutParams
import kotlin.math.min

/**
 * @author Hoshiiro
 */
class PLVECRealTimeSubtitleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PLVDragScaleLayout(context, attrs, defStyleAttr) {

    private val liveRealTimeSubtitleRv by lazy { findViewById<RecyclerView>(R.id.plvec_live_real_time_subtitle_rv) }
    private val liveRealTimeSubtitleCloseIv by lazy { findViewById<ImageView>(R.id.plvec_live_real_time_subtitle_close_iv) }
    private val liveRealTimeSubtitleExpandIv by lazy { findViewById<ImageView>(R.id.plvec_live_real_time_subtitle_expand_iv) }

    private val languageSetting = mutableStateOf<PLVECRealTimeSubtitleLanguageSetting>()
    private val settingPopupMenu by lazy { PLVECLiveSubtitleSettingPopupMenu(context, languageSetting) }

    private val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    private val adapter = PLVECRealTimeSubtitleAdapter(languageSetting)

    var onViewActionListener: OnViewActionListener? = null
    private val observers = mutableListOf<MutableObserver<*>>()

    init {
        LayoutInflater.from(context).inflate(R.layout.plvec_live_real_time_subtitle_layout, this)
        liveRealTimeSubtitleRv.layoutManager = layoutManager
        liveRealTimeSubtitleRv.adapter = adapter

        watchState()
        setOnClickListeners()
        post {
            initLayoutParam()
            initDrag()
        }
    }

    private fun initDrag() {
        setDragScaleMode(FLAG_EDGE_RIGHT, 0)
        setEdgeResponseSize(28.dp().px())
        updateLayoutSizePosition()
    }

    private fun watchState() {
        watchStates {
            val setting = languageSetting.value ?: return@watchStates
            val translateLanguage = setting.translateLanguage ?: return@watchStates
            onViewActionListener?.onSetSubtitleTranslateLanguage(translateLanguage)
        }.addTo(this.observers)
        watchStates {
            val setting = languageSetting.value ?: return@watchStates
            val visible = setting.showOriginSubtitle || setting.showTranslateSubtitle
            visibility = if (visible) View.VISIBLE else View.GONE
        }.addTo(this.observers)
    }

    private fun initLayoutParam() {
        val margin = 12.dp().px()
        val width = ScreenUtils.getScreenOrientatedWidth() - 2 * margin
        val height = 106.dp().px()
        val top = ScreenUtils.getScreenOrientatedHeight() - 400.dp().px()
        updateLayoutParams<MarginLayoutParams> {
            leftMargin = margin
            topMargin = top
            this@updateLayoutParams.width = width
            this@updateLayoutParams.height = height
        }
    }

    private fun setOnClickListeners() {
        liveRealTimeSubtitleCloseIv.setOnClickListener {
            val setting = languageSetting.value ?: return@setOnClickListener
            languageSetting.setValue(
                setting.copy(
                    showOriginSubtitle = false,
                    showTranslateSubtitle = false
                )
            )
        }
        liveRealTimeSubtitleExpandIv.setOnClickListener {
            val isExpanded = liveRealTimeSubtitleExpandIv.isSelected
            if (isExpanded) {
                collapse()
            } else {
                expand()
            }
        }
    }

    fun initData(liveRoomDataManager: IPLVLiveRoomDataManager) {
        val subtitleSetting = PLVChannelFeatureManager.onChannel(liveRoomDataManager.config.channelId)
            .get(PLVChannelFeature.LIVE_SUBTITLE_SETTING)
        val showOriginSubtitle = subtitleSetting?.enable == true && subtitleSetting.originLanguage != null
        val showTranslateSubtitle = subtitleSetting?.enable == true && subtitleSetting.translationEnable && subtitleSetting.defaultTranslationLanguage != null
        val originLanguage = subtitleSetting?.originLanguage
        val translateLanguage = subtitleSetting?.defaultTranslationLanguage
        languageSetting.setValue(
            PLVECRealTimeSubtitleLanguageSetting(
                subtitleSetting,
                showOriginSubtitle,
                showTranslateSubtitle,
                originLanguage,
                translateLanguage
            )
        )
    }

    fun updateSubtitles(subtitles: List<PLVLiveSubtitleTranslation>?) {
        val isScrollerAtBottom = layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1
        adapter.updateItems(subtitles ?: emptyList())
        if (isScrollerAtBottom) {
            liveRealTimeSubtitleRv.scrollToPosition(adapter.itemCount - 1)
        }
    }

    fun expand() {
        updateLayoutSizePosition(260.dp().px())
        liveRealTimeSubtitleExpandIv.isSelected = true
    }

    fun collapse() {
        val isScrollerAtBottom = layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1
        updateLayoutSizePosition(106.dp().px())
        if (isScrollerAtBottom) {
            liveRealTimeSubtitleRv.scrollToPosition(adapter.itemCount - 1)
        }
        liveRealTimeSubtitleExpandIv.isSelected = false
    }

    fun showSettingPopupMenu() {
        settingPopupMenu.initData()
        settingPopupMenu.show()
    }

    fun destroy() {
        this.observers.disposeAll()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        updateLayoutSizePosition()
    }

    private fun updateLayoutSizePosition(
        height: Int = layoutParams.height
    ) {
        val isPortrait = ScreenUtils.isPortrait()
        val margin = 12.dp().px().toFloat()
        val marginRightExtra = if (!isPortrait) BarUtils.getStatusBarHeight() else 0
        val layoutParams = layoutParams as MarginLayoutParams
        val left = layoutParams.leftMargin
        val top = layoutParams.topMargin
        val width = min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()) - 2 * margin
        val height = height.coerceIn(106.dp().px(), 260.dp().px())
        val right = left + width
        val bottom = top + height
        setDragRange(
            margin,
            ScreenUtils.getScreenOrientatedWidth() - margin - marginRightExtra,
            margin,
            ScreenUtils.getScreenOrientatedHeight() - margin
        )

        val dx = if (left < minX) minX - left else if (right > maxX) maxX - right else 0F
        val dy = if (top < minY) minY - top else if (bottom > maxY) maxY - bottom else 0F
        updateLayoutParams<MarginLayoutParams> {
            leftMargin = left + dx.toInt()
            topMargin = top + dy.toInt()
            this@updateLayoutParams.width = width.toInt()
            this@updateLayoutParams.height = height
        }
    }

    private class PLVECRealTimeSubtitleAdapter(
        private val languageSetting: State<PLVECRealTimeSubtitleLanguageSetting>
    ) : RecyclerView.Adapter<PLVECRealTimeSubtitleAdapter.ViewHolder>() {

        private var items = listOf<PLVLiveSubtitleTranslation>()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            return ViewHolder(parent, languageSetting)
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        fun updateItems(newItems: List<PLVLiveSubtitleTranslation>) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = items.size

                override fun getNewListSize(): Int = newItems.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return items[oldItemPosition].index == newItems[newItemPosition].index
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return items[oldItemPosition] == newItems[newItemPosition]
                }
            })
            items = newItems
            diff.dispatchUpdatesTo(this)
        }

        class ViewHolder(
            val parent: ViewGroup,
            private val languageSetting: State<PLVECRealTimeSubtitleLanguageSetting>
        ) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.plvec_live_real_time_subtitle_item, parent, false)
        ) {
            private val liveRealTimeSubtitleOriginTv by lazy { findViewById<TextView>(R.id.plvec_live_real_time_subtitle_origin_tv) }
            private val liveRealTimeSubtitleTranslateDivider by lazy { findViewById<View>(R.id.plvec_live_real_time_subtitle_translate_divider) }
            private val liveRealTimeSubtitleTranslateTv by lazy { findViewById<TextView>(R.id.plvec_live_real_time_subtitle_translate_tv) }
            private val liveRealTimeSubtitleTranslateDividerNextSubtitle by lazy { findViewById<View>(R.id.plvec_live_real_time_subtitle_translate_divider_next_subtitle) }

            private val item = mutableStateOf<PLVLiveSubtitleTranslation>()

            init {
                itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        watchStateChange()
                    }

                    override fun onViewDetachedFromWindow(v: View) {

                    }
                })
            }

            fun bind(item: PLVLiveSubtitleTranslation) {
                this.item.setValue(item)
            }

            private fun watchStateChange() {
                watchStates {
                    val setting = languageSetting.value ?: return@watchStates
                    val item = item.value ?: return@watchStates
                    val showTranslation = setting.showTranslateSubtitle && item.translation != null && item.translation!!.text.isNotBlank()
                    val showDivider = setting.showOriginSubtitle && showTranslation
                    itemView.visibility = if (setting.showOriginSubtitle || showTranslation) View.VISIBLE else View.GONE
                    if (setting.showOriginSubtitle) {
                        liveRealTimeSubtitleOriginTv.visibility = View.VISIBLE
                        liveRealTimeSubtitleOriginTv.text = item.origin.text
                    } else {
                        liveRealTimeSubtitleOriginTv.visibility = View.GONE
                    }
                    liveRealTimeSubtitleTranslateDivider.visibility = if (showDivider) View.VISIBLE else View.GONE
                    if (showTranslation) {
                        liveRealTimeSubtitleTranslateTv.visibility = View.VISIBLE
                        liveRealTimeSubtitleTranslateTv.text = item.translation?.text
                        liveRealTimeSubtitleTranslateTv.alpha = if (setting.showOriginSubtitle) 0.6F else 1F
                    } else {
                        liveRealTimeSubtitleTranslateTv.visibility = View.GONE
                    }
                    liveRealTimeSubtitleTranslateDividerNextSubtitle.visibility = if (item.origin.stable) View.VISIBLE else View.GONE
                }.disposeOnDetached(itemView)
            }
        }
    }

    interface OnViewActionListener {
        fun onSetSubtitleTranslateLanguage(language: String)
    }

}

private class PLVECLiveSubtitleSettingPopupMenu(
    context: Context,
    private val stateLanguageSetting: MutableState<PLVECRealTimeSubtitleLanguageSetting>
) : FrameLayout(context) {
    private val liveSubtitleSettingTitle by lazy { findViewById<TextView>(R.id.plvec_live_subtitle_setting_title) }
    private val liveSubtitleEnableLayout by lazy { findViewById<ConstraintLayout>(R.id.plvec_live_subtitle_enable_layout) }
    private val liveSubtitleEnableSwitch by lazy { findViewById<Switch>(R.id.plvec_live_subtitle_enable_switch) }
    private val liveSubtitleLanguageLayout by lazy { findViewById<ConstraintLayout>(R.id.plvec_live_subtitle_language_layout) }
    private val liveSubtitleLanguageTv by lazy { findViewById<TextView>(R.id.plvec_live_subtitle_language_tv) }
    private val liveSubtitleLanguageIv by lazy { findViewById<ImageView>(R.id.plvec_live_subtitle_language_iv) }
    private val liveSubtitleDoubleLanguageLayout by lazy { findViewById<ConstraintLayout>(R.id.plvec_live_subtitle_double_language_layout) }
    private val liveSubtitleDoubleLanguageEnableSwitch by lazy { findViewById<Switch>(R.id.plvec_live_subtitle_double_language_enable_switch) }

    private val stateMenuSetting = mutableStateOf<PopupMenuLanguageSetting>()
    private val selectLanguagePopupMenu = PLVECLiveSubtitleSettingLanguagePopupMenu(
        context,
        stateLanguageSetting,
        stateMenuSetting,
        onClose = { this.show() }
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.plvec_live_subtitle_setting_popup_menu, this)
        liveSubtitleEnableSwitch.setOnCheckedChangeListener { _, isChecked ->
            val menuSetting = stateMenuSetting.value ?: return@setOnCheckedChangeListener
            stateMenuSetting.setValue(menuSetting.copy(showSubtitle = isChecked))
        }
        liveSubtitleDoubleLanguageEnableSwitch.setOnCheckedChangeListener { _, isChecked ->
            val menuSetting = stateMenuSetting.value ?: return@setOnCheckedChangeListener
            stateMenuSetting.setValue(menuSetting.copy(showDoubleLanguage = isChecked))
        }
        liveSubtitleLanguageLayout.setOnClickListener {
            close()
            selectLanguagePopupMenu.show()
        }
    }

    fun initData() {
        val languageSetting = stateLanguageSetting.value
        if (languageSetting == null) {
            stateMenuSetting.setValue(PopupMenuLanguageSetting(true, "", true))
        } else {
            val showSubtitle = languageSetting.showOriginSubtitle || languageSetting.showTranslateSubtitle
            val showDoubleLanguage = languageSetting.showOriginSubtitle && languageSetting.showTranslateSubtitle
            val language = when {
                showDoubleLanguage -> languageSetting.translateLanguage
                languageSetting.showOriginSubtitle -> languageSetting.originLanguage
                languageSetting.showTranslateSubtitle -> languageSetting.translateLanguage
                else -> stateMenuSetting.value?.subtitleLanguage ?: ""
            }
            stateMenuSetting.setValue(PopupMenuLanguageSetting(showSubtitle, language ?: "", showDoubleLanguage))
        }
        selectLanguagePopupMenu.initData()
    }

    fun show() {
        context.showPopupMenu(this, Position.BOTTOM, 250.dp().px())
    }

    fun close() {
        context.hidePopupMenu(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // menuSetting -> languageSetting
        stateMenuSetting.observe { menuSetting ->
            val setting = stateLanguageSetting.value ?: return@observe
            val originLanguage = setting.originLanguage
            val showOriginSubtitle = menuSetting.showSubtitle && (menuSetting.showDoubleLanguage || menuSetting.subtitleLanguage == originLanguage)
            val showTranslateSubtitle = menuSetting.showSubtitle && (menuSetting.showDoubleLanguage || menuSetting.subtitleLanguage != originLanguage)
            val translateLanguage = if (menuSetting.subtitleLanguage != originLanguage) menuSetting.subtitleLanguage else setting.translateLanguage
            stateLanguageSetting.setValue(
                setting.copy(
                    showOriginSubtitle = showOriginSubtitle,
                    showTranslateSubtitle = showTranslateSubtitle,
                    translateLanguage = translateLanguage
                )
            )
        }.disposeOnDetached(this)
        // menuSetting -> ui
        stateMenuSetting.observe { menuSetting ->
            val setting = stateLanguageSetting.value ?: return@observe
            val originLanguage = setting.originLanguage
            liveSubtitleEnableSwitch.isChecked = menuSetting.showSubtitle
            liveSubtitleLanguageLayout.visibility = if (menuSetting.showSubtitle) View.VISIBLE else View.GONE
            liveSubtitleDoubleLanguageLayout.visibility = if (menuSetting.showSubtitle) View.VISIBLE else View.GONE
            liveSubtitleLanguageTv.text = if (menuSetting.subtitleLanguage == originLanguage) {
                getString(R.string.plv_live_subtitle_language_origin_not_translated)
            } else getLanguageText(menuSetting.subtitleLanguage)
            liveSubtitleDoubleLanguageEnableSwitch.isChecked = menuSetting.showDoubleLanguage && menuSetting.subtitleLanguage != originLanguage
            liveSubtitleDoubleLanguageEnableSwitch.isEnabled = menuSetting.subtitleLanguage != setting.originLanguage
        }.disposeOnDetached(this)
    }

}

private class PLVECLiveSubtitleSettingLanguagePopupMenu(
    context: Context,
    private val stateLanguageSetting: MutableState<PLVECRealTimeSubtitleLanguageSetting>,
    private val stateMenuSetting: MutableState<PopupMenuLanguageSetting>,
    private val onClose: () -> Unit
) : FrameLayout(context) {
    private val liveSubtitleSettingLanguageTitle by lazy { findViewById<TextView>(R.id.plvec_live_subtitle_setting_language_title) }
    private val liveSubtitleSettingLanguageRv by lazy { findViewById<RecyclerView>(R.id.plvec_live_subtitle_setting_language_rv) }
    private val adapter = Adapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.plvec_live_subtitle_setting_language_popup_menu, this)
        liveSubtitleSettingLanguageRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        liveSubtitleSettingLanguageRv.adapter = adapter
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun initData() {
        val languages = buildList {
            add(stateLanguageSetting.value?.originLanguage)
            addAll(stateLanguageSetting.value?.subtitleSetting?.availableTranslationLanguages.orEmpty())
        }.filterNotNull().distinct()
        adapter.setLanguages(languages)
    }

    fun show() {
        context.showPopupMenu(this, Position.BOTTOM, 400.dp().px(), null, onClose)
    }

    fun close() {
        context.hidePopupMenu(this)
    }

    private inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private val languages = mutableListOf<String>()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) = ViewHolder(parent)

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            holder.bind(languages[position])
        }

        override fun getItemCount(): Int = languages.size

        fun setLanguages(languages: List<String>) {
            this.languages.clear()
            this.languages.addAll(languages)
        }

        inner class ViewHolder(
            parent: ViewGroup,
        ) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.plvec_live_subtitle_setting_language_item, parent, false)
        ) {
            private val liveSubtitleSettingLanguageTv by lazy { findViewById<TextView>(R.id.plvec_live_subtitle_setting_language_tv) }
            private val liveSubtitleSettingLanguageSelectedIv by lazy { findViewById<ImageView>(R.id.plvec_live_subtitle_setting_language_selected_iv) }

            private val itemLanguage = mutableStateOf<String>()

            init {
                itemView.setOnClickListener {
                    val language = itemLanguage.value ?: return@setOnClickListener
                    val menuSetting = stateMenuSetting.value ?: return@setOnClickListener
                    val originLanguage = stateLanguageSetting.value?.originLanguage
                    if (language == menuSetting.subtitleLanguage) return@setOnClickListener
                    var showDoubleLanguage = menuSetting.showDoubleLanguage
                    if (!showDoubleLanguage && menuSetting.subtitleLanguage == originLanguage) {
                        showDoubleLanguage = true
                    }
                    stateMenuSetting.setValue(
                        menuSetting.copy(
                            subtitleLanguage = language,
                            showDoubleLanguage = showDoubleLanguage
                        )
                    )
                    close()
                }
                itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        watchStateChange()
                    }

                    override fun onViewDetachedFromWindow(v: View) {

                    }
                })
            }

            fun bind(language: String) {
                itemLanguage.setValue(language)
            }

            private fun watchStateChange() {
                watchStates {
                    val language = itemLanguage.value ?: return@watchStates
                    val originLanguage = stateLanguageSetting.value?.originLanguage
                    val selectedLanguage = stateMenuSetting.value?.subtitleLanguage
                    liveSubtitleSettingLanguageTv.text = if (language == originLanguage) {
                        getString(R.string.plv_live_subtitle_language_origin_not_translated)
                    } else getLanguageText(language)
                    liveSubtitleSettingLanguageSelectedIv.visibility = if (language == selectedLanguage) View.VISIBLE else View.GONE

                }.disposeOnDetached(itemView)
            }
        }
    }

}

private data class PLVECRealTimeSubtitleLanguageSetting(
    val subtitleSetting: PLVLiveSubtitleSettingVO?,
    val showOriginSubtitle: Boolean,
    val showTranslateSubtitle: Boolean,
    val originLanguage: String?,
    val translateLanguage: String?,
)

private data class PopupMenuLanguageSetting(
    val showSubtitle: Boolean,
    val subtitleLanguage: String,
    val showDoubleLanguage: Boolean
)

private fun getLanguageText(language: String?): String {
    if (language == null) return ""
    val context = PLVAppUtils.getApp() ?: return ""
    val id = context.resources.getIdentifier(
        "plv_subtitle_language_${language.lowercase()}",
        "string",
        context.packageName
    )
    return if (id != 0) context.resources.getString(id) else ""
}