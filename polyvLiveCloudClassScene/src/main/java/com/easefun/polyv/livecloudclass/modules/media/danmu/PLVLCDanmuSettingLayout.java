package com.easefun.polyv.livecloudclass.modules.media.danmu;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.seekbar.PLVRangeSeekBar;
import com.easefun.polyv.livecommon.ui.widget.seekbar.PLVSeekBarOnRangeChangedListener;

/**
 * Author:lzj
 * Time:2023/5/7
 * Description:
 */
public class PLVLCDanmuSettingLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private String  channelId;
    PLVLCDanmuSettingManager manager;

    //popupWindow
    private PopupWindow popupWindow;
    //View
    private View root;
    private View anchor;

    private PLVRangeSeekBar seekBar;
    private TextView speedTv;

    //速度级别
    private PLVDanmuSpeedType speedType;
    private PLVLCDanmuWrapper danmuWrapper;

    //该频道的本地是否有更改过弹幕速度，如果有的话，使用本地频道速度
    private boolean isLocalCache = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造函数">
    public PLVLCDanmuSettingLayout(View anchor) {
        this.anchor = anchor;
        if (popupWindow == null) {
            popupWindow = new PopupWindow(anchor.getContext());

            View.OnClickListener handleHideListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            };
            root = PLVViewInitUtils.initPopupWindow(anchor, R.layout.plvlc_danmusetting_layout, popupWindow, handleHideListener);
            popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            initView();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        seekBar = root.findViewById(R.id.plvlc_danmu_setting_speed_sb);
        //这里要注意 后面是直接返回速度的
        seekBar.setOnRangeChangedListener(new PLVSeekBarOnRangeChangedListener() {
            @Override
            public void onRangeChanged(PLVRangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {

            }

            @Override
            public void onStartTrackingTouch(PLVRangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(PLVRangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onRangeChangeStep(int step) {
                if(step >= 0 && step < 5){
                    PLVDanmuSpeedType speedType = PLVDanmuSpeedType.matchByLevel(step);
                    speedTv.setText(speedType.getSpeedType());
                    if(danmuWrapper != null){
                        danmuWrapper.setDanmuSpeed(speedType.speed);
                    }
                    if(manager !=null){
                        manager.updateSpeedByData(speedType.speed);
                    }
                }
            }
        });

        speedTv = root.findViewById(R.id.plvlc_danmu_setting_speed_dsc_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化Data">
    private void initCacheData() {
        manager = new PLVLCDanmuSettingManager(channelId);
        String speedByCacheData = manager.getSpeedByCacheData();
        if(speedByCacheData.equals(PLVLCDanmuSettingManager.DANMU_NONE_CACHE_SETTING)) {
            isLocalCache = false;
            //没有本地缓存设置，那么就使用默认速度
            setProgressBySpeed(PLVDanmuSpeedType.DANMU_NORMAL.speed);
        } else {
            isLocalCache = true;
            //设置本地速度
            setProgressBySpeed(Integer.parseInt(speedByCacheData));
        }
    }

    private void setProgressBySpeed(int speed) {
        PLVDanmuSpeedType speedType = PLVDanmuSpeedType.matchBySpeed(speed);
        float stepPercent = 1.0f / (seekBar.getSteps());
        float progress = (speedType.level) * stepPercent * seekBar.getMaxProgress();
        seekBar.setProgress(progress);
        //根据速度来筛选速度文本
        speedTv.setText(speedType.getSpeedType());
        if(danmuWrapper != null) {
            danmuWrapper.setDanmuSpeed(speedType.speed);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="显示/隐藏控制">
    public void hide() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public void show() {
        show(root);
    }

    private void show(View contentView) {
        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                hide();
            }
            popupWindow.setContentView(contentView);
            popupWindow.showAtLocation(anchor, Gravity.RIGHT, 0, 0);
        }
    }

    public void registerDanWrapper(PLVLCDanmuWrapper danmuWrapper) {
        this.danmuWrapper = danmuWrapper;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void destroy(){
        danmuWrapper = null;
    }

    public void setDanmuSpeedOnServer(int speed) {
        if(!isLocalCache) {
            //本地没有缓存记录 就响应后端的速度
            setProgressBySpeed(speed);
        }
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
        initCacheData();
    }
    // </editor-fold>
}
