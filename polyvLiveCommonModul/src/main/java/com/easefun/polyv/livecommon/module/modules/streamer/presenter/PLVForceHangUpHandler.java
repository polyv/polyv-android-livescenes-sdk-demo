package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.streamer.linkmic.PLVLinkMicEventSender;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 强制下麦处理器。当触发下麦动作后，如果连麦用户在40s后还处于连麦状态，那么可以触发强制下麦的逻辑。
 */
public class PLVForceHangUpHandler {
    private PLVStreamerPresenter streamerPresenter;
    private final Map<String, PLVLinkMicItemDataBean> waitForceHangUpMap = new HashMap<>();
    private final Map<String, Disposable> disposableMap = new HashMap<>();

    public PLVForceHangUpHandler(PLVStreamerPresenter streamerPresenter) {
        this.streamerPresenter = streamerPresenter;
    }

    public void put(final String linkMicId, final PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (waitForceHangUpMap.containsKey(linkMicId) || linkMicItemDataBean == null) {
            return;
        }
        final Disposable disposable = Observable.just(1).delay(40, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        linkMicItemDataBean.setStatusMethodCallListener(null);
                        remove(linkMicId);
                        if (linkMicItemDataBean.isGuest()) {
                            showForceHangUpDialog(linkMicId, linkMicItemDataBean);
                        } else {
                            PLVLinkMicEventSender.getInstance().forceRemoveUserMic(linkMicId, linkMicItemDataBean.getLoginId(), null, null);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposableMap.put(linkMicId, disposable);
        waitForceHangUpMap.put(linkMicId, linkMicItemDataBean);
        linkMicItemDataBean.setStatusMethodCallListener(new Runnable() {
            @Override
            public void run() {
                if (linkMicItemDataBean.getStatus() != PLVLinkMicItemDataBean.LinkMicStatus.JOIN
                        && linkMicItemDataBean.getStatus() != PLVLinkMicItemDataBean.LinkMicStatus.RTC_JOIN
                        && linkMicItemDataBean.getStatus() != PLVLinkMicItemDataBean.LinkMicStatus.IDLE) {
                    linkMicItemDataBean.setStatusMethodCallListener(null);
                    remove(linkMicId);
                }
            }
        });
    }

    public void remove(String linkMicId) {
        if (!waitForceHangUpMap.containsKey(linkMicId)) {
            return;
        }
        PLVLinkMicItemDataBean linkMicItemDataBean = waitForceHangUpMap.remove(linkMicId);
        if (linkMicItemDataBean != null) {
            linkMicItemDataBean.setStatusMethodCallListener(null);
        }
        if (disposableMap.containsKey(linkMicId)) {
            Disposable disposable = disposableMap.remove(linkMicId);
            if (disposable != null) {
                disposable.dispose();
            }
        }
    }

    public void destroy() {
        for (Map.Entry<String, PLVLinkMicItemDataBean> entry : waitForceHangUpMap.entrySet()) {
            remove(entry.getKey());
        }
    }

    private void showForceHangUpDialog(final String linkMicId, final PLVLinkMicItemDataBean linkMicItemDataBean) {
        final Activity activity = ActivityUtils.getTopActivity();
        if (activity != null && !activity.isFinishing()) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.plv_linkmic_hang_up_tips)
                    .setMessage(PLVAppUtils.formatString(R.string.plv_linkmic_focus_hang_up_message, linkMicItemDataBean.getNick()))
                    .setPositiveButton(R.string.plv_linkmic_hang_up_forced, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            PLVLinkMicEventSender.getInstance().forceRemoveUserMic(linkMicId, linkMicItemDataBean.getLoginId(), null, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    showForceHangUpRetryDialog(activity, linkMicId, linkMicItemDataBean);
                                }
                            });
                        }
                    })
                    .setNegativeButton(R.string.plv_linkmic_hang_up_normal, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            streamerPresenter.normalCloseUserLinkMic(linkMicId, false);
                        }
                    })
                    .show();
        }
    }

    private void showForceHangUpRetryDialog(final Activity activity, final String linkMicId, final PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.plv_linkmic_hang_up_tips)
                .setMessage(PLVAppUtils.formatString(R.string.plv_linkmic_focus_hang_up_message_retry, linkMicItemDataBean.getNick()))
                .setPositiveButton(R.string.plv_linkmic_hang_up_forced, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PLVLinkMicEventSender.getInstance().forceRemoveUserMic(linkMicId, linkMicItemDataBean.getLoginId(), null, null);
                    }
                })
                .setNegativeButton(R.string.plv_common_dialog_cancel, null)
                .show();
    }
}
