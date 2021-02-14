package org.igutech.utils.events;

public class SingleTimerEvent {
    private Callback callback;
    private long time;
    private boolean hasFired = false;
    private String name;

    public SingleTimerEvent(int time, Callback m) {
        callback = m;
        this.time = time + System.currentTimeMillis();
    }

    public SingleTimerEvent(long time, String name, Callback m) {
        callback = m;
        this.time = time + System.currentTimeMillis();
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean hasFired() {
        return hasFired;
    }

    public void fire() {
        hasFired = true;
        callback.call();
    }

    public String getName() {
        return name;
    }

    public Callback getCallback() {
        return callback;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SingleTimerEvent)) {
            return false;
        }
        return name.equals(((SingleTimerEvent) obj).name);
    }
}
