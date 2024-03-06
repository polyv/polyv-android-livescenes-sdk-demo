package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_DEFINITION;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_FLOATING;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_LANGUAGE_SWITCH;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_LATENCY;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_PLAY_MODE;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_RATE;
import static com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMoreLayout.MORE_FUNCTION_TYPE_ROUTE;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.ui.widget.PLVLanguageSwitchPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.plv.business.model.video.PLVMediaPlayMode;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.model.interact.PLVChatFunctionVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

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
    //语言切换弹窗
    private PLVLanguageSwitchPopupWindow languageSwitchPopupWindow;
    //播放速度弹窗
    private PLVSpeedPopupView speedPopupView;

    private PLVECMoreLatencyPopupView latencyPopupView;

    //监听器
    private OnLiveMoreClickListener liveMoreClickListener;

    //回放更多布局
    private PopupWindow playbackMorePopupWindow;
    //监听器
    private OnPlaybackMoreClickListener playbackMoreClickListener;

    //播放状态view的显示状态
    private int playStatusViewVisibility = View.GONE;
    private boolean enableSpeedControl = true;

    private boolean isJoinRtcChannel = false;
    private boolean isJoinLinkMic = false;
    private boolean isVideoMode = true;
    private boolean isLowLatencyWatching = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();
    //是否有清晰度信息
    private boolean isHasDefinitionVO;
    //是否有多线路信息
    private boolean isHasLinesInfo;

    //更多弹窗布局
    private View rootview;
    private PLVOrientationSensibleLinearLayout orientationLayout;
    private PLVRoundRectLayout roundBgRl;
    private LinearLayout moreLinearLayout;
    private TextView titleTv;

    //当前是否是处于横屏状态
    private boolean isOnLandscape = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 直播更多布局控制">
    public void initLiveMoreLayout(View v) {
        if (liveMorePopupWindow == null) {
            liveMorePopupWindow = new PopupWindow(v.getContext());
            rootview = initPopupWindow(v, R.layout.plvec_live_more_popup_layout, liveMorePopupWindow);

            orientationLayout = rootview.findViewById(R.id.plvec_more_pop_vertical_ly);
            roundBgRl = rootview.findViewById(R.id.plvec_widget_round_ly);
            moreLinearLayout = rootview.findViewById(R.id.more_ly);
            titleTv = rootview.findViewById(R.id.plvec_popup_title_tv);

            PLVBlurUtils.initBlurView((PLVBlurView) rootview.findViewById(R.id.blur_ly));
            moreLayout = rootview.findViewById(R.id.plvec_more_ly);

            orientationLayout.setOnLandscape(new Runnable() {
                @Override
                public void run() {
                    onLandscape();
                }
            });

            orientationLayout.setOnPortrait(new Runnable() {
                @Override
                public void run() {
                    onPortrait();
                }
            });

            updateSubViewVisibility();
            observeFloatingPlayer(rootview.getContext());
        }
    }

    public void initPlaybackMoreLayout(View v) {
        if (playbackMorePopupWindow == null) {
            playbackMorePopupWindow = new PopupWindow(v.getContext());
            rootview = initPopupWindow(v, R.layout.plvec_live_more_popup_layout, playbackMorePopupWindow);
            titleTv = rootview.findViewById(R.id.plvec_popup_title_tv);
            orientationLayout = rootview.findViewById(R.id.plvec_more_pop_vertical_ly);

            orientationLayout.setOnLandscape(new Runnable() {
                @Override
                public void run() {
                    onLandscape();
                }
            });

            orientationLayout.setOnPortrait(new Runnable() {
                @Override
                public void run() {
                    onPortrait();
                }
            });

            PLVBlurUtils.initBlurView((PLVBlurView) rootview.findViewById(R.id.blur_ly));
            moreLayout = rootview.findViewById(R.id.plvec_more_ly);

        }
    }

    public void onPortrait() {
        isOnLandscape = false;
        initPortraitChangeLayout(rootview,liveMorePopupWindow);
        initPortraitChangeLayout(rootview,playbackMorePopupWindow);
        titleTv.setVisibility(View.GONE);

    }

    public void onLandscape() {
        isOnLandscape = true;
        initLandscapeChangeLayout(rootview,liveMorePopupWindow);
        initLandscapeChangeLayout(rootview,playbackMorePopupWindow);
        if (titleTv != null) {
            titleTv.setVisibility(View.VISIBLE);
        }

    }

    public void showLiveMoreLayout(final View v, boolean isCurrentVideoMode, final String channelId, final OnLiveMoreClickListener clickListener) {
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
                                        playModeFunction.setName(PLVAppUtils.getString(!playModeFunction.isSelected() ? R.string.plv_player_audio_mode : R.string.plv_player_video_mode));
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
                        case MORE_FUNCTION_TYPE_LANGUAGE_SWITCH:
                            if (languageSwitchPopupWindow == null) {
                                languageSwitchPopupWindow = new PLVLanguageSwitchPopupWindow(v);
                            }
                            languageSwitchPopupWindow.show(channelId);
                            hideMoreWindow();
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
            playModeFunction.setName(PLVAppUtils.getString(!playModeFunction.isSelected() ? R.string.plv_player_audio_mode : R.string.plv_player_video_mode));
            moreLayout.updateFunctionStatus(playModeFunction);
        }
        liveMorePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    public void showLinesChangeLayout(View v, int[] lines) {
        if (linesChangePopupWindow == null) {
            linesChangePopupWindow = new PopupWindow(v.getContext());
            final View view = initPopupWindow(v, R.layout.plvec_live_more_lines_change_layout, linesChangePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeLinesLy = view.findViewById(R.id.change_lines_ly);
            PLVOrientationSensibleLinearLayout orientationLinesLayout = view.findViewById(R.id.plvec_more_lines_pop_vertical_ly);
            orientationLinesLayout.setOnPortrait(new Runnable() {
                @Override
                public void run() {
                    initPortraitChangeLayout(linesChangePopupWindow.getContentView(), linesChangePopupWindow);
                }
            });

            orientationLinesLayout.setOnLandscape(new Runnable() {
                @Override
                public void run() {
                    initLandscapeChangeLayout(linesChangePopupWindow.getContentView(), linesChangePopupWindow);
                }
            });

        }
        updateLinesView(lines);
        if (isOnLandscape) {
            initLandscapeChangeLayout(linesChangePopupWindow.getContentView(),linesChangePopupWindow);
            linesChangePopupWindow.showAtLocation(v, Gravity.RIGHT, 0, 0);
        } else {
            initPortraitChangeLayout(linesChangePopupWindow.getContentView(),linesChangePopupWindow);
            linesChangePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
        }

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
            final View view = initPopupWindow(v, R.layout.plvec_live_more_definition_change_layout, definitionPopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeDefinitionLy = view.findViewById(R.id.change_definition_ly);
            PLVOrientationSensibleLinearLayout definition = view.findViewById(R.id.plvec_more_definition_pop_vertical_ly);
            definition.setOnLandscape(new Runnable() {
                @Override
                public void run() {
                    initLandscapeChangeLayout(view, definitionPopupWindow);
                }
            });

            definition.setOnPortrait(new Runnable() {
                @Override
                public void run() {
                    initPortraitChangeLayout(view, definitionPopupWindow);
                }
            });
        }
        updateDefinitionView(listIntegerPair);
        if (isOnLandscape) {
            initLandscapeChangeLayout(definitionPopupWindow.getContentView(), definitionPopupWindow);
            definitionPopupWindow.showAtLocation(v, Gravity.RIGHT, 0, 0);
        } else {
            initPortraitChangeLayout(definitionPopupWindow.getContentView(), definitionPopupWindow);
            definitionPopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    private void initPortraitChangeLayout(View view, PopupWindow popupWindow) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        View linesRoundBg = view.findViewById(R.id.plvec_widget_round_ly);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) linesRoundBg.getLayoutParams();
        layoutParams.height = ConvertUtils.dp2px(130);
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        linesRoundBg.setLayoutParams(layoutParams);

        View morely = view.findViewById(R.id.more_ly);
        RelativeLayout.LayoutParams morelyParams = (RelativeLayout.LayoutParams) morely.getLayoutParams();
        morelyParams.height = ConvertUtils.dp2px(130);
        morelyParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        morelyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        morelyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        morely.setLayoutParams(morelyParams);
    }

    private void initLandscapeChangeLayout(View view, PopupWindow popupWindow) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        View linesRoundBg = view.findViewById(R.id.plvec_widget_round_ly);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) linesRoundBg.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ConvertUtils.dp2px(375);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        linesRoundBg.setLayoutParams(layoutParams);

        View morely = view.findViewById(R.id.more_ly);
        RelativeLayout.LayoutParams morelyParams = (RelativeLayout.LayoutParams) morely.getLayoutParams();
        morelyParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        morelyParams.width = ConvertUtils.dp2px(375);
        morelyParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        morelyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        morely.setLayoutParams(morelyParams);
    }

    public void setEnableSpeedControl(boolean enableSpeedControl) {
        this.enableSpeedControl = enableSpeedControl;
        updateSpeedControlVisibility();
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
    public void showPlaybackMoreLayout(final View v, final float currentSpeed, final String channelId, final OnPlaybackMoreClickListener clickListener) {
        this.playbackMoreClickListener = clickListener;
        if (moreLayout != null) {
            moreLayout.setFunctionListener(new PLVECFunctionListener() {
                @Override
                public void onFunctionCallback(String type, String data, View iconView) {
                    switch (type) {
                        case MORE_FUNCTION_TYPE_RATE:
                            if (speedPopupView == null) {
                                speedPopupView = new PLVSpeedPopupView(v);
                            }
                            speedPopupView.show(isOnLandscape, currentSpeed, new PLVSpeedPopupView.OnViewActionListener() {
                                @Override
                                public void onChangeSpeedClick(View v, float speed) {
                                    if (playbackMoreClickListener != null) {
                                        playbackMoreClickListener.onChangeSpeedClick(v, speed);
                                    }
                                    hideAll();
                                }
                            });
                            hideMoreWindow();
                            break;
                        case MORE_FUNCTION_TYPE_LANGUAGE_SWITCH:
                            if (languageSwitchPopupWindow == null) {
                                languageSwitchPopupWindow = new PLVLanguageSwitchPopupWindow(v);
                            }
                            languageSwitchPopupWindow.show(channelId);
                            hideMoreWindow();
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
        playbackMorePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 弹窗控制">
    public void hideMoreWindow() {
        if (liveMorePopupWindow != null) {
            liveMorePopupWindow.dismiss();
        }
        if (playbackMorePopupWindow != null) {
            playbackMorePopupWindow.dismiss();
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
        if (speedPopupView != null) {
            speedPopupView.dismiss();
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
        updateSpeedControlVisibility();
        updatePlayModeVisibility();
        updateLineViewVisibility();
        updateDefinitionViewVisibility();
        updateLatencyLayoutVisibility();
        updateFloatingControlVisibility();
    }

    private void updateSpeedControlVisibility() {
        if (moreLayout == null) {
            return;
        }
        moreLayout.updateFunctionShow(MORE_FUNCTION_TYPE_RATE, enableSpeedControl);
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
            liveMoreLatencyLy = rootView.findViewById(R.id.more_ly);
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
            PLVOrientationSensibleLinearLayout definition = rootView.findViewById(R.id.plvec_more_latency_pop_vertical_ly);
            definition.setOnLandscape(new Runnable() {
                @Override
                public void run() {
                    initLandscapeChangeLayout(rootView, latencyPopupWindow);
                }
            });
            definition.setOnPortrait(new Runnable() {
                @Override
                public void run() {
                    initPortraitChangeLayout(rootView, latencyPopupWindow);
                }
            });
            if (isOnLandscape) {
                initLandscapeChangeLayout(rootView, latencyPopupWindow);
                latencyPopupWindow.showAtLocation(rootView, Gravity.RIGHT, 0, 0);
            } else {
                initPortraitChangeLayout(rootView, latencyPopupWindow);
                latencyPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
            }
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
                        playModeFunction.setName(PLVAppUtils.getString(!playModeFunction.isSelected() ? R.string.plv_player_audio_mode : R.string.plv_player_video_mode));
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

        /**
         * 点击了动态功能控件
         *
         * @param event 动态功能的event data
         */
        void onClickDynamicFunction(String event);
    }
    // </editor-fold>
}
