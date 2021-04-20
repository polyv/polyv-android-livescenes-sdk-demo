package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.graphics.drawable.ColorDrawable;
import android.support.annotation.LayoutRes;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.liveecommerce.R;

import java.util.List;

/**
 * 更多-弹窗view
 */
public class PLVECMorePopupView {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播更多布局
    private PopupWindow liveMorePopupWindow;
    private ImageView playModeIv;
    private TextView playModeTv;
    private ImageView changeLinesIv;
    private ImageView changeDefinitionIv;
    //直播切换线路布局
    private PopupWindow linesChangePopupWindow;
    private ViewGroup changeLinesLy;
    //直播切换清晰度布局
    private PopupWindow definitionPopupWindow;
    private ViewGroup changeDefinitionLy;
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
    //播放模式view的显示状态
    private int playModeViewVisibility = View.GONE;
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
    public void showLiveMoreLayout(final View v, boolean isCurrentVideoMode, final OnLiveMoreClickListener clickListener) {
        this.liveMoreClickListener = clickListener;
        if (liveMorePopupWindow == null) {
            liveMorePopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_live_more_layout, liveMorePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            playModeIv = view.findViewById(R.id.play_mode_iv);
            playModeTv = view.findViewById(R.id.play_mode_tv);
            changeLinesIv = view.findViewById(R.id.change_lines_iv);
            changeDefinitionIv = view.findViewById(R.id.change_definition_iv);
            ((ViewGroup) playModeIv.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        boolean result = clickListener.onPlayModeClick(playModeIv);
                        if (result) {
                            playModeIv.setSelected(!playModeIv.isSelected());
                            playModeTv.setText(!playModeIv.isSelected() ? "音频模式" : "视频模式");
                            if (playModeIv.isSelected()) {
                                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.GONE);
                            } else if (isHasDefinitionVO) {
                                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.VISIBLE);
                            }
                            hide();
                        }
                    }
                }
            });
            ((ViewGroup) changeLinesIv.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        int[] lines = clickListener.onShowLinesClick(changeLinesIv);
                        showLinesChangeLayout(v, lines);
                    }
                }
            });
            ((ViewGroup) changeDefinitionIv.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair = clickListener.onShowDefinitionClick(changeDefinitionIv);
                        showDefinitionChangeLayout(v, listIntegerPair);
                    }
                }
            });
            if (playStatusViewVisibility == View.VISIBLE) {
                ((ViewGroup) playModeIv.getParent()).setVisibility(playStatusViewVisibility);
                if (isHasLinesInfo) {
                    ((ViewGroup) changeLinesIv.getParent()).setVisibility(playStatusViewVisibility);
                }
                if (playModeViewVisibility == View.VISIBLE && isHasDefinitionVO) {
                    ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(playStatusViewVisibility);
                }
            }
        }
        playModeIv.setSelected(!isCurrentVideoMode);
        playModeTv.setText(!playModeIv.isSelected() ? "音频模式" : "视频模式");
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
        if (changeLinesIv != null) {
            if (!isHasLinesInfo) {
                ((ViewGroup) changeLinesIv.getParent()).setVisibility(View.GONE);
            } else if (playStatusViewVisibility == View.VISIBLE) {
                ((ViewGroup) changeLinesIv.getParent()).setVisibility(View.VISIBLE);
            }
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
                        hide();
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
        if (changeDefinitionIv != null) {
            if (!isHasDefinitionVO) {
                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.GONE);
            } else if (playModeViewVisibility == View.VISIBLE) {
                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.VISIBLE);
            }
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
                        hide();
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
        if (liveMorePopupWindow != null) {
            ((ViewGroup) playModeIv.getParent()).setVisibility(visibility);
            if (visibility != View.VISIBLE) {//根据视频是否支持线路切换来显示
                ((ViewGroup) changeLinesIv.getParent()).setVisibility(visibility);
            }
            if (visibility != View.VISIBLE) {//根据视频是否支持多码率切换来显示
                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(visibility);
            }
        }
    }

    //音频模式时隐藏切换清晰度的按钮
    public void updatePlayModeView(int visibility) {
        this.playModeViewVisibility = visibility;
        if (liveMorePopupWindow != null && visibility != View.VISIBLE) {
            //根据视频是否支持多码率切换来显示
            ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(visibility);
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
                        hide();
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
    public void hide() {
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
                hide();
            }
        });
        View closeBt = root.findViewById(R.id.close_iv);
        if (closeBt != null) {
            closeBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            });
        }
        return root;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnLiveMoreClickListener {
        //播放模式是否切换成功
        boolean onPlayModeClick(View view);

        //[线路总数，当前线路]
        int[] onShowLinesClick(View view);

        //切换线路
        void onLinesChangeClick(View view, int linesPos);

        //[清晰度信息，清晰度索引]
        Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view);

        //切换清晰度
        void onDefinitionChangeClick(View view, int definitionPos);
    }

    public interface OnPlaybackMoreClickListener {
        void onChangeSpeedClick(View view, float speed);
    }
    // </editor-fold>
}
