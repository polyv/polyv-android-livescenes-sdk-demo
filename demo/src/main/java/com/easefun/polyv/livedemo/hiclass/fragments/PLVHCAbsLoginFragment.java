package com.easefun.polyv.livedemo.hiclass.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livedemo.hiclass.IPLVLoginHiClassActivity;

/**
 * @author suhongtao
 */
public abstract class PLVHCAbsLoginFragment extends PLVBaseFragment {

    // <editor-fold defaultstate="collapsed" desc="Fragment生命周期重写">

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerOnBackPressedToActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterOnBackPressedToActivity();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="子类应重写的抽象方法">

    public abstract int getFragmentId();

    public boolean onBackPressed() {
        return false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理 - 向 Activity 注册">

    private void registerOnBackPressedToActivity() {
        if (getContext() instanceof IPLVLoginHiClassActivity) {
            ((IPLVLoginHiClassActivity) getContext()).registerFragment(this);
        }
    }

    private void unregisterOnBackPressedToActivity() {
        if (getContext() instanceof IPLVLoginHiClassActivity) {
            ((IPLVLoginHiClassActivity) getContext()).unregisterFragment(this);
        }
    }

    // </editor-fold>

}
