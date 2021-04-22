package com.easefun.polyv.livestreamer.modules.chatroom.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livestreamer.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

public class PLVLSEmojiIndicatorView extends FrameLayout {
    private ViewPager viewPager;
    private List<View> views;

    public PLVLSEmojiIndicatorView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSEmojiIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSEmojiIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_chatroom_chat_emoji_indicator_layout, this);

        views = new ArrayList<>();
        views.add(findViewById(R.id.one_view));
        views.add(findViewById(R.id.two_view));
        views.add(findViewById(R.id.three_view));
//        views.get(0).setSelected(true);
        ViewGroup.LayoutParams vp = views.get(0).getLayoutParams();
        vp.width = ConvertUtils.dp2px(22);
        views.get(0).setLayoutParams(vp);

        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewPager != null) {
                        viewPager.setCurrentItem(finalI);
                    }
                }
            });
        }
    }

    public void bindViewPager(final ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (views != null && position < views.size()) {
//                    for (View view : views) {
//                        view.setSelected(false);
//                    }
//                    views.get(position).setSelected(true);
                    for (View view : views) {
                        ViewGroup.LayoutParams vp = view.getLayoutParams();
                        vp.width = ConvertUtils.dp2px(6);
                        view.setLayoutParams(vp);
                    }
                    ViewGroup.LayoutParams vp = views.get(position).getLayoutParams();
                    vp.width = ConvertUtils.dp2px(22);
                    views.get(position).setLayoutParams(vp);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}
