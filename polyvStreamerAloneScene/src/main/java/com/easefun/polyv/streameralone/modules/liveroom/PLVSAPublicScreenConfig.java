package com.easefun.polyv.streameralone.modules.liveroom;

import android.content.Context;
import android.content.SharedPreferences;

/** SA 公屏本地显示设置，不改变推流画面或观众端设置。 */
public final class PLVSAPublicScreenConfig {
    private PLVSAPublicScreenConfig() { }
    public static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences("plvsa_public_screen", Context.MODE_PRIVATE);
    }
    public static int heightLevel(Context context) { return validLevel(preferences(context).getInt("height", 1)); }
    public static int fontLevel(Context context) { return validLevel(preferences(context).getInt("font", 0)); }
    private static int validLevel(int value) { return Math.max(0, Math.min(2, value)); }
    public static float heightRatio(Context context) { return new float[]{0.22f, 0.38f, 0.48f}[heightLevel(context)]; }
    public static int textSizeSp(Context context) { return new int[]{12, 14, 16}[fontLevel(context)]; }
    public static void setHeight(Context context, int level) { preferences(context).edit().putInt("height", validLevel(level)).apply(); }
    public static void setFont(Context context, int level) { preferences(context).edit().putInt("font", validLevel(level)).apply(); }
}
