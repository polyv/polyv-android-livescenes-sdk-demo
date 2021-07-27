package com.plv.streameralone.modules.chatroom.widget;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.plv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.plv.livecommon.module.utils.PLVToast;
import com.plv.livecommon.module.utils.PLVUriPathHelper;
import com.plv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.plv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.plv.livecommon.ui.window.PLVInputWindow;
import com.plv.livescenes.chatroom.send.img.PLVSendChatImageHelper;
import com.plv.livescenes.chatroom.send.img.PLVSendLocalImgEvent;
import com.plv.streameralone.R;
import com.plv.streameralone.modules.chatroom.utils.PLVSAChatroomUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;

/**
 * 聊天信息输入窗口
 */
public class PLVSAChatMsgInputWindow extends PLVInputWindow implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int REQUEST_SELECT_IMG = 0x01;//选择图片请求标志
    public static final String ANSWER_USER_NAME = "answer_user_name";
    public static final String ANSWER_USER_CONTENT = "answer_user_content";
    public static final String ANSWER_USER_IMG_URL = "answer_user_img_url";
    public static final String ANSWER_USER_IMG_WIDTH = "answer_user_img_width";
    public static final String ANSWER_USER_IMG_HEIGHT = "answer_user_img_height";

    //params
    private String answerUserName;
    private String answerUserContent;
    private String answerUserImgUrl;
    private double answerUserImgWidth;
    private double answerUserImgHeight;

    //view
    private ImageView plvsaChatroomSelEmojiIv;
    private ImageView plvsaChatroomSelImgIv;
    private EditText plvsaChatroomChatMsgInputEt;
    private RecyclerView plvsaChatroomEmojiRv;
    private ImageView plvsaChatroomMsgDeleteIv;
    private TextView plvsaChatroomMsgSendTv;
    private ViewGroup plvsaEmojiListLayout;
    private RelativeLayout plvsaChatroomAnswerLy;
    private TextView plvsaChatroomAnswerUserContentTv;
    private ImageView plvsaChatroomAnswerUserImgIv;
    private TextView plvsaChatroomCloseAnswerWindowTv;
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
            } else {
            }
        }
    };

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParams();
        initView();
    }
    // </editor-fold>

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

    // <editor-fold defaultstate="collapsed" desc="初始化参数">
    private void initParams() {
        Intent intent = getIntent();
        answerUserName = intent.getStringExtra(ANSWER_USER_NAME);
        answerUserContent = intent.getStringExtra(ANSWER_USER_CONTENT);
        answerUserImgUrl = intent.getStringExtra(ANSWER_USER_IMG_URL);
        answerUserImgWidth = intent.getDoubleExtra(ANSWER_USER_IMG_WIDTH, 0);
        answerUserImgHeight = intent.getDoubleExtra(ANSWER_USER_IMG_HEIGHT, 0);
        if (answerUserImgWidth == 0) {
            answerUserImgWidth = ConvertUtils.dp2px(40);
        }
        if (answerUserImgHeight == 0) {
            answerUserImgHeight = ConvertUtils.dp2px(40);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        plvsaChatroomSelEmojiIv = findViewById(R.id.plvsa_chatroom_sel_emoji_iv);
        plvsaChatroomSelImgIv = findViewById(R.id.plvsa_chatroom_sel_img_iv);
        plvsaChatroomChatMsgInputEt = findViewById(R.id.plvsa_chatroom_chat_msg_input_et);
        plvsaChatroomEmojiRv = findViewById(R.id.emoji_rv);
        plvsaChatroomMsgDeleteIv = findViewById(R.id.delete_msg_iv);
        plvsaChatroomMsgSendTv = findViewById(R.id.send_msg_tv);
        plvsaEmojiListLayout = findViewById(R.id.plvsa_emoji_list_layout);
        plvsaChatroomAnswerLy = findViewById(R.id.plvsa_chatroom_answer_ly);
        plvsaChatroomAnswerUserContentTv = findViewById(R.id.plvsa_chatroom_answer_user_content_tv);
        plvsaChatroomAnswerUserImgIv = findViewById(R.id.plvsa_chatroom_answer_user_img_iv);
        plvsaChatroomCloseAnswerWindowTv = findViewById(R.id.plvsa_chatroom_close_answer_window_tv);


        plvsaChatroomSelEmojiIv.setOnClickListener(this);
        plvsaChatroomSelImgIv.setOnClickListener(this);
        plvsaChatroomMsgDeleteIv.setOnClickListener(this);
        plvsaChatroomMsgSendTv.setOnClickListener(this);
        plvsaChatroomCloseAnswerWindowTv.setOnClickListener(this);
        plvsaChatroomChatMsgInputEt.addTextChangedListener(inputViewTextWatcher);

        plvsaChatroomChatMsgInputEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    postMsgWithAnswer();
                    return true;
                }
                return false;
            }
        });

        //初始化表情布局
        PLVSAChatroomUtils.initEmojiList(plvsaChatroomEmojiRv, plvsaChatroomChatMsgInputEt);
        //添加弹层按钮和布局
        addPopupButton(plvsaChatroomSelEmojiIv);
        addPopupLayout(plvsaEmojiListLayout);
        //初始化回复布局
        if (!TextUtils.isEmpty(answerUserContent)) {
            plvsaChatroomAnswerLy.setVisibility(View.VISIBLE);
            SpannableStringBuilder nickSpan = new SpannableStringBuilder(answerUserName + "：");
            CharSequence charSequenceContent = PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(answerUserContent), ConvertUtils.dp2px(12), Utils.getApp());
            plvsaChatroomAnswerUserContentTv.setText(nickSpan.append(charSequenceContent));
        } else if (!TextUtils.isEmpty(answerUserImgUrl)) {
            plvsaChatroomAnswerLy.setVisibility(View.VISIBLE);
            plvsaChatroomAnswerUserImgIv.setVisibility(View.VISIBLE);
            plvsaChatroomAnswerUserContentTv.setText(answerUserName + "：");
            PLVChatMessageBaseViewHolder.fitChatImgWH((int) answerUserImgWidth, (int) answerUserImgHeight, plvsaChatroomAnswerUserImgIv, 40, 0);
            PLVImageLoader.getInstance().loadImage(answerUserImgUrl, plvsaChatroomAnswerUserImgIv);
        } else {
            plvsaChatroomAnswerLy.setVisibility(View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现父类PLVInputWindow定义的方法">
    @Override
    public boolean firstShowInput() {
        return true;
    }

    @Override
    public int layoutId() {
        return R.layout.plvsa_chatroom_chat_msg_input_window;
    }

    @Override
    public int bgViewId() {
        return R.id.plvsa_chatroom_chat_input_bg;
    }

    @Override
    public int inputViewId() {
        return R.id.plvsa_chatroom_chat_msg_input_et;
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
                            PLVToast.Builder.context(PLVSAChatMsgInputWindow.this)
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
                        PLVFastPermission.getInstance().jump2Settings(PLVSAChatMsgInputWindow.this);
                    }
                })
                .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setCancelable(false).show();
    }

    private void sendImg(String picturePath) {
        PLVSendLocalImgEvent sendLocalImgEvent = new PLVSendLocalImgEvent();
        sendLocalImgEvent.setImageFilePath(picturePath);
        int[] pictureWh = PLVSendChatImageHelper.getPictureWh(picturePath);
        sendLocalImgEvent.setWidth(pictureWh[0]);
        sendLocalImgEvent.setHeight(pictureWh[1]);

        if (inputListener instanceof MessageSendListener) {
            ((MessageSendListener) inputListener).onSendImg(sendLocalImgEvent);
        }
        requestClose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 提交信息">
    private void postMsgWithAnswer() {
        if (plvsaChatroomAnswerLy.getVisibility() != View.VISIBLE) {
            postMsg();
        } else {
            String message = plvsaChatroomChatMsgInputEt.getText().toString();
            if (message.trim().length() == 0) {
                ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
            } else {
                boolean sendResult = true;
                if (inputListener instanceof MessageSendListener) {
                    sendResult = ((MessageSendListener) inputListener).onSendQuoteMsg(message);
                }
                if (sendResult) {
                    plvsaChatroomChatMsgInputEt.setText("");
                    requestClose();
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvsa_chatroom_sel_emoji_iv) {
            togglePopupLayout(plvsaChatroomSelEmojiIv, plvsaEmojiListLayout);
        } else if (id == R.id.delete_msg_iv) {
            PLVSAChatroomUtils.deleteEmoText(plvsaChatroomChatMsgInputEt);
        } else if (id == R.id.send_msg_tv) {
            postMsgWithAnswer();
        } else if (id == R.id.plvsa_chatroom_sel_img_iv) {
            selectImg();
        } else if (id == R.id.plvsa_chatroom_close_answer_window_tv) {
            plvsaChatroomAnswerLy.setVisibility(View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 信息发送监听器">
    public interface MessageSendListener extends InputListener {
        void onSendImg(PLVSendLocalImgEvent imgEvent);

        boolean onSendQuoteMsg(String message);
    }
    // </editor-fold>
}
