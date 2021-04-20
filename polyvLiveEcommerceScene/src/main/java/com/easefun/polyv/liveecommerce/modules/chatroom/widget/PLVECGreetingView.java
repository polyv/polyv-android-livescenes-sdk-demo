package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.event.login.PLVLoginEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PLVECGreetingView extends FrameLayout {
    private static final String TAG = "PLVECGreetingView";
    private TextView greetTv;
    private List<PLVLoginEvent> loginEventList = new ArrayList<>();
    private boolean isStart;
    private Disposable acceptLoginDisposable;

    public PLVECGreetingView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECGreetingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECGreetingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.plvec_chat_greeting_layout, this);
        greetTv = findViewById(R.id.greet_tv);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        loginEventList.clear();
        if (acceptLoginDisposable != null) {
            acceptLoginDisposable.dispose();
        }
    }

    private void showGreetingText() {
        if (loginEventList.isEmpty()) {
            setVisibility(View.INVISIBLE);
            TranslateAnimation animation = new TranslateAnimation(0f, -getWidth(), 0f, 0f);
            animation.setDuration(500);
            startAnimation(animation);
            isStart = !isStart;
            return;
        }

        SpannableStringBuilder span;
        if (loginEventList.size() >= 10) {
            StringBuilder stringBuilder = new StringBuilder();
            int lf = 0;
            int ls = 0;
            for (int i = 0; i <= 2; i++) {
                PLVLoginEvent loginEvent = loginEventList.get(i);
                if (i != 2) {
                    stringBuilder.append(loginEvent.getUser().getNick()).append("、");
                } else {
                    stringBuilder.append(loginEvent.getUser().getNick());
                }
                if (i == 0) {
                    lf = stringBuilder.toString().length() - 1;
                } else if (i == 1) {
                    ls = stringBuilder.toString().length() - lf - 2;
                }
            }
            span = new SpannableStringBuilder("欢迎 " + stringBuilder.toString() + " 等" + loginEventList.size() + "人进入直播间");
            /**
             * ///暂时保留该代码
             *  span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, 3 + lf, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             *  span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1, 3 + lf + 1 + ls, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             *  span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1 + ls + 1, span.length() - 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             *
             */
            loginEventList.clear();
        } else {
            PLVLoginEvent loginEvent = loginEventList.remove(0);
            span = new SpannableStringBuilder("欢迎 " + loginEvent.getUser().getNick() + " 进入直播间");

            /**
             * ///暂时保留该代码
             * span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, span.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             */
         }
        final SpannableStringBuilder finalSpan = span;

        acceptLoginDisposable = Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setVisibility(View.VISIBLE);
                        greetTv.setText(finalSpan);
                        TranslateAnimation animation = new TranslateAnimation(-getWidth(), 0f, 0f, 0f);
                        animation.setDuration(500);
                        startAnimation(animation);
                    }
                })
                .delay(500 + 2 * 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        showGreetingText();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.e(TAG, "accept throwable:" + throwable.toString());
                    }
                });
    }

    public void acceptGreetingMessage(final PLVLoginEvent loginEvent) {
        post(new Runnable() {
            @Override
            public void run() {
                loginEventList.add(loginEvent);
                if (!isStart) {
                    isStart = !isStart;
                    showGreetingText();
                }
            }
        });
    }
}
