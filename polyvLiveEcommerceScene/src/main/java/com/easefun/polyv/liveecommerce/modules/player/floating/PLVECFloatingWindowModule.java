package com.easefun.polyv.liveecommerce.modules.player.floating;

import android.support.annotation.NonNull;

import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVECFloatingWindowModule extends PLVDependModule {

    public static final PLVECFloatingWindowModule instance = new PLVECFloatingWindowModule();

    {
        provide(new LazyProvider<PLVECFloatingWindow>() {
            @NonNull
            @Override
            public PLVECFloatingWindow onProvide() {
                return new PLVECFloatingWindow();
            }
        });
    }

}
