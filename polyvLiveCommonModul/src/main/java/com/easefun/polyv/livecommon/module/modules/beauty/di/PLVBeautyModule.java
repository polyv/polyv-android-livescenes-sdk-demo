package com.easefun.polyv.livecommon.module.modules.beauty.di;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.beauty.model.PLVBeautyRepo;
import com.easefun.polyv.livecommon.module.modules.beauty.model.datasource.PLVBeautyLocalDataSource;
import com.easefun.polyv.livecommon.module.modules.beauty.model.datasource.PLVBeautySdkDataSource;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase.PLVBeautyOptionListInitUseCase;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase.PLVBeautyResetUseCase;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.usecase.PLVBeautySwitchUseCase;
import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVBeautyModule extends PLVDependModule {

    public static final PLVBeautyModule instance = new PLVBeautyModule();

    {

        provide(new LazyProvider<PLVBeautyLocalDataSource>() {
            @NonNull
            @Override
            public PLVBeautyLocalDataSource onProvide() {
                return new PLVBeautyLocalDataSource();
            }
        });
        provide(new LazyProvider<PLVBeautySdkDataSource>() {
            @NonNull
            @Override
            public PLVBeautySdkDataSource onProvide() {
                return new PLVBeautySdkDataSource();
            }
        });

        provide(new LazyProvider<PLVBeautyRepo>() {
            @NonNull
            @Override
            public PLVBeautyRepo onProvide() {
                return new PLVBeautyRepo(
                        get(PLVBeautyLocalDataSource.class),
                        get(PLVBeautySdkDataSource.class)
                );
            }
        });

        provide(new LazyProvider<PLVBeautyOptionListInitUseCase>() {
            @NonNull
            @Override
            public PLVBeautyOptionListInitUseCase onProvide() {
                return new PLVBeautyOptionListInitUseCase(get(PLVBeautyRepo.class));
            }
        });
        provide(new LazyProvider<PLVBeautySwitchUseCase>() {
            @NonNull
            @Override
            public PLVBeautySwitchUseCase onProvide() {
                return new PLVBeautySwitchUseCase(get(PLVBeautyRepo.class));
            }
        });
        provide(new LazyProvider<PLVBeautyResetUseCase>() {
            @NonNull
            @Override
            public PLVBeautyResetUseCase onProvide() {
                return new PLVBeautyResetUseCase(get(PLVBeautyRepo.class));
            }
        });

        provide(new LazyProvider<PLVBeautyViewModel>() {
            @NonNull
            @Override
            public PLVBeautyViewModel onProvide() {
                return new PLVBeautyViewModel(
                        get(PLVBeautyRepo.class),
                        get(PLVBeautyOptionListInitUseCase.class),
                        get(PLVBeautySwitchUseCase.class),
                        get(PLVBeautyResetUseCase.class));
            }
        });

    }

}
