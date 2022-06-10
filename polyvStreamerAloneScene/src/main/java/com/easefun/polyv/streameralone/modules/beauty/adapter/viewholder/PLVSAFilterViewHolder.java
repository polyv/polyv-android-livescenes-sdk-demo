package com.easefun.polyv.streameralone.modules.beauty.adapter.viewholder;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.beauty.adapter.vo.PLVSABeautyOptionVO;
import com.plv.beauty.api.options.PLVFilterOption;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSAFilterViewHolder extends PLVSAAbsBeautyViewHolder {

    public static PLVSAFilterViewHolder create(@NonNull ViewGroup viewGroup) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvsa_live_room_beauty_option_filter_item, viewGroup, false);
        return new PLVSAFilterViewHolder(view);
    }

    private ConstraintLayout beautyOptionFilterItemLayout;
    private PLVRoundImageView beautyOptionFilterItemIv;
    private ImageView beautyOptionFilterItemSelectedMaskIv;
    private TextView beautyOptionFilterItemTv;
    private View beautyOptionFilterItemEnabledMaskView;

    private PLVSABeautyOptionVO bindingOptionVO;

    private PLVSAFilterViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        findView();
    }

    private void findView() {
        beautyOptionFilterItemLayout = itemView.findViewById(R.id.plvsa_beauty_option_filter_item_layout);
        beautyOptionFilterItemIv = itemView.findViewById(R.id.plvsa_beauty_option_filter_item_iv);
        beautyOptionFilterItemSelectedMaskIv = itemView.findViewById(R.id.plvsa_beauty_option_filter_item_selected_mask_iv);
        beautyOptionFilterItemTv = itemView.findViewById(R.id.plvsa_beauty_option_filter_item_tv);
        beautyOptionFilterItemEnabledMaskView = itemView.findViewById(R.id.plvsa_beauty_option_filter_item_enabled_mask_view);
    }

    @Override
    public void bind(PLVSABeautyOptionVO beautyOptionVO) {
        final boolean canProcess = beautyOptionVO.getOption() instanceof PLVFilterOption;
        if (!canProcess) {
            return;
        }

        this.bindingOptionVO = beautyOptionVO;
        bindImage(((PLVFilterOption) beautyOptionVO.getOption()).getIconDrawableId());
        bindText(((PLVFilterOption) beautyOptionVO.getOption()).getName());
        bindLayout(beautyOptionVO);
        bindOnClickListener(beautyOptionVO);
        updateUiState(
                getOrDefault(beautyOptionVO.getCurrentEnableStateLiveData().getValue(), false),
                beautyOptionVO.equals(beautyOptionVO.getCurrentSelectedOptionVOLiveData().getValue())
        );
        observeCurrentEnableState();
        observeCurrentSelectedOption();
    }

    private void bindImage(@DrawableRes Integer resId) {
        if (resId == null) {
            beautyOptionFilterItemIv.setImageDrawable(null);
        } else {
            beautyOptionFilterItemIv.setImageResource(resId);
        }
    }

    private void bindText(String text) {
        beautyOptionFilterItemTv.setText(text != null ? text : "");
    }

    private void bindLayout(final PLVSABeautyOptionVO optionVO) {
        LayoutHandler.layout(itemView, beautyOptionFilterItemLayout, optionVO);
    }

    private void bindOnClickListener(final PLVSABeautyOptionVO myOptionVO) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectChanged(myOptionVO, true);
                if (myOptionVO.getOnSelectedListener() != null) {
                    myOptionVO.getOnSelectedListener().onSelected(myOptionVO);
                }
            }
        });
    }

    private void updateUiState(
            final boolean enable,
            final boolean selected
    ) {
        itemView.setEnabled(enable);

        beautyOptionFilterItemEnabledMaskView.setVisibility(enable ? View.GONE : View.VISIBLE);
        beautyOptionFilterItemSelectedMaskIv.setVisibility(enable && selected ? View.VISIBLE : View.GONE);
    }

    private final Observer<Boolean> currentEnableStateObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable Boolean enable) {
            onEnableChanged(bindingOptionVO, enable != null && enable);
        }
    };

    private void observeCurrentEnableState() {
        bindingOptionVO.getCurrentEnableStateLiveData().observe((LifecycleOwner) itemView.getContext(), currentEnableStateObserver);
    }

    private final Observer<PLVSABeautyOptionVO> currentSelectedOptionObserver = new Observer<PLVSABeautyOptionVO>() {
        @Override
        public void onChanged(@Nullable PLVSABeautyOptionVO beautyOptionVO) {
            final boolean isSelected = beautyOptionVO != null && bindingOptionVO.getOption().equals(beautyOptionVO.getOption());
            onSelectChanged(bindingOptionVO, isSelected);
        }
    };

    private void observeCurrentSelectedOption() {
        bindingOptionVO.getCurrentSelectedOptionVOLiveData().observe((LifecycleOwner) itemView.getContext(), currentSelectedOptionObserver);
    }

    private void onEnableChanged(final PLVSABeautyOptionVO myOptionVO, boolean enable) {
        updateUiState(
                enable,
                myOptionVO.equals(myOptionVO.getCurrentSelectedOptionVOLiveData().getValue())
        );
    }

    private void onSelectChanged(final PLVSABeautyOptionVO myOptionVO, boolean selected) {
        updateUiState(
                getOrDefault(myOptionVO.getCurrentEnableStateLiveData().getValue(), false),
                selected
        );
    }


    private static class LayoutHandler {

        private static void layout(final View itemView, final View innerLayout, final PLVSABeautyOptionVO optionVO) {
            if (PLVScreenUtils.isPortrait(itemView.getContext())) {
                layoutVertical(itemView, innerLayout, optionVO);
            } else {
                layoutHorizontal(itemView, innerLayout, optionVO);
            }
        }

        private static final int VERTICAL_MARGIN_LEFT_FIRST_OPTION = ConvertUtils.dp2px(24);
        private static final int VERTICAL_MARGIN_RIGHT_LAST_OPTION = ConvertUtils.dp2px(24);
        private static final int VERTICAL_MARGIN_NORMAL = ConvertUtils.dp2px(6);
        private static final int VERTICAL_MARGIN_VERTICAL = 0;

        private static void layoutVertical(final View itemView, final View innerLayout, final PLVSABeautyOptionVO optionVO) {
            final boolean isFirstOption = optionVO.getOptionItemIndex() == 0;
            final boolean isLastOption = optionVO.getOptionItemIndex() == optionVO.getOptionGroupSize() - 1;

            final ViewGroup.MarginLayoutParams itemViewLp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            itemViewLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            itemViewLp.leftMargin = isFirstOption ? VERTICAL_MARGIN_LEFT_FIRST_OPTION : VERTICAL_MARGIN_NORMAL;
            itemViewLp.rightMargin = isLastOption ? VERTICAL_MARGIN_RIGHT_LAST_OPTION : VERTICAL_MARGIN_NORMAL;
            itemViewLp.topMargin = VERTICAL_MARGIN_VERTICAL;
            itemViewLp.bottomMargin = VERTICAL_MARGIN_VERTICAL;
            itemView.setLayoutParams(itemViewLp);

            final ConstraintLayout.LayoutParams innerLayoutLp = (ConstraintLayout.LayoutParams) innerLayout.getLayoutParams();
            innerLayoutLp.horizontalBias = 0.5F;
            innerLayout.setLayoutParams(innerLayoutLp);
        }

        private static final int HORIZONTAL_MARGIN_TOP = ConvertUtils.dp2px(4);
        private static final int HORIZONTAL_MARGIN_BOTTOM = ConvertUtils.dp2px(8);

        private static void layoutHorizontal(final View itemView, final View innerLayout, final PLVSABeautyOptionVO optionVO) {
            final GridLayoutManager.LayoutParams itemViewLp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
            itemViewLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            itemViewLp.topMargin = HORIZONTAL_MARGIN_TOP;
            itemViewLp.bottomMargin = HORIZONTAL_MARGIN_BOTTOM;
            itemView.setLayoutParams(itemViewLp);

            final ConstraintLayout.LayoutParams innerLayoutLp = (ConstraintLayout.LayoutParams) innerLayout.getLayoutParams();
            innerLayoutLp.horizontalBias = (optionVO.getOptionItemIndex() % 3) / 2F;
            innerLayout.setLayoutParams(innerLayoutLp);
        }

    }

}
