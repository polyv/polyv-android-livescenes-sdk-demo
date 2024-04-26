package com.easefun.polyv.livestreamer.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVAppUtils.getString;
import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVSipLinkMicViewModel;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingInListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingOutListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicConnectedListState;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.liveroom.adapter.viewholder.PLVLSSipLinkMicViewHolder;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVLSSipLinkMicMemberLayout extends FrameLayout {

    private final PLVSipLinkMicViewModel sipLinkMicViewModel = PLVDependManager.getInstance().get(PLVSipLinkMicViewModel.class);

    private ImageView liveRoomSipListCallInIcon;
    private TextView liveRoomSipListCallInTv;
    private LinearLayout liveRoomSipListCallInLl;
    private ImageView liveRoomSipListCallOutIcon;
    private TextView liveRoomSipListCallOutTv;
    private LinearLayout liveRoomSipListCallOutLl;
    private ImageView liveRoomSipListConnectedIcon;
    private TextView liveRoomSipListConnectedTv;
    private LinearLayout liveRoomSipListConnectedLl;

    @NonNull
    private List<PLVSipLinkMicViewerVO> callingInList = Collections.emptyList();
    @NonNull
    private List<PLVSipLinkMicViewerVO> callingOutList = Collections.emptyList();
    @NonNull
    private List<PLVSipLinkMicViewerVO> connectedList = Collections.emptyList();

    public PLVLSSipLinkMicMemberLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLSSipLinkMicMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLSSipLinkMicMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_member_sip_linkmic_list_layout, this);

        findView();

        observeSipLinkMicList();

        sipLinkMicViewModel.requestSipChannelInfo();
    }

    private void findView() {
        liveRoomSipListCallInIcon = findViewById(R.id.plvls_live_room_sip_list_call_in_icon);
        liveRoomSipListCallInTv = findViewById(R.id.plvls_live_room_sip_list_call_in_tv);
        liveRoomSipListCallInLl = findViewById(R.id.plvls_live_room_sip_list_call_in_ll);
        liveRoomSipListCallOutIcon = findViewById(R.id.plvls_live_room_sip_list_call_out_icon);
        liveRoomSipListCallOutTv = findViewById(R.id.plvls_live_room_sip_list_call_out_tv);
        liveRoomSipListCallOutLl = findViewById(R.id.plvls_live_room_sip_list_call_out_ll);
        liveRoomSipListConnectedIcon = findViewById(R.id.plvls_live_room_sip_list_connected_icon);
        liveRoomSipListConnectedTv = findViewById(R.id.plvls_live_room_sip_list_connected_tv);
        liveRoomSipListConnectedLl = findViewById(R.id.plvls_live_room_sip_list_connected_ll);
    }

    private void observeSipLinkMicList() {
        sipLinkMicViewModel.getCallingInListStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicCallingInListState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicCallingInListState sipLinkMicCallingInListState) {
                if (sipLinkMicCallingInListState == null) {
                    return;
                }
                callingInList = sipLinkMicCallingInListState.callingInViewerList;
                onCallInListUpdated();
            }
        });

        sipLinkMicViewModel.getCallingOutListStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicCallingOutListState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicCallingOutListState sipLinkMicCallingOutListState) {
                if (sipLinkMicCallingOutListState == null) {
                    return;
                }
                callingOutList = sipLinkMicCallingOutListState.callingOutViewerList;
                onCallOutListUpdated();
            }
        });

        sipLinkMicViewModel.getConnectedListStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicConnectedListState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicConnectedListState sipLinkMicConnectedListState) {
                if (sipLinkMicConnectedListState == null) {
                    return;
                }
                connectedList = sipLinkMicConnectedListState.connectedViewerList;
                onConnectedListUpdated();
            }
        });
    }

    private void onCallInListUpdated() {
        liveRoomSipListCallInTv.setText(format(getString(R.string.plvls_live_member_sip_calling_in_count_text), callingInList.size()));
        liveRoomSipListCallInLl.setVisibility(callingInList.isEmpty() ? GONE : VISIBLE);
        liveRoomSipListCallInLl.removeAllViews();
        foreach(callingInList, new PLVSugarUtil.Consumer<PLVSipLinkMicViewerVO>() {
            @Override
            public void accept(PLVSipLinkMicViewerVO viewerVO) {
                addSipLinkMicItem(liveRoomSipListCallInLl, viewerVO);
            }
        });
    }

    private void onCallOutListUpdated() {
        liveRoomSipListCallOutTv.setText(format(getString(R.string.plvls_live_member_sip_calling_out_count_text), callingOutList.size()));
        liveRoomSipListCallOutLl.setVisibility(callingOutList.isEmpty() ? GONE : VISIBLE);
        liveRoomSipListCallOutLl.removeAllViews();
        foreach(callingOutList, new PLVSugarUtil.Consumer<PLVSipLinkMicViewerVO>() {
            @Override
            public void accept(PLVSipLinkMicViewerVO viewerVO) {
                addSipLinkMicItem(liveRoomSipListCallOutLl, viewerVO);
            }
        });
    }

    private void onConnectedListUpdated() {
        liveRoomSipListConnectedTv.setText(format(getString(R.string.plvls_live_member_sip_connected_count_text), connectedList.size()));
        liveRoomSipListConnectedLl.setVisibility(connectedList.isEmpty() ? GONE : VISIBLE);
        liveRoomSipListConnectedLl.removeAllViews();
        foreach(connectedList, new PLVSugarUtil.Consumer<PLVSipLinkMicViewerVO>() {
            @Override
            public void accept(PLVSipLinkMicViewerVO viewerVO) {
                addSipLinkMicItem(liveRoomSipListConnectedLl, viewerVO);
            }
        });
    }

    private void addSipLinkMicItem(LinearLayout linearLayout, PLVSipLinkMicViewerVO vo) {
        if (vo == null) {
            return;
        }
        final PLVLSSipLinkMicViewHolder viewHolder = PLVLSSipLinkMicViewHolder.Factory.create(linearLayout, -1);
        viewHolder.bind(vo);
        linearLayout.addView(viewHolder.itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
