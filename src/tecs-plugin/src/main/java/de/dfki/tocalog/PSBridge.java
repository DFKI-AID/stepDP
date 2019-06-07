package de.dfki.tocalog;

import de.dfki.tecs.net.Event;
import de.dfki.tecs.ps.PSClient;
import de.dfki.tecs.ps.PSFactory;
import org.apache.thrift.TBase;

import java.util.*;

/**
 */
public class PSBridge  {
    private final String uri;
    private final PSClient psc;
    private final Object monitor;
    private Thread updateThread;
    private final Callback callback;

    interface Callback {
        void onEvent(Event event);
    }

    public PSBridge(Builder builder) {
        this.uri = builder.uri;
        this.monitor = builder.monitor;
        psc = PSFactory.create(uri);
        for(String sub : builder.subscriptions) {
            psc.subscribe(sub);
        }
        this.callback = builder.callback;
    }

    protected void start() {
        psc.open();
        updateThread = new Thread(() -> {
            psc.recv().ifPresent(tecsEvent -> {
                callback.onEvent(tecsEvent);
            });
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void publish(String topic, TBase payload) {
        psc.publish(topic, payload);
    }

    public static Builder build(Callback callback) {
        return new Builder(callback);
    }


    public static class Builder {
        private String uri = "tecs://de.dfki.step.dialog@localhost:9000/ps";
        private Set<String> subscriptions = new HashSet<>();
        private Object monitor;
        private final Callback callback;

        public Builder(Callback callback) {
            this.callback = callback;
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
