package com.easefun.polyv.livedemo.hiclass.fragments.teacher.adapter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.notEmptyOrDefault;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livedemo.R;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo.PLVHCLoginCompanyVO;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suhongtao
 */
public class PLVHCTeacherCompanyAdapter extends RecyclerView.Adapter<PLVHCTeacherCompanyAdapter.PLVHCTeacherCompanyViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private List<PLVHCLoginCompanyVO> companies;

    private PLVHCLoginCompanyVO selectedCompany = null;

    private OnItemClickedListener onItemClickedListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCTeacherCompanyAdapter() {
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adapter方法重写">

    @NonNull
    @Override
    public PLVHCTeacherCompanyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvhc_login_teacher_company_item, viewGroup, false);
        return new PLVHCTeacherCompanyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull final PLVHCTeacherCompanyViewHolder plvhcTeacherCompanyViewHolder, int i) {
        final PLVHCLoginCompanyVO vo = companies.get(i);
        plvhcTeacherCompanyViewHolder.bind(vo);
        plvhcTeacherCompanyViewHolder.setSelected(vo.equals(selectedCompany));
        plvhcTeacherCompanyViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedListener != null) {
                    onItemClickedListener.onClick(vo);
                }
                selectedCompany = vo;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (companies == null) {
            return 0;
        }
        return companies.size();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setCompanies(List<PLVHCLoginCompanyVO> companies) {
        if (this.companies == null) {
            this.companies = new ArrayList<>();
        } else {
            this.companies.clear();
        }

        if (companies != null) {
            this.companies.addAll(companies);
        }

        notifyDataSetChanged();
    }

    public void setSelectedCompany(PLVHCLoginCompanyVO selectedCompany) {
        this.selectedCompany = selectedCompany;
        notifyDataSetChanged();
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ViewHolder定义">

    public static class PLVHCTeacherCompanyViewHolder extends RecyclerView.ViewHolder {

        private TextView plvhcLoginTeacherCompanyTv;
        private ImageView plvhcLoginTeacherCompanyIv;

        public PLVHCTeacherCompanyViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(PLVHCLoginCompanyVO vo) {
            findView();
            plvhcLoginTeacherCompanyTv.setText(notEmptyOrDefault(vo.getCompanyName(), "未知公司名称"));
        }

        private void findView() {
            plvhcLoginTeacherCompanyTv = (TextView) itemView.findViewById(R.id.plvhc_login_teacher_company_tv);
            plvhcLoginTeacherCompanyIv = (ImageView) itemView.findViewById(R.id.plvhc_login_teacher_company_iv);
        }

        public void setSelected(boolean isSelected) {
            itemView.setSelected(isSelected);
            plvhcLoginTeacherCompanyIv.setSelected(isSelected);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnItemClickedListener {
        void onClick(PLVHCLoginCompanyVO vo);
    }

    // </editor-fold>

}
