package com.easefun.polyv.livestreamer.modules.liveroom;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livescenes.streamer.linkmic.IPLVSLinkMicEventSender;
import com.easefun.polyv.livescenes.streamer.linkmic.PLVSLinkMicEventSender;
import com.easefun.polyv.livestreamer.R;

/**
 * 连麦开启关闭控制弹窗
 */
public class PLVLSLinkMicControlWindow implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private PopupWindow popupWindow;
    private LinearLayout plvlsLinkmicVideoTypeLy;
    private ImageView plvlsLinkmicVideoTypeIv;
    private TextView plvlsLinkmicVideoTypeTv;
    private View plvlsLinkmicControlSplitView;
    private LinearLayout plvlsLinkmicAudioTypeLy;
    private ImageView plvlsLinkmicAudioTypeIv;
    private TextView plvlsLinkmicAudioTypeTv;

    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSLinkMicControlWindow(View anchor) {
        View contentView = View.inflate(anchor.getContext(), R.layout.plvls_live_room_linkmic_control_layout, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        popupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
        popupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        plvlsLinkmicVideoTypeLy = contentView.findViewById(R.id.plvls_linkmic_video_type_ly);
        plvlsLinkmicVideoTypeIv = contentView.findViewById(R.id.plvls_linkmic_video_type_iv);
        plvlsLinkmicVideoTypeTv = contentView.findViewById(R.id.plvls_linkmic_video_type_tv);
        plvlsLinkmicControlSplitView = contentView.findViewById(R.id.plvls_linkmic_control_split_view);
        plvlsLinkmicAudioTypeLy = contentView.findViewById(R.id.plvls_linkmic_audio_type_ly);
        plvlsLinkmicAudioTypeIv = contentView.findViewById(R.id.plvls_linkmic_audio_type_iv);
        plvlsLinkmicAudioTypeTv = contentView.findViewById(R.id.plvls_linkmic_audio_type_tv);

        plvlsLinkmicVideoTypeLy.setOnClickListener(this);
        plvlsLinkmicAudioTypeLy.setOnClickListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void show(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);//window

        //在控件上方显示
        View contentView = popupWindow.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int popupWidth = contentView.getMeasuredWidth();

        int x = location[0] + view.getWidth() / 2 - popupWidth / 2;
        int y = location[1] + view.getHeight();

        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        popupWindow.setOnDismissListener(onDismissListener);
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    public void resetLinkMicControlView() {
        plvlsLinkmicVideoTypeLy.setSelected(false);
        plvlsLinkmicVideoTypeLy.setVisibility(View.VISIBLE);
        plvlsLinkmicAudioTypeLy.setSelected(false);
        plvlsLinkmicAudioTypeLy.setVisibility(View.VISIBLE);
        plvlsLinkmicControlSplitView.setVisibility(View.VISIBLE);
        plvlsLinkmicVideoTypeTv.setText("视频连麦");
        plvlsLinkmicAudioTypeTv.setText("音频连麦");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦开关控制">
    private void acceptLinkMicControl(final boolean isVideoType, final boolean isOpen) {
        if (onViewActionListener != null && onViewActionListener.isStreamerStartSuccess()) {
            boolean isOpenLinkMic = PLVSLinkMicEventSender.getInstance().openLinkMic(isVideoType, isOpen, new IPLVSLinkMicEventSender.PLVSMainCallAck() {
                @Override
                public void onCall(Object... args) {
                    updateLinkMicControlView(isVideoType, isOpen);
                }
            });
            if (!isOpenLinkMic) {
                PLVToast.Builder.context(popupWindow.getContentView().getContext())
                        .setText(R.string.plv_linkmic_error_tip_have_not_opened)
                        .build()
                        .show();
            }
        } else {
            PLVToast.Builder.context(popupWindow.getContentView().getContext())
                    .setText(R.string.plv_streamer_toast_can_not_linkmic_before_the_class)
                    .build()
                    .show();
        }
    }

    private void updateLinkMicControlView(boolean isVideoType, boolean isOpen) {
        plvlsLinkmicVideoTypeLy.setVisibility(View.GONE);
        plvlsLinkmicAudioTypeLy.setVisibility(View.GONE);
        plvlsLinkmicControlSplitView.setVisibility(View.GONE);
        if (isVideoType) {
            plvlsLinkmicVideoTypeLy.setSelected(isOpen);
            if (isOpen) {
                plvlsLinkmicVideoTypeLy.setVisibility(View.VISIBLE);
                plvlsLinkmicVideoTypeTv.setText("结束连麦");
            } else {
                resetLinkMicControlView();
            }
        } else {
            plvlsLinkmicAudioTypeLy.setSelected(isOpen);
            if (isOpen) {
                plvlsLinkmicAudioTypeLy.setVisibility(View.VISIBLE);
                plvlsLinkmicAudioTypeTv.setText("结束连麦");
            } else {
                resetLinkMicControlView();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvls_linkmic_video_type_ly) {
            acceptLinkMicControl(true, !v.isSelected());
        } else if (id == R.id.plvls_linkmic_audio_type_ly) {
            acceptLinkMicControl(false, !v.isSelected());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        /**
         * 是否推流开始成功
         *
         * @return true：成功，false：未成功
         */
        boolean isStreamerStartSuccess();
    }
    // </editor-fold>
}
