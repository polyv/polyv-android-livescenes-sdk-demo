package com.easefun.polyv.livecloudclass.modules.media.widget;

import static com.plv.foundationsdk.component.exts.LangsKt.isLiteralTrue;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.download.layout.PLVLCPlaybackCachePopupLayout;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheVideoViewModel;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.playback.subtitle.vo.PLVPlaybackSubtitleVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.functions.Function1;

/**
 * date: 2019/6/10 0010
 *
 * @author hwj
 * description 回放控制栏右上角的“更多”布局
 */
public class PLVLCPlaybackMoreLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVPlaybackCacheVideoViewModel playbackCacheVideoViewModel = PLVDependManager.getInstance().get(PLVPlaybackCacheVideoViewModel.class);

    //popupWindow
    private PopupWindow popupWindow;
    //View
    private View root;
    private View anchor;
    private ViewGroup containerLy;
    private PLVOrientationSensibleLinearLayout llMoreVertical;
    private RecyclerView rvSpeed;
    private RvSpeedAdapter rvAdapter;
    private ViewGroup llSpeed;

    private TextView portraitSpeedTv;

    private TextView landscapeSpeedTv;
    private LinearLayout playbackCacheLl;
    private PLVLCPlaybackCachePopupLayout playbackCachePopupLayout;
    private ConstraintLayout playbackSubtitleSettingLayout;
    private TextView playbackSubtitleSettingTitle;
    private CheckBox playbackSubtitleSettingOriginalCheckbox;
    private CheckBox playbackSubtitleSettingTranslateCheckbox;
    private TextView playbackSubtitleSettingTranslateOptionTv;

    private TranslateSubtitlePopupWindow translateSubtitlePopupWindow;

    //callback
    private OnSpeedSelectedListener onSpeedSelectedListener;
    private OnSubtitleActionListener onSubtitleActionListener;

    private int portraitHeight;

    private View speedTipsLy;
    private PLVOrientationSensibleLinearLayout speedTipsContainerLy;
    private TextView speedTipsTv;

    //倍速列表数据
    private List<Float> speedVO = new ArrayList<>();

    private boolean enableSpeedControl = true;
    private PLVPlaybackSubtitleVO lastSelectTranslateSubtitle = null;

    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVLCPlaybackMoreLayout(View anchor) {
        this.anchor = anchor;

        speedVO.add(0.5f);
        speedVO.add(1.0f);
        speedVO.add(1.25f);
        speedVO.add(1.5f);
        speedVO.add(2.0f);
        speedVO.add(3.0f);

        if (popupWindow == null) {
            popupWindow = new PopupWindow(anchor.getContext());

            View.OnClickListener handleHideListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            };
            root = PLVViewInitUtils.initPopupWindow(anchor, R.layout.plvlc_playback_controller_more_layout, popupWindow, handleHideListener);
            initView();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        //父布局
        containerLy = root.findViewById(R.id.plvlc_danmu_container_ly);
        containerLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        //监听屏幕方向
        llMoreVertical = (PLVOrientationSensibleLinearLayout) root.findViewById(R.id.ll_more_vertical);
        llMoreVertical.setOnLandscape(new Runnable() {
            @Override
            public void run() {
                if (popupWindow != null && popupWindow.isShowing()) {
                    /**
                     * ///暂时先保留代码
                     * popupWindow.setClippingEnabled(true);
                     */
                    popupWindow.update();
                }
                onLandscape();
            }
        });
        llMoreVertical.setOnPortrait(new Runnable() {
            @Override
            public void run() {
                if (portraitHeight == 0) {
                    hide();
                    return;
                }
                if (popupWindow != null && popupWindow.isShowing()) {
                    /**
                     * ///暂时先保留代码
                     * popupWindow.setClippingEnabled(false);
                     */
                    popupWindow.update();
                }
                onPortrait();
            }
        });

        //倍速列表
        rvSpeed = root.findViewById(R.id.rv_more_speed);
        rvAdapter = new RvSpeedAdapter();
        rvSpeed.setAdapter(rvAdapter);
        rvSpeed.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false));

        llSpeed = root.findViewById(R.id.fl_speed);

        portraitSpeedTv = root.findViewById(R.id.portrait_speed_tv);

        landscapeSpeedTv = root.findViewById(R.id.landscape_speed_tv);

        playbackCacheLl = root.findViewById(R.id.plvlc_playback_cache_ll);
        playbackCachePopupLayout = new PLVLCPlaybackCachePopupLayout(root.getContext());
        playbackCacheLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playbackCachePopupLayout.show();
                PLVLCPlaybackMoreLayout.this.hide();
            }
        });

        playbackSubtitleSettingLayout = root.findViewById(R.id.plvlc_playback_subtitle_setting_layout);
        playbackSubtitleSettingTitle = root.findViewById(R.id.plvlc_playback_subtitle_setting_title);
        playbackSubtitleSettingOriginalCheckbox = root.findViewById(R.id.plvlc_playback_subtitle_setting_original_checkbox);
        playbackSubtitleSettingTranslateCheckbox = root.findViewById(R.id.plvlc_playback_subtitle_setting_translate_checkbox);
        playbackSubtitleSettingTranslateOptionTv = root.findViewById(R.id.plvlc_playback_subtitle_setting_translate_option_tv);
        translateSubtitlePopupWindow = new TranslateSubtitlePopupWindow(root);
        playbackSubtitleSettingOriginalCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (onSubtitleActionListener == null) {
                    return;
                }
                List<PLVPlaybackSubtitleVO> allSubtitles = onSubtitleActionListener.getAllSubtitleSettings();
                List<PLVPlaybackSubtitleVO> showSubtitles = onSubtitleActionListener.getShowSubtitles();
                List<PLVPlaybackSubtitleVO> setSubtitles = new ArrayList<>(showSubtitles);
                PLVPlaybackSubtitleVO originalSubtitle = PLVSequenceWrapper.wrap(allSubtitles).firstOrNull(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
                    @Override
                    public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                        return isLiteralTrue(vo.isOriginal());
                    }
                });
                if (isChecked) {
                    if (originalSubtitle != null && !setSubtitles.contains(originalSubtitle)) {
                        setSubtitles.add(0, originalSubtitle);
                    }
                } else {
                    setSubtitles.remove(originalSubtitle);
                }
                onSubtitleActionListener.setShowSubtitles(setSubtitles);
            }
        });
        playbackSubtitleSettingTranslateCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (onSubtitleActionListener == null) {
                    return;
                }
                List<PLVPlaybackSubtitleVO> showSubtitles = onSubtitleActionListener.getShowSubtitles();
                List<PLVPlaybackSubtitleVO> setSubtitles = new ArrayList<>(showSubtitles);
                if (isChecked) {
                    if (lastSelectTranslateSubtitle != null && !setSubtitles.contains(lastSelectTranslateSubtitle)) {
                        setSubtitles.add(lastSelectTranslateSubtitle);
                    }
                    onSubtitleActionListener.setShowSubtitles(setSubtitles);
                } else {
                    PLVPlaybackSubtitleVO translateSubtitle = PLVSequenceWrapper.wrap(showSubtitles).firstOrNull(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
                        @Override
                        public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                            return !isLiteralTrue(vo.isOriginal());
                        }
                    });
                    setSubtitles.remove(translateSubtitle);
                    onSubtitleActionListener.setShowSubtitles(setSubtitles);
                }
            }
        });
        playbackSubtitleSettingTranslateOptionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateSubtitlePopupWindow.show();
            }
        });

        observePlaybackCacheEnable((LifecycleOwner) root.getContext());
    }

    private void observePlaybackCacheEnable(@NonNull LifecycleOwner lifecycleOwner) {
        playbackCacheVideoViewModel.getPlaybackCacheUpdateLiveData().observe(lifecycleOwner, new Observer<PLVPlaybackCacheVideoVO>() {
            @Override
            public void onChanged(@Nullable PLVPlaybackCacheVideoVO vo) {
                if (vo == null) {
                    return;
                }
                final boolean enableDownload = vo.isEnableDownload() != null && vo.isEnableDownload();
                playbackCacheLl.setVisibility(enableDownload ? View.VISIBLE : View.GONE);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    // <editor-fold defaultstate="collapsed" desc="显示/隐藏控制">
    public void hide() {
        handler.removeCallbacksAndMessages(null);
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public void showWhenPortrait(int height) {
        this.portraitHeight = height;
        onPortrait();
        show(true);
    }

    public void showWhenLandscape() {
        onLandscape();
        show(false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置监听器">
    public void setOnSpeedSelectedListener(OnSpeedSelectedListener onSpeedSelectedListener) {
        this.onSpeedSelectedListener = onSpeedSelectedListener;
    }

    public void setOnSubtitleActionListener(OnSubtitleActionListener onSubtitleActionListener) {
        this.onSubtitleActionListener = onSubtitleActionListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="参数配置">
    public void initSpeed(List<Float> speedVO, int speedPos) {
        rvAdapter.updateSpeedListData(speedVO, speedPos);
        showSpeed(true);
    }

    public void setEnableSpeedControl(boolean enableSpeedControl) {
        this.enableSpeedControl = enableSpeedControl;
        showSpeed(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="状态更新">
    public void updateViewWithPlayInfo(float speed) {
        int speedPos = 1;
        for (int i = 0; i < speedVO.size(); i++) {
            if (speedVO.get(i) == speed) {
                speedPos = i;
                break;
            }
        }
        initSpeed(speedVO, speedPos);
    }

    public void updateSubtitleSetting() {
        if (onSubtitleActionListener == null) {
            return;
        }
        List<PLVPlaybackSubtitleVO> allSubtitles = onSubtitleActionListener.getAllSubtitleSettings();
        List<PLVPlaybackSubtitleVO> showSubtitles = onSubtitleActionListener.getShowSubtitles();
        boolean hasOriginalSubtitle = PLVSequenceWrapper.wrap(allSubtitles).any(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
            @Override
            public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                return isLiteralTrue(vo.isOriginal());
            }
        });
        boolean showOriginalSubtitle = PLVSequenceWrapper.wrap(showSubtitles).any(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
            @Override
            public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                return isLiteralTrue(vo.isOriginal());
            }
        });
        boolean hasTranslateSubtitle = PLVSequenceWrapper.wrap(allSubtitles).any(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
            @Override
            public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                return !isLiteralTrue(vo.isOriginal());
            }
        });
        boolean showTranslateSubtitle = PLVSequenceWrapper.wrap(showSubtitles).any(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
            @Override
            public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                return !isLiteralTrue(vo.isOriginal());
            }
        });
        PLVPlaybackSubtitleVO currentTranslateSubtitle = PLVSequenceWrapper.wrap(showSubtitles).firstOrNull(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
            @Override
            public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                return !isLiteralTrue(vo.isOriginal());
            }
        });
        playbackSubtitleSettingLayout.setVisibility(allSubtitles.isEmpty() ? View.GONE : View.VISIBLE);
        playbackSubtitleSettingOriginalCheckbox.setVisibility(hasOriginalSubtitle ? View.VISIBLE : View.GONE);
        playbackSubtitleSettingOriginalCheckbox.setChecked(showOriginalSubtitle);
        playbackSubtitleSettingTranslateCheckbox.setVisibility(hasTranslateSubtitle ? View.VISIBLE : View.GONE);
        playbackSubtitleSettingTranslateOptionTv.setVisibility(hasTranslateSubtitle ? View.VISIBLE : View.GONE);
        playbackSubtitleSettingTranslateCheckbox.setChecked(showTranslateSubtitle);
        if (currentTranslateSubtitle != null) {
            playbackSubtitleSettingTranslateOptionTv.setText(getLanguageText(currentTranslateSubtitle.getLanguage()));
            lastSelectTranslateSubtitle = currentTranslateSubtitle;
        } else if (lastSelectTranslateSubtitle != null) {
            playbackSubtitleSettingTranslateOptionTv.setText(getLanguageText(lastSelectTranslateSubtitle.getLanguage()));
        } else {
            playbackSubtitleSettingTranslateOptionTv.setText(R.string.plv_live_not_set);
        }
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="切换横竖屏">
    private void onLandscape() {
        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) containerLy.getLayoutParams();
        flp.width = -2;
        flp.height = -1;
        flp.gravity = Gravity.RIGHT;
        containerLy.setLayoutParams(flp);
        containerLy.setBackgroundColor(Color.parseColor("#CC000000"));

        LinearLayout.LayoutParams bitRvLp = (LinearLayout.LayoutParams) rvSpeed.getLayoutParams();
        bitRvLp.leftMargin = 0;
        rvSpeed.setLayoutParams(bitRvLp);

        portraitSpeedTv.setVisibility(View.GONE);
        if (llSpeed.getVisibility() == View.VISIBLE) {
            landscapeSpeedTv.setVisibility(View.VISIBLE);
        } else {
            landscapeSpeedTv.setVisibility(View.GONE);
        }
    }

    private void onPortrait() {
        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) containerLy.getLayoutParams();
        flp.width = -1;
        flp.height = portraitHeight;
        flp.gravity = Gravity.NO_GRAVITY;
        containerLy.setLayoutParams(flp);
        containerLy.setBackgroundColor(Color.parseColor("#D8000000"));

        LinearLayout.LayoutParams bitRvLp = (LinearLayout.LayoutParams) rvSpeed.getLayoutParams();
        bitRvLp.leftMargin = ConvertUtils.dp2px(24);
        rvSpeed.setLayoutParams(bitRvLp);

        portraitSpeedTv.setVisibility(View.VISIBLE);
        landscapeSpeedTv.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制UI显示">
    private void show(boolean isPort) {
        show(isPort, root);
    }

    private void show(boolean isPort, View contentView) {
        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                hide();
            }
            updateSubtitleSetting();
            popupWindow.setContentView(contentView);
            /**
             * ///暂时先保留代码
             * popupWindow.setClippingEnabled(!isPort);
             */
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    private void showSpeedTips(boolean isPort, String message) {
        if (speedTipsLy == null && anchor != null) {
            speedTipsLy = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plvlc_tips_view_speed, null, false);
            speedTipsContainerLy = speedTipsLy.findViewById(R.id.speed_tips_container_ly);
            speedTipsContainerLy.setOnLandscape(new Runnable() {
                @Override
                public void run() {
                    FrameLayout.LayoutParams speedLyFlp = (FrameLayout.LayoutParams) speedTipsContainerLy.getLayoutParams();
                    speedLyFlp.height = -1;
                    speedTipsContainerLy.setLayoutParams(speedLyFlp);
                }
            });
            speedTipsContainerLy.setOnPortrait(new Runnable() {
                @Override
                public void run() {
                    FrameLayout.LayoutParams speedLyFlp = (FrameLayout.LayoutParams) speedTipsContainerLy.getLayoutParams();
                    speedLyFlp.height = portraitHeight;
                    speedTipsContainerLy.setLayoutParams(speedLyFlp);
                }
            });
            speedTipsContainerLy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            });
            speedTipsTv = speedTipsLy.findViewById(R.id.speed_tips_tv);
        }
        if (speedTipsContainerLy != null) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) speedTipsContainerLy.getLayoutParams();
            flp.height = isPort ? portraitHeight : -1;
            speedTipsContainerLy.setLayoutParams(flp);
        }
        if (speedTipsTv != null) {
            speedTipsTv.setText(message);
        }
        show(isPort, speedTipsLy);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, 500);
    }

    public void showSpeed(boolean show) {
        if (show && enableSpeedControl && rvAdapter.getItemCount() > 1) {
            llSpeed.setVisibility(View.VISIBLE);
            if (ScreenUtils.isLandscape()) {
                landscapeSpeedTv.setVisibility(View.VISIBLE);
            }
        } else {
            llSpeed.setVisibility(View.GONE);
            if (ScreenUtils.isLandscape()) {
                landscapeSpeedTv.setVisibility(View.GONE);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="翻译字幕">

    private String getLanguageText(String language) {
        if (language == null) {
            return "";
        }
        int id = root.getContext().getResources().getIdentifier("plv_subtitle_language_" + language.toLowerCase(Locale.ROOT), "string", root.getContext().getPackageName());
        if (id != 0) {
            return root.getContext().getResources().getString(id);
        } else {
            return language;
        }
    }

    private class TranslateSubtitlePopupWindow {
        private String channelId;
        private PopupWindow popupWindow;
        private View root;
        private PLVRoundRectConstraintLayout playbackSubtitleTranslatePopupRoot;
        private TextView playbackSubtitleTranslateSelectTitle;
        private RecyclerView playbackSubtitleTranslateOptionRv;
        private TextView playbackSubtitleTranslateSelectCancelTv;

        private TranslateSubtitleAdapter adapter = new TranslateSubtitleAdapter();

        public TranslateSubtitlePopupWindow(View anchorView) {
            popupWindow = new PopupWindow(anchorView.getContext());
            this.root = PLVViewInitUtils.initPopupWindow(anchorView, R.layout.plvlc_playback_subtitle_translate_select_popup_window, popupWindow, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            playbackSubtitleTranslatePopupRoot = root.findViewById(R.id.plvlc_playback_subtitle_translate_popup_root);
            playbackSubtitleTranslateSelectTitle = root.findViewById(R.id.plvlc_playback_subtitle_translate_select_title);
            playbackSubtitleTranslateOptionRv = root.findViewById(R.id.plvlc_playback_subtitle_translate_option_rv);
            playbackSubtitleTranslateSelectCancelTv = root.findViewById(R.id.plvlc_playback_subtitle_translate_select_cancel_tv);

            playbackSubtitleTranslateSelectCancelTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            playbackSubtitleTranslateOptionRv.setAdapter(adapter);
            playbackSubtitleTranslateOptionRv.setLayoutManager(new LinearLayoutManager(anchorView.getContext(), LinearLayoutManager.VERTICAL, false));
        }

        public void show() {
            if (onSubtitleActionListener == null) {
                return;
            }
            List<PLVPlaybackSubtitleVO> allSubtitles = onSubtitleActionListener.getAllSubtitleSettings();
            List<PLVPlaybackSubtitleVO> allTranslateSubtitles = PLVSequenceWrapper.wrap(allSubtitles).filter(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
                @Override
                public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                    return !isLiteralTrue(vo.isOriginal());
                }
            }).toMutableList();
            adapter.updateAllSubtitles(allTranslateSubtitles);
            adapter.setOnSelectSubtitleListener(new PLVSugarUtil.Consumer<PLVPlaybackSubtitleVO>() {
                @Override
                public void accept(PLVPlaybackSubtitleVO vo) {
                    handleSwitch(vo);
                    dismiss();
                }
            });

            if (ScreenUtils.isPortrait()) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) playbackSubtitleTranslatePopupRoot.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ConvertUtils.dp2px(375);
                lp.gravity = Gravity.BOTTOM;
                playbackSubtitleTranslatePopupRoot.setLayoutParams(lp);
                popupWindow.showAtLocation(root, Gravity.BOTTOM, 0, 0);
            } else {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) playbackSubtitleTranslatePopupRoot.getLayoutParams();
                lp.width = ConvertUtils.dp2px(375);
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.gravity = Gravity.END;
                playbackSubtitleTranslatePopupRoot.setLayoutParams(lp);
                popupWindow.showAtLocation(root, Gravity.END, 0, 0);
            }
        }

        public void dismiss() {
            popupWindow.dismiss();
        }

        private void handleSwitch(PLVPlaybackSubtitleVO vo) {
            List<PLVPlaybackSubtitleVO> showSubtitles = onSubtitleActionListener.getShowSubtitles();
            PLVPlaybackSubtitleVO currentShowTranslateSubtitle = PLVSequenceWrapper.wrap(showSubtitles).firstOrNull(new Function1<PLVPlaybackSubtitleVO, Boolean>() {
                @Override
                public Boolean invoke(PLVPlaybackSubtitleVO vo) {
                    return !isLiteralTrue(vo.isOriginal());
                }
            });
            lastSelectTranslateSubtitle = vo;
            List<PLVPlaybackSubtitleVO> setSubtitles = new ArrayList<>(showSubtitles);
            setSubtitles.remove(currentShowTranslateSubtitle);
            if (playbackSubtitleSettingTranslateCheckbox.isChecked()) {
                setSubtitles.add(vo);
            }
            onSubtitleActionListener.setShowSubtitles(setSubtitles);
            updateSubtitleSetting();
        }
    }

    private class TranslateSubtitleAdapter extends RecyclerView.Adapter<TranslateSubtitleAdapter.ViewHolder> {

        private List<PLVPlaybackSubtitleVO> allSubtitles = new ArrayList<>();
        private PLVSugarUtil.Consumer<PLVPlaybackSubtitleVO> onSelectSubtitleListener;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvlc_playback_subtitle_translate_select_option_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final PLVPlaybackSubtitleVO vo = allSubtitles.get(i);
            viewHolder.bind(vo);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSelectSubtitleListener != null) {
                        onSelectSubtitleListener.accept(vo);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.allSubtitles.size();
        }

        public void updateAllSubtitles(List<PLVPlaybackSubtitleVO> allSubtitles) {
            this.allSubtitles = allSubtitles;
            notifyDataSetChanged();
        }

        public TranslateSubtitleAdapter setOnSelectSubtitleListener(PLVSugarUtil.Consumer<PLVPlaybackSubtitleVO> onSelectSubtitleListener) {
            this.onSelectSubtitleListener = onSelectSubtitleListener;
            return this;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private TextView playbackSubtitleTranslateOptionTv;

            public ViewHolder(View itemView) {
                super(itemView);
                playbackSubtitleTranslateOptionTv = itemView.findViewById(R.id.plvlc_playback_subtitle_translate_option_tv);
            }

            public void bind(PLVPlaybackSubtitleVO vo) {
                playbackSubtitleTranslateOptionTv.setText(getLanguageText(vo.getLanguage()));
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表适配器">
    //////////////////////
    //倍速列表适配器
    //////////////////////
    private class RvSpeedAdapter extends RecyclerView.Adapter<RvSpeedAdapter.RvMoreViewHolder> {
        private int curSelectPos = -1;
        private List<Float> speedVO;

        void updateSpeedListData(List<Float> speedVO, int speedPos) {
            this.speedVO = speedVO == null ? new ArrayList<Float>() : speedVO;
            curSelectPos = speedPos;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RvMoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvlc_live_controller_bitrate_item, parent, false);
            return new RvMoreViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final RvMoreViewHolder holder, int position) {
            final String speed = speedVO.get(position) + "x";
            holder.tvSpeed.setText(speed);

            boolean isSelect = position == curSelectPos;
            holder.tvSpeed.setSelected(isSelect);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    if (holder.getAdapterPosition() == curSelectPos) {
                        return;
                    }
                    curSelectPos = holder.getAdapterPosition();
                    RvSpeedAdapter.this.notifyDataSetChanged();
                    if (onSpeedSelectedListener != null) {
                        onSpeedSelectedListener.onSpeedSelected(speedVO.get(curSelectPos), curSelectPos);
                    }
                    rvSpeed.post(new Runnable() {
                        @Override
                        public void run() {
                            showSpeedTips(ScreenUtils.isPortrait(), speed);
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            if (speedVO != null) {
                return speedVO.size();
            } else {
                return 0;
            }
        }

        public int getCurSelectPos() {
            return curSelectPos;
        }

        class RvMoreViewHolder extends RecyclerView.ViewHolder {
            TextView tvSpeed;

            RvMoreViewHolder(View itemView) {
                super(itemView);
                tvSpeed = (TextView) itemView.findViewById(R.id.tv_bitrate);
                ViewGroup.LayoutParams layoutParams = tvSpeed.getLayoutParams();
                layoutParams.width = ConvertUtils.dp2px(50);
                tvSpeed.setLayoutParams(layoutParams);
            }
        }

    }

    // <editor-fold defaultstate="collapsed" desc="监听器定义">

    /**
     * 选择倍速监听器
     */
    public interface OnSpeedSelectedListener {
        void onSpeedSelected(Float speed, int pos);
    }

    public interface OnSubtitleActionListener {
        List<PLVPlaybackSubtitleVO> getAllSubtitleSettings();

        List<PLVPlaybackSubtitleVO> getShowSubtitles();

        void setShowSubtitles(List<PLVPlaybackSubtitleVO> subtitles);
    }

    // </editor-fold>

}
