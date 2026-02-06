package com.easefun.polyv.livecloudclass.modules.pagemenu.member;

import static com.plv.foundationsdk.utils.PLVTimeUnit.minutes;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.pagemenu.member.adapter.PLVLCMemberListAdapter;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.plv.foundationsdk.rx.PLVTimer;
import com.plv.livescenes.model.PLVLiveViewerListVO;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVLCMemberListPortFragment extends PLVBaseFragment {

    private TextView pageMenuMemberListRuleTv;
    private RecyclerView pageMenuMemberListRv;

    @Nullable
    private PLVLCMemberListRuleDescriptionLayout ruleDescriptionLayout;

    private final PLVLCMemberListAdapter memberListAdapter = new PLVLCMemberListAdapter();

    private boolean isVisibleToUser = false;
    @Nullable
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    private Disposable updateViewerListDisposable = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_page_menu_member_list_port_fragment, null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pageMenuMemberListRuleTv = findViewById(R.id.plvlc_page_menu_member_list_rule_tv);
        pageMenuMemberListRv = findViewById(R.id.plvlc_page_menu_member_list_rv);
        ruleDescriptionLayout = new PLVLCMemberListRuleDescriptionLayout(view.getContext());

        pageMenuMemberListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        pageMenuMemberListRv.setAdapter(memberListAdapter);

        pageMenuMemberListRuleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ruleDescriptionLayout != null) {
                    ruleDescriptionLayout.show();
                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isVisibleToUser && updateViewerListDisposable == null) {
            startUpdateViewerList();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ruleDescriptionLayout = null;
        stopUpdateViewerList();
    }

    public void init(IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        this.chatroomPresenter = chatroomPresenter;
        if (chatroomPresenter != null) {
            observeLiveViewerListUpdated(chatroomPresenter);
        }
    }

    private void observeLiveViewerListUpdated(@NonNull IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        chatroomPresenter.registerView(new PLVAbsChatroomView() {
            @Override
            public void onLiveViewerListUpdate(List<PLVLiveViewerListVO.Data.LiveViewer> liveViewerList) {
                memberListAdapter.updateList(liveViewerList);
            }
        });
    }

    private void startUpdateViewerList() {
        stopUpdateViewerList();
        updateViewerListDisposable = PLVTimer.timer((int) minutes(1).toMillis(), new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (!isVisibleToUser) {
                    stopUpdateViewerList();
                    return;
                }
                if (chatroomPresenter != null) {
                    chatroomPresenter.requestUpdateLiveViewerList();
                }
            }
        });
    }

    private void stopUpdateViewerList() {
        if (updateViewerListDisposable != null) {
            updateViewerListDisposable.dispose();
        }
        updateViewerListDisposable = null;
    }

}
