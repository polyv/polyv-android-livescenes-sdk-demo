package com.easefun.polyv.streameralone.modules.beauty.adapter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getNullableOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.transformList;

import androidx.lifecycle.MutableLiveData;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.easefun.polyv.streameralone.modules.beauty.adapter.viewholder.PLVSAAbsBeautyViewHolder;
import com.easefun.polyv.streameralone.modules.beauty.adapter.vo.PLVSABeautyOptionVO;
import com.plv.beauty.api.options.IPLVBeautyOption;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVSABeautyOptionAdapter extends RecyclerView.Adapter<PLVSAAbsBeautyViewHolder> implements PLVSABeautyOptionVO.OnSelectedListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final SparseArray<List<PLVSABeautyOptionVO>> options = new SparseArray<>(3);
    private final SparseArray<PLVSABeautyOptionVO> lastSelectedOptionMap = new SparseArray<>(3);
    private final MutableLiveData<PLVSABeautyOptionVO> currentSelectedOptionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> currentEnableStateLiveData = new MutableLiveData<>();

    private PLVSABeautyOptionAdapter.OnSelectedListener onSelectedListener;
    private int currentItemViewType = ItemType.TYPE_BEAUTY;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adapter方法重写">

    @NonNull
    @Override
    public PLVSAAbsBeautyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
        return PLVSAAbsBeautyViewHolder.Factory.create(viewGroup, itemType);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVSAAbsBeautyViewHolder beautyViewHolder, int index) {
        beautyViewHolder.bind(getCurrentOptionList().get(index));
    }

    @Override
    public int getItemCount() {
        return getNullableOrDefault(new PLVSugarUtil.Supplier<Integer>() {
            @Override
            public Integer get() {
                return options.get(currentItemViewType).size();
            }
        }, 0);
    }

    @Override
    public int getItemViewType(int position) {
        return currentItemViewType;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setBeautyOptionList(@ItemType int itemType, List<? extends IPLVBeautyOption> beautyOptions) {
        options.put(itemType, wrapBeautyOptionList(beautyOptions));
        updateLastSelectFilterOptionOnListChanged(itemType);
    }

    public void changeOptionList(@ItemType int itemType) {
        this.currentItemViewType = itemType;
        notifyDataSetChanged();
        changeCurrentSelectedOption();
    }

    public void setEnableState(boolean enable) {
        if (currentEnableStateLiveData.getValue() != null && currentEnableStateLiveData.getValue() == enable) {
            return;
        }
        currentEnableStateLiveData.postValue(enable);
    }

    public void setLastSelectFilterOption(IPLVBeautyOption option) {
        if (option == null) {
            return;
        }
        if (lastSelectedOptionMap.get(ItemType.TYPE_FILTER) != null) {
            return;
        }
        if (options.get(ItemType.TYPE_FILTER) == null) {
            return;
        }
        for (final PLVSABeautyOptionVO filterOptionVO : options.get(ItemType.TYPE_FILTER)) {
            if (option.equals(filterOptionVO.getOption())) {
                lastSelectedOptionMap.put(ItemType.TYPE_FILTER, filterOptionVO);
                break;
            }
        }
        if (currentItemViewType == ItemType.TYPE_FILTER) {
            changeCurrentSelectedOption();
        }
    }

    public void updateCurrentSelectedOption() {
        changeCurrentSelectedOption();
    }

    public void onReset() {
        lastSelectedOptionMap.put(ItemType.TYPE_FILTER, null);
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    private List<PLVSABeautyOptionVO> getCurrentOptionList() {
        return options.get(currentItemViewType);
    }

    private List<PLVSABeautyOptionVO> wrapBeautyOptionList(final List<? extends IPLVBeautyOption> beautyOptions) {
        return transformList(new ArrayList<>(beautyOptions), new PLVSugarUtil.Function<IPLVBeautyOption, PLVSABeautyOptionVO>() {
            @Override
            public PLVSABeautyOptionVO apply(IPLVBeautyOption option) {
                return new PLVSABeautyOptionVO(option)
                        .setOptionItemIndex(beautyOptions.indexOf(option))
                        .setOptionGroupSize(beautyOptions.size())
                        .setCurrentSelectedOptionVOLiveData(currentSelectedOptionLiveData)
                        .setCurrentEnableStateLiveData(currentEnableStateLiveData)
                        .setOnSelectedListener(PLVSABeautyOptionAdapter.this);
            }
        });
    }

    private void changeCurrentSelectedOption() {
        final PLVSABeautyOptionVO lastSelectedOption = lastSelectedOptionMap.get(currentItemViewType);
        if (lastSelectedOption != null && getCurrentOptionList().contains(lastSelectedOption)) {
            this.onSelected(lastSelectedOption);
            return;
        }

        final List<PLVSABeautyOptionVO> currentOptionList = getCurrentOptionList();
        if (currentOptionList == null || currentOptionList.isEmpty()) {
            this.onSelected(null);
            return;
        }
        this.onSelected(currentOptionList.get(0));
    }

    private void updateLastSelectFilterOptionOnListChanged(int itemType) {
        if (lastSelectedOptionMap.get(itemType) == null) {
            return;
        }
        final IPLVBeautyOption beautyOption = lastSelectedOptionMap.get(itemType).getOption();
        for (final PLVSABeautyOptionVO filterOptionVO : options.get(itemType)) {
            if (beautyOption.equals(filterOptionVO.getOption())) {
                lastSelectedOptionMap.put(itemType, filterOptionVO);
                break;
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回调接口实现">

    @Override
    public void onSelected(@Nullable PLVSABeautyOptionVO optionVO) {
        lastSelectedOptionMap.put(currentItemViewType, optionVO);
        currentSelectedOptionLiveData.postValue(optionVO);
        if (this.onSelectedListener != null) {
            this.onSelectedListener.onSelected(optionVO == null ? null : optionVO.getOption());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部接口定义">

    public interface OnSelectedListener {
        void onSelected(@Nullable IPLVBeautyOption beautyOption);
    }

    @IntDef({
            ItemType.TYPE_BEAUTY,
            ItemType.TYPE_FILTER,
            ItemType.TYPE_DETAIL,
    })
    public @interface ItemType {
        int TYPE_BEAUTY = 1;
        int TYPE_FILTER = 2;
        int TYPE_DETAIL = 3;
    }

    // </editor-fold>
}
