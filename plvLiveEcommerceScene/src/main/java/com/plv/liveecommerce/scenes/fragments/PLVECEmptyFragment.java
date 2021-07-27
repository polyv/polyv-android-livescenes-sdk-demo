package com.plv.liveecommerce.scenes.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.plv.livecommon.ui.window.PLVBaseFragment;
import com.plv.liveecommerce.R;

/**
 * 空白页
 */
public class PLVECEmptyFragment extends PLVBaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_empty_page_fragment, null);
        return view;
    }
}
