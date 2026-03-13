package com.easefun.polyv.livecloudclass.modules.media.widget

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.easefun.polyv.livecloudclass.R
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract
import com.easefun.polyv.livecommon.module.utils.imageloader.loadImage
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.findViewById
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position
import com.easefun.polyv.livecommon.ui.widget.menudrawer.hidePopupMenu
import com.easefun.polyv.livecommon.ui.widget.menudrawer.showPopupMenu
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout
import com.plv.foundationsdk.utils.PLVTimeUnit.Companion.seconds
import com.plv.foundationsdk.utils.PLVTimeUtils
import com.plv.livescenes.playback.vo.PLVPlaybackMarksResponseVO
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils

/**
 * 回放精彩看点
 * @author Hoshiiro
 */
class PLVLCPlaybackVideoMarksBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val popupMenu = PLVLCPlaybackVideoMarksPopupMenu(context)

    private var presenter: IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.plvlc_playback_video_marks_bar_layout, this)
        setOnClickListener { popupMenu.show() }
        popupMenu.onClickVideoMark = {
            val position = it.markTime?.toIntOrNull()?.seconds()?.toMillis()?.toInt()
            if (position != null) {
                presenter?.seekTo(position)
            }
        }
    }

    fun initData(presenter: IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter) {
        this.presenter = presenter
        presenter.data.videoMarks.observe(context as LifecycleOwner, Observer {
            val videoMarks = it ?: emptyList()
            visibility = if (videoMarks.isEmpty()) View.GONE else View.VISIBLE
            popupMenu.updateMarks(videoMarks)
        })
    }

}

class PLVLCPlaybackVideoMarksPopupMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val playbackVideoMarksPopupMenuLayoutRoot by lazy { findViewById<PLVRoundRectConstraintLayout>(R.id.plvlc_playback_video_marks_popup_menu_layout_root) }
    private val playbackVideoMarksTitleTv by lazy { findViewById<TextView>(R.id.plvlc_playback_video_marks_title_tv) }
    private val playbackVideoMarksSplitLine by lazy { findViewById<View>(R.id.plvlc_playback_video_marks_split_line) }
    private val playbackVideoMarksRv by lazy { findViewById<RecyclerView>(R.id.plvlc_playback_video_marks_rv) }

    private val adapter = VideoMarksAdapter()

    var onClickVideoMark: (mark: PLVPlaybackMarksResponseVO.Data) -> Unit = {}

    init {
        LayoutInflater.from(context).inflate(R.layout.plvlc_playback_video_marks_popup_menu_layout, this)
        adapter.onClickVideoMark = {
            onClickVideoMark(it)
            hide()
        }
        playbackVideoMarksRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        playbackVideoMarksRv.adapter = adapter
    }

    fun updateMarks(marks: List<PLVPlaybackMarksResponseVO.Data>) {
        adapter.updateMarks(marks)
    }

    fun show() {
        if (ScreenUtils.isPortrait()) {
            playbackVideoMarksPopupMenuLayoutRoot.setRoundMode(PLVRoundRectConstraintLayout.MODE_TOP)
            context.showPopupMenu(this, Position.BOTTOM, ConvertUtils.dp2px(570F))
        } else {
            playbackVideoMarksPopupMenuLayoutRoot.setRoundMode(PLVRoundRectConstraintLayout.MODE_LEFT)
            context.showPopupMenu(this, Position.RIGHT, ConvertUtils.dp2px(375F))
        }
    }

    fun hide() {
        context.hidePopupMenu(this)
    }

    class VideoMarksAdapter : RecyclerView.Adapter<VideoMarksViewHolder>() {

        private val marks = mutableListOf<PLVPlaybackMarksResponseVO.Data>()
        var onClickVideoMark: (mark: PLVPlaybackMarksResponseVO.Data) -> Unit = {}

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) = VideoMarksViewHolder(parent)

        override fun onBindViewHolder(
            holder: VideoMarksViewHolder,
            position: Int
        ) {
            holder.bind(marks[position])
            holder.onClickVideoMark = {
                onClickVideoMark(it)
            }
        }

        override fun getItemCount() = marks.size

        fun updateMarks(marks: List<PLVPlaybackMarksResponseVO.Data>) {
            this.marks.clear()
            this.marks.addAll(marks)
            notifyDataSetChanged()
        }

    }

    class VideoMarksViewHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.plvlc_playback_video_marks_popup_menu_item, parent, false)
    ) {
        private val playbackVideoMarksPreviewIv by lazy { findViewById<ImageView>(R.id.plvlc_playback_video_marks_preview_iv) }
        private val playbackVideoMarksTitleTv by lazy { findViewById<TextView>(R.id.plvlc_playback_video_marks_title_tv) }
        private val playbackVideoMarksTimeTv by lazy { findViewById<TextView>(R.id.plvlc_playback_video_marks_time_tv) }

        var onClickVideoMark: (mark: PLVPlaybackMarksResponseVO.Data) -> Unit = {}

        fun bind(mark: PLVPlaybackMarksResponseVO.Data) {
            val previewUrl = mark.markMessage?.previewUrl
            val markTitle = mark.markMessage?.title
            val timeText = runCatching {
                PLVTimeUtils.generateTime(mark.markTime?.toInt()?.seconds()?.toMillis() ?: 0, true)
            }.getOrDefault("")

            playbackVideoMarksPreviewIv.loadImage(previewUrl)
            playbackVideoMarksTitleTv.text = markTitle
            playbackVideoMarksTimeTv.text = timeText
            itemView.setOnClickListener {
                onClickVideoMark(mark)
            }
        }
    }

}

