package com.easefun.polyv.livecommon.module.modules.ppt.presenter;

import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.ppt.contract.IPLVLiveFloatingContract;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVOTeacherInfoEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVSocketUserBean;

/**
 * date: 2020/9/16
 * author: HWilliamgo
 * description:PPT悬浮窗的Presenter
 */
public class PLVLiveFloatingPresenter implements IPLVLiveFloatingContract.IPLVLiveFloatingPresenter {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //View
    @Nullable
    private IPLVLiveFloatingContract.IPLVLiveFloatingView view;

    //Listener
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    @Override
    public void init(final IPLVLiveFloatingContract.IPLVLiveFloatingView view) {
        this.view = view;
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                PLVSocketUserBean teacherUser = null;
                if (PLVEventConstant.Class.O_TEACHER_INFO.equals(event)) {
                    PLVOTeacherInfoEvent teacherInfoEvent = PLVEventHelper.toMessageEventModel(message, PLVOTeacherInfoEvent.class);
                    if (teacherInfoEvent != null) {
                        teacherUser = teacherInfoEvent.getData();
                    }
                } else if (PLVEventConstant.MESSAGE_EVENT_LOGIN.equals(event)) {
                    PLVLoginEvent loginEvent = PLVEventHelper.toMessageEventModel(message, PLVLoginEvent.class);
                    if (loginEvent != null) {
                        PLVSocketUserBean loginEventUser = loginEvent.getUser();
                        if (loginEventUser != null && PLVEventHelper.isChatroomTeacher(loginEventUser.getUserType(), loginEventUser.getUserSource())) {
                            teacherUser = loginEventUser;
                        }
                    }
                }
                if (teacherUser != null) {
                    String teacherNick = teacherUser.getNick();
                    String actor = teacherUser.getActor();
                    if (teacherUser.getAuthorization() != null) {//自定义头衔
                        actor = teacherUser.getAuthorization().getActor();
                    }
                    if (view != null) {
                        view.updateTeacherInfo(actor, teacherNick);
                    }
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="销毁">
    @Override
    public void destroy() {
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
        view = null;
    }
// </editor-fold>
}
