package com.easefun.polyv.livestreamer.modules.chatroom.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livestreamer.R;

import java.util.List;

public class PLVLSEmoGridViewAdapter extends BaseAdapter {
    private List<String> lists;
    private Context context;
    private LayoutInflater inflater;

    public PLVLSEmoGridViewAdapter(List<String> lists, Context context) {
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
            convertView = inflater.inflate(R.layout.plvls_chatroom_chat_emoji_gridview_item, null);
        ImageView iv_emo = PLVViewInitUtils.get(convertView, R.id.plvls_chatroom_emoji_iv);
        if (iv_emo.getTag() == null) {
            int id = PLVFaceManager.getInstance().getFaceId(lists.get(position));
            Drawable drawable = context.getResources().getDrawable(id);
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                bitmap = PLVFaceManager.eraseColor(bitmap, Color.parseColor("#2B2C35"));
                iv_emo.setImageBitmap(bitmap);
            } else {
                iv_emo.setImageDrawable(drawable);
            }
            iv_emo.setTag(iv_emo.getId());
        }
        return convertView;
    }
}
