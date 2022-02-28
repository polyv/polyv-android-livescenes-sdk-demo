package com.easefun.polyv.livecloudclass.modules.pagemenu.previous;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.pagemenu.previous.adapter.PLVLCChapterAdapter;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVChapterAdapter;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVChapterView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;

/**
 * 回放章节Fragment
 */
public class PLVLCPlaybackChapterFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //章节列表view
    private PLVChapterView plvChapterView;
    //presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPresenter;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_chapter_fragment, container, false);
        initView();
        initData();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (plvChapterView != null) {
            plvChapterView.onDestroy();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">

    /**
     * 初始化，将presenter传进来注册plvChapterView
     * @param presenter presenter
     */
    public void init(IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter presenter) {
        this.previousPresenter = presenter;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        plvChapterView = findViewById(R.id.plv_chapter_view);
        PLVChapterView.Builder builder = new PLVChapterView.Builder(this.getContext());
        PLVLCChapterAdapter adapter = new PLVLCChapterAdapter();
        adapter.setOnViewActionListener(new PLVChapterAdapter.OnViewActionListener() {
            @Override
            public void changeVideoSeekClick(int timeStamp) {
                if (plvChapterView != null) {
                    plvChapterView.changePlaybackVideoSeek(timeStamp);
                }
            }
        });
        builder.setAdapter(adapter)
                .setRecyclerViewLayoutManager(new LinearLayoutManager(getContext()));

        plvChapterView.setParams(builder);
        if (previousPresenter != null) {
            previousPresenter.registerView(plvChapterView.getPreviousView());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化data">
    private void initData() {
        if (plvChapterView != null) {
            plvChapterView.requestChapterList();
        }
    }
    // </editor-fold>
}
