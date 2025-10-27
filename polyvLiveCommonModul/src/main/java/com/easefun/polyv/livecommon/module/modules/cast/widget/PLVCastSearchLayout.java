package com.easefun.polyv.livecommon.module.modules.cast.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.cast.adapter.PLVCastDeviceListAdapter;
import com.easefun.polyv.livecommon.module.modules.cast.manager.IPLVCastUpdateListener;
import com.easefun.polyv.livecommon.module.modules.cast.manager.PLVCastBusinessManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import net.polyv.android.media.cast.model.vo.PLVMediaCastDevice;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * 投屏设备搜索封装View
 */
class PLVCastSearchLayout extends FrameLayout implements View.OnClickListener {
    private static final String TAG = PLVCastSearchLayout.class.getSimpleName();

    /**
     * 投屏搜索可连接设备
     */
    private PLVCastDeviceListAdapter screencastDeviceListAdapter;

    private ImageView mPlvBackIv;
    private TextView mPlvSearchStatusTv;
    private ImageView mPlvSearchIv;
    private GifImageView mPlvSearchingGif;
    private RecyclerView mPlvDevicesRv;
    private LinearLayout mPlvEmptyLl;
    private TextView mPlvRetryTv;
    private TextView mPlvEmptyStatusTv;


    // <editor-fold defaultstate="collapsed" desc="生命周期与构造器初始化">
    public PLVCastSearchLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVCastSearchLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVCastSearchLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_cast_search_device, this);

        mPlvBackIv = findViewById(R.id.plv_back_iv);
        mPlvSearchStatusTv = findViewById(R.id.plv_search_status_tv);
        mPlvSearchIv = findViewById(R.id.plv_search_iv);
        mPlvSearchingGif = findViewById(R.id.plv_searching_gif);
        mPlvDevicesRv = findViewById(R.id.plv_devices_rv);
        mPlvEmptyLl = findViewById(R.id.plv_empty_ll);
        mPlvRetryTv = findViewById(R.id.plv_retry_tv);
        mPlvEmptyStatusTv = findViewById(R.id.plv_empty_status_tv);

        mPlvSearchIv.setOnClickListener(this);
        mPlvBackIv.setOnClickListener(this);
        mPlvRetryTv.setOnClickListener(this);

        screencastDeviceListAdapter = new PLVCastDeviceListAdapter(R.layout.plv_cast_device_item);
        mPlvDevicesRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mPlvDevicesRv.setAdapter(screencastDeviceListAdapter);
        if (PLVCastBusinessManager.getInstance().isSameChannelId()) {
            screencastDeviceListAdapter.setSelectInfo(PLVCastBusinessManager.getInstance().getSelectInfo());
        }

        PLVCastBusinessManager.getInstance().addPlvScreencastStateListener(mStateListener);
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="投屏设备搜索">
    public void browse() {
        if (!PLVNetworkUtils.isWifiConnected(getContext())) {
            Toast.makeText(getContext(), R.string.plv_cast_network_not_connected, Toast.LENGTH_SHORT).show();
            PLVCommonLog.e(TAG, "network error or not local area network");
            showEmptyLayout();
            stopBrowse();
            return;
        }
        PLVCastBusinessManager.getInstance().startBrowse();
        mPlvSearchStatusTv.setText(R.string.plv_cast_title_device_searched);
        mPlvSearchingGif.setVisibility(View.VISIBLE);
        mPlvDevicesRv.setVisibility(VISIBLE);
        mPlvEmptyLl.setVisibility(GONE);
    }

    public void stopBrowse() {
        PLVCastBusinessManager.getInstance().stopBrowse();
        mPlvSearchingGif.setVisibility(View.GONE);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="监听器">

    /**
     * 点击选中设备列表监听
     */
    public void setOnItemClickListener(PLVCastDeviceListAdapter.OnItemClickListener listener) {
        screencastDeviceListAdapter.setOnItemClickListener(listener);
    }

    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="其他对外接口">
    public void setSelectInfo(PLVMediaCastDevice info) {
        screencastDeviceListAdapter.setSelectInfo(info);
        screencastDeviceListAdapter.notifyDataSetChanged();
    }

    public void showEmptyLayout() {
        mPlvDevicesRv.setVisibility(GONE);
        mPlvSearchingGif.setVisibility(GONE);
        mPlvEmptyStatusTv.setText(R.string.plv_cast_search_state_device_not_found);
        mPlvSearchStatusTv.setText(R.string.plv_cast_search_state_device_not_found_2);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPlvEmptyLl.getLayoutParams();
        if (PLVScreenUtils.isLandscape(getContext())) {
            params.topMargin = ConvertUtils.dp2px(10);
        } else {
            params.topMargin = ConvertUtils.dp2px(90);
        }
        mPlvEmptyLl.setLayoutParams(params);
        mPlvEmptyLl.setVisibility(VISIBLE);
    }

    public void clearCastListener() {
        PLVCastBusinessManager.getInstance().removePlvScreencastStateListener(mStateListener);
    }

    public void selectNull() {
        screencastDeviceListAdapter.setSelectInfo(null);
        screencastDeviceListAdapter.notifyDataSetChanged();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="内部接口">


    private void showStatus(String text) {
        mPlvEmptyStatusTv.setText(text);
        showEmptyLayout();
        mPlvDevicesRv.setVisibility(GONE);
        PLVCastBusinessManager.getInstance().stopBrowse();
    }

    private void showNoResult() {
        showStatus(PLVAppUtils.getString(R.string.plv_cast_search_state_device_not_found));
        mPlvSearchStatusTv.setText(R.string.plv_cast_search_state_device_not_found_2);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="投屏状态监听">
    //投屏状态回调
    private IPLVCastUpdateListener mStateListener = new IPLVCastUpdateListener() {

        @Override
        public void onStateUpdate(int state, Object object) {
            PLVCommonLog.d(TAG, "IPlvCastUpdateListener: state: " + state + "object: " + object);
            switch (state) {
                case PLVCastBusinessManager.STATE_CONNECT_SUCCESS:
                    PLVMediaCastDevice info = (PLVMediaCastDevice) object;
                    if (TextUtils.isEmpty(info.getFriendlyName())) {
                        // pin码，则全部去掉
                        return;
                    }
                    setSelectInfo(info);
                    break;
                case PLVCastBusinessManager.STATE_SEARCH_SUCCESS:
                    List<PLVMediaCastDevice> infos = PLVCastBusinessManager.getInstance().getBrowseInfos();
                    screencastDeviceListAdapter.updateDatas(infos);
                    break;
                case PLVCastBusinessManager.STATE_SEARCH_ERROR:
                    if (object != null && object instanceof String) {
                        ToastUtils.showShort((String) object);
                    }
                    mPlvSearchingGif.setVisibility(View.GONE);
                    break;
                case PLVCastBusinessManager.STATE_SEARCH_STOP:
                    mPlvSearchingGif.setVisibility(View.GONE);
                    break;
                case PLVCastBusinessManager.STATE_SEARCH_NO_RESULT:
                    showNoResult();
                    break;
                case PLVCastBusinessManager.STATE_DISCONNECT:
                    ToastUtils.showShort((String) object);
                    selectNull();
                    break;
                case PLVCastBusinessManager.STATE_CONNECT_FAILURE:
                case PLVCastBusinessManager.STATE_STOP:
                case PLVCastBusinessManager.STATE_COMPLETION:
                    selectNull();
                    break;
            }
        }
    };
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="点击监听">
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.plv_search_iv) {
            browse();
        } else if (id == R.id.plv_back_iv) {
            stopBrowse();
            setVisibility(GONE);
        } else if (id == R.id.plv_retry_tv) {
            browse();
        }
    }
    // </editor-fold >


}
