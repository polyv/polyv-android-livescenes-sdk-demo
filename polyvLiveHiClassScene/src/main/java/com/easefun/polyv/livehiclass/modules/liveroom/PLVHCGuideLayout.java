package com.easefun.polyv.livehiclass.modules.liveroom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.easefun.polyv.livehiclass.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * @author suhongtao
 */
public class PLVHCGuideLayout extends FrameLayout {

    public static final int SHOW_SUCCESS = 0;
    public static final int ALREADY_SHOW_BEFORE = 1;
    public static final int SHOW_FAIL_NOT_ENOUGH_VIEW = 2;

    private static final String KEY_SHOW_GUIDE = "key_plvhc_guide_is_shown";

    private View view;
    private PLVTriangleIndicateLayout plvhcLiveRoomGuideLinkmicLayout;
    private RelativeLayout plvhcLiveRoomGuideStartClassLayout;

    private WeakReference<View> linkMicViewWeakReference = null;
    private WeakReference<View> startClassViewWeakReference = null;

    private static final int SHOW_TYPE_NONE = 0;
    private static final int SHOW_TYPE_LINKMIC = 1;
    private static final int SHOW_TYPE_START_CLASS = 2;
    private static final int SHOW_TYPE_END = 3;
    private int currentShowType = SHOW_TYPE_NONE;
    private Path path;

    public PLVHCGuideLayout(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public PLVHCGuideLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCGuideLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_guide_layout, this);
        findView();
    }

    private void findView() {
        plvhcLiveRoomGuideLinkmicLayout = (PLVTriangleIndicateLayout) view.findViewById(R.id.plvhc_live_room_guide_linkmic_layout);
        plvhcLiveRoomGuideStartClassLayout = (RelativeLayout) view.findViewById(R.id.plvhc_live_room_guide_start_class_layout);
    }

    public void setLinkMicView(View linkMicView) {
        this.linkMicViewWeakReference = new WeakReference<>(linkMicView);
    }

    @Nullable
    public View getLinkMicView() {
        if (this.linkMicViewWeakReference == null) {
            return null;
        }
        return this.linkMicViewWeakReference.get();
    }

    public void setStartClassView(View startClassView) {
        this.startClassViewWeakReference = new WeakReference<>(startClassView);
    }

    @Nullable
    public View getStartClassView() {
        if (this.startClassViewWeakReference == null) {
            return null;
        }
        return this.startClassViewWeakReference.get();
    }

    public int showIfNeeded() {
        if (getLinkMicView() == null || getStartClassView() == null) {
            return SHOW_FAIL_NOT_ENOUGH_VIEW;
        }
        if (isShowBefore()) {
            removeFromParent();
            return ALREADY_SHOW_BEFORE;
        }

        currentShowType = SHOW_TYPE_LINKMIC;
        processShow();
        return SHOW_SUCCESS;
    }

    private void processShow() {
        if (currentShowType == SHOW_TYPE_LINKMIC) {
            processShowLinkMic();
        } else if (currentShowType == SHOW_TYPE_START_CLASS) {
            processShowStartClass();
        } else if (currentShowType == SHOW_TYPE_END) {
            setShown(true);
            removeFromParent();
        }
        final boolean showing = currentShowType > SHOW_TYPE_NONE && currentShowType < SHOW_TYPE_END;
        setWillNotDraw(!showing);
    }

    private void processShowLinkMic() {
        final View linkMicView = getLinkMicView();
        if (linkMicView == null) {
            currentShowType = SHOW_TYPE_END;
            return;
        }
        if (currentShowType > SHOW_TYPE_LINKMIC) {
            return;
        }

        if (path == null) {
            path = new Path();
        } else {
            path.reset();
        }

        linkMicView.post(new Runnable() {
            @Override
            public void run() {
                final int[] locations = new int[2];
                linkMicView.getLocationInWindow(locations);
                path.addRect(locations[0],
                        locations[1],
                        locations[0] + linkMicView.getWidth(),
                        locations[1] + linkMicView.getHeight(),
                        Path.Direction.CCW);

                plvhcLiveRoomGuideLinkmicLayout.setVisibility(VISIBLE);
                plvhcLiveRoomGuideStartClassLayout.setVisibility(GONE);

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) plvhcLiveRoomGuideLinkmicLayout.getLayoutParams();
                lp.topMargin = locations[1] + linkMicView.getHeight() + ConvertUtils.dp2px(8);
                lp.leftMargin = locations[0] - ConvertUtils.dp2px(24);
                plvhcLiveRoomGuideLinkmicLayout.setLayoutParams(lp);

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentShowType = SHOW_TYPE_START_CLASS;
                        processShow();
                    }
                });
                setVisibility(View.VISIBLE);
            }
        });
    }

    private void processShowStartClass() {
        final View startClassView = getStartClassView();
        if (startClassView == null) {
            currentShowType = SHOW_TYPE_END;
            return;
        }
        if (currentShowType > SHOW_TYPE_START_CLASS) {
            return;
        }

        if (path == null) {
            path = new Path();
        } else {
            path.reset();
        }

        startClassView.post(new Runnable() {
            @Override
            public void run() {
                final int[] locations = new int[2];
                startClassView.getLocationInWindow(locations);
                path.addCircle(locations[0] + startClassView.getWidth() / 2F,
                        locations[1] + startClassView.getHeight() / 2F,
                        startClassView.getWidth() / 2F,
                        Path.Direction.CCW);

                plvhcLiveRoomGuideLinkmicLayout.setVisibility(GONE);
                plvhcLiveRoomGuideStartClassLayout.setVisibility(VISIBLE);

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) plvhcLiveRoomGuideStartClassLayout.getLayoutParams();
                lp.topMargin = locations[1] + startClassView.getHeight() / 2 - lp.height / 2;
                lp.leftMargin = locations[0] + startClassView.getWidth() + ConvertUtils.dp2px(8) - lp.width;
                plvhcLiveRoomGuideLinkmicLayout.setLayoutParams(lp);

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentShowType = SHOW_TYPE_END;
                        processShow();
                    }
                });
                setVisibility(View.VISIBLE);
            }
        });
    }

    private void removeFromParent() {
        setVisibility(GONE);
        if (!(view.getParent() instanceof ViewGroup)) {
            return;
        }
        ((ViewGroup) view.getParent()).removeView(view);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final boolean showing = currentShowType > SHOW_TYPE_NONE && currentShowType < SHOW_TYPE_END;

        super.onDraw(canvas);
        if (path != null && showing) {
            canvas.clipPath(path, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.parseColor("#BF000000"));
        }
    }

    private static boolean isShowBefore() {
        return SPUtils.getInstance().getBoolean(KEY_SHOW_GUIDE, false);
    }

    private static void setShown(boolean isShown) {
        SPUtils.getInstance().put(KEY_SHOW_GUIDE, isShown);
    }

}
