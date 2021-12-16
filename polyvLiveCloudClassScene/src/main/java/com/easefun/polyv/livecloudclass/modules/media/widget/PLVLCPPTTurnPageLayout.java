package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.plv.livescenes.document.model.PLVPPTStatus;

/**
 * ppt翻页控件
 */
public class PLVLCPPTTurnPageLayout extends RelativeLayout implements View.OnClickListener {

    public static final String PPT_TURN_PAGE_PREVIOUS = "gotoPreviousStep";
    public static final String PPT_TURN_PAGE_NEXT = "gotoNextStep";
    public static final String PPT_TURN_PAGE_GO_BACK = "pptBtnBack";

    private PLVTriangleIndicateTextView plvlcPptTurnPageTipTv;
    private LinearLayout plvlcPptTurnPageLayout;
    private ImageView plvlcPptTurnPagePreviousIv;
    private TextView plvlcPptTurnPageProgressTv;
    private ImageView plvlcPptTurnPageNextIv;

    //当前页
    private int currentPage = 0;
    //最大页
    private int maxPage;
    //讲师操作工的最大页面
    private int teacherMaxPage;

    private int autoId = -1;

    private boolean isPreviousClickFlag = false;

    private OnPPTTurnPageListener onPPTTurnPageListener;

    public PLVLCPPTTurnPageLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCPPTTurnPageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCPPTTurnPageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_ppt_turn_page_layout, this);

        plvlcPptTurnPageTipTv = (PLVTriangleIndicateTextView) findViewById(R.id.plvlc_ppt_turn_page_tip_tv);
        plvlcPptTurnPageLayout = (LinearLayout) findViewById(R.id.plvlc_ppt_turn_page_layout);
        plvlcPptTurnPagePreviousIv = (ImageView) findViewById(R.id.plvlc_ppt_turn_page_previous_iv);
        plvlcPptTurnPageProgressTv = (TextView) findViewById(R.id.plvlc_ppt_turn_page_progress_tv);
        plvlcPptTurnPageNextIv = (ImageView) findViewById(R.id.plvlc_ppt_turn_page_next_iv);

        plvlcPptTurnPageProgressTv.setOnClickListener(this);
        plvlcPptTurnPagePreviousIv.setOnClickListener(this);
        plvlcPptTurnPageNextIv.setOnClickListener(this);

        plvlcPptTurnPagePreviousIv.setSelected(true);
        plvlcPptTurnPageNextIv.setSelected(true);
    }


    // <editor-fold defaultstate="collapsed" desc="对外接口">


    public void setOnPPTTurnPageListener(OnPPTTurnPageListener onPPTTurnPageListener) {
        this.onPPTTurnPageListener = onPPTTurnPageListener;
    }

    public void initPageData(@NonNull PLVPPTStatus plvpptStatus){
        if(plvpptStatus != null && plvpptStatus.getMaxTeacherOp() != null){
            PLVPPTStatus.MaxTeacherOp maxTeacherOp = plvpptStatus.getMaxTeacherOp();
            teacherMaxPage = maxTeacherOp.getPageId();
            currentPage = teacherMaxPage;
            maxPage = plvpptStatus.getTotal();

            updateStep();

        }


    }

    public void updatePageData(PLVPPTStatus plvpptStatus){
        if(plvpptStatus != null && plvpptStatus.getMaxTeacherOp() != null){
            PLVPPTStatus.MaxTeacherOp maxTeacherOp = plvpptStatus.getMaxTeacherOp();
            teacherMaxPage = maxTeacherOp.getPageId();
            if(autoId != plvpptStatus.getAutoId()){
                maxPage = plvpptStatus.getTotal();
                currentPage = plvpptStatus.getPageId();
            }
            updateStep();
        }
    }

    public void gotoPreviousStep(){
        //提示回到当前页
        if(!isPreviousClickFlag){
            plvlcPptTurnPageTipTv.setVisibility(View.VISIBLE);
            isPreviousClickFlag = true;
        } else {
            plvlcPptTurnPageTipTv.setVisibility(View.INVISIBLE);
        }
        currentPage--;
        if(plvlcPptTurnPagePreviousIv.isSelected() && onPPTTurnPageListener != null){
            onPPTTurnPageListener.onPPTTurnPage(PPT_TURN_PAGE_PREVIOUS);
        }
    }

    public void gotoNextStep(){
        if(currentPage >= teacherMaxPage){
            //不允许翻页超过讲师曾经翻过的最大页
            return;
        }
        //提示回到当前页
        if(!isPreviousClickFlag){
            plvlcPptTurnPageTipTv.setVisibility(View.VISIBLE);
            isPreviousClickFlag = true;
        } else {
            plvlcPptTurnPageTipTv.setVisibility(View.INVISIBLE);
        }
        currentPage++;
        if(plvlcPptTurnPageNextIv.isSelected() && onPPTTurnPageListener != null){
            onPPTTurnPageListener.onPPTTurnPage(PPT_TURN_PAGE_NEXT);
        }
    }

    public void goBackTeacherPage(){
        plvlcPptTurnPageTipTv.setVisibility(View.INVISIBLE);
        if(onPPTTurnPageListener != null){
            onPPTTurnPageListener.onPPTTurnPage(PPT_TURN_PAGE_GO_BACK);
        }
    }


    // </editor-fold >

    private void updateStep(){

        int page = currentPage + 1;
        if(page <= 1){
            plvlcPptTurnPagePreviousIv.setSelected(false);
            currentPage = 0;
        } else {
            plvlcPptTurnPagePreviousIv.setSelected(true);
        }

        if (currentPage >= teacherMaxPage){
            plvlcPptTurnPageNextIv.setSelected(false);
            currentPage = teacherMaxPage;
        } else {
            plvlcPptTurnPageNextIv.setSelected(true);
        }

        plvlcPptTurnPageProgressTv.setText(String.format("%s/%s", page, maxPage));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.plvlc_ppt_turn_page_previous_iv) {
            gotoPreviousStep();
        } else if (view.getId() == R.id.plvlc_ppt_turn_page_next_iv){
            gotoNextStep();
        } else if (view.getId() == R.id.plvlc_ppt_turn_page_progress_tv){
            goBackTeacherPage();
        }
    }

    /**
     * PPT翻页回调
     */
    public interface OnPPTTurnPageListener {
        /**
         * PPT翻页回调
         * @param type 翻页类型（gotoPreviousStep、gotoNextStep、pptBtnBack）
         */
        void onPPTTurnPage(String type);
    }

}
