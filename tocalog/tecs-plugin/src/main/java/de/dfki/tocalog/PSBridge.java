package de.dfki.tocalog;

import de.dfki.tecs.ps.PSClient;
import de.dfki.tecs.ps.PSFactory;
import de.dfki.tocalog.core.*;
import org.apache.thrift.TBase;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 */
public class PSBridge implements EventProducer {
    private final String uri;
    private final PSClient psc;
    private final Object monitor;
    private Thread updateThread;
    private Queue<Event> queue = new ConcurrentLinkedQueue<>();

    public PSBridge(Builder builder) {
        this.uri = builder.uri;
        this.monitor = builder.monitor;
        psc = PSFactory.create(uri);
        for(String sub : builder.subscriptions) {
            psc.subscribe(sub);
        }
    }

    protected void start() {
        psc.open();
        updateThread = new Thread(() -> {
            psc.recv().ifPresent(tecsEvent -> {
                Event dialogEvent = Event.create(tecsEvent)
                        .setSource(this.getClass().getSimpleName())
                        .build();
                queue.add(dialogEvent);
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            });
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void publish(String topic, TBase payload) {
        psc.publish(topic, payload);
    }

    public static Builder build() {
        return new Builder();
    }

    @Override
    public Optional<Event> nextEvent() {
        return Optional.ofNullable(queue.poll());
    }

    public static class Builder {
        private String uri = "tecs://dialog@localhost:9000/ps";
        private Set<String> subscriptions = new HashSet<>();
        private Object monitor;

        public Builder() {
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setMonitor(Object monitor) {
            this.monitor = monitor;
            return this;
        }

        public Builder subscribe(String sub) {
            subscriptions.add(sub);
            return this;
        }

        public PSBridge build() {
            return new PSBridge(this);
        }
    }
}
