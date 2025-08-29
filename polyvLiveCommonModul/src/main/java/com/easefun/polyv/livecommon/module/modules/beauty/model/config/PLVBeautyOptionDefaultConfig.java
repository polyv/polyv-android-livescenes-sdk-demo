package com.easefun.polyv.livecommon.module.modules.beauty.model.config;

import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import com.easefun.polyv.livecommon.R;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.foundationsdk.utils.PLVAppUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVBeautyOptionDefaultConfig {

    private PLVBeautyOptionDefaultConfig() {
    }

    // 美颜默认强度
    public static final Map<PLVBeautyOption, Float> DEFAULT_BEAUTY_OPTION_VALUE = mapOf(
            // 磨皮
            pair(PLVBeautyOption.BEAUTY_SMOOTH, 0.5F),
            // 美白
            pair(PLVBeautyOption.BEAUTY_WHITEN, 0.25F),
            // 锐化
            pair(PLVBeautyOption.BEAUTY_SHARP, 0.25F),
            // 瘦脸
            pair(PLVBeautyOption.RESHAPE_DEFORM_OVERALL, 0.35F),
            // 眼睛大小
            pair(PLVBeautyOption.RESHAPE_DEFORM_EYE, 0.25F),
            // 鼻子大小
            pair(PLVBeautyOption.RESHAPE_DEFORM_NOSE, 0.3F),
            // 嘴巴大小
            pair(PLVBeautyOption.RESHAPE_DEFORM_ZOOM_MOUTH, 0.2F),
            // 发际线
            pair(PLVBeautyOption.RESHAPE_DEFORM_FOREHEAD, 0.4F),
            // 下颌
            pair(PLVBeautyOption.RESHAPE_DEFORM_ZOOM_JAWBONE, 0.2F),
            // 白牙
            pair(PLVBeautyOption.RESHAPE_BEAUTY_WHITEN_TEETH, 0.35F),
            // 亮眼
            pair(PLVBeautyOption.RESHAPE_BEAUTY_BRIGHTEN_EYE, 0.35F)
    );

    // 滤镜默认强度
    public static final float DEFAULT_FILTER_VALUE = 0.5F;

    private static Map<String, String> getFilterI18nMap(){
        return new LinkedHashMap<String, String>(){
            {
                put("原图", PLVAppUtils.getString(R.string.plv_beauty_filter_original));
                put("氧气", PLVAppUtils.getString(R.string.plv_beauty_filter_oxygen));
                put("初见", PLVAppUtils.getString(R.string.plv_beauty_filter_first_blush));
                put("冷氧", PLVAppUtils.getString(R.string.plv_beauty_filter_cold_oxygen));
                put("温柔", PLVAppUtils.getString(R.string.plv_beauty_filter_gentle));
                put("慕斯", PLVAppUtils.getString(R.string.plv_beauty_filter_mousse));
                put("蜜桃", PLVAppUtils.getString(R.string.plv_beauty_filter_peach));
                put("物语", PLVAppUtils.getString(R.string.plv_beauty_filter_lore));
                put("樱花", PLVAppUtils.getString(R.string.plv_beauty_filter_cherry));
                put("胶片", PLVAppUtils.getString(R.string.plv_beauty_filter_film));
                put("夜色", PLVAppUtils.getString(R.string.plv_beauty_filter_night));

                put("冷白", PLVAppUtils.getString(R.string.plv_beauty_filter_cool_white));
                put("牛奶", PLVAppUtils.getString(R.string.plv_beauty_filter_milk));
                put("蓝调", PLVAppUtils.getString(R.string.plv_beauty_filter_blue_tone));
                put("元气", PLVAppUtils.getString(R.string.plv_beauty_filter_vitality));
                put("清新", PLVAppUtils.getString(R.string.plv_beauty_filter_fresh));
                put("质感", PLVAppUtils.getString(R.string.plv_beauty_filter_texture));
                put("粉瓷", PLVAppUtils.getString(R.string.plv_beauty_filter_porcelain));
                put("樱红", PLVAppUtils.getString(R.string.plv_beauty_filter_cherry_red));
                put("唯美", PLVAppUtils.getString(R.string.plv_beauty_filter_aesthetic));
            }
        };
    }

    public static List<String> getDefaultFilterKeyOrder() {
        return new ArrayList<>(getFilterI18nMap().values());
    }

    public static String getFilterI18n(String filterName) {
        Map<String, String> map = getFilterI18nMap();
        return map.containsKey(filterName) ? map.get(filterName) : filterName;
    }
}
