package com.easefun.polyv.livecommon.ui.window;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

//need android:windowSoftInputMode="stateHidden|adjustResize"
public abstract class PLVInputWindow extends PLVBaseActivity {
    private static final String TAG = "PLVInputWindow";
    private static final int ALLOW_SHOW_INTERVAL = 1200;
    private static long lastStartTime;
    private static SpannableStringBuilder lastInputText;//editText CharSequence 持有activity引用
    private boolean isShowKeyBoard;
    private boolean willShowKeyBoard;
    private View viewBg;
    private EditText inputView;

    private List<View> popupButtonList = new ArrayList<>();
    private List<ViewGroup> popupLayoutList = new ArrayList<>();

    protected static InputListener inputListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isHideStatusBar()) {
            hideStatusBar();
        }
        setContentView(layoutId());
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        initView();
        FrameLayout content = findViewById(android.R.id.content);
        final View childOfContent = content.getChildAt(0);
        childOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {//all call, but not show viewBg
                int usableHeightNow = computeUsableHeight(childOfContent);
                int usableHeightSansKeyboard = childOfContent.getRootView().getHeight();
                int heightDifference = Math.abs(usableHeightSansKeyboard - usableHeightNow);
                if (heightDifference > (usableHeightNow / 4)) {
                    // keyboard probably just became visible
                    isShowKeyBoard = true;
                    if (willShowKeyBoard && isHideStatusBar()) {
                        hideStatusBar();
                        viewBg.setVisibility(View.VISIBLE);
                    }
                    willShowKeyBoard = false;
                    if (viewBg.getTag() == null || (((int) viewBg.getTag()) != heightDifference)) {
                        ViewGroup.LayoutParams lp = viewBg.getLayoutParams();
                        lp.height = heightDifference;
                        viewBg.setLayoutParams(lp);
                        viewBg.setTag(heightDifference);
                    }
                } else {
                    isShowKeyBoard = false;
                    if (!willShowKeyBoard) {
                        viewBg.setVisibility(View.GONE);
                    }
                }
            }
        });
        if (firstShowInput()) {
            willShowInput();
        } else {
            showPopupLayout();
        }
    }

    private int computeUsableHeight(View view) {
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        //隐藏状态栏情况下才需计算
        return r.bottom;//待完善，横屏为r.bottom，竖屏时为r.bottom-r.top+navbarHeight，存在刘海屏需-r.top，不存在刘海时需使用0(r.top有值)
    }

    public boolean isHideStatusBar() {
        return false;//待完善
    }

    private void initView() {
        viewBg = findViewById(bgViewId());
        viewBg.setVisibility(View.GONE);
        inputView = findViewById(inputViewId());
        if (!TextUtils.isEmpty(lastInputText)) {
            inputView.setText(lastInputText);
            inputView.setSelection(inputView.getText().length());
        }
        inputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                willShowKeyBoard = true;
                hideAllPopupLayout();
                if (isHideStatusBar()) {
                    viewBg.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                PLVCommonLog.d(TAG, " beforeTextChanged:" + s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    //send enabled true
                    PLVCommonLog.d(TAG, "onTextChanged: enabled true");
                } else {
                    //send enabled false
                    PLVCommonLog.d(TAG, "onTextChanged: enabled false");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                PLVCommonLog.d(TAG, " beforeTextChanged:");
            }
        });
        inputView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    postMsg();
                    return true;
                }
                return false;
            }
        });
    }

    public void hideStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiOptions = uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void willShowInput() {
        willShowKeyBoard = true;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);//用always，其他界面回到界面会显示
    }

    public void showPopupLayout() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        showPopupLayout(firstPopupView(), firstPopupLayout());
    }

    public View firstPopupView() {
        return null;
    }

    public ViewGroup firstPopupLayout() {
        return null;
    }

    public void postMsg() {
        String message = inputView.getText().toString();
        if (message.trim().length() == 0) {
            ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
        } else {
            boolean sendResult = true;
            if (inputListener != null) {
                sendResult = inputListener.onSendMsg(message);
            }
            if (sendResult) {
                inputView.setText("");
                requestClose();
            }
        }
    }

    public void backPressed() {
    }

    public abstract boolean firstShowInput();

    public abstract int layoutId();

    public abstract int bgViewId();

    public abstract int inputViewId();

    public static void show(Activity packageActivity, Class<? extends PLVInputWindow> cls, InputListener listener) {
        if (System.currentTimeMillis() - lastStartTime > ALLOW_SHOW_INTERVAL) {
            lastStartTime = System.currentTimeMillis();
            inputListener = listener;
            Intent intent = new Intent(packageActivity, cls);
            packageActivity.startActivity(intent);
            packageActivity.overridePendingTransition(0, 0);
        }
    }

    public static void show(Activity packageActivity, Intent intent, InputListener listener) {
        if (System.currentTimeMillis() - lastStartTime > ALLOW_SHOW_INTERVAL) {
            lastStartTime = System.currentTimeMillis();
            inputListener = listener;
            packageActivity.startActivity(intent);
            packageActivity.overridePendingTransition(0, 0);
        }
    }

    public void requestClose() {
        hideSoftInputAndPopupLayout();
        isShowKeyBoard = false;
        finish();
    }

    // <editor-fold defaultstate="collapsed" desc="生命周期等方法">
    @Override
    public void onBackPressed() {
        if (!isShowKeyBoard && !popupLayoutIsVisible()) {
            backPressed();
        }
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if (isShowKeyBoard || popupLayoutIsVisible()) {
            hideSoftInputAndPopupLayout();
            return;
        }
        if (inputView != null) {
            lastInputText = new SpannableStringBuilder(inputView.getText());//Spannable避免内存泄漏
        }
        inputListener = null;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.finish();
        overridePendingTransition(0, 0);//after finish
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestClose();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            hideSoftInputAndPopupLayout();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="popupLayout">
    protected void addPopupButton(View view) {
        popupButtonList.add(view);
    }

    protected void addPopupLayout(ViewGroup popupLayout) {
        popupLayoutList.add(popupLayout);
    }

    protected void togglePopupLayout(View view, ViewGroup popupLayout) {
        if (!view.isSelected()) {
            showPopupLayout(view, popupLayout);
        } else {
            hidePopupLayout(view, popupLayout);
        }
    }

    protected void showPopupLayout(final View view, final ViewGroup popupLayout) {
        willShowKeyBoard = false;
        hideAllPopupLayout();
        KeyboardUtils.hideSoftInput(inputView);
        viewBg.setVisibility(View.GONE);
        popupLayout.setVisibility(View.VISIBLE);
        view.setSelected(true);
    }

    protected void hidePopupLayout(View view, ViewGroup popupLayout) {
        popupLayout.setVisibility(View.GONE);
        view.setSelected(false);
    }

    protected boolean popupLayoutIsVisible() {
        for (ViewGroup viewGroup : popupLayoutList) {
            if (viewGroup.getVisibility() == View.VISIBLE)
                return true;
        }
        return false;
    }

    protected void hideAllPopupLayout() {
        if (popupLayoutIsVisible()) {
            for (ViewGroup viewGroup : popupLayoutList) {
                viewGroup.setVisibility(View.GONE);
            }
            for (View view : popupButtonList) {
                view.setSelected(false);
            }
        }
    }

    protected void hideSoftInputAndPopupLayout() {
        if (inputView != null) {
            willShowKeyBoard = false;
            KeyboardUtils.hideSoftInput(inputView);
            viewBg.setVisibility(View.GONE);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        hideAllPopupLayout();
    }
    // </editor-fold>

    public interface InputListener {
        boolean onSendMsg(String message);
    }
}
