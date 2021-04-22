package com.easefun.polyv.livestreamer.modules.liveroom;

/**
 * date: 2019/10/15 0015
 *
 * @author hwj
 * description 倒计时View
 */
public interface IPLVLSCountDownView {
    void startCountDown();

    void stopCountDown();

    void setOnCountDownListener(OnCountDownListener listener);

    interface OnCountDownListener {
        //倒计时完成
        void onCountDownFinished();

        //倒计时被取消
        void onCountDownCanceled();
    }
}
