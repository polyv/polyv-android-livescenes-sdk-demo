package com.easefun.polyv.livecommon.ui.widget;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class PLVCountdownToast {

    public static Disposable showShort(final int resId, final int sec, final Action doFinally) {
        return Observable.intervalRange(0, sec + 1, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (doFinally != null) {
                            doFinally.run();
                        }
                        PLVAppUtils.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.cancel();
                            }
                        });
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        String message = PLVAppUtils.getString(resId);
                        ToastUtils.showShort(message + "(" + (sec - aLong) + "s)");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }
}
