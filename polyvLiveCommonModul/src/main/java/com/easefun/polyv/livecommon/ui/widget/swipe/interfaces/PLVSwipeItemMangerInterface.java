package com.easefun.polyv.livecommon.ui.widget.swipe.interfaces;



import com.easefun.polyv.livecommon.ui.widget.swipe.PLVSwipeLayout;
import com.easefun.polyv.livecommon.ui.widget.swipe.util.PLVAttributes;

import java.util.List;

public interface PLVSwipeItemMangerInterface {

    void openItem(int position);

    void closeItem(int position);

    void closeAllExcept(PLVSwipeLayout layout);
    
    void closeAllItems();

    List<Integer> getOpenItems();

    List<PLVSwipeLayout> getOpenLayouts();

    void removeShownLayouts(PLVSwipeLayout layout);

    boolean isOpen(int position);

    PLVAttributes.Mode getMode();

    void setMode(PLVAttributes.Mode mode);
}
