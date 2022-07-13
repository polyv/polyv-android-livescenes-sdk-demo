package com.easefun.polyv.livecommon.module.modules.commodity.di;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.commodity.model.PLVCommodityRepo;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVCommodityModule extends PLVDependModule {

    public static final PLVCommodityModule instance = new PLVCommodityModule();

    {

        provide(new LazyProvider<PLVCommodityRepo>() {
            @NonNull
            @Override
            public PLVCommodityRepo onProvide() {
                return new PLVCommodityRepo();
            }
        });

        provide(new LazyProvider<PLVCommodityViewModel>() {
            @NonNull
            @Override
            public PLVCommodityViewModel onProvide() {
                return new PLVCommodityViewModel(
                        get(PLVCommodityRepo.class)
                );
            }
        });

    }

}
