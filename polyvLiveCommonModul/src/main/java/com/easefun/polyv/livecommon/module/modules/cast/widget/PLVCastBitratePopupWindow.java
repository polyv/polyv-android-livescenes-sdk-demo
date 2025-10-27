package com.easefun.polyv.livecommon.module.modules.cast.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.cast.adapter.PolyvScreencastBitrateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制投屏视频的清晰度选择播放
 */
public class PLVCastBitratePopupWindow extends PopupWindow {

    private Context context;
    private View mPopView;
    private RecyclerView mRvBitrate;
    private PolyvScreencastBitrateAdapter mBitrateAdapter;
    private List<PolyvDefinitionVO> mBitrateList;
    private IPlvCastBitrateChangeListener mBitrateChangeListener;
    private int mCurBitrateIndex;

    public void setBitrateChangeListener(IPlvCastBitrateChangeListener bitrateChangeListener) {
        this.mBitrateChangeListener = bitrateChangeListener;
    }

    public PLVCastBitratePopupWindow(Context context) {
        super(context);
        initial(context);
    }

    private void initial(Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
        mPopView = inflater.inflate(R.layout.plv_cast_bitrate_window, null);

        mRvBitrate = mPopView.findViewById(R.id.rv_bitrate);
        mRvBitrate.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mBitrateList = new ArrayList<>();
        mBitrateAdapter = new PolyvScreencastBitrateAdapter();
        mRvBitrate.setAdapter(mBitrateAdapter);

        mBitrateAdapter.setClickListener(new PolyvScreencastBitrateAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int index) {
                mCurBitrateIndex = index;
                if (mBitrateChangeListener != null) {
                    mBitrateChangeListener.onBitrateChange(index, mBitrateList.get(index).getDefinition());
                }
                dismiss();
            }
        });

        setPopupWindow();
    }

    private void setPopupWindow() {
        setContentView(mPopView);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        //初始属性设置
        setFocusable(true);// 取得焦点
        setAnimationStyle(R.style.plv_buttom_popwindow_anim_style);
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        setBackgroundDrawable(new ColorDrawable());
        //点击外部消失
        setOutsideTouchable(true);
        //设置可以点击
        setTouchable(true);
    }

    public void showBottomUpWithMask() {
        if (context != null) {
            final Activity activity = ((Activity) context);
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.alpha = 0.7f;//设置阴影透明度
            activity.getWindow().setAttributes(lp);
            setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                    lp.alpha = 1f;
                    activity.getWindow().setAttributes(lp);
                }
            });
        }
        showAtLocation(mPopView, Gravity.BOTTOM, 0, 0);
    }

    public void initBitRateView(List<PolyvDefinitionVO> definitions, int curBitrateIndex) {

        mCurBitrateIndex = curBitrateIndex;
        mBitrateList.clear();
        mBitrateList.addAll(definitions);

        mBitrateAdapter.setBitrateList(mBitrateList, mCurBitrateIndex);
        mBitrateAdapter.notifyDataSetChanged();
    }

    public void changeBitrate(int index) {
        mCurBitrateIndex = index;
        if (mBitrateAdapter != null) {
            mBitrateAdapter.setBitrateIndex(index);
            mBitrateAdapter.notifyDataSetChanged();
        }
    }

    public int getCurBitrateIndex() {
        return mCurBitrateIndex;
    }

    public interface IPlvCastBitrateChangeListener {
        void onBitrateChange(int index, String bitrate);
    }

}
