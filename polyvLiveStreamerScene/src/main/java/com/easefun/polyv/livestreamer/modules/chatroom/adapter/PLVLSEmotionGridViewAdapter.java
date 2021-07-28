package com.easefun.polyv.livestreamer.modules.chatroom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.livestreamer.R;

import java.util.List;

/**
 * 个性化表情
 */
public class PLVLSEmotionGridViewAdapter extends BaseAdapter {
    private List<PLVEmotionImageVO.EmotionImage> lists;
    private Context context;
    private LayoutInflater inflater;

    public PLVLSEmotionGridViewAdapter(List<PLVEmotionImageVO.EmotionImage> lists, Context context) {
        this.lists = lists;
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.plvls_chatroom_chat_emotion_gridview_item, null);
        ImageView iv_emo = PLVViewInitUtils.get(convertView, R.id.plvls_emotion_iv);
        TextView tv_emo = PLVViewInitUtils.get(convertView, R.id.plvls_emotion_name_tv);
        
        String url = PLVFaceManager.getInstance().getEmotionUrl(lists.get(position).getId());
        PLVImageLoader.getInstance().loadImage(url, iv_emo);
        tv_emo.setText(lists.get(position).getTitle());
        return convertView;
    }


}
