package com.easefun.polyv.livecommon.module.modules.interact.cardpush;

import android.graphics.Color;
import androidx.annotation.NonNull;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.livescenes.model.interact.PLVCardPushVO;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.interact.PLVShowPushCardEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 卡片推送管理器
 */
public class PLVCardPushManager {
    private List<ImageView> cardEnterViews = new ArrayList<>();
    private List<TextView> cardEnterCdTvs = new ArrayList<>();
    private List<PLVTriangleIndicateTextView> cardEnterTipsViews = new ArrayList<>();
    private OnCardEnterClickListener onCardEnterClickListener;
    private boolean canSendCardPushEvent;
    private PLVShowPushCardEvent showPushCardEvent;
    private Disposable requestCardPushInfoTask;
    private Disposable cardLookCountdownTask;

    public void registerView(ImageView cardEnterView, TextView cardEnterCdTv, final PLVTriangleIndicateTextView cardEnterTipsView) {
        cardEnterViews.add(cardEnterView);
        if (cardEnterView != null) {
            cardEnterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (canSendCardPushEvent && onCardEnterClickListener != null && showPushCardEvent != null) {
                        onCardEnterClickListener.onClick(showPushCardEvent);
                    }
                    final boolean[] isShowCdText = {false};
                    forView(new RunnableT<TextView>() {

                        @Override
                        public void run(@NonNull TextView textView) {
                            if (textView.getVisibility() == View.VISIBLE) {
                                isShowCdText[0] = true;
                            }
                        }
                    }, cardEnterCdTvs);
                    if (isShowCdText[0]) {
                        forView(new RunnableT<PLVTriangleIndicateTextView>() {
                            @Override
                            public void run(@NonNull final PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                plvTriangleIndicateTextView.setVisibility(View.VISIBLE);
                                plvTriangleIndicateTextView.removeCallbacks(getTag(plvTriangleIndicateTextView));
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        plvTriangleIndicateTextView.setVisibility(View.GONE);
                                    }
                                };
                                plvTriangleIndicateTextView.setTag(runnable);
                                plvTriangleIndicateTextView.postDelayed(runnable, 3000);
                            }
                        }, cardEnterTipsViews);
                    }
                }
            });
        }
        cardEnterCdTvs.add(cardEnterCdTv);
        cardEnterTipsViews.add(cardEnterTipsView);
    }

    public void setOnCardEnterClickListener(OnCardEnterClickListener listener) {
        this.onCardEnterClickListener = listener;
    }

    public void acceptNewsPushStartMessage(IPLVChatroomContract.IChatroomPresenter chatroomPresenter, final PLVNewsPushStartEvent newsPushStartEvent) {
        disposeCardPushAllTask();
        if (chatroomPresenter == null) {
            return;
        }
        requestCardPushInfoTask = chatroomPresenter.getCardPushInfo(newsPushStartEvent.getId())
                .subscribe(new Consumer<PLVCardPushVO>() {
                    @Override
                    public void accept(PLVCardPushVO plvCardPushVO) throws Exception {
                        acceptCardPushVO(plvCardPushVO, newsPushStartEvent);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    public void acceptNewsPushCancelMessage() {
        disposeCardPushAllTask();
        AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
            @Override
            public void run() {
                forView(new RunnableT<ImageView>() {
                    @Override
                    public void run(@NonNull ImageView imageView) {
                        imageView.setVisibility(View.GONE);
                    }
                }, cardEnterViews);
                forView(new RunnableT<TextView>() {
                    @Override
                    public void run(@NonNull TextView textView) {
                        textView.setVisibility(View.GONE);
                    }
                }, cardEnterCdTvs);
                forView(new RunnableT<PLVTriangleIndicateTextView>() {
                    @Override
                    public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                        plvTriangleIndicateTextView.setVisibility(View.GONE);
                    }
                }, cardEnterTipsViews);
            }
        });
    }

    public void disposeCardPushAllTask() {
        disposeCardPushAllTask(true);
    }

    public void disposeCardPushAllTask(boolean isRemoveTipsViewTask) {
        if (cardLookCountdownTask != null) {
            cardLookCountdownTask.dispose();
        }
        if (requestCardPushInfoTask != null) {
            requestCardPushInfoTask.dispose();
        }
        if (isRemoveTipsViewTask) {
            forView(new RunnableT<PLVTriangleIndicateTextView>() {
                @Override
                public void run(@NonNull PLVTriangleIndicateTextView view) {
                    view.removeCallbacks(getTag(view));
                }
            }, cardEnterTipsViews);
        }
    }

    private Runnable getTag(PLVTriangleIndicateTextView view) {
        if (view.getTag() instanceof Runnable) {
            return (Runnable) view.getTag();
        }
        return null;
    }

    private void acceptCardPushVO(final PLVCardPushVO cardPushVO, PLVNewsPushStartEvent newsPushStartEvent) {
        final String id = newsPushStartEvent.getId();
        final int lookTime = newsPushStartEvent.getLookTime();
        final int alreadyLookTime = PLVCardLookTimeLocalRepository.getCache(id);
        final int needLookTime = lookTime - alreadyLookTime;
        boolean isEntrance = newsPushStartEvent.isEntrance();
        final boolean isEntranceOrNeedLook = isEntrance || needLookTime > 0;
        canSendCardPushEvent = needLookTime <= 0;
        forView(new RunnableT<ImageView>() {
            @Override
            public void run(@NonNull ImageView cardEnterView) {
                cardEnterView.setVisibility(isEntranceOrNeedLook ? View.VISIBLE : View.GONE);
                if (cardPushVO.isRedpackType()) {
                    cardEnterView.setImageResource(R.drawable.plv_interact_redpack_gain);
                } else if (cardPushVO.isGiftboxType()) {
                    cardEnterView.setImageResource(R.drawable.plv_interact_giftbox_gain);
                } else if (cardPushVO.isCustomType()) {
                    PLVImageLoader.getInstance().loadImage(cardPushVO.getData().getEnterImage(), cardEnterView);
                }
            }
        }, cardEnterViews);
        forView(new RunnableT<TextView>() {
            @Override
            public void run(@NonNull TextView cardEnterCdTv) {
                cardEnterCdTv.setVisibility(isEntranceOrNeedLook && needLookTime > 0 ? View.VISIBLE : View.GONE);
                if (needLookTime > 0) {
                    cardEnterCdTv.setText(PLVTimeUtils.generateTime(needLookTime, true));
                }
            }
        }, cardEnterCdTvs);
        forView(new RunnableT<PLVTriangleIndicateTextView>() {
            @Override
            public void run(@NonNull final PLVTriangleIndicateTextView cardEnterTipsView) {
                cardEnterTipsView.removeCallbacks(getTag(cardEnterTipsView));
                cardEnterTipsView.setVisibility(isEntranceOrNeedLook && lookTime > 0 ? View.VISIBLE : View.GONE);
                cardEnterTipsView.setText(cardPushVO.getTipsMsg());
                if (cardPushVO.isGiftboxType()) {
                    cardEnterTipsView.setColor(Color.parseColor("#F6A125"), Color.parseColor("#FD8121"));
                } else {
                    cardEnterTipsView.setColor(Color.parseColor("#FF9D4D"), Color.parseColor("#F65F49"));
                }
                if (cardEnterTipsView.getTrianglePosition() == PLVTriangleIndicateTextView.POSITION_RIGHT) {
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) cardEnterTipsView.getLayoutParams();
                    mlp.bottomMargin = needLookTime > 0 ? ConvertUtils.dp2px(108) : ConvertUtils.dp2px(98);
                    cardEnterTipsView.setLayoutParams(mlp);
                }
                if (needLookTime > 0) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            cardEnterTipsView.setVisibility(View.GONE);
                        }
                    };
                    cardEnterTipsView.setTag(runnable);
                    cardEnterTipsView.postDelayed(runnable, 3000);
                }
                if (cardEnterTipsView.getVisibility() == View.VISIBLE
                        && cardEnterTipsView.getTrianglePosition() == PLVTriangleIndicateTextView.POSITION_BOTTOM) {
                    int cardEnterViewWidth = ConvertUtils.dp2px(36);
                    float cardEnterTipsViewWidth = Layout.getDesiredWidth(cardEnterTipsView.getText(), cardEnterTipsView.getPaint()) + 2 * cardEnterTipsView.getPaddingStart();
                    float translationX = (cardEnterViewWidth - cardEnterTipsViewWidth) / 2;
                    cardEnterTipsView.setTranslationX(-translationX);
                }
            }
        }, cardEnterTipsViews);
        showPushCardEvent = new PLVShowPushCardEvent(newsPushStartEvent);
        // 卡片观看计时
        if (needLookTime > 0) {
            startCardLookCountdownTask(id, needLookTime, alreadyLookTime, isEntrance);
        }
    }

    private <T> void forView(RunnableT<T> runnable, List<T> views) {
        for (T view : views) {
            if (view != null) {
                runnable.run(view);
            }
        }
    }

    private void startCardLookCountdownTask(final String id, final int needLookTime, final int alreadyLookTime, final boolean isEntrance) {
        disposeCardPushAllTask(false);
        cardLookCountdownTask = Observable.intervalRange(1, needLookTime / 1000, 1000, 1000, TimeUnit.MILLISECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        PLVCardLookTimeLocalRepository.saveCache(id, (int) (alreadyLookTime + (aLong * 1000)));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        final int remainTime = (int) (needLookTime - (aLong * 1000));
                        forView(new RunnableT<TextView>() {
                            @Override
                            public void run(@NonNull TextView textView) {
                                textView.setText(PLVTimeUtils.generateTime(remainTime, true));
                                if (remainTime <= 0) {
                                    textView.setVisibility(View.GONE);
                                    forView(new RunnableT<PLVTriangleIndicateTextView>() {
                                        @Override
                                        public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                            if (plvTriangleIndicateTextView.getTrianglePosition() == PLVTriangleIndicateTextView.POSITION_RIGHT) {
                                                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) plvTriangleIndicateTextView.getLayoutParams();
                                                mlp.bottomMargin = ConvertUtils.dp2px(98);
                                                plvTriangleIndicateTextView.setLayoutParams(mlp);
                                            }
                                        }
                                    }, cardEnterTipsViews);
                                }
                            }
                        }, cardEnterCdTvs);
                        if (remainTime <= 0) {
                            canSendCardPushEvent = true;
                            if (onCardEnterClickListener != null && showPushCardEvent != null) {
                                onCardEnterClickListener.onClick(showPushCardEvent);
                            }
                            forView(new RunnableT<PLVTriangleIndicateTextView>() {
                                @Override
                                public void run(@NonNull PLVTriangleIndicateTextView plvTriangleIndicateTextView) {
                                    plvTriangleIndicateTextView.removeCallbacks(getTag(plvTriangleIndicateTextView));
                                    plvTriangleIndicateTextView.setVisibility(View.GONE);
                                }
                            }, cardEnterTipsViews);
                            if (!isEntrance) {
                                forView(new RunnableT<ImageView>() {
                                    @Override
                                    public void run(@NonNull ImageView imageView) {
                                        imageView.setVisibility(View.GONE);
                                    }
                                }, cardEnterViews);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    private interface RunnableT<T> {
        void run(@NonNull T t);
    }

    public interface OnCardEnterClickListener {
        void onClick(PLVShowPushCardEvent event);
    }
}
