package com.easefun.polyv.livestreamer.modules.chatroom.utils;

import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.easefun.polyv.businesssdk.sub.gif.RelativeImageSpan;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.PLVLSEmoGridViewAdapter;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.PLVLSEmoPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室工具类
 */
public class PLVLSChatroomUtils {
    private static int emojiLength;

    // <editor-fold defaultstate="collapsed" desc="发送表情相关">
    //初始化表情列表
    public static void initEmojiList(ViewPager emojiVp, int gridViewLayoutId, final EditText inputEt) {
        List<View> lists = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            lists.add(initEmojiGridView(gridViewLayoutId, inputEt, i));
        }
        PLVLSEmoPagerAdapter emoPagerAdapter = new PLVLSEmoPagerAdapter(lists, emojiVp.getContext());
        emojiVp.setAdapter(emoPagerAdapter);
    }

    //初始化表情列表的gridView
    private static View initEmojiGridView(int gridViewLayoutId, final EditText inputEt, int position) {
        GridView gridView = (GridView) LayoutInflater.from(inputEt.getContext()).inflate(gridViewLayoutId, null);
        List<String> lists = new ArrayList<>(PLVFaceManager.getInstance().getFaceMap().keySet());
        final List<String> elists = lists.subList(position * (12 * 4),
                Math.min((position + 1) * (12 * 4), lists.size()));
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
