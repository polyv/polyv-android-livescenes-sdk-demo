package com.easefun.polyv.livecommon.module.modules.venue.model;

import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.feature.venues.PLVMultiVenueApiRequestHelper;
import com.plv.livescenes.feature.venues.model.PLVVenueDataVO;
import com.plv.livescenes.feature.venues.model.PLVVenueStatusVO;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PLVMultiVenueRepo implements IPLVLifecycleAwareDependComponent {

    private static final int TIMESPAN = 10;
    private static volatile PLVMultiVenueRepo INSTANCE;
    private Emitter<List<PLVVenueDataVO>> venueListEmitter;
    private Emitter<List<PLVVenueStatusVO>> venueStatusListEmitter;
    private Disposable venuesDataDisposable;
    private Disposable venuesStatusDisposable;

    private PLVMultiVenueRepo() {

    }

    public static PLVMultiVenueRepo getInstance() {
        if(INSTANCE == null) {
            synchronized (PLVMultiVenueRepo.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVMultiVenueRepo();
                }
            }
        }
        return INSTANCE;
    }


    public final Observable<List<PLVVenueDataVO>> venueListObservable = Observable.create(new ObservableOnSubscribe<List<PLVVenueDataVO>>() {
        @Override
        public void subscribe(ObservableEmitter<List<PLVVenueDataVO>> emitter) throws Exception {
            venueListEmitter = emitter;
        }
    });

    public final Observable<List<PLVVenueStatusVO>> venueStatusObservable = Observable.create(new ObservableOnSubscribe<List<PLVVenueStatusVO>>() {
        @Override
        public void subscribe(ObservableEmitter<List<PLVVenueStatusVO>> emitter) throws Exception {
            venueStatusListEmitter = emitter;
        }
    });

    public void getMultipleVenueData(String channelId, String mainVenueId, int pageSize) {
        if (venuesDataDisposable != null) {
            venuesDataDisposable.dispose();
        }
        venuesDataDisposable = PLVMultiVenueApiRequestHelper.getInstance().getMultiVenueList(channelId, mainVenueId, pageSize)
                .subscribe(new Consumer<List<PLVVenueDataVO>>() {
                    @Override
                    public void accept(List<PLVVenueDataVO> plvVenueDataVOS) throws Exception {
                        if (plvVenueDataVOS != null && plvVenueDataVOS.size() > 0) {
                            venueListEmitter.onNext(plvVenueDataVOS);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    public void updateVenueStatus(final String mainVenueId) {
        if (venuesStatusDisposable != null) {
            venuesStatusDisposable.dispose();
        }
        venuesStatusDisposable = Observable.interval(TIMESPAN, TIMESPAN, TimeUnit.SECONDS)
                .flatMap(new Function<Long, ObservableSource<List<PLVVenueStatusVO>>>() {
                    @Override
                    public ObservableSource<List<PLVVenueStatusVO>> apply(Long aLong) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<List<PLVVenueStatusVO>>() {
                            @Override
                            public void subscribe(final ObservableEmitter<List<PLVVenueStatusVO>> emitter) throws Exception {
                                PLVMultiVenueApiRequestHelper.getInstance().getMultiVenueStatus(mainVenueId)
                                        .subscribe(new Consumer<List<PLVVenueStatusVO>>() {
                                            @Override
                                            public void accept(List<PLVVenueStatusVO> statusVOS) throws Exception {
                                                emitter.onNext(statusVOS);
                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                PLVCommonLog.exception(throwable);
                                            }
                                        });
                            }
                        });
                    }
                }).subscribe(new Consumer<List<PLVVenueStatusVO>>() {
                    @Override
                    public void accept(List<PLVVenueStatusVO> list) throws Exception {
                        venueStatusListEmitter.onNext(list);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });

    }

    @Override
    public void onCleared() {
    }
}