package com.easefun.polyv.livecommon.module.modules.beauty.model.config;

import static com.plv.foundationsdk.utils.PLVAppUtils.getString;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.R;
import com.plv.beauty.api.options.PLVBeautyOption;

/**
 * @author Hoshiiro
 */
public class PLVBeautyEnums {

    public enum BeautyOption {

        /**
         * 美颜 - 磨皮
         */
        BEAUTY_SMOOTH(PLVBeautyOption.BEAUTY_SMOOTH, getString(R.string.plv_beauty_smooth), R.drawable.plv_beauty_smooth_icon),

        /**
         * 美颜 - 美白
         */
        BEAUTY_WHITEN(PLVBeautyOption.BEAUTY_WHITEN, getString(R.string.plv_beauty_whiten), R.drawable.plv_beauty_whiten_icon),

        /**
         * 美颜 - 锐化
         */
        BEAUTY_SHARP(PLVBeautyOption.BEAUTY_SHARP, getString(R.string.plv_beauty_sharp), R.drawable.plv_beauty_sharp_icon),

        ;

        public final PLVBeautyOption beautyOption;
        public final String name;
        @DrawableRes
        public final int iconResId;

        BeautyOption(
                final PLVBeautyOption beautyOption,
                final String name,
                final int iconResId
        ) {
            this.beautyOption = beautyOption;
            this.name = name;
            this.iconResId = iconResId;
        }

        @Nullable
        public static BeautyOption getByBeautyOption(PLVBeautyOption beautyOption) {
            for (BeautyOption option : BeautyOption.values()) {
                if (option.beautyOption == beautyOption) {
                    return option;
                }
            }
            return null;
        }

        public static boolean contains(PLVBeautyOption beautyOption) {
            return getByBeautyOption(beautyOption) != null;
        }

    }

    public enum DetailOption {
        /**
         * 微整形 - 眼睛大小调整
         */
        RESHAPE_DEFORM_EYE(PLVBeautyOption.RESHAPE_DEFORM_EYE, getString(R.string.plv_beauty_deform_eye), R.drawable.plv_beauty_deform_eye_icon),

        /**
         * 微整形 - 瘦脸
         */
        RESHAPE_DEFORM_OVERALL(PLVBeautyOption.RESHAPE_DEFORM_OVERALL, getString(R.string.plv_beauty_deform_overall), R.drawable.plv_beauty_deform_overall_icon),

        /**
         * 微整形 - 下颌调整
         */
        RESHAPE_DEFORM_ZOOM_JAWBONE(PLVBeautyOption.RESHAPE_DEFORM_ZOOM_JAWBONE, getString(R.string.plv_beauty_deform_zoom_jawbone), R.drawable.plv_beauty_deform_zoom_jawbone_icon),

        /**
         * 微整形 - 发际线调整
         */
        RESHAPE_DEFORM_FOREHEAD(PLVBeautyOption.RESHAPE_DEFORM_FOREHEAD, getString(R.string.plv_beauty_deform_forehead), R.drawable.plv_beauty_deform_forehead_icon),

        /**
         * 微整形 - 亮眼
         */
        RESHAPE_BEAUTY_BRIGHTEN_EYE(PLVBeautyOption.RESHAPE_BEAUTY_BRIGHTEN_EYE, getString(R.string.plv_beauty_brighten_eye), R.drawable.plv_beauty_brighten_eye_icon),

        /**
         * 微整形 - 瘦鼻调整
         */
        RESHAPE_DEFORM_NOSE(PLVBeautyOption.RESHAPE_DEFORM_NOSE, getString(R.string.plv_beauty_deform_nose), R.drawable.plv_beauty_deform_nose_icon),

        /**
         * 微整形 - 嘴巴大小调整
         */
        RESHAPE_DEFORM_ZOOM_MOUTH(PLVBeautyOption.RESHAPE_DEFORM_ZOOM_MOUTH, getString(R.string.plv_beauty_deform_zoom_mouth), R.drawable.plv_beauty_deform_zoom_mouth_icon),

        /**
         * 微整形 - 白牙
         */
        RESHAPE_BEAUTY_WHITEN_TEETH(PLVBeautyOption.RESHAPE_BEAUTY_WHITEN_TEETH, getString(R.string.plv_beauty_whiten_teeth), R.drawable.plv_beauty_whiten_teeth_icon),

        ;

        public final PLVBeautyOption beautyOption;
        public final String name;
        @DrawableRes
        public final int iconResId;

        DetailOption(
                final PLVBeautyOption beautyOption,
                final String name,
                final int iconResId
        ) {
            this.beautyOption = beautyOption;
            this.name = name;
            this.iconResId = iconResId;
        }

        @Nullable
        public static DetailOption getByBeautyOption(PLVBeautyOption beautyOption) {
            for (DetailOption option : DetailOption.values()) {
                if (option.beautyOption == beautyOption) {
                    return option;
                }
            }
            return null;
        }

        public static boolean contains(PLVBeautyOption beautyOption) {
            return getByBeautyOption(beautyOption) != null;
        }
    }

}
