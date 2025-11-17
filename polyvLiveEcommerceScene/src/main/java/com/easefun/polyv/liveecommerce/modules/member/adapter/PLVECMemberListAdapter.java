package com.easefun.polyv.liveecommerce.modules.member.adapter;

import static com.plv.foundationsdk.utils.PLVFormatUtils.parseColor;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVViewerNameMaskMapper;
import com.plv.livescenes.model.PLVLiveViewerListVO;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Hoshiiro
 */
public class PLVECMemberListAdapter extends RecyclerView.Adapter<PLVECMemberListAdapter.MemberListViewHolder> {

    private final List<PLVLiveViewerListVO.Data.LiveViewer> liveViewerList = new ArrayList<>();

    public static final int VIEW_TYPE_CONTENT = 0;
    public static final int VIEW_TYPE_FOOTER = 1;

    @NonNull
    @Override
    public MemberListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CONTENT) {
            return new MemberListViewHolder.Content(parent);
        } else {
            return new MemberListViewHolder.Footer(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MemberListViewHolder holder, int position) {
        holder.bind(position < liveViewerList.size() ? liveViewerList.get(position) : null);
    }

    @Override
    public int getItemCount() {
        if (liveViewerList.size() > 20) {
            return liveViewerList.size() + 1/*footer*/;
        } else {
            return liveViewerList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < liveViewerList.size()) {
            return VIEW_TYPE_CONTENT;
        } else {
            return VIEW_TYPE_FOOTER;
        }
    }

    public void updateList(List<PLVLiveViewerListVO.Data.LiveViewer> liveViewerList) {
        this.liveViewerList.clear();
        this.liveViewerList.addAll(liveViewerList);
        notifyDataSetChanged();
    }

    public static abstract class MemberListViewHolder extends RecyclerView.ViewHolder {

        public MemberListViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(@Nullable PLVLiveViewerListVO.Data.LiveViewer liveViewer);

        public static class Content extends MemberListViewHolder {

            private CircleImageView memberListAvatarIv;
            private TextView memberListNickTv;
            private PLVRoundRectGradientTextView memberListActorTv;

            public Content(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_member_list_content_item, parent, false));
                memberListAvatarIv = itemView.findViewById(R.id.plvec_member_list_avatar_iv);
                memberListNickTv = itemView.findViewById(R.id.plvec_member_list_nick_tv);
                memberListActorTv = itemView.findViewById(R.id.plvec_member_list_actor_tv);
            }

            private static final Map<String, Integer> ACTOR_COLOR_MAP = mapOf(
                    pair(PLVSocketUserConstant.USERTYPE_TEACHER, PLVFormatUtils.parseColor("#F09343")),
                    pair(PLVSocketUserConstant.USERTYPE_ASSISTANT, PLVFormatUtils.parseColor("#598FE5")),
                    pair(PLVSocketUserConstant.USERTYPE_GUEST, PLVFormatUtils.parseColor("#EB6165")),
                    pair(PLVSocketUserConstant.USERTYPE_MANAGER, PLVFormatUtils.parseColor("#33BBC5"))
            );

            public void bind(@Nullable PLVLiveViewerListVO.Data.LiveViewer liveViewer) {
                if (liveViewer == null) {
                    return;
                }
                PLVImageLoader.getInstance().loadImage(liveViewer.getAvatarUrl(), memberListAvatarIv);
                String nick = maskViewerName(liveViewer);
                if (Boolean.TRUE.equals(liveViewer.isMe())) {
                    memberListNickTv.setText(nick + itemView.getContext().getString(R.string.plv_chat_me_2));
                } else {
                    memberListNickTv.setText(nick);
                }
                memberListNickTv.requestLayout();
                if (!TextUtils.isEmpty(liveViewer.getActor())) {
                    memberListActorTv.setVisibility(View.VISIBLE);
                    memberListActorTv.setText(liveViewer.getActor());
                    if (ACTOR_COLOR_MAP.containsKey(liveViewer.getUserType())) {
                        int color = ACTOR_COLOR_MAP.get(liveViewer.getUserType());
                        memberListActorTv.updateBackgroundColor(color);
                    } else {
                        memberListActorTv.updateBackgroundColor(parseColor("#F36E72"), parseColor("#F64F9F"));
                    }
                } else {
                    memberListActorTv.setVisibility(View.GONE);
                }
            }

            private String maskViewerName(PLVLiveViewerListVO.Data.LiveViewer liveViewer) {
                PLVViewerNameMaskMapper mapper = PLVChannelFeatureManager.onChannel(liveViewer.getChannelId())
                        .getOrDefault(PLVChannelFeature.LIVE_VIEWER_NAME_MASK_TYPE, PLVViewerNameMaskMapper.KEEP_SOURCE);
                return mapper.invoke(
                        liveViewer.getNick(),
                        liveViewer.getUserType(),
                        Boolean.TRUE.equals(liveViewer.isMe())
                );
            }

        }

        public static class Footer extends MemberListViewHolder {

            private TextView memberListFooterTv;

            public Footer(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_member_list_footer_item, parent, false));
                memberListFooterTv = itemView.findViewById(R.id.plvec_member_list_footer_tv);
            }

            @Override
            public void bind(@Nullable PLVLiveViewerListVO.Data.LiveViewer liveViewer) {

            }
        }

    }

}
