package com.easefun.polyv.livecloudclass.modules.pagemenu.chapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.pagemenu.chapter.adapter.PLVLCChapterAdapter;
import com.easefun.polyv.livecommon.module.modules.chapter.view.PLVChapterAdapter;
import com.easefun.polyv.livecommon.module.modules.chapter.view.PLVChapterView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;

/**
 * 回放章节Fragment
 */
public class PLVLCPlaybackChapterFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //章节列表view
    private PLVChapterView plvChapterView;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_chapter_fragment, container, false);
        initView();
        return view;
    }

    // </editor-fold>

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
    }

    // </editor-fold>
}
