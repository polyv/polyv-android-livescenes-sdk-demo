package com.easefun.polyv.livecommon.module.modules.document.model.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 上传文档状态分为以下几种情况：
 * 1.Unprepared 已选择需要上传文件的预备状态，尚未开始上传文件
 * 2.Prepared 已经创建上传任务，即将上传
 * 3.Uploading 文件上传中状态
 * 4.Upload Failed 文件上传失败，提示重新上传
 * 5.Upload Success 文件上传成功，下一步服务器会自动开始转码
 * 6.Converting 文件转码中
 * 7.Convert Fail 文件转码失败，提示重新上传
 * 8.Convert Animate Loss 文件转码出现动效丢失，属于文件转码成功的一种，该状态的PPT文档能正常使用，但是缺少动画
 * 9.Convert Success 文件转码成功，所有PPT文件上传步骤完成
 *
 * @author suhongtao
 */
public class PLVPptUploadStatus {

    public static final int STATUS_UNPREPARED = 0;
    public static final int STATUS_PREPARED = 1;
    public static final int STATUS_UPLOADING = 2;
    public static final int STATUS_UPLOAD_FAILED = 3;
    public static final int STATUS_UPLOAD_SUCCESS = 4;
    public static final int STATUS_CONVERTING = 5;
    public static final int STATUS_CONVERT_FAILED = 6;
    public static final int STATUS_CONVERT_ANIMATE_LOSS = 7;
    public static final int STATUS_CONVERT_SUCCESS = 8;

    public static boolean isStatusUploadSuccess(Integer status) {
        if (status == null) {
            return true;
        }
        return status >= STATUS_UPLOAD_SUCCESS;
    }

    public static boolean isStatusConvertSuccess(Integer status) {
        if (status == null) {
            return true;
        }
        return status >= STATUS_CONVERT_SUCCESS;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_UNPREPARED,
            STATUS_PREPARED,
            STATUS_UPLOADING,
            STATUS_UPLOAD_FAILED,
            STATUS_UPLOAD_SUCCESS,
            STATUS_CONVERTING,
            STATUS_CONVERT_FAILED,
            STATUS_CONVERT_ANIMATE_LOSS,
            STATUS_CONVERT_SUCCESS})
    public @interface Range {
    }

}
