package com.easefun.polyv.livedemo.hiclass.fragments.viewpager;

import static com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager.FRAG_LESSON_SELECT;
import static com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager.FRAG_ROLE_SELECT;
import static com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager.FRAG_STUDENT_LOGIN;
import static com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager.FRAG_STUDENT_VERIFY;
import static com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager.FRAG_TEACHER_COMPANY;
import static com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager.FRAG_TEACHER_LOGIN;
import static com.easefun.polyv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager.FragmentRange;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.share.PLVHCLessonSelectFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.share.PLVHCRoleSelectFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.student.PLVHCStudentLoginFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.student.PLVHCStudentVerifyFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.PLVHCTeacherCompanyFragment;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.PLVHCTeacherLoginFragment;
import com.plv.foundationsdk.log.PLVCommonLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * viewPagerAdapter
 */
public class PLVHCLoginViewPagerAdapter extends FragmentStatePagerAdapter {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final List<Integer> fragments = new ArrayList<>();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLoginViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">
    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        try {
            super.restoreState(state, loader);
        } catch (Exception e) {
            PLVCommonLog.exception(e);
            e.printStackTrace();
        }
    }

    @Override
    public Fragment getItem(int position) {
        return createFragment(fragments.get(position));
    }

    @Override
    public int getItemPosition(@NonNull @NotNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void updateFragments(List<Integer> fragmentIds) {
        this.fragments.clear();
        if (fragmentIds != null) {
            this.fragments.addAll(fragmentIds);
        }

        notifyDataSetChanged();
    }

    public List<Integer> getFragments() {
        return fragments;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部Fragment构建">

    @Nullable
    private static PLVBaseFragment createFragment(@FragmentRange int id) {
        switch (id) {
            case FRAG_ROLE_SELECT:
                return new PLVHCRoleSelectFragment();
            case FRAG_TEACHER_LOGIN:
                return new PLVHCTeacherLoginFragment();
            case FRAG_TEACHER_COMPANY:
                return new PLVHCTeacherCompanyFragment();
            case FRAG_STUDENT_LOGIN:
                return new PLVHCStudentLoginFragment();
            case FRAG_STUDENT_VERIFY:
                return new PLVHCStudentVerifyFragment();
            case FRAG_LESSON_SELECT:
                return new PLVHCLessonSelectFragment();
            default:
                return null;
        }
    }

    // </editor-fold>

}
