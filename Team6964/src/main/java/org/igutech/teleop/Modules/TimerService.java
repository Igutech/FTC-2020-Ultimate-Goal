package org.igutech.teleop.Modules;


import org.igutech.teleop.Service;
import org.igutech.utils.events.Callback;
import org.igutech.utils.events.RepeatedTimerEvent;
import org.igutech.utils.events.SingleTimerEvent;

import java.util.ArrayList;

public class TimerService extends Service {
    private ArrayList<SingleTimerEvent> events = new ArrayList<>();
    private ArrayList<RepeatedTimerEvent> repeatedTimerEvents = new ArrayList<>();

    private long startTime;

    public TimerService() {
        super("TimerService");
    }

    @Override
    public void start() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void loop() {
        long current = System.currentTimeMillis() - startTime;

        for (SingleTimerEvent e : events) {
            if(!e.hasFired() && current>=e.getTime()){
                e.fire();
            }
        }
        for(RepeatedTimerEvent e:repeatedTimerEvents){
            if(current>=e.getTime()){
                e.fire();
                e.setTime(current+e.getInitalTime());
            }
        }
    }

    public void registerSingleTimerEvent(int time, Callback m) {
        events.add(new SingleTimerEvent(time, m));
    }

    public void registerRepeatedTimerEvents(int time, Callback m) {
        repeatedTimerEvents.add(new RepeatedTimerEvent(time, m));
    }




}
