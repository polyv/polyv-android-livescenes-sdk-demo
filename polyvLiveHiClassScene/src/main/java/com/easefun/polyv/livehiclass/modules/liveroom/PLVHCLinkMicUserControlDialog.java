package com.easefun.polyv.livehiclass.modules.liveroom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.linkmic.zoom.PLVHCLinkMicZoomManager;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 连麦成员控制对话框
 */
public class PLVHCLinkMicUserControlDialog implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private Dialog dialog;
    private View view;
    private TextView plvhcLinkmicNickTv;
    private ImageView plvhcLinkmicMicIv;
    private LinearLayout plvhcLinkmicControlMicLayout;
    private ImageView plvhcLinkmicCameraIv;
    private LinearLayout plvhcLinkmicControlCameraLayout;
    private ImageView plvhcLinkmicPaintIv;
    private TextView plvhcLinkmicPaintTv;
    private LinearLayout plvhcLinkmicControlPaintLayout;
    private ImageView plvhcLinkmicCupIv;
    private TextView plvhcLinkmicCupTv;
    private LinearLayout plvhcLinkmicControlCupLayout;
    private ImageView plvhcLinkmicCameraOrientIv;
    private TextView plvhcLinkmicCameraOrientTv;
    private LinearLayout plvhcLinkmicControlCameraOrientLayout;
    private ImageView plvhcLinkmicZoomIv;
    private TextView plvhcLinkmicZoomTv;
    private LinearLayout plvhcLinkmicControlZoomLayout;
    private TextView plvhcLinkmicCloseTv;
    private CircleImageView plvhcLinkmicAvatarIv;

    private String linkMicUid;

    //listener
    private OnViewActionListener onViewActionListener;

    private long lastClickCameraSwitchViewTime;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCLinkMicUserControlDialog(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.plvhc_live_room_linkmic_user_control_layout, null, false);

        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        plvhcLinkmicNickTv = findViewById(R.id.plvhc_linkmic_nick_tv);
        plvhcLinkmicMicIv = findViewById(R.id.plvhc_linkmic_mic_iv);
        plvhcLinkmicControlMicLayout = findViewById(R.id.plvhc_linkmic_control_mic_layout);
        plvhcLinkmicCameraIv = findViewById(R.id.plvhc_linkmic_camera_iv);
        plvhcLinkmicControlCameraLayout = findViewById(R.id.plvhc_linkmic_control_camera_layout);
        plvhcLinkmicPaintIv = findViewById(R.id.plvhc_linkmic_paint_iv);
        plvhcLinkmicPaintTv = findViewById(R.id.plvhc_linkmic_paint_tv);
        plvhcLinkmicControlPaintLayout = findViewById(R.id.plvhc_linkmic_control_paint_layout);
        plvhcLinkmicCupIv = findViewById(R.id.plvhc_linkmic_cup_iv);
        plvhcLinkmicCupTv = findViewById(R.id.plvhc_linkmic_cup_tv);
        plvhcLinkmicControlCupLayout = findViewById(R.id.plvhc_linkmic_control_cup_layout);
        plvhcLinkmicCameraOrientIv = findViewById(R.id.plvhc_linkmic_camera_orient_iv);
        plvhcLinkmicCameraOrientTv = findViewById(R.id.plvhc_linkmic_camera_orient_tv);
        plvhcLinkmicControlCameraOrientLayout = findViewById(R.id.plvhc_linkmic_control_camera_orient_layout);
        plvhcLinkmicZoomIv = findViewById(R.id.plvhc_linkmic_zoom_iv);
        plvhcLinkmicZoomTv = findViewById(R.id.plvhc_linkmic_zoom_tv);
        plvhcLinkmicControlZoomLayout = findViewById(R.id.plvhc_linkmic_control_zoom_layout);
        plvhcLinkmicCloseTv = findViewById(R.id.plvhc_linkmic_close_tv);
        plvhcLinkmicAvatarIv = findViewById(R.id.plvhc_linkmic_avatar_iv);

        plvhcLinkmicMicIv.setOnClickListener(this);
        plvhcLinkmicCameraIv.setOnClickListener(this);
        plvhcLinkmicPaintIv.setOnClickListener(this);
        plvhcLinkmicCupIv.setOnClickListener(this);
        plvhcLinkmicCameraOrientIv.setOnClickListener(this);
        plvhcLinkmicCloseTv.setOnClickListener(this);
        plvhcLinkmicZoomIv.setOnClickListener(this);
    }

    private <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public void bindViewData(PLVLinkMicItemDataBean linkMicItemDataBean, boolean isMySelf, boolean isLeader) {
        if (linkMicItemDataBean == null) {
            return;
        }
        linkMicUid = linkMicItemDataBean.getLinkMicId();
        final boolean isUserZoomIn = PLVHCLinkMicZoomManager.getInstance().isZoomIn(linkMicUid);
        final boolean canZoomIn = PLVHCLinkMicZoomManager.getInstance().canZoomInItem();
        final boolean showZoomLayout = !isLeader && (isUserZoomIn || canZoomIn);
        plvhcLinkmicControlPaintLayout.setVisibility(isMySelf ? View.GONE : View.VISIBLE);
        plvhcLinkmicControlCupLayout.setVisibility((isMySelf || isLeader) ? View.GONE : View.VISIBLE);
        plvhcLinkmicControlCameraOrientLayout.setVisibility((isMySelf && !linkMicItemDataBean.isMuteVideo()) ? View.VISIBLE : View.GONE);
        plvhcLinkmicControlZoomLayout.setVisibility(showZoomLayout ? View.VISIBLE : View.GONE);
        //头像
        String pic = linkMicItemDataBean.getPic();
        int drawableId = linkMicItemDataBean.isTeacher() ? R.drawable.plvhc_chatroom_ic_teacher : R.drawable.plvhc_chatroom_ic_viewer;
        PLVImageLoader.getInstance().loadImageNoDiskCache(
                view.getContext(),
                pic,
                drawableId,
                drawableId,
                plvhcLinkmicAvatarIv
        );
        //昵称
        plvhcLinkmicNickTv.setText(linkMicItemDataBean.getNick());
        //媒体状态
        plvhcLinkmicMicIv.setSelected(linkMicItemDataBean.isMuteAudio());
        plvhcLinkmicCameraIv.setSelected(linkMicItemDataBean.isMuteVideo());
        plvhcLinkmicPaintIv.setSelected(linkMicItemDataBean.isHasPaint());

        plvhcLinkmicZoomIv.setSelected(isUserZoomIn);
        plvhcLinkmicZoomTv.setText(isUserZoomIn ? "恢复原位" : "放大窗口");
    }

    public String getLinkMicUid() {
        return linkMicUid;
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.dismiss();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvhc_linkmic_close_tv) {
            hide();
        } else if (id == R.id.plvhc_linkmic_mic_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickMic(v.isSelected());
            }
        } else if (id == R.id.plvhc_linkmic_camera_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickCamera(v.isSelected());
            }
        } else if (id == R.id.plvhc_linkmic_paint_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickPaint(!v.isSelected());
            }
        } else if (id == R.id.plvhc_linkmic_cup_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickCup();
            }
            hide();
        } else if (id == R.id.plvhc_linkmic_camera_orient_iv) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickCameraSwitchViewTime > 500) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClickCameraOrient();
                }
                lastClickCameraSwitchViewTime = currentTime;
                hide();
            }
        } else if (id == plvhcLinkmicZoomIv.getId()) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickZoom();
                hide();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onClickCamera(boolean isWillOpen);

        void onClickMic(boolean isWillOpen);

        void onClickPaint(boolean isHasPaint);

        void onClickCup();

        void onClickCameraOrient();

        void onClickZoom();
    }
    // </editor-fold>
}
