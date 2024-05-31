package com.easefun.polyv.livecommon.module.modules.interact;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVJsonUtils;
import com.plv.foundationsdk.utils.PLVSDCardUtils;
import com.plv.livescenes.feature.interact.PLVStreamerInteractWebView;
import com.plv.livescenes.feature.interact.download.IPLVInteractFileDownloadListener;
import com.plv.livescenes.feature.interact.download.PLVInteractFileDownloadManager;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class PLVStreamerInteractLayout extends FrameLayout implements IPLVStreamerInteractLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVStreamerInteractLayout.class.getSimpleName();
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVStreamerInteractWebView plvStreamerInteractWeb;

    private static final String EVENT_DOWNLOADSIGNINRECORD = "downloadSignInRecord";

    private static final List<String> JS_HANDLER = listOf(
            PLVInteractJSBridgeEventConst.V2_GET_NATIVE_APP_PARAMS_INFO,
            PLVInteractJSBridgeEventConst.V2_SHOW_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_CLOSE_WEB_VIEW,
            PLVInteractJSBridgeEventConst.V2_CALL_APP_EVENT
    );


    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVStreamerInteractLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVStreamerInteractLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVStreamerInteractLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_streamer_interact_layout, this, true);
        plvStreamerInteractWeb = findViewById(R.id.plv_streamer_interact_web);
        setVisibility(View.INVISIBLE);

        for (final String event : JS_HANDLER) {
            plvStreamerInteractWeb.registerHandler(event, new BridgeHandler() {
                @Override
                public void handler(String param, CallBackFunction callBackFunction) {
                    PLVCommonLog.d(TAG, event + ", param= " + param);
                    handlerJsCall(event, param, callBackFunction);
                }
            });
        }

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        plvStreamerInteractWeb.setLang(PLVLanguageUtil.isENLanguage() ? PLVStreamerInteractWebView.LANG_EN : PLVStreamerInteractWebView.LANG_ZH);
        plvStreamerInteractWeb.loadWeb();

    }

    @Override
    public void showSignIn() {
        String data = "{\"event\" : \"SHOW_SIGN\"}";
        plvStreamerInteractWeb.sendMsgToJs(PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT, data, new CallBackFunction() {
            @Override
            public void onCallBack(String s) {
                PLVCommonLog.d(TAG, PLVInteractJSBridgeEventConst.V2_APP_CALL_WEB_VIEW_EVENT + " " + s);
            }
        });
    }

    @Override
    public boolean onBackPress() {
        if (getVisibility() == View.VISIBLE) {
            setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (plvStreamerInteractWeb != null) {
            plvStreamerInteractWeb.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void destroy() {
        if (plvStreamerInteractWeb != null) {
            plvStreamerInteractWeb.removeAllViews();
            ViewParent viewParent = plvStreamerInteractWeb.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(plvStreamerInteractWeb);
            }
            plvStreamerInteractWeb.destroy();
            plvStreamerInteractWeb = null;
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private void processCallAppEvent(String param, CallBackFunction callBackFunction) {
        PLVCommonLog.d(TAG, "CallAppEvent param = " + param);
        try {
            JSONObject jsonObject = new JSONObject(param);
            String event = PLVJsonUtils.getString(jsonObject, "event", "");
           JSONObject value = PLVJsonUtils.getObject(jsonObject,"value", null);
            switch (event) {
                case EVENT_DOWNLOADSIGNINRECORD:
                    processorDownloadSignInRecord(value);
                    break;
                default:
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void handlerJsCall(String event, String param, CallBackFunction callBackFunction) {
        switch (event) {
            case PLVInteractJSBridgeEventConst.V2_GET_NATIVE_APP_PARAMS_INFO:
                processGetNativeAppParamsInfo(param, callBackFunction);
                break;
            case PLVInteractJSBridgeEventConst.V2_CLOSE_WEB_VIEW:
                processWebViewVisibility(true);
                break;
            case PLVInteractJSBridgeEventConst.V2_SHOW_WEB_VIEW:
                processWebViewVisibility(false);
                break;
            case PLVInteractJSBridgeEventConst.V2_CALL_APP_EVENT:
                processCallAppEvent(param, callBackFunction);
                break;
        }
    }

    private void processGetNativeAppParamsInfo(String param, CallBackFunction callBackFunction) {
        String nativeAppPramsInfo = getNativeAppPramsInfo();
        PLVCommonLog.d(TAG, "processGetNativeAppParamsInfo= " + nativeAppPramsInfo);
        callBackFunction.onCallBack(nativeAppPramsInfo);
    }

    private String getNativeAppPramsInfo() {
        if (liveRoomDataManager != null) {
            PLVInteractNativeAppParams nativeAppParams = PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager);
            return PLVGsonUtil.toJsonSimple(nativeAppParams);
        }
        return "";
    }

    private void processWebViewVisibility(boolean close) {
        PLVCommonLog.d(TAG, "processWebViewVisibility close: " + close);
        setVisibility(close ? View.INVISIBLE : View.VISIBLE);
    }

    private void processorDownloadSignInRecord(JSONObject value) {
        if (value != null) {
            PLVCommonLog.d(TAG, "processDownloadSignRecord is: " + value.toString());
            final String url = PLVJsonUtils.getString(value, "downloadURL", "");
            if (TextUtils.isEmpty(url)) {
                ToastUtils.showShort(R.string.plv_live_save_sign_in_failed, " url is empty");
                return;
            }
            ArrayList<String> permissions = new ArrayList<>(1);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            PLVFastPermission.getInstance()
                    .start((Activity) getContext(), permissions, new PLVOnPermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            downloadSignRecord(url);
                        }

                        @Override
                        public void onPartialGranted(ArrayList<String> grantedPermissions,
                                                     ArrayList<String> deniedPermissions,
                                                     ArrayList<String> deniedForeverP) {
                            if (deniedForeverP != null && !deniedForeverP.isEmpty()) {
                                new AlertDialog.Builder(getContext()).setTitle(R.string.plv_common_dialog_tip)
                                        .setMessage(R.string.plv_live_no_save_record_permission_hint)
                                        .setPositiveButton(R.string.plv_common_dialog_confirm_2, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                PLVFastPermission.getInstance().jump2Settings(getContext());
                                            }
                                        })
                                        .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                PLVCommonLog.d(TAG, "cancel");
                                            }
                                        }).setCancelable(false).show();
                            } else {
                                ToastUtils.showShort(PLVAppUtils.getString(R.string.plv_live_allow_permission_save_img_hint));
                            }
                        }
                    });
        }
    }

    private void downloadSignRecord(String url) {

        final String savePath = PLVSDCardUtils.createPath(getContext(), "PLVSignInRecord");
        PLVInteractFileDownloadManager.getInstance().requestDownLoadFile(url, savePath, new IPLVInteractFileDownloadListener() {
            @Override
            public void onSuccess() {
                ToastUtils.showShort(R.string.plv_live_save_record_path, savePath);
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                ToastUtils.showShort(R.string.plv_live_save_record_failed_2, "loadFailed");
            }
        });
    }


    // </editor-fold >

}
