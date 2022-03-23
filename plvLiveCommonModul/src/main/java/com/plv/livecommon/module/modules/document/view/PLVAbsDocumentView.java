package com.plv.livecommon.module.modules.document.view;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.plv.livecommon.module.modules.document.contract.IPLVDocumentContract;
import com.plv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.plv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.plv.livescenes.document.model.PLVPPTInfo;
import com.plv.livescenes.document.model.PLVPPTJsModel;
import com.plv.livescenes.document.model.PLVPPTPaintStatus;
import com.plv.livescenes.document.model.PLVPPTStatus;

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
    public void onPptCoverList(@Nullable PLVPPTInfo pptInfo) {
        // Not implemented.
    }

    @Override
    public void onPptPageList(@Nullable PLVPPTJsModel plvspptJsModel) {
        // Not implemented.
    }

    @Override
    public void onAssistantChangePptPage(int pageId) {
        // Not implemented.
    }

    @Override
    public void onUserPermissionChange() {
        // Not implemented.
    }

    @Override
    public void onPptPageChange(int autoId, int pageId) {
        // Not implemented.
    }

    @Override
    public void onPptStatusChange(PLVPPTStatus pptStatus) {
        // Not implemented.
    }

    @Override
    public void onPptPaintStatus(@Nullable PLVPPTPaintStatus pptPaintStatus) {
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
    public void onPptDelete(boolean success, @Nullable PLVPPTInfo.DataBean.ContentsBean deletedPptBean) {
        // Not implemented.
    }

    @Override
    public boolean onRequestOpenPptView(int pptId, String pptName) {
        // Not implemented.
        return false;
    }
}
