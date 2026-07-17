package com.easefun.polyv.livecommon.module.modules.interact.luckybag;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.socket.event.interact.PLVCheckLuckyBagCommentEvent;
import com.plv.socket.event.interact.PLVShowLuckyBagEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class PLVLuckyBagManager implements IPLVLifecycleAwareDependComponent {
    private final List<ImageView> enterViews = new ArrayList<>();
    private final List<TextView> enterCdTvs = new ArrayList<>();
    private final List<PLVTriangleIndicateTextView> tipsViews = new ArrayList<>();

    private OnLuckyBagEnterClickListener onLuckyBagEnterClickListener;
    private OnCheckCommendListener onCheckCommendListener;
    private OnJSLuckyBagCommentListener commentListener;
    private Disposable luckyBagCountdownTask;
    private Disposable showTipsTask;

    private boolean isShowPendant = false;
    private long remainTime = -1L;
    private String content = "";

    public void registerView(ImageView enterView, TextView enterCdTv, PLVTriangleIndicateTextView tipsView) {
        enterViews.add(enterView);
        enterCdTvs.add(enterCdTv);
        tipsViews.add(tipsView);
        if (enterView != null) {
            enterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onLuckyBagEnterClickListener != null) {
                        onLuckyBagEnterClickListener.onClick(new PLVShowLuckyBagEvent());
                    }
                }
            });
        }
    }

    public void acceptLuckyBagVO(final PLVLuckyBagVO data) {
        if (data == null) {
            hide();
            return;
        }
        content = data.getContent();
        isShowPendant = data.isHasPendant();
        if (!isShowPendant) {
            hide();
            return;
        }
        remainTime = data.getCountdownTime() > 0 ? data.getCountdownTime() : -1;
        if (!TextUtils.isEmpty(data.getIconUrl())) {
            forView(new RunnableT<ImageView>() {
                @Override
                public void run(@NonNull ImageView imageView) {
                    PLVImageLoader.getInstance().loadImage(data.getIconUrl(), imageView);
                }
            }, enterViews);
        }

        if (remainTime != -1) {
            startLuckyBagCountDownTask(remainTime);
        } else {
            disposeLuckyBagTask();
            disposeShowTipsTask();
            if (!TextUtils.isEmpty(content)) {
                forView(new RunnableT<TextView>() {
                    @Override
                    public void run(@NonNull TextView textView) {
                        textView.setText(content);
                    }
                }, enterCdTvs);
            }
        }
        show();
    }

    public void sendCommentForLottery(String msg) {
        if (!isShowPendant) {
            return;
        }
        PLVCheckLuckyBagCommentEvent event = new PLVCheckLuckyBagCommentEvent();
        PLVCheckLuckyBagCommentEvent.PLVCommentEvent data = new PLVCheckLuckyBagCommentEvent.PLVCommentEvent();
        data.setComment(msg);
        event.setData(data);
        if (onCheckCommendListener != null) {
            onCheckCommendListener.onCommendMessage(event);
        }
    }

    public void handleLuckyBagComment(String comment) {
        if (commentListener != null) {
            commentListener.onJSLuckyBagComment(comment);
        }
    }

    public void setOnLuckyBagEnterClickListener(OnLuckyBagEnterClickListener listener) {
        this.onLuckyBagEnterClickListener = listener;
    }

    public void setOnCheckCommendListener(OnCheckCommendListener listener) {
        this.onCheckCommendListener = listener;
    }

    public void setJSLuckyBagCommentListener(OnJSLuckyBagCommentListener listener) {
        this.commentListener = listener;
    }

    private void startLuckyBagCountDownTask(final long needLookTime) {
        disposeLuckyBagTask();
        luckyBagCountdownTask = Observable.intervalRange(0, needLookTime, 0, 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        remainTime = needLookTime - aLong;
                        forView(new RunnableT<TextView>() {
                            @Override
                            public void run(@NonNull TextView textView) {
                                textView.setText(PLVTimeUtils.generateTime(remainTime * 1000, false));
                            }
                        }, enterCdTvs);

                        if (0 < remainTime && remainTime <= 3) {
                            forView(new RunnableT<PLVTriangleIndicateTextView>() {
                                @Override
                                public void run(@NonNull PLVTriangleIndicateTextView tipsView) {
                                    tipsView.setText(R.string.plv_lucky_bag_will_open);
                                    showTipsTask();
                                }
                            }, tipsViews);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        forView(new RunnableT<TextView>() {
                            @Override
                            public void run(@NonNull TextView textView) {
                                textView.setText(R.string.plv_lucky_bag_opening);
                            }
                        }, enterCdTvs);

                        forView(new RunnableT<PLVTriangleIndicateTextView>() {
                            @Override
                            public void run(@NonNull PLVTriangleIndicateTextView tipsView) {
                                tipsView.setVisibility(View.GONE);
                            }
                        }, tipsViews);
                    }
                });
    }

    private void showTipsTask() {
        disposeShowTipsTask();
        forView(new RunnableT<PLVTriangleIndicateTextView>() {
            @Override
            public void run(@NonNull PLVTriangleIndicateTextView tipsView) {
                tipsView.setVisibility(View.VISIBLE);
            }
        }, tipsViews);
        showTipsTask = Observable.timer(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        forView(new RunnableT<PLVTriangleIndicateTextView>() {
                            @Override
                            public void run(@NonNull PLVTriangleIndicateTextView tipsView) {
                                tipsView.setVisibility(View.GONE);
                            }
                        }, tipsViews);
                    }
                });
    }

    private void hide() {
        disposeLuckyBagTask();
        disposeShowTipsTask();
        forView(new RunnableT<ImageView>() {
            @Override
            public void run(@NonNull ImageView imageView) {
                imageView.setVisibility(View.GONE);
            }
        }, enterViews);

        forView(new RunnableT<TextView>() {
            @Override
            public void run(@NonNull TextView textView) {
                textView.setVisibility(View.GONE);
            }
        }, enterCdTvs);

        forView(new RunnableT<PLVTriangleIndicateTextView>() {
            @Override
            public void run(@NonNull PLVTriangleIndicateTextView tipsView) {
                tipsView.setVisibility(View.GONE);
            }
        }, tipsViews);
    }

    private void show() {
        forView(new RunnableT<ImageView>() {
            @Override
            public void run(@NonNull ImageView imageView) {
                imageView.setVisibility(View.VISIBLE);
            }
        }, enterViews);

        forView(new RunnableT<TextView>() {
            @Override
            public void run(@NonNull TextView textView) {
                textView.setVisibility(View.VISIBLE);
            }
        }, enterCdTvs);
    }

    private <T> void forView(RunnableT<T> runnable, List<T> views) {
        for (T view : views) {
            if (view != null) {
                runnable.run(view);
            }
        }
    }

    private void disposeLuckyBagTask() {
        if (luckyBagCountdownTask != null) {
            luckyBagCountdownTask.dispose();
        }
    }

    private void disposeShowTipsTask() {
        if (showTipsTask != null) {
            showTipsTask.dispose();
        }
    }

    public void destroy() {
        disposeLuckyBagTask();
        disposeShowTipsTask();
        enterCdTvs.clear();
        tipsViews.clear();
        enterViews.clear();
    }

    @Override
    public void onCleared() {
        destroy();
    }

    private interface RunnableT<T> {
        void run(@NonNull T t);
    }

    public interface OnLuckyBagEnterClickListener {
        void onClick(PLVShowLuckyBagEvent event);
    }

    public interface OnCheckCommendListener {
        void onCommendMessage(PLVCheckLuckyBagCommentEvent event);
    }

    public interface OnJSLuckyBagCommentListener {
        void onJSLuckyBagComment(String comment);
    }
}
