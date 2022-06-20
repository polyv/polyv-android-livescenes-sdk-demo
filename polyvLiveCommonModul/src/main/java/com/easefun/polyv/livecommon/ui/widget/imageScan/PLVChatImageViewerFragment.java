package com.easefun.polyv.livecommon.ui.widget.imageScan;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVUrlTag;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVMyProgressManager;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.IPLVQuoteEvent;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatImgContent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class PLVChatImageViewerFragment extends PLVBaseFragment {
    private PLVChatImageViewer imageViewer;
    private List<PLVUrlTag> imgTagList;
    private int position;

    public static PLVChatImageViewerFragment newInstance() {
        return new PLVChatImageViewerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plv_image_viewer_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //移除图片加载监听器，避免内存泄漏
        PLVMyProgressManager.removeModuleListener(PLVChatImageContainerWidget.LOADIMG_MOUDLE_TAG);
    }

    private void initView() {
        imageViewer = findViewById(R.id.image_viewer);
        imageViewer.setOnClickImgListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        imageViewer.setDataList(imgTagList, position);
    }

    public void setImgList(List<PLVUrlTag> imgList, int position) {
        this.imgTagList = imgList;
        this.position = position;
        if (imageViewer != null) {
            imageViewer.setDataList(imgList, position);
        }
    }

    public void hide() {
        if (getActivity() == null || isHidden()) {
            return;
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(PLVChatImageViewerFragment.this).commitAllowingStateLoss();
        if (ScreenUtils.isPortrait()) {
            PLVScreenUtils.exitFullScreen(getActivity());
        }
    }

    public static PLVChatImageViewerFragment show(AppCompatActivity activity, List<PLVBaseViewData> dataList, PLVBaseViewData<PLVBaseEvent> selData, int containerViewId) {
        if (activity == null) {
            return null;
        }
        List<PLVUrlTag> imgTagList = new ArrayList<>();
        int imgPosition = toImgList(dataList, selData, imgTagList);
        return show(activity, imgTagList, imgPosition, containerViewId);
    }

    public static PLVChatImageViewerFragment show(AppCompatActivity activity, PLVBaseViewData selData, int containerViewId) {
        List<PLVUrlTag> imgTagList = new ArrayList<>();
        if (selData.getData() instanceof PLVBaseEvent) {
            PLVBaseEvent baseEvent = (PLVBaseEvent) selData.getData();
            if (baseEvent instanceof IPLVQuoteEvent) {
                IPLVQuoteEvent quoteEvent = (IPLVQuoteEvent) baseEvent;
                if (quoteEvent.getQuote() != null && quoteEvent.getQuote().getImage() != null) {
                    if (baseEvent.getObj3() instanceof PLVUrlTag) {
                        imgTagList.add((PLVUrlTag) baseEvent.getObj3());
                    } else {
                        String imageUrl = quoteEvent.getQuote().getImage().getUrl();
                        PLVUrlTag urlTag = new PLVUrlTag(imageUrl, baseEvent);
                        baseEvent.setObj3(urlTag);
                        imgTagList.add(urlTag);
                    }
                }
            }
        } else if (selData.getData() instanceof PLVChatPlaybackData) {
            PLVChatPlaybackData chatPlaybackData = ((PLVChatPlaybackData) selData.getData());
            PLVChatQuoteVO chatQuoteVO = ((PLVChatPlaybackData) selData.getData()).getChatQuoteVO();
            if (chatQuoteVO != null && chatQuoteVO.getImage() != null) {
                if (chatPlaybackData.getObj3() instanceof PLVUrlTag) {
                    imgTagList.add((PLVUrlTag) chatPlaybackData.getObj3());
                } else {
                    String imageUrl = chatQuoteVO.getImage().getUrl();
                    PLVUrlTag urlTag = new PLVUrlTag(imageUrl, chatPlaybackData);
                    chatPlaybackData.setObj3(urlTag);
                    imgTagList.add(urlTag);
                }
            }
        }
        return show(activity, imgTagList, 0, containerViewId);
    }

    private static PLVChatImageViewerFragment show(AppCompatActivity activity, List<PLVUrlTag> imgTagList, int position, int containerViewId) {
        if (ScreenUtils.isPortrait()) {
            PLVScreenUtils.enterLandscape(activity);
        }
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //优先检查，fragment是否存在，避免重叠
        PLVBaseFragment tempFragment = (PLVBaseFragment) fragmentManager.findFragmentByTag("PLVChatImageViewerFragment");
        if (tempFragment instanceof PLVChatImageViewerFragment && tempFragment.isAdded()) {
            ((PLVChatImageViewerFragment) tempFragment).setImgList(imgTagList, position);
            fragmentTransaction.show(tempFragment).commitAllowingStateLoss();
            return (PLVChatImageViewerFragment) tempFragment;
        }
        PLVChatImageViewerFragment chatImageViewerFragment = PLVChatImageViewerFragment.newInstance();
        chatImageViewerFragment.setImgList(imgTagList, position);
        fragmentTransaction.add(containerViewId, chatImageViewerFragment, "PLVChatImageViewerFragment").commitAllowingStateLoss();
        return chatImageViewerFragment;
    }

    public static int toImgList(List<PLVBaseViewData> dataList, PLVBaseViewData<PLVBaseEvent> selData, List<PLVUrlTag> imgTagList) {
        int selImgPosition = 0;
        for (int i = 0; i < dataList.size(); i++) {
            PLVBaseViewData data = dataList.get(i);
            if (data == selData) {
                selImgPosition = imgTagList.size();
            }
            Object baseEvent = data.getData();
            String imgUrl = null;
            PLVUrlTag urlTag = null;
            if (baseEvent instanceof PLVChatImgHistoryEvent) {
                imgUrl = ((PLVChatImgHistoryEvent) baseEvent).getContent().getUploadImgUrl();
                if (((PLVChatImgHistoryEvent) baseEvent).getObj2() instanceof PLVUrlTag) {
                    urlTag = (PLVUrlTag) ((PLVChatImgHistoryEvent) baseEvent).getObj2();
                } else {
                    urlTag = new PLVUrlTag(imgUrl, baseEvent);
                    ((PLVChatImgHistoryEvent) baseEvent).setObj2(urlTag);
                }
            } else if (baseEvent instanceof PLVTAnswerEvent) {   //兼容提问-图片信息
                PLVTAnswerEvent answerEven = (PLVTAnswerEvent) baseEvent;
                if (answerEven.getObj2() instanceof PLVUrlTag) {
                    urlTag = (PLVUrlTag) answerEven.getObj2();
                    imgUrl = urlTag.getUrl();
                }else {
                    if(answerEven.getObj1()!=null){
                        imgUrl = answerEven.getObj1().toString();
                        urlTag = new PLVUrlTag(imgUrl, baseEvent);
                        ((PLVTAnswerEvent) baseEvent).setObj2(urlTag);
                    }
                }
            }
            else if (baseEvent instanceof PLVChatImgEvent) {
                imgUrl = ((PLVChatImgEvent) baseEvent).getValues().get(0).getUploadImgUrl();
                if (((PLVChatImgEvent) baseEvent).getObj2() instanceof PLVUrlTag) {
                    urlTag = (PLVUrlTag) ((PLVChatImgEvent) baseEvent).getObj2();
                } else {
                    urlTag = new PLVUrlTag(imgUrl, baseEvent);
                    ((PLVChatImgEvent) baseEvent).setObj2(urlTag);
                }
            } else if (baseEvent instanceof PolyvSendLocalImgEvent) {
                imgUrl = ((PolyvSendLocalImgEvent) baseEvent).getImageFilePath();
                if (((PolyvSendLocalImgEvent) baseEvent).getObj2() instanceof PLVUrlTag) {
                    urlTag = (PLVUrlTag) ((PolyvSendLocalImgEvent) baseEvent).getObj2();
                } else {
                    urlTag = new PLVUrlTag(imgUrl, baseEvent);
                    ((PolyvSendLocalImgEvent) baseEvent).setObj2(urlTag);
                }
            } else if (baseEvent instanceof PLVChatEmotionEvent) {
                PLVChatEmotionEvent emotionEvent = (PLVChatEmotionEvent) baseEvent;
                imgUrl = PLVFaceManager.getInstance().getEmotionUrl(emotionEvent.getId());
                if (emotionEvent.getObj2() instanceof PLVUrlTag) {
                    urlTag = (PLVUrlTag) emotionEvent.getObj2();
                } else {
                    urlTag = new PLVUrlTag(imgUrl, baseEvent);
                    emotionEvent.setObj2(urlTag);
                }
            } else if (baseEvent instanceof PLVChatPlaybackData) {
                PLVChatPlaybackData chatPlaybackData = (PLVChatPlaybackData) baseEvent;
                PLVChatImgContent chatImgContent = chatPlaybackData.getChatImgContent();
                if (chatImgContent != null) {
                    imgUrl = chatImgContent.getUploadImgUrl();
                    if (chatPlaybackData.getObj2() instanceof PLVUrlTag) {
                        urlTag = (PLVUrlTag) chatPlaybackData.getObj2();
                    } else {
                        urlTag = new PLVUrlTag(imgUrl, baseEvent);
                        chatPlaybackData.setObj2(urlTag);
                    }
                }
            } else {
                continue;
            }
            if (urlTag == null && imgUrl!=null) {
                urlTag = new PLVUrlTag(imgUrl, baseEvent);
            }
            if(urlTag!=null){
                imgTagList.add(urlTag);
            }
        }
        return selImgPosition;
    }
}
