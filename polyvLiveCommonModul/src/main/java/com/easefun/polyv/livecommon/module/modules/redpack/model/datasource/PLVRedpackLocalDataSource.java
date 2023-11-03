package com.easefun.polyv.livecommon.module.modules.redpack.model.datasource;

import static com.plv.foundationsdk.rx.PLVRxSchedulersUtil.runOn;

import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.PLVRedpackCacheDataBase;
import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.entity.PLVRedpackCacheVO;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;

import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVRedpackLocalDataSource {

    private final PLVRedpackCacheDataBase redpackCacheDataBase;

    public PLVRedpackLocalDataSource(
            PLVRedpackCacheDataBase redpackCacheDataBase
    ) {
        this.redpackCacheDataBase = redpackCacheDataBase;
    }

    public PLVRedPaperReceiveType getReceiveStatus(String redpackId, String viewerId) {
        final PLVRedpackCacheVO cacheVO = redpackCacheDataBase.getRedpackCacheDAO().get(createPrimaryKey(redpackId, viewerId));
        if (cacheVO == null) {
            return PLVRedPaperReceiveType.AVAILABLE;
        }
        return cacheVO.getRedPaperReceiveType();
    }

    public void updateReceiveStatus(PLVRedPaperEvent redPaperEvent, String roomId, String viewerId, PLVRedPaperReceiveType newReceiveType) {
        final PLVRedpackCacheVO redpackCacheVO = new PLVRedpackCacheVO();
        redpackCacheVO.setPrimaryKey(createPrimaryKey(redPaperEvent, viewerId));
        redpackCacheVO.setRedCacheId(redPaperEvent.getRedCacheId());
        redpackCacheVO.setRedpackId(redPaperEvent.getRedpackId());
        redpackCacheVO.setRoomId(roomId);
        redpackCacheVO.setViewerId(viewerId);
        redpackCacheVO.setRedPaperReceiveType(newReceiveType);

        runOn(Schedulers.io(), new Runnable() {
            @Override
            public void run() {
                redpackCacheDataBase.getRedpackCacheDAO().insert(redpackCacheVO);
            }
        });
    }

    private static String createPrimaryKey(PLVRedPaperEvent redPaperEvent, String viewerId) {
        return createPrimaryKey(redPaperEvent.getRedpackId(), viewerId);
    }

    private static String createPrimaryKey(String redpackId, String viewerId) {
        return redpackId + "_" + viewerId;
    }

}
