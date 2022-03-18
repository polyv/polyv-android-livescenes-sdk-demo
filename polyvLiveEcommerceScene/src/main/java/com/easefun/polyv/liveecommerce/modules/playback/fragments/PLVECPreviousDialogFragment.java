package com.easefun.polyv.liveecommerce.modules.playback.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.blurview.SupportRenderScriptBlur;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVViewPagerAdapter;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVMagicIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVViewPagerHelper;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.PLVCommonNavigator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerTitleView;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.PLVCommonNavigatorAdapter;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.indicators.PLVLinePagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles.PLVColorTransitionPagerTitleView;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles.PLVSimplePagerTitleView;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousView;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.previous.PLVECPreviousFragment;
import com.plv.livescenes.model.PLVPlaybackListVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示往期视频列表的DialogFragment
 */
public class PLVECPreviousDialogFragment extends DialogFragment implements IPLVECPreviousDialogFragment {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVECPreviousDialogFragment";

    private View contentView;
    private PLVMagicIndicator pageTabIndicator;
    private ViewPager pageTabViewPage;
    private PLVViewPagerAdapter pageTabAdapter;
    private ImageView closeIm;
    //DialogFragment在消失时的回调监听
    private IPLVECPreviousDialogFragment.DismissListener dismissListener;

    //回放视频列表
    private List<PLVPlaybackListVO.DataBean.ContentsBean> datas;
    //当前回放视频的vid
    private String vid;
    //回放菜单tabFragment列表
    private List<Fragment> pageTabFragmentList;
    //回放菜单tab标题列表
    private List<String> pageTabTitleList;
    //tab
    private PLVECPreviousFragment previousFragment;//往期tab页
    //presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter presenter;
    private PLVPreviousView plvPreviousView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);

        contentView = inflater.inflate(R.layout.plvec_playback_more_video_layout, container, false);
        initView(contentView);
        addPreviousTab();
        return contentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //让dialogFragment的宽度铺满屏幕
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PLVEC_Playback_Them_Dialog_Fragment);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        //防止重复添加Fragment
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment != null || isAdded()) {
            transaction.remove(this);
        }
        transaction.commit();
        super.show(manager, tag);
    }

    @Override
    public void onDestroyView() {
        //销毁掉previousFragment，防止重复添加
        pageTabAdapter.destroyItem(null, 0, previousFragment);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (dismissListener != null) {
            dismissListener.onDismissListener();
        }
        super.onDestroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化布局">
    private void initView(View view) {
        PLVBlurView blurView = view.findViewById(R.id.blur_ly);
        blurView.setupWith((ViewGroup) (getActivity()).findViewById(Window.ID_ANDROID_CONTENT))
                .setFrameClearDrawable(null)
                .setBlurAlgorithm(new SupportRenderScriptBlur(blurView.getContext()))
                .setBlurRadius(1)
                .setHasFixedTransformationMatrix(false);
        pageTabIndicator = view.findViewById(R.id.plvec_playback_videos_tab);
        pageTabViewPage = view.findViewById(R.id.plvec_playback_videos_vp);
        pageTabFragmentList = new ArrayList<>();
        pageTabTitleList = new ArrayList<>();
        pageTabAdapter = new PLVViewPagerAdapter(getChildFragmentManager(), pageTabFragmentList);
        pageTabViewPage.setAdapter(pageTabAdapter);
        PLVCommonNavigator commonNavigator = new PLVCommonNavigator(getContext());
        commonNavigator.setAdapter(new PLVCommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return pageTabAdapter.getCount();
            }

            @Override
            public IPLVPagerTitleView getTitleView(Context context, final int index) {
                if (pageTabTitleList.isEmpty() || pageTabTitleList.size() < index + 1) {
                    return null;
                }
                PLVSimplePagerTitleView simplePagerTitleView = new PLVColorTransitionPagerTitleView(context);
                simplePagerTitleView.setPadding(0, ConvertUtils.dp2px(16), 0, ConvertUtils.dp2px(16));
                simplePagerTitleView.setNormalColor(Color.parseColor("#FFFFFF"));
                simplePagerTitleView.setSelectedColor(Color.parseColor("#FFD16B"));
                simplePagerTitleView.setText(pageTabTitleList.get(index));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pageTabViewPage.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPLVPagerIndicator getIndicator(Context context) {
                PLVLinePagerIndicator linePagerIndicator = new PLVLinePagerIndicator(context);
                linePagerIndicator.setMode(PLVLinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setLineHeight(ConvertUtils.dp2px(2));
                linePagerIndicator.setXOffset(0);
                linePagerIndicator.setRoundRadius(ConvertUtils.dp2px(1f));
                linePagerIndicator.setColors(Color.parseColor("#FFFFA611"));
                return linePagerIndicator;
            }
        });
        pageTabIndicator.setNavigator(commonNavigator);
        PLVViewPagerHelper.bind(pageTabIndicator, pageTabViewPage);

        closeIm = view.findViewById(R.id.plvec_playback_more_dialog_close_iv);
        closeIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVECPreviousDialogFragment.this.hide();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="添加tab页"
    private void addPreviousTab() {
        pageTabTitleList.add("回放列表");
        previousFragment = new PLVECPreviousFragment();
        previousFragment.setPrviousView(plvPreviousView);
        pageTabFragmentList.add(previousFragment);
        refreshPageMenuTabAdapter();
    }

    private void refreshPageMenuTabAdapter() {
        if (pageTabAdapter.getCount() > 0) {
            pageTabAdapter.notifyDataSetChanged();
            pageTabIndicator.getNavigator().notifyDataSetChanged();
            pageTabViewPage.setOffscreenPageLimit(pageTabAdapter.getCount() - 1);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外api">
    @Override
    public void setPrviousView(PLVPreviousView plvPreviousView) {
        this.plvPreviousView = plvPreviousView;
    }

    @Override
    public void setDismissListener(DismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    public void hide() {
        this.dismiss();
    }

    @Override
    public void showPlaybackMoreVideoDialog(List<PLVPlaybackListVO.DataBean.ContentsBean> datas, String vid, Fragment fm) {
        this.datas = datas;
        this.vid = vid;
        this.show(fm.getChildFragmentManager(), TAG);
    }

    // </editor-fold>
}
