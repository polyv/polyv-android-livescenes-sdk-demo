package com.easefun.polyv.livecommon.ui.window;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.R;

/**
 * 空fragment
 */
public class PLVEmptyFragment extends PLVBaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plv_horizontal_linear_layout, null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (viewActionListener != null) {
            viewActionListener.onViewCreated(view);
        }
    }

    private ViewActionListener viewActionListener;

    public void setViewActionListener(ViewActionListener listener) {
        this.viewActionListener = listener;
    }

    public interface ViewActionListener {
        void onViewCreated(View view);
    }
}
