package com.easefun.polyv.livecommon.ui.widget.menudrawer

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup

import com.easefun.polyv.livecommon.R

/**
 * @author Hoshiiro
 */
interface IPLVMenuDrawerContainer {
    fun menuDrawerContainer(): ViewGroup
}

@JvmSynthetic
fun Context.showPopupMenu(
    view: View,
    position: Position,
    size: Int,
    onDrawerStateChange: PLVMenuDrawer.OnDrawerStateChangeListener? = null,
    onClosed: (() -> Unit)? = null
): PLVMenuDrawer? {
    val activity = (this as? Activity) ?: return null
    val container = (this as? IPLVMenuDrawerContainer)?.menuDrawerContainer() ?: return null
    val menuDrawer = PLVMenuDrawer.attach(
        activity,
        PLVMenuDrawer.Type.OVERLAY,
        position,
        PLVMenuDrawer.MENU_DRAG_CONTAINER,
        container
    ).apply {
        menuView = view
        menuSize = size
        touchMode = PLVMenuDrawer.TOUCH_MODE_BEZEL
        drawOverlay = false
        setDropShadowEnabled(false)
        setOnDrawerStateChangeListener(object : PLVMenuDrawer.OnDrawerStateChangeListener {
            override fun onDrawerStateChange(oldState: Int, newState: Int) {
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    this@apply.detachToContainer()
                    view.setTag(R.id.plv_menu_drawer_instance_tag, null)
                }
                onDrawerStateChange?.onDrawerStateChange(oldState, newState)
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    onClosed?.invoke()
                }
            }

            override fun onDrawerSlide(openRatio: Float, offsetPixels: Int) {
                onDrawerStateChange?.onDrawerSlide(openRatio, offsetPixels)
            }
        })
    }

    menuDrawer.openMenu()
    view.setTag(R.id.plv_menu_drawer_instance_tag, menuDrawer)
    return menuDrawer
}

@JvmSynthetic
fun Context.hidePopupMenu(view: View) {
    val menuDrawer = view.getTag(R.id.plv_menu_drawer_instance_tag) as? PLVMenuDrawer
    menuDrawer?.closeMenu()
}