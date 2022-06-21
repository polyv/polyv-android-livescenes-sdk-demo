package com.easefun.polyv.livestreamer.modules.beauty.adapter.viewholder;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.beauty.model.config.PLVBeautyEnums;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.beauty.adapter.vo.PLVLSBeautyOptionVO;
import com.plv.beauty.api.options.PLVBeautyOption;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLSBeautyViewHolder extends PLVLSAbsBeautyViewHolder {

    public static PLVLSBeautyViewHolder create(@NonNull ViewGroup viewGroup) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvls_live_room_beauty_option_beauty_item, viewGroup, false);
        return new PLVLSBeautyViewHolder(view);
    }

    private static final int TEXT_VIEW_SELECTED_COLOR = PLVFormatUtils.parseColor("#4399FF");
    private static final int TEXT_VIEW_NORMAL_COLOR = PLVFormatUtils.parseColor("#99F0F1F5");
    private static final int TEXT_VIEW_DISABLED_COLOR = PLVFormatUtils.parseColor("#66F0F1F5");
    private static final int IMAGE_VIEW_NORMAL_COLOR = PLVFormatUtils.parseColor("#E6FFFFFF");
    private static final int IMAGE_VIEW_DISABLED_COLOR = PLVFormatUtils.parseColor("#66000000");

    private LinearLayout beautyOptionBeautyItemLl;
    private PLVRoundImageView beautyOptionBeautyItemIv;
    private TextView beautyOptionBeautyItemTv;

    private PLVLSBeautyOptionVO bindingOptionVO;

    private PLVLSBeautyViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        findView();
    }

    private void findView() {
        beautyOptionBeautyItemLl = itemView.findViewById(R.id.plvls_beauty_option_beauty_item_ll);
        beautyOptionBeautyItemIv = itemView.findViewById(R.id.plvls_beauty_option_beauty_item_iv);
        beautyOptionBeautyItemTv = itemView.findViewById(R.id.plvls_beauty_option_beauty_item_tv);
    }

    @Override
    public void bind(PLVLSBeautyOptionVO beautyOptionVO) {
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

    private void bindBeautyOption(PLVLSBeautyOptionVO beautyOptionVO) {
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

    private void bindDetailOption(PLVLSBeautyOptionVO beautyOptionVO) {
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

    private void bindLayout(final PLVLSBeautyOptionVO optionVO) {
        LayoutHandler.layout(itemView, beautyOptionBeautyItemLl, optionVO);
    }

    private void bindOnClickListener(final PLVLSBeautyOptionVO optionVO) {
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

    private final Observer<PLVLSBeautyOptionVO> currentSelectedOptionObserver = new Observer<PLVLSBeautyOptionVO>() {
        @Override
        public void onChanged(@Nullable PLVLSBeautyOptionVO beautyOptionVO) {
            final boolean isSelected = beautyOptionVO != null && bindingOptionVO.getOption().equals(beautyOptionVO.getOption());
            onSelectChanged(bindingOptionVO, isSelected);
        }
    };

    private void observeCurrentSelectedOption() {
        bindingOptionVO.getCurrentSelectedOptionVOLiveData().observe((LifecycleOwner) itemView.getContext(), currentSelectedOptionObserver);
    }

    private void onEnableChanged(final PLVLSBeautyOptionVO myOptionVO, boolean enable) {
        updateUiState(
                enable,
                myOptionVO.equals(myOptionVO.getCurrentSelectedOptionVOLiveData().getValue())
        );
    }

    private void onSelectChanged(final PLVLSBeautyOptionVO myOptionVO, boolean selected) {
        updateUiState(
                getOrDefault(myOptionVO.getCurrentEnableStateLiveData().getValue(), false),
                selected
        );
    }

    private static class LayoutHandler {

        private static final int MARGIN_LEFT_FIRST_OPTION = ConvertUtils.dp2px(64);
        private static final int MARGIN_RIGHT_LAST_OPTION = ConvertUtils.dp2px(64);
        private static final int MARGIN_HORIZON_NORMAL = ConvertUtils.dp2px(18);

        private static void layout(final View itemView, final View innerLayout, final PLVLSBeautyOptionVO optionVO) {
            final boolean isFirstOption = optionVO.getOptionItemIndex() == 0;
            final boolean isLastOption = optionVO.getOptionItemIndex() == optionVO.getOptionGroupSize() - 1;

            final ViewGroup.MarginLayoutParams itemViewLp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            itemViewLp.leftMargin = isFirstOption ? MARGIN_LEFT_FIRST_OPTION : MARGIN_HORIZON_NORMAL;
            itemViewLp.rightMargin = isLastOption ? MARGIN_RIGHT_LAST_OPTION : MARGIN_HORIZON_NORMAL;
            itemView.setLayoutParams(itemViewLp);
        }

    }
}
