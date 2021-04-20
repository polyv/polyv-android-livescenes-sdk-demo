package com.easefun.polyv.liveecommerce.modules.reward.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.liveecommerce.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 打赏礼物动画view
 */
public class PLVECRewardGiftAnimView extends FrameLayout {
    private TextView rewardUserNameTv;
    private TextView rewardGiftNameTv;
    private ImageView rewardGiftPicIv;

    private List<RewardGiftInfo> rewardGiftInfoList = new ArrayList<>();
    private boolean isStart;
    private Disposable acceptRewardGiftDisposable;

    public PLVECRewardGiftAnimView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECRewardGiftAnimView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECRewardGiftAnimView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        rewardGiftInfoList.clear();
        if (acceptRewardGiftDisposable != null) {
            acceptRewardGiftDisposable.dispose();
        }
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_reward_gift_anim_layout, this);
        rewardUserNameTv = findViewById(R.id.reward_user_name_tv);
        rewardGiftNameTv = findViewById(R.id.reward_gift_name_tv);
        rewardGiftPicIv = findViewById(R.id.reward_gift_pic_iv);
    }

    private void showRewardLayout() {
        if (rewardGiftInfoList.isEmpty()) {
            setVisibility(View.INVISIBLE);
            TranslateAnimation animation = new TranslateAnimation(0f, -getWidth(), 0f, 0f);
            animation.setDuration(400);
            startAnimation(animation);
            isStart = !isStart;
            return;
        }

        final RewardGiftInfo rewardGiftInfo = rewardGiftInfoList.remove(0);

        acceptRewardGiftDisposable = Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setVisibility(View.VISIBLE);
                        rewardUserNameTv.setText(rewardGiftInfo.getUserName());
                        rewardGiftNameTv.setText("赠送    " + rewardGiftInfo.getGiftName());
                        rewardGiftPicIv.setImageResource(rewardGiftInfo.getGiftDrawableId());
                        TranslateAnimation animation = new TranslateAnimation(-getWidth(), 0f, 0f, 0f);
                        animation.setDuration(400);
                        startAnimation(animation);
                    }
                })
                .delay(400 + 1200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        showRewardLayout();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    public void acceptRewardGiftMessage(final RewardGiftInfo rewardGiftInfo) {
        post(new Runnable() {
            @Override
            public void run() {
                rewardGiftInfoList.add(rewardGiftInfo);
                //列表最多10条数据。超过10条数据时，如果有新来的礼物数据，则移除旧的数据
                if (rewardGiftInfoList.size() > 10) {
                    rewardGiftInfoList.remove(0);
                }
                if (!isStart) {
                    isStart = !isStart;
                    showRewardLayout();
                }
            }
        });
    }

    public static class RewardGiftInfo {
        private String userName;//送礼物的用户昵称
        private String giftName;//礼物名称
        private int giftDrawableId;//礼物图片Id

        public RewardGiftInfo(String userName, String giftName, int giftDrawableId) {
            this.userName = userName;
            this.giftName = giftName;
            this.giftDrawableId = giftDrawableId;
        }

        public String getUserName() {
            return userName;
        }

        public String getGiftName() {
            return giftName;
        }

        public int getGiftDrawableId() {
            return giftDrawableId;
        }
    }
}
