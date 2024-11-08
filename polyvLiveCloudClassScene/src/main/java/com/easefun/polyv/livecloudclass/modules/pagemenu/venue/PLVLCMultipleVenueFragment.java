package com.easefun.polyv.livecloudclass.modules.pagemenu.venue;

import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.venue.view.PLVMultiVenueView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 多会场fragment
 */
public class PLVLCMultipleVenueFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    // 多会场列表view
    private PLVMultiVenueView multiVenueView;
    private String channelId;
    private String mainVenueId;
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_multiple_venue_fragment, container, false);
        initView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">

    private void initView() {
        multiVenueView  = view.findViewById(R.id.plvlc_multi_venue_view);
        PLVMultiVenueView.Builder builder = new PLVMultiVenueView.Builder();
        PLVLCMultiVenueAdapter adapter = new PLVLCMultiVenueAdapter();
        adapter.setMainChannelId(mainVenueId);
        builder.setAdapter(adapter)
                .setRecyclerViewItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.bottom = ConvertUtils.dp2px(18);
                    }
                })
                .setRecyclerViewLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false))
                .setOnChangeVenueListener(new PLVMultiVenueView.PLVMultiVenueViewInterface.OnChangeVenueListener() {
                    @Override
                    public void onChangeVenue(String channelId, boolean isPlayback) {

                    }
                });
        multiVenueView.setParams(builder);
        multiVenueView.init(channelId, mainVenueId);
    }

    public void init(String channelId, String mainVenueId) {
        this.channelId = channelId;
        this.mainVenueId = mainVenueId;
    }

    //</editor-fold>



}
