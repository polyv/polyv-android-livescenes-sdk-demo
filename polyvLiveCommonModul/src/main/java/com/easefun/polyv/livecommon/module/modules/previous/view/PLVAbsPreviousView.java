package com.easefun.polyv.livecommon.module.modules.previous.view;

import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.plv.livescenes.model.PLVPlaybackListVO;

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
    public void requestPreviousError() {

    }

    public PLVAbsPreviousView() {
        super();
    }

    @Override
    public void previousNoMoreData() {

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
}
