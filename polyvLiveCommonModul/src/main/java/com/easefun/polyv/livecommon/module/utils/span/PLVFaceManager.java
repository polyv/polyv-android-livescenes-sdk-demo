package com.easefun.polyv.livecommon.module.utils.span;


import android.graphics.Bitmap;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 表情集合类
 */
public class PLVFaceManager {

    /**
     * 黄脸表情emoji
     */
    private Map<String, Integer> mFaceMap;

    private List<PLVEmotionImageVO.EmotionImage> emotionList = new ArrayList<>();

    private PLVFaceManager() {
        initFaceMap();
    }

    private static PLVFaceManager instance;

    public static PLVFaceManager getInstance() {
        if (null == instance)
            instance = new PLVFaceManager();
        return instance;
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


    /**
     * 初始化黄脸emoji表情
     */
    private void initFaceMap() {
        mFaceMap = new LinkedHashMap<String, Integer>();

        mFaceMap.put("[呲牙]", R.drawable.polyv_101);// no need i18n
        mFaceMap.put("[大笑]", R.drawable.polyv_102);// no need i18n
        mFaceMap.put("[可爱]", R.drawable.polyv_103);// no need i18n
        mFaceMap.put("[害羞]", R.drawable.polyv_104);// no need i18n
        mFaceMap.put("[偷笑]", R.drawable.polyv_105);// no need i18n
        mFaceMap.put("[再见]", R.drawable.polyv_106);// no need i18n
        mFaceMap.put("[惊讶]", R.drawable.polyv_107);// no need i18n
        mFaceMap.put("[哭笑]", R.drawable.polyv_108);// no need i18n
        mFaceMap.put("[酷]", R.drawable.polyv_109);// no need i18n
        mFaceMap.put("[奸笑]", R.drawable.polyv_110);// no need i18n
        mFaceMap.put("[鼓掌]", R.drawable.polyv_111);// no need i18n
        mFaceMap.put("[大哭]", R.drawable.polyv_112);// no need i18n
        mFaceMap.put("[敲打]", R.drawable.polyv_113);// no need i18n
        mFaceMap.put("[吃瓜]", R.drawable.polyv_114);// no need i18n
        mFaceMap.put("[让我看看]", R.drawable.polyv_115);// no need i18n
        mFaceMap.put("[按脸哭]", R.drawable.polyv_116);// no need i18n
        mFaceMap.put("[打哈欠]", R.drawable.polyv_117);// no need i18n
        mFaceMap.put("[愤怒]", R.drawable.polyv_118);// no need i18n
        mFaceMap.put("[难过]", R.drawable.polyv_119);// no need i18n
        mFaceMap.put("[ok]", R.drawable.polyv_120);// no need i18n
        mFaceMap.put("[爱心]", R.drawable.polyv_121);// no need i18n
        mFaceMap.put("[心碎]", R.drawable.polyv_123);// no need i18n
        mFaceMap.put("[加1]", R.drawable.polyv_122);// no need i18n
        mFaceMap.put("[正确]", R.drawable.polyv_124);// no need i18n
        mFaceMap.put("[错误]", R.drawable.polyv_125);// no need i18n
        mFaceMap.put("[满分]", R.drawable.polyv_126);// no need i18n
        mFaceMap.put("[笔记]", R.drawable.polyv_127);// no need i18n
        mFaceMap.put("[胜利]", R.drawable.polyv_128);// no need i18n
        mFaceMap.put("[比心]", R.drawable.polyv_129);// no need i18n
        mFaceMap.put("[赞]", R.drawable.polyv_130);// no need i18n
        mFaceMap.put("[蛋糕]", R.drawable.polyv_131);// no need i18n
        mFaceMap.put("[礼物]", R.drawable.polyv_132);// no need i18n
        mFaceMap.put("[红包]", R.drawable.polyv_133);// no need i18n
        mFaceMap.put("[奶茶]", R.drawable.polyv_134);// no need i18n
        mFaceMap.put("[时钟]", R.drawable.polyv_135);// no need i18n
        mFaceMap.put("[晚安]", R.drawable.polyv_136);// no need i18n
        mFaceMap.put("[拍手]", R.drawable.polyv_137);// no need i18n
        mFaceMap.put("[鲜花]", R.drawable.polyv_138);// no need i18n
    }

    public void initEmotionList(@NotNull List<PLVEmotionImageVO.EmotionImage> emotionImages){
        emotionList.clear();
        this.emotionList.addAll(emotionImages);
    }

    /**
     * 获取个性表情列表
     */
    public List<PLVEmotionImageVO.EmotionImage> getEmotionList(){
        return emotionList;
    }

    public String getEmotionUrl(@NotNull String id){
        for(PLVEmotionImageVO.EmotionImage emotion: emotionList){
            if(id.equals(emotion.getId())){
                return emotion.getUrl();
            }
        }
        return "";
    }

    public int getFaceId(String faceStr) {
        if (mFaceMap.containsKey(faceStr)) {
            return mFaceMap.get(faceStr);
        }
        return -1;
    }

}
