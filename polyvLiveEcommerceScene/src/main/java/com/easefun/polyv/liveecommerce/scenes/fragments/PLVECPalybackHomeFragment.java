package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.presenter.PLVPreviousPlaybackPresenter;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousAdapter;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousView;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECBulletinView;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.IPLVECPreviousDialogFragment;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.PLVECPreviousDialogFragment;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.previous.PLVECPreviousAdapter;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.livescenes.model.PLVPlaybackListVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

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
    //更多回放视频
    private ImageView moreVideoListIv;
    //监听器
    private OnViewActionListener onViewActionListener;
    //回放更多视频的弹窗
    private IPLVECPreviousDialogFragment previousPopupView;
    //回放视频列表
    private List<PLVPlaybackListVO.DataBean.ContentsBean> dataList;
    //当前回放视频的vid
    private String currentVid;

    //更多回放视频的presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPresenter;
    private PLVPreviousView plvPreviousView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_playback_page_home_fragment, container, false);
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
        moreVideoListIv = findViewById(R.id.more_video_list_iv);
        moreVideoListIv.setVisibility(View.GONE);
        //当进入的时候没有vid才会开启回放列表的功能
        if (TextUtils.isEmpty(liveRoomDataManager.getConfig().getVid())) {
            moreVideoListIv.setOnClickListener(this);
            moreVideoListIv.setVisibility(View.VISIBLE);
        }
        morePopupView = new PLVECMorePopupView();

        previousPresenter = new PLVPreviousPlaybackPresenter(liveRoomDataManager);
        previousPopupView = new PLVECPreviousDialogFragment();
        initPreviousView();
        dataList = new ArrayList<>();
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
    protected void updateWatchCount(long times) {
        watchInfoLy.updateWatchCount(times);
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
            morePopupView.hideAll();
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

        //判断是否播放完成，播放完成通知
        if (position >= totalTime && totalTime > 0) {
            if (previousPresenter != null) {
                previousPresenter.onPlayComplete();
            }
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

    // <editor-fold defaultstate="collapsed" desc="回放视频列表 - MVP模式view层的实现">

    /**
     * 设置PreviousView的参数
     */
    private void initPreviousView() {
        PLVPreviousView.Builder builder = new PLVPreviousView.Builder(getContext());
        //创建PLVPreviousView
        plvPreviousView = builder.create();
        PLVECPreviousAdapter plvecPreviousAdapter = new PLVECPreviousAdapter();
        plvecPreviousAdapter.setOnViewActionListener(new PLVPreviousAdapter.OnViewActionListener() {
            @Override
            public void changeVideoVidClick(String vid) {
                plvPreviousView.changePlaybackVideoVid(vid);
            }
        });
        builder.setAdapter(plvecPreviousAdapter)
                .setRecyclerViewLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false))
                .setRecyclerViewItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.left = ConvertUtils.dp2px(8);
                        outRect.bottom = ConvertUtils.dp2px(20);
                    }
                }).setThemeColor("#FFFFA611")
                .setOnPrepareChangeVidListener(new PLVPreviousView.PLVPreviousViewInterface.OnPrepareChangeVideoVidListener() {
                    @Override
                    public void onPrepareChangeVideoVid(String vid) {
                        if (onViewActionListener != null) {
                            onViewActionListener.onChangePlaybackVidAndPlay(vid);
                        }
                    }
                });
        plvPreviousView.setParams(builder);
        //注册presenter
        if (previousPresenter != null) {
            previousPresenter.registerView(plvPreviousView.getPreviousView());
        }
        //注意这里要判断vid是否为空，为空才会去请求
        if (liveRoomDataManager.getConfig().getVid() == null || liveRoomDataManager.getConfig().getVid().isEmpty()) {
            //当进入的时候没有输入vid，那么这里就要请求回放视频列表
            plvPreviousView.requestPreviousList();
        }
    }
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
        } else if (id == R.id.more_video_list_iv) {
            //弹出更多回放视频的popview
            if (previousPopupView == null) {
                previousPopupView = new PLVECPreviousDialogFragment();
            }
            if (plvPreviousView != null) {
                previousPopupView.setPrviousView(plvPreviousView);
            }
            previousPopupView.showPlaybackMoreVideoDialog(dataList, currentVid, this);
            //设置DialogFragment隐藏时的回调方法
            previousPopupView.setDismissListener(new IPLVECPreviousDialogFragment.DismissListener() {
                @Override
                public void onDismissListener() {
                    //在销毁的时候将previousPopupView置空，防止previousPopupView销毁的时候因为PLVECPlaybackFragment
                    //持有previousPopupView导致不能销毁成功从而导致内存泄漏
                    previousPopupView = null;
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

        /**
         * 切换回放视频的vid并立即播放视频
         *
         * @param vid
         */
        void onChangePlaybackVidAndPlay(String vid);
    }
    // </editor-fold>
}
