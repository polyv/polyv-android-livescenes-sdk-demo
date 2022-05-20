package com.easefun.polyv.livecloudclass.modules.chatroom.chatmore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.easefun.polyv.livecloudclass.R;
import com.plv.livescenes.model.interact.PLVChatFunctionVO;

import java.util.ArrayList;
import java.util.Arrays;

public class PLVLCChatMoreLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //功能类型
    /**
     * 只看讲师
     */
    public static final int CHAT_FUNCTION_TYPE_ONLY_TEACHER = 1;
    /**
     * 发送图片
     */
    public static final int CHAT_FUNCTION_TYPE_SEND_IMAGE = 2;
    /**
     * 拍摄
     */
    public static final int CHAT_FUNCTION_TYPE_OPEN_CAMERA = 3;
    /**
     * 公告
     */
    public static final int CHAT_FUNCTION_TYPE_BULLETIN = 4;
    /**
     * 消息
     */
    public static final int CHAT_FUNCTION_TYPE_MESSAGE = 5;
    /**
     * 屏蔽/展示 特效
     */
    public static final int CHAT_FUNCTION_TYPE_EFFECT = 6;


    //每行显示的功能模块数量
    public static final int LAYOUT_SPAN_COUNT = 4;

    //初始化支持的功能模块
    private ArrayList<PLVChatFunctionVO> functionList = new ArrayList<>(
            Arrays.asList(
                    new PLVChatFunctionVO(CHAT_FUNCTION_TYPE_ONLY_TEACHER, R.drawable.plvlc_chatroom_btn_view_message_selector, "只看讲师", true),
                    new PLVChatFunctionVO(CHAT_FUNCTION_TYPE_SEND_IMAGE, R.drawable.plvlc_chatroom_btn_img_send, "发送图片", false),
                    new PLVChatFunctionVO(CHAT_FUNCTION_TYPE_OPEN_CAMERA, R.drawable.plvlc_chatroom_btn_camera, "拍摄", false),
                    new PLVChatFunctionVO(CHAT_FUNCTION_TYPE_BULLETIN, R.drawable.plvlc_chatroom_btn_bulletin_show, "公告", true),
                    new PLVChatFunctionVO(CHAT_FUNCTION_TYPE_MESSAGE, R.drawable.plvlc_chatroom_btn_message, "消息", false),
                    new PLVChatFunctionVO(CHAT_FUNCTION_TYPE_EFFECT, R.drawable.plvlc_chatroom_btn_view_effect_selector, "屏蔽特效", false)
            )
    );

    //功能响应监听
    private PLVLCChatFunctionListener functionListener;
    //功能列表
    private RecyclerView chatMoreRv;
    //功能列表适配器
    private PLVLCChatMoreAdapter adapter;
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCChatMoreLayout(Context context) {
        super(context);
        initView();
    }

    public PLVLCChatMoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLCChatMoreLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_chatroom_chat_more_layout, this, true);
        chatMoreRv = findViewById(R.id.plvlc_chat_more_rv);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), LAYOUT_SPAN_COUNT);
        chatMoreRv.setLayoutManager(layoutManager);
        adapter = new PLVLCChatMoreAdapter(LAYOUT_SPAN_COUNT, getContext());
        adapter.setData(functionList);
        adapter.setListener(new PLVLCChatMoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int type) {
                if (functionListener != null) {
                    functionListener.onFunctionCallback(type);
                }
            }
        });
        chatMoreRv.setAdapter(adapter);

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API">

    /**
     * 设置功能回调监听
     */
    public void setFunctionListener(PLVLCChatFunctionListener functionListener) {
        this.functionListener = functionListener;
    }

    public void updateFunctionShow(int type, boolean isShow) {
        for (int i = 0; i < functionList.size(); i++) {
            PLVChatFunctionVO functionVO = functionList.get(i);
            if (type == functionVO.getType()) {
                functionList.get(i).setShow(isShow);
                break;
            }
        }
        adapter.updateFunctionList(functionList);
    }

    public void updateFunctionNew(int type, boolean isShow, boolean hasNew) {
        for (int i = 0; i < functionList.size(); i++) {
            PLVChatFunctionVO functionVO = functionList.get(i);
            if (type == functionVO.getType()) {
                functionList.get(i).setShow(isShow);
                functionList.get(i).setHasNew(hasNew);
                break;
            }
        }
        adapter.updateFunctionList(functionList);
    }

    public PLVChatFunctionVO getFunctionByType(int type) {
        for (int i = 0; i < functionList.size(); i++) {
            PLVChatFunctionVO functionVO = functionList.get(i);
            if (type == functionVO.getType()) {
                return functionVO;
            }
        }
        return null;
    }

    public void updateFunctionStatus(@NonNull PLVChatFunctionVO functionVO) {
        for (int i = 0; i < functionList.size(); i++) {
            if (functionVO.getType() == functionList.get(i).getType()) {
                functionList.set(i, functionVO);
                break;
            }
        }
        adapter.updateFunctionList(functionList);
    }
    // </editor-fold >

}
