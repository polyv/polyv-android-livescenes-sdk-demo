package com.plv.livedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;

import com.plv.livedemo.hiclass.PLVLoginHiClassActivity;

/**
 * @author suhongtao
 */
public class PLVEntranceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final SparseArray<Class<? extends Activity>> MAP_VIEW_ID_TO_ACTIVITY_CLASS =
            new SparseArray<Class<? extends Activity>>() {{
                put(R.id.plv_entrance_live_streamer_btn, PLVLoginStreamerActivity.class);
                put(R.id.plv_entrance_live_cloudclass_btn, PLVLoginWatcherActivity.class);
                put(R.id.plv_entrance_hi_class_btn, PLVLoginHiClassActivity.class);
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

}
