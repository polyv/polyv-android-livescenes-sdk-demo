package com.easefun.polyv.livecommon.module.modules.linkmic.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * date: 2021/1/5
 * author: HWilliamgo
 * description:
 * 连麦mute缓存列表
 * <p>
 * mute缓存用于处理这种情况：
 * 当远端用户加入频道后，先收到了mute数据，后收到了用户加入频道的事件，导致遗漏了初始化的mute事件。
 * 例如：rtc的onJoinSuccess()和onMute()事件均快于socket消息的onJoinSuccess事件，导致先处理mute，后加入连麦列表。
 * 因此这里做一个缓存，防止遗漏初始化的mute事件。
 */
public class PLVLinkMicMuteCacheList {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //mute缓存数据
    private List<PLVLinkMicMuteCacheBean> muteCacheList = new LinkedList<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 当用户加入连麦时，将缓存的Mute信息更新到用户数据
     *
     * @param dataBean 用户数据
     */
    public void updateUserMuteCacheWhenJoinList(PLVLinkMicItemDataBean dataBean) {
        Iterator<PLVLinkMicMuteCacheBean> muteCacheBeanIterator = muteCacheList.iterator();
        while (muteCacheBeanIterator.hasNext()) {
            PLVLinkMicMuteCacheBean muteCacheBean = muteCacheBeanIterator.next();
            if (muteCacheBean.getLinkMicId().equals(dataBean.getLinkMicId())) {
                dataBean.setMuteAudio(muteCacheBean.isMuteAudio());
                dataBean.setMuteVideo(muteCacheBean.isMuteVideo());
                muteCacheBeanIterator.remove();
            }
        }
    }

    /**
     * 添加或者更新音频mute数据到缓存列表
     *
     * @param linkMicId 连麦Id
     * @param mute      mute
     */
    public void addOrUpdateAudioMuteCacheList(String linkMicId, boolean mute) {
        addOrUpdateMuteCacheList(linkMicId, mute, true);
    }

    /**
     * 添加或更新视频mute数据到缓存列表
     *
     * @param linkMicId 连麦Id
     * @param mute      mute
     */
    public void addOrUpdateVideoMuteCacheList(String linkMicId, boolean mute) {
        addOrUpdateMuteCacheList(linkMicId, mute, false);
    }

    /**
     * 清空缓存列表
     */
    public void clear() {
        muteCacheList.clear();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    /**
     * @param isAudio true表示是音频mute，false表示是视频mute
     */
    private void addOrUpdateMuteCacheList(String linkMicId, boolean mute, boolean isAudio) {
        boolean existInCacheList = false;
        for (PLVLinkMicMuteCacheBean muteCacheBean : muteCacheList) {
            if (linkMicId.equals(muteCacheBean.getLinkMicId())) {
                existInCacheList = true;
                if (isAudio) {
                    muteCacheBean.setMuteAudio(mute);
                } else {
                    muteCacheBean.setMuteVideo(mute);
                }
            }
        }
        if (!existInCacheList) {
            PLVLinkMicMuteCacheBean muteCacheBean = new PLVLinkMicMuteCacheBean(linkMicId);
            if (isAudio) {
                muteCacheBean.setMuteAudio(mute);
            } else {
                muteCacheBean.setMuteVideo(mute);
            }
            muteCacheList.add(muteCacheBean);
        }
    }

    // </editor-fold>
}
