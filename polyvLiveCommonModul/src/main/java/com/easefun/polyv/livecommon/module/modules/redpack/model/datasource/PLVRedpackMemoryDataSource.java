package com.easefun.polyv.livecommon.module.modules.redpack.model.datasource;

import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;

import com.plv.socket.event.redpack.PLVRedPaperEvent;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * @author Hoshiiro
 */
public class PLVRedpackMemoryDataSource {

    private final Map<String, PLVRedPaperEvent> redpackMap = mapOf();

    public void cacheRedPaper(PLVRedPaperEvent redPaperEvent) {
        redpackMap.put(redPaperEvent.getRedpackId(), redPaperEvent);
    }

    @Nullable
    public PLVRedPaperEvent getCachedRedPaper(String redpackId) {
        return redpackMap.get(redpackId);
    }

}
