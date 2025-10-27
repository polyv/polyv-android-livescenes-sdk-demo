package com.easefun.polyv.livecommon.module.modules.cast.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livescenes.log.PolyvLiveViewLog;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.foundationsdk.utils.PLVUtils;

import net.polyv.android.common.libs.app.PLVApplicationContext;
import net.polyv.android.common.libs.lang.Duration;
import net.polyv.android.media.cast.model.vo.PLVMediaCastDevice;
import net.polyv.android.media.cast.model.vo.PLVMediaCastPlayState;
import net.polyv.android.media.cast.model.vo.PLVMediaCastResource;
import net.polyv.android.media.cast.rx.PLVMediaCastControllerAdapterRxJava;
import net.polyv.android.media.cast.rx.PLVMediaCastManagerAdapterRxJava;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.functions.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * 投屏封装管理类
 */
//投屏统计方案：http://wiki.igeeker.org/pages/viewpage.action?pageId=156303739
public class PLVCastBusinessManager {
    // <editor-fold defaultstate="collapsed" desc="投屏状态码">
    //搜索状态
    public static final int STATE_SEARCH_SUCCESS = 1;
    public static final int STATE_SEARCH_ERROR = 2;
    public static final int STATE_SEARCH_NO_RESULT = 3;
    public static final int STATE_SEARCH_STOP = 4;
    //连接状态
    public static final int STATE_CONNECT_SUCCESS = 10;
    /*当接收端设备关机或者网络断开发送端不会立即产生回调，因为要做多次检测回调时间大概在10到3秒之内*/
    public static final int STATE_DISCONNECT = 11;//连接断开
    public static final int STATE_CONNECT_FAILURE = 12;//连接失败
    public static final int STATE_CONNECTING = 13;//正在连接
    //播放状态
    public static final int STATE_PLAY = 20;
    public static final int STATE_PAUSE = 21;
    public static final int STATE_COMPLETION = 22;
    public static final int STATE_STOP = 23;
    public static final int STATE_PLAY_ERROR = 24;
    public static final int STATE_LOADING = 25;
//    public static final int STATE_SEEK = 26;
//    public static final int STATE_POSITION_UPDATE = 27;

    // </editor-fold>

    private String TAG = this.getClass().getSimpleName();
    private volatile static PLVCastBusinessManager instance;

    private Context context;
    private UIHandler mUIHandler;
    private PLVMediaCastDevice mSelectInfo;
    private List<PLVMediaCastDevice> mBrowseInfos;
    @Nullable
    private PLVMediaCastControllerAdapterRxJava mController = null;

    private int mBrowseTimeout = 30 * 1000;//扫描设备，超时时间，单位为秒

    private boolean isExitScreencast;//退出投屏

    private IPLVCastStatusListener mCastStatusListener;
    private int mCurrentState = -1;

    /**
     * 投屏初始化结果，若为false则认为初始化失败，投屏功能将会异常，如搜索无结果等。
     */
    private boolean mCastAuthorize = false;
    volatile boolean isActiveStop = false;

    //正在投屏的码率
    private int mCastBitratePos = -1;
    //当前正在投屏的频道号
    private String mCastChannelId = "";
    //当前频道
    private String mCurChannelId = "";
    private int remoteVolume = 0;


    //统计数据
    private String mUserID;
    private String mViewerID;
    private String mNickName;
    private String mChannelSessionId;
    private String viewLogParam4, viewLogParam5;


    private OnCastInitListener castInitListener;

    // <editor-fold defaultstate="collapsed" desc="初始化与销毁">

    private PLVCastBusinessManager() {
        mUIHandler = new UIHandler(Looper.getMainLooper());
    }

    public static PLVCastBusinessManager getInstance() {
        if (instance == null) {
            synchronized (PLVCastBusinessManager.class) {
                if (instance == null) {
                    instance = new PLVCastBusinessManager();
                }
            }
        }
        return instance;
    }

    /**
     * 投屏SDK初始化；
     * 若授权成功{@link #mCastAuthorize} = true,则不再重复初始化
     *
     * @param context ApplicationContext
     */
    public void init(Context context) {
        if (mCastAuthorize) {
            return;
        }
        this.context = context;
        if (mCastStatusListener != null) {
            //对外的投屏结果回调
            mCastStatusListener.castAuthorize(true);
        }
        if (castInitListener != null) {
            castInitListener.onCastInit(true);
        }
        initListener();
        mCastAuthorize = true;
    }

    private void initListener() {
        PLVMediaCastManagerAdapterRxJava.getListenerRegistry().getPlayState()
                .observe(new Function1<PLVMediaCastPlayState, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaCastPlayState state) {
                        if (state == PLVMediaCastPlayState.PLAYING) {
                            mCurrentState = STATE_PLAY;
                            if (null != mUIHandler) {
                                mUIHandler.sendMessage(buildStateMessage(STATE_PLAY, context.getString(R.string.plv_cast_state_start)));
                                callCastingStatusListener(STATE_PLAY);
                            }
                        } else if (state == PLVMediaCastPlayState.PAUSED) {
                            mCurrentState = STATE_PAUSE;
                            if (null != mUIHandler) {
                                mUIHandler.sendMessage(buildStateMessage(STATE_PAUSE, context.getString(R.string.plv_cast_state_pause)));
                                callCastingStatusListener(STATE_PAUSE);
                            }
                        } else if (state == PLVMediaCastPlayState.STOPPED) {
                            mCurrentState = STATE_STOP;
                            if (null != mUIHandler) {
                                mUIHandler.sendMessage(buildStateMessage(STATE_STOP, context.getString(R.string.plv_cast_state_stop)));
                                callCastingStatusListener(STATE_STOP);
                                if (!isActiveStop) {
                                    mUIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //接收端主动退出投屏时也会回调
                                            clearInfos();
                                        }
                                    });
                                }
                                isActiveStop = false;
                            }
                        }
                        return Unit.INSTANCE;
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="搜索设备">

    /**
     * 开始搜索
     */
    public void startBrowse() {
        PLVCommonLog.d(TAG, "startBrowse");
        mUIHandler.removeCallbacks(mSearchTimeoutRunnable);
        mUIHandler.postDelayed(mSearchTimeoutRunnable, mBrowseTimeout);
        doBrowse();
    }

    public void stopBrowse() {
        PLVCommonLog.d(TAG, "stopBrowse");
        mUIHandler.removeCallbacks(mSearchTimeoutRunnable);
        mUIHandler.sendMessage(buildStateMessage(STATE_SEARCH_STOP, context.getString(R.string.plv_cast_search_stop)));
    }

    private void doBrowse() {
        PLVMediaCastManagerAdapterRxJava.scanDevices(Duration.seconds(5))
                .subscribe(
                        new Consumer<List<PLVMediaCastDevice>>() {
                            @Override
                            public void accept(List<PLVMediaCastDevice> devices) throws Exception {
                                mBrowseInfos = devices;
                                if (mUIHandler != null) {
                                    mUIHandler.sendMessage(buildStateMessage(STATE_SEARCH_SUCCESS, mBrowseInfos));
                                }
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        }
                );
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连接设备">

    public void connect(PLVMediaCastDevice info) {
        PLVCommonLog.d(TAG, "connect ");
        setExitScreencast(false);
        setSelectInfo(info);
        if (mUIHandler != null) {
            mUIHandler.sendMessage(buildStateMessage(STATE_CONNECTING, context.getString(R.string.plv_cast_state_connecting)));
        }
        if (mCastStatusListener != null) {
            mCastStatusListener.castConnectStart();
        }
        if (mUIHandler != null) {
            mUIHandler.sendMessage(buildStateMessage(STATE_CONNECT_SUCCESS, info));
        }
        if (mCastStatusListener != null) {
            mCastStatusListener.castConnectSuccess();
        }
    }

    /**
     * @param info
     * @return 返回true表示断开连接成功false表示失败，断开连接不会产生回调
     */
    public boolean disConnect(PLVMediaCastDevice info) {
        String name = info != null ? info.getFriendlyName() : null;
        PLVCommonLog.d(TAG, "disConnect " + name);
        stopPlay();
        setExitScreencast(true);
        mUIHandler.removeCallbacks(mSearchTimeoutRunnable);
        setSelectInfo(null);
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="投屏播放">

    public void play(PLVMediaCastDevice renderDevice, String url, int mediaType, boolean isSendLog, boolean isLocalFile) {
        String pid = PLVUtils.getPid();
        PLVMediaCastResource mediaCastResource = new PLVMediaCastResource(url, "");
        PLVMediaCastManagerAdapterRxJava.startCast(renderDevice, mediaCastResource)
                .subscribe(new Consumer<PLVMediaCastControllerAdapterRxJava>() {
                    @Override
                    public void accept(PLVMediaCastControllerAdapterRxJava controller) throws Exception {
                        mController = controller;
                        setSelectInfo(renderDevice);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVToast.Builder.context(PLVApplicationContext.getApplicationContext())
                                .setText(R.string.plv_cast_toast_play_failed)
                                .show();
                    }
                });
        if (isSendLog) {
            sendViewLog(pid);
        }
    }

    public void play(PLVMediaCastDevice renderDevice, String url, int mediaType, boolean isSendLog) {
        play(renderDevice, url, mediaType, isSendLog, false);
    }

    public void stopPlay() {
        isActiveStop = true;
        PLVCommonLog.d(TAG, "stopPlay");
        setExitScreencast(true);
        if (mController != null) {
            PLVMediaCastManagerAdapterRxJava.stopCast(mController.getController()).subscribe(
                    new Consumer<Unit>() {
                        @Override
                        public void accept(Unit unit) throws Exception {

                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            final PLVCastError error = new PLVCastError(
                                    PLVCastError.CAST_ERROR_NO_RESPONSE_STOP,
                                    context.getString(R.string.plv_cast_toast_exit_failed)
                            );
                            if (null != mUIHandler) {
                                mUIHandler.sendMessage(buildStateMessage(STATE_PLAY_ERROR, error.getErrorDesc()));
                                mUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCastStatusListener != null) {
                                            mCastStatusListener.castPlayError(error);
                                        }
                                    }
                                });
                            }
                        }
                    }
            );
        }
        mController = null;
    }

    public void pause() {
        if (mController != null) {
            mController.pause().subscribe(
                    new Consumer<Unit>() {
                        @Override
                        public void accept(Unit unit) throws Exception {

                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            final PLVCastError error = new PLVCastError(
                                    PLVCastError.CAST_ERROR_NO_RESPONSE_PAUSE,
                                    context.getString(R.string.plv_cast_toast_pause_failed)
                            );
                            if (null != mUIHandler) {
                                mUIHandler.sendMessage(buildStateMessage(STATE_PLAY_ERROR, error.getErrorDesc()));
                                mUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCastStatusListener != null) {
                                            mCastStatusListener.castPlayError(error);
                                        }
                                    }
                                });
                            }
                        }
                    }
            );
        }
    }

    public void resume() {
        if (mController != null) {
            mController.play().subscribe(
                    new Consumer<Unit>() {
                        @Override
                        public void accept(Unit unit) throws Exception {

                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            final PLVCastError error = new PLVCastError(
                                    PLVCastError.CAST_ERROR_NO_RESPONSE_RESUME,
                                    context.getString(R.string.plv_cast_toast_resume_failed)
                            );
                            if (null != mUIHandler) {
                                mUIHandler.sendMessage(buildStateMessage(STATE_PLAY_ERROR, error.getErrorDesc()));
                                mUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCastStatusListener != null) {
                                            mCastStatusListener.castPlayError(error);
                                        }
                                    }
                                });
                            }
                        }
                    }
            );
        }
    }

    // </editor-fold>

    private void callCastingStatusListener(final int state) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCastStatusListener != null) {
                    mCastStatusListener.castPlayStatus(state);
                }
            }
        });
    }

    private Message buildStateMessage(int state, Object object) {
        Message message = Message.obtain();
        message.what = UIHandler.MSG_STATE;
        message.arg1 = state;
        if (null != object) {
            message.obj = object;
        }
        return message;
    }


    private static class UIHandler extends Handler {
        private static final int MSG_STATE = 1;
        private List<IPLVCastUpdateListener> stateListeners = new ArrayList<>();

        private UIHandler(Looper looper) {
            super(looper);
        }

        private void addStateListener(IPLVCastUpdateListener stateListener) {
            if (stateListener != null) {
                stateListeners.add(stateListener);
            }
        }

        private void removeStateListener(IPLVCastUpdateListener stateListener) {
            if (stateListener != null) {
                stateListeners.remove(stateListener);
            }
        }


        @Override
        public void handleMessage(Message msg) {
            PLVCommonLog.d("PlvCastBusinessManager - Handler Message: ", msg.arg1 + "  " + msg.obj);
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_STATE:
                    if (stateListeners.size() > 0) {
                        for (int i = 0; i < stateListeners.size(); i++) {
                            stateListeners.get(i).onStateUpdate(msg.arg1, msg.obj);
                        }
                    }
                    break;
                default:
                    break;
            }

        }
    }

    // <editor-fold defaultstate="collapsed" desc="其他控制">

    /**
     * 增加音量
     */
    public void addVolume() {
        remoteVolume += 10;
        setVolume(remoteVolume);
    }

    /**
     * 减少音量
     */
    public void subVolume() {
        remoteVolume -= 10;
        setVolume(remoteVolume);
    }

    public void setVolume(int volume) {
        this.remoteVolume = PLVSugarUtil.clamp(volume, 0, 100);
        if (mController != null) {
            mController.setVolume(remoteVolume).subscribe(
                    new Consumer<Unit>() {
                        @Override
                        public void accept(Unit unit) throws Exception {

                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            final PLVCastError error = new PLVCastError(
                                    PLVCastError.CAST_ERROR_MIRROR_FORCE_STOP,
                                    context.getString(R.string.plv_cast_toast_set_volume_failed)
                            );
                            if (null != mUIHandler) {
                                mUIHandler.sendMessage(buildStateMessage(STATE_PLAY_ERROR, error.getErrorDesc()));
                                mUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCastStatusListener != null) {
                                            mCastStatusListener.castPlayError(error);
                                        }
                                    }
                                });
                            }
                        }
                    }
            );
        }
    }


    /**
     * 退出投屏时的数据清理
     */
    public void clearInfos() {
//        this.mCurChannelId = "";

        this.mCastChannelId = "";
        this.mSelectInfo = null;
        this.mCastBitratePos = -1;
    }

    public void enterChannel(String channel, String UserId, String sessionid, String viewerid, String nickName) {
        mCurChannelId = channel;
        mUserID = UserId;
        mViewerID = viewerid;
        mNickName = nickName;
        mChannelSessionId = sessionid;
        if (isSameChannelId() && isCasting()) {
            if (PLVCastBusinessManager.getInstance().getCastStatusListener() != null) {
                PLVCastBusinessManager.getInstance().getCastStatusListener().castEnterLiveRoom();
            }
        }
    }

    public void leaveChannel() {
        if (isSameChannelId() && isCasting()) {
            if (PLVCastBusinessManager.getInstance().getCastStatusListener() != null) {
                PLVCastBusinessManager.getInstance().getCastStatusListener().castLeaveLiveRoom();
            }
        }
    }

    public void sendViewLog(String pid) {
        PLVCommonLog.d(TAG, "sendcastlog: pid-" + pid);
        PolyvLiveViewLog.getInstance().statCast(pid, mUserID, mCastChannelId, 0, 0, 0, mChannelSessionId,
                mViewerID, mNickName, "live", viewLogParam4, viewLogParam5);
    }

    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="get set 方法">


    public IPLVCastStatusListener getCastStatusListener() {
        return mCastStatusListener;
    }

    public void setCastStatusListener(IPLVCastStatusListener castStatusListener) {
        this.mCastStatusListener = castStatusListener;
        if (mCastStatusListener != null) {
            mCastStatusListener.castAuthorize(mCastAuthorize);
        }
    }

    /**
     * @param listener
     * @see #addPlvScreencastStateListener(IPLVCastUpdateListener listener)
     */
    @Deprecated
    public void setPlvScreencastStateListener(IPLVCastUpdateListener listener) {

    }

    /**
     * 添加投屏状态监听
     */
    public void addPlvScreencastStateListener(IPLVCastUpdateListener listener) {
        mUIHandler.addStateListener(listener);
    }

    public void removePlvScreencastStateListener(IPLVCastUpdateListener listener) {
        mUIHandler.removeStateListener(listener);
    }

    public List<PLVMediaCastDevice> getBrowseInfos() {
        return mBrowseInfos;
    }

    public PLVMediaCastDevice getSelectInfo() {
        return mSelectInfo;
    }

    public void setSelectInfo(PLVMediaCastDevice mSelectInfo) {
        this.mSelectInfo = mSelectInfo;
        if (mSelectInfo != null) {
            PLVCommonLog.d(TAG, "setSelectInfo: " + mSelectInfo.getFriendlyName());
        } else {
            PLVCommonLog.d(TAG, "setSelectInfo: null");
        }
    }

    public boolean isConnected() {
        return mSelectInfo != null;
    }

    public String getChannelId() {
        return mCurChannelId;
    }

    public String getCastChannleId() {
        return mCastChannelId;
    }

    public boolean isSameChannelId() {
        return mCurChannelId.equals(mCastChannelId);
    }

    public String getViewLogParam4() {
        return viewLogParam4;
    }

    public void setViewLogParam4(String viewLogParam4) {
        this.viewLogParam4 = viewLogParam4;
    }

    public String getViewLogParam5() {
        return viewLogParam5;
    }

    public void setViewLogParam5(String viewLogParam5) {
        this.viewLogParam5 = viewLogParam5;
    }

    public void setExitScreencast(boolean exitScreencast) {
        isExitScreencast = exitScreencast;
    }

    public boolean isExitScreencast() {
        return isExitScreencast;
    }

    /**
     * 是否正在投屏中
     */
    public boolean isCasting() {
        return mCurrentState == STATE_PLAY || mCurrentState == STATE_LOADING;
    }

    public void resetState() {
        mCurrentState = -1;
    }

    /**
     * 投屏SDK初始化结果
     *
     * @return 初始化成功返回true
     */
    public boolean isCastAuthorize() {
        return mCastAuthorize;
    }

    public int getCastBitratePos() {
        return mCastBitratePos;
    }

    public void setCastBitratePos(int mCastBitratePos) {
        this.mCastBitratePos = mCastBitratePos;
    }

    // </editor-fold>

    /**
     * 搜索设备时间到
     */
    private Runnable mSearchTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (getBrowseInfos() == null || getBrowseInfos().isEmpty()) {
                mUIHandler.sendMessage(buildStateMessage(STATE_SEARCH_NO_RESULT, context.getString(R.string.plv_cast_state_available_device_not_found)));
            } else {
                mUIHandler.sendMessage(buildStateMessage(STATE_SEARCH_STOP, context.getString(R.string.plv_cast_search_stop)));
            }
        }
    };

    /**
     * 内部接口，请勿复写
     */
    public interface OnCastInitListener {
        void onCastInit(boolean initResult);
    }

    public void setCastInitListener(OnCastInitListener listener) {
        if (listener != null) {
            this.castInitListener = listener;
        /*
        投屏初始化结果回调，设置完成后会立即回调当前初始化结果。
        如果因为网络原因初始化结果加载较慢，会第二次回调
         */
            castInitListener.onCastInit(isCastAuthorize());
        }
    }

}
