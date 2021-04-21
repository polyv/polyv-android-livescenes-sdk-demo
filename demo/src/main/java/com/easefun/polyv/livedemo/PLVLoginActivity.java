package com.easefun.polyv.livedemo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
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
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.widget.PLVSoftView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.liveecommerce.scenes.PLVECLiveEcommerceActivity;
import com.easefun.polyv.livescenes.config.PolyvLiveChannelType;
import com.easefun.polyv.livescenes.feature.login.IPLVSceneLoginManager;
import com.easefun.polyv.livescenes.feature.login.PLVSceneLoginManager;
import com.easefun.polyv.livescenes.feature.login.PolyvLiveLoginResult;
import com.easefun.polyv.livescenes.feature.login.PolyvPlaybackLoginResult;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackListType;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

/**
 * date: 2020-04-29
 * author: hwj
 * 登录界面
 */
public class PLVLoginActivity extends PLVBaseActivity {

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
                        || isEtEmpty(etPlaybackChannelId) || isEtEmpty(etPlaybackUserId)
                        || isEtEmpty(etPlaybackVideoId);
                tvLogin.setEnabled(!hasEmpty);
            }
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_login_activity);

        //初始化登录管理器
        loginManager = new PLVSceneLoginManager();
        //初始化View
        initView();
        //设置测试数据
        setTestData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginManager.destroy();
        loginProgressDialog.dismiss();
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
    }

    private void findAllView() {
        ivLogo = findViewById(R.id.plv_login_logo);
        tvLogoText = findViewById(R.id.plv_login_logo_text);
        rlLiveGroupLayout = findViewById(R.id.plv_login_live_group_layout);
        rlPlaybackGroupLayout = findViewById(R.id.plv_login_playback_group_layout);
        etLiveUserId = findViewById(R.id.plv_login_live_user_id);
        etLiveChannelId = findViewById(R.id.plv_login_live_channel_id);
        etLiveAppId = findViewById(R.id.plv_login_live_app_id);
        etLiveAppSecert = findViewById(R.id.plv_login_live_app_secert);
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置测试数据">
    private void setTestData() {
        etLiveAppId.setText("");
        etLiveAppSecert.setText("");
        etLiveUserId.setText("");
        etLiveChannelId.setText("");

        etPlaybackAppId.setText("");
        etPlaybackAppSecret.setText("");
        etPlaybackUserId.setText("");
        etPlaybackChannelId.setText("");
        etPlaybackVideoId.setText("");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录直播">
    private void loginLive() {
        String appId = etLiveAppId.getText().toString();
        String appSecret = etLiveAppSecert.getText().toString();
        String userId = etLiveUserId.getText().toString();
        String channelId = etLiveChannelId.getText().toString();
        loginManager.loginLive(appId, appSecret, userId, channelId, new IPLVSceneLoginManager.OnLoginListener<PolyvLiveLoginResult>() {
            @Override
            public void onLoginSuccess(PolyvLiveLoginResult polyvLiveLoginResult) {
                loginProgressDialog.dismiss();
                PLVLiveChannelConfigFiller.setupAccount(userId, appId, appSecret);
                PolyvLiveChannelType channelType = polyvLiveLoginResult.getChannelType();
                switch (curScene) {
                    //进入云课堂场景
                    case CLOUDCLASS:
                        if (PLVLiveScene.isCloudClassSceneSupportType(channelType)) {
                            PLVLaunchResult launchResult = PLVLCCloudClassActivity.launchLive(PLVLoginActivity.this, channelId, channelType, getViewerId(), getViewerName(), getViewerAvatar());
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
                            PLVLaunchResult launchResult = PLVECLiveEcommerceActivity.launchLive(PLVLoginActivity.this, channelId, getViewerId(), getViewerName(),getViewerAvatar());
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
                PLVCommonLog.e(TAG,"loginLive onLoginFailed:"+throwable.getMessage());
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录回放">
    private void loginPlayback() {
        String appId = etPlaybackAppId.getText().toString();
        String appSecret = etPlaybackAppSecret.getText().toString();
        String userId = etPlaybackUserId.getText().toString();
        String channelId = etPlaybackChannelId.getText().toString();
        String vid = etPlaybackVideoId.getText().toString();
        loginManager.loginPlayback(appId, appSecret, userId, channelId, vid, new IPLVSceneLoginManager.OnLoginListener<PolyvPlaybackLoginResult>() {
            @Override
            public void onLoginSuccess(PolyvPlaybackLoginResult polyvPlaybackLoginResult) {
                loginProgressDialog.dismiss();
                PLVLiveChannelConfigFiller.setupAccount(userId, appId, appSecret);
                PolyvLiveChannelType channelType = polyvPlaybackLoginResult.getChannelType();
                switch (curScene) {
                    //进入云课堂场景
                    case CLOUDCLASS:
                        if (PLVLiveScene.isCloudClassSceneSupportType(channelType)) {
                            PLVLaunchResult launchResult = PLVLCCloudClassActivity.launchPlayback(PLVLoginActivity.this, channelId, channelType,
                                    vid, getViewerId(), getViewerName(),getViewerAvatar(),
                                    swtichPlaybackVodlistSw.isChecked() ? PolyvPlaybackListType.VOD : PolyvPlaybackListType.PLAYBACK
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
                            PLVLaunchResult launchResult = PLVECLiveEcommerceActivity.launchPlayback(PLVLoginActivity.this, channelId,
                                    vid, getViewerId(), getViewerName(),getViewerAvatar(),
                                    swtichPlaybackVodlistSw.isChecked() ? PolyvPlaybackListType.VOD : PolyvPlaybackListType.PLAYBACK);
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
                PLVCommonLog.e(TAG,"loginPlayback onLoginFailed:"+throwable.getMessage());
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
