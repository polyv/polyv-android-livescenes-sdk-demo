package com.easefun.polyv.livedemo.hiclass.fragments.teacher.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livedemo.R;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.adapter.PLVHCAreaCodeAdapter;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.enums.PLVHCLoginAreaCodes;
import com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo.PLVHCLoginAreaCodeVO;

import org.jetbrains.annotations.NotNull;

/**
 * @author suhongtao
 */
public class PLVHCLoginAreaCodeSelectLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private View rootView;
    private TextView plvhcLoginAreaCodeLabelTv;
    private ImageView plvhcLoginAreaCodeBackIv;
    private RecyclerView plvhcLoginAreaCodeRv;

    private PLVHCAreaCodeAdapter areaCodeAdapter;

    private OnSelectAreaCodeListener onSelectAreaCodeListener;
    private OnVisibilityChangedListener onVisibilityChangedListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLoginAreaCodeSelectLayout(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public PLVHCLoginAreaCodeSelectLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLoginAreaCodeSelectLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_login_area_code_select_layout, this);
        findView();
        initRecyclerView();
        initOnClickBackListener();
    }

    private void findView() {
        plvhcLoginAreaCodeLabelTv = (TextView) rootView.findViewById(R.id.plvhc_login_area_code_label_tv);
        plvhcLoginAreaCodeBackIv = (ImageView) rootView.findViewById(R.id.plvhc_login_area_code_back_iv);
        plvhcLoginAreaCodeRv = (RecyclerView) rootView.findViewById(R.id.plvhc_login_area_code_rv);
    }

    private void initRecyclerView() {
        areaCodeAdapter = new PLVHCAreaCodeAdapter();
        areaCodeAdapter.setAreaCodeVOList(PLVHCLoginAreaCodes.AREA_CODE_LIST);
        areaCodeAdapter.setOnClickAreaCodeListener(new PLVHCAreaCodeAdapter.OnClickAreaCodeListener() {
            @Override
            public void onClick(PLVHCLoginAreaCodeVO vo) {
                if (onSelectAreaCodeListener != null) {
                    onSelectAreaCodeListener.onSelect(vo);
                }
                hide();
            }
        });

        plvhcLoginAreaCodeRv.setAdapter(areaCodeAdapter);
        plvhcLoginAreaCodeRv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void initOnClickBackListener() {
        plvhcLoginAreaCodeBackIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void show() {
        setVisibility(VISIBLE);
        if (onVisibilityChangedListener != null) {
            onVisibilityChangedListener.onChanged(true);
        }
    }

    public void hide() {
        setVisibility(GONE);
        if (onVisibilityChangedListener != null) {
            onVisibilityChangedListener.onChanged(false);
        }
    }

    public void setOnSelectAreaCodeListener(OnSelectAreaCodeListener listener) {
        this.onSelectAreaCodeListener = listener;
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener onVisibilityChangedListener) {
        this.onVisibilityChangedListener = onVisibilityChangedListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnSelectAreaCodeListener {
        void onSelect(PLVHCLoginAreaCodeVO vo);
    }

    public interface OnVisibilityChangedListener {
        void onChanged(boolean isVisible);
    }

    // </editor-fold>

}
