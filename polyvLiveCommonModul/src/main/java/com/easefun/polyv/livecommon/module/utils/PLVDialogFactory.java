package com.easefun.polyv.livecommon.module.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.easefun.polyv.livecommon.R;

/**
 * date: 2020/9/9
 * author: HWilliamgo
 * description: 对话框工厂类
 * 对话框的创建全部统一写在该工厂类中，方便对各种样式的对话框都进行统一的处理，例如对话框样式
 */
public class PLVDialogFactory {

    /**
     * 创建确认对话框
     *
     * @param context         上下文
     * @param message         对话框主体确认内容
     * @param positiveMsg     确认按钮的文本
     * @param onClickListener 点击确认按钮监听器
     * @return Dialog，请调用show
     */
    public static Dialog createConfirmDialog(Context context, String message, String positiveMsg, DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(positiveMsg, onClickListener)
                .setNegativeButton(R.string.plv_common_dialog_click_wrong, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
}
