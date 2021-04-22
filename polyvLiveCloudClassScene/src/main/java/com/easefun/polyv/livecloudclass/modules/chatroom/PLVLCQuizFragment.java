package com.easefun.polyv.livecloudclass.modules.chatroom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecloudclass.modules.chatroom.utils.PLVChatroomUtils;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVInputFragment;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

/**
 * 咨询提问tab页
 */
public class PLVLCQuizFragment extends PLVInputFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //提问信息列表
    private PLVMessageRecyclerView quizMsgRv;
    private PLVLCMessageAdapter messageAdapter;

    //输入框
    private EditText inputEt;

    //表情布局开关
    private ImageView toggleEmojiIv;

    //表情布局
    private ViewGroup emojiLy;
    private TextView sendMsgTv;
    private ImageView deleteMsgIv;

    //表情列表
    private RecyclerView emojiRv;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_chatroom_quiz_fragment, null);
        initView();
        return view;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        if (chatroomPresenter == null) {
            return;
        }
        quizMsgRv = findViewById(R.id.chat_msg_rv);
        quizMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(16), ConvertUtils.dp2px(16)));
        PLVMessageRecyclerView.setLayoutManager(quizMsgRv);
        messageAdapter = new PLVLCMessageAdapter();
        //设置信息索引，需在chatroomPresenter.registerView后设置
        messageAdapter.setMsgIndex(chatroomPresenter.getViewIndex(chatroomView));
        quizMsgRv.setAdapter(messageAdapter);
        //添加一条提示信息到列表中
        addQuizTipsToList();

        inputEt = findViewById(R.id.input_et);
        inputEt.addTextChangedListener(inputTextWatcher);

        toggleEmojiIv = findViewById(R.id.toggle_emoji_iv);
        toggleEmojiIv.setOnClickListener(this);

        emojiLy = findViewById(R.id.emoji_ly);
        sendMsgTv = findViewById(R.id.send_msg_tv);
        sendMsgTv.setOnClickListener(this);
        deleteMsgIv = findViewById(R.id.delete_msg_iv);
        deleteMsgIv.setOnClickListener(this);

        emojiRv = findViewById(R.id.emoji_rv);
        PLVChatroomUtils.initEmojiList(emojiRv, inputEt);

        addPopupButton(toggleEmojiIv);
        addPopupLayout(emojiLy);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现PLVInputFragment定义的方法">
    @Override
    public int inputLayoutId() {
        return R.id.bottom_input_ly;
    }

    @Override
    public int inputViewId() {
        return R.id.input_et;
    }

    @Override
    public boolean onSendMsg(String message) {
        return sendQuestionMessage(message);
    }

    @Override
    public int attachContainerViewId() {
        return R.id.plvlc_chatroom_input_layout_container;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void setPresenter(@NonNull IPLVChatroomContract.IChatroomPresenter presenter) {
            super.setPresenter(presenter);
            chatroomPresenter = presenter;
        }

        @Override
        public int getQuizEmojiSize() {
            return ConvertUtils.dp2px(16);//提问列表里的文本信息textSize
        }

        @Override
        public void onLocalQuestionMessage(@Nullable PolyvQuestionMessage questionMessage) {
            super.onLocalQuestionMessage(questionMessage);
            if (questionMessage == null) {
                return;
            }
            final PLVBaseViewData viewData = new PLVBaseViewData<>(questionMessage, PLVChatMessageItemType.ITEMTYPE_SEND_QUIZ, new PLVSpecialTypeTag());
            if (!isShowKeyBoard(new OnceHideKeyBoardListener() {
                @Override
                public void call() {
                    //添加信息至列表
                    addQuizMessageToList(viewData);//如果键盘还没完全隐藏，则等待键盘隐藏后再添加到列表中，避免出现列表布局动画问题
                }
            })) {
                //添加信息至列表
                addQuizMessageToList(viewData);
            }
        }

        @Override
        public void onAnswerEvent(@NonNull PLVTAnswerEvent answerEvent) {
            super.onAnswerEvent(answerEvent);
            //添加信息至列表
            addQuizMessageToList(new PLVBaseViewData<>(answerEvent, PLVChatMessageItemType.ITEMTYPE_RECEIVE_QUIZ, new PLVSpecialTypeTag()));
        }
    };

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 添加信息至列表">
    private void addQuizMessageToList(final PLVBaseViewData baseViewData) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (messageAdapter == null) {
                    return;
                }
                boolean result = messageAdapter.addDataChangedAtLast(baseViewData);
                if (result) {
                    quizMsgRv.scrollToPosition(messageAdapter.getItemCount() - 1);
                }
            }
        });
    }

    private void addQuizTipsToList() {
        PLVTAnswerEvent tAnswerEvent = new PLVTAnswerEvent();
        tAnswerEvent.setContent("同学，您好！请问有什么问题吗？");
        tAnswerEvent.setObjects(PLVTextFaceLoader.messageToSpan(tAnswerEvent.getContent(), ConvertUtils.dp2px(14), getContext()));
        PLVSocketUserBean userBean = new PLVSocketUserBean();
        userBean.setUserType(PLVSocketUserConstant.USERTYPE_TEACHER);
        userBean.setNick("讲师");
        userBean.setActor("讲师");
        userBean.setPic(PLVSocketUserConstant.TEACHER_AVATAR_URL);
        tAnswerEvent.setUser(userBean);

        addQuizMessageToList(new PLVBaseViewData<>(tAnswerEvent, PLVChatMessageItemType.ITEMTYPE_RECEIVE_QUIZ, new PLVSpecialTypeTag()));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 发送提问信息">
    private boolean sendQuestionMessage(String message) {
        if (message.trim().length() == 0) {
            ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
            return false;
        } else {
            PolyvQuestionMessage questionMessage = new PolyvQuestionMessage(message);
            if (chatroomPresenter == null) {
                return false;
            }
            int sendValue = chatroomPresenter.sendQuestionMessage(questionMessage);
            if (sendValue > 0) {
                //清空输入框内容并隐藏键盘/弹出的表情布局等
                inputEt.setText("");
                hideSoftInputAndPopupLayout();
                return true;
            } else {
                //如果为-3，则已离线状态
                //发送失败
                ToastUtils.showShort(getString(R.string.plv_chat_toast_send_quiz_failed) + ":" + sendValue);
                return false;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="输入框 - 文本改变监听器">
    private TextWatcher inputTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null && s.length() > 0) {
                sendMsgTv.setEnabled(true);
                sendMsgTv.setSelected(true);
            } else {
                sendMsgTv.setSelected(false);
                sendMsgTv.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.toggle_emoji_iv) {
            togglePopupLayout(toggleEmojiIv, emojiLy);
        } else if (id == R.id.delete_msg_iv) {
            PLVChatroomUtils.deleteEmoText(inputEt);
        } else if (id == R.id.send_msg_tv) {
            sendQuestionMessage(inputEt.getText().toString());
        }
    }
    // </editor-fold>
}
