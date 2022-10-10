package com.easefun.polyv.livecommon.module.modules.chapter.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.easefun.polyv.livecommon.module.modules.chapter.model.PLVPlaybackChapterRepo;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.playback.vo.PLVPlaybackDataVO;
import com.plv.livescenes.previous.model.PLVChapterDataVO;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackChapterViewModel implements IPLVLifecycleAwareDependComponent {

    private final PLVPlaybackChapterRepo playbackChapterRepo;
    private final MutableLiveData<PLVPlaybackDataVO> playbackDataVOLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<PLVChapterDataVO>> chapterListLiveData = new MutableLiveData<>();
    private final MutableLiveData<PLVPlayInfoVO> playInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> seekToChapterLiveData = new MutableLiveData<>();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PLVPlaybackChapterViewModel(
            final PLVPlaybackChapterRepo playbackChapterRepo
    ) {
        this.playbackChapterRepo = playbackChapterRepo;

        observeRepo();
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
    }

    private void observeRepo() {
        Disposable disposable = playbackChapterRepo.chapterListObservable
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Consumer<List<PLVChapterDataVO>>() {
                    @Override
                    public void accept(List<PLVChapterDataVO> chapterDataVOList) throws Exception {
                        chapterListLiveData.postValue(chapterDataVOList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });

        compositeDisposable.add(disposable);
    }

    public void updatePlaybackData(PLVPlaybackDataVO playbackDataVO) {
        playbackChapterRepo.updatePlaybackData(playbackDataVO);
        playbackDataVOLiveData.postValue(playbackDataVO);
    }

    public void updatePlayInfo(PLVPlayInfoVO playInfoVO) {
        playInfoLiveData.postValue(playInfoVO);
    }

    public void seekToChapter(int pos) {
        seekToChapterLiveData.postValue(new Event<>(pos));
    }

    public LiveData<PLVPlaybackDataVO> getPlaybackDataVOLiveData() {
        return playbackDataVOLiveData;
    }

    public LiveData<List<PLVChapterDataVO>> getChapterListLiveData() {
        return chapterListLiveData;
    }

    public LiveData<PLVPlayInfoVO> getPlayInfoLiveData() {
        return playInfoLiveData;
    }

    public LiveData<Event<Integer>> getSeekToChapterLiveData() {
        return seekToChapterLiveData;
    }

}
