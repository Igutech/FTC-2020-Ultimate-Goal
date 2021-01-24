package org.igutech.teleop.Modules;


import org.igutech.utils.events.Callback;
import org.igutech.utils.events.RepeatedTimerEvent;
import org.igutech.utils.events.SingleTimerEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TimerService {
    private ArrayList<SingleTimerEvent> uniqueEvents = new ArrayList<>();
    private ArrayList<RepeatedTimerEvent> repeatedTimerEvents = new ArrayList<>();
    private long startTime;
    private Queue<SingleTimerEvent> uniqueEventQueue = new LinkedList<>();


    private Queue<SingleTimerEvent> singleEventQueue = new LinkedList<>();
    private ArrayList<SingleTimerEvent> events = new ArrayList<>();



    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void loop() {
        long current = System.currentTimeMillis() - startTime;

        ArrayList<SingleTimerEvent> eventsToBeRemoved = new ArrayList<>();
        for (SingleTimerEvent i : singleEventQueue) {
            events.add(i);
        }
        singleEventQueue.clear();

        for (SingleTimerEvent e : events) {
            if (!e.hasFired() && System.currentTimeMillis() >= e.getTime()) {
                e.fire();
            }
        }

        if (uniqueEventQueue.peek() != null && uniqueEvents.size() == 0) {
            uniqueEvents.add(uniqueEventQueue.peek());
        }
        uniqueEventQueue.clear();

        for (SingleTimerEvent uniqueEvent : uniqueEvents) {
            if (!uniqueEvent.hasFired() && System.currentTimeMillis() >= uniqueEvent.getTime()) {
                uniqueEvent.fire();
                eventsToBeRemoved.add(uniqueEvent);
            }
        }

        for (SingleTimerEvent e : eventsToBeRemoved) {
            uniqueEvents.remove(e);
        }
        eventsToBeRemoved.clear();

        for (RepeatedTimerEvent e : repeatedTimerEvents) {
            if (current >= e.getTime()) {
                e.fire();
                e.setTime(current + e.getInitalTime());
            }
        }
    }

    public void registerUniqueTimerEvent(int time, Callback m) {
        uniqueEventQueue.add(new SingleTimerEvent(time, m));
    }

    public void registerSingleTimerEvent(int time, Callback m) {
        SingleTimerEvent event = new SingleTimerEvent(time, m);
        if(events.size()>0){
            event.setTime(events.get(events.size()-1).getTime()+time);
        }
        singleEventQueue.add(event);
    }

    public void registerRepeatedTimerEvents(int time, Callback m) {
        repeatedTimerEvents.add(new RepeatedTimerEvent(time, m));
    }


}