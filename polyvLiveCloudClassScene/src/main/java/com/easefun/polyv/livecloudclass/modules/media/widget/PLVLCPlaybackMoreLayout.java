package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

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
 * description 回放控制栏右上角的“更多”布局
 */
public class PLVLCPlaybackMoreLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
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

    //callback
    private OnSpeedSelectedListener onSpeedSelectedListener;

    private int portraitHeight;

    private View speedTipsLy;
    private PLVOrientationSensibleLinearLayout speedTipsContainerLy;
    private TextView speedTipsTv;

    //倍速列表数据
    private List<Float> speedVO = new ArrayList<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVLCPlaybackMoreLayout(View anchor) {
        this.anchor = anchor;

        speedVO.add(0.5f);
        speedVO.add(1.0f);
        speedVO.add(1.5f);
        speedVO.add(2.0f);

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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="参数配置">
    public void initSpeed(List<Float> speedVO, int speedPos) {
        rvAdapter.updateSpeedListData(speedVO, speedPos);
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
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) llMoreVertical.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        llMoreVertical.setLayoutParams(lp);

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
        if (show && rvAdapter.getItemCount() > 1) {
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
    // </editor-fold>

}
