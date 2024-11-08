package com.easefun.polyv.livecommon.module.modules.venue.di;

import com.easefun.polyv.livecommon.module.modules.venue.model.PLVMultiVenueRepo;
import com.easefun.polyv.livecommon.module.modules.venue.viewmodel.PLVMultiVenueViewModel;
import com.plv.foundationsdk.component.di.PLVDependModule;
import com.plv.foundationsdk.log.PLVCommonLog;

public class PLVMultiVenueModule extends PLVDependModule {
    public static final PLVMultiVenueModule instance = new PLVMultiVenueModule();
    {
        provide(new LazyProvider<PLVMultiVenueRepo>() {

            @Override
            public PLVMultiVenueRepo onProvide() {
                return PLVMultiVenueRepo.getInstance();
            }
        });

        provide(new LazyProvider<PLVMultiVenueViewModel>() {
            @Override
            public PLVMultiVenueViewModel onProvide() {
                return PLVMultiVenueViewModel.getInstance(get(PLVMultiVenueRepo.class));
            }
        });
    }
}
