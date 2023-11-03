package com.easefun.polyv.livecommon.ui.widget.seekbar;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2018/5/8
 * 描    述:
 * ================================================
 */
public class PLVSavedState extends View.BaseSavedState {
    public float minValue;
    public float maxValue;
    public float rangeInterval;
    public int tickNumber;
    public float currSelectedMin;
    public float currSelectedMax;

    public PLVSavedState(Parcelable superState) {
        super(superState);
    }

    private PLVSavedState(Parcel in) {
        super(in);
        minValue = in.readFloat();
        maxValue = in.readFloat();
        rangeInterval = in.readFloat();
        tickNumber = in.readInt();
        currSelectedMin = in.readFloat();
        currSelectedMax = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeFloat(minValue);
        out.writeFloat(maxValue);
        out.writeFloat(rangeInterval);
        out.writeInt(tickNumber);
        out.writeFloat(currSelectedMin);
        out.writeFloat(currSelectedMax);
    }

    public static final Creator<PLVSavedState> CREATOR = new Creator<PLVSavedState>() {
        public PLVSavedState createFromParcel(Parcel in) {
            return new PLVSavedState(in);
        }

        public PLVSavedState[] newArray(int size) {
            return new PLVSavedState[size];
        }
    };
}
