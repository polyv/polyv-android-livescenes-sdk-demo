package com.easefun.polyv.livestreamer.modules.statusbar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerNetworkStatusDetailLayout;
import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerNetworkStatusLayout;
import com.easefun.polyv.livestreamer.R;

/**
 * @author Hoshiiro
 */
public class PLVLSStreamerNetworkStatusLayout extends PLVStreamerNetworkStatusLayout {

    public PLVLSStreamerNetworkStatusLayout(@NonNull Context context) {
        super(context);
    }

    public PLVLSStreamerNetworkStatusLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVLSStreamerNetworkStatusLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    @Override
    protected PLVStreamerNetworkStatusDetailLayout createDetailLayout(@NonNull Context context) {
        return new DetailLayout(context);
    }

    private static class DetailLayout extends PLVStreamerNetworkStatusDetailLayout {

        public DetailLayout(@NonNull Context context) {
            super(context);
        }

        public DetailLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public DetailLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected int layoutId() {
            return R.layout.plvls_streamer_network_status_detail_layout;
        }
    }
}
