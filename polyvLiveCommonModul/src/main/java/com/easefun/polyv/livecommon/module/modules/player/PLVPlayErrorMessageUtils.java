package com.easefun.polyv.livecommon.module.modules.player;

import android.view.View;

import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.elog.logcode.play.PLVErrorCodePlayVideoInfo;
import com.plv.foundationsdk.utils.PLVAppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放失败的提示信息工具
 */
public class PLVPlayErrorMessageUtils {
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

    private static String getTipsLiveLoadSlow() {
        return PLVAppUtils.getString(R.string.plv_player_live_load_slow_hint);
    }

    private static String getTipsPlaybackLoadSlow() {
        return PLVAppUtils.getString(R.string.plv_player_playback_load_slow_hint);
    }

    private static String getTipsRestrictWatch() {
        return PLVAppUtils.getString(R.string.plv_player_restrict_watch_hint);
    }

    private static String getTipsSuggestExit() {
        return PLVAppUtils.getString(R.string.plv_player_suggest_exit_hint);
    }

    private static String getTipsSuggestRefresh() {
        return PLVAppUtils.getString(R.string.plv_player_suggest_refresh_hint);
    }

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
            errorTips = isLive ? getTipsLiveLoadSlow() : getTipsPlaybackLoadSlow();
            isShowChangeLinesView = isLive;
            isShowRefreshView = true;
        } else {
            int errorCode = playErrorReason.errorCode;
            String errorCodeInfo = PLVAppUtils.formatString(R.string.plv_player_error_code, errorCode + "");
            if (ERROR_CODE_SUGGEST_EXIT.contains(errorCode)) {
                errorTips = errorCode == PLVErrorCodePlayVideoInfo.ErrorCode.LIVE_RESTRICT_WATCH_ERROR
                        ? String.format(getTipsRestrictWatch(), playErrorReason.errorDescribe + errorCodeInfo)
                        : String.format(getTipsSuggestExit(), errorCodeInfo);
                isShowChangeLinesView = false;
                isShowRefreshView = false;
            } else {
                errorTips = String.format(getTipsSuggestRefresh(), errorCodeInfo);
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
