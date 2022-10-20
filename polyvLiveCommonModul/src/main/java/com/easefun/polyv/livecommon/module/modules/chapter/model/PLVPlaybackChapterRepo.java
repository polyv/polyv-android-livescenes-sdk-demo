package com.easefun.polyv.livecommon.module.modules.chapter.model;

import androidx.annotation.NonNull;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.livescenes.playback.vo.PLVPlaybackDataVO;
import com.plv.livescenes.previous.model.PLVChapterDataVO;

import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackChapterRepo {

    public final Observable<List<PLVChapterDataVO>> chapterListObservable = Observable.create(new ObservableOnSubscribe<List<PLVChapterDataVO>>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<List<PLVChapterDataVO>> emitter) throws Exception {
            chapterListEmitter = emitter;
        }
    });

    private Emitter<List<PLVChapterDataVO>> chapterListEmitter;

    public void updatePlaybackData(PLVPlaybackDataVO playbackDataVO) {
        if (playbackDataVO.getPlaybackListType() == PLVPlaybackListType.TEMP_STORE) {
            updateChapterListByRecord(playbackDataVO);
        } else {
            updateChapterListByPlayback(playbackDataVO);
        }
    }

    private void updateChapterListByPlayback(PLVPlaybackDataVO playbackDataVO) {
        Disposable disposable = PLVChatApiRequestHelper.getInstance().getPlaybackChapterList(playbackDataVO.getChannelId(), playbackDataVO.getVideoId())
                .subscribe(new Consumer<List<PLVChapterDataVO>>() {
                    @Override
                    public void accept(List<PLVChapterDataVO> chapterDataVOList) throws Exception {
                        chapterListEmitter.onNext(chapterDataVOList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    private void updateChapterListByRecord(PLVPlaybackDataVO playbackDataVO) {
        Disposable disposable = PLVChatApiRequestHelper.getInstance().getRecordChapterList(playbackDataVO.getChannelId(), playbackDataVO.getFileId())
                .subscribe(new Consumer<List<PLVChapterDataVO>>() {
                    @Override
                    public void accept(List<PLVChapterDataVO> chapterDataVOList) throws Exception {
                        chapterListEmitter.onNext(chapterDataVOList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

}
