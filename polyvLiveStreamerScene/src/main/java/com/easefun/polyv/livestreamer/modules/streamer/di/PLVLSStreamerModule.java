package com.easefun.polyv.livestreamer.modules.streamer.di;

import androidx.annotation.NonNull;

import com.easefun.polyv.livestreamer.modules.streamer.position.PLVLSStreamerViewPositionManager;
import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVLSStreamerModule extends PLVDependModule {

    public static final PLVLSStreamerModule instance = new PLVLSStreamerModule();

    {
        provide(new LazyProvider<PLVLSStreamerViewPositionManager>() {
            @NonNull
            @Override
            public PLVLSStreamerViewPositionManager onProvide() {
                return new PLVLSStreamerViewPositionManager();
            }
        });
    }

}
