package com.easefun.polyv.livehiclass.modules.chatroom.utils;

import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.ui.widget.gif.RelativeImageSpan;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.PLVHCEmoGridViewAdapter;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.PLVHCEmoPagerAdapter;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.PLVHCEmojiListAdapter;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室工具类
 */
public class PLVHCChatroomUtils {
    private static int emojiLength;
    //一页的表情数量
    private static final float EMOJI_PAGE_COUNT = 12 * 4;

    // <editor-fold defaultstate="collapsed" desc="发送表情相关">
    //初始化表情列表
    public static void initEmojiList(RecyclerView emojiRv, final EditText inputEt) {
        emojiRv.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(emojiRv.getContext(), 4, GridLayoutManager.VERTICAL, false);
        emojiRv.setLayoutManager(gridLayoutManager);
        emojiRv.addItemDecoration(new PLVHCEmojiListAdapter.GridSpacingItemDecoration(4, ConvertUtils.dp2px(4), false));
        PLVHCEmojiListAdapter emojiListAdapter = new PLVHCEmojiListAdapter();
        emojiListAdapter.setOnViewActionListener(new PLVHCEmojiListAdapter.OnViewActionListener() {
            @Override
            public void onEmojiViewClick(String emoKey) {
                appendEmo(emoKey, inputEt);
            }
        });
        emojiRv.setAdapter(emojiListAdapter);
    }

    //初始化表情列表
    public static void initEmojiList(ViewPager emojiVp, int gridViewLayoutId, final EditText inputEt) {
        List<View> lists = new ArrayList<>();
        int size = (int) Math.ceil(PLVFaceManager.getInstance().getFaceMap().size() / EMOJI_PAGE_COUNT);
        for (int i = 0; i < size; i++) {
            lists.add(initEmojiGridView(gridViewLayoutId, inputEt, i));
        }
        PLVHCEmoPagerAdapter emoPagerAdapter = new PLVHCEmoPagerAdapter(lists, emojiVp.getContext());
        emojiVp.setAdapter(emoPagerAdapter);
    }

    //初始化表情列表的gridView
    private static View initEmojiGridView(int gridViewLayoutId, final EditText inputEt, int position) {
        GridView gridView = (GridView) LayoutInflater.from(inputEt.getContext()).inflate(gridViewLayoutId, null);
        List<String> lists = new ArrayList<>(PLVFaceManager.getInstance().getFaceMap().keySet());
        final List<String> elists = lists.subList(Math.min(position * (int) EMOJI_PAGE_COUNT, lists.size()),
                Math.min((position + 1) * (int) EMOJI_PAGE_COUNT, lists.size()));
        PLVHCEmoGridViewAdapter emoGridViewAdapter = new PLVHCEmoGridViewAdapter(elists, inputEt.getContext());
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
            PLVHCToast.Builder.context(inputEt.getContext())
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
