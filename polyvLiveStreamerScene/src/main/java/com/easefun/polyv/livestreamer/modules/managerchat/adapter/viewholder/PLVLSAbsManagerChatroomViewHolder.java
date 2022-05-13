package com.easefun.polyv.livestreamer.modules.managerchat.adapter.viewholder;

import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectSpan;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.Map;

import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

/**
 * @author Hoshiiro
 */
public abstract class PLVLSAbsManagerChatroomViewHolder extends RecyclerView.ViewHolder {

    // <editor-fold defaultstate="collapsed" desc="对外 - ViewHolder工厂">

    public static class Factory {

        public static final int VIEW_TYPE_RECEIVE_MSG = 1;
        public static final int VIEW_TYPE_SEND_MSG = 2;

        public Factory() {
        }

        public PLVLSAbsManagerChatroomViewHolder create(ViewGroup viewGroup, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_SEND_MSG:
                    return new PLVLSManagerChatroomSendMsgViewHolder(PLVLSManagerChatroomSendMsgViewHolder.createItemView(viewGroup));
                case VIEW_TYPE_RECEIVE_MSG:
                default:
                    return new PLVLSManagerChatroomReceiveMsgViewHolder(PLVLSManagerChatroomReceiveMsgViewHolder.createItemView(viewGroup));
            }
        }

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    protected PLVLSAbsManagerChatroomViewHolder(View itemView) {
        super(itemView);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="抽象方法">

    protected abstract ImageView getAvatarIv();

    protected abstract TextView getNameTv();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void bindData(PLVChatEventWrapVO vo) {
        CommonViewBinder.setAvatar(getAvatarIv(), vo);
        CommonViewBinder.setNickName(getNameTv(), vo);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="共用的视图内容绑定工具类">

    private static class CommonViewBinder {

        private static final Map<String, Integer> USER_TYPE_COLOR_MAP = mapOf(
                pair(PLVSocketUserConstant.USERTYPE_TEACHER, Color.parseColor("#FFC161")),
                pair(PLVSocketUserConstant.USERTYPE_GUEST, Color.parseColor("#4399FF")),
                pair(PLVSocketUserConstant.USERTYPE_MANAGER, Color.parseColor("#EB6165")),
                pair(PLVSocketUserConstant.USERTYPE_ASSISTANT, Color.parseColor("#33BBC5"))
        );

        private static void setAvatar(ImageView avatarIv, PLVChatEventWrapVO vo) {
            PLVImageLoader.getInstance().loadImage(vo.getUser().getPic(), avatarIv);
        }

        private static void setNickName(TextView nameTv, PLVChatEventWrapVO vo) {
            final PLVSpannableStringBuilder spannableStringBuilder = new PLVSpannableStringBuilder();
            if (USER_TYPE_COLOR_MAP.containsKey(vo.getUser().getUserType()) && vo.getUser().getActor() != null) {
                spannableStringBuilder.appendExclude(
                        vo.getUser().getActor(),
                        new PLVRoundRectSpan()
                                .textColor(Color.parseColor("#313540"))
                                .textSize(10)
                                .backgroundColor(USER_TYPE_COLOR_MAP.get(vo.getUser().getUserType()))
                                .paddingLeft(3)
                                .paddingRight(3)
                                .marginRight(4)
                                .radius(2)
                );
            }
            spannableStringBuilder.append(vo.getUser().getNick());
            nameTv.setText(spannableStringBuilder);
        }
    }

    // </editor-fold>

}
