package com.easefun.polyv.livecommon.ui.window;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class PLVInputFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private PLVEmptyFragment emptyFragment;
    private ViewGroup fragmentView;

    private ViewGroup inputLayout;
    private ViewGroup inputLayoutParent;
    private ViewGroup.LayoutParams inputLayoutParams;
    private EditText inputView;

    private boolean isShowKeyBoard;
    private boolean willShowKeyBoard;
    private boolean willShowPopupLayout;

    private List<View> popupButtonList = new ArrayList<>();
    private List<ViewGroup> popupLayoutList = new ArrayList<>();
    private View curShowView;
    private ViewGroup curShowLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        inputLayout = findViewById(inputLayoutId());
        inputView = findViewById(inputViewId());
        inputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideAllPopupLayout();
                moveInputLayoutToOtherWindow(true);
                return false;
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

        emptyFragment = new PLVEmptyFragment();
        emptyFragment.setViewActionListener(new PLVEmptyFragment.ViewActionListener() {
            @Override
            public void onViewCreated(View view) {
                fragmentView = (ViewGroup) view;
                fragmentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftInputAndPopupLayout();
                    }
                });
            }
        });
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(attachContainerViewId(), emptyFragment, "PLVEmptyFragment").hide(emptyFragment).commitAllowingStateLoss();

        FrameLayout content = getActivity().findViewById(android.R.id.content);
        final View childOfContent = content.getChildAt(0);
        childOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {//all call
                int usableHeightNow = computeUsableHeight(childOfContent);
                int usableHeightSansKeyboard = childOfContent.getRootView().getHeight();
                int heightDifference = Math.abs(usableHeightSansKeyboard - usableHeightNow);
                if (heightDifference > (usableHeightNow / 4)) {
                    // keyboard probably just became visible
                    isShowKeyBoard = true;
                    willShowKeyBoard = false;
                } else {
                    isShowKeyBoard = false;
                    if (onceHideKeyBoardListener != null) {
                        onceHideKeyBoardListener.call();
                        onceHideKeyBoardListener = null;
                    }
                    if (willShowPopupLayout) {
                        if (curShowLayout != null) {
                            curShowLayout.setVisibility(View.VISIBLE);
                        }
                        if (curShowView != null) {
                            curShowView.setSelected(true);
                        }
                    }
                    willShowPopupLayout = false;
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="当前是否显示键盘">
    protected boolean isShowKeyBoard(OnceHideKeyBoardListener listener) {
        this.onceHideKeyBoardListener = listener;
        if (!isShowKeyBoard) {
            onceHideKeyBoardListener = null;
        }
        return isShowKeyBoard;
    }

    private OnceHideKeyBoardListener onceHideKeyBoardListener;

    public interface OnceHideKeyBoardListener {
        void call();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="抽象方法">
    public abstract int inputLayoutId();

    public abstract int inputViewId();

    public abstract boolean onSendMsg(String message);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="输入布局附加到的parentId">
    public int attachContainerViewId() {
        return Window.ID_ANDROID_CONTENT;//windowContentTransitions need fitsSystemWindows true
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="计算可用高度">
    private int computeUsableHeight(View view) {
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        //隐藏状态栏情况下才需计算
        return r.bottom;//待完善，横屏为r.bottom，竖屏时为r.bottom-r.top+navbarHeight，存在刘海屏需-r.top，不存在刘海时需使用0(r.top有值)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="提交信息">
    private void postMsg() {
        String message = inputView.getText().toString();
        if (message.trim().length() == 0) {
            ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
        } else {
            boolean sendResult = onSendMsg(message);
            if (sendResult) {
                inputView.setText("");
                hideSoftInputAndPopupLayout();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="inputLayout">
    private void moveInputLayoutToOtherWindow(boolean isShowInput) {
        if (!emptyFragment.isVisible() && !willShowKeyBoard) {
            willShowKeyBoard = true;
            if (inputLayout.getParent() instanceof ViewGroup) {
                inputLayoutParams = inputLayout.getLayoutParams();
                inputLayoutParent = (ViewGroup) inputLayout.getParent();//touch editText onTouch, use !willShowKeyBoard
                ((ViewGroup) inputLayout.getParent()).removeView(inputLayout);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.BOTTOM;
                inputLayout.setLayoutParams(lp);
                fragmentView.addView(inputLayout);
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.show(emptyFragment).commitAllowingStateLoss();
            if (isShowInput) {
                KeyboardUtils.showSoftInput(inputView);
            }
        }
        if (!isShowInput) {
            willShowKeyBoard = false;
            KeyboardUtils.hideSoftInput(inputView);
        }
    }

    private void moveInputLayoutToSrcWindow() {
        if (inputLayout.getParent().equals(fragmentView)) {
            willShowKeyBoard = false;
            fragmentView.removeView(inputLayout);
            inputLayout.setLayoutParams(inputLayoutParams);
            inputLayoutParent.addView(inputLayout);

            KeyboardUtils.hideSoftInput(inputView);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(emptyFragment).commitAllowingStateLoss();
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
        moveInputLayoutToOtherWindow(false);
        if (isShowKeyBoard) {//关闭键盘之后才显示，避免布局抖动
            willShowPopupLayout = true;
            curShowView = view;
            curShowLayout = popupLayout;
        } else {
            popupLayout.setVisibility(View.VISIBLE);
            view.setSelected(true);
            inputView.requestFocus();//显示光标
        }
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
        moveInputLayoutToSrcWindow();
        hideAllPopupLayout();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="是否拦截返回事件">
    public boolean onBackPressed() {
        if (popupLayoutIsVisible()) {
            hideAllPopupLayout();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="横竖屏切换">
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideSoftInputAndPopupLayout();
    }
    // </editor-fold>
}
