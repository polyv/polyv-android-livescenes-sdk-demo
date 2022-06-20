package com.easefun.polyv.livecommon.ui.widget.textview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVShadowTextView extends AppCompatTextView {

    private float shadowRadius = ConvertUtils.dp2px(0.5F);
    private int shadowColor = PLVFormatUtils.parseColor("#66000000");

    public PLVShadowTextView(Context context) {
        super(context);
        initView();
    }

    public PLVShadowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVShadowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setShadowLayer(shadowRadius, 0, 0, shadowColor);
        super.onDraw(canvas);
    }
}
