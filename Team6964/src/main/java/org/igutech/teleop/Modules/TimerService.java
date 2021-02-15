package org.igutech.teleop.Modules;


import org.igutech.teleop.Service;
import org.igutech.utils.events.Callback;
import org.igutech.utils.events.RepeatedTimerEvent;
import org.igutech.utils.events.SingleTimerEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TimerService extends Service {
    private long startTime;

    private ArrayList<RepeatedTimerEvent> repeatedTimerEvents = new ArrayList<>();
    private Queue<SingleTimerEvent> singleEventQueue = new LinkedList<>();
    private ArrayList<SingleTimerEvent> singleEvents = new ArrayList<>();

    private Set<SingleTimerEvent> uniqueEventSet = new HashSet<>();
    private Queue<SingleTimerEvent> uniqueEventsToBeAdded = new LinkedList<>();

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

        for (SingleTimerEvent e : uniqueEventsToBeAdded) {
            uniqueEventSet.add(e);
        }
        uniqueEventsToBeAdded.clear();

        Iterator<SingleTimerEvent> iterator = uniqueEventSet.iterator();
        while (iterator.hasNext()) {

            SingleTimerEvent e = iterator.next();
            if (System.currentTimeMillis() >= e.getTime()) {
                System.out.println(e.getName()+" Firing");
                try {
                    e.fire();
                } catch (Exception error) {
                    error.printStackTrace();
                }
                iterator.remove();
            }
        }

        for (RepeatedTimerEvent e : repeatedTimerEvents) {
            if (current >= e.getTime()) {
                e.fire();
                e.setTime(current + e.getInitalTime());
            }
        }
    }

    public void registerUniqueTimerEvent(long time, String name, Callback m) {

        uniqueEventsToBeAdded.add(new SingleTimerEvent(time, name, m));
    }

    public void registerSingleTimerEvent(int time, Callback m) {
        SingleTimerEvent event = new SingleTimerEvent(time, m);
        if (singleEvents.size() > 0) {
            event.setTime(singleEvents.get(singleEvents.size() - 1).getTime() + time);
        }
        singleEventQueue.add(event);
    }

    public void registerRepeatedTimerEvents(int time, Callback m) {
        repeatedTimerEvents.add(new RepeatedTimerEvent(time, m));
    }


}
