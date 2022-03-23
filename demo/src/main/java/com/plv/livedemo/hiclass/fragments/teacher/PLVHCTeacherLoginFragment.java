package com.plv.livedemo.hiclass.fragments.teacher;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listMap;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plv.livecommon.ui.widget.PLVAlignTopFillWidthImageView;
import com.plv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.plv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.plv.livedemo.PLVContractActivity;
import com.plv.livedemo.R;
import com.plv.livedemo.hiclass.fragments.PLVHCAbsLoginFragment;
import com.plv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager;
import com.plv.livedemo.hiclass.fragments.share.vo.PLVHCLoginLessonVO;
import com.plv.livedemo.hiclass.fragments.teacher.vo.PLVHCLoginAreaCodeVO;
import com.plv.livedemo.hiclass.fragments.teacher.vo.PLVHCLoginCompanyVO;
import com.plv.livedemo.hiclass.fragments.teacher.vo.PLVHCTeacherLoginAccountVO;
import com.plv.livedemo.hiclass.fragments.teacher.widget.PLVHCLoginAreaCodeSelectLayout;
import com.plv.livedemo.hiclass.model.vo.PLVHCLoginDataVO;
import com.plv.livedemo.hiclass.viewmodel.PLVHCLoginViewModel;
import com.plv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.livescenes.feature.login.model.PLVHCTeacherLoginVO;
import com.plv.livescenes.hiclass.vo.PLVHCTeacherLoginResultVO;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 讲师登录页
 *
 * @author suhongtao
 */
public class PLVHCTeacherLoginFragment extends PLVHCAbsLoginFragment implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVAlignTopFillWidthImageView plvhcTeacherLoginBgIv;
    private TextView plvhcLoginLabelTv;
    private PLVRoundRectGradientTextView plvhcLoginLabelTeacherTv;
    private LinearLayout plvhcLoginTeacherAccountLl;
    private TextView plvhcLoginTeacherAccountAreaCodeTv;
    private ImageView plvhcLoginAreaCodeSelectIv;
    private EditText plvhcLoginAccountEt;
    private ImageView plvhcLoginAccountClearIv;
    private View plvhcLoginTeacherAccountBottomSeparateView;
    private LinearLayout plvhcLoginTeacherPasswordLl;
    private EditText plvhcLoginTeacherPasswordEt;
    private ImageView plvhcLoginTeacherPasswordClearIv;
    private ImageView plvhcLoginTeacherPasswordVisibilityIv;
    private View plvhcLoginTeacherPasswordBottomSeparateView;
    private TextView plvhcLoginTeacherBtn;
    private LinearLayout plvhcLoginTeacherRememberPasswordLl;
    private CheckBox plvhcLoginTeacherRememberPasswordCb;
    private LinearLayout plvhcLoginTeacherAgreeContractLl;
    private CheckBox plvhcLoginAgreeContractCb;
    private TextView plvhcLoginContractTv;
    private PLVTriangleIndicateLayout plvhcLoginTeacherAgreeContractIndicateLayout;
    private ImageView plvhcLoginBackIv;
    private PLVHCLoginAreaCodeSelectLayout plvhcLoginAreaCodeSelectLayout;

    // 登录数据viewModel
    private PLVHCLoginViewModel loginViewModel;
    // 登录接口
    private IPLVSceneLoginManager loginManager;

    // 定时隐藏同意协议提示条
    private static final int MSG_WHAT_HIDE_INDICATE = 0;
    private final Handler hideIndicateHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_HIDE_INDICATE) {
                hideAgreeContractIndicate();
            }
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment生命周期重写">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.plvhc_login_teacher_login_fragment, null);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        findView();
        initViewModel();
        initAreaCodeSelectLayout();
        readLastLoginData();
        initEditTextOnChangedListener();
    }

    private void findView() {
        plvhcTeacherLoginBgIv = (PLVAlignTopFillWidthImageView) view.findViewById(R.id.plvhc_teacher_login_bg_iv);
        plvhcLoginLabelTv = (TextView) view.findViewById(R.id.plvhc_login_label_tv);
        plvhcLoginLabelTeacherTv = (PLVRoundRectGradientTextView) view.findViewById(R.id.plvhc_login_label_teacher_tv);
        plvhcLoginTeacherAccountLl = (LinearLayout) view.findViewById(R.id.plvhc_login_teacher_account_ll);
        plvhcLoginTeacherAccountAreaCodeTv = (TextView) view.findViewById(R.id.plvhc_login_teacher_account_area_code_tv);
        plvhcLoginAreaCodeSelectIv = (ImageView) view.findViewById(R.id.plvhc_login_area_code_select_iv);
        plvhcLoginAccountEt = (EditText) view.findViewById(R.id.plvhc_login_account_et);
        plvhcLoginAccountClearIv = (ImageView) view.findViewById(R.id.plvhc_login_account_clear_iv);
        plvhcLoginTeacherAccountBottomSeparateView = (View) view.findViewById(R.id.plvhc_login_teacher_account_bottom_separate_view);
        plvhcLoginTeacherPasswordLl = (LinearLayout) view.findViewById(R.id.plvhc_login_teacher_password_ll);
        plvhcLoginTeacherPasswordEt = (EditText) view.findViewById(R.id.plvhc_login_teacher_password_et);
        plvhcLoginTeacherPasswordClearIv = (ImageView) view.findViewById(R.id.plvhc_login_teacher_password_clear_iv);
        plvhcLoginTeacherPasswordVisibilityIv = (ImageView) view.findViewById(R.id.plvhc_login_teacher_password_visibility_iv);
        plvhcLoginTeacherPasswordBottomSeparateView = (View) view.findViewById(R.id.plvhc_login_teacher_password_bottom_separate_view);
        plvhcLoginTeacherBtn = (TextView) view.findViewById(R.id.plvhc_login_teacher_btn);
        plvhcLoginTeacherRememberPasswordLl = (LinearLayout) view.findViewById(R.id.plvhc_login_teacher_remember_password_ll);
        plvhcLoginTeacherRememberPasswordCb = (CheckBox) view.findViewById(R.id.plvhc_login_teacher_remember_password_cb);
        plvhcLoginTeacherAgreeContractLl = (LinearLayout) view.findViewById(R.id.plvhc_login_teacher_agree_contract_ll);
        plvhcLoginAgreeContractCb = (CheckBox) view.findViewById(R.id.plvhc_login_agree_contract_cb);
        plvhcLoginContractTv = (TextView) view.findViewById(R.id.plvhc_login_contract_tv);
        plvhcLoginTeacherAgreeContractIndicateLayout = (PLVTriangleIndicateLayout) view.findViewById(R.id.plvhc_login_teacher_agree_contract_indicate_layout);
        plvhcLoginBackIv = (ImageView) view.findViewById(R.id.plvhc_login_back_iv);
        plvhcLoginAreaCodeSelectLayout = (PLVHCLoginAreaCodeSelectLayout) view.findViewById(R.id.plvhc_login_area_code_select_layout);

        plvhcLoginTeacherAccountAreaCodeTv.setOnClickListener(this);
        plvhcLoginAreaCodeSelectIv.setOnClickListener(this);
        plvhcLoginTeacherBtn.setOnClickListener(this);
        plvhcLoginAgreeContractCb.setOnClickListener(this);
        plvhcLoginContractTv.setOnClickListener(this);
        plvhcLoginTeacherPasswordVisibilityIv.setOnClickListener(this);
        plvhcLoginBackIv.setOnClickListener(this);
        plvhcLoginAccountClearIv.setOnClickListener(this);
        plvhcLoginTeacherPasswordClearIv.setOnClickListener(this);
    }

    private void initViewModel() {
        if (getContext() == null) {
            return;
        }
        loginViewModel = new ViewModelProvider((ViewModelStoreOwner) getContext(),
                new ViewModelProvider.AndroidViewModelFactory((Application) getContext().getApplicationContext()))
                .get(PLVHCLoginViewModel.class);
    }

    private void initAreaCodeSelectLayout() {
        plvhcLoginAreaCodeSelectLayout.hide();
        plvhcLoginAreaCodeSelectLayout.setOnSelectAreaCodeListener(new PLVHCLoginAreaCodeSelectLayout.OnSelectAreaCodeListener() {
            @Override
            public void onSelect(PLVHCLoginAreaCodeVO vo) {
                plvhcLoginTeacherAccountAreaCodeTv.setText(vo.getCode());
            }
        });
        plvhcLoginAreaCodeSelectLayout.setOnVisibilityChangedListener(new PLVHCLoginAreaCodeSelectLayout.OnVisibilityChangedListener() {
            @Override
            public void onChanged(boolean isAreaCodeSelectLayoutVisible) {
                plvhcLoginBackIv.setVisibility(isAreaCodeSelectLayoutVisible ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void readLastLoginData() {
        plvhcLoginTeacherAccountAreaCodeTv.setText(readLastLoginAreaCode());
        plvhcLoginAccountEt.setText(readLastLoginAccount());
        plvhcLoginTeacherPasswordEt.setText(readLastLoginPassword());
        plvhcLoginTeacherRememberPasswordCb.setChecked(readRememberPassword());
        plvhcLoginAgreeContractCb.setChecked(readAgreeContract());
        updateLoginButtonState();
        updateClearInputButtonState();
    }

    private void initEditTextOnChangedListener() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonState();
                updateClearInputButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        plvhcLoginAccountEt.addTextChangedListener(textWatcher);
        plvhcLoginTeacherPasswordEt.addTextChangedListener(textWatcher);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public int getFragmentId() {
        return PLVHCLoginFragmentManager.FRAG_TEACHER_LOGIN;
    }

    @Override
    public boolean onBackPressed() {
        PLVHCLoginFragmentManager.getInstance().removeLast();
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI更新">

    private void updateLoginButtonState() {
        plvhcLoginTeacherBtn.setEnabled(!TextUtils.isEmpty(plvhcLoginAccountEt.getText().toString())
                && !TextUtils.isEmpty(plvhcLoginTeacherPasswordEt.getText().toString()));
    }

    private void updateClearInputButtonState() {
        final boolean isAccountEmpty = TextUtils.isEmpty(plvhcLoginAccountEt.getText().toString());
        final boolean isPasswordEmpty = TextUtils.isEmpty(plvhcLoginTeacherPasswordEt.getText().toString());
        plvhcLoginAccountClearIv.setVisibility(isAccountEmpty ? View.INVISIBLE : View.VISIBLE);
        plvhcLoginTeacherPasswordClearIv.setVisibility(isPasswordEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    private void showAgreeContractIndicate() {
        if (plvhcLoginTeacherAgreeContractIndicateLayout == null) {
            return;
        }
        plvhcLoginTeacherAgreeContractIndicateLayout.setVisibility(View.VISIBLE);

        // 3秒后自动隐藏
        hideIndicateHandler.sendMessageDelayed(Message.obtain(hideIndicateHandler, MSG_WHAT_HIDE_INDICATE), 3000);
    }

    private void hideAgreeContractIndicate() {
        hideIndicateHandler.removeMessages(MSG_WHAT_HIDE_INDICATE);
        if (plvhcLoginTeacherAgreeContractIndicateLayout != null) {
            plvhcLoginTeacherAgreeContractIndicateLayout.setVisibility(View.GONE);
        }
    }

    private void changePasswordVisibility() {
        final int passwordEtCurrentPosition = plvhcLoginTeacherPasswordEt.getSelectionEnd();

        plvhcLoginTeacherPasswordVisibilityIv.setSelected(!plvhcLoginTeacherPasswordVisibilityIv.isSelected());
        if (plvhcLoginTeacherPasswordVisibilityIv.isSelected()) {
            plvhcLoginTeacherPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            plvhcLoginTeacherPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        plvhcLoginTeacherPasswordEt.setSelection(passwordEtCurrentPosition);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录">

    private void processLogin() {
        if (!plvhcLoginAgreeContractCb.isChecked()) {
            showAgreeContractIndicate();
            return;
        }
        if (loginManager == null) {
            loginManager = new PLVSceneLoginManager();
        }
        final String areaCode = plvhcLoginTeacherAccountAreaCodeTv.getText().toString();
        final String account = plvhcLoginAccountEt.getText().toString();
        final String password = plvhcLoginTeacherPasswordEt.getText().toString();
        final long timestamp = System.currentTimeMillis();
        loginManager.loginHiClassTeacher(
                areaCode,
                account,
                password,
                null,
                null,
                new IPLVSceneLoginManager.OnLoginListener<PLVHCTeacherLoginVO>() {
                    @Override
                    public void onLoginSuccess(PLVHCTeacherLoginVO result) {
                        if (result == null
                                || result.getStatus() == null
                                || loginViewModel == null
                                || getContext() == null) {
                            return;
                        }

                        PLVHCTeacherLoginAccountVO accountVO = new PLVHCTeacherLoginAccountVO();
                        accountVO.setAreaCode(areaCode);
                        accountVO.setAccount(account);
                        accountVO.setPassword(password);
                        loginViewModel.getTeacherLoginAccountLiveData().setValue(accountVO);

                        saveLastLoginAccount(areaCode, account);
                        saveLastLoginPassword(plvhcLoginTeacherRememberPasswordCb.isChecked(), password);
                        saveAgreeContract(true);

                        if (result.getStatus() == PLVHCTeacherLoginVO.STATUS_LOGIN_SELECT_COMPANY) {
                            List<PLVHCLoginCompanyVO> loginCompanyVOList = new ArrayList<>();
                            for (PLVHCTeacherLoginResultVO.CompanyVO companyVO : result.getCompanyList()) {
                                loginCompanyVOList.add(PLVHCLoginCompanyVO.fromLoginResult(companyVO));
                            }
                            loginViewModel.getCompanyListLiveData().setValue(loginCompanyVOList);
                            KeyboardUtils.hideSoftInput(plvhcLoginTeacherPasswordEt);
                            PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_TEACHER_COMPANY);
                        } else if (result.getStatus() == PLVHCTeacherLoginVO.STATUS_LOGIN_SUCCESS) {
                            if (result.getSuccessData() != null) {
                                PLVHCLoginDataVO loginTokenVO = new PLVHCLoginDataVO();
                                loginTokenVO.setRole(PLVSocketUserConstant.USERTYPE_TEACHER);
                                loginTokenVO.setToken(result.getSuccessData().getToken());
                                loginTokenVO.setNickname(result.getSuccessData().getNickname());
                                loginTokenVO.setViewerId(result.getSuccessData().getViewerId());
                                loginViewModel.setLoginDataVOWithSave(loginTokenVO, timestamp);

                                List<PLVHCLoginLessonVO> loginLessonVOList = listMap(result.getSuccessData().getLessonList(),
                                        new PLVSugarUtil.Function<PLVHCTeacherLoginResultVO.DataVO.LessonVO, PLVHCLoginLessonVO>() {
                                            @Override
                                            public PLVHCLoginLessonVO apply(PLVHCTeacherLoginResultVO.DataVO.LessonVO lessonVO) {
                                                return PLVHCLoginLessonVO.fromTeacherLoginResultLessonVO(lessonVO);
                                            }
                                        });
                                loginViewModel.getLoginLessonListLiveData().setValue(loginLessonVOList);
                            }
                            KeyboardUtils.hideSoftInput(plvhcLoginTeacherPasswordEt);
                            PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_LESSON_SELECT);
                        } else {
                            PLVHCToast.Builder.context(getContext())
                                    .setText("登录状态错误：" + result.getStatus())
                                    .build().show();
                        }
                    }

                    @Override
                    public void onLoginFailed(String msg, Throwable throwable) {
                        if (getContext() == null) {
                            return;
                        }
                        PLVHCToast.Builder.context(getContext())
                                .setText(msg)
                                .build().show();
                    }
                }
        );
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录账号本地记忆保存">

    private void saveLastLoginAccount(String areaCode, String account) {
        if (loginViewModel != null) {
            loginViewModel.getLastLoginTeacherAreaCode().postValue(areaCode);
            loginViewModel.getLastLoginTeacherAccount().postValue(account);
        }
    }

    private void saveLastLoginPassword(boolean isRemember, String password) {
        if (!isRemember) {
            password = "";
        }
        if (loginViewModel != null) {
            loginViewModel.getLastLoginTeacherPassword().postValue(password);
            loginViewModel.getTeacherRememberPassword().postValue(isRemember);
        }
    }

    private void saveAgreeContract(boolean agree) {
        if (loginViewModel != null) {
            loginViewModel.getTeacherAgreeContract().postValue(agree);
        }
    }

    @NonNull
    private String readLastLoginAreaCode() {
        if (loginViewModel == null) {
            return "+86";
        }
        return getOrDefault(loginViewModel.getLastLoginTeacherAreaCode().getValue(), "+86");
    }

    @NonNull
    private String readLastLoginAccount() {
        if (loginViewModel == null) {
            return "";
        }
        return getOrDefault(loginViewModel.getLastLoginTeacherAccount().getValue(), "");
    }

    @NonNull
    private String readLastLoginPassword() {
        if (loginViewModel == null) {
            return "";
        }
        return getOrDefault(loginViewModel.getLastLoginTeacherPassword().getValue(), "");
    }

    private boolean readRememberPassword() {
        if (loginViewModel == null) {
            return false;
        }
        return getOrDefault(loginViewModel.getTeacherRememberPassword().getValue(), false);
    }

    private boolean readAgreeContract() {
        if (loginViewModel == null) {
            return false;
        }
        return getOrDefault(loginViewModel.getTeacherAgreeContract().getValue(), false);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击处理">

    @Override
    public void onClick(View v) {
        if (v.getId() == plvhcLoginTeacherAccountAreaCodeTv.getId()
                || v.getId() == plvhcLoginAreaCodeSelectIv.getId()) {
            plvhcLoginAreaCodeSelectLayout.show();
        } else if (v.getId() == plvhcLoginTeacherBtn.getId()) {
            KeyboardUtils.hideSoftInput(v);
            processLogin();
        } else if (v.getId() == plvhcLoginAgreeContractCb.getId()) {
            hideAgreeContractIndicate();
        } else if (v.getId() == plvhcLoginContractTv.getId()) {
            navigateToContractActivity();
        } else if (v.getId() == plvhcLoginTeacherPasswordVisibilityIv.getId()) {
            changePasswordVisibility();
        } else if (v.getId() == plvhcLoginBackIv.getId()) {
            PLVHCLoginFragmentManager.getInstance().removeLast();
        } else if (v.getId() == plvhcLoginAccountClearIv.getId()) {
            plvhcLoginAccountEt.setText("");
        } else if (v.getId() == plvhcLoginTeacherPasswordClearIv.getId()) {
            plvhcLoginTeacherPasswordEt.setText("");
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面跳转">

    private void navigateToContractActivity() {
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), PLVContractActivity.class);
        intent.putExtra(PLVContractActivity.KEY_IS_PRIVATE_POLICY, false);
        startActivity(intent);
    }

    // </editor-fold>

}
