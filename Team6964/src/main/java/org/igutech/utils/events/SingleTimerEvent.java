package org.igutech.utils.events;

public class SingleTimerEvent {
    private Callback callback;
    private int time;
    private boolean hasFired = false;

    public SingleTimerEvent(int time, Callback m) {
        callback = m;
        this.time = time;
    }

    public int getTime() {
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
