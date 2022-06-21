package com.easefun.polyv.livecloudclass.modules.media.floating;

import androidx.annotation.NonNull;

import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVLCFloatingWindowModule extends PLVDependModule {

    public static final PLVLCFloatingWindowModule instance = new PLVLCFloatingWindowModule();

    {
        provide(new LazyProvider<PLVLCFloatingWindow>() {
            @NonNull
            @Override
            public PLVLCFloatingWindow onProvide() {
                return new PLVLCFloatingWindow();
            }
        });
    }

}
