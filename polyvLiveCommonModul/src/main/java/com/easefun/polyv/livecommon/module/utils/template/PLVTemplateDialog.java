package com.easefun.polyv.livecommon.module.utils.template;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livescenes.model.template.PLVWaterTemplateVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 水印、虚拟背景模版弹窗
 */
public class PLVTemplateDialog extends Dialog {

    private Context context;
    private RecyclerView recyclerView;
    private PLVTemplateAdapter adapter;
    private TextView tvTitle;
    private ImageView ivClose;
    private View vBackground;

    private List<PLVWaterTemplateVO> templateList = new ArrayList<>();
    private OnTemplateSelectListener selectListener;

    public interface OnTemplateSelectListener {
        void onTemplateSelect(PLVWaterTemplateVO item, int position);

        void onDialogDismiss();
    }

    public PLVTemplateDialog(@NonNull Context context) {
        super(context, R.style.PLVTheme_Dialog_BottomSheet);
        this.context = context;
        init();
    }

    public PLVTemplateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        init();
    }

    private void init() {
        // 设置弹窗属性
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getWindow().setDimAmount(0f);
            // 设置窗口参数
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            getWindow().setAttributes(lp);
        }

        // 设置布局
        View view = LayoutInflater.from(context).inflate(R.layout.plv_template_dialog_layout, null);
        setContentView(view);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        tvTitle = view.findViewById(R.id.tv_title);
        ivClose = view.findViewById(R.id.iv_close);
        vBackground = view.findViewById(R.id.v_background);
    }

    private void setupRecyclerView() {
        adapter = new PLVTemplateAdapter(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        adapter.setOnTemplateClickListener(new PLVTemplateAdapter.OnTemplateClickListener() {
            @Override
            public void onTemplateClick(PLVWaterTemplateVO item, int position) {
                if (selectListener != null) {
                    selectListener.onTemplateSelect(item, position);
                }
                dismiss();
            }
        });

        // 设置数据
        if (!templateList.isEmpty()) {
            adapter.setTemplateList(templateList);
        }
    }

    private void setupClickListeners() {
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        vBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 设置监听器
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (selectListener != null) {
                    selectListener.onDialogDismiss();
                }
            }
        });
    }

    /**
     * 设置模版列表
     */
    public void setTemplateList(List<PLVWaterTemplateVO> templateList) {
        if (templateList != null) {
            this.templateList.clear();
            this.templateList.addAll(templateList);
            if (adapter != null) {
                adapter.setTemplateList(this.templateList);
            }
        }
    }

    /**
     * 添加单个模版
     */
    public void addTemplate(PLVWaterTemplateVO item) {
        if (item != null) {
            this.templateList.add(item);
            if (adapter != null) {
                adapter.addTemplate(item);
            }
        }
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }
    }

    /**
     * 设置模版选择监听器
     */
    public void setOnTemplateSelectListener(OnTemplateSelectListener listener) {
        this.selectListener = listener;
    }

    /**
     * 清空模版列表
     */
    public void clearTemplates() {
        this.templateList.clear();
        if (adapter != null) {
            adapter.setTemplateList(new ArrayList<PLVWaterTemplateVO>());
        }
    }

    /**
     * 获取当前模版列表
     */
    public List<PLVWaterTemplateVO> getTemplateList() {
        return new ArrayList<>(templateList);
    }

    /**
     * 显示弹窗
     */
    @Override
    public void show() {
        super.show();

        // 添加进入动画
        animateIn();
    }

    /**
     * 进入动画
     */
    private void animateIn() {
        View contentView = findViewById(R.id.content_view);
        if (contentView != null) {
            contentView.setTranslationY(contentView.getHeight());
            contentView.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }

        if (vBackground != null) {
            vBackground.setBackgroundColor(Color.parseColor("#30000000"));
            vBackground.setAlpha(0f);
            vBackground.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    /**
     * 退出动画
     */
    private void animateOut() {
        View contentView = findViewById(R.id.content_view);
        if (contentView != null) {
            contentView.animate()
                    .translationY(contentView.getHeight())
                    .setDuration(300)
                    .start();
        }

        if (vBackground != null) {
            vBackground.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .start();
        }
    }

    @Override
    public void dismiss() {
        animateOut();
        // 延迟关闭，等待动画完成
        if (getWindow() != null) {
            getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PLVTemplateDialog.super.dismiss();
                }
            }, 300);
        }
    }

    /**
     * 创建并显示弹窗的便捷方法
     */
    public static PLVTemplateDialog show(Context context, List<PLVWaterTemplateVO> templates,
                                         OnTemplateSelectListener listener) {
        PLVTemplateDialog dialog = new PLVTemplateDialog(context);
        dialog.setTemplateList(templates);
        dialog.setOnTemplateSelectListener(listener);
        dialog.show();
        return dialog;
    }
}
