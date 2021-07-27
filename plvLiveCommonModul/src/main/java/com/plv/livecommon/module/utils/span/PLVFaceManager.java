package com.plv.livecommon.module.utils.span;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.plv.livecommon.R;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表情集合类
 */
public class PLVFaceManager {

    private PLVFaceManager() {
        initFaceMap();
    }

    private static PLVFaceManager instance;

    public static PLVFaceManager getInstance() {
        if (null == instance)
            instance = new PLVFaceManager();
        return instance;
    }

    public static Drawable eraseColor(Drawable drawable, int id) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap = eraseColor(bitmap, Color.WHITE);
            bitmap = eraseColor(bitmap, Color.rgb(230, 230, 230));
            if (id != R.drawable.plv_33 && id != R.drawable.plv_71)
                bitmap = eraseColor(bitmap, Color.BLACK);
            return new BitmapDrawable(bitmap);
        } else {
            return drawable;
        }
    }

    public static Bitmap eraseColor(Bitmap src, int color) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap b = src.copy(Bitmap.Config.ARGB_8888, true);
        b.setHasAlpha(true);
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width * height; i++) {
            if (pixels[i] == color) {
                pixels[i] = 0;
            }
        }
        b.setPixels(pixels, 0, width, 0, 0, width, height);
        return b;
    }

    public Map<String, Integer> getFaceMap() {
        return mFaceMap;
    }

    private Map<String, Integer> mFaceMap;

    private void initFaceMap() {
        mFaceMap = new LinkedHashMap<String, Integer>();
        mFaceMap.put("[微笑]", R.drawable.plv_1);
        mFaceMap.put("[撇嘴]", R.drawable.plv_2);
        mFaceMap.put("[色]", R.drawable.plv_3);
        mFaceMap.put("[发呆]", R.drawable.plv_4);
        mFaceMap.put("[得意]", R.drawable.plv_5);
        mFaceMap.put("[流泪]", R.drawable.plv_6);
        mFaceMap.put("[害羞]", R.drawable.plv_7);
        mFaceMap.put("[闭嘴]", R.drawable.plv_8);
        mFaceMap.put("[睡]", R.drawable.plv_9);
        mFaceMap.put("[大哭]", R.drawable.plv_10);
        mFaceMap.put("[尴尬]", R.drawable.plv_11);
        mFaceMap.put("[发怒]", R.drawable.plv_12);
        mFaceMap.put("[调皮]", R.drawable.plv_13);
        mFaceMap.put("[呲牙]", R.drawable.plv_14);
        mFaceMap.put("[惊讶]", R.drawable.plv_15);
        mFaceMap.put("[难过]", R.drawable.plv_16);
        mFaceMap.put("[酷]", R.drawable.plv_17);
        mFaceMap.put("[冷汗]", R.drawable.plv_18);
        mFaceMap.put("[抓狂]", R.drawable.plv_19);
        mFaceMap.put("[吐]", R.drawable.plv_20);

        mFaceMap.put("[偷笑]", R.drawable.plv_21);
        mFaceMap.put("[可爱]", R.drawable.plv_22);
        mFaceMap.put("[白眼]", R.drawable.plv_23);
        mFaceMap.put("[傲慢]", R.drawable.plv_24);
        mFaceMap.put("[饥饿]", R.drawable.plv_25);
        mFaceMap.put("[困]", R.drawable.plv_26);
        mFaceMap.put("[惊恐]", R.drawable.plv_27);
        mFaceMap.put("[流汗]", R.drawable.plv_28);
        mFaceMap.put("[憨笑]", R.drawable.plv_29);
        mFaceMap.put("[大兵]", R.drawable.plv_30);
        mFaceMap.put("[奋斗]", R.drawable.plv_31);
        mFaceMap.put("[咒骂]", R.drawable.plv_32);
        mFaceMap.put("[疑问]", R.drawable.plv_33);
        mFaceMap.put("[嘘]", R.drawable.plv_34);
        mFaceMap.put("[晕]", R.drawable.plv_35);
        mFaceMap.put("[折磨]", R.drawable.plv_36);
        mFaceMap.put("[衰]", R.drawable.plv_37);
        mFaceMap.put("[骷髅]", R.drawable.plv_38);
        mFaceMap.put("[敲打]", R.drawable.plv_39);
        mFaceMap.put("[再见]", R.drawable.plv_40);

        mFaceMap.put("[擦汗]", R.drawable.plv_41);
        mFaceMap.put("[抠鼻]", R.drawable.plv_42);
        mFaceMap.put("[鼓掌]", R.drawable.plv_43);
        mFaceMap.put("[糗大了]", R.drawable.plv_44);
        mFaceMap.put("[坏笑]", R.drawable.plv_45);
        mFaceMap.put("[左哼哼]", R.drawable.plv_46);
        mFaceMap.put("[右哼哼]", R.drawable.plv_47);
        mFaceMap.put("[哈欠]", R.drawable.plv_48);
        mFaceMap.put("[鄙视]", R.drawable.plv_49);
        mFaceMap.put("[委屈]", R.drawable.plv_50);
        mFaceMap.put("[快哭了]", R.drawable.plv_51);
        mFaceMap.put("[阴险]", R.drawable.plv_52);
        mFaceMap.put("[亲亲]", R.drawable.plv_53);
        mFaceMap.put("[吓]", R.drawable.plv_54);
        mFaceMap.put("[可怜]", R.drawable.plv_55);
        mFaceMap.put("[菜刀]", R.drawable.plv_56);
        mFaceMap.put("[西瓜]", R.drawable.plv_57);
        mFaceMap.put("[啤酒]", R.drawable.plv_58);
        mFaceMap.put("[篮球]", R.drawable.plv_59);
        mFaceMap.put("[乒乓]", R.drawable.plv_60);


        mFaceMap.put("[咖啡]", R.drawable.plv_61);
        mFaceMap.put("[饭]", R.drawable.plv_62);
        mFaceMap.put("[猪头]", R.drawable.plv_63);
        mFaceMap.put("[玫瑰]", R.drawable.plv_64);
        mFaceMap.put("[凋谢]", R.drawable.plv_65);
        mFaceMap.put("[示爱]", R.drawable.plv_66);
        mFaceMap.put("[爱心]", R.drawable.plv_67);
        mFaceMap.put("[心碎]", R.drawable.plv_68);
        mFaceMap.put("[蛋糕]", R.drawable.plv_69);
        mFaceMap.put("[闪电]", R.drawable.plv_70);
        mFaceMap.put("[炸弹]", R.drawable.plv_71);
        mFaceMap.put("[刀]", R.drawable.plv_72);
        mFaceMap.put("[足球]", R.drawable.plv_73);
        mFaceMap.put("[瓢虫]", R.drawable.plv_74);
        mFaceMap.put("[便便]", R.drawable.plv_75);
        mFaceMap.put("[月亮]", R.drawable.plv_76);
        mFaceMap.put("[太阳]", R.drawable.plv_77);
        mFaceMap.put("[礼物]", R.drawable.plv_78);
        mFaceMap.put("[拥抱]", R.drawable.plv_79);
        mFaceMap.put("[强]", R.drawable.plv_80);

        mFaceMap.put("[弱]", R.drawable.plv_81);
        mFaceMap.put("[握手]", R.drawable.plv_82);
        mFaceMap.put("[胜利]", R.drawable.plv_83);
        mFaceMap.put("[抱拳]", R.drawable.plv_84);
        mFaceMap.put("[勾引]", R.drawable.plv_85);
        mFaceMap.put("[拳头]", R.drawable.plv_86);
        mFaceMap.put("[差劲]", R.drawable.plv_87);
        mFaceMap.put("[爱你]", R.drawable.plv_88);
        mFaceMap.put("[NO]", R.drawable.plv_89);
        mFaceMap.put("[OK]", R.drawable.plv_90);
        mFaceMap.put("[爱情]", R.drawable.plv_91);
        mFaceMap.put("[飞吻]", R.drawable.plv_92);
        mFaceMap.put("[跳跳]", R.drawable.plv_93);
        mFaceMap.put("[发抖]", R.drawable.plv_94);
        mFaceMap.put("[怄火]", R.drawable.plv_95);
        mFaceMap.put("[转圈]", R.drawable.plv_96);
        mFaceMap.put("[磕头]", R.drawable.plv_97);
        mFaceMap.put("[回头]", R.drawable.plv_98);
        mFaceMap.put("[跳绳]", R.drawable.plv_99);
        mFaceMap.put("[挥手]", R.drawable.plv_100);
    }

    public int getFaceId(String faceStr) {
        if (mFaceMap.containsKey(faceStr)) {
            return mFaceMap.get(faceStr);
        }
        return -1;
    }

}
