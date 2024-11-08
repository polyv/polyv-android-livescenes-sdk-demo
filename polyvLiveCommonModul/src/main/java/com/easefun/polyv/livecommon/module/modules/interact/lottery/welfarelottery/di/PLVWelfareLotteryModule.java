package com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.di;

import com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.PLVWelfareLotteryManager;
import com.plv.foundationsdk.component.di.PLVDependModule;

public class PLVWelfareLotteryModule extends PLVDependModule {

    public static final PLVWelfareLotteryModule instance = new PLVWelfareLotteryModule();

    {
        provide(new LazyProvider<PLVWelfareLotteryManager>() {
            @Override
            public PLVWelfareLotteryManager onProvide() {
                return new PLVWelfareLotteryManager();
            }
        });
    }
}
