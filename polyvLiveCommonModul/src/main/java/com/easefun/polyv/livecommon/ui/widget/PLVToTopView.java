package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.gif.GifSpanTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVViewPagerAdapter;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackCallDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.playback.chat.PLVChatPlaybackCallDataExListener;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.chat.PLVCancelTopEvent;
import com.plv.socket.event.chat.PLVToTopEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * 评论上墙View
 */
public class PLVToTopView extends FrameLayout {
    //是否聊天回放布局
    private boolean isChatPlaybackLayout;
    private boolean isLiveType;
    private ViewPager messageVp;
    private TextView closeTv;
    private TextView cancelTv;
    private ToTopIndicator toTopIndicator;

    private final List<PLVToTopEvent> toTopEvents = new ArrayList<>();
    private final List<ToTopFragment> toTopFragments = new ArrayList<>();
    private final PLVViewPagerAdapter viewPagerAdapter = new PLVViewPagerAdapter(((FragmentActivity) getContext()).getSupportFragmentManager(), toTopFragments);
    private boolean showEnabled = true;
    private boolean isShowStatus = false;

    public PLVToTopView(@NonNull Context context) {
        this(context, null);
    }

    public PLVToTopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVToTopView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_to_top_layout, this, true);
        PLVBlurUtils.initBlurView((PLVBlurView) findViewById(R.id.blur_ly));

        messageVp = findViewById(R.id.message_vp);
        closeTv = (TextView) findViewById(R.id.close_tv);
        cancelTv = (TextView) findViewById(R.id.cancel_tv);
        toTopIndicator = findViewById(R.id.to_top_indicator);
        closeTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        cancelTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ToTopFragment fragment = (ToTopFragment) viewPagerAdapter.getItem(messageVp.getCurrentItem());
                PLVChatroomManager.getInstance().cancelTopMessage(fragment.event.getId());
                if (!PLVSocketWrapper.getInstance().isOnlineStatus()) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_common_toast_network_error)
                            .show();
                }
            }
        });
        messageVp.setAdapter(viewPagerAdapter);
        toTopIndicator.bindViewPager(messageVp);
    }

    public void setShowEnabled(boolean showEnabled) {
        this.showEnabled = showEnabled;
        if (showEnabled && isShowStatus) {
            show();
        } else if (!showEnabled) {
            close();
        }
    }

    public void setCancelTopStyle() {
        closeTv.setVisibility(View.GONE);
        cancelTv.setVisibility(View.VISIBLE);
    }

    private void updateToTopFragments() {
        final int currentItem = messageVp.getCurrentItem();
        List<ToTopFragment> fragments = PLVSequenceWrapper.wrap(toTopEvents)
                .map(new Function1<PLVToTopEvent, ToTopFragment>() {
                    @Override
                    public ToTopFragment invoke(PLVToTopEvent event) {
                        return new ToTopFragment(event);
                    }
                })
                .toMutableList();
        toTopFragments.clear();
        toTopFragments.addAll(fragments);
        viewPagerAdapter.notifyDataSetChanged();
        toTopIndicator.updateSize(toTopFragments.size());
        if (toTopFragments.isEmpty()) {
            close();
        } else {
            show();
        }
        if (currentItem >= viewPagerAdapter.getCount()) {
            messageVp.setCurrentItem(0);
        }
    }

    public void show() {
        if (showEnabled) {
            setVisibility(View.VISIBLE);
        }
        isShowStatus = true;
    }

    public void close() {
        isShowStatus = false;
        post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        });
    }

    public void setIsChatPlaybackLayout(boolean isChatPlaybackLayout) {
        this.isChatPlaybackLayout = isChatPlaybackLayout;
    }

    public void setIsLiveType(boolean isLiveType) {
        this.isLiveType = isLiveType;
    }

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }

    public IPLVChatPlaybackCallDataListener getChatPlaybackDataListener() {
        return chatPlaybackDataListener;
    }

    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {

        @Override
        public void onToTopEvent(final PLVToTopEvent toTopEvent) {
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            post(new Runnable() {
                @Override
                public void run() {
                    toTopEvents.clear();
                    toTopEvents.addAll(toTopEvent.flatten());
                    updateToTopFragments();
                }
            });
        }

        @Override
        public void onCancelTopEvent(@NonNull final PLVCancelTopEvent cancelTopEvent) {
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            post(new Runnable() {
                @Override
                public void run() {
                    for (PLVToTopEvent toTopEvent : toTopEvents) {
                        if (toTopEvent.getId() == cancelTopEvent.getId()) {
                            toTopEvents.remove(toTopEvent);
                            break;
                        }
                    }
                    updateToTopFragments();
                }
            });
        }
    };

    private IPLVChatPlaybackCallDataListener chatPlaybackDataListener = new PLVChatPlaybackCallDataExListener() {

        @Override
        public void onToTopEvent(final PLVToTopEvent event) {
            post(new Runnable() {
                @Override
                public void run() {
                    toTopEvents.clear();
                    toTopEvents.addAll(event.flatten());
                    updateToTopFragments();
                }
            });
        }

        @Override
        public void onCancelTopEvent(@Nullable PLVCancelTopEvent event) {
            close();
        }

        @Override
        public void onLoadPreviousEnabled(boolean enabled, boolean isByClearData) {

        }

        @Override
        public void onHasNotAddedData() {

        }

        @Override
        public void onLoadPreviousFinish() {

        }

        @Override
        public void onDataInserted(int startPosition, int count, List<PLVChatPlaybackData> insertDataList, boolean inHead, int time) {

        }

        @Override
        public void onDataRemoved(int startPosition, int count, List<PLVChatPlaybackData> removeDataList, boolean inHead) {

        }

        @Override
        public void onDataCleared() {

        }

        @Override
        public void onData(List<PLVChatPlaybackData> dataList) {

        }

        @Override
        public void onManager(IPLVChatPlaybackManager chatPlaybackManager) {

        }
    };

    public static class ToTopFragment extends Fragment {

        public final PLVToTopEvent event;

        public ToTopFragment(PLVToTopEvent event) {
            super();
            this.event = event;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.plv_to_top_message_fragment, container, false);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final GifSpanTextView messageTv = getView().findViewById(R.id.message_tv);
            SpannableStringBuilder span = new SpannableStringBuilder(event.getNick() + ": ");
            span.setSpan(new ForegroundColorSpan(PLVFormatUtils.parseColor("#FFD16B")), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            CharSequence content = PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(event.getContent()), ConvertUtils.dp2px(14), Utils.getApp());
            messageTv.setTextInner(span.append(content), true);
            messageTv.setWebLinkClickListener(new GifSpanTextView.WebLinkClickListener() {
                @Override
                public void webLinkOnClick(String url) {
                    PLVWebUtils.openWebLink(url, messageTv.getContext());
                }
            });
        }
    }

    public static class ToTopIndicator extends View {

        private int currentIndex = 0;
        private int size = 0;
        private final Paint paint = new Paint();

        public ToTopIndicator(Context context) {
            super(context);
        }

        public ToTopIndicator(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public ToTopIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void bindViewPager(final ViewPager viewPager) {
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    currentIndex = position;
                    invalidate();
                }
            });
        }

        public void updateSize(int size) {
            this.size = size;
            requestLayout();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (size <= 1) {
                setMeasuredDimension(0, 0);
                return;
            }
            int desiredWidth = ConvertUtils.dp2px(10 * size - 2);
            int desiredHeight = ConvertUtils.dp2px(4);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            if (widthMode == MeasureSpec.EXACTLY) {
                desiredWidth = widthSize;
            } else {
                desiredWidth = Math.min(desiredWidth, widthSize);
            }
            if (heightMode == MeasureSpec.EXACTLY) {
                desiredHeight = heightSize;
            } else {
                desiredHeight = Math.min(desiredHeight, heightSize);
            }
            setMeasuredDimension(desiredWidth, desiredHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            int x = 0;
            int y = 0;
            int r = ConvertUtils.dp2px(2);
            for (int index = 0; index < size; index++) {
                int width = ConvertUtils.dp2px(4);
                int height = ConvertUtils.dp2px(4);
                paint.setColor(0x33FFFFFF);
                if (index == currentIndex) {
                    width = ConvertUtils.dp2px(8);
                    paint.setColor(0x99FFFFFF);
                }
                canvas.drawRoundRect(x, y, x + width, y + height, r, r, paint);
                x += width;
                x += ConvertUtils.dp2px(6);
            }
        }
    }
}
