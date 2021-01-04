package org.igutech.teleop.Modules;


import org.igutech.teleop.Service;
import org.igutech.utils.events.Callback;
import org.igutech.utils.events.RepeatedTimerEvent;
import org.igutech.utils.events.SingleTimerEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TimerService extends Service {
    private ArrayList<SingleTimerEvent> events = new ArrayList<>();
    private ArrayList<RepeatedTimerEvent> repeatedTimerEvents = new ArrayList<>();
    private long startTime;
    private Queue<SingleTimerEvent> singleEventQueue = new LinkedList<>();
    public TimerService() {
        super("TimerService");
    }

    @Override
    public void start() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void loop() {
        ArrayList<SingleTimerEvent> eventsToBeRemoved = new ArrayList<>();
        long current = System.currentTimeMillis() - startTime;
//        for(SingleTimerEvent i:singleEventQueue){
//            events.add(i);
//        }
        if(singleEventQueue.peek()!=null && events.size()==0){
            events.add(singleEventQueue.peek());
        }
        singleEventQueue.clear();

        for (SingleTimerEvent e : events) {
            if(!e.hasFired() && System.currentTimeMillis()>=e.getTime()){
                e.fire();
                eventsToBeRemoved.add(e);
            }
        }

        for(SingleTimerEvent e:eventsToBeRemoved){
            events.remove(e);
        }
        eventsToBeRemoved.clear();

        for(RepeatedTimerEvent e:repeatedTimerEvents){
            if(current>=e.getTime()){
                e.fire();
                e.setTime(current+e.getInitalTime());
            }
        }
    }

    public void registerSingleTimerEvent(int time, Callback m) {
        System.out.println("event registered");
        singleEventQueue.add(new SingleTimerEvent(time, m));
    }

    public void registerRepeatedTimerEvents(int time, Callback m) {
        repeatedTimerEvents.add(new RepeatedTimerEvent(time, m));
    }

}
