package com.easefun.polyv.livecommon.module.modules.player.playback.di;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.PLVPlaybackCacheRepo;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheDatabaseDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheLocalStorageDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheMemoryDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheNetworkDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.PLVPlaybackCacheDatabase;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config.PLVPlaybackCacheConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheListViewModel;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheVideoViewModel;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.config.PLVPlaybackCacheVideoConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.usecase.PLVPlaybackCacheListMergeUseCase;
import com.plv.foundationsdk.component.di.PLVDependModule;
import com.plv.livescenes.download.PLVDownloaderManager;
import com.plv.livescenes.download.api.PLVPlaybackDownloadApiManager;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheModule extends PLVDependModule {

    public static final PLVPlaybackCacheModule instance = new PLVPlaybackCacheModule();

    {
        provide(new LazyProvider<PLVDownloaderManager>() {
            @NonNull
            @Override
            public PLVDownloaderManager onProvide() {
                return PLVDownloaderManager.getInstance();
            }
        });
        provide(new LazyProvider<PLVPlaybackDownloadApiManager>() {
            @NonNull
            @Override
            public PLVPlaybackDownloadApiManager onProvide() {
                return PLVPlaybackDownloadApiManager.getInstance();
            }
        });

        provide(new LazyProvider<PLVPlaybackCacheConfig>() {
            @NonNull
            @Override
            public PLVPlaybackCacheConfig onProvide() {
                return new PLVPlaybackCacheConfig();
            }
        });
        provide(new LazyProvider<PLVPlaybackCacheVideoConfig>() {
            @NonNull
            @Override
            public PLVPlaybackCacheVideoConfig onProvide() {
                return new PLVPlaybackCacheVideoConfig();
            }
        });

        provide(new LazyProvider<PLVPlaybackCacheDatabase>() {
            @NonNull
            @Override
            public PLVPlaybackCacheDatabase onProvide() {
                return PLVPlaybackCacheDatabase.getInstance(
                        get(PLVPlaybackCacheConfig.class)
                );
            }
        });

        provide(new LazyProvider<PLVPlaybackCacheDatabaseDataSource>() {
            @NonNull
            @Override
            public PLVPlaybackCacheDatabaseDataSource onProvide() {
                return PLVPlaybackCacheDatabaseDataSource.getInstance(
                        get(PLVPlaybackCacheDatabase.class)
                );
            }
        });
        provide(new LazyProvider<PLVPlaybackCacheLocalStorageDataSource>() {
            @NonNull
            @Override
            public PLVPlaybackCacheLocalStorageDataSource onProvide() {
                return PLVPlaybackCacheLocalStorageDataSource.getInstance(
                        get(PLVDownloaderManager.class),
                        get(PLVPlaybackCacheConfig.class)
                );
            }
        });
        provide(new LazyProvider<PLVPlaybackCacheMemoryDataSource>() {
            @NonNull
            @Override
            public PLVPlaybackCacheMemoryDataSource onProvide() {
                return new PLVPlaybackCacheMemoryDataSource();
            }
        });
        provide(new LazyProvider<PLVPlaybackCacheNetworkDataSource>() {
            @NonNull
            @Override
            public PLVPlaybackCacheNetworkDataSource onProvide() {
                return new PLVPlaybackCacheNetworkDataSource(
                        get(PLVPlaybackDownloadApiManager.class)
                );
            }
        });

        provide(new LazyProvider<PLVPlaybackCacheRepo>() {
            @NonNull
            @Override
            public PLVPlaybackCacheRepo onProvide() {
                return PLVPlaybackCacheRepo.getInstance(
                        get(PLVPlaybackCacheDatabaseDataSource.class),
                        get(PLVPlaybackCacheLocalStorageDataSource.class),
                        get(PLVPlaybackCacheMemoryDataSource.class),
                        get(PLVPlaybackCacheNetworkDataSource.class)
                );
            }
        });

        provide(new LazyProvider<PLVPlaybackCacheListMergeUseCase>() {
            @NonNull
            @Override
            public PLVPlaybackCacheListMergeUseCase onProvide() {
                return new PLVPlaybackCacheListMergeUseCase();
            }
        });

        provide(new LazyProvider<PLVPlaybackCacheListViewModel>() {
            @NonNull
            @Override
            public PLVPlaybackCacheListViewModel onProvide() {
                return PLVPlaybackCacheListViewModel.getInstance(
                        get(PLVPlaybackCacheRepo.class),
                        get(PLVPlaybackCacheListMergeUseCase.class)
                );
            }
        });
        provide(new LazyProvider<PLVPlaybackCacheVideoViewModel>() {
            @NonNull
            @Override
            public PLVPlaybackCacheVideoViewModel onProvide() {
                return new PLVPlaybackCacheVideoViewModel(
                        get(PLVPlaybackCacheRepo.class),
                        get(PLVPlaybackCacheVideoConfig.class)
                );
            }
        });
    }

}
