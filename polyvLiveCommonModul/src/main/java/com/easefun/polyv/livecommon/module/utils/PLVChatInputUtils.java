package com.easefun.polyv.livecommon.module.utils;

import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.livescenes.model.PLVLiveClassDetailVO;

/** 聊天菜单配置的默认输入提示；仅修改 hint，不修改用户输入或输入行数。 */
public class PLVChatInputUtils {
    @Nullable
    public static String getPlaceholder(@Nullable PLVLiveClassDetailVO detail) {
        if (detail == null || detail.getData() == null || detail.getData().getChannelMenus() == null) {
            return null;
        }
        for (PLVLiveClassDetailVO.DataBean.ChannelMenusBean menu : detail.getData().getChannelMenus()) {
            if (menu == null || !PLVLiveClassDetailVO.MENUTYPE_CHAT.equals(menu.getMenuType()) || menu.getContent() == null) {
                continue;
            }
            String content = menu.getContent().replaceAll("[\\r\\n\\u0085\\u2028\\u2029]", " ").trim();
            if (!TextUtils.isEmpty(content)) {
                return content;
            }
        }
        return null;
    }

    public static String getHint(@Nullable IPLVLiveRoomDataManager manager, String fallback) {
        PLVStatefulData<PolyvLiveClassDetailVO> state = manager == null ? null : manager.getClassDetailVO().getValue();
        String placeholder = state == null || !state.isSuccess() ? null : getPlaceholder(state.getData());
        return placeholder == null ? fallback : placeholder;
    }

    /** 将长提示截为单行；输入框本身仍可正常输入多行。 */
    public static void setHint(final EditText input, String hint) {
        boolean first = input.getTag(R.id.plv_chat_input_hint) == null;
        input.setTag(R.id.plv_chat_input_hint, hint);
        updateHint(input);
        if (first) {
            input.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b, int oldL, int oldT, int oldR, int oldB) {
                    if (r - l != oldR - oldL) {
                        updateHint(input);
                    }
                }
            });
        }
    }

    private static void updateHint(EditText input) {
        CharSequence hint = (CharSequence) input.getTag(R.id.plv_chat_input_hint);
        int width = input.getWidth() - input.getCompoundPaddingLeft() - input.getCompoundPaddingRight();
        input.setHint(width > 0 ? TextUtils.ellipsize(hint, input.getPaint(), width, TextUtils.TruncateAt.END) : hint);
    }
}
