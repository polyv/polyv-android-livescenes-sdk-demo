package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import static com.plv.foundationsdk.utils.PLVSugarUtil.arrayListOf;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.model.interact.PLVChatFunctionVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PLVECMoreLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //功能类型
    /**
     * 音视频模式
     */
    public static final String MORE_FUNCTION_TYPE_PLAY_MODE = "MORE_FUNCTION_TYPE_PLAY_MODE";
    /**
     * 线路
     */
    public static final String MORE_FUNCTION_TYPE_ROUTE = "MORE_FUNCTION_TYPE_ROUTE";
    /**
     * 清晰度
     */
    public static final String MORE_FUNCTION_TYPE_DEFINITION = "MORE_FUNCTION_TYPE_DEFINITION";
    /**
     * 无延迟观看模式
     */
    public static final String MORE_FUNCTION_TYPE_LATENCY = "MORE_FUNCTION_TYPE_LATENCY";
    /**
     * 小窗播放
     */
    public static final String MORE_FUNCTION_TYPE_FLOATING = "MORE_FUNCTION_TYPE_FLOATING";
    /**
     * 播放速度
     */
    public static final String MORE_FUNCTION_TYPE_RATE = "MORE_FUNCTION_TYPE_RATE";
    /**
     * 语言切换
     */
    public static final String MORE_FUNCTION_TYPE_LANGUAGE_SWITCH = "MORE_FUNCTION_TYPE_LANGUAGE_SWITCH";

    /**
     * 抽奖挂件事件
     */
    public static final String CLICK_LOTTERY_PENDANT= "CLICK_LOTTERY_PENDANT";


    //每行显示的功能模块数量
    public static final int LAYOUT_SPAN_COUNT = 5;

    //初始化支持的功能模块
    private final ArrayList<PLVChatFunctionVO> functionList = arrayListOf(
            new PLVChatFunctionVO(MORE_FUNCTION_TYPE_PLAY_MODE, R.drawable.plvec_play_mode_sel, PLVAppUtils.getString(R.string.plv_player_audio_mode), false),
            new PLVChatFunctionVO(MORE_FUNCTION_TYPE_ROUTE, R.drawable.plvec_route, PLVAppUtils.getString(R.string.plv_player_route), false),
            new PLVChatFunctionVO(MORE_FUNCTION_TYPE_DEFINITION, R.drawable.plvec_definition, PLVAppUtils.getString(R.string.plv_player_definition), false),
            new PLVChatFunctionVO(MORE_FUNCTION_TYPE_LATENCY, R.drawable.plvec_live_more_latency, PLVAppUtils.getString(R.string.plv_player_mode), false),
            new PLVChatFunctionVO(MORE_FUNCTION_TYPE_FLOATING, R.drawable.plvec_live_more_floating_icon, PLVAppUtils.getString(R.string.plv_player_floating), false),
            new PLVChatFunctionVO(MORE_FUNCTION_TYPE_RATE, R.drawable.plvec_live_more_rate, PLVAppUtils.getString(R.string.plv_player_speed), false),
            new PLVChatFunctionVO(MORE_FUNCTION_TYPE_LANGUAGE_SWITCH, R.drawable.plvec_live_more_language_switch, PLVAppUtils.getString(R.string.plv_live_language_switch), true)
    );

    //初始化不支持的事件
    private ArrayList<String> unacceptFunctions = arrayListOf(CLICK_LOTTERY_PENDANT);

    //功能响应监听
    private PLVECFunctionListener functionListener;
    //功能列表
    private RecyclerView chatMoreRv;
    //功能列表适配器
    private PLVECMoreAdapter adapter;
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECMoreLayout(Context context) {
        super(context);
        initView();
    }

    public PLVECMoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVECMoreLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_more_layout, this, true);
        chatMoreRv = findViewById(R.id.plvec_chat_more_rv);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), LAYOUT_SPAN_COUNT);
        chatMoreRv.setLayoutManager(layoutManager);
        adapter = new PLVECMoreAdapter(LAYOUT_SPAN_COUNT, getContext());
        adapter.setData(functionList);
        adapter.setListener(new PLVECMoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String type, View iconView) {
                if (functionListener != null) {
                    String data = String.format("{\"event\" : \"%s\"}", type);
                    functionListener.onFunctionCallback(type, data, iconView);
                }
            }
        });
        chatMoreRv.setAdapter(adapter);

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API">

    /**
     * 增加功能按钮（通过互动应用控制）
     */
    public void updateFunctionView(PLVWebviewUpdateAppStatusVO functionsVO) {
        if (functionsVO == null || functionsVO.getValue() == null || functionsVO.getValue().getDataArray() == null) {
            return;
        }

        final List<String> sortList = new ArrayList<>();
        List<PLVWebviewUpdateAppStatusVO.Value.Function> functions = functionsVO.getValue().getDataArray();
        for (PLVWebviewUpdateAppStatusVO.Value.Function function : functions) {
            sortList.add(function.getEvent());
            int index = -1;
            for (int i = 0; i < functionList.size(); i++) {
                if (function.getEvent().equals(functionList.get(i).getType())) {
                    index = i;
                    break;
                }
            }

            for (String unacceptFunction : unacceptFunctions) {
                if (function.getEvent().equals(unacceptFunction)) {
                    return;
                }
            }

            PLVChatFunctionVO chatFunctionVO = new PLVChatFunctionVO(function.getEvent(), function.getTitle(), function.isShow(), function.getIcon());
            if (index < 0) {
                //不存在已有list里面，添加
                functionList.add(chatFunctionVO);
            } else {
                functionList.set(index, chatFunctionVO);
            }
        }
        Collections.sort(functionList, new Comparator<PLVChatFunctionVO>() {
            @Override
            public int compare(PLVChatFunctionVO o1, PLVChatFunctionVO o2) {
                String event1 = o1.getType();
                String event2 = o2.getType();
                int index1 = sortList.indexOf(event1);
                int index2 = sortList.indexOf(event2);
                return index1 - index2;
            }
        });

        if (adapter != null) {
            adapter.updateFunctionList(functionList);
        }

    }

    /**
     * 设置功能回调监听
     */
    public void setFunctionListener(PLVECFunctionListener functionListener) {
        this.functionListener = functionListener;
    }

    public void updateFunctionShow(String type, boolean isShow) {
        for (PLVChatFunctionVO functionVO : functionList) {
            if (type.equals(functionVO.getType())) {
                functionVO.setShow(isShow);
                break;
            }
        }
        adapter.updateFunctionList(functionList);
    }

    public void updateFunctionNew(String type, boolean isShow, boolean hasNew) {
        for (PLVChatFunctionVO functionVO : functionList) {
            if (type.equals(functionVO.getType())) {
                functionVO.setShow(isShow);
                break;
            }
        }
        adapter.updateFunctionList(functionList);
    }

    @Nullable
    public PLVChatFunctionVO getFunctionByType(String type) {
        for (PLVChatFunctionVO functionVO : functionList) {
            if (type.equals(functionVO.getType())) {
                return functionVO;
            }
        }
        return null;
    }

    public void updateFunctionStatus(@NonNull PLVChatFunctionVO functionVO) {
        for (int i = 0; i < functionList.size(); i++) {
            if (functionVO.getType() != null && functionVO.getType().equals(functionList.get(i).getType())) {
                functionList.set(i, functionVO);
                break;
            }
        }
        adapter.updateFunctionList(functionList);
    }
    // </editor-fold >

}
