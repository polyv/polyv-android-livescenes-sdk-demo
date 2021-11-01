package com.easefun.polyv.livecommon.module.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.Map;

/**
 * 直播行为本地活动配置
 * 用于缓存直播的状态信息，如横竖屏状态，当前摄像头状态
 */
public class PLVLiveLocalActionHelper {

    /**
     * 当超过此数值时，将不在保存到本地
     */
    private static final int MAX_COUNT = 10;

    private static final String NAME = "PLVLocationAction";

    private SharedPreferences sp;

    private String currentChannel;

    private SimpleArrayMap<String, Action> actions;

    private static volatile PLVLiveLocalActionHelper INSTANCE;

    public static PLVLiveLocalActionHelper getInstance() {

        if (INSTANCE == null) {
            synchronized (PLVLiveLocalActionHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVLiveLocalActionHelper();
                }
            }
        }
        return INSTANCE;
    }

    private PLVLiveLocalActionHelper() {
        sp = Utils.getApp().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        actions = new SimpleArrayMap<>(MAX_COUNT);
        //获取缓存信息
        Map<String, String> all = (Map<String, String>) sp.getAll();
        long lastDay = System.currentTimeMillis() - 86400000L;
        SharedPreferences.Editor edit = sp.edit();
        for (Map.Entry<String, String> entry : all.entrySet()) {
            Action action = PLVGsonUtil.fromJson(Action.class, entry.getValue());
            //超过限制后，删除一天前的缓存
            if(all.size() >= MAX_COUNT && action != null && action.timestamp <= lastDay){
                edit.remove(entry.getKey());
                continue;
            }
            actions.put(entry.getKey(), action);
        }
        edit.commit();
    }

    public String getCurrentChannel() {
        return currentChannel;
    }

    public void enterChannel(String currentChannel) {
        this.currentChannel = currentChannel;
    }

    public Action getChannelAction(String channel) {
        Action action = actions.get(channel);
        if (action == null) {
            Action defaultAction = new Action(channel);
            defaultAction.isPortrait = ScreenUtils.isPortrait();
            actions.put(channel, defaultAction);
            return defaultAction;
        }
        return action;
    }

    /**
     * 更新操作行为状态
     * @param action 本地操作行为状态，如切换横竖屏
     */
    public void updateAction(@NonNull Action action) {
        if (action == null) return;
        actions.put(action.channel, action);
        saveAction(action);
    }

    /**
     * 更新屏幕方向
     */
    public void updateOrientation(boolean isPortrait) {
        Action action = getChannelAction(currentChannel);
        action.isPortrait = isPortrait;
        saveAction(action);
    }

    /**
     * 更新码率
     */
    public void updateBitrate(int bitrate) {
        Action action = getChannelAction(currentChannel);
        action.bitrate = bitrate;
        saveAction(action);
    }

    /**
     * 更新摄像头允许状态
     */
    public void updateCameraEnable(boolean isEnable) {
        Action action = getChannelAction(currentChannel);
        action.isEnableCamera = isEnable;
        saveAction(action);
    }

    /**
     * 更新摄像头方向
     */
    public void updateCameraDirection(boolean isFrontCamera) {
        Action action = getChannelAction(currentChannel);
        action.isFrontCamera = isFrontCamera;
        saveAction(action);
    }

    /**
     * 更新ppt类型
     * @param type 0 - 白板；1 - ppt
     */
    public void updatePptType(int type){
        Action action = getChannelAction(currentChannel);
        action.pptType = type;
        saveAction(action);
    }

    private void saveAction(Action action){
        sp.edit().putString(currentChannel, PLVGsonUtil.toJson(action)).commit();
    }

    /**
     * 操作行为状态实体类
     * 用于保存当前用户操作改变的直播状态的数据。如横竖屏，摄像头开关，麦克风等
     */
    public static class Action {
        public Action(String channel) {
            this.channel = channel;
        }

        String channel;//唯一识别
        long timestamp = System.currentTimeMillis();
        public boolean isPortrait;
        public int bitrate;
        public boolean isEnableCamera = true;//是否允许摄像头
        public boolean isFrontCamera = true;//是否是前置摄像头，默认是的
        //白板还是ppt（0-白板，1-ppt）,目前仅有三分屏使用
        public int pptType = 0;


    }


}
