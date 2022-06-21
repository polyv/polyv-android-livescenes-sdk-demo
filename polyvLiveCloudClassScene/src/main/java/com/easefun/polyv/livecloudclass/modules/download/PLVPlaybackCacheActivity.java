package com.easefun.polyv.livecloudclass.modules.download;

import static com.plv.thirdpart.blankj.utilcode.util.ConvertUtils.dp2px;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.download.fragment.PLVPlaybackCacheFragment;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVViewPagerAdapter;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVMagicIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVViewPagerHelper;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.PLVCommonNavigator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerTitleView;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.PLVCommonNavigatorAdapter;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.indicators.PLVLinePagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles.PLVColorTransitionPagerTitleView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.plv.foundationsdk.utils.PLVFormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheActivity extends PLVBaseActivity implements View.OnClickListener {

    private ImageView playbackCacheBackIv;
    private PLVMagicIndicator playbackCacheTab;
    private ViewPager playbackCacheVp;

    private final List<PLVPlaybackCacheFragment> playbackCacheFragments = new ArrayList<>();

    private PagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_playback_cache_activity);

        findView();
        initTab();
    }

    private void findView() {
        playbackCacheBackIv = findViewById(R.id.plv_playback_cache_back_iv);
        playbackCacheTab = findViewById(R.id.plv_playback_cache_tab);
        playbackCacheVp = findViewById(R.id.plv_playback_cache_vp);

        playbackCacheBackIv.setOnClickListener(this);
    }

    private void initTab() {
        playbackCacheFragments.add(PLVPlaybackCacheFragment.create("下载中", true));
        playbackCacheFragments.add(PLVPlaybackCacheFragment.create("已下载", false));

        viewPagerAdapter = new PLVViewPagerAdapter(getSupportFragmentManager(), playbackCacheFragments);
        playbackCacheVp.setAdapter(viewPagerAdapter);
        playbackCacheVp.setOffscreenPageLimit(playbackCacheFragments.size() - 1);

        playbackCacheTab.setNavigator(new PLVCommonNavigator(this) {{
            setAdapter(new PLVCommonNavigatorAdapter() {
                @Override
                public int getCount() {
                    return playbackCacheFragments.size();
                }

                @Override
                public IPLVPagerTitleView getTitleView(Context context, final int index) {
                    if (playbackCacheFragments.isEmpty() || playbackCacheFragments.size() <= index) {
                        return null;
                    }
                    return new PLVColorTransitionPagerTitleView(context) {{
                        setPadding(dp2px(16), 0, dp2px(16), 0);
                        setNormalColor(PLVFormatUtils.parseColor("#ADADC0"));
                        setSelectedColor(PLVFormatUtils.parseColor("#FFFFFF"));
                        setText(playbackCacheFragments.get(index).getTitle());
                        setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                playbackCacheVp.setCurrentItem(index);
                            }
                        });
                    }};
                }

                @Override
                public IPLVPagerIndicator getIndicator(Context context) {
                    return new PLVLinePagerIndicator(context) {{
                        setMode(PLVLinePagerIndicator.MODE_WRAP_CONTENT);
                        setLineHeight(dp2px(2));
                        setRoundRadius(dp2px(1));
                        setColors(PLVFormatUtils.parseColor("#FFFFFF"));
                    }};
                }
            });
        }});

        PLVViewPagerHelper.bind(playbackCacheTab, playbackCacheVp);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == playbackCacheBackIv.getId()) {
            finish();
        }
    }
}
