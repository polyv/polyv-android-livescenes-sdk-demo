package com.easefun.polyv.livecloudclass.modules.media.danmu;

import android.text.TextUtils;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Author:lzj
 * Time:2023/5/9
 * Description: 弹幕设置Manager
 */
public class PLVLCDanmuSettingManager {

    // <editor-fold defaultstate="collapsed" desc="属性">
    private static final String SP_CHANNELID_DANMU = "danmu_channel_";
    public static final String DANMU_NONE_CACHE_SETTING = "none";
    private String cacheSetting = DANMU_NONE_CACHE_SETTING;
    private String channelId;
    private String danmuSpeed = DANMU_NONE_CACHE_SETTING;

    private Disposable updateCacheDateDisposable;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVLCDanmuSettingManager(String channelId){
        this.channelId = channelId;
    }
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="外部方法">
    public String getSpeedByCacheData(){
        if(TextUtils.isEmpty(channelId)){
            cacheSetting = DANMU_NONE_CACHE_SETTING;
        }
        if(cacheSetting.equals(DANMU_NONE_CACHE_SETTING)){
            cacheSetting = SPUtils.getInstance().getString(SP_CHANNELID_DANMU, DANMU_NONE_CACHE_SETTING);
            String speed = getSpeedByData(cacheSetting);
            return speed;
        }
        return danmuSpeed;
    }

    //更新缓存的 弹幕速度值
    public void updateSpeedByData(int speed){
        danmuSpeed = String.valueOf(speed);
        StringBuffer buffer = new StringBuffer();
        buffer.append("speed_").append(danmuSpeed).append("%");
        updateCache(buffer.toString());
    }
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void updateCache(final String data){
        if(TextUtils.isEmpty(channelId)){
            return ;
        }
        dispose();
        //延迟写入，防止短时间内多次滑动 导致频繁写入数据
        updateCacheDateDisposable = Observable.just(1).delay(2000, TimeUnit.MILLISECONDS)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        SPUtils.getInstance().put(SP_CHANNELID_DANMU, data);
                    }
                }).subscribe();
    }

    private void dispose(){
        if(updateCacheDateDisposable != null){
            updateCacheDateDisposable.dispose();
        }
    }

    //当前通过解析数据获取到想要的速度，字符串格式为 speed_200%color_#123%
    private String getSpeedByData(String data){
        //默认速度 200
        if(data.equals(DANMU_NONE_CACHE_SETTING)){
            return danmuSpeed;
        }
        String[] splits = data.split("%");
        for (String split : splits) {
            if(split.contains("speed")){
                String[] s = split.split("_");
                danmuSpeed = s[1];
                break;
            }
        }
        return danmuSpeed;
    }
    // </editor-fold>
}
