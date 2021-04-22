package com.easefun.polyv.livestreamer.modules.chatroom.widget;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.ui.window.PLVInputWindow;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageHelper;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.chatroom.utils.PLVLSChatroomUtils;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;

import java.util.ArrayList;

/**
 * 聊天信息输入窗口
 */
public class PLVLSChatMsgInputWindow extends PLVInputWindow implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int REQUEST_SELECT_IMG = 0x01;//选择图片请求标志

    //view
    private ImageView plvlsChatroomSelEmojiIv;
    private ImageView plvlsChatroomSelImgIv;
    private TextView plvlsChatroomChatMsgSendTv;
    private EditText plvlsChatroomChatMsgInputEt;
    private ViewPager plvlsChatroomEmojiVp;
    private PLVLSEmojiIndicatorView plvlsChatroomEmojiIndicatorView;
    private ImageView plvlsChatroomMsgDeleteIv;
    private TextView plvlsChatroomMsgSendTv;
    private ViewGroup plvlsEmojiListLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMG && resultCode == Activity.RESULT_OK) {
            final Uri selectedUri = data.getData();
            if (selectedUri != null) {
                String picturePath = PLVUriPathHelper.getPrivatePath(this, selectedUri);
                sendImg(picturePath);
            } else {
                PLVToast.Builder.context(this)
                        .setText(R.string.plv_chat_cannot_retrieve_selected_image)
                        .build()
                        .show();

            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        plvlsChatroomSelEmojiIv = findViewById(R.id.plvls_chatroom_sel_emoji_iv);
        plvlsChatroomSelImgIv = findViewById(R.id.plvls_chatroom_sel_img_iv);
        plvlsChatroomChatMsgSendTv = findViewById(R.id.plvls_chatroom_chat_msg_send_tv);
        plvlsChatroomChatMsgInputEt = findViewById(R.id.plvls_chatroom_chat_msg_input_et);
        plvlsChatroomEmojiVp = findViewById(R.id.plvls_chatroom_emoji_vp);
        plvlsChatroomEmojiIndicatorView = findViewById(R.id.plvls_chatroom_emoji_indicator_view);
        plvlsChatroomMsgDeleteIv = findViewById(R.id.plvls_chatroom_msg_delete_iv);
        plvlsChatroomMsgSendTv = findViewById(R.id.plvls_chatroom_msg_send_tv);
        plvlsEmojiListLayout = findViewById(R.id.plvls_emoji_list_layout);

        plvlsChatroomSelEmojiIv.setOnClickListener(this);
        plvlsChatroomSelImgIv.setOnClickListener(this);
        plvlsChatroomMsgDeleteIv.setOnClickListener(this);
        plvlsChatroomMsgSendTv.setOnClickListener(this);
        plvlsChatroomChatMsgSendTv.setOnClickListener(this);
        plvlsChatroomChatMsgInputEt.addTextChangedListener(inputViewTextWatcher);

        if (plvlsChatroomChatMsgInputEt.getText().toString().length() > 0) {
            plvlsChatroomChatMsgSendTv.setSelected(true);
        }

        //初始化表情布局
        PLVLSChatroomUtils.initEmojiList(plvlsChatroomEmojiVp, R.layout.plvls_chatroom_chat_emoji_gridview_widget, plvlsChatroomChatMsgInputEt);
        plvlsChatroomEmojiIndicatorView.bindViewPager(plvlsChatroomEmojiVp);
        //添加弹层按钮和布局
        addPopupButton(plvlsChatroomSelEmojiIv);
        addPopupLayout(plvlsEmojiListLayout);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现父类PLVInputWindow定义的方法">
    @Override
    public boolean firstShowInput() {
        return true;
    }

    @Override
    public int layoutId() {
        return R.layout.plvls_chatroom_chat_msg_input_window;
    }

    @Override
    public int bgViewId() {
        return R.id.plvls_chatroom_chat_input_bg;
    }

    @Override
    public int inputViewId() {
        return R.id.plvls_chatroom_chat_msg_input_et;
    }

    @Override
    public boolean isHideStatusBar() {
        return true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 选择图片发送">
    private void requestSelectImg() {
        ArrayList<String> permissions = new ArrayList<>(1);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PLVFastPermission.getInstance()
                .start(this, permissions, new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        selectImg();
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                        if (deniedForeverP.size() > 0) {
                            showRequestPermissionDialog(getString(R.string.plv_chat_send_img_error_tip_permission_denied));
                        } else {
                            PLVToast.Builder.context(PLVLSChatMsgInputWindow.this)
                                    .setText(R.string.plv_chat_send_img_error_tip_permission_cancel)
                                    .build()
                                    .show();
                        }
                    }
                });
    }

    private void selectImg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.plv_chat_chooser_sel_img)), REQUEST_SELECT_IMG);
    }

    private void showRequestPermissionDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.plv_common_dialog_tip)
                .setMessage(message)
                .setPositiveButton(R.string.plv_common_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PLVFastPermission.getInstance().jump2Settings(PLVLSChatMsgInputWindow.this);
                    }
                })
                .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setCancelable(false).show();
    }

    private void sendImg(String picturePath) {
        PolyvSendLocalImgEvent sendLocalImgEvent = new PolyvSendLocalImgEvent();
        sendLocalImgEvent.setImageFilePath(picturePath);
        int[] pictureWh = PolyvSendChatImageHelper.getPictureWh(picturePath);
        sendLocalImgEvent.setWidth(pictureWh[0]);
        sendLocalImgEvent.setHeight(pictureWh[1]);

        if (inputListener instanceof MessageSendListener) {
            ((MessageSendListener) inputListener).onSendImg(sendLocalImgEvent);
        }
        requestClose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvls_chatroom_sel_emoji_iv) {
            togglePopupLayout(plvlsChatroomSelEmojiIv, plvlsEmojiListLayout);
        } else if (id == R.id.plvls_chatroom_msg_delete_iv) {
            PLVLSChatroomUtils.deleteEmoText(plvlsChatroomChatMsgInputEt);
        } else if (id == R.id.plvls_chatroom_msg_send_tv || id == R.id.plvls_chatroom_chat_msg_send_tv) {
            postMsg();
        } else if (id == R.id.plvls_chatroom_sel_img_iv) {
            selectImg();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听器">
    private TextWatcher inputViewTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                plvlsChatroomChatMsgSendTv.setSelected(true);
            } else {
                plvlsChatroomChatMsgSendTv.setSelected(false);
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 信息发送监听器">
    public interface MessageSendListener extends InputListener {
        void onSendImg(PolyvSendLocalImgEvent imgEvent);
    }
    // </editor-fold>
}
