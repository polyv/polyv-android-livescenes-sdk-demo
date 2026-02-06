package com.easefun.polyv.livecloudclass.modules.pagemenu.iframe;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewFragment;

/**
 * 推广外链tab页
 */
public class PLVLCIFrameFragment extends PLVSimpleWebViewFragment {
    private String url;

    public void init(String url, IPLVLiveRoomDataManager liveRoomDataManager) {
        this.url = liveRoomDataManager != null ? liveRoomDataManager.appendIFrameParams(url) : url;
    }

    @Override
    protected boolean isLoadUrl() {
        return true;
    }

    @Override
    protected String urlOrHtmlText() {
        return url;
    }

    @Override
    protected boolean isUseActionView() {
        return false;//isLoadUrl true need use false, or else go to default browser
    }
}
