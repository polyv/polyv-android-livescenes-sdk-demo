package com.easefun.polyv.liveecommerce.modules.chatroom.layout;

import static com.plv.foundationsdk.rx.PLVRxAutoDispose.AutoDisposeKey.autoDisposeKey;
import static com.plv.foundationsdk.rx.PLVRxAutoDispose.autoDispose;
import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.mapOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectSpan;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.component.exts.AsyncLazy;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVECChatOverLengthMessageLayout extends FrameLayout {

    private TextView chatroomOverLengthMessageTitle;
    private ImageView chatroomOverLengthMessageCloseBtn;
    private View chatroomOverLengthMessageTitleSplitLine;
    private ScrollView chatroomOverLengthMessageContentSv;
    private TextView chatroomOverLengthMessageTextTv;
    private TextView chatroomOverLengthMessageCopyBtn;

    private PLVMenuDrawer menuDrawer;

    public PLVECChatOverLengthMessageLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVECChatOverLengthMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVECChatOverLengthMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_chatroom_over_length_message_layout, this);

        chatroomOverLengthMessageTitle = findViewById(R.id.plvec_chatroom_over_length_message_title);
        chatroomOverLengthMessageCloseBtn = findViewById(R.id.plvec_chatroom_over_length_message_close_btn);
        chatroomOverLengthMessageTitleSplitLine = findViewById(R.id.plvec_chatroom_over_length_message_title_split_line);
        chatroomOverLengthMessageContentSv = findViewById(R.id.plvec_chatroom_over_length_message_content_sv);
        chatroomOverLengthMessageTextTv = findViewById(R.id.plvec_chatroom_over_length_message_text_tv);
        chatroomOverLengthMessageCopyBtn = findViewById(R.id.plvec_chatroom_over_length_message_copy_btn);

        chatroomOverLengthMessageCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                menuDrawer.closeMenu();
            }
        });
    }

    public void show(final BaseChatMessageDataBean chatMessageDataBean) {
        setContent(chatMessageDataBean);

        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvec_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setMenuSize((int) (ScreenUtils.getScreenOrientatedHeight() * 0.67F));
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
            menuDrawer.openMenu();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void hide() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    private void setContent(final BaseChatMessageDataBean chatMessageDataBean) {
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
            autoDispose(disposable, (LifecycleOwner) getContext(), autoDisposeKey(this, "setContent"));
        }

        chatroomOverLengthMessageCopyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!chatMessageDataBean.isOverLength || chatMessageDataBean.onOverLengthFullMessage == null) {
                    PLVCopyBoardPopupWindow.copy(v.getContext(), chatMessageDataBean.message.toString());
                } else {
                    chatMessageDataBean.onOverLengthFullMessage.getAsync(new PLVSugarUtil.Consumer<String>() {
                        @Override
                        public void accept(final String s) {
                            postToMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    PLVCopyBoardPopupWindow.copy(v.getContext(), s);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private static final Map<String, Integer> ACTOR_COLOR_MAP = mapOf(
            pair(PLVSocketUserConstant.USERTYPE_TEACHER, PLVFormatUtils.parseColor("#F09343")),
            pair(PLVSocketUserConstant.USERTYPE_ASSISTANT, PLVFormatUtils.parseColor("#598FE5")),
            pair(PLVSocketUserConstant.USERTYPE_GUEST, PLVFormatUtils.parseColor("#EB6165")),
            pair(PLVSocketUserConstant.USERTYPE_MANAGER, PLVFormatUtils.parseColor("#33BBC5"))
    );

    private PLVSpannableStringBuilder createActorNickSpan(final BaseChatMessageDataBean chatMessageDataBean) {
        final PLVSpannableStringBuilder spannableStringBuilder = new PLVSpannableStringBuilder();
        if (ACTOR_COLOR_MAP.containsKey(chatMessageDataBean.userType)) {
            spannableStringBuilder.appendExclude(chatMessageDataBean.actor,
                    new PLVRoundRectSpan()
                            .backgroundColor(getOrDefault(ACTOR_COLOR_MAP.get(chatMessageDataBean.userType), Color.TRANSPARENT))
                            .marginRight(4)
                            .paddingRight(4)
                            .paddingLeft(4)
                            .radius(9)
                            .textSize(12)
                            .textColor(Color.WHITE)
            );
        }

        final String nickSpanContent = chatMessageDataBean.nick + ": ";
        spannableStringBuilder.appendExclude(nickSpanContent, new ForegroundColorSpan(PLVFormatUtils.parseColor("#FFD16B")));

        return spannableStringBuilder;
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
