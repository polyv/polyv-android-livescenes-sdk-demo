package com.easefun.polyv.livedemo.hiclass.viewmodel;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.easefun.polyv.livedemo.hiclass.fragments.share.vo.PLVHCLoginLessonVO;
import com.easefun.polyv.livedemo.hiclass.fragments.student.vo.PLVHCStudentLoginAccountVO;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo.PLVHCLoginCompanyVO;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo.PLVHCTeacherLoginAccountVO;
import com.easefun.polyv.livedemo.hiclass.model.PLVHCLoginRepo;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLaunchHiClassVO;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLoginDataVO;
import com.easefun.polyv.livehiclass.modules.liveroom.event.PLVHCOnLessonStatusEvent;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author suhongtao
 */
public class PLVHCLoginViewModel extends ViewModel {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // MVP - Model
    private final PLVHCLoginRepo loginRepo = new PLVHCLoginRepo();

    // 讲师端数据
    private final MutableLiveData<List<PLVHCLoginCompanyVO>> liveDataCompanyVOList = new MutableLiveData<>();
    private final MutableLiveData<PLVHCTeacherLoginAccountVO> liveDataTeacherLoginAccountVO = new MutableLiveData<>();

    // 学生端数据
    private final MutableLiveData<PLVHCStudentLoginAccountVO> liveDataStudentLoginAccountVO = new MutableLiveData<>();

    // 两端通用数据
    private final MutableLiveData<PLVHCLoginDataVO> liveDataLoginDataVO = new MutableLiveData<>();
    private final MutableLiveData<List<PLVHCLoginLessonVO>> liveDataLoginLessonVOList = new MutableLiveData<>();

    // 事件
    private final MutableLiveData<Event<PLVHCOnLessonStatusEvent>> onLessonStatusEventLiveData = new MutableLiveData<>();

    private Disposable lessonStartDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLoginViewModel() {
        initViewModel();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initViewModel() {
        observeLessonStatusEvent();
    }

    private void observeLessonStatusEvent() {
        lessonStartDisposable = PLVHCOnLessonStatusEvent.Bus.observe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVHCOnLessonStatusEvent>() {
                    @Override
                    public void accept(PLVHCOnLessonStatusEvent onLessonStatusEvent) throws Exception {
                        getLessonOnGoingLastClass().postValue(onLessonStatusEvent.isStart());
                        onLessonStatusEventLiveData.postValue(new Event<>(onLessonStatusEvent));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @Override
    protected void onCleared() {
        super.onCleared();
        if (lessonStartDisposable != null) {
            lessonStartDisposable.dispose();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 讲师登录数据保存">

    /**
     * 讲师登录保存数据
     */
    public void setLoginDataVOWithSave(PLVHCLoginDataVO loginDataVO, long tokenCreateTimestamp) {
        getLoginDataLiveData().setValue(loginDataVO);

        PLVHCLaunchHiClassVO launchHiClassVO = new PLVHCLaunchHiClassVO()
                .setTokenCreateTimestamp(tokenCreateTimestamp)
                .createByLoginDataVO(loginDataVO);
        loginRepo.getLaunchHiClassVO().postValue(launchHiClassVO);
    }

    public void saveLaunchHiClassData(PLVHCLaunchHiClassVO vo) {
        PLVHCLaunchHiClassVO lastLaunchVO = getOrDefault(loginRepo.getLaunchHiClassVO().getValue(), new PLVHCLaunchHiClassVO());
        loginRepo.getLaunchHiClassVO().postValue(lastLaunchVO.copyFrom(vo));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="LiveData - getter">

    public MutableLiveData<List<PLVHCLoginCompanyVO>> getCompanyListLiveData() {
        return liveDataCompanyVOList;
    }

    public MutableLiveData<PLVHCTeacherLoginAccountVO> getTeacherLoginAccountLiveData() {
        return liveDataTeacherLoginAccountVO;
    }

    public MutableLiveData<String> getLastLoginTeacherAreaCode() {
        return loginRepo.getLastLoginTeacherAreaCode();
    }

    public MutableLiveData<String> getLastLoginTeacherAccount() {
        return loginRepo.getLastLoginTeacherAccount();
    }

    public MutableLiveData<String> getLastLoginTeacherPassword() {
        return loginRepo.getLastLoginTeacherPassword();
    }

    public MutableLiveData<Boolean> getTeacherRememberPassword() {
        return loginRepo.getTeacherRememberPassword();
    }

    public MutableLiveData<Boolean> getTeacherAgreeContract() {
        return loginRepo.getTeacherAgreeContract();
    }

    public PLVHCLaunchHiClassVO getLaunchHiClassVO() {
        return loginRepo.getLaunchHiClassVO().getValue();
    }

    public MutableLiveData<Boolean> getLessonOnGoingLastClass() {
        return loginRepo.getLessonOnGoingLastClass();
    }

    public MutableLiveData<PLVHCStudentLoginAccountVO> getStudentLoginAccountLiveData() {
        return liveDataStudentLoginAccountVO;
    }

    public MutableLiveData<PLVHCLoginDataVO> getLoginDataLiveData() {
        return liveDataLoginDataVO;
    }

    public MutableLiveData<List<PLVHCLoginLessonVO>> getLoginLessonListLiveData() {
        return liveDataLoginLessonVOList;
    }

    public MutableLiveData<Event<PLVHCOnLessonStatusEvent>> getOnLessonStatusEventLiveData() {
        return onLessonStatusEventLiveData;
    }

    // </editor-fold>

}
