package com.easefun.polyv.livecommon.module.modules.cast.manager;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 直播投屏错误码
 */
public class PLVCastError {

    public static final int CAST_ERROR_DEFAULT = 30000;

    /**
     * 连接失败
     */
    public static final int CAST_ERROR_CONNECT_FAIL = 30001;
    /**
     * 连接拒绝
     */
    public static final int CAST_ERROR_CONNECT_REFUSE = 30002;
    /**
     * 连接超时（超时未允许）
     */
    public static final int CAST_ERROR_CONNECT_TIMEOUT = 30003;
    /**
     * 连接黑名单
     */
    public static final int CAST_ERROR_CONNECT_BACKLIST = 30004;


    /**
     * 退出 播放无响应
     */
    public static final int CAST_ERROR_NO_RESPONSE_STOP = 30010;
    /**
     * 暂停无响应
     */
    public static final int CAST_ERROR_NO_RESPONSE_PAUSE = 30020;
    /**
     * 恢复无响应
     */
    public static final int CAST_ERROR_NO_RESPONSE_RESUME = 30030;
    /**
     * 接收端断开
     */
    public static final int CAST_ERROR_MIRROR_FORCE_STOP = 30040;


    private int mErrorCode;
    private String mErrorDesc;
    private String mExtra;

    public PLVCastError(@PlvCastErrorCode int mErrorCode, String mErrorDesc) {
        this.mErrorCode = mErrorCode;
        this.mErrorDesc = mErrorDesc;
    }

    public PLVCastError(@PlvCastErrorCode int mErrorCode, String mErrorDesc, String mExtra) {
        this.mErrorCode = mErrorCode;
        this.mErrorDesc = mErrorDesc;
        this.mExtra = mExtra;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(int mErrorCode) {
        this.mErrorCode = mErrorCode;
    }

    public String getErrorDesc() {
        return mErrorDesc;
    }

    public void setErrorDesc(String mErrorDesc) {
        this.mErrorDesc = mErrorDesc;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String mExtra) {
        this.mExtra = mExtra;
    }

    @IntDef({
            CAST_ERROR_DEFAULT,
            CAST_ERROR_CONNECT_FAIL,
            CAST_ERROR_CONNECT_REFUSE,
            CAST_ERROR_CONNECT_TIMEOUT,
            CAST_ERROR_CONNECT_BACKLIST,
            CAST_ERROR_NO_RESPONSE_STOP,
            CAST_ERROR_NO_RESPONSE_PAUSE,
            CAST_ERROR_NO_RESPONSE_RESUME,
            CAST_ERROR_MIRROR_FORCE_STOP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlvCastErrorCode {}

}
