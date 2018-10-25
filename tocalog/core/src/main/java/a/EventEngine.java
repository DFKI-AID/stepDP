package a;

import de.dfki.tocalog.core.Event;

import java.util.*;

/**
 * Event system, that notifies all registered listeners if an event was submitted.
 * It can also execute arbitrary functions in the same thread.
 * TODO rename EventQueue / EventSystem
 */
public class EventEngine implements Runnable {
    private Queue<Runnable> tasks = new ArrayDeque<>();
    private List<Listener> listeners;


    protected EventEngine(Builder builder) {
        //copy from set to list to avoid duplicates and keep a fixed order during runtime
        this.listeners = new ArrayList<>(builder.listeners);
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Runnable task;
            synchronized (tasks) {
                task = tasks.poll();
                if (task == null) {
                    try {
                        tasks.wait();
                    } catch (InterruptedException e) {
                    } finally {
                        continue;
                    }
                }
            }
            task.run();
        }
    }

    public void submit(Runnable task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
    }



    public void submit(Event event) {
        this.submit(() -> publishEvent(event));
    }

    private void publishEvent(Event event) {
        listeners.forEach(l -> l.onEvent(this, event));
    }

    public static Builder build() {
        return new Builder();
    }

    public interface Listener {
        void onEvent(EventEngine engine, Event event);
    }

    public static class Builder {
        private Set<Listener> listeners = new HashSet<>();

        public Builder addListener(Listener listener) {
            listeners.add(listener);
            return this;
        }

        public EventEngine build() {
            return new EventEngine(this);
        }
    }


}
