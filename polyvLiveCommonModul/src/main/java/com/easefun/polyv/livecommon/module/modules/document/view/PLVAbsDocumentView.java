package com.easefun.polyv.livecommon.module.modules.document.view;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.document.contract.IPLVDocumentContract;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.document.model.PLVSPPTPaintStatus;

import java.util.List;

/**
 * 文档MVP模式View层空实现
 *
 * @author suhongtao
 * @see IPLVDocumentContract.View
 */
public abstract class PLVAbsDocumentView implements IPLVDocumentContract.View {

    @Override
    public void onSwitchShowMode(PLVDocumentMode showMode) {
        // Not implemented.
    }

    @Override
    public void onPptCoverList(@Nullable PLVSPPTInfo pptInfo) {
        // Not implemented.
    }

    @Override
    public void onPptPageList(@Nullable PLVSPPTJsModel plvspptJsModel) {
        // Not implemented.
    }

    @Override
    public void onAssistantChangePptPage(int pageId) {
        // Not implemented.
    }

    @Override
    public void onPptPageChange(int autoId, int pageId) {
        // Not implemented.
    }

    @Override
    public void onPptPaintStatus(@Nullable PLVSPPTPaintStatus pptPaintStatus) {
        // Not implemented.
    }

    @Override
    public boolean requestSelectUploadFileConvertType(Uri fileUri) {
        // Not implemented.
        return false;
    }

    @Override
    public boolean notifyFileUploadNotSuccess(@NonNull List<PLVPptUploadLocalCacheVO> cacheVOS) {
        // Not implemented.
        return false;
    }

    @Override
    public boolean notifyFileConvertAnimateLoss(@NonNull List<PLVPptUploadLocalCacheVO> cacheVOS) {
        // Not implemented.
        return false;
    }

    @Override
    public void onPptDelete(boolean success, @Nullable PLVSPPTInfo.DataBean.ContentsBean deletedPptBean) {
        // Not implemented.
    }
}
