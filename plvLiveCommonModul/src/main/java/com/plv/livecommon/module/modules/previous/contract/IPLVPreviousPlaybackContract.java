package com.plv.livecommon.module.modules.previous.contract;

import com.plv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.plv.livecommon.module.modules.previous.presenter.data.PLVPreviousData;
import com.plv.livescenes.model.PLVPlaybackListVO;
import com.plv.livescenes.previous.model.PLVChapterDataVO;

import java.util.List;

/**
 * 往期回放-章节 MVP模式 View和Presenter层接口定义
 */
public interface IPLVPreviousPlaybackContract {

    //<editor-fold defaultstate="collapsed" desc="1、mvp-往期播放视频view接口">
    interface IPreviousPlaybackView {

        /**
         * 设置presenter
         * @param presenter
         */
        void setPresenter(IPreviousPlaybackPresenter presenter);

        /**
         * 更新回放视频列表
         * @param playbackListInfo 回放的列表数据信息
         */
        void updatePreviousVideoList(PLVPlaybackListVO playbackListInfo);

        /**
         * 跟新章节列表
         */
        void updateChapterList(List<PLVChapterDataVO> dataList);

        /**
         * 请求往期回放视频错误
         */
        void requestPreviousError();

        /**
         * 请求往期章节错误
         */
        void requestChapterError();

        /**
         * 往期回放视频没有更多数据
         */
        void previousNoMoreData();

        /**
         * 章节没有更多数据
         */
        void chapterNoMoreData();

        /**
         * 加载更多视频
         *
         * @param listVO 返回的视频列表信息
         */
        void previousLoadModreData(PLVPlaybackListVO listVO);

        /**
         * 加载更多视频错误
         */
        void previousLoadMoreError();

        /**
         * 视频播放完毕
         */
        void onPlayComplete();

        /**
         * 进度条发生变化
         * @param position 进度条当前进度
         */
        void onSeekChange(int position);
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、mvp-往期播放视频presenter接口">
    /**
     * mvp-往期Presenter层接口
     */
    interface IPreviousPlaybackPresenter {

        /**
         * 注册View
         * @param view IPreviousPlaybackView
         */
        void registerView(IPreviousPlaybackView view);

        /**
         * 取消注册View
         * @param view
         */
        void unregisterView(IPreviousPlaybackView view);


        /**
         * 改变回放视频的vid
         * @param vid 回放视频的vid
         */
        void changePlaybackVideoVid(String vid);

        /**
         * 改变回放视频的进度
         * @param position 跳转进度的位置
         */
        void changePlaybackVideoSeek(int position);

        /**
         * 请求回放视频列表
         */
        void requestPreviousList();

        /**
         * 请求回放章节的详细信息
         */
        void requestChapterDetail();

        /**
         * 初始化数据
         */
        void init();

        /**
         * 获取回放视频的数据
         * @return 返回回放视频的数据（包括vid和要跳转的进度信息)
         */
        PLVPreviousData getData();

        /**
         * 请求加载更多往期视频
         */
        void requestLoadMorePreviousVideo();

        /**
         * 销毁presenter
         */
        void onDestroy();

        /**
         * mediaLayout里的视频播放完毕
         */
        void onPlayComplete();

        /**
         * 当进度条发生改变的
         * @param position 当前进度条的位置 单位为秒
         */
        void onSeekChange(int position);

        /**
         * 页面菜单更新当前进度，通知往期、章节页面更新
         * @param playInfoVO
         */
        void updatePlaybackCurrentPosition(PLVPlayInfoVO playInfoVO);
    }
    // </editor-fold>
}
