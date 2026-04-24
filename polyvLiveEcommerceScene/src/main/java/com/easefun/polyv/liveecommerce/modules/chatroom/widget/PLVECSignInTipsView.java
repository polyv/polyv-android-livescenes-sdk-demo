package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVStringTruncator;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVViewerNameMaskMapper;
import com.plv.socket.event.interact.PLVSignInTimesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PLVECSignInTipsView extends FrameLayout {
    private static final String TAG = "PLVECSignInTipsView";
    private static final int EVENT_MAX_LENGTH = 100;
    private TextView textTv;
    private List<PLVSignInTimesEvent> eventList = new ArrayList<>();
    private boolean isStart;
    private boolean isEnableEffect = false;
    private Disposable acceptDisposable;

    public PLVECSignInTipsView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECSignInTipsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECSignInTipsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.plvec_chat_sign_in_tips_layout, this);
        textTv = findViewById(R.id.sign_in_tips_tv);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        eventList.clear();
        if (acceptDisposable != null) {
            acceptDisposable.dispose();
        }
    }

    private void showText() {
        if (eventList.isEmpty()) {
            setVisibility(View.INVISIBLE);
            TranslateAnimation animation = new TranslateAnimation(0f, -getWidth(), 0f, 0f);
            animation.setDuration(500);
            startAnimation(animation);
            isStart = !isStart;
            return;
        }

        SpannableStringBuilder span;
        if (eventList.size() >= 10) {
            StringBuilder stringBuilder = new StringBuilder();
            int lf = 0;
            int ls = 0;
            for (int i = 0; i <= 2; i++) {
                PLVSignInTimesEvent event = eventList.get(i);
                String viewerName = maskViewerName(event);
                if (i != 2) {
                    stringBuilder.append(viewerName).append("、");
                } else {
                    stringBuilder.append(viewerName);
                }
                if (i == 0) {
                    lf = stringBuilder.toString().length() - 1;
                } else if (i == 1) {
                    ls = stringBuilder.toString().length() - lf - 2;
                }
            }
            String text = PLVStringTruncator.truncateToMax6ChineseWidth(stringBuilder.toString());
            span = new SpannableStringBuilder(String.format("%s 等%s人签到成功", text, eventList.size() + ""));
            /**
             * ///暂时保留该代码
             *  span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, 3 + lf, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             *  span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1, 3 + lf + 1 + ls, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             *  span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1 + ls + 1, span.length() - 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             *
             */
            eventList.clear();
        } else {
            PLVSignInTimesEvent event = eventList.remove(0);
            String viewerName = maskViewerName(event);
            viewerName = PLVStringTruncator.truncateToMax6ChineseWidth(viewerName);
            span = new SpannableStringBuilder(String.format("%s 签到成功", viewerName));

            /**
             * ///暂时保留该代码
             * span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, span.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             */
         }
        final SpannableStringBuilder finalSpan = span;

        acceptDisposable = Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setVisibility(View.VISIBLE);
                        textTv.setText(finalSpan);
                        TranslateAnimation animation = new TranslateAnimation(-getWidth(), 0f, 0f, 0f);
                        animation.setDuration(500);
                        startAnimation(animation);
                    }
                })
                .delay(500 + 2 * 1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        showText();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.e(TAG, "accept throwable:" + throwable.toString());
                    }
                });
    }

    public void acceptMessage(final PLVSignInTimesEvent event) {
        if (!isEnableEffect) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (eventList.size() >= EVENT_MAX_LENGTH) {
                    return;
                }
                eventList.add(event);
                if (!isStart) {
                    isStart = !isStart;
                    showText();
                }
            }
        });
    }

    private String maskViewerName(PLVSignInTimesEvent event) {
        PLVViewerNameMaskMapper mapper = PLVChannelFeatureManager.onChannel(event.getChannelId())
                .getOrDefault(PLVChannelFeature.LIVE_VIEWER_NAME_MASK_TYPE, PLVViewerNameMaskMapper.KEEP_SOURCE);
        return mapper.invoke(
                event.getNick(),
                "",
                false
        );
    }
}
