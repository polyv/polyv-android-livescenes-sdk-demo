package com.easefun.polyv.liveecommerce.modules.playback.fragments;

import android.support.v4.app.Fragment;

import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousView;
import com.plv.livescenes.model.PLVPlaybackListVO;

import java.util.List;

/**
 * 更多回放视频弹窗DialogFragment
 */
public interface IPLVECPreviousDialogFragment {


    // <editor-fold defaultstate="collapsed" desc="对外提供的api">

    /**
     * 将previousView提供给其他view使用
     *
     * @param plvPreviousView 回放视频view
     */
    void setPrviousView(PLVPreviousView plvPreviousView);

    /**
     * 隐藏调DialogFragment
     */
    void hide();

    /**
     * 弹出更多回放视频的窗口
     *
     * @param datas 回放视频列表
     * @param vid   当前播放视频的vid
     */
    void showPlaybackMoreVideoDialog(List<PLVPlaybackListVO.DataBean.ContentsBean> datas, String vid, Fragment fm);

    /**
     * 设置dimiss的回调方法
     *
     * @param listener
     */
    void setDismissListener(DismissListener listener);

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dimiss事件执行需要回调的方法">

    /**
     * Dimiss时需要执行的方法
     */
    interface DismissListener {

        /**
         * 当DialogFragment消失的时候执行的回调
         */
        void onDismissListener();
    }
    // </editor-fold>
}
