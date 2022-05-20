package com.easefun.polyv.livecommon.module.modules.reward.view.effect;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import io.reactivex.functions.Consumer;

/**
 * date: 2019-12-05
 * author: hwj
 * description:积分打赏动效。即从左到右弹出，停留2秒 ，又上划消失。
 * 动画分为top、bottom两个item。分别取event来呈现
 */
public class PLVPointRewardEffectWidget extends FrameLayout {

    private View plvVPointRewardEffectBg1;
    private TextView plvTvPointRewardEffectNickname1;
    private ImageView plvIvPointRewardEffect1;
    private View plvVPointRewardEffectBg2;
    private TextView plvTvPointRewardEffectNickname2;
    private ImageView plvIvPointRewardEffect2;

    private IPLVPointRewardEventProducer pointRewardEventProducer;
    private RelativeLayout rlPointRewardEffectTop;
    private RelativeLayout rlPointRewardEffectBottom;
    private TextView tvPointRewardEffectRewardContent1;
    private TextView tvPointRewardEffectRewardContent2;

    private boolean isFetchingBottom;
    private PLVPointRewardStrokeTextView plvTvPointRewardEffectCount1;
    private LinearLayout plvLlPointRewardCount1;
    private PLVPointRewardStrokeTextView plvTvPointRewardEffectCount2;
    private LinearLayout plvLlPointRewardCount2;

    private IPLVPointRewardEventProducer.OnPreparedListener onPreparedListener;

    private boolean isLandscape = false;

    /**
     * 标志位，用于标记是否release并隐藏动画
     */
    private boolean isRelease = false;

    // <editor-fold defaultstate="collapsed" desc="构造函数">
    public PLVPointRewardEffectWidget(Context context) {
        this(context, null);
    }

    public PLVPointRewardEffectWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPointRewardEffectWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.plv_point_reward_effect, this, true);
        init();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置积分打赏事件生产者">
    public void setEventProducer(IPLVPointRewardEventProducer producer) {
        pointRewardEventProducer = producer;
        prepareEventProducer();
    }

    private void prepareEventProducer() {
        if(pointRewardEventProducer == null) {
            return;
        }
        pointRewardEventProducer.prepare(onPreparedListener = new IPLVPointRewardEventProducer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                //当生产者准备好后，就开始从生产者中拉取积分打赏事件来展示。

                fetchEventForTopItem();
                fetchEventForBottomItem();
            }
        });
    }

    private void destroyEventProducer() {
        if(pointRewardEventProducer != null) {
            pointRewardEventProducer.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void init() {
        initView();
        isLandscape = ScreenUtils.isLandscape();
    }

    private void initView() {
        rlPointRewardEffectTop = findViewById(R.id.rl_point_reward_effect_top);
        rlPointRewardEffectBottom = findViewById(R.id.rl_point_reward_effect_bottom);

        //上
        plvVPointRewardEffectBg1 = findViewById(R.id.plv_v_point_reward_effect_bg_1);
        plvTvPointRewardEffectNickname1 = findViewById(R.id.plv_tv_point_reward_effect_nickname_1);
        plvIvPointRewardEffect1 = findViewById(R.id.plv_iv_point_reward_effect_1);
        tvPointRewardEffectRewardContent1 = findViewById(R.id.tv_point_reward_effect_reward_content_1);
        plvTvPointRewardEffectCount1 = findViewById(R.id.plv_tv_point_reward_effect_count_1);
        plvLlPointRewardCount1 = findViewById(R.id.plv_ll_point_reward_count_1);
        //下
        plvVPointRewardEffectBg2 = findViewById(R.id.plv_v_point_reward_effect_bg_2);
        plvTvPointRewardEffectNickname2 = findViewById(R.id.plv_tv_point_reward_effect_nickname_2);
        plvIvPointRewardEffect2 = findViewById(R.id.plv_iv_point_reward_effect_2);
        tvPointRewardEffectRewardContent2 = findViewById(R.id.tv_point_reward_effect_reward_content_2);
        plvTvPointRewardEffectCount2 = findViewById(R.id.plv_tv_point_reward_effect_count_2);
        plvLlPointRewardCount2 = findViewById(R.id.plv_ll_point_reward_count_2);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="从队列中获取事件">
    private void fetchEventForTopItem() {
        pointRewardEventProducer.fetchEvent(new IPLVPointRewardEventProducer.IPLVOnFetchRewardEventListener() {
            @Override
            public void onFetchSucceed(PLVRewardEvent event) {
                //如果是横屏，那么抛弃这个打赏事件，不再执行动画
                if (isLandscape) {
                    return;
                }

                handleEvent(event, plvTvPointRewardEffectNickname1,
                        plvIvPointRewardEffect1, plvLlPointRewardCount1,
                        plvTvPointRewardEffectCount1, tvPointRewardEffectRewardContent1
                );

                makeOnceAnim(rlPointRewardEffectTop, plvLlPointRewardCount1, new Runnable() {
                    @Override
                    public void run() {
                        if (isFetchingBottom) {
                            //如果第一行的动画完成时，第二行依然没有收到event，那么下一次又要发送到第一行了，那么重置
                            //event生产者线程，并从新开始从第一行获取数据。
                            // TODO: 2019-12-06 每次都要销毁原先线程和创建新的线程，损耗较大，待优化。
                            pointRewardEventProducer.destroy();
                            pointRewardEventProducer.prepare(onPreparedListener = new IPLVPointRewardEventProducer.OnPreparedListener() {
                                @Override
                                public void onPrepared() {
                                    fetchEventForTopItem();
                                    fetchEventForBottomItem();
                                }
                            });
                            return;
                        }
                        fetchEventForTopItem();
                    }
                });
            }
        });
    }

    private void fetchEventForBottomItem() {
        isFetchingBottom = true;
        pointRewardEventProducer.fetchEvent(new IPLVPointRewardEventProducer.IPLVOnFetchRewardEventListener() {
            @Override
            public void onFetchSucceed(PLVRewardEvent event) {
                isFetchingBottom = false;
                if (isLandscape) {
                    return;
                }
                handleEvent(event, plvTvPointRewardEffectNickname2,
                        plvIvPointRewardEffect2, plvLlPointRewardCount2,
                        plvTvPointRewardEffectCount2, tvPointRewardEffectRewardContent2
                );

                makeOnceAnim(rlPointRewardEffectBottom, plvLlPointRewardCount2, new Runnable() {
                    @Override
                    public void run() {
                        fetchEventForBottomItem();
                    }
                });
            }
        });
    }

    private void handleEvent(PLVRewardEvent event, TextView tvNickName, ImageView ivGoodImage,
                             LinearLayout llGoodCountParent, TextView tvGoodCount, TextView tvGoodContent) {
        String goodImageUrl = event.getContent().getGimg();
        String userImageUrl = event.getContent().getUimg();
        int goodNum = event.getContent().getGoodNum();
        String rewardContent = event.getContent().getRewardContent();
        String nickname = event.getContent().getUnick();

        tvNickName.setText(nickname);
        loadImage(goodImageUrl, ivGoodImage);

        if (goodNum > 1) {
            llGoodCountParent.setVisibility(VISIBLE);
            tvGoodCount.setText(String.valueOf(goodNum));
        } else {
            llGoodCountParent.setVisibility(INVISIBLE);
        }

        tvGoodContent.setText("赠送   " + rewardContent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="开始一次动画">
    private void makeOnceAnim(final RelativeLayout itemView,final  View zoomView, final Runnable animEnd) {
        //开始动画
        //当动画结束后，再次fetch。

        //从左到右进入0.15s
        Animation enterAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0
        );

        enterAnim.setDuration((long) (0.15 * 1000));
        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                itemView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //二次标记，用于标记delay过程中的状态变更，避免关闭后又开启，导致播放了exitAnimation
                final boolean isStop = isRelease;

                //停留两秒
                PLVRxTimer.delay(2 * 1000, new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        final boolean stopTag = isStop || isRelease;

                        //向上消失0.1s
                        Animation exitAnim = new TranslateAnimation(
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, -1
                        );
                        exitAnim.setDuration((long) (0.1 * 1000));
                        exitAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                if(stopTag){
                                    itemView.setVisibility(View.INVISIBLE);
                                    animation.cancel();
                                    itemView.clearAnimation();
                                }
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                itemView.setVisibility(INVISIBLE);
                                animEnd.run();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {/**/}
                        });
                        if(!stopTag){
                            itemView.startAnimation(exitAnim);
                        } else {
                            itemView.setVisibility(View.INVISIBLE);
                        }
                    }
                });

                if (!zoomView.isShown()) {
                    return;
                }

                //数字动效变化：1. 放大到150%（0.1s)。2 .停留0.05s。 3. 缩小到100%（0.15s）
                Animation magnify = new ScaleAnimation(1f, 1.5f, 1f, 1.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                );
                magnify.setFillAfter(true);
                magnify.setDuration((long) (0.1 * 1000));
                magnify.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {/**/}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        PLVRxTimer.delay((long) (0.05 * 1000), new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {
                                Animation shrink = new ScaleAnimation(1.5f, 1f, 1.5f, 1f,
                                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                                );
                                shrink.setDuration((long) (0.15 * 1000));
                                zoomView.startAnimation(shrink);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {/**/}
                });
                zoomView.startAnimation(magnify);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {/**/}
        });
        itemView.startAnimation(enterAnim);
    }
    // </editor-fold>

    private void loadImage(String url, ImageView iv) {
        if (!url.startsWith("http")) {
            url = "https:/" + url;
        }
        PLVImageLoader.getInstance().loadImage(iv.getContext(),url,iv);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandscape) {
            hideAndReleaseEffect();
        } else {
            showAndPrepareEffect();
        }
    }

    private void clearEffectAnim() {
        rlPointRewardEffectTop.clearAnimation();
        rlPointRewardEffectTop.setVisibility(GONE);
        rlPointRewardEffectBottom.clearAnimation();
        rlPointRewardEffectBottom.setVisibility(GONE);

        plvLlPointRewardCount1.clearAnimation();
        plvLlPointRewardCount2.clearAnimation();
    }

    public void hideAndReleaseEffect(){
        isRelease = true;
        clearEffectAnim();
        this.setVisibility(INVISIBLE);
        destroyEventProducer();
    }


    public void showAndPrepareEffect(){
        isRelease = false;
        this.setVisibility(VISIBLE);
        prepareEventProducer();
    }
}
