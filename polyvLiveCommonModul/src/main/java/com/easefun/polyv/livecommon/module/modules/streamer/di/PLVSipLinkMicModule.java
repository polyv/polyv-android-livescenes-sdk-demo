package com.easefun.polyv.livecommon.module.modules.streamer.di;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVSipLinkMicRepo;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVSipLinkMicViewModel;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.usecase.PLVSipLinkMicMergeViewerUseCase;
import com.plv.foundationsdk.component.di.PLVDependModule;
import com.plv.livescenes.linkmic.sip.datasource.PLVSipRemoteDataSource;
import com.plv.livescenes.linkmic.sip.datasource.PLVSipSocketDataSource;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicModule extends PLVDependModule {

    public static final PLVSipLinkMicModule instance = new PLVSipLinkMicModule();

    {

        provide(new LazyProvider<PLVSipRemoteDataSource>() {
            @NonNull
            @Override
            public PLVSipRemoteDataSource onProvide() {
                return new PLVSipRemoteDataSource();
            }
        });
        provide(new LazyProvider<PLVSipSocketDataSource>() {
            @NonNull
            @Override
            public PLVSipSocketDataSource onProvide() {
                return new PLVSipSocketDataSource();
            }
        });

        provide(new LazyProvider<PLVSipLinkMicRepo>() {
            @NonNull
            @Override
            public PLVSipLinkMicRepo onProvide() {
                return new PLVSipLinkMicRepo(
                        get(PLVSipRemoteDataSource.class),
                        get(PLVSipSocketDataSource.class)
                );
            }
        });

        provide(new LazyProvider<PLVSipLinkMicMergeViewerUseCase>() {
            @NonNull
            @Override
            public PLVSipLinkMicMergeViewerUseCase onProvide() {
                return new PLVSipLinkMicMergeViewerUseCase();
            }
        });

        provide(new LazyProvider<PLVSipLinkMicViewModel>() {
            @NonNull
            @Override
            public PLVSipLinkMicViewModel onProvide() {
                return new PLVSipLinkMicViewModel(
                        get(PLVSipLinkMicRepo.class),
                        get(PLVSipLinkMicMergeViewerUseCase.class)
                );
            }
        });

    }

}
