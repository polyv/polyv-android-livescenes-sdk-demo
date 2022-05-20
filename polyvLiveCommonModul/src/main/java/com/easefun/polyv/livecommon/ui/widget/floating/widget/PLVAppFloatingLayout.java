package com.easefun.polyv.livecommon.ui.widget.floating.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

//TODO 应用内悬浮窗，待实现
public class PLVAppFloatingLayout extends PLVAbsFloatingLayout {
    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVAppFloatingLayout(@NonNull Context context) {
        super(context);
    }

    public PLVAppFloatingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVAppFloatingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="接口实现">


    @Override
    public void setContentView(View view) {

    }

    @Override
    public View getContentView() {
        return null;
    }

    @Override
    public void show(Activity activity) {


    }

    @Override
    public void hide() {

    }

    @Override
    public boolean isShowing() {
        return false;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void updateFloatSize(int width, int height) {

    }

    @Override
    public void updateFloatLocation(int x, int y) {

    }

    @Override
    public Point getFloatLocation() {
        return null;
    }


    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="功能方法">

    // </editor-fold >
}
