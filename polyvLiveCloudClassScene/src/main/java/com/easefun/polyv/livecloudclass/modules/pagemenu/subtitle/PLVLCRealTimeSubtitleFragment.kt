package com.easefun.polyv.livecloudclass.modules.pagemenu.subtitle

import android.content.Context
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.easefun.polyv.livecloudclass.R
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.findViewById
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position
import com.easefun.polyv.livecommon.ui.widget.menudrawer.hidePopupMenu
import com.easefun.polyv.livecommon.ui.widget.menudrawer.showPopupMenu
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment
import com.plv.foundationsdk.utils.PLVAppUtils
import com.plv.livescenes.access.PLVChannelFeature
import com.plv.livescenes.access.PLVChannelFeatureManager
import com.plv.livescenes.video.subtitle.vo.PLVLiveSubtitleSettingVO
import com.plv.livescenes.video.subtitle.vo.PLVLiveSubtitleTranslation
import net.polyv.android.common.libs.lang.getString
import net.polyv.android.common.libs.lang.graphic.dp
import net.polyv.android.common.libs.lang.state.MutableObserver
import net.polyv.android.common.libs.lang.state.MutableObserver.Companion.disposeAll
import net.polyv.android.common.libs.lang.state.MutableState
import net.polyv.android.common.libs.lang.state.State
import net.polyv.android.common.libs.lang.state.mutableStateOf
import net.polyv.android.common.libs.lang.state.watchStates

/**
 * @author Hoshiiro
 */
class PLVLCRealTimeSubtitleFragment : PLVBaseFragment() {

    private val liveRealTimeSubtitleLanguageLayout by lazy { findViewById<LinearLayout>(R.id.plvlc_live_real_time_subtitle_language_layout) }
    private val liveRealTimeSubtitleLanguageTv by lazy { findViewById<TextView>(R.id.plvlc_live_real_time_subtitle_language_tv) }
    private val liveRealTimeSubtitleRv by lazy { findViewById<RecyclerView>(R.id.plvlc_live_real_time_subtitle_rv) }

    private val languageSetting = mutableStateOf<PLVLCRealTimeSubtitleLanguageSetting>()
    private val settingPopupMenu by lazy { PLVLCLiveSubtitleSettingPopupMenu(context!!, languageSetting) }

    private val layoutManager = LinearLayoutManager(
        context,
        LinearLayoutManager.VERTICAL,
        false
    )
    private val adapter = PLVLCRealTimeSubtitleAdapter(languageSetting)

    var onViewActionListener: OnViewActionListener? = null
    private val observers = mutableListOf<MutableObserver<*>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.plvlc_live_real_time_subtitle_layout, container, false)
        initView()
        return view
    }

    private fun initView() {
        liveRealTimeSubtitleRv.layoutManager = layoutManager
        liveRealTimeSubtitleRv.adapter = adapter

        liveRealTimeSubtitleLanguageLayout.setOnClickListener { settingPopupMenu.show() }

        watchStates {
            val setting = languageSetting.value ?: return@watchStates
            val subtitleLanguageText = when {
                setting.showOriginSubtitle && setting.showTranslateSubtitle -> {
                    val type = getString(R.string.plv_live_subtitle_double_language)
                    val language = getLanguageText(setting.translateLanguage)
                    "$type/$language"
                }

                setting.showOriginSubtitle -> {
                    val type = getString(R.string.plvlc_playback_subtitle_original)
                    val language = getLanguageText(setting.originLanguage)
                    "$type/$language"
                }

                setting.showTranslateSubtitle -> {
                    val type = getString(R.string.plvlc_playback_subtitle_translate)
                    val language = getLanguageText(setting.translateLanguage)
                    "$type/$language"
                }

                else -> getString(R.string.plv_live_subtitle_not_show)
            }
            liveRealTimeSubtitleLanguageTv.text = subtitleLanguageText
        }.addTo(this.observers)
        watchStates {
            val setting = languageSetting.value ?: return@watchStates
            val translateLanguage = setting.translateLanguage ?: return@watchStates
            onViewActionListener?.onSetSubtitleTranslateLanguage(translateLanguage)
        }.addTo(this.observers)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.observers.disposeAll()
    }

    fun initData(liveRoomDataManager: IPLVLiveRoomDataManager) = runAfterOnActivityCreated {
        val subtitleSetting = PLVChannelFeatureManager.onChannel(liveRoomDataManager.config.channelId)
            .get(PLVChannelFeature.LIVE_SUBTITLE_SETTING)
        val showOriginSubtitle = subtitleSetting?.enable == true && subtitleSetting.originLanguage != null
        val showTranslateSubtitle = subtitleSetting?.enable == true && subtitleSetting.translationEnable && subtitleSetting.defaultTranslationLanguage != null
        val originLanguage = subtitleSetting?.originLanguage
        val translateLanguage = subtitleSetting?.defaultTranslationLanguage
        languageSetting.setValue(
            PLVLCRealTimeSubtitleLanguageSetting(
                subtitleSetting,
                showOriginSubtitle,
                showTranslateSubtitle,
                originLanguage,
                translateLanguage
            )
        )
        settingPopupMenu.initData()
    }

    fun updateSubtitles(subtitles: List<PLVLiveSubtitleTranslation>?) {
        val isScrollerAtBottom = layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1
        adapter.updateItems(subtitles ?: emptyList())
        if (isScrollerAtBottom) {
            liveRealTimeSubtitleRv.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private class PLVLCRealTimeSubtitleAdapter(
        private val languageSetting: State<PLVLCRealTimeSubtitleLanguageSetting>
    ) : RecyclerView.Adapter<PLVLCRealTimeSubtitleAdapter.ViewHolder>() {

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
            private val languageSetting: State<PLVLCRealTimeSubtitleLanguageSetting>
        ) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.plvlc_live_real_time_subtitle_item, parent, false)
        ) {
            private val liveRealTimeSubtitleOriginTv by lazy { findViewById<TextView>(R.id.plvlc_live_real_time_subtitle_origin_tv) }
            private val liveRealTimeSubtitleTranslateDivider by lazy { findViewById<View>(R.id.plvlc_live_real_time_subtitle_translate_divider) }
            private val liveRealTimeSubtitleTranslateTv by lazy { findViewById<TextView>(R.id.plvlc_live_real_time_subtitle_translate_tv) }

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
                }.disposeOnDetached(itemView)
            }
        }
    }

    interface OnViewActionListener {
        fun onSetSubtitleTranslateLanguage(language: String)
    }

}

private class PLVLCLiveSubtitleSettingPopupMenu(
    context: Context,
    private val stateLanguageSetting: MutableState<PLVLCRealTimeSubtitleLanguageSetting>
) : FrameLayout(context) {
    private val liveSubtitleSettingTitle by lazy { findViewById<TextView>(R.id.plvlc_live_subtitle_setting_title) }
    private val liveSubtitleEnableLayout by lazy { findViewById<ConstraintLayout>(R.id.plvlc_live_subtitle_enable_layout) }
    private val liveSubtitleEnableSwitch by lazy { findViewById<Switch>(R.id.plvlc_live_subtitle_enable_switch) }
    private val liveSubtitleLanguageLayout by lazy { findViewById<ConstraintLayout>(R.id.plvlc_live_subtitle_language_layout) }
    private val liveSubtitleLanguageTv by lazy { findViewById<TextView>(R.id.plvlc_live_subtitle_language_tv) }
    private val liveSubtitleLanguageIv by lazy { findViewById<ImageView>(R.id.plvlc_live_subtitle_language_iv) }
    private val liveSubtitleDoubleLanguageLayout by lazy { findViewById<ConstraintLayout>(R.id.plvlc_live_subtitle_double_language_layout) }
    private val liveSubtitleDoubleLanguageEnableSwitch by lazy { findViewById<Switch>(R.id.plvlc_live_subtitle_double_language_enable_switch) }

    private val stateMenuSetting = mutableStateOf<PopupMenuLanguageSetting>()
    private val selectLanguagePopupMenu = PLVLCLiveSubtitleSettingLanguagePopupMenu(
        context,
        stateLanguageSetting,
        stateMenuSetting,
        onClose = { this.show() }
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.plvlc_live_subtitle_setting_popup_menu, this)
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
                else -> ""
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

private class PLVLCLiveSubtitleSettingLanguagePopupMenu(
    context: Context,
    private val stateLanguageSetting: MutableState<PLVLCRealTimeSubtitleLanguageSetting>,
    private val stateMenuSetting: MutableState<PopupMenuLanguageSetting>,
    private val onClose: () -> Unit
) : FrameLayout(context) {
    private val liveSubtitleSettingLanguageTitle by lazy { findViewById<TextView>(R.id.plvlc_live_subtitle_setting_language_title) }
    private val liveSubtitleSettingLanguageRv by lazy { findViewById<RecyclerView>(R.id.plvlc_live_subtitle_setting_language_rv) }
    private val adapter = Adapter()

    init {
        LayoutInflater.from(context).inflate(R.layout.plvlc_live_subtitle_setting_language_popup_menu, this)
        liveSubtitleSettingLanguageRv.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
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
                .inflate(R.layout.plvlc_live_subtitle_setting_language_item, parent, false)
        ) {
            private val liveSubtitleSettingLanguageTv by lazy { findViewById<TextView>(R.id.plvlc_live_subtitle_setting_language_tv) }
            private val liveSubtitleSettingLanguageSelectedIv by lazy { findViewById<ImageView>(R.id.plvlc_live_subtitle_setting_language_selected_iv) }

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

private data class PLVLCRealTimeSubtitleLanguageSetting(
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