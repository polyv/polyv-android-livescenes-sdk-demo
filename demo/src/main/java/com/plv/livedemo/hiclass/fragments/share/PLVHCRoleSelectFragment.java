package com.plv.livedemo.hiclass.fragments.share;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.plv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.plv.livedemo.R;
import com.plv.livedemo.hiclass.fragments.PLVHCAbsLoginFragment;
import com.plv.livedemo.hiclass.fragments.PLVHCLoginFragmentManager;

/**
 * 登录 - 角色选择页面
 *
 * @author suhongtao
 */
public class PLVHCRoleSelectFragment extends PLVHCAbsLoginFragment implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private ImageView plvhcLoginWelcomeTv;
    private TextView plvhcLoginRoleSelectTv;
    private PLVRoundRectLayout plvhcLoginRoleStudentLayout;
    private ImageView plvhcLoginRoleStudentIconIv;
    private TextView plvhcLoginRoleStudentLabelTv;
    private TextView plvhcLoginRoleStudentLabelDescTv;
    private ImageView plvhcLoginRoleStudentNavIv;
    private PLVRoundRectLayout plvhcLoginRoleTeacherLayout;
    private ImageView plvhcLoginRoleTeacherIconIv;
    private TextView plvhcLoginRoleTeacherLabelTv;
    private TextView plvhcLoginRoleTeacherLabelDescTv;
    private ImageView plvhcLoginRoleTeacherNavIv;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment生命周期重写">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.plvhc_login_role_select_fragment, null);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        findView();
    }

    private void findView() {
        plvhcLoginWelcomeTv = (ImageView) view.findViewById(R.id.plvhc_login_welcome_tv);
        plvhcLoginRoleSelectTv = (TextView) view.findViewById(R.id.plvhc_login_role_select_tv);
        plvhcLoginRoleStudentLayout = (PLVRoundRectLayout) view.findViewById(R.id.plvhc_login_role_student_layout);
        plvhcLoginRoleStudentIconIv = (ImageView) view.findViewById(R.id.plvhc_login_role_student_icon_iv);
        plvhcLoginRoleStudentLabelTv = (TextView) view.findViewById(R.id.plvhc_login_role_student_label_tv);
        plvhcLoginRoleStudentLabelDescTv = (TextView) view.findViewById(R.id.plvhc_login_role_student_label_desc_tv);
        plvhcLoginRoleStudentNavIv = (ImageView) view.findViewById(R.id.plvhc_login_role_student_nav_iv);
        plvhcLoginRoleTeacherLayout = (PLVRoundRectLayout) view.findViewById(R.id.plvhc_login_role_teacher_layout);
        plvhcLoginRoleTeacherIconIv = (ImageView) view.findViewById(R.id.plvhc_login_role_teacher_icon_iv);
        plvhcLoginRoleTeacherLabelTv = (TextView) view.findViewById(R.id.plvhc_login_role_teacher_label_tv);
        plvhcLoginRoleTeacherLabelDescTv = (TextView) view.findViewById(R.id.plvhc_login_role_teacher_label_desc_tv);
        plvhcLoginRoleTeacherNavIv = (ImageView) view.findViewById(R.id.plvhc_login_role_teacher_nav_iv);

        plvhcLoginRoleStudentLayout.setOnClickListener(this);
        plvhcLoginRoleTeacherLayout.setOnClickListener(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public int getFragmentId() {
        return PLVHCLoginFragmentManager.FRAG_ROLE_SELECT;
    }

    @Override
    public boolean onBackPressed() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).finish();
            return true;
        }
        return super.onBackPressed();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击处理">

    @Override
    public void onClick(View v) {
        if (v.getId() == plvhcLoginRoleStudentLayout.getId()) {
            PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_STUDENT_LOGIN);
        } else if (v.getId() == plvhcLoginRoleTeacherLayout.getId()) {
            PLVHCLoginFragmentManager.getInstance().addLast(PLVHCLoginFragmentManager.FRAG_TEACHER_LOGIN);
        }
    }

    // </editor-fold>

}
