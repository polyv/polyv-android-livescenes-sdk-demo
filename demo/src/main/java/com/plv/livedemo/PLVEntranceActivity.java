package com.plv.livedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author suhongtao
 */
public class PLVEntranceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final SparseArray<Class<? extends Activity>> MAP_VIEW_ID_TO_ACTIVITY_CLASS =
            new SparseArray<Class<? extends Activity>>() {{
                put(R.id.plv_entrance_live_streamer_btn, PLVLoginStreamerActivity.class);
                put(R.id.plv_entrance_live_cloudclass_btn, PLVLoginWatcherActivity.class);
            }};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_entrance_activity);
    }

    @Override
    public void onClick(View v) {
        Class<? extends Activity> clazz = MAP_VIEW_ID_TO_ACTIVITY_CLASS.get(v.getId());
        if (clazz != null) {
            startActivity(new Intent(this, clazz));
        }
    }

    public static class ScaleBgImageView extends AppCompatImageView {

        private final Matrix matrix = new Matrix();

        public ScaleBgImageView(Context context) {
            super(context);
        }

        public ScaleBgImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ScaleBgImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            int screenWidth = ScreenUtils.getScreenWidth();
            int drawableWidth = getDrawable().getIntrinsicWidth();
            float scale = ((float) screenWidth) / drawableWidth;

            matrix.setScale(scale, scale);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.concat(matrix);
            getDrawable().draw(canvas);
        }
    }
}
