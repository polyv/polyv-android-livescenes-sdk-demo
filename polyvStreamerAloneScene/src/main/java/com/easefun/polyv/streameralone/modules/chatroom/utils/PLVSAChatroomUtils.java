package com.easefun.polyv.streameralone.modules.chatroom.utils;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.EditText;

import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.ui.widget.gif.RelativeImageSpan;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.PLVSAEmojiListAdapter;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.PLVSAEmotionPersonalListAdapter;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.List;

/**
 * 聊天室工具类
 */
public class PLVSAChatroomUtils {
    private static int emojiLength;

    // <editor-fold defaultstate="collapsed" desc="发送表情相关">
    //初始化表情列表
    public static void initEmojiList(RecyclerView emojiRv, final EditText inputEt) {
        emojiRv.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(emojiRv.getContext(), 6, GridLayoutManager.VERTICAL, false);
        emojiRv.setLayoutManager(gridLayoutManager);
        emojiRv.addItemDecoration(new PLVSAEmojiListAdapter.GridSpacingItemDecoration(6, ConvertUtils.dp2px(4), true));
        PLVSAEmojiListAdapter emojiListAdapter = new PLVSAEmojiListAdapter();
        emojiListAdapter.setOnViewActionListener(new PLVSAEmojiListAdapter.OnViewActionListener() {
            @Override
            public void onEmojiViewClick(String emoKey) {
                appendEmo(emoKey, inputEt);
            }
        });
        emojiRv.setAdapter(emojiListAdapter);
    }

    /**
     * 初始化个性表情
     */
    public static void initEmojiPersonalList(RecyclerView emojiPersonalRv, int spanCount, List<PLVEmotionImageVO.EmotionImage> emotionImages,
                                             PLVSAEmotionPersonalListAdapter.OnViewActionListener actionListener ) {
        emojiPersonalRv.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(emojiPersonalRv.getContext(), spanCount, GridLayoutManager.VERTICAL, false);
        emojiPersonalRv.setLayoutManager(gridLayoutManager);
        PLVSAEmotionPersonalListAdapter emotionListAdapter = new PLVSAEmotionPersonalListAdapter(emotionImages);
        emojiPersonalRv.addItemDecoration(new PLVSAEmotionPersonalListAdapter.GridSpacingItemDecoration(spanCount, ConvertUtils.dp2px(10), true));

        emotionListAdapter.setOnViewActionListener(actionListener);
        emojiPersonalRv.setAdapter(emotionListAdapter);

    }

    // 删除表情
    public static void deleteEmoText(EditText inputEt) {
        int start = inputEt.getSelectionStart();
        int end = inputEt.getSelectionEnd();
        if (end > 0) {
            if (start != end) {
                inputEt.getText().delete(start, end);
            } else if (isEmo(end, inputEt)) {
                inputEt.getText().delete(end - emojiLength, end);
            } else {
                inputEt.getText().delete(end - 1, end);
            }
        }
    }

    //判断是否是表情
    private static boolean isEmo(int end, EditText inputEt) {
        String preMsg = inputEt.getText().subSequence(0, end).toString();
        int regEnd = preMsg.lastIndexOf("]");
        int regStart = preMsg.lastIndexOf("[");
        if (regEnd == end - 1 && regEnd - regStart >= 2) {
            String regex = preMsg.substring(regStart);
            emojiLength = regex.length();
            if (PLVFaceManager.getInstance().getFaceId(regex) != -1) {
                return true;
            }
        }
        return false;
    }

    //添加表情
    private static void appendEmo(String emoKey, EditText inputEt) {
        SpannableStringBuilder span = new SpannableStringBuilder(emoKey);
        if (inputEt.getText().length() + span.length() >= 2000) {
            Log.e("ChatroomUtils", "appendEmo fail because exceed maxLength 2000");
            return;
        }
        int textSize = (int) inputEt.getTextSize();
        Drawable drawable;
        ImageSpan imageSpan;
        try {
            drawable = inputEt.getResources().getDrawable(PLVFaceManager.getInstance().getFaceId(emoKey));
            imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
        } catch (Exception e) {
            PLVCommonLog.e("ChatroomUtils", "添加表情失败！");// no need i18n
            return;
        }
        drawable.setBounds(0, 0, (int) (textSize * 1.5), (int) (textSize * 1.5));
        span.setSpan(imageSpan, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int selectionStart = inputEt.getSelectionStart();
        int selectionEnd = inputEt.getSelectionEnd();
        if (selectionStart != selectionEnd) {
            inputEt.getText().replace(selectionStart, selectionEnd, span);
        } else {
            inputEt.getText().insert(selectionStart, span);
        }
    }
    // </editor-fold>
}
