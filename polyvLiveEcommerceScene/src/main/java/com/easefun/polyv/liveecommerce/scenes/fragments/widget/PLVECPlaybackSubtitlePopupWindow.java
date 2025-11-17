package com.easefun.polyv.liveecommerce.scenes.fragments.widget;


import static com.plv.foundationsdk.component.exts.LangsKt.isLiteralTrue;

import android.support.annotation.NonNull;
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
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.playback.subtitle.vo.PLVPlaybackSubtitleVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.functions.Function1;

/**
 * 播放字幕弹窗
 */
public class PLVECPlaybackSubtitlePopupWindow {
    private PopupWindow popupWindow;
    private View view;
    private ConstraintLayout playbackSubtitleSettingLayout;
    private TextView playbackSubtitleSettingTitle;
    private CheckBox playbackSubtitleSettingOriginalCheckbox;
    private CheckBox playbackSubtitleSettingTranslateCheckbox;
    private TextView playbackSubtitleSettingTranslateOptionTv;

    private TranslateSubtitlePopupWindow translateSubtitlePopupWindow;

    private OnSubtitleActionListener onSubtitleActionListener;

    private PLVPlaybackSubtitleVO lastSelectTranslateSubtitle = null;

    public PLVECPlaybackSubtitlePopupWindow(View anchorView) {
        popupWindow = new PopupWindow(anchorView.getContext());
        this.view = PLVViewInitUtils.initPopupWindow(anchorView, R.layout.plvec_playback_more_subtitle_popup_window, popupWindow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        this.translateSubtitlePopupWindow = new TranslateSubtitlePopupWindow(view);

        playbackSubtitleSettingLayout = view.findViewById(R.id.plvec_playback_subtitle_setting_layout);
        playbackSubtitleSettingTitle = view.findViewById(R.id.plvec_playback_subtitle_setting_title);
        playbackSubtitleSettingOriginalCheckbox = view.findViewById(R.id.plvec_playback_subtitle_setting_original_checkbox);
        playbackSubtitleSettingTranslateCheckbox = view.findViewById(R.id.plvec_playback_subtitle_setting_translate_checkbox);
        playbackSubtitleSettingTranslateOptionTv = view.findViewById(R.id.plvec_playback_subtitle_setting_translate_option_tv);

        View widgetRoundLy = this.view.findViewById(com.easefun.polyv.livecommon.R.id.plv_widget_round_ly);
        widgetRoundLy.setVisibility(View.VISIBLE);
        PLVBlurUtils.initBlurView((PLVBlurView) this.view.findViewById(R.id.blur_ly));

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
    }

    public void show() {
        updateSubtitleSetting();
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    public void dismiss() {
        popupWindow.dismiss();
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

    public void setOnSubtitleActionListener(OnSubtitleActionListener onSubtitleActionListener) {
        this.onSubtitleActionListener = onSubtitleActionListener;
    }

    private String getLanguageText(String language) {
        if (this.view == null || language == null) {
            return "";
        }
        int id = this.view.getContext().getResources().getIdentifier("plv_subtitle_language_" + language.toLowerCase(Locale.ROOT), "string", this.view.getContext().getPackageName());
        if (id != 0) {
            return this.view.getContext().getResources().getString(id);
        } else {
            return language;
        }
    }

    public interface OnSubtitleActionListener {
        List<PLVPlaybackSubtitleVO> getAllSubtitleSettings();

        List<PLVPlaybackSubtitleVO> getShowSubtitles();

        void setShowSubtitles(List<PLVPlaybackSubtitleVO> subtitles);
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
            this.root = PLVViewInitUtils.initPopupWindow(anchorView, R.layout.plvec_playback_subtitle_translate_select_popup_window, popupWindow, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            playbackSubtitleTranslatePopupRoot = root.findViewById(R.id.plvec_playback_subtitle_translate_popup_root);
            playbackSubtitleTranslateSelectTitle = root.findViewById(R.id.plvec_playback_subtitle_translate_select_title);
            playbackSubtitleTranslateOptionRv = root.findViewById(R.id.plvec_playback_subtitle_translate_option_rv);
            playbackSubtitleTranslateSelectCancelTv = root.findViewById(R.id.plvec_playback_subtitle_translate_select_cancel_tv);

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
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvec_playback_subtitle_translate_select_option_item, viewGroup, false));
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
                playbackSubtitleTranslateOptionTv = itemView.findViewById(R.id.plvec_playback_subtitle_translate_option_tv);
            }

            public void bind(PLVPlaybackSubtitleVO vo) {
                playbackSubtitleTranslateOptionTv.setText(getLanguageText(vo.getLanguage()));
            }
        }
    }

}
