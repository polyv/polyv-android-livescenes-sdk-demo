package com.easefun.polyv.livecloudclass.modules.pagemenu.previous;

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
import com.easefun.polyv.livecloudclass.modules.pagemenu.previous.adapter.PLVLCPreviousAdapter;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousAdapter;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 回放往期Fragment
 */
public class PLVLCPlaybackPreviousFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //回放视频view
    private PLVPreviousView plvPreviousView;

    //presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPresenter;

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_previous_fragment, container, false);
        initView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (plvPreviousView != null) {
            plvPreviousView.onDestroy();
        }
    }

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        plvPreviousView = findViewById(R.id.plvlc_previous_view);
        PLVPreviousView.Builder builder = new PLVPreviousView.Builder(getContext());
        PLVLCPreviousAdapter plvlcPreviousAdapter = new PLVLCPreviousAdapter();
        plvlcPreviousAdapter.setOnViewActionListener(new PLVPreviousAdapter.OnViewActionListener() {
            @Override
            public void changeVideoVidClick(String vid) {
                plvPreviousView.changePlaybackVideoVid(vid);
            }
        });
        builder.setAdapter(plvlcPreviousAdapter)
                .setRecyclerViewItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.left = ConvertUtils.dp2px(16);
                        outRect.top = ConvertUtils.dp2px(16);
                    }
                })
                .setRecyclerViewLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        plvPreviousView.setParams(builder);

        if (previousPresenter != null) {
            previousPresenter.registerView(plvPreviousView.getPreviousView());
        }

    }

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="提供方法">

    /**
     * 初始化-获取presenter来注册
     *
     * @param presenter presenter
     */
    public void init(IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter presenter) {
        this.previousPresenter = presenter;
    }
    //</editor-fold>


}
