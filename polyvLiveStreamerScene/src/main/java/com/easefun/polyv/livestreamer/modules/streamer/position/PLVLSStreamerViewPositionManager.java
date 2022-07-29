package com.easefun.polyv.livestreamer.modules.streamer.position;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livestreamer.modules.streamer.position.vo.PLVLSStreamerViewPositionUiState;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVLSStreamerViewPositionManager {

    private static final String TAG = PLVLSStreamerViewPositionManager.class.getSimpleName();

    private final PLVViewSwitcher documentStreamerViewSwitcher = new PLVViewSwitcher();
    private final Map<String, WeakReference<PLVSwitchViewAnchorLayout>> linkMicIdToStreamerViewMap = new HashMap<>(16);
    private WeakReference<PLVSwitchViewAnchorLayout> documentAnchorLayoutWeak;
    private String firstIndexLinkMicId;

    private final MutableLiveData<PLVLSStreamerViewPositionUiState> documentInMainScreenLiveData = new MutableLiveData<>();
    private final PLVLSStreamerViewPositionUiState uiState = new PLVLSStreamerViewPositionUiState();

    public void updateStreamerView(String linkMicId, PLVSwitchViewAnchorLayout streamerView) {
        linkMicIdToStreamerViewMap.put(linkMicId, new WeakReference<>(streamerView));
    }

    public void updateDocumentAnchorLayout(PLVSwitchViewAnchorLayout anchorLayout) {
        documentAnchorLayoutWeak = new WeakReference<>(anchorLayout);
    }

    public void updateFirstIndexLinkMicId(String linkMicId) {
        this.firstIndexLinkMicId = linkMicId;
    }

    public void switchMainScreen(final boolean documentToMainScreen, final boolean needSyncUpdateToRemote) {
        final PLVSwitchViewAnchorLayout streamerView = getFirstScreenStreamerView();
        if (streamerView == null) {
            PLVCommonLog.w(TAG, "first screen streamer view not found");
            return;
        }
        final PLVSwitchViewAnchorLayout documentView = nullable(new PLVSugarUtil.Supplier<PLVSwitchViewAnchorLayout>() {
            @Override
            public PLVSwitchViewAnchorLayout get() {
                return documentAnchorLayoutWeak.get();
            }
        });
        if (documentView == null) {
            PLVCommonLog.w(TAG, "document view not found");
            return;
        }
        if (documentToMainScreen == isDocumentInMainScreen()) {
            return;
        }
        documentStreamerViewSwitcher.registerSwitchView(streamerView, documentView);
        documentStreamerViewSwitcher.switchView();

        uiState.setDocumentInMainScreen(isDocumentInMainScreen())
                .setNeedSyncUpdateToRemote(needSyncUpdateToRemote);
        documentInMainScreenLiveData.postValue(uiState.copy());
    }

    // <editor-fold defaultstate="collapsed" desc="API - 第一画面更新处理">

    private boolean switchDocumentBackToMainScreenOnBeforeFirstScreenChange = false;

    public void switchOnBeforeFirstScreenChange() {
        if (isDocumentInMainScreen()) {
            return;
        }
        switchDocumentBackToMainScreenOnBeforeFirstScreenChange = true;
        documentStreamerViewSwitcher.switchView();
    }

    public void switchOnAfterFirstScreenChange() {
        if (!switchDocumentBackToMainScreenOnBeforeFirstScreenChange) {
            return;
        }
        switchDocumentBackToMainScreenOnBeforeFirstScreenChange = false;
        switchMainScreen(false, true);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外数据源">

    public LiveData<PLVLSStreamerViewPositionUiState> getDocumentInMainScreenLiveData() {
        return documentInMainScreenLiveData;
    }

    // </editor-fold>

    private boolean isDocumentInMainScreen() {
        return !documentStreamerViewSwitcher.isViewSwitched();
    }

    @Nullable
    private PLVSwitchViewAnchorLayout getFirstScreenStreamerView() {
        return nullable(new PLVSugarUtil.Supplier<PLVSwitchViewAnchorLayout>() {
            @Override
            public PLVSwitchViewAnchorLayout get() {
                return linkMicIdToStreamerViewMap.get(firstIndexLinkMicId).get();
            }
        });
    }

}
