package com.easefun.polyv.livecommon.module.modules.interact.cardpush;

import android.support.annotation.NonNull;

import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡片推送已观看时间 本地缓存
 */
public class PLVCardLookTimeLocalRepository {

    private static final String TAG = PLVCardLookTimeLocalRepository.class.getSimpleName();

    /**
     * SharedPreference存储
     * Key: 卡片推送的id
     * Value: 已观看时间
     */
    private static final String SP_NAME = "plv_card_look_time_local_cache";

    public static void saveCache(String id, int lookTime) {
        SPUtils.getInstance(SP_NAME).put(id, lookTime);
    }

    public static void removeCache(String id) {
        SPUtils.getInstance(SP_NAME).remove(id);
    }

    @NonNull
    public static List<Integer> listCache() {
        List<Integer> resultList = new ArrayList<>();
        for (Object obj : SPUtils.getInstance(SP_NAME).getAll().values()) {
            if (!(obj instanceof Integer)) {
                continue;
            }
            resultList.add((Integer) obj);
        }
        return resultList;
    }

    public static int getCache(String id) {
        return SPUtils.getInstance(SP_NAME).getInt(id, 0);
    }

}
