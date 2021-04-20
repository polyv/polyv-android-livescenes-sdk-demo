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
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2019/6/10 0010
 *
 * @author hwj
 * description 控制栏右上角的“更多”布局
 */
public class PLVLCLiveMoreLayout {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //popupWindow
    private PopupWindow popupWindow;
    //View
    private View root;
    private View anchor;
    private ViewGroup containerLy;
    private PLVOrientationSensibleLinearLayout llMoreVertical;
    private RecyclerView rvBitrate;
    private RecyclerView rvLines;
    private RvBitrateAdapter rvAdapter;
    private RvLinesAdapter linesAdapter;
    private TextView tvPlayVideoSwitch;
    private TextView tvOnlyAudioSwitch;
    private FrameLayout llBitrate;
    private FrameLayout linesContainer;

    private TextView portraitModeTv;
    private TextView portraitBitrateTv;
    private TextView portraitLinesTv;

    private TextView landscapeModeTv;
    private TextView landscapeBitrateTv;
    private TextView landscapeLinesTv;

    //callback
    private OnBitrateSelectedListener onBitrateSelectedListener;
    private OnLinesSelectedListener onLinesSelectedListener;
    private OnOnlyAudioSwitchListener onOnlyAudioSwitchListener;

    private int portraitHeight;

    //是否是音频模式
    private boolean isAudioMode;
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
        //父布局
        containerLy = root.findViewById(R.id.container_ly);
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
                     * ///暂时保留代码
                     *popupWindow.setClippingEnabled(true);
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
                     *  ///暂时保留代码
                     *popupWindow.setClippingEnabled(false);
                     */
                    popupWindow.update();
                }
                onPortrait();
            }
        });

        //码率列表
        rvBitrate = root.findViewById(R.id.rv_more_bitrate);
        rvAdapter = new RvBitrateAdapter();
        rvBitrate.setAdapter(rvAdapter);
        rvBitrate.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false));

        //多綫路列表
        linesAdapter = new RvLinesAdapter();
        rvLines = root.findViewById(R.id.rv_more_lines);
        rvLines.setAdapter(linesAdapter);
        rvLines.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false));
        linesContainer = root.findViewById(R.id.fl_lines);

        //播放画面
        tvPlayVideoSwitch = root.findViewById(R.id.cb_play_video_switch);
        tvPlayVideoSwitch.setSelected(true);
        tvPlayVideoSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        //仅听声音
        tvOnlyAudioSwitch = root.findViewById(R.id.cb_only_audio_switch);
        tvOnlyAudioSwitch.setSelected(false);
        tvOnlyAudioSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        llBitrate = (FrameLayout) root.findViewById(R.id.fl_bitrate);

        portraitModeTv = root.findViewById(R.id.portrait_mode_tv);
        portraitBitrateTv = root.findViewById(R.id.portrait_bitrate_tv);
        portraitLinesTv = root.findViewById(R.id.portrait_lines_tv);

        landscapeModeTv = root.findViewById(R.id.landscape_mode_tv);
        landscapeBitrateTv = root.findViewById(R.id.landscape_bitrate_tv);
        landscapeLinesTv = root.findViewById(R.id.landscape_lines_tv);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    // <editor-fold defaultstate="collapsed" desc="显示/隐藏控制">
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

    public void showWhenLandscape() {
        onLandscape();
        show(false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置监听器">
    public void setOnBitrateSelectedListener(OnBitrateSelectedListener onBitrateSelectedListener) {
        this.onBitrateSelectedListener = onBitrateSelectedListener;
    }

    public void setOnOnlyAudioSwitchListener(OnOnlyAudioSwitchListener onOnlyAudioSwitchListener) {
        this.onOnlyAudioSwitchListener = onOnlyAudioSwitchListener;
    }

    public void setOnLinesSelectedListener(OnLinesSelectedListener onLinesSelectedListener) {
        this.onLinesSelectedListener = onLinesSelectedListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="参数配置">
    public void initBitrate(List<PolyvDefinitionVO> bitrateVO, int bitratePos) {
        rvAdapter.updateBitrateListData(bitrateVO, bitratePos);
    }

    public void initLines(int linesCount, int linesPos) {
        linesAdapter.updateLinesDatas(linesCount, linesPos);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="状态更新">
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
    // </editor-fold>

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

        FrameLayout.LayoutParams bitRvLp = (FrameLayout.LayoutParams) rvBitrate.getLayoutParams();
        bitRvLp.leftMargin = 0;
        rvBitrate.setLayoutParams(bitRvLp);

        FrameLayout.LayoutParams linesRvLp = (FrameLayout.LayoutParams) rvLines.getLayoutParams();
        linesRvLp.leftMargin = 0;
        rvLines.setLayoutParams(linesRvLp);

        portraitModeTv.setVisibility(View.GONE);
        portraitBitrateTv.setVisibility(View.GONE);
        portraitLinesTv.setVisibility(View.GONE);
        landscapeModeTv.setVisibility(View.VISIBLE);
        if (llBitrate.getVisibility() == View.VISIBLE) {
            landscapeBitrateTv.setVisibility(View.VISIBLE);
        } else {
            landscapeBitrateTv.setVisibility(View.GONE);
        }
        if (linesContainer.getVisibility() == View.VISIBLE) {
            landscapeLinesTv.setVisibility(View.VISIBLE);
        } else {
            landscapeLinesTv.setVisibility(View.GONE);
        }
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

        FrameLayout.LayoutParams bitRvLp = (FrameLayout.LayoutParams) rvBitrate.getLayoutParams();
        bitRvLp.leftMargin = ConvertUtils.dp2px(84);
        rvBitrate.setLayoutParams(bitRvLp);

        FrameLayout.LayoutParams linesRvLp = (FrameLayout.LayoutParams) rvLines.getLayoutParams();
        linesRvLp.leftMargin = ConvertUtils.dp2px(84);
        rvLines.setLayoutParams(linesRvLp);

        portraitModeTv.setVisibility(View.VISIBLE);
        portraitBitrateTv.setVisibility(View.VISIBLE);
        portraitLinesTv.setVisibility(View.VISIBLE);
        landscapeModeTv.setVisibility(View.GONE);
        landscapeBitrateTv.setVisibility(View.GONE);
        landscapeLinesTv.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制UI显示">
    private void show(boolean isPort) {
        if (popupWindow != null) {
            /**
             *  ///暂时保留代码
             *popupWindow.setClippingEnabled(!isPort);
             */
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    public void showBitrate(boolean show) {
        if (show && rvAdapter.getItemCount() > 1) {
            llBitrate.setVisibility(View.VISIBLE);
            if (ScreenUtils.isLandscape()) {
                landscapeBitrateTv.setVisibility(View.VISIBLE);
            }
        } else {
            llBitrate.setVisibility(View.GONE);
            if (ScreenUtils.isLandscape()) {
                landscapeBitrateTv.setVisibility(View.GONE);
            }
        }
    }

    public void showLines(boolean show) {
        linesContainer.setVisibility(show && linesAdapter.getItemCount() > 1 ? View.VISIBLE : View.GONE);
        if (ScreenUtils.isLandscape()) {
            landscapeLinesTv.setVisibility(show && linesAdapter.getItemCount() > 1 ? View.VISIBLE : View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表适配器">
    //////////////////////
    //码率列表适配器
    //////////////////////
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
                    rvBitrate.post(new Runnable() {
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

    //////////////////////
    //多线路列表适配器
    //////////////////////
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
            holder.tvBitrate.setText("线路" + (position + 1));

            holder.tvBitrate.setSelected(position == curSelectPos);

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
                    rvBitrate.post(new Runnable() {
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
            TextView tvBitrate;

            RvLinesViewHolder(View itemView) {
                super(itemView);
                tvBitrate = itemView.findViewById(R.id.tv_bitrate);
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
    // </editor-fold>

}
