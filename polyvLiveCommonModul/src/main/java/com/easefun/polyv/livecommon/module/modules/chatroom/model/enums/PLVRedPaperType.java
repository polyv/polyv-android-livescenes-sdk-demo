package com.easefun.polyv.livecommon.module.modules.chatroom.model.enums;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Hoshiiro
 */
public enum PLVRedPaperType {

    DEFAULT_RED_PAPER("", "红包", "恭喜发财，大吉大利"),
    ALIPAY_PASSWORD_RED_PAPER("alipay_password_official_normal", "支付宝口令红包", "输入口令，领取红包"),

    ;

    public final String serverType;
    public final String typeName;
    public final String defaultBlessingMessage;

    PLVRedPaperType(
            String serverType,
            String typeName,
            String defaultBlessingMessage
    ) {
        this.serverType = serverType;
        this.typeName = typeName;
        this.defaultBlessingMessage = defaultBlessingMessage;
    }

    @Nullable
    public static PLVRedPaperType match(String serverType, boolean ignoreCase) {
        for (PLVRedPaperType value : values()) {
            if (ignoreCase) {
                if (value.serverType.equalsIgnoreCase(serverType)) {
                    return value;
                }
            } else {
                if (value.serverType.equals(serverType)) {
                    return value;
                }
            }
        }
        return null;
    }

    @Nullable
    public static PLVRedPaperType match(String serverType) {
        return match(serverType, false);
    }

    @NonNull
    public static PLVRedPaperType matchOrDefault(String serverType, @NonNull PLVRedPaperType defValue) {
        return getOrDefault(match(serverType), defValue);
    }

    @NonNull
    public static PLVRedPaperType matchOrDefault(String serverType, @NonNull PLVRedPaperType defValue, boolean ignoreCase) {
        return getOrDefault(match(serverType, false), defValue);
    }

    public static boolean isSupportType(String serverType) {
        return match(serverType, true) != null;
    }

}
