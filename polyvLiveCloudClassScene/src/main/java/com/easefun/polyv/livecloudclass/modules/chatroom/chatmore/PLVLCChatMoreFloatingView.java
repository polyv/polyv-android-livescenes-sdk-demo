package com.easefun.polyv.livecloudclass.modules.chatroom.chatmore;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

public class PLVLCChatMoreFloatingView extends FrameLayout {
    // <editor-folder defaultstate="collapsed" desc="变量">
    private PopupWindow popupWindow;
    private PLVLCChatMoreLayout chatMoreLayout;
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="构造器">
    public PLVLCChatMoreFloatingView(Context context) {
        this(context, null);
    }

    public PLVLCChatMoreFloatingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCChatMoreFloatingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="初始化">
    private void initView() {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.plvlc_chatroom_btn_more_toggle);
        FrameLayout.LayoutParams flp = new LayoutParams(ConvertUtils.dp2px(30), ConvertUtils.dp2px(30));
        imageView.setLayoutParams(flp);
        addView(imageView);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
            }
        });

        chatMoreLayout = new PLVLCChatMoreLayout(getContext());
        chatMoreLayout.setVisibility(View.VISIBLE);
        updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_ONLY_TEACHER, false);
        updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_SEND_IMAGE, false);
        updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_OPEN_CAMERA, false);
        updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_EFFECT, false);
        chatMoreLayout.setFunctionListener(new PLVLCChatFunctionListener() {
            @Override
            public void onFunctionCallback(String type, String data) {
                switch (type) {
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_ONLY_TEACHER:
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_SEND_IMAGE:
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_OPEN_CAMERA:
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_EFFECT:
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_BULLETIN:
                        popupWindow.dismiss();
                        if (onViewActionListener != null) {
                            onViewActionListener.onShowBulletinAction();
                        }
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_LANGUAGE_SWITCH:
                        popupWindow.dismiss();
                        if (onViewActionListener != null) {
                            onViewActionListener.onShowLanguageAction();
                        }
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_SCREENSHOT:
                        popupWindow.dismiss();
                        if (onViewActionListener != null) {
                            onViewActionListener.onScreenshot();
                        }
                        break;
                    default:
                        popupWindow.dismiss();
                        if (onViewActionListener != null) {
                            onViewActionListener.onClickDynamicFunction(data);
                        }
                        break;
                }
            }
        });

        popupWindow = new PopupWindow(getContext());
        View.OnClickListener handleHideListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        };
        PLVViewInitUtils.initPopupWindow(chatMoreLayout, popupWindow, handleHideListener);
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="对外API">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (!liveRoomDataManager.getConfig().isLive()) {
            updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_BULLETIN, false);
        }
        liveRoomDataManager.getInteractStatusData().observe((LifecycleOwner) getContext(), new Observer<PLVWebviewUpdateAppStatusVO>() {
            @Override
            public void onChanged(@Nullable PLVWebviewUpdateAppStatusVO plvWebviewUpdateAppStatusVO) {
                if (plvWebviewUpdateAppStatusVO != null) {
                    updateChatMoreFunction(plvWebviewUpdateAppStatusVO);
                }
            }
        });
    }

    public void updateChatMoreFunction(PLVWebviewUpdateAppStatusVO functionsVO) {
        if (chatMoreLayout != null) {
            chatMoreLayout.updateFunctionView(functionsVO);
        }
    }

    public void updateFunctionShow(String type, boolean isShow) {
        if (chatMoreLayout != null) {
            chatMoreLayout.updateFunctionShow(type, isShow);
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        /**
         * 显示公告
         */
        void onShowBulletinAction();

        /**
         * 显示语言切换弹窗
         */
        void onShowLanguageAction();

        /**
         * 截屏
         */
        void onScreenshot();

        /**
         * 点击了动态功能控件
         *
         * @param event 动态功能的event data
         */
        void onClickDynamicFunction(String event);
    }
    // </editor-fold>
}
