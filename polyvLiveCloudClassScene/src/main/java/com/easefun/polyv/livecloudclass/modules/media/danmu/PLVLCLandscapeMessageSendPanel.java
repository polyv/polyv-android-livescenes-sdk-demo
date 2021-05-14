package com.easefun.polyv.livecloudclass.modules.media.danmu;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

/**
 * date: 2019/6/6 0006
 *
 * @author hwj
 * description 横屏发送信息输入框
 */
public class PLVLCLandscapeMessageSendPanel implements IPLVLCLandscapeMessageSender {
    private PopupWindow window;
    private AppCompatActivity activity;

    private PLVOrientationSensibleLinearLayout llSendMessage;
    private EditText etSendMessage;
    private TextView tvSendMessage;
    private View anchor;

    private OnSendMessageListener sendMessageListener;

    private int usableHeightPrevious;

    public PLVLCLandscapeMessageSendPanel(AppCompatActivity appCompatActivity, View anchor) {
        this.anchor = anchor;
        this.activity = appCompatActivity;
        this.window = new PopupWindow(activity);
        View root = LayoutInflater.from(activity).inflate(R.layout.plvlc_player_message_send_layout, null);
        window.setContentView(root);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.setBackgroundDrawable(null);

        int width = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        int height = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());

        window.setWidth(width);
        window.setHeight(height);

        initView(root);

        FrameLayout content = activity.findViewById(android.R.id.content);
        final View childOfContent = content.getChildAt(0);
        childOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {//all call
                int usableHeightNow = computeUsableHeight(childOfContent);
                if (usableHeightPrevious != usableHeightNow) {
                    ViewGroup.LayoutParams flp = (ViewGroup.LayoutParams) llSendMessage.getLayoutParams();
                    if (flp == null) {
                        return;
                    }
                    int usableHeightSansKeyboard = childOfContent.getRootView().getHeight();
                    int heightDifference = Math.abs(usableHeightSansKeyboard - usableHeightNow);
                    if (heightDifference > (usableHeightNow / 4)) {
                        // keyboard probably just became visible
                        flp.height = usableHeightSansKeyboard - heightDifference;//can invalid
                    } else {
                        flp.height = usableHeightSansKeyboard;
                    }
                    childOfContent.requestLayout();
                    usableHeightPrevious = usableHeightNow;
                }
            }
        });
    }

    private int computeUsableHeight(View view) {
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        //隐藏状态栏情况下才需计算
        return r.bottom;//待完善，横屏为r.bottom，竖屏时为r.bottom-r.top+navbarHeight，存在刘海屏需-r.top，不存在刘海时需使用0(r.top有值)
    }

    @Override
    public void dismiss() {
        if (window != null) {
            window.dismiss();
        }
    }

    @Override
    public void setOnSendMessageListener(OnSendMessageListener listener) {
        this.sendMessageListener = listener;
    }

    @Override
    public void openMessageSender() {
        window.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        llSendMessage.post(new Runnable() {
            @Override
            public void run() {
                KeyboardUtils.showSoftInput(etSendMessage);
            }
        });
    }

    private void initView(View root) {
        llSendMessage = root.findViewById(R.id.ll_send_message);
        tvSendMessage = root.findViewById(R.id.tv_send_message);
        etSendMessage = root.findViewById(R.id.et_send_message);
        llSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                PLVLCLandscapeMessageSendPanel.this.onClick(view2);
            }
        });
        tvSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                PLVLCLandscapeMessageSendPanel.this.onClick(view1);
            }
        });

        etSendMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {/**/}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {/**/}

            @Override
            public void afterTextChanged(Editable s) {
                boolean enable = TextUtils.isEmpty(etSendMessage.getText());
                tvSendMessage.setEnabled(!enable);
            }
        });
        etSendMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        llSendMessage.setOnPortrait(new Runnable() {
            @Override
            public void run() {
                PLVLCLandscapeMessageSendPanel.this.hide();
            }
        });
    }

    private void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ll_send_message) {
            hide();

        } else if (i == R.id.tv_send_message) {
            sendMessage();

        }
    }

    private void sendMessage() {
        String msg = etSendMessage.getText().toString();
        if (msg.trim().length() == 0) {
            ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
            return;
        }
        etSendMessage.setText("");
        //发送消息
        if (sendMessageListener != null) {
            sendMessageListener.onSend(msg);
        }
        KeyboardUtils.hideSoftInput(etSendMessage);
        hide();
    }

    private void hide() {
        KeyboardUtils.hideSoftInput(etSendMessage);
        window.dismiss();
    }

}
