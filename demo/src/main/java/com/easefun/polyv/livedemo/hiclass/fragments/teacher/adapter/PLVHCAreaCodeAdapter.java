package com.easefun.polyv.livedemo.hiclass.fragments.teacher.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livedemo.R;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo.PLVHCLoginAreaCodeVO;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suhongtao
 */
public class PLVHCAreaCodeAdapter extends RecyclerView.Adapter<PLVHCAreaCodeAdapter.PLVHCAreaCodeViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private List<PLVHCLoginAreaCodeVO> areaCodeVOList;

    private OnClickAreaCodeListener onClickAreaCodeListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCAreaCodeAdapter() {

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adapter方法重写">

    @NonNull
    @Override
    public PLVHCAreaCodeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvhc_login_area_code_select_item, viewGroup, false);
        return new PLVHCAreaCodeViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PLVHCAreaCodeViewHolder plvhcAreaCodeViewHolder, int i) {
        final PLVHCLoginAreaCodeVO vo = areaCodeVOList.get(i);
        plvhcAreaCodeViewHolder.bind(vo);
        plvhcAreaCodeViewHolder.setShowSeparateView(i != getItemCount() - 1);
        plvhcAreaCodeViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickAreaCodeListener != null) {
                    onClickAreaCodeListener.onClick(vo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (this.areaCodeVOList == null) {
            return 0;
        } else {
            return this.areaCodeVOList.size();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setAreaCodeVOList(List<PLVHCLoginAreaCodeVO> areaCodeVOList) {
        if (this.areaCodeVOList == null) {
            this.areaCodeVOList = new ArrayList<>();
        } else {
            this.areaCodeVOList.clear();
        }

        if (areaCodeVOList != null) {
            this.areaCodeVOList.addAll(areaCodeVOList);
        }

        notifyDataSetChanged();
    }

    public void setOnClickAreaCodeListener(OnClickAreaCodeListener onClickAreaCodeListener) {
        this.onClickAreaCodeListener = onClickAreaCodeListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ViewHolder定义">

    public static class PLVHCAreaCodeViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout plvhcLoginAreaCodeLocateLl;
        private TextView plvhcLoginAreaLocateTv;
        private TextView plvhcLoginAreaCodeTv;
        private View plvhcLoginAreaCodeSeparateView;

        public PLVHCAreaCodeViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(PLVHCLoginAreaCodeVO vo) {
            findView();
            this.plvhcLoginAreaLocateTv.setText(vo.getName());
            this.plvhcLoginAreaCodeTv.setText(vo.getCode());
        }

        public void setShowSeparateView(boolean show) {
            if (show) {
                plvhcLoginAreaCodeSeparateView.setVisibility(View.VISIBLE);
            } else {
                plvhcLoginAreaCodeSeparateView.setVisibility(View.GONE);
            }
        }

        private void findView() {
            plvhcLoginAreaCodeLocateLl = (LinearLayout) itemView.findViewById(R.id.plvhc_login_area_code_locate_ll);
            plvhcLoginAreaLocateTv = (TextView) itemView.findViewById(R.id.plvhc_login_area_locate_tv);
            plvhcLoginAreaCodeTv = (TextView) itemView.findViewById(R.id.plvhc_login_area_code_tv);
            plvhcLoginAreaCodeSeparateView = (View) itemView.findViewById(R.id.plvhc_login_area_code_separate_view);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnClickAreaCodeListener {
        void onClick(PLVHCLoginAreaCodeVO vo);
    }

    // </editor-fold>

}
