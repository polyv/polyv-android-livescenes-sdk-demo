package com.plv.livedemo.hiclass.fragments.student;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

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
import com.plv.livedemo.hiclass.fragments.student.vo.PLVHCStudentLoginAccountVO;
import com.plv.livedemo.hiclass.viewmodel.PLVHCLoginViewModel;
import com.plv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.hiclass.api.PLVHCApiManager;
import com.plv.livescenes.hiclass.vo.PLVHCLessonSimpleInfoResultVO;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 学生登录 - 课程号课节号输入页面
 *
 * @author suhongtao
 */
public class PLVHCStudentLoginFragment extends PLVHCAbsLoginFragment implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVAlignTopFillWidthImageView plvhcStudentLoginBgIv;
    private TextView plvhcLoginLabelTv;
    private PLVRoundRectGradientTextView plvhcLoginLabelStudentTv;
    private EditText plvhcLoginStudentCourseIdEt;
    private ImageView plvhcStudentLoginCodeClearIv;
    private View plvhcLoginStudentCourseIdBottomSeparateView;
    private TextView plvhcLoginStudentBtn;
    private LinearLayout plvhcLoginStudentAgreeContractLl;
    private CheckBox plvhcLoginAgreeContractCb;
    private TextView plvhcLoginContractTv;
    private PLVTriangleIndicateLayout plvhcLoginStudentAgreeContractIndicateLayout;
    private ImageView plvhcLoginBackIv;

    // 登录数据viewModel
    private PLVHCLoginViewModel loginViewModel;

    private Disposable lessonSimpleInfoDisposable;

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
            view = inflater.inflate(R.layout.plvhc_login_student_login_fragment, null);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (lessonSimpleInfoDisposable != null) {
            lessonSimpleInfoDisposable.dispose();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        findView();
        initViewModel();
        initCourseIdOnTextChangedListener();
        initAgreeContract();

        updateLoginButtonState();
        updateClearCodeButtonState();
    }

    private void findView() {
        plvhcStudentLoginBgIv = (PLVAlignTopFillWidthImageView) view.findViewById(R.id.plvhc_student_login_bg_iv);
        plvhcLoginLabelTv = (TextView) view.findViewById(R.id.plvhc_login_label_tv);
        plvhcLoginLabelStudentTv = (PLVRoundRectGradientTextView) view.findViewById(R.id.plvhc_login_label_student_tv);
        plvhcLoginStudentCourseIdEt = (EditText) view.findViewById(R.id.plvhc_login_student_course_id_et);
        plvhcStudentLoginCodeClearIv = (ImageView) view.findViewById(R.id.plvhc_student_login_code_clear_iv);
        plvhcLoginStudentCourseIdBottomSeparateView = (View) view.findViewById(R.id.plvhc_login_student_course_id_bottom_separate_view);
        plvhcLoginStudentBtn = (TextView) view.findViewById(R.id.plvhc_login_student_btn);
        plvhcLoginStudentAgreeContractLl = (LinearLayout) view.findViewById(R.id.plvhc_login_student_agree_contract_ll);
        plvhcLoginAgreeContractCb = (CheckBox) view.findViewById(R.id.plvhc_login_agree_contract_cb);
        plvhcLoginContractTv = (TextView) view.findViewById(R.id.plvhc_login_contract_tv);
        plvhcLoginStudentAgreeContractIndicateLayout = (PLVTriangleIndicateLayout) view.findViewById(R.id.plvhc_login_student_agree_contract_indicate_layout);
        plvhcLoginBackIv = (ImageView) view.findViewById(R.id.plvhc_login_back_iv);

        plvhcLoginStudentBtn.setOnClickListener(this);
        plvhcLoginAgreeContractCb.setOnClickListener(this);
        plvhcLoginContractTv.setOnClickListener(this);
        plvhcLoginBackIv.setOnClickListener(this);
        plvhcStudentLoginCodeClearIv.setOnClickListener(this);
    }

    private void initViewModel() {
        if (getContext() == null) {
            return;
        }
        loginViewModel = new ViewModelProvider((ViewModelStoreOwner) getContext(),
                new ViewModelProvider.AndroidViewModelFactory((Application) getContext().getApplicationContext()))
                .get(PLVHCLoginViewModel.class);
    }

    private void initCourseIdOnTextChangedListener() {
        plvhcLoginStudentCourseIdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonState();
                updateClearCodeButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initAgreeContract() {
        plvhcLoginAgreeContractCb.setChecked(readAgreeContract());
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public int getFragmentId() {
        return PLVHCLoginFragmentManager.FRAG_STUDENT_LOGIN;
    }

    @Override
    public boolean onBackPressed() {
        PLVHCLoginFragmentManager.getInstance().removeLast();
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录">

    private void processLogin() {
        if (!plvhcLoginAgreeContractCb.isChecked()) {
            showAgreeContractIndicate();
            return;
        }
        if (lessonSimpleInfoDisposable != null) {
            lessonSimpleInfoDisposable.dispose();
        }

        final String lessonIdOrCourseCode = plvhcLoginStudentCourseIdEt.getText().toString();
        Long parsedLessonId = null;
        try {
            parsedLessonId = Long.parseLong(lessonIdOrCourseCode);
        } catch (NumberFormatException e) {
            // ignore: 课程号为字母+数字，课节号为纯数字，解析异常表明输入的是课程号
        }
        // 当课节号解析出不为空时，表明输入的是课节号，课程号置空
        final String courseCode = parsedLessonId == null ? lessonIdOrCourseCode : null;
        final Long lessonId = parsedLessonId;

        lessonSimpleInfoDisposable = PLVHCApiManager.getInstance().getLessonSimpleInfo(courseCode, lessonId)
                .subscribe(new Consumer<PLVHCLessonSimpleInfoResultVO>() {
                    @Override
                    public void accept(PLVHCLessonSimpleInfoResultVO plvhcLessonSimpleInfoResultVO) throws Exception {
                        if (plvhcLessonSimpleInfoResultVO.getSuccess() != null
                                && plvhcLessonSimpleInfoResultVO.getSuccess()
                                && plvhcLessonSimpleInfoResultVO.getData() != null) {
                            PLVHCStudentLoginAccountVO loginAccountVO = null;
                            switch (plvhcLessonSimpleInfoResultVO.getData().getWatchCondition()) {
                                case PLVHCLessonSimpleInfoResultVO.DataVO.WATCH_CONDITION_NULL:
                                    loginAccountVO = new PLVHCStudentLoginAccountVO(PLVHCStudentLoginAccountVO.AUTH_TYPE_NONE);
                                    break;
                                case PLVHCLessonSimpleInfoResultVO.DataVO.WATCH_CONDITION_CODE:
                                    loginAccountVO = new PLVHCStudentLoginAccountVO(PLVHCStudentLoginAccountVO.AUTH_TYPE_CODE);
                                    break;
                                case PLVHCLessonSimpleInfoResultVO.DataVO.WATCH_CONDITION_WHITE_LIST:
                                    loginAccountVO = new PLVHCStudentLoginAccountVO(PLVHCStudentLoginAccountVO.AUTH_TYPE_WHITE_LIST);
                                    break;
                                default:
                            }
                            if (loginAccountVO != null && loginViewModel != null) {
                                loginAccountVO.setCourseCode(courseCode);
                                loginAccountVO.setLessonId(lessonId);
                                loginViewModel.getStudentLoginAccountLiveData().setValue(loginAccountVO);
                                saveAgreeContract(true);
                                KeyboardUtils.hideSoftInput(plvhcLoginStudentCourseIdEt);
                                PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_STUDENT_VERIFY);
                                return;
                            }
                        }
                        if (plvhcLessonSimpleInfoResultVO.getError() != null && getContext() != null) {
                            PLVHCToast.Builder.context(getContext())
                                    .setText(plvhcLessonSimpleInfoResultVO.getError().getDesc())
                                    .build().show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        if (getContext() == null) {
                            return;
                        }
                        PLVHCToast.Builder.context(getContext())
                                .setText(throwable.getMessage())
                                .build().show();
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI更新">

    private void showAgreeContractIndicate() {
        if (plvhcLoginStudentAgreeContractIndicateLayout == null) {
            return;
        }
        plvhcLoginStudentAgreeContractIndicateLayout.setVisibility(View.VISIBLE);

        // 3秒后自动隐藏
        hideIndicateHandler.sendMessageDelayed(Message.obtain(hideIndicateHandler, MSG_WHAT_HIDE_INDICATE), 3000);
    }

    private void hideAgreeContractIndicate() {
        hideIndicateHandler.removeMessages(MSG_WHAT_HIDE_INDICATE);
        if (plvhcLoginStudentAgreeContractIndicateLayout != null) {
            plvhcLoginStudentAgreeContractIndicateLayout.setVisibility(View.GONE);
        }
    }

    private void updateLoginButtonState() {
        plvhcLoginStudentBtn.setEnabled(!TextUtils.isEmpty(plvhcLoginStudentCourseIdEt.getText().toString()));
    }

    private void updateClearCodeButtonState() {
        final boolean isCodeEmpty = TextUtils.isEmpty(plvhcLoginStudentCourseIdEt.getText().toString());
        plvhcStudentLoginCodeClearIv.setVisibility(isCodeEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录账号本地记忆保存">

    private void saveAgreeContract(boolean agree) {
        if (loginViewModel != null) {
            loginViewModel.getTeacherAgreeContract().postValue(agree);
        }
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
        if (v.getId() == plvhcLoginStudentBtn.getId()) {
            KeyboardUtils.hideSoftInput(v);
            processLogin();
        } else if (v.getId() == plvhcLoginAgreeContractCb.getId()) {
            hideAgreeContractIndicate();
        } else if (v.getId() == plvhcLoginContractTv.getId()) {
            navigateToContractActivity();
        } else if (v.getId() == plvhcLoginBackIv.getId()) {
            PLVHCLoginFragmentManager.getInstance().removeLast();
        } else if (v.getId() == plvhcStudentLoginCodeClearIv.getId()) {
            plvhcLoginStudentCourseIdEt.setText("");
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
