package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.streameralone.R;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

/**
 * @author suhongtao
 */
public class PLVSASettingTitleInputLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private View rootView;
    private EditText plvsaLiveRoomSettingTitleInputEt;
    private TextView plvsaLiveRoomSettingTitleLengthTv;

    // 直播标题
    private String title;

    private int usableHeightPrevious;

    private ViewTreeObserver.OnGlobalLayoutListener inputMethodLayoutListener;

    // 监听器
    private OnTitleChangeListener onTitleChangeListener;
    private OnAttachDetachListener onAttachDetachListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造函数">
    public PLVSASettingTitleInputLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSASettingTitleInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSASettingTitleInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">
    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_setting_title_input_layout, this);
        plvsaLiveRoomSettingTitleInputEt = (EditText) findViewById(R.id.plvsa_live_room_setting_title_input_et);
        plvsaLiveRoomSettingTitleLengthTv = (TextView) findViewById(R.id.plvsa_live_room_setting_title_length_tv);

        // 监听输入框文本变化
        plvsaLiveRoomSettingTitleInputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                title = s.toString().trim();
                plvsaLiveRoomSettingTitleLengthTv.setText(String.valueOf(title.length()));
                if (onTitleChangeListener != null) {
                    onTitleChangeListener.onChange(title);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 监听输入事件
        plvsaLiveRoomSettingTitleInputEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    // 点击输入法完成按钮 关闭
                    ((ViewGroup) getParent()).removeView(PLVSASettingTitleInputLayout.this);
                    return true;
                }
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // 输入回车键时，消费事件，不换行
                    return true;
                }
                return false;
            }
        });

        // 点击非输入框位置，移除直播标题输入布局
        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) getParent()).removeView(PLVSASettingTitleInputLayout.this);
            }
        });

        initInputMethodObserver();
    }

    /**
     * 监听输入法引起的高度变化 关闭输入法时关闭该输入布局
     */
    private void initInputMethodObserver() {
        post(new Runnable() {
            @Override
            public void run() {
                FrameLayout content = ((Activity) getContext()).findViewById(android.R.id.content);
                final View childOfContent = content.getChildAt(0);
                if (inputMethodLayoutListener != null) {
                    childOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(inputMethodLayoutListener);
                }
                childOfContent.getViewTreeObserver().addOnGlobalLayoutListener(inputMethodLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {//all call
                        int usableHeightNow = computeUsableHeight(childOfContent);
                        if (usableHeightPrevious != usableHeightNow) {
                            int usableHeightSansKeyboard = childOfContent.getRootView().getHeight();
                            int heightDifference = Math.abs(usableHeightSansKeyboard - usableHeightNow);
                            final ViewGroup viewGroup = (ViewGroup) PLVSASettingTitleInputLayout.this.getParent();
                            if (viewGroup != null && heightDifference <= (usableHeightNow / 4)) {
                                viewGroup.removeView(PLVSASettingTitleInputLayout.this);
                            }
                            usableHeightPrevious = usableHeightNow;
                        }
                    }
                });
            }
        });
    }

    private int computeUsableHeight(View view) {
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        //隐藏状态栏情况下才需计算
        return r.bottom;//待完善，横屏为r.bottom，竖屏时为r.bottom-r.top+navbarHeight，存在刘海屏需-r.top，不存在刘海时需使用0(r.top有值)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View父类方法重写">
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        plvsaLiveRoomSettingTitleInputEt.requestFocus();
        KeyboardUtils.showSoftInput(plvsaLiveRoomSettingTitleInputEt);

        if (onAttachDetachListener != null) {
            onAttachDetachListener.onAttach(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyboardUtils.hideSoftInput((Activity) getContext());

        if (onAttachDetachListener != null) {
            onAttachDetachListener.onDetach(this);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API 外部调用方法">
    public void initTitle(String title) {
        this.title = title;
        plvsaLiveRoomSettingTitleInputEt.setText(title);
        plvsaLiveRoomSettingTitleLengthTv.setText(String.valueOf(title.length()));
    }

    public void setOnTitleChangeListener(OnTitleChangeListener onTitleChangeListener) {
        this.onTitleChangeListener = onTitleChangeListener;
    }

    public void setOnAttachDetachListener(OnAttachDetachListener onAttachDetachListener) {
        this.onAttachDetachListener = onAttachDetachListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">
    interface OnTitleChangeListener {
        void onChange(String newTitle);
    }

    interface OnAttachDetachListener {
        void onAttach(View v);

        void onDetach(View v);
    }
    // </editor-fold>

}
