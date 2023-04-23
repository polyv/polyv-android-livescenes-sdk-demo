package com.easefun.polyv.livecommon.module.modules.redpack.di;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.redpack.model.PLVRedpackRepo;
import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.PLVRedpackLocalDataSource;
import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.PLVRedpackMemoryDataSource;
import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.PLVRedpackCacheDataBase;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.PLVRedpackViewModel;
import com.plv.foundationsdk.component.di.PLVDependModule;
import com.plv.livescenes.feature.redpack.PLVRedpackApiManager;

/**
 * @author Hoshiiro
 */
public class PLVRedpackModule extends PLVDependModule {

    public static final PLVRedpackModule instance = new PLVRedpackModule();

    {
        provide(new LazyProvider<PLVRedpackCacheDataBase>() {
            @NonNull
            @Override
            public PLVRedpackCacheDataBase onProvide() {
                return PLVRedpackCacheDataBase.getInstance();
            }
        });

        provide(new LazyProvider<PLVRedpackMemoryDataSource>() {
            @NonNull
            @Override
            public PLVRedpackMemoryDataSource onProvide() {
                return new PLVRedpackMemoryDataSource();
            }
        });
        provide(new LazyProvider<PLVRedpackLocalDataSource>() {
            @NonNull
            @Override
            public PLVRedpackLocalDataSource onProvide() {
                return new PLVRedpackLocalDataSource(
                        get(PLVRedpackCacheDataBase.class)
                );
            }
        });
        provide(new LazyProvider<PLVRedpackApiManager>() {
            @NonNull
            @Override
            public PLVRedpackApiManager onProvide() {
                return PLVRedpackApiManager.INSTANCE;
            }
        });

        provide(new LazyProvider<PLVRedpackRepo>() {
            @NonNull
            @Override
            public PLVRedpackRepo onProvide() {
                return new PLVRedpackRepo(
                        get(PLVRedpackMemoryDataSource.class),
                        get(PLVRedpackLocalDataSource.class),
                        get(PLVRedpackApiManager.class)
                );
            }
        });

        provide(new LazyProvider<PLVRedpackViewModel>() {
            @NonNull
            @Override
            public PLVRedpackViewModel onProvide() {
                return new PLVRedpackViewModel(
                        get(PLVRedpackRepo.class)
                );
            }
        });
    }

}
