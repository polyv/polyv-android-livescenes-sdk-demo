package com.easefun.polyv.streameralone.modules.liveroom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.streameralone.R;
import com.plv.livescenes.streamer.linkmic.IPLVLinkMicEventSender;
import com.plv.livescenes.streamer.linkmic.PLVLinkMicEventSender;

/**
 * 连麦开启关闭控制弹窗
 */
public class PLVSALinkMicControlWindow implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private PopupWindow popupWindow;
    private TextView linkmicVideoTypeTv;
    private View linkmicControlSplitView;
    private TextView linkmicAudioTypeTv;

    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSALinkMicControlWindow(Context context) {
        View contentView = View.inflate(context, R.layout.plvsa_live_room_linkmic_control_layout, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        linkmicVideoTypeTv = contentView.findViewById(R.id.plvsa_linkmic_video_type_tv);
        linkmicControlSplitView = contentView.findViewById(R.id.plvsa_linkmic_control_split_view);
        linkmicAudioTypeTv = contentView.findViewById(R.id.plvsa_linkmic_audio_type_tv);

        linkmicVideoTypeTv.setOnClickListener(this);
        linkmicAudioTypeTv.setOnClickListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void show(View anchor) {
        final int[] location = new int[2];
        anchor.getLocationInWindow(location);

        final View contentView = popupWindow.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final int popupWidth = contentView.getMeasuredWidth();
        final int popupHeight = contentView.getMeasuredHeight();

        final int x = location[0] + anchor.getWidth() / 2 - popupWidth / 2;
        final int y = location[1] - popupHeight;

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    public void acceptLinkMicControl(final boolean isVideoType, final boolean isOpen) {
        if (onViewActionListener != null && onViewActionListener.isStreamerStartSuccess()) {
            boolean isOpenLinkMic = PLVLinkMicEventSender.getInstance().openLinkMic(isVideoType, isOpen, new IPLVLinkMicEventSender.PLVSMainCallAck() {
                @Override
                public void onCall(Object... args) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onLinkMicMediaTypeUpdate(isVideoType, isOpen);
                    }
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

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == linkmicVideoTypeTv.getId()) {
            acceptLinkMicControl(true, true);
            popupWindow.dismiss();
        } else if (id == linkmicAudioTypeTv.getId()) {
            acceptLinkMicControl(false, true);
            popupWindow.dismiss();
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

        /**
         * 更新连麦媒体类型
         *
         * @param isVideoLinkMicType true：视频类型，false：音频类型
         * @param isOpen             true：打开连麦，false：关闭连麦
         */
        void onLinkMicMediaTypeUpdate(boolean isVideoLinkMicType, boolean isOpen);
    }
    // </editor-fold>
}
