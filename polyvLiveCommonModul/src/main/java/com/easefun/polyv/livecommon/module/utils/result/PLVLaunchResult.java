package com.easefun.polyv.livecommon.module.utils.result;

/**
 * 启动结果
 */
public class PLVLaunchResult {
    /**
     * 是否启动成功
     */
    private boolean isSuccess;
    /**
     * 启动失败时的提示信息
     */
    private String errorMessage;
    /**
     * 启动失败时的异常
     */
    private Throwable error;

    private PLVLaunchResult(boolean isSuccess) {
        this(isSuccess, "");
    }

    private PLVLaunchResult(boolean isSuccess, String errorMessage) {
        this(isSuccess, errorMessage, new Throwable(errorMessage));
    }

    private PLVLaunchResult(boolean isSuccess, Throwable error) {
        this(isSuccess, error == null ? "" : error.getMessage(), error);
    }

    private PLVLaunchResult(boolean isSuccess, String errorMessage, Throwable error) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.error = error;
    }

    public static PLVLaunchResult success() {
        return new PLVLaunchResult(true);
    }

    public static PLVLaunchResult error(String errorMessage) {
        return new PLVLaunchResult(false, errorMessage);
    }

    public static PLVLaunchResult error(Throwable error) {
        return new PLVLaunchResult(false, error);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getError() {
        return error;
    }
}
