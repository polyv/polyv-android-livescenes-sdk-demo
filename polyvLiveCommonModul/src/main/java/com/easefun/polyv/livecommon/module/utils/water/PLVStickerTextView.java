package com.easefun.polyv.livecommon.module.utils.water;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.easefun.polyv.livecommon.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

public class PLVStickerTextView extends FrameLayout implements IPLVToggleView {
    private LinearLayout contentLayout;
    private ImageView ivLeftIcon;
    private PLVStrokeTextView tvContent;
    private ImageView ivDelete;
    private ImageView ivEdit;
    private int styleType = 1;
    private boolean mBorderVisible = true;
    private boolean mBorderVisibleTwo = false;
    private OnToggleBorderListener mOnToggleBorderListener;
    private String text = "";
    // save
    private boolean hasSave = false;
    private String saveText = "";
    private int saveStyle = 1;

    public PLVStickerTextView(Context context) {
        this(context, null);
    }

    public PLVStickerTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVStickerTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mOnToggleBorderListener != null && mOnToggleBorderListener.isSettingFinished()) {
            return;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                // 获取父控件
                ViewGroup parent = (ViewGroup) getParent();
                if (parent == null) {
                    return;
                }
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                // 获取当前View的布局参数和尺寸
                ViewGroup.LayoutParams params = getLayoutParams();
                if (params == null) {
                    return;
                }
                int viewWidth = getWidth();
                int viewHeight = getHeight();
                if (viewWidth == 0 || viewHeight == 0) {
                    measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                    viewWidth = getMeasuredWidth();
                    viewHeight = getMeasuredHeight();
                }

                // 计算当前宽高比（假设宽高不为0）
                float ratio = (viewHeight != 0) ? (float) viewWidth / viewHeight : 1f;
                boolean needResize = false;

                // 如果View宽度超出父控件，则将宽度设置为父控件宽度，同时等比计算高度
                if (viewWidth > parentWidth) {
                    viewWidth = parentWidth;
                    viewHeight = (int) (viewWidth / ratio);
                    needResize = true;
                }
                // 如果View高度超出父控件，则将高度设置为父控件高度，同时等比计算宽度
                if (viewHeight > parentHeight) {
                    viewHeight = parentHeight;
                    viewWidth = (int) (viewHeight * ratio);
                    needResize = true;
                }
                if (needResize) {
                    params.width = viewWidth;
                    params.height = viewHeight;
                    setLayoutParams(params);
                }

                setX((parentWidth - viewWidth) / 2f);
                setY((parentHeight - viewHeight) / 2f);
            }
        }, 100);
    }

    private void init() {
        setWillNotDraw(false);
        LayoutInflater.from(getContext()).inflate(R.layout.plv_sticker_text_view, this, true);
        contentLayout = findViewById(R.id.plv_sticker_text_content_layout);
        ivLeftIcon = findViewById(R.id.plv_sticker_text_left_icon);
        tvContent = findViewById(R.id.plv_sticker_text_content);
        ivDelete = findViewById(R.id.plv_sticker_text_delete);
        ivEdit = findViewById(R.id.plv_sticker_text_edit);

        setStyle(styleType);
        ivDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromParent();
            }
        });
        ivEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });
    }

    public void setStyle(int style) {
        this.styleType = style;
        ivLeftIcon.setVisibility(GONE);
        ivLeftIcon.setTranslationY(0);
        tvContent.setStyle(styleType);
        tvContent.setLetterSpacing(0f);
        switch (style) {
            case 1:
            case 2:
                tvContent.setTextSize(18);
                break;
            case 3:
            case 4:
                tvContent.setTextSize(16);
                tvContent.setLetterSpacing(0.04f);
                break;
            case 5:
                tvContent.setTextSize(18);
                break;
            case 6:
                tvContent.setTextSize(18);
                tvContent.setLetterSpacing(0.04f);
                break;
            case 7:
                ivLeftIcon.setVisibility(VISIBLE);
                ivLeftIcon.setImageResource(R.drawable.plv_sticker_seven_ic);
                break;
            case 8:
                ivLeftIcon.setVisibility(VISIBLE);
                ivLeftIcon.setImageResource(R.drawable.plv_sticker_eight_ic);
                ivLeftIcon.setTranslationY(-ConvertUtils.dp2px(4));
                break;
        }
    }

    public void setText(String text, int style) {
        this.text = text;
        tvContent.setText(text);
        setStyle(style);

        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup parent = (ViewGroup) getParent();
                if (parent == null) return;
                int parentWidth = parent.getWidth();
                float curX = getX();
                int contentRight = (int) (curX + getWidth());
                int maxRight = parentWidth;
                float newX = curX;
                if (contentRight > maxRight) {
                    float offset = contentRight - maxRight;
                    newX = curX - offset;
                }
                setX(Math.max(0, newX));
            }
        });
    }

    public String getText() {
        return text;
    }

    public int getStyle() {
        return styleType;
    }

    public void saveStatus() {
        saveText = text;
        saveStyle = styleType;
        hasSave = true;
    }

    public void restoreStatus() {
        text = saveText;
        styleType = saveStyle;
        setText(text, styleType);
    }

    public boolean isHasSave() {
        return hasSave;
    }

    public void toggleBorder(boolean show) {
        mBorderVisibleTwo = mBorderVisible && show;
        mBorderVisible = show;
        contentLayout.setBackgroundResource(show ? R.drawable.plv_sticker_text_border_bg : 0);
        if (!show) {
            ivDelete.setVisibility(View.INVISIBLE);
            ivEdit.setVisibility(View.INVISIBLE);
        }
        if (mOnToggleBorderListener != null) {
            mOnToggleBorderListener.onToggleBorder(show, this);
        }
    }

    public boolean isBorderVisible() {
        return mBorderVisible;
    }

    @Override
    public void onClick(float upX, float upY) {
        if (ivEdit.getVisibility() == View.VISIBLE && mBorderVisibleTwo) {
            tryShowEditDialog();
        }
        if (mBorderVisibleTwo) {
            ivDelete.setVisibility(View.VISIBLE);
            ivEdit.setVisibility(View.VISIBLE);
            if (mOnToggleBorderListener != null) {
                mOnToggleBorderListener.onShouldShowTextSelectLayout();
            }
        }
    }

    @Override
    public Rect getExtraPadding() {
        return new Rect(0, 0, 0, 0);
    }

    public void removeFromParent() {
        if (getParent() instanceof FrameLayout) {
            ((FrameLayout) getParent()).removeView(this);
        }
        if (mOnToggleBorderListener != null) {
            mOnToggleBorderListener.onRemoveFromParent();
        }
    }

    public void tryShowEditDialog() {
        if (ivEdit.getVisibility() == VISIBLE) {
            showEditDialog();
        }
    }

    @Override
    public View getCanScaleView() {
        return contentLayout;
    }

    public void setOnToggleBorderListener(OnToggleBorderListener listener) {
        mOnToggleBorderListener = listener;
    }

    public boolean isEditMode() {
        return mOnToggleBorderListener != null && mOnToggleBorderListener.isEditMode();
    }

    private void showEditDialog() {
        PLVStickerTextInputWindow.show((Activity) getContext(), PLVStickerTextInputWindow.class, new PLVStickerTextInputWindow.InputWindowListener() {
            final String beforeText = text;
            boolean isSend = false;
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                if (mOnToggleBorderListener != null) {
                    mOnToggleBorderListener.onShowInputWindow(true);
                }
            }

            @Override
            public void onSoftKeyboardClosed(boolean isFinished) {
                if (mOnToggleBorderListener != null && isFinished) {
                    mOnToggleBorderListener.onShowInputWindow(false);
                    if (!isSend) {
                        setText(beforeText, styleType);
                    }
                }
            }

            @Override
            public boolean onSendMsg(String message) {
                setText(message, styleType);
                isSend = true;
                return true;
            }

            @Override
            public void onInputContext(PLVStickerTextInputWindow inputWindow) {
                if (inputWindow != null) {
                    inputWindow.setInputText(text);
                }
            }

            @Override
            public void afterTextChanged(String s) {
                if (!isSend) {
                    setText(s, styleType);
                }
            }
        });
    }

    public interface OnToggleBorderListener {
        void onToggleBorder(boolean show, View view);

        void onRemoveFromParent();

        boolean isEditMode();

        boolean isSettingFinished();

        void onShowInputWindow(boolean isShow);

        void onShouldShowTextSelectLayout();
    }
}