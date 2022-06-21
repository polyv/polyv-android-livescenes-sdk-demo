package com.easefun.polyv.livecommon.module.modules.reward.view.effect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.plv.socket.event.chat.PLVRewardEvent;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 积分打赏SVGA动画播放辅助类
 * 以队列形式保存打赏事件，播放完成一次动画再播放下一次
 * 由于svga库自己管理了线程解析资源，这里不做子线程开辟，而是Handler控制事件分发和动画播放
 */
public class PLVRewardSVGAHelper {

    private final String TAG = this.getClass().getSimpleName();

    //事件队列
    private Queue<PLVRewardEvent> eventQueue = new LinkedList<>();

    private RewardHandler handler;
    private WeakReference<SVGAImageView> imageViewRef;
    private SVGAParser parser;

    String[] svgaFile = null;

    public void init(SVGAImageView imageView, SVGAParser parser) {
        this.parser = parser;
        imageView.setLoops(1);//控制只播放一次
        imageView.setFillMode(SVGAImageView.FillMode.Clear);
        imageViewRef = new WeakReference<>(imageView);
        if (handler == null) {
            handler = new RewardHandler(Looper.getMainLooper(), PLVRewardSVGAHelper.this);
        }

        try {
            svgaFile = imageView.getContext().getAssets().list("svg");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "svga file list failed in assets");
        }

    }

    /**
     * 添加打赏事件，添加后会自动加入队列，出列播放对应的svga
     */
    public void addEvent(PLVRewardEvent event) {
        synchronized (PLVRewardSVGAHelper.class) {
            eventQueue.add(event);
            handler.sendEmptyMessage(RewardHandler.ADD_EVENT);
        }
    }

    /**
     * 清除打赏队列，打赏动画
     */
    public void clear(){
        synchronized (PLVRewardSVGAHelper.class){
            eventQueue.clear();
            handler.removeCallbacksAndMessages(null);
            if(imageViewRef != null){
                imageViewRef.get().clearAnimation();
                imageViewRef.get().clear();
            }
        }
    }


    /**
     * 根据名称选择本地的svga文件
     *
     * @param name
     * @return
     */
    private String hitSvgaFile(String name) {
        if (svgaFile == null) {
            return "";
        }

        for (int i = 0; i < svgaFile.length; i++) {
            String svga = svgaFile[i].replace(".svga", "");
            if (svga.equals(name)) {
                return svgaFile[i];
            }
        }
        return "";
    }

    /**
     * 打赏svga控制handler
     */
    private static class RewardHandler extends Handler {

        private static final int FETCH_EVENT = 0x01;
        private static final int ADD_EVENT = 0x02;

        private WeakReference<PLVRewardSVGAHelper> helperWeakReference;
        private volatile boolean isFetching = false;

        public RewardHandler(Looper looper, PLVRewardSVGAHelper helper) {
            super(looper);
            this.helperWeakReference = new WeakReference<>(helper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ADD_EVENT:
                    if (!isFetching) {
                        synchronized (this) {
                            isFetching = true;
                        }
                        sendEmptyMessage(FETCH_EVENT);
                    }
                    break;
                case FETCH_EVENT:
                    post(new Runnable() {
                        @Override
                        public void run() {
                            makeRewardSvga();
                        }
                    });
                    break;
                default:
                    break;
            }
        }

        /**
         * 消费队列一个打赏事件，播放一次svga动画，动画播放完成后，重新发送FETCH_EVENT，直到队列消费完所有事件
         */
        private void makeRewardSvga() {
            PLVRewardEvent rewardEvent = null;
            synchronized (PLVRewardSVGAHelper.class) {
                final PLVRewardSVGAHelper helper = helperWeakReference.get();
                rewardEvent = helper.eventQueue.poll();
                if (rewardEvent != null) {
                    synchronized (this) {
                        isFetching = true;
                    }
                    String gimg = rewardEvent.getContent().getGimg();
//                    gimg = "http://liveimages.videocc.net/uploaded/images/webapp/channel/donate/03-good.png";
                    if (!TextUtils.isEmpty(gimg) && gimg.contains("/")) {
                        String name = gimg.substring(gimg.lastIndexOf("-") + 1, gimg.lastIndexOf("."));
                        String hit = helper.hitSvgaFile(name);
                        if (!TextUtils.isEmpty(hit)) {
                            helper.parser.decodeFromAssets("svg/" + hit, new SVGAParser.ParseCompletion() {
                                @Override
                                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                                    helper.imageViewRef.get().setVideoItem(svgaVideoEntity);
                                    helper.imageViewRef.get().startAnimation();
                                }

                                @Override
                                public void onError() {

                                }
                            }, null);
                            helper.imageViewRef.get().setCallback(new SVGACallback() {
                                @Override
                                public void onPause() {

                                }

                                @Override
                                public void onFinished() {
                                    //动画播放完成才发送FETCH_EVENT，去消费下一个事件
                                    helper.handler.sendEmptyMessage(FETCH_EVENT);
                                }

                                @Override
                                public void onRepeat() {

                                }

                                @Override
                                public void onStep(int i, double v) {

                                }
                            });
                        } else {
                            //没有对应的动画，跳过
                            helper.handler.sendEmptyMessage(FETCH_EVENT);
                        }
                    }
                } else {
                    synchronized (this) {
                        isFetching = false;
                    }
                }
            }
        }
    }


}
