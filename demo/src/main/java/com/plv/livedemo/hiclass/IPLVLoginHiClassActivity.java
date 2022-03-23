package com.plv.livedemo.hiclass;

import com.plv.livecommon.module.utils.result.PLVLaunchResult;
import com.plv.livedemo.hiclass.fragments.PLVHCAbsLoginFragment;
import com.plv.livedemo.hiclass.model.vo.PLVHCLaunchHiClassVO;

/**
 * @author suhongtao
 */
public interface IPLVLoginHiClassActivity {

    /**
     * 注册 Fragment
     *
     * @param loginFragment
     */
    void registerFragment(PLVHCAbsLoginFragment loginFragment);

    /**
     * 注销 Fragment
     *
     * @param loginFragment
     */
    void unregisterFragment(PLVHCAbsLoginFragment loginFragment);

    /**
     * 跳转至上课页面
     *
     * @param vo 登录数据
     * @return 跳转结果
     */
    PLVLaunchResult requestLaunchHiClass(PLVHCLaunchHiClassVO vo);

    /**
     * 跳转至上课页面
     *
     * @param vo                          登录数据
     * @param isShowDeviceDetectionLayout 是否显示设备检测布局
     * @return 跳转结果
     */
    PLVLaunchResult requestLaunchHiClass(PLVHCLaunchHiClassVO vo, boolean isShowDeviceDetectionLayout);
}
