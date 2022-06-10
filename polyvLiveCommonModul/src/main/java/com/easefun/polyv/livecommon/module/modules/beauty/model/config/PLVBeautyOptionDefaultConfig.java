package com.easefun.polyv.livecommon.module.modules.beauty.model.config;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import com.plv.beauty.api.options.PLVBeautyOption;

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
            pair(PLVBeautyOption.BEAUTY_SMOOTH, 0.85F),
            // 美白
            pair(PLVBeautyOption.BEAUTY_WHITEN, 0.7F),
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
            pair(PLVBeautyOption.RESHAPE_BEAUTY_BRIGHTEN_EYE, 0.65F)
    );

    // 滤镜默认强度
    public static final float DEFAULT_FILTER_VALUE = 0.5F;

    public static final List<String> DEFAULT_FILTER_KEY_ORDER = listOf(
            "原图",
            "氧气",
            "初见",
            "冷氧",
            "温柔",
            "慕斯",
            "蜜桃",
            "物语",
            "樱花",
            "胶片",
            "夜色"
    );

}
