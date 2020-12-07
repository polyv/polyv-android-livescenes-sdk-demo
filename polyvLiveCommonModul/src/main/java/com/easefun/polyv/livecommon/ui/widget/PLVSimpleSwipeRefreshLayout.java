package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class PLVSimpleSwipeRefreshLayout extends SwipeRefreshLayout {

    public PLVSimpleSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSimpleSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(false);
        //SwipeRefreshLayout启用setChildrenDrawingOrderEnabled并重写了getChildDrawingOrder
        //部分手机在(切换到横屏的时候)使用SwipeRefreshLayout添加view，会出现下面的异常
        //java.lang.IndexOutOfBoundsException: getChildDrawingOrder() returned invalid index 1 (child count is 1)
        //        at android.view.ViewGroup.getAndVerifyPreorderedIndex(ViewGroup.java:2038)
        //        at android.view.ViewGroup.populateChildrenForAutofill(ViewGroup.java:3619)
        //        at android.view.ViewGroup.getChildrenForAutofill(ViewGroup.java:3605)
        //        at android.view.ViewGroup.dispatchProvideAutofillStructure(ViewGroup.java:3587)
        //        at android.view.ViewGroup.dispatchProvideAutofillStructure(ViewGroup.java:3593)
        //        at android.view.ViewGroup.dispatchProvideAutofillStructure(ViewGroup.java:3593)
        //        at android.view.ViewGroup.dispatchProvideAutofillStructure(ViewGroup.java:3593)
        //        at android.view.ViewGroup.dispatchProvideAutofillStructure(ViewGroup.java:3593)
        //        at android.app.assist.AssistStructure$WindowNode.<init>(AssistStructure.java:517)
        //        at android.app.assist.AssistStructure.<init>(AssistStructure.java:2047)
        //        at android.app.ActivityThread.handleRequestAssistContextExtras(ActivityThread.java:3794)
        //        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2153)
        //        at android.os.Handler.dispatchMessage(Handler.java:112)
        //        at android.os.Looper.loop(Looper.java:216)
        //        at android.app.ActivityThread.main(ActivityThread.java:7625)
        //        at java.lang.reflect.Method.invoke(Native Method)
        //        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:524)
        //        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:987)
    }
}
