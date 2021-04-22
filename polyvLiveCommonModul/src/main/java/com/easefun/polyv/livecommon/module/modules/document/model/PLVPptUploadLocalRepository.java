package com.easefun.polyv.livecommon.module.modules.document.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PPT文档上传任务 本地缓存
 *
 * @author suhongtao
 */
public class PLVPptUploadLocalRepository {

    private static final String TAG = PLVPptUploadLocalRepository.class.getSimpleName();

    /**
     * SharedPreference存储
     * Key: PPT文档的fileId
     * Value: jsonString {@link PLVPptUploadLocalCacheVO}
     */
    private static final String SP_NAME = "polyv_ppt_upload_local_cache";

    public void saveCache(PLVPptUploadLocalCacheVO vo) {
        if (vo == null || TextUtils.isEmpty(vo.getFileId())) {
            Log.w(TAG, "file id is empty.");
            return;
        }

        SPUtils.getInstance(SP_NAME).put(vo.getFileId(), PLVPptUploadLocalCacheVO.Serializer.toJson(vo));
    }

    public void removeCache(String fileId) {
        if (TextUtils.isEmpty(fileId)) {
            Log.w(TAG, "file id is empty.");
            return;
        }

        SPUtils.getInstance(SP_NAME).remove(fileId);
    }

    @NonNull
    public List<PLVPptUploadLocalCacheVO> listCache() {
        List<PLVPptUploadLocalCacheVO> resultList = new ArrayList<>();
        for (Object obj : SPUtils.getInstance(SP_NAME).getAll().values()) {
            if (!(obj instanceof String)) {
                continue;
            }
            PLVPptUploadLocalCacheVO vo = PLVPptUploadLocalCacheVO.Serializer.fromJson((String) obj);
            if (vo == null) {
                continue;
            }
            resultList.add(vo);
        }
        return resultList;
    }

    @Nullable
    public PLVPptUploadLocalCacheVO getCache(String fileId) {
        String cacheJson = SPUtils.getInstance(SP_NAME).getString(fileId);
        if (TextUtils.isEmpty(cacheJson)) {
            return null;
        }
        return PLVPptUploadLocalCacheVO.Serializer.fromJson(cacheJson);
    }

}
