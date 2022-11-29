package com.easefun.polyv.livecommon.module.modules.linkmic.di;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicLocalShareData;
import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVLinkMicModule extends PLVDependModule {

    public static final PLVLinkMicModule instance = new PLVLinkMicModule();

    {
        provide(new LazyProvider<PLVLinkMicLocalShareData>() {
            @NonNull
            @Override
            public PLVLinkMicLocalShareData onProvide() {
                return new PLVLinkMicLocalShareData();
            }
        });
    }

}
