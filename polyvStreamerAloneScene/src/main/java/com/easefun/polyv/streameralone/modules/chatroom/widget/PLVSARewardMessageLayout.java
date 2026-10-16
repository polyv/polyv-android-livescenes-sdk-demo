package com.easefun.polyv.streameralone.modules.chatroom.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.streameralone.R;

/** 优先保留礼物图片和打赏内容，昵称使用剩余空间。 */
public class PLVSARewardMessageLayout extends LinearLayout {
    public PLVSARewardMessageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        TextView nickname = findViewById(R.id.chat_nick_tv);
        TextView message = findViewById(R.id.chat_msg_tv);
        ImageView image = findViewById(R.id.chat_img_iv);
        int maximumMessageWidth = Integer.MAX_VALUE;
        int maximumNicknameWidth = Integer.MAX_VALUE;
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            ViewGroup.MarginLayoutParams imageParams = (ViewGroup.MarginLayoutParams) image.getLayoutParams();
            int imageSpace = imageParams.width + imageParams.leftMargin + imageParams.rightMargin;
            int available = Math.max(0, MeasureSpec.getSize(widthMeasureSpec)
                    - getPaddingLeft() - getPaddingRight() - imageSpace);
            int nicknameSpace = nickname.length() == 0 ? 0 : (int) Math.ceil(nickname.getPaint().measureText("…"));
            maximumMessageWidth = Math.max(0, available - nicknameSpace);
            if (message.getMaxWidth() != maximumMessageWidth) {
                message.setMaxWidth(maximumMessageWidth);
            }
            // 先测量当前打赏内容，再把剩余空间作为昵称上限，不能用 weight 撑满。
            // 每次都重新测量，避免长昵称 item 复用为短昵称后留下空白。
            message.measure(MeasureSpec.makeMeasureSpec(maximumMessageWidth, MeasureSpec.AT_MOST),
                    getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(),
                            message.getLayoutParams().height));
            maximumNicknameWidth = Math.max(0, available - message.getMeasuredWidth());
        }
        if (message.getMaxWidth() != maximumMessageWidth) {
            message.setMaxWidth(maximumMessageWidth);
        }
        if (nickname.getMaxWidth() != maximumNicknameWidth) {
            nickname.setMaxWidth(maximumNicknameWidth);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
