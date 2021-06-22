package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suhongtao
 */
public class PLVGradientView extends View {

    private static final String TAG = PLVGradientView.class.getSimpleName();

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

    public PLVGradientView(Context context) {
        this(context, null);
    }

    public PLVGradientView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVGradientView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        List<Integer> colorList = new ArrayList<>();
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVGradientView);

            if (typedArray.hasValue(R.styleable.PLVGradientView_plvGradientColors)) {
                parseColors(colorList, typedArray);
            } else {
                parseSimpleColor(colorList, typedArray);
            }

            orientation = typedArray.getInt(R.styleable.PLVGradientView_plvGradientOrientation, orientation);

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
        if (typedArray.hasValue(R.styleable.PLVGradientView_plvGradientStartColor)) {
            int startColor = typedArray.getColor(R.styleable.PLVGradientView_plvGradientStartColor, Color.TRANSPARENT);
            colorList.add(startColor);
        }
        if (typedArray.hasValue(R.styleable.PLVGradientView_plvGradientMiddleColor)) {
            int middleColor = typedArray.getColor(R.styleable.PLVGradientView_plvGradientMiddleColor, Color.TRANSPARENT);
            colorList.add(middleColor);
        }
        if (typedArray.hasValue(R.styleable.PLVGradientView_plvGradientEndColor)) {
            int endColor = typedArray.getColor(R.styleable.PLVGradientView_plvGradientEndColor, Color.TRANSPARENT);
            colorList.add(endColor);
        }
    }

    private void parseColors(List<Integer> colorList, TypedArray typedArray) {
        if (!typedArray.hasValue(R.styleable.PLVGradientView_plvGradientColors)) {
            return;
        }
        String colorsString = typedArray.getString(R.styleable.PLVGradientView_plvGradientColors);
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
}
