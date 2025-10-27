package com.easefun.polyv.livecommon.ui.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView

import com.easefun.polyv.livecommon.R
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils

/**
 * @author Hoshiiro
 */
class PLVChatMessagePopupMenu(
    val anchor: View,
    val actions: PLVChatMessagePopupMenuActions
) {

    companion object {
        @JvmStatic
        fun show(anchor: View, actions: PLVChatMessagePopupMenuActions): PLVChatMessagePopupMenu {
            return PLVChatMessagePopupMenu(anchor, actions).also { it.show() }
        }
    }

    private val root by lazy {
        LayoutInflater.from(anchor.context).inflate(R.layout.plv_chat_message_popup_menu_layout, null, false)
    }
    private val chatMessagePopupMenuLayoutRoot by lazy { root.findViewById<PLVTriangleIndicateLayout>(R.id.plv_chat_message_popup_menu_layout_root) }
    private val chatMessagePopupActionCopy by lazy { root.findViewById<TextView>(R.id.plv_chat_message_popup_action_copy) }
    private val chatMessagePopupActionReply by lazy { root.findViewById<TextView>(R.id.plv_chat_message_popup_action_reply) }
    private val chatMessagePopupActionPin by lazy { root.findViewById<TextView>(R.id.plv_chat_message_popup_action_pin) }
    private val chatMessagePopupActionBan by lazy { root.findViewById<TextView>(R.id.plv_chat_message_popup_action_ban) }
    private val chatMessagePopupActionKick by lazy { root.findViewById<TextView>(R.id.plv_chat_message_popup_action_kick) }

    private val popupWindow by lazy {
        PopupWindow().apply {
            contentView = root
            setBackgroundDrawable(ColorDrawable())
            isFocusable = true
            isOutsideTouchable = true
        }
    }

    init {
        chatMessagePopupActionCopy.bindAction(actions.copy)
        chatMessagePopupActionReply.bindAction(actions.reply)
        chatMessagePopupActionPin.bindAction(actions.pin)
        chatMessagePopupActionBan.bindAction(actions.ban)
        chatMessagePopupActionKick.bindAction(actions.kick)

        chatMessagePopupMenuLayoutRoot.setMarginAnchor(anchor)
    }

    private fun TextView.bindAction(action: PLVChatMessagePopupMenuAction) {
        visibility = if (action.visible) View.VISIBLE else View.GONE
        if (action.textRes != null) {
            setText(action.textRes)
        }
        setOnClickListener { action.onClick?.invoke(this@PLVChatMessagePopupMenu) }
    }

    fun show() {
        root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popupWindow.width = root.measuredWidth
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        val anchorLocation = IntArray(2).also { anchor.getLocationInWindow(it) }
        popupWindow.showAtLocation(
            anchor,
            Gravity.TOP or Gravity.START,
            (anchorLocation[0] + anchor.width / 2 - popupWindow.width / 2)
                .coerceIn(0, ScreenUtils.getScreenOrientatedWidth() - popupWindow.width),
            anchorLocation[1] - ConvertUtils.dp2px(46F)
        )
    }

    fun hide() {
        popupWindow.dismiss()
    }

}

class PLVChatMessagePopupMenuActions(
    val copy: PLVChatMessagePopupMenuAction,
    val reply: PLVChatMessagePopupMenuAction,
    val pin: PLVChatMessagePopupMenuAction,
    val ban: PLVChatMessagePopupMenuAction,
    val kick: PLVChatMessagePopupMenuAction
) {
    class Builder {
        private var copy = PLVChatMessagePopupMenuAction()
        private var reply = PLVChatMessagePopupMenuAction()
        private var pin = PLVChatMessagePopupMenuAction()
        private var ban = PLVChatMessagePopupMenuAction()
        private var kick = PLVChatMessagePopupMenuAction()

        fun setCopy(copy: PLVChatMessagePopupMenuAction) = apply { this.copy = copy }
        fun setReply(reply: PLVChatMessagePopupMenuAction) = apply { this.reply = reply }
        fun setPin(pin: PLVChatMessagePopupMenuAction) = apply { this.pin = pin }
        fun setBan(ban: PLVChatMessagePopupMenuAction) = apply { this.ban = ban }
        fun setKick(kick: PLVChatMessagePopupMenuAction) = apply { this.kick = kick }

        fun build() = PLVChatMessagePopupMenuActions(
            copy = copy,
            reply = reply,
            pin = pin,
            ban = ban,
            kick = kick
        )
    }
}

class PLVChatMessagePopupMenuAction @JvmOverloads constructor(
    val visible: Boolean = false,
    val onClick: ((PLVChatMessagePopupMenu) -> Unit)? = null,
    val textRes: Int? = null
) {
    companion object {
        @JvmStatic
        fun copy(context: Context, text: CharSequence?) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Label", text)
            clipboard.setPrimaryClip(clipData)
        }
    }
}
