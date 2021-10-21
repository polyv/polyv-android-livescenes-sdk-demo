package com.easefun.polyv.livehiclass.modules.liveroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 * 学生获得奖杯动效
 *
 * @author suhongtao
 */
public class PLVHCStudentCupGainLayout extends FrameLayout {

    private View rootView;
    private ConstraintLayout plvhcLiveRoomStudentCupGainMainLayout;
    private ImageView plvhcLiveRoomStudentCupGainLightBgIv;
    private ImageView plvhcLiveRoomStudentCupGainNameBgIv;
    private ImageView plvhcLiveRoomStudentCupGainCupIv;
    private TextView plvhcLiveRoomStudentCupGainNameTv;
    private TextView plvhcLiveRoomStudentCupGainLabelTv;
    private ImageView plvhcLiveRoomStudentCupLightenIv;

    private final LinkedList<String> animationLinkedList = new LinkedList<>();

    private final AnimatorSet animatorSet = new AnimatorSet();
    private SoundPool soundPool;
    private int currentPlayingStreamId = 0;
    private int cupGainSoundEffectId = 0;

    public PLVHCStudentCupGainLayout(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public PLVHCStudentCupGainLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCStudentCupGainLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_student_cup_gain_layout, this);
        findView();
        prepareAnimation();
        prepareSound();
    }

    private void findView() {
        plvhcLiveRoomStudentCupGainMainLayout = (ConstraintLayout) rootView.findViewById(R.id.plvhc_live_room_student_cup_gain_main_layout);
        plvhcLiveRoomStudentCupGainLightBgIv = (ImageView) rootView.findViewById(R.id.plvhc_live_room_student_cup_gain_light_bg_iv);
        plvhcLiveRoomStudentCupGainNameBgIv = (ImageView) rootView.findViewById(R.id.plvhc_live_room_student_cup_gain_name_bg_iv);
        plvhcLiveRoomStudentCupGainCupIv = (ImageView) rootView.findViewById(R.id.plvhc_live_room_student_cup_gain_cup_iv);
        plvhcLiveRoomStudentCupGainNameTv = (TextView) rootView.findViewById(R.id.plvhc_live_room_student_cup_gain_name_tv);
        plvhcLiveRoomStudentCupGainLabelTv = (TextView) rootView.findViewById(R.id.plvhc_live_room_student_cup_gain_label_tv);
        plvhcLiveRoomStudentCupLightenIv = (ImageView) findViewById(R.id.plvhc_live_room_student_cup_lighten_iv);
    }

    private void prepareAnimation() {
        setVisibility(GONE);

        animatorSet.play(prepareMainLayoutAnimation())
                .with(prepareLightBgAnimation())
                .with(prepareCupAnimation())
                .with(prepareNameBgAnimation())
                .with(prepareLabelAnimation())
                .with(prepareNameAnimation())
                .with(prepareLightenAnimation());

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                pickFirstToShow();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(VISIBLE);
                playCupGainSoundEffect();
            }
        });
    }

    private void prepareSound() {
        soundPool = new SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0);
        cupGainSoundEffectId = soundPool.load(getContext(), R.raw.plvhc_live_room_student_cup_gain_effect, 1);
    }

    public void show(String studentName) {
        animationLinkedList.addLast(studentName);
        pickFirstToShow();
    }

    public void destroy() {
        animationLinkedList.clear();
        animatorSet.cancel();
        if (currentPlayingStreamId != 0) {
            soundPool.stop(currentPlayingStreamId);
        }
    }

    private void pickFirstToShow() {
        if (!animationLinkedList.isEmpty() && !animatorSet.isRunning()) {
            String studentName = animationLinkedList.removeFirst();
            plvhcLiveRoomStudentCupGainNameTv.setText(studentName);
            animatorSet.start();
        }
    }

    private void playCupGainSoundEffect() {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }
        float streamVolumeCurrent = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        float streamVolumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        float volume = streamVolumeCurrent / streamVolumeMax;
        if (cupGainSoundEffectId != 0) {
            currentPlayingStreamId = soundPool.play(cupGainSoundEffectId, volume, volume, 1, 0, 1F);
        }
    }

    private AnimatorSet prepareMainLayoutAnimation() {
        // Scale in
        Animator scale1x = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleX", 40F / 144F, 180F / 144F).setDuration((long) (7F / 30F * 1000));
        Animator scale1y = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleY", 40F / 144F, 180F / 144F).setDuration((long) (7F / 30F * 1000));

        Animator scale2x = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleX", 180F / 144F, 120F / 144F).setDuration((long) (5F / 30F * 1000));
        Animator scale2y = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleY", 180F / 144F, 120F / 144F).setDuration((long) (5F / 30F * 1000));
        scale2x.setStartDelay((long) (5F / 30F * 1000));
        scale2y.setStartDelay((long) (5F / 30F * 1000));

        Animator scale3x = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleX", 120F / 144F, 160F / 144F).setDuration((long) (4F / 30F * 1000));
        Animator scale3y = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleY", 120F / 144F, 160F / 144F).setDuration((long) (4F / 30F * 1000));
        scale3x.setStartDelay((long) (12F / 30F * 1000));
        scale3y.setStartDelay((long) (12F / 30F * 1000));

        Animator scale4x = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleX", 160F / 144F, 144F / 144F).setDuration((long) (4F / 30F * 1000));
        Animator scale4y = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleY", 160F / 144F, 144F / 144F).setDuration((long) (4F / 30F * 1000));
        scale4x.setStartDelay((long) (16F / 30F * 1000));
        scale4y.setStartDelay((long) (16F / 30F * 1000));

        // Scale out
        Animator scale5x = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleX", 144F / 144F, 0F / 144F).setDuration((long) (10F / 30F * 1000));
        Animator scale5y = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "scaleY", 144F / 144F, 0F / 144F).setDuration((long) (10F / 30F * 1000));
        scale5x.setStartDelay((long) (60F / 30F * 1000));
        scale5y.setStartDelay((long) (60F / 30F * 1000));

        // Scale
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.play(scale1x).with(scale1y)
                .with(scale2x).with(scale2y)
                .with(scale3x).with(scale3y)
                .with(scale4x).with(scale4y)
                .with(scale5x).with(scale5y);

        // Alpha in
        Animator alphaIn = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "alpha", 0F, 1F).setDuration((long) (11F / 30F * 1000));
        // Alpha out
        Animator alphaOut = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainMainLayout, "alpha", 1F, 0F).setDuration((long) (10F / 30F * 1000));
        alphaOut.setStartDelay((long) (60F / 30F * 1000));
        // Alpha
        AnimatorSet alphaSet = new AnimatorSet();
        alphaSet.play(alphaIn).with(alphaOut);

        AnimatorSet mainLayoutAnimatorSet = new AnimatorSet();
        mainLayoutAnimatorSet.play(scaleSet).with(alphaSet);
        return mainLayoutAnimatorSet;
    }

    private AnimatorSet prepareLightBgAnimation() {
        // Rotation
        Animator rotation = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainLightBgIv, "rotation", 0, 270).setDuration(3000);
        // Alpha init set 0
        plvhcLiveRoomStudentCupGainLightBgIv.setAlpha(0F);
        // Alpha in
        Animator alphaIn = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainLightBgIv, "alpha", 0, 1).setDuration((long) (3F / 30F * 1000));
        alphaIn.setStartDelay((long) (5F / 30F * 1000));
        // Alpha out
        Animator alphaOut = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainLightBgIv, "alpha", 1, 0).setDuration((long) (3F / 30F * 1000));
        alphaOut.setStartDelay((long) (63F / 30F * 1000));

        AnimatorSet lightBgAnimatorSet = new AnimatorSet();
        lightBgAnimatorSet.play(rotation).with(alphaIn).with(alphaOut);
        return lightBgAnimatorSet;
    }

    private AnimatorSet prepareCupAnimation() {
        // Alpha init set 0
        plvhcLiveRoomStudentCupGainCupIv.setAlpha(0F);
        // Alpha in
        Animator alphaIn = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainCupIv, "alpha", 0, 1).setDuration((long) (5F / 30F * 1000));
        // Alpha out
        Animator alphaOut = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainCupIv, "alpha", 1, 0).setDuration((long) (4F / 30F * 1000));
        alphaOut.setStartDelay((long) (66F / 30F * 1000));

        AnimatorSet cupAnimatorSet = new AnimatorSet();
        cupAnimatorSet.play(alphaIn).with(alphaOut);
        return cupAnimatorSet;
    }

    private AnimatorSet prepareNameBgAnimation() {
        // Scale in
        Animator scaleInX = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainNameBgIv, "scaleX", 30F / 25F, 1).setDuration((long) (5F / 30F * 1000));
        Animator scaleInY = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainNameBgIv, "scaleY", 30F / 25F, 1).setDuration((long) (5F / 30F * 1000));
        scaleInX.setStartDelay((long) (15F / 30F * 1000));
        scaleInY.setStartDelay((long) (15F / 30F * 1000));
        // Alpha init set 0
        plvhcLiveRoomStudentCupGainNameBgIv.setAlpha(0F);
        // Alpha in
        Animator alphaIn = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainNameBgIv, "alpha", 0, 1).setDuration((long) (5F / 30F * 1000));
        alphaIn.setStartDelay((long) (15F / 30F * 1000));
        // Alpha out
        Animator alphaOut = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainNameBgIv, "alpha", 1, 0).setDuration((long) (4F / 30F * 1000));
        alphaOut.setStartDelay((long) (66F / 30F * 1000));

        AnimatorSet nameBgAnimatorSet = new AnimatorSet();
        nameBgAnimatorSet.play(scaleInX).with(scaleInY).with(alphaIn).with(alphaOut);
        return nameBgAnimatorSet;
    }

    private AnimatorSet prepareLabelAnimation() {
        // Alpha init set 0
        plvhcLiveRoomStudentCupGainLabelTv.setAlpha(0F);
        // Alpha in
        Animator alphaIn = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainLabelTv, "alpha", 0, 1).setDuration((long) (3F / 30F * 1000));
        alphaIn.setStartDelay((long) (22F / 30F * 1000));
        // Alpha out
        Animator alphaOut = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainLabelTv, "alpha", 1, 0).setDuration((long) (3F / 30F * 1000));
        alphaOut.setStartDelay((long) (70F / 30F * 1000));

        AnimatorSet labelAnimatorSet = new AnimatorSet();
        labelAnimatorSet.play(alphaIn).with(alphaOut);
        return labelAnimatorSet;
    }

    private AnimatorSet prepareNameAnimation() {
        // Alpha init set 0
        plvhcLiveRoomStudentCupGainNameTv.setAlpha(0F);
        // Alpha in
        Animator alphaIn = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainNameTv, "alpha", 0, 1).setDuration((long) (3F / 30F * 1000));
        alphaIn.setStartDelay((long) (18F / 30F * 1000));
        // Alpha out
        Animator alphaOut = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupGainNameTv, "alpha", 1, 0).setDuration((long) (3F / 30F * 1000));
        alphaOut.setStartDelay((long) (66F / 30F * 1000));

        AnimatorSet nameAnimatorSet = new AnimatorSet();
        nameAnimatorSet.play(alphaIn).with(alphaOut);
        return nameAnimatorSet;
    }

    private AnimatorSet prepareLightenAnimation() {
        // Alpha init set 0
        plvhcLiveRoomStudentCupLightenIv.setAlpha(0F);

        // Group 1
        // Translation move
        Animator translationMove1 = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupLightenIv, "translationX", 0, ConvertUtils.dp2px(56));
        translationMove1.setDuration((long) (7F / 30F * 1000)).setStartDelay((long) (23F / 30F * 1000));
        // Alpha
        Animator alpha1 = ObjectAnimator.ofFloat(plvhcLiveRoomStudentCupLightenIv, "alpha", 1, 0);
        alpha1.setDuration((long) (7F / 30F * 1000)).setStartDelay((long) (23F / 30F * 1000));

        // Group 2
        Animator translationMove2 = translationMove1.clone();
        translationMove2.setStartDelay((long) (32F / 30F * 1000));
        Animator alpha2 = alpha1.clone();
        alpha2.setStartDelay((long) (32F / 30F * 1000));

        AnimatorSet lightenAnimatorSet = new AnimatorSet();
        lightenAnimatorSet.play(translationMove1).with(alpha1)
                .with(translationMove2).with(alpha2);
        // Alpha init set 0
        lightenAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                plvhcLiveRoomStudentCupLightenIv.setAlpha(0F);
            }
        });

        return lightenAnimatorSet;
    }

}
