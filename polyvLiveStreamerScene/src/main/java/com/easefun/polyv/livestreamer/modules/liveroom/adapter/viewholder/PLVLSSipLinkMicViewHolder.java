package com.easefun.polyv.livestreamer.modules.liveroom.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.model.enums.PLVSipLinkMicState;
import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;
import com.easefun.polyv.livestreamer.R;

/**
 * @author Hoshiiro
 */
public class PLVLSSipLinkMicViewHolder extends RecyclerView.ViewHolder {

    public static class Factory {
        public static PLVLSSipLinkMicViewHolder create(@NonNull ViewGroup viewGroup, int itemType) {
            final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvls_live_room_member_sip_linkmic_list_item, viewGroup, false);
            return new PLVLSSipLinkMicViewHolder(view);
        }
    }

    private TextView memberSipLinkmicItemAvatarTv;
    private TextView memberSipLinkmicItemNameTv;
    private ImageView memberSipLinkmicItemAudioVolumeIv;

    public PLVLSSipLinkMicViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        memberSipLinkmicItemAvatarTv = itemView.findViewById(R.id.plvls_member_sip_linkmic_item_avatar_tv);
        memberSipLinkmicItemNameTv = itemView.findViewById(R.id.plvls_member_sip_linkmic_item_name_tv);
        memberSipLinkmicItemAudioVolumeIv = itemView.findViewById(R.id.plvls_member_sip_linkmic_item_audio_volume_iv);
    }

    public void bind(PLVSipLinkMicViewerVO viewerVO) {
        memberSipLinkmicItemAvatarTv.setText(viewerVO.getAvatarString());
        memberSipLinkmicItemNameTv.setText(viewerVO.getNameString());

        bindAudioVolume(viewerVO);
    }

    private void bindAudioVolume(PLVSipLinkMicViewerVO viewerVO) {
        final boolean showAudioIcon = viewerVO.getSipLinkMicStatus() == PLVSipLinkMicState.CONNECTED;
        if (!showAudioIcon) {
            memberSipLinkmicItemAudioVolumeIv.setVisibility(View.GONE);
            return;
        }
        memberSipLinkmicItemAudioVolumeIv.setVisibility(View.VISIBLE);
        final boolean muted = viewerVO.getAudioMuted() != null && viewerVO.getAudioMuted();
        if (muted) {
            memberSipLinkmicItemAudioVolumeIv.setVisibility(View.VISIBLE);
            memberSipLinkmicItemAudioVolumeIv.setImageResource(R.drawable.plvls_streamer_mic_close);
        } else {
            memberSipLinkmicItemAudioVolumeIv.setVisibility(View.GONE);
            memberSipLinkmicItemAudioVolumeIv.setImageResource(R.drawable.plvls_streamer_mic_open);
        }
    }

}
