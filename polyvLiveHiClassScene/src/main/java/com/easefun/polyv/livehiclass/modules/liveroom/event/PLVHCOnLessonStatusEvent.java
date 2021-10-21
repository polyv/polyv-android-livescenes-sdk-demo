package com.easefun.polyv.livehiclass.modules.liveroom.event;

import com.plv.foundationsdk.rx.PLVRxBus;

import io.reactivex.Observable;

/**
 * @author suhongtao
 */
public class PLVHCOnLessonStatusEvent {

    private final boolean start;
    private final boolean teacherType;
    private final boolean hasNextClass;

    public PLVHCOnLessonStatusEvent(boolean isStart) {
        this(isStart, false, false);
    }

    public PLVHCOnLessonStatusEvent(boolean isStart, boolean isTeacherType, boolean hasNextClass) {
        this.start = isStart;
        this.teacherType = isTeacherType;
        this.hasNextClass = hasNextClass;
    }

    public boolean isStart() {
        return start;
    }

    public boolean isTeacherType() {
        return teacherType;
    }

    public boolean hasNextClass() {
        return hasNextClass;
    }

    public static class Bus {

        public static void post(PLVHCOnLessonStatusEvent event) {
            PLVRxBus.get().post(event);
        }

        public static Observable<PLVHCOnLessonStatusEvent> observe() {
            return PLVRxBus.get().toObservable(PLVHCOnLessonStatusEvent.class);
        }

    }

}
