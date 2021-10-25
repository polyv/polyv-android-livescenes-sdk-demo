package com.easefun.polyv.livehiclass.modules.linkmic.widget;

import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 上台邀请倒计时布局
 */
public class PLVHCLinkMicInvitationCountdownWindow {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //倒数次数
    private static final int COUNT_DOWN_TIMES = 5;
    //view
    private View anchor;
    private TextView plvhcLinkmicAnswerTv;
    private PopupWindow popupWindow;

    private View.OnClickListener answerListener;

    //disposable
    private Disposable countdownDisposable;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCLinkMicInvitationCountdownWindow(View anchor) {
        this.anchor = anchor;
        View rootView = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plvhc_linkmic_invitation_countdown_layout, null);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        initView(rootView);

        popupWindow = new PopupWindow();
        popupWindow.setContentView(rootView);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (countdownDisposable != null) {
                    countdownDisposable.dispose();
                }
                if (answerListener != null) {
                    answerListener.onClick(plvhcLinkmicAnswerTv);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView(View view) {
        plvhcLinkmicAnswerTv = view.findViewById(R.id.plvhc_linkmic_answer_tv);
        plvhcLinkmicAnswerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void setOnAnswerListener(final View.OnClickListener listener) {
        answerListener = listener;
    }

    public void show() {
        popupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        if (countdownDisposable != null) {
            countdownDisposable.dispose();
        }
        countdownDisposable = Observable.intervalRange(0, COUNT_DOWN_TIMES + 1, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        plvhcLinkmicAnswerTv.setText("立即上台(" + (COUNT_DOWN_TIMES - aLong) + "s)");
                        if (aLong == COUNT_DOWN_TIMES) {
                            hide();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        // nothing
                    }
                });
    }

    public void hide() {
        popupWindow.dismiss();
        if (countdownDisposable != null) {
            countdownDisposable.dispose();
        }
    }
    // </editor-fold>
}
