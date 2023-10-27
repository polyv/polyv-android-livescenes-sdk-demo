package com.easefun.polyv.livecommon.module.modules.streamer.view.ui;

import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.util.PLVPopupHelper;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVStreamerNetworkStatusLayout extends FrameLayout {

    private static final Map<PLVLinkMicConstant.NetworkQuality, Integer> QUALITY_IMAGE_MAP = mapOf(
            pair(PLVLinkMicConstant.NetworkQuality.EXCELLENT, R.drawable.plv_streamer_network_status_good_icon),
            pair(PLVLinkMicConstant.NetworkQuality.GOOD, R.drawable.plv_streamer_network_status_good_icon),
            pair(PLVLinkMicConstant.NetworkQuality.POOR, R.drawable.plv_streamer_network_status_moderate_icon),
            pair(PLVLinkMicConstant.NetworkQuality.BAD, R.drawable.plv_streamer_network_status_moderate_icon),
            pair(PLVLinkMicConstant.NetworkQuality.VERY_BAD, R.drawable.plv_streamer_network_status_bad_icon),
            pair(PLVLinkMicConstant.NetworkQuality.DISCONNECT, R.drawable.plv_streamer_network_status_bad_icon)
    );

    private int backgroundColorNetworkGood = PLVFormatUtils.parseColor("#331B202D");
    private int backgroundColorNetworkModerate = PLVFormatUtils.parseColor("#331B202D");
    private int backgroundColorNetworkBad = PLVFormatUtils.parseColor("#331B202D");
    private int backgroundColorNetworkDisconnect = PLVFormatUtils.parseColor("#33FF6363");
    private int textColorNetworkConnected = PLVFormatUtils.parseColor("#F0F1F5");
    private int textColorNetworkDisconnected = PLVFormatUtils.parseColor("#FF6363");

    private PLVRoundRectConstraintLayout streamerNetworkStatusLayoutRoot;
    private ImageView streamerNetworkStatusIv;
    private TextView streamerNetworkStatusTv;
    private final PLVStreamerNetworkStatusDetailLayout streamerNetworkStatusDetailLayout = createDetailLayout(getContext());

    private PLVLinkMicConstant.NetworkQuality lastNetworkQuality = null;

    private static Map<PLVLinkMicConstant.NetworkQuality, String> getQualityDescriptionMap() {
        return mapOf(
                pair(PLVLinkMicConstant.NetworkQuality.EXCELLENT, PLVAppUtils.getString(R.string.plv_streamer_network_excellent)),
                pair(PLVLinkMicConstant.NetworkQuality.GOOD, PLVAppUtils.getString(R.string.plv_streamer_network_good)),
                pair(PLVLinkMicConstant.NetworkQuality.POOR, PLVAppUtils.getString(R.string.plv_streamer_network_poor)),
                pair(PLVLinkMicConstant.NetworkQuality.BAD, PLVAppUtils.getString(R.string.plv_streamer_network_bad_3)),
                pair(PLVLinkMicConstant.NetworkQuality.VERY_BAD, PLVAppUtils.getString(R.string.plv_streamer_network_very_bad)),
                pair(PLVLinkMicConstant.NetworkQuality.DISCONNECT, PLVAppUtils.getString(R.string.plv_streamer_network_disconnect))
        );
    }

    public PLVStreamerNetworkStatusLayout(@NonNull Context context) {
        super(context);
        init(null);
    }

    public PLVStreamerNetworkStatusLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVStreamerNetworkStatusLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attributeSet) {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_streamer_network_status_layout, this);
        parseAttrs(attributeSet);

        streamerNetworkStatusLayoutRoot = findViewById(R.id.plv_streamer_network_status_layout_root);
        streamerNetworkStatusIv = findViewById(R.id.plv_streamer_network_status_iv);
        streamerNetworkStatusTv = findViewById(R.id.plv_streamer_network_status_tv);

        setOnClickListener();
    }

    private void parseAttrs(@Nullable AttributeSet attributeSet) {
        if (attributeSet == null) {
            return;
        }
        final TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PLVStreamerNetworkStatusLayout);

        backgroundColorNetworkGood = typedArray.getColor(R.styleable.PLVStreamerNetworkStatusLayout_plvBackgroundColorNetworkGood, backgroundColorNetworkGood);
        backgroundColorNetworkModerate = typedArray.getColor(R.styleable.PLVStreamerNetworkStatusLayout_plvBackgroundColorNetworkModerate, backgroundColorNetworkModerate);
        backgroundColorNetworkBad = typedArray.getColor(R.styleable.PLVStreamerNetworkStatusLayout_plvBackgroundColorNetworkBad, backgroundColorNetworkBad);
        backgroundColorNetworkDisconnect = typedArray.getColor(R.styleable.PLVStreamerNetworkStatusLayout_plvBackgroundColorNetworkDisconnect, backgroundColorNetworkDisconnect);

        typedArray.recycle();
    }

    private void setOnClickListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVPopupHelper.show(v, streamerNetworkStatusDetailLayout,
                        new PLVPopupHelper.ShowPopupConfig()
                                .setPosition(PLVPopupHelper.PopupPosition.BOTTOM_ALIGN_RIGHT)
                                .setFocusable(true)
                                .setOutsideTouchable(true)
                                .setMarginTop(ConvertUtils.dp2px(4))
                );
            }
        });
    }

    @NonNull
    protected PLVStreamerNetworkStatusDetailLayout createDetailLayout(@NonNull Context context) {
        return new PLVStreamerNetworkStatusDetailLayout(context);
    }

    public void onNetworkQuality(PLVLinkMicConstant.NetworkQuality networkQuality) {
        if (networkQuality == null || lastNetworkQuality == networkQuality) {
            return;
        }
        lastNetworkQuality = networkQuality;
        switch (networkQuality) {
            case EXCELLENT:
            case GOOD:
                streamerNetworkStatusTv.setTextColor(textColorNetworkConnected);
                streamerNetworkStatusLayoutRoot.setBackgroundColor(backgroundColorNetworkGood);
                break;
            case POOR:
            case BAD:
                streamerNetworkStatusTv.setTextColor(textColorNetworkConnected);
                streamerNetworkStatusLayoutRoot.setBackgroundColor(backgroundColorNetworkModerate);
                break;
            case VERY_BAD:
                streamerNetworkStatusTv.setTextColor(textColorNetworkConnected);
                streamerNetworkStatusLayoutRoot.setBackgroundColor(backgroundColorNetworkBad);
                break;
            case DISCONNECT:
                streamerNetworkStatusTv.setTextColor(textColorNetworkDisconnected);
                streamerNetworkStatusLayoutRoot.setBackgroundColor(backgroundColorNetworkDisconnect);
                break;
            default:
        }
        if (QUALITY_IMAGE_MAP.containsKey(networkQuality)) {
            streamerNetworkStatusIv.setImageResource(QUALITY_IMAGE_MAP.get(networkQuality));
        }
        Map<PLVLinkMicConstant.NetworkQuality, String> map = getQualityDescriptionMap();
        if (map.containsKey(networkQuality)) {
            streamerNetworkStatusTv.setText(map.get(networkQuality));
        }
    }

    public void onNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
        streamerNetworkStatusDetailLayout.onNetworkStatus(networkStatusVO);
    }

}
