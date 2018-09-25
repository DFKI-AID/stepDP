package de.dfki.tocalog.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public class EventDelayer implements Runnable {
    private EventEngine ee;
    private ArrayList<DelayedEvent> delayedEvents = new ArrayList<>();

    private static class DelayedEvent {
        public Event event;
        public long when;
    }

    public EventDelayer(EventEngine ee) {
        this.ee = ee;
    }

    @Override
    public void run() {
        List<DelayedEvent> ready = new ArrayList<>();
        long next, now;
        while(!Thread.currentThread().isInterrupted()) {
            now = System.currentTimeMillis();
            next = Long.MAX_VALUE;
            synchronized (this) {
                if(delayedEvents.isEmpty()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                    continue;
                }

                Iterator<DelayedEvent> iter = delayedEvents.iterator();
                while(iter.hasNext()) {
                    DelayedEvent de = iter.next();
                    next = Math.min(next, de.when);
                    if(de.when <= now) {
                        ready.add(de);
                        iter.remove();
                    }
                }


                if(ready.isEmpty()) {
                    try {
                        this.wait(Math.max(1, next - now));
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
            }

            for(DelayedEvent de : delayedEvents) {
                ee.submit(de.event);
            }

            ready.clear();
        }
    }

    public synchronized void submit(Event event, long delayMs) {
        DelayedEvent de = new DelayedEvent();
        de.event = event;
        de.when = System.currentTimeMillis() + delayMs;
        delayedEvents.add(de);
    }

}
