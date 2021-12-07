package com.easefun.polyv.livehiclass.modules.linkmic.widget;

import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 分组讨论倒计时布局
 */
public class PLVHCSeminarCountdownWindow {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //倒数次数
    private static final int COUNT_DOWN_TIMES = 5;
    //view
    private View anchor;
    private TextView plvhcLinkmicSeminarTitleTv;
    private TextView plvhcLinkmicMessageTv;
    private PopupWindow popupWindow;

    private String buttonText = "好的";

    //disposable
    private Disposable countdownDisposable;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCSeminarCountdownWindow(View anchor) {
        this.anchor = anchor;
        View rootView = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plvhc_linkmic_seminar_countdown_layout, null);
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
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - view">
    private void initView(View view) {
        plvhcLinkmicSeminarTitleTv = view.findViewById(R.id.plvhc_linkmic_seminar_title_tv);
        plvhcLinkmicMessageTv = view.findViewById(R.id.plvhc_linkmic_message_tv);
        plvhcLinkmicMessageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void acceptOnUserHasGroupLeader(boolean isHasGroupLeader, String nick, boolean isGroupChanged, boolean isLeaderChanged, String groupName) {
        if (isLeaderChanged) {
            setTitleText(isHasGroupLeader ? "你已成为组长" : "老师已将" + nick + "设为组长");
            show();
        } else {
            if (isGroupChanged) {
                setTitleText("老师已将你分配至" + groupName);
                show();
                if (isHasGroupLeader) {
                    PLVHCSeminarCountdownWindow window = new PLVHCSeminarCountdownWindow(anchor);
                    window.setTitleText("你已进入" + groupName + ",并成为组长");
                    window.show();
                }
            } else {
                if (isHasGroupLeader) {
                    setTitleText("你已进入" + groupName + ",并成为组长");
                    show();
                } else {
                    PLVHCToast.Builder.context(anchor.getContext())
                            .setText("你已进入" + groupName)
                            .build()
                            .show();
                }
            }
        }
    }

    public void acceptOnLeaveDiscuss() {
        setTitleText("老师结束分组讨论,即将返回教室");
        setButtonText("返回教室");
        show();
    }

    public PLVHCSeminarCountdownWindow setTitleText(String titleText) {
        plvhcLinkmicSeminarTitleTv.setText(titleText);
        return this;
    }

    public PLVHCSeminarCountdownWindow setButtonText(String buttonText) {
        this.buttonText = buttonText;
        return this;
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
                        plvhcLinkmicMessageTv.setText(buttonText + " (" + (COUNT_DOWN_TIMES - aLong) + "s)");
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
