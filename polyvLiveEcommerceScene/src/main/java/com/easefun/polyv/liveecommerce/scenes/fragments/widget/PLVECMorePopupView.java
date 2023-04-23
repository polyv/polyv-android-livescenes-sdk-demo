package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_DEFINITION;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_FLOATING;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_LATENCY;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_PLAY_MODE;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_ROUTE;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.plv.business.model.video.PLVMediaPlayMode;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.model.interact.PLVChatFunctionVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;

import java.util.List;

/**
 * 更多-弹窗view
 */
public class PLVECMorePopupView {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播更多布局
    private PopupWindow liveMorePopupWindow;
    private PLVECMoreLayout moreLayout;
    //直播切换线路布局
    private PopupWindow linesChangePopupWindow;
    private ViewGroup changeLinesLy;
    //直播切换清晰度布局
    private PopupWindow definitionPopupWindow;
    private ViewGroup changeDefinitionLy;

    private PLVECMoreLatencyPopupView latencyPopupView;

    //监听器
    private OnLiveMoreClickListener liveMoreClickListener;

    //回放更多布局
    private PopupWindow playbackMorePopupWindow;
    //回放切换倍速布局
    private ViewGroup changeSpeedLy;
    //监听器
    private OnPlaybackMoreClickListener playbackMoreClickListener;

    //播放状态view的显示状态
    private int playStatusViewVisibility = View.GONE;

    private boolean isJoinRtcChannel = false;
    private boolean isJoinLinkMic = false;
    private boolean isVideoMode = true;
    private boolean isLowLatencyWatching = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();
    //是否有清晰度信息
    private boolean isHasDefinitionVO;
    //是否有多线路信息
    private boolean isHasLinesInfo;

    private SparseArray<Float> speedArray;

    public PLVECMorePopupView() {
        speedArray = new SparseArray<>();
        speedArray.put(0, 0.5f);
        speedArray.put(1, 1f);
        speedArray.put(2, 1.25f);
        speedArray.put(3, 1.5f);
        speedArray.put(4, 2.0f);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 直播更多布局控制">
    public void init(View v) {
        if (liveMorePopupWindow == null) {
            liveMorePopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_live_more_popup_layout, liveMorePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            moreLayout = view.findViewById(R.id.plvec_more_ly);

            updateSubViewVisibility();
            observeFloatingPlayer(view.getContext());
        }
    }

    public void showLiveMoreLayout(final View v, boolean isCurrentVideoMode, final OnLiveMoreClickListener clickListener) {
        this.liveMoreClickListener = clickListener;
        if (latencyPopupView == null) {
            latencyPopupView = new PLVECMoreLatencyPopupView();
            latencyPopupView.init(v.getContext());
        }
        if (moreLayout != null) {
            moreLayout.setFunctionListener(new PLVECFunctionListener() {
                @Override
                public void onFunctionCallback(String type, String data, View iconView) {
                    switch (type) {
                        case MORE_FUNCTION_TYPE_PLAY_MODE:
                            if (clickListener != null) {
                                boolean result = clickListener.onPlayModeClick(iconView);
                                if (result) {
                                    PLVChatFunctionVO playModeFunction = moreLayout.getFunctionByType(MORE_FUNCTION_TYPE_PLAY_MODE);
                                    if (playModeFunction != null) {
                                        playModeFunction.setSelected(!iconView.isSelected());
                                        playModeFunction.setName(!playModeFunction.isSelected() ? "音频模式" : "视频模式");
                                        moreLayout.updateFunctionStatus(playModeFunction);
                                    }
                                    updateDefinitionViewVisibility();
                                    hideAll();
                                }
                            }
                            break;
                        case MORE_FUNCTION_TYPE_ROUTE:
                            if (clickListener != null) {
                                int[] lines = clickListener.onShowLinesClick(iconView);
                                showLinesChangeLayout(v, lines);
                                hideMoreWindow();
                            }
                            break;
                        case MORE_FUNCTION_TYPE_DEFINITION:
                            if (clickListener != null) {
                                Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair = clickListener.onShowDefinitionClick(iconView);
                                showDefinitionChangeLayout(v, listIntegerPair);
                                hideMoreWindow();
                            }
                            break;
                        case MORE_FUNCTION_TYPE_LATENCY:
                            if (clickListener == null) {
                                return;
                            }
                            latencyPopupView.show(clickListener.isCurrentLowLatencyMode());
                            hideMoreWindow();
                            break;
                        case MORE_FUNCTION_TYPE_FLOATING:
                            final PLVECFloatingWindow floatingWindow = PLVDependManager.getInstance().get(PLVECFloatingWindow.class);
                            floatingWindow.showByUser(!floatingWindow.isRequestingShowByUser());
                            hideAll();
                            break;
                        default:
                            if (clickListener != null) {
                                clickListener.onClickDynamicFunction(data);
                            }
                            hideAll();
                            break;
                    }
                }
            });
        }
        PLVChatFunctionVO playModeFunction = moreLayout.getFunctionByType(MORE_FUNCTION_TYPE_PLAY_MODE);
        if (playModeFunction != null) {
            playModeFunction.setSelected(!isCurrentVideoMode);
            playModeFunction.setName(!playModeFunction.isSelected() ? "音频模式" : "视频模式");
            moreLayout.updateFunctionStatus(playModeFunction);
        }
        liveMorePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    public void showLinesChangeLayout(View v, int[] lines) {
        if (linesChangePopupWindow == null) {
            linesChangePopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_live_more_lines_change_layout, linesChangePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeLinesLy = view.findViewById(R.id.change_lines_ly);
        }
        updateLinesView(lines);
        linesChangePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);

    }

    public void updateLinesView(final int[] lines) {
        isHasLinesInfo = lines[0] > 1;
        if (moreLayout != null) {
            updateLineViewVisibility();
        }
        if (changeLinesLy != null) {
            if (!isHasLinesInfo) {
                changeLinesLy.setVisibility(View.GONE);
                return;
            } else {
                changeLinesLy.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < changeLinesLy.getChildCount(); i++) {
                View view = changeLinesLy.getChildAt(i);
                final int finalI = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateLinesView(new int[]{lines[0], finalI});
                        if (liveMoreClickListener != null) {
                            liveMoreClickListener.onLinesChangeClick(v, finalI);
                        }
                        hideAll();
                    }
                });
                view.setSelected(false);
                if (i <= lines[0] - 1) {
                    view.setVisibility(View.VISIBLE);
                    if (i == lines[1]) {
                        view.setSelected(true);
                    }
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public void updateDefinitionView(final Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair) {
        isHasDefinitionVO = listIntegerPair.first != null;
        if (moreLayout != null) {
            updateDefinitionViewVisibility();
        }
        if (changeDefinitionLy != null) {
            if (!isHasDefinitionVO) {
                changeDefinitionLy.setVisibility(View.GONE);
                return;
            } else {
                changeDefinitionLy.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < changeDefinitionLy.getChildCount(); i++) {
                View view = changeDefinitionLy.getChildAt(i);
                final int finalI = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDefinitionView(listIntegerPair);
                        if (liveMoreClickListener != null) {
                            liveMoreClickListener.onDefinitionChangeClick(v, finalI);
                        }
                        hideAll();
                    }
                });
                view.setSelected(false);
                if (i <= listIntegerPair.first.size() - 1) {
                    if (view instanceof TextView) {
                        ((TextView) view).setText(listIntegerPair.first.get(i).getDefinition());
                    }
                    view.setVisibility(View.VISIBLE);
                    if (i == listIntegerPair.second) {
                        view.setSelected(true);
                    }
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public void showDefinitionChangeLayout(View v, Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair) {
        if (definitionPopupWindow == null) {
            definitionPopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_live_more_definition_change_layout, definitionPopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeDefinitionLy = view.findViewById(R.id.change_definition_ly);
        }
        updateDefinitionView(listIntegerPair);
        definitionPopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    //暖场/无直播时隐藏切换音视频模式、切换线路相关的按钮
    public void updatePlayStateView(int visibility) {
        this.playStatusViewVisibility = visibility;
        updateSubViewVisibility();
    }

    public void updatePlayMode(int playMode) {
        this.isVideoMode = playMode == PLVMediaPlayMode.MODE_VIDEO;
        updateDefinitionViewVisibility();
    }

    public void updateLatencyMode(boolean isLowLatency) {
        this.isLowLatencyWatching = isLowLatency;
        updateSubViewVisibility();
    }

    public void updateJoinRTCChannel(boolean isJoinRtcChannel) {
        this.isJoinRtcChannel = isJoinRtcChannel;
        updateSubViewVisibility();
    }

    public void updateJoinLinkMic(boolean isJoinLinkMic) {
        this.isJoinLinkMic = isJoinLinkMic;
        updateSubViewVisibility();
    }

    public void acceptInteractStatusData(PLVWebviewUpdateAppStatusVO webviewUpdateAppStatusVO) {
        if (moreLayout != null) {
            moreLayout.updateFunctionView(webviewUpdateAppStatusVO);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API-  回放更多布局控制">
    public void showPlaybackMoreLayout(View v, float currentSpeed, OnPlaybackMoreClickListener clickListener) {
        this.playbackMoreClickListener = clickListener;
        if (playbackMorePopupWindow == null) {
            playbackMorePopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_playback_more_speed_change_layout, playbackMorePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeSpeedLy = view.findViewById(R.id.change_speed_ly);
        }
        updateSpeedView(currentSpeed);
        playbackMorePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    public void updateSpeedView(float currentSpeed) {
        if (changeSpeedLy != null) {
            for (int i = 0; i < changeSpeedLy.getChildCount(); i++) {
                View view = changeSpeedLy.getChildAt(i);
                final int finalI = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateSpeedView(speedArray.get(finalI));
                        if (playbackMoreClickListener != null) {
                            playbackMoreClickListener.onChangeSpeedClick(v, speedArray.get(finalI));
                        }
                        hideAll();
                    }
                });
                view.setSelected(false);
                if (speedArray.valueAt(i).equals(currentSpeed)) {
                    view.setSelected(true);
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 弹窗控制">
    public void hideMoreWindow() {
        if (liveMorePopupWindow != null) {
            liveMorePopupWindow.dismiss();
        }
    }

    public void hideAll() {
        if (liveMorePopupWindow != null) {
            liveMorePopupWindow.dismiss();
        }
        if (linesChangePopupWindow != null) {
            linesChangePopupWindow.dismiss();
        }
        if (definitionPopupWindow != null) {
            definitionPopupWindow.dismiss();
        }
        if (playbackMorePopupWindow != null) {
            playbackMorePopupWindow.dismiss();
        }
        if (latencyPopupView != null) {
            latencyPopupView.hide();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化popupWindow配置">
    private View initPopupWindow(View v, @LayoutRes int resource, final PopupWindow popupWindow) {
        View root = LayoutInflater.from(v.getContext()).inflate(resource, null, false);
        popupWindow.setContentView(root);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAll();
            }
        });
        View closeBt = root.findViewById(R.id.plvec_playback_more_dialog_close_iv);
        if (closeBt != null) {
            closeBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideAll();
                }
            });
        }
        return root;
    }

    private void observeFloatingPlayer(final Context context) {
        PLVFloatingPlayerManager.getInstance().getFloatingViewShowState()
                .observe((LifecycleOwner) context, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        if (aBoolean != null) {
                            updateSubViewVisibility();
                        }
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理 - UI显示">

    private void updateSubViewVisibility() {
        updatePlayModeVisibility();
        updateLineViewVisibility();
        updateDefinitionViewVisibility();
        updateLatencyLayoutVisibility();
        updateFloatingControlVisibility();
    }

    private void updatePlayModeVisibility() {
        if (moreLayout == null) {
            return;
        }
        final boolean mediaPlaying = playStatusViewVisibility == View.VISIBLE;
        final boolean showPlayMode = mediaPlaying && !isLowLatencyWatching && !isJoinRtcChannel;
        moreLayout.updateFunctionShow(MORE_FUNCTION_TYPE_PLAY_MODE, showPlayMode);
    }

    private void updateLineViewVisibility() {
        if (moreLayout == null) {
            return;
        }
        final boolean supportMultiLine = isHasLinesInfo;
        final boolean mediaPlaying = playStatusViewVisibility == View.VISIBLE;
        final boolean showChangeLine = supportMultiLine && mediaPlaying && !isLowLatencyWatching && !isJoinRtcChannel;
        moreLayout.updateFunctionShow(MORE_FUNCTION_TYPE_ROUTE, showChangeLine);
    }

    private void updateDefinitionViewVisibility() {
        if (moreLayout == null) {
            return;
        }
        final boolean supportMultiDefinition = isHasDefinitionVO;
        final boolean videoPlaying = playStatusViewVisibility == View.VISIBLE && isVideoMode;
        final boolean showChangeDefinition = supportMultiDefinition && videoPlaying && !isLowLatencyWatching && !isJoinRtcChannel;
        moreLayout.updateFunctionShow(MORE_FUNCTION_TYPE_DEFINITION, showChangeDefinition);
    }

    private void updateLatencyLayoutVisibility() {
        if (moreLayout == null) {
            return;
        }
        final boolean supportLowLatencyWatch = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();
        final boolean mediaPlaying = playStatusViewVisibility == View.VISIBLE;
        final boolean isFloatingPlayerShowing = PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing();
        final boolean showLatencyLayout = supportLowLatencyWatch && mediaPlaying && !isFloatingPlayerShowing && !isJoinLinkMic;
        moreLayout.updateFunctionShow(MORE_FUNCTION_TYPE_LATENCY, showLatencyLayout);
    }

    private void updateFloatingControlVisibility() {
        if (moreLayout == null) {
            return;
        }
        final boolean mediaPlaying = playStatusViewVisibility == View.VISIBLE && !isJoinLinkMic;
        moreLayout.updateFunctionShow(MORE_FUNCTION_TYPE_FLOATING, mediaPlaying);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 子布局">

    /**
     * 延迟切换弹层布局
     */
    private class PLVECMoreLatencyPopupView implements View.OnClickListener {

        private PopupWindow latencyPopupWindow;
        private View rootView;
        private LinearLayout liveMoreLatencyLy;
        private ImageView liveMoreLatencyBackIv;
        private LinearLayout liveMoreLatencyLl;
        private TextView liveMoreLowLatencyTv;
        private TextView liveMoreNormalLatencyTv;
        private ImageView liveMoreLatencyCloseIv;

        public void init(Context context) {
            rootView = LayoutInflater.from(context).inflate(R.layout.plvec_live_more_latency_layout, null);
            liveMoreLatencyLy = rootView.findViewById(R.id.plvec_live_more_latency_ly);
            liveMoreLatencyBackIv = rootView.findViewById(R.id.plvec_live_more_latency_back_iv);
            liveMoreLatencyLl = rootView.findViewById(R.id.plvec_live_more_latency_ll);
            liveMoreLowLatencyTv = rootView.findViewById(R.id.plvec_live_more_low_latency_tv);
            liveMoreNormalLatencyTv = rootView.findViewById(R.id.plvec_live_more_normal_latency_tv);
            liveMoreLatencyCloseIv = rootView.findViewById(R.id.plvec_live_more_latency_close_iv);

            PLVBlurUtils.initBlurView((PLVBlurView) rootView.findViewById(R.id.blur_ly));

            rootView.setOnClickListener(this);
            liveMoreLatencyBackIv.setOnClickListener(this);
            liveMoreLowLatencyTv.setOnClickListener(this);
            liveMoreNormalLatencyTv.setOnClickListener(this);
            liveMoreLatencyCloseIv.setOnClickListener(this);

            initPopupWindow(context);
        }

        private void initPopupWindow(Context context) {
            latencyPopupWindow = new PopupWindow(context);
            latencyPopupWindow.setContentView(rootView);
            latencyPopupWindow.setFocusable(true);
            latencyPopupWindow.setOutsideTouchable(true);
            latencyPopupWindow.setBackgroundDrawable(new ColorDrawable());
            latencyPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            latencyPopupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        }

        public void show(boolean isCurrentLowLatency) {
            liveMoreLowLatencyTv.setSelected(isCurrentLowLatency);
            liveMoreNormalLatencyTv.setSelected(!isCurrentLowLatency);
            latencyPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        }

        public void hide() {
            latencyPopupWindow.dismiss();
        }

        @Override
        public void onClick(View v) {
            final int viewId = v.getId();
            if (viewId == liveMoreLowLatencyTv.getId()) {
                // 切换到无延迟观看时，需要切换到视频观看模式
                PLVChatFunctionVO playModeFunction = moreLayout.getFunctionByType(MORE_FUNCTION_TYPE_PLAY_MODE);
                if (liveMoreClickListener != null && playModeFunction != null && playModeFunction.isSelected()) {
                    boolean result = liveMoreClickListener.onPlayModeClick(playModeFunction.isSelected());
                    if (result) {
                        playModeFunction.setSelected(!playModeFunction.isSelected());
                        playModeFunction.setName(!playModeFunction.isSelected() ? "音频模式" : "视频模式");
                        moreLayout.updateFunctionStatus(playModeFunction);
                        updateDefinitionViewVisibility();
                    }
                }
                if (liveMoreClickListener != null) {
                    liveMoreClickListener.switchLowLatencyMode(true);
                }
                PLVECMorePopupView.this.hideAll();
            } else if (viewId == liveMoreNormalLatencyTv.getId()) {
                if (liveMoreClickListener != null) {
                    liveMoreClickListener.switchLowLatencyMode(false);
                }
                PLVECMorePopupView.this.hideAll();
            } else if (viewId == liveMoreLatencyBackIv.getId()) {
                hide();
            } else if (viewId == rootView.getId()
                    || viewId == liveMoreLatencyCloseIv.getId()) {
                PLVECMorePopupView.this.hideAll();
            }
        }

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnLiveMoreClickListener {
        //播放模式是否切换成功
        boolean onPlayModeClick(View view);

        //播放模式是否切换成功
        boolean onPlayModeClick(boolean viewSelected);

        //[线路总数，当前线路]
        int[] onShowLinesClick(View view);

        //切换线路
        void onLinesChangeClick(View view, int linesPos);

        //[清晰度信息，清晰度索引]
        Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view);

        //切换清晰度
        void onDefinitionChangeClick(View view, int definitionPos);

        boolean isCurrentLowLatencyMode();

        void switchLowLatencyMode(boolean isLowLatency);

        /**
         * 点击了动态功能控件
         *
         * @param event 动态功能的event data
         */
        void onClickDynamicFunction(String event);
    }

    public interface OnPlaybackMoreClickListener {
        void onChangeSpeedClick(View view, float speed);
    }
    // </editor-fold>
}
