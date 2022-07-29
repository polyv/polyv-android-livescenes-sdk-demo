package com.easefun.polyv.livestreamer.modules.streamer.position.vo;

/**
 * @author Hoshiiro
 */
public class PLVLSStreamerViewPositionUiState {

    private boolean isDocumentInMainScreen;
    private boolean needSyncUpdateToRemote;

    public boolean isDocumentInMainScreen() {
        return isDocumentInMainScreen;
    }

    public boolean isNeedSyncUpdateToRemote() {
        return needSyncUpdateToRemote;
    }

    public PLVLSStreamerViewPositionUiState setDocumentInMainScreen(boolean documentInMainScreen) {
        isDocumentInMainScreen = documentInMainScreen;
        return this;
    }

    public PLVLSStreamerViewPositionUiState setNeedSyncUpdateToRemote(boolean needSyncUpdateToRemote) {
        this.needSyncUpdateToRemote = needSyncUpdateToRemote;
        return this;
    }

    public PLVLSStreamerViewPositionUiState copy() {
        return new PLVLSStreamerViewPositionUiState()
                .setDocumentInMainScreen(isDocumentInMainScreen)
                .setNeedSyncUpdateToRemote(needSyncUpdateToRemote);
    }
}
