package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.usecase;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheListMergeUseCase {

    public boolean reduceDownloadingList(final List<PLVPlaybackCacheVideoVO> voList, final PLVPlaybackCacheVideoVO vo) {
        return reduce(voList, vo,
                listOf(
                        PLVPlaybackCacheDownloadStatusEnum.WAITING,
                        PLVPlaybackCacheDownloadStatusEnum.PAUSING,
                        PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING,
                        PLVPlaybackCacheDownloadStatusEnum.DOWNLOAD_FAIL
                )
        );
    }

    public boolean reduceDownloadedList(final List<PLVPlaybackCacheVideoVO> voList, final PLVPlaybackCacheVideoVO vo) {
        return reduce(voList, vo,
                listOf(
                        PLVPlaybackCacheDownloadStatusEnum.DOWNLOADED
                )
        );
    }

    private boolean reduce(
            final List<PLVPlaybackCacheVideoVO> voList,
            final PLVPlaybackCacheVideoVO vo,
            final List<PLVPlaybackCacheDownloadStatusEnum> fitStatusList
    ) {
        if (voList == null || vo == null) {
            return false;
        }

        final PLVPlaybackCacheVideoVO voInList = findPlaybackCacheInList(voList, vo.getVideoPoolId());
        if (voInList != null) {
            vo.mergeFrom(voInList);
        }

        final boolean fitStatus = fitStatusList.contains(vo.getDownloadStatusEnum());

        if (fitStatus) {
            if (voInList != null) {
                voList.set(voList.indexOf(voInList), vo);
            } else {
                voList.add(vo);
            }
            return true;
        } else {
            if (voInList != null) {
                voList.remove(voInList);
                return true;
            }
        }

        return false;
    }

    @Nullable
    private static PLVPlaybackCacheVideoVO findPlaybackCacheInList(
            final List<PLVPlaybackCacheVideoVO> voList,
            final String id
    ) {
        if (id == null) {
            return null;
        }
        for (PLVPlaybackCacheVideoVO vo : voList) {
            if (id.equals(vo.getVideoPoolId())) {
                return vo;
            }
        }
        return null;
    }

}
