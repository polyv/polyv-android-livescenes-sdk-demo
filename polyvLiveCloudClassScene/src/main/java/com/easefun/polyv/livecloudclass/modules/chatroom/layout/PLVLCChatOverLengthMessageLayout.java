package com.easefun.polyv.livecloudclass.modules.chatroom.layout;

import static com.plv.foundationsdk.rx.PLVRxAutoDispose.AutoDisposeKey.autoDisposeKey;
import static com.plv.foundationsdk.rx.PLVRxAutoDispose.autoDispose;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectSpan;
import com.plv.foundationsdk.component.exts.AsyncLazy;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVLCChatOverLengthMessageLayout extends FrameLayout {

    private ObservableEmitter<ChildEvent> childEventObservableEmitter;

    private Lazy<ChatOverLengthMessagePortLayout> portLayout;
    private Lazy<ChatOverLengthMessageLandLayout> landLayout;
    private PLVMenuDrawer menuDrawer;

    private boolean isPortrait = ScreenUtils.isPortrait();

    public PLVLCChatOverLengthMessageLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLCChatOverLengthMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLCChatOverLengthMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        observeChildEvent();
        portLayout = new Lazy<ChatOverLengthMessagePortLayout>() {
            @Override
            public ChatOverLengthMessagePortLayout onLazyInit() {
                return new ChatOverLengthMessagePortLayout(getContext(), childEventObservableEmitter);
            }
        };
        landLayout = new Lazy<ChatOverLengthMessageLandLayout>() {
            @Override
            public ChatOverLengthMessageLandLayout onLazyInit() {
                return new ChatOverLengthMessageLandLayout(getContext(), childEventObservableEmitter);
            }
        };
    }

    private void observeChildEvent() {
        final Disposable disposable = Observable.create(
                        new ObservableOnSubscribe<ChildEvent>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<ChildEvent> emitter) throws Exception {
                                childEventObservableEmitter = emitter;
                            }
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Consumer<ChildEvent>() {
                    @Override
                    public void accept(ChildEvent childEvent) throws Exception {
                        childEvent.handle(PLVLCChatOverLengthMessageLayout.this);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });

        autoDispose(disposable, (LifecycleOwner) getContext());
    }

    public void show(final BaseChatMessageDataBean chatMessageDataBean) {
        isPortrait = ScreenUtils.isPortrait();
        final IChatOverLengthMessageLayout chatOverLengthMessageLayout = isPortrait ? portLayout.get() : landLayout.get();
        chatOverLengthMessageLayout.setData(chatMessageDataBean);

        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    chatOverLengthMessageLayout.menuPosition(),
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvlc_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {

                }
            });
        } else {
            menuDrawer.attachToContainer();
        }

        removeAllViews();
        addView((View) chatOverLengthMessageLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        menuDrawer.setMenuSize(chatOverLengthMessageLayout.menuSize());
        menuDrawer.setPosition(chatOverLengthMessageLayout.menuPosition());
        menuDrawer.openMenu();
    }

    public void hide() {
        removeAllViews();
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final boolean newIsPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait != newIsPortrait) {
            hide();
            isPortrait = newIsPortrait;
        }
    }

    private interface IChatOverLengthMessageLayout {
        int menuSize();

        Position menuPosition();

        void setData(BaseChatMessageDataBean chatMessageDataBean);
    }

    private static class ChatOverLengthMessagePortLayout extends FrameLayout implements IChatOverLengthMessageLayout {

        private TextView chatroomOverLengthMessageTitle;
        private ImageView chatroomOverLengthMessageCloseBtn;
        private View chatroomOverLengthMessageTitleSplitLine;
        private ImageView chatroomOverLengthMessageAvatarIv;
        private TextView chatroomOverLengthMessageNameTv;
        private ScrollView chatroomOverLengthMessageContentSv;
        private TextView chatroomOverLengthMessageTextTv;
        private TextView chatroomOverLengthMessageCopyBtn;

        private final ObservableEmitter<ChildEvent> eventEmitter;

        public ChatOverLengthMessagePortLayout(@NonNull Context context, ObservableEmitter<ChildEvent> emitter) {
            super(context);
            eventEmitter = emitter;
            initView();
        }

        private void initView() {
            LayoutInflater.from(getContext()).inflate(R.layout.plvlc_chatroom_over_length_message_port_layout, this);

            chatroomOverLengthMessageTitle = findViewById(R.id.plvlc_chatroom_over_length_message_title);
            chatroomOverLengthMessageCloseBtn = findViewById(R.id.plvlc_chatroom_over_length_message_close_btn);
            chatroomOverLengthMessageTitleSplitLine = findViewById(R.id.plvlc_chatroom_over_length_message_title_split_line);
            chatroomOverLengthMessageAvatarIv = findViewById(R.id.plvlc_chatroom_over_length_message_avatar_iv);
            chatroomOverLengthMessageNameTv = findViewById(R.id.plvlc_chatroom_over_length_message_name_tv);
            chatroomOverLengthMessageContentSv = findViewById(R.id.plvlc_chatroom_over_length_message_content_sv);
            chatroomOverLengthMessageTextTv = findViewById(R.id.plvlc_chatroom_over_length_message_text_tv);
            chatroomOverLengthMessageCopyBtn = findViewById(R.id.plvlc_chatroom_over_length_message_copy_btn);

            chatroomOverLengthMessageCloseBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    eventEmitter.onNext(new ChildEvent.CloseEvent());
                }
            });
        }

        @Override
        public int menuSize() {
            return (int) (ScreenUtils.getScreenOrientatedHeight() * 0.67F);
        }

        @Override
        public Position menuPosition() {
            return Position.BOTTOM;
        }

        @Override
        public void setData(BaseChatMessageDataBean chatMessageDataBean) {
            setAvatar(chatMessageDataBean);
            setNickWithActor(chatMessageDataBean);
            setContent(chatMessageDataBean);
        }

        private static final Map<String, Integer> DEFAULT_AVATARS = mapOf(
                pair(PLVSocketUserConstant.USERTYPE_MANAGER, R.drawable.plvlc_chatroom_ic_teacher),
                pair(PLVSocketUserConstant.USERTYPE_TEACHER, R.drawable.plvlc_chatroom_ic_teacher),
                pair(PLVSocketUserConstant.USERTYPE_ASSISTANT, R.drawable.plvlc_chatroom_ic_assistant),
                pair(PLVSocketUserConstant.USERTYPE_GUEST, R.drawable.plvlc_chatroom_ic_guest),
                pair(PLVSocketUserConstant.USERTYPE_VIEWER, R.drawable.plvlc_chatroom_ic_viewer)
        );

        private void setAvatar(BaseChatMessageDataBean chatMessageDataBean) {
            final int defaultAvatar = getOrDefault(DEFAULT_AVATARS.get(chatMessageDataBean.userType), R.drawable.plvlc_chatroom_ic_viewer);

            PLVImageLoader.getInstance().loadImageNoDiskCache(
                    getContext(),
                    chatMessageDataBean.avatar,
                    defaultAvatar,
                    defaultAvatar,
                    chatroomOverLengthMessageAvatarIv
            );
        }

        private void setNickWithActor(BaseChatMessageDataBean chatMessageDataBean) {
            final PLVSpannableStringBuilder spannableStringBuilder = new PLVSpannableStringBuilder(chatMessageDataBean.nick);
            if (chatMessageDataBean.actor != null) {
                spannableStringBuilder.appendExclude(chatMessageDataBean.actor,
                        new PLVRoundRectSpan()
                                .marginLeft(4)
                                .paddingLeft(4)
                                .paddingRight(4)
                                .radius(4)
                                .backgroundColor(PLVFormatUtils.parseColor("#5394F6"))
                                .textColor(Color.WHITE)
                                .textSize(10)
                );
            }
            chatroomOverLengthMessageNameTv.setText(spannableStringBuilder);
        }

        private void setContent(final BaseChatMessageDataBean chatMessageDataBean) {
            chatroomOverLengthMessageTextTv.setText(chatMessageDataBean.message);
            chatroomOverLengthMessageContentSv.scrollTo(0, 0);
            if (chatMessageDataBean.isOverLength && chatMessageDataBean.onOverLengthFullMessage != null) {
                final Disposable disposable = chatMessageDataBean.onOverLengthFullMessage
                        .asSingle()
                        .observeOn(Schedulers.computation())
                        .map(new Function<String, CharSequence>() {
                            @Override
                            public CharSequence apply(@NonNull String s) throws Exception {
                                return PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(s), ConvertUtils.sp2px(16), Utils.getApp());
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<CharSequence>() {
                            @Override
                            public void accept(CharSequence charSequence) throws Exception {
                                chatroomOverLengthMessageTextTv.setText(charSequence);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                PLVCommonLog.exception(throwable);
                            }
                        });
                autoDispose(disposable, (LifecycleOwner) getContext(), autoDisposeKey(this, "setContent"));
            }

            chatroomOverLengthMessageCopyBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!chatMessageDataBean.isOverLength || chatMessageDataBean.onOverLengthFullMessage == null) {
                        eventEmitter.onNext(new ChildEvent.CopyEvent(chatMessageDataBean.message.toString()));
                    } else {
                        chatMessageDataBean.onOverLengthFullMessage.getAsync(new PLVSugarUtil.Consumer<String>() {
                            @Override
                            public void accept(final String s) {
                                eventEmitter.onNext(new ChildEvent.CopyEvent(s));
                            }
                        });
                    }
                }
            });
        }

    }

    private static class ChatOverLengthMessageLandLayout extends FrameLayout implements IChatOverLengthMessageLayout {

        private TextView chatroomOverLengthMessageTitle;
        private ImageView chatroomOverLengthMessageCloseBtn;
        private View chatroomOverLengthMessageTitleSplitLine;
        private ScrollView chatroomOverLengthMessageContentSv;
        private TextView chatroomOverLengthMessageTextTv;
        private TextView chatroomOverLengthMessageCopyBtn;

        private final ObservableEmitter<ChildEvent> eventEmitter;

        public ChatOverLengthMessageLandLayout(@NonNull Context context, ObservableEmitter<ChildEvent> emitter) {
            super(context);
            eventEmitter = emitter;
            initView();
        }

        private void initView() {
            LayoutInflater.from(getContext()).inflate(R.layout.plvlc_chatroom_over_length_message_land_layout, this);

            chatroomOverLengthMessageTitle = findViewById(R.id.plvlc_chatroom_over_length_message_title);
            chatroomOverLengthMessageCloseBtn = findViewById(R.id.plvlc_chatroom_over_length_message_close_btn);
            chatroomOverLengthMessageTitleSplitLine = findViewById(R.id.plvlc_chatroom_over_length_message_title_split_line);
            chatroomOverLengthMessageContentSv = findViewById(R.id.plvlc_chatroom_over_length_message_content_sv);
            chatroomOverLengthMessageTextTv = findViewById(R.id.plvlc_chatroom_over_length_message_text_tv);
            chatroomOverLengthMessageCopyBtn = findViewById(R.id.plvlc_chatroom_over_length_message_copy_btn);

            chatroomOverLengthMessageCloseBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    eventEmitter.onNext(new ChildEvent.CloseEvent());
                }
            });

        }

        @Override
        public int menuSize() {
            return ConvertUtils.dp2px(375);
        }

        @Override
        public Position menuPosition() {
            return Position.RIGHT;
        }

        @Override
        public void setData(final BaseChatMessageDataBean chatMessageDataBean) {
            chatroomOverLengthMessageTextTv.setText(createActorNickSpan(chatMessageDataBean).append(chatMessageDataBean.message));
            chatroomOverLengthMessageContentSv.scrollTo(0, 0);
            if (chatMessageDataBean.isOverLength && chatMessageDataBean.onOverLengthFullMessage != null) {
                final Disposable disposable = chatMessageDataBean.onOverLengthFullMessage
                        .asSingle()
                        .observeOn(Schedulers.computation())
                        .map(new Function<String, CharSequence>() {
                            @Override
                            public CharSequence apply(@NonNull String s) throws Exception {
                                return createActorNickSpan(chatMessageDataBean)
                                        .append(PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(s), ConvertUtils.sp2px(14), Utils.getApp()));
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<CharSequence>() {
                            @Override
                            public void accept(CharSequence charSequence) throws Exception {
                                chatroomOverLengthMessageTextTv.setText(charSequence);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                PLVCommonLog.exception(throwable);
                            }
                        });
                autoDispose(disposable, (LifecycleOwner) getContext(), autoDisposeKey(this, "setData"));
            }

            chatroomOverLengthMessageCopyBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!chatMessageDataBean.isOverLength || chatMessageDataBean.onOverLengthFullMessage == null) {
                        eventEmitter.onNext(new ChildEvent.CopyEvent(chatMessageDataBean.message.toString()));
                    } else {
                        chatMessageDataBean.onOverLengthFullMessage.getAsync(new PLVSugarUtil.Consumer<String>() {
                            @Override
                            public void accept(final String s) {
                                eventEmitter.onNext(new ChildEvent.CopyEvent(s));
                            }
                        });
                    }
                }
            });
        }

        private PLVSpannableStringBuilder createActorNickSpan(final BaseChatMessageDataBean chatMessageDataBean) {
            final PLVSpannableStringBuilder spannableStringBuilder = new PLVSpannableStringBuilder();
            if (!TextUtils.isEmpty(chatMessageDataBean.actor)) {
                spannableStringBuilder.appendExclude(chatMessageDataBean.actor + "-", new ForegroundColorSpan(PLVFormatUtils.parseColor("#F09343")));
            }
            spannableStringBuilder.appendExclude(chatMessageDataBean.nick + ": ", new ForegroundColorSpan(PLVFormatUtils.parseColor("#F09343")));
            return spannableStringBuilder;
        }

    }

    private static abstract class ChildEvent {
        abstract void handle(PLVLCChatOverLengthMessageLayout layout);

        private static final class CloseEvent extends ChildEvent {
            @Override
            void handle(PLVLCChatOverLengthMessageLayout layout) {
                layout.hide();
            }
        }

        private static final class CopyEvent extends ChildEvent {

            private final String content;

            public CopyEvent(String content) {
                this.content = content;
            }

            @Override
            void handle(PLVLCChatOverLengthMessageLayout layout) {
                PLVCopyBoardPopupWindow.copy(layout.getContext(), content);
            }

        }
    }

    public static class BaseChatMessageDataBean {
        private final String avatar;
        private final String nick;
        private final String userType;
        private final String actor;
        private final CharSequence message;
        private final boolean isOverLength;
        @Nullable
        private final AsyncLazy<String> onOverLengthFullMessage;

        private BaseChatMessageDataBean(Builder builder) {
            avatar = builder.avatar;
            nick = builder.nick;
            userType = builder.userType;
            actor = builder.actor;
            message = builder.message;
            isOverLength = builder.isOverLength;
            onOverLengthFullMessage = builder.onOverLengthFullMessage;
        }

        public static class Builder {
            private String avatar;
            private String nick;
            private String userType;
            private String actor;
            private CharSequence message;
            private boolean isOverLength;
            @Nullable
            private AsyncLazy<String> onOverLengthFullMessage;

            public Builder setAvatar(String avatar) {
                this.avatar = avatar;
                return this;
            }

            public Builder setNick(String nick) {
                this.nick = nick;
                return this;
            }

            public Builder setUserType(String userType) {
                this.userType = userType;
                return this;
            }

            public Builder setActor(String actor) {
                this.actor = actor;
                return this;
            }

            public Builder setMessage(CharSequence message) {
                this.message = message;
                return this;
            }

            public Builder setOverLength(boolean overLength) {
                isOverLength = overLength;
                return this;
            }

            public Builder setOnOverLengthFullMessage(@Nullable AsyncLazy<String> onOverLengthFullMessage) {
                this.onOverLengthFullMessage = onOverLengthFullMessage;
                return this;
            }

            public BaseChatMessageDataBean build() {
                return new BaseChatMessageDataBean(this);
            }
        }

    }

}
