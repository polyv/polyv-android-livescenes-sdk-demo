package com.easefun.polyv.livestreamer.modules.managerchat.adapter.viewholder;

import android.arch.lifecycle.ViewModelStoreOwner;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVManagerChatViewModel;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.imageScan.PLVChatImageViewerFragment;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVCircleProgressView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.chatroom.widget.PLVLSChatMsgTipsWindow;
import com.plv.foundationsdk.component.viewmodel.PLVViewModels;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.chatroom.PLVLocalMessage;
import com.plv.livescenes.chatroom.send.img.PLVSendLocalImgEvent;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgContent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.socket.event.history.PLVSpeakHistoryEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

/**
 * @author Hoshiiro
 */
public class PLVLSManagerChatroomSendMsgViewHolder extends PLVLSAbsManagerChatroomViewHolder {

    // <editor-fold defaultstate="collapsed" desc="对外 - 创建布局">

    public static View createItemView(ViewGroup viewGroup) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvls_manager_chatroom_msg_send_item, viewGroup, false);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final SendMsgViewBinder viewBinder = new SendMsgViewBinder();

    private ImageView managerChatroomSendMsgAvatarIv;
    private TextView managerChatroomSendMsgNameTv;
    private LinearLayout managerChatroomSendMsgListLl;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSManagerChatroomSendMsgViewHolder(View itemView) {
        super(itemView);
        findView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void findView() {
        managerChatroomSendMsgAvatarIv = itemView.findViewById(R.id.plvls_manager_chatroom_send_msg_avatar_iv);
        managerChatroomSendMsgNameTv = itemView.findViewById(R.id.plvls_manager_chatroom_send_msg_name_tv);
        managerChatroomSendMsgListLl = itemView.findViewById(R.id.plvls_manager_chatroom_send_msg_list_ll);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    protected ImageView getAvatarIv() {
        return managerChatroomSendMsgAvatarIv;
    }

    @Override
    protected TextView getNameTv() {
        return managerChatroomSendMsgNameTv;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    @Override
    public void bindData(PLVChatEventWrapVO vo) {
        super.bindData(vo);
        viewBinder.setSendMessage(managerChatroomSendMsgListLl, vo);
    }

    // </editor-fold>

    /**
     * 通用的发送类型消息视图绑定
     */
    private static class SendMsgViewBinder {

        @Px
        private static final int SPEAK_EMOJI_SIZE = ConvertUtils.dp2px(12);

        private static final Map<Class<? extends PLVBaseEvent>, SendMsgViewBinder> binders = new HashMap<Class<? extends PLVBaseEvent>, SendMsgViewBinder>() {{
            put(PLVSpeakEvent.class, new SendSpeakMsgViewBinder());
            put(PLVSpeakHistoryEvent.class, new SendSpeakHistoryViewBinder());
            put(PLVLocalMessage.class, new SendLocalSpeakMsgViewBinder());
            put(PolyvLocalMessage.class, new SendLocalSpeakMsgViewBinder());
            put(PLVChatImgEvent.class, new SendChatImgViewBinder());
            put(PLVChatImgHistoryEvent.class, new SendChatImgHistoryViewBinder());
            put(PLVSendLocalImgEvent.class, new SendLocalChatImgViewBinder());
            put(PolyvSendLocalImgEvent.class, new SendLocalChatImgViewBinder());
        }};

        public void setSendMessage(LinearLayout sendMsgLl, PLVChatEventWrapVO vo) {
            int viewGroupChildIndex = 0;
            for (PLVBaseEvent event : vo.getEvents()) {
                if (findBinderForEvent(event) == null) {
                    continue;
                }
                final View view = createViewOrReuse(sendMsgLl, viewGroupChildIndex);
                bindSendMessage(view, event);
                viewGroupChildIndex++;
            }
            removeRestView(sendMsgLl, viewGroupChildIndex);
        }

        /**
         * 分发到子类，需要由子类重写消息绑定具体实现
         */
        protected void bindSendMessage(View view, PLVBaseEvent event) {
            final SendMsgViewBinder binder = findBinderForEvent(event);
            if (binder != null) {
                binder.bindSendMessage(view, event);
            }
        }

        protected void hideAllViews(View view) {
            final List<Integer> viewIds = listOf(
                    R.id.plvls_manager_chatroom_send_fail_iv,
                    R.id.plvls_manager_chatroom_send_text_tv,
                    R.id.plvls_manager_chatroom_send_image_iv,
                    R.id.plvls_manager_chatroom_send_image_loading_view
            );

            for (final int viewId : viewIds) {
                view.findViewById(viewId).setVisibility(View.GONE);
            }
        }

        private static View createViewOrReuse(LinearLayout ll, int index) {
            View view = ll.getChildAt(index);
            if (view == null) {
                view = createView(ll.getContext());
                ll.addView(view, index);
            }
            return view;
        }

        private static View createView(Context context) {
            return LayoutInflater.from(context).inflate(R.layout.plvls_manager_chatroom_msg_send_content_item, null);
        }

        private static void removeRestView(ViewGroup viewGroup, int startIndex) {
            for (int i = viewGroup.getChildCount() - 1; i >= startIndex; i--) {
                viewGroup.removeViewAt(i);
            }
        }

        @Nullable
        private static SendMsgViewBinder findBinderForEvent(PLVBaseEvent event) {
            Class eventClass = event.getClass();
            while (eventClass != null && eventClass != Object.class) {
                if (binders.containsKey(eventClass)) {
                    return binders.get(eventClass);
                }
                eventClass = eventClass.getSuperclass();
            }
            return null;
        }

    }

    /**
     * 发送文本类型即时消息绑定
     */
    private static class SendSpeakMsgViewBinder extends SendMsgViewBinder {

        @Override
        protected void bindSendMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVSpeakEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVSpeakEvent speakEvent = (PLVSpeakEvent) event;

            final TextView textView = view.findViewById(R.id.plvls_manager_chatroom_send_text_tv);
            final CharSequence parsedMessage = PLVTextFaceLoader.messageToSpan(speakEvent.getValues().get(0), SendMsgViewBinder.SPEAK_EMOJI_SIZE, view.getContext());
            textView.setText(parsedMessage);
            textView.setVisibility(View.VISIBLE);

            PLVLSManagerChatImageViewBinderHelper.bindCopyTextOnLongClickListener(textView, textView.getText().toString());
        }
    }

    /**
     * 发送文本类型历史消息绑定
     */
    private static class SendSpeakHistoryViewBinder extends SendMsgViewBinder {

        @Override
        protected void bindSendMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVSpeakHistoryEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVSpeakHistoryEvent speakHistoryEvent = (PLVSpeakHistoryEvent) event;

            final TextView textView = view.findViewById(R.id.plvls_manager_chatroom_send_text_tv);
            final CharSequence parsedMessage = PLVTextFaceLoader.messageToSpan(speakHistoryEvent.getContent(), SendMsgViewBinder.SPEAK_EMOJI_SIZE, view.getContext());
            textView.setText(parsedMessage);
            textView.setVisibility(View.VISIBLE);

            PLVLSManagerChatImageViewBinderHelper.bindCopyTextOnLongClickListener(textView, textView.getText().toString());
        }
    }

    /**
     * 发送文本类型本地消息绑定
     */
    private static class SendLocalSpeakMsgViewBinder extends SendMsgViewBinder {

        @Override
        protected void bindSendMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVLocalMessage)) {
                return;
            }
            hideAllViews(view);

            final PLVLocalMessage localMessage = (PLVLocalMessage) event;

            final TextView textView = view.findViewById(R.id.plvls_manager_chatroom_send_text_tv);
            final CharSequence parsedMessage = PLVTextFaceLoader.messageToSpan(localMessage.getSpeakMessage(), SendMsgViewBinder.SPEAK_EMOJI_SIZE, view.getContext());
            textView.setText(parsedMessage);
            textView.setVisibility(View.VISIBLE);

            PLVLSManagerChatImageViewBinderHelper.bindCopyTextOnLongClickListener(textView, textView.getText().toString());

            final boolean isSendFailByProhibited = localMessage.getProhibitedWord() != null;
            if (isSendFailByProhibited) {
                bindSendFail(view, localMessage);
            }
        }

        private void bindSendFail(final View view, PLVLocalMessage localMessage) {
            final ImageView sendFailIv = view.findViewById(R.id.plvls_manager_chatroom_send_fail_iv);
            final String sendFailHint = format("{}:{}", localMessage.getProhibitedWord().getMessage(), localMessage.getProhibitedWord().getValue());
            sendFailIv.setVisibility(View.VISIBLE);
            sendFailIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    view.getLocationInWindow(location);
                    new PLVLSChatMsgTipsWindow(v).show(v, sendFailHint, location[0], location[0] + view.getWidth(), location[1] + view.getHeight());
                }
            });
        }
    }

    /**
     * 发送图片类型即时消息绑定
     */
    private static class SendChatImgViewBinder extends SendMsgViewBinder {

        @Override
        protected void bindSendMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVChatImgEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVChatImgEvent chatImgEvent = (PLVChatImgEvent) event;
            final PLVChatImgContent content = chatImgEvent.getValues().get(0);

            final PLVRoundImageView imageView = view.findViewById(R.id.plvls_manager_chatroom_send_image_iv);
            final PLVCircleProgressView loadingView = view.findViewById(R.id.plvls_manager_chatroom_send_image_loading_view);

            PLVLSManagerChatImageViewBinderHelper.fitChatImgWH((int) content.getSize().getWidth(), (int) content.getSize().getHeight(), imageView, 80, 0);
            PLVLSManagerChatImageViewBinderHelper.loadNetworkImage(imageView, loadingView, content.getUploadImgUrl());

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PLVBaseViewData<PLVBaseEvent> viewData = new PLVBaseViewData<PLVBaseEvent>(chatImgEvent, PLVBaseViewData.ITEMTYPE_UNDEFINED);
                    final List<PLVBaseViewData> baseViewDataList = PLVSugarUtil.<PLVBaseViewData>listOf(viewData);
                    PLVChatImageViewerFragment.show((AppCompatActivity) imageView.getContext(), baseViewDataList, viewData, Window.ID_ANDROID_CONTENT);
                }
            });

            imageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 发送图片类型历史消息绑定
     */
    private static class SendChatImgHistoryViewBinder extends SendMsgViewBinder {

        @Override
        protected void bindSendMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVChatImgHistoryEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVChatImgHistoryEvent chatImgHistoryEvent = (PLVChatImgHistoryEvent) event;
            final PLVChatImgContent content = chatImgHistoryEvent.getContent();

            final PLVRoundImageView imageView = view.findViewById(R.id.plvls_manager_chatroom_send_image_iv);
            final PLVCircleProgressView loadingView = view.findViewById(R.id.plvls_manager_chatroom_send_image_loading_view);

            PLVLSManagerChatImageViewBinderHelper.fitChatImgWH((int) content.getSize().getWidth(), (int) content.getSize().getHeight(), imageView, 80, 0);
            PLVLSManagerChatImageViewBinderHelper.loadNetworkImage(imageView, loadingView, content.getUploadImgUrl());

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PLVBaseViewData<PLVBaseEvent> viewData = new PLVBaseViewData<PLVBaseEvent>(chatImgHistoryEvent, PLVBaseViewData.ITEMTYPE_UNDEFINED);
                    final List<PLVBaseViewData> baseViewDataList = PLVSugarUtil.<PLVBaseViewData>listOf(viewData);
                    PLVChatImageViewerFragment.show((AppCompatActivity) imageView.getContext(), baseViewDataList, viewData, Window.ID_ANDROID_CONTENT);
                }
            });

            imageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 发送图片类型本地消息绑定
     */
    private static class SendLocalChatImgViewBinder extends SendMsgViewBinder {
        @Override
        protected void bindSendMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVSendLocalImgEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVSendLocalImgEvent localImgEvent = (PLVSendLocalImgEvent) event;
            final String imgPath = localImgEvent.getImageFilePath();

            final PLVRoundImageView imageView = view.findViewById(R.id.plvls_manager_chatroom_send_image_iv);

            PLVLSManagerChatImageViewBinderHelper.fitChatImgWH(localImgEvent.getWidth(), localImgEvent.getHeight(), imageView, 80, 0);
            PLVImageLoader.getInstance().loadImage(imageView.getContext(), imgPath, imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PLVBaseViewData<PLVBaseEvent> viewData = new PLVBaseViewData<PLVBaseEvent>(localImgEvent, PLVBaseViewData.ITEMTYPE_UNDEFINED);
                    final List<PLVBaseViewData> baseViewDataList = PLVSugarUtil.<PLVBaseViewData>listOf(viewData);
                    PLVChatImageViewerFragment.show((AppCompatActivity) imageView.getContext(), baseViewDataList, viewData, Window.ID_ANDROID_CONTENT);
                }
            });

            imageView.setVisibility(View.VISIBLE);

            final boolean showLoadingView = localImgEvent.getSendStatus() == PLVSendLocalImgEvent.SENDSTATUS_SENDING;
            if (showLoadingView) {
                bindLoading(view, localImgEvent);
            }

            final boolean isSendFail = localImgEvent.getSendStatus() == PLVSendLocalImgEvent.SENDSTATUS_FAIL;
            if (isSendFail) {
                bindSendFail(view, localImgEvent);
            }
        }

        private void bindLoading(View view, PLVSendLocalImgEvent localImgEvent) {
            final PLVCircleProgressView loadingView = view.findViewById(R.id.plvls_manager_chatroom_send_image_loading_view);
            loadingView.setVisibility(View.VISIBLE);
            loadingView.setProgress(localImgEvent.getSendProgress());
        }

        private void bindSendFail(View view, final PLVSendLocalImgEvent localImgEvent) {
            final ImageView sendFailIv = view.findViewById(R.id.plvls_manager_chatroom_send_fail_iv);
            sendFailIv.setVisibility(View.VISIBLE);
            sendFailIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PolyvSendLocalImgEvent event = new PolyvSendLocalImgEvent();
                    event.setId(localImgEvent.getId());
                    event.setTime(localImgEvent.getTime());
                    event.setIsManagerChatMsg(localImgEvent.isManagerChatMsg());
                    event.setImageFilePath(localImgEvent.getImageFilePath());
                    event.setWidth(localImgEvent.getWidth());
                    event.setHeight(localImgEvent.getHeight());
                    PLVViewModels.on((ViewModelStoreOwner) v.getContext()).get(PLVManagerChatViewModel.class).sendImageMessage(event);
                }
            });
        }
    }

}
