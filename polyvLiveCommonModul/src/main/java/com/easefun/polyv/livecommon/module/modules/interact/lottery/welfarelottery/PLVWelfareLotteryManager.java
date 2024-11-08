package com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery;

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
import com.plv.livescenes.model.interact.PLVWelfareLotteryVO;
import com.plv.socket.event.interact.PLVCheckLotteryCommentEvent;
import com.plv.socket.event.interact.PLVShowWelfareLotteryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PLVWelfareLotteryManager implements IPLVLifecycleAwareDependComponent {
    private List<ImageView> enterViews = new ArrayList<>();
    private List<TextView> enterCdTvs = new ArrayList<>();
    private List<PLVTriangleIndicateTextView> tipsViews = new ArrayList<>();

    private OnWelfareLotteryEnterClickListener onWelfareLotteryEnterClickListener;
    private OnWelfareLotteryCommendListener onWelfareLotteryCommendListener;
    private OnJSLotteryCommentListener commentListener;
    private Disposable lotteryLookCountdownTask;
    private Disposable showTipsTask;
    private PLVShowWelfareLotteryEvent event;

    private PLVWelfareLotteryVO welfareLotteryVO;
    // 是否显示挂件
    private boolean isShowPendant = false;
    //-1 表示没有设定结束时间
    private long remainTime = -1L;
    // 状态文案
    private String content = "";

    public void registerView(ImageView enterView, TextView enterCdTv, PLVTriangleIndicateTextView tipsView) {
        enterViews.add(enterView);
        enterCdTvs.add(enterCdTv);
        tipsViews.add(tipsView);
        if (enterView != null) {
            enterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onWelfareLotteryEnterClickListener != null) {
                        event = new PLVShowWelfareLotteryEvent();
                        onWelfareLotteryEnterClickListener.onClick(event);
                    }
                }
            });
        }
    }

    public void sendCommentForLottery(String msg) {
        if (!isShowPendant) {
            return;
        }
        PLVCheckLotteryCommentEvent event = new PLVCheckLotteryCommentEvent();
        PLVCheckLotteryCommentEvent.PLVLotteryCommentEvent data = new PLVCheckLotteryCommentEvent.PLVLotteryCommentEvent();
        data.setComment(msg);
        event.setData(data);
        if (onWelfareLotteryCommendListener != null) {
            onWelfareLotteryCommendListener.onCommendMessage(event);
        }
    }

    public void handleLotteryComment(String comment) {
        if (commentListener != null) {
            commentListener.onJSLotteryComment(comment);
        }
    }

    public void acceptWelfareLotteryVO(final PLVWelfareLotteryVO data) {
        if (data == null) {
            hide();
            return;
        }
        welfareLotteryVO = data;
        content = data.getContent();
        isShowPendant = data.isHasPendant();
        if (!isShowPendant) {
            hide();
            return;
        }
        if (data.getCountdownTime() > 0) {
            remainTime = data.getCountdownTime();
        } else {
            remainTime = -1;
        }
        // 加载自定义的URL图片
        if (!TextUtils.isEmpty(data.getIconUrl())) {
            forView(new RunnableT<ImageView>() {
                @Override
                public void run(@NonNull ImageView imageView) {
                    PLVImageLoader.getInstance().loadImage(data.getIconUrl(), imageView);
                }
            }, enterViews);
        }


        if (remainTime != -1) {
            startLotteryLookCountDownTask(remainTime);
        } else {
            disposeLotteryTask();
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

    public void setOnWelfareLotteryEnterClickListener(OnWelfareLotteryEnterClickListener listener) {
        this.onWelfareLotteryEnterClickListener = listener;
    }

    public void setOnWelfareLotteryCommendListener(OnWelfareLotteryCommendListener listener) {
        this.onWelfareLotteryCommendListener = listener;
    }

    public void setJSLotteryCommentListener(OnJSLotteryCommentListener listener) {
        this.commentListener = listener;
    }

    private void startLotteryLookCountDownTask(final long needLookTime) {
        disposeLotteryTask();
        lotteryLookCountdownTask = Observable.intervalRange(0, needLookTime, 0, 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        remainTime = needLookTime - aLong;
                        // 设置倒数时间
                        forView(new RunnableT<TextView>() {
                            @Override
                            public void run(@NonNull TextView textView) {
                                textView.setText(PLVTimeUtils.generateTime(remainTime * 1000, true));
                            }
                        }, enterCdTvs);

                        if (0 < remainTime && remainTime <= 3) {
                            forView(new RunnableT<PLVTriangleIndicateTextView>() {
                                @Override
                                public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                    plvTriangleIndicateTextView.setText(R.string.plv_lottery_will_start);
                                    showTipsTask();
                                }
                            }, tipsViews);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        forView(new RunnableT<TextView>() {
                            @Override
                            public void run(@NonNull TextView textView) {
                                textView.setText(R.string.plv_lottery_running);
                            }
                        }, enterCdTvs);

                        forView(new RunnableT<PLVTriangleIndicateTextView>() {
                            @Override
                            public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                plvTriangleIndicateTextView.setVisibility(View.GONE);
                            }
                        }, tipsViews);
                    }
                });
    }

    private void showTipsTask() {
        disposeShowTipsTask();
        forView(new RunnableT<PLVTriangleIndicateTextView>() {
            @Override
            public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                plvTriangleIndicateTextView.setVisibility(View.VISIBLE);
            }
        }, tipsViews);
        showTipsTask = Observable.timer(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        forView(new RunnableT<PLVTriangleIndicateTextView>() {
                            @Override
                            public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                plvTriangleIndicateTextView.setVisibility(View.GONE);
                            }
                        }, tipsViews);
                    }
                });
    }

    private void hide() {
        disposeLotteryTask();
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
            public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                plvTriangleIndicateTextView.setVisibility(View.GONE);
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

    private <T> void forView(PLVWelfareLotteryManager.RunnableT<T> runnable, List<T> views) {
        for (T view : views) {
            if (view != null) {
                runnable.run(view);
            }
        }
    }

    private void disposeLotteryTask() {
        if (lotteryLookCountdownTask != null) {
            lotteryLookCountdownTask.dispose();
        }
    }

    public void destroy() {
        disposeLotteryTask();
        disposeShowTipsTask();
        enterCdTvs.clear();
        tipsViews.clear();
        enterViews.clear();
    }

    private void disposeShowTipsTask() {
        if (showTipsTask != null) {
            showTipsTask.dispose();
        }
    }

    @Override
    public void onCleared() {
        this.destroy();
    }

    private interface RunnableT<T> {
        void run(@NonNull T t);
    }

    public interface OnWelfareLotteryEnterClickListener {
        void onClick(PLVShowWelfareLotteryEvent event);
    }

    public interface OnWelfareLotteryCommendListener {
        void onCommendMessage(PLVCheckLotteryCommentEvent event);
    }

    public interface OnJSLotteryCommentListener {
        void onJSLotteryComment(String comment);
    }

}
