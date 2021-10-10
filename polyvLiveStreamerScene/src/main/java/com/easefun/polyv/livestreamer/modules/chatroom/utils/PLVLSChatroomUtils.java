package com.easefun.polyv.livestreamer.modules.chatroom.utils;

import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;


import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.ui.widget.gif.RelativeImageSpan;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.PLVLSEmoGridViewAdapter;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.PLVLSEmoPagerAdapter;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.PLVLSEmotionGridViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室工具类
 */
public class PLVLSChatroomUtils {
    private static int emojiLength;
    //一页的表情数量
    private static float emojiPageCount = 12 * 4;

    private static float emotionPageCount = 10 * 5;//目前上限为50个

    // <editor-fold defaultstate="collapsed" desc="发送表情相关">
    //初始化表情列表
    public static void initEmojiList(ViewPager emojiVp, int gridViewLayoutId, final EditText inputEt) {
        List<View> lists = new ArrayList<>();
        int size = (int) Math.ceil(PLVFaceManager.getInstance().getFaceMap().size() / emojiPageCount);
        for (int i = 0; i < size; i++) {
            lists.add(initEmojiGridView(gridViewLayoutId, inputEt, i));
        }
        PLVLSEmoPagerAdapter emoPagerAdapter = new PLVLSEmoPagerAdapter(lists, emojiVp.getContext());
        emojiVp.setAdapter(emoPagerAdapter);
    }

    //初始化表情列表的gridView
    private static View initEmojiGridView(int gridViewLayoutId, final EditText inputEt, int position) {
        GridView gridView = (GridView) LayoutInflater.from(inputEt.getContext()).inflate(gridViewLayoutId, null);
        List<String> lists = new ArrayList<>(PLVFaceManager.getInstance().getFaceMap().keySet());
        final List<String> elists = lists.subList(Math.min(position * (int) emojiPageCount, lists.size()),
                Math.min((position + 1) * (int) emojiPageCount, lists.size()));
        PLVLSEmoGridViewAdapter emoGridViewAdapter = new PLVLSEmoGridViewAdapter(elists, inputEt.getContext());
        gridView.setAdapter(emoGridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                appendEmo(elists.get(position), inputEt);
            }
        });
        return gridView;
    }

    public static void initEmotionList(ViewPager emotionVp, int gridViewLayoutId, List<PLVEmotionImageVO.EmotionImage> emotionImageList,
                                       final AdapterView.OnItemClickListener listener,
                                       AdapterView.OnItemLongClickListener longClickListener){
        List<View> lists = new ArrayList<>();
        int size = (int) Math.ceil(emotionImageList.size() / emotionPageCount);
        for (int i = 0; i < size; i++) {
            GridView gridView = (GridView) LayoutInflater.from(emotionVp.getContext()).inflate(gridViewLayoutId, null);
            gridView.setNumColumns(7);
            gridView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
//            final List<PLVEmotionImageVO.EmotionImage> elists = emotionImageList.subList(Math.min(i * (int) emotionPageCount, emotionImageList.size()),
//                    Math.min((i + 1) * (int) emotionPageCount, emotionImageList.size()));

            PLVLSEmotionGridViewAdapter adapter = new PLVLSEmotionGridViewAdapter(emotionImageList, emotionVp.getContext());
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(listener);
            gridView.setOnItemLongClickListener(longClickListener);
            lists.add(gridView);
        }
        PLVLSEmoPagerAdapter emoPagerAdapter = new PLVLSEmoPagerAdapter(lists, emotionVp.getContext());
        emotionVp.setAdapter(emoPagerAdapter);
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
            PLVToast.Builder.context(inputEt.getContext())
                    .setText("添加表情失败！")
                    .build()
                    .show();
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
