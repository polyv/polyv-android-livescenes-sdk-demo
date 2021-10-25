package com.easefun.polyv.livedemo.hiclass.fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.IntDef;
import androidx.annotation.MainThread;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author suhongtao
 */
public class PLVHCLoginFragmentManager {

    // <editor-fold defaultstate="collapsed" desc="变量">

    public static final int FRAG_ROLE_SELECT = 1;
    public static final int FRAG_TEACHER_LOGIN = 2;
    public static final int FRAG_TEACHER_COMPANY = 3;
    public static final int FRAG_STUDENT_LOGIN = 4;
    public static final int FRAG_STUDENT_VERIFY = 5;
    public static final int FRAG_LESSON_SELECT = 6;
    public static final int FRAG_MIN_ID = FRAG_ROLE_SELECT;
    public static final int FRAG_MAX_ID = FRAG_LESSON_SELECT;

    private final LinkedList<Integer> fragmentIdList = new LinkedList<>();
    private final MutableLiveData<Integer> liveDataFragmentChanged = new MutableLiveData<>();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="单例">

    private volatile static PLVHCLoginFragmentManager INSTANCE = null;

    private PLVHCLoginFragmentManager() {
    }

    public static PLVHCLoginFragmentManager getInstance() {
        if (INSTANCE == null) {
            synchronized (PLVHCLoginFragmentManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVHCLoginFragmentManager();
                }
            }
        }
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public List<Integer> getReadOnlyFragmentIdList() {
        return Collections.unmodifiableList(fragmentIdList);
    }

    @MainThread
    public void addLast(@FragmentRange int id) {
        fragmentIdList.addLast(id);
        liveDataFragmentChanged.postValue(fragmentIdList.size());
    }

    @MainThread
    public void removeLast() {
        if (fragmentIdList.size() <= 1) {
            // 保留1个Fragment（即角色选择页面）
            return;
        }
        fragmentIdList.removeLast();
        liveDataFragmentChanged.postValue(fragmentIdList.size());
    }

    @MainThread
    public void removeAfter(@FragmentRange int id) {
        while (fragmentIdList.size() > 1 && fragmentIdList.getLast() != id) {
            removeLast();
        }
    }

    public LiveData<Integer> observeOnFragmentListChanged() {
        return liveDataFragmentChanged;
    }

    public void destroy() {
        fragmentIdList.clear();
        INSTANCE = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="枚举注解定义">

    @IntDef({
            FRAG_ROLE_SELECT,
            FRAG_TEACHER_LOGIN,
            FRAG_TEACHER_COMPANY,
            FRAG_STUDENT_LOGIN,
            FRAG_STUDENT_VERIFY,
            FRAG_LESSON_SELECT
    })
    public @interface FragmentRange {
    }

    // </editor-fold>

}
