package com.easefun.polyv.livecommon.module.modules.previous.view;

import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.plv.livescenes.model.PLVPlaybackListVO;
import com.plv.livescenes.previous.model.PLVChapterDataVO;

import java.util.List;

/**
 * 往期回放MVP模式View层空实现
 */
public abstract class PLVAbsPreviousView implements IPLVPreviousPlaybackContract.IPreviousPlaybackView {
    @Override
    public void setPresenter(IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter presenter) {

    }

    @Override
    public void updatePreviousVideoList(PLVPlaybackListVO playbackListInfo) {

    }

    @Override
    public void updateChapterList(List<PLVChapterDataVO> dataList) {

    }

    @Override
    public void requestPreviousError() {

    }

    @Override
    public void requestChapterError() {

    }

    public PLVAbsPreviousView() {
        super();
    }

    @Override
    public void previousNoMoreData() {

    }

    @Override
    public void chapterNoMoreData() {

    }

    @Override
    public void previousLoadModreData(PLVPlaybackListVO listVO) {

    }

    @Override
    public void previousLoadMoreError() {

    }

    @Override
    public void onPlayComplete() {

    }

    @Override
    public void onSeekChange(int position) {

    }
}
