package com.easefun.polyv.livecommon.ui.widget.itemview;

/**
 * 基础列表item数据
 */
public class PLVBaseViewData<Data> {
    public static final int ITEMTYPE_UNDEFINED = 0;
    private Data data;
    private int itemType;
    private Object tag;

    public PLVBaseViewData(Data data, int itemType) {
        this.data = data;
        this.itemType = itemType;
    }

    public PLVBaseViewData(Data data, int itemType, Object tag) {
        this.data = data;
        this.itemType = itemType;
        this.tag = tag;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
