package com.easefun.polyv.livecommon.module.modules.previous.presenter;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.presenter.data.PLVPreviousData;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.model.PLVPlaybackListVO;
import com.plv.livescenes.previous.PLVPreviousManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 回放视频列表的Presenter
 */
public class PLVPreviousPlaybackPresenter implements IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter {

    // <editor-fold defaultstate="collapsed" desc="变量">

    //默认每页加载的内容
    private static final int DEFAULT_PAGE_SIZE = 50;

    //当前页数
    private int mCurrentPage = 1;

    //当前往期视频列表
    private final List<PLVPlaybackListVO.DataBean.ContentsBean> mPlaybackList = new ArrayList<>();

    //往期-章节回放mvp模式的view
    private List<IPLVPreviousPlaybackContract.IPreviousPlaybackView> mPreviousViews;

    //数据处理器
    private final IPLVLiveRoomDataManager mLiveRoomDataManager;

    //回放视频信息
    private final PLVPreviousData mPlvPreviousData;

    //请求往期回放视频的Disposable
    private Disposable mPreviousListDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVPreviousPlaybackPresenter(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.mLiveRoomDataManager = liveRoomDataManager;
        mPlvPreviousData = new PLVPreviousData();

        init();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Presenter层定义的方法">
    @Override
    public void registerView(IPLVPreviousPlaybackContract.IPreviousPlaybackView view) {
        if (mPreviousViews == null) {
            mPreviousViews = new ArrayList<>();
        }
        if (!mPreviousViews.contains(view)) {
            mPreviousViews.add(view);
        }
        view.setPresenter(this);
    }

    @Override
    public void unregisterView(IPLVPreviousPlaybackContract.IPreviousPlaybackView view) {
        if (mPreviousViews != null) {
            mPreviousViews.remove(view);
        }
    }

    @Override
    public void changePlaybackVideoVid(String vid) {
        if (vid == null) {
            return;
        }
        mPlvPreviousData.getPlaybackVideoVidData().postValue(vid);
        //更新当前往期视频的信息
        mPlvPreviousData.update(mPlaybackList, vid);
    }

    @Override
    public void requestPreviousList() {
        if (mPreviousListDisposable != null) {
            mPreviousListDisposable.dispose();
        }
        String channelId = mLiveRoomDataManager.getConfig().getChannelId();
        Consumer<PLVPlaybackListVO> successCallback = new Consumer<PLVPlaybackListVO>() {
            @Override
            public void accept(final PLVPlaybackListVO listVO) throws Exception {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVPreviousPlaybackContract.IPreviousPlaybackView view) {
                        if (listVO.getData() != null) {
                            if (listVO.getData().getContents().size() > 0) {
                                //将请求到的数据先暂时存储，供章节使用
                                mPlaybackList.addAll(listVO.getData().getContents());
                                view.updatePreviousVideoList(listVO);
                                if (listVO.getData().getContents().size() < DEFAULT_PAGE_SIZE) {
                                    view.previousNoMoreData();
                                }
                            } else {
                                view.previousNoMoreData();
                            }
                        } else {
                            view.requestPreviousError();
                        }
                    }
                });

            }
        };

        Consumer<Throwable> failCallback = new Consumer<Throwable>() {
            @Override
            public void accept(final Throwable throwable) throws Exception {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVPreviousPlaybackContract.IPreviousPlaybackView view) {
                        PLVCommonLog.exception(throwable);
                        view.requestPreviousError();
                    }
                });
            }
        };
        commonRequestPreviousList(channelId, mCurrentPage, successCallback, failCallback);
    }

    @Override
    public void init() {
        requestPreviousList();
    }

    @Override
    public PLVPreviousData getData() {
        return mPlvPreviousData;
    }

    @Override
    public void requestLoadMorePreviousVideo() {
        //能加载更多，没有更多视频
        mCurrentPage++;
        String channelId = mLiveRoomDataManager.getConfig().getChannelId();
        Consumer<PLVPlaybackListVO> successCallback = new Consumer<PLVPlaybackListVO>() {
            @Override
            public void accept(final PLVPlaybackListVO listVO) throws Exception {
                if (listVO.getData() != null) {
                    final List<PLVPlaybackListVO.DataBean.ContentsBean> contents = listVO.getData().getContents();
                    mPlaybackList.addAll(contents);
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVPreviousPlaybackContract.IPreviousPlaybackView view) {
                            if (contents.size() >= DEFAULT_PAGE_SIZE) {
                                //加载数据大于默认列
                                view.previousLoadModreData(listVO);
                            } else if (contents.size() > 0) {
                                //加载有数据但是小于默认页
                                view.previousLoadModreData(listVO);
                                view.previousNoMoreData();

                            } else {
                                //没有数据
                                view.previousNoMoreData();
                            }
                        }
                    });
                }
            }
        };

        Consumer<Throwable> failCallback = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                PLVCommonLog.exception(throwable);
                //加载错误，将页数恢复
                mCurrentPage--;
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVPreviousPlaybackContract.IPreviousPlaybackView view) {
                        view.previousLoadMoreError();
                    }
                });
            }
        };

        commonRequestPreviousList(channelId, mCurrentPage, successCallback, failCallback);

    }

    @Override
    public void onDestroy() {
        if (mPreviousListDisposable != null) {
            mPreviousListDisposable.dispose();
        }

        if (mPreviousListDisposable != null) {
            mPreviousListDisposable.dispose();
        }
    }

    @Override
    public void onPlayComplete() {
        if (mPreviousViews != null) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVPreviousPlaybackContract.IPreviousPlaybackView view) {
                    view.onPlayComplete();
                }
            });
        }
    }

    @Override
    public void updatePlaybackCurrentPosition(PLVPlayInfoVO playInfoVO) {
        if (playInfoVO.getTotalTime() > 0 && playInfoVO.getPosition() >= playInfoVO.getTotalTime()) {
            onPlayComplete();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="内部定义的方法">
    private void commonRequestPreviousList(String channelId, int mCurrentPage,
                                           Consumer<PLVPlaybackListVO> successCallback, Consumer<Throwable> failCallback) {
        if (mPreviousListDisposable != null) {
            mPreviousListDisposable.dispose();
        }
        mPreviousListDisposable = PLVPreviousManager.getInstance().getPLVChatApi()
                .getPlaybackList(channelId, mCurrentPage, DEFAULT_PAGE_SIZE, mLiveRoomDataManager.getConfig().getVideoListType())
                .subscribe(successCallback, failCallback);
    }

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view回调">
    private void callbackToView(PLVPreviousPlaybackPresenter.ViewRunnable runnable) {
        if (mPreviousViews != null) {
            for (IPLVPreviousPlaybackContract.IPreviousPlaybackView view : mPreviousViews) {
                if (view != null && runnable != null) {
                    runnable.run(view);
                }
            }
        }
    }

    private interface ViewRunnable {
        void run(@NonNull IPLVPreviousPlaybackContract.IPreviousPlaybackView view);
    }
    // </editor-fold>
}
