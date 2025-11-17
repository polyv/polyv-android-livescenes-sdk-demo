package com.easefun.polyv.livecommon.module.utils.imageloader

import android.widget.ImageView

/**
 * @author Hoshiiro
 */
fun ImageView.loadImage(url: String?) = PLVImageLoader.getInstance().loadImage(url, this)