package com.easefun.polyv.livecommon.module.modules.chapter.di;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.chapter.model.PLVPlaybackChapterRepo;
import com.easefun.polyv.livecommon.module.modules.chapter.viewmodel.PLVPlaybackChapterViewModel;
import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackChapterModule extends PLVDependModule {

    public static final PLVPlaybackChapterModule instance = new PLVPlaybackChapterModule();

    {
        provide(new LazyProvider<PLVPlaybackChapterRepo>() {
            @NonNull
            @Override
            public PLVPlaybackChapterRepo onProvide() {
                return new PLVPlaybackChapterRepo();
            }
        });

        provide(new LazyProvider<PLVPlaybackChapterViewModel>() {
            @NonNull
            @Override
            public PLVPlaybackChapterViewModel onProvide() {
                return new PLVPlaybackChapterViewModel(get(PLVPlaybackChapterRepo.class));
            }
        });
    }

}
