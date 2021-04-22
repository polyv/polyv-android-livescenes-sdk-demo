package com.easefun.polyv.livecommon.ui.widget.imageScan;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVUrlTag;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVSDCardUtils;
import com.plv.thirdpart.blankj.utilcode.util.FileUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PLVChatImageViewer extends FrameLayout {
    private static final String TAG = "PLVChatImageViewer";
    private View view;
    private TextView tvPage;
    private ImageView ivDownload;
    private ViewPager vpImageViewer;
    private PLVImageViewPagerAdapter<PLVUrlTag, PLVChatImageContainerWidget> pagerAdapter;
    private List<PLVUrlTag> imgUrlTags;
    private OnClickListener imgOnClickListener;
    private int currentPosition = -1;

    private CompositeDisposable compositeDisposable;

    public PLVChatImageViewer(@NonNull Context context) {
        this(context, null);
    }

    public PLVChatImageViewer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVChatImageViewer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.plv_image_view_pager_layout, this);
        vpImageViewer = view.findViewById(R.id.vp_image_viewer);
        tvPage = view.findViewById(R.id.tv_page);
        ivDownload = view.findViewById(R.id.iv_download);
        ivDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> permissions = new ArrayList<>(1);
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                PLVFastPermission.getInstance()
                        .start((Activity) getContext(), permissions, new PLVOnPermissionCallback() {
                            @Override
                            public void onAllGranted() {
                                downloadImg();
                            }

                            @Override
                            public void onPartialGranted(ArrayList<String> grantedPermissions,
                                                         ArrayList<String> deniedPermissions,
                                                         ArrayList<String> deniedForeverP) {
                                if (deniedForeverP != null && !deniedForeverP.isEmpty()) {
                                    new AlertDialog.Builder(getContext()).setTitle("提示")
                                            .setMessage("保存图片所需的存储权限被拒绝，请到应用设置的权限管理中恢复")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    PLVFastPermission.getInstance().jump2Settings(getContext());
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    PLVCommonLog.d(TAG, "cancel");
                                                }
                                            }).setCancelable(false).show();
                                } else {
                                    toast("请允许存储权限后再保存图片");
                                }
                            }
                        });
            }
        });
    }

    private void downloadImg() {
        if (currentPosition > -1) {
            final String imgUrl = imgUrlTags.get(currentPosition).getUrl();
            if (imgUrl == null) {
                toast("图片保存失败(null)");
                return;
            }
            final String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
            final String savePath = PLVSDCardUtils.createPath(getContext(), "PLVChatImg");
            if (compositeDisposable == null) {
                compositeDisposable = new CompositeDisposable();
            }
            compositeDisposable.add(
                    Observable.just(1)
                            .map(new Function<Integer, File>() {
                                @Override
                                public File apply(Integer integer) throws Exception {
                                    try {
                                        File file = new File(imgUrl);//对于本地gif图片的情况
                                        if (file.isFile() && file.exists()) {
                                            return file;
                                        }
                                    } catch (Exception e) {
                                        PLVCommonLog.e(TAG, "downloadImg：" + e.getMessage());
                                    }
                                    File file = PLVImageLoader.getInstance().saveImageAsFile(getContext(), imgUrl, PLVChatImageContainerWidget.LOADIMG_MOUDLE_TAG + imgUrlTags.get(currentPosition));
                                    return file;
                                }
                            })
                            .map(new Function<File, Boolean>() {
                                @Override
                                public Boolean apply(File file) throws Exception {
                                    if (file.getAbsolutePath().equals(new File(savePath, fileName).getAbsolutePath()))
                                        return true;
                                    return FileUtils.copyFile(file, new File(savePath, fileName),//同path时，复制失败
                                            new FileUtils.OnReplaceListener() {
                                                @Override
                                                public boolean onReplace() {
                                                    return true;
                                                }
                                            });
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) throws Exception {
                                    toast(aBoolean ? "图片保存在：" + new File(savePath, fileName).getAbsolutePath() : "图片保存失败(saveFailed)");
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    toast("图片保存失败(loadFailed)");
                                }
                            })
            );
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) {
            if (compositeDisposable != null) {
                compositeDisposable.dispose();
                compositeDisposable = null;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    private void toast(String message) {
        ToastUtils.showShort(message);
    }

    public void setOnClickImgListener(OnClickListener listener) {
        imgOnClickListener = listener;
    }

    public void setDataList(final List<PLVUrlTag> dataList, int curPosition) {
        if (dataList != null && !dataList.isEmpty()) {
            imgUrlTags = dataList;
            if (pagerAdapter == null) {
                pagerAdapter = new PLVImageViewPagerAdapter<>(getContext());
                pagerAdapter.setOnImgClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (imgOnClickListener != null) {
                            imgOnClickListener.onClick(v);
                        }
                    }
                });
                pagerAdapter.bindData(imgUrlTags);
                vpImageViewer.setAdapter(pagerAdapter);
                vpImageViewer.clearOnPageChangeListeners();
                vpImageViewer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        PLVCommonLog.d(TAG, "onPageScrolled position:" + position
                                + " positionOffset:" + positionOffset
                                + " positionOffsetPixels:" + positionOffsetPixels);
                    }

                    @Override
                    public void onPageSelected(int position) {
                        currentPosition = position;
                        tvPage.setText(position + 1 + "/" + imgUrlTags.size());
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        PLVCommonLog.d(TAG, "onPageScrollStateChanged:" + state);
                    }
                });
            } else {
                pagerAdapter.bindData(imgUrlTags);
                pagerAdapter.notifyDataSetChanged();
            }
            vpImageViewer.setCurrentItem(curPosition, false);

            currentPosition = curPosition;
            tvPage.setText(curPosition + 1 + "/" + imgUrlTags.size());
        }
    }
}
