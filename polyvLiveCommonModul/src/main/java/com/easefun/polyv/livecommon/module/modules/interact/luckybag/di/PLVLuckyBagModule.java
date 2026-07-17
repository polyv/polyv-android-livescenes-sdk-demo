package com.easefun.polyv.livecommon.module.modules.interact.luckybag.di;

import com.easefun.polyv.livecommon.module.modules.interact.luckybag.PLVLuckyBagManager;
import com.plv.foundationsdk.component.di.PLVDependModule;

public class PLVLuckyBagModule extends PLVDependModule {
    public static final PLVLuckyBagModule instance = new PLVLuckyBagModule();

    {
        provide(new LazyProvider<PLVLuckyBagManager>() {
            @Override
            public PLVLuckyBagManager onProvide() {
                return new PLVLuckyBagManager();
            }
        });
    }
}
