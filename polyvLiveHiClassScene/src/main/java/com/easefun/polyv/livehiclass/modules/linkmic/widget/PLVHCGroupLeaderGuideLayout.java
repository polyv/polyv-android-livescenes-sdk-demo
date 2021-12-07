package com.easefun.polyv.livehiclass.modules.linkmic.widget;

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

import com.easefun.polyv.livehiclass.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

/**
 * 分组组长指引布局
 */
public class PLVHCGroupLeaderGuideLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String KEY_REQUEST_HELP_GUIDE = "key_plvhc_guide_request_help";
    private static final String KEY_CANCEL_HELP_GUIDE = "key_plvhc_guide_cancel_help";
    //view
    private RelativeLayout plvhcGroupLeaderCancelHelpGuideLayout;
    private RelativeLayout plvhcGroupLeaderRequestHelpGuideLayout;

    private Path path;
    private boolean isShow;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCGroupLeaderGuideLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCGroupLeaderGuideLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCGroupLeaderGuideLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_linkmic_group_leader_guide_layout, this);

        plvhcGroupLeaderCancelHelpGuideLayout = (RelativeLayout) findViewById(R.id.plvhc_group_leader_cancel_help_guide_layout);
        plvhcGroupLeaderRequestHelpGuideLayout = (RelativeLayout) findViewById(R.id.plvhc_group_leader_request_help_guide_layout);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void hide() {
        isShow = false;
        setWillNotDraw(true);
        setVisibility(GONE);
    }

    public void showRequestHelpGuide(final View view) {
        if (isRequestHelpGuideSaved()) {
            return;
        }
        saveRequestHelpGuide();
        plvhcGroupLeaderCancelHelpGuideLayout.setVisibility(View.GONE);
        show(view, plvhcGroupLeaderRequestHelpGuideLayout);
    }

    public void showCancelHelpGuide(final View view) {
        if (isCancelHelpGuideSaved()) {
            return;
        }
        saveCancelHelpGuide();
        plvhcGroupLeaderRequestHelpGuideLayout.setVisibility(View.GONE);
        show(view, plvhcGroupLeaderCancelHelpGuideLayout);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写 - View方法">
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (path != null && isShow) {
            canvas.clipPath(path, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.parseColor("#BF000000"));
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void show(final View anchorView, final ViewGroup guideLayout) {
        anchorView.post(new Runnable() {
            @Override
            public void run() {
                if (path == null) {
                    path = new Path();
                } else {
                    path.reset();
                }
                final int[] locations = new int[2];
                anchorView.getLocationInWindow(locations);
                path.addCircle(locations[0] + anchorView.getWidth() / 2F,
                        locations[1] + anchorView.getHeight() / 2F,
                        anchorView.getWidth() / 2F,
                        Path.Direction.CCW);

                guideLayout.setVisibility(VISIBLE);

                MarginLayoutParams lp = (MarginLayoutParams) guideLayout.getLayoutParams();
                lp.topMargin = locations[1] + anchorView.getHeight() / 2 - lp.height / 2;
                lp.leftMargin = locations[0] + anchorView.getWidth() + ConvertUtils.dp2px(8) - lp.width;
                guideLayout.setLayoutParams(lp);

                setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hide();
                    }
                });
                isShow = true;
                setWillNotDraw(false);
                setVisibility(View.VISIBLE);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="保存指引状态到本地">
    private void saveRequestHelpGuide() {
        SPUtils.getInstance().put(KEY_REQUEST_HELP_GUIDE, true);
    }

    private boolean isRequestHelpGuideSaved() {
        return SPUtils.getInstance().getBoolean(KEY_REQUEST_HELP_GUIDE, false);
    }

    private void saveCancelHelpGuide() {
        SPUtils.getInstance().put(KEY_CANCEL_HELP_GUIDE, true);
    }

    private boolean isCancelHelpGuideSaved() {
        return SPUtils.getInstance().getBoolean(KEY_CANCEL_HELP_GUIDE, false);
    }
    // </editor-fold>
}
