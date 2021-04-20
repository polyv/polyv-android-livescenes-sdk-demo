package com.easefun.polyv.livecloudclass.modules.chatroom.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;

import com.easefun.polyv.livecommon.ui.widget.PLVMarqueeTextView;
import com.plv.socket.event.login.PLVLoginEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

//need parent layout
public class PLVLCGreetingTextView extends PLVMarqueeTextView {
    private List<PLVLoginEvent> loginEventList = new ArrayList<>();
    private boolean isStart;
    private Disposable acceptLoginDisposable;
    private Runnable runnable;
    private int rollDuration;

    public PLVLCGreetingTextView(Context context) {
        super(context);
    }

    public PLVLCGreetingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVLCGreetingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loginEventList = Collections.synchronizedList(loginEventList);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        loginEventList.clear();
        removeCallbacks(runnable);
        if (acceptLoginDisposable != null) {
            acceptLoginDisposable.dispose();
        }
    }

    private int getScrollTime() {
        int scrollTime = 2;
        return scrollTime;
    }

    private int getStayTime() {
        int stayTime = 2;
        return stayTime;
    }

    private void showGreetingText() {
        if (loginEventList.isEmpty()) {
            stopScroll();
            setVisibility(View.INVISIBLE);
            ((ViewGroup) getParent()).setVisibility(View.GONE);
            ((ViewGroup) getParent()).clearAnimation();
            ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0f);
            scaleAnimation.setDuration(500);
            ((ViewGroup) getParent()).startAnimation(scaleAnimation);
            isStart = !isStart;
            return;
        }

        final int scrollTime = getScrollTime();
        rollDuration = scrollTime * 1000;

        SpannableStringBuilder span;
        if (loginEventList.size() >= 10) {
            StringBuilder stringBuilder = new StringBuilder();
            int lf = 0;
            int ls = 0;
            for (int i = 0; i <= 2; i++) {
                PLVLoginEvent loginEvent = loginEventList.get(i);
                if (i != 2)
                    stringBuilder.append(loginEvent.getUser().getNick()).append("、");
                else
                    stringBuilder.append(loginEvent.getUser().getNick());
                if (i == 0)
                    lf = stringBuilder.toString().length() - 1;
                else if (i == 1)
                    ls = stringBuilder.toString().length() - lf - 2;
            }
            span = new SpannableStringBuilder("欢迎 " + stringBuilder.toString() + " 等" + loginEventList.size() + "人加入");
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, 3 + lf, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1, 3 + lf + 1 + ls, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1 + ls + 1, span.length() - 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            loginEventList.clear();
        } else {
            PLVLoginEvent loginEvent = loginEventList.remove(0);
            span = new SpannableStringBuilder("欢迎 " + loginEvent.getUser().getNick() + " 加入");
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, span.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        final SpannableStringBuilder finalSpan = span;

        acceptLoginDisposable = Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        ((ViewGroup) getParent()).setVisibility(View.VISIBLE);
                        setVisibility(View.INVISIBLE);
                        setText(finalSpan);
                        setStopToCenter(true);
                        setRndDuration(scrollTime * 1000);
                        setOnGetRollDurationListener(new OnGetRollDurationListener() {
                            @Override
                            public void onFirstGetRollDuration(int rollDuration) {
                                PLVLCGreetingTextView.this.rollDuration = rollDuration;
                            }
                        });
                        stopScroll();
                        startScroll();
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer integer) throws Exception {
                        return Observable.timer(rollDuration + getStayTime() * 1000, TimeUnit.MILLISECONDS);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        showGreetingText();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    public void acceptLoginEvent(final PLVLoginEvent loginEvent) {
        loginEventList.add(loginEvent);
        if (!isStart) {
            isStart = !isStart;
            if (getWidth() > 0) {
                showGreetingText();
            } else {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        ((ViewGroup) getParent()).setVisibility(View.VISIBLE);//先显示父控件才能获取到宽
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                showGreetingText();
                            }
                        };
                        post(runnable);
                    }
                };
                post(runnable);
            }
        }
    }
}
