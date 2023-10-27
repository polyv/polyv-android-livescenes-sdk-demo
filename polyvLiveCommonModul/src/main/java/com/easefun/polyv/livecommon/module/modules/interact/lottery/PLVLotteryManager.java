package com.easefun.polyv.livecommon.module.modules.interact.lottery;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.socket.event.interact.PLVShowLotteryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Author:lzj
 * Time:2023/7/13
 * Description:
 */
public class PLVLotteryManager {
    private final static String TAG = "PLVLotteryManager";

    private final String EVENT_CLICK_LOTTERY_PENDANT = "CLICK_LOTTERY_PENDANT";
    private final String EVENT_UPDATE_IAR_PENDANT = "UPDATE_IAR_PENDANT";
    private final String STATUS_OVER = "over";
    private final String STATUS_RUNNING = "running";
    private final String STATUS_DELAYTIME = "delayTime";

    private List<ImageView> lotteryEnterViews = new ArrayList<>();
    private List<TextView> lotteryEnterCdTvs = new ArrayList<>();
    private List<PLVTriangleIndicateTextView> lotteryEnterTipsViews = new ArrayList<>();

    private OnLotteryEnterClickListener onLotteryEnterClickListener;
    private Disposable lotteryLookCountdownTask;
    private Disposable showTipsTask;
    private PLVShowLotteryEvent event;

    private PLVWebviewUpdateAppStatusVO.Value.Function lotteryVo;

    //-1 表示没有设定结束时间
    private long remainTime = -1L;
    //当前抽奖状态
    private String lotteryStatus = "";

    public void registerView(ImageView lotteryEnterView, TextView lotteryEnterCdTv, PLVTriangleIndicateTextView lotteryEnterTipsView) {
        lotteryEnterViews.add(lotteryEnterView);
        lotteryEnterCdTvs.add(lotteryEnterCdTv);
        lotteryEnterTipsViews.add(lotteryEnterTipsView);
        if (lotteryEnterView != null) {
            lotteryEnterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onLotteryEnterClickListener != null && !TextUtils.isEmpty(lotteryStatus)) {
                        //这里有两种抽奖情况
                        // 一种是即时抽奖，即时抽奖的状态为running，时间为0
                        //一种是定时抽奖 定时抽奖有 状态有 delayTime、running、over 时间可以为0也可以不为0
                        if (lotteryStatus.equals(STATUS_OVER) || lotteryStatus.equals(STATUS_RUNNING)) {
                            event = new PLVShowLotteryEvent();
                            onLotteryEnterClickListener.onClick(event);
                        } else {
                            showTipsTask();
                        }
                    }
                }
            });
        }
    }

    private void paraseLotteryMessage() {
        if (lotteryVo != null) {
            //解析数据
            if (lotteryVo.getDelayTime() != 0) {
                remainTime = lotteryVo.getDelayTime();
            } else {
                remainTime = -1;
            }
            lotteryStatus = lotteryVo.getStatus();
            if (!lotteryVo.isShow()) {
                hide();
                return;
            }

            //加载自定义的URL图片
            if (!TextUtils.isEmpty(lotteryVo.getIconUrl())) {
                forView(new RunnableT<ImageView>() {
                    @Override
                    public void run(@NonNull ImageView imageView) {
                        PLVImageLoader.getInstance().loadImage(lotteryVo.getIconUrl(), imageView);
                    }
                }, lotteryEnterViews);
            }

            if (lotteryVo.getStatus().equals(STATUS_OVER)) {
                disposeLotteryTask();
                disposeShowTipsTask();
                forView(new RunnableT<TextView>() {
                    @Override
                    public void run(@NonNull TextView textView) {
                        textView.setText(R.string.plv_lottery_over);
                        textView.setVisibility(View.VISIBLE);
                    }
                }, lotteryEnterCdTvs);
                forView(new RunnableT<ImageView>() {
                    @Override
                    public void run(@NonNull ImageView imageView) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                }, lotteryEnterViews);
                forView(new RunnableT<PLVTriangleIndicateTextView>() {
                    @Override
                    public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                        plvTriangleIndicateTextView.setVisibility(View.GONE);
                    }
                }, lotteryEnterTipsViews);
                return;
            }

            if (remainTime != -1) {
                startLotteryLookCountDownTask(remainTime);
            } else {
                startLotteryNoCountDown();
            }

            show();
        }
    }

    public void setLotteryEnterClickListener(OnLotteryEnterClickListener listener) {
        this.onLotteryEnterClickListener = listener;
    }


    private void hide() {
        disposeLotteryTask();
        disposeShowTipsTask();
        forView(new RunnableT<ImageView>() {
            @Override
            public void run(@NonNull ImageView imageView) {
                imageView.setVisibility(View.GONE);
            }
        }, lotteryEnterViews);

        forView(new RunnableT<TextView>() {
            @Override
            public void run(@NonNull TextView textView) {
                textView.setVisibility(View.GONE);
            }
        }, lotteryEnterCdTvs);

        forView(new RunnableT<PLVTriangleIndicateTextView>() {
            @Override
            public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                plvTriangleIndicateTextView.setVisibility(View.GONE);
            }
        }, lotteryEnterTipsViews);

    }

    private void show() {
        forView(new RunnableT<ImageView>() {
            @Override
            public void run(@NonNull ImageView imageView) {
                imageView.setVisibility(View.VISIBLE);
            }
        }, lotteryEnterViews);

        forView(new RunnableT<TextView>() {
            @Override
            public void run(@NonNull TextView textView) {
                textView.setVisibility(View.VISIBLE);
            }
        }, lotteryEnterCdTvs);
    }

    private void startLotteryNoCountDown() {
        disposeLotteryTask();
        disposeShowTipsTask();
        if (lotteryVo != null) {
            switch (lotteryStatus) {
                case STATUS_RUNNING:
                    forView(new RunnableT<PLVTriangleIndicateTextView>() {
                        @Override
                        public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                            plvTriangleIndicateTextView.setText(R.string.plv_lottery_will_start);
                        }
                    }, lotteryEnterTipsViews);

                    forView(new RunnableT<TextView>() {
                        @Override
                        public void run(@NonNull TextView textView) {
                            textView.setText(R.string.plv_lottery_running);
                        }
                    }, lotteryEnterCdTvs);
                    break;
                case STATUS_OVER:
                    forView(new RunnableT<TextView>() {
                        @Override
                        public void run(@NonNull TextView textView) {
                            textView.setText(R.string.plv_lottery_over);
                            textView.setVisibility(View.VISIBLE);
                        }
                    }, lotteryEnterCdTvs);
                    break;
                default:
                    forView(new RunnableT<PLVTriangleIndicateTextView>() {
                        @Override
                        public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                            plvTriangleIndicateTextView.setVisibility(View.GONE);
                        }
                    }, lotteryEnterTipsViews);
                    break;
            }
        }
    }

    private void startLotteryLookCountDownTask(final long needLookTime) {
        if (!lotteryStatus.equals(STATUS_DELAYTIME)) {
            return;
        }
        disposeLotteryTask();
        lotteryLookCountdownTask = Observable.intervalRange(0, needLookTime, 0, 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        remainTime = needLookTime - aLong;
                        //设置倒数时间
                        forView(new RunnableT<TextView>() {
                            @Override
                            public void run(@NonNull TextView textView) {
                                textView.setText(PLVTimeUtils.generateTime(remainTime * 1000, true));
                            }
                        }, lotteryEnterCdTvs);

                        //设置tips文案 大于3s的显示 '抽奖暂未开始' 小于3s显示'抽奖即将开始'
                        if (remainTime > 3) {
                            forView(new RunnableT<PLVTriangleIndicateTextView>() {
                                @Override
                                public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                    plvTriangleIndicateTextView.setText(R.string.plv_lottery_no_start);
                                }
                            }, lotteryEnterTipsViews);
                        }
                        if (remainTime <= 3 && remainTime > 0) {
                            forView(new RunnableT<PLVTriangleIndicateTextView>() {
                                @Override
                                public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                    plvTriangleIndicateTextView.setText(R.string.plv_lottery_will_start);
                                    showTipsTask();
                                }
                            }, lotteryEnterTipsViews);
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
                                textView.setText(R.string.plv_lottery_running);
                            }
                        }, lotteryEnterCdTvs);

                        forView(new RunnableT<PLVTriangleIndicateTextView>() {
                            @Override
                            public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                plvTriangleIndicateTextView.setVisibility(View.GONE);
                            }
                        }, lotteryEnterTipsViews);
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
        }, lotteryEnterTipsViews);
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
                        }, lotteryEnterTipsViews);
                    }
                });
    }


    public void acceptLotteryVo(PLVWebviewUpdateAppStatusVO statusVO) {
        if (statusVO != null) {
            if (statusVO.getEvent() != null && statusVO.getEvent().equals(EVENT_UPDATE_IAR_PENDANT)) {
                if (statusVO.getValue() != null && statusVO.getValue().getDataArray() != null
                        && statusVO.getValue().getDataArray().size() != 0) {
                    for (PLVWebviewUpdateAppStatusVO.Value.Function function : statusVO.getValue().getDataArray()) {
                        if (function.getEvent().equals(EVENT_CLICK_LOTTERY_PENDANT)) {
                            lotteryVo = function;
                            paraseLotteryMessage();
                        }
                    }
                } else {
                    hide();
                }
            }

        }
    }

    private void disposeLotteryTask() {
        if (lotteryLookCountdownTask != null) {
            lotteryLookCountdownTask.dispose();
        }
    }

    private void disposeShowTipsTask() {
        if (showTipsTask != null) {
            showTipsTask.dispose();
        }
    }

    private <T> void forView(PLVLotteryManager.RunnableT<T> runnable, List<T> views) {
        for (T view : views) {
            if (view != null) {
                runnable.run(view);
            }
        }
    }

    private interface RunnableT<T> {
        void run(@NonNull T t);
    }

    public interface OnLotteryEnterClickListener {
        void onClick(PLVShowLotteryEvent event);
    }

    public void destroy() {
        disposeLotteryTask();
        disposeShowTipsTask();
        lotteryEnterCdTvs.clear();
        lotteryEnterTipsViews.clear();
        lotteryEnterViews.clear();
    }

}
