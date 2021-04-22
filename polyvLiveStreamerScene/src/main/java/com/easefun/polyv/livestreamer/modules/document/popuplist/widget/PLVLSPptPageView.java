package com.easefun.polyv.livestreamer.modules.document.popuplist.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.document.popuplist.vo.PLVLSPptVO;

/**
 * PPT页面列表项视图
 *
 * @author suhongtao
 */
public class PLVLSPptPageView extends PLVLSAbsPptViewItem {

    // <editor-fold defaultstate="collapsed" desc="变量">

    // 子View
    private View rootView;
    private PLVRoundImageView plvlsDocumentPptItemIv;
    private TextView plvlsDocumentPptIndexTv;
    private View plvlsDocumentPptSelectedMask;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSPptPageView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSPptPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSPptPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvls_document_ppt_page_item, this);
        findView();
    }

    private void findView() {
        plvlsDocumentPptItemIv = (PLVRoundImageView) rootView.findViewById(R.id.plvls_document_ppt_item_iv);
        plvlsDocumentPptIndexTv = (TextView) rootView.findViewById(R.id.plvls_document_ppt_index_tv);
        plvlsDocumentPptSelectedMask = (View) rootView.findViewById(R.id.plvls_document_ppt_selected_mask);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onMeasure重写 - 动态调整尺寸">

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        GridLayoutManager.LayoutParams rlLp = (GridLayoutManager.LayoutParams) rootView.getLayoutParams();
        rlLp.leftMargin = rlLp.rightMargin = ITEM_MARGIN_LEFT;

        RelativeLayout.LayoutParams ivLp = (RelativeLayout.LayoutParams) plvlsDocumentPptItemIv.getLayoutParams();
        ivLp.width = ITEM_PICTURE_WIDTH;
        ivLp.height = ITEM_PICTURE_HEIGHT;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    @Override
    public void processData(PLVLSPptVO pptVO) {
        PLVImageLoader.getInstance().loadImage(pptVO.getImage(), plvlsDocumentPptItemIv);
    }

    public void setIndexText(String indexText) {
        plvlsDocumentPptIndexTv.setText(indexText);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        plvlsDocumentPptIndexTv.setSelected(selected);
        if (selected) {
            plvlsDocumentPptSelectedMask.setVisibility(VISIBLE);
        } else {
            plvlsDocumentPptSelectedMask.setVisibility(GONE);
        }
    }

    // </editor-fold>

}
