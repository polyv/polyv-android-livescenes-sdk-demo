package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.R;

/**
 * 多模式recyclerView。继承自relativeLayout，内部封装了一个View+rv，用以支持一对多列表。
 * 可用于连麦模式切换，目前支持平铺模式、主讲模式
 *
 */
public class PLVMultiModeRecyclerViewLayout extends RelativeLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    /**
     * 平铺模式
     */
    public static final int MODE_TILED = 0;
    /**
     * 主讲模式，一对多
     */
    public static final int MODE_ONE_TO_MORE = 1;
    /**
     * 占位模式，嘉宾登录时主讲位置占位
     */
    public static final int MODE_PLACEHOLDER = 2;

    /**
     * 全屏模式，控制某个item全屏
     */
    public static final int MODE_ITEM_FULLSCREEN = 3;

    private FrameLayout frameLayout;
    private PLVNoInterceptTouchRecyclerView recyclerView;



    private int currentMode = MODE_TILED;

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造方法与重写方法">
    public PLVMultiModeRecyclerViewLayout(Context context) {
        this(context, null);
    }

    public PLVMultiModeRecyclerViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVMultiModeRecyclerViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void init( AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_multi_mode_recyclerview_layout, this);
        frameLayout = findViewById(R.id.plv_frame_layout);
        recyclerView = findViewById(R.id.plv_recycler_view);


    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">

    public PLVNoInterceptTouchRecyclerView getRecyclerView(){
        return recyclerView;
    }

    public ViewGroup getMainContainer(){
        return frameLayout;
    }

    public void addViewToMain(@NonNull View view, int width, int height){
        frameLayout.addView(view, width, height);
    }

    public void clearMainView(){
        frameLayout.removeAllViews();
    }

    public void setMainViewVisibility(int visibility){
        frameLayout.setVisibility(visibility);
    }

    public int getCurrentMode() {
        return currentMode;
    }

    /**
     * 切换模式
     * @param mode {@link #MODE_TILED} 、
     *             {@link #MODE_ONE_TO_MORE}
     */
    public void changeMode(int mode){
        currentMode = mode;
    }

    // </editor-fold >

}
