package com.easefun.polyv.streameralone.modules.liveroom;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * 成员控制弹窗
 */
public class PLVSAMemberControlWindow implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //window
    private PopupWindow popupWindow;
    //view
    private ImageView plvsaMemberCameraIv;
    private TextView plvsaMemberCameraTv;
    private ImageView plvsaMemberMicIv;
    private TextView plvsaMemberMicTv;
    private ImageView plvsaMemberKickIv;
    private TextView plvsaMemberKickTv;
    private ImageView plvsaMemberBanIv;
    private TextView plvsaMemberBanTv;
    private View plvsaMemberBottomTriangleView;
    private View plvsaMemberTopTriangleView;
    private ImageView plvsaMemberGrantSpeakerIv;
    private TextView plvsaMemberGrantSpeakerTv;
    //window parentView
    private View windowParentView;
    //data
    private int position;
    private String userId;
    //listener
    private OnViewActionListener onViewActionListener;

    private boolean isNeedPermissionDialogShow = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAMemberControlWindow(View anchor) {
        View contentView = View.inflate(anchor.getContext(), R.layout.plvsa_live_room_member_control_layout, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        popupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
        popupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        plvsaMemberCameraIv = (ImageView) contentView.findViewById(R.id.plvsa_member_camera_iv);
        plvsaMemberCameraTv = (TextView) contentView.findViewById(R.id.plvsa_member_camera_tv);
        plvsaMemberMicIv = (ImageView) contentView.findViewById(R.id.plvsa_member_mic_iv);
        plvsaMemberMicTv = (TextView) contentView.findViewById(R.id.plvsa_member_mic_tv);
        plvsaMemberKickIv = (ImageView) contentView.findViewById(R.id.plvsa_member_kick_iv);
        plvsaMemberKickTv = (TextView) contentView.findViewById(R.id.plvsa_member_kick_tv);
        plvsaMemberBanIv = (ImageView) contentView.findViewById(R.id.plvsa_member_ban_iv);
        plvsaMemberBanTv = (TextView) contentView.findViewById(R.id.plvsa_member_ban_tv);
        plvsaMemberGrantSpeakerIv = contentView.findViewById(R.id.plvsa_member_grant_speaker_iv);
        plvsaMemberGrantSpeakerTv = contentView.findViewById(R.id.plvsa_member_grant_speaker_tv);
        plvsaMemberBottomTriangleView = contentView.findViewById(R.id.plvsa_member_bottom_triangle_view);
        plvsaMemberTopTriangleView = contentView.findViewById(R.id.plvsa_member_top_triangle_view);

        plvsaMemberCameraIv.setOnClickListener(this);
        plvsaMemberCameraTv.setOnClickListener(this);
        plvsaMemberMicIv.setOnClickListener(this);
        plvsaMemberMicTv.setOnClickListener(this);
        plvsaMemberKickIv.setOnClickListener(this);
        plvsaMemberKickTv.setOnClickListener(this);
        plvsaMemberBanIv.setOnClickListener(this);
        plvsaMemberBanTv.setOnClickListener(this);
        plvsaMemberGrantSpeakerIv.setOnClickListener(this);
        plvsaMemberGrantSpeakerTv.setOnClickListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void bindData(String userId, int pos) {
        this.position = pos;
        this.userId = userId;
    }

    public void show(View view, boolean isRTCJoin, boolean isOpenCamera, boolean isOpenMic, boolean isBan, boolean isSpecialType, boolean isGuest, boolean isHasSpeaker, PLVSocketUserBean speakerUser) {
        this.windowParentView = view;
        int mediaViewVisibility = isRTCJoin ? View.VISIBLE : View.GONE;
        plvsaMemberCameraIv.setVisibility(mediaViewVisibility);
        plvsaMemberCameraTv.setVisibility(mediaViewVisibility);
        plvsaMemberMicIv.setVisibility(mediaViewVisibility);
        plvsaMemberMicTv.setVisibility(mediaViewVisibility);
        plvsaMemberGrantSpeakerTv.setVisibility(mediaViewVisibility);
        plvsaMemberGrantSpeakerIv.setVisibility(mediaViewVisibility);

        int kickBanViewVisibility = isSpecialType ? View.GONE : View.VISIBLE;
        plvsaMemberKickIv.setVisibility(kickBanViewVisibility);
        plvsaMemberKickTv.setVisibility(kickBanViewVisibility);
        plvsaMemberBanIv.setVisibility(kickBanViewVisibility);
        plvsaMemberBanTv.setVisibility(kickBanViewVisibility);

        plvsaMemberCameraIv.setSelected(!isOpenCamera);
        plvsaMemberMicIv.setSelected(!isOpenMic);
        plvsaMemberBanIv.setSelected(isBan);
        plvsaMemberBanTv.setText(isBan ? "取消禁言" : "禁言");

        //主讲权限
        int speakerViewVisibility = isGuest ? View.VISIBLE : View.GONE;
        plvsaMemberGrantSpeakerTv.setVisibility(speakerViewVisibility);
        plvsaMemberGrantSpeakerIv.setVisibility(speakerViewVisibility);
        plvsaMemberGrantSpeakerIv.setSelected(isHasSpeaker);
        plvsaMemberGrantSpeakerTv.setSelected(isHasSpeaker);
        plvsaMemberGrantSpeakerTv.setText(isHasSpeaker ? "移除主讲权限" : "授予主讲权限");
        if(isHasSpeaker){
            //FIXME 屏幕共享中应该提示，目前没有状态判断
            isNeedPermissionDialogShow = false;
        } else {
            isNeedPermissionDialogShow = speakerUser != null;
        }

        final int maxScreenLength = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        final int minScreenLength = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        final boolean isPortrait = PLVScreenUtils.isPortrait(view.getContext());

        int screenHeight = isPortrait ? maxScreenLength : minScreenLength;
        int screenWidth = isPortrait ? minScreenLength : maxScreenLength;

        View contentView = popupWindow.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int contentViewHeight = contentView.getMeasuredHeight();
        int contentViewWidth = contentView.getMeasuredWidth();

        int[] location = new int[2];
        view.getLocationInWindow(location);//window

        boolean isSideUp = contentViewHeight + location[1] + view.getHeight() > screenHeight;
        if (isSideUp) {
            plvsaMemberTopTriangleView.setVisibility(View.GONE);
            plvsaMemberBottomTriangleView.setVisibility(View.VISIBLE);
        } else {
            plvsaMemberTopTriangleView.setVisibility(View.VISIBLE);
            plvsaMemberBottomTriangleView.setVisibility(View.GONE);
        }
        int contentViewX = screenWidth - ConvertUtils.dp2px(8) - contentViewWidth;
        int contentViewY = isSideUp ? (location[1] - contentViewHeight) : (location[1] + view.getHeight());
        int triangleX = location[0] + view.getWidth() / 2 - contentViewX - ConvertUtils.dp2px(8);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) plvsaMemberBottomTriangleView.getLayoutParams();
        marginLayoutParams.leftMargin = triangleX;
        if (isSideUp) {
            plvsaMemberBottomTriangleView.setLayoutParams(marginLayoutParams);
        } else {
            plvsaMemberTopTriangleView.setLayoutParams(marginLayoutParams);
        }

        if (popupWindow.isShowing()) {
            popupWindow.update(contentViewX, contentViewY, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, contentViewX, contentViewY);
        }
    }

    public void update(boolean isRTCJoin, boolean isOpenCamera, boolean isOpenMic, boolean isBan, boolean isSpecialType,boolean isGuest, boolean isHasSpeaker, PLVSocketUserBean speakerUser) {
        if (windowParentView == null) {
            return;
        }
        show(windowParentView, isRTCJoin, isOpenCamera, isOpenMic, isBan, isSpecialType, isGuest, isHasSpeaker, speakerUser);
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public int getPosition() {
        return position;
    }

    public String getUserId() {
        return userId;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvsa_member_camera_iv
                || id == R.id.plvsa_member_camera_tv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickCamera(plvsaMemberCameraIv.isSelected());
            }
            popupWindow.dismiss();
        } else if (id == R.id.plvsa_member_mic_iv
                || id == R.id.plvsa_member_mic_tv) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickMic(plvsaMemberMicIv.isSelected());
            }
            popupWindow.dismiss();
        } else if (id == R.id.plvsa_member_kick_iv
                || id == R.id.plvsa_member_kick_tv) {
            popupWindow.dismiss();
            new PLVSAConfirmDialog(v.getContext())
                    .setTitle("确定踢出" + (onViewActionListener != null ? onViewActionListener.getNick() : "") + "吗？")
                    .setContent("踢出后24小时内无法进入")
                    .setLeftButtonText("取消")
                    .setRightButtonText("确定")
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            if (onViewActionListener != null) {
                                onViewActionListener.onClickKick();
                            }
                        }
                    })
                    .show();
        } else if (id == R.id.plvsa_member_ban_iv
                || id == R.id.plvsa_member_ban_tv) {
            popupWindow.dismiss();
            if (!plvsaMemberBanIv.isSelected()) {
                new PLVSAConfirmDialog(v.getContext())
                        .setTitle("确定禁言" + (onViewActionListener != null ? onViewActionListener.getNick() : "") + "吗？")
                        .setContentVisibility(View.GONE)
                        .setLeftButtonText("取消")
                        .setRightButtonText("确定")
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                if (onViewActionListener != null) {
                                    onViewActionListener.onClickBan(true);
                                }
                            }
                        })
                        .show();
            } else {
                if (onViewActionListener != null) {
                    onViewActionListener.onClickBan(false);
                }
            }
        } else if (id == R.id.plvsa_member_grant_speaker_iv
                || id == R.id.plvsa_member_grant_speaker_tv){
            popupWindow.dismiss();
            boolean isGrant = plvsaMemberGrantSpeakerIv.isSelected();
            if(!isNeedPermissionDialogShow){
                if (onViewActionListener != null) {
                    onViewActionListener.onClickGrantSpeaker(!plvsaMemberGrantSpeakerIv.isSelected());
                }
                plvsaMemberGrantSpeakerIv.setSelected(!plvsaMemberGrantSpeakerIv.isSelected());
                plvsaMemberGrantSpeakerTv.setSelected(!plvsaMemberGrantSpeakerIv.isSelected());
                plvsaMemberGrantSpeakerTv.setText(!plvsaMemberGrantSpeakerIv.isSelected() ? "移除主讲权限" : "授予主讲权限");
                return;
            }
            String title = isGrant ? "确定移除ta的":"确定授予ta" ;
            String content = isGrant ? "移除后主讲人的屏幕共享将会自动结束":"当前已有主讲人，确认后将替换为新的主讲人";
            new PLVSAConfirmDialog(v.getContext())
                    .setTitle(title+ "主讲权限吗？")
                    .setContent(content)
                    .setLeftButtonText("取消")
                    .setRightButtonText("确定")
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            if (onViewActionListener != null) {
                                onViewActionListener.onClickGrantSpeaker(!plvsaMemberGrantSpeakerIv.isSelected());
                            }
                            plvsaMemberGrantSpeakerIv.setSelected(!plvsaMemberGrantSpeakerIv.isSelected());
                            plvsaMemberGrantSpeakerTv.setSelected(!plvsaMemberGrantSpeakerIv.isSelected());
                            plvsaMemberGrantSpeakerTv.setText(!plvsaMemberGrantSpeakerIv.isSelected() ? "移除主讲权限" : "授予主讲权限");

                        }
                    })
                    .show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onClickCamera(boolean isWillOpen);

        void onClickMic(boolean isWillOpen);

        void onClickKick();

        void onClickBan(boolean isWillBan);

        void onClickGrantSpeaker(boolean isGrant);

        String getNick();
    }
    // </editor-fold>
}
