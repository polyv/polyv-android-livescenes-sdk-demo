package com.easefun.polyv.livedemo.hiclass.fragments.teacher;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listMap;

import android.app.Application;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livedemo.R;
import com.easefun.polyv.livedemo.hiclass.fragments.PLVHCAbsLoginFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager;
import com.easefun.polyv.livedemo.hiclass.fragments.share.vo.PLVHCLoginLessonVO;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.adapter.PLVHCTeacherCompanyAdapter;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo.PLVHCLoginCompanyVO;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo.PLVHCTeacherLoginAccountVO;
import com.easefun.polyv.livedemo.hiclass.model.constant.PLVHCLoginSPKeys;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLoginDataVO;
import com.easefun.polyv.livedemo.hiclass.viewmodel.PLVHCLoginViewModel;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.foundationsdk.utils.PLVUtils;
import com.plv.livescenes.feature.login.IPLVSceneLoginManager;
import com.plv.livescenes.feature.login.PLVSceneLoginManager;
import com.plv.livescenes.feature.login.model.PLVHCTeacherLoginVO;
import com.plv.livescenes.hiclass.vo.PLVHCTeacherLoginResultVO;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.List;

/**
 * 讲师公司选择页面
 *
 * @author suhongtao
 */
public class PLVHCTeacherCompanyFragment extends PLVHCAbsLoginFragment implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private TextView plvhcTeacherCompanyLabelTv;
    private RecyclerView plvhcTeacherCompanyRv;
    private TextView plvhcTeacherCompanyLoginTv;
    private ImageView plvhcLoginBackIv;

    // 登录数据viewModel
    private PLVHCLoginViewModel loginViewModel;
    private Observer<List<PLVHCLoginCompanyVO>> companiesObserver;
    private Observer<PLVHCTeacherLoginAccountVO> teacherLoginAccountObserver;

    // 登录接口
    private IPLVSceneLoginManager loginManager;

    private PLVHCTeacherCompanyAdapter companyAdapter;

    private PLVHCTeacherLoginAccountVO loginAccountVO;
    private PLVHCLoginCompanyVO selectedLoginCompanyVO = null;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment生命周期重写">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.plvhc_login_teacher_company_select_fragment, null);
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
        if (loginViewModel != null && companiesObserver != null) {
            loginViewModel.getCompanyListLiveData().removeObserver(companiesObserver);
        }
        if (loginViewModel != null && teacherLoginAccountObserver != null) {
            loginViewModel.getTeacherLoginAccountLiveData().removeObserver(teacherLoginAccountObserver);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        findView();
        initViewModel();
        initRecyclerView();

        observeData();
    }

    private void findView() {
        plvhcTeacherCompanyLabelTv = (TextView) view.findViewById(R.id.plvhc_teacher_company_label_tv);
        plvhcTeacherCompanyRv = (RecyclerView) view.findViewById(R.id.plvhc_teacher_company_rv);
        plvhcTeacherCompanyLoginTv = (TextView) view.findViewById(R.id.plvhc_teacher_company_login_tv);
        plvhcLoginBackIv = (ImageView) view.findViewById(R.id.plvhc_login_back_iv);

        plvhcTeacherCompanyLoginTv.setOnClickListener(this);
        plvhcLoginBackIv.setOnClickListener(this);
    }

    private void initViewModel() {
        if (getContext() == null) {
            return;
        }
        loginViewModel = new ViewModelProvider((ViewModelStoreOwner) getContext(),
                new ViewModelProvider.AndroidViewModelFactory((Application) getContext().getApplicationContext()))
                .get(PLVHCLoginViewModel.class);
    }

    private void initRecyclerView() {
        companyAdapter = new PLVHCTeacherCompanyAdapter();
        plvhcTeacherCompanyRv.setAdapter(companyAdapter);
        plvhcTeacherCompanyRv.setLayoutManager(new LinearLayoutManager(getContext()));

        companyAdapter.setOnItemClickedListener(new PLVHCTeacherCompanyAdapter.OnItemClickedListener() {
            @Override
            public void onClick(PLVHCLoginCompanyVO vo) {
                selectedLoginCompanyVO = vo;
            }
        });

        if (loginViewModel != null) {
            companyAdapter.setCompanies(loginViewModel.getCompanyListLiveData().getValue());
        }
    }

    private void observeData() {
        if (getContext() == null || loginViewModel == null) {
            return;
        }
        loginViewModel.getCompanyListLiveData().observe((LifecycleOwner) getContext(),
                companiesObserver = new Observer<List<PLVHCLoginCompanyVO>>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable List<PLVHCLoginCompanyVO> companyVOList) {
                        if (companyAdapter != null) {
                            companyAdapter.setCompanies(companyVOList);
                        }
                    }
                });

        loginViewModel.getTeacherLoginAccountLiveData().observe((LifecycleOwner) getContext(),
                teacherLoginAccountObserver = new Observer<PLVHCTeacherLoginAccountVO>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVHCTeacherLoginAccountVO plvhcTeacherLoginAccountVO) {
                        loginAccountVO = plvhcTeacherLoginAccountVO;
                        readLocalLastLoginCompany(loginAccountVO);
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public int getFragmentId() {
        return PLVHCLoginFragmentManager.FRAG_TEACHER_COMPANY;
    }

    @Override
    public boolean onBackPressed() {
        PLVHCLoginFragmentManager.getInstance().removeLast();
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录">

    private void processLogin() {
        if (getContext() == null
                || loginAccountVO == null) {
            return;
        }
        if (loginManager == null) {
            loginManager = new PLVSceneLoginManager();
        }
        if (selectedLoginCompanyVO == null || selectedLoginCompanyVO.getCompanyId() == null) {
            PLVHCToast.Builder.context(getContext())
                    .setText("请选择公司")
                    .build().show();
            return;
        }
        final String areaCode = loginAccountVO.getAreaCode();
        final String account = loginAccountVO.getAccount();
        final String password = loginAccountVO.getPassword();
        final String companyId = selectedLoginCompanyVO.getCompanyId();
        final String selectCompanyVoJson = selectedLoginCompanyVO.toJson();
        final long timestamp = System.currentTimeMillis();
        loginManager.loginHiClassTeacher(
                areaCode,
                account,
                password,
                null,
                companyId,
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
                        accountVO.setCompanyId(companyId);
                        loginViewModel.getTeacherLoginAccountLiveData().setValue(accountVO);
                        saveLoginSelectCompany(accountVO, selectCompanyVoJson);

                        if (result.getStatus() == PLVHCTeacherLoginVO.STATUS_LOGIN_SUCCESS) {
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
                            PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_LESSON_SELECT);
                        } else if (result.getStatus() == PLVHCTeacherLoginVO.STATUS_LOGIN_SELECT_COMPANY) {
                            PLVHCToast.Builder.context(getContext())
                                    .setText("请选择公司")
                                    .build().show();
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

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录公司本地记忆保存">

    private void readLocalLastLoginCompany(PLVHCTeacherLoginAccountVO accountVO) {
        if (accountVO == null || this.selectedLoginCompanyVO != null) {
            return;
        }
        PLVHCLoginCompanyVO loginCompanyVO = readLastLoginSelectCompany(accountVO);
        if (loginCompanyVO != null) {
            this.selectedLoginCompanyVO = loginCompanyVO;
            if (companyAdapter != null) {
                companyAdapter.setSelectedCompany(loginCompanyVO);
            }
        }
    }

    private void saveLoginSelectCompany(PLVHCTeacherLoginAccountVO accountVO, String selectCompanyVoJson) {
        final String key = PLVUtils.MD5(accountVO.getAreaCode() + accountVO.getAccount() + accountVO.getPassword()).substring(0, 16);
        SPUtils.getInstance().put(PLVHCLoginSPKeys.KEY_LAST_LOGIN_SELECT_COMPANY_PREFIX + key, selectCompanyVoJson);
    }

    private PLVHCLoginCompanyVO readLastLoginSelectCompany(PLVHCTeacherLoginAccountVO accountVO) {
        final String key = PLVUtils.MD5(accountVO.getAreaCode() + accountVO.getAccount() + accountVO.getPassword()).substring(0, 16);
        final String json = SPUtils.getInstance().getString(PLVHCLoginSPKeys.KEY_LAST_LOGIN_SELECT_COMPANY_PREFIX + key);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return PLVHCLoginCompanyVO.fromJson(json);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击处理">

    @Override
    public void onClick(View v) {
        if (v.getId() == plvhcTeacherCompanyLoginTv.getId()) {
            processLogin();
        } else if (v.getId() == plvhcLoginBackIv.getId()) {
            PLVHCLoginFragmentManager.getInstance().removeLast();
        }
    }


    // </editor-fold>

}
