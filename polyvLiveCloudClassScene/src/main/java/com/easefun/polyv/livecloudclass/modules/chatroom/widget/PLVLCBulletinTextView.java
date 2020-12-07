package com.easefun.polyv.livecloudclass.modules.chatroom.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;

import com.easefun.polyv.livecommon.ui.widget.PLVMarqueeTextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

//need parent layout
public class PLVLCBulletinTextView extends PLVMarqueeTextView {
    private Disposable bulletinCdDisposable;

    public PLVLCBulletinTextView(Context context) {
        super(context);
    }

    public PLVLCBulletinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVLCBulletinTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposeBulletinCd();
    }

    // <editor-fold defaultstate="collapsed" desc="公告跑马灯处理">
    public void startMarquee(final CharSequence msg) {
        disposeBulletinCd();
        post(new Runnable() {
            @Override
            public void run() {
                ((ViewGroup) getParent()).setVisibility(View.VISIBLE);
                setText(msg);
                setOnGetRollDurationListener(new PLVMarqueeTextView.OnGetRollDurationListener() {
                    @Override
                    public void onFirstGetRollDuration(int rollDuration) {
                        startCountDown(rollDuration * 3 + getScrollFirstDelay());
                    }
                });
                stopScroll();
                startScroll();
            }
        });
    }

    private void startCountDown(long time) {
        disposeBulletinCd();
        bulletinCdDisposable = Observable.timer(time, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        setVisibility(View.INVISIBLE);
                        stopScroll();
                        ((ViewGroup) getParent()).setVisibility(View.GONE);
                        ((ViewGroup) getParent()).clearAnimation();
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0f);
                        scaleAnimation.setDuration(500);
                        ((ViewGroup) getParent()).startAnimation(scaleAnimation);
                    }
                });
    }

    private void disposeBulletinCd() {
        if (bulletinCdDisposable != null) {
            bulletinCdDisposable.dispose();
            bulletinCdDisposable = null;
        }
    }
    // </editor-fold>
}
