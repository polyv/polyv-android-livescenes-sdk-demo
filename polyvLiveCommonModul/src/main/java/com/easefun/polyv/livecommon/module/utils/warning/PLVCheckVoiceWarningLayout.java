package com.easefun.polyv.livecommon.module.utils.warning;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.socket.event.backword.PLVCheckVoiceEvent;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PLVCheckVoiceWarningLayout extends FrameLayout {
    private TextView levelTitleTv;
    private TextView contentTv;
    private TextView levelTv;
    private ImageView closeIv;
    private ViewGroup goDetailLy;
    private TextView goDetailTv1;

    private List<PLVCheckVoiceEvent.BadWordBean> badwords = new ArrayList<>();

    private boolean isUseBlackStyle;

    private PopupWindow popupWindow;
    private View popupView;
    private PLVRoundRectLayout checkVoiceWarningLy;
    private ViewGroup warningTitleLy;
    private RecyclerView warningContentRv;
    private WarningContentAdapter warningContentAdapter;
    private TextView warningConfirmTv;

    public PLVCheckVoiceWarningLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVCheckVoiceWarningLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVCheckVoiceWarningLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_check_voice_warning_layout, this);
        levelTitleTv = findViewById(R.id.plv_warning_level_title_tv);
        contentTv = findViewById(R.id.plv_warning_content_tv);
        levelTv = findViewById(R.id.plv_warning_level_tv);
        closeIv = findViewById(R.id.plv_warning_close_iv);
        goDetailLy = findViewById(R.id.plv_warning_go_detail_ly);
        goDetailTv1 = findViewById(R.id.plv_warning_go_detail_tv_1);

        // init popupWindow
        popupWindow = new PopupWindow(getContext());
        popupView = PLVViewInitUtils.initPopupWindow(this, R.layout.plv_check_voice_warning_popup_layout, popupWindow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopupWindow();
            }
        });
        checkVoiceWarningLy = popupView.findViewById(R.id.plv_check_voice_warning_ly);
        warningTitleLy = popupView.findViewById(R.id.plv_warning_title_ly);
        warningContentRv = popupView.findViewById(R.id.plv_warning_content_rv);
        warningContentRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        warningContentAdapter = new WarningContentAdapter();
        warningContentRv.setAdapter(warningContentAdapter);
        warningConfirmTv = popupView.findViewById(R.id.plv_warning_confirm_tv);

        warningTitleLy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                warningContentRv.smoothScrollToPosition(warningContentAdapter.getItemCount() - 1);
            }
        });
        warningConfirmTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopupWindow();
            }
        });

        closeIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(View.GONE);

                badwords.clear();
            }
        });
        goDetailLy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(View.GONE);

                List<PLVCheckVoiceEvent.BadWordBean> badWordList = new ArrayList<>(badwords);
                badwords.clear();
                Collections.reverse(badWordList);
                showPopupWindow(badWordList);
            }
        });

        if (isUseBlackStyle) {
            setUseBlackStyle();
        }
    }

    public void setUseBlackStyle() {
        this.isUseBlackStyle = true;
        checkVoiceWarningLy.setBackground(null);
        checkVoiceWarningLy.setRoundMode(PLVRoundRectLayout.MODE_NONE);

        PLVBlurView blurView = (PLVBlurView) this.popupView.findViewById(R.id.blur_ly);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) blurView.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = (int) (Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()) * 0.44f);
        layoutParams.gravity = Gravity.RIGHT;
        blurView.setLayoutParams(layoutParams);
        blurView.setVisibility(View.VISIBLE);
        PLVBlurUtils.initBlurView(blurView);
    }

    public void onPortrait() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) checkVoiceWarningLy.getLayoutParams();
        layoutParams.height = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        checkVoiceWarningLy.setLayoutParams(layoutParams);
    }

    public void onLandscape() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) checkVoiceWarningLy.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = isUseBlackStyle ? (int) (landscapeWidth * 0.44f) : Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        layoutParams.gravity = Gravity.RIGHT;
        checkVoiceWarningLy.setLayoutParams(layoutParams);
    }

    private void showPopupWindow(List<PLVCheckVoiceEvent.BadWordBean> badWordList) {
        warningContentAdapter.addDataFirst(badWordList);
        warningTitleLy.setVisibility(View.GONE);
        warningContentRv.scrollToPosition(0);

        if (ScreenUtils.isPortrait()) {
            onPortrait();
        } else {
            onLandscape();
        }
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
    }

    private void dismissPopupWindow() {
        popupWindow.dismiss();
    }

    public void acceptCheckVoiceEvent(PLVCheckVoiceEvent checkVoiceEvent) {
        if (checkVoiceEvent.getBadwords() != null && !checkVoiceEvent.getBadwords().isEmpty()) {
            setVisibility(View.VISIBLE);
            for (PLVCheckVoiceEvent.BadWordBean badWordBean : checkVoiceEvent.getBadwords()) {
                badWordBean.set_timestamp(checkVoiceEvent.get_timestamp());
                badwords.add(badWordBean);
            }
            if (!badwords.isEmpty()) {
                PLVCheckVoiceEvent.BadWordBean badWordBean = badwords.get(badwords.size() - 1);
                String title;
                String level;
                int levelColor;
                int bgResource;
                if (badWordBean.getScore() > 0.8f) {
                    level = PLVAppUtils.getString(R.string.plv_streamer_check_voice_high_risk);
                    title = String.format(PLVAppUtils.getString(R.string.plv_streamer_check_voice_tips), level);
                    levelColor = PLVFormatUtils.parseColor("#E14138");
                    bgResource = R.drawable.plv_red_warning_bg;
                } else if (badWordBean.getScore() > 0.5f) {
                    level = PLVAppUtils.getString(R.string.plv_streamer_check_voice_medium_risk);
                    title = String.format(PLVAppUtils.getString(R.string.plv_streamer_check_voice_tips), level);
                    levelColor = PLVFormatUtils.parseColor("#F4BA75");
                    bgResource = R.drawable.plv_orange_warning_bg;
                } else {
                    level = PLVAppUtils.getString(R.string.plv_streamer_check_voice_low_risk);
                    title = String.format(PLVAppUtils.getString(R.string.plv_streamer_check_voice_tips), level);
                    levelColor = PLVFormatUtils.parseColor("#EABF96");
                    bgResource = R.drawable.plv_yellow_warning_bg;
                }
                levelTitleTv.setText(title);
                levelTv.setText(level);
                levelTv.setTextColor(levelColor);
                levelTv.setBackgroundResource(bgResource);
                contentTv.setText("“" + badWordBean.getContent() + "”");

                String remindCount = badwords.size() > 1 ? " (" + badwords.size() + ")" : "";
                goDetailTv1.setText(getResources().getString(R.string.plv_streamer_check_voice_read_remind_tips, remindCount));
            }
        }
    }

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }

    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {

        @Override
        public void onCheckVoiceWarning(PLVCheckVoiceEvent checkVoiceEvent) {
            post(new Runnable() {
                @Override
                public void run() {
                    acceptCheckVoiceEvent(checkVoiceEvent);
                }
            });
        }
    };

    private class WarningContentAdapter extends RecyclerView.Adapter<WarningContentAdapter.ViewHolder> {
        private List<PLVCheckVoiceEvent.BadWordBean> badWordList;

        public void setData(List<PLVCheckVoiceEvent.BadWordBean> badWordList) {
            this.badWordList = badWordList;
            notifyDataSetChanged();
        }

        public void addDataFirst(List<PLVCheckVoiceEvent.BadWordBean> badWordList) {
            if (this.badWordList == null) {
                this.badWordList = new ArrayList<>();
            }
            this.badWordList.addAll(0, badWordList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_check_voice_warning_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PLVCheckVoiceEvent.BadWordBean badWordBean = badWordList.get(position);
            holder.warningTimeTv.setText(PLVTimeUtils.formatMillisToHHMMSS(badWordBean.get_timestamp()));

            String level;
            int levelColor;
            if (badWordBean.getScore() > 0.8f) {
                level = PLVAppUtils.getString(R.string.plv_streamer_check_voice_high_risk);
                levelColor = PLVFormatUtils.parseColor("#E14138");
            } else if (badWordBean.getScore() > 0.5f) {
                level = PLVAppUtils.getString(R.string.plv_streamer_check_voice_medium_risk);
                levelColor = PLVFormatUtils.parseColor("#F4BA75");
            } else {
                level = PLVAppUtils.getString(R.string.plv_streamer_check_voice_low_risk);
                levelColor = PLVFormatUtils.parseColor("#EABF96");
            }
            holder.warningLevelTitleTv.setText(level);
            holder.warningLevelTitleTv.setTextColor(levelColor);

            holder.warningContentTv.setText("“" + badWordBean.getContent() + "”");
            holder.warningContentTv.setTextColor(levelColor);

            holder.warningTypeTv.setText(badWordBean.getType());

            holder.warningTypeLevelTv.setText(level);
            holder.warningTypeLevelTv.setTextColor(levelColor);
        }

        @Override
        public int getItemCount() {
            return badWordList == null ? 0 : badWordList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView warningTimeTv;
            public TextView warningLevelTitleTv;
            public TextView warningContentTv;
            public TextView warningTypeTv;
            public TextView warningTypeLevelTv;

            public ViewHolder(View itemView) {
                super(itemView);
                warningTimeTv = itemView.findViewById(R.id.plv_warning_time_tv);
                warningLevelTitleTv = itemView.findViewById(R.id.plv_warning_level_title_tv);
                warningContentTv = itemView.findViewById(R.id.plv_warning_content_tv);
                warningTypeTv = itemView.findViewById(R.id.plv_warning_type_tv);
                warningTypeLevelTv = itemView.findViewById(R.id.plv_warning_type_level_tv);
            }
        }
    }
}
