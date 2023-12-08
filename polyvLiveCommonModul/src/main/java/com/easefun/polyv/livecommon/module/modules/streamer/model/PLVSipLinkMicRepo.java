package com.easefun.polyv.livecommon.module.modules.streamer.model;

import static com.plv.foundationsdk.utils.PLVSugarUtil.collectionMinus;
import static com.plv.foundationsdk.utils.PLVSugarUtil.transformList;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.streamer.model.enums.PLVSipLinkMicState;
import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.linkmic.sip.datasource.PLVSipRemoteDataSource;
import com.plv.livescenes.linkmic.sip.datasource.PLVSipSocketDataSource;
import com.plv.livescenes.linkmic.sip.vo.PLVSipChannelInfoVO;
import com.plv.livescenes.linkmic.sip.vo.PLVSipMemberListVO;
import com.plv.livescenes.linkmic.sip.vo.PLVSipSocketMsgVO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicRepo implements IPLVLifecycleAwareDependComponent {

    private final PLVSipRemoteDataSource remoteDataSource;
    private final PLVSipSocketDataSource socketDataSource;

    private final Observable<PLVSipLinkMicViewerVO> linkMicViewerObservable = Observable.create(new ObservableOnSubscribe<PLVSipLinkMicViewerVO>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<PLVSipLinkMicViewerVO> emitter) throws Exception {
            linkMicViewerEmitter = emitter;
        }
    });

    private ObservableEmitter<PLVSipLinkMicViewerVO> linkMicViewerEmitter;

    private final Set<String> activeLinkMicViewerPhoneSet = new HashSet<>();

    private final CompositeDisposable disposables = new CompositeDisposable();
    private Disposable updateSipLinkMicViewerListDisposable;

    public PLVSipLinkMicRepo(
            final PLVSipRemoteDataSource remoteDataSource,
            final PLVSipSocketDataSource socketDataSource
    ) {
        this.remoteDataSource = remoteDataSource;
        this.socketDataSource = socketDataSource;

        observeSocketLinkMicViewerUpdate();
        timerUpdateSipLinkMicViewerList();
    }

    private void observeSocketLinkMicViewerUpdate() {
        final Disposable disposable = socketDataSource.sipSocketMsgObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .retry()
                .map(new Function<PLVSipSocketMsgVO, PLVSipLinkMicViewerVO>() {
                    @Override
                    public PLVSipLinkMicViewerVO apply(@NonNull PLVSipSocketMsgVO sipSocketMsgVO) throws Exception {
                        final PLVSipLinkMicViewerVO sipLinkMicViewerVO = new PLVSipLinkMicViewerVO();
                        sipLinkMicViewerVO.setPhone(sipSocketMsgVO.getPhoneNumber());
                        sipLinkMicViewerVO.setId(sipSocketMsgVO.getId());
                        sipLinkMicViewerVO.setContactName(sipSocketMsgVO.getName());
                        switch (sipSocketMsgVO.getType()) {
                            case PLVSipSocketMsgVO.TYPE_CALL_IN:
                                sipLinkMicViewerVO.setSipLinkMicStatus(PLVSipLinkMicState.ON_CALLING_IN);
                                break;
                            case PLVSipSocketMsgVO.TYPE_CALL_OUT:
                                sipLinkMicViewerVO.setSipLinkMicStatus(PLVSipLinkMicState.ON_CALLING_OUT);
                                break;
                            case PLVSipSocketMsgVO.TYPE_ACCEPT_CALL_IN:
                                sipLinkMicViewerVO.setSipLinkMicStatus(PLVSipLinkMicState.CONNECTED);
                                break;
                            case PLVSipSocketMsgVO.TYPE_HANG_UP:
                                sipLinkMicViewerVO.setSipLinkMicStatus(PLVSipLinkMicState.HANG_UP);
                                break;
                            case PLVSipSocketMsgVO.TYPE_MUTE:
                                sipLinkMicViewerVO.setAudioMuted(true);
                                break;
                            case PLVSipSocketMsgVO.TYPE_UNMUTE:
                                sipLinkMicViewerVO.setAudioMuted(false);
                                break;
                            default:
                                return sipLinkMicViewerVO;
                        }
                        linkMicViewerEmitter.onNext(sipLinkMicViewerVO);
                        return sipLinkMicViewerVO;
                    }
                })
                .subscribe(new Consumer<PLVSipLinkMicViewerVO>() {
                    @Override
                    public void accept(PLVSipLinkMicViewerVO sipLinkMicViewerVO) throws Exception {
                        final boolean isActive = sipLinkMicViewerVO.getSipLinkMicStatus() == PLVSipLinkMicState.HANG_UP;
                        if (isActive) {
                            activeLinkMicViewerPhoneSet.add(sipLinkMicViewerVO.getPhone());
                        } else {
                            activeLinkMicViewerPhoneSet.remove(sipLinkMicViewerVO.getPhone());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    private void timerUpdateSipLinkMicViewerList() {
        final Disposable disposable = Observable.interval(20, 20, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .retry()
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        updateSipLinkMicViewerList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    @Override
    public void onCleared() {
        disposables.dispose();
    }

    public void updateSipLinkMicViewerList() {
        if (updateSipLinkMicViewerListDisposable != null) {
            updateSipLinkMicViewerListDisposable.dispose();
        }
        updateSipLinkMicViewerListDisposable = remoteDataSource.getSipMemberList()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<PLVSipMemberListVO, List<PLVSipLinkMicViewerVO>>() {
                    @Override
                    public List<PLVSipLinkMicViewerVO> apply(@NonNull PLVSipMemberListVO sipMemberListVO) throws Exception {
                        final List<PLVSipLinkMicViewerVO> viewerVOList = new ArrayList<>();
                        if (sipMemberListVO.getData() != null && sipMemberListVO.getData().getCallInDials() != null && !sipMemberListVO.getData().getCallInDials().isEmpty()) {
                            for (PLVSipMemberListVO.Data.SipMember member : sipMemberListVO.getData().getCallInDials()) {
                                final PLVSipLinkMicViewerVO viewerVO = new PLVSipLinkMicViewerVO();
                                viewerVO.setId(String.valueOf(member.getId()));
                                viewerVO.setContactName(member.getUserName());
                                viewerVO.setPhone(member.getPhone());
                                viewerVO.setSipLinkMicStatus(PLVSipLinkMicState.ON_CALLING_IN);
                                viewerVOList.add(viewerVO);
                            }
                        }
                        if (sipMemberListVO.getData() != null && sipMemberListVO.getData().getCallOutDials() != null && !sipMemberListVO.getData().getCallOutDials().isEmpty()) {
                            for (PLVSipMemberListVO.Data.SipMember member : sipMemberListVO.getData().getCallOutDials()) {
                                final PLVSipLinkMicViewerVO viewerVO = new PLVSipLinkMicViewerVO();
                                viewerVO.setId(String.valueOf(member.getId()));
                                viewerVO.setContactName(member.getUserName());
                                viewerVO.setPhone(member.getPhone());
                                viewerVO.setSipLinkMicStatus(PLVSipLinkMicState.ON_CALLING_OUT);
                                viewerVOList.add(viewerVO);
                            }
                        }
                        if (sipMemberListVO.getData() != null && sipMemberListVO.getData().getInLineDials() != null && !sipMemberListVO.getData().getInLineDials().isEmpty()) {
                            for (PLVSipMemberListVO.Data.SipMember member : sipMemberListVO.getData().getInLineDials()) {
                                final PLVSipLinkMicViewerVO viewerVO = new PLVSipLinkMicViewerVO();
                                viewerVO.setId(String.valueOf(member.getId()));
                                viewerVO.setContactName(member.getUserName());
                                viewerVO.setPhone(member.getPhone());
                                viewerVO.setAudioMuted(member.isMuted());
                                viewerVO.setSipLinkMicStatus(PLVSipLinkMicState.CONNECTED);
                                viewerVOList.add(viewerVO);
                            }
                        }
                        return viewerVOList;
                    }
                })
                .doOnNext(new Consumer<List<PLVSipLinkMicViewerVO>>() {
                    @Override
                    public void accept(List<PLVSipLinkMicViewerVO> sipLinkMicViewerVOList) throws Exception {
                        final List<String> currentActivePhoneList = transformList(sipLinkMicViewerVOList, new PLVSugarUtil.Function<PLVSipLinkMicViewerVO, String>() {
                            @Override
                            public String apply(PLVSipLinkMicViewerVO viewerVO) {
                                return viewerVO.getPhone();
                            }
                        });
                        for (String inactiveViewerPhone : collectionMinus(activeLinkMicViewerPhoneSet, currentActivePhoneList)) {
                            final PLVSipLinkMicViewerVO viewerVO = new PLVSipLinkMicViewerVO();
                            viewerVO.setPhone(inactiveViewerPhone);
                            viewerVO.setSipLinkMicStatus(PLVSipLinkMicState.HANG_UP);
                            sipLinkMicViewerVOList.add(viewerVO);
                        }
                        activeLinkMicViewerPhoneSet.clear();
                        activeLinkMicViewerPhoneSet.addAll(currentActivePhoneList);
                    }
                })
                .flatMap(new Function<List<PLVSipLinkMicViewerVO>, ObservableSource<PLVSipLinkMicViewerVO>>() {
                    @Override
                    public ObservableSource<PLVSipLinkMicViewerVO> apply(@NonNull List<PLVSipLinkMicViewerVO> sipLinkMicViewerVOList) throws Exception {
                        return Observable.fromIterable(sipLinkMicViewerVOList);
                    }
                })
                .subscribe(new Consumer<PLVSipLinkMicViewerVO>() {
                    @Override
                    public void accept(PLVSipLinkMicViewerVO viewerVO) throws Exception {
                        if (linkMicViewerEmitter != null) {
                            linkMicViewerEmitter.onNext(viewerVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    public Observable<PLVSipLinkMicViewerVO> getLinkMicViewerObservable() {
        return linkMicViewerObservable;
    }

    public Observable<PLVSipChannelInfoVO> getSipChannelInfo() {
        return remoteDataSource.getSipChannelInfo();
    }
}
