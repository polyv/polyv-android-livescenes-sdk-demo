package com.easefun.polyv.livecommon.ui.widget

import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import net.polyv.android.common.libs.lang.ui.children
import net.polyv.android.common.libs.lang.ui.updateLayoutParams

/**
 * @author Hoshiiro
 */
fun FlexboxLayout.placeChildrenEvenly(columns: Int) {
    post {
        val childMaxWidth = width / columns
        children().forEach { child ->
            child.post {
                val childWidth = child.width
                child.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    val horizonMargin = ((childMaxWidth - childWidth) / 2).coerceAtLeast(0)
                    leftMargin = horizonMargin
                    rightMargin = horizonMargin
                }
            }
        }
    }
}