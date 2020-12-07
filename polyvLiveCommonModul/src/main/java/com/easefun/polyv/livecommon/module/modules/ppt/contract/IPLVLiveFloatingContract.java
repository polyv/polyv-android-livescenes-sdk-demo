package com.easefun.polyv.livecommon.module.modules.ppt.contract;

/**
 * date: 2020/9/16
 * author: HWilliamgo
 * description: 悬浮窗业务MVP
 */
public interface IPLVLiveFloatingContract {

    /**
     * 悬浮窗View
     */
    interface IPLVLiveFloatingView {

        /**
         * 设置讲师信息
         *
         * @param actor 讲师头衔
         * @param nick  讲师昵称
         */
        void updateTeacherInfo(String actor, String nick);
    }

    /**
     * 悬浮窗业务Presenter
     */
    interface IPLVLiveFloatingPresenter {
        /**
         * 初始化
         */
        void init(IPLVLiveFloatingView view);

        /**
         * 销毁
         */
        void destroy();
    }
}
