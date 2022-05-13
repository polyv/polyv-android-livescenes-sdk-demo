package com.easefun.polyv.livestreamer.modules.managerchat.adapter.viewholder;

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

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.imageScan.PLVChatImageViewerFragment;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVCircleProgressView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.utils.PLVSugarUtil;
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

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

/**
 * @author Hoshiiro
 */
public class PLVLSManagerChatroomReceiveMsgViewHolder extends PLVLSAbsManagerChatroomViewHolder {

    // <editor-fold defaultstate="collapsed" desc="对外 - 创建布局">

    public static View createItemView(ViewGroup viewGroup) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvls_manager_chatroom_msg_receive_item, viewGroup, false);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final ReceiveMsgViewBinder viewBinder = new ReceiveMsgViewBinder();

    private LinearLayout managerChatroomReceiveMsgUserInfoLl;
    private ImageView managerChatroomReceiveMsgAvatarIv;
    private TextView managerChatroomReceiveMsgNameTv;
    private LinearLayout managerChatroomReceiveMsgListLl;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSManagerChatroomReceiveMsgViewHolder(View itemView) {
        super(itemView);
        findView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void findView() {
        managerChatroomReceiveMsgUserInfoLl = itemView.findViewById(R.id.plvls_manager_chatroom_receive_msg_user_info_ll);
        managerChatroomReceiveMsgAvatarIv = itemView.findViewById(R.id.plvls_manager_chatroom_receive_msg_avatar_iv);
        managerChatroomReceiveMsgNameTv = itemView.findViewById(R.id.plvls_manager_chatroom_receive_msg_name_tv);
        managerChatroomReceiveMsgListLl = itemView.findViewById(R.id.plvls_manager_chatroom_receive_msg_list_ll);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    protected ImageView getAvatarIv() {
        return managerChatroomReceiveMsgAvatarIv;
    }

    @Override
    protected TextView getNameTv() {
        return managerChatroomReceiveMsgNameTv;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    @Override
    public void bindData(PLVChatEventWrapVO vo) {
        super.bindData(vo);
        viewBinder.setReceiveMessage(managerChatroomReceiveMsgListLl, vo);
    }

    // </editor-fold>

    /**
     * 通用的接收类型消息视图绑定
     */
    private static class ReceiveMsgViewBinder {

        @Px
        private static final int SPEAK_EMOJI_SIZE = ConvertUtils.dp2px(12);

        private static final Map<Class<? extends PLVBaseEvent>, ReceiveMsgViewBinder> binders = new HashMap<Class<? extends PLVBaseEvent>, ReceiveMsgViewBinder>() {{
            put(PLVSpeakEvent.class, new ReceiveSpeakMsgViewBinder());
            put(PLVSpeakHistoryEvent.class, new ReceiveSpeakHistoryViewBinder());
            put(PLVChatImgEvent.class, new ReceiveChatImgViewBinder());
            put(PLVChatImgHistoryEvent.class, new ReceiveChatImgHistoryViewBinder());
        }};

        public void setReceiveMessage(LinearLayout receiveMsgLl, PLVChatEventWrapVO vo) {
            int viewGroupChildIndex = 0;
            for (PLVBaseEvent event : vo.getEvents()) {
                if (findBinderForEvent(event) == null) {
                    continue;
                }
                final View view = createViewOrReuse(receiveMsgLl, viewGroupChildIndex);
                bindReceiveMessage(view, event);
                viewGroupChildIndex++;
            }
            removeRestView(receiveMsgLl, viewGroupChildIndex);
        }

        /**
         * 分发到子类，需要由子类重写消息绑定具体实现
         */
        protected void bindReceiveMessage(View view, PLVBaseEvent event) {
            final ReceiveMsgViewBinder binder = findBinderForEvent(event);
            if (binder != null) {
                binder.bindReceiveMessage(view, event);
            }
        }

        protected void hideAllViews(View view) {
            final List<Integer> viewIds = listOf(
                    R.id.plvls_manager_chatroom_receive_text_tv,
                    R.id.plvls_manager_chatroom_receive_image_iv,
                    R.id.plvls_manager_chatroom_receive_image_loading_view
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
            return LayoutInflater.from(context).inflate(R.layout.plvls_manager_chatroom_msg_receive_content_item, null);
        }

        private static void removeRestView(ViewGroup viewGroup, int startIndex) {
            for (int i = viewGroup.getChildCount() - 1; i >= startIndex; i--) {
                viewGroup.removeViewAt(i);
            }
        }

        @Nullable
        private static ReceiveMsgViewBinder findBinderForEvent(PLVBaseEvent event) {
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
     * 接收文本类型即时消息绑定
     */
    private static class ReceiveSpeakMsgViewBinder extends ReceiveMsgViewBinder {

        @Override
        protected void bindReceiveMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVSpeakEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVSpeakEvent speakEvent = (PLVSpeakEvent) event;

            final TextView textView = view.findViewById(R.id.plvls_manager_chatroom_receive_text_tv);
            final CharSequence parsedMessage = PLVTextFaceLoader.messageToSpan(speakEvent.getValues().get(0), ReceiveMsgViewBinder.SPEAK_EMOJI_SIZE, view.getContext());
            textView.setText(parsedMessage);
            textView.setVisibility(View.VISIBLE);

            PLVLSManagerChatImageViewBinderHelper.bindCopyTextOnLongClickListener(textView, textView.getText().toString());
        }

    }

    /**
     * 接收文本类型历史消息绑定
     */
    private static class ReceiveSpeakHistoryViewBinder extends ReceiveMsgViewBinder {

        @Override
        protected void bindReceiveMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVSpeakHistoryEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVSpeakHistoryEvent speakHistoryEvent = (PLVSpeakHistoryEvent) event;

            final TextView textView = view.findViewById(R.id.plvls_manager_chatroom_receive_text_tv);
            final CharSequence parsedMessage = PLVTextFaceLoader.messageToSpan(speakHistoryEvent.getContent(), ReceiveMsgViewBinder.SPEAK_EMOJI_SIZE, view.getContext());
            textView.setText(parsedMessage);
            textView.setVisibility(View.VISIBLE);

            PLVLSManagerChatImageViewBinderHelper.bindCopyTextOnLongClickListener(textView, textView.getText().toString());
        }

    }

    /**
     * 接收图片类型即时消息绑定
     */
    private static class ReceiveChatImgViewBinder extends ReceiveMsgViewBinder {

        @Override
        protected void bindReceiveMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVChatImgEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVChatImgEvent chatImgEvent = (PLVChatImgEvent) event;
            final PLVChatImgContent content = chatImgEvent.getValues().get(0);

            final PLVRoundImageView imageView = view.findViewById(R.id.plvls_manager_chatroom_receive_image_iv);
            final PLVCircleProgressView loadingView = view.findViewById(R.id.plvls_manager_chatroom_receive_image_loading_view);

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
     * 接收图片类型历史消息绑定
     */
    private static class ReceiveChatImgHistoryViewBinder extends ReceiveMsgViewBinder {

        @Override
        protected void bindReceiveMessage(View view, PLVBaseEvent event) {
            if (!(event instanceof PLVChatImgHistoryEvent)) {
                return;
            }
            hideAllViews(view);

            final PLVChatImgHistoryEvent chatImgHistoryEvent = (PLVChatImgHistoryEvent) event;
            final PLVChatImgContent content = chatImgHistoryEvent.getContent();

            final PLVRoundImageView imageView = view.findViewById(R.id.plvls_manager_chatroom_receive_image_iv);
            final PLVCircleProgressView loadingView = view.findViewById(R.id.plvls_manager_chatroom_receive_image_loading_view);

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

}
