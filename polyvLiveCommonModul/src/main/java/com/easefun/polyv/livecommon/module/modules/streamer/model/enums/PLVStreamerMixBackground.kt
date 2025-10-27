package com.easefun.polyv.livecommon.module.modules.streamer.model.enums

import androidx.annotation.StringRes
import com.easefun.polyv.livecommon.R

/**
 * @author Hoshiiro
 */
enum class PLVStreamerMixBackground(
    @param:StringRes val displayName: Int,
    val url: String
) {
    BLACK(
        R.string.plv_streamer_mix_background_image_black,
        "https://liveimages.videocc.net/defaultImg/appsdk/backgroud/black.jpg"
    ),
    BLUE(
        R.string.plv_streamer_mix_background_image_blue,
        "https://liveimages.videocc.net/defaultImg/appsdk/backgroud/blue.jpg"
    ),
    PURPLE(
        R.string.plv_streamer_mix_background_image_purple,
        "https://liveimages.videocc.net/defaultImg/appsdk/backgroud/purple.jpg"
    ),
    GREEN(
        R.string.plv_streamer_mix_background_image_green,
        "https://liveimages.videocc.net/defaultImg/appsdk/backgroud/green.jpg"
    ),
    ORANGE(
        R.string.plv_streamer_mix_background_image_orange,
        "https://liveimages.videocc.net/defaultImg/appsdk/backgroud/orange.jpg"
    ),
    BLACK_NORMAL(
        R.string.plv_streamer_mix_background_image_black_normal,
        "https://liveimages.videocc.net/defaultImg/appsdk/backgroud/red.jpg"
    ),
    ;

    companion object {
        @JvmField
        val DEFAULT = BLACK_NORMAL
    }
}
