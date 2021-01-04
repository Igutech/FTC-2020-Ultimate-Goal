package org.igutech.utils.events;

public class SingleTimerEvent {
    private Callback callback;
    private long time;
    private boolean hasFired = false;

    public SingleTimerEvent(int time, Callback m) {
        callback = m;
        this.time = time+System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public boolean hasFired() {
        return hasFired;
    }

    public void fire() {
        hasFired = true;
        callback.call();
    }
}
