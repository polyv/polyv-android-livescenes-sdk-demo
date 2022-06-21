package com.easefun.polyv.livecommon.module.modules.beauty.model.datasource;


import com.plv.beauty.api.IPLVBeautyManager;
import com.plv.beauty.api.PLVBeautyManager;
import com.plv.beauty.api.enums.PLVBeautyErrorCode;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.beauty.api.options.PLVFilterOption;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author Hoshiiro
 */
public class PLVBeautySdkDataSource {

    public final Observable<Boolean> beautyInitFinishObservable = Observable.create(new ObservableOnSubscribe<Boolean>() {
        @Override
        public void subscribe(@NotNull ObservableEmitter<Boolean> emitter) throws Exception {
            beautyInitFinishEmitter = emitter;
            emitter.onNext(initFinish);
        }
    });

    private ObservableEmitter<Boolean> beautyInitFinishEmitter;

    private IPLVBeautyManager.InitCallback beautyInitCallback;
    private boolean initFinish = false;

    public PLVBeautySdkDataSource() {
        setBeautyInitCallback();
    }

    private void setBeautyInitCallback() {
        beautyInitCallback = new IPLVBeautyManager.InitCallback() {
            @Override
            public void onStartInit() {

            }

            @Override
            public void onFinishInit(Integer code) {
                initFinish = code != null && code == PLVBeautyErrorCode.SUCCESS;
                if (beautyInitFinishEmitter != null) {
                    beautyInitFinishEmitter.onNext(initFinish);
                }
            }
        };
        PLVBeautyManager.getInstance().addInitCallback(new WeakReference<>(beautyInitCallback));
    }

    public List<PLVFilterOption> getSupportFilterOptions() {
        return new ArrayList<>(PLVBeautyManager.getInstance().getSupportFilterOption());
    }

    public void updateBeautyOption(PLVBeautyOption beautyOption) {
        PLVBeautyManager.getInstance().updateBeautyOption(beautyOption);
    }

    public void updateFilterOption(PLVFilterOption filterOption) {
        PLVBeautyManager.getInstance().setFilterOption(filterOption);
    }

    public void clearBeautyOption() {
        PLVBeautyManager.getInstance().clearBeautyOption();
    }

}
