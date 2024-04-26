package com.easefun.polyv.livedemo;

import android.Manifest;
import android.annotation.SuppressLint;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livestreamer.scenes.PLVLSLiveStreamerActivity;
import com.easefun.polyv.streameralone.scenes.PLVSAStreamerAloneActivity;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.livescenes.feature.login.model.PLVLoginVO;
import com.plv.thirdpart.blankj.utilcode.util.EncodeUtils;
import com.plv.thirdpart.blankj.utilcode.util.LogUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 手机开播登录页面
 */
public class PLVLoginStreamerActivity extends PLVBaseActivity implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVLoginStreamerActivity.class.getSimpleName();

    /**
     * 提供给外部跳转调用的scheme，应根据实际情况配置
     * 如果不需要支持从外部跳转进入app，需要删除AndroidManifest中的相关配置
     */
    private static final String SCHEME_LOGIN_FOR_WEB = "plvapp";
    private static final String HOST_LOGIN_FOR_WEB = "live.polyv.net";
    private static final String PATH_LOGIN_FOR_WEB = "/streamer";

    private EditText plvlsLoginInputChannelEt;
    private EditText plvlsLoginInputNickEt;
    private EditText plvlsLoginInputPwdEt;
    private ImageView plvlsLoginInputChannelDeleteIv;
    private ImageView plvlsLoginInputNickDeleteIv;
    private ImageView plvlsLoginInputPwdDeleteIv;
    private Button plvlsLoginEnterBtn;
    private CheckBox plvlsLoginRememberPasswordCb;
    private CheckBox plvlsLoginAgreeContractCb;
    private TextView plvlsLoginPrivatePolicyTv;
    private TextView plvlsLoginUsageContractTv;
    private ProgressBar plvlsLoginLoadingPb;

    private View lastTouchInputView;
    private View lastShowInputDeleteView;

    private PLVLSLoginLocalInfoManager localInfoManager;
    private IPLVSceneLoginManager loginManager;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_login_streamer_activity);

        setTransparentStatusBar();

        initView();

        if (isOpenFromScheme(getIntent())) {
            tryLoginForScheme(getIntent());
        } else {
            initViewForSavedData();
        }
    }

    private void setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            View decorView = window.getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            attributes.flags |= flagTranslucentStatus;
            window.setAttributes(attributes);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginManager != null) {
            loginManager.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面UI">
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        plvlsLoginInputChannelEt = findViewById(R.id.plvls_login_input_channel_et);
        plvlsLoginInputNickEt = findViewById(R.id.plvls_login_input_nick_et);
        plvlsLoginInputPwdEt = findViewById(R.id.plvls_login_input_pwd_et);
        plvlsLoginInputChannelDeleteIv = findViewById(R.id.plvls_login_input_channel_delete_iv);
        plvlsLoginInputNickDeleteIv = findViewById(R.id.plvls_login_input_nick_delete_iv);
        plvlsLoginInputPwdDeleteIv = findViewById(R.id.plvls_login_input_pwd_delete_iv);
        plvlsLoginEnterBtn = findViewById(R.id.plvls_login_enter_btn);
        plvlsLoginRememberPasswordCb = findViewById(R.id.plvls_login_remember_password_cb);
        plvlsLoginAgreeContractCb = findViewById(R.id.plvls_login_agree_contract_cb);
        plvlsLoginPrivatePolicyTv = findViewById(R.id.plvls_login_private_policy_tv);
        plvlsLoginUsageContractTv = findViewById(R.id.plvls_login_usage_contract_tv);
        plvlsLoginLoadingPb = findViewById(R.id.plvls_login_loading_pb);

        plvlsLoginInputChannelEt.setOnTouchListener(inputViewOnTouchListener);
        plvlsLoginInputChannelEt.addTextChangedListener(inputViewTextWatcher);
        plvlsLoginInputNickEt.setOnTouchListener(inputViewOnTouchListener);
        plvlsLoginInputNickEt.addTextChangedListener(inputViewTextWatcher);
        plvlsLoginInputPwdEt.setOnTouchListener(inputViewOnTouchListener);
        plvlsLoginInputPwdEt.addTextChangedListener(inputViewTextWatcher);

        plvlsLoginInputChannelDeleteIv.setOnClickListener(this);
        plvlsLoginInputNickDeleteIv.setOnClickListener(this);
        plvlsLoginInputPwdDeleteIv.setOnClickListener(this);

        plvlsLoginPrivatePolicyTv.setOnClickListener(this);
        plvlsLoginUsageContractTv.setOnClickListener(this);

        plvlsLoginEnterBtn.setOnClickListener(this);

        plvlsLoginRememberPasswordCb.setOnCheckedChangeListener(onRememberPwdCheckedChangeListener);
    }

    private void initViewForSavedData() {
        localInfoManager = new PLVLSLoginLocalInfoManager();
        localInfoManager.getLastLoginInfo();
        localInfoManager.observeGetLastLoginInfoResult()
                .observe(this, new Observer<PLVStatefulData<PLVLSLoginLocalInfoManager.PLVSLoginInfoVO>>() {
                    @Override
                    public void onChanged(@Nullable PLVStatefulData<PLVLSLoginLocalInfoManager.PLVSLoginInfoVO> plvsLoginInfoPLVSStatefulData) {
                        if (plvsLoginInfoPLVSStatefulData == null) {
                            return;
                        }
                        if (plvsLoginInfoPLVSStatefulData.isSuccess()) {
                            PLVLSLoginLocalInfoManager.PLVSLoginInfoVO loginInfoVO = plvsLoginInfoPLVSStatefulData.getData();
                            plvlsLoginInputChannelEt.setText(loginInfoVO.getChannelId());
                            plvlsLoginInputNickEt.setText(loginInfoVO.getNick());
                            plvlsLoginInputPwdEt.setText(loginInfoVO.getPassword());
                            plvlsLoginRememberPasswordCb.setChecked(localInfoManager.isRememberPassword());
                            plvlsLoginAgreeContractCb.setChecked(localInfoManager.isAgreeContract());
                        }
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录开播">
    private void loginStreamer() {
        if (isEtEmpty(plvlsLoginInputChannelEt) || isEtEmpty(plvlsLoginInputPwdEt)) {
            PLVToast.Builder.context(this)
                    .setText(R.string.plv_scene_login_toast_login_failed_channelid_or_pwd_empty)
                    .build()
                    .show();
            return;
        }
        if (!plvlsLoginAgreeContractCb.isChecked()) {
            PLVToast.Builder.context(this)
                    .setText("请勾选协议")
                    .build()
                    .show();
            return;
        }
        updateLoginViewStatus(true);
        final String channelId = plvlsLoginInputChannelEt.getText().toString().trim();
        final String nick = plvlsLoginInputNickEt.getText().toString().trim();
        final String password = plvlsLoginInputPwdEt.getText().toString();

        if (loginManager == null) {
            loginManager = new PLVSceneLoginManager();
        }
        loginManager.loginStreamerNew(channelId, password, new IPLVSceneLoginManager.OnStringCodeLoginListener<PLVLoginVO>() {
            @Override
            public void onLoginSuccess(PLVLoginVO loginVO) {
                updateLoginViewStatus(false);
                if (localInfoManager == null) {
                    localInfoManager = new PLVLSLoginLocalInfoManager();
                }
                if (plvlsLoginRememberPasswordCb.isChecked()) {
                    localInfoManager.saveLoginInfo(channelId, password, nick, true);
                } else {
                    localInfoManager.saveLoginInfo("", "", "", false);
                }

                //更新开播状态
                PLVLiveChannelConfigFiller.setLiveStreamingWhenLogin(loginVO.isLiveStatus());

                //不填写登录昵称时，使用登录接口返回的后台设置的昵称
                String loginNick = TextUtils.isEmpty(nick) ? loginVO.getTeacherNickname() : nick;

                PLVLiveChannelType liveChannelType = loginVO.getLiveChannelTypeNew();
                if (PLVLiveChannelType.PPT.equals(liveChannelType)) {
                    //进入手机开播三分屏场景
                    final boolean isOpenCamera = "N".equals(loginVO.getIsOnlyAudio());
                    requireStreamerPermissionThenRun(
                            isOpenCamera,
                            true,
                            new Runnable() {
                                @Override
                                public void run() {
                                    PLVLaunchResult launchResult = PLVLSLiveStreamerActivity.launchStreamer(
                                            PLVLoginStreamerActivity.this,
                                            loginVO.getChannelId(),
                                            loginVO.getInteractUid(),
                                            loginNick,
                                            loginVO.getTeacherAvatar(),
                                            loginVO.getTeacherActor(),
                                            loginVO.getRole(),
                                            loginVO.getColinMicType(),
                                            true,
                                            isOpenCamera,
                                            true
                                    );
                                    if (!launchResult.isSuccess()) {
                                        onLoginFailed(launchResult.getErrorMessage(), launchResult.getError());
                                    }
                                }
                            }
                    );
                } else if (PLVLiveChannelType.ALONE.equals(liveChannelType)) {
                    //进入手机开播纯视频场景
                    requireStreamerPermissionThenRun(
                            true,
                            true,
                            new Runnable() {
                                @Override
                                public void run() {
                                    PLVLaunchResult launchResult = PLVSAStreamerAloneActivity.launchStreamer(
                                            PLVLoginStreamerActivity.this,
                                            loginVO.getChannelId(),
                                            loginVO.getInteractUid(),
                                            loginNick,
                                            loginVO.getTeacherAvatar(),
                                            loginVO.getTeacherActor(),
                                            loginVO.getChannelName(),
                                            loginVO.getRole(),
                                            loginVO.getColinMicType()
                                    );
                                    if (!launchResult.isSuccess()) {
                                        onLoginFailed(launchResult.getErrorMessage(), launchResult.getError());
                                    }
                                }
                            }
                    );
                } else {
                    String errorMsg = getResources().getString(R.string.plv_scene_login_toast_streamer_no_support_type);
                    onLoginFailed(errorMsg, new Throwable(errorMsg));
                }
            }

            @Override
            public void onLoginFailed(String msg, Throwable throwable) {
                onLoginFailed(msg, "", throwable);
            }

            @Override
            public void onLoginFailed(String msg, String code, Throwable throwable) {
                throwable.printStackTrace();

                String throwableMsg = parseThrowableToMessage(throwable);

                PLVToast.Builder.context(PLVLoginStreamerActivity.this)
                        .setText(throwableMsg == null ? msg : throwableMsg)
                        .build()
                        .show();
                updateLoginViewStatus(false);

                if (IPLVSceneLoginManager.ERROR_PASSWORD_IS_WRONG.equals(code)) {
                    plvlsLoginInputPwdEt.setSelected(true);
                    plvlsLoginInputPwdEt.requestFocusFromTouch();
                    updateDeleteView(plvlsLoginInputPwdEt);
                } else if (IPLVSceneLoginManager.ERROR_CHANNEL_NOT_EXIST.equals(code)) {
                    plvlsLoginInputChannelEt.setSelected(true);
                    plvlsLoginInputChannelEt.requestFocusFromTouch();
                    updateDeleteView(plvlsLoginInputChannelEt);
                }
            }
        });
    }

    private void updateLoginViewStatus(boolean isLogging) {
        if (isLogging) {
            plvlsLoginLoadingPb.setVisibility(View.VISIBLE);
        } else {
            plvlsLoginLoadingPb.setVisibility(View.GONE);
        }
        updateLoginButtonStatus(isLogging);
    }

    private void updateLoginButtonStatus(boolean isLogging) {
        if (isEtEmpty(plvlsLoginInputChannelEt)) {
            plvlsLoginEnterBtn.setEnabled(false);
            // %50 #F0F1F5
            plvlsLoginEnterBtn.setTextColor(Color.argb(128, 240, 241, 245));
            return;
        }
        if (isEtEmpty(plvlsLoginInputPwdEt)) {
            plvlsLoginEnterBtn.setEnabled(false);
            plvlsLoginEnterBtn.setTextColor(Color.argb(128, 240, 241, 245));
            return;
        }
        if (isLogging) {
            plvlsLoginEnterBtn.setEnabled(false);
            plvlsLoginEnterBtn.setTextColor(Color.argb(128, 240, 241, 245));
            return;
        }
        plvlsLoginEnterBtn.setEnabled(true);
        // #F0F1F5
        plvlsLoginEnterBtn.setTextColor(Color.argb(255, 240, 241, 245));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private boolean isEtEmpty(EditText et) {
        return TextUtils.isEmpty(et.getText().toString());
    }

    private void updateDeleteView(View lastTouchInputView) {
        this.lastTouchInputView = lastTouchInputView;
        if (lastShowInputDeleteView != null) {
            lastShowInputDeleteView.setVisibility(View.GONE);
        }
        if (lastTouchInputView == plvlsLoginInputChannelEt) {
            lastShowInputDeleteView = plvlsLoginInputChannelDeleteIv;
        } else if (lastTouchInputView == plvlsLoginInputNickEt) {
            lastShowInputDeleteView = plvlsLoginInputNickDeleteIv;
        } else if (lastTouchInputView == plvlsLoginInputPwdEt) {
            lastShowInputDeleteView = plvlsLoginInputPwdDeleteIv;
        }
        if (lastShowInputDeleteView != null
                && lastTouchInputView instanceof EditText
                && ((EditText) lastTouchInputView).getText().length() > 0) {
            lastShowInputDeleteView.setVisibility(View.VISIBLE);
        }
    }

    @Nullable
    private static String parseThrowableToMessage(Throwable throwable) {
        if (throwable instanceof UnknownHostException) {
            return "当前网络不可用，请检查网络设置";
        }
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法 - 权限检查">

    private void requireStreamerPermissionThenRun(
            final boolean camera,
            final boolean audio,
            final Runnable runnable
    ) {
        final List<String> permissions = new ArrayList<>();
        if (camera) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (audio) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissions.isEmpty() || PLVFastPermission.hasPermission(this, permissions)) {
            runnable.run();
            return;
        }

        PLVFastPermission.getInstance().start(this, permissions, new PLVOnPermissionCallback() {
            @Override
            public void onAllGranted() {
                runnable.run();
            }

            @Override
            public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                final boolean hasCameraPermission = PLVFastPermission.hasPermission(PLVLoginStreamerActivity.this, Manifest.permission.CAMERA);
                final boolean hasAudioPermission = PLVFastPermission.hasPermission(PLVLoginStreamerActivity.this, Manifest.permission.RECORD_AUDIO);
                if (hasCameraPermission && hasAudioPermission) {
                    runnable.run();
                    return;
                }
                final String notGrantedPermissionDescription;
                if (hasCameraPermission) {
                    notGrantedPermissionDescription = "麦克风";
                } else if (hasAudioPermission) {
                    notGrantedPermissionDescription = "摄像头";
                } else {
                    notGrantedPermissionDescription = "摄像头和麦克风";
                }

                new PLVLoginStreamerConfirmDialog(PLVLoginStreamerActivity.this)
                        .setTitle(notGrantedPermissionDescription + "权限被禁止")
                        .setContent("参与直播需要" + notGrantedPermissionDescription + "权限，请前往系统设置开启权限")
                        .setLeftButtonText("取消")
                        .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                            }
                        })
                        .setRightButtonText("前往设置")
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                PLVFastPermission.getInstance().jump2Settings(PLVLoginStreamerActivity.this);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private static class PLVLoginStreamerConfirmDialog extends PLVConfirmDialog {

        public PLVLoginStreamerConfirmDialog(Context context) {
            super(context);
        }

        @Override
        protected int layoutId() {
            return R.layout.plv_login_streamer_confirm_window_layout;
        }

        @Override
        protected float dialogWidthInDp() {
            return 260;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法 - 由外部app或web网页跳转到app登录页的处理">

    private void tryLoginForScheme(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }
        if (!isOpenFromScheme(intent)) {
            return;
        }
        final String param = intent.getData().getQueryParameter("param");
        final SchemeLoginParam schemeLoginParam = parseSchemeParam(param);
        if (schemeLoginParam == null) {
            return;
        }
        setTextIfNotEmpty(plvlsLoginInputChannelEt, schemeLoginParam.channel);
        setTextIfNotEmpty(plvlsLoginInputNickEt, schemeLoginParam.nick);
        setTextIfNotEmpty(plvlsLoginInputPwdEt, schemeLoginParam.pwd);
        setCheckBox(plvlsLoginRememberPasswordCb, schemeLoginParam.rememberPwd);
        setCheckBox(plvlsLoginAgreeContractCb, schemeLoginParam.agreeContract);

        final boolean canAutoLogin = schemeLoginParam.channel != null
                && schemeLoginParam.pwd != null
                && plvlsLoginAgreeContractCb.isChecked()
                && schemeLoginParam.autoLogin != null
                && schemeLoginParam.autoLogin;
        if (canAutoLogin) {
            loginStreamer();
        }
    }

    private static boolean isOpenFromScheme(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return false;
        }
        final String scheme = intent.getData().getScheme();
        final String host = intent.getData().getHost();
        final String path = intent.getData().getPath();
        return SCHEME_LOGIN_FOR_WEB.equals(scheme) && HOST_LOGIN_FOR_WEB.equals(host) && PATH_LOGIN_FOR_WEB.equals(path);
    }

    private static void setTextIfNotEmpty(EditText et, String text) {
        if (!TextUtils.isEmpty(text)) {
            et.setText(text);
        }
    }

    private static void setCheckBox(CheckBox checkBox, Boolean value) {
        if (checkBox != null && value != null) {
            checkBox.setChecked(value);
        }
    }

    @Nullable
    private static SchemeLoginParam parseSchemeParam(String param) {
        if (param == null) {
            return null;
        }
        final String raw = EncodeUtils.base64UrlSafeDecodeToString(param);
        return PLVGsonUtil.fromJson(SchemeLoginParam.class, raw);
    }

    private static class SchemeLoginParam {
        private String channel;
        private String nick;
        private String pwd;
        private Boolean rememberPwd;
        private Boolean agreeContract;
        private Boolean autoLogin;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvls_login_enter_btn) {
            loginStreamer();
        } else if (id == R.id.plvls_login_private_policy_tv) {
            Intent intent = new Intent(this, PLVContractActivity.class);
            intent.putExtra(PLVContractActivity.KEY_IS_PRIVATE_POLICY, true);
            startActivity(intent);
        } else if (id == R.id.plvls_login_usage_contract_tv) {
            Intent intent = new Intent(this, PLVContractActivity.class);
            intent.putExtra(PLVContractActivity.KEY_IS_PRIVATE_POLICY, false);
            startActivity(intent);
        } else if (id == R.id.plvls_login_input_channel_delete_iv) {
            plvlsLoginInputChannelEt.setText("");
        } else if (id == R.id.plvls_login_input_nick_delete_iv) {
            plvlsLoginInputNickEt.setText("");
        } else if (id == R.id.plvls_login_input_pwd_delete_iv) {
            plvlsLoginInputPwdEt.setText("");
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听器">
    private TextWatcher inputViewTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateLoginButtonStatus(false);
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                if (lastShowInputDeleteView != null) {
                    lastShowInputDeleteView.setVisibility(View.VISIBLE);
                }
            } else {
                if (lastShowInputDeleteView != null) {
                    lastShowInputDeleteView.setVisibility(View.GONE);
                }
            }
        }
    };

    private View.OnTouchListener inputViewOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            plvlsLoginInputChannelEt.setSelected(false);
            plvlsLoginInputPwdEt.setSelected(false);

            if (lastTouchInputView == v) {
                return false;
            }
            updateDeleteView(v);
            return false;
        }
    };

    private CompoundButton.OnCheckedChangeListener onRememberPwdCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            buttonView.setSelected(isChecked);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 本地登录信息管理器">
    static class PLVLSLoginLocalInfoManager {
        private MutableLiveData<PLVStatefulData<PLVSLoginInfoVO>> getLoginInfoLiveData = new MutableLiveData<>();

        void getLastLoginInfo() {
            Observable.just(1).observeOn(Schedulers.io()).doOnNext(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    try {
                        PLVSLoginInfoVO loginInfo = LastLoginInfoIOManager.read();
                        getLoginInfoLiveData.postValue(PLVStatefulData.success(loginInfo));
                    } catch (ClassCastException e) {
                        LogUtils.e(e);
                        getLoginInfoLiveData.postValue(PLVStatefulData.<PLVSLoginInfoVO>error("获取本地登录信息失败"));
                    }
                }
            }).subscribe();
        }

        void saveLoginInfo(String channelId, String pwd, String nick, boolean rememberPassword) {
            LastLoginInfoIOManager.write(channelId, pwd, nick);
            LastLoginInfoIOManager.writeRememberPassword(rememberPassword);
            LastLoginInfoIOManager.writeAgreeContract(true);
        }

        boolean isRememberPassword() {
            return LastLoginInfoIOManager.readRememberPassword();
        }

        boolean isAgreeContract() {
            return LastLoginInfoIOManager.readAgreeContract();
        }

        LiveData<PLVStatefulData<PLVSLoginInfoVO>> observeGetLastLoginInfoResult() {
            return getLoginInfoLiveData;
        }

        private static class LastLoginInfoIOManager {
            private static final String KEY_CHANNEL_ID = "key_channel_id";
            private static final String KEY_PASSWORD = "key_password";
            private static final String KEY_NICK = "key_nick";
            private static final String KEY_REMEMBER_PASSWORD = "key_remember_password";
            private static final String KEY_AGREE_CONTRACT = "key_agree_contract";

            static PLVSLoginInfoVO read() throws ClassCastException {
                String channelId = SPUtils.getInstance().getString(KEY_CHANNEL_ID);
                String pwd = SPUtils.getInstance().getString(KEY_PASSWORD);
                String nick = SPUtils.getInstance().getString(KEY_NICK);
                return new PLVSLoginInfoVO(channelId, pwd, nick);
            }

            static void write(String channelId, String pwd, String nick) {
                SPUtils.getInstance().put(KEY_CHANNEL_ID, channelId);
                SPUtils.getInstance().put(KEY_PASSWORD, pwd);
                SPUtils.getInstance().put(KEY_NICK, nick);
            }

            static void writeRememberPassword(boolean isRemember) {
                SPUtils.getInstance().put(KEY_REMEMBER_PASSWORD, isRemember);
            }

            static boolean readRememberPassword() {
                return SPUtils.getInstance().getBoolean(KEY_REMEMBER_PASSWORD, false);
            }

            static void writeAgreeContract(boolean isAgree) {
                SPUtils.getInstance().put(KEY_AGREE_CONTRACT, isAgree);
            }

            static boolean readAgreeContract() {
                return SPUtils.getInstance().getBoolean(KEY_AGREE_CONTRACT, false);
            }
        }

        //用于记住频道号和密码和昵称等登录信息
        public static class PLVSLoginInfoVO {
            private String channelId;
            private String password;
            private String nick;

            PLVSLoginInfoVO(String channelId, String password, String nick) {
                this.channelId = channelId;
                this.password = password;
                this.nick = nick;
            }

            public PLVSLoginInfoVO() {
            }

            String getChannelId() {
                return channelId;
            }

            public void setChannelId(String channelId) {
                this.channelId = channelId;
            }

            String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getNick() {
                return nick;
            }

            public void setNick(String nick) {
                this.nick = nick;
            }
        }
    }
    // </editor-fold>
}
