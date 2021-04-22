package com.easefun.polyv.livecommon.module.utils;

import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.easefun.polyv.businesssdk.api.common.player.PolyvBaseVideoView;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayerScreenRatio;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PLVVideoSizeUtils {

    //适配播放器的填充模式和位置大小
    public static void fitVideoRatioAndRect(PolyvBaseVideoView baseVideoView, ViewParent viewParent, Rect rect) {
        int ratio = fitVideoRatio(baseVideoView);
        fitVideoRect(ratio == PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT, viewParent, rect);
    }

    //适配播放器的位置大小，isFill：是否等比铺满
    public static void fitVideoRect(boolean isFill, ViewParent viewParent, Rect rect) {
        if (!(viewParent instanceof ViewGroup)) {
            return;
        }
        ViewGroup videoViewParent = (ViewGroup) viewParent;
        if (!(videoViewParent.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) videoViewParent.getLayoutParams();
        if (!isFill && rect != null) {
            if (rect.bottom > rect.top) {
                lp.height = rect.bottom - rect.top;
            }
            if (rect.right > rect.left) {
                lp.width = rect.right - rect.left;
            }
            lp.topMargin = rect.top;
            lp.leftMargin = rect.left;
        } else {
            lp.height = -1;
            lp.width = -1;
            lp.topMargin = 0;
            lp.leftMargin = 0;
        }
        videoViewParent.requestLayout();
        videoViewParent.invalidate();
    }

    //适配播放器的视频比例，如果宽>=高，那么使用等比缩放，否则使用等比填充父窗
    public static int fitVideoRatio(PolyvBaseVideoView baseVideoView) {
        int ratio = -1;
        if (baseVideoView != null) {
            int[] videoSize = getVideoWH(baseVideoView);
            if (videoSize[0] >= videoSize[1]) {
                ratio = PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT;
                baseVideoView.setAspectRatio(ratio);
            } else {
                ratio = PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT;
                baseVideoView.setAspectRatio(ratio);
            }
        }
        return ratio;
    }

    //获取视频的宽高
    public static int[] getVideoWH(PolyvBaseVideoView baseVideoView) {
        if (baseVideoView != null) {
            IjkMediaPlayer mediaPlayer = baseVideoView.getIjkMediaPlayer();
            if (mediaPlayer != null) {
                return new int[]{mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight()};
            }
        }
        return new int[]{0, 0};
    }

    //获取视频区域显示的高度
    public static int getVideoDisplayRectHeight(PolyvBaseVideoView baseVideoView, ViewParent viewParent, Rect rect) {
        //视频显示区域的高度
        int videoDisplayRectHeight = 0;
        int videoDisplayRectWidth = 0;
        //视频的宽高
        int[] videoSize = PLVVideoSizeUtils.getVideoWH(baseVideoView);
        if (videoSize[0] == 0 || videoSize[1] == 0) {
            return videoDisplayRectHeight;
        }
        if (videoSize[0] >= videoSize[1]) {//视频的宽>=视频的高
            //播放器布局的高度
            int videoViewParentHeight = rect.bottom - rect.top;
            //播放器布局的宽度
            int videoViewParentWidth = ((ViewGroup) viewParent).getWidth();
            if (videoViewParentHeight == 0 || videoViewParentWidth == 0) {
                return videoDisplayRectHeight;
            }
            //由于视频的宽>=视频的高时，使用等比缩放模式，因此可以计算出视频显示区域的高度
            if (videoSize[0] >= videoViewParentWidth) {//视频的宽>=播放器布局的宽
                float ratioW = videoSize[0] * 1.0f / videoViewParentWidth;//>=1
                float ratioH = videoSize[1] * 1.0f / videoViewParentHeight;//<0或>=1
                //使用较大的比例缩放
                if (ratioW > ratioH) {
                    videoDisplayRectHeight = (int) (videoSize[1] * 1.0f / ratioW);
                    videoDisplayRectWidth = videoViewParentWidth;
                } else {
                    videoDisplayRectHeight = videoViewParentHeight;
                    videoDisplayRectWidth = (int) (videoSize[0] * 1.0f / ratioH);
                }
            } else {
                float ratioW = videoViewParentWidth * 1.0f / videoSize[0];//>=1
                float ratioH = videoViewParentHeight * 1.0f / videoSize[1];//<0或>=1
                //使用较小的比例缩放
                if (ratioW > ratioH) {
                    videoDisplayRectHeight = videoViewParentHeight;
                    videoDisplayRectWidth = (int) (videoSize[0] * 1.0f * ratioH);
                } else {
                    videoDisplayRectHeight = (int) (videoSize[1] * 1.0f * ratioW);
                    videoDisplayRectWidth = videoViewParentWidth;
                }
            }
        } else {//视频的高>视频的宽
            //由于视频的高>宽时，使用等比铺满模式，因此视频显示区域的高度=播放器布局的高度
            videoDisplayRectHeight = ((ViewGroup) viewParent).getHeight();
        }
        return videoDisplayRectHeight;
    }
}
