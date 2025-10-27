package com.easefun.polyv.streameralone.modules.streamer.overlay

import android.app.Activity
import android.content.Context
import androidx.appcompat.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout
import com.easefun.polyv.streameralone.R
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils
import net.polyv.android.common.libs.lang.di.PLVLocalProvider
import net.polyv.android.common.libs.lang.graphic.dp
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
private val LocalViewState = PLVLocalProvider<MediaOverlayAudioSettingViewState>()

class PLVSAStreamerMediaOverlayAudioSetting @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val streamerMediaOverlayAudioSettingLayoutRoot by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plvsa_streamer_media_overlay_audio_setting_layout_root) }
    private val streamerMediaOverlayAudioSettingTitle by lazy { findViewById<TextView>(R.id.plvsa_streamer_media_overlay_audio_setting_title) }
    private val streamerMediaOverlayRemoteVolumeTitle by lazy { findViewById<TextView>(R.id.plvsa_streamer_media_overlay_remote_volume_title) }
    private val streamerMediaOverlayRemoteVolumeTv by lazy { findViewById<TextView>(R.id.plvsa_streamer_media_overlay_remote_volume_tv) }
    private val streamerMediaOverlayRemoteVolumeSeekBar by lazy {
        findViewById<PLVSAStreamerMediaOverlayRemoteVolumeSeekBar>(
            R.id.plvsa_streamer_media_overlay_remote_volume_seek_bar
        )
    }
    private val streamerMediaOverlayLocalVolumeTitle by lazy { findViewById<TextView>(R.id.plvsa_streamer_media_overlay_local_volume_title) }
    private val streamerMediaOverlayLocalVolumeTv by lazy { findViewById<TextView>(R.id.plvsa_streamer_media_overlay_local_volume_tv) }
    private val streamerMediaOverlayLocalVolumeSeekBar by lazy {
        findViewById<PLVSAStreamerMediaOverlayLocalVolumeSeekBar>(
            R.id.plvsa_streamer_media_overlay_local_volume_seek_bar
        )
    }

    private val menuDrawer by lazy {
        PLVMenuDrawer.attach(
            context as Activity,
            PLVMenuDrawer.Type.OVERLAY,
            Position.BOTTOM,
            PLVMenuDrawer.MENU_DRAG_CONTAINER,
            context.findViewById(R.id.plvsa_live_room_popup_container)
        ).apply {
            menuView = this@PLVSAStreamerMediaOverlayAudioSetting
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

    init {
        LayoutInflater.from(context).inflate(R.layout.plvsa_streamer_media_overlay_audio_setting_popup_layout, this)
    }

    fun show(
        presenter: IPLVStreamerContract.IStreamerPresenter,
        mediaPlayer: IPLVMediaPlayer
    ) {
        LocalViewState.on(this).provide(MediaOverlayAudioSettingViewState(presenter, mediaPlayer))

        if (ScreenUtils.isPortrait()) {
            menuDrawer.menuSize = 240.dp().px()
            menuDrawer.setPosition(Position.BOTTOM)
            streamerMediaOverlayAudioSettingLayoutRoot.setRoundMode(PLVRoundRectConstraintLayout.MODE_TOP)
        } else {
            menuDrawer.menuSize = 375.dp().px()
            menuDrawer.setPosition(Position.END)
            streamerMediaOverlayAudioSettingLayoutRoot.setRoundMode(PLVRoundRectConstraintLayout.MODE_LEFT)
        }

        menuDrawer.attachToContainer()
        menuDrawer.openMenu()
    }

    fun hide() {
        menuDrawer.closeMenu()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val viewState = LocalViewState.on(this).current()!!
        watchStates {
            val localVolume = viewState.mediaPlayer.getStateListenerRegistry().volume.value ?: 100
            streamerMediaOverlayLocalVolumeTv.text = "${localVolume}%"
            val remoteVolume = viewState.presenter.data.mediaOverlayRemoteVolume.value ?: 100
            streamerMediaOverlayRemoteVolumeTv.text = "${remoteVolume}%"
        }.disposeOnDetached(this)
    }

}

class PLVSAStreamerMediaOverlayRemoteVolumeSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatSeekBar(context, attrs, defStyle), OnSeekBarChangeListener {

    init {
        setOnSeekBarChangeListener(this)
        max = 100
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val viewState = LocalViewState.on(this).current()!!
        watchStates {
            val volume = viewState.presenter.data.mediaOverlayRemoteVolume.value ?: 100
            progress = volume
        }.disposeOnDetached(this)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val viewState = LocalViewState.on(this).current()
        viewState?.presenter?.setMediaOverlayVolume(progress)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

}

class PLVSAStreamerMediaOverlayLocalVolumeSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatSeekBar(context, attrs, defStyle), OnSeekBarChangeListener {

    init {
        setOnSeekBarChangeListener(this)
        max = 100
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return
        val player = LocalViewState.on(this).current()!!.mediaPlayer
        watchStates {
            progress = player.getStateListenerRegistry().volume.value ?: 0
        }.disposeOnDetached(this)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val player = LocalViewState.on(this).current()?.mediaPlayer
        player?.setVolume(progress)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

}

private class MediaOverlayAudioSettingViewState(
    val presenter: IPLVStreamerContract.IStreamerPresenter,
    val mediaPlayer: IPLVMediaPlayer
)