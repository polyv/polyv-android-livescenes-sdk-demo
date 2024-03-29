package com.easefun.polyv.livestreamer.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVTimeUnit.minutes;
import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livestreamer.R;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.linkmic.model.PLVPushDowngradePreference;

/**
 * @author Hoshiiro
 */
public class PLVLSPushDowngradeAlertToastLayout extends FrameLayout {

    private TextView liveRoomPushDowngradeAlertToastDesc;
    private TextView liveRoomPushDowngradeAlertSwitchTv;
    private ImageView liveRoomPushDowngradeAlertCloseIv;

    private IPLVStreamerContract.IStreamerPresenter presenter;

    private long lastAlertBadNetworkTimestamp = 0;

    public PLVLSPushDowngradeAlertToastLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLSPushDowngradeAlertToastLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLSPushDowngradeAlertToastLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_push_downgrade_alert_toast_layout, this);

        liveRoomPushDowngradeAlertToastDesc = findViewById(R.id.plvls_live_room_push_downgrade_alert_toast_desc);
        liveRoomPushDowngradeAlertSwitchTv = findViewById(R.id.plvls_live_room_push_downgrade_alert_switch_tv);
        liveRoomPushDowngradeAlertCloseIv = findViewById(R.id.plvls_live_room_push_downgrade_alert_close_iv);

        liveRoomPushDowngradeAlertSwitchTv.getPaint().setFlags(
                Paint.UNDERLINE_TEXT_FLAG | liveRoomPushDowngradeAlertSwitchTv.getPaint().getFlags()
        );

        setOnClickListener();
    }

    private void setOnClickListener() {
        liveRoomPushDowngradeAlertCloseIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        liveRoomPushDowngradeAlertSwitchTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToPreferFluency();
                hide();
            }
        });
    }

    private void observeDowngradePreferenceChanged(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
        presenter.getData().getDowngradePreferenceLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVPushDowngradePreference>() {
            @Override
            public void onChanged(@Nullable PLVPushDowngradePreference downgradePreference) {
                if (downgradePreference == null) {
                    return;
                }
                switch (downgradePreference) {
                    case PREFER_BETTER_FLUENCY:
                        hide();
                        break;
                    case PREFER_BETTER_QUALITY:
                        lastAlertBadNetworkTimestamp = 0;
                        break;
                    default:
                }
            }
        });
    }

    public final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            PLVLSPushDowngradeAlertToastLayout.this.presenter = presenter;
            observeDowngradePreferenceChanged(presenter);
        }

        @Override
        public void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatus) {
            final boolean isBadUpstreamNetwork = networkStatus.getUpPackageLost() != null && networkStatus.getUpPackageLost() > 30;
            final boolean canChangeToFluency = presenter.getPushDowngradePreference() != PLVPushDowngradePreference.PREFER_BETTER_FLUENCY;
            if (isBadUpstreamNetwork
                    && canChangeToFluency
                    && System.currentTimeMillis() - lastAlertBadNetworkTimestamp > minutes(10).toMillis()) {
                showPushDowngradeAlert();
                lastAlertBadNetworkTimestamp = System.currentTimeMillis();
            }
        }

    };

    private void showPushDowngradeAlert() {
        PLVViewUtil.showViewForDuration(this, seconds(10).toMillis());
    }

    private void hide() {
        setVisibility(GONE);
    }

    private void changeToPreferFluency() {
        if (presenter == null) {
            return;
        }
        presenter.setPushDowngradePreference(PLVPushDowngradePreference.PREFER_BETTER_FLUENCY);
        PLVToast.Builder.context(getContext())
                .setText(R.string.plv_streamer_change_to_prefer_fluency)
                .show();
    }

}
