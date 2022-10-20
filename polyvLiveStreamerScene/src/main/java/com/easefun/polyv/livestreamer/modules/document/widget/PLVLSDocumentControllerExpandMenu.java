package com.easefun.polyv.livestreamer.modules.document.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.ui.widget.expandmenu.utils.DpOrPxUtils;
import com.easefun.polyv.livestreamer.R;

/**
 * 自定义横向展开菜单栏
 *
 * @see com.easefun.polyv.livecommon.ui.widget.expandmenu.widget.PLVHorizontalExpandMenu
 */
public class PLVLSDocumentControllerExpandMenu extends RelativeLayout {

    private Context mContext;
    private AttributeSet mAttrs;

    private Path path;
    private Paint buttonIconPaint;//按钮icon画笔
    private ExpandMenuAnim anim;

    private int defaultWidth;//默认宽度
    private int defaultHeight;//默认长度
    private int viewWidth;
    private int viewHeight;
    private float backPathWidth;//绘制子View区域宽度
    private float maxBackPathWidth;//绘制子View区域最大宽度
    private int menuLeft;//menu区域left值
    private int menuRight;//menu区域right值

    private int menuBackColor;//菜单栏背景色
    private float menuStrokeSize;//菜单栏边框线的size
    private int menuStrokeColor;//菜单栏边框线的颜色
    private float menuCornerRadius;//菜单栏圆角半径

    private float buttonIconDegrees;//按钮icon符号竖线的旋转角度
    private float buttonIconSize;//按钮icon符号的大小
    private float buttonIconStrokeWidth;//按钮icon符号的粗细
    private int buttonIconColor;//按钮icon颜色

    private int buttonStyle;//按钮类型
    private int buttonRadius;//按钮矩形区域内圆半径
    private float buttonTop;//按钮矩形区域top值
    private float buttonBottom;//按钮矩形区域bottom值

    private Point rightButtonCenter;//右按钮中点
    private float rightButtonLeft;//右按钮矩形区域left值
    private float rightButtonRight;//右按钮矩形区域right值

    private Point leftButtonCenter;//左按钮中点
    private float leftButtonLeft;//左按钮矩形区域left值
    private float leftButtonRight;//左按钮矩形区域right值

    private boolean isExpand;//菜单是否展开，默认为展开
    private boolean isAnimEnd;//动画是否结束
    private float downX = -1;
    private float downY = -1;
    private int expandAnimTime;//展开收起菜单的动画时间

    private View childView;

    /**
     * 根按钮所在位置，默认为右边
     */
    public static final int Right = 0;
    public static final int Left = 1;

    private int leftIconId;
    private Bitmap leftIconBitmap;
    private int leftIconExpandedId;
    private Bitmap leftIconExpandedBitmap;

    private int rightIconId;
    private Bitmap rightIconBitmap;
    private int rightIconExpandedId;
    private Bitmap rightIconExpandedBitmap;

    private OnFoldExpandListener onFoldExpandListener;
    private OnIconClickListener onIconClickListener;

    public PLVLSDocumentControllerExpandMenu(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public PLVLSDocumentControllerExpandMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mAttrs = attrs;
        init();
    }

    private void init() {
        TypedArray typedArray = mContext.obtainStyledAttributes(mAttrs, R.styleable.PLVLSDocumentControllerExpandMenu);
        defaultWidth = DpOrPxUtils.dip2px(mContext, 200);
        defaultHeight = DpOrPxUtils.dip2px(mContext, 32);

        menuBackColor = typedArray.getColor(R.styleable.PLVLSDocumentControllerExpandMenu_backgroundColor, Color.WHITE);
        menuStrokeSize = typedArray.getDimension(R.styleable.PLVLSDocumentControllerExpandMenu_strokeSize, 1);
        menuStrokeColor = typedArray.getColor(R.styleable.PLVLSDocumentControllerExpandMenu_strokeColor, Color.GRAY);
        menuCornerRadius = typedArray.getDimension(R.styleable.PLVLSDocumentControllerExpandMenu_cornerRadius, DpOrPxUtils.dip2px(mContext, 20));

        buttonStyle = typedArray.getInteger(R.styleable.PLVLSDocumentControllerExpandMenu_btnStyle, Right);
        buttonIconDegrees = 90;
        buttonIconSize = typedArray.getDimension(R.styleable.PLVLSDocumentControllerExpandMenu_buttonIconSize, DpOrPxUtils.dip2px(mContext, 8));
        buttonIconStrokeWidth = typedArray.getDimension(R.styleable.PLVLSDocumentControllerExpandMenu_buttonIconStrokeWidth, 8);
        buttonIconColor = typedArray.getColor(R.styleable.PLVLSDocumentControllerExpandMenu_buttonIconColor, Color.GRAY);

        expandAnimTime = typedArray.getInteger(R.styleable.PLVLSDocumentControllerExpandMenu_expandTime, 200);

        leftIconId = typedArray.getResourceId(R.styleable.PLVLSDocumentControllerExpandMenu_leftIconId, 0);
        leftIconExpandedId = typedArray.getResourceId(R.styleable.PLVLSDocumentControllerExpandMenu_leftIconExpandId, 0);
        rightIconId = typedArray.getResourceId(R.styleable.PLVLSDocumentControllerExpandMenu_rightIconId, 0);
        rightIconExpandedId = typedArray.getResourceId(R.styleable.PLVLSDocumentControllerExpandMenu_rightIconExpandId, 0);

        typedArray.recycle();

        isExpand = true;
        isAnimEnd = false;

        buttonIconPaint = new Paint();
        buttonIconPaint.setColor(buttonIconColor);
        buttonIconPaint.setStyle(Paint.Style.STROKE);
        buttonIconPaint.setStrokeWidth(buttonIconStrokeWidth);
        buttonIconPaint.setAntiAlias(true);

        path = new Path();
        leftButtonCenter = new Point();
        rightButtonCenter = new Point();
        anim = new ExpandMenuAnim();
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimEnd = true;
                requestLayout();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void setRightIconResId(@DrawableRes int rightIconId) {
        if (rightIconId != 0) {
            this.rightIconId = rightIconId;
            rightIconBitmap = BitmapFactory.decodeResource(getResources(), rightIconId);
        }
    }

    public void close() {
        if (isExpand) {
            expandMenu(0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        if (wMode == MeasureSpec.EXACTLY && hMode == MeasureSpec.EXACTLY) {
            buttonRadius = hSize / 2;
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize - buttonRadius * 2 - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
            measureChildren(childWidthMeasureSpec, heightMeasureSpec);

            setMeasuredDimension(wSize, hSize);
        } else if (wMode == MeasureSpec.AT_MOST && hMode == MeasureSpec.EXACTLY) {
            buttonRadius = hSize / 2;
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize - buttonRadius * 2 - getPaddingLeft() - getPaddingRight(), MeasureSpec.AT_MOST);
            measureChildren(childWidthMeasureSpec, heightMeasureSpec);

            int realWidth = getChildAt(0).getMeasuredWidth() + buttonRadius * 2 + getPaddingLeft() + getPaddingRight();
            setMeasuredDimension(realWidth, hSize);
        } else if (wMode == MeasureSpec.EXACTLY && hMode == MeasureSpec.AT_MOST) {
            buttonRadius = defaultHeight / 2;
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize - buttonRadius * 2 - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(defaultHeight, MeasureSpec.AT_MOST);
            measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);

            setMeasuredDimension(wSize, defaultHeight);
        } else {
            buttonRadius = defaultHeight / 2;
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize - buttonRadius * 2 - getPaddingLeft() - getPaddingRight(), MeasureSpec.AT_MOST);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(defaultHeight, MeasureSpec.AT_MOST);
            measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);

            int realWidth = getChildAt(0).getMeasuredWidth() + buttonRadius * 2 + getPaddingLeft() + getPaddingRight();
            setMeasuredDimension(realWidth, defaultHeight);
        }

        viewHeight = getMeasuredHeight();
        viewWidth = getMeasuredWidth();
        layoutRootButton();

        maxBackPathWidth = viewWidth - buttonRadius * 2;
        backPathWidth = maxBackPathWidth;

        //布局代码中如果没有设置background属性则在此处添加一个背景
        if (getBackground() == null) {
            setMenuBackground();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        menuLeft = getLeft();
        menuRight = getRight();

        if (getChildCount() > 0) {
            childView = getChildAt(0);
            if (isExpand) {
                if (buttonStyle == Right) {
                    childView.layout(getPaddingLeft(), (int) buttonTop, (int) rightButtonLeft, (int) buttonBottom);
                } else {
                    childView.layout((int) (leftButtonRight), (int) buttonTop, (int) rightButtonRight, (int) buttonBottom);
                }
            } else {
                childView.setVisibility(GONE);
            }
        }
        if (getChildCount() > 1) {//限制直接子View的数量
            throw new IllegalStateException("HorizontalExpandMenu can host only one direct child");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;//当menu的宽度改变时，重新给viewWidth赋值
        if (isAnimEnd) {//防止出现动画结束后菜单栏位置大小测量错误的bug
            if (buttonStyle == Right) {
                if (!isExpand) {
//                    layout((int)(menuRight - buttonRadius *2-backPathWidth),getTop(), menuRight,getBottom());
                    layout((getRight() - buttonRadius * 2), getTop(), getRight(), getBottom());
                }
            } else {
                if (!isExpand) {
//                    layout(menuLeft,getTop(),(int)(menuLeft + buttonRadius *2+backPathWidth),getBottom());
                    layout(getLeft(), getTop(), (getLeft() + buttonRadius * 2), getBottom());
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        layoutRootButton();
        if (buttonStyle == Right) {
            drawRightIcon(canvas);
        } else {
            drawLeftIcon(canvas);
        }

        super.onDraw(canvas);//注意父方法在最后调用，以免icon被遮盖
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (backPathWidth == maxBackPathWidth || backPathWidth == 0) {//动画结束时按钮才生效
                    switch (buttonStyle) {
                        case Right:
                            if (x == downX && y == downY && y >= buttonTop && y <= buttonBottom && x >= rightButtonLeft && x <= rightButtonRight) {
                                expandMenu(expandAnimTime);
                                if (onIconClickListener != null) {
                                    onIconClickListener.onClick();
                                }
                            }
                            break;
                        case Left:
                            if (x == downX && y == downY && y >= buttonTop && y <= buttonBottom && x >= leftButtonLeft && x <= leftButtonRight) {
                                expandMenu(expandAnimTime);
                                if (onIconClickListener != null) {
                                    onIconClickListener.onClick();
                                }
                            }
                            break;
                    }
                }
                break;
        }
        return true;
    }

    private class ExpandMenuAnim extends Animation {
        public ExpandMenuAnim() {
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float left = menuRight - buttonRadius * 2;//按钮在右边，菜单收起时按钮区域left值
            float right = menuLeft + buttonRadius * 2;//按钮在左边，菜单收起时按钮区域right值
            if (childView != null) {
                childView.setVisibility(GONE);
            }
            if (isExpand) {//打开菜单
                backPathWidth = maxBackPathWidth * interpolatedTime;
                buttonIconDegrees = 90 * interpolatedTime;

                if (backPathWidth == maxBackPathWidth) {
                    if (childView != null) {
                        childView.setVisibility(VISIBLE);
                    }
                }
            } else {//关闭菜单
                backPathWidth = maxBackPathWidth - maxBackPathWidth * interpolatedTime;
                buttonIconDegrees = 90 - 90 * interpolatedTime;
            }
            if (buttonStyle == Right) {
                layout((int) (left - backPathWidth), getTop(), menuRight, getBottom());//会调用onLayout重新测量子View位置
            } else {
                layout(menuLeft, getTop(), (int) (right + backPathWidth), getBottom());
            }
            postInvalidate();
        }
    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    /**
     * 设置菜单背景，如果要显示阴影，需在onLayout之前调用
     */
    private void setMenuBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(menuBackColor);
        gd.setStroke((int) menuStrokeSize, menuStrokeColor);
        gd.setCornerRadius(menuCornerRadius);
        setBackground(gd);
    }

    /**
     * 测量按钮中点和矩形位置
     */
    private void layoutRootButton() {
        buttonTop = 0;
        buttonBottom = viewHeight;

        rightButtonCenter.x = viewWidth - buttonRadius - getPaddingRight();
        rightButtonCenter.y = viewHeight / 2;
        rightButtonLeft = rightButtonCenter.x - buttonRadius;
        rightButtonRight = rightButtonCenter.x + buttonRadius;

        leftButtonCenter.x = getPaddingLeft() + buttonRadius;
        leftButtonCenter.y = viewHeight / 2;
        leftButtonLeft = leftButtonCenter.x - buttonRadius;
        leftButtonRight = leftButtonCenter.x + buttonRadius;
    }

    /**
     * 绘制左边的按钮
     *
     * @param canvas
     */
    private void drawLeftIcon(Canvas canvas) {
        Bitmap bitmap = null;
        if (isExpand) {
            if (leftIconExpandedId != 0) {
                // 展开 有声明左侧展开状态图标
                if (leftIconExpandedBitmap == null) {
                    leftIconExpandedBitmap = BitmapFactory.decodeResource(getResources(), leftIconExpandedId);
                }
                bitmap = leftIconExpandedBitmap;
            } else if (leftIconId != 0) {
                // 展开 未声明左侧展开状态图标 用左侧图标替代
                if (leftIconBitmap == null) {
                    leftIconBitmap = BitmapFactory.decodeResource(getResources(), leftIconId);
                }
                bitmap = leftIconBitmap;
            }
        } else {
            if (leftIconId != 0) {
                // 未展开 有声明左侧图标
                if (leftIconBitmap == null) {
                    leftIconBitmap = BitmapFactory.decodeResource(getResources(), leftIconId);
                }
                bitmap = leftIconBitmap;
            }
        }

        if (bitmap != null) {
            Rect destRect = new Rect(0, 0, (int) leftButtonRight, (int) buttonBottom);
            canvas.drawBitmap(bitmap, null, destRect, buttonIconPaint);
        } else {
            path.reset();
            path.moveTo(leftButtonCenter.x - buttonIconSize, leftButtonCenter.y);
            path.lineTo(leftButtonCenter.x + buttonIconSize, leftButtonCenter.y);
            canvas.drawPath(path, buttonIconPaint);//划横线

            canvas.save();
            canvas.rotate(-buttonIconDegrees, leftButtonCenter.x, leftButtonCenter.y);//旋转画布，让竖线可以随角度旋转
            path.reset();
            path.moveTo(leftButtonCenter.x, leftButtonCenter.y - buttonIconSize);
            path.lineTo(leftButtonCenter.x, leftButtonCenter.y + buttonIconSize);
            canvas.drawPath(path, buttonIconPaint);//画竖线
            canvas.restore();
        }
    }

    /**
     * 绘制右边的按钮
     *
     * @param canvas
     */
    private void drawRightIcon(Canvas canvas) {
        Bitmap bitmap = null;
        if (isExpand) {
            if (rightIconExpandedId != 0) {
                // 展开 有声明右侧展开状态图标
                if (rightIconExpandedBitmap == null) {
                    rightIconExpandedBitmap = BitmapFactory.decodeResource(getResources(), rightIconExpandedId);
                }
                bitmap = rightIconExpandedBitmap;
            } else if (rightIconId != 0) {
                // 展开 未声明右侧展开状态图标 用右侧图标替代
                if (rightIconBitmap == null) {
                    rightIconBitmap = BitmapFactory.decodeResource(getResources(), rightIconId);
                }
                bitmap = rightIconBitmap;
            }
        } else {
            if (rightIconId != 0) {
                // 未展开 有声明右侧图标
                if (rightIconBitmap == null) {
                    rightIconBitmap = BitmapFactory.decodeResource(getResources(), rightIconId);
                }
                bitmap = rightIconBitmap;
            }
        }

        if (bitmap != null) {
            Rect destRect = new Rect((int) rightButtonLeft, 0, (int) rightButtonRight, (int) buttonBottom);
            canvas.drawBitmap(bitmap, null, destRect, buttonIconPaint);
        } else {
            path.reset();
            path.moveTo(rightButtonCenter.x - buttonIconSize, rightButtonCenter.y);
            path.lineTo(rightButtonCenter.x + buttonIconSize, rightButtonCenter.y);
            canvas.drawPath(path, buttonIconPaint);//划横线

            canvas.save();
            canvas.rotate(buttonIconDegrees, rightButtonCenter.x, rightButtonCenter.y);//旋转画布，让竖线可以随角度旋转
            path.reset();
            path.moveTo(rightButtonCenter.x, rightButtonCenter.y - buttonIconSize);
            path.lineTo(rightButtonCenter.x, rightButtonCenter.y + buttonIconSize);
            canvas.drawPath(path, buttonIconPaint);//画竖线
            canvas.restore();
        }
    }

    /**
     * 展开收起菜单
     *
     * @param time 动画时间
     */
    private void expandMenu(int time) {
        anim.setDuration(time);
        isExpand = !isExpand;
        this.startAnimation(anim);
        isAnimEnd = false;

        if (onFoldExpandListener != null) {
            onFoldExpandListener.onFoldExpand(isExpand);
        }
    }

    public void setOnFoldExpandListener(OnFoldExpandListener onFoldExpandListener) {
        this.onFoldExpandListener = onFoldExpandListener;
    }

    public void setOnIconClickListener(OnIconClickListener onIconClickListener) {
        this.onIconClickListener = onIconClickListener;
    }

    public interface OnFoldExpandListener {
        void onFoldExpand(boolean isExpand);
    }

    public interface OnIconClickListener {
        void onClick();
    }
}
