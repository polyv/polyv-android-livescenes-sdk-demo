package com.easefun.polyv.livecloudclass.modules.media.controller.widget

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.widget.AppCompatSeekBar
import android.util.AttributeSet
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract
import com.plv.foundationsdk.utils.PLVTimeUnit.Companion.seconds
import com.plv.livescenes.playback.vo.PLVPlaybackMarksResponseVO
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils

/**
 * + 精彩看点标记点位
 * @author Hoshiiro
 */
class PLVLCPlaybackControllerSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    private var videoDuration: Long = 0
    private val videoMarks = mutableListOf<PLVPlaybackMarksResponseVO.Data>()

    private val markPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
    }

    fun initData(presenter: IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter) {
        presenter.data.playInfoVO.observe(context as LifecycleOwner, Observer {
            it ?: return@Observer
            videoDuration = it.totalTime.toLong()
        })
        presenter.data.videoMarks.observe(context as LifecycleOwner, Observer {
            videoMarks.clear()
            if (it != null) {
                videoMarks.addAll(it)
            }
        })
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawMarks(canvas)
        drawThumb(canvas)
    }

    private fun drawMarks(canvas: Canvas) {
        if (videoDuration == 0L) return

        val saveCount = canvas.save()
        canvas.translate(getPaddingLeft().toFloat(), paddingTop.toFloat())

        videoMarks.forEach {
            val position = it.markTime?.toIntOrNull()?.seconds()?.toMillis() ?: return@forEach
            val centerX = (width - getPaddingLeft() - getPaddingRight()) * (position.toFloat() / videoDuration)
            val centerY = (height - paddingTop - paddingBottom) * 0.5F
            canvas.drawCircle(centerX, centerY, ConvertUtils.dp2px(1.5F).toFloat(), markPaint)
        }

        canvas.restoreToCount(saveCount)
    }

    // super.drawThumb
    private fun drawThumb(canvas: Canvas) {
        if (thumb != null) {
            val saveCount = canvas.save()
            canvas.translate((getPaddingLeft() - thumbOffset).toFloat(), paddingTop.toFloat())
            thumb.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }

}
