package com.easefun.polyv.livedemo.hiclass.fragments.student;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listMap;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.widget.PLVAlignTopFillWidthImageView;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livedemo.R;
import com.easefun.polyv.livedemo.hiclass.IPLVLoginHiClassActivity;
import com.easefun.polyv.livedemo.hiclass.fragments.PLVHCAbsLoginFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager;
import com.easefun.polyv.livedemo.hiclass.fragments.share.vo.PLVHCLoginLessonVO;
import com.easefun.polyv.livedemo.hiclass.fragments.student.vo.PLVHCStudentLoginAccountVO;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLaunchHiClassVO;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLoginDataVO;
import com.easefun.polyv.livedemo.hiclass.viewmodel.PLVHCLoginViewModel;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.livescenes.hiclass.vo.PLVHCStudentVerifyResultVO;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 学生登录 - 验证观看条件页面
 *
 * @author suhongtao
 */
public class PLVHCStudentVerifyFragment extends PLVHCAbsLoginFragment implements LifecycleObserver, View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVAlignTopFillWidthImageView plvhcStudentLoginBgIv;
    private TextView plvhcLoginLabelTv;
    private PLVRoundRectGradientTextView plvhcLoginLabelStudentTv;
    private LinearLayout plvhcLoginStudentVerifyLl;
    private LinearLayout plvhcLoginStudentVerify1Ll;
    private EditText plvhcLoginStudentVerify1Et;
    private ImageView plvhcLoginStudentVerifyClear1Iv;
    private View plvhcLoginStudentVerify1SeparateView;
    private LinearLayout plvhcLoginStudentVerify2Ll;
    private EditText plvhcLoginStudentVerify2Et;
    private ImageView plvhcLoginStudentVerifyClear2Iv;
    private View plvhcLoginStudentVerify2SeparateView;
    private TextView plvhcLoginStudentBtn;
    private ImageView plvhcLoginBackIv;

    private final LinkedList<Runnable> onActivityStopPendingTaskList = new LinkedList<>();

    // 登录数据viewModel
    @Nullable
    private PLVHCLoginViewModel loginViewModel;
    private Observer<PLVHCStudentLoginAccountVO> loginAccountVOObserver;
    private PLVHCStudentLoginAccountVO currentLoginAccountVO = null;

    // 登录接口
    private IPLVSceneLoginManager loginManager;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment生命周期重写">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.plvhc_login_student_verify_fragment, null);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLifecycle().addObserver(this);
        initView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLifecycle().removeObserver(this);
        if (loginAccountVOObserver != null && loginViewModel != null) {
            loginViewModel.getStudentLoginAccountLiveData().removeObserver(loginAccountVOObserver);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onActivityStop() {
        while (!onActivityStopPendingTaskList.isEmpty()) {
            onActivityStopPendingTaskList.removeFirst().run();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        findView();
        initViewModel();
        observeAuthType();
        initEditText();

        updateLoginButtonEnableState();
        updateClearInputButtonState();
    }

    private void findView() {
        plvhcStudentLoginBgIv = (PLVAlignTopFillWidthImageView) view.findViewById(R.id.plvhc_student_login_bg_iv);
        plvhcLoginLabelTv = (TextView) view.findViewById(R.id.plvhc_login_label_tv);
        plvhcLoginLabelStudentTv = (PLVRoundRectGradientTextView) view.findViewById(R.id.plvhc_login_label_student_tv);
        plvhcLoginStudentVerifyLl = (LinearLayout) view.findViewById(R.id.plvhc_login_student_verify_ll);
        plvhcLoginStudentVerify1Ll = (LinearLayout) view.findViewById(R.id.plvhc_login_student_verify_1_ll);
        plvhcLoginStudentVerify1Et = (EditText) view.findViewById(R.id.plvhc_login_student_verify_1_et);
        plvhcLoginStudentVerifyClear1Iv = (ImageView) view.findViewById(R.id.plvhc_login_student_verify_clear_1_iv);
        plvhcLoginStudentVerify1SeparateView = (View) view.findViewById(R.id.plvhc_login_student_verify_1_separate_view);
        plvhcLoginStudentVerify2Ll = (LinearLayout) view.findViewById(R.id.plvhc_login_student_verify_2_ll);
        plvhcLoginStudentVerify2Et = (EditText) view.findViewById(R.id.plvhc_login_student_verify_2_et);
        plvhcLoginStudentVerifyClear2Iv = (ImageView) view.findViewById(R.id.plvhc_login_student_verify_clear_2_iv);
        plvhcLoginStudentVerify2SeparateView = (View) view.findViewById(R.id.plvhc_login_student_verify_2_separate_view);
        plvhcLoginStudentBtn = (TextView) view.findViewById(R.id.plvhc_login_student_btn);
        plvhcLoginBackIv = (ImageView) view.findViewById(R.id.plvhc_login_back_iv);

        plvhcLoginStudentBtn.setOnClickListener(this);
        plvhcLoginBackIv.setOnClickListener(this);
        plvhcLoginStudentVerifyClear1Iv.setOnClickListener(this);
        plvhcLoginStudentVerifyClear2Iv.setOnClickListener(this);
    }

    private void initViewModel() {
        if (getContext() == null) {
            return;
        }
        loginViewModel = new ViewModelProvider((ViewModelStoreOwner) getContext(),
                new ViewModelProvider.AndroidViewModelFactory((Application) getContext().getApplicationContext()))
                .get(PLVHCLoginViewModel.class);
    }

    private void observeAuthType() {
        if (loginViewModel == null || getActivity() == null) {
            return;
        }
        // 初始化无条件观看
        setAuthNoneStyle();

        loginAccountVOObserver = new Observer<PLVHCStudentLoginAccountVO>() {
            @Override
            public void onChanged(@Nullable PLVHCStudentLoginAccountVO studentLoginAccountVO) {
                if (studentLoginAccountVO == null) {
                    return;
                }
                currentLoginAccountVO = studentLoginAccountVO;
                switch (studentLoginAccountVO.getAuthType()) {
                    case PLVHCStudentLoginAccountVO.AUTH_TYPE_CODE:
                        setAuthCodeStyle();
                        break;
                    case PLVHCStudentLoginAccountVO.AUTH_TYPE_WHITE_LIST:
                        setAuthWhiteListStyle();
                        break;
                    case PLVHCStudentLoginAccountVO.AUTH_TYPE_NONE:
                    default:
                        setAuthNoneStyle();
                        break;
                }
            }
        };
        loginViewModel.getStudentLoginAccountLiveData().observe(getActivity(), loginAccountVOObserver);
    }

    private void initEditText() {
        plvhcLoginStudentVerify1Et.setText("");
        plvhcLoginStudentVerify2Et.setText("");

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonEnableState();
                updateClearInputButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        plvhcLoginStudentVerify1Et.addTextChangedListener(textWatcher);
        plvhcLoginStudentVerify2Et.addTextChangedListener(textWatcher);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public int getFragmentId() {
        return PLVHCLoginFragmentManager.FRAG_STUDENT_VERIFY;
    }

    @Override
    public boolean onBackPressed() {
        PLVHCLoginFragmentManager.getInstance().removeLast();
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI更新">

    private void setAuthNoneStyle() {
        plvhcLoginLabelTv.setText("设置名称");
        plvhcLoginStudentVerify1Et.setHint("请设置你的名称");
        plvhcLoginStudentVerify2Ll.setVisibility(View.GONE);
        plvhcLoginStudentVerify2SeparateView.setVisibility(View.GONE);
    }

    private void setAuthWhiteListStyle() {
        plvhcLoginLabelTv.setText("学生码");
        plvhcLoginStudentVerify1Et.setHint("请输入学生码");
        plvhcLoginStudentVerify2Ll.setVisibility(View.GONE);
        plvhcLoginStudentVerify2SeparateView.setVisibility(View.GONE);
    }

    private void setAuthCodeStyle() {
        plvhcLoginLabelTv.setText("验证信息");
        plvhcLoginStudentVerify1Et.setHint("请输入密码");
        plvhcLoginStudentVerify2Et.setHint("请设置你的名称");
        plvhcLoginStudentVerify2Ll.setVisibility(View.VISIBLE);
        plvhcLoginStudentVerify2SeparateView.setVisibility(View.VISIBLE);
    }

    private void updateLoginButtonEnableState() {
        final boolean et1NotEmpty = !TextUtils.isEmpty(plvhcLoginStudentVerify1Et.getText().toString());
        final boolean et2Visible = plvhcLoginStudentVerify2Ll.getVisibility() == View.VISIBLE;
        final boolean et2NotEmpty = !TextUtils.isEmpty(plvhcLoginStudentVerify2Et.getText().toString());
        plvhcLoginStudentBtn.setEnabled(et1NotEmpty && (!et2Visible || et2NotEmpty));
    }

    private void updateClearInputButtonState() {
        final boolean isEt1Empty = TextUtils.isEmpty(plvhcLoginStudentVerify1Et.getText().toString());
        final boolean isEt2Empty = TextUtils.isEmpty(plvhcLoginStudentVerify2Et.getText().toString());
        plvhcLoginStudentVerifyClear1Iv.setVisibility(isEt1Empty ? View.INVISIBLE : View.VISIBLE);
        plvhcLoginStudentVerifyClear2Iv.setVisibility(isEt2Empty ? View.INVISIBLE : View.VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录">

    private void processLogin() {
        if (currentLoginAccountVO == null || getContext() == null) {
            return;
        }
        if (loginManager == null) {
            loginManager = new PLVSceneLoginManager();
        }
        final String editTextParam1 = plvhcLoginStudentVerify1Et.getText().toString().trim();
        final String editTextParam2 = plvhcLoginStudentVerify2Et.getText().toString().trim();

        PLVSceneLoginManager.PLVHCStudentLoginVerifyType loginVerifyType = null;
        if (PLVHCStudentLoginAccountVO.AUTH_TYPE_NONE.equals(currentLoginAccountVO.getAuthType())) {
            loginVerifyType = PLVSceneLoginManager.PLVHCStudentLoginVerifyType.NONE;
        } else if (PLVHCStudentLoginAccountVO.AUTH_TYPE_CODE.equals(currentLoginAccountVO.getAuthType())) {
            loginVerifyType = PLVSceneLoginManager.PLVHCStudentLoginVerifyType.CODE;
        } else if (PLVHCStudentLoginAccountVO.AUTH_TYPE_WHITE_LIST.equals(currentLoginAccountVO.getAuthType())) {
            loginVerifyType = PLVSceneLoginManager.PLVHCStudentLoginVerifyType.WHITE_LIST;
        }
        if (loginVerifyType == null) {
            return;
        }

        String loginName = null;
        boolean needCheckNameNotEmpty = false;
        if (PLVHCStudentLoginAccountVO.AUTH_TYPE_NONE.equals(currentLoginAccountVO.getAuthType())) {
            loginName = editTextParam1;
            needCheckNameNotEmpty = true;
        } else if (PLVHCStudentLoginAccountVO.AUTH_TYPE_CODE.equals(currentLoginAccountVO.getAuthType())) {
            loginName = editTextParam2;
            needCheckNameNotEmpty = true;
        }

        String loginCode = null;
        if (PLVHCStudentLoginAccountVO.AUTH_TYPE_CODE.equals(currentLoginAccountVO.getAuthType())) {
            loginCode = editTextParam1;
        }

        String loginStudentCode = null;
        if (PLVHCStudentLoginAccountVO.AUTH_TYPE_WHITE_LIST.equals(currentLoginAccountVO.getAuthType())) {
            loginStudentCode = editTextParam1;
        }

        final String courseCode = currentLoginAccountVO.getCourseCode();
        final Long lessonId = currentLoginAccountVO.getLessonId();
        final String name = loginName;
        final String code = loginCode;
        final String studentCode = loginStudentCode;

        if (needCheckNameNotEmpty && TextUtils.isEmpty(name)) {
            PLVHCToast.Builder.context(getContext())
                    .setText("昵称不能为空")
                    .build().show();
            return;
        }

        loginManager.loginHiClassStudent(loginVerifyType, courseCode, lessonId, name, code, studentCode,
                new IPLVSceneLoginManager.OnLoginListener<PLVHCStudentVerifyResultVO>() {
                    @Override
                    public void onLoginSuccess(PLVHCStudentVerifyResultVO result) {
                        if (result != null
                                && result.getSuccess()
                                && result.getData() != null) {
                            if (courseCode != null) {
                                // 课程号登录
                                PLVHCLoginDataVO loginDataVO = new PLVHCLoginDataVO();
                                loginDataVO.setRole(PLVSocketUserConstant.USERTYPE_SCSTUDENT);
                                loginDataVO.setCourseCode(courseCode);
                                loginDataVO.setToken(result.getData().getToken());
                                loginDataVO.setNickname(result.getData().getNickname());
                                loginDataVO.setViewerId(result.getData().getViewerId());

                                List<PLVHCLoginLessonVO> loginLessonVOList = listMap(result.getData().getList(),
                                        new PLVSugarUtil.Function<PLVHCStudentVerifyResultVO.DataVO.LessonVO, PLVHCLoginLessonVO>() {
                                            @Override
                                            public PLVHCLoginLessonVO apply(PLVHCStudentVerifyResultVO.DataVO.LessonVO lessonVO) {
                                                return PLVHCLoginLessonVO.fromStudentLoginResultLessonVO(lessonVO);
                                            }
                                        });
                                if (loginViewModel != null) {
                                    loginViewModel.getLoginDataLiveData().setValue(loginDataVO);
                                    loginViewModel.getLoginLessonListLiveData().setValue(loginLessonVOList);
                                    KeyboardUtils.hideSoftInput(plvhcLoginStudentVerify1Et);
                                    PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_LESSON_SELECT);
                                    return;
                                }
                            } else if (lessonId != null) {
                                KeyboardUtils.hideSoftInput(plvhcLoginStudentVerify1Et);
                                // 课节号登录
                                launchLiveHiClassActivity(
                                        result.getData().getToken(),
                                        lessonId,
                                        result.getData().getViewerId(),
                                        result.getData().getNickname(),
                                        result.getData().getChannelId(lessonId)
                                        );
                                return;
                            }
                        }
                        if (result != null
                                && result.getError() != null
                                && getContext() != null) {
                            PLVHCToast.Builder.context(getContext())
                                    .setText(result.getError().getDesc())
                                    .build().show();
                        }
                    }

                    @Override
                    public void onLoginFailed(String msg, Throwable throwable) {
                        PLVCommonLog.exception(throwable);
                        if (getContext() != null) {
                            PLVHCToast.Builder.context(getContext())
                                    .setText(msg)
                                    .build().show();
                        }
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击处理">

    @Override
    public void onClick(View v) {
        if (v.getId() == plvhcLoginStudentBtn.getId()) {
            KeyboardUtils.hideSoftInput(v);
            processLogin();
        } else if (v.getId() == plvhcLoginBackIv.getId()) {
            PLVHCLoginFragmentManager.getInstance().removeLast();
        } else if (v.getId() == plvhcLoginStudentVerifyClear1Iv.getId()) {
            plvhcLoginStudentVerify1Et.setText("");
        } else if (v.getId() == plvhcLoginStudentVerifyClear2Iv.getId()) {
            plvhcLoginStudentVerify2Et.setText("");
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面跳转">

    private void launchLiveHiClassActivity(final String token,
                                           final long lessonId,
                                           final String viewerId,
                                           final String viewerName,
                                           final String channelId) {
        if (!(getActivity() instanceof IPLVLoginHiClassActivity)) {
            return;
        }
        PLVHCLaunchHiClassVO launchHiClassVO = new PLVHCLaunchHiClassVO(
                channelId,
                null,
                lessonId,
                token,
                lessonId + "",
                PLVSocketUserConstant.USERTYPE_SCSTUDENT,
                viewerId,
                viewerName,
                PLVSocketUserConstant.STUDENT_AVATAR_URL_V2
        );

        PLVLaunchResult launchResult = ((IPLVLoginHiClassActivity) getActivity()).requestLaunchHiClass(launchHiClassVO);

        if (launchResult.isSuccess()) {
            // 学生登录，退出时需要返回到课程课节号输入界面
            onActivityStopPendingTaskList.add(new Runnable() {
                @Override
                public void run() {
                    PLVHCLoginFragmentManager.getInstance().removeAfter(PLVHCLoginFragmentManager.FRAG_STUDENT_LOGIN);
                }
            });
        } else {
            PLVHCToast.Builder.context(getActivity())
                    .setText(launchResult.getErrorMessage())
                    .build().show();
        }
    }

    // </editor-fold>

}
