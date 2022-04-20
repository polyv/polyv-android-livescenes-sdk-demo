package com.easefun.polyv.livecommon.ui.widget.autolineView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于自动换行的功能格子View
 */
public class PLVAutoLineSpanLayout extends ViewGroup {

    List<PLVAutoLineStateModel> list = new ArrayList<>();

    int span = 4;

    public void addItem(PLVAutoLineStateModel model){
        list.add(model);
    }

    //todo 增加配置item的宽高
    public PLVAutoLineSpanLayout(Context context) {
        super(context);
    }

    public PLVAutoLineSpanLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVAutoLineSpanLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setItemView(View view){

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int sumWidth = getWidth();

        int curLineWidth = 0, curLineTop = 0;
        int lastLineMaxHeight = 0;
        for (int i = 0; i < list.size(); i++) {
            View itemView = createItemView(list.get(i));

            addView(itemView);
//            measureChildWithMargins(itemView, 0, 0,0, 0);
            int width = getMeasuredWidth();
//            int height = getDecoratedMeasuredHeight(view);
//
//            curLineWidth += width;
//            if (curLineWidth <= sumWidth) {//不需要换行
//                layoutDecorated(view, curLineWidth - width, curLineTop, curLineWidth, curLineTop + height);
//                //比较当前行多有item的最大高度
//                lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
//            } else {//换行
//                curLineWidth = width;
//                if (lastLineMaxHeight == 0) {
//                    lastLineMaxHeight = height;
//                }
//                //记录当前行top
//                curLineTop += lastLineMaxHeight;
//
//                layoutDecorated(view, 0, curLineTop, width, curLineTop + height);
//                lastLineMaxHeight = height;
//            }
        }
    }


    private View createItemView(PLVAutoLineStateModel model){

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams layoutLayoutParams = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        if(model.isShow()){
            layoutLayoutParams.width = getWidth() / span;
//            layoutLayoutParams.height = LayoutParams.WRAP_CONTENT;
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        linearLayout.setLayoutParams(layoutLayoutParams);

        //添加图片
        ImageView image = new ImageView(getContext());
        LayoutParams params = new LinearLayout.LayoutParams(ConvertUtils.dp2px(30),ConvertUtils.dp2px(30));

        image.setLayoutParams(params);
        image.setImageResource(model.getImageSource());
        image.setSelected(model.isActive());

        TextView textView = new TextView(getContext());
        LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        textView.setText(model.getName());
        textView.setSelected(model.isActive());

        linearLayout.addView(image);
        linearLayout.addView(textView);

        return linearLayout;

    }

}
