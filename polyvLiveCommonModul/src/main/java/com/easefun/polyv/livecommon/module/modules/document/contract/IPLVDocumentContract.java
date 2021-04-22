package com.easefun.polyv.livecommon.module.modules.document.contract;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMarkToolType;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livescenes.document.PLVSDocumentWebProcessor;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.document.model.PLVSPPTPaintStatus;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.easefun.polyv.livescenes.upload.PLVSDocumentUploadConstant;

import java.io.File;
import java.util.List;

/**
 * 开播文档模块 MVP模式 View和Presenter层接口定义
 *
 * @author suhongtao
 */
public interface IPLVDocumentContract {

    /**
     * MVP - View
     *
     * @see PLVAbsDocumentView
     */
    interface View {

        /**
         * 回调 改变文档区域展示的内容 白板、PPT
         *
         * @param showMode
         * @see Presenter#switchShowMode(PLVDocumentMode)
         */
        void onSwitchShowMode(PLVDocumentMode showMode);

        /**
         * 回调 所有PPT文档列表
         *
         * @param pptInfo ppt文档列表
         * @see Presenter#requestGetPptCoverList()
         * @see Presenter#requestGetPptCoverList(boolean)
         */
        void onPptCoverList(@Nullable PLVSPPTInfo pptInfo);

        /**
         * 回调 单个PPT文档的所有页面
         *
         * @param plvspptJsModel ppt页面列表
         * @see Presenter#requestGetPptPageList(int)
         */
        void onPptPageList(@Nullable PLVSPPTJsModel plvspptJsModel);

        /**
         * 回调 当助教切换PPT页时调用
         *
         * @param pageId 页面id
         */
        void onAssistantChangePptPage(int pageId);

        /**
         * 回调 PPT页面变更
         *
         * @param autoId PPT ID，0表示白板
         * @param pageId 页面ID
         */
        void onPptPageChange(int autoId, int pageId);

        /**
         * 回调 标注工具输入文本时，当点击文本区域触发
         *
         * @param pptPaintStatus 输入的文本
         */
        void onPptPaintStatus(@Nullable PLVSPPTPaintStatus pptPaintStatus);

        /**
         * 上传PPT文档时选择转码类型
         *
         * @param fileUri 上传的PPT文档Uri
         * @return 是否消费事件 当返回true时不再分发给其它view 返回false时继续分发
         */
        boolean requestSelectUploadFileConvertType(Uri fileUri);

        /**
         * 回调 文件上传不成功
         *
         * @param cacheVOS
         * @return 是否消费事件 当返回true时不再分发给其它view 返回false时继续分发
         * @see Presenter#checkUploadFileStatus()
         */
        boolean notifyFileUploadNotSuccess(@NonNull List<PLVPptUploadLocalCacheVO> cacheVOS);

        /**
         * 回调 文件转码动画丢失
         *
         * @param cacheVOS
         * @return 是否消费事件 当返回true时不再分发给其它view 返回false时继续分发
         * @see Presenter#checkUploadFileStatus()
         */
        boolean notifyFileConvertAnimateLoss(@NonNull List<PLVPptUploadLocalCacheVO> cacheVOS);

        /**
         * PPT文档删除回调
         *
         * @param success 是否成功删除
         * @param deletedPptBean 被删除的PPT文档vo 当删除失败时为null
         */
        void onPptDelete(boolean success, @Nullable PLVSPPTInfo.DataBean.ContentsBean deletedPptBean);

    }

    /**
     * MVP - Presenter
     *
     * @see PLVDocumentPresenter
     */
    interface Presenter {

        /**
         * 初始化方法，必须调用一次
         *
         * @param lifecycleOwner 生命周期
         * @param liveRoomDataManager
         * @param documentWebProcessor
         */
        void init(LifecycleOwner lifecycleOwner, IPLVLiveRoomDataManager liveRoomDataManager, PLVSDocumentWebProcessor documentWebProcessor);

        /**
         * 注册MVP-View
         *
         * @param view MVP-View
         * @see PLVAbsDocumentView
         */
        void registerView(View view);

        /**
         * 设置状态 是否正在推流
         *
         * @param isStreamStarted 是否正在推流
         */
        void notifyStreamStatus(boolean isStreamStarted);

        /**
         * 改变文档区域显示的内容 白板、PPT
         *
         * @param showMode
         * @see View#onSwitchShowMode(PLVDocumentMode)
         */
        void switchShowMode(PLVDocumentMode showMode);

        /**
         * 启用标注工具
         *
         * @param enable 是否启用
         */
        void enableMarkTool(boolean enable);

        /**
         * 改变标注工具颜色
         *
         * @param colorString 颜色字符串，格式为16进制RGB，如#AABBCC
         */
        void changeColor(String colorString);

        /**
         * 改变标注工具类型
         *
         * @param markToolType {@link PLVDocumentMarkToolType}
         */
        void changeMarkToolType(@PLVDocumentMarkToolType.Range String markToolType);

        /**
         * 切换至白板
         */
        void changeToWhiteBoard();

        /**
         * 切换至白板指定页面
         *
         * @param pageId 页面ID
         */
        void changeWhiteBoardPage(int pageId);

        /**
         * 切换至PPT文档
         *
         * @param autoId PPT ID
         */
        void changePpt(int autoId);

        /**
         * 切换至PPT文档指定页面
         *
         * @param autoId PPT ID
         * @param pageId 页面ID
         */
        void changePptPage(int autoId, int pageId);

        /**
         * 改变文本标注工具在webview中的显示内容
         *
         * @param content 文本
         */
        void changeTextContent(String content);

        /**
         * 请求获取所有PPT文档列表
         *
         * @see View#onPptCoverList(PLVSPPTInfo)
         */
        void requestGetPptCoverList();

        /**
         * 请求获取所有PPT文档列表
         *
         * @param forceRefresh 强制从服务端刷新
         * @see View#onPptCoverList(PLVSPPTInfo)
         */
        void requestGetPptCoverList(boolean forceRefresh);

        /**
         * 请求获取单个PPT所有页面列表
         *
         * @param autoId PPT ID
         * @see View#onPptPageList(PLVSPPTJsModel)
         */
        void requestGetPptPageList(int autoId);

        /**
         * 回调 用户选择上传的PPT文件
         *
         * @param fileUri PPT文件Uri
         * @see #uploadFile(Uri, String)
         */
        void onSelectUploadFile(Uri fileUri);

        /**
         * 上传PPT文件
         *
         * @param context context
         * @param uploadFile PPT文件
         * @param convertType 转码类型
         * @param listener 上传回调
         */
        void uploadFile(Context context,
                        File uploadFile,
                        @PLVSDocumentUploadConstant.PPTConvertType.PPTConvertTypeAnno String convertType,
                        OnPLVSDocumentUploadListener listener);

        /**
         * 重新开始上传任务
         *
         * @param context context
         * @param fileId ppt文件ID
         * @param listener 上传回调
         */
        void restartUploadFromCache(Context context, String fileId, OnPLVSDocumentUploadListener listener);

        /**
         * 检查上次上传PPT文件状态
         *
         * @see View#notifyFileUploadNotSuccess(List)
         * @see View#notifyFileConvertAnimateLoss(List)
         */
        void checkUploadFileStatus();

        /**
         * 移除本地上传文档缓存
         *
         * @param autoId 文档ID
         */
        void removeUploadCache(int autoId);

        /**
         * 移除本地上传文档缓存
         *
         * @param localCacheVOS
         */
        void removeUploadCache(List<PLVPptUploadLocalCacheVO> localCacheVOS);

        /**
         * 删除PPT文档
         *
         * @param autoId 文档ID
         */
        void deleteDocument(int autoId);

        /**
         * 删除PPT文档
         *
         * @param fileId ppt文件ID
         */
        void deleteDocument(String fileId);

        /**
         * 销毁方法
         */
        void destroy();

    }

}
