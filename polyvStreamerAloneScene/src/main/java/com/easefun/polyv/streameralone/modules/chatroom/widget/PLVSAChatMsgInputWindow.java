package com.easefun.polyv.streameralone.modules.chatroom.widget;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVImagePreviewPopupWindow;
import com.easefun.polyv.livecommon.ui.window.PLVInputWindow;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageHelper;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.PLVSAEmotionPersonalListAdapter;
import com.easefun.polyv.streameralone.modules.chatroom.utils.PLVSAChatroomUtils;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

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

    private boolean isInitEmotion = false;


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
    private ImageView plvsaEmojiTabEmojiIv;
    private ImageView plvsaEmojiTabPersonalIv;
    private RecyclerView emojiPersonalRv;
    @Nullable
    private TextView plvsaChatroomChatMsgSendTvLand;

    //个性化表情预览弹窗
    private PLVImagePreviewPopupWindow emotionPreviewWindow;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParams();
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

    private void inflateView() {
        ViewStub chatMsgInputViewStub;
        if (PLVScreenUtils.isPortrait(this)) {
            chatMsgInputViewStub = findViewById(R.id.plvsa_chatroom_chat_msg_input_layout_port_view_stub);
        } else {
            chatMsgInputViewStub = findViewById(R.id.plvsa_chatroom_chat_msg_input_layout_land_view_stub);
        }
        chatMsgInputViewStub.inflate();
    }

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
        plvsaEmojiTabEmojiIv = findViewById(R.id.plvsa_emoji_tab_emoji_iv);
        plvsaEmojiTabPersonalIv = findViewById(R.id.plvsa_emoji_tab_personal_iv);
        emojiPersonalRv = findViewById(R.id.emoji_personal_rv);
        plvsaChatroomChatMsgSendTvLand = findViewById(R.id.plvsa_chatroom_chat_msg_send_tv_land);

        emotionPreviewWindow = new PLVImagePreviewPopupWindow(this);

        plvsaEmojiTabEmojiIv.setOnClickListener(this);
        plvsaEmojiTabPersonalIv.setOnClickListener(this);
        plvsaChatroomSelEmojiIv.setOnClickListener(this);
        plvsaChatroomSelImgIv.setOnClickListener(this);
        plvsaChatroomMsgDeleteIv.setOnClickListener(this);
        plvsaChatroomMsgSendTv.setOnClickListener(this);
        plvsaChatroomCloseAnswerWindowTv.setOnClickListener(this);
        if (plvsaChatroomChatMsgSendTvLand != null) {
            plvsaChatroomChatMsgSendTvLand.setOnClickListener(this);
        }

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
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        inflateView();
    }

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
        } else if (id == R.id.send_msg_tv
                || id == R.id.plvsa_chatroom_chat_msg_send_tv_land) {
            postMsgWithAnswer();
        } else if (id == R.id.plvsa_chatroom_sel_img_iv) {
            requestSelectImg();
        } else if (id == R.id.plvsa_chatroom_close_answer_window_tv) {
            plvsaChatroomAnswerLy.setVisibility(View.GONE);
        } else if (id == R.id.plvsa_emoji_tab_emoji_iv) {
            changeEmojiTab(true);
        } else if (id == R.id.plvsa_emoji_tab_personal_iv) {
            changeEmojiTab(false);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="表情">
    private void initEmotionTab(){
        final List<PLVEmotionImageVO.EmotionImage> emotionList = PLVFaceManager.getInstance().getEmotionList();
        if(isInitEmotion || emotionList.isEmpty()){
            return;
        }
        PLVSAChatroomUtils.initEmojiPersonalList(emojiPersonalRv, 5, emotionList, new PLVSAEmotionPersonalListAdapter.OnViewActionListener() {
            @Override
            public void onEmotionViewClick(PLVEmotionImageVO.EmotionImage emotionImage) {
                //发送图片表情
                boolean sendResult = true;
                if(inputListener instanceof MessageSendListener) {
                    sendResult = ((MessageSendListener) inputListener).onSendEmotion(emotionImage.getId());
                }
                if (sendResult) {
                    requestClose();
                } else {
                    ToastUtils.showShort("图片表情发送失败");
                }
            }

            @Override
            public void onEmotionViewLongClick(PLVEmotionImageVO.EmotionImage emotionImage, View view) {
                emotionPreviewWindow.showInTopCenter(emotionImage.getUrl(), view);
            }
        });
        isInitEmotion = true;
    }


    private void changeEmojiTab(boolean isEmoji){
        initEmotionTab();
        plvsaEmojiTabEmojiIv.setSelected(isEmoji);
        plvsaEmojiTabPersonalIv.setSelected(isEmoji);
        int selectColor = Color.parseColor("#FF2B2C35");
        int unSelectColor = Color.parseColor("#FF535353");
        plvsaEmojiTabEmojiIv.setBackgroundColor(isEmoji ? selectColor : unSelectColor);
        plvsaEmojiTabPersonalIv.setBackgroundColor(isEmoji ?  unSelectColor : selectColor);
        //切换rv的表情库
        if(isEmoji){
            //显示emoji表情库
            plvsaChatroomEmojiRv.setVisibility(View.VISIBLE);
            plvsaChatroomMsgSendTv.setVisibility(View.VISIBLE);
            plvsaChatroomMsgDeleteIv.setVisibility(View.VISIBLE);
            emojiPersonalRv.setVisibility(View.INVISIBLE);
        } else {
            //显示个性表情包
            plvsaChatroomEmojiRv.setVisibility(View.INVISIBLE);
            plvsaChatroomMsgSendTv.setVisibility(View.INVISIBLE);
            plvsaChatroomMsgDeleteIv.setVisibility(View.INVISIBLE);
            emojiPersonalRv.setVisibility(View.VISIBLE);
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="内部类 - 信息发送监听器">
    public interface MessageSendListener extends InputListener {
        void onSendImg(PolyvSendLocalImgEvent imgEvent);

        boolean onSendQuoteMsg(String message);

        /**
         * 发送个性表情
         */
        boolean onSendEmotion(String emotionId);
    }
    // </editor-fold>
}
