package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

/**
 * date: 2019/6/10 0010
 *
 * @author hwj
 * description 控制栏右上角的“更多”布局
 */
public class PLVLCLiveMoreLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //popupWindow
    private PopupWindow popupWindow;
    //View
    private View root;
    private View anchor;
    private TextView plvlcLiveControlMoreModeTv;
    private TextView tvPlayVideoSwitch;
    private TextView tvOnlyAudioSwitch;
    private LinearLayout plvlcLiveControlMoreModeSwitchLl;
    private LinearLayout plvlcLiveControlMoreModeLl;
    private TextView plvlcLiveControlMoreBitrateTv;
    private RecyclerView plvlcLiveControlMoreBitrateRv;
    private LinearLayout plvlcLiveControlMoreBitrateLl;
    private TextView plvlcLiveControlMoreLinesTv;
    private RecyclerView plvlcLiveControlMoreLinesRv;
    private LinearLayout plvlcLiveControlMoreLinesLl;
    private TextView plvlcLiveControlMoreLatencyTv;
    private RecyclerView plvlcLiveControlMoreLatencyRv;
    private LinearLayout plvlcLiveControlMoreLatencyLl;
    private PLVOrientationSensibleLinearLayout llMoreVertical;
    private FrameLayout containerLy;

    private RvBitrateAdapter rvBitrateAdapter;
    private RvLinesAdapter rvLinesAdapter;
    private RvLatencyAdapter rvLatencyAdapter;

    //callback
    private OnBitrateSelectedListener onBitrateSelectedListener;
    private OnLinesSelectedListener onLinesSelectedListener;
    private OnOnlyAudioSwitchListener onOnlyAudioSwitchListener;
    private OnChangeLowLatencyListener onChangeLowLatencyListener;

    private int portraitHeight;

    private boolean shouldShowBitrateLayout;
    private boolean shouldShowLinesLayout;
    //是否是音频模式
    private boolean isAudioMode;
    // 是否无延迟观看
    private boolean isLowLatency;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVLCLiveMoreLayout(View anchor) {
        this.anchor = anchor;
        if (popupWindow == null) {
            popupWindow = new PopupWindow(anchor.getContext());

            View.OnClickListener handleHideListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            };
            root = PLVViewInitUtils.initPopupWindow(anchor, R.layout.plvlc_live_controller_more_layout, popupWindow, handleHideListener);
            initView();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        findView();
        //父布局
        containerLy.setOnClickListener(this);
        //监听屏幕方向
        llMoreVertical.setOnLandscape(new Runnable() {
            @Override
            public void run() {
                if (popupWindow != null && popupWindow.isShowing()) {
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
                    popupWindow.update();
                }
                onPortrait();
            }
        });

        //码率列表
        rvBitrateAdapter = new RvBitrateAdapter();
        plvlcLiveControlMoreBitrateRv.setAdapter(rvBitrateAdapter);
        plvlcLiveControlMoreBitrateRv.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false));

        //多綫路列表
        rvLinesAdapter = new RvLinesAdapter();
        plvlcLiveControlMoreLinesRv.setAdapter(rvLinesAdapter);
        plvlcLiveControlMoreLinesRv.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false));

        //播放画面
        tvPlayVideoSwitch.setSelected(true);
        tvPlayVideoSwitch.setOnClickListener(this);
        //仅听声音
        tvOnlyAudioSwitch.setSelected(false);
        tvOnlyAudioSwitch.setOnClickListener(this);

        initLatencyRv();
    }

    private void findView() {
        plvlcLiveControlMoreModeTv = root.findViewById(R.id.plvlc_live_control_more_mode_tv);
        tvPlayVideoSwitch = root.findViewById(R.id.tv_play_video_switch);
        tvOnlyAudioSwitch = root.findViewById(R.id.tv_only_audio_switch);
        plvlcLiveControlMoreModeSwitchLl = root.findViewById(R.id.plvlc_live_control_more_mode_switch_ll);
        plvlcLiveControlMoreModeLl = root.findViewById(R.id.plvlc_live_control_more_mode_ll);
        plvlcLiveControlMoreBitrateTv = root.findViewById(R.id.plvlc_live_control_more_bitrate_tv);
        plvlcLiveControlMoreBitrateRv = root.findViewById(R.id.plvlc_live_control_more_bitrate_rv);
        plvlcLiveControlMoreBitrateLl = root.findViewById(R.id.plvlc_live_control_more_bitrate_ll);
        plvlcLiveControlMoreLinesTv = root.findViewById(R.id.plvlc_live_control_more_lines_tv);
        plvlcLiveControlMoreLinesRv = root.findViewById(R.id.plvlc_live_control_more_lines_rv);
        plvlcLiveControlMoreLinesLl = root.findViewById(R.id.plvlc_live_control_more_lines_ll);
        plvlcLiveControlMoreLatencyTv = root.findViewById(R.id.plvlc_live_control_more_latency_tv);
        plvlcLiveControlMoreLatencyRv = root.findViewById(R.id.plvlc_live_control_more_latency_rv);
        plvlcLiveControlMoreLatencyLl = root.findViewById(R.id.plvlc_live_control_more_latency_ll);
        llMoreVertical = root.findViewById(R.id.ll_more_vertical);
        containerLy = root.findViewById(R.id.container_ly);
    }

    private void initLatencyRv() {
        setEnableLowLatency(PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled());

        rvLatencyAdapter = new RvLatencyAdapter();
        rvLatencyAdapter.setOnChangeLowLatencyListener(new OnChangeLowLatencyListener() {
            @Override
            public void accept(Boolean isLowLatency) {
                hide();
                if (isLowLatency) {
                    // 切换到无延迟观看时，需要切换到视频观看模式
                    onClickChangeToPlayVideo();
                }
                if (PLVLCLiveMoreLayout.this.isLowLatency != isLowLatency) {
                    if (onChangeLowLatencyListener != null) {
                        onChangeLowLatencyListener.accept(isLowLatency);
                    }
                    PLVLCLiveMoreLayout.this.isLowLatency = isLowLatency;
                }
            }
        });
        rvLatencyAdapter.setCurrentIsLowLatency(isLowLatency = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled());

        plvlcLiveControlMoreLatencyRv.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false));
        plvlcLiveControlMoreLatencyRv.setAdapter(rvLatencyAdapter);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 显示/隐藏控制">
    public void hide() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public void showWhenPortrait(int height) {
        this.portraitHeight = height;
        onPortrait();
        show(true);
    }

    public void updateWhenOnlyAudio(boolean isOnlyAudio){
        tvPlayVideoSwitch.setVisibility(isOnlyAudio ? View.GONE : View.VISIBLE);
        tvOnlyAudioSwitch.setSelected(isOnlyAudio);
    }

    public void showWhenLandscape() {
        onLandscape();
        show(false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 设置监听器">
    public void setOnBitrateSelectedListener(OnBitrateSelectedListener onBitrateSelectedListener) {
        this.onBitrateSelectedListener = onBitrateSelectedListener;
    }

    public void setOnOnlyAudioSwitchListener(OnOnlyAudioSwitchListener onOnlyAudioSwitchListener) {
        this.onOnlyAudioSwitchListener = onOnlyAudioSwitchListener;
    }

    public void setOnLinesSelectedListener(OnLinesSelectedListener onLinesSelectedListener) {
        this.onLinesSelectedListener = onLinesSelectedListener;
    }

    public void setOnChangeLowLatencyListener(OnChangeLowLatencyListener onChangeLowLatencyListener) {
        this.onChangeLowLatencyListener = onChangeLowLatencyListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 参数配置">
    public void initBitrate(List<PolyvDefinitionVO> bitrateVO, int bitratePos) {
        rvBitrateAdapter.updateBitrateListData(bitrateVO, bitratePos);
    }

    public void initLines(int linesCount, int linesPos) {
        rvLinesAdapter.updateLinesDatas(linesCount, linesPos);
    }

    public void setEnableLowLatency(boolean enableLowLatency) {
        plvlcLiveControlMoreLatencyLl.setVisibility(enableLowLatency ? View.VISIBLE : View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 状态更新">
    public void updateViewWithPlayInfo(@PolyvMediaPlayMode.Mode int mediaPlayMode, Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair, int[] lines) {
        updateViewWithPlayMode(mediaPlayMode);

        initBitrate(listIntegerPair.first, listIntegerPair.second);
        initLines(lines[0], lines[1]);
        showLines(true);
        showBitrate(!isAudioMode);
    }

    //从别处而不是从MoreLayout里切换模式，所以改变当前MoreLayout的状态。
    public void updateViewWithPlayMode(@PolyvMediaPlayMode.Mode int mediaPlayMode) {
        isAudioMode = mediaPlayMode == PolyvMediaPlayMode.MODE_AUDIO;
        if (isAudioMode) {
            showBitrate(false);
        }
        tvOnlyAudioSwitch.setSelected(isAudioMode);
        tvPlayVideoSwitch.setSelected(!isAudioMode);
    }

    public void updateViewWithLatency(boolean isLowLatency) {
        this.isLowLatency = isLowLatency;
        rvLatencyAdapter.setCurrentIsLowLatency(isLowLatency);
        updateViewVisibility();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="切换横竖屏">
    private void onLandscape() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) llMoreVertical.getLayoutParams();
        lp.gravity = Gravity.TOP;
        llMoreVertical.setLayoutParams(lp);

        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) containerLy.getLayoutParams();
        flp.width = -2;
        flp.height = -1;
        flp.gravity = Gravity.RIGHT;
        containerLy.setLayoutParams(flp);
        containerLy.setBackgroundColor(Color.parseColor("#CC000000"));

        onModeLayoutReactOrientation(false);
        onBitrateLayoutReactOrientation(false);
        onLinesLayoutReactOrientation(false);
        onLatencyLayoutReactOrientation(false);
    }

    private void onPortrait() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) llMoreVertical.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        llMoreVertical.setLayoutParams(lp);

        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) containerLy.getLayoutParams();
        flp.width = -1;
        flp.height = portraitHeight;
        flp.gravity = Gravity.NO_GRAVITY;
        containerLy.setLayoutParams(flp);
        containerLy.setBackgroundColor(Color.parseColor("#D8000000"));

        onModeLayoutReactOrientation(true);
        onBitrateLayoutReactOrientation(true);
        onLinesLayoutReactOrientation(true);
        onLatencyLayoutReactOrientation(true);
    }

    private void onModeLayoutReactOrientation(boolean isPortrait) {
        ViewGroup.LayoutParams tvLp = plvlcLiveControlMoreModeTv.getLayoutParams();
        ViewGroup.MarginLayoutParams switchLlLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreModeSwitchLl.getLayoutParams();
        ViewGroup.MarginLayoutParams llLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreModeLl.getLayoutParams();
        if (isPortrait) {
            tvLp.width = ConvertUtils.dp2px(60);
            switchLlLp.leftMargin = ConvertUtils.dp2px(24);
            llLp.topMargin = 0;
            plvlcLiveControlMoreModeLl.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            tvLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            switchLlLp.leftMargin = 0;
            llLp.topMargin = ConvertUtils.dp2px(16);
            plvlcLiveControlMoreModeLl.setOrientation(LinearLayout.VERTICAL);
        }
        plvlcLiveControlMoreModeTv.setLayoutParams(tvLp);
        plvlcLiveControlMoreModeSwitchLl.setLayoutParams(switchLlLp);
        plvlcLiveControlMoreModeLl.setLayoutParams(llLp);
    }

    private void onBitrateLayoutReactOrientation(boolean isPortrait) {
        ViewGroup.LayoutParams tvLp = plvlcLiveControlMoreBitrateTv.getLayoutParams();
        ViewGroup.MarginLayoutParams rvLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreBitrateRv.getLayoutParams();
        ViewGroup.MarginLayoutParams llLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreBitrateLl.getLayoutParams();
        if (isPortrait) {
            tvLp.width = ConvertUtils.dp2px(60);
            rvLp.leftMargin = ConvertUtils.dp2px(24);
            llLp.topMargin = 0;
            plvlcLiveControlMoreBitrateLl.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            tvLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            rvLp.leftMargin = 0;
            llLp.topMargin = ConvertUtils.dp2px(16);
            plvlcLiveControlMoreBitrateLl.setOrientation(LinearLayout.VERTICAL);
        }
        plvlcLiveControlMoreBitrateTv.setLayoutParams(tvLp);
        plvlcLiveControlMoreBitrateRv.setLayoutParams(rvLp);
        plvlcLiveControlMoreBitrateLl.setLayoutParams(llLp);
    }

    private void onLinesLayoutReactOrientation(boolean isPortrait) {
        ViewGroup.LayoutParams tvLp = plvlcLiveControlMoreLinesTv.getLayoutParams();
        ViewGroup.MarginLayoutParams rvLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreLinesRv.getLayoutParams();
        ViewGroup.MarginLayoutParams llLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreLinesLl.getLayoutParams();
        if (isPortrait) {
            tvLp.width = ConvertUtils.dp2px(60);
            rvLp.leftMargin = ConvertUtils.dp2px(24);
            llLp.topMargin = 0;
            plvlcLiveControlMoreLinesLl.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            tvLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            rvLp.leftMargin = 0;
            llLp.topMargin = ConvertUtils.dp2px(16);
            plvlcLiveControlMoreLinesLl.setOrientation(LinearLayout.VERTICAL);
        }
        plvlcLiveControlMoreLinesTv.setLayoutParams(tvLp);
        plvlcLiveControlMoreLinesRv.setLayoutParams(rvLp);
        plvlcLiveControlMoreLinesLl.setLayoutParams(llLp);
    }

    private void onLatencyLayoutReactOrientation(boolean isPortrait) {
        ViewGroup.LayoutParams tvLp = plvlcLiveControlMoreLatencyTv.getLayoutParams();
        ViewGroup.MarginLayoutParams rvLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreLatencyRv.getLayoutParams();
        ViewGroup.MarginLayoutParams llLp = (ViewGroup.MarginLayoutParams) plvlcLiveControlMoreLatencyLl.getLayoutParams();
        if (isPortrait) {
            tvLp.width = ConvertUtils.dp2px(60);
            rvLp.leftMargin = ConvertUtils.dp2px(24);
            llLp.topMargin = 0;
            plvlcLiveControlMoreLatencyLl.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            tvLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            rvLp.leftMargin = 0;
            llLp.topMargin = ConvertUtils.dp2px(16);
            plvlcLiveControlMoreLatencyLl.setOrientation(LinearLayout.VERTICAL);
        }
        plvlcLiveControlMoreLatencyTv.setLayoutParams(tvLp);
        plvlcLiveControlMoreLatencyRv.setLayoutParams(rvLp);
        plvlcLiveControlMoreLatencyLl.setLayoutParams(llLp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制UI显示">
    private void show(boolean isPort) {
        if (popupWindow != null) {
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, 0);
        }
        updateViewVisibility();
    }

    public void showBitrate(boolean show) {
        shouldShowBitrateLayout = show;
        updateViewVisibility();
    }

    public void showLines(boolean show) {
        shouldShowLinesLayout = show;
        updateViewVisibility();
    }

    private void updateViewVisibility() {
        if (!isLowLatency) {
            plvlcLiveControlMoreModeLl.setVisibility(View.VISIBLE);
        } else {
            plvlcLiveControlMoreModeLl.setVisibility(View.GONE);
        }
        if (shouldShowBitrateLayout && rvBitrateAdapter.getItemCount() > 1 && !isLowLatency) {
            plvlcLiveControlMoreBitrateLl.setVisibility(View.VISIBLE);
        } else {
            plvlcLiveControlMoreBitrateLl.setVisibility(View.GONE);
        }
        if (shouldShowLinesLayout && rvLinesAdapter.getItemCount() > 1 && !isLowLatency) {
            plvlcLiveControlMoreLinesLl.setVisibility(View.VISIBLE);
        } else {
            plvlcLiveControlMoreLinesLl.setVisibility(View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表适配器">

    /**
     * 码率列表适配器
     */
    private class RvBitrateAdapter extends RecyclerView.Adapter<RvBitrateAdapter.RvMoreViewHolder> {
        private int curSelectPos = -1;
        private List<PolyvDefinitionVO> bitrateVO;

        void updateBitrateListData(List<PolyvDefinitionVO> bitrateVO, int bitratePos) {
            this.bitrateVO = bitrateVO == null ? new ArrayList<PolyvDefinitionVO>() : bitrateVO;
            curSelectPos = bitratePos;
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
            String defenition = bitrateVO.get(position).getDefinition();
            holder.tvBitrate.setText(defenition);

            boolean isSelect = position == curSelectPos;
            holder.tvBitrate.setSelected(isSelect);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    if (holder.getAdapterPosition() == curSelectPos) {
                        return;
                    }
                    curSelectPos = holder.getAdapterPosition();
                    RvBitrateAdapter.this.notifyDataSetChanged();
                    if (onBitrateSelectedListener != null) {
                        onBitrateSelectedListener.onBitrateSelected(bitrateVO.get(curSelectPos), curSelectPos);
                    }
                    plvlcLiveControlMoreBitrateTv.post(new Runnable() {
                        @Override
                        public void run() {
                            PLVLCLiveMoreLayout.this.hide();
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            if (bitrateVO != null) {
                return bitrateVO.size();
            } else {
                return 0;
            }
        }

        public int getCurSelectPos() {
            return curSelectPos;
        }

        class RvMoreViewHolder extends RecyclerView.ViewHolder {
            TextView tvBitrate;

            RvMoreViewHolder(View itemView) {
                super(itemView);
                tvBitrate = (TextView) itemView.findViewById(R.id.tv_bitrate);
            }
        }

    }

    /**
     * 多线路列表适配器
     */
    private class RvLinesAdapter extends RecyclerView.Adapter<RvLinesAdapter.RvLinesViewHolder> {
        private int curSelectPos = 0;
        private int lines;

        @NonNull
        @Override
        public RvLinesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plvlc_live_controller_bitrate_item, parent, false);
            return new RvLinesViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final RvLinesViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.tvLines.setText("线路" + (position + 1));

            holder.tvLines.setSelected(position == curSelectPos);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    if (holder.getAdapterPosition() == curSelectPos) {
                        return;
                    }
                    curSelectPos = holder.getAdapterPosition();

                    RvLinesAdapter.this.notifyDataSetChanged();
                    if (onLinesSelectedListener != null) {
                        onLinesSelectedListener.onLineSelected(lines, position);
                    }
                    plvlcLiveControlMoreLinesRv.post(new Runnable() {
                        @Override
                        public void run() {
                            PLVLCLiveMoreLayout.this.hide();
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return lines;
        }

        public void updateLinesDatas(int linesCount, int pos) {
            this.lines = linesCount;
            curSelectPos = pos;
            notifyDataSetChanged();
        }

        class RvLinesViewHolder extends RecyclerView.ViewHolder {
            TextView tvLines;

            RvLinesViewHolder(View itemView) {
                super(itemView);
                tvLines = itemView.findViewById(R.id.tv_bitrate);
            }
        }

    }

    /**
     * 延迟选项列表适配器
     */
    private static class RvLatencyAdapter extends RecyclerView.Adapter<RvLatencyAdapter.RvLatencyViewHolder> {

        private final List<LatencyType> latencyTypeList = listOf(
                LatencyType.LOW_LATENCY,
                LatencyType.NORMAL_LATENCY
        );

        private LatencyType curSelectLatencyType = LatencyType.LOW_LATENCY;

        private OnChangeLowLatencyListener onChangeLowLatencyListener;

        @NonNull
        @Override
        public RvLatencyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvlc_live_controller_bitrate_item, viewGroup, false);
            return new RvLatencyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RvLatencyViewHolder rvLatencyViewHolder, int i) {
            final LatencyType latencyType = latencyTypeList.get(i);
            rvLatencyViewHolder.bind(latencyType);
            rvLatencyViewHolder.itemView.setSelected(latencyType == curSelectLatencyType);
            rvLatencyViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onChangeLowLatencyListener != null) {
                        onChangeLowLatencyListener.accept(latencyType.isLowLatency());
                    }
                    curSelectLatencyType = latencyType;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return latencyTypeList.size();
        }

        public void setOnChangeLowLatencyListener(OnChangeLowLatencyListener onChangeLowLatencyListener) {
            this.onChangeLowLatencyListener = onChangeLowLatencyListener;
        }

        public void setCurrentIsLowLatency(boolean isLowLatency) {
            if (curSelectLatencyType.isLowLatency() != isLowLatency) {
                curSelectLatencyType = LatencyType.getLatencyType(isLowLatency);
                notifyDataSetChanged();
            }
        }

        private static class RvLatencyViewHolder extends RecyclerView.ViewHolder {

            private TextView latencyTextView;

            public RvLatencyViewHolder(View itemView) {
                super(itemView);
                findView();
            }

            private void findView() {
                latencyTextView = itemView.findViewById(R.id.tv_bitrate);
            }

            public void bind(LatencyType latencyType) {
                latencyTextView.setText(latencyType.getLatencyName());
            }

        }

        private enum LatencyType {
            LOW_LATENCY("无延迟", true),
            NORMAL_LATENCY("正常延迟", false);

            private final String latencyName;
            private final boolean lowLatency;

            LatencyType(String latencyName, boolean lowLatency) {
                this.latencyName = latencyName;
                this.lowLatency = lowLatency;
            }

            public static LatencyType getLatencyType(boolean isLowLatency) {
                if (isLowLatency) {
                    return LOW_LATENCY;
                }
                return NORMAL_LATENCY;
            }

            public String getLatencyName() {
                return latencyName;
            }

            public boolean isLowLatency() {
                return lowLatency;
            }
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听器定义">

    /**
     * 选择码率监听器
     */
    public interface OnBitrateSelectedListener {
        void onBitrateSelected(PolyvDefinitionVO definitionVO, int pos);
    }

    /**
     * 选择多线路监听器
     */
    public interface OnLinesSelectedListener {
        void onLineSelected(int linesCount, int linesPos);
    }

    /**
     * 选择仅听音频或者观看画面监听器
     */
    public interface OnOnlyAudioSwitchListener {
        boolean onOnlyAudioSelect(boolean onlyAudio);
    }

    /**
     * 延迟选择变更监听器
     */
    public interface OnChangeLowLatencyListener extends PLVSugarUtil.Consumer<Boolean> {}
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    @Override
    public void onClick(View v) {
        if (v.getId() == containerLy.getId()) {
            hide();
        } else if (v.getId() == tvPlayVideoSwitch.getId()) {
            onClickChangeToPlayVideo();
        } else if (v.getId() == tvOnlyAudioSwitch.getId()) {
            onClickChangeToPlayAudio();
        }
    }

    private void onClickChangeToPlayVideo() {
        if (tvPlayVideoSwitch.isSelected()) {
            return;
        }
        //是否成功切换模式
        boolean isChangeModeSucceed = false;

        if (onOnlyAudioSwitchListener != null) {
            isChangeModeSucceed = onOnlyAudioSwitchListener.onOnlyAudioSelect(false);
        }
        if (!isChangeModeSucceed) {
            PLVLCLiveMoreLayout.this.hide();
            return;
        }
        PLVLCLiveMoreLayout.this.showBitrate(true);
        tvOnlyAudioSwitch.setSelected(false);
        tvPlayVideoSwitch.setSelected(true);
        PLVLCLiveMoreLayout.this.hide();
    }

    private void onClickChangeToPlayAudio() {
        if (tvOnlyAudioSwitch.isSelected()) {
            return;
        }
        //是否成功切换模式
        boolean isChangeModeSucceed = false;

        if (onOnlyAudioSwitchListener != null) {
            isChangeModeSucceed = onOnlyAudioSwitchListener.onOnlyAudioSelect(true);
        }
        if (!isChangeModeSucceed) {
            PLVLCLiveMoreLayout.this.hide();
            return;
        }
        PLVLCLiveMoreLayout.this.showBitrate(false);
        tvOnlyAudioSwitch.setSelected(true);
        tvPlayVideoSwitch.setSelected(false);
        PLVLCLiveMoreLayout.this.hide();
    }

    // </editor-fold>

}
