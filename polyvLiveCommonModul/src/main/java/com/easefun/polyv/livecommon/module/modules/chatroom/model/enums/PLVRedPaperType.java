package com.easefun.polyv.livecommon.module.modules.chatroom.model.enums;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVAppUtils;

/**
 * @author Hoshiiro
 */
public enum PLVRedPaperType {

    DEFAULT_RED_PAPER("", "", "") {
        @Override
        public String getTypeName() {
            return PLVAppUtils.getString(R.string.plv_red_paper_name);
        }

        @Override
        public String getDefaultBlessingMessage() {
            return PLVAppUtils.getString(R.string.plv_red_paper_blessing_message);
        }
    },
    ALIPAY_PASSWORD_RED_PAPER("alipay_password_official_normal", "", "") {
        @Override
        public String getTypeName() {
            return PLVAppUtils.getString(R.string.plv_red_paper_alipay);
        }

        @Override
        public String getDefaultBlessingMessage() {
            return PLVAppUtils.getString(R.string.plv_red_paper_blessing_message_2);
        }
    },

    ;

    public final String serverType;
    private final String typeName;
    private final String defaultBlessingMessage;

    PLVRedPaperType(
            String serverType,
            String typeName,
            String defaultBlessingMessage
    ) {
        this.serverType = serverType;
        this.typeName = typeName;
        this.defaultBlessingMessage = defaultBlessingMessage;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getDefaultBlessingMessage() {
        return defaultBlessingMessage;
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
