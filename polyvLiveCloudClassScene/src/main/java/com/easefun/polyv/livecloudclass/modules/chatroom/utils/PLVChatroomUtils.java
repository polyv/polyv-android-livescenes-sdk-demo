package com.easefun.polyv.livecloudclass.modules.chatroom.utils;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.EditText;

import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCEmojiListAdapter;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCEmotionPersonalListAdapter;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.ui.widget.gif.RelativeImageSpan;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

/**
 * 聊天室工具类
 */
public class PLVChatroomUtils {
    private static int emojiLength;

    // <editor-fold defaultstate="collapsed" desc="发送表情相关">
    //初始化emoji表情列表
    public static void initEmojiList(RecyclerView emojiRv, final EditText inputEt) {
        emojiRv.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(emojiRv.getContext(), 6, GridLayoutManager.VERTICAL, false);
        emojiRv.setLayoutManager(gridLayoutManager);
        emojiRv.addItemDecoration(new PLVLCEmojiListAdapter.GridSpacingItemDecoration(6, ConvertUtils.dp2px(4), true));
        PLVLCEmojiListAdapter emojiListAdapter = new PLVLCEmojiListAdapter();
        emojiListAdapter.setOnViewActionListener(new PLVLCEmojiListAdapter.OnViewActionListener() {
            @Override
            public void onEmojiViewClick(String emoKey) {
                if (inputEt.isEnabled()) {
                    appendEmo(emoKey, inputEt);
                }
            }
        });
        emojiRv.setAdapter(emojiListAdapter);
    }

    /**
     * 初始化个性表情
     */
    public static void initEmojiPersonalList(RecyclerView emojiPersonalRv, int spanCount, List<PLVEmotionImageVO.EmotionImage> emotionImages,
                                             PLVLCEmotionPersonalListAdapter.OnViewActionListener actionListener ) {
        emojiPersonalRv.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(emojiPersonalRv.getContext(), spanCount, GridLayoutManager.VERTICAL, false);
        emojiPersonalRv.setLayoutManager(gridLayoutManager);
        PLVLCEmotionPersonalListAdapter emotionListAdapter = new PLVLCEmotionPersonalListAdapter(emotionImages);
        emojiPersonalRv.addItemDecoration(new PLVLCEmotionPersonalListAdapter.GridSpacingItemDecoration(spanCount, ConvertUtils.dp2px(10), true));

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
        int regEnd = preMsg.lastIndexOf(']');
        int regStart = preMsg.lastIndexOf('[');
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
        if(inputEt.getText().length() + span.length() >= 200){
            Log.e("ChatroomUtils", "appendEmo fail because exceed maxLength 200");
            return;
        }
        int textSize = (int) inputEt.getTextSize();
        Drawable drawable;
        ImageSpan imageSpan;
        try {
            drawable = inputEt.getResources().getDrawable(PLVFaceManager.getInstance().getFaceId(emoKey));
            imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
        } catch (Exception e) {
            ToastUtils.showShort("添加表情失败！");
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
