package com.easefun.polyv.livecommon.module.modules.popover;

import android.content.Intent;

import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.interact.IPLVInteractLayout;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractLayout2;
import com.easefun.polyv.livecommon.module.modules.reward.OnPointRewardListener;
import com.easefun.polyv.livecommon.module.modules.reward.PLVPointRewardLayout;

public interface IPLVPopoverLayout {


    /**
     * 初始化
     */
    void init(PLVLiveScene scene, IPLVLiveRoomDataManager roomDataManager);

    /**
     * 设置打开内部链接WebView监听器
     */
    void setOnOpenInsideWebViewListener(PLVInteractLayout2.OnOpenInsideWebViewListener listener);

    /**
     * 获取互动应用布局
     */
    IPLVInteractLayout getInteractLayout();

    /**
     * 获取积分打赏布局
     */
    PLVPointRewardLayout getRewardView();


    /**
     * 设置积分打赏配置回调监听
     */
    void setOnPointRewardListener(OnPointRewardListener listener);

    /**
     * 设置点击商品回调监听
     */
    void setOnClickProductListener(PLVInteractLayout2.OnClickProductListener listener);

    /**
     * 点击返回
     *
     * @return 返回true表示拦截事件
     */
    boolean onBackPress();

    /**
     * ActivityResult回调触发时调用
     */
    void onActivityResult(final int requestCode, final int resultCode, final Intent intent);

    /**
     * 销毁
     */
    void destroy();


}
