package com.easefun.polyv.livecommon.module.utils;

import com.plv.foundationsdk.log.PLVCommonLog;

import java.lang.reflect.Field;

public class PLVReflectionUtils {

    //置空该对象的非final、非基本数据类型的成员
    public static void cleanFields(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                field.set(object, null);
            } catch (IllegalAccessException e) {
                PLVCommonLog.d("PLVReflectionUtils", e.getMessage());
            }
        }
    }
}
