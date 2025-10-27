package com.easefun.polyv.streameralone.modules.streamer.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.polyv.android.common.libs.lang.ui.removeFromParent
import net.polyv.android.common.libs.lang.ui.updateLayoutParams
import net.polyv.android.player.business.scene.common.coroutine.PLVMediaPlayerGlobalCoroutineScope
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.core.api.option.PLVMediaPlayerOptionEnum
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.watchStates
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

/**
 * @author Hoshiiro
 */
class PLVSAStreamerMediaOverlayContainer(
    context: Context
) : FrameLayout(context) {

    private val settingLayout by lazy {
        PLVSAStreamerMediaOverlaySetting(context).apply {
            onClickStopMediaOverlay = {
                mediaOverlay?.removeFromParent()
                mediaOverlay?.destroy()
                mediaOverlay = null
                this@apply.hide()
            }
        }
    }
    private var mediaOverlay: PLVSAStreamerMediaOverlay? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mediaOverlay?.updatePosition(width, height)
    }

    fun showSetting(presenter: IPLVStreamerContract.IStreamerPresenter) {
        if (mediaOverlay != null) {
            settingLayout.show(presenter, mediaOverlay!!.mediaOverlay)
        } else {
            PLVMediaPlayerGlobalCoroutineScope.launch(Dispatchers.Main) {
                val result = (context as FragmentActivity).selectVideo()
                if (result.isSuccess) {
                    val uri = result.getOrThrow()
                    mediaOverlay = PLVSAStreamerMediaOverlay(context, presenter, uri).also {
                        addView(it)
                        it.updatePosition(width, height)
                        it.setOnClickListener {
                            settingLayout.show(presenter, mediaOverlay!!.mediaOverlay)
                        }
                    }

                    settingLayout.show(presenter, mediaOverlay!!.mediaOverlay)
                }
            }
        }
    }

    fun canProcessTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        mediaOverlay ?: return false
        val location = Rect()
        mediaOverlay!!.getGlobalVisibleRect(location)
        return location.contains(event.rawX.toInt(), event.rawY.toInt())
    }

    fun processTouchEvent(event: MotionEvent?): Boolean {
        return if (canProcessTouchEvent(event)) {
            mediaOverlay?.processTouchEvent(event!!)
            true
        } else false
    }

    fun destroy() {
        mediaOverlay?.removeFromParent()
        mediaOverlay?.destroy()
    }

}

/**
 * 占位边框，实际视频内容不在视图树上
 */
class PLVSAStreamerMediaOverlay(
    context: Context,
    val presenter: IPLVStreamerContract.IStreamerPresenter,
    mediaUri: Uri
) : FrameLayout(context) {

    val mediaOverlay = presenter.createMediaOverlay()
    private val mediaOverlayDisplayRect = RectF()
    private var displayRectInitObserver: MutableObserver<*>? = null
    private var isPortrait = ScreenUtils.isPortrait()

    init {
        presenter.setMediaOverlayDisplayRect(mediaOverlayDisplayRect)
        mediaOverlay.setPlayerOption(
            listOf(
                PLVMediaPlayerOptionEnum.ENABLE_ACCURATE_SEEK.value("1"),
                PLVMediaPlayerOptionEnum.SKIP_ACCURATE_SEEK_AT_START.value("1")
            )
        )
        mediaOverlay.setVolume(0)
        mediaOverlay.setMediaResource(PLVMediaResource.uri(mediaUri))
        observeVideoSizeInitDisplayRect()
    }

    private fun observeVideoSizeInitDisplayRect() {
        displayRectInitObserver = watchStates {
            val videoSize = mediaOverlay.getStateListenerRegistry().videoSize.value ?: return@watchStates
            val videoRotation = mediaOverlay.getStateListenerRegistry().videoRotation.value ?: return@watchStates
            if (videoSize.width() == 0 || videoSize.height() == 0) return@watchStates
            val flipWidthHeight = videoRotation % 180 != 0
            val width = if (!flipWidthHeight) videoSize.width() else videoSize.height()
            val height = if (!flipWidthHeight) videoSize.height() else videoSize.width()
            val parent = parent as? View ?: return@watchStates
            val parentWidth = parent.width
            val parentHeight = parent.height
            if (parentWidth == 0 || parentHeight == 0) return@watchStates
            displayRectInitObserver?.dispose()

            val videoRatio = width.toFloat() / height
            val cameraRatio = if (ScreenUtils.isPortrait()) 9F / 16 else 16F / 9
            if (videoRatio > cameraRatio) {
                mediaOverlayDisplayRect.left = 0F
                mediaOverlayDisplayRect.right = 1F
                val mediaWidth = parentWidth * mediaOverlayDisplayRect.width()
                val mediaHeight = mediaWidth / videoRatio
                mediaOverlayDisplayRect.top = 0.2F
                mediaOverlayDisplayRect.bottom = mediaOverlayDisplayRect.top + mediaHeight / parentHeight
                if (mediaOverlayDisplayRect.bottom > 0.8F) {
                    mediaOverlayDisplayRect.top = 0.5F - mediaHeight / 2 / parentHeight
                    mediaOverlayDisplayRect.bottom = 0.5F + mediaHeight / 2 / parentHeight
                }
            } else {
                mediaOverlayDisplayRect.top = 0F
                mediaOverlayDisplayRect.bottom = 1F
                val mediaHeight = parentHeight * mediaOverlayDisplayRect.height()
                val mediaWidth = mediaHeight * videoRatio
                mediaOverlayDisplayRect.left = 0.5F - mediaWidth / 2 / parentWidth
                mediaOverlayDisplayRect.right = 0.5F + mediaWidth / 2 / parentWidth
            }
            updatePosition(parentWidth, parentHeight)
        }
    }

    fun processTouchEvent(event: MotionEvent) {
        singleClickGestureDetector.onTouchEvent(event)
        moveGestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
    }

    private val singleClickGestureDetector = object {
        private var downTimestamp: Long = 0
        private var downX: Float = 0F
        private var downY: Float = 0F
        fun onTouchEvent(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downTimestamp = System.currentTimeMillis()
                    downX = event.rawX
                    downY = event.rawY
                }

                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - downTimestamp > 300) {
                        return
                    }
                    if (abs(downX - event.rawX) > 3 || abs(downY - event.rawY) > 3) {
                        return
                    }
                    callOnClick()
                }

                MotionEvent.ACTION_CANCEL -> {
                    downTimestamp = 0
                }
            }
        }
    }

    private val moveGestureDetector = object {
        private var masterPointerIndex = -1
        private var lastX = 0
        private var lastY = 0
        fun onTouchEvent(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    masterPointerIndex = event.actionIndex
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    masterPointerIndex = event.actionIndex
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (masterPointerIndex == -1) {
                        masterPointerIndex = event.actionIndex
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        return
                    }
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    move(dx.toInt(), dy.toInt())
                }

                MotionEvent.ACTION_POINTER_UP,
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    masterPointerIndex = -1
                }
            }
        }
    }

    private val scaleGestureDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scale(detector.scaleFactor)
                return true
            }
        }
    )

    private fun move(dx: Int, dy: Int) {
        val parent = parent as? View ?: return
        val parentWidth = parent.width
        val parentHeight = parent.height
        val newLeft = parentWidth * mediaOverlayDisplayRect.left + dx
        val newTop = parentHeight * mediaOverlayDisplayRect.top + dy
        val newRight = parentWidth * mediaOverlayDisplayRect.right + dx
        val newBottom = parentHeight * mediaOverlayDisplayRect.bottom + dy
        mediaOverlayDisplayRect.left = newLeft / parentWidth
        mediaOverlayDisplayRect.top = newTop / parentHeight
        mediaOverlayDisplayRect.right = newRight / parentWidth
        mediaOverlayDisplayRect.bottom = newBottom / parentHeight
        updatePosition(parentWidth, parentHeight)
    }

    private fun scale(factor: Float) {
        val parent = parent as? View ?: return
        val parentWidth = parent.width
        val parentHeight = parent.height
        val centerX = parentWidth * (mediaOverlayDisplayRect.left + mediaOverlayDisplayRect.width() / 2)
        val centerY = parentHeight * (mediaOverlayDisplayRect.top + mediaOverlayDisplayRect.height() / 2)
        val newWidth = parentWidth * mediaOverlayDisplayRect.width() * factor
        val newHeight = parentHeight * mediaOverlayDisplayRect.height() * factor
        val newLeft = centerX - newWidth / 2
        val newTop = centerY - newHeight / 2
        val newRight = centerX + newWidth / 2
        val newBottom = centerY + newHeight / 2
        mediaOverlayDisplayRect.left = newLeft / parentWidth
        mediaOverlayDisplayRect.top = newTop / parentHeight
        mediaOverlayDisplayRect.right = newRight / parentWidth
        mediaOverlayDisplayRect.bottom = newBottom / parentHeight
        updatePosition(parentWidth, parentHeight)
    }

    fun updatePosition(parentWidth: Int, parentHeight: Int) {
        updateLayoutParams<MarginLayoutParams> {
            leftMargin = (mediaOverlayDisplayRect.left * parentWidth).toInt()
            topMargin = (mediaOverlayDisplayRect.top * parentHeight).toInt()
            width = (mediaOverlayDisplayRect.width() * parentWidth).toInt()
            height = (mediaOverlayDisplayRect.height() * parentHeight).toInt()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val portrait = newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT
        if (portrait != isPortrait) {
            isPortrait = portrait
            post { observeVideoSizeInitDisplayRect() }
        }
    }

    fun destroy() {
        displayRectInitObserver?.dispose()
        presenter.removeMediaOverlay(this.mediaOverlay)
    }

}

class PLVSAStreamerMediaOverlaySelectVideoFragment : Fragment() {
    lateinit var onSelectResult: (result: Result<Uri>) -> Unit

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val intent = Intent.createChooser(
            Intent().apply {
                setType("video/*")
                setAction(Intent.ACTION_GET_CONTENT)
                addCategory(Intent.CATEGORY_OPENABLE)
            },
            "Select Video"
        )
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1000) {
            return
        }
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                onSelectResult(Result.success(uri))
            } else {
                onSelectResult(Result.failure(IllegalStateException("result is null")))
            }
        } else {
            onSelectResult(Result.failure(IllegalStateException("result is not ok: $resultCode")))
        }
        fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
    }
}

private suspend fun FragmentActivity.selectVideo(): Result<Uri> = suspendCoroutine { continuation ->
    val fragment = PLVSAStreamerMediaOverlaySelectVideoFragment().apply {
        onSelectResult = { continuation.resume(it) }
    }
    supportFragmentManager.beginTransaction()
        .add(fragment, fragment.javaClass.simpleName)
        .commitAllowingStateLoss()
}