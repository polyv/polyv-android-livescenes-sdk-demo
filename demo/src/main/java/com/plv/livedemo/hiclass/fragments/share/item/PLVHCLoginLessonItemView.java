package com.plv.livedemo.hiclass.fragments.share.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.plv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.plv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.plv.livedemo.R;
import com.plv.livedemo.hiclass.fragments.share.vo.PLVHCLoginLessonVO;

/**
 * @author suhongtao
 */
public class PLVHCLoginLessonItemView extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private View rootView;
    private PLVRoundImageView plvhcLoginLessonItemImageIv;
    private TextView plvhcLoginLessonItemTitleTv;
    private TextView plvhcLoginLessonItemTimeTv;
    private TextView plvhcLoginCourseItemTitleTv;
    private View plvhcLoginLessonItemSeparateView;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVHCLoginLessonItemView(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCLoginLessonItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLoginLessonItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView(@Nullable AttributeSet attrs) {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_login_lesson_select_item, this);
        findView();
        parseStyle(attrs);
    }

    private void findView() {
        plvhcLoginLessonItemImageIv = (PLVRoundImageView) rootView.findViewById(R.id.plvhc_login_lesson_item_image_iv);
        plvhcLoginLessonItemTitleTv = (TextView) rootView.findViewById(R.id.plvhc_login_lesson_item_title_tv);
        plvhcLoginLessonItemTimeTv = (TextView) rootView.findViewById(R.id.plvhc_login_lesson_item_time_tv);
        plvhcLoginCourseItemTitleTv = (TextView) rootView.findViewById(R.id.plvhc_login_course_item_title_tv);
        plvhcLoginLessonItemSeparateView = (View) rootView.findViewById(R.id.plvhc_login_lesson_item_separate_view);
    }

    private void parseStyle(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVHCLoginLessonItemView);

        final boolean needSeparateView = typedArray.getBoolean(R.styleable.PLVHCLoginLessonItemView_plvNeedSeparateView, true);

        typedArray.recycle();

        if (needSeparateView) {
            plvhcLoginLessonItemSeparateView.setVisibility(VISIBLE);
        } else {
            plvhcLoginLessonItemSeparateView.setVisibility(GONE);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setData(PLVHCLoginLessonVO vo) {
        PLVImageLoader.getInstance().loadImage(getContext(),
                vo.getImageUrl(),
                R.drawable.plvhc_login_lesson_item_image_default,
                R.drawable.plvhc_login_lesson_item_image_default,
                plvhcLoginLessonItemImageIv);

        plvhcLoginLessonItemTitleTv.setText(vo.getLessonTitle());
        plvhcLoginLessonItemTimeTv.setText(vo.getLessonTime());
        plvhcLoginCourseItemTitleTv.setText(vo.getCourseTitle());
    }

    public void setIsLastItem(boolean last) {
        if (last) {
            plvhcLoginLessonItemSeparateView.setVisibility(GONE);
        } else {
            plvhcLoginLessonItemSeparateView.setVisibility(VISIBLE);
        }
    }

    // </editor-fold>

}
