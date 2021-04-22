package com.easefun.polyv.livestreamer.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.livestreamer.R;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 设置布局
 */
public class PLVLSSettingLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //布局弹层
    private PLVMenuDrawer menuDrawer;

    //模糊背景view
    private PLVBlurView blurView;
    private Disposable updateBlurViewDisposable;

    //view
    private TextView plvlsSettingTv;
    private TextView plvlsSettingBitTv;
    private RecyclerView plvlsSettingBitRv;
    private TextView plvlsSettingExitTv;

    //adapter
    private BitrateAdapter bitrateAdapter;

    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSSettingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_setting_layout, this);

        blurView = findViewById(R.id.blur_ly);
        plvlsSettingTv = findViewById(R.id.plvls_setting_tv);
        plvlsSettingBitTv = findViewById(R.id.plvls_setting_bit_tv);
        plvlsSettingBitRv = findViewById(R.id.plvls_setting_bit_rv);
        plvlsSettingExitTv = findViewById(R.id.plvls_setting_exit_tv);

        plvlsSettingExitTv.setOnClickListener(this);

        blurView = findViewById(R.id.blur_ly);
        PLVBlurUtils.initBlurView(blurView);

        plvlsSettingBitRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bitrateAdapter = new BitrateAdapter();
        plvlsSettingBitRv.setAdapter(bitrateAdapter);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="布局控制">
    public void open() {
        if (onViewActionListener != null && onViewActionListener.getBitrateInfo() != null) {
            bitrateAdapter.updateData(onViewActionListener.getBitrateInfo().first, onViewActionListener.getBitrateInfo().second);
        }

        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setMenuSize((int) (landscapeWidth * 0.44));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        close();
        stopUpdateBlurViewTimer();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="定时更新模糊背景view">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        blurView.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvls_setting_exit_tv) {
            close();
            ((Activity) getContext()).onBackPressed();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 清晰度适配器">
    private class BitrateAdapter extends RecyclerView.Adapter<BitrateAdapter.BitrateViewHolder> {
        private int maxBitrate;
        private int selPosition;

        @NonNull
        @Override
        public BitrateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BitrateViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvls_live_room_setting_bitrate_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final BitrateViewHolder holder, int position) {
            String bitrateText = getBitrateText(position);
            holder.plvlsSettingBitrateSelTv.setText(bitrateText);
            if (position == selPosition) {
                holder.plvlsSettingBitrateSelIndicatorView.setVisibility(View.VISIBLE);
                holder.plvlsSettingBitrateSelTv.setSelected(true);
            } else {
                holder.plvlsSettingBitrateSelIndicatorView.setVisibility(View.GONE);
                holder.plvlsSettingBitrateSelTv.setSelected(false);
            }
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.getAdapterPosition() == selPosition) {
                        return;
                    }
                    selPosition = holder.getAdapterPosition();
                    if (onViewActionListener != null) {
                        onViewActionListener.onBitrateClick(getBitrate(selPosition));
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return maxBitrate;
        }

        public void updateData(int maxBitrate, int selBitrate) {
            this.maxBitrate = maxBitrate;
            this.selPosition = Math.max(0, maxBitrate - selBitrate);
            notifyDataSetChanged();
        }

        private String getBitrateText(int position) {
            String bitrateText = "";
            if (position == 0) {
                bitrateText = maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? "超清" : maxBitrate == PLVSStreamerConfig.Bitrate.BITRATE_HIGH ? "高清" : "标清";
            } else if (position == 1) {
                bitrateText = maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? "高清" : "标清";
            } else if (position == 2) {
                bitrateText = "标清";
            }
            return bitrateText;
        }

        private int getBitrate(int pos) {
            if (pos == 0) {
                return maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? PLVSStreamerConfig.Bitrate.BITRATE_SUPER : maxBitrate == PLVSStreamerConfig.Bitrate.BITRATE_HIGH ? PLVSStreamerConfig.Bitrate.BITRATE_HIGH : PLVSStreamerConfig.Bitrate.BITRATE_STANDARD;
            } else if (pos == 1) {
                return maxBitrate >= PLVSStreamerConfig.Bitrate.BITRATE_SUPER ? PLVSStreamerConfig.Bitrate.BITRATE_HIGH : PLVSStreamerConfig.Bitrate.BITRATE_STANDARD;
            } else {
                return PLVSStreamerConfig.Bitrate.BITRATE_STANDARD;
            }
        }

        class BitrateViewHolder extends RecyclerView.ViewHolder {
            private TextView plvlsSettingBitrateSelTv;
            private View plvlsSettingBitrateSelIndicatorView;

            BitrateViewHolder(View itemView) {
                super(itemView);
                plvlsSettingBitrateSelTv = itemView.findViewById(R.id.plvls_setting_bitrate_sel_tv);
                plvlsSettingBitrateSelIndicatorView = itemView.findViewById(R.id.plvls_setting_bitrate_sel_indicator_view);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        Pair<Integer, Integer> getBitrateInfo();

        void onBitrateClick(int bitrate);
    }
    // </editor-fold>
}
