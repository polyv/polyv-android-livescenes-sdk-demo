package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suhongtao
 */
public class PLVRoundRectGradientTextView extends AppCompatTextView {

    private static final String TAG = PLVRoundRectGradientTextView.class.getSimpleName();

    public static final int ORIENTATION_LEFT_TO_RIGHT = 0;
    public static final int ORIENTATION_BOTTOM_LEFT_TO_TOP_RIGHT = 1;
    public static final int ORIENTATION_BOTTOM_TO_TOP = 2;
    public static final int ORIENTATION_BOTTOM_RIGHT_TO_TOP_LEFT = 3;
    public static final int ORIENTATION_RIGHT_TO_LEFT = 4;
    public static final int ORIENTATION_TOP_RIGHT_TO_BOTTOM_LEFT = 5;
    public static final int ORIENTATION_TOP_TO_BOTTOM = 6;
    public static final int ORIENTATION_TOP_LEFT_TO_BOTTOM_RIGHT = 7;

    private final SparseArray<GradientDrawable.Orientation> orientationMapper = new SparseArray<GradientDrawable.Orientation>() {{
        put(ORIENTATION_LEFT_TO_RIGHT, GradientDrawable.Orientation.LEFT_RIGHT);
        put(ORIENTATION_BOTTOM_LEFT_TO_TOP_RIGHT, GradientDrawable.Orientation.BL_TR);
        put(ORIENTATION_BOTTOM_TO_TOP, GradientDrawable.Orientation.BOTTOM_TOP);
        put(ORIENTATION_BOTTOM_RIGHT_TO_TOP_LEFT, GradientDrawable.Orientation.BR_TL);
        put(ORIENTATION_RIGHT_TO_LEFT, GradientDrawable.Orientation.RIGHT_LEFT);
        put(ORIENTATION_TOP_RIGHT_TO_BOTTOM_LEFT, GradientDrawable.Orientation.TR_BL);
        put(ORIENTATION_TOP_TO_BOTTOM, GradientDrawable.Orientation.TOP_BOTTOM);
        put(ORIENTATION_TOP_LEFT_TO_BOTTOM_RIGHT, GradientDrawable.Orientation.TL_BR);
    }};

    private int orientation = ORIENTATION_LEFT_TO_RIGHT;
    private float radiusTopLeft = 0;
    private float radiusTopRight = 0;
    private float radiusBottomLeft = 0;
    private float radiusBottomRight = 0;
    private int width = 0;
    private int height = 0;
    private Path radiusPath = null;

    public PLVRoundRectGradientTextView(Context context) {
        this(context, null);
    }

    public PLVRoundRectGradientTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVRoundRectGradientTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        List<Integer> colorList = new ArrayList<>();
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVRoundRectGradientTextView);

            // Gradient color
            if (typedArray.hasValue(R.styleable.PLVRoundRectGradientTextView_plvGradientColors)) {
                parseColors(colorList, typedArray);
            } else {
                parseSimpleColor(colorList, typedArray);
            }

            // Gradient orientation
            orientation = typedArray.getInt(R.styleable.PLVRoundRectGradientTextView_plvGradientOrientation, orientation);

            // Radius
            parseRadius(typedArray);

            typedArray.recycle();
        }

        if (colorList.isEmpty()) {
            colorList.add(Color.TRANSPARENT);
        }
        if (colorList.size() == 1) {
            colorList.add(colorList.get(0));
        }

        int[] colors = new int[colorList.size()];
        for (int i = 0; i < colorList.size(); ++i) {
            colors[i] = colorList.get(i);
        }

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setOrientation(orientationMapper.get(orientation));
        gradientDrawable.setColors(colors);

        setBackground(gradientDrawable);
    }

    private void parseSimpleColor(List<Integer> colorList, TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.PLVRoundRectGradientTextView_plvGradientStartColor)) {
            int startColor = typedArray.getColor(R.styleable.PLVRoundRectGradientTextView_plvGradientStartColor, Color.TRANSPARENT);
            colorList.add(startColor);
        }
        if (typedArray.hasValue(R.styleable.PLVRoundRectGradientTextView_plvGradientMiddleColor)) {
            int middleColor = typedArray.getColor(R.styleable.PLVRoundRectGradientTextView_plvGradientMiddleColor, Color.TRANSPARENT);
            colorList.add(middleColor);
        }
        if (typedArray.hasValue(R.styleable.PLVRoundRectGradientTextView_plvGradientEndColor)) {
            int endColor = typedArray.getColor(R.styleable.PLVRoundRectGradientTextView_plvGradientEndColor, Color.TRANSPARENT);
            colorList.add(endColor);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        updateRadiusPathIfNeeded();
        if (radiusPath != null) {
            int saveCount = canvas.save();
            canvas.clipPath(radiusPath);
            super.draw(canvas);
            canvas.restoreToCount(saveCount);
        } else {
            super.draw(canvas);
        }
    }

    public void updateBackgroundColor(@ColorInt int... colors) {
        if (colors == null) {
            return;
        }
        final List<Integer> colorList = new ArrayList<>(colors.length);
        for (int color : colors) {
            colorList.add(color);
        }
        if (colorList.size() == 0) {
            colorList.add(Color.TRANSPARENT);
        }
        if (colorList.size() == 1) {
            colorList.add(colorList.get(0));
        }
        final int[] colorArray = new int[colorList.size()];
        for (int i = 0; i < colorList.size(); ++i) {
            colorArray[i] = colorList.get(i);
        }
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setOrientation(orientationMapper.get(orientation));
        gradientDrawable.setColors(colorArray);

        setBackground(gradientDrawable);
    }

    public void updateRadius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        radiusTopLeft = topLeft;
        radiusTopRight = topRight;
        radiusBottomRight = bottomRight;
        radiusBottomLeft = bottomLeft;

        if (radiusPath == null) {
            radiusPath = new Path();
        } else {
            radiusPath.reset();
        }

        radiusPath.addRoundRect(
                new RectF(0, 0, width, height),
                new float[]{radiusTopLeft, radiusTopLeft,
                        radiusTopRight, radiusTopRight,
                        radiusBottomRight, radiusBottomRight,
                        radiusBottomLeft, radiusBottomLeft},
                Path.Direction.CCW);
    }

    private void parseColors(List<Integer> colorList, TypedArray typedArray) {
        if (!typedArray.hasValue(R.styleable.PLVRoundRectGradientTextView_plvGradientColors)) {
            return;
        }
        String colorsString = typedArray.getString(R.styleable.PLVRoundRectGradientTextView_plvGradientColors);
        if (colorsString == null) {
            return;
        }
        colorsString = colorsString.replaceAll(" ", "");
        String[] colorStrArray = colorsString.split(",");

        for (String colorStr : colorStrArray) {
            try {
                colorList.add(Color.parseColor(colorStr));
            } catch (Exception e) {
                PLVCommonLog.e(TAG, e.getMessage());
            }
        }
    }

    private void parseRadius(TypedArray typedArray) {
        // Radius all
        final float radius = typedArray.getDimension(R.styleable.PLVRoundRectGradientTextView_plvRadius, 0);
        radiusTopLeft = radiusTopRight = radiusBottomLeft = radiusBottomRight = radius;
        // Radius specific single corner
        radiusTopLeft = typedArray.getDimension(R.styleable.PLVRoundRectGradientTextView_plvTopLeftRadius, radiusTopLeft);
        radiusTopRight = typedArray.getDimension(R.styleable.PLVRoundRectGradientTextView_plvTopRightRadius, radiusTopRight);
        radiusBottomLeft = typedArray.getDimension(R.styleable.PLVRoundRectGradientTextView_plvBottomLeftRadius, radiusBottomLeft);
        radiusBottomRight = typedArray.getDimension(R.styleable.PLVRoundRectGradientTextView_plvBottomRightRadius, radiusBottomRight);
    }

    private void updateRadiusPathIfNeeded() {
        if (getWidth() == width && getHeight() == height) {
            return;
        }

        width = getWidth();
        height = getHeight();

        if (radiusTopLeft == 0
                && radiusTopRight == 0
                && radiusBottomLeft == 0
                && radiusBottomRight == 0) {
            radiusPath = null;
            return;
        }

        if (radiusPath == null) {
            radiusPath = new Path();
        } else {
            radiusPath.reset();
        }

        radiusPath.addRoundRect(
                new RectF(0, 0, width, height),
                new float[]{radiusTopLeft, radiusTopLeft,
                        radiusTopRight, radiusTopRight,
                        radiusBottomRight, radiusBottomRight,
                        radiusBottomLeft, radiusBottomLeft},
                Path.Direction.CCW);
    }
}
