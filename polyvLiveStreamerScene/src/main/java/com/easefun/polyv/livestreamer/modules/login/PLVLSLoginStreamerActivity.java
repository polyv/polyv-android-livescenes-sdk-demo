package com.easefun.polyv.livestreamer.modules.login;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livescenes.feature.login.IPLVSceneLoginManager;
import com.easefun.polyv.livescenes.feature.login.PLVSceneLoginManager;
import com.easefun.polyv.livescenes.feature.login.model.PLVSLoginVO;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.scenes.PLVLSLiveStreamerActivity;
import com.plv.thirdpart.blankj.utilcode.util.LogUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 手机开播登录页面
 */
public class PLVLSLoginStreamerActivity extends PLVBaseActivity implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private EditText plvlsLoginInputChannelEt;
    private EditText plvlsLoginInputNickEt;
    private EditText plvlsLoginInputPwdEt;
    private ImageView plvlsLoginInputChannelDeleteIv;
    private ImageView plvlsLoginInputNickDeleteIv;
    private ImageView plvlsLoginInputPwdDeleteIv;
    private Button plvlsLoginEnterBtn;
    private CheckBox plvlsLoginAgreeContractCb;
    private TextView plvlsLoginPrivatePolicyTv;
    private TextView plvlsLoginUsageContractTv;
    private ImageView plvlsLoginMicIv;
    private ImageView plvlsLoginCameraIv;
    private ImageView plvlsLoginFrontCameraIv;
    private ProgressBar plvlsLoginLoadingPb;
    private Group plvlsLoginBottomGroup;

    private View lastTouchInputView;
    private View lastShowInputDeleteView;

    private PLVLSLoginLocalInfoManager localInfoManager;
    private IPLVSceneLoginManager loginManager;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvls_login_streamer_activity);
        initView();
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
        plvlsLoginAgreeContractCb = findViewById(R.id.plvls_login_agree_contract_cb);
        plvlsLoginPrivatePolicyTv = findViewById(R.id.plvls_login_private_policy_tv);
        plvlsLoginUsageContractTv = findViewById(R.id.plvls_login_usage_contract_tv);
        plvlsLoginMicIv = findViewById(R.id.plvls_login_mic_iv);
        plvlsLoginCameraIv = findViewById(R.id.plvls_login_camera_iv);
        plvlsLoginFrontCameraIv = findViewById(R.id.plvls_login_front_camera_iv);
        plvlsLoginLoadingPb = findViewById(R.id.plvls_login_loading_pb);
        plvlsLoginBottomGroup = findViewById(R.id.plvls_login_bottom_group);

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

        plvlsLoginMicIv.setOnClickListener(this);
        plvlsLoginCameraIv.setOnClickListener(this);
        plvlsLoginFrontCameraIv.setOnClickListener(this);

        plvlsLoginAgreeContractCb.setOnCheckedChangeListener(onContractCheckedChangeListener);

        initViewTreeObserver();
        initViewForSavedData();
    }

    private void initViewTreeObserver() {
        FrameLayout content = findViewById(android.R.id.content);
        final View childOfContent = content.getChildAt(0);
        childOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                Rect r = new Rect();
                childOfContent.getWindowVisibleDisplayFrame(r);
                int usableHeightNow = r.bottom;
                int usableHeightSansKeyboard = childOfContent.getRootView().getHeight();
                int heightDifference = Math.abs(usableHeightSansKeyboard - usableHeightNow);
                if (heightDifference > (usableHeightNow / 4)) {
                    plvlsLoginBottomGroup.setVisibility(View.GONE);
                } else {
                    plvlsLoginBottomGroup.setVisibility(View.VISIBLE);
                }
            }
        });
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
        updateLoginViewStatus(true);
        final String channelId = plvlsLoginInputChannelEt.getText().toString();
        final String nick = plvlsLoginInputNickEt.getText().toString();
        final String password = plvlsLoginInputPwdEt.getText().toString();

        if (loginManager == null) {
            loginManager = new PLVSceneLoginManager();
        }
        loginManager.loginStreamer(channelId, password, new IPLVSceneLoginManager.OnStringCodeLoginListener<PLVSLoginVO>() {
            @Override
            public void onLoginSuccess(PLVSLoginVO loginVO) {
                updateLoginViewStatus(false);
                localInfoManager.saveLoginInfo(channelId, password, nick);

                //不填写登录昵称时，使用登录接口返回的后台设置的昵称
                String loginNick = TextUtils.isEmpty(nick) ? loginVO.getTeacherNickname() : nick;
                boolean isOpenMic = !plvlsLoginMicIv.isSelected();
                boolean isOpenCamera = !plvlsLoginCameraIv.isSelected();
                boolean isFrontCamera = plvlsLoginFrontCameraIv.getTag() == null;

                //进入手机开播场景
                PLVLSLiveStreamerActivity.launchStreamer(
                        PLVLSLoginStreamerActivity.this,
                        loginVO.getChannelId(),
                        loginVO.getAccountId(),
                        loginNick,
                        loginVO.getTeacherAvatar(),
                        loginVO.getTeacherActor(),
                        isOpenMic,
                        isOpenCamera,
                        isFrontCamera
                );
            }

            @Override
            public void onLoginFailed(String msg, Throwable throwable) {
                onLoginFailed(msg, "", throwable);
            }

            @Override
            public void onLoginFailed(String msg, String code, Throwable throwable) {
                throwable.printStackTrace();

                PLVToast.Builder.context(PLVLSLoginStreamerActivity.this)
                        .setText(msg)
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
            plvlsLoginEnterBtn.setEnabled(false);
        } else {
            plvlsLoginLoadingPb.setVisibility(View.GONE);
            plvlsLoginEnterBtn.setEnabled(true);
        }
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvls_login_mic_iv) {
            v.setSelected(!v.isSelected());
        } else if (id == R.id.plvls_login_camera_iv) {
            v.setSelected(!v.isSelected());
            plvlsLoginFrontCameraIv.setSelected(v.isSelected());
        } else if (id == R.id.plvls_login_front_camera_iv) {
            if (!v.isSelected()) {
                v.setTag(v.getTag() == null ? "back" : null);
            }
        } else if (id == R.id.plvls_login_enter_btn) {
            loginStreamer();
        } else if (id == R.id.plvls_login_private_policy_tv) {
            Intent intent = new Intent(this, PLVLSContractActivity.class);
            intent.putExtra(PLVLSContractActivity.KEY_IS_PRIVATE_POLICY, true);
            startActivity(intent);
        } else if (id == R.id.plvls_login_usage_contract_tv) {
            Intent intent = new Intent(this, PLVLSContractActivity.class);
            intent.putExtra(PLVLSContractActivity.KEY_IS_PRIVATE_POLICY, false);
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

    private CompoundButton.OnCheckedChangeListener onContractCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            plvlsLoginEnterBtn.setSelected(isChecked);
            plvlsLoginEnterBtn.setEnabled(isChecked);
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

        void saveLoginInfo(String channelId, String pwd, String nick) {
            LastLoginInfoIOManager.write(channelId, pwd, nick);
            LastLoginInfoIOManager.writeAgreeContract(true);
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
