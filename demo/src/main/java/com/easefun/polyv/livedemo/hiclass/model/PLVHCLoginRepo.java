package com.easefun.polyv.livedemo.hiclass.model;

import androidx.lifecycle.MutableLiveData;

import com.easefun.polyv.livedemo.hiclass.model.constant.PLVHCLoginSPKeys;
import com.easefun.polyv.livedemo.hiclass.model.vo.PLVHCLaunchHiClassVO;
import com.plv.foundationsdk.component.livedata.PLVAutoSaveLiveData;

/**
 * @author suhongtao
 */
public class PLVHCLoginRepo {

    // 本地记忆存储数据，用于自动填充登录信息
    private final MutableLiveData<String> lastLoginTeacherAreaCode = new PLVAutoSaveLiveData<String>(PLVHCLoginSPKeys.KEY_LAST_LOGIN_TEACHER_AREA_CODE) {};
    private final MutableLiveData<String> lastLoginTeacherAccount = new PLVAutoSaveLiveData<String>(PLVHCLoginSPKeys.KEY_LAST_LOGIN_TEACHER_ACCOUNT) {};
    private final MutableLiveData<String> lastLoginTeacherPassword = new PLVAutoSaveLiveData<String>(PLVHCLoginSPKeys.KEY_LAST_LOGIN_TEACHER_PASSWORD) {};
    private final MutableLiveData<Boolean> teacherRememberPassword = new PLVAutoSaveLiveData<Boolean>(PLVHCLoginSPKeys.KEY_TEACHER_REMEMBER_PASSWORD) {};
    private final MutableLiveData<Boolean> teacherAgreeContract = new PLVAutoSaveLiveData<Boolean>(PLVHCLoginSPKeys.KEY_AGREE_CONTRACT) {};

    // 本地记忆存储数据，用于讲师自动恢复登录状态
    private final MutableLiveData<PLVHCLaunchHiClassVO> launchHiClassVO = new PLVAutoSaveLiveData<PLVHCLaunchHiClassVO>(PLVHCLoginSPKeys.KEY_LOGIN_LAUNCH_HI_CLASS_DATA) {};
    private final MutableLiveData<Boolean> lessonOnGoingLastClass = new PLVAutoSaveLiveData<Boolean>(PLVHCLoginSPKeys.KEY_LESSON_ON_GOING_LAST_CLASS) {};

    public MutableLiveData<String> getLastLoginTeacherAreaCode() {
        return lastLoginTeacherAreaCode;
    }

    public MutableLiveData<String> getLastLoginTeacherAccount() {
        return lastLoginTeacherAccount;
    }

    public MutableLiveData<String> getLastLoginTeacherPassword() {
        return lastLoginTeacherPassword;
    }

    public MutableLiveData<Boolean> getTeacherRememberPassword() {
        return teacherRememberPassword;
    }

    public MutableLiveData<Boolean> getTeacherAgreeContract() {
        return teacherAgreeContract;
    }

    public MutableLiveData<PLVHCLaunchHiClassVO> getLaunchHiClassVO() {
        return launchHiClassVO;
    }

    public MutableLiveData<Boolean> getLessonOnGoingLastClass() {
        return lessonOnGoingLastClass;
    }

}
