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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.beauty.model.config.PLVBeautyEnums;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.beauty.adapter.vo.PLVSABeautyOptionVO;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSABeautyViewHolder extends PLVSAAbsBeautyViewHolder {

    public static PLVSABeautyViewHolder create(@NonNull ViewGroup viewGroup) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvsa_live_room_beauty_option_beauty_item, viewGroup, false);
        return new PLVSABeautyViewHolder(view);
    }

    private static final int TEXT_VIEW_SELECTED_COLOR = PLVFormatUtils.parseColor("#4399FF");
    private static final int TEXT_VIEW_NORMAL_COLOR = PLVFormatUtils.parseColor("#99F0F1F5");
    private static final int TEXT_VIEW_DISABLED_COLOR = PLVFormatUtils.parseColor("#66F0F1F5");
    private static final int IMAGE_VIEW_NORMAL_COLOR = PLVFormatUtils.parseColor("#E6FFFFFF");
    private static final int IMAGE_VIEW_DISABLED_COLOR = PLVFormatUtils.parseColor("#66000000");

    private LinearLayout beautyOptionBeautyItemLl;
    private PLVRoundImageView beautyOptionBeautyItemIv;
    private TextView beautyOptionBeautyItemTv;

    private PLVSABeautyOptionVO bindingOptionVO;

    private PLVSABeautyViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        findView();
    }

    private void findView() {
        beautyOptionBeautyItemLl = itemView.findViewById(R.id.plvsa_beauty_option_beauty_item_ll);
        beautyOptionBeautyItemIv = itemView.findViewById(R.id.plvsa_beauty_option_beauty_item_iv);
        beautyOptionBeautyItemTv = itemView.findViewById(R.id.plvsa_beauty_option_beauty_item_tv);
    }

    @Override
    public void bind(PLVSABeautyOptionVO beautyOptionVO) {
        final boolean canProcess = beautyOptionVO.getOption() instanceof PLVBeautyOption;
        if (!canProcess) {
            return;
        }

        this.bindingOptionVO = beautyOptionVO;
        if (PLVBeautyEnums.BeautyOption.contains((PLVBeautyOption) beautyOptionVO.getOption())) {
            bindBeautyOption(beautyOptionVO);
        } else if (PLVBeautyEnums.DetailOption.contains((PLVBeautyOption) beautyOptionVO.getOption())) {
            bindDetailOption(beautyOptionVO);
        }
    }

    private void bindBeautyOption(PLVSABeautyOptionVO beautyOptionVO) {
        final PLVBeautyEnums.BeautyOption beautyOptionEnum = PLVBeautyEnums.BeautyOption.getByBeautyOption((PLVBeautyOption) beautyOptionVO.getOption());
        if (beautyOptionEnum == null) {
            return;
        }

        bindImage(beautyOptionEnum.iconResId);
        bindText(beautyOptionEnum.name);
        bindLayout(beautyOptionVO);
        bindOnClickListener(beautyOptionVO);
        updateUiState(
                getOrDefault(beautyOptionVO.getCurrentEnableStateLiveData().getValue(), false),
                beautyOptionVO.equals(beautyOptionVO.getCurrentSelectedOptionVOLiveData().getValue())
        );
        observeCurrentEnableState();
        observeCurrentSelectedOption();
    }

    private void bindDetailOption(PLVSABeautyOptionVO beautyOptionVO) {
        final PLVBeautyEnums.DetailOption detailOptionEnum = PLVBeautyEnums.DetailOption.getByBeautyOption((PLVBeautyOption) beautyOptionVO.getOption());
        if (detailOptionEnum == null) {
            return;
        }

        bindImage(detailOptionEnum.iconResId);
        bindText(detailOptionEnum.name);
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
            beautyOptionBeautyItemIv.setImageDrawable(null);
        } else {
            beautyOptionBeautyItemIv.setImageResource(resId);
        }
    }

    private void bindText(String text) {
        beautyOptionBeautyItemTv.setText(text != null ? text : "");
    }

    private void bindLayout(final PLVSABeautyOptionVO optionVO) {
        LayoutHandler.layout(itemView, beautyOptionBeautyItemLl, optionVO);
    }

    private void bindOnClickListener(final PLVSABeautyOptionVO optionVO) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectChanged(optionVO, true);
                if (optionVO.getOnSelectedListener() != null) {
                    optionVO.getOnSelectedListener().onSelected(optionVO);
                }
            }
        });
    }

    private void updateUiState(
            final boolean enable,
            final boolean selected
    ) {
        itemView.setEnabled(enable);

        beautyOptionBeautyItemIv.setEnabled(enable);
        beautyOptionBeautyItemIv.setColorFilter(enable ? IMAGE_VIEW_NORMAL_COLOR : IMAGE_VIEW_DISABLED_COLOR);
        beautyOptionBeautyItemIv.setSelected(selected);

        if (!enable) {
            beautyOptionBeautyItemTv.setTextColor(TEXT_VIEW_DISABLED_COLOR);
        } else {
            beautyOptionBeautyItemTv.setTextColor(selected ? TEXT_VIEW_SELECTED_COLOR : TEXT_VIEW_NORMAL_COLOR);
        }
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
        private static final int VERTICAL_MARGIN_HORIZON_NORMAL = ConvertUtils.dp2px(16);
        private static final int VERTICAL_MARGIN_BOTTOM = ConvertUtils.dp2px(8);

        private static void layoutVertical(final View itemView, final View innerLayout, final PLVSABeautyOptionVO optionVO) {
            final boolean isFirstOption = optionVO.getOptionItemIndex() == 0;
            final boolean isLastOption = optionVO.getOptionItemIndex() == optionVO.getOptionGroupSize() - 1;

            final ViewGroup.MarginLayoutParams itemViewLp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            itemViewLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            itemViewLp.leftMargin = isFirstOption ? VERTICAL_MARGIN_LEFT_FIRST_OPTION : VERTICAL_MARGIN_HORIZON_NORMAL;
            itemViewLp.rightMargin = isLastOption ? VERTICAL_MARGIN_RIGHT_LAST_OPTION : VERTICAL_MARGIN_HORIZON_NORMAL;
            itemViewLp.bottomMargin = VERTICAL_MARGIN_BOTTOM;
            itemView.setLayoutParams(itemViewLp);

            final ConstraintLayout.LayoutParams innerLayoutLp = (ConstraintLayout.LayoutParams) innerLayout.getLayoutParams();
            innerLayoutLp.horizontalBias = 0.5F;
            innerLayout.setLayoutParams(innerLayoutLp);
        }

        private static final int HORIZONTAL_MARGIN_BOTTOM = ConvertUtils.dp2px(16);

        private static void layoutHorizontal(final View itemView, final View innerLayout, final PLVSABeautyOptionVO optionVO) {
            final GridLayoutManager.LayoutParams itemViewLp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
            itemViewLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            itemViewLp.bottomMargin = HORIZONTAL_MARGIN_BOTTOM;
            itemView.setLayoutParams(itemViewLp);

            final ConstraintLayout.LayoutParams innerLayoutLp = (ConstraintLayout.LayoutParams) innerLayout.getLayoutParams();
            innerLayoutLp.horizontalBias = (optionVO.getOptionItemIndex() % 3) / 2F;
            innerLayout.setLayoutParams(innerLayoutLp);
        }

    }
}
