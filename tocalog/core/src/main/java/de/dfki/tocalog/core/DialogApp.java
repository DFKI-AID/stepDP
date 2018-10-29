package de.dfki.tocalog.core;

import a.Resolution;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.OutputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

/**
 * Entry point for the dialog application. See DialogApp::Builder for creating a new instance.
 * Implements the main plus loop.
 */
public class DialogApp implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DialogApp.class);
    private static final long INPUT_TIMEOUT = 20000L;
    private final KnowledgeBase knowledgeBase;
    private final List<EventProducer> eventProducers;
    private final List<SensorComponent> sensorComponents;
    private final List<Store> stores;
    private final List<InputComponent> inputComponents;
    private final List<DialogComponent> dialogComponents;
    private final Object monitor;
    private Inputs inputs = new Inputs();
    private final DialogCoordinator dialogCoordinator;


    protected DialogApp(Builder builder) {
        this.knowledgeBase = builder.knowledgeBase;
        this.dialogCoordinator = builder.dialogCoordinator;
        this.inputComponents = Collections.unmodifiableList(builder.inputComponents);
        this.sensorComponents = builder.sensorComponents;
        this.dialogComponents = builder.dialogComponents;
        this.stores = builder.stores;
        this.eventProducers = builder.eventProducers;
        this.monitor = builder.monitor;
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public Optional<InputComponent> getInputComponent(Predicate<InputComponent> filter) {
        return inputComponents.stream()
                .filter(filter)
                .findAny();
    }

    public Object getNotifyObject() {
        return monitor;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            update();
        }
    }

    public void update() {
        // get new events and sleep if no new data is available
        List<Event> events = new ArrayList<>();
        eventProducers.forEach(ep -> ep.nextEvent().ifPresent(e -> events.add(e)));
        if (events.isEmpty()) {
            synchronized (monitor) {
                try {
                    monitor.wait(50);
                } catch (InterruptedException e) {
                    log.error("interrupted {}", this.getClass(), e.toString());
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            return;
        }

        inputs.removeOld(INPUT_TIMEOUT);

        for (Event event : events) {
            //udpate sensor data
            sensorComponents.forEach(
                    sc -> sc.process(event).ifPresent(
                            si -> stores.forEach(s -> s.process(si))
                    )
            );

            //process inputs
            for (InputComponent ic : inputComponents) {
                Collection<Input> tmpInputs = ic.process(event);
                if (tmpInputs.isEmpty()) {
                    continue;
                }

                inputs.add(tmpInputs);
            }
        }


        final List<DialogFunction> dfs = new ArrayList<>();
        dialogComponents.forEach(dc -> dc.process(inputs).ifPresent(df -> dfs.add(df)));

        final List<DialogFunction> coordinatedDfs = dialogCoordinator.coordinate(dfs);
        coordinatedDfs.forEach(df -> {
            df.consumedInputs().forEach(ci -> inputs.consume(ci, df.getOrigin()));
            df.run();
        });
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        protected KnowledgeBase knowledgeBase;
        private List<EventProducer> eventProducers = new ArrayList<>();
        private List<SensorComponent> sensorComponents = new ArrayList();
        private List<Store> stores = new ArrayList<>();
        protected List<InputComponent> inputComponents = new ArrayList<>();
        protected Imp imp;
        private Resolution resolution;
        private Object monitor = new Object();
        private List<DialogComponent> dialogComponents = new ArrayList<>();
        private DialogCoordinator dialogCoordinator = new SimpleDialogCoordinator();


        protected Builder() {

        }

        public Builder addEventProducer(EventProducer ep) {
            this.eventProducers.add(ep);
            return this;
        }

        public Builder addInputComponent(InputComponent component) {
            inputComponents.add(component);
            return this;
        }

        public Builder addOutputComponent(OutputComponent component) {
            imp.addOutputComponent(component);
            return this;
        }

        public Builder addDialogComponent(DialogComponent component) {
            dialogComponents.add(component);
            return this;
        }

        public KnowledgeBase getKnowledgeBase() {
            return knowledgeBase;
        }

        public Builder setKnowledgeBase(KnowledgeBase knowledgeBase) {
            this.knowledgeBase = knowledgeBase;
            return this;
        }

        public Builder setDialogCoordinator(DialogCoordinator dialogCoordinator) {
            this.dialogCoordinator = dialogCoordinator;
            return this;
        }

        public Imp getImp() {
            return imp;
        }

        public Builder setImp(Imp imp) {
            this.imp = imp;
            return this;
        }

        public Resolution getResolution() {
            return resolution;
        }

        public void setResolution(Resolution resolution) {
            this.resolution = resolution;
        }

        public DialogApp build() {
            DialogApp dc = new DialogApp(this);
            return dc;
        }

        public Object getMonitor() {
            return monitor;
        }
    }
}
