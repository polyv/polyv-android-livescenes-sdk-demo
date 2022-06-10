package com.easefun.polyv.livedemo;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.scenes.PLVLCCloudClassActivity;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.playback.di.PLVPlaybackCacheModule;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config.PLVPlaybackCacheConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheListViewModel;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.widget.PLVSoftView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.liveecommerce.scenes.PLVECLiveEcommerceActivity;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.foundationsdk.utils.PLVUtils;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVLiveLoginResult;
import com.plv.livescenes.feature.login.PLVPlaybackLoginResult;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.io.File;

/**
 * date: 2020-04-29
 * author: hwj
 * 云课堂场景 & 直播带货场景 登录界面
 */
public class PLVLoginWatcherActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="实例变量">
    private static final String TAG = "PLVLoginActivity";
    //manager
    private IPLVSceneLoginManager loginManager;

    //View
    private ProgressDialog loginProgressDialog;
    private ImageView ivLogo;
    private TextView tvLogoText;
    private RelativeLayout rlLiveGroupLayout;
    private RelativeLayout rlPlaybackGroupLayout;
    private EditText etLiveUserId;
    private EditText etLiveChannelId;
    private EditText etLiveAppId;
    private EditText etLiveAppSecert;
    private LinearLayout llLiveLayout;
    private EditText etPlaybackChannelId;
    private EditText etPlaybackUserId;
    private EditText etPlaybackAppId;
    private EditText etPlaybackAppSecret;
    private EditText etPlaybackVideoId;
    private LinearLayout llPlaybackLayout;
    private TextView tvLogin;
    private RadioGroup rgScene;
    private SwitchCompat swtichPlaybackVodlistSw;
    private TextView tvCopyright;
    private PLVSoftView softListenerLayout;

    //status
    //当前是否显示的是直播，默认显示直播tab
    private boolean isShowLive = true;
    //当前选择的场景
    private PLVLiveScene curScene = PLVLiveScene.CLOUDCLASS;

    //listener
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isShowLive) {
                boolean hasEmpty = isEtEmpty(etLiveAppId) || isEtEmpty(etLiveAppSecert)
                        || isEtEmpty(etLiveChannelId) || isEtEmpty(etLiveUserId);
                tvLogin.setEnabled(!hasEmpty);
            } else {
                boolean hasEmpty = isEtEmpty(etPlaybackAppSecret) || isEtEmpty(etPlaybackAppId)
                        || isEtEmpty(etPlaybackChannelId) || isEtEmpty(etPlaybackUserId);
                tvLogin.setEnabled(!hasEmpty);
            }
        }
    };

    private PLVPlaybackCacheListViewModel playbackCacheListViewModel;
    private Observer<Event<PLVPlaybackCacheVideoVO>> playbackCacheLaunchObserver;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependency();
        setContentView(R.layout.plv_login_watcher_activity);

        //初始化登录管理器
        loginManager = new PLVSceneLoginManager();
        //初始化View
        initView();
        //设置测试数据
        setTestData();
    }

    @Override
    protected void onDestroy() {
        if (playbackCacheListViewModel != null && playbackCacheLaunchObserver != null) {
            playbackCacheListViewModel
                    .getOnRequestLaunchDownloadedPlaybackLiveData()
                    .removeObserver(playbackCacheLaunchObserver);
        }

        super.onDestroy();
        loginManager.destroy();
        loginProgressDialog.dismiss();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 依赖注入">

    private void injectDependency() {
        PLVDependManager.getInstance()
                .switchStore(this)
                .addModule(PLVPlaybackCacheModule.instance);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        //赋值View
        findAllView();
        //设置View监听器
        setListenerForView();
        //初始化提示弹窗
        initDialog();
        //默认选择直播
        rlLiveGroupLayout.performClick();

        initPlaybackCacheConfig();
        initPlaybackCacheViewModel();
        observePlaybackCacheLaunch();
    }

    private void findAllView() {
        ivLogo = findViewById(R.id.plv_login_logo);
        tvLogoText = findViewById(R.id.plv_login_logo_text);
        rlLiveGroupLayout = findViewById(R.id.plv_login_live_group_layout);
        rlPlaybackGroupLayout = findViewById(R.id.plv_login_playback_group_layout);
        etLiveUserId = findViewById(R.id.plv_login_live_user_id);
        etLiveChannelId = findViewById(R.id.plv_login_live_channel_id);
        etLiveAppId = findViewById(R.id.plv_login_live_app_id);
        etLiveAppSecert = findViewById(R.id.plv_login_live_app_secret);
        llLiveLayout = findViewById(R.id.plv_login_live_layout);
        etPlaybackChannelId = findViewById(R.id.plv_login_playback_channel_id);
        etPlaybackUserId = findViewById(R.id.plv_login_playback_user_id);
        etPlaybackAppId = findViewById(R.id.plv_login_playback_app_id);
        etPlaybackAppSecret = findViewById(R.id.plv_login_playback_app_secret);
        etPlaybackVideoId = findViewById(R.id.plv_login_playback_video_id);
        llPlaybackLayout = findViewById(R.id.playback_layout);
        tvLogin = findViewById(R.id.plv_login_tv_login);
        swtichPlaybackVodlistSw = findViewById(R.id.plv_login_playback_vodlist_sw);
        softListenerLayout = findViewById(R.id.plv_login_soft_listener_layout);
        rgScene = findViewById(R.id.plv_login_rg_scene);
        tvCopyright = findViewById(R.id.plv_login_tv_copyright);
    }

    private void setListenerForView() {
        //监听键盘弹起
        softListenerLayout.setOnKeyboardStateChangedListener(new PLVSoftView.IOnKeyboardStateChangedListener() {
            @Override
            public void onKeyboardStateChanged(int state) {
                boolean showTitleLogo = state != PLVSoftView.KEYBOARD_STATE_SHOW;
                tvLogoText.setVisibility(!showTitleLogo ? View.VISIBLE : View.GONE);
                ivLogo.setVisibility(showTitleLogo ? View.VISIBLE : View.GONE);
                tvCopyright.setVisibility(!showTitleLogo ? View.GONE : View.VISIBLE);
            }
        });

        //监听直播输入框
        etLiveChannelId.addTextChangedListener(textWatcher);
        etLiveUserId.addTextChangedListener(textWatcher);
        etLiveAppSecert.addTextChangedListener(textWatcher);
        etLiveAppId.addTextChangedListener(textWatcher);
        //监听回放输入框
        etPlaybackVideoId.addTextChangedListener(textWatcher);
        etPlaybackChannelId.addTextChangedListener(textWatcher);
        etPlaybackUserId.addTextChangedListener(textWatcher);
        etPlaybackAppSecret.addTextChangedListener(textWatcher);
        etPlaybackAppId.addTextChangedListener(textWatcher);

        //监听tab 选择
        rlLiveGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowLive = true;
                rlLiveGroupLayout.setSelected(true);
                rlPlaybackGroupLayout.setSelected(false);

                llLiveLayout.setVisibility(View.VISIBLE);
                llPlaybackLayout.setVisibility(View.GONE);
                swtichPlaybackVodlistSw.setVisibility(View.GONE);
                textWatcher.afterTextChanged(etLiveChannelId.getText());
            }
        });
        rlPlaybackGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowLive = false;
                rlLiveGroupLayout.setSelected(false);
                rlPlaybackGroupLayout.setSelected(true);

                llLiveLayout.setVisibility(View.GONE);
                llPlaybackLayout.setVisibility(View.VISIBLE);
                swtichPlaybackVodlistSw.setVisibility(View.VISIBLE);
                textWatcher.afterTextChanged(etPlaybackChannelId.getText());
            }
        });

        rgScene.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    //check 云课堂场景
                    case R.id.plv_login_rb_cloudclass_scene:
                        curScene = PLVLiveScene.CLOUDCLASS;
                        break;
                    //check 直播带货场景
                    case R.id.plv_login_rb_ecommerce_scene:
                        curScene = PLVLiveScene.ECOMMERCE;
                        break;
                    default:
                        break;
                }
            }
        });

        //监听登录按钮
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgressDialog.show();
                if (isShowLive) {
                    loginLive();
                } else {
                    loginPlayback();
                }
            }
        });

    }

    private void initDialog() {
        loginProgressDialog = new ProgressDialog(this);
        loginProgressDialog.setMessage("正在登录中，请稍等...");
        loginProgressDialog.setCanceledOnTouchOutside(false);
        loginProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loginManager.destroy();
            }
        });
    }

    private void initPlaybackCacheConfig() {
        PLVDependManager.getInstance().get(PLVPlaybackCacheConfig.class)
                .setApplicationContext(getApplicationContext())
                .setDatabaseNameByViewerId(getViewerId())
                .setDownloadRootDirectory(new File(PLVPlaybackCacheConfig.defaultPlaybackCacheDownloadDirectory(this)));
    }

    private void initPlaybackCacheViewModel() {
        playbackCacheListViewModel = PLVDependManager.getInstance().get(PLVPlaybackCacheListViewModel.class);
    }

    private void observePlaybackCacheLaunch() {
        if (playbackCacheLaunchObserver != null) {
            playbackCacheListViewModel.getOnRequestLaunchDownloadedPlaybackLiveData().removeObserver(playbackCacheLaunchObserver);
        }
        playbackCacheListViewModel
                .getOnRequestLaunchDownloadedPlaybackLiveData()
                .observeForever(playbackCacheLaunchObserver = new Observer<Event<PLVPlaybackCacheVideoVO>>() {
                    @Override
                    public void onChanged(@Nullable Event<PLVPlaybackCacheVideoVO> event) {
                        final PLVPlaybackCacheVideoVO vo = nullable(new PLVSugarUtil.Supplier<PLVPlaybackCacheVideoVO>() {
                            @Override
                            public PLVPlaybackCacheVideoVO get() {
                                return event.get();
                            }
                        });
                        if (vo == null) {
                            return;
                        }
                        final Intent clearTop = new Intent(PLVLoginWatcherActivity.this, PLVLoginWatcherActivity.class);
                        clearTop.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PLVLoginWatcherActivity.this.startActivity(clearTop);
                        loginPlayback(vo.getViewerInfoVO().getChannelId(), vo.getViewerInfoVO().getVid(), vo.getVideoPoolId());
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置测试数据">
    private void setTestData() {
        /**
         * 现在的登录参数可以在local.properties中进行相应设置，在local.properties中设置下面的
         * 代码即可配置相关的登录信息，不输入的话默认为“”
         * LiveAppId=xxx
         * LiveAppSecert=xxx
         * LiveUserId=xxx
         * LiveChannelId=xxx
         *
         * 回放参数
         * PlaybackAppId=xxx
         * PlaybackAppSecert=xxx
         * PlaybackUserId=xxx
         * PlaybackChannelId=xxx
         * PlaybackVideoId=xxx
         */
        //直播登录参数
        etLiveAppId.setText(PLVLoadParams.LiveAppId);
        etLiveAppSecert.setText(PLVLoadParams.LiveAppSecert);
        etLiveUserId.setText(PLVLoadParams.LiveUserId);
        etLiveChannelId.setText(PLVLoadParams.LiveChannelId);

        //登录回放参数
        etPlaybackAppId.setText(PLVLoadParams.PlaybackAppId);
        etPlaybackAppSecret.setText(PLVLoadParams.PlaybackAppSecert);
        etPlaybackUserId.setText(PLVLoadParams.PlaybackUserId);
        etPlaybackChannelId.setText(PLVLoadParams.PlaybackChannelId);
        etPlaybackVideoId.setText(PLVLoadParams.PlaybackVideoId);

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录直播">
    private void loginLive() {
        PLVFloatingPlayerManager.getInstance().clear();

        final String appId = etLiveAppId.getText().toString();
        final String appSecret = etLiveAppSecert.getText().toString();
        final String userId = etLiveUserId.getText().toString();
        final String channelId = etLiveChannelId.getText().toString();
        loginManager.loginLiveNew(appId, appSecret, userId, channelId, new IPLVSceneLoginManager.OnLoginListener<PLVLiveLoginResult>() {
            @Override
            public void onLoginSuccess(PLVLiveLoginResult plvLiveLoginResult) {
                loginProgressDialog.dismiss();
                PLVLiveChannelConfigFiller.setupAccount(userId, appId, appSecret);
                PLVLiveChannelType channelType = plvLiveLoginResult.getChannelTypeNew();
                switch (curScene) {
                    //进入云课堂场景
                    case CLOUDCLASS:
                        if (PLVLiveScene.isCloudClassSceneSupportType(channelType)) {
                            PLVLaunchResult launchResult = PLVLCCloudClassActivity.launchLive(PLVLoginWatcherActivity.this, channelId, channelType, getViewerId(), getViewerName(), getViewerAvatar());
                            if (!launchResult.isSuccess()) {
                                ToastUtils.showShort(launchResult.getErrorMessage());
                            }
                        } else {
                            ToastUtils.showShort(R.string.plv_scene_login_toast_cloudclass_no_support_type);
                        }
                        break;
                    //进入直播带货场景
                    case ECOMMERCE:
                        if (PLVLiveScene.isLiveEcommerceSceneSupportType(channelType)) {
                            PLVLaunchResult launchResult = PLVECLiveEcommerceActivity.launchLive(PLVLoginWatcherActivity.this, channelId, getViewerId(), getViewerName(), getViewerAvatar());
                            if (!launchResult.isSuccess()) {
                                ToastUtils.showShort(launchResult.getErrorMessage());
                            }
                        } else {
                            ToastUtils.showShort(R.string.plv_scene_login_toast_liveecommerce_no_support_type);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onLoginFailed(String msg, Throwable throwable) {
                loginProgressDialog.dismiss();
                ToastUtils.showShort(msg);
                PLVCommonLog.e(TAG, "loginLive onLoginFailed:" + throwable.getMessage());
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录回放">
    private void loginPlayback() {
        final String channelId = etPlaybackChannelId.getText().toString();
        final String vid = etPlaybackVideoId.getText().toString();
        loginPlayback(channelId, vid, null);
    }

    private void loginPlayback(final String channelId, final String vid, final String tempStoreFileId) {
        final String appId = etPlaybackAppId.getText().toString();
        final String appSecret = etPlaybackAppSecret.getText().toString();
        final String userId = etPlaybackUserId.getText().toString();

        loginManager.loginPlaybackNew(appId, appSecret, userId, channelId, vid, new IPLVSceneLoginManager.OnLoginListener<PLVPlaybackLoginResult>() {
            @Override
            public void onLoginSuccess(PLVPlaybackLoginResult plvPlaybackLoginResult) {
                loginProgressDialog.dismiss();
                PLVLiveChannelConfigFiller.setupAccount(userId, appId, appSecret);
                PLVLiveChannelType channelType = plvPlaybackLoginResult.getChannelTypeNew();
                if(swtichPlaybackVodlistSw.isChecked() && channelType == PLVLiveChannelType.PPT){
                    ToastUtils.showShort(R.string.plv_scene_login_toast_cloudclass_loing_no_support_vod_playback);
                    return;
                }
                switch (curScene) {
                    //进入云课堂场景
                    case CLOUDCLASS:
                        if (PLVLiveScene.isCloudClassSceneSupportType(channelType)) {
                            PLVLaunchResult launchResult = PLVLCCloudClassActivity.launchPlayback(
                                    PLVLoginWatcherActivity.this,
                                    channelId,
                                    channelType,
                                    vid,
                                    tempStoreFileId,
                                    getViewerId(),
                                    getViewerName(),
                                    getViewerAvatar(),
                                    swtichPlaybackVodlistSw.isChecked() ? PLVPlaybackListType.VOD : PLVPlaybackListType.PLAYBACK
                            );
                            if (!launchResult.isSuccess()) {
                                ToastUtils.showShort(launchResult.getErrorMessage());
                            }
                        } else {
                            ToastUtils.showShort(R.string.plv_scene_login_toast_cloudclass_no_support_type);
                        }
                        break;
                    //进入直播带货场景
                    case ECOMMERCE:
                        if (PLVLiveScene.isLiveEcommerceSceneSupportType(channelType)) {
                            PLVLaunchResult launchResult = PLVECLiveEcommerceActivity.launchPlayback(PLVLoginWatcherActivity.this, channelId,
                                    vid, getViewerId(), getViewerName(),getViewerAvatar(),
                                    swtichPlaybackVodlistSw.isChecked() ? PLVPlaybackListType.VOD : PLVPlaybackListType.PLAYBACK);
                            if (!launchResult.isSuccess()) {
                                ToastUtils.showShort(launchResult.getErrorMessage());
                            }
                        } else {
                            ToastUtils.showShort(R.string.plv_scene_login_toast_liveecommerce_no_support_type);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onLoginFailed(String msg, Throwable throwable) {
                loginProgressDialog.dismiss();
                ToastUtils.showShort(msg);
                PLVCommonLog.e(TAG, "loginPlayback onLoginFailed:" + throwable.getMessage());
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取登录参数">

    private String getViewerId() {
        /**
         *  todo 请务必在这里替换为你的学员(用户)ID，设置学员(用户)ID的意义详细可以查看：https://github.com/polyv/polyv-android-cloudClass-sdk-demo/wiki/6-%E8%AE%BE%E7%BD%AE%E5%AD%A6%E5%91%98%E5%94%AF%E4%B8%80%E6%A0%87%E8%AF%86%E7%9A%84%E6%84%8F%E4%B9%89
         */
        return PLVUtils.getAndroidId(this) + "";
    }

    private String getViewerName() {
        /**
         * todo 请务必在这里替换为你的学员(用户)昵称
         */
        return "观众" + getViewerId();
    }

    private String getViewerAvatar(){
        //todo 在这里可替换为你的学员(用户)头像地址
        return "";
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private boolean isEtEmpty(EditText et) {
        return TextUtils.isEmpty(et.getText().toString());
    }
    // </editor-fold>

}
