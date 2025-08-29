package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 贴图类型选择布局
 */
public class PLVSAStickerLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 布局弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 布局宽度、高度、布局位置
    private static final int LAYOUT_WIDTH_LAND = ConvertUtils.dp2px(314);
    private static final int LAYOUT_HEIGHT_PORT = ConvertUtils.dp2px(214);
    // 布局背景
    private static final int LAYOUT_BACKGROUND_RES_PORT = R.drawable.plvsa_setting_bitrate_ly_shape;
    private static final int LAYOUT_BACKGROUND_RES_LAND = R.drawable.plvsa_setting_bitrate_ly_shape_land;
    private static PLVSAStickerLayout instance;

    //布局弹层
    private PLVMenuDrawer menuDrawer;

    //view
    private RelativeLayout plvsaSettingStickerLayoutRoot;
    private TextView plvsaSettingStickerText;
    private TextView plvsaSettingStickerImage;

    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="静态方法">
    public static PLVSAStickerLayout init(Context context, OnViewActionListener listener) {
        if (instance == null) {
            instance = new PLVSAStickerLayout(context);
            instance.setOnViewActionListener(listener);
        }
        return instance;
    }

    @Nullable
    public static PLVSAStickerLayout useInstance() {
        return instance;
    }

    public static void tryShow() {
        if (instance != null) {
            instance.open();
        }
    }

    public static void destroy() {
        instance = null;
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStickerLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStickerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStickerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_setting_sticker_layout, this, true);

        plvsaSettingStickerLayoutRoot = findViewById(R.id.plv_setting_sticker_layout_root);
        plvsaSettingStickerText = findViewById(R.id.plvsa_setting_sticker_text);
        plvsaSettingStickerImage = findViewById(R.id.plvsa_setting_sticker_image);

        plvsaSettingStickerText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClick(true);
                }
            }
        });
        plvsaSettingStickerImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClick(false);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        updateViewWithOrientation();

        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuSize(PLVScreenUtils.isPortrait(getContext()) ? LAYOUT_HEIGHT_PORT : LAYOUT_WIDTH_LAND);
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
            menuDrawer.setMenuSize(PLVScreenUtils.isPortrait(getContext()) ? LAYOUT_HEIGHT_PORT : LAYOUT_WIDTH_LAND);
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

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
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

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        Position menuDrawerPosition;
        LayoutParams layoutParam = (LayoutParams) plvsaSettingStickerLayoutRoot.getLayoutParams();

        if (PLVScreenUtils.isPortrait(getContext())) {
            plvsaSettingStickerLayoutRoot.setBackgroundResource(LAYOUT_BACKGROUND_RES_PORT);
            menuDrawerPosition = MENU_DRAWER_POSITION_PORT;
        } else {
            plvsaSettingStickerLayoutRoot.setBackgroundResource(LAYOUT_BACKGROUND_RES_LAND);
            menuDrawerPosition = MENU_DRAWER_POSITION_LAND;
        }

        plvsaSettingStickerLayoutRoot.setLayoutParams(layoutParam);
        if (menuDrawer != null) {
            menuDrawer.setPosition(menuDrawerPosition);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onClick(boolean isClickText);
    }
    // </editor-fold>
}
