package com.easefun.polyv.livedemo.hiclass.fragments.share.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livedemo.hiclass.fragments.share.item.PLVHCLoginLessonItemView;
import com.easefun.polyv.livedemo.hiclass.fragments.share.vo.PLVHCLoginLessonVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suhongtao
 */
public class PLVHCLoginLessonAdapter extends RecyclerView.Adapter<PLVHCLoginLessonAdapter.PLVHCLoginLessonViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private List<PLVHCLoginLessonVO> vos;

    private OnItemClickListener onItemClickListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLoginLessonAdapter() {

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adatper方法重写">

    @NonNull
    @Override
    public PLVHCLoginLessonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PLVHCLoginLessonViewHolder(new PLVHCLoginLessonItemView(viewGroup.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull PLVHCLoginLessonViewHolder plvhcLoginLessonViewHolder, int i) {
        final PLVHCLoginLessonVO loginLessonVO = vos.get(i);
        plvhcLoginLessonViewHolder.bind(loginLessonVO, i == getItemCount() - 1);
        plvhcLoginLessonViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(loginLessonVO);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (vos == null) {
            return 0;
        }
        return vos.size();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setLessons(List<PLVHCLoginLessonVO> lessons) {
        if (vos == null) {
            vos = new ArrayList<>();
        } else {
            vos.clear();
        }
        if (lessons != null) {
            vos.addAll(lessons);
        }

        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ViewHolder定义">

    public static class PLVHCLoginLessonViewHolder extends RecyclerView.ViewHolder {

        private PLVHCLoginLessonItemView view;

        public PLVHCLoginLessonViewHolder(PLVHCLoginLessonItemView itemView) {
            super(itemView);
            this.view = itemView;
        }

        public void bind(PLVHCLoginLessonVO vo, boolean isLast) {
            view.setData(vo);
            view.setIsLastItem(isLast);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnItemClickListener {
        void onClick(PLVHCLoginLessonVO loginLessonVO);
    }

    // </editor-fold>

}
