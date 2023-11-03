package com.easefun.polyv.livecommon.module.modules.multiroom.transmit.di;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.PLVMultiRoomTransmitRepo;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.viewmodel.PLVMultiRoomTransmitViewModel;
import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVMultiRoomTransmitModule extends PLVDependModule {

    public static final PLVMultiRoomTransmitModule instance = new PLVMultiRoomTransmitModule();

    {
        provide(new LazyProvider<PLVMultiRoomTransmitRepo>() {
            @NonNull
            @Override
            public PLVMultiRoomTransmitRepo onProvide() {
                return new PLVMultiRoomTransmitRepo();
            }
        });

        provide(new LazyProvider<PLVMultiRoomTransmitViewModel>() {
            @NonNull
            @Override
            public PLVMultiRoomTransmitViewModel onProvide() {
                return new PLVMultiRoomTransmitViewModel(get(PLVMultiRoomTransmitRepo.class));
            }
        });
    }

}
