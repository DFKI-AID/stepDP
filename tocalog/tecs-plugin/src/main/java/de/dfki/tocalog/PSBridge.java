package de.dfki.tocalog;

import de.dfki.tecs.ps.PSClient;
import de.dfki.tecs.ps.PSFactory;
import de.dfki.tocalog.framework.Event;
import de.dfki.tocalog.framework.EventEngine;
import de.dfki.tocalog.framework.ProjectManager;
import de.dfki.tocalog.framework.DialogComponent;
import org.apache.thrift.TBase;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class PSBridge implements DialogComponent {
    private final String uri;
    private PSClient psc;
    private Thread updateThread;

    public PSBridge(Builder builder) {
        this.uri = builder.uri;
        psc = PSFactory.create(uri);
        for(String sub : builder.subscriptions) {
            psc.subscribe(sub);
        }
    }

    @Override
    public void init(ProjectManager dialogCore) {
        EventEngine ee = dialogCore.getEventEngine();

        psc.open();
        updateThread = new Thread(() -> {
            psc.recv().ifPresent(tecsEvent -> {
                Event dialogEvent = Event.build(tecsEvent)
                        .setSource(this.getClass().getSimpleName())
                        .build();
                ee.submit(dialogEvent);
            });
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    @Override
    public void onEvent(EventEngine engine, Event event) {
    }

    public void publish(String target, String topic, TBase payload) {
        psc.publish(target, topic, payload);
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        private String uri = "tecs://dialog@localhost:9000/ps";
        private Set<String> subscriptions = new HashSet<>();

        public Builder() {
        }

        public Builder setUri(String uri) {
            this.uri = uri;
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
