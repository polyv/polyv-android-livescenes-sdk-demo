package com.easefun.polyv.livecommon.module.modules.player;

import android.view.View;

import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.plv.foundationsdk.log.elog.logcode.play.PLVErrorCodePlayVideoInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放失败的提示信息工具
 */
public class PLVPlayErrorMessageUtils {
    private static String TIPS_LIVE_LOAD_SLOW = "视频加载缓慢，请切换线路或退出重进";
    private static String TIPS_PLAYBACK_LOAD_SLOW = "视频加载缓慢，请刷新或退出重进";
    private static String TIPS_RESTRICT_WATCH = "存在观看限制，暂不支持进入%s";
    private static String TIPS_SUGGEST_EXIT = "视频加载失败，请退出重进%s";
    private static String TIPS_SUGGEST_REFRESH = "视频加载失败，请检查网络或退出重进%s";
    // 建议退出重进的错误码集合
    private static List<Integer> ERROR_CODE_SUGGEST_EXIT = new ArrayList<Integer>() {
        {
            add(PLVErrorCodePlayVideoInfo.ErrorCode.PLAYBACK_INFO_DATA_ERROR);
            add(PLVErrorCodePlayVideoInfo.ErrorCode.PLAYBACK_INFO_VID_ERROR);
            add(PLVErrorCodePlayVideoInfo.ErrorCode.LIVE_INFO_DATA_ERROR);
            add(PLVErrorCodePlayVideoInfo.ErrorCode.LIVE_INFO_CODE_ERROR);
            add(PLVErrorCodePlayVideoInfo.ErrorCode.LIVE_RESTRICT_WATCH_ERROR);
        }
    };

    public static void showOnPlayError(IPLVPlayErrorView playErrorView, PolyvPlayError playErrorReason, boolean isLive) {
        PlayErrorContent playErrorContent = buildPlayErrorContent(playErrorReason, isLive);
        playErrorView.setChangeLinesViewVisibility(playErrorContent.isShowChangeLinesView ? View.VISIBLE : View.GONE);
        playErrorView.setRefreshViewVisibility(playErrorContent.isShowRefreshView ? View.VISIBLE : View.GONE);
        playErrorView.setPlaceHolderText(playErrorContent.errorTips);
        playErrorView.setViewVisibility(View.VISIBLE);
    }

    public static void showOnLoadSlow(IPLVPlayErrorView playErrorView, boolean isLive) {
        showOnPlayError(playErrorView, null, isLive);
    }

    private static PlayErrorContent buildPlayErrorContent(PolyvPlayError playErrorReason, boolean isLive) {
        String errorTips;
        boolean isShowChangeLinesView;
        boolean isShowRefreshView;

        if (playErrorReason == null) {
            errorTips = isLive ? TIPS_LIVE_LOAD_SLOW : TIPS_PLAYBACK_LOAD_SLOW;
            isShowChangeLinesView = isLive;
            isShowRefreshView = true;
        } else {
            int errorCode = playErrorReason.errorCode;
            String errorCodeInfo = "(错误码:" + errorCode + ")";
            if (ERROR_CODE_SUGGEST_EXIT.contains(errorCode)) {
                errorTips = errorCode == PLVErrorCodePlayVideoInfo.ErrorCode.LIVE_RESTRICT_WATCH_ERROR
                        ? String.format(TIPS_RESTRICT_WATCH, playErrorReason.errorDescribe + errorCodeInfo)
                        : String.format(TIPS_SUGGEST_EXIT, errorCodeInfo);
                isShowChangeLinesView = false;
                isShowRefreshView = false;
            } else {
                errorTips = String.format(TIPS_SUGGEST_REFRESH, errorCodeInfo);
                isShowChangeLinesView = false;
                isShowRefreshView = true;
            }
        }
        return new PlayErrorContent(errorTips, isShowChangeLinesView, isShowRefreshView);
    }

    public static class PlayErrorContent {
        private String errorTips;
        private boolean isShowChangeLinesView;
        private boolean isShowRefreshView;

        public PlayErrorContent(String errorTips, boolean isShowChangeLinesView, boolean isShowRefreshView) {
            this.errorTips = errorTips;
            this.isShowChangeLinesView = isShowChangeLinesView;
            this.isShowRefreshView = isShowRefreshView;
        }
    }
}
