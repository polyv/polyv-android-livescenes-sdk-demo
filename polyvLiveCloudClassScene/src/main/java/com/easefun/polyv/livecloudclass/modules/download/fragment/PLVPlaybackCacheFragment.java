package com.easefun.polyv.livecloudclass.modules.download.fragment;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.download.adapter.PLVPlaybackCacheAdapter;
import com.easefun.polyv.livecloudclass.modules.download.adapter.viewholder.PLVAbsPlaybackCacheViewHolder;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackCacheListViewModel;
import com.plv.foundationsdk.component.di.PLVDependManager;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheFragment extends Fragment {

    private String title;
    private boolean isShowDownloadingList;

    private View view;
    private RecyclerView playbackCacheRv;

    private PLVPlaybackCacheAdapter playbackCacheAdapter;

    public static PLVPlaybackCacheFragment create(
            final String title,
            final boolean isDownloading
    ) {
        final PLVPlaybackCacheFragment playbackCacheFragment = new PLVPlaybackCacheFragment();
        playbackCacheFragment.title = title;
        playbackCacheFragment.isShowDownloadingList = isDownloading;
        return playbackCacheFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.plv_playback_cache_fragment, null);

            initView();
        }
        return view;
    }

    private void initView() {
        findView();
        initPlaybackCacheRv();

        observePlaybackCacheList();
    }

    private void findView() {
        playbackCacheRv = view.findViewById(R.id.plv_playback_cache_rv);
    }

    private void initPlaybackCacheRv() {
        playbackCacheAdapter = new PLVPlaybackCacheAdapter(isShowDownloadingList ? PLVAbsPlaybackCacheViewHolder.TYPE_DOWNLOADING_CACHE : PLVAbsPlaybackCacheViewHolder.TYPE_DOWNLOADED_CACHE);
        playbackCacheRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        playbackCacheRv.setAdapter(playbackCacheAdapter);
    }

    private void observePlaybackCacheList() {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        final PLVPlaybackCacheListViewModel viewModel = PLVDependManager.getInstance().get(PLVPlaybackCacheListViewModel.class);
        if (isShowDownloadingList) {
            viewModel.getDownloadingListLiveData().observe((LifecycleOwner) context, new Observer<List<PLVPlaybackCacheVideoVO>>() {
                @Override
                public void onChanged(@Nullable List<PLVPlaybackCacheVideoVO> vos) {
                    if (vos == null) {
                        return;
                    }
                    playbackCacheAdapter.updateData(vos);
                }
            });
        } else {
            viewModel.getDownloadedListLiveData().observe((LifecycleOwner) context, new Observer<List<PLVPlaybackCacheVideoVO>>() {
                @Override
                public void onChanged(@Nullable List<PLVPlaybackCacheVideoVO> vos) {
                    if (vos == null) {
                        return;
                    }
                    playbackCacheAdapter.updateData(vos);
                }
            });
        }
    }

    public String getTitle() {
        return title;
    }


}
