package com.easefun.polyv.livecommon.module.modules.previous.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.view.PLVAbsPreviousView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livescenes.previous.model.PLVChapterDataVO;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:lzj
 * Time:2021/12/29
 * Description: 视频回放-章节layout
 */
public class PLVChapterView extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private RecyclerView recyclerView;

    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPresenter;
    private PLVChapterAdapter chapterAdapter;

    private final List<PLVChapterDataVO> dataList = new ArrayList<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVChapterView(@NonNull Context context) {
        this(context, null);
    }

    public PLVChapterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVChapterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_playback_chapter_layout, this, true);
        recyclerView = findViewById(R.id.plv_chapter_rv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回放-章节-view层的实现">
    private final IPLVPreviousPlaybackContract.IPreviousPlaybackView chapterView = new PLVAbsPreviousView() {
        @Override
        public void updateChapterList(List<PLVChapterDataVO> datas) {
            if (previousPresenter != null) {
                dataList.clear();
                dataList.addAll(datas);
                if (chapterAdapter != null) {
                    List<PLVBaseViewData> plvBaseViewData = toPlayBackList(datas);
                    chapterAdapter.setDataList(plvBaseViewData);
                }
            }
        }

        @Override
        public void requestChapterError() {
            super.requestChapterError();
        }

        @Override
        public void onSeekChange(int time) {
            if (chapterAdapter != null) {
                // 通过二分法来找出 适合当前的章节，他需要选中比他小的章节并且这个章节是所有小的章节中是最大的章节
                int position = findIndex2(dataList, time);
                chapterAdapter.updataItmeTime(position);
            }
        }

        @Override
        public void setPresenter(IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter presenter) {
            previousPresenter = presenter;
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外提供的方法">

    /**
     * 为PLVPreviousView设置参数
     *
     * @param builder builder参数
     */
    public void setParams(PLVChapterView.Builder builder) {
        if (builder.layoutManager != null) {
            recyclerView.setLayoutManager(builder.layoutManager);
        }
        if (builder.itemDecoration != null) {
            recyclerView.addItemDecoration(builder.itemDecoration);
        }
        if (builder.adapter != null) {
            chapterAdapter = builder.adapter;
            recyclerView.setAdapter(chapterAdapter);
        }
    }

    /**
     * 改变视频的进度
     *
     * @param timeStamp 视频进度
     */
    public void changePlaybackVideoSeek(int timeStamp) {
        if (previousPresenter != null) {
            previousPresenter.changePlaybackVideoSeek(timeStamp);
        }
    }

    /**
     * 将MVP-V返回出去
     *
     * @return
     */
    public IPLVPreviousPlaybackContract.IPreviousPlaybackView getPreviousView() {
        return chapterView;
    }

    /**
     * 请求章节列表
     */
    public void requestChapterList() {
        if (previousPresenter != null) {
            previousPresenter.requestChapterDetail();
        }
    }

    /**
     * 销毁view
     */
    public void onDestroy() {
        if (previousPresenter != null) {
            previousPresenter.unregisterView(chapterView);
            previousPresenter.onDestroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="自己的内部方法">

    /**
     * 通过二分法查找合适的位置
     * 因为不断会回调视频的播放进度，章节需要根据视频的进度来显示在播放哪一个章节
     * 这里通过二分法来查找出，当前合适的章节
     *
     * @param list 章节列表
     * @param time 播放的进度
     * @return 合适的位置
     */
    private int findIndex2(List<PLVChapterDataVO> list, int time) {
        int minIndex = 0;
        int maxIndex = dataList.size() - 1;
        int midIndex = (minIndex + maxIndex) / 2;
        while (minIndex < maxIndex) {
            if (time == list.get(midIndex).getTimeStamp()) {
                return midIndex;
            } else if (time >= list.get(maxIndex).getTimeStamp()) {
                return maxIndex;
            } else if (time > list.get(midIndex).getTimeStamp() && midIndex + 1 == maxIndex) {
                return midIndex;
            } else if (time >= list.get(minIndex).getTimeStamp() && minIndex + 1 == maxIndex) {
                return minIndex;
            } else if (time > list.get(midIndex).getTimeStamp()) {
                minIndex = midIndex;
            } else if (time < list.get(midIndex).getTimeStamp()) {
                maxIndex = midIndex;
            }
            midIndex = (minIndex + maxIndex) / 2;
        }
        return midIndex;
    }

    /**
     * 將list数据转为List<PLVBaseViewData>
     *
     * @param listVO 章节列表数据
     * @return List<PLVBaseViewData>
     */
    private List<PLVBaseViewData> toPlayBackList(List<PLVChapterDataVO> listVO) {
        List<PLVBaseViewData> playbackList = new ArrayList<>();
        if (listVO != null) {
            for (PLVChapterDataVO plvChapterDataVO : listVO) {
                playbackList.add(new PLVBaseViewData<>(plvChapterDataVO, PLVBaseViewData.ITEMTYPE_UNDEFINED));
            }
        }
        return playbackList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类">

    /**
     * PLVChapterView内部参数设置类
     * 可以通过这个内部类来为PLVChapterView设置相应的参数
     */
    public static class Builder {
        private final Context context;
        private RecyclerView.ItemDecoration itemDecoration;
        private RecyclerView.LayoutManager layoutManager;
        private PLVChapterAdapter adapter;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置recyclerView的layoutManager
         *
         * @param manager layoutManager
         * @return Builder
         */
        public Builder setRecyclerViewLayoutManager(RecyclerView.LayoutManager manager) {
            this.layoutManager = manager;
            return this;
        }

        /**
         * 设置RecyclerViewItemDecoration
         *
         * @param itemDecoration itemDecoration
         * @return Builder
         */
        public Builder setRecyclerViewItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
            this.itemDecoration = itemDecoration;
            return this;
        }

        /**
         * 设置RecycleView.Adapter
         *
         * @param adapter 适配器
         * @return Builder
         */
        public Builder setAdapter(PLVChapterAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * 可以通过这个Build来创建PLVPreviousView
         *
         * @return PLVPreviousView
         */
        public PLVChapterView create() {
            PLVChapterView plvChapterView = new PLVChapterView(context);
            plvChapterView.setParams(this);
            return plvChapterView;
        }
    }
    // </editor-fold>
}
