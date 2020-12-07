package com.easefun.polyv.livecommon.module.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PLVWebUtils {

    public static void openWebLink(String action, Context activity){
        Intent intent = new Intent();
        if(!action.startsWith("http")){
            action = "https://"+action;
        }
        intent.setData(Uri.parse(action));//Url 就是你要打开的网址
        intent.setAction(Intent.ACTION_VIEW);
        activity.startActivity(intent); //启动浏览器
    }
}
