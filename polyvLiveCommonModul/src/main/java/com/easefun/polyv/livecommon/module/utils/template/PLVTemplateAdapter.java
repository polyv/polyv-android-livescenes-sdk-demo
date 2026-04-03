package com.easefun.polyv.livecommon.module.utils.template;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livescenes.model.template.PLVWaterTemplateVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 水印、虚拟背景模版列表适配器
 */
public class PLVTemplateAdapter extends RecyclerView.Adapter<PLVTemplateAdapter.TemplateViewHolder> {
    
    private Context context;
    private List<PLVWaterTemplateVO> templateList = new ArrayList<>();
    private int selectPosition = -1;
    private OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(PLVWaterTemplateVO item, int position);
    }

    public PLVTemplateAdapter(Context context) {
        this.context = context;
    }

    public void setOnTemplateClickListener(OnTemplateClickListener listener) {
        this.listener = listener;
    }

    public void setTemplateList(List<PLVWaterTemplateVO> templateList) {
        if (templateList != null) {
            this.templateList.clear();
            this.templateList.addAll(templateList);
            notifyDataSetChanged();
        }
    }

    public void addTemplate(PLVWaterTemplateVO item) {
        if (item != null) {
            this.templateList.add(item);
            notifyItemInserted(this.templateList.size() - 1);
        }
    }

    public PLVWaterTemplateVO getItem(int position) {
        if (position >= 0 && position < templateList.size()) {
            return templateList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.plv_template_item_layout, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        PLVWaterTemplateVO item = templateList.get(position);
        if (item == null) return;

        holder.tvTemplateName.setText(item.getName());

        PLVImageLoader.getInstance().loadImage(item.getCoverUrl(), holder.ivTemplateImage);

        holder.vgTemplate.setSelected(selectPosition == position);
        holder.ivTemplateSelImage.setVisibility(selectPosition == position ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onTemplateClick(item, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        ViewGroup vgTemplate;
        ImageView ivTemplateImage;
        ImageView ivTemplateSelImage;
        TextView tvTemplateName;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            vgTemplate = itemView.findViewById(R.id.vg_template);
            ivTemplateImage = itemView.findViewById(R.id.iv_template_image);
            ivTemplateSelImage = itemView.findViewById(R.id.iv_template_sel_image);
            tvTemplateName = itemView.findViewById(R.id.tv_template_name);
        }
    }
}
