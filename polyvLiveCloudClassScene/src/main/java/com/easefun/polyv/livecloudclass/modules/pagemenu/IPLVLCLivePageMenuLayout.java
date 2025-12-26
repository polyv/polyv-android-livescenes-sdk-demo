package com.easefun.polyv.livecloudclass.modules.pagemenu;

import androidx.annotation.Nullable;

import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCChatCommonMessageList;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.PLVLotteryManager;
import com.easefun.polyv.livecommon.module.modules.player.live.enums.PLVLiveStateEnum;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.video.subtitle.vo.PLVLiveSubtitleTranslation;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.interact.PLVShowProductDetailEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;

import java.util.List;

/**
 * 直播页面菜单布局的接口
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVLCLivePageMenuLayout {

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - 定义 页面菜单布局中 外部可以直接调用的方法">
    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 获取横竖屏共用的聊天信息列表
     *
     * @return 聊天信息列表
     */
    PLVLCChatCommonMessageList getChatCommonMessageList();

    /**
     * 获取聊天室presenter
     *
     * @return 聊天室presenter
     */
    IPLVChatroomContract.IChatroomPresenter getChatroomPresenter();

    /**
     * 获取卡片推送管理器
     */
    PLVCardPushManager getCardPushManager();

    /**
     * 获取无条件抽奖挂件管理器
     * @return
     */
    PLVLotteryManager getLotteryManager();

    /**
     * 获取聊天回放管理器
     *
     * @return 聊天回放manager
     */
    IPLVChatPlaybackManager getChatPlaybackManager();

    /**
     * 获取回放的presenter
     *
     * @return 回放presenter
     */
    IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter getPreviousPresenter();

    /**
     * 设置view交互事件监听器
     *
     * @param listener
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 添加观看热度监听器
     *
     * @param listener 监听器
     */
    void addOnViewerCountListener(IPLVOnDataChangedListener<Long> listener);

    /**
     * 添加在线人数监听器
     * @param listener 在线人数
     */
    void addOnViewOnlineCountData(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 回放视频准备完成
     *
     * @param sessionId sessionId
     * @param channelId 频道号
     * @param fileId    文件Id
     */
    void onPlaybackVideoPrepared(String sessionId, String channelId, String fileId);

    /**
     * 回放视频准备完成通知AI看
     *
     * @param playDataId   播放数据Id
     * @param playDataType 播放数据类型
     */
    void onAISummaryPrepared(String playDataId, String playDataType);

    /**
     * 回放视频seek完成
     *
     * @param time 时间，单位：毫秒
     */
    void onPlaybackVideoSeekComplete(int time);

    /**
     * 是否是聊天回放tab
     *
     * @return true：聊天回放，false：在线聊天
     */
    boolean isChatPlaybackEnabled();

    /**
     * 更新直播状态
     */
    void updateLiveStatus(PLVLiveStateEnum liveStateEnum);

    @Nullable
    PLVChatQuoteVO getChatQuoteContent();

    void onCloseChatQuote();

    /**
     * 实时字幕更新
     */
    void onSubtitleUpdate(List<PLVLiveSubtitleTranslation> subtitles);

    /**
     * 是否拦截返回事件，拦截的情况有：
     * 1.当前显示的tab是推广外链，并且其webView可以返回
     * 2.当前显示的tab是聊天，并且表情等布局是显示状态
     * 3.当前显示的tab是提问，并且表情等布局是显示状态
     * 4.当前显示的tab是介绍，并且其webView可以返回
     * 5.当前显示的tab是自定义图文菜单，并且器webView可以返回
     * 6.当前是在查看聊天信息列表里的图片大图
     *
     * @return true：拦截，false：不拦截
     */
    boolean onBackPressed();

    /**
     * 销毁
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器 - 定义 页面菜单布局中UI控件 触发的交互事件的回调方法">
    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 显示公告
         */
        void onShowBulletinAction();

        /**
         * 发送弹幕动作，在本地发送的聊天信息/接收到发言信息时需要发送弹幕
         *
         * @param message 弹幕信息
         */
        void onSendDanmuAction(CharSequence message);

        /**
         * 切换往期视频的动作
         * @param vid 回放视频的vid
         * @param fileId 回放视频的fileId
         */
        void onChangeVideoVidAction(String vid, String fileId);

        /**
         * 跳转进度条的动作
         *
         * @param progress 需要切换到进度的位置 单位是秒
         */
        void onSeekToAction(int progress);

        /**
         * 获取视频当前播放时间
         *
         * @return 时间，单位：毫秒
         */
        int getVideoCurrentPosition();

        /**
         * 聊天tab准备完成
         *
         * @param isChatPlaybackEnabled 聊天回放是否可用
         * @param isDisplayEnabled      是否显示tab
         */
        void onChatTabPrepared(boolean isChatPlaybackEnabled, boolean isDisplayEnabled);

        /**
         * 显示积分打赏弹窗
         */
        void onShowRewardAction();

        /**
         * 是否显示特效
         */
        void onShowEffectAction(boolean isShow);

        /**
         * 显示问卷
         */
        void onShowQuestionnaire();

        /**
         * 点击了聊天室更多-动态功能按钮
         *
         * @param event 功能event
         */
        void onClickChatMoreDynamicFunction(String event);

        /**
         * 回调 引用回复消息
         */
        void onReplyMessage(PLVChatQuoteVO chatQuoteVO);

        /**
         * 回调 拆开红包
         */
        void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent);

        /**
         * 回调 展示职位详情
         */
        void onShowJobDetail(PLVShowJobDetailEvent param);

        /**
         * 回调 展示产品详情
         */
        void onShowProductDetail(PLVShowProductDetailEvent param);

        /**
         * 展示用于跳转微信复制的二维码
         */
        void onShowOpenLink();

        /**
         * 截屏
         */
        void onScreenshot();

        /**
         * 修改实时字幕翻译语言
         */
        void onSetSubtitleTranslateLanguage(String language);
    }
    // </editor-fold>
}
