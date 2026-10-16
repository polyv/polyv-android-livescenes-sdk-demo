package com.easefun.polyv.streameralone.modules.liveroom;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.PLVSAMessageAdapter;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** Full-screen SA settings overlay. The camera is left in its original parent. */
public class PLVSAPublicScreenSettingsLayout extends FrameLayout {
    private final ViewGroup sceneRoot;
    private final View camera;
    private final Map<View, Integer> savedVisibility = new LinkedHashMap<>();
    private final PLVSAMessageAdapter previewAdapter = new PLVSAMessageAdapter();
    private RecyclerView preview;
    private int lastWidth;
    private int lastHeight;
    private boolean landscape;
    private final ViewTreeObserver.OnPreDrawListener keepSceneHidden = new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
            if (getVisibility() == VISIBLE) {
                for (View view : savedVisibility.keySet()) {
                    if (view.getVisibility() != INVISIBLE) {
                        // 保存设置期间由直播状态/横竖屏更新的新状态，关闭时恢复最新状态。
                        savedVisibility.put(view, view.getId() == R.id.plvsa_popup_container_mask ? GONE : view.getVisibility());
                        view.setVisibility(INVISIBLE);
                    }
                }
            }
            return true;
        }
    };

    public PLVSAPublicScreenSettingsLayout(Context context, ViewGroup sceneRoot, View camera) {
        super(context);
        this.sceneRoot = sceneRoot;
        this.camera = camera;
        setClickable(true);
        setBackgroundColor(0x1A000000);
        setVisibility(GONE);
        // 只构造已有发言类型，不向聊天 presenter/socket 注入模拟数据。
        String[] names = getResources().getStringArray(R.array.plvsa_public_screen_preview_names);
        String[] messages = getResources().getStringArray(R.array.plvsa_public_screen_preview_messages);
        for (int i = 0; i < 18; i++) {
            PLVSocketUserBean user = new PLVSocketUserBean();
            user.setNick(names[i % names.length]);
            user.setUserId("public-screen-preview-" + i);
            user.setUserType(i % 3 == 2 ? "teacher" : "viewer");
            user.setActor(i % 3 == 2 ? getContext().getString(R.string.plvsa_public_screen_preview_teacher) : "");
            PLVSpeakEvent event = new PLVSpeakEvent();
            event.setId("public-screen-preview-" + i);
            event.setUser(user);
            event.setValues(Arrays.asList(messages[i % messages.length]));
            event.setObjects(PLVTextFaceLoader.messageToSpan(event.getValues().get(0), ConvertUtils.dp2px(12), Utils.getApp()));
            previewAdapter.addDataChangedAtLast(new PLVBaseViewData<>(event, PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK));
        }
    }

    public void show() {
        if (getVisibility() == VISIBLE) return;
        savedVisibility.clear();
        for (int i = 0; i < sceneRoot.getChildCount(); i++) {
            View child = sceneRoot.getChildAt(i);
            if (child == camera || child == this) continue;
            // 更多菜单已关闭，不能在恢复时再次显示它的蒙层。
            savedVisibility.put(child, child.getId() == R.id.plvsa_popup_container_mask ? GONE : child.getVisibility());
            child.setVisibility(INVISIBLE);
        }
        sceneRoot.getViewTreeObserver().addOnPreDrawListener(keepSceneHidden);
        setVisibility(VISIBLE);
        bringToFront();
        requestFocus();
        post(new Runnable() { @Override public void run() { rebuild(); } });
    }

    public void close() {
        if (getVisibility() != VISIBLE) return;
        sceneRoot.getViewTreeObserver().removeOnPreDrawListener(keepSceneHidden);
        setVisibility(GONE);
        if (preview != null) preview.setAdapter(null);
        for (Map.Entry<View, Integer> entry : savedVisibility.entrySet()) {
            entry.getKey().setVisibility(entry.getValue());
        }
        savedVisibility.clear();
    }

    @Override protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (getVisibility() == VISIBLE) post(new Runnable() { @Override public void run() { rebuild(); } });
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getVisibility() == VISIBLE && (w != lastWidth || h != lastHeight)) {
            post(new Runnable() { @Override public void run() { rebuild(); } });
        }
    }

    private void rebuild() {
        if (getVisibility() != VISIBLE || getWidth() == 0 || getHeight() == 0) return;
        if (preview != null) preview.setAdapter(null);
        removeAllViews();
        lastWidth = getWidth();
        lastHeight = getHeight();
        landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        preview = new RecyclerView(getContext()) {
            // 模拟消息只用于展示，不能触发回复、禁言等真实聊天室操作。
            @Override public boolean dispatchTouchEvent(MotionEvent event) { return true; }
        };
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        preview.setLayoutManager(manager);
        preview.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(dp(4)));
        preview.setOverScrollMode(OVER_SCROLL_NEVER);
        previewAdapter.setMessageTextSizeSp(PLVSAPublicScreenConfig.textSizeSp(getContext()));
        preview.setAdapter(previewAdapter);
        addView(preview);
        updatePreviewSize();
        preview.scrollToPosition(previewAdapter.getItemCount() - 1);

        if (landscape) {
            LinearLayout panel = new LinearLayout(getContext());
            panel.setOrientation(LinearLayout.VERTICAL);
            panel.setPadding(dp(16), dp(12), dp(16), dp(12));
            panel.setBackground(rounded(0xB34B4B4B, 12));
            LayoutParams params = new LayoutParams((int) (lastWidth * 0.40f), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT);
            addView(panel, params);
            LinearLayout title = new LinearLayout(getContext());
            TextView close = text("×", 26);
            close.setContentDescription(getContext().getString(R.string.plvsa_public_screen_close));
            close.setOnClickListener(v -> close());
            title.addView(close, new LinearLayout.LayoutParams(dp(40), dp(44)));
            title.addView(text(getContext().getString(R.string.plvsa_public_screen_title), 16), new LinearLayout.LayoutParams(0, dp(44), 1));
            TextView done = text("✓", 24);
            done.setContentDescription(getContext().getString(R.string.plvsa_public_screen_done));
            done.setOnClickListener(v -> close());
            title.addView(done, new LinearLayout.LayoutParams(dp(40), dp(44)));
            panel.addView(title);
            addHorizontalOptions(panel, true);
            addHorizontalOptions(panel, false);
        } else {
            TextView title = text(getContext().getString(R.string.plvsa_public_screen_title), 16);
            addView(title, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48), Gravity.TOP));
            TextView close = text("×", 26);
            close.setContentDescription(getContext().getString(R.string.plvsa_public_screen_close));
            close.setOnClickListener(v -> close());
            addView(close, new LayoutParams(dp(48), dp(48), Gravity.TOP | Gravity.LEFT));
            LinearLayout options = new LinearLayout(getContext());
            options.setOrientation(LinearLayout.HORIZONTAL);
            options.addView(verticalOptions(true));
            options.addView(verticalOptions(false));
            LayoutParams optionsParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
            optionsParams.rightMargin = dp(12);
            optionsParams.bottomMargin = (int) (lastHeight * 0.14f);
            addView(options, optionsParams);
            TextView done = text(getContext().getString(R.string.plvsa_public_screen_done), 16);
            done.setBackground(rounded(0xFF4388FF, 22));
            done.setOnClickListener(v -> close());
            LayoutParams doneParams = new LayoutParams(dp(104), dp(44), Gravity.BOTTOM | Gravity.RIGHT);
            doneParams.rightMargin = dp(20);
            doneParams.bottomMargin = dp(24);
            addView(done, doneParams);
        }
    }

    private LinearLayout verticalOptions(boolean height) {
        LinearLayout column = new LinearLayout(getContext());
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(4), 0, dp(4), 0);
        column.addView(text(getContext().getString(height ? R.string.plvsa_public_screen_height : R.string.plvsa_public_screen_font), 12),
                new LinearLayout.LayoutParams(dp(40), dp(32)));
        LinearLayout choices = new LinearLayout(getContext());
        choices.setOrientation(LinearLayout.VERTICAL);
        choices.setBackground(rounded(0x664B4B4B, 20));
        for (int i = 2; i >= 0; i--) choices.addView(option(height, i), new LinearLayout.LayoutParams(dp(40), dp(48)));
        column.addView(choices);
        return column;
    }

    private void addHorizontalOptions(LinearLayout panel, boolean height) {
        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        rowParams.topMargin = dp(16);
        panel.addView(row, rowParams);
        row.addView(text(getContext().getString(height ? R.string.plvsa_public_screen_height : R.string.plvsa_public_screen_font), 14),
                new LinearLayout.LayoutParams(dp(44), ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout choices = new LinearLayout(getContext());
        choices.setBackground(rounded(0x664B4B4B, 20));
        row.addView(choices, new LinearLayout.LayoutParams(0, dp(40), 1));
        for (int i = 0; i < 3; i++) choices.addView(option(height, i), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    private TextView option(final boolean height, final int level) {
        int[] labels = height ? new int[]{R.string.plvsa_public_screen_low, R.string.plvsa_public_screen_middle, R.string.plvsa_public_screen_high}
                : new int[]{R.string.plvsa_public_screen_small, R.string.plvsa_public_screen_middle, R.string.plvsa_public_screen_large};
        TextView view = text(getContext().getString(labels[level]), 14);
        boolean selected = level == (height ? PLVSAPublicScreenConfig.heightLevel(getContext()) : PLVSAPublicScreenConfig.fontLevel(getContext()));
        view.setSelected(selected);
        if (selected) { view.setBackground(rounded(Color.WHITE, 20)); view.setTextColor(0xFF444444); }
        view.setOnClickListener(v -> {
            if (height) PLVSAPublicScreenConfig.setHeight(getContext(), level);
            else PLVSAPublicScreenConfig.setFont(getContext(), level);
            rebuild();
        });
        return view;
    }

    private void updatePreviewSize() {
        int previewWidth = landscape ? (int) (lastWidth * 0.46f)
                : Math.min((int) (lastWidth * 0.65f), Math.max(dp(80), lastWidth - dp(136)));
        LayoutParams params = new LayoutParams(previewWidth,
                (int) (lastHeight * PLVSAPublicScreenConfig.heightRatio(getContext())), Gravity.BOTTOM | Gravity.LEFT);
        params.leftMargin = dp(16);
        params.bottomMargin = landscape ? dp(16) : (int) (lastHeight * 0.14f);
        preview.setLayoutParams(params);
    }

    private TextView text(String label, int size) {
        TextView view = new TextView(getContext());
        view.setText(label);
        view.setTextSize(size);
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(40));
        return view;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
