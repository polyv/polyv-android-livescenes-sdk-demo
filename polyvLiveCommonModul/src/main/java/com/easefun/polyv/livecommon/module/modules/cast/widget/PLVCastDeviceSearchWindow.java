package com.easefun.polyv.livecommon.module.modules.cast.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.cast.adapter.PLVCastDeviceListAdapter;
import com.easefun.polyv.livecommon.module.modules.cast.manager.PLVCastBusinessManager;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.media.cast.model.vo.PLVMediaCastDevice;

import java.lang.ref.WeakReference;


/**
 * 设备搜索弹窗，处理投屏设备搜索连接逻辑
 */
public class PLVCastDeviceSearchWindow extends PopupWindow {

    private static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private NetworkReceiver networkReceiver;

    private Context mContext;
    private PLVCastSearchLayout mSearchView;
    private OnWindowDismissListener onWindowDismissListener;
    private OnCastDeviceItemClickListener onItemClickListener;

    // <editor-fold defaultstate="collapsed" desc="初始化">
    public PLVCastDeviceSearchWindow(Context context) {
        super(context);
        initial(context);
    }

    private void initial(Context context) {
        mContext = context;
        mSearchView = new PLVCastSearchLayout(context);
        setPopupWindow();

        if (PLVCastBusinessManager.getInstance().isSameChannelId()) {
            mSearchView.setSelectInfo(PLVCastBusinessManager.getInstance().getSelectInfo());
        }

        mSearchView.findViewById(R.id.plv_back_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mSearchView.setOnItemClickListener(new PLVCastDeviceListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, PLVMediaCastDevice pInfo) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(pInfo);
                }
            }
        });
    }

    private void setPopupWindow() {
        setContentView(mSearchView);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);// 设置弹出窗口的高
        //初始属性设置
//        setFocusable(true);// 取得焦点
        setAnimationStyle(R.style.plv_right_popwindow_anim_style);
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        setBackgroundDrawable(new ColorDrawable());
        //点击外部消失
        setOutsideTouchable(true);
        //设置可以点击
        setTouchable(true);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (null != networkReceiver) {
                    mContext.unregisterReceiver(networkReceiver);
                    networkReceiver = null;
                }

                if (null != onWindowDismissListener) {
                    onWindowDismissListener.onDismiss();
                }

                mSearchView.stopBrowse();
            }
        });
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">

    public void show() {
        registerNetworkReceiver();
        if (ScreenUtils.isPortrait()) {
            showInPort();
        } else {
            showInLandscape();
        }
        mSearchView.browse();
    }

    public void clearCastListener() {
        if (mSearchView != null) {
            mSearchView.clearCastListener();
        }
    }

    public void clearCastSelectedInfo() {
        if (mSearchView != null) {
            mSearchView.selectNull();
        }
    }
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="对外设置监听器">
    public void setOnWindowDismissListener(OnWindowDismissListener onWindowDismissListener) {
        this.onWindowDismissListener = onWindowDismissListener;
    }

    public void setOnCastDeviceItemClickListener(OnCastDeviceItemClickListener listener) {
        onItemClickListener = listener;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="私有方法">
    private void showInPort() {
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        showAtLocation(mSearchView, Gravity.END, 0, 0);
    }

    private void showInLandscape() {
        setWidth(ScreenUtils.getScreenWidth() / 2);
        showAtLocation(mSearchView, Gravity.END, 0, 0);
    }

    private void registerNetworkReceiver() {
        networkReceiver = new NetworkReceiver(mSearchView);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(networkReceiver, intentFilter);
    }
    // </editor-fold >

    private static class NetworkReceiver extends BroadcastReceiver {
        private WeakReference<PLVCastSearchLayout> reference;

        public NetworkReceiver(PLVCastSearchLayout rf) {
            reference = new WeakReference<>(rf);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == reference || null == reference.get()) {
                return;
            }
            PLVCastSearchLayout searchLayout = reference.get();
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equalsIgnoreCase(action) ||
                    PLVCastDeviceSearchWindow.WIFI_AP_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) searchLayout.getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isAvailable()) {
                    int type2 = networkInfo.getType();
                    switch (type2) {
                        case 0://移动 网络
                        case 9:  //网线连接
                            searchLayout.showEmptyLayout();
                            break;
                        case 1: //wifi网络
                            searchLayout.browse();
                            break;
                    }
                } else {// 无网络
                    searchLayout.showEmptyLayout();
                }

            }
        }
    }

    public interface OnWindowDismissListener {
        void onDismiss();
    }

    public interface OnCastDeviceItemClickListener {
        void onClick(PLVMediaCastDevice pInfo);
    }

}
