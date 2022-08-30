package com.easefun.polyv.streameralone.modules.streamer;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author suhongtao
 */
public class PLVSAStreamerFinishLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 顶部图标间距
    private static final int FINISH_ICON_MARGIN_TOP_PORT = ConvertUtils.dp2px(69);
    private static final int FINISH_ICON_MARGIN_TOP_LAND = ConvertUtils.dp2px(38);
    // 分割线间距
    private static final int SPLIT_VIEW_MARGIN_TOP_PORT = ConvertUtils.dp2px(40);
    private static final int SPLIT_VIEW_MARGIN_TOP_LAND = ConvertUtils.dp2px(18);
    // 底部确认按钮间距
    private static final int FINISH_CLOSE_BUTTON_MARGIN_BOTTOM_PORT = ConvertUtils.dp2px(119);
    private static final int FINISH_CLOSE_BUTTON_MARGIN_BOTTOM_LAND = ConvertUtils.dp2px(44);

    private View rootView;
    private ImageView plvsaStreamerFinishIv;
    private TextView plvsaStreamerFinishTv;
    private View plvsaStreamerFinishSplitView;
    private TextView plvsaStreamerFinishStartEndTimeLabelTv;
    private TextView plvsaStreamerFinishStartEndTimeTv;
    private TextView plvsaStreamerFinishStreamTimeLabelTv;
    private TextView plvsaStreamerFinishStreamTimeTv;
    private Button plvsaStreamerFinishCloseBtn;

    // 开播时长
    private int secondsSinceStartTiming;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVSAStreamerFinishLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStreamerFinishLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStreamerFinishLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvsa_streamer_finish_layout, this);
        findView();
        initOnClickCloseButtonListener();
    }

    private void findView() {
        plvsaStreamerFinishIv = (ImageView) findViewById(R.id.plvsa_streamer_finish_iv);
        plvsaStreamerFinishTv = (TextView) findViewById(R.id.plvsa_streamer_finish_tv);
        plvsaStreamerFinishSplitView = (View) findViewById(R.id.plvsa_streamer_finish_split_view);
        plvsaStreamerFinishStartEndTimeLabelTv = (TextView) findViewById(R.id.plvsa_streamer_finish_start_end_time_label_tv);
        plvsaStreamerFinishStartEndTimeTv = (TextView) findViewById(R.id.plvsa_streamer_finish_start_end_time_tv);
        plvsaStreamerFinishStreamTimeLabelTv = (TextView) findViewById(R.id.plvsa_streamer_finish_stream_time_label_tv);
        plvsaStreamerFinishStreamTimeTv = (TextView) findViewById(R.id.plvsa_streamer_finish_stream_time_tv);
        plvsaStreamerFinishCloseBtn = (Button) findViewById(R.id.plvsa_streamer_finish_close_btn);
    }

    private void initOnClickCloseButtonListener() {
        plvsaStreamerFinishCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) getContext()).finish();
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API 外部调用方法">

    public void updateSecondsSinceStartTiming(int secondsSinceStartTiming) {
        this.secondsSinceStartTiming = secondsSinceStartTiming;
    }

    public void show() {
        initStartEndTime();
        initStreamTime();
        updateViewWithOrientation();
        setVisibility(VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        MarginLayoutParams finishIconLayoutParam = (MarginLayoutParams) plvsaStreamerFinishIv.getLayoutParams();
        MarginLayoutParams splitViewLayoutParam = (MarginLayoutParams) plvsaStreamerFinishSplitView.getLayoutParams();
        MarginLayoutParams closeButtonLayoutParam = (MarginLayoutParams) plvsaStreamerFinishCloseBtn.getLayoutParams();

        if (PLVScreenUtils.isPortrait(getContext())) {
            finishIconLayoutParam.topMargin = FINISH_ICON_MARGIN_TOP_PORT;
            splitViewLayoutParam.topMargin = SPLIT_VIEW_MARGIN_TOP_PORT;
            closeButtonLayoutParam.bottomMargin = FINISH_CLOSE_BUTTON_MARGIN_BOTTOM_PORT;
        } else {
            finishIconLayoutParam.topMargin = FINISH_ICON_MARGIN_TOP_LAND;
            splitViewLayoutParam.topMargin = SPLIT_VIEW_MARGIN_TOP_LAND;
            closeButtonLayoutParam.bottomMargin = FINISH_CLOSE_BUTTON_MARGIN_BOTTOM_LAND;
        }

        plvsaStreamerFinishIv.setLayoutParams(finishIconLayoutParam);
        plvsaStreamerFinishSplitView.setLayoutParams(splitViewLayoutParam);
        plvsaStreamerFinishCloseBtn.setLayoutParams(closeButtonLayoutParam);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理 - 直播时间计算">

    private void initStartEndTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(new Date());
        int endMinute = calendar.get(Calendar.MINUTE);
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endSecond = calendar.get(Calendar.SECOND);
        calendar.add(Calendar.SECOND, -secondsSinceStartTiming);
        int startMinute = calendar.get(Calendar.MINUTE);
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startSecond = calendar.get(Calendar.SECOND);

        String startHourStr = String.format(Locale.getDefault(), "%02d", startHour);
        String startMinuteStr = String.format(Locale.getDefault(), "%02d", startMinute);
        String startSecondStr = String.format(Locale.getDefault(), "%02d", startSecond);
        String endHourStr = String.format(Locale.getDefault(), "%02d", endHour);
        String endMinuteStr = String.format(Locale.getDefault(), "%02d", endMinute);
        String endSecondStr = String.format(Locale.getDefault(), "%02d", endSecond);

        final String startEndText = startHourStr + ":" + startMinuteStr + ":" + startSecondStr + "～" + endHourStr + ":" + endMinuteStr + ":" + endSecondStr;

        plvsaStreamerFinishStartEndTimeTv.setText(startEndText);
    }

    private void initStreamTime() {
        int seconds = secondsSinceStartTiming % 60;
        int minutes = (secondsSinceStartTiming % (60 * 60)) / 60;
        int hours = (secondsSinceStartTiming % (60 * 60 * 24)) / (60 * 60);

        String secondString = String.format(Locale.getDefault(), "%02d", seconds);
        String minuteString = String.format(Locale.getDefault(), "%02d", minutes);
        String hourString = String.format(Locale.getDefault(), "%02d", hours);

        final String timingText = hourString + ":" + minuteString + ":" + secondString;

        plvsaStreamerFinishStreamTimeTv.setText(timingText);
    }

    // </editor-fold>

}
