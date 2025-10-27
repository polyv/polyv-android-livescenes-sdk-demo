package com.easefun.polyv.streameralone.modules.streamer.overlay

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position
import com.easefun.polyv.streameralone.R
import com.plv.foundationsdk.utils.PLVTimeUtils
import net.polyv.android.common.libs.lang.Duration.Companion.seconds
import net.polyv.android.common.libs.lang.di.PLVLocalProvider
import net.polyv.android.common.libs.lang.graphic.dp
import net.polyv.android.common.libs.lang.state.mutableStateOf
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
private val LocalViewState = PLVLocalProvider<MediaOverlaySettingViewState>()

class PLVSAStreamerMediaOverlaySetting @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val streamerMediaOverlayProgressTv by lazy { findViewById<PLVSAStreamerMediaOverlayProgressTextView>(R.id.plvsa_streamer_media_overlay_progress_tv) }
    private val streamerMediaOverlayDurationTv by lazy { findViewById<PLVSAStreamerMediaOverlayDurationTextView>(R.id.plvsa_streamer_media_overlay_duration_tv) }
    private val streamerMediaOverlayProgressSeekBar by lazy { findViewById<PLVSAStreamerMediaOverlayProgressSeekBar>(R.id.plvsa_streamer_media_overlay_progress_seek_bar) }
    private val streamerMediaOverlayPlayButton by lazy { findViewById<PLVSAStreamerMediaOverlayPlayButton>(R.id.plvsa_streamer_media_overlay_play_button) }
    private val streamerMediaOverlaySeekBackwardIv by lazy { findViewById<PLVSAStreamerMediaOverlaySeekBackwardIcon>(R.id.plvsa_streamer_media_overlay_seek_backward_iv) }
    private val streamerMediaOverlaySeekForwardIv by lazy { findViewById<PLVSAStreamerMediaOverlaySeekForwardIcon>(R.id.plvsa_streamer_media_overlay_seek_forward_iv) }
    private val streamerMediaOverlayDeleteIv by lazy { findViewById<ImageView>(R.id.plvsa_streamer_media_overlay_delete_iv) }
    private val streamerMediaOverlayVolumeIv by lazy { findViewById<PLVSAStreamerMediaOverlayVolumeIcon>(R.id.plvsa_streamer_media_overlay_volume_iv) }

    private val menuDrawer by lazy {
        PLVMenuDrawer.attach(
            context as Activity,
            PLVMenuDrawer.Type.OVERLAY,
            Position.BOTTOM,
            PLVMenuDrawer.MENU_DRAG_CONTAINER,
            context.findViewById(R.id.plvsa_live_room_popup_container)
        ).apply {
            menuView = this@PLVSAStreamerMediaOverlaySetting
            menuSize = 110.dp().px()
            setPosition(Position.BOTTOM)
            touchMode = PLVMenuDrawer.TOUCH_MODE_BEZEL
            drawOverlay = false
            setDropShadowEnabled(false)
            setOnDrawerStateChangeListener(object : PLVMenuDrawer.OnDrawerStateChangeListener {
                override fun onDrawerStateChange(oldState: Int, newState: Int) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        detachToContainer()
                    }
                }

                override fun onDrawerSlide(openRatio: Float, offsetPixels: Int) {

                }
            })
        }
    }

    var onClickStopMediaOverlay: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.plvsa_streamer_media_overlay_setting_popup_layout, this)
        streamerMediaOverlayDeleteIv.setOnClickListener {
            onClickStopMediaOverlay?.invoke()
        }
    }

    fun show(
        presenter: IPLVStreamerContract.IStreamerPresenter,
        mediaPlayer: IPLVMediaPlayer
    ) {
        LocalViewState.on(this).provide(MediaOverlaySettingViewState(this, presenter, mediaPlayer))

        menuDrawer.attachToContainer()
        menuDrawer.openMenu()
    }

    fun hide() {
        menuDrawer.closeMenu()
    }

}

class PLVSAStreamerMediaOverlayProgressSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatSeekBar(context, attrs, defStyle), OnSeekBarChangeListener {
    private var seekBarHeight = 0f
    private var seekBarHeightOnDrag = 0f
    private var progressDrawableOnDrag: Drawable? = null
    private var thumbDrawableOnDrag: Drawable? = null

    init {
        parseAttrs(attrs)
        setOnSeekBarChangeListener(this)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PLVSAStreamerMediaOverlayProgressSeekBar)
        seekBarHeight = typedArray.getDimension(
            R.styleable.PLVSAStreamerMediaOverlayProgressSeekBar_plv_seek_bar_height,
            seekBarHeight
        )
        seekBarHeightOnDrag = typedArray.getDimension(
            R.styleable.PLVSAStreamerMediaOverlayProgressSeekBar_plv_seek_bar_height_on_drag,
            seekBarHeight
        )
        progressDrawableOnDrag = typedArray.getDrawable(R.styleable.PLVSAStreamerMediaOverlayProgressSeekBar_plv_progress_drawable_on_drag)
        thumbDrawableOnDrag = typedArray.getDrawable(R.styleable.PLVSAStreamerMediaOverlayProgressSeekBar_plv_thumb_drawable_on_drag)
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val viewState = LocalViewState.on(this).current()!!

        viewState.mediaPlayer.getEventListenerRegistry()
            .onSeekCompleteEvent
            .observe { viewState.isDragSeekFinished.setValue(true) }
            .disposeOnDetached(this)

        watchStates {
            val playerProgress = viewState.playerProgress.value ?: 0
            val duration = viewState.duration.value ?: 0
            val dragProgress = viewState.dragProgress.value ?: 0
            val isDragging = viewState.isDragging.value ?: false
            val isDragSeekFinished = viewState.isDragSeekFinished.value ?: true
            val showProgress = if (isDragging || !isDragSeekFinished) dragProgress else playerProgress
            progress = showProgress.toInt()
            max = duration.toInt()
        }.disposeOnDetached(this)

    }

    // <editor-fold defaultstate="collapsed" desc="OnSeekBarChangeListener">

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val viewState = LocalViewState.on(this).current()!!
        viewState.dragProgress.setValue(progress.toLong())
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        val viewState = LocalViewState.on(this).current()!!
        viewState.isDragging.setValue(true)
        viewState.isDragSeekFinished.setValue(false)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        val viewState = LocalViewState.on(this).current()!!
        viewState.isDragging.setValue(false)
        val dragProgress = viewState.dragProgress.value ?: 0
        viewState.mediaPlayer.seek(dragProgress)
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onDraw">

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        drawBackground(canvas)
        drawProgress(canvas)
        drawThumb(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun drawBackground(canvas: Canvas?) {
        val drawable = getProgressDrawableById(android.R.id.background) ?: return
        val height = getSeekBarHeight()
        val top = (getHeight() - height) / 2
        drawable.setBounds(0, top, this.getSeekBarWidth(), top + height)
        drawable.draw(canvas!!)
    }

    private fun drawProgress(canvas: Canvas?) {
        val drawable = getProgressDrawableById(android.R.id.progress) ?: return
        val height = getSeekBarHeight()
        val top = (getHeight() - height) / 2
        drawable.setLevel((progressPercent() * 10000).toInt())
        drawable.setBounds(0, top, this.getSeekBarWidth(), top + height)
        drawable.draw(canvas!!)
    }

    private fun drawThumb(canvas: Canvas?) {
        val drawable = getThumbDrawable() ?: return
        val thumbWidth = drawable.intrinsicWidth
        val thumbHeight = drawable.intrinsicHeight
        val left = (progressPercent() * this.getSeekBarWidth() - thumbWidth / 2).toInt()
        val top = (height - thumbHeight) / 2
        drawable.setBounds(left, top, left + thumbWidth, top + thumbHeight)
        drawable.draw(canvas!!)
    }

    private fun getProgressDrawableById(id: Int): Drawable? {
        val viewState = LocalViewState.on(this).current()!!
        val isDragging = viewState.isDragging.value ?: false
        var drawable = if (!isDragging) {
            progressDrawable
        } else {
            progressDrawableOnDrag
        }
        if (drawable is LayerDrawable) {
            drawable = drawable.findDrawableByLayerId(id)
        }
        return drawable
    }

    private fun getThumbDrawable(): Drawable? {
        val viewState = LocalViewState.on(this).current()!!
        val isDragging = viewState.isDragging.value ?: false
        return if (!isDragging) {
            thumb
        } else {
            thumbDrawableOnDrag
        }
    }

    private fun getSeekBarWidth(): Int {
        return width - getPaddingLeft() - getPaddingRight()
    }

    private fun getSeekBarHeight(): Int {
        val viewState = LocalViewState.on(this).current()!!
        val isDragging = viewState.isDragging.value ?: false
        return if (!isDragging) {
            seekBarHeight.toInt()
        } else {
            seekBarHeightOnDrag.toInt()
        }
    }

    private fun progressPercent(): Double {
        return progress.toDouble() / max
    }

    // </editor-fold>
}

class PLVSAStreamerMediaOverlayPlayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle), View.OnClickListener {

    init {
        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val player = LocalViewState.on(this).current()!!.mediaPlayer

        watchStates {
            val isPlaying = player.getStateListenerRegistry().playingState.value == PLVMediaPlayerPlayingState.PLAYING
            if (isPlaying) {
                setImageResource(R.drawable.plvsa_streamer_media_overlay_play_button_to_pause_icon)
            } else {
                setImageResource(R.drawable.plvsa_streamer_media_overlay_play_button_to_play_icon)
            }
        }.disposeOnDetached(this)
    }

    override fun onClick(v: View?) {
        val player = LocalViewState.on(this).current()?.mediaPlayer ?: return
        val isPlaying = player.getStateListenerRegistry().playingState.value == PLVMediaPlayerPlayingState.PLAYING
        if (isPlaying) {
            player.pause()
        } else {
            player.start()
        }
    }

}

class PLVSAStreamerMediaOverlayProgressTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val viewState = LocalViewState.on(this).current()!!
        watchStates {
            val playerProgress = viewState.playerProgress.value ?: 0
            val dragProgress = viewState.dragProgress.value ?: 0
            val isDragging = viewState.isDragging.value ?: false
            val isDragSeekFinished = viewState.isDragSeekFinished.value ?: true
            val showProgress = if (isDragging || !isDragSeekFinished) dragProgress else playerProgress
            if (showProgress > 0) {
                text = PLVTimeUtils.generateTime(showProgress)
            }
        }.disposeOnDetached(this)
    }
}

class PLVSAStreamerMediaOverlayDurationTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val viewState = LocalViewState.on(this).current()!!
        watchStates {
            val duration = viewState.duration.value ?: 0
            if (duration > 0) {
                text = PLVTimeUtils.generateTime(duration)
            }
        }.disposeOnDetached(this)
    }
}

class PLVSAStreamerMediaOverlaySeekBackwardIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    init {
        setImageResource(R.drawable.plvsa_streamer_media_overlay_seek_backward_icon)
        setOnClickListener {
            val viewState = LocalViewState.on(this).current() ?: return@setOnClickListener
            val progress = viewState.playerProgress.value ?: 0
            val target = (progress - 10.seconds().toMillis()).coerceAtLeast(0)
            viewState.mediaPlayer.seek(target)
        }
    }
}

class PLVSAStreamerMediaOverlaySeekForwardIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    init {
        setImageResource(R.drawable.plvsa_streamer_media_overlay_seek_forward_icon)
        setOnClickListener {
            val viewState = LocalViewState.on(this).current() ?: return@setOnClickListener
            val progress = viewState.playerProgress.value ?: 0
            val duration = viewState.duration.value ?: 0
            val target = (progress + 10.seconds().toMillis()).coerceAtMost(duration)
            viewState.mediaPlayer.seek(target)
        }
    }
}

class PLVSAStreamerMediaOverlayVolumeIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val audioSettingLayout by lazy { PLVSAStreamerMediaOverlayAudioSetting(context) }

    init {
        setOnClickListener {
            val viewState = LocalViewState.on(this).current()!!
            viewState.settingLayout.hide()
            audioSettingLayout.show(viewState.presenter, viewState.mediaPlayer)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val viewState = LocalViewState.on(this).current()!!
        watchStates {
            val localVolume = viewState.mediaPlayer.getStateListenerRegistry().volume.value ?: 100
            val remoteVolume = viewState.presenter.data.mediaOverlayRemoteVolume.value ?: 100
            if (remoteVolume > 0 || localVolume > 0) {
                setImageResource(R.drawable.plvsa_streamer_media_overlay_volume_open)
            } else {
                setImageResource(R.drawable.plvsa_streamer_media_overlay_volume_close)
            }
        }
    }

}

private class MediaOverlaySettingViewState(
    val settingLayout: PLVSAStreamerMediaOverlaySetting,
    val presenter: IPLVStreamerContract.IStreamerPresenter,
    val mediaPlayer: IPLVMediaPlayer
) {
    val playerProgress = mediaPlayer.getStateListenerRegistry().progressState
    val duration = mediaPlayer.getStateListenerRegistry().durationState
    val dragProgress = mutableStateOf(0L)
    val isDragging = mutableStateOf(false)
    val isDragSeekFinished = mutableStateOf(true)
}