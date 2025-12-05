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
        BEAUTY_SMOOTH(PLVBeautyOption.BEAUTY_SMOOTH, "", R.drawable.plv_beauty_smooth_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_smooth);
            }
        },

        /**
         * 美颜 - 美白
         */
        BEAUTY_WHITEN(PLVBeautyOption.BEAUTY_WHITEN, "", R.drawable.plv_beauty_whiten_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_whiten);
            }
        },

        /**
         * 美颜 - 锐化
         */
        BEAUTY_SHARP(PLVBeautyOption.BEAUTY_SHARP, "", R.drawable.plv_beauty_sharp_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_sharp);
            }
        },

        ;

        public final PLVBeautyOption beautyOption;
        private final String name;
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

        public String getName() {
            return name;
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
        RESHAPE_DEFORM_EYE(PLVBeautyOption.RESHAPE_DEFORM_EYE, "", R.drawable.plv_beauty_deform_eye_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_deform_eye);
            }
        },

        /**
         * 微整形 - 瘦脸
         */
        RESHAPE_DEFORM_OVERALL(PLVBeautyOption.RESHAPE_DEFORM_OVERALL, "", R.drawable.plv_beauty_deform_overall_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_deform_overall);
            }
        },

        /**
         * 微整形 - 下颌调整
         */
        RESHAPE_DEFORM_ZOOM_JAWBONE(PLVBeautyOption.RESHAPE_DEFORM_ZOOM_JAWBONE, "", R.drawable.plv_beauty_deform_zoom_jawbone_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_deform_zoom_jawbone);
            }
        },

        /**
         * 微整形 - 发际线调整
         */
        RESHAPE_DEFORM_FOREHEAD(PLVBeautyOption.RESHAPE_DEFORM_FOREHEAD, "", R.drawable.plv_beauty_deform_forehead_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_deform_forehead);
            }
        },

        /**
         * 微整形 - 亮眼
         */
        RESHAPE_BEAUTY_BRIGHTEN_EYE(PLVBeautyOption.RESHAPE_BEAUTY_BRIGHTEN_EYE, "", R.drawable.plv_beauty_brighten_eye_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_brighten_eye);
            }
        },

        /**
         * 微整形 - 瘦鼻调整
         */
        RESHAPE_DEFORM_NOSE(PLVBeautyOption.RESHAPE_DEFORM_NOSE, "", R.drawable.plv_beauty_deform_nose_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_deform_nose);
            }
        },

        /**
         * 微整形 - 嘴巴大小调整
         */
        RESHAPE_DEFORM_ZOOM_MOUTH(PLVBeautyOption.RESHAPE_DEFORM_ZOOM_MOUTH, "", R.drawable.plv_beauty_deform_zoom_mouth_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_deform_zoom_mouth);
            }
        },

        /**
         * 微整形 - 红唇
         */
        BEAUTY_LIPSTICK(PLVBeautyOption.RESHAPE_BEAUTY_LIPSTICK, "", R.drawable.plv_beauty_lipstick_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_lipstick);
            }
        },

        /**
         * 微整形 - 白牙
         */
        RESHAPE_BEAUTY_WHITEN_TEETH(PLVBeautyOption.RESHAPE_BEAUTY_WHITEN_TEETH, "", R.drawable.plv_beauty_whiten_teeth_icon) {
            @Override
            public String getName() {
                return getString(R.string.plv_beauty_whiten_teeth);
            }
        },

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

        public String getName() {
            return name;
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
