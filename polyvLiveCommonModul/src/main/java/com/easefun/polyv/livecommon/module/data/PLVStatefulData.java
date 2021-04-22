package com.easefun.polyv.livecommon.module.data;

/**
 * date: 2019/5/16 0016
 *
 * @author hwj
 * description 有状态的数据
 */
public class PLVStatefulData<T> {
    private static final int SUCCESS = 0;
    private static final int ERROR = 1;
    private static final int LOADING = 2;

    private int status;
    private T data;
    private String errorMsg;
    private Throwable throwable;

    private PLVStatefulData(int status, T data, String errorMsg, Throwable throwable) {
        this.status = status;
        this.data = data;
        this.errorMsg = errorMsg;
        this.throwable = throwable;
    }

    public static <T> PLVStatefulData<T> success(T data) {
        return new PLVStatefulData<>(PLVStatefulData.SUCCESS, data, null, null);
    }

    public static <T> PLVStatefulData<T> error(String errorMsg) {
        return new PLVStatefulData<>(PLVStatefulData.ERROR, null, errorMsg, new Throwable(errorMsg));
    }

    public static <T> PLVStatefulData<T> error(String errorMsg, Throwable throwable) {
        return new PLVStatefulData<>(PLVStatefulData.ERROR, null, errorMsg, throwable);
    }

    public static <T> PLVStatefulData<T> loading() {
        return new PLVStatefulData<>(PLVStatefulData.LOADING, null, null, null);
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return status == SUCCESS;
    }

    public boolean isError() {
        return status == ERROR;
    }

    public boolean isLoading() {
        return status == LOADING;
    }

    public PLVStatefulData<T> ifSuccess(SuccessHandler<T> successHandler) {
        if (status == SUCCESS) {
            successHandler.success(data);
        }
        return this;
    }

    public PLVStatefulData<T> ifError(ErrorHandler errorHandler) {
        if (status == ERROR) {
            errorHandler.error(errorMsg, throwable);
        }
        return this;
    }

    public void ifLoading(LoadingHandler loadingHandler) {
        if (status == LOADING) {
            loadingHandler.loading();
        }
    }

    ////////////////////////////
    // Handler，使代码易读
    ////////////////////////////
    public interface SuccessHandler<T> {
        void success(T data);
    }

    public interface ErrorHandler {
        void error(String errorMsg, Throwable throwable);
    }

    public interface LoadingHandler {
        void loading();
    }
}

