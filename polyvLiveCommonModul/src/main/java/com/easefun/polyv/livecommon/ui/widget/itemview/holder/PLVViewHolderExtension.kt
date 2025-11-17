package com.easefun.polyv.livecommon.ui.widget.itemview.holder

import androidx.recyclerview.widget.RecyclerView
import android.view.View

/**
 * @author Hoshiiro
 */
fun <T : View> RecyclerView.ViewHolder.findViewById(id: Int): T = itemView.findViewById<T>(id)
