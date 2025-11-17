package com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.enums.PLVRedPaperType;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVViewerNameMaskMapper;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.PLVRedPaperHistoryEvent;
import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;
import com.plv.socket.impl.PLVSocketManager;
import com.plv.socket.user.PLVSocketUserConstant;

/**
 * @author Hoshiiro
 */
public class PLVLCRedPaperPortViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> {

    private ImageView chatroomRedPaperAvatarIv;
    private LinearLayout chatroomRedPaperUserLl;
    private TextView chatroomRedPaperNickTv;
    private TextView chatroomRedPaperBlessingTv;
    private TextView chatroomRedPaperStatusTv;
    private TextView chatroomRedPaperTypeTv;
    private View chatroomRedPaperReceiveMask;

    @Nullable
    private LiveData<PLVRedPaperReceiveType> redPaperReceiveTypeLiveData;
    @Nullable
    private Observer<PLVRedPaperReceiveType> redPaperReceiveTypeObserver;

    public PLVLCRedPaperPortViewHolder(View itemView, PLVLCMessageAdapter adapter) {
        super(itemView, adapter);
        initView();
    }

    private void initView() {
        chatroomRedPaperAvatarIv = itemView.findViewById(R.id.plvlc_chatroom_red_paper_avatar_iv);
        chatroomRedPaperUserLl = itemView.findViewById(R.id.plvlc_chatroom_red_paper_user_ll);
        chatroomRedPaperNickTv = itemView.findViewById(R.id.plvlc_chatroom_red_paper_nick_tv);
        chatroomRedPaperBlessingTv = itemView.findViewById(R.id.plvlc_chatroom_red_paper_blessing_tv);
        chatroomRedPaperStatusTv = itemView.findViewById(R.id.plvlc_chatroom_red_paper_status_tv);
        chatroomRedPaperTypeTv = itemView.findViewById(R.id.plvlc_chatroom_red_paper_type_tv);
        chatroomRedPaperReceiveMask = itemView.findViewById(R.id.plvlc_chatroom_red_paper_receive_mask);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        bindAvatar();
        bindNickName();
        bindRedPaper();
    }

    private void bindAvatar() {
        if (avatar == null) {
            return;
        }
        int defaultAvatar;
        //根据用户类型使用不同的占位图
        if (PLVSocketUserConstant.USERTYPE_MANAGER.equals(userType) || PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
            defaultAvatar = R.drawable.plvlc_chatroom_ic_teacher;
        } else if (PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(userType)) {
            defaultAvatar = R.drawable.plvlc_chatroom_ic_assistant;
        } else if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
            defaultAvatar = R.drawable.plvlc_chatroom_ic_guest;
        } else {
            defaultAvatar = R.drawable.plvlc_chatroom_ic_viewer;
        }
        PLVImageLoader.getInstance().loadImageNoDiskCache(
                itemView.getContext(),
                avatar,
                defaultAvatar,
                defaultAvatar,
                chatroomRedPaperAvatarIv
        );
    }

    private void bindNickName() {
        if (nickName == null) {
            return;
        }
        final String loginUserId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return PLVSocketWrapper.getInstance().getLoginVO().getUserId();
            }
        });
        String showName = maskViewerName(nickName);
        if (loginUserId != null && loginUserId.equals(userId)) {
            showName = showName + PLVAppUtils.getString(R.string.plv_chat_me_2);
        }
        if (actor != null) {
            showName = actor + "-" + showName;
        }
        chatroomRedPaperNickTv.setText(showName);
        chatroomRedPaperNickTv.setTextColor(Color.parseColor(actor != null ? "#78A7ED" : "#ADADC0"));
    }

    private void bindRedPaper() {
        final PLVRedPaperEvent redPaperEvent;
        if (messageData instanceof PLVRedPaperEvent) {
            redPaperEvent = (PLVRedPaperEvent) messageData;
        } else if (messageData instanceof PLVRedPaperHistoryEvent) {
            redPaperEvent = ((PLVRedPaperHistoryEvent) messageData).asRedPaperEvent();
        } else {
            return;
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.callOnReceiveRedPaper(redPaperEvent);
            }
        });

        final PLVRedPaperType redPaperType = PLVRedPaperType.matchOrDefault(redPaperEvent.getType(), PLVRedPaperType.DEFAULT_RED_PAPER);
        chatroomRedPaperBlessingTv.setText(redPaperType.getDefaultBlessingMessage());
        chatroomRedPaperTypeTv.setText(redPaperType.getTypeName());

        observeRedPaperStatus(redPaperEvent);
    }

    private void observeRedPaperStatus(final PLVRedPaperEvent redPaperEvent) {
        if (redPaperReceiveTypeLiveData != null && redPaperReceiveTypeObserver != null) {
            redPaperReceiveTypeLiveData.removeObserver(redPaperReceiveTypeObserver);
        }

        redPaperReceiveTypeLiveData = redPaperEvent.getReceiveTypeLiveData();
        redPaperReceiveTypeLiveData.observe(
                (LifecycleOwner) itemView.getContext(),
                redPaperReceiveTypeObserver = new Observer<PLVRedPaperReceiveType>() {
                    @Override
                    public void onChanged(@Nullable PLVRedPaperReceiveType redPaperReceiveType) {
                        if (redPaperReceiveType == null) {
                            return;
                        }
                        switch (redPaperReceiveType) {
                            case AVAILABLE:
                                chatroomRedPaperStatusTv.setText(R.string.plv_red_paper_receive);
                                chatroomRedPaperReceiveMask.setVisibility(View.GONE);
                                break;
                            case AVAILABLE_CLICKED:
                                chatroomRedPaperStatusTv.setText(R.string.plv_red_paper_receive);
                                chatroomRedPaperReceiveMask.setVisibility(View.VISIBLE);
                                break;
                            case RECEIVED:
                                chatroomRedPaperStatusTv.setText(R.string.plv_red_paper_received);
                                chatroomRedPaperReceiveMask.setVisibility(View.VISIBLE);
                                break;
                            case RUN_OUT:
                                chatroomRedPaperStatusTv.setText(R.string.plv_red_paper_run_out);
                                chatroomRedPaperReceiveMask.setVisibility(View.VISIBLE);
                                break;
                            case EXPIRED:
                                chatroomRedPaperStatusTv.setText(R.string.plv_red_paper_expired);
                                chatroomRedPaperReceiveMask.setVisibility(View.VISIBLE);
                                break;
                            default:
                        }
                    }
                });
    }

    private String maskViewerName(String nickName) {
        PLVViewerNameMaskMapper mapper = PLVChannelFeatureManager.onChannel(PLVSocketManager.getInstance().getLoginRoomId())
                .getOrDefault(PLVChannelFeature.LIVE_VIEWER_NAME_MASK_TYPE, PLVViewerNameMaskMapper.KEEP_SOURCE);
        return mapper.invoke(
                nickName,
                userType,
                PLVSocketManager.getInstance().getLoginVO().getUserId().equals(userId)
        );
    }

}
