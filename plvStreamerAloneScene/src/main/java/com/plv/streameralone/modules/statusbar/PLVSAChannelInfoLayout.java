package com.plv.streameralone.modules.statusbar;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.livecommon.module.data.PLVStatefulData;
import com.plv.livecommon.module.utils.PLVToast;
import com.plv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.plv.livecommon.ui.widget.menudrawer.Position;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.streameralone.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 频道信息弹层
 *
 * @author suhongtao
 */
public class PLVSAChannelInfoLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private View rootView;
    private TextView plvsaChannelInfoTv;
    private LinearLayout plvsaChannelInfoLl;
    private LinearLayout plvsaChannelInfoNameLl;
    private TextView plvsaChannelInfoNameLabelTv;
    private TextView plvsaChannelInfoNameTv;
    private LinearLayout plvsaChannelInfoStartTimeLl;
    private TextView plvsaChannelInfoStartTimeLabelTv;
    private TextView plvsaChannelInfoStartTimeTv;
    private LinearLayout plvsaChannelInfoIdLl;
    private TextView plvsaChannelInfoIdLabelTv;
    private TextView plvsaChannelInfoIdTv;
    private Button plvsaChannelInfoIdCopyBtn;

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVSAChannelInfoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAChannelInfoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAChannelInfoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">
    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvsa_status_bar_channel_info_layout, this);
        findView();
        initCopyChannelIdButtonOnClickListener();
    }

    private void findView() {
        plvsaChannelInfoTv = (TextView) findViewById(R.id.plvsa_channel_info_tv);
        plvsaChannelInfoLl = (LinearLayout) findViewById(R.id.plvsa_channel_info_ll);
        plvsaChannelInfoNameLl = (LinearLayout) findViewById(R.id.plvsa_channel_info_name_ll);
        plvsaChannelInfoNameLabelTv = (TextView) findViewById(R.id.plvsa_channel_info_name_label_tv);
        plvsaChannelInfoNameTv = (TextView) findViewById(R.id.plvsa_channel_info_name_tv);
        plvsaChannelInfoStartTimeLl = (LinearLayout) findViewById(R.id.plvsa_channel_info_start_time_ll);
        plvsaChannelInfoStartTimeLabelTv = (TextView) findViewById(R.id.plvsa_channel_info_start_time_label_tv);
        plvsaChannelInfoStartTimeTv = (TextView) findViewById(R.id.plvsa_channel_info_start_time_tv);
        plvsaChannelInfoIdLl = (LinearLayout) findViewById(R.id.plvsa_channel_info_id_ll);
        plvsaChannelInfoIdLabelTv = (TextView) findViewById(R.id.plvsa_channel_info_id_label_tv);
        plvsaChannelInfoIdTv = (TextView) findViewById(R.id.plvsa_channel_info_id_tv);
        plvsaChannelInfoIdCopyBtn = (Button) findViewById(R.id.plvsa_channel_info_id_copy_btn);
    }

    /**
     * 复制按钮
     */
    private void initCopyChannelIdButtonOnClickListener() {
        plvsaChannelInfoIdCopyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //获取剪贴板管理器
                    ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", plvsaChannelInfoIdTv.getText());
                    // 将ClipData内容放到系统剪贴板里。
                    if (cm != null) {
                        cm.setPrimaryClip(mClipData);

                        PLVToast.Builder.context(getContext())
                                .setText("已复制")
                                .setTextColor(Color.WHITE)
                                .duration(Toast.LENGTH_SHORT)
                                .build().show();
                    }
                } catch (Exception e) {
                    PLVCommonLog.exception(e);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法 - 外部调用一次">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        setChannelNameAndId(liveRoomDataManager);
        observeChannelStartTime(liveRoomDataManager);
    }

    public void updateChannelName(String channelName) {
        plvsaChannelInfoNameLl.setVisibility(VISIBLE);
        plvsaChannelInfoNameTv.setText(channelName);
    }

    /**
     * 频道直播标题 & 频道号
     *
     * @param liveRoomDataManager
     */
    private void setChannelNameAndId(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (liveRoomDataManager == null) {
            plvsaChannelInfoNameLl.setVisibility(GONE);
            plvsaChannelInfoIdLl.setVisibility(GONE);
            return;
        } else {
            plvsaChannelInfoNameLl.setVisibility(VISIBLE);
            plvsaChannelInfoIdLl.setVisibility(VISIBLE);
        }

        plvsaChannelInfoNameTv.setText(liveRoomDataManager.getConfig().getChannelName());
        plvsaChannelInfoIdTv.setText(liveRoomDataManager.getConfig().getChannelId());
    }

    /**
     * 频道开播时间
     *
     * @param liveRoomDataManager
     */
    private void observeChannelStartTime(IPLVLiveRoomDataManager liveRoomDataManager) {
        plvsaChannelInfoStartTimeLl.setVisibility(GONE);

        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PLVLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PLVLiveClassDetailVO> plvLiveClassDetailVOPLVStatefulData) {
                if (plvLiveClassDetailVOPLVStatefulData == null
                        || !plvLiveClassDetailVOPLVStatefulData.isSuccess()
                        || plvLiveClassDetailVOPLVStatefulData.getData() == null
                        || plvLiveClassDetailVOPLVStatefulData.getData().getData() == null) {
                    plvsaChannelInfoStartTimeLl.setVisibility(GONE);
                    return;
                } else {
                    plvsaChannelInfoStartTimeLl.setVisibility(VISIBLE);
                }

                String startTime = plvLiveClassDetailVOPLVStatefulData.getData().getData().getStartTime();
                if (startTime == null || TextUtils.isEmpty(startTime.trim())) {
                    startTime = "未设置";
                }

                plvsaChannelInfoStartTimeTv.setText(startTime);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API 外部调用方法">
    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuSize(ConvertUtils.dp2px(300));
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                    }

                    ViewGroup popupContainer = (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
                    View maskView = ((Activity) getContext()).findViewById(R.id.plvsa_popup_container_mask);
                    if (popupContainer.getChildCount() > 0) {
                        maskView.setVisibility(View.VISIBLE);
                    } else {
                        maskView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
            menuDrawer.openMenu();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }
    // </editor-fold>

}
