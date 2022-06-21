package com.easefun.polyv.livecommon.module.modules.beauty.helper;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.Manifest;
import android.app.Activity;
import androidx.lifecycle.GenericLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;

import com.plv.beauty.api.IPLVBeautyManager;
import com.plv.beauty.api.PLVBeautyManager;
import com.plv.beauty.api.enums.PLVBeautyErrorCode;
import com.plv.beauty.api.resource.RemoteResource;
import com.plv.beauty.api.vo.PLVBeautyInitParam;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.feature.beauty.PLVBeautyApiManager;
import com.plv.livescenes.feature.beauty.vo.PLVBeautySettingVO;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVBeautyInitHelper {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private static volatile PLVBeautyInitHelper instance;

    private PLVBeautyInitHelper() {
    }

    public static PLVBeautyInitHelper getInstance() {
        if (instance == null) {
            synchronized (PLVBeautyInitHelper.class) {
                if (instance == null) {
                    instance = new PLVBeautyInitHelper();
                }
            }
        }
        return instance;
    }

    // </editor-fold>

    private static final String TAG = PLVBeautyInitHelper.class.getSimpleName();

    private IPLVBeautyManager.InitCallback beautyInitCallback;
    private final LifecycleObserver clearCallbackOnDestroy = new GenericLifecycleObserver() {
        @Override
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                beautyInitCallback = null;
                if (getBeautySettingDisposable != null) {
                    getBeautySettingDisposable.dispose();
                    getBeautySettingDisposable = null;
                }
                source.getLifecycle().removeObserver(this);
            }
        }
    };

    private Disposable getBeautySettingDisposable;

    public void init(final Context context, final PLVSugarUtil.Consumer<Boolean> onInitFinishCallback) {
        if (!PLVBeautyManager.getInstance().isBeautySupport()) {
            if (onInitFinishCallback != null) {
                onInitFinishCallback.accept(false);
            }
            return;
        }

        requirePermissionThenRun(context, new Runnable() {
            @Override
            public void run() {
                startBeautyInit(context, onInitFinishCallback);
            }
        });
    }

    public void destroy() {
        if (getBeautySettingDisposable != null) {
            getBeautySettingDisposable.dispose();
            getBeautySettingDisposable = null;
        }
        PLVBeautyManager.getInstance().destroy();
    }

    private void requirePermissionThenRun(final Context context, final Runnable onGrantedPermission) {
        if (PLVFastPermission.hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            onGrantedPermission.run();
            return;
        }

        PLVFastPermission.getInstance().start(
                (Activity) context,
                listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        onGrantedPermission.run();
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {

                    }
                });
    }

    private void startBeautyInit(final Context context, final PLVSugarUtil.Consumer<Boolean> onInitFinishCallback) {
        if (context instanceof LifecycleOwner) {
            ((LifecycleOwner) context).getLifecycle().addObserver(clearCallbackOnDestroy);
        }

        if (getBeautySettingDisposable != null) {
            getBeautySettingDisposable.dispose();
        }
        getBeautySettingDisposable = PLVBeautyApiManager.getInstance().getBeautySetting()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVBeautySettingVO>() {
                    @Override
                    public void accept(PLVBeautySettingVO beautySettingVO) throws Exception {
                        setupInitParam(beautySettingVO);
                        startInnerInit(context, onInitFinishCallback);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        if (onInitFinishCallback != null) {
                            onInitFinishCallback.accept(false);
                        }
                    }
                });
    }

    private void setupInitParam(final PLVBeautySettingVO beautySettingVO) {
        RemoteResource resource = new RemoteResource();
        resource.setName("BeautyEffectResource");
        resource.setUrl(beautySettingVO.getData().getMaterialUrl());
        resource.setHash(beautySettingVO.getData().getMaterialMd5());

        PLVBeautyManager.getInstance().setInitParam(
                new PLVBeautyInitParam()
                        .setRemoteResourceList(listOf(resource))
                        .setOnlineLicense(true)
                        .setLicenseKey(beautySettingVO.getData().getKey())
                        .setLicenseSecret(beautySettingVO.getData().getSecret())
        );
    }

    private void startInnerInit(final Context context, final PLVSugarUtil.Consumer<Boolean> callback) {
        beautyInitCallback = new IPLVBeautyManager.InitCallback() {
            @Override
            public void onStartInit() {

            }

            @Override
            public void onFinishInit(Integer code) {
                PLVCommonLog.i(TAG, "onBeautyFinishInit, code: " + code);
                final boolean success = code != null && code == PLVBeautyErrorCode.SUCCESS;
                if (callback != null) {
                    callback.accept(success);
                }
                beautyInitCallback = null;
            }
        };
        PLVBeautyManager.getInstance().addInitCallback(new WeakReference<>(beautyInitCallback));

        PLVCommonLog.d(TAG, "startBeautyInitInner");
        PLVBeautyManager.getInstance().init(context);
    }

}
