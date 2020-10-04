package org.igutech.utils.events;

public class RepeatedTimerEvent {
    private Callback callback;
    private long time;
    private boolean hasFired = false;
    private long initalTime;

    public RepeatedTimerEvent(long time, Callback m) {
        callback = m;
        this.time = time;
        initalTime = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long t) {
        time = t;
    }

    public long getInitalTime() {
        return initalTime;
    }

    public boolean isHasFired() {
        return hasFired;
    }

    public void fire() {
        hasFired = true;
        callback.call();
    }


}
