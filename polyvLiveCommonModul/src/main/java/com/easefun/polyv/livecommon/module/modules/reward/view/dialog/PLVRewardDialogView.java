package com.easefun.polyv.livecommon.module.modules.reward.view.dialog;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.reward.view.adapter.PLVRewardListAdapter;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVBeadWidget;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2021-03-05
 * author: ysh
 * description: 选择打赏View
 * 支持切换横屏
 */
public class PLVRewardDialogView {


    // <editor-fold defaultstate="collapsed" desc="变量">
    private AppCompatActivity context;
    //打赏弹窗的根视图
    private View rootView;

    private LinearLayout plvLlRewardBottomLayout;
    private RelativeLayout plvTvPointRewardTopLayout;
    private TextView plvTvPointRewardTitle;
    private TextView plvTvPointRewardRemainingPoint;
    //用来不分页，可滑动展示礼物列表的recyclerView（横屏）
    private RecyclerView plvRvRewardLandscape;
    //用来分页展示礼物列表的（竖屏）
    private ViewPager plvVpPointReward;
    //分页用到的圆点指示器
    private PLVBeadWidget plvBeadPointReward;
    private RadioGroup plvRgPointRewardSendCount;
    //顶部透明部分，响应消失事件
    private View plvViewTopTransparent;
    //实际可见打赏弹窗ViewGroup
    private LinearLayout plvLlRewardDialogView;
    //打赏按钮
    private Button plvBtnPointRewardMakeReward;
    //关闭按钮
    private ImageView plvIvPointRewardClose;

    //管理分页礼物列表的adapter
    private PLVRewardPageAdapter adapter;
    //管理横屏不分页的礼物列表adapter
    private PLVRewardListAdapter landscapeAdapter;

    //打赏的礼物数量
    private int makeRewardNum;
    //标记显示状态
    private boolean isShown = false;
    //标记横屏状态
    private boolean isLandscape;

    //点击打赏按钮的回调监听
    private OnMakeRewardListener makeRewardListener;
    //显示回调监听
    private OnShowListener showListener;
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVRewardDialogView(AppCompatActivity activity, ViewGroup parent) {
        this.context = activity;

        rootView = LayoutInflater.from(context).inflate(R.layout.plv_point_reward_window, parent, false);

        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        rootView.setClickable(true);
        parent.addView(rootView);

        plvTvPointRewardTopLayout = rootView.findViewById(R.id.plv_tv_point_reward_top_layout);
        plvTvPointRewardTitle = rootView.findViewById(R.id.plv_tv_point_reward_title);
        plvTvPointRewardRemainingPoint = rootView.findViewById(R.id.plv_tv_point_reward_remaining_point);
        plvIvPointRewardClose = rootView.findViewById(R.id.plv_iv_point_reward_close);
        plvVpPointReward = rootView.findViewById(R.id.plv_vp_point_reward);
        plvBeadPointReward = rootView.findViewById(R.id.plv_bead_point_reward);
        plvRgPointRewardSendCount = rootView.findViewById(R.id.plv_rg_point_reward_send_count);
        plvBtnPointRewardMakeReward = rootView.findViewById(R.id.plv_btn_point_reward_make_reward);
        plvLlRewardBottomLayout = rootView.findViewById(R.id.plv_ll_reward_bottom);
        plvViewTopTransparent = rootView.findViewById(R.id.plv_v_top_transparent);
        plvLlRewardDialogView = rootView.findViewById(R.id.plv_ll_reward_dialog_view);
        plvRvRewardLandscape = rootView.findViewById(R.id.plv_rv_reward_landscape);

        plvViewTopTransparent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        plvIvPointRewardClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        plvBtnPointRewardMakeReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeReward();
            }
        });

        plvRgPointRewardSendCount.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.plv_rb_point_reward_reward_1) {
                    makeRewardNum = 1;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_5) {
                    makeRewardNum = 5;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_10) {
                    makeRewardNum = 10;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_66) {
                    makeRewardNum = 66;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_88) {
                    makeRewardNum = 88;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_666) {
                    makeRewardNum = 666;
                }
            }
        });

        rootView.setVisibility(View.GONE);

    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口 - get、set方法">

    /**
     * 设置打赏回调监听
     */
    public void setMakeRewardListener(OnMakeRewardListener makeRewardListener) {
        this.makeRewardListener = makeRewardListener;
    }

    /**
     * 设置显示监听
     */
    public void setShowListener(OnShowListener showListener) {
        this.showListener = showListener;
    }

    /**
     * 获取打赏按钮，该按钮不可设置点击监听，否则影响打赏点击回调
     */
    public Button getMakeRewardButton(){
        return plvBtnPointRewardMakeReward;
    }


    /**
     * 获取打赏余额Tv
     */
    public TextView getRemainingPointTextView(){
        return plvTvPointRewardRemainingPoint;
    }

    /**
     * 获取打赏Title Tv
     */
    public TextView getRewardTitleTextView(){
        return plvTvPointRewardTitle;
    }

    /**
     * 获取关闭弹窗按钮
     */
    public ImageView getCloseButton(){
        return plvIvPointRewardClose;
    }

    /**
     * 是否可见
     */
    public boolean isShown() {
        return isShown;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">

    /**
     * 更新积分数
     */
    public void updateRemainingPoint(String remainingPoint) {
        String remainingPointToShow = "我的积分：" + remainingPoint;
        plvTvPointRewardRemainingPoint.setText(remainingPointToShow);
    }

    /**
     * 设置礼物列表数据，设置后将自动分页
     */
    public void init(List<PLVBaseViewData> dataList) {
        if(adapter == null) {
            adapter = new PLVRewardPageAdapter(context.getSupportFragmentManager(),
                    dataList, true, 10);
            plvVpPointReward.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    plvBeadPointReward.setCurrentSelectedIndex(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            //设置预加载
            plvVpPointReward.setAdapter(adapter);
            //圆点指示器数量
            plvBeadPointReward.setBeadCount(adapter.getPageCount());
            adapter.notifyDataSetChanged();
            plvVpPointReward.setOffscreenPageLimit(adapter.getPageCount());
        }

        //初始化横屏打赏列表
        plvRvRewardLandscape.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        landscapeAdapter = new PLVRewardListAdapter(true);
        landscapeAdapter.setDataList(new ArrayList<PLVBaseViewData>(dataList));
        plvRvRewardLandscape.setAdapter(landscapeAdapter);
        plvRvRewardLandscape.setVisibility(View.GONE);
    }

    /**
     * 切换到横屏样式微调
     */
    public void changeToLandscape(){
        isLandscape = true;
        plvRvRewardLandscape.setVisibility(View.VISIBLE);
        plvVpPointReward.setVisibility(View.GONE);
        plvBeadPointReward.setVisibility(View.GONE);
        //打赏数量样式调整
        changeRewardNumButtonSpace(8);

        RelativeLayout.LayoutParams titleParams = (RelativeLayout.LayoutParams) plvTvPointRewardTitle.getLayoutParams();
        titleParams.leftMargin = ConvertUtils.dp2px(30);

    }

    /**
     * 切换到竖屏样式微调
     */
    public void changeToPortrait(){
        isLandscape = false;
        plvVpPointReward.setVisibility(View.VISIBLE);
        plvRvRewardLandscape.setVisibility(View.GONE);
        if(adapter != null && adapter.getPageCount() > 0){
            plvBeadPointReward.setVisibility(View.VISIBLE);
        }
        changeRewardNumButtonSpace(4);

        RelativeLayout.LayoutParams titleParams = (RelativeLayout.LayoutParams) plvTvPointRewardTitle.getLayoutParams();
        titleParams.leftMargin = ConvertUtils.dp2px(16);
    }

    /**
     * 显示弹窗
     */
    public void show(){
        if(isShown()){
            return;
        }
        rootView.setVisibility(View.VISIBLE);
        rootView.requestFocus();

        Animation enterAnim = AnimationUtils.loadAnimation(context, R.anim.plv_point_reward_enter);
        rootView.startAnimation(enterAnim);
        isShown = true;
        if(showListener != null){
            showListener.onShow();
        }
    }

    /**
     * 隐藏弹窗
     */
    public void hide(){
        if(!isShown()){
            return;
        }
        Animation exitAnim = AnimationUtils.loadAnimation(context, R.anim.plv_point_reward_exit);
        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                rootView.setVisibility(View.GONE);
                rootView.clearFocus();
                PLVOrientationManager.getInstance().unlockOrientation();
                isShown = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rootView.startAnimation(exitAnim);
    }

    /**
     * 改变弹窗顶部是否是直角
     * @param isRightAngle true-直角；false-圆角
     *                     默认为true直角
     */
    public void changeDialogTop(boolean isRightAngle){
        plvLlRewardDialogView.setBackgroundResource(isRightAngle ?
                R.drawable.plv_shape_reward_right_angle : R.drawable.plv_shape_reward_fillet );
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对内接口">
    /**
     * 打赏
     */
    private void makeReward() {
        if (makeRewardNum <= 0) {
            ToastUtils.showShort("请选择打赏数量");
            return;
        }

        PLVBaseViewData selectData = null;
        if(isLandscape){
            if(landscapeAdapter == null || landscapeAdapter.getSelectData() == null){
                ToastUtils.showShort("请选择打赏道具");
                return;
            }
            selectData = landscapeAdapter.getSelectData();
        } else {
            if (adapter == null || adapter.getSelectData() == null) {
                ToastUtils.showShort("请选择打赏道具");
                return;
            }
            selectData = adapter.getSelectData();
        }

        if(makeRewardListener != null){
            makeRewardListener.onMakeReward(selectData, makeRewardNum);
        }

        hide();
    }

    private void changeRewardNumButtonSpace(int dpSpace){
        //第0个不用marginLeft
        for (int i = 1; i < plvRgPointRewardSendCount.getChildCount(); i++) {
            RadioButton childAt = (RadioButton) plvRgPointRewardSendCount.getChildAt(i);
            RadioGroup.LayoutParams layoutParams = (RadioGroup.LayoutParams) childAt.getLayoutParams();
            layoutParams.leftMargin = ConvertUtils.dp2px(dpSpace);
        }
    }
    // </editor-fold >

    /**
     * 观众点击积分打赏回调。
     * 客户端应使用该回调发送积分打赏请求。
     */
    public interface OnMakeRewardListener {
        /**
         * 打赏回调
         * @param data 打赏礼物选中的item
         * @param rewardNum 打赏数量
         */
        void onMakeReward(PLVBaseViewData data, int rewardNum);
    }


    /**
     * 打赏弹窗显示回调
     */
    public interface OnShowListener {
        /**
         * 打赏弹窗显示
         */
        void onShow();
    }

}
