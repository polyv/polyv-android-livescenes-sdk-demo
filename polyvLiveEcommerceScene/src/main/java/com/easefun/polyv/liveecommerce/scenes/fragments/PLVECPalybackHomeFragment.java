package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECBulletinView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.utils.PLVTimeUtils;

/**
 * 回放首页：主持人信息、播放控制、进度条、更多
 */
public class PLVECPalybackHomeFragment extends PLVECCommonHomeFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //观看信息布局
    private PLVECWatchInfoView watchInfoLy;
    //公告布局
    private PLVECBulletinView bulletinLy;
    //播放控制
    private ImageView playControlIv;
    private TextView playTimeTv;
    private SeekBar playProgressSb;
    private TextView totalTimeTv;
    private boolean isPlaySbDragging;
    //更多
    private ImageView moreIv;
    private PLVECMorePopupView morePopupView;
    //监听器
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_playback_page_home_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        watchInfoLy = findViewById(R.id.watch_info_ly);
        bulletinLy = findViewById(R.id.bulletin_ly);
        playControlIv = findViewById(R.id.play_control_iv);
        playControlIv.setOnClickListener(this);
        playTimeTv = findViewById(R.id.play_time_tv);
        playProgressSb = findViewById(R.id.play_progress_sb);
        playProgressSb.setOnSeekBarChangeListener(playProgressChangeListener);
        totalTimeTv = findViewById(R.id.total_time_tv);
        moreIv = findViewById(R.id.more_iv);
        moreIv.setOnClickListener(this);

        morePopupView = new PLVECMorePopupView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    @Override
    protected void registerChatroomView() {
        chatroomPresenter.registerView(chatroomView);
    }

    @Override
    protected void updateWatchInfo(String coverImage, String publisher) {
        watchInfoLy.updateWatchInfo(coverImage, publisher);
        watchInfoLy.setVisibility(View.VISIBLE);
    }

    @Override
    protected void updateWatchInfo(long watchCount) {
        watchInfoLy.updateWatchCount(watchCount);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //设置播放状态
    @Override
    public void setPlayerState(PLVPlayerState state) {
        if (state == PLVPlayerState.PREPARED) {
            if (onViewActionListener != null) {
                totalTimeTv.setText(PLVTimeUtils.generateTime(onViewActionListener.onGetDurationAction(), true));
            }
        }
    }

    //设置播放信息
    @Override
    public void setPlaybackPlayInfo(PLVPlayInfoVO playInfoVO) {
        if (playInfoVO == null) {
            return;
        }
        int position = playInfoVO.getPosition();
        int totalTime = playInfoVO.getTotalTime();
        int bufPercent = playInfoVO.getBufPercent();
        boolean isPlaying = playInfoVO.isPlaying();
        boolean isSubViewPlaying = playInfoVO.isSubVideoViewPlaying();
        if (isSubViewPlaying) {
            playControlIv.setSelected(false);
            playProgressSb.setProgress(0);
            moreIv.setClickable(false);
            morePopupView.hide();
        } else {
            playControlIv.setClickable(true);
            playProgressSb.setClickable(true);
            moreIv.setClickable(true);
            //在拖动进度条的时候，这里不更新
            if (!isPlaySbDragging) {
                playTimeTv.setText(PLVTimeUtils.generateTime(position, true));
                if (totalTime > 0) {
                    playProgressSb.setProgress((int) ((long) playProgressSb.getMax() * position / totalTime));
                } else {
                    playProgressSb.setProgress(0);
                }
            }
            playProgressSb.setSecondaryProgress(playProgressSb.getMax() * bufPercent / 100);
            playControlIv.setSelected(isPlaying);
        }
    }

    @Override
    public void setOnViewActionListener(PLVECCommonHomeFragment.OnViewActionListener listener) {
        this.onViewActionListener = (OnViewActionListener) listener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 公告控制">
    private void acceptBulletinMessage(final PolyvBulletinVO bulletinVO) {
        bulletinLy.acceptBulletinMessage(bulletinVO);
    }

    private void removeBulletin() {
        bulletinLy.removeBulletin();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {
            super.onBulletinEvent(bulletinVO);
            acceptBulletinMessage(bulletinVO);
        }

        @Override
        public void onRemoveBulletinEvent() {
            super.onRemoveBulletinEvent();
            removeBulletin();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 进度条拖动事件处理">
    private SeekBar.OnSeekBarChangeListener playProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            isPlaySbDragging = true;
            if (onViewActionListener != null) {
                int seekPosition = (int) ((long) onViewActionListener.onGetDurationAction() * progress / seekBar.getMax());
                playTimeTv.setText(PLVTimeUtils.generateTime(seekPosition, true));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(false);
            isPlaySbDragging = false;
            if (onViewActionListener != null) {
                onViewActionListener.onSeekToAction(seekBar.getProgress(), seekBar.getMax());
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_control_iv) {
            if (onViewActionListener != null) {
                v.setSelected(onViewActionListener.onPauseOrResumeClick(v));
            }
        } else if (id == R.id.more_iv) {
            float currentSpeed = onViewActionListener == null ? 1 : onViewActionListener.onGetSpeedAction();
            morePopupView.showPlaybackMoreLayout(v, currentSpeed, new PLVECMorePopupView.OnPlaybackMoreClickListener() {
                @Override
                public void onChangeSpeedClick(View view, float speed) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onChangeSpeedClick(view, speed);
                    }
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener extends PLVECCommonHomeFragment.OnViewActionListener {
        //暂停/播放，true: doResume, false: doPause
        boolean onPauseOrResumeClick(View view);

        //切换倍速
        void onChangeSpeedClick(View view, float speed);

        //跳转播放
        void onSeekToAction(int progress, int max);

        //获取总视频时长
        int onGetDurationAction();

        //获取倍速
        float onGetSpeedAction();
    }
    // </editor-fold>
}
