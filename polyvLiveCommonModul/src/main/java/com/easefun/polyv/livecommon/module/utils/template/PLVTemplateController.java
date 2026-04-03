package com.easefun.polyv.livecommon.module.utils.template;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.utils.virtualbg.PLVVirtualBackgroundLayout;
import com.easefun.polyv.livecommon.module.utils.water.PLVPhotoContainer;
import com.easefun.polyv.livescenes.model.template.PLVWaterTemplateVO;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

public class PLVTemplateController {
    private static PLVTemplateController instance;
    private PLVTemplateDialog templateDialog;
    private PLVVirtualBackgroundLayout virtualBackgroundLayout;
    private PLVPhotoContainer waterLayout;
    private Context context;

    public static boolean tryShowTemplateDialog() {
        if (instance != null) {
            return instance.showTemplateDialog();
        }
        return false;
    }

    public static void destroy() {
        instance = null;
    }

    public PLVTemplateController(Context context) {
        instance = this;
        this.context = context;
        this.templateDialog = new PLVTemplateDialog(context);
        this.templateDialog.setOnTemplateSelectListener(new PLVTemplateDialog.OnTemplateSelectListener() {
            @Override
            public void onTemplateSelect(PLVWaterTemplateVO item, int position) {
                if (virtualBackgroundLayout == null || waterLayout == null) {
                    return;
                }
                if (virtualBackgroundLayout.isFeatureEnabled()) {
                    setVirtualBg(item.getBackgroundConfig());
                }
                if (waterLayout.isFeatureEnabled()) {
                    setWater(item.getLayers());
                }
            }

            @Override
            public void onDialogDismiss() {
            }
        });
    }

    public void setup(PLVPhotoContainer waterLayout, PLVVirtualBackgroundLayout virtualBackgroundLayout) {
        this.waterLayout = waterLayout;
        this.virtualBackgroundLayout = virtualBackgroundLayout;
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        liveRoomDataManager.getTemplateListData().observe((LifecycleOwner) context, new Observer<PLVStatefulData<List<PLVWaterTemplateVO>>>() {

            @Override
            public void onChanged(@Nullable PLVStatefulData<List<PLVWaterTemplateVO>> listPLVStatefulData) {
                if (listPLVStatefulData != null && listPLVStatefulData.isSuccess()) {
                    templateDialog.setTemplateList(listPLVStatefulData.getData());
                }
            }
        });
    }

    public boolean showTemplateDialog() {
        if (PLVScreenUtils.isLandscape(context)) {
            ToastUtils.showShort(R.string.plv_streamer_setting_template_no_support_tips);
            return false;
        }
        templateDialog.show();
        return true;
    }

    private void setVirtualBg(PLVWaterTemplateVO.BackgroundConfig bgConfig) {
        virtualBackgroundLayout.reset();
        if (bgConfig == null) {
            return;
        }
        if (bgConfig.isPreset()) {
            virtualBackgroundLayout.setBgByStyle(bgConfig.getBgStyle());
        } else if (!TextUtils.isEmpty(bgConfig.getUrl())) {
            virtualBackgroundLayout.setBgByUrl(bgConfig.getUrl());
        }
    }

    private void setWater(List<PLVWaterTemplateVO.Layer> layers) {
        waterLayout.clear();
        if (layers == null || layers.isEmpty()) {
            return;
        }
        for (int i = layers.size() - 1; i >= 0; i--) {
            PLVWaterTemplateVO.Layer layer = layers.get(i);
            if (layer.isImage()) {
                waterLayout.addImage(layer.getSrc(), layer.getX(), layer.getY(), layer.getWidth(), layer.getHeight(), new Runnable() {
                    @Override
                    public void run() {
                        waterLayout.hideAllBorders();
                    }
                });
            } else if (layer.isTextTemplate()) {
                waterLayout.addText(layer.getMainText(), layer.getTextStyle(), layer.getX(), layer.getY(), layer.getWidth(), layer.getHeight());
            }
        }
        waterLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                waterLayout.hideAllBorders();
            }
        }, 300);
    }
}
