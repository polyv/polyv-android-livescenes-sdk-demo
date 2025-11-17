package com.easefun.polyv.livecommon.ui.widget.itemview.holder

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * @author Hoshiiro
 */
fun <T : View> RecyclerView.ViewHolder.findViewById(id: Int): T = itemView.findViewById<T>(id)
