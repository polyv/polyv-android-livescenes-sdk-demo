package com.plv.livedemo.hiclass.fragments.share;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getNullableOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.plv.livecommon.module.utils.imageloader.glide.PLVImageUtils;
import com.plv.livecommon.module.utils.result.PLVLaunchResult;
import com.plv.livecommon.ui.widget.PLVConfirmDialog;
import com.plv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.plv.livedemo.R;
import com.plv.livedemo.hiclass.IPLVLoginHiClassActivity;
import com.plv.livedemo.hiclass.fragments.PLVHCAbsLoginFragment;
import com.plv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager;
import com.plv.livedemo.hiclass.fragments.share.adapter.PLVHCLoginLessonAdapter;
import com.plv.livedemo.hiclass.fragments.share.item.PLVHCLoginLessonItemView;
import com.plv.livedemo.hiclass.fragments.share.vo.PLVHCLoginLessonVO;
import com.plv.livedemo.hiclass.model.vo.PLVHCLaunchHiClassVO;
import com.plv.livedemo.hiclass.model.vo.PLVHCLoginDataVO;
import com.plv.livedemo.hiclass.viewmodel.PLVHCLoginViewModel;
import com.plv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.plv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.hiclass.api.PLVHCApiManager;
import com.plv.livescenes.hiclass.vo.PLVHCLessonDetailVO;
import com.plv.livescenes.hiclass.vo.PLVHCLessonStatusVO;
import com.plv.livescenes.hiclass.vo.PLVHCStudentLessonListVO;
import com.plv.livescenes.hiclass.vo.PLVHCTeacherLessonListVO;
import com.plv.socket.user.PLVSocketUserConstant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * 登录 - 课节选择页面
 *
 * @author suhongtao
 */
public class PLVHCLessonSelectFragment extends PLVHCAbsLoginFragment implements LifecycleObserver, View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private TextView plvhcLoginLessonWelcomeTv;
    private TextView plvhcLoginLessonLabelTv;
    private TextView plvhcLogoutTv;
    private LinearLayout plvhcLoginNoLessonLayout;
    private ScrollView plvhcLoginLessonSv;
    private PLVRoundRectLayout plvhcLoginCurrentLessonLayout;
    private TextView plvhcLoginCurrentLessonLabelTv;
    private PLVHCLoginLessonItemView plvhcLoginCurrentLessonItem;
    private PLVRoundRectLayout plvhcLoginOtherLessonLayout;
    private TextView plvhcLoginOtherLessonLabelTv;
    private RecyclerView plvhcLoginOtherLessonRv;
    private ImageView plvhcLoginBackIv;

    // 登录数据viewModel
    private PLVHCLoginViewModel loginViewModel;
    private PLVHCLoginDataVO loginDataVO;
    private boolean isTeacherType = false;

    private PLVHCLoginLessonAdapter loginLessonAdapter;

    private Observer<PLVHCLoginDataVO> loginDataObserver;
    private Observer<List<PLVHCLoginLessonVO>> loginLessonObserver;
    private Disposable lessonRequestDisposable;
    private Disposable lessonStatusDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment生命周期重写">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.plvhc_login_lesson_select_fragment, null);
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
        if (loginDataObserver != null) {
            loginViewModel.getLoginDataLiveData().removeObserver(loginDataObserver);
        }
        if (loginLessonObserver != null) {
            loginViewModel.getLoginLessonListLiveData().removeObserver(loginLessonObserver);
        }
        if (lessonRequestDisposable != null) {
            lessonRequestDisposable.dispose();
        }
        if (lessonStatusDisposable != null) {
            lessonStatusDisposable.dispose();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onActivityStart() {
        requestLessonList();
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
        plvhcLoginLessonWelcomeTv = (TextView) view.findViewById(R.id.plvhc_login_lesson_welcome_tv);
        plvhcLoginLessonLabelTv = (TextView) view.findViewById(R.id.plvhc_login_lesson_label_tv);
        plvhcLogoutTv = (TextView) view.findViewById(R.id.plvhc_logout_tv);
        plvhcLoginNoLessonLayout = (LinearLayout) view.findViewById(R.id.plvhc_login_no_lesson_layout);
        plvhcLoginLessonSv = (ScrollView) view.findViewById(R.id.plvhc_login_lesson_sv);
        plvhcLoginCurrentLessonLayout = (PLVRoundRectLayout) view.findViewById(R.id.plvhc_login_current_lesson_layout);
        plvhcLoginCurrentLessonLabelTv = (TextView) view.findViewById(R.id.plvhc_login_current_lesson_label_tv);
        plvhcLoginCurrentLessonItem = (PLVHCLoginLessonItemView) view.findViewById(R.id.plvhc_login_current_lesson_item);
        plvhcLoginOtherLessonLayout = (PLVRoundRectLayout) view.findViewById(R.id.plvhc_login_other_lesson_layout);
        plvhcLoginOtherLessonLabelTv = (TextView) view.findViewById(R.id.plvhc_login_other_lesson_label_tv);
        plvhcLoginOtherLessonRv = (RecyclerView) view.findViewById(R.id.plvhc_login_other_lesson_rv);
        plvhcLoginBackIv = (ImageView) view.findViewById(R.id.plvhc_login_back_iv);

        plvhcLogoutTv.setOnClickListener(this);
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
        loginLessonAdapter = new PLVHCLoginLessonAdapter();
        plvhcLoginOtherLessonRv.setAdapter(loginLessonAdapter);
        plvhcLoginOtherLessonRv.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        loginLessonAdapter.setOnItemClickListener(new PLVHCLoginLessonAdapter.OnItemClickListener() {
            @Override
            public void onClick(PLVHCLoginLessonVO loginLessonVO) {
                if (isTeacherType) {
                    checkLessonStatusToLogin(loginLessonVO);
                } else {
                    loginLesson(loginLessonVO);
                }
            }
        });
    }

    private void observeData() {
        if (getContext() == null || loginViewModel == null) {
            return;
        }
        loginViewModel.getLoginDataLiveData().observe((LifecycleOwner) getContext(),
                loginDataObserver = new Observer<PLVHCLoginDataVO>() {
                    @Override
                    public void onChanged(@Nullable PLVHCLoginDataVO loginDataVO) {
                        if (loginDataVO == null) {
                            return;
                        }
                        PLVHCLessonSelectFragment.this.loginDataVO = loginDataVO;
                        PLVHCLessonSelectFragment.this.isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(loginDataVO.getRole());
                        updateWelcomeUserText();
                        updateBackIcon();
                    }
                });

        loginViewModel.getLoginLessonListLiveData().observe((LifecycleOwner) getContext(),
                loginLessonObserver = new Observer<List<PLVHCLoginLessonVO>>() {
                    @Override
                    public void onChanged(@Nullable List<PLVHCLoginLessonVO> loginLessonVOList) {
                        processLoginLessonList(loginLessonVOList);
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public int getFragmentId() {
        return PLVHCLoginFragmentManager.FRAG_LESSON_SELECT;
    }

    @Override
    public boolean onBackPressed() {
        if (isTeacherType) {
            onTeacherClickLogout();
        } else {
            PLVHCLoginFragmentManager.getInstance().removeLast();
        }
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - UI控件更新">

    private void updateWelcomeUserText() {
        String welcomeName = "Hello " + loginDataVO.getNickname();
        if (!isTeacherType) {
            welcomeName = welcomeName + "同学";
        } else {
            welcomeName = welcomeName + "老师";
        }
        plvhcLoginLessonWelcomeTv.setText(welcomeName);
    }

    private void updateBackIcon() {
        plvhcLogoutTv.setVisibility(isTeacherType ? View.VISIBLE : View.GONE);
        plvhcLoginBackIv.setVisibility(isTeacherType ? View.GONE : View.VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 拉取课节数据">

    private void requestLessonList() {
        if (loginViewModel == null) {
            return;
        }
        PLVHCLoginDataVO loginTokenVO = loginViewModel.getLoginDataLiveData().getValue();
        if (loginTokenVO == null) {
            return;
        }
        final String token = loginTokenVO.getToken();
        if (token == null) {
            return;
        }

        Observable<List<PLVHCLoginLessonVO>> lessonListObservable = null;
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(loginTokenVO.getRole())) {
            lessonListObservable = mapTeacherLessonList(token);
        } else if (PLVSocketUserConstant.USERTYPE_SCSTUDENT.equals(loginTokenVO.getRole())) {
            final String courseCode = loginTokenVO.getCourseCode();
            if (courseCode == null) {
                return;
            }
            lessonListObservable = mapStudentLessonList(token, courseCode);
        }
        if (lessonListObservable == null) {
            return;
        }

        if (lessonRequestDisposable != null) {
            lessonRequestDisposable.dispose();
        }
        lessonRequestDisposable = lessonListObservable
                .subscribe(new Consumer<List<PLVHCLoginLessonVO>>() {
                    @Override
                    public void accept(List<PLVHCLoginLessonVO> plvhcLoginLessonVOS) throws Exception {
                        processLoginLessonList(plvhcLoginLessonVOS);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    private static Observable<List<PLVHCLoginLessonVO>> mapTeacherLessonList(@NonNull String token) {
        return PLVHCApiManager.getInstance().listTeacherLesson(token)
                .filter(new Predicate<PLVHCTeacherLessonListVO>() {
                    @Override
                    public boolean test(@NotNull PLVHCTeacherLessonListVO teacherLessonListVO) throws Exception {
                        return teacherLessonListVO.getData() != null;
                    }
                })
                .map(new Function<PLVHCTeacherLessonListVO, List<PLVHCLoginLessonVO>>() {
                    @Override
                    public List<PLVHCLoginLessonVO> apply(@NotNull PLVHCTeacherLessonListVO teacherLessonListVO) throws Exception {
                        List<PLVHCLoginLessonVO> result = new ArrayList<>();
                        for (PLVHCTeacherLessonListVO.DataVO dataVO : teacherLessonListVO.getData()) {
                            PLVHCLoginLessonVO loginLessonVO = new PLVHCLoginLessonVO();
                            loginLessonVO.setImageUrl(PLVImageUtils.fixImageUrl(dataVO.getCover()));
                            loginLessonVO.setLessonTitle(dataVO.getName());
                            loginLessonVO.setLessonTime(dataVO.getTime());
                            loginLessonVO.setCourseTitle(dataVO.getCourseNames());
                            final long lessonId = dataVO.getLessonId() == null ? 0 : dataVO.getLessonId();
                            loginLessonVO.setLessonId(lessonId);
                            loginLessonVO.setChannelId(dataVO.getChannelId());
                            result.add(loginLessonVO);
                        }
                        return result;
                    }
                });
    }

    private static Observable<List<PLVHCLoginLessonVO>> mapStudentLessonList(@NonNull String token, @NonNull String courseCode) {
        return PLVHCApiManager.getInstance().listStudentLesson(token, courseCode)
                .filter(new Predicate<PLVHCStudentLessonListVO>() {
                    @Override
                    public boolean test(@NotNull PLVHCStudentLessonListVO plvhcStudentLessonListVO) throws Exception {
                        return plvhcStudentLessonListVO.getData() != null;
                    }
                })
                .map(new Function<PLVHCStudentLessonListVO, List<PLVHCLoginLessonVO>>() {
                    @Override
                    public List<PLVHCLoginLessonVO> apply(@NotNull PLVHCStudentLessonListVO studentLessonListVO) throws Exception {
                        List<PLVHCLoginLessonVO> result = new ArrayList<>();
                        for (PLVHCStudentLessonListVO.DataVO dataVO : studentLessonListVO.getData()) {
                            PLVHCLoginLessonVO loginLessonVO = new PLVHCLoginLessonVO();
                            loginLessonVO.setImageUrl(PLVImageUtils.fixImageUrl(dataVO.getCover()));
                            loginLessonVO.setLessonTitle(dataVO.getName());
                            loginLessonVO.setLessonTime(dataVO.getStartTime());
                            loginLessonVO.setCourseTitle("");
                            final long lessonId = dataVO.getLessonId() == null ? 0 : dataVO.getLessonId();
                            loginLessonVO.setLessonId(lessonId);
                            loginLessonVO.setChannelId(dataVO.getChannelId());
                            result.add(loginLessonVO);
                        }
                        return result;
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 课节数据更新">

    private void processLoginLessonList(List<PLVHCLoginLessonVO> loginLessonVOList) {
        if (loginLessonVOList == null || loginLessonVOList.size() <= 0) {
            setCurrentLesson(null);
            setOtherLesson(null);
            plvhcLoginNoLessonLayout.setVisibility(View.VISIBLE);
            return;
        }

        plvhcLoginNoLessonLayout.setVisibility(View.GONE);
        setCurrentLesson(loginLessonVOList.get(0));
        setOtherLesson(loginLessonVOList.subList(1, loginLessonVOList.size()));
    }

    private void setCurrentLesson(@Nullable final PLVHCLoginLessonVO loginLessonVO) {
        if (loginLessonVO == null) {
            plvhcLoginCurrentLessonLayout.setVisibility(View.GONE);
            return;
        }

        plvhcLoginCurrentLessonLayout.setVisibility(View.VISIBLE);
        plvhcLoginCurrentLessonItem.setData(loginLessonVO);
        plvhcLoginCurrentLessonItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTeacherType) {
                    checkLessonStatusToLogin(loginLessonVO);
                } else {
                    loginLesson(loginLessonVO);
                }
            }
        });
    }

    private void setOtherLesson(List<PLVHCLoginLessonVO> loginLessonVOList) {
        if (loginLessonVOList == null || loginLessonVOList.size() <= 0) {
            plvhcLoginOtherLessonLayout.setVisibility(View.GONE);
            return;
        }

        plvhcLoginOtherLessonLayout.setVisibility(View.VISIBLE);
        loginLessonAdapter.setLessons(loginLessonVOList);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 登录">

    private void checkLessonStatusToLogin(final PLVHCLoginLessonVO loginLessonVO) {
        final String token = loginDataVO.getToken();
        final long lessonId = loginLessonVO.getLessonId();
        final boolean isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(loginDataVO.getRole());
        final String courseCode = loginDataVO.getCourseCode();

        if (lessonStatusDisposable != null) {
            lessonStatusDisposable.dispose();
        }
        lessonStatusDisposable = PLVHCApiManager.getInstance().getLessonDetail(isTeacherType, courseCode, lessonId, token)
                .filter(new Predicate<PLVHCLessonDetailVO>() {
                    @Override
                    public boolean test(@io.reactivex.annotations.NonNull PLVHCLessonDetailVO detailVO) throws Exception {
                        final boolean success = getNullableOrDefault(new PLVSugarUtil.Supplier<Boolean>() {
                            @Override
                            public Boolean get() {
                                return detailVO.isSuccess().booleanValue() && detailVO.getData() != null;
                            }
                        }, false);

                        if (success) {
                            loginLesson(loginLessonVO);
                        }
                        return !success;
                    }
                })
                .flatMap(new Function<PLVHCLessonDetailVO, ObservableSource<PLVHCLessonStatusVO>>() {
                    @Override
                    public ObservableSource<PLVHCLessonStatusVO> apply(@io.reactivex.annotations.NonNull PLVHCLessonDetailVO plvhcLessonDetailVO) throws Exception {
                        return PLVHCApiManager.getInstance().getLessonStatus(token, loginLessonVO.getLessonId());
                    }
                })
                .map(new Function<PLVHCLessonStatusVO, Integer>() {
                    @Override
                    public Integer apply(@io.reactivex.annotations.NonNull PLVHCLessonStatusVO plvhcLessonStatusVO) throws Exception {
                        return nullable(new PLVSugarUtil.Supplier<Integer>() {
                            @Override
                            public Integer get() {
                                return plvhcLessonStatusVO.getData().getStatus();
                            }
                        });
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(final Integer status) throws Exception {
                        if (getContext() == null) {
                            return;
                        }

                        if (status == null) {
                            PLVHCToast.Builder.context(getContext())
                                    .setText("进入课节异常")
                                    .build().show();
                        } else if (status == PLVHCLessonStatusVO.DataVO.STATUS_ON_CLASS) {
                            PLVHCToast.Builder.context(getContext())
                                    .setText("课节正在进行，您不能重复进入")
                                    .build().show();
                        } else {
                            loginLesson(loginLessonVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (getContext() == null) {
                            return;
                        }

                        PLVHCToast.Builder.context(getContext())
                                .setText("进入课节异常")
                                .build().show();
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 讲师退出登录弹窗确认">

    private void onTeacherClickLogout() {
        if (getContext() == null) {
            return;
        }
        new PLVHCConfirmDialog(getContext())
                .setTitle("提示")
                .setContent("是否要退出登录")
                .setLeftButtonText("取消")
                .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setRightButtonText("退出")
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        loginViewModel.saveLaunchHiClassData(null);
                        PLVHCLoginFragmentManager.getInstance().removeAfter(PLVHCLoginFragmentManager.FRAG_ROLE_SELECT);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击处理">

    @Override
    public void onClick(View v) {
        if (v.getId() == plvhcLogoutTv.getId()) {
            onTeacherClickLogout();
        } else if (v.getId() == plvhcLoginBackIv.getId()) {
            PLVHCLoginFragmentManager.getInstance().removeLast();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面跳转">

    private void loginLesson(PLVHCLoginLessonVO loginLessonVO) {
        if (!(getActivity() instanceof IPLVLoginHiClassActivity)) {
            return;
        }
        PLVHCLaunchHiClassVO launchHiClassVO = new PLVHCLaunchHiClassVO(
                loginLessonVO.getChannelId(),
                loginDataVO.getCourseCode(),
                loginLessonVO.getLessonId(),
                loginDataVO.getToken(),
                loginLessonVO.getLessonId() + "",
                loginDataVO.getRole(),
                loginDataVO.getViewerId(),
                loginDataVO.getNickname(),
                loginDataVO.getAvatarUrl()
        );

        PLVLaunchResult launchResult = ((IPLVLoginHiClassActivity) getActivity()).requestLaunchHiClass(launchHiClassVO);

        if (!launchResult.isSuccess()) {
            PLVHCToast.Builder.context(getActivity())
                    .setText(launchResult.getErrorMessage())
                    .build().show();
        }
    }

    // </editor-fold>

}
