package com.easefun.polyv.livecommon.module.modules.di;

import com.easefun.polyv.livecommon.module.modules.beauty.di.PLVBeautyModule;
import com.easefun.polyv.livecommon.module.modules.chapter.di.PLVPlaybackChapterModule;
import com.easefun.polyv.livecommon.module.modules.commodity.di.PLVCommodityModule;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.di.PLVWelfareLotteryModule;
import com.easefun.polyv.livecommon.module.modules.linkmic.di.PLVLinkMicModule;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.di.PLVMultiRoomTransmitModule;
import com.easefun.polyv.livecommon.module.modules.player.playback.di.PLVPlaybackCacheModule;
import com.easefun.polyv.livecommon.module.modules.redpack.di.PLVRedpackModule;
import com.easefun.polyv.livecommon.module.modules.streamer.di.PLVSipLinkMicModule;
import com.easefun.polyv.livecommon.module.modules.venue.di.PLVMultiVenueModule;
import com.plv.foundationsdk.component.di.PLVDependModule;

/**
 * @author Hoshiiro
 */
public class PLVCommonModule extends PLVDependModule {

    public final static PLVCommonModule instance = new PLVCommonModule();

    {
        include(PLVPlaybackCacheModule.instance);
        include(PLVPlaybackChapterModule.instance);
        include(PLVCommodityModule.instance);
        include(PLVMultiRoomTransmitModule.instance);
        include(PLVRedpackModule.instance);
        include(PLVLinkMicModule.instance);
        include(PLVBeautyModule.instance);
        include(PLVSipLinkMicModule.instance);
        include(PLVMultiVenueModule.instance);
        include(PLVWelfareLotteryModule.instance);
    }

}
