package com.easefun.polyv.livedemo.hiclass;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getNullableOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livedemo.R;
import com.easefun.polyv.livedemo.hiclass.fragments.PLVHCAbsLoginFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager;
import com.easefun.polyv.livedemo.hiclass.fragments.viewpager.PLVHCLoginViewPagerAdapter;
import com.easefun.polyv.livedemo.hiclass.fragments.viewpager.PLVHCNoTouchViewPager;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLaunchHiClassVO;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLoginDataVO;
import com.easefun.polyv.livedemo.hiclass.viewmodel.PLVHCLoginViewModel;
import com.easefun.polyv.livehiclass.modules.liveroom.event.PLVHCOnLessonStatusEvent;
import com.easefun.polyv.livehiclass.scenes.PLVHCLiveHiClassActivity;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.socket.user.PLVSocketUserConstant;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * 互动学堂登录页面
 */
public class PLVLoginHiClassActivity extends PLVBaseActivity implements IPLVLoginHiClassActivity {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final long HI_CLASS_TOKEN_OUTDATED_TIMEOUT = TimeUnit.HOURS.toMillis(12);

    private PLVHCNoTouchViewPager plvhcLoginVp;
    private PLVHCLoginViewPagerAdapter viewPagerAdapter;

    // 登录数据viewModel
    private PLVHCLoginViewModel loginViewModel;

    /**
     * key: fragment id, get by {@link PLVHCAbsLoginFragment#getFragmentId()},
     * see also {@link PLVHCLoginFragmentManager}
     * <p>
     * value: weak reference of fragment
     */
    private final SparseArray<WeakReference<PLVHCAbsLoginFragment>> fragmentMap = new SparseArray<>();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvhc_login_hi_class_activity);

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVHCLoginFragmentManager.getInstance().destroy();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity方法 返回按键重写">

    @Override
    public void onBackPressed() {
        for (int i = PLVHCLoginFragmentManager.FRAG_MAX_ID; i >= PLVHCLoginFragmentManager.FRAG_MIN_ID; --i) {
            final int fragId = i;
            boolean consume = getNullableOrDefault(new PLVSugarUtil.Supplier<Boolean>() {
                @Override
                public Boolean get() {
                    return fragmentMap.get(fragId).get().onBackPressed();
                }
            }, false);
            if (consume) {
                return;
            }
        }
        super.onBackPressed();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">

    private void initView() {
        setTransparentStatusBar();

        findView();
        initViewModel();
        initViewPager();

        observeLessonEndEvent();

        tryNavigateByLastTeacherLoginLaunchVO();
    }

    private void findView() {
        plvhcLoginVp = (PLVHCNoTouchViewPager) findViewById(R.id.plvhc_login_vp);
    }

    private void initViewModel() {
        loginViewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                new ViewModelProvider.AndroidViewModelFactory((Application) getApplicationContext()))
                .get(PLVHCLoginViewModel.class);
    }

    private void initViewPager() {
        viewPagerAdapter = new PLVHCLoginViewPagerAdapter(getSupportFragmentManager());
        plvhcLoginVp.setAdapter(viewPagerAdapter);

        PLVHCLoginFragmentManager.getInstance().observeOnFragmentListChanged()
                .observe(this, new Observer<Integer>() {
                    @Override
                    public void onChanged(@Nullable Integer size) {
                        viewPagerAdapter.updateFragments(PLVHCLoginFragmentManager.getInstance().getReadOnlyFragmentIdList());
                        if (size != null && size > 0) {
                            plvhcLoginVp.setCurrentItem(size - 1);
                        }
                    }
                });

        PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_ROLE_SELECT);
    }

    private void observeLessonEndEvent() {
        loginViewModel.getOnLessonStatusEventLiveData().observe(this, new Observer<Event<PLVHCOnLessonStatusEvent>>() {
            @Override
            public void onChanged(@Nullable final Event<PLVHCOnLessonStatusEvent> eventWrapper) {
                PLVHCOnLessonStatusEvent event = nullable(new PLVSugarUtil.Supplier<PLVHCOnLessonStatusEvent>() {
                    @Override
                    public PLVHCOnLessonStatusEvent get() {
                        return eventWrapper.get();
                    }
                });
                if (event == null) {
                    return;
                }
                if (!event.isStart() && !event.isTeacherType() && !event.hasNextClass()) {
                    // 学生观看结束并且没有下一课节时，返回输入课程号课节号页面
                    PLVHCLoginFragmentManager.getInstance().removeAfter(PLVHCLoginFragmentManager.FRAG_STUDENT_LOGIN);
                }
            }
        });
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

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 提供给Fragment的接口方法实现">

    @Override
    public void registerFragment(PLVHCAbsLoginFragment loginFragment) {
        fragmentMap.put(loginFragment.getFragmentId(), new WeakReference<>(loginFragment));
    }

    @Override
    public void unregisterFragment(PLVHCAbsLoginFragment loginFragment) {
        fragmentMap.remove(loginFragment.getFragmentId());
    }

    @Override
    public PLVLaunchResult requestLaunchHiClass(PLVHCLaunchHiClassVO vo) {
        return requestLaunchHiClass(vo, true);
    }

    @Override
    public PLVLaunchResult requestLaunchHiClass(PLVHCLaunchHiClassVO vo, boolean isShowDeviceDetectionLayout) {
        return launchHiClass(vo, isShowDeviceDetectionLayout);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑 - 判断并使用讲师上次登录已保存的数据">

    /**
     * 讲师自动登录使用上次保存的数据
     * 1、讲师已登录并且上次退出前处于上课状态，直接进入上课页面
     * 2、讲师已登录，不处于上课状态，进入课节选择页面
     * <p>
     * 上次保存数据有效期为12小时 {@link #HI_CLASS_TOKEN_OUTDATED_TIMEOUT}
     */
    private void tryNavigateByLastTeacherLoginLaunchVO() {
        if (loginViewModel == null) {
            return;
        }
        PLVHCLaunchHiClassVO vo = loginViewModel.getLaunchHiClassVO();
        if (canUseLastLoginLaunchVO(vo)) {
            navigateByLastLoginLaunchVO(vo);
        }
    }

    private boolean canUseLastLoginLaunchVO(PLVHCLaunchHiClassVO launchHiClassVO) {
        if (launchHiClassVO == null) {
            return false;
        }
        if (!PLVSocketUserConstant.USERTYPE_TEACHER.equals(launchHiClassVO.getUserType())) {
            return false;
        }
        if (System.currentTimeMillis() - launchHiClassVO.getTokenCreateTimestamp() >= HI_CLASS_TOKEN_OUTDATED_TIMEOUT) {
            return false;
        }
        if (launchHiClassVO.getToken() == null) {
            return false;
        }
        return true;
    }

    private void navigateByLastLoginLaunchVO(PLVHCLaunchHiClassVO launchHiClassVO) {
        if (loginViewModel == null) {
            return;
        }

        boolean lastTimeOnClass = getOrDefault(loginViewModel.getLessonOnGoingLastClass().getValue(), false);
        if (lastTimeOnClass) {
            // 上一次退出时仍在上课，直接跳转到上课 Activity
            navigateToClass(launchHiClassVO);
        }
        // 无论是否跳转到上课页面，都添加课节选择页面
        // 未跳转到上课页面时可以选择课节
        // 已跳转到上课页面时，退出可以返回到课节选择页面
        navigateToLessonSelect(launchHiClassVO);
    }

    private boolean navigateToLessonSelect(PLVHCLaunchHiClassVO launchHiClassVO) {
        if (loginViewModel == null) {
            return false;
        }
        PLVHCLoginDataVO loginDataVO = new PLVHCLoginDataVO().recreateByLaunchHiClassVO(launchHiClassVO);

        loginViewModel.getLoginDataLiveData().setValue(loginDataVO);
        PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_LESSON_SELECT);
        return true;
    }

    private boolean navigateToClass(PLVHCLaunchHiClassVO launchHiClassVO) {
        PLVLaunchResult result = requestLaunchHiClass(launchHiClassVO, false);
        return result.isSuccess();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面跳转">

    private PLVLaunchResult launchHiClass(PLVHCLaunchHiClassVO vo, boolean isShowDeviceDetectionLayout) {
        PLVLaunchResult result = PLVHCLiveHiClassActivity.launchHiClass(
                this,
                vo.getChannelId(),
                vo.getCourseCode(),
                getOrDefault(vo.getLessonId(), 0L),
                vo.getToken(),
                vo.getSessionId(),
                vo.getUserType(),
                vo.getViewerId(),
                vo.getViewerName(),
                vo.getAvatarUrl(),
                isShowDeviceDetectionLayout
        );

        if (result.isSuccess()) {
            loginViewModel.saveLaunchHiClassData(vo);
        }

        return result;
    }

    // </editor-fold>

}
